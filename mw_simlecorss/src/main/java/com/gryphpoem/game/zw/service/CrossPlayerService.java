package com.gryphpoem.game.zw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.crosssimple.constant.SimpleCrossConstant;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.executor.ExcutorQueueType;
import com.gryphpoem.game.zw.executor.ExcutorType;
import com.gryphpoem.game.zw.mgr.ExecutorPoolMgr;
import com.gryphpoem.game.zw.mgr.FortressMgr;
import com.gryphpoem.game.zw.mgr.PlayerMgr;
import com.gryphpoem.game.zw.mgr.SessionMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.player.CrossHero;
import com.gryphpoem.game.zw.model.player.HeroModel;
import com.gryphpoem.game.zw.model.player.LordModel;
import com.gryphpoem.game.zw.model.player.PlayerModelType;
import com.gryphpoem.game.zw.pb.BasePb.Base.Builder;
import com.gryphpoem.game.zw.pb.CommonPb.CrossHeroPb;
import com.gryphpoem.game.zw.pb.CommonPb.CrossPlayerPb;
import com.gryphpoem.game.zw.pb.CrossPb.ChoiceHeroRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRs;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossFort;
import com.gryphpoem.game.zw.task.AbstractTask;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName CrossPlayerService.java
 * @Description 处理跨服的玩家逻辑
 * @author QiuKun
 * @date 2019年5月13日
 */
@Service
public class CrossPlayerService {

    @Autowired
    private PlayerMgr playerMgr;
    @Autowired
    private SessionMgr sessionMgr;
    @Autowired
    private FortressMgr fortressMgr;

    public void loginPlayerDispatch(int serverId, long lordId, CrossLoginRq req) {
        int opType = req.getOpType(); // 1 登陆跨服 2 退出跨服 3 玩家下线 4 信息同步
        switch (opType) {
            case SimpleCrossConstant.OP_TYPE_LOGIN:
                login(serverId, lordId, req);
                break;
            case SimpleCrossConstant.OP_TYPE_CLOSE:
                close(serverId, lordId, req);
                break;
            case SimpleCrossConstant.OP_TYPE_OFFLINE:
                offline(serverId, lordId, req);
                break;
            case SimpleCrossConstant.OP_TYPE_SYNCINFO:
                syncinfo(serverId, lordId, req);
                break;
        }
    }

    private void login(int serverId, long lordId, CrossLoginRq req) {
        // 创建玩家
        if (!playerMgr.containsPlayer(lordId)) {
            CrossPlayer crossPlayer = new CrossPlayer(lordId, serverId);
            // 数据填充
            CrossPlayerPb playerPb = req.getPlayer();
            LordModel lordModel = (LordModel) crossPlayer.getModel(PlayerModelType.LORD_MODEL);

            lordModel.setLordPb(playerPb.getCrossLord());
            crossPlayer.focus();
            playerMgr.addPlayer(crossPlayer); // 添加玩家
        }
        // 回消息
        EnterCrossRs.Builder builder = EnterCrossRs.newBuilder();
        Builder okBase = PbMsgUtil.okBase(EnterCrossRs.EXT_FIELD_NUMBER, lordId, EnterCrossRs.ext, builder.build());
        sessionMgr.sendMsg(okBase.build(), serverId);
    }

    /**
     * 关闭跨服界面
     * 
     * @param serverId
     * @param lordId
     * @param req
     */
    private void close(int serverId, long lordId, CrossLoginRq req) {
        CrossPlayer player = playerMgr.getPlayer(lordId);
        if (player != null) {
            player.unFocus();
        }
        EnterCrossRs.Builder builder = EnterCrossRs.newBuilder();
        Builder okBase = PbMsgUtil.okBase(EnterCrossRs.EXT_FIELD_NUMBER, lordId, EnterCrossRs.ext, builder.build());
        sessionMgr.sendMsg(okBase.build(), serverId);
    }

    /**
     * 
     * @param serverId
     * @param lordId
     * @param req
     */
    private void offline(int serverId, long lordId, CrossLoginRq req) {
        CrossPlayer player = playerMgr.getPlayer(lordId);
        if (player != null) {
            LogUtil.debug("玩家下线 lordId:", lordId, ", serverId:", player.getMainServerId());
            player.offline(); // 离线
        }
    }

    /**
     * 同步玩家信息
     * 
     * @param serverId
     * @param lordId
     * @param req
     */
    private void syncinfo(int serverId, long lordId, CrossLoginRq req) {
    }

    /**
     * 退出跨服消息
     * 
     */
    public void sendLogout(CrossPlayer crossPlayer) {
        CrossLoginRs.Builder builder = CrossLoginRs.newBuilder();
        builder.setOpType(SimpleCrossConstant.OP_TYPE_LOGOUT);
        Builder okBase = PbMsgUtil.okBase(CrossLoginRs.EXT_FIELD_NUMBER, crossPlayer.getLordId(), CrossLoginRs.ext,
                builder.build());
        sessionMgr.sendMsg(okBase.build(), crossPlayer.getMainServerId());
    }

    /**
     * 选择将领加入
     * 
     * @param player
     * @param req
     */
    public void choiceHero(CrossPlayer player, ChoiceHeroRq req) {
        // 大本营的堡垒
        StaticCrossFort campFort = StaticCrossDataMgr.getCampFort(player.getCamp());
        if (campFort == null) {
            LogUtil.error("未找到大本营配置 camp:", player.getCamp());
            return;
        }
        int fortId = campFort.getId();
        HeroModel heroModel = player.getHeroModel();
        List<CrossHeroPb> herosList = req.getHerosList();
        Map<Integer, CrossHero> heros = heroModel.getHeros();

        List<CrossHero> newAddList = new ArrayList<>(4); // 新加入的将
        // 将将领添加到玩家身上
        for (CrossHeroPb heroPb : herosList) {
            if (!heros.containsKey(heroPb.getHeroId())) {
                CrossHero c = CrossHero.newInstance(heroPb, fortId);
                heros.put(heroPb.getHeroId(), c);
                newAddList.add(c);
            } else {
                heros.get(heroPb.getHeroId()).refreshData(heroPb);
            }
        }
        // 加入到堡垒中去
        fortressMgr.joinCrossHeroFortess(newAddList, fortId, player.getCamp());

        // 返回结果给客户端
        ChoiceHeroJoinRs.Builder builder = ChoiceHeroJoinRs.newBuilder();
        builder.addAllHero(heros.values().stream().map(CrossHero::toFortHeroPb).collect(Collectors.toList()));
        PbMsgUtil.sendOkMsgToPlayer(player, ChoiceHeroJoinRs.EXT_FIELD_NUMBER, ChoiceHeroJoinRs.ext, builder.build());
    }

    /**
     * 停服时的将领的处理
     */
    public void stopSeverPlayerHeroProcess() {
        ExecutorPoolMgr.getIns().addTask(ExcutorType.LOGIC, ExcutorQueueType.LOGIC_MAIN, new AbstractTask() {
            @Override
            public void work() {
                playerMgr.getPlayerMap().values().forEach(crossPlayer -> {
                    for (CrossHero crossHero : crossPlayer.getHeroModel().getHeros().values()) {
                        heroTick(crossPlayer, crossHero);
                    }
                    crossPlayer.getHeroModel().getHeros().clear();
                });
            }
        });
    }

    private void heroTick(CrossPlayer crossPlayer, CrossHero crossHero) {
        crossHero.setState(0);
        crossHero.setRevivalTime(0);
        crossHero.setRevivalCnt(0);
        SyncFortHeroRs.Builder builder = SyncFortHeroRs.newBuilder();
        builder.setHero(crossHero.toFortHeroPb());
        PbMsgUtil.sendOkMsgToPlayer(crossPlayer, SyncFortHeroRs.EXT_FIELD_NUMBER, SyncFortHeroRs.ext, builder.build());
    }

}
