package com.gryphpoem.game.zw.resource.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gryphpoem.game.zw.core.util.HttpUtils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.ServerType;
import com.gryphpoem.game.zw.resource.dao.impl.p.StaticParamDao;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.StaticParam;
import com.gryphpoem.game.zw.resource.domain.p.TargetServerCamp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

public class ServerSetting {

    public static final String CONFIG_MODE = "configMode";

    private List<TargetServerCamp> allowJoinServerIdCampList;

    private String allowJoinServerCampStr;
    @Autowired
    private StaticParamDao staticParamDao;

    // @Value("${accountServerUrl}")
    private String accountServerUrl;

    private String payServerUrl; // 支付服务器地址

    // @Value("${testMode}")
    private String testMode;

    // @Value("${openWhiteName}")
    private String openWhiteName;

    // @Value("${cryptMsg}")
    private String cryptMsg;

    // @Value("${msgCryptCode}")
    private String msgCryptCode;

    // @Value("${convertUrl}")
    private String convertUrl;

    // @Value("${pay}")
    private String pay;

    // @Value("${serverId}")
    private String serverId;

    private String clientPort;

    private String httpPort;

    private String openTime;

    private String serverName;

    private String environment; // 运行环境; test 表示测试环境, release 表示线上环境

    private int serverID = 1;

    private int actMoldId = 1;

    private String zkUrl; // zk的地址

    // 禁止创角
    private int forbidCreateRole;

    public String getCryptMsg() {
        return cryptMsg;
    }

    public void setCryptMsg(String cryptMsg) {
        this.cryptMsg = cryptMsg;
    }

    public String getMsgCryptCode() {
        return msgCryptCode;
    }

    public void setMsgCryptCode(String msgCryptCode) {
        this.msgCryptCode = msgCryptCode;
    }

    public void setConvertUrl(String convertUrl) {
        this.convertUrl = convertUrl;
    }

    public String getConvertUrl() {
        return convertUrl;
    }

    public String getAccountServerUrl() {
        return accountServerUrl;
    }

    public String getClientPort() {
        return clientPort;
    }

    public void setClientPort(String clientPort) {
        this.clientPort = clientPort;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public String getOpenTime() {
        LogUtil.gm("openTime:", openTime);
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isTestMode() {
        return "yes".equals(testMode);
    }

    public boolean isOpenWhiteName() {
        return "yes".equals(openWhiteName);
    }

    public boolean isCryptMsg() {
        return "yes".equals(cryptMsg);
    }

    public boolean isOpenPay() {
        return "yes".equals(pay);
    }

    public String getEnvironment() {
        return environment;
    }

    public List<TargetServerCamp> getAllowJoinServerIdCampList() {
        return allowJoinServerIdCampList;
    }

    public String getAllowJoinServerCampStr() {
        return allowJoinServerCampStr;
    }

    public boolean isForbidCreateRole() {
        return forbidCreateRole == 1;
    }

    public void setForbidCreateRole(int forbidCreateRole) {
        this.forbidCreateRole = forbidCreateRole;
    }

    @PostConstruct
    public void init() {
        if (ServerType.selfServerType == ServerType.SERVER_TYPE_CORSS) { // 跨服不需要加载
            return;
        }
        List<StaticParam> params = staticParamDao.selectStaticParams();
        Map<String, String> paramMap = new Hashtable<>();
        for (StaticParam param : params) {
            paramMap.put(param.getParamName(), param.getParamValue());
        }
        serverId = paramMap.get("serverId");
        this.zkUrl = paramMap.get("zkUrl");
        if (this.zkUrl != null) {
            this.zkUrl = this.zkUrl.trim();
        }
        String configMode = paramMap.get(CONFIG_MODE);
        if ("db".equals(configMode)) {
            // 判断是合服还是
            if (isMergeProcess()) {
                this.initWithDbOld(paramMap);
            } else {
                accountServerUrl = paramMap.get("accountServerUrl");
                try {
                    Map<String, Object> sm = getServerConfig(accountServerUrl, Integer.parseInt(serverId));
                    // 初始化配置
                    this.initWithDb(sm);
                    // 活动模板和开服时间读自己的u_server_config
                    // this.openTime = paramMap.get("openTime");
                    // this.actMoldId = Integer.valueOf(paramMap.get("actMold"));
                } catch (Exception e) {
                    LogUtil.error("==============================出事啦!====================从账号服获取服务区配置信息出错,accountUrl:"
                            + accountServerUrl + " sid:" + serverId, e);
                    // 服务区配置拿不到，直接启动不成功
                    System.exit(1);
                }
            }
        }
    }

    /**
     * 是否是合服程序
     *
     * @return
     */
    private boolean isMergeProcess() {
        return ServerType.selfServerType == ServerType.SERVER_TYPE_MERGE;
    }

    /**
     * 从账号服获取服务器的配置信息
     *
     * @param accountUrl
     * @param sid
     * @return
     */
    private Map<String, Object> getServerConfig(String accountUrl, int sid) {
        Map<String, Object> serverConfig = new HashMap<>();
        Map<String, String> hm = new HashMap<>();
        hm.put("request_type", "sync_http");
        hm.put("command", "detailserverinfo");
        String rs = HttpUtils.sendPost(accountUrl, String.valueOf(sid), "utf-8", hm, 5 * 1000);
        JSONObject jo2 = JSONObject.parseObject(rs);
        serverConfig.put("id", jo2.get("id"));
        serverConfig.put("statusHot", jo2.get("statusHot"));
        serverConfig.put("serverStatus", jo2.get("serverStatus"));
        serverConfig.put("statusNew", jo2.get("statusNew"));
        serverConfig.put("cryptMsg", jo2.get("cryptMsg"));
        serverConfig.put("ip", jo2.get("ip"));
        serverConfig.put("payServerUrl", jo2.get("payServerUrl"));
        serverConfig.put("pay", jo2.get("pay"));
        serverConfig.put("serverName", jo2.get("serverName"));
        serverConfig.put("openWhiteName", jo2.get("openWhiteName"));
        serverConfig.put("environment", jo2.get("environment"));
        serverConfig.put("urlGameHttp", jo2.get("urlGameHttp"));
        serverConfig.put("urlGameSocket", jo2.get("urlGameSocket"));
        serverConfig.put("msgCryptCode", jo2.get("msgCryptCode"));
        serverConfig.put("convertUrl", jo2.get("convertUrl"));
        serverConfig.put("testMode", jo2.get("testMode"));
        serverConfig.put("mergeStatus", jo2.get("mergeStatus"));
        serverConfig.put("openTime", jo2.get("openTime"));
        serverConfig.put("actMold", jo2.get("actMold"));
        serverConfig.put("forbidCreateRole", jo2.get("forbidCreateRole"));

        // 获取该区服包含的 区服阵营信息, 如账号服返回空值,说明这个服务器Id不合法,不让启动
        JSONArray jsonArray = jo2.getJSONArray("allowServerIdCamp");
        if (jsonArray == null) {
            throw new IllegalArgumentException("服务器id不合法,不能启动 sid:" + sid);
        }
        List<TargetServerCamp> allowJoinList = JSONObject.parseArray(jsonArray.toString(), TargetServerCamp.class);
        if (CheckNull.isEmpty(allowJoinList)) {
            throw new IllegalArgumentException("不包含任何 , 服务器id不合法,不能启动 sid:" + sid);
        }
        this.allowJoinServerIdCampList = Collections.unmodifiableList(allowJoinList);
        StringBuilder sb = new StringBuilder();
        for (TargetServerCamp tsc : this.allowJoinServerIdCampList) {
            LogUtil.start("允许进入的   serverId:" + tsc.getOriginServerId() + ", camp:" + tsc.getCamp());
            sb.append(tsc.getOriginServerId()).append("_").append(tsc.getCamp()).append(", ");
        }
        this.allowJoinServerCampStr = sb.toString();
        return serverConfig;
    }

    private void initWithDbOld(Map<String, String> params) {
        LogUtil.start("game server config initWithDb!!!");
        accountServerUrl = params.get("accountServerUrl");
        testMode = params.get("testMode");
        openWhiteName = params.get("openWhiteName");
        cryptMsg = params.get("cryptMsg");
        msgCryptCode = params.get("msgCryptCode");
        convertUrl = params.get("convertUrl");
        pay = params.get("pay");
        serverId = params.get("serverId");
        clientPort = params.get("clientPort");
        httpPort = params.get("httpPort");
        openTime = Optional.ofNullable(params.get("openTime")).orElse("2018-04-29 10:00:00");
        serverName = params.get("serverName");
        setServerID(Integer.valueOf(serverId));
        setActMoldId(Integer.valueOf(params.get("actMold")));
        // LogUtil.start("clientPort:" + clientPort);
        payServerUrl = params.get("payServerUrl");
        environment = Optional.ofNullable(params.get("environment")).orElse("test");
    }

    /**
     * 除了区服id和account地址和zk地址，其他配置均从账号服读取
     *
     * @param params
     */
    private void initWithDb(Map<String, Object> params) {
        LogUtil.start("game server config initWithDb!!!");
        testMode = (String) params.get("testMode");
        openWhiteName = (String) params.get("openWhiteName");
        cryptMsg = (String) params.get("cryptMsg");
        msgCryptCode = (String) params.get("msgCryptCode");
        convertUrl = (String) params.get("convertUrl");
        pay = (String) params.get("pay");
        String urlGameSocket = (String) params.get("urlGameSocket");
        String[] ss = urlGameSocket.split(":");
        clientPort = ss[1];
        String urlHttpSocket = (String) params.get("urlGameHttp");
        String[] sh = urlHttpSocket.split(":");
        httpPort = sh[1].split("/")[0];
        Date ot = new Date();
        ot.setTime((Long) (params.get("openTime")));
        openTime = DateHelper.formatDateTime(ot, DateHelper.format1);
        serverName = (String) params.get("serverName");
        setServerID(Integer.valueOf(serverId));
        setActMoldId((int) params.get("actMold"));
        forbidCreateRole = (int) params.get("forbidCreateRole");
        LogUtil.start("clientPort:" + clientPort);
        LogUtil.start("httpPort:" + httpPort);

        payServerUrl = (String) params.get("payServerUrl");
        environment = (String) Optional.ofNullable(params.get("environment")).orElse("test");

    }

    public boolean forbidByWhiteName(Account account) {
        if (isOpenWhiteName() && account.getWhiteName() == 0) {
            return true;
        }

        return false;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public int getActMoldId() {
        return actMoldId;
    }

    public void setActMoldId(int actMoldId) {
        this.actMoldId = actMoldId;
    }

    public String getPayServerUrl() {
        return payServerUrl;
    }

    public String getZkUrl() {
        return zkUrl;
    }

    public String getServerId() {
        return serverId;
    }

    /**
     * 开服的第几天
     *
     * @param nowDate
     * @return
     */
    public int getOpenServerDay(Date nowDate) {
        return DateHelper.dayiy(DateHelper.parseDate(openTime), nowDate);
    }

    /**
     * 开服时间
     *
     * @return
     */
    public Date getOpenServerDate() {
        return DateHelper.parseDate(openTime);
    }
}
