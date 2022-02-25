package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActVoucher;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;

/**
 * 喜悦金秋活动
 *
 */
public abstract class GoldenAutumnService extends AbsActivityService {

    private int[] actTypes = {ActivityConst.ACT_GOLDEN_AUTUMN_FARM};

    /**
     * 获取金秋活动
     */
    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        CommonPb.GoldenAutumnActivityData.Builder goldenAutumnActivityData = CommonPb.GoldenAutumnActivityData.newBuilder();
        Map<String, GoldenAutumnService> resultMap = DataResource.ac.getBeansOfType(GoldenAutumnService.class);
        if (null != resultMap && !resultMap.isEmpty())
        {
            for (Map.Entry<String, GoldenAutumnService> entry : resultMap.entrySet())
            {
                GeneratedMessage generatedMessage = entry.getValue().buildGoldenAutumnInfo(player, activity);
                if (generatedMessage instanceof CommonPb.GoldenAutumnFarm)
                {
                    goldenAutumnActivityData.setGoldenAutumnFarm((CommonPb.GoldenAutumnFarm) generatedMessage);
                }
                else if (generatedMessage instanceof CommonPb.GoldenAutumnSunrise)
                {
                    goldenAutumnActivityData.setGoldenAutumnSunrise((CommonPb.GoldenAutumnSunrise) generatedMessage);
                }
                else if (generatedMessage instanceof CommonPb.GoldenAutumnFruitful)
                {
                    goldenAutumnActivityData.setGoldenAutumnFruitful((CommonPb.GoldenAutumnFruitful) generatedMessage);
                }
            }
        }
        builder.setActivityType(activity.getActivityType());
        builder.setGoldenAutumnInfo(goldenAutumnActivityData);
        return builder;
    }

    public abstract GeneratedMessage buildGoldenAutumnInfo(Player player, Activity activity);

    @Override
    protected int[] getActivityType() {
        return actTypes;
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        playerDataManager.getPlayers().values().forEach(player -> {
            Activity activity = player.activitys.get(activityType);
            if (Objects.nonNull(activity) && activity.getActivityType() == ActivityConst.ACT_GOLDEN_AUTUMN_FARM)
            {
                //清除喜悦金秋活动用到的活动数据
                activity.getStatusCnt().clear();
                activity.getStatusMap().clear();
                activity.getPropMap().clear();
                activity.getSaveMap().clear();
                //清除种子和稻穗活动道具
                List<StaticActVoucher> staticActVoucherList = StaticActivityDataMgr.getActVoucherListByActId(activityId);
                if (null != staticActVoucherList && !staticActVoucherList.isEmpty())
                {
                    for (StaticActVoucher staticActVoucher : staticActVoucherList)
                    {
                        Prop prop = player.props.get(staticActVoucher.getConsume().get(1));
                        if (Objects.nonNull(prop) && prop.getCount() >= staticActVoucher.getConsume().get(2))
                        {
                            List<List<Integer>> awardList = new ArrayList<>();
                            awardList.add(staticActVoucher.getAwardList());
                            List<CommonPb.Award> nonAwards = PbHelper.createAwardsPb(awardList);
                            mailDataManager.sendAttachMail(player,nonAwards, MailConstant.MOLD_ACT_EXCHANGE_REWARD,AwardFrom.GOLDEN_AUTUMN_GET_AWARD, TimeHelper.getCurrentSecond(),activityType,activityId,activityType,activityId);
                            player.props.remove(prop.getPropId());
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }
}
