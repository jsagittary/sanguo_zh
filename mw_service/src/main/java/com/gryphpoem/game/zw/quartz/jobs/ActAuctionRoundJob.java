package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.service.activity.ActivityAuctionService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.quartz.JobExecutionContext;

import java.util.Date;

public class ActAuctionRoundJob extends AbsMainLogicThreadJob {

    public static final String NAME_ROUND_BEGIN = "ActAuctionRoundBegin_";
    public static final String NAME_ROUND_END = "ActAuctionRoundEnd_";
    public static final String NAME_ROUND_ABOUT_TO_END = "ActAuctionAboutToEnd_";
    public static final String GROUP_NAME = "ActAuctionRound";

    @Override
    protected void executeInMain(JobExecutionContext context) {
        ActivityAuctionService activityAuctionService = DataResource.ac.getBean(ActivityAuctionService.class);
        String jobKeyName = context.getJobDetail().getKey().getName();
        LogUtil.debug("-----秋季拍卖活动 act name: ", jobKeyName, ", now: ", DateHelper.formatDateMiniTime(new Date()));
        String[] strArr = jobKeyName.split("_");
        if (jobKeyName.indexOf(NAME_ROUND_BEGIN) != -1) {
            ActivityAuctionService.activityAuctionRoundBegin(Integer.parseInt(strArr[1]), Integer.parseInt(strArr[2]));
            LogUtil.debug("秋季拍卖活动回合开始ActAuctionRoundBegin执行job成功, jobKeyName: ", jobKeyName);
        } else if (jobKeyName.indexOf(NAME_ROUND_END) != -1) {
            ActivityAuctionService.activityAuctionRoundEnd(Integer.parseInt(strArr[1]), Integer.parseInt(strArr[2]), Integer.parseInt(strArr[3]), false);
            LogUtil.debug("秋季拍卖活动回合结束ActAuctionRoundEnd执行job成功, jobKeyName: ", jobKeyName);
        } else if (jobKeyName.indexOf(NAME_ROUND_ABOUT_TO_END) != -1) {
            activityAuctionService.aboutToEndAct();
            LogUtil.debug("秋季拍卖活动回合即将结束ActAuctionAboutToEnd执行job成功, jobKeyName: ", jobKeyName);
        }
    }
}
