package com.gryphpoem.game.zw.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.crosssimple.constant.SimpleCrossConstant;
import com.gryphpoem.game.zw.dataMgr.StaticCrossDataMgr;
import com.gryphpoem.game.zw.mgr.FortressMgr;
import com.gryphpoem.game.zw.mgr.RewardMgr;
import com.gryphpoem.game.zw.mgr.SessionMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.fort.Fortress;
import com.gryphpoem.game.zw.model.fort.RoleForce;
import com.gryphpoem.game.zw.model.player.CrossHero;
import com.gryphpoem.game.zw.pb.CommonPb.CrossHeroPb;
import com.gryphpoem.game.zw.pb.CommonPb.FortHeroPb;
import com.gryphpoem.game.zw.pb.CommonPb.FortressPb;
import com.gryphpoem.game.zw.pb.CrossPb.CrossHeroReviveRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossHeroSyncRq;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossFort;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName FortressService.java
 * @Description
 * @author QiuKun
 * @date 2019年5月16日
 */
@Component
public class FortressService {

    @Autowired
    private SessionMgr sessionMgr;
    @Autowired
    private FortressMgr fortressMgr;
    @Autowired
    private RewardMgr rewardMgr;
    @Autowired
    private HeroRevivalService heroRevivalService;
    @Autowired
    private CrossWarService crossWarService;

    /**
     * 获取堡垒信息
     * 
     * @param player
     * @param req
     */
    public void getCrossFort(CrossPlayer player, GetCrossFortRq req) {
        List<FortressPb> fortressPbList = fortressMgr.getFortresses().values().stream().map(Fortress::toFortressPb)
                .collect(Collectors.toList());
        List<FortHeroPb> fortHeroPbList = player.getHeroModel().getHeros().values().stream()
                .map(CrossHero::toFortHeroPb).collect(Collectors.toList());
        GetCrossFortRs.Builder builder = GetCrossFortRs.newBuilder();
        builder.addAllHero(fortHeroPbList);
        builder.addAllFortress(fortressPbList);

        sessionMgr.sendMsg(PbMsgUtil
                .okBase(GetCrossFortRs.EXT_FIELD_NUMBER, player.getLordId(), GetCrossFortRs.ext, builder.build())
                .build(), player.getMainServerId());
    }

    /**
     * 跨服中堡垒将领操作
     * 
     * @param player
     * @param req
     */
    public void opFortHero(CrossPlayer player, OpFortHeroRq req) {
        int opType = req.getOpType();
        switch (opType) {
            case SimpleCrossConstant.HERO_OPERATE_GOTO:// 前往
                goTofortress(player, req);
                break;
            case SimpleCrossConstant.HERO_OPERATE_BACK_COURT:// 回防
                backCourtfortress(player, req);
                break;
            case SimpleCrossConstant.HERO_OPERATE_ATTACK:// 进攻
                attackFortress(player, req);
                break;
            case SimpleCrossConstant.HERO_OPERATE_SNEAK:// 偷袭
                sneakFortress(player, req);
                break;
            case SimpleCrossConstant.HERO_OPERATE_SOLO:// 单挑
                soloFortress(player, req);
                break;
            default:// 错误消息
                PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                        new MwException(GameError.PARAM_ERROR.getCode(), "无效操作 lordId:", player.getLordId()));
                break;
        }
    }

    /**
     * 复活将领
     * 
     * @param player
     * @param req
     */
    public void crossHeroRevive(CrossPlayer player, CrossHeroReviveRq req) {
        int heroId = req.getHero().getHeroId();
        CrossHero crossHero = player.getHeroModel().getHeros().get(heroId);
        if (crossHero == null) {
            StaticCrossFort campFort = StaticCrossDataMgr.getCampFort(player.getCamp());
            crossHero = CrossHero.newInstance(req.getHero(), campFort.getId()); // 重新创建一个crossHero
            player.getHeroModel().getHeros().put(heroId, crossHero);
        }
        crossHero.setRevivalTime(0);
        crossHero.setState(ArmyConstant.ARMY_STATE_CROSS);
        crossHero.setCount(req.getHero().getCount());
        crossHero.setRevivalCnt(crossHero.getRevivalCnt() + 1); // 复活次数+1

        RoleForce roleForce = heroRevivalService.removeRoleForceFromQueue(player.getLordId(), heroId);
        if (roleForce == null) {
            roleForce = RoleForce.createInstance(crossHero, player.getCamp());
        }
        List<CrossHero> newAddList = new ArrayList<>();
        newAddList.add(crossHero);
        // 加入到堡垒中
        fortressMgr.joinCrossHeroFortess(newAddList, crossHero.getFortId(), player.getCamp());

        OpFortHeroRs.Builder builder = OpFortHeroRs.newBuilder();
        builder.setHero(crossHero.toFortHeroPb());
        PbMsgUtil.sendOkMsgToPlayer(player, OpFortHeroRs.EXT_FIELD_NUMBER, OpFortHeroRs.ext, builder.build());
    }

    /**
     * 检查将领状态是否正确
     * 
     * @param player
     * @param req
     * @return true 状态正确
     */
    private boolean checkHeroLiveState(CrossPlayer player, OpFortHeroRq req) {
        int heroId = req.getHeroId();
        CrossHero crossHero = player.getHeroModel().getHeros().get(heroId);
        if (crossHero == null) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                    new MwException(GameError.CROSS_HERO_STATE_ERR.getCode(), "将领不在跨服中 lordId:", player.getLordId(),
                            ", heroId:", heroId));
            return false;
        }
        if (crossHero.getState() != ArmyConstant.ARMY_STATE_CROSS) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                    new MwException(GameError.CROSS_HERO_STATE_ERR.getCode(), "将领在跨服中状态不正确 lordId:",
                            player.getLordId(), ", heroId:", heroId, ", state:", crossHero.getState()));
            return false;
        }
        return true;
    }

    /**
     * 单挑
     * 
     * @param player
     * @param req
     */
    private void soloFortress(CrossPlayer player, OpFortHeroRq req) {
        if (!checkHeroLiveState(player, req)) return;
        Fortress fortress = fortressMgr.getFortress(player.getHeroModel().getHeros().get(req.getHeroId()).getFortId());
        if (!checkSoloCond(fortress, player)) return;
        RoleForce roleForce = fortress.findRoleForce(player.getLordId(), req.getHeroId());
        if (roleForce == null) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                    new MwException(GameError.PARAM_ERROR.getCode(), "将领不在对应的堡垒中: lordId", player.getLordId(),
                            ", heroId:", req.getHeroId()));
            return;
        }
        // 单挑扣钱
        List<Integer> subAward = Constant.CROSS_OPERATE_HERO_COST.get(0);
        rewardMgr.subAward((p) -> {
            // 单挑逻辑
            CrossHero crossHero = player.getHeroModel().getHeros().get(req.getHeroId());
            if (crossHero.getCount() > 0) {
                crossWarService.fightSolo(player, crossHero);
            }
            OpFortHeroRs.Builder builder = OpFortHeroRs.newBuilder();
            builder.setHero(crossHero.toFortHeroPb());
            PbMsgUtil.sendOkMsgToPlayer(player, OpFortHeroRs.EXT_FIELD_NUMBER, OpFortHeroRs.ext, builder.build());
        }, player, subAward, AwardFrom.CROSS_HERO_OPERATE, OpFortHeroRs.EXT_FIELD_NUMBER);
    }

    /**
     * 检测单挑条件是否满足
     * 
     * @param fortress
     * @return
     */
    private boolean checkSoloCond(Fortress fortress, CrossPlayer player) {
        boolean f = fortress.canSolo(player.getCamp());
        if (!f) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                    new MwException(GameError.PARAM_ERROR.getCode(), "对方没有兵力不能单挑:", player.getLordId()));
        }

        return f;
    }

    /**
     * 偷袭
     * 
     * @param player
     * @param req
     */
    private void sneakFortress(CrossPlayer player, OpFortHeroRq req) {
        if (!checkHeroLiveState(player, req)) return;
        if (!checkFortressState(player, req, true)) return;

        int heroId = req.getHeroId();
        final CrossHero crossHero = player.getHeroModel().getHeros().get(heroId);
        int reqFortId = req.getFortId(); // 需要到达的堡垒
        // 偷袭扣钱操作
        List<Integer> subAward = Constant.CROSS_OPERATE_HERO_COST.get(2);
        rewardMgr.subAward((p) -> {
            // 扣款成功走此处
            sendMoveHeroResMsg(player, crossHero, reqFortId);
        }, player, subAward, AwardFrom.CROSS_HERO_OPERATE, OpFortHeroRs.EXT_FIELD_NUMBER);
    }

    private void sendMoveHeroResMsg(CrossPlayer player, final CrossHero crossHero, int reqFortId) {
        fortressMgr.moveToFortess(player, crossHero, reqFortId);
        OpFortHeroRs.Builder builder = OpFortHeroRs.newBuilder();
        builder.setHero(crossHero.toFortHeroPb());
        PbMsgUtil.sendOkMsgToPlayer(player, OpFortHeroRs.EXT_FIELD_NUMBER, OpFortHeroRs.ext, builder.build());
    }

    /**
     * 进攻
     * 
     * @param player
     * @param req
     */
    private void attackFortress(CrossPlayer player, OpFortHeroRq req) {
        if (!checkHeroLiveState(player, req)) return;
        if (!checkFortressState(player, req, true)) return;
        int heroId = req.getHeroId();
        CrossHero crossHero = player.getHeroModel().getHeros().get(heroId);
        int heroFortId = crossHero.getFortId();
        int reqFortId = req.getFortId();
        Fortress heroFortress = fortressMgr.getFortress(heroFortId);
        // 不相邻检测
        if (!heroFortress.checkNeighbor(reqFortId)) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                    new MwException(GameError.PARAM_ERROR.getCode(), "将领所在堡垒与去的堡垒不相邻 lordId:", player.getLordId(),
                            ", reqFortId:", reqFortId));
            return;
        }
        sendMoveHeroResMsg(player, crossHero, reqFortId);
    }

    /**
     * 回防
     * 
     * @param player
     * @param req
     */
    private void backCourtfortress(CrossPlayer player, OpFortHeroRq req) {
        if (!checkHeroLiveState(player, req)) return;
        if (!checkFortressState(player, req, false)) return;

        int heroId = req.getHeroId();
        final CrossHero crossHero = player.getHeroModel().getHeros().get(heroId);
        int reqFortId = req.getFortId(); // 需要到达的堡垒
        List<Integer> subAward = Constant.CROSS_OPERATE_HERO_COST.get(1);
        rewardMgr.subAward((p) -> {
            sendMoveHeroResMsg(player, crossHero, reqFortId);
        }, player, subAward, AwardFrom.CROSS_HERO_OPERATE, OpFortHeroRs.EXT_FIELD_NUMBER);
    }

    /**
     * 检测城堡状态
     * 
     * @param player
     * @param req
     * @param isAttack
     * @return
     */
    private boolean checkFortressState(CrossPlayer player, OpFortHeroRq req, boolean isAttack) {
        int heroId = req.getHeroId();
        CrossHero crossHero = player.getHeroModel().getHeros().get(heroId);
        int reqFortId = req.getFortId(); // 需要到达的堡垒
        int heroFortId = crossHero.getFortId(); // 将领所在堡垒
        if (heroFortId == reqFortId) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER, new MwException(
                    GameError.BERLIN_HERO_STATUS_ERROR.getCode(), "当前将领已经在该堡垒 lordId:", player.getLordId()));
            return false;
        }
        Fortress reqFortress = fortressMgr.getFortress(reqFortId);
        if (reqFortress == null) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER, new MwException(
                    GameError.NO_CONFIG.getCode(), "堡垒的配置未找到 lordId:", player.getLordId(), ", reqFortId:", reqFortId));
            return false;
        }
        Fortress heroFortress = fortressMgr.getFortress(heroFortId);
        if (!isAttack) {
            if (heroFortress.getCamp() != reqFortress.getCamp()) {
                PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                        new MwException(GameError.PARAM_ERROR.getCode(), "前往或回防的堡垒阵营不同 lordId:", player.getLordId(),
                                ", reqFortId:", reqFortId));
                return false;
            }
        } else {
            if (heroFortress.getCamp() == reqFortress.getCamp()) {
                PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                        new MwException(GameError.PARAM_ERROR.getCode(), "攻击城堡时阵营是一致 lordId:", player.getLordId(),
                                ", reqFortId:", reqFortId));
                return false;
            }
        }
        return true;
    }

    /**
     * 前往
     * 
     * @param player
     * @param req
     */
    private void goTofortress(CrossPlayer player, OpFortHeroRq req) {
        int heroId = req.getHeroId();
        if (!checkHeroLiveState(player, req)) return;
        if (!checkFortressState(player, req, false)) return;

        CrossHero crossHero = player.getHeroModel().getHeros().get(heroId);
        int reqFortId = req.getFortId(); // 需要到达的堡垒
        int heroFortId = crossHero.getFortId(); // 将领所在堡垒
        Fortress heroFortress = fortressMgr.getFortress(heroFortId);

        if (!heroFortress.checkNeighbor(reqFortId)) {
            PbMsgUtil.sendErrToPlayerLog(player, OpFortHeroRs.EXT_FIELD_NUMBER,
                    new MwException(GameError.PARAM_ERROR.getCode(), "将领所在堡垒与去的堡垒不相邻 lordId:", player.getLordId(),
                            ", reqFortId:", reqFortId));
            return;
        }
        sendMoveHeroResMsg(player, crossHero, reqFortId);
    }

    /**
     * 将领信息同步
     * 
     * @param player
     * @param req
     */
    public void crossHeroSync(CrossPlayer player, CrossHeroSyncRq req) {
        List<CrossHeroPb> heroList = req.getHeroList();
        if (CheckNull.isEmpty(heroList)) return;
        for (CrossHeroPb crossHeroPb : heroList) {
            CrossHero crossHero = player.getHeroModel().getHeros().get(crossHeroPb.getHeroId());
            if (crossHero == null) continue;
            crossHero.refreshData(crossHeroPb);
        }
    }
}
