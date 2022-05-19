package com.gryphpoem.game.zw.service.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticActExchange;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishing;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishingLv;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.activity.Fishing;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 年年有鱼
 * @author xwind
 * @date 2021/12/17
 */
@Service
public class Year2022FishService extends AbsActivityService implements GmCmdService {
    private int[] actTypes = {ActivityConst.ACT_NEWYEAR_2022_FISH};

    private static final int SAVEMAP_STATE = 0;//状态 0未开始 1已开始
    private static final int SAVEMAP_SCORE = 1;//积分
    private static final int SAVEMAP_LV = 2;//难度
    private static final int SAVEMAP_KEY = 3;//唯一id
//    private static final int SAVEMAP_LVEXP = 0x5;//当前难度的积分
    private static final int SAVEMAP_BEGINSTAMP = 6;//开始时间戳秒
    private static final String DATAMAP_FISH = "FISH";//value:[[Fishing],[Fishing]]
    private static final int SAVEMAP_ROUND_KEY = 7;//

    @Autowired
    private ChatDataManager chatDataManager;

    public GamePb5.YearFishBeginRs beginFishing(long roleId,int actType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = super.checkAndGetActivityBase(player,actType);
        Activity activity = super.checkAndGetActivity(player,actType);
        if(!super.isOpenStage(activityBase)){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(),GameError.err(roleId,"年年有鱼开始时活动未开启"));
        }
//        if(activity.getSaveMap().getOrDefault(SAVEMAP_STATE,0) != 0){
//            throw new MwException(GameError.YEARFISH_BEGIN_NO_END.getCode(),GameError.err(roleId,"年年有鱼开始时活动未结束"));
//        }
        List<Integer> timesList = StaticDataMgr.getFishDayFree();
        int dayUsedFreeTimes = dayUsedFreeTimes(activity);
        int dayBoughtTimes = dayBoughtTimes(activity);
        if(dayUsedFreeTimes >= timesList.get(0) && dayBoughtTimes >= timesList.get(1)){//今日达到次数限制
            throw new MwException(GameError.YEARFISH_TIMES_LIMIT.getCode(),GameError.err(roleId,"年年有鱼无法开始达到次数上限"));
        }

        int currLv = activity.getSaveMap().getOrDefault(SAVEMAP_LV,1);
        StaticFishingLv staticFishingLv = StaticDataMgr.getStaticFishingLv(activity.getActivityId(),currLv);
        if(Objects.isNull(staticFishingLv)){
            throw new MwException(GameError.NO_CONFIG.getCode(),GameError.err(roleId,"年年有鱼开始时活动配置不存在",activity.getActivityId(),currLv));
        }

        //消耗
        if(dayUsedFreeTimes < timesList.get(0)){//消耗免费次数
            dayUsedFreeTimes = this.addDayUsedFreeTimes(activity);
        }else {//消耗钻石
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY,AwardType.Money.GOLD,timesList.get(2),AwardFrom.YEAR_FISH_BEGIN);
            dayBoughtTimes = this.addDayBoughtTimes(activity);
        }

        int wave = RandomUtil.randomIntIncludeEnd(staticFishingLv.getWave().get(0),staticFishingLv.getWave().get(1));
        List<List<Fishing>> fishingList = new ArrayList<>();
        for(int i=0;i<wave;i++){
            List<Integer> list1 = RandomUtil.getWeightByList(staticFishingLv.getFishId(),f -> f.get(1));
            int num = RandomUtil.randomIntIncludeEnd(list1.get(2),list1.get(3));
            List<Fishing> waveList = new ArrayList<>();
            Stream.iterate(0,j->j+1).limit(num).forEach(a -> {
                Fishing fishing = new Fishing(genKey(activity),list1.get(0));
                waveList.add(fishing);
            });
            fishingList.add(waveList);
        }
        activity.getSaveMap().put(SAVEMAP_STATE,1);
        activity.getDataMap().put(DATAMAP_FISH,JSON.toJSONString(fishingList));
        activity.getSaveMap().put(SAVEMAP_BEGINSTAMP, TimeHelper.getCurrentSecond());
        activity.getStatusCnt().put(SAVEMAP_ROUND_KEY,genKey(activity));
        LogUtil.activity(String.format("%s年年有鱼roundKey=%s开始,fishData=%s",player.roleId,activity.getStatusCnt().get(SAVEMAP_ROUND_KEY),activity.getDataMap().get(DATAMAP_FISH)));

        GamePb5.YearFishBeginRs.Builder resp = GamePb5.YearFishBeginRs.newBuilder();
        resp.setActType(actType);
        resp.setSceneInfo(buildYearFishSceneInfo(activity));
        resp.setUsedFreeTimes(dayUsedFreeTimes);
        resp.setBoughtTimes(dayBoughtTimes);
        return resp.build();
    }

    private long genKey(Activity activity){
        return activity.getStatusCnt().compute(SAVEMAP_KEY,(k,v)->{
            if(Objects.isNull(v)) return 100000L;
            else return v + 1;
        });
    }

    private int dayUsedFreeTimes(Activity activity){
        return activity.getPropMap().getOrDefault(TimeHelper.getCurrentDay(),0);
    }
    private int dayBoughtTimes(Activity activity){
        return activity.getPropMap().getOrDefault(TimeHelper.getCurrentDay0(),0);
    }
    private int addDayUsedFreeTimes(Activity activity){
        return activity.getPropMap().merge(TimeHelper.getCurrentDay(),1,Integer::sum);
    }
    private int addDayBoughtTimes(Activity activity){
        return activity.getPropMap().merge(TimeHelper.getCurrentDay0(),1,Integer::sum);
    }

//    private int genXY(Activity activity,Fishing fishing){
//        int currX = activity.getSaveMap().getOrDefault(SAVEMAP_X,1);
//        StaticFishing staticFishing = StaticDataMgr.getStaticFishing(fishing.getCid());
//        String hedao = activity.getDataMap().getOrDefault(DATAMAP_Y,"[0,0,0,0]");
//        List<Integer> hedaoList = JSONArray.parseArray(hedao,Integer.class);
//    }

    public GamePb5.YearFishEndRs endFishing(long roleId,int actType,List<Integer> fishKeys) throws MwException {
        Player player = super.playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = super.checkAndGetActivityBase(player,actType);
        Activity activity = super.checkAndGetActivity(player,actType);
        GlobalActivityData globalActivityData = super.checkAndGetGlobalActivity(player,actType);
        if(!super.isOpenStage(activityBase)){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(),GameError.err(roleId,"年年有鱼结算时活动未开启"));
        }
        if(activity.getSaveMap().getOrDefault(SAVEMAP_STATE,0) != 1){
            throw new MwException(GameError.YEARFISH_END_NO.getCode(),GameError.err(roleId,"年年有鱼未开始不能结算"));
        }
        String fishJson = activity.getDataMap().get(DATAMAP_FISH);
        if(Objects.isNull(fishJson)){
            throw new MwException(GameError.YEARFISH_END_NO.getCode(),GameError.err(roleId,"年年有鱼结算失败没有鱼的数据"));
        }
        boolean illegal = false;
        Set<Integer> set = new HashSet<>(fishKeys);
        if(set.size() != fishKeys.size()){
//            throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"年年有鱼结算参数错误fishKeys有重复key 可能作弊了"));
            illegal = true;
            LogUtil.activity(String.format("%s年年有鱼结算可能作弊fishKeys=%s,set=%s",player.roleId, fishKeys.size(),set.size()));
        }
        List<List<Fishing>> fishingList = JSON.parseObject(fishJson,new TypeReference<List<List<Fishing>>>(){});
        int getScore = 0;
        Map<Integer,Integer> fishSum = new HashMap<>();
        boolean hasLong = false;
        for (Integer fishKey : fishKeys) {
            Fishing fishing = null;
            for (List<Fishing> fishings : fishingList) {
                fishing = fishings.stream().filter(o -> o.getKey()==fishKey).findFirst().orElse(null);
                if(Objects.nonNull(fishing)){
                    break;
                }
            }
            if(Objects.nonNull(fishing)){
                StaticFishing staticFishing = StaticDataMgr.getStaticFishing(fishing.getCid());
                getScore += staticFishing.getScore();
                fishSum.merge(fishing.getCid(),1,Integer::sum);
                if(staticFishing.getId() == 3){
                    hasLong = true;
                }
            }else {
//                throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"年年有鱼结算找不到这个鱼 可能作弊了"));
                illegal = true;
                LogUtil.activity(String.format("%s年年有鱼结算可能作弊fishKey=%s不存在",player.roleId, fishKey));
            }
        }
        //作弊 本轮不算积分
        if(illegal){
            getScore = 0;
        }

        this.addScore(activity,player,getScore,AwardFrom.YEAR_FISH_END);
        super.addGeneralRank(player,getScore,TimeHelper.getCurrentSecond(),activity,globalActivityData);
        int currLv = activity.getSaveMap().getOrDefault(SAVEMAP_LV,1);
        StaticFishingLv nextLv = StaticDataMgr.getStaticFishingLv(activity.getActivityId(),currLv + 1);
        if(Objects.nonNull(nextLv)){
            if(getScore >= nextLv.getUpScore()){
                activity.getSaveMap().put(SAVEMAP_LV,nextLv.getLv());
                LogLordHelper.commonLog("yearFishLv",AwardFrom.YEAR_FISH_END,player,nextLv.getLv(),getScore,DataResource.serverId,player.getCamp());
//                activity.getSaveMap().put(SAVEMAP_LVEXP,0);
            }else {
//                int currExp = activity.getSaveMap().getOrDefault(SAVEMAP_LVEXP,0);
//                if(getScore > currExp){
//                    activity.getSaveMap().put(SAVEMAP_LVEXP,getScore);
//                }
            }
        }

        LogUtil.activity(String.format("%s年年有鱼roundKey=%s结算,获得积分=%s,fishKeys=%s",player.roleId,activity.getStatusCnt().get(SAVEMAP_ROUND_KEY),getScore,ListUtils.toString(fishKeys)));

        this.clearDataOnEnd(activity);

        //跑马灯
        if(hasLong){
            chatDataManager.sendSysChat(ChatConst.YEARFISH_LONG,player.getCamp(),0,player.getCamp(),player.lord.getNick());
        }

        GamePb5.YearFishEndRs.Builder resp = GamePb5.YearFishEndRs.newBuilder();
        resp.setActType(actType);
        fishSum.entrySet().forEach(entry -> resp.addFishing(PbHelper.createTwoIntPb(entry.getKey(),entry.getValue())));
        resp.setGetScore(getScore);
        resp.setCurrLv(activity.getSaveMap().getOrDefault(SAVEMAP_LV,1));
        resp.setCurrLvExp(getScore);
        resp.setCurrScore(activity.getSaveMap().getOrDefault(SAVEMAP_SCORE,0));
        resp.setIllegal(illegal?1:0);
        return resp.build();
    }

    private void addScore(Activity activity, Player player, int add, AwardFrom awardFrom) {
        if (add > 0) {
            int val = activity.getSaveMap().merge(SAVEMAP_SCORE, add, Integer::sum);
            LogLordHelper.activityScore("yearFishScore", awardFrom, player, val, add, activity, DataResource.serverId, player.getCamp());
        }
    }

    private void clearDataOnEnd(Activity activity){
        activity.getSaveMap().remove(SAVEMAP_STATE);
        activity.getSaveMap().remove(SAVEMAP_BEGINSTAMP);
        activity.getDataMap().remove(DATAMAP_FISH);
        activity.getStatusCnt().remove(SAVEMAP_ROUND_KEY);
    }

    public GamePb5.YearFishShopExchangeRs shopExchange(long roleId, int actType, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player, actType);
        ActivityBase activityBase = super.checkAndGetActivityBase(player, actType);
        StaticActExchange staticActExchange = StaticActivityDataMgr.getActExchangeListByKeyId(keyId);
        if (Objects.isNull(staticActExchange)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "年年有鱼商店兑换找不到配置", keyId));
        }
        if (staticActExchange.getActivityId() != activity.getActivityId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "年年有鱼商店兑换错误 活动id不匹配", keyId, activity.getActivityId()));
        }
        int num = activity.getStatusMap().getOrDefault(keyId, 0);
        if (num >= staticActExchange.getNumberLimit()) {
            throw new MwException(GameError.YEARFISH_EXCHANGE_LIMIT.getCode(), GameError.err(roleId, "年年有鱼兑换达到上限"));
        }
        int score = activity.getSaveMap().getOrDefault(SAVEMAP_SCORE, 0);
        if (score < staticActExchange.getNeedPoint()) {
            throw new MwException(GameError.YEARFISH_EXCHANGE_SCORE_NO.getCode(), GameError.err(roleId, "年年有鱼兑换积分不够"));
        }
        score = score - staticActExchange.getNeedPoint();
        activity.getSaveMap().put(SAVEMAP_SCORE, score);
        LogLordHelper.activityScore("yearFishScore", AwardFrom.YEAR_FISH_EXCHANGE, player, score, -staticActExchange.getNeedPoint(), activity);
        activity.getStatusMap().merge(keyId, 1, Integer::sum);
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticActExchange.getAwardList(), AwardFrom.YEAR_FISH_EXCHANGE);
        GamePb5.YearFishShopExchangeRs.Builder resp = GamePb5.YearFishShopExchangeRs.newBuilder();
        resp.setShopInfo(buildYearFishShopInfo(activity));
        resp.addAllAwards(awardList);
        return resp.build();
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setYearFishInfo(buildYearFishInfo(activity));
        return builder;
    }

    private CommonPb.YearFishInfo buildYearFishInfo(Activity activity){
        CommonPb.YearFishInfo.Builder builder = CommonPb.YearFishInfo.newBuilder();
        builder.setSceneInfo(buildYearFishSceneInfo(activity));
        builder.setShopInfo(buildYearFishShopInfo(activity));
        builder.setCurrLv(activity.getSaveMap().getOrDefault(SAVEMAP_LV,1));
        builder.setCurrLvExp(0);
        builder.setUsedFreeTimes(dayUsedFreeTimes(activity));
        builder.setBoughtTimes(dayBoughtTimes(activity));
        return builder.build();
    }

    private CommonPb.YearFishSceneInfo buildYearFishSceneInfo(Activity activity){
        CommonPb.YearFishSceneInfo.Builder builder = CommonPb.YearFishSceneInfo.newBuilder();
        builder.setStatus(activity.getSaveMap().getOrDefault(SAVEMAP_STATE,0));
        builder.setBeginStamp(activity.getSaveMap().getOrDefault(SAVEMAP_BEGINSTAMP,0));
        String fishJson = activity.getDataMap().get(DATAMAP_FISH);
        if(Objects.nonNull(fishJson)){
            List<List<Fishing>> fishingList = JSON.parseObject(fishJson,new TypeReference<List<List<Fishing>>>(){});
            fishingList.forEach(tmps -> {
                CommonPb.FishWave.Builder fishWaveBuilder = CommonPb.FishWave.newBuilder();
                tmps.forEach(tmp -> {
                    CommonPb.MyFish.Builder myFishBuilder = CommonPb.MyFish.newBuilder();
                    myFishBuilder.setKey(tmp.getKey());
                    myFishBuilder.setCid(tmp.getCid());
                    fishWaveBuilder.addMyFish(myFishBuilder);
                });
                builder.addFishWave(fishWaveBuilder);
            });
        }
        return builder.build();
    }

    private CommonPb.YearFishShopInfo buildYearFishShopInfo(Activity activity){
        CommonPb.YearFishShopInfo.Builder builder = CommonPb.YearFishShopInfo.newBuilder();
        builder.setScore(activity.getSaveMap().getOrDefault(SAVEMAP_SCORE,0));
        List<StaticActExchange> list = StaticActivityDataMgr.getActExchangeListById(activity.getActivityId());
        list.forEach(o -> builder.addExchanged(PbHelper.createTwoIntPb(o.getKeyId(),activity.getStatusMap().getOrDefault(o.getKeyId(),0))));
        return builder.build();
    }

    @Override
    protected int[] getActivityType() {
        return actTypes;
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        GlobalActivityData globalActivityData = activityDataManager.getActivityMap().get(activityType);
        if(Objects.nonNull(globalActivityData)){
            List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activityId);
            int maxCond = actAwardList.get(actAwardList.size()-1).getCond();
            LinkedList<ActRank> rankLinkedList = globalActivityData.getPlayerRanks(activityType);
            LogUtil.activity("年年有鱼活动结束处理排行榜奖励,排行数据" + System.lineSeparator() + "" + JSON.toJSONString(rankLinkedList));
            AtomicInteger rank = new AtomicInteger(1);
            Player player;
            for (ActRank actRank : rankLinkedList) {
                if(rank.get() > maxCond){
                    break;
                }
                StaticActAward staticActAward = actAwardList.stream().filter(o -> rank.get()>=o.getParam().get(1)&&rank.get()<=o.getCond()).findFirst().orElse(null);
                if(Objects.nonNull(staticActAward)){
                    player = playerDataManager.getPlayer(actRank.getLordId());
                    List<CommonPb.Award> mailAwards = PbHelper.createAwardsPb(staticActAward.getAwardList());
                    mailDataManager.sendAttachMail(player,mailAwards, MailConstant.WORLD_WAR_PERSONAL_RANK_REWARD,AwardFrom.YEAR_FISH_RANK_AWARD,TimeHelper.getCurrentSecond(),activityType, activityType, rank.get());
                    LogUtil.activity("年年有鱼活动结束处理排行榜奖励,roleId=" + player.roleId + ",rank=" + rank.get() + ",award=" + JSON.toJSONString(staticActAward.getAwardList()));
                }
                rank.incrementAndGet();
            }
        }
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }

    @Override
    protected boolean hasGeneralRank() {
        return true;
    }

    @GmCmd("yearfish")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        Activity activity = super.getActivity(player,ActivityConst.ACT_NEWYEAR_2022_FISH);
        GlobalActivityData globalActivityData = super.getGlobalActivity(ActivityConst.ACT_NEWYEAR_2022_FISH);
        if(Objects.nonNull(activity) && Objects.nonNull(globalActivityData)){
            if(params[0].equalsIgnoreCase("addscore")){
                int add = Integer.parseInt(params[1]);
                this.addScore(activity,player,add,AwardFrom.DO_SOME);
                super.addGeneralRank(player,add,TimeHelper.getCurrentSecond(),activity,globalActivityData);
            }
            if(params[0].equalsIgnoreCase("clearlv")){
                activity.getSaveMap().remove(SAVEMAP_LV);
            }
            if(params[0].equalsIgnoreCase("clearrank")){
                Optional.ofNullable(globalActivityData.getPlayerRanks(player,ActivityConst.ACT_NEWYEAR_2022_FISH)).ifPresent(tmps -> tmps.clear());
            }
        }
    }
}
