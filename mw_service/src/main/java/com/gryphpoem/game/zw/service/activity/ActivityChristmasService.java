package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3.GetActRankRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncChristmasDataRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetChristmasAwardRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetChristmasInfoRs;
import com.gryphpoem.game.zw.pb.GamePb4.HandInChristmasChipRs;
import com.gryphpoem.game.zw.quartz.jobs.ActAutoConverJob;
import com.gryphpoem.game.zw.quartz.jobs.ActMailJob;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivityPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticChristmasAward;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.*;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 圣诞活动
 * 修缮城堡
 */
@Service
public class ActivityChristmasService {

    private static final int CHIP_ID = 1923;
    private static final int CHIP_REPAIRCASTLE = 1928;

    private static final byte SAVEMAP_KEY1 = 0x1;//自己捐献的圣诞衣服碎片数量
    private static final byte SAVEMAP_KEY2 = 0x2;//上交碎片临时数量，用于计算积分
    private static final byte SAVEMAP_KEY3 = 0x3;//积分
    private static final byte SAVEMAP_KEY4 = 0x4;//商家哦碎片临时数量，用于计算随机奖励

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private MailDataManager mailDataManager;

    private Activity getPlayerActivity(Player player,int activityType) throws MwException {
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 活动未开启(Player.Activity=null), roleId:,", player.lord.getLordId(), ", activityType:", activityType);
        }
        return activity;
    }

    private GlobalActivityData getGlobalActivityData(Player player,int activityType) throws MwException {
        GlobalActivityData globalActivityData = activityDataManager.getGlobalActivity(activityType);
        if (CheckNull.isNull(globalActivityData)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 活动未开启(GlobalActivityData=null), roleId:,", player.getLordId(), ", activityType:", activityType);
        }
        return globalActivityData;
    }

    public GetChristmasInfoRs getInfo(long roleId,int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if(activityType != ActivityConst.ACT_CHRISTMAS && activityType != ActivityConst.ACT_REPAIR_CASTLE){
            throw new MwException(GameError.PARAM_ERROR.getCode(),"参数错误, roleid=" + roleId + ", activityType=" + activityType);
        }

        Activity activity = getPlayerActivity(player,activityType);

        GlobalActivityData globalActivityData = getGlobalActivityData(player,activityType);

        GetChristmasInfoRs.Builder builder = GetChristmasInfoRs.newBuilder();

        for (int camp : Constant.Camp.camps) {
            builder.addCampChips(PbHelper.createTwoIntPb(camp, getCampValue(globalActivityData, camp)));
        }
        builder.setMyHandedChips(getMyChipCount(activity));
        builder.setMyScore(getMyScore(activity));
        builder.setMyCampChips(getCampValue(globalActivityData, player.getCamp()));
        List<Integer> tmp1 = new ArrayList<>();
        if(activityType == ActivityConst.ACT_REPAIR_CASTLE){
            tmp1.addAll(ActParamConstant.CHRISTMAS_314);
        }else if(activityType == ActivityConst.ACT_CHRISTMAS){
            tmp1.addAll(ActParamConstant.REPAIRCASTLE_314);
        }
        for(int i=0;i<tmp1.size();i++){
            builder.addGotAward(PbHelper.createTwoIntPb(i+1, getAwardState0(player, activity, globalActivityData, i+1)));
        }
        return builder.build();
    }

    public HandInChristmasChipRs handInChips(long roleId, int count, int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (count < 1) {
            count = 1;
        }
        if(activityType != ActivityConst.ACT_CHRISTMAS && activityType != ActivityConst.ACT_REPAIR_CASTLE){
            throw new MwException(GameError.PARAM_ERROR.getCode(),"参数错误, roleid=" + roleId + ", activityType=" + activityType);
        }

        Activity activity = getPlayerActivity(player,activityType);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 活动未开启(Player.ActivityBase=null), roleId:,", player.lord.getLordId(), ", activityType:", activityType);
        }

        Date now = new Date();
        if (activityBase.getDisplayTime() != null && activityBase.getDisplayTime().before(now)) {
            throw new MwException(GameError.ACTIVITY_IS_OVER.getCode(), " 活动已结束(ActivityBase.getStep()=" + activityBase.getStep() + "), roleId:,", player.lord.getLordId(), ", activityType:", activityType);
        }

        GlobalActivityData globalActivityData = getGlobalActivityData(player,activityType);

        List<Integer> propList = new ArrayList<>(3);
        propList.add(AwardType.PROP);
        propList.add(activityType == ActivityConst.ACT_CHRISTMAS ? CHIP_ID : CHIP_REPAIRCASTLE);
        propList.add(count);
        List<List<Integer>> consumeList = new ArrayList<>(1);
        consumeList.add(propList);
        rewardDataManager.checkAndSubPlayerRes(player, consumeList, AwardFrom.ACT_CHRISTMAS_HANDIN_CHIP, activity.getActivityType(), activity.getActivityId());

        addMyChipCount(player, activity, count, AwardFrom.ACT_CHRISTMAS_HANDIN_CHIP);

        int randomTimes = canGetRandomAward(activity, count);
        List<CommonPb.Award> randomAwardList_ = new ArrayList<>();
        if (randomTimes > 0) {
            //随机奖励
            StaticChristmasAward staticChristmasAward = StaticActivityDataMgr.getStaticChristmasAwardByLv(player.lord.getLevel(), activity.getActivityId());
            if (staticChristmasAward == null) {
                LogUtil.activity("圣诞活动获取上交碎片奖励错误，根据等级取不到配置,lv=" + player.lord.getLevel());
            } else {
                List<List<Integer>> tmpList = new ArrayList<>();
                for(int i=0;i<randomTimes;i++){
                    tmpList.addAll(RandomUtil.getListRandomWeight(staticChristmasAward.getAwardList(), 1, 3));
                }
                randomAwardList_.addAll(rewardDataManager.sendReward(player, tmpList, AwardFrom.ACT_CHRISTMAS_GET_RANDOM_AWARD));
            }
        }

        activityDataManager.updRankActivity(player, activityType, count);

        if(activityBase.getEndTime() != null && activityBase.getEndTime().after(now)){
            globalActivityData.addCampValByCamp(player.getCamp(), count);
            syncInfo(player, globalActivityData);
        }

        HandInChristmasChipRs.Builder builder = HandInChristmasChipRs.newBuilder();
        builder.setMyHandedChips(getMyChipCount(activity));
        builder.addAllRandomAwards(randomAwardList_);
        builder.setMyScore(getMyScore(activity));
        return builder.build();
    }

    private void calcScore(Player player, Activity activity, int chipCount, AwardFrom awardFrom) {
        int tmpCount = getChipTmpCount(activity, SAVEMAP_KEY2);
        tmpCount = tmpCount + chipCount;
        int c1 = 0;
        if(activity.getActivityType() == ActivityConst.ACT_REPAIR_CASTLE){
            c1 = ActParamConstant.REPAIRCASTLE_311;
        }else if(activity.getActivityType() == ActivityConst.ACT_CHRISTMAS){
            c1 = ActParamConstant.CHRISTMAS_311;
        }
        int n = tmpCount / c1;
        if (n > 0) {
            addMyScore(player, activity, n, awardFrom);
            tmpCount = tmpCount % c1;
        }
        setChipTmpCount(activity, SAVEMAP_KEY2, tmpCount);
    }

    private int canGetRandomAward(Activity activity, int count) {
        int tmpCount = getChipTmpCount(activity, SAVEMAP_KEY4);
        tmpCount += count;
        int c1 ;
        if(activity.getActivityType() == ActivityConst.ACT_CHRISTMAS){
            c1 = ActParamConstant.CHRISTMAS_313;
        } else {
            c1 = ActParamConstant.REPAIRCASTLE_313;
        }
        int n = tmpCount / c1;
        if (n > 0) {
            tmpCount = tmpCount % c1;
        }
        setChipTmpCount(activity, SAVEMAP_KEY4, tmpCount);
        return n;
    }

    private void addMyChipCount(Player player, Activity activity, int count, AwardFrom awardFrom) {
        int val = getMyChipCount(activity);
        activity.getSaveMap().put((int) SAVEMAP_KEY1, val + count);

        calcScore(player, activity, count, awardFrom);
    }

    private int getMyChipCount(Activity activity) {
        return activity.getSaveMap().getOrDefault((int) SAVEMAP_KEY1, 0);
    }

    private int getCampValue(GlobalActivityData data, int camp) {
        return (int) data.getCampValByCamp(camp);
    }

    private int getAwardState(Activity activity, int stage) {
        return activity.getStatusMap().getOrDefault(stage << 8, 0);
    }

    private void setAwardState(Activity activity, int stage, int val) {
        activity.getStatusMap().put(stage << 8, val);
    }

    private int getAwardState0(Player player, Activity activity, GlobalActivityData globalActivityData, int stage) {
        if (checkCanGetAward(player, activity, globalActivityData, stage)) {
            if (getAwardState(activity, stage) == 1)
                return 2;
            else return 1;
        } else {
            return 3;
        }
    }

    private int getRandomAwardState(Activity activity, int stage) {
        return 0;
    }

    private void setRandomAwardState(Activity activity) {

    }

    private int getChipTmpCount(Activity activity, byte key) {
        return activity.getSaveMap().getOrDefault((int) key, 0);
    }

    private void setChipTmpCount(Activity activity, byte key, int tmpCount) {
        activity.getSaveMap().put((int) key, tmpCount);
    }

    public int getMyScore(Activity activity) {
        return activity.getSaveMap().getOrDefault((int) SAVEMAP_KEY3, 0);
    }

    private void setMyScore(Activity activity, int score) {
        activity.getSaveMap().put((int) SAVEMAP_KEY3, score);
    }

    private void addMyScore(Player player, Activity activity, int add, AwardFrom awardFrom) {
        int val = getMyScore(activity);
        setMyScore(activity, val + add);
        LogLordHelper.actScore(player, awardFrom, add, getMyScore(activity), activity.getActivityType(), activity.getActivityId(), add >= 0 ? "ADD" : "SUB");
    }

    public void syncInfo(Player player, GlobalActivityData globalActivityData) {
        SyncChristmasDataRs.Builder builder = SyncChristmasDataRs.newBuilder();
        for (int camp : Constant.Camp.camps) {
            builder.addCampChips(PbHelper.createTwoIntPb(camp, getCampValue(globalActivityData, camp)));
        }
        builder.setActivityType(globalActivityData.getActivityType());
        BasePb.Base msg = PbHelper.createSynBase(SyncChristmasDataRs.EXT_FIELD_NUMBER, SyncChristmasDataRs.ext, builder.build()).build();
        playerDataManager.getAllOnlinePlayer().values().forEach(p -> {
            if (player.getCamp() == p.getCamp()) MsgDataManager.getIns().add(new Msg(p.ctx, msg, p.roleId));
        });
    }

    public GetChristmasAwardRs getAward(long roleId, int stage, int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if(activityType != ActivityConst.ACT_CHRISTMAS && activityType != ActivityConst.ACT_REPAIR_CASTLE){
            throw new MwException(GameError.PARAM_ERROR.getCode(),"参数错误, roleid=" + roleId + ", activityType=" + activityType);
        }

        List<Integer> tmps314;
        if(activityType == ActivityConst.ACT_CHRISTMAS){
            tmps314 = ActParamConstant.CHRISTMAS_314;
        } else {
            tmps314 = ActParamConstant.REPAIRCASTLE_314;
        }

        if (stage < 1 || stage > tmps314.size()) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "领取碎片奖励参数错误, roleId=" + roleId, "stage=" + stage,"activityType=" + activityType);
        }
        Activity activity = getPlayerActivity(player,activityType);
        int state = getAwardState(activity, stage);
        if (state != 0) {
            throw new MwException(GameError.CHRISTMAS_CHIP_AWARD_GOT.getCode(), "领取碎片奖励已领取, roleId=" + roleId, "stage=" + stage,"activityType=" + activityType);
        }

        GlobalActivityData globalActivityData = getGlobalActivityData(player,activityType);
        int campChips = getCampValue(globalActivityData, player.getCamp());
        int myHandedChips = getMyChipCount(activity);
        if (myHandedChips < tmps314.get(stage-1)) {
            throw new MwException(GameError.CHRISTMAS_CHIP_AWARD_MY_NOT_ENOUGH.getCode(), "领取碎片奖励上交碎片不足, roleId=" + roleId, "stage=" + stage,"activityType=" + activityType);
        }

        List<Integer> tmps318 ;
        int tmp312 ;
        if(activityType == ActivityConst.ACT_CHRISTMAS){
            tmps318 = ActParamConstant.CHRISTMAS_318;
            tmp312 = ActParamConstant.CHRISTMAS_312;
        } else {
            tmps318 = ActParamConstant.REPAIRCASTLE_318;
            tmp312 = ActParamConstant.REPAIRCASTLE_312;
        }

        int v1 = tmps318.get(stage-1);
        int v2 = tmp312 * v1 / 100;
        if (campChips < v2) {
            throw new MwException(GameError.CHRISTMAS_CHIP_AWARD_CAMP_NOT_ENOUGH.getCode(), "领取碎片阵营碎片不足, roleId=" + roleId, "stage=" + stage,"activityType=" + activityType);
        }

        setAwardState(activity, stage, 1);

        StaticActAward staticActAward = StaticActivityDataMgr.getSupplyMaxByParam(activity.getActivityId(), stage);
        List<CommonPb.Award> chipAwards = rewardDataManager.sendReward(player, staticActAward.getAwardList(), AwardFrom.ACT_CHRISTMAS_GET_CHIP_AWARD);

        GetChristmasAwardRs.Builder builder = GetChristmasAwardRs.newBuilder();
        for(int i=0;i<tmps314.size();i++){
            builder.addGotAward(PbHelper.createTwoIntPb(i+1, getAwardState0(player, activity, globalActivityData, i+1)));
        }
        builder.addAllAwards(chipAwards);

        return builder.build();
    }

    private boolean checkCanGetAward(Player player, Activity activity, GlobalActivityData globalActivityData, int stage) {
        List<Integer> tmps318 ;
        int tmp312 ;
        List<Integer> tmps314;
        if(activity.getActivityType() == ActivityConst.ACT_CHRISTMAS){
            tmps318 = ActParamConstant.CHRISTMAS_318;
            tmp312 = ActParamConstant.CHRISTMAS_312;
            tmps314 = ActParamConstant.CHRISTMAS_314;
        } else {
            tmps318 = ActParamConstant.REPAIRCASTLE_318;
            tmp312 = ActParamConstant.REPAIRCASTLE_312;
            tmps314 = ActParamConstant.REPAIRCASTLE_314;
        }
        int v1 = tmps318.get(stage-1);
        int v2 = tmp312 * v1 / 100;
        int myHandedChips = getMyChipCount(activity);
        int campChips = getCampValue(globalActivityData, player.getCamp());
        return !(myHandedChips < tmps314.get(stage-1) || campChips < v2);
    }

    protected GetActRankRs getRank(Player player, int activityType) throws MwException {
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "获取圣诞活动排行榜错误, 活动未开启(Player.Activity=null) roleId:", player.roleId, "activityType=" + activityType);
        }
        GlobalActivityData gActDate = activityDataManager.getGlobalActivity(activityType);
        if (gActDate == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "获取圣诞活动排行榜错误, GlobalActivityData=null, roleId:", player.roleId, "activityType=" + activityType);
        }
        int showSize = 9;
        LinkedList<ActRank> rankList = gActDate.getPlayerRanks(player, activityType);
        ActRank myRank = gActDate.getPlayerRank(player, activityType, player.roleId);
        GetActRankRs.Builder builder = GetActRankRs.newBuilder();
        int i = 0;
        for (ActRank actRank : rankList) {
            if (i >= showSize) {
                break;
            }
            actRank.setRank(i + 1);
            Player p = playerDataManager.getPlayer(actRank.getLordId());
            actRank.setParam(actRank.getRankValue() / ActParamConstant.CHRISTMAS_311 + "");
            builder.addActRank(PbHelper.createActRank(actRank, p.lord.getNick(), p.lord.getCamp(), p.lord.getPortrait(), p.getDressUp().getCurPortraitFrame()));
            i++;
        }
        if(myRank == null){
            myRank = new ActRank(player.roleId,activityType,0,0);
        }
        myRank.setParam(myRank.getRankValue() / ActParamConstant.CHRISTMAS_311 + "");
        builder.addActRank(PbHelper.createActRank(myRank,player.lord.getNick(),player.lord.getCamp(),player.lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        return builder.build();
    }

    protected void checkAndSubScore4Exchange(Player player, Activity activity, int needScore, AwardFrom awardFrom) throws MwException {
        if (getMyScore(activity) < needScore) {
            throw new MwException(GameError.CHRISTMAS_EXCHANGE_SCORE_NOT_ENOUGH.getCode(), "圣诞活动积分兑换错误, 积分不够 roleId:",
                    player.roleId, "activityType=" + activity.getActivityType(), ", activityId=" + activity.getActivityId(), ", currScore=" + getMyScore(activity), ", needScore=" + needScore);
        }
        addMyScore(player, activity, -needScore, awardFrom);
    }

    public boolean isShowEffect(Player player) {
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (CheckNull.isNull(activityMap)) {
            return false;
        }
        GlobalActivityData activityData = activityMap.get(ActivityConst.ACT_CHRISTMAS);
        Activity activity = player.activitys.get(ActivityConst.ACT_CHRISTMAS);
        boolean can = false;
        boolean isShow = false;
        if (activityData != null && activity != null) {
            can = checkCanGetAward(player, activity, activityData, ActParamConstant.CHRISTMAS_314.size());
            isShow = DateHelper.nowBefore(ActParamConstant.CHRISTMAS_317);
        }
        boolean isEnd = false;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_CHRISTMAS);
        if(activityBase != null){
            if(activityBase.getEndTime() != null && activityBase.getEndTime().before(new Date())){
                isEnd = true;
            }
        }else {
            if(activity != null ){
                isEnd = true;
            }
        }
        if((can || isEnd) && isShow){
            return true;
        }
        return false;
    }

    protected CommonPb.Activity buildActivityPb(Activity activity, ActivityBase activityBase, boolean cangetAward, int tips) {
        CommonPb.Activity activity_ = PbHelper.createActivityPb(activityBase, cangetAward, tips);//.toBuilder().setDisplayTime((int) (activityBase.getPlan().getDisplayTime().getTime()/1000)).build()
        if (activityBase.getDisplayTime() == null && activityBase.getPlan() != null && activityBase.getPlan().getDisplayTime() != null) {
            activity_ = activity_.toBuilder().setDisplayTime((int) (activityBase.getPlan().getDisplayTime().getTime() / 1000)).build();
        }
        return activity_;
    }

    protected void overAndSendMail(String jobKey) {
        String[] arr = jobKey.split("_");
        int activityType = Integer.parseInt(arr[0]);
        int activityId = Integer.parseInt(arr[1]);
        int planKey = Integer.parseInt(arr[2]);
        LogUtil.activity("--------------------------------[" + activityType + "]活动结束，处理奖励，发送邮件");
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (CheckNull.isNull(activityMap)) {
            LogUtil.activity("--------------------------------[" + activityType + "]活动结束，处理奖励，发送邮件，Map<Integer, GlobalActivityData> is null");
            return;
        }
        GlobalActivityData globalActivityData = activityMap.get(activityType);
        if (Objects.isNull(globalActivityData)) {
            LogUtil.activity("--------------------------------[" + arr[0] + "]活动结束，处理奖励，发送邮件，GlobalActivityData is null");
            return;
        }

        int tmp311;
        List<Integer> tmps314;
        int tmp315;
        List<Integer> tmps316 ;
        if(activityType == ActivityConst.ACT_CHRISTMAS){
            tmps316 = ActParamConstant.CHRISTMAS_316;
            tmp315 = ActParamConstant.CHRISTMAS_315;
            tmp311 = ActParamConstant.CHRISTMAS_311;
            tmps314 = ActParamConstant.CHRISTMAS_314;
        }else {
            tmps316 = ActParamConstant.REPAIRCASTLE_316;
            tmp315 = ActParamConstant.REPAIRCASTLE_315;
            tmp311 = ActParamConstant.REPAIRCASTLE_311;
            tmps314 = ActParamConstant.REPAIRCASTLE_314;
        }

        List<List<Integer>> scoreList = new ArrayList<>();
        for (int camp : Constant.Camp.camps) {
            List<Integer> list = new ArrayList<>(2);
            list.add(camp);
            list.add(getCampValue(globalActivityData, camp));
            scoreList.add(list);
        }
        Collections.sort(scoreList, (o1, o2) -> o2.get(1) - o1.get(1));
        for (int i = 0; i < scoreList.size(); i++) {
            scoreList.get(i).add(i);
        }


        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().forEach(player -> {
            List<Integer> list = scoreList.stream().filter(o -> o.get(0) == player.getCamp()).findFirst().orElse(null);
            int getScore = tmps316.get(list.get(2));
            try {
                Activity activity = player.activitys.get(activityType);
                if (Objects.isNull(activity)) {
                    return;
                }
                //获得阵营排名积分
                if (getMyChipCount(activity) >= tmp315 * tmp311) {
                    addMyScore(player, activity, getScore, AwardFrom.ACT_CHRISTMAS_OVER_CHIP_AWARD);
                    activityDataManager.updRankActivity(player, activityType, getScore);

                    mailDataManager.sendNormalMail(player, MailConstant.ACT_CHRISTMAS_CAMP_RANK_MAIL, now, list.get(2) + 1, getScore, list.get(2) + 1, getScore);
                }

                //未领取奖励
                List<CommonPb.Award> notAwards = new ArrayList<>();
                for (int i = 0; i < tmps314.size(); i++) {
                    if (getAwardState0(player, activity, globalActivityData, i + 1) == 1) {
                        StaticActAward staticActAward = StaticActivityDataMgr.getSupplyMaxByParam(activity.getActivityId(), i + 1);
                        if (staticActAward != null) {
                            notAwards.addAll(PbHelper.createAwardsPb(staticActAward.getAwardList()));
                            setAwardState(activity, i + 1, 1);
                        }
                    }
                }
                if (!notAwards.isEmpty()) {
                    mailDataManager.sendAttachMail(player, notAwards, MailConstant.MOLD_ACT_UNREWARDED_REWARD, AwardFrom.ACT_CHRISTMAS_OVER_CHIP_AWARD, now, activity.getActivityType(), activity.getActivityId(), activity.getActivityType(), activity.getActivityId());
                }
                LogUtil.activity("圣诞活动结束, 处理玩家未领取的奖励发放阵营排名积分奖励, roleId=" + player.roleId + ", getScore=" + getScore + ", notAwards=" + notAwards.size());
            } catch (Exception e) {
                LogUtil.error(e, "圣诞活动结束, 处理奖励错误, roleId=" + player.roleId);
            }
        });
    }

    protected void overAutoConver(String jobKey){
        String[] arr = jobKey.split("_");
        int activityType = Integer.parseInt(arr[0]);
        int activityId = Integer.parseInt(arr[1]);
        int planKey = Integer.parseInt(arr[2]);
        LogUtil.activity("--------------------------------圣诞活动结束，兑换背包的碎片");
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (CheckNull.isNull(activityMap)) {
            LogUtil.activity("--------------------------------圣诞活动结束，兑换背包的碎片，Map<Integer, GlobalActivityData> is null");
            return;
        }
        GlobalActivityData globalActivityData = activityMap.get(activityType);
        if (Objects.isNull(globalActivityData)) {
            LogUtil.activity("--------------------------------圣诞活动结束，兑换背包的碎片，GlobalActivityData is null");
            return;
        }

        int chipId;
        if(activityType == ActivityConst.ACT_CHRISTMAS){
            chipId = CHIP_ID;
        }else {
            chipId = CHIP_REPAIRCASTLE;
        }

        StaticActBandit staticActBandit = StaticActivityDataMgr.getActBanditList(globalActivityData.getActivityId(), 1);
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().forEach(player -> {
            try {
                Activity activity = player.activitys.get(activityType);
                int chipNum = Math.toIntExact(rewardDataManager.getRoleResByType(player, staticActBandit.getDrop().get(0).get(0), staticActBandit.getDrop().get(0).get(1)));
                if (chipNum > 0) {
                    rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, chipId, chipNum, AwardFrom.ACT_OVER_AUTO_EXCHANGE_NON_USED_ITEMS, true, activity.getActivityType(), activity.getActivityId());
                    List<CommonPb.Award> awards = Collections.singletonList(PbHelper.createAwardPb(staticActBandit.getConvert().get(0).get(0), staticActBandit.getConvert().get(0).get(1), staticActBandit.getConvert().get(0).get(2) * chipNum));
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_BANDIT_CONVERT_AWARD, AwardFrom.ACT_UNREWARDED_RETURN, now,activity.getActivityType(), activity.getActivityId());
                }
                LogUtil.activity("圣诞活动结束, 自动兑换玩家背包的碎片, roleId=" + player.roleId + ", chipNum=" + chipNum);
            }catch (Exception e) {
                LogUtil.error(e, "圣诞活动结束, 自动兑换碎片错误, roleId=" + player.roleId);
            }
        });
    }

    public static void addJob(Scheduler scheduler, ActivityBase activityBase, Date now){
        Date jobTime = activityBase.getSendMailTime();// 发送邮件的时间
        StaticActivityPlan plan = activityBase.getPlan();
        if (jobTime.getTime() > now.getTime()) {
            StringBuilder name = new StringBuilder();
            name.append(plan.getActivityType()).append("_").append(plan.getActivityId()).append("_").append(plan.getKeyId());
            //移除已有的定时器
            QuartzHelper.removeJob(scheduler, name.toString(), "actMail");
            // 加入定时器
            QuartzHelper.addJob(scheduler, name.toString(), "actMail", ActMailJob.class, jobTime);
            LogUtil.debug("----------添加活动定时任务 :", plan.getName(), name.toString(), ", Date,", DateHelper.formatDateMiniTime(jobTime), "-------------------");
        }
        if(activityBase.getDisplayTime() != null && activityBase.getDisplayTime().getTime() > now.getTime()){
            QuartzHelper.removeJob(scheduler, String.valueOf(activityBase.getActivityType()), "actAutoConver");
            StringBuilder name = new StringBuilder();
            name.append(activityBase.getActivityType()).append("_").append(activityBase.getActivityId()).append("_").append(activityBase.getPlan().getKeyId());
            // 加入定时器
            QuartzHelper.addJob(scheduler, name.toString(), "actAutoConver", ActAutoConverJob.class, activityBase.getDisplayTime());
        }
    }
}
