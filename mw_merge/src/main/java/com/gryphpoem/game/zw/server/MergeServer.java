package com.gryphpoem.game.zw.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gryphpoem.game.zw.constant.MergeConstant;
import com.gryphpoem.game.zw.core.Server;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.FileUtil;
import com.gryphpoem.game.zw.core.util.HttpUtils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.datasource.DataSourceHelper;
import com.gryphpoem.game.zw.datasource.DynamicDataSource;
import com.gryphpoem.game.zw.domain.*;
import com.gryphpoem.game.zw.resource.common.ServerConfig;
import com.gryphpoem.game.zw.resource.dao.impl.p.MergeDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.StaticParamDao;
import com.gryphpoem.game.zw.resource.domain.p.StaticParam;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.LoadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName MergeServer.java
 * @Description 合服服务
 * @date 2018年9月6日
 */
public class MergeServer extends Server {

    private static MergeServer ins;

    public static ApplicationContext ac;
    // 合服的信息
    private MergServerInfo mergServerInfo;

    // 执行的线程合服的线程池
    private ExecutorService executorService;

    // 每个主服的配置 key:serverId, val配置列表
    private Map<Integer, MasterServerSetting> mServerCfg = new HashMap<>();

    private MergeServer() {
        super("mergeServer");
    }

    public static MergeServer getIns() {
        if (ins == null) {
            ins = new MergeServer();
        }
        return ins;
    }

    @Override
    public void run() {
        super.run();
        long startTime = System.currentTimeMillis();
        try {
            init();
            // 开始执行合服逻辑
            startMergeLogic();
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error("初始化失败 退出", e);
            System.exit(1);
        }
        LogUtil.start("合服程序执行总耗时:" + (System.currentTimeMillis() - startTime) + " 毫秒");
        System.exit(0);
    }

    /**
     * 合服逻辑
     *
     * @throws InterruptedException
     */
    private void startMergeLogic() {
        // 每个主服分配一个线程进行执行
        int threadSize = mergServerInfo.getMasterServerList().size();
        this.executorService = Executors.newFixedThreadPool(threadSize);
        List<MasterServerWork> workList = mergServerInfo.getMasterServerList().stream()
                .map(masterServerInfo -> new MasterServerWork(masterServerInfo)).collect(Collectors.toList());
        List<Future<WorkResult>> futureList = null;
        try {
            // 等待其他任务都支线完成
            futureList = executorService.invokeAll(workList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (futureList != null) {
            futureList.forEach(f -> {
                try {
                    WorkResult workResult = f.get();
                    if (workResult.getThrowable() == null) {
                        LogUtil.start("------------主服" + workResult.getServerId() + "合服成功------------");
                    } else {
                        LogUtil.start("------------主服" + workResult.getServerId() + "合服失败------------");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }
        // 关闭线程
        executorService.shutdown();
        LogUtil.start("-----------------合服程序执行完毕,关闭程序---------------------");

    }

    private void init() throws Exception {
        // 初始化spring
        ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        DataResource.ac = ac;
        // 读取合服配置文件
        loadMergeConf();
        // 检测合服的配置文件正确性
        checkMergConf();
        // 初始化连数据库连接源
        initDataSource();
        // // 加载游戏数据
        loadGameConf();

    }

    private void loadGameConf() {
        LoadService loadService = ac.getBean(LoadService.class);
        loadService.loadAll();
        LogUtil.start("**********配置表数据加载完成**********");
    }

    /**
     * 创建数据源
     *
     * @throws Exception
     */
    private void initDataSource() throws Exception {
        // 创建查询数据源
        createSrcDataSource();
        // 创建目标数据源
        createDstDataSource();
    }

    /**
     * 创建目标数据源
     *
     * @throws Exception
     */
    private void createDstDataSource() throws Exception {
        List<MasterServer> masterServerList = mergServerInfo.getMasterServerList();
        // 创建数据库
        for (MasterServer ms : masterServerList) {
            createDstDB(ms.getServerId());
            LogUtil.start("创建数据库 serverId:" + ms.getServerId() + " 完成");
        }
        LogUtil.start("-------创建所有数据库完成  createDB end--------");
    }

    /**
     * 创建查询数据源
     *
     * @throws PropertyVetoException
     */
    private void createSrcDataSource() throws PropertyVetoException {
        DynamicDataSource dyDataSource = ac.getBean(DynamicDataSource.class);
        for (ServerDBInfo serverDbinfo : mergServerInfo.getServerDBinfos()) {
            DataSource dataC3p0Source = DataSourceHelper.createDataC3p0Source(serverDbinfo);
            String datasourceKey = MergeConstant.getSrcDatasourceKey(serverDbinfo.getServerId());
            dyDataSource.addDataSocurce(datasourceKey, dataC3p0Source);
            LogUtil.start("创建查询源的 DataSource :" + datasourceKey + " :成功");
        }
    }

    private void addServerCfg(int serverId, List<StaticParam> settingList) throws Exception {
        MasterServerSetting mss = new MasterServerSetting(serverId);
        Map<String, String> paramMap = new Hashtable<>();
        for (StaticParam param : settingList) {
            paramMap.put(param.getParamName(), param.getParamValue());
        }
        String accountUrl = paramMap.get("accountServerUrl");
        try {
            Map<String, String> hm = new HashMap<>();
            hm.put("request_type", "sync_http");
            hm.put("command", "detailserverinfo");
            String result = HttpUtils.sendPost(accountUrl, String.valueOf(serverId), "utf-8", hm, 5 * 1000);
            JSONObject jo2 = JSONObject.parseObject(result);
            mss.setOpenTime(jo2.getDate("openTime"));
            mss.setActMold(jo2.getInteger("actMold"));
        } catch (Exception e) {
            throw new MwException("serverId:" + serverId + " ,从账号服获取服务区配置信息出错, accountUrl: " + accountUrl, e);

        }
        if (mss.getActMold() != 0 && mss.getOpenTime() != null) {
            mServerCfg.put(serverId, mss);
        } else {
            throw new MwException("serverId:" + serverId + " ,主服的 s_server_setting表的 openTime和actMold字段合服时必须填写");
        }

    }

    /**
     * 创建数据库并copy表结构
     *
     * @param serverId
     * @throws PropertyVetoException
     */
    private void createDstDB(int serverId) throws Exception {
        ServerConfig serverConfig = ac.getBean(ServerConfig.class);
        MergeDao mergeDao = ac.getBean(MergeDao.class);
        StaticParamDao staticParamDao = ac.getBean(StaticParamDao.class);
        // 切换到源目标数据源
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        List<StaticParam> settingList = staticParamDao.selectStaticParams(); // setting的配置
        addServerCfg(serverId, settingList);
        List<String> tables = mergeDao.showTables(); // 所有表名
        List<Map<String, String>> cTable = new ArrayList<>(); // 建表的DDL
        for (String t : tables) {
            Map<String, String> tSql = mergeDao.showCreateTable(t);
            cTable.add(tSql);
        }

        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.MASTER_DATASOURCE_KEY);
        String dbName = MergeConstant.getDstDatasourceKey(serverId);
        mergeDao.dropDb(dbName);// 删除原来的库
        mergeDao.createGameDb(dbName);// 创建数据库
        LogUtil.start("创建数据库  " + dbName + " 成功");
        // 创建数据源
        String jdbcUrl = getDbUrl(serverConfig.getJdbcUrl(), dbName);
        LogUtil.start("创建链接源: jdbcUrl=" + jdbcUrl);
        DataSource dstDataSource = DataSourceHelper.createDataC3p0Source(jdbcUrl, serverConfig.getUser(),
                serverConfig.getPassword());
        DynamicDataSource dyDataSource = ac.getBean(DynamicDataSource.class);
        dyDataSource.addDataSocurce(dbName, dstDataSource);
        // 切换到该数据源
        DynamicDataSource.DataSourceContextHolder.setDBType(dbName);
        // 创建表
        for (Map<String, String> c : cTable) {
            String ddlSql = c.get(MergeConstant.CREATE_TABLE_DDL_COLUMN);
            String tableName = c.get(MergeConstant.CREATE_TABLE_NAME_COLUMN);
            // String dropSql = "DROP TABLE IF EXISTS `" + tableName + "`;";
            // mergeDao.execSql(dropSql);
            mergeDao.execSql(ddlSql);
            // 拷贝数setting中的数据
            if (MergeConstant.SERVER_SETTING_TABLE_NAME.equals(tableName)) {
                for (StaticParam s : settingList) {
                    staticParamDao.insertStaticParam(s);
                }
            }
        }
    }

    /**
     * 获取本机数据库链接源地址
     *
     * @param oldJdbcUrl
     * @param dbName
     * @return
     * @throws MwException
     */
    private static String getDbUrl(String oldJdbcUrl, String dbName) throws MwException {
        // String
        // regEx="((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)";
        // String regEx = "(rm-.*?.mysql.rds.aliyuncs.com):(\\d+)";
        String regEx = "//([^/]*):(\\d+)?/";
        // String regEx = "(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)";
        Pattern pattern = Pattern.compile(regEx);
        Matcher m = pattern.matcher(oldJdbcUrl);
        if (m.find()) {
            String ip = m.group(1);
            String port = m.group(2);
            return "jdbc:mysql://" + ip + ":" + port + "/" + dbName + MergeConstant.MYSQL_PARAMS;
        }
        throw new MwException("jdbcUrl生成失败");
    }

    public static void main(String[] args) {
        try {
            // System.out.println(getDbUrl("jdbc\\:mysql\\://rm-rj9r3ca3sgv973sx3.mysql.rds.aliyuncs.com\\:3306/empire_7?useUnicode=true&characterEncoding=utf-8",
            // System.out.println(getDbUrl("jdbc:mysql://rm-rj94nu1u3944bwed5.mysql.rds.aliyuncs.com:3306/empire_1?useUnicode=true&characterEncoding=utf-8",
            System.out.println(getDbUrl("jdbc\\:mysql\\://rm-rj9r3ca3sgv973sx3.mysql.rds.aliyuncs.com\\:3307/empire_7?useUnicode=true&characterEncoding=utf-8",
            // // System.out.println(getDbUrl("jdbc\\:mysql\\://rm-rj9r3ca3sgv973sx3.mysql.rds.aliyuncs.com\\:3306/empire_merge_20200514_7?useUnicode=true&characterEncoding=utf-8",
            //         //         "m"));
            //
            // System.out.println(getDbUrl("jdbc:mysql://127.0.0.1:3306/modernwar_1",
                    "empire_merge_20200511_3"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取配置文件
     *
     * @throws IOException
     */
    private void loadMergeConf() throws IOException {
        Path path = Paths.get("mergeServerList.json");
        LogUtil.start("配置路径: " + path.toAbsolutePath().toString());
        String mergeJson = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        LogUtil.start(mergeJson);
        this.mergServerInfo = JSON.parseObject(mergeJson, MergServerInfo.class);
        LogUtil.start("读取合服配置文件成功 ... ");
    }

    /**
     * 检查配置文件是
     *
     * @throws MwException
     */
    private void checkMergConf() throws MwException {
        if (mergServerInfo == null) {
            throw new MwException("合服配置文件初始化失败....");
        }
        if (!mergServerInfo.checkLegal()) {
            throw new MwException("合服配置不合法");
        }
        LogUtil.start("检查合服配置文件正确完成 ");
        // 打印SQL文件
        printRelationTableSql();

    }

    /**
     * 打印u_server_relation的sql数据
     */
    private void printRelationTableSql() {
        // INSERT INTO `honor_account`.`u_server_relation`(`id`, `server_id_from`, `camp`, `server_id_master`,
        // `date_create`, `version`)
        // VALUES (3, 102, 1, 101, '2018-10-22 20:31:24', 0);
        LogUtil.start("u_server_relation 打印sql语句开始 ");
        StringBuilder sb = new StringBuilder();
        List<MasterServer> masterServerList = mergServerInfo.getMasterServerList();
        String createDataStr = DateHelper.displayNowDateTime();
        for (MasterServer ms : masterServerList) {
            for (ElementServer es : ms.getComposeServer()) {
                if (es.getServerId() != ms.getServerId()) {
                    // 打印sql语句
                    String sql = "INSERT INTO `u_server_relation`(`server_id_from`, `camp`, `server_id_master`,`date_create`, `version`) VALUES (%s, %s, %s, '%s', %s);";
                    String server_id_from = es.getServerId() + "";
                    String camp = es.getCamp() + "";
                    String server_id_master = ms.getServerId() + "";
                    String date_create = createDataStr;
                    String version = mergServerInfo.getVersion() + "";
                    String fromatSql = String.format(sql, server_id_from, camp, server_id_master, date_create, version);
                    LogUtil.start(fromatSql);
                    sb.append(fromatSql);
                    sb.append(System.getProperty("line.separator"));
                }
            }
        }
        // 写文件
        FileUtil.wirteFile("u_server_relation.sql", sb.toString());
        LogUtil.start("生成了 u_server_relation.sql 文件");
        LogUtil.start("u_server_relation 打印sql语句结束 ");
    }

    @Override
    public String getGameType() {
        return super.name;
    }

    public Map<Integer, MasterServerSetting> getmServerCfg() {
        return mServerCfg;
    }

    @Override
    protected void stop() {
        // System.out.println("----------退出的回调-------------");
    }

}
