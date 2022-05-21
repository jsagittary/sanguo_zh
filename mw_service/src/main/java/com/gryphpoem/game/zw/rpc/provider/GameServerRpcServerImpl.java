package com.gryphpoem.game.zw.rpc.provider;

import com.gryphpoem.cross.chat.dto.CrossChat;
import com.gryphpoem.cross.chat.dto.CrossRoleChat;
import com.gryphpoem.cross.chat.dto.CrossSystemChat;
import com.gryphpoem.cross.constants.GameServerConst;
import com.gryphpoem.cross.gameserver.GameServerRpcService;
import com.gryphpoem.cross.gameserver.dto.GameServerInfo;
import com.gryphpoem.cross.gameserver.dto.PlayerShow;
import com.gryphpoem.cross.manager.GameServerManagerService;
import com.gryphpoem.game.zw.core.util.JVMUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.SyncChatRs;
import com.gryphpoem.game.zw.resource.common.ServerConfig;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.NumberUtil;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.config.DubboShutdownHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-10-22 15:14
 */
@Service
public class GameServerRpcServerImpl implements GameServerRpcService {
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private ServerConfig serverConfig;
    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private GameServerManagerService gameServerManagerService;


    @Override
    public Set<GameServerInfo> getGameServerInfo() {
        GameServerInfo serverInfo = new GameServerInfo();
        serverInfo.setServerId(serverSetting.getServerID());
        serverInfo.setServerName(serverSetting.getServerName());
        serverInfo.setIp(NetUtils.getLocalHost());
        serverInfo.setPort(Integer.parseInt(serverSetting.getClientPort()));
        serverInfo.setJdbcKey(getJdbcKey());
        serverInfo.setPid(getPid());
        Set<GameServerInfo> set = new HashSet<>();
        set.add(serverInfo);
        LogUtil.common("当前服务器信息: " + serverInfo);
        return set;
    }

    @Override
    public Map<Integer, Map<String, String>> getGameServerJvmInfo() {
        int playerOnlineCount = playerDataManager.getAllOnlinePlayer().size();
        int playerLoadCount = playerDataManager.getAllPlayer().size();
        Map<String, String> paramMap = new HashMap<>();
        JVMUtil.collectMemoryInfo(paramMap);
        JVMUtil.collectGarbageInfo(paramMap);
        paramMap.put(GameServerConst.ONLINE_COUNT, String.valueOf(playerOnlineCount));
        paramMap.put(GameServerConst.LOAD_COUNT, String.valueOf(playerLoadCount));
        LogUtil.debug("当前jvm信息: " + paramMap);
        Map<Integer, Map<String, String>> serverMap = new HashMap<>();
        serverMap.put(serverSetting.getServerID(), paramMap);
        return serverMap;
    }

    @Override
    public void syncCrossChat(CrossChat crossChat, Collection<Integer> camps) {
        Map<String, Player> onlineMap = playerDataManager.getAllOnlinePlayer();
        SyncChatRs syncChatRs = PbCrossUtil.createSyncChatRs(crossChat);
        BasePb.Base.Builder baseBuilder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext, syncChatRs);
        for (Map.Entry<String, Player> playerEntry : onlineMap.entrySet()) {
            Player player = playerEntry.getValue();
            if (player.ctx != null && player.isLogin && camps.contains(player.getCamp())) {
                MsgDataManager.getIns().add(new Msg(player.ctx, baseBuilder.build(), player.roleId));
            }
        }
    }

    @Override
    public PlayerShow getPlayerShowInfo(long lordId) {
        PlayerShow showInfo = new PlayerShow();
        showInfo.setLordId(lordId);
        Player player = playerDataManager.getPlayer(lordId);
        if (Objects.nonNull(player)) {
            CommonPb.Friend.Builder builder = CommonPb.Friend.newBuilder();
            CommonPb.Man man = PbHelper.createManPbByLord(player);
            builder.setMan(man.toBuilder().setServerId(serverSetting.getServerID()).build());
            for (Hero h : player.getAllOnBattleHeros()) {
                builder.addHero(PbHelper.createFriendAndHeroPb(h, player));
            }
            showInfo.setBytes(builder.build().toByteArray());
        }
        return showInfo;
    }

    private String getJdbcKey() {
        String jdbcUrl = serverConfig.getJdbcUrl();
        String prefix = "jdbc:mysql://";
        if (jdbcUrl.startsWith(prefix)) {
            int idx = jdbcUrl.indexOf("?");
            return jdbcUrl.substring(prefix.length(), idx);
        }
        throw new IllegalArgumentException("jdbcUrl 必须是mysql 配置格式");
    }

    private String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int idx = name.indexOf("@");
        return name.substring(0, idx);
    }

//    public void checkServerAlreadyStart() {
//        try {
//            int serverId = serverSetting.getServerID();
//            String ip = NetUtils.getLocalHost();
//            int port = Integer.parseInt(serverSetting.getClientPort());
//            long startMill = System.currentTimeMillis();
//            CompletableFuture<Collection<GameServerInfo>> completableFuture = gameServerManagerService.registerGameServerStart(serverId, ip, port);
//            Collection<GameServerInfo> findServerList = completableFuture.get(60, TimeUnit.SECONDS);
//            long costSec = (System.currentTimeMillis() - startMill) / NumberUtil.THOUSAND;
//            LogUtil.start("区服ID 检测花费时间: " + costSec + " 秒");
//            boolean findOtherRuntimeStart = false;
//            if (Objects.nonNull(findServerList)) {
//                for (GameServerInfo gsi : findServerList) {
//                    if (!ip.equals(gsi.getIp()) || port != gsi.getPort()) {
//                        findOtherRuntimeStart = true;
//                        LogUtil.error2Sentry(String.format("区服ID: %d, 已经在其它地方启动了, 启动信息: %s", serverId, gsi));
//                    }
//                }
//            }
//            if (findOtherRuntimeStart) {
//                LogUtil.start(String.format("区服ID :%d 唯一性检测失败 程序关闭!!!", serverSetting.getServerID()));
//                stopStart();
//            } else {
//                LogUtil.start(String.format("区服ID :%d 唯一性检测成功", serverSetting.getServerID()));
//            }
//        } catch (Exception e) {
//            LogUtil.error(String.format("区服ID :%d 唯一性检测失败 程序关闭!!!", serverSetting.getServerID()), e);
//            stopStart();
//        }
//    }

    public void checkServerAlreadyStart() {
        long startMill = System.currentTimeMillis();
        try {
            int serverId = serverSetting.getServerID();
            String jdbcKey = getJdbcKey();
            String ip = NetUtils.getLocalHost();
            String pid = getPid();
            CompletableFuture<List<GameServerInfo>> completableFuture = gameServerManagerService.getServerInfo(serverId, jdbcKey);
            List<GameServerInfo> findServerList = completableFuture.get(60, TimeUnit.SECONDS);
            boolean findOtherRuntimeStart = false;
            if (Objects.nonNull(findServerList)) {
                for (GameServerInfo serverInfo : findServerList) {
                    if (!ip.equals(serverInfo.getIp()) || !pid.equals(serverInfo.getPid())) {
                        findOtherRuntimeStart = true;
                        LogUtil.error2Sentry(String.format("区服ID: %d, 已经在其它地方启动了, 启动信息: %s", serverId, serverInfo));
                    }
                }
            }
            if (findOtherRuntimeStart) {
                LogUtil.start(String.format("区服ID :%d 唯一性检测失败 程序关闭!!!", serverSetting.getServerID()));
                stopStart();
            } else {
                LogUtil.start(String.format("区服ID :%d 唯一性检测成功", serverSetting.getServerID()));
            }
        } catch (Exception e) {
            LogUtil.error(String.format("区服ID :%d 唯一性检测失败 程序关闭!!!", serverSetting.getServerID()), e);
            stopStart();
        } finally {
            long costSec = (System.currentTimeMillis() - startMill) / NumberUtil.THOUSAND;
            LogUtil.start("区服ID 检测花费时间: " + costSec + " 秒");
        }
    }

    private void stopStart() {
        try {
            DubboShutdownHook.destroyAll();
        } catch (Exception e) {
            LogUtil.error("", e);
        } finally {
            System.exit(1);
        }
    }

    @Override
    public String executeProtoCmd(long l, String s) {
        return null;
    }

    @Override
    public void shutdownGameServer(String msg) {
        try {
            LogUtil.error("开始关闭游戏服务器: " + msg);
            DubboShutdownHook.destroyAll();
        } catch (Exception e) {
            LogUtil.error("", e);
        } finally {
            System.exit(1);
        }
    }

}
