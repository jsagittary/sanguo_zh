package com.gryphpoem.game.zw.service.activity.anniversary;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticAnniversaryMgr;
import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.Pay;
import com.gryphpoem.game.zw.resource.domain.s.StaticActSkinEncore;
import com.gryphpoem.game.zw.resource.domain.s.StaticPay;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.AbsSimpleActivityService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-07-26 19:35
 */
@Service
public class ActivitySkinEncoreService extends AbsSimpleActivityService {


    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        ActivityPb.SkinEncoreActivityData.Builder skinBuilder = ActivityPb.SkinEncoreActivityData.newBuilder();
        if (CheckNull.nonEmpty(activity.getSaveMap())) {
            Map<Integer, StaticActSkinEncore> skinEncoreMap = StaticAnniversaryMgr.getActSkinEncoreMap();
            for (Integer uid : activity.getSaveMap().keySet()) {
                if (uid != SAVE_IDX_0) {
                    StaticActSkinEncore sSkinEncore = skinEncoreMap.get(uid);
                    skinBuilder.addSkinId(sSkinEncore.getSkinId());
                }
            }
        }
        builder.setSkinEncoreInfo(skinBuilder);
        return builder;
    }

    @Override
    protected ActivityPb.ActivityData getActivityData(Player player, int activityType) {
        ActivityPb.SkinEncoreActivityData.Builder builder = ActivityPb.SkinEncoreActivityData.newBuilder();
        Activity activity = getActivity(player, activityType);
        if (CheckNull.nonEmpty(activity.getSaveMap())) {
            builder.addAllSkinId(activity.getSaveMap().keySet());
        }
        return null;
//        return buildActivityData(activityType, builder.build(), ActivityPb.SkinEncoreActivityData.ext, ActivityPb.SkinEncoreActivityData.EXT_FIELD_NUMBER);
    }


    /**
     * 使用金币购买返场皮肤
     *
     * @param player
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.BuyEncoreSkinRs buyEncoreSkin(Player player, GamePb4.BuyEncoreSkinRq req) throws MwException {
        long lordId = player.getLordId();
        int actType = req.getActType();
        //验证活动类型是否正确
        validateActivityType(actType);
        //获取并验证活动是否开启
        checkAndGetActivityBase(player, actType);
        int id = req.getId();
        StaticActSkinEncore sSkinEncore = id > 0 ? StaticAnniversaryMgr.getStaticActSkinEncore(req.getId()) : null;
        if (sSkinEncore == null || sSkinEncore.getGoldCost() <= 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, uid :%d error!!!", lordId, id));
        }
        Activity activity = super.checkAndGetActivity(player, actType);

        Map<Integer, Integer> saveMap = activity.getSaveMap();
        int buyCount = saveMap.getOrDefault(SAVE_IDX_0, 0);
        if (buyCount >= ActParamConstant.ACT_SKIN_ENCORE_BUY_COUNT) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, 已达购买次数上限 [%d - %d]", lordId, buyCount, ActParamConstant.ACT_SKIN_ENCORE_BUY_COUNT));
        }

        int alreadyBuy = activity.getSaveMap().getOrDefault(id, 0);
        if (alreadyBuy > 0) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), String.format("roleId :%d, id :%d, 已经购买过该皮肤", lordId, id));
        }
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, sSkinEncore.getGoldCost(),
                AwardFrom.ANNIVERSARY_SKIN_ENCORE_BUY_SKIN, actType, id);
        activity.getSaveMap().put(id, buyCount + 1);
        rewardDataManager.sendRewardSignle(player, AwardType.CASTLE_SKIN, sSkinEncore.getSkinId(), 1,
                AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, sSkinEncore.getActivityId(), id);
        return GamePb4.BuyEncoreSkinRs.newBuilder().build();
    }


    /**
     * 皮肤返场充值购买皮肤
     *
     * @param player
     * @param pay
     * @param sPay
     */
    public void pay4SkinEncore(Player player, Pay pay, StaticPay sPay) {
        try {
            //皮肤返场---充值皮肤配置不存在
            Map<Integer, StaticActSkinEncore> skinEncoreMap = StaticAnniversaryMgr.getActSkinEncoreMap();
            StaticActSkinEncore sSkinEncore = skinEncoreMap.values().stream().filter(s -> s.getPayId() == sPay.getPayId()).findFirst().orElse(null);
            if (Objects.isNull(sSkinEncore)) {
                LogUtil.error(String.format("not found Activity SkinEncore config !!! lordId :%d, serId :%s, payId :%d ",
                        player.getLordId(), pay.getSerialId(), sPay.getPayId()));
                return;
            }

            //活动不存在
            ActivityBase foundActivityBase = null;
            for (int actType : getActivityType()) {
                ActivityBase actBase = StaticActivityDataMgr.getActivityByType(actType);
                if (Objects.nonNull(actBase) && actBase.getActivityId() == sSkinEncore.getActivityId()) {
                    foundActivityBase = actBase;
                }
            }
            if (Objects.isNull(foundActivityBase)) {
                LogUtil.error(String.format("Activity SkinEncore not found !!! lordId :%d, serId :%s, payId :%d, activityId :%d",
                        player.getLordId(), pay.getSerialId(), sPay.getPayId(), sSkinEncore.getActivityId()));
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_ACT_SKIN_ENCORE_FAIL,
                        TimeHelper.getCurrentSecond(), sSkinEncore.getActivityId(), sSkinEncore.getSkinId());
                return;
            }

            Activity activity = getActivity(player, foundActivityBase.getActivityType());

            Map<Integer, Integer> saveMap = activity.getSaveMap();
            int buyCount = saveMap.getOrDefault(SAVE_IDX_0, 0);
            if (buyCount >= ActParamConstant.ACT_SKIN_ENCORE_BUY_COUNT) {
                LogUtil.error(String.format("roleId :%d, 已达购买次数上限 [%d - %d], buy ids :%s",
                        player.getLordId(), buyCount, ActParamConstant.ACT_SKIN_ENCORE_BUY_COUNT, Arrays.toString(saveMap.keySet().toArray())));
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_ACT_SKIN_ENCORE_FAIL,
                        TimeHelper.getCurrentSecond(), sSkinEncore.getActivityId(), sSkinEncore.getSkinId());
                return;
            }

            int alreadyBuy = saveMap.getOrDefault(sSkinEncore.getId(), 0);
            if (alreadyBuy > 0) {
                LogUtil.error(String.format("roleId :%d, id :%d, 已经购买过该皮肤", player.getLordId(), sSkinEncore.getId()));
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_ACT_SKIN_ENCORE_FAIL,
                        TimeHelper.getCurrentSecond(), sSkinEncore.getActivityId(), sSkinEncore.getSkinId());
                return;
            }

            saveMap.put(SAVE_IDX_0, buyCount + 1);
            saveMap.put(sSkinEncore.getId(), 1);
            rewardDataManager.sendRewardSignle(player, AwardType.CASTLE_SKIN, sSkinEncore.getSkinId(), 1,
                    AwardFrom.ANNIVERSARY_SKIN_ENCORE_RECHARGE, pay.getSerialId(), sPay.getPayId(), sSkinEncore.getId());
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ACT_SKIN_ENCORE_SUCCESS, TimeHelper.getCurrentSecond(), sSkinEncore.getActivityId(), sSkinEncore.getSkinId());
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_ANNIVERSARY_SKIN};
    }
}
