package com.gryphpoem.game.zw.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName MergServerInfo.java
 * @Description
 * @author QiuKun
 * @date 2018年9月7日
 */
public class MergServerInfo implements CheckLegal {

    // 合服之后主服的信息
    private List<MasterServer> masterServerList;

    // 需要合服的数据
    private List<ServerDBInfo> serverDBinfos;

    // 合服的版本
    private int version;

    public List<MasterServer> getMasterServerList() {
        return masterServerList;
    }

    public void setMasterServerList(List<MasterServer> masterServerList) {
        this.masterServerList = masterServerList;
    }

    public List<ServerDBInfo> getServerDBinfos() {
        return serverDBinfos;
    }

    public void setServerDBinfos(List<ServerDBInfo> serverDBinfos) {
        this.serverDBinfos = serverDBinfos;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "MergServerInfo [masterServerList=" + masterServerList + ", serverDBinfos=" + serverDBinfos
                + ", version=" + version + "]";
    }

    @Override
    public boolean checkLegal() {
        if (masterServerList != null && !masterServerList.isEmpty() && serverDBinfos != null
                && !serverDBinfos.isEmpty()) {
            // 去重计算需要合服的数量
            int dbCnt = (int) serverDBinfos.stream().filter(Java8Utils.distinctByKey(s -> {
                return s.getDbIp() + s.getDbPort() + s.getDbName();
            })).count();
            if (dbCnt != serverDBinfos.size()) {
                LogUtil.error("合服的DB连接有重复的");
                return false;
            }
            // 需要合服服务器id
            Set<Integer> serverIdSet = serverDBinfos.stream().map(sDBinfo -> sDBinfo.getServerId())
                    .collect(Collectors.toSet());
            if (serverIdSet.size() != serverDBinfos.size()) {
                LogUtil.error("合服的serverId有重复的");
                return false;
            }

            if (masterServerList.size() > serverIdSet.size()) {
                LogUtil.error("选择的主服务器大于了 serverDBinfos的数量");
                return false;
            }
            int masterServerCnt = (int) masterServerList.stream().mapToInt(ms -> ms.getServerId()).distinct().count();
            if (masterServerCnt != masterServerList.size()) {
                LogUtil.error("选择的主服务器有重复数据");
                return false;
            }
            // 检查masterServerList的正确
            for (MasterServer ms : masterServerList) {
                if (!serverIdSet.contains(ms.getServerId())) {
                    LogUtil.error("选择的主服务器在serverDBinfos不存在: serverId:", ms.getServerId());
                    return false;
                }
                if (!ms.checkLegal()) {
                    LogUtil.error("主服务器数据不合法 serverId:", ms.getServerId());
                    return false;
                }
            }
            // 检查ElementServer 数据正确
            // 拆分数据
            List<ElementServer> elemServerSplitList = serverDBinfos.stream()
                    .flatMap(sDBinfo -> Stream.of(1, 2, 3).map(camp -> new ElementServer(sDBinfo.getServerId(), camp)))
                    .collect(Collectors.toList());
            // 平铺MasterServer的compose信息
            List<ElementServer> elemServerByMaster = masterServerList.stream()
                    .flatMap(ms -> ms.getComposeServer().stream()).collect(Collectors.toList());
            if (elemServerSplitList.size() != elemServerByMaster.size()) {
                LogUtil.debug("选择合成的ElementServer服务器有误");
                return false;
            }
            for (ElementServer es : elemServerSplitList) {
                if (!elemServerByMaster.contains(es)) {
                    LogUtil.debug("选择合服的服务器数据有误 serverId:", es.getServerId(), ", camp:", es.getCamp());
                }
            }
            return true;
        }
        return false;
    }

    public static void main(String[] args) {

        // 初始化需要合服的数据库

        ServerDBInfo serverDbinfo101 = new ServerDBInfo();
        serverDbinfo101.setServerId(101);
        serverDbinfo101.setDbIp("10.66.183.106");
        serverDbinfo101.setDbUser("root");
        serverDbinfo101.setDbPasswd("jeC02GfP");
        serverDbinfo101.setDbName("honor_merge_101");
        serverDbinfo101.setDbPort(3306);

//        ServerDBInfo serverDbinfo102 = new ServerDBInfo();
//        serverDbinfo102.setServerId(102);
//        serverDbinfo102.setDbIp("10.66.238.48");
//        serverDbinfo102.setDbUser("root");
//        serverDbinfo102.setDbPasswd("jeC02GfP");
//        serverDbinfo102.setDbName("honor_102");
//        serverDbinfo102.setDbPort(3306);
//
//        ServerDBInfo serverDbinfo103 = new ServerDBInfo();
//        serverDbinfo103.setServerId(103);
//        serverDbinfo103.setDbIp("10.66.238.48");
//        serverDbinfo103.setDbUser("root");
//        serverDbinfo103.setDbPasswd("jeC02GfP");
//        serverDbinfo103.setDbName("honor_103");
//        serverDbinfo103.setDbPort(3306);
//
//        ServerDBInfo serverDbinfo104 = new ServerDBInfo();
//        serverDbinfo104.setServerId(104);
//        serverDbinfo104.setDbIp("10.66.238.48");
//        serverDbinfo104.setDbUser("root");
//        serverDbinfo104.setDbPasswd("jeC02GfP");
//        serverDbinfo104.setDbName("honor_104");
//        serverDbinfo104.setDbPort(3306);

        ServerDBInfo serverDbinfo105 = new ServerDBInfo();
        serverDbinfo105.setServerId(105);
        serverDbinfo105.setDbIp("10.66.183.106");
        serverDbinfo105.setDbUser("root");
        serverDbinfo105.setDbPasswd("jeC02GfP");
        serverDbinfo105.setDbName("honor_merge_105");
        serverDbinfo105.setDbPort(3306);

        ServerDBInfo serverDbinfo106 = new ServerDBInfo();
        serverDbinfo106.setServerId(106);
        serverDbinfo106.setDbIp("10.66.183.106");
        serverDbinfo106.setDbUser("root");
        serverDbinfo106.setDbPasswd("jeC02GfP");
        serverDbinfo106.setDbName("honor_merge_106");
        serverDbinfo106.setDbPort(3306);

//        ServerDBInfo serverDbinfo107 = new ServerDBInfo();
//        serverDbinfo107.setServerId(107);
//        serverDbinfo107.setDbIp("10.66.255.27");
//        serverDbinfo107.setDbUser("root");
//        serverDbinfo107.setDbPasswd("jeC02GfP");
//        serverDbinfo107.setDbName("honor_107");
//        serverDbinfo107.setDbPort(3306);
//
//        ServerDBInfo serverDbinfo108 = new ServerDBInfo();
//        serverDbinfo108.setServerId(108);
//        serverDbinfo108.setDbIp("10.66.237.70");
//        serverDbinfo108.setDbUser("root");
//        serverDbinfo108.setDbPasswd("jeC02GfP");
//        serverDbinfo108.setDbName("honor_108");
//        serverDbinfo108.setDbPort(3306);
//
//        ServerDBInfo serverDbinfo109 = new ServerDBInfo();
//        serverDbinfo109.setServerId(109);
//        serverDbinfo109.setDbIp("10.66.237.70");
//        serverDbinfo109.setDbUser("root");
//        serverDbinfo109.setDbPasswd("jeC02GfP");
//        serverDbinfo109.setDbName("honor_109");
//        serverDbinfo109.setDbPort(3306);
//
//        ServerDBInfo serverDbinfo110 = new ServerDBInfo();
//        serverDbinfo110.setServerId(110);
//        serverDbinfo110.setDbIp("10.66.237.70");
//        serverDbinfo110.setDbUser("root");
//        serverDbinfo110.setDbPasswd("jeC02GfP");
//        serverDbinfo110.setDbName("honor_110");
//        serverDbinfo110.setDbPort(3306);

        List<ServerDBInfo> dbInfoList = new ArrayList<>();
        dbInfoList.add(serverDbinfo101);
//        dbInfoList.add(serverDbinfo102);
//        dbInfoList.add(serverDbinfo103);
//        dbInfoList.add(serverDbinfo104);
        dbInfoList.add(serverDbinfo105);
        dbInfoList.add(serverDbinfo106);
//        dbInfoList.add(serverDbinfo107);
//        dbInfoList.add(serverDbinfo108);
//        dbInfoList.add(serverDbinfo109);
//        dbInfoList.add(serverDbinfo110);

        // 合服之后的数据
        // 生成元素
        /*
        LinkedList<ElementServer> elementServers = (LinkedList<ElementServer>) dbInfoList.stream()
                .flatMap(sDBinfo -> Stream.of(1, 2, 3).map(camp -> new ElementServer(sDBinfo.getServerId(), camp)))
                .collect(Collectors.toCollection(LinkedList::new));
        Collections.shuffle(elementServers);
        
        final int msCnt = 2; // RandomHelper.randomInSize(dbInfoList.size()) + 1; // 合服后主服的数量
        // 前面几个选择主服
        List<MasterServer> masterServerList = dbInfoList.stream().map(db -> {
            MasterServer master = new MasterServer();
            master.setServerId(db.getServerId());
            master.setComposeServer(new ArrayList<>());
            master.getComposeServer().add(findCamp(elementServers, 1));
            master.getComposeServer().add(findCamp(elementServers, 2));
            master.getComposeServer().add(findCamp(elementServers, 3));
            return master;
        }).limit(msCnt).collect(Collectors.toList());
        
        while (!elementServers.isEmpty()) {
            ElementServer poll = elementServers.poll();
            int index = RandomHelper.randomInSize(msCnt);
            masterServerList.get(index).getComposeServer().add(poll);
        }
        */
        // 线上临时数据 start
        List<MasterServer> masterServerList = new ArrayList<>();
        // 主服
        MasterServer ms101 = new MasterServer();
        ms101.setServerId(101);
        List<ElementServer> cs101 = new ArrayList<>();
        ms101.setComposeServer(cs101);
        cs101.add(new ElementServer(101, 1));
        cs101.add(new ElementServer(105, 2));
        cs101.add(new ElementServer(106, 3));
//        cs101.add(new ElementServer(101, 1));
//        cs101.add(new ElementServer(102, 1));
//        cs101.add(new ElementServer(107, 1));
//        cs101.add(new ElementServer(101, 2));
//        cs101.add(new ElementServer(107, 2));
//        cs101.add(new ElementServer(109, 2));
//        cs101.add(new ElementServer(101, 3));
//        cs101.add(new ElementServer(107, 3));
//        cs101.add(new ElementServer(110, 3));

        MasterServer ms105 = new MasterServer();
        ms105.setServerId(105);
        List<ElementServer> cs105 = new ArrayList<>();
        ms105.setComposeServer(cs105);
        cs105.add(new ElementServer(101, 2));
        cs105.add(new ElementServer(105, 3));
        cs105.add(new ElementServer(106, 1));
        
//        cs105.add(new ElementServer(104, 1));
//        cs105.add(new ElementServer(105, 1));
//        cs105.add(new ElementServer(108, 1));
//        cs105.add(new ElementServer(109, 1));
//        cs105.add(new ElementServer(102, 2));
//        cs105.add(new ElementServer(105, 2));
//        cs105.add(new ElementServer(110, 2));
//        cs105.add(new ElementServer(102, 3));
//        cs105.add(new ElementServer(105, 3));
//        cs105.add(new ElementServer(106, 3));

        MasterServer ms106 = new MasterServer();
        ms106.setServerId(106);
        List<ElementServer> cs106 = new ArrayList<>();
        ms106.setComposeServer(cs106);
//        cs106.add(new ElementServer(103, 1));
//        cs106.add(new ElementServer(106, 1));
//        cs106.add(new ElementServer(110, 1));
//        cs106.add(new ElementServer(103, 2));
//        cs106.add(new ElementServer(104, 2));
//        cs106.add(new ElementServer(106, 2));
//        cs106.add(new ElementServer(108, 2));
//        cs106.add(new ElementServer(103, 3));
//        cs106.add(new ElementServer(104, 3));
//        cs106.add(new ElementServer(108, 3));
//        cs106.add(new ElementServer(109, 3));
        
        cs106.add(new ElementServer(105, 1));
        cs106.add(new ElementServer(106, 2));
        cs106.add(new ElementServer(101, 3));

        masterServerList.add(ms101);
        masterServerList.add(ms105);
        masterServerList.add(ms106);
        // 线上临时数据 end

        MergServerInfo mInfo = new MergServerInfo();
        mInfo.setServerDBinfos(dbInfoList);
        mInfo.setMasterServerList(masterServerList);

        String jsonString = JSON.toJSONString(mInfo);
        System.out.println(jsonString);

        MergServerInfo mm = JSON.parseObject(jsonString, MergServerInfo.class);
        boolean checkLegal = mm.checkLegal();
        System.out.println(checkLegal ? "数据正确" : "数据有误");
        System.exit(0);
    }

    public static ElementServer findCamp(LinkedList<ElementServer> elementServers, int camp) {
        ElementServer ees = elementServers.stream().filter(es -> es.getCamp() == camp).findFirst().orElse(null);
        if (ees != null) {
            elementServers.remove(ees);
            return ees;
        }
        return null;
    }

}
