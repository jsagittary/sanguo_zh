package com.gryphpoem.game.zw.gameplay.cross.serivce.map;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.aop.CrossGameMapDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.VipConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Game2CrossArmyService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossGameMapDataMgr crossGameMapDataMgr;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private CrossGamePlayService crossGamePlayService;

    /**
     * 撤回跨服战火燎原部队
     *
     * @param roleId
     * @param req
     * @return
     * @throws Exception
     */
    public void retreatNewCrossArmy(long roleId, GamePb6.RetreatCrossWarFireRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), true);

        int keyId = req.getKeyId();
        int type = req.getType();

        if (keyId < 0 || type < 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId:,", roleId, ", type:", type, ", keyId: ", keyId);
        }

        crossGameMapDataMgr.retreatCross(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), keyId, type, checkCostPropReturnArmy(roleId, player, type));
    }

    /**
     * 校验并可以扣除或不扣除资源
     *
     * @param roleId
     * @param player
     * @param type
     * @throws MwException
     */
    private List<byte[]> checkCostPropReturnArmy(long roleId, Player player, int type) throws MwException {
        if (type == 0) {
            return null;
        }

        // 需要消耗道具
        NewCrossConstant.RetreatCrossArmyProp prop = NewCrossConstant.RetreatCrossArmyProp.convertTo(type);
        if (CheckNull.isNull(prop)) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "跨服行军召回传参错误, roleId:,", roleId, ", type:", type);
        }
        if (prop.getPropId() == 0) {
            // vip特权次数,免费召回
            if (player.common.getRetreat() >= vipDataManager.getNum(player.lord.getVip(), VipConstant.RETREAT)) {
                throw new MwException(GameError.SHOP_VIP_BUY_CNT.getCode(), "商品购买时，vip次数不够, roleId:" + roleId);
            }
            player.common.setRetreat(player.common.getRetreat() + 1);
        }

//        if (!isFree) {
//            if (sub) {
//                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, 1,
//                        AwardFrom.MOVE_RETREAT);
//            } else {
//                rewardDataManager.checkPlayerResIsEnough(player, AwardType.PROP, propId, 1);
//            }
//        }

        List<byte[]> consumeBytes = new ArrayList<>();
        if (prop.getPropId() != 0) {
            consumeBytes.add(PbHelper.createAwardPb(AwardType.PROP, prop.getPropId(), 1).toByteArray());
        }

        return consumeBytes;
    }

    /**
     * 获取跨服战火地图上的军队信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public void getNewCrossArmy(long roleId, int functionId) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);

        crossGameMapDataMgr.getCrossWarFireArmy(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), -1);
    }

    /**
     * 跨服地图行军加速
     *
     * @param roleId
     * @param req
     * @return
     * @throws Exception
     */
    public void accelerateCrossArmy(long roleId, GamePb6.NewCrossAccelerateArmyRq req) throws Exception {
        int armyKeyId = req.getArmyKeyId();
        int type = req.getType();
        int functionId = req.getFunctionId();

        NewCrossConstant.AccelerateCrossArmyProp prop = NewCrossConstant.AccelerateCrossArmyProp.convertTo(type);
        if (CheckNull.isNull(prop) || armyKeyId <= 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ",
                    roleId, ", type: ", type, ", armyKeyId: ", armyKeyId, ", functionId: ", functionId);
        }

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);

        List<byte[]> consume = new ArrayList<>();
        consume.add(PbHelper.createAwardPb(AwardType.PROP, prop.getPropId(), 1).toByteArray());
        crossGameMapDataMgr.accelerateCrossArmy(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), armyKeyId, type, consume);

//        GamePb6.NewCrossAccelerateArmyRs.Builder builder = GamePb6.NewCrossAccelerateArmyRs.newBuilder();
//        Optional.ofNullable(armies).ifPresent(cArmies -> builder.setArmy(cArmies.get(0)));
//        if (player.props.get(prop.getPropId()) != null) {
//            builder.setProp(PbHelper.createPropPb(player.props.get(prop.getPropId())));
//        }
//        return builder.build();
    }

    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {

    }
}
