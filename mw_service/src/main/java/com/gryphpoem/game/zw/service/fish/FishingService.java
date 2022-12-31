package com.gryphpoem.game.zw.service.fish;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticFishMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.fish.*;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.TitleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xwind
 * @date 2021/8/5
 */
@Service
public class FishingService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private TitleService titleService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;

    public void updateBaitTeamHeroId(Player player, int oldHeroId, int newHeroId) {
        try {
            player.getFishingData().getBaitTeams().forEach(baitTeam -> Stream.iterate(0, i -> i + 1).limit(baitTeam.getHeroIds().length).forEach(idx -> {
                if (baitTeam.getHeroIds()[idx] == oldHeroId) {
                    baitTeam.getHeroIds()[idx] = newHeroId;
                }
            }));
        } catch (Exception e) {
            LogUtil.error("英雄突破更新鱼饵采集队列的英雄id错误,", e);
        }
    }

    public GamePb4.FishingCollectBaitDispatchHerosRs collectBaitDispatchHeros(long roleId, GamePb4.FishingCollectBaitDispatchHerosRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int teamId = req.getTeamId();
        List<Integer> heroIds = req.getHeroIdsList();

        FishingData fishingData = player.getFishingData();
        BaitTeam baitTeam = fishingData.getBaitTeam(teamId);
        if (Objects.isNull(baitTeam) || heroIds.size() < 4) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "采集鱼饵派遣 参数错误", teamId, heroIds));
        }
        if (baitTeam.getLastDay() != 0 && baitTeam.getLastDay() == TimeHelper.getCurrentDay()) {
            throw new MwException(GameError.FISHING_COLLECT_BAIT_TEAM_COLLECTED.getCode(), GameError.err(roleId, "采集鱼饵派遣 此队列今日已采集过", teamId));
        }

        int totalSec = 0;
        List<AwardItem> getBaits = new ArrayList<>();
        int groupId = 0;

        List<Hero> heroes = new ArrayList<>();
        if (fishingData.getGuide() == 0) {
            StaticFishBait staticFishBait = StaticFishMgr.getStaticFishBait(StaticFishMgr.getFirstGetList().get(0).get(0));
            getBaits.add(new AwardItem(AwardType.PROP, staticFishBait.getPropID(), StaticFishMgr.getFirstGetList().get(0).get(1)));
            staticFishBait = StaticFishMgr.getStaticFishBait(StaticFishMgr.getFirstGetList().get(1).get(0));
            getBaits.add(new AwardItem(AwardType.PROP, staticFishBait.getPropID(), StaticFishMgr.getFirstGetList().get(1).get(1)));

            fishingData.setGuide(1);
        } else {
            for (Integer heroId : heroIds) {
                Hero hero = player.heros.get(heroId);
                if (Objects.nonNull(hero)) {
                    if (hero.isOnBaitTeam()) {
                        throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "采集鱼饵派遣 队列中有英雄正在采集", heroIds, heroId));
                    }
                    if (heroes.contains(hero)) {
                        throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "采集鱼饵派遣 队列中有重复英雄", heroIds, heroId));
                    }
                    heroes.add(hero);
                } else {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "采集鱼饵派遣 英雄id错误", heroIds, heroId));
                }
            }
            List<List<Integer>> baitIdList = StaticFishMgr.getNonGroupBaits();
            StaticFishBaitHerocombination staticFishBaitHerocombination = this.getHeroGroup(player, heroIds);
            if (Objects.isNull(staticFishBaitHerocombination)) {
                LogUtil.common(String.format("roleId=%d,钓鱼派遣英雄没有组合,英雄ids=%s", roleId, ListUtils.toString(heroes)));
            } else {
                groupId = staticFishBaitHerocombination.getId();
                baitIdList = staticFishBaitHerocombination.getCollectionResult();
            }
            //确认获得的鱼饵
            List<Integer> getBaitIds = baitIdList.stream().filter(o -> RandomHelper.isHitRangeIn10000(o.get(1))).map(o -> o.get(0)).collect(Collectors.toList());
            for (Integer baitId : getBaitIds) {
                StaticFishBait staticFishBait = StaticFishMgr.getStaticFishBait(baitId);
                if (Objects.nonNull(staticFishBait)) {
                    getBaits.add(new AwardItem(4, staticFishBait.getPropID(), 1));
                    totalSec += staticFishBait.getGettime();
                }
            }
        }

        heroes.forEach(hero -> hero.setOnBaitTeam(true));

        baitTeam.setHeroIds(heroIds);
        baitTeam.setState(FishingConst.TEAM_STATE_DOING);
        baitTeam.setGroupId(groupId);
        baitTeam.setLastDay(TimeHelper.getCurrentDay());
        baitTeam.setDispatchTime(TimeHelper.getCurrentSecond());
        baitTeam.setNeedSec(totalSec);
        baitTeam.getGetAwards().addAll(getBaits);

        LogLordHelper.commonLog("fishCollectDispatch", AwardFrom.FISHING_COLLECT_BAIT_DISPATCH, player, teamId, ListUtils.toString(heroIds), groupId, totalSec);

        GamePb4.FishingCollectBaitDispatchHerosRs.Builder resp = GamePb4.FishingCollectBaitDispatchHerosRs.newBuilder();
        resp.setBaitTeamInfo(buildBaitTeamInfo(baitTeam));
        resp.setFishingGuide(fishingData.getGuide());
        return resp.build();
    }

    private CommonPb.BaitTeamInfo.Builder buildBaitTeamInfo(BaitTeam baitTeam) {
        CommonPb.BaitTeamInfo.Builder builder = CommonPb.BaitTeamInfo.newBuilder();
        builder.setTeamId(baitTeam.getTeamId());
        Arrays.stream(baitTeam.getHeroIds()).forEach(o -> builder.addHeroIds(o));
        builder.setState(baitTeam.teamState());
        builder.setGroupId(baitTeam.getGroupId());
        builder.setUsedTimes(baitTeam.usedTimes());
        baitTeam.getGetAwards().forEach(o -> builder.addGetBaits(PbHelper.createAward(o)));
        builder.setDispatchTime(baitTeam.getDispatchTime());
        builder.setNeedSec(baitTeam.getNeedSec());
        return builder;
    }

    private StaticFishBaitHerocombination getHeroGroup(Player player, List<Integer> heroIds) {
        return StaticFishMgr.getStaticFishBaitHerocombinationList().stream().filter(o -> checkGroup(o, heroIds, player))
                .sorted(Comparator.comparing(StaticFishBaitHerocombination::getPriority).reversed()).findFirst().orElse(null);
    }

    private boolean checkGroup(StaticFishBaitHerocombination config, List<Integer> heroIds, Player player) {
        boolean satisfy = true;
//        //检查等级
//        for (Integer heroId : heroIds) {
//            Hero hero = player.heros.get(heroId);
//            if(Objects.isNull(hero) || hero.getLevel() < config.getHeroLV().get(0) || hero.getLevel() > config.getHeroLV().get(1)){
//                return false;
//            }
//        }
        //检查组合
        for (List<Integer> ids : config.getPersonnel()) {
            if (!ids.isEmpty()) {
                boolean isHas = false;
                for (Integer id : ids) {
                    if (heroIds.contains(id)) {
                        Hero hero = player.heros.get(id);
                        if (hero.getLevel() >= config.getHeroLV().get(0) && hero.getLevel() <= config.getHeroLV().get(1)) {
                            isHas = true;
                        }
                        break;
                    }
                }
                if (!isHas) {
                    satisfy = false;
                    break;
                }
            }
        }
        return satisfy;
    }

    public void handleBaitAltas(Player player, int propId) {
        Optional.ofNullable(StaticFishMgr.getStaticFishBaitByPropId(propId)).ifPresent(tmp -> {
            try {
                BaitAltas baitAltas = player.getFishingData().getBaitAltasMap().get(tmp.getBaitID());
                if (Objects.isNull(baitAltas)) {
                    baitAltas = new BaitAltas(TimeHelper.getCurrentSecond(), tmp.getBaitID(), true);
                    player.getFishingData().getBaitAltasMap().put(tmp.getBaitID(), baitAltas);
                }
            } catch (Exception e) {
                LogUtil.error("获得道具时处理鱼饵图鉴错误, ", e);
            }
        });
    }

    public GamePb4.FishingCollectBaitGetAwardRs collectBaitGetAward(long roleId, GamePb4.FishingCollectBaitGetAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int teamId = req.getTeamId();
        FishingData fishingData = player.getFishingData();
        BaitTeam baitTeam = fishingData.getBaitTeam(teamId);
        if (Objects.isNull(baitTeam)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "采集鱼饵领取奖励 队伍不存在", teamId));
        }
        if (baitTeam.teamState() != FishingConst.TEAM_STATE_GET) {
            throw new MwException(GameError.FISHING_COLLECT_BAIT_NO_CAN_GET.getCode(), GameError.err(roleId, "采集鱼饵领取奖励 不可领奖", teamId));
        }
        List<CommonPb.Award> awardList = new ArrayList<>();
        baitTeam.getGetAwards().forEach(o -> {
            awardList.add(PbHelper.createAward(o));
            //鱼饵图鉴
//            Optional.ofNullable(StaticFishMgr.getStaticFishBaitByPropId(o.getId())).ifPresent(tmp -> {
//                BaitAltas baitAltas = fishingData.getBaitAltasMap().get(tmp.getBaitID());
//                if(Objects.isNull(baitAltas)){
//                    baitAltas = new BaitAltas(TimeHelper.getCurrentSecond(),tmp.getBaitID(),true);
//                    fishingData.getBaitAltasMap().put(tmp.getBaitID(),baitAltas);
//                }
//            });
        });
        rewardDataManager.sendRewardByAwardList(player, awardList, AwardFrom.FISHING_COLLECT_BAIT_AWARD);

        baitTeam.setState(FishingConst.TEAM_STATE_GOT);

        //设置英雄状态
        Arrays.stream(baitTeam.getHeroIds()).mapToObj(o -> player.heros.get(o)).filter(Objects::nonNull).forEach(hero -> hero.setOnBaitTeam(false));

        if (fishingData.getGuide() == 1) fishingData.setGuide(2);

        LogLordHelper.commonLog("fishCollectAward", AwardFrom.FISHING_COLLECT_BAIT_AWARD, player, teamId, ListUtils.toString(baitTeam.getGetAwards()));

        GamePb4.FishingCollectBaitGetAwardRs.Builder resp = GamePb4.FishingCollectBaitGetAwardRs.newBuilder();
        resp.setBaitTeamInfo(buildBaitTeamInfo(baitTeam));
        resp.addAllGetBaits(awardList);
        resp.setFishingGuide(fishingData.getGuide());
        return resp.build();
    }

    public GamePb4.FishingCollectBaitGetInfoRs collectBaitGetInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        FishingData fishingData = player.getFishingData();

        GamePb4.FishingCollectBaitGetInfoRs.Builder resp = GamePb4.FishingCollectBaitGetInfoRs.newBuilder();
        fishingData.getBaitTeams().forEach(o -> resp.addBaitTeamInfo(buildBaitTeamInfo(o)));

        return resp.build();
    }

    public GamePb4.FishingFisheryGetInfoRs fisheryGetInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        FishingData fishingData = player.getFishingData();

        //兼容线上已抛竿的
        if (fishingData.getFishRod().getState() == FishingConst.ROD_STATE_DOING) {
            int fishId = whatFishId(player);
            fishingData.getFishRod().setFishId(fishId);
        }

        GamePb4.FishingFisheryGetInfoRs.Builder resp = GamePb4.FishingFisheryGetInfoRs.newBuilder();
        resp.setFisheryInfo(buildFisheryInfo(fishingData));
        return resp.build();
    }

    private CommonPb.FisheryInfo buildFisheryInfo(FishingData fishingData) {
        CommonPb.FisheryInfo.Builder builder = CommonPb.FisheryInfo.newBuilder();
        builder.setPlaceId(fishingData.getPlaceId());
        builder.setWeatherId(fishingData.getWeather());
        builder.setTitleId(fishingData.getTitleId());
        builder.setMasteries(fishingData.getMasteries());
        builder.setScore(fishingData.getScore());
        builder.setFishRodInfo(buildFishRodInfo(fishingData.getFishRod()));
        return builder.build();
    }

    public GamePb4.FishingFisheryThrowRodRs fisheryThrowRod(long roleId, GamePb4.FishingFisheryThrowRodRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int rodId = req.getRodId();
        int baitId = req.getBaitId();

        FishingData fishingData = player.getFishingData();
        FishRod fishRod = fishingData.getFishRod();
        if (fishRod.getState() != FishingConst.ROD_STATE_NON) {
            throw new MwException(GameError.FISHING_THROW_ROD_NOT_NON.getCode(), GameError.err(roleId, "抛竿失败 当前正在钓鱼中"));
        }
        StaticFishBait staticFishBait = StaticFishMgr.getStaticFishBait(baitId);
        if (Objects.isNull(staticFishBait)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "抛竿失败 参数错误", baitId));
        }
        rewardDataManager.checkPropIsEnough(player, staticFishBait.getPropID(), 1, "鱼饵");

        fishRod.setRodId(1);
        fishRod.setBaitId(staticFishBait.getBaitID());
        fishRod.setState(FishingConst.ROD_STATE_DOING);
        fishRod.setSliderIdx(RandomUtil.randomIntIncludeEnd(1, 4));

        rewardDataManager.subAndSyncProp(player, AwardType.PROP, staticFishBait.getPropID(), 1, AwardFrom.FISHING_THROW_ROD);

        if (fishingData.getGuide() == 2) fishingData.setGuide(3);

        int fishId = whatFishId(player);
        fishRod.setFishId(fishId);

        LogLordHelper.commonLog("fishThrowRod", AwardFrom.FISHING_THROW_ROD, player, baitId, fishId);

        //喜悦金秋-日出而作-在码头钓鱼
        TaskService.processTask(player, ETask.GOLDEN_AUTUMN_FISHING, 1);
        GamePb4.FishingFisheryThrowRodRs.Builder resp = GamePb4.FishingFisheryThrowRodRs.newBuilder();
        resp.setFishRodInfo(buildFishRodInfo(fishRod));
        resp.setFishingGuide(fishingData.getGuide());
        resp.setFishId(fishId);
        return resp.build();
    }

    private CommonPb.FishRodInfo buildFishRodInfo(FishRod fishRod) {
        CommonPb.FishRodInfo.Builder builder = CommonPb.FishRodInfo.newBuilder();
        builder.setRodId(fishRod.getRodId());
        builder.setBaitId(fishRod.getBaitId());
        builder.setState(fishRod.getState());
        builder.setSliderIdx(fishRod.getSliderIdx());
        builder.setFishId(fishRod.getFishId());
        return builder.build();
    }

    public GamePb4.FishingFisheryStowRodRs fisheryStowRod(long roleId, GamePb4.FishingFisheryStowRodRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<Integer> colorsList = req.getColorsList();

        FishingData fishingData = player.getFishingData();
        FishRod fishRod = fishingData.getFishRod();

        if (fishRod.getState() != FishingConst.ROD_STATE_DOING) {
            throw new MwException(GameError.FISHING_STOW_ROD_NOT_DOING.getCode(), GameError.err(roleId, "收杆失败 当前未抛竿"));
        }

        if (colorsList.isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "收杆失败 参数错误", colorsList));
        }
        List<Integer> colors = new ArrayList<>(colorsList);
        Collections.sort(colors);
        StaticFishResults staticFishResults = StaticFishMgr.getStaticFishResults(colors.get(0));
        if (Objects.isNull(staticFishResults)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "收杆失败 参数错误", colors));
        }

        StaticFishBait staticFishBait = StaticFishMgr.getStaticFishBait(fishRod.getBaitId());
        if (Objects.isNull(staticFishBait)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "收杆失败 鱼饵不存在", fishRod.toString()));
        }

        CommonPb.FishInfo.Builder fishInfoBuilder = null;
        int getMastery = staticFishResults.getGetproficiency();
        int fishId = fishRod.getFishId(), fishNum = 0, fishSize = 0, fishScore;
        if (staticFishResults.getColorID() != 0) {
//            fishId = RandomUtil.randomByWeight(staticFishBait.getFishID(), tmps -> tmps.get(1)).get(0);
//            if(fishingData.getGuide() == 3){
//                fishId = StaticFishMgr.getFirstGetList().get(2).get(0);
//            }
            StaticFishattribute staticFishattribute = StaticFishMgr.getStaticFishattribute(fishId);
            if (Objects.isNull(staticFishattribute)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "收杆失败 鱼配置不存在", fishRod.toString(), fishId));
            }
            fishNum = staticFishResults.getFishingHaverest();
            int rdmSize = RandomUtil.randomIntIncludeEnd(staticFishattribute.getSizeChange().get(0), staticFishattribute.getSizeChange().get(1));
            fishSize = staticFishattribute.getBasesize() * rdmSize / 10000;
            fishScore = staticFishattribute.getBasegoal();
            if (staticFishResults.getColorID() == 2) {
                StaticFishProficiency staticFishProficiency = StaticFishMgr.getStaticFishProficiency(fishingData.getTitleId());
                if (Objects.nonNull(staticFishProficiency)) {
                    int multipleNum = RandomUtil.randomIntIncludeEnd(staticFishProficiency.getMultipleCrit().get(0), staticFishProficiency.getMultipleCrit().get(1));
                    fishNum *= multipleNum;
                    List<Integer> sizeUp = staticFishProficiency.getSizeUP().stream().filter(tmps -> tmps.get(0) == staticFishattribute.getQuality()).findFirst().orElse(null);
                    if (ListUtils.isNotBlank(sizeUp)) {
                        fishSize += fishSize * sizeUp.get(1) / 10000;
                    }
                    List<Integer> scoreUp = staticFishProficiency.getGoalUP().stream().filter(tmps -> tmps.get(0) == staticFishattribute.getQuality()).findFirst().orElse(null);
                    if (ListUtils.isNotBlank(scoreUp)) {
                        fishScore += fishScore * scoreUp.get(1) / 10000;
                    }
                }
            }
            fishScore *= fishNum;
            List<List<Integer>> scoreList = ListUtils.createItems(AwardType.MONEY, AwardType.Money.FISH_SCORE, fishScore);
            rewardDataManager.sendReward(player, scoreList, AwardFrom.FISHING_STOW_ROD);
//            fishingData.setScore(fishingData.getScore() + fishScore);

            FishingLog fishingLog = new FishingLog(1, fishId, fishNum, fishSize);
            fishingData.addFishingLog(fishingLog);

            FishAltas fishAltas = fishingData.getFishAltasMap().get(fishId);
            if (Objects.isNull(fishAltas)) {
                fishAltas = new FishAltas(TimeHelper.getCurrentSecond(), fishId, fishSize, true);
                fishingData.getFishAltasMap().put(fishId, fishAltas);
            } else {
                if (fishAltas.getSize() < fishSize) {
                    fishAltas.setSize(fishSize);
                }
            }

            fishInfoBuilder = CommonPb.FishInfo.newBuilder();
            fishInfoBuilder.setFishId(fishId);
            fishInfoBuilder.setSize(fishSize);
            fishInfoBuilder.setScore(fishScore);
            fishInfoBuilder.setNum(fishNum);
            fishInfoBuilder.setLogId(fishingLog.logId);

            //跑马灯公告
            if (staticFishattribute.getQuality() == 5) {
                chatDataManager.sendSysChat(ChatConst.CHAT_FISHING_STOWROD, player.getCamp(), 0, player.getCamp(), player.lord.getNick(), fishId, fishSize, fishNum);
            }
        }
        if (fishingData.getGuide() == 3) fishingData.setGuide(4);

        fishingData.setMasteries(fishingData.getMasteries() + getMastery);
        this.calcTitle(fishingData);

        fishRod.reset();

        LogLordHelper.commonLog("fishStowRod", AwardFrom.FISHING_STOW_ROD, player, staticFishResults.getColorID(), Arrays.toString(new int[]{fishId, fishSize, fishNum}), Arrays.toString(new int[]{fishingData.getTitleId(), fishingData.getMasteries()}));

        GamePb4.FishingFisheryStowRodRs.Builder resp = GamePb4.FishingFisheryStowRodRs.newBuilder();
        resp.setResult(staticFishResults.getColorID());
        if (Objects.nonNull(fishInfoBuilder)) {
            resp.setFishInfo(fishInfoBuilder);

            //完成一次钓鱼
            taskDataManager.updTask(player, TaskType.COND_FISHING_MASTER, 1);
            //喜悦金秋-日出而作-在码头钓到鱼
            TaskService.processTask(player, ETask.GOLDEN_AUTUMN_CATCH_FISH, fishNum);
            //称号-在码头钓到鱼
            titleService.processTask(player, ETask.GOLDEN_AUTUMN_CATCH_FISH, fishNum);
            //战令任务
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_FISHING_MASTER, 1);
        }
        resp.setFisheryInfo(buildFisheryInfo(fishingData));
        resp.setFishingGuide(fishingData.getGuide());
        return resp.build();
    }

    private int whatFishId(Player player) {
        FishingData fishingData = player.getFishingData();
        FishRod fishRod = fishingData.getFishRod();
        StaticFishBait staticFishBait = StaticFishMgr.getStaticFishBait(fishRod.getBaitId());
        int fishId = RandomUtil.randomByWeight(staticFishBait.getFishID(), tmps -> tmps.get(1)).get(0);
        if (fishingData.getGuide() == 3) {
            fishId = StaticFishMgr.getFirstGetList().get(2).get(0);
        }
        return fishId;
    }

    private void calcTitle(FishingData fishingData) {
        StaticFishProficiency nextTitle = StaticFishMgr.getStaticFishProficiency(fishingData.getTitleId() + 1);
        if (Objects.isNull(nextTitle) || fishingData.getMasteries() < nextTitle.getExp()) {
            return;
        }
        fishingData.setTitleId(nextTitle.getTitleID());
        fishingData.setMasteries(fishingData.getMasteries() - nextTitle.getExp());
        this.calcTitle(fishingData);
    }

    public GamePb4.FishingAltasRs getAltas(long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.FishingAltasRs.Builder resp = GamePb4.FishingAltasRs.newBuilder();
        resp.setType(type);
        if (type == 1) {
            player.getFishingData().getBaitAltasMap().values().forEach(o -> resp.addAltasInfo(buildFishingAltasInfo(o)));
        } else {
            player.getFishingData().getFishAltasMap().values().forEach(o -> resp.addAltasInfo(buildFishingAltasInfo(o)));
        }

        return resp.build();
    }

    private CommonPb.FishingAltasInfo buildFishingAltasInfo(Altas altas) {
        CommonPb.FishingAltasInfo.Builder builder = CommonPb.FishingAltasInfo.newBuilder();
        builder.setId(altas.getId());
        builder.setStamp(altas.getStamp());
        builder.setIsNew(altas.isNew());
        if (altas instanceof FishAltas) {
            FishAltas fishAltas = (FishAltas) altas;
            builder.setFishMaxSize(fishAltas.getSize());
        }
        return builder.build();
    }


    public GamePb4.FishingFishLogRs fishLogs(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb4.FishingFishLogRs.Builder resp = GamePb4.FishingFishLogRs.newBuilder();
        player.getFishingData().getFishingLogs().forEach(o -> resp.addLogInfos(buildFishLogInfo(o)));

        return resp.build();
    }

    private CommonPb.FishLogInfo buildFishLogInfo(FishingLog fishingLog) {
        CommonPb.FishLogInfo.Builder builder = CommonPb.FishLogInfo.newBuilder();
        builder.setLogId(fishingLog.logId);
        builder.setPlaceId(fishingLog.placeId);
        builder.setFishId(fishingLog.fishId);
        builder.setFishNum(fishingLog.fishNum);
        builder.setFishSize(fishingLog.fishSize);
        builder.setShareTimes(fishingLog.shareTimes);
        return builder.build();
    }

    public GamePb4.FishingShareFishLogRs shareFishLog(long roleId, long logId) throws MwException {
//        Player player = playerDataManager.checkPlayerIsExist(roleId);
//        FishingLog fishingLog = player.getFishingData().getFishingLogs().stream().filter(o -> o.logId==logId).findAny().orElse(null);
//        if(Objects.isNull(fishingLog)){
//            throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"分享钓鱼日志 参数错误",logId));
//        }
//
//        if(fishingLog.shareTimes >= StaticFishMgr.getShareLogLimit()){
//            throw new MwException(GameError.FISHING_SHARE_FISH_LIMIT.getCode(),GameError.err(roleId,"分享鱼达到上限",fishingLog));
//        }
//
////        //分享到阵营频道
////        chatDataManager.sendSysChat(ChatConst.CHAT_FISHING_SHARE_FISH, player.lord.getCamp(), 0,
////                player.lord.getCamp(), player.lord.getNick(),fishingLog.fishId,fishingLog.fishSize,fishingLog.fishNum);
//
//        fishingLog.shareTimes = fishingLog.shareTimes + 1;

        GamePb4.FishingShareFishLogRs.Builder resp = GamePb4.FishingShareFishLogRs.newBuilder();
        return resp.build();
    }

    public GamePb4.FishingShopGetInfoRs shopGetInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb4.FishingShopGetInfoRs.Builder resp = GamePb4.FishingShopGetInfoRs.newBuilder();
        resp.setShopInfo(buildFishShopInfo(player.getFishingData()));
        return resp.build();
    }

    private CommonPb.FishShopInfo buildFishShopInfo(FishingData fishingData) {
        CommonPb.FishShopInfo.Builder builder = CommonPb.FishShopInfo.newBuilder();
        builder.setScore(fishingData.getScore());
        fishingData.getFishShop().getLimit().entrySet().forEach(o -> builder.addExchanges(PbHelper.createTwoIntPb(o.getKey(), o.getValue())));
        return builder.build();
    }

    public GamePb4.FishingShopExchangeRs shopExchange(long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticFishShop staticFishShop = StaticFishMgr.getStaticFishShop(id);
        if (Objects.isNull(staticFishShop)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "渔市兑换 配置不存在", id));
        }
        FishingData fishingData = player.getFishingData();
        int limit = fishingData.getFishShop().getLimit().getOrDefault(id, 0);
        if (limit >= staticFishShop.getLimit()) {
            throw new MwException(GameError.FISHING_SHOP_EXCHAGE_LIMIT.getCode(), GameError.err(roleId, "渔市兑换 达到上限", id));
        }
        rewardDataManager.checkAndSubPlayerRes(player, staticFishShop.getExpendProp(), AwardFrom.FISHING_SHOP_EXCHANGE);

        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticFishShop.getAwardList(), AwardFrom.FISHING_SHOP_EXCHANGE);

        fishingData.getFishShop().getLimit().put(id, limit + 1);

        GamePb4.FishingShopExchangeRs.Builder resp = GamePb4.FishingShopExchangeRs.newBuilder();
        resp.setShopInfo(buildFishShopInfo(fishingData));
        resp.addAllAward(awardList);
        return resp.build();
    }

    public GamePb4.FishingGetFishAltasAwardRs getFishAltasAward(long roleId, int altasId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        FishAltas fishAltas = player.getFishingData().getFishAltasMap().get(altasId);
        if (Objects.isNull(fishAltas)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "领取鱼图鉴奖励 图鉴不存在", altasId));
        }
        if (!fishAltas.isNew()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "领取鱼图鉴奖励 已被领取", fishAltas));
        }
        StaticFishattribute staticFishattribute = StaticFishMgr.getStaticFishattribute(fishAltas.getId());
        if (Objects.isNull(staticFishattribute)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "领取鱼图鉴奖励 配置不存在", altasId));
        }
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticFishattribute.getReward(), AwardFrom.FISHING_GET_FISH_ALTAS_AWARD);

        fishAltas.setNew(false);

        GamePb4.FishingGetFishAltasAwardRs.Builder resp = GamePb4.FishingGetFishAltasAwardRs.newBuilder();
        resp.setAltasInfo(buildFishingAltasInfo(fishAltas));
        resp.addAllAwards(awardList);

        return resp.build();
    }

    @GmCmd("fish")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "resetBaitTeams":
                //重置采集队列
                List<BaitTeam> baitTeams = new ArrayList<>(FishingData.BAIT_TEAMS);
                Stream.iterate(1, n -> n + 1).limit(FishingData.BAIT_TEAMS).forEach(o -> baitTeams.add(new BaitTeam(o)));
                player.getFishingData().setBaitTeams(baitTeams);
                break;
            case "resetHeroState":
                //重置所有的英雄状态
                player.heros.values().forEach(o -> o.setOnBaitTeam(false));
                break;
            case "fishScore":
                int add = Integer.parseInt(params[1]);
                if (add > 0) {
                    rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.FISH_SCORE, add, AwardFrom.DO_SOME);
                } else if (add < 0) {
                    List<List<Integer>> tmpss = ListUtils.createItems(AwardType.MONEY, AwardType.Money.FISH_SCORE, Math.abs(add));
                    rewardDataManager.subPlayerResHasChecked(player, tmpss, 1, AwardFrom.DO_SOME);
                }
                break;
            default:
        }
    }
}
