package com.gryphpoem.game.zw.gameplay.local.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.dto.RetreatArmyParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.VipDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.Prop;
import com.gryphpoem.game.zw.pb.GamePb2.MoveCDRs;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.constant.VipConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName CrossArmyService.java
 * @Description 部队相关
 * @author QiuKun
 * @date 2019年3月21日
 */
@Component
public class CrossArmyService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossWorldMapService crossWorldMapService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private VipDataManager vipDataManager;

    /**
     * 跨服的撤回部队
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public RetreatCrossRs retreatCross(long roleId, RetreatCrossRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        int keyId = req.getKeyId();
        int type = req.getType();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        BaseArmy baseArmy = mapMarchArmy.getBaseArmyByLordIdAndKeyId(roleId, keyId);
        if (baseArmy == null) {
            throw new MwException(GameError.RETREAT_ARMY_NOT_FOUND.getCode(), "撤回部队，未找到部队信息, roleId:", roleId,
                    ", keyId:", keyId);
        }
        // 检测消耗
        checkCostPropReturnArmy(roleId, player, type, baseArmy.getArmy());
        RetreatArmyParamDto param = new RetreatArmyParamDto();
        param.setInvokePlayer(player);
        param.setType(type);
        param.setCrossWorldMap(cMap);
        baseArmy.retreat(param);
        RetreatCrossRs.Builder builder = RetreatCrossRs.newBuilder();
        return builder.build();
    }

    /**
     * 部队撤回消耗检测
     * 
     * @param roleId
     * @param player
     * @param type
     * @param army
     * @throws MwException
     */
    private void checkCostPropReturnArmy(long roleId, Player player, int type, Army army) throws MwException {
        if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // 需要消耗道具
            boolean isFree = false;
            int propId = 0;
            if (type == ArmyConstant.MOVE_BACK_TYPE_1) {
                propId = PropConstant.ITEM_ID_5021;
            } else if (type == ArmyConstant.MOVE_BACK_TYPE_2) {
                propId = PropConstant.ITEM_ID_5022;
            } else if (type == 3) {
                // vip特权次数,免费召回
                if (player.common.getRetreat() >= vipDataManager.getNum(player.lord.getVip(), VipConstant.RETREAT)) {
                    throw new MwException(GameError.SHOP_VIP_BUY_CNT.getCode(), "商品购买时，vip次数不够, roleId:" + roleId);
                }
                // 普通撤回
                type = ArmyConstant.MOVE_BACK_TYPE_1;
                isFree = true;
                player.common.setRetreat(player.common.getRetreat() + 1);
            } else {
                throw new MwException(GameError.NO_CD_TIME.getCode(), "行军召回传参错误, roleId:,", roleId, ", type:", type);
            }
            if (!isFree) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, 1,
                        AwardFrom.MOVE_RETREAT);
            }
        }
    }

    /**
     * 获取跨服的部队
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetCrossArmyRs getCrossArmy(long roleId, GetCrossArmyRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        PlayerArmy playerArmy = mapMarchArmy.getPlayerArmyMap().get(roleId);
        GetCrossArmyRs.Builder builder = GetCrossArmyRs.newBuilder();
        if (playerArmy != null && !playerArmy.getArmy().isEmpty()) {
            playerArmy.getArmy().values().forEach(baseArmy -> {
                builder.addArmy(PbHelper.createArmyPb(baseArmy.getArmy(), false));
            });
        }
        return builder.build();
    }

    /**
     * 行军加速
     * 
     * @param player
     * @param type
     * @param keyId
     * @return
     * @throws MwException
     */
    public MoveCDRs moveCd(Player player, int type, int keyId) throws MwException {
        int mapId = player.lord.getArea();
        long roleId = player.lord.getLordId();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        BaseArmy baseArmy = mapMarchArmy.getBaseArmyByLordIdAndKeyId(roleId, keyId);
        if (baseArmy == null) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "行军加速没有战斗, roleId:,", roleId, ", type:", type);
        }
        Army army = baseArmy.getArmy();
        if ((type == ArmyConstant.MOVE_TYPE_1 || type == ArmyConstant.MOVE_TYPE_2)
                && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "只有撤回才能行军加速, roleId:,", roleId, ", type:", type);
        }
        int now = TimeHelper.getCurrentSecond();
        if (now > army.getEndTime()) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "行军加速传参错误, roleId:,", roleId, ", type:", type);
        }
        int propId = 0;
        if (type == ArmyConstant.MOVE_TYPE_1) {
            propId = PropConstant.ITEM_ID_5011;
        } else if (type == ArmyConstant.MOVE_TYPE_2) {
            propId = PropConstant.ITEM_ID_5012;
        } else {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "行军加速传参错误, roleId:,", roleId, ", type:", type);
        }

        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, 1, AwardFrom.MOVE_CD);
        MoveCDRs.Builder builder = MoveCDRs.newBuilder();
        if (type == ArmyConstant.MOVE_TYPE_1) {
            army.setDuration(army.getDuration() / 2);
            baseArmy.setEndTime(mapMarchArmy, now + (army.getEndTime() - now) / 2);
        } else if (type == ArmyConstant.MOVE_TYPE_2) {
            army.setDuration(1);
            baseArmy.setEndTime(mapMarchArmy, now + 1);

        }
        // 更新地图速度
        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setStatus(1);
        builder.setProp(Prop.newBuilder().setPropId(propId).setCount(player.props.get(propId).getCount()));
        return builder.build();
    }

}
