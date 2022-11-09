package com.gryphpoem.game.zw.service.sandtable;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.sandtable.SandTableJob;
import com.gryphpoem.game.zw.quartz.jobs.sandtable.SandTableOpenEndRoundJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.FeatureCategory;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticSandTableAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticSandTableExchange;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.sandtable.*;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author xwind
 * @date 2020年12月24日
 */
@Service
public class SandTableContestService {

    public static final int[] LINES = {1, 2, 3};
    public static final int DISPATCH_NEED_SECOND = 3;

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private FightService fightService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private CampDataManager campDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private SeasonService seasonService;
    /**
     * 翻倍奖励
     */
    private static List<Integer> multipleAwardType;

    static {
        multipleAwardType = new ArrayList<>();
        multipleAwardType.add(AwardType.SANDTABLE_SCORE);
    }

    public GamePb4.SandTableEnrollRs enroll(long roleId, int line, List<Integer> heroIds) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (Arrays.binarySearch(LINES, line) < 0) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ENROLL_INVALID_LINE.getCode(), "沙盘演武，报名失败，选择线路无效，lordId=" + roleId + ", line=" + line);
        }
        if (ListUtils.isBlank(heroIds)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ENROLL_INVALID_HEROS.getCode(), "沙盘演武，报名失败，未派遣英雄，lordId=" + roleId + ", line=" + line + ", heroIds=" + ListUtils.toString(heroIds));
        }
        int townLv = BuildingDataManager.getBuildingLv(BuildingType.COMMAND, player);
        if (townLv < 12) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ENROLL_BUILDLV_NOT_ENOUGHT.getCode(), "沙盘演武，报名失败，城镇中心等级不足，lordId=" + roleId + ", 城镇等级=" + townLv);
        }
        List<Hero> validHeroIds = checkHeroIds(player, heroIds);
        if (ListUtils.isBlank(validHeroIds)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ENROLL_INVALID_HEROS.getCode(), "沙盘演武，报名失败，没有出战的英雄，lordId=" + roleId + ", line=" + line + ", heroIds=" + ListUtils.toString(heroIds));
        }

        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        SandTableCamp sandTableCamp = sandTableContest.getSandTableCamp(player.getCamp());
        SandTableCamp.LineObject lineObject = sandTableCamp.getLine(line);
        LinePlayer linePlayer = new LinePlayer(roleId);
        for (SandTableCamp.LineObject obj : sandTableCamp.getLines().values()) {
            if (obj.list.contains(linePlayer)) {
                throw new MwException(GameError.SANDTABLE_CONTEST_ENROLLED.getCode(), "沙盘演武，报名失败，已报名，lordId=" + roleId + ", line=" + line + ", heroIds=" + ListUtils.toString(heroIds));
            }
        }

        if (sandTableContest.state() != SandTableContest.STATE_PREVIEW) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ENROLL_EXPIRED.getCode(), "沙盘演武，报名失败，不在报名时间内，lordId=" + roleId + ", line=" + line + ", heroIds=" + ListUtils.toString(heroIds));
        }

        if (!checkDispatchTime(sandTableContest)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ENROLL_DISPATCH_NO_TIME.getCode(), "沙盘演武，报名失败，派遣部队时间不够，lordId=" + roleId + ", line=" + line + ", heroIds=" + ListUtils.toString(heroIds));
        }

        validHeroIds.forEach(o -> {
            linePlayer.heroIds.add(o.getHeroId());
            o.setSandTableState(1);
        });

        setLinePlayerFightVal(player, linePlayer);

        linePlayer.line = line;
        lineObject.list.add(linePlayer);

        //sync camp info
        syncLinesInfoToCamp(sandTableCamp, sandTableContest.state());

        //任务
        TaskService.handleTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.SAND_TABLE.getCategory());
        ActivityDiaoChanService.completeTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.SAND_TABLE.getCategory());
        TaskService.processTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.SAND_TABLE.getCategory());

        GamePb4.SandTableEnrollRs.Builder resp = GamePb4.SandTableEnrollRs.newBuilder();
        resp.setMyLine(line);
        return resp.build();
    }

    private boolean checkDispatchTime(SandTableContest sandTableContest) {
        int now = TimeHelper.getCurrentSecond();
        int begin = (int) (sandTableContest.getOpenBeginDate().getTime() / 1000);
        return now + DISPATCH_NEED_SECOND <= begin;
    }

    private void setLinePlayerFightVal(Player player, LinePlayer linePlayer) {
        int fightVal = CalculateUtil.calcHeroesFightVal(player, linePlayer.heroIds);
        linePlayer.fightVal = fightVal;
    }

    private void syncLinesInfoToCamp(SandTableCamp sandTableCamp, int state) {
//        Turple<Integer, Long> currWinner = BerlinWar.getCurWinner();
//        if(currWinner != null){
//            Player winPlayer = playerDataManager.getPlayer(currWinner.getB().longValue());
//            if(winPlayer.isLogin){
//
//            }
//        }{
//            SandTableCamp.LineObject lineObject = sandTableCamp.getLine(line);
//            CommonPb.SandTableLineInfo.Builder lineInfoBuilder = buildLineInfo(lineObject);
//            int tfval = lineObject.list.stream().mapToInt(LinePlayer::getFightVal).sum();
//            lineInfoBuilder.setTfval(tfval);
//            builder.addLineInfo(lineInfoBuilder.build());
//        }
        GamePb4.SyncSandTableEnrollRs enrollRs = this.buildSyncEnrollRs(sandTableCamp, state);
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSandTableEnrollRs.EXT_FIELD_NUMBER, GamePb4.SyncSandTableEnrollRs.ext, enrollRs).build();
        playerService.syncMsgToCamp(msg, sandTableCamp.getCamp());
    }

    public void syncSandTablePreview(SandTableContest sandTableContest) {
        GamePb4.SyncSandTablePreviewRs.Builder resp = GamePb4.SyncSandTablePreviewRs.newBuilder();
        resp.setState(sandTableContest.state());
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSandTablePreviewRs.EXT_FIELD_NUMBER, GamePb4.SyncSandTablePreviewRs.ext, resp.build()).build();
        playerService.syncMsgToAll(msg);
    }

    private GamePb4.SyncSandTableEnrollRs buildSyncEnrollRs(SandTableCamp sandTableCamp, int state) {
        GamePb4.SyncSandTableEnrollRs.Builder builder = GamePb4.SyncSandTableEnrollRs.newBuilder();
        for (int line : LINES) {
            SandTableCamp.LineObject lineObject = sandTableCamp.getLine(line);
            List<LinePlayer> tmps = new ArrayList<>();
            lineObject.list.sort((o1, o2) -> o2.fightVal - o1.fightVal);
            if (state == SandTableContest.STATE_OPEN) {
                tmps.addAll(lineObject.list.stream().limit(Constant.SAND_TABLE_1057).collect(Collectors.toList()));
            } else {
                tmps.addAll(lineObject.list);
            }

            CommonPb.SandTableLineInfo.Builder lineInfoBuilder = this.buildLineInfo(line, tmps);
            builder.addLineInfo(lineInfoBuilder.build());
        }
        return builder.build();
    }

    public GamePb4.SandTableUpdateArmyRs updateArmy(long roleId, List<Integer> heroIds) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        if (ListUtils.isBlank(heroIds)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_UPDATE_ARMY_INVALID_PARAMS.getCode(), "沙盘演武，更新军队失败，无效参数，lordId=" + roleId + ", heroIds=" + ListUtils.toString(heroIds));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        SandTableCamp sandTableCamp = sandTableContest.getSandTableCamp(player.getCamp());
        LinePlayer linePlayer = sandTableCamp.getMyLine(roleId);
        if (Objects.isNull(linePlayer)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_UPDATE_ARMY_NO_ENROLL.getCode(), "沙盘演武，更新军队失败，未报名，lordId=" + roleId + ", heroIds=" + ListUtils.toString(heroIds));
        }
        List<Hero> validHeroIds = checkHeroIds(player, heroIds);
        if (ListUtils.isBlank(validHeroIds)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ENROLL_INVALID_HEROS.getCode(), "沙盘演武，更新军队失败，没有出战的英雄，lordId=" + roleId + ", heroIds=" + ListUtils.toString(heroIds));
        }
        if (sandTableContest.state() != SandTableContest.STATE_PREVIEW) {
            throw new MwException(GameError.SANDTABLE_CONTEST_UPDATE_ARMY_EXPIRED.getCode(), "沙盘演武，更新军队失败，此阶段无法更新，lordId=" + roleId + ", heroIds=" + ListUtils.toString(heroIds));
        }

        linePlayer.heroIds.forEach(o -> {
            Hero hero = player.heros.get(o);
            if (Objects.nonNull(hero)) {
                hero.setSandTableState(0);
            }
        });

        linePlayer.heroIds.clear();
        validHeroIds.forEach(o -> {
            linePlayer.heroIds.add(o.getHeroId());
            o.setSandTableState(1);
        });

        setLinePlayerFightVal(player, linePlayer);

        //sync camp info to boss
        if (player.lord.getJob() != PartyConstant.Job.KING) {

        }
        GamePb4.SyncSandTableEnrollRs enrollRs = this.buildSyncEnrollRs(sandTableCamp, sandTableContest.state());
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSandTableEnrollRs.EXT_FIELD_NUMBER, GamePb4.SyncSandTableEnrollRs.ext, enrollRs).build();
        playerService.syncMsgToCamp(msg, player.getCamp());

        GamePb4.SandTableUpdateArmyRs.Builder resp = GamePb4.SandTableUpdateArmyRs.newBuilder();
        return resp.build();
    }

    private List<Hero> checkHeroIds(Player player, List<Integer> heroIds) {
        List<Hero> valid = new ArrayList<>();
        for (int heroId : heroIds) {
            Hero hero = player.heros.get(heroId);
            if (Objects.isNull(hero)) continue;
            valid.add(hero);
        }
        return valid;
    }

    public GamePb4.SandTableChangeLineRs changeLine(long roleId, int line) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        SandTableCamp sandTableCamp = sandTableContest.getSandTableCamp(player.getCamp());
        LinePlayer linePlayer = sandTableCamp.getMyLine(roleId);
        if (Objects.isNull(linePlayer)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_CHANGE_LINE_NO_ENROLL.getCode(), "沙盘演武，更换线路失败，未报名，lordId=" + roleId + ", line=" + line);
        }
        if (!ArrayUtils.contains(LINES, line) || line == linePlayer.line) {
            throw new MwException(GameError.SANDTABLE_CONTEST_CHANGE_LINE_NO_ENROLL.getCode(), "沙盘演武，更换线路失败，无效参数，lordId=" + roleId + ", line=" + line + ", myLine=" + linePlayer.line);
        }
        if (sandTableContest.state() != SandTableContest.STATE_PREVIEW) {
            throw new MwException(GameError.SANDTABLE_CONTEST_CHANGE_LINE_EXPIRED.getCode(), "沙盘演武，更换线路失败，此阶段无法更换，lordId=" + roleId + ", line=" + line + ", myLine=" + linePlayer.line);
        }

        if (sandTableCamp.removeLinePlayer(linePlayer)) {
            linePlayer.line = line;
            sandTableCamp.getLine(line).list.add(linePlayer);
        }

        //sync camp info
        syncLinesInfoToCamp(sandTableCamp, sandTableContest.state());

        GamePb4.SandTableChangeLineRs.Builder resp = GamePb4.SandTableChangeLineRs.newBuilder();
        resp.setMyLine(linePlayer.line);
        return resp.build();
    }

    public GamePb4.SandTableAdjustLineRs adjustLine(long roleId, int line1, int line2) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!ArrayUtils.contains(LINES, line1) || !ArrayUtils.contains(LINES, line2) || line1 == line2) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ADJUST_INVALID_PARAMS.getCode(), "沙盘演武，调整分路部署，无效参数，lordId=" + roleId + ", line1=" + line1 + ", line2=" + line2);
        }
        if (player.lord.getJob() != PartyConstant.Job.KING) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ADJUST_NO_WINNER.getCode(), "沙盘演武，调整分路部署，不是本阵营大哥，lordId=" + roleId + ", line1=" + line1 + ", line2=" + line2);
        }
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();

        if (sandTableContest.state() != SandTableContest.STATE_OPEN) {
            throw new MwException(GameError.SANDTABLE_CONTEST_ADJUST_EXPIRED.getCode(), "沙盘演武，调整分路部署，此阶段无法调整，lordId=" + roleId + ", line1=" + line1 + ", line2=" + line2);
        }

        SandTableCamp sandTableCamp = sandTableContest.getSandTableCamp(player.getCamp());
        SandTableCamp.LineObject lineObject1 = sandTableCamp.getLine(line1);
        SandTableCamp.LineObject lineObject2 = sandTableCamp.getLine(line2);
        sandTableCamp.getLines().put(line1, lineObject2);
        sandTableCamp.getLines().put(line2, lineObject1);
        lineObject1.list.forEach(o -> o.line = line2);
        lineObject2.list.forEach(o -> o.line = line1);

        //sync camp info
        syncLinesInfoToCamp(sandTableCamp, sandTableContest.state());

        //同步玩家路线
        GamePb4.SyncSandTablePlayerLineRs.Builder playerLineRs = GamePb4.SyncSandTablePlayerLineRs.newBuilder();
        playerLineRs.setMyLine(line2);
        BasePb.Base msg1 = PbHelper.createSynBase(GamePb4.SyncSandTablePlayerLineRs.EXT_FIELD_NUMBER, GamePb4.SyncSandTablePlayerLineRs.ext, playerLineRs.build()).build();
        lineObject1.list.forEach(o -> playerService.syncMsgToPlayer(msg1, o.lordId));

        playerLineRs.setMyLine(line1);
        BasePb.Base msg2 = PbHelper.createSynBase(GamePb4.SyncSandTablePlayerLineRs.EXT_FIELD_NUMBER, GamePb4.SyncSandTablePlayerLineRs.ext, playerLineRs.build()).build();
        lineObject2.list.forEach(o -> playerService.syncMsgToPlayer(msg2, o.lordId));

        GamePb4.SandTableAdjustLineRs.Builder resp = GamePb4.SandTableAdjustLineRs.newBuilder();
        return resp.build();
    }

    public GamePb4.SandTableGetInfoRs getInfo(long roleId) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();

        GamePb4.SandTableGetInfoRs.Builder resp = GamePb4.SandTableGetInfoRs.newBuilder();

        resp.setState(sandTableContest.state());
        if (resp.getState() != SandTableContest.STATE_NOTOPEN) {
            resp.setPreViewBegin((int) (sandTableContest.getPreviewBeginDate().getTime() / 1000));
            resp.setOpenBegin((int) (sandTableContest.getOpenBeginDate().getTime() / 1000));
            resp.setOpenEnd((int) (sandTableContest.getOpenEndDate().getTime() / 1000));
            resp.setExchangeEnd((int) (sandTableContest.getExchangeEndDate().getTime() / 1000));

            SandTableCamp sandTableCamp = sandTableContest.getSandTableCamp(player.getCamp());
            LinePlayer linePlayer = sandTableCamp.getMyLine(roleId);
            if (Objects.nonNull(linePlayer)) {
                resp.setMyLine(linePlayer.line);
            }
            for (int line : LINES) {
                SandTableCamp.LineObject lineObject = sandTableCamp.getLine(line);
                List<LinePlayer> tmps = new ArrayList<>();
                lineObject.list.sort((o1, o2) -> o2.fightVal - o1.fightVal);
                if (resp.getState() == SandTableContest.STATE_OPEN) {
                    tmps.addAll(lineObject.list.stream().limit(Constant.SAND_TABLE_1057).collect(Collectors.toList()));
                } else {
                    tmps.addAll(lineObject.list);
                }
//                CommonPb.SandTableLineInfo.Builder builder = CommonPb.SandTableLineInfo.newBuilder();
//                builder.setLine(line);
//                builder.setPcount(tmps.size());
//                int tfval = tmps.stream().mapToInt(LinePlayer::getFightVal).sum();
//                builder.setTfval(tfval);
                CommonPb.SandTableLineInfo.Builder builder = this.buildLineInfo(line, tmps);
                resp.addLineInfo(builder.build());
            }
            if (resp.getState() == SandTableContest.STATE_OPEN) {
                SandTableGroup sandTableGroup = sandTableContest.getMatchGroup().get(sandTableContest.getRound());
                if (Objects.nonNull(sandTableGroup)) {
                    CommonPb.SandTableHisMatchInfo.Builder currMatchInfo_ = CommonPb.SandTableHisMatchInfo.newBuilder();
                    currMatchInfo_.setRound(sandTableGroup.round);
                    currMatchInfo_.setState(sandTableGroup.state);
                    currMatchInfo_.setTime(sandTableGroup.beginTime);
                    currMatchInfo_.setCamp1(sandTableGroup.camp1);
                    currMatchInfo_.setCamp2(sandTableGroup.camp2);
                    int nowSec = TimeHelper.getCurrentSecond();
                    if (sandTableGroup.beginTime > nowSec) {
                        currMatchInfo_.setLeftSecond(sandTableGroup.beginTime - nowSec);
                    }
                    resp.setCurrRoundMatchInfo(currMatchInfo_);
                } else {
                    LogUtil.debug("沙盘演武, 获取信息未获取到分组数据，Current Round = " + sandTableContest.getRound());
                }
            }
            resp.setShopInfo(buildSandTableShopInfo(player, sandTableContest));
        } else {
            //setting next open timestamp
            resp.setNextOpen(TimeHelper.getNextTimeStampByCron(Constant.SAND_TABLE_PREVIEW));
        }
        if (sandTableContest.getHisCampRanks().size() > 0) {
            resp.setUnlock(true);
        } else {
            resp.setUnlock(false);
        }
        return resp.build();
    }

    public GamePb4.SandTableHisRankRs getHisRank(long roleId) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        GamePb4.SandTableHisRankRs.Builder resp = GamePb4.SandTableHisRankRs.newBuilder();
        for (int i = sandTableContest.getHisCampRanks().size() - 1; i >= 0; i--) {
            resp.addHisRankInfo(sandTableHisRankInfo(sandTableContest.getHisCampRanks().get(i)));
        }
        return resp.build();
    }

    private CommonPb.SandTableHisRankInfo sandTableHisRankInfo(HisCampRank hisCampRank) {
        CommonPb.SandTableHisRankInfo.Builder builder = CommonPb.SandTableHisRankInfo.newBuilder();
        builder.setHisDate(hisCampRank.hisDate);
        hisCampRank.hisInfos.forEach(o -> builder.addHisCampInfo(sandTableHisCampInfo(o)));
        return builder.build();
    }

    private CommonPb.SandTableHisCampInfo sandTableHisCampInfo(HisCampRank.RankInfo rankInfo) {
        CommonPb.SandTableHisCampInfo.Builder builder = CommonPb.SandTableHisCampInfo.newBuilder();
        builder.setCampRank(rankInfo.rank);
        builder.setCamp(rankInfo.camp);
        builder.setScore(rankInfo.score);
        builder.setFlag(rankInfo.flag);
        builder.setKilled(rankInfo.killed);
        return builder.build();
    }

    private CommonPb.SandTableLineInfo.Builder buildLineInfo(int line, List<LinePlayer> tmps) {
        CommonPb.SandTableLineInfo.Builder builder = CommonPb.SandTableLineInfo.newBuilder();
        builder.setLine(line);
        builder.setPcount(tmps.size());
        int tfval = tmps.stream().mapToInt(LinePlayer::getFightVal).sum();
        builder.setTfval(tfval);
        return builder;
    }

    public GamePb4.SandTableHisContestRs getHisContest(long roleId) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        GamePb4.SandTableHisContestRs.Builder resp = GamePb4.SandTableHisContestRs.newBuilder();
        for (int i = sandTableContest.getHisMatches().size() - 1; i >= 0; i--) {
            resp.addHisContestInfo(sandTableHisContestInfo(sandTableContest.getHisMatches().get(i)));
        }
        return resp.build();
    }

    private CommonPb.SandTableHisContestInfo sandTableHisContestInfo(HisMatch hisMatch) {
        CommonPb.SandTableHisContestInfo.Builder builder = CommonPb.SandTableHisContestInfo.newBuilder();
        builder.setHisDate(hisMatch.hisDate);
        hisMatch.matchInfos.forEach(o -> {
            CommonPb.SandTableHisMatchInfo.Builder sandTableHisMatchInfo_ = CommonPb.SandTableHisMatchInfo.newBuilder();
            sandTableHisMatchInfo_.setRound(o.round);
            sandTableHisMatchInfo_.setState(o.state);
            sandTableHisMatchInfo_.setTime(o.time);
            sandTableHisMatchInfo_.setCamp1(o.camp1);
            sandTableHisMatchInfo_.setCamp2(o.camp2);
            sandTableHisMatchInfo_.setCamp1Score(o.score1);
            sandTableHisMatchInfo_.setCamp2Score(o.score2);
            sandTableHisMatchInfo_.setWinCamp(o.winCamp);
            if (o.state == 0) {
                sandTableHisMatchInfo_.setLeftSecond(Math.abs(o.time - TimeHelper.getCurrentSecond()));
            }
            builder.addHisMatchInfo(sandTableHisMatchInfo_);
        });
        return builder.build();
    }

    public GamePb4.SandTableReplayRs getReplay(long roleId, int hisDate, int round) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        if (hisDate <= 0) {
            if (sandTableContest.getMatchDate() == 0)
                hisDate = TimeHelper.getCurrentDay();
            else
                hisDate = sandTableContest.getMatchDate();
        }
        int finalHisDate = hisDate;
        HisMatch hisMatch = sandTableContest.getHisMatches().stream().filter(o -> o.hisDate == finalHisDate).findFirst().orElse(null);
        if (Objects.isNull(hisMatch)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_REPLAY_NODATA.getCode(), "沙盘演武, 获取重播数据(HisMatch is NULL), roleId=" + player.roleId + ", hisDate=" + hisDate + ", round=" + round);
        }
        HisMatch.MatchInfo matchInfo = hisMatch.matchInfos.stream().filter(o -> o.round == round).findFirst().orElse(null);
        if (Objects.isNull(matchInfo)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_REPLAY_NODATA.getCode(), "沙盘演武, 获取重播数据(MatchInfo is NULL), roleId=" + player.roleId + "，hisDate=" + hisDate + ", round=" + round);
        }
        GamePb4.SandTableReplayRs.Builder resp = GamePb4.SandTableReplayRs.newBuilder();
        resp.setCamp1(matchInfo.camp1);
        resp.setCamp2(matchInfo.camp2);
        resp.setScore1(matchInfo.score1);
        resp.setScore2(matchInfo.score2);
        matchInfo.fightReplays.entrySet().forEach(o -> {
            int line = o.getKey();
            List<FightReplay> replays = o.getValue();
            int result = matchInfo.linesResult.getOrDefault(line, 0);
            CommonPb.SandTableFightReplays.Builder builder_ = buildSandTableFightReplays(line, replays);
            builder_.setWinCamp(result);
            HisMatch.WarReportInfo warReportInfo = matchInfo.warReportInfos.get(line);
            builder_.setWarReportInfo(buildSandTableWarReportInfo(warReportInfo));
            resp.addFightReplay(builder_);
        });
        //
        return resp.build();
    }

    private CommonPb.SandTableWarReportInfo buildSandTableWarReportInfo(HisMatch.WarReportInfo warReportInfo) {
        CommonPb.SandTableWarReportInfo.Builder builder = CommonPb.SandTableWarReportInfo.newBuilder();
        builder.setTotalNum1(Objects.isNull(warReportInfo) ? 0 : warReportInfo.totalNum1);
        builder.setLeftNum1(Objects.isNull(warReportInfo) ? 0 : warReportInfo.leftNum1);
        builder.setTotalNum2(Objects.isNull(warReportInfo) ? 0 : warReportInfo.totalNum2);
        builder.setLeftNum2(Objects.isNull(warReportInfo) ? 0 : warReportInfo.leftNum2);
        return builder.build();
    }

    private CommonPb.SandTableWarReportInfo reverseBuildSandTableWarReportInfo(HisMatch.WarReportInfo warReportInfo) {
        CommonPb.SandTableWarReportInfo.Builder builder = CommonPb.SandTableWarReportInfo.newBuilder();
        builder.setTotalNum1(Objects.isNull(warReportInfo) ? 0 : warReportInfo.totalNum2);
        builder.setLeftNum1(Objects.isNull(warReportInfo) ? 0 : warReportInfo.leftNum2);
        builder.setTotalNum2(Objects.isNull(warReportInfo) ? 0 : warReportInfo.totalNum1);
        builder.setLeftNum2(Objects.isNull(warReportInfo) ? 0 : warReportInfo.leftNum1);
        return builder.build();
    }

    private CommonPb.SandTableFightReplays.Builder buildSandTableFightReplays(int line, List<FightReplay> replays) {
        CommonPb.SandTableFightReplays.Builder builder = CommonPb.SandTableFightReplays.newBuilder();
        builder.setLine(line);
        replays.forEach(o -> builder.addReplayInfo(buildSandTableReplayInfo(o)));
        return builder;
    }

    private CommonPb.SandTableReplayInfo buildSandTableReplayInfo(FightReplay fightReplay) {
        CommonPb.SandTableReplayInfo.Builder info = CommonPb.SandTableReplayInfo.newBuilder();
        CommonPb.SandTableFightObject obj1_ = buildSandTableFightObject(fightReplay.obj1);
        CommonPb.SandTableFightObject obj2_ = buildSandTableFightObject(fightReplay.obj2);
        info.setFObj1(obj1_);
        info.setFObj2(obj2_);
        info.setOnlyId(fightReplay.onlyIdx);
        info.setWarReportInfo(buildSandTableWarReportInfo(fightReplay.warReportInfo));
        return info.build();
    }

    private CommonPb.SandTableFightObject buildSandTableFightObject(FightReplay.FightObject obj) {
        CommonPb.SandTableFightObject.Builder builder = CommonPb.SandTableFightObject.newBuilder();
        builder.setLordId(obj.lordId);
        builder.setLordNick(obj.lordNick);
        builder.setAttackTimes(obj.attackTimes);
        builder.setIsWin(obj.isWin);
        int maxHp = obj.heroDetails.stream().mapToInt(o -> o.hp).sum();
        int lostHp = obj.heroDetails.stream().mapToInt(o -> o.lost).sum();
        builder.setHpInfo(PbHelper.createTwoIntPb(maxHp, maxHp - lostHp));
        return builder.build();
    }

    public void matchGroup() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        int idx11 = RandomUtils.nextInt(0, list.size());
        int camp11 = list.get(idx11);
        list.remove(idx11);
        int idx12 = RandomUtils.nextInt(0, list.size());
        int camp12 = list.get(idx12);
        list.remove(idx12);
        int camp21 = camp11;
        int camp22 = list.get(0);
        int camp31 = camp12;
        int camp32 = camp22;
        this.matchGroup(camp11, camp12, camp21, camp22, camp31, camp32);
    }

    private void matchGroup(int... camps) {
        try {
            SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
            int openBeginStamp = (int) (sandTableContest.getOpenBeginDate().getTime() / 1000);
            sandTableContest.getMatchGroup().put(1, new SandTableGroup(1, openBeginStamp + Constant.SAND_TABLE_1054, camps[0], camps[1]));
            sandTableContest.getMatchGroup().put(2, new SandTableGroup(2, openBeginStamp + Constant.SAND_TABLE_1054 * 2, camps[2], camps[3]));
            sandTableContest.getMatchGroup().put(3, new SandTableGroup(3, openBeginStamp + Constant.SAND_TABLE_1054 * 3, camps[4], camps[5]));

            this.addJob(sandTableContest);

            sandTableContest.addHisMatch();
            LogUtil.error("执行沙盘演武分组完成, Group=" + JSON.toJSONString(sandTableContest.getMatchGroup()));
        } catch (Exception e) {
            LogUtil.error("执行沙盘演武分组异常, ", e);
        }
    }

    public void addJob(SandTableContest sandTableContest) {
        Map<Integer, SandTableGroup> groupMap = sandTableContest.getMatchGroup();
        groupMap.entrySet().forEach(o -> {
            int round = o.getKey();
            SandTableGroup group = o.getValue();
            QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), SandTableJob.name_round + round, SandTableJob.groupName, SandTableOpenEndRoundJob.class, TimeHelper.getDateByStamp(group.beginTime));
        });
    }

    public void fightLines(int round) {
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        try {
            int currRound = sandTableContest.getRound();
            if (round != currRound) {
                LogUtil.error("沙盘演武执行战斗错误, 要执行的战斗的轮数和当前的轮数不一致, round=" + round + ", currRound=" + currRound);
                return;
            }
            this.fightLines();

            LogUtil.error("沙盘演武执行战斗完成, round=" + round + ", nextRound=" + sandTableContest.getRound());
        } catch (Exception e) {
            LogUtil.error("沙盘演武执行战斗异常, round=" + round + ", nextRound=" + sandTableContest.getRound(), e);
        }
    }

    public synchronized void fightLines() {
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        int round = sandTableContest.getRound();
        SandTableGroup group = sandTableContest.getMatchGroup().get(round);
        if (Objects.isNull(group) || group.state != 0) {
            LogUtil.error("沙盘演武执行战斗错误, 没有分组数据或此轮已结束, round=" + round + ", group=" + JSON.toJSONString(group));
            return;
        }

        LogUtil.error("沙盘演武处理所有线路战斗, round=" + round);

        SandTableCamp sandTableCamp1 = sandTableContest.getSandTableCamp(group.camp1);
        SandTableCamp sandTableCamp2 = sandTableContest.getSandTableCamp(group.camp2);

        //reset linePlayer data fightTimes,killedNum
        sandTableCamp1.resetLinePlayers();
        sandTableCamp2.resetLinePlayers();

        HisMatch hisMatch = this.getHistMatch(sandTableContest, sandTableContest.getMatchDate());
        HisMatch.MatchInfo matchInfo = getHisMatchInfo(hisMatch, round);
        for (int line : LINES) {
            SandTableCamp.LineObject lineA = sandTableCamp1.getLine(line);
            SandTableCamp.LineObject lineB = sandTableCamp2.getLine(line);

            fightLine(line, lineA, lineB, matchInfo);

            if (lineA.result == 1 && lineB.result == 0)
                matchInfo.linesResult.put(line, sandTableCamp1.getCamp());
            else if (lineB.result == 1 && lineA.result == 0)
                matchInfo.linesResult.put(line, sandTableCamp2.getCamp());
            else
                matchInfo.linesResult.put(line, 0);

            sandTableCamp1.setKilled(sandTableCamp1.getKilled() + lineA.killed);
            sandTableCamp2.setKilled(sandTableCamp2.getKilled() + lineB.killed);

            lineA.killed = 0;
            lineB.killed = 0;
        }
        fightResult(sandTableCamp1, sandTableCamp2);

        syncRoundOverToCamps(group, sandTableCamp1, sandTableCamp2);

        //his match
        if (Objects.nonNull(matchInfo)) {
            matchInfo.state = 1;
            matchInfo.score1 = sandTableCamp1.getFlag();
            matchInfo.score2 = sandTableCamp2.getFlag();
            matchInfo.winCamp = winCamp(sandTableCamp1, sandTableCamp2);
        }

        sendRoundOverWarReportMail(matchInfo, sandTableCamp1, sandTableCamp2);

        sendRoundOverKillingRewardMail(round, sandTableCamp1, sandTableCamp2);

        group.state = 1;

        sandTableCamp1.clearAndSum();
        sandTableCamp2.clearAndSum();

        if (round >= 3) {
            List<SandTableCamp> campList = sandTableContest.getCampLines().values().stream().sorted(Comparator.comparing(SandTableCamp::getTscore).thenComparing(SandTableCamp::getTkilled).reversed()).collect(Collectors.toList());

            //his rank
            HisCampRank hisCampRank = new HisCampRank();
            hisCampRank.hisDate = sandTableContest.getMatchDate();
            for (int i = 0; i < campList.size(); i++) {
                HisCampRank.RankInfo rankInfo = new HisCampRank.RankInfo();
                rankInfo.rank = i + 1;
                rankInfo.camp = campList.get(i).getCamp();
                rankInfo.score = campList.get(i).getTscore();
                rankInfo.flag = campList.get(i).getTflag();
                rankInfo.killed = campList.get(i).getTkilled();
                hisCampRank.hisInfos.add(rankInfo);
            }
            sandTableContest.getHisCampRanks().add(hisCampRank);

            //last round is end, send camp rank reward
            sendCampRankRewardMail(hisCampRank.hisInfos);

            //over, send enroll reward mail
            sendNoFightRewardMail(sandTableContest);

            //record continue win
            campDataManager.getParty(campList.get(0).getCamp()).setSandTableWinAndMax(campDataManager.getParty(campList.get(0).getCamp()).getSandTableWin() + 1);
            campDataManager.getPartyMap().values().stream().forEach(camp -> {
                if (camp.getCamp() != campList.get(0).getCamp()) {
                    camp.setSandTableWin(0);
                }
            });
            sandTableContest.setPreWinCamp(campList.get(0).getCamp());

            //setting hero state
            overResetHeroState(sandTableContest);
        }
        //next round
        SandTableGroup nextGroup = sandTableContest.getMatchGroup().get(round + 1);
        if (Objects.nonNull(nextGroup)) {
            sandTableContest.setRound(nextGroup.round);
        }
    }

    private HisMatch getHistMatch(SandTableContest sandTableContest, int hisDate) {
        HisMatch hisMatch = sandTableContest.getHisMatches().stream().filter(o -> o.hisDate == hisDate).findFirst().orElse(null);
        return hisMatch;
    }

    private HisMatch.MatchInfo getHisMatchInfo(HisMatch hisMatch, int round) {
        HisMatch.MatchInfo matchInfo = hisMatch.matchInfos.stream().filter(o -> o.round == round).findFirst().orElse(null);
        return matchInfo;
    }

    private void fightLine(int line, SandTableCamp.LineObject lineA, SandTableCamp.LineObject lineB, HisMatch.MatchInfo matchInfo) {
        try {
            LinkedList<LinePlayer> linePlayersA = getFightPlayers(lineA.list);
            LinkedList<LinePlayer> linePlayersB = getFightPlayers(lineB.list);
            int totalNum1, leftNum1, totalNum2, leftNum2;
            totalNum1 = linePlayersA.size();
            totalNum2 = linePlayersB.size();
            if (!ListUtils.isBlank(lineA.list) && ListUtils.isBlank(lineB.list)) {
                //A win
                lineA.result = 1;
                lineB.result = 0;
            } else if (ListUtils.isBlank(lineA.list) && !ListUtils.isBlank(lineB.list)) {
                //B win
                lineB.result = 1;
                lineA.result = 0;
            } else if (!ListUtils.isBlank(lineA.list) && !ListUtils.isBlank(lineB.list)) {
                // A vs B
                for (; ; ) {
                    LinePlayer linePlayerA = null;
                    LinePlayer linePlayerB = null;
                    if (linePlayersA.size() > 0) {
                        linePlayerA = linePlayersA.getLast();
                        linePlayerA.tmpFought = true;
                    }
                    if (linePlayersB.size() > 0) {
                        linePlayerB = linePlayersB.getLast();
                        linePlayerB.tmpFought = true;
                    }
                    Fighter fighterA;
                    Fighter fighterB;
                    Player playerA = playerDataManager.getPlayer(linePlayerA.lordId);
                    if (Objects.nonNull(linePlayerA.fighter)) {
                        fighterA = linePlayerA.fighter;
                    } else {
                        fighterA = fightService.createSandTableFighter(playerA, linePlayerA.heroIds);
                    }
                    Player playerB = playerDataManager.getPlayer(linePlayerB.lordId);
                    if (Objects.nonNull(linePlayerB.fighter)) {
                        fighterB = linePlayerB.fighter;
                    } else {
                        fighterB = fightService.createSandTableFighter(playerB, linePlayerB.heroIds);
                    }

                    FightLogic fightLogic = new FightLogic(fighterA, fighterB, true);
                    fightLogic.start();
                    //his
                    List<FightReplay> fightReplayList = matchInfo.fightReplays.get(line);
                    FightReplay.FightObject fightObjectA = newFightObject(playerA, playerA.roleId, playerA.lord.getNick(), linePlayerA.fightTimes, fighterA);
                    FightReplay.FightObject fightObjectB = newFightObject(playerB, playerB.roleId, playerB.lord.getNick(), linePlayerB.fightTimes, fighterB);

                    if (fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS) {
                        linePlayersB.removeLast();
                        fightObjectA.isWin = true;

                        linePlayerA.fighter = fighterA;
                        linePlayerB.fighter = null;
                        linePlayerB.isAlive = false;

                        linePlayerA.killingNum++;
                    } else if (fightLogic.getWinState() == FightConstant.FIGHT_RESULT_FAIL) {
                        linePlayersA.removeLast();
                        fightObjectB.isWin = true;

                        linePlayerB.fighter = fighterB;
                        linePlayerA.fighter = null;
                        linePlayerA.isAlive = false;

                        linePlayerB.killingNum++;
                    } else {
                    }

                    int fightId = matchInfo.idxGen.incrementAndGet();
                    FightReplay fightReplay = new FightReplay(fightObjectA, fightObjectB, fightId);
                    HisMatch.WarReportInfo warReportInfo_ = new HisMatch.WarReportInfo(totalNum1, linePlayersA.size(), totalNum2, linePlayersB.size());
                    fightReplay.warReportInfo = warReportInfo_;
                    fightReplayList.add(fightReplay);

                    lineA.killed += fighterA.hurt;
                    lineB.killed += fighterB.hurt;
                    linePlayerA.fightTimes++;
                    linePlayerB.fightTimes++;
                    if (linePlayersA.size() > 0 && linePlayersB.size() <= 0) {
                        lineA.result = 1;
                        lineB.result = 0;
                    } else if (linePlayersB.size() > 0 && linePlayersA.size() <= 0) {
                        lineB.result = 1;
                        lineA.result = 0;
                    }
                    if (lineA.result > 0 || lineB.result > 0) {
                        linePlayerA.fighter = null;
                        linePlayerB.fighter = null;
                        break;
                    } else {
                        if (linePlayerA.fightTimes >= Constant.SAND_TABLE_1056) {
                            linePlayersA.remove(linePlayerA);
                            linePlayerA.fighter = null;
                        }
                        if (linePlayerB.fightTimes >= Constant.SAND_TABLE_1056) {
                            linePlayersB.remove(linePlayerB);
                            linePlayerB.fighter = null;
                        }
                        warReportInfo_ = new HisMatch.WarReportInfo(totalNum1, linePlayersA.size(), totalNum2, linePlayersB.size());
                        fightReplay.warReportInfo = warReportInfo_;
                        if (linePlayersA.size() > 0 && linePlayersB.size() <= 0) {
                            lineA.result = 1;
                            lineB.result = 0;
                        } else if (linePlayersB.size() > 0 && linePlayersA.size() <= 0) {
                            lineB.result = 1;
                            lineA.result = 0;
                        }
                        if (lineA.result > 0 || lineB.result > 0) {
                            linePlayerA.fighter = null;
                            linePlayerB.fighter = null;
                            break;
                        }
                    }
                }
            } else {
                //all blank
                lineA.result = 0;
                lineB.result = 0;
            }

            leftNum1 = linePlayersA.size();
            leftNum2 = linePlayersB.size();

            HisMatch.WarReportInfo warReportInfo = new HisMatch.WarReportInfo(totalNum1, leftNum1, totalNum2, leftNum2);
            matchInfo.warReportInfos.put(line, warReportInfo);

            LogUtil.error("沙盘演武处理线路战斗完成, line=" + line, ", result=[" + lineA.result + "," + lineB.result + "]", ", WarReportInfo=" + JSON.toJSONString(warReportInfo));
        } catch (Exception e) {
            LogUtil.error("沙盘演武处理线路战斗异常, line=" + line, e);
        }
    }

    private FightReplay.FightObject newFightObject(Player player, long lordId, String lordNick, int attackTimes, Fighter fighter) {
        FightReplay.FightObject obj = new FightReplay.FightObject();
        obj.lordId = lordId;
        obj.lordNick = lordNick;
        obj.attackTimes = attackTimes;
        for (Force force : fighter.forces) {
            Hero hero = player.heros.get(force.id);
            obj.heroDetails.add(new FightReplay.FightHeroDetail(force.id, force.killed, force.totalLost, force.maxHp, hero.getDecorated()));
        }
        return obj;
    }

    private LinkedList<LinePlayer> getFightPlayers(List<LinePlayer> line) {
        LinkedList linkedList = new LinkedList();
        if (!ListUtils.isBlank(line)) {
//            line.stream().forEach(o -> o. fightVal= CalculateUtil.calcHeroesFightVal(playerDataManager.getPlayer(o.lordId), o.heroIds));
            line.sort((o1, o2) -> o2.fightVal - o1.fightVal);
            for (int i = 0; i < line.size(); i++) {
                if (i >= Constant.SAND_TABLE_1057) {
                    break;
                }
                LinePlayer linePlayer = line.get(i);
                linePlayer.tmpFight = true;
                linkedList.add(linePlayer);
            }
        }
        return linkedList;
    }

    private void fightResult(SandTableCamp sandTableCamp1, SandTableCamp sandTableCamp2) {
        int flag1 = 0, flag2 = 0;
        for (int line : LINES) {
            SandTableCamp.LineObject lineA = sandTableCamp1.getLine(line);
            SandTableCamp.LineObject lineB = sandTableCamp2.getLine(line);
            if (lineA.result == 1 && lineB.result == 0)
                flag1++;
            if (lineB.result == 1 && lineA.result == 0)
                flag2++;
        }
        sandTableCamp1.setFlag(sandTableCamp1.getFlag() + flag1);
        sandTableCamp2.setFlag(sandTableCamp2.getFlag() + flag2);
        if (flag1 > flag2) {
            sandTableCamp1.setResult(1);
            sandTableCamp2.setResult(0);

            sandTableCamp1.setScore(sandTableCamp1.getScore() + 3);
        } else if (flag1 < flag2) {
            sandTableCamp1.setResult(0);
            sandTableCamp2.setResult(1);

            sandTableCamp2.setScore(sandTableCamp2.getScore() + 3);
        } else {
            sandTableCamp1.setResult(2);
            sandTableCamp2.setResult(2);

            sandTableCamp1.setScore(sandTableCamp1.getScore() + 1);
            sandTableCamp2.setScore(sandTableCamp2.getScore() + 1);
        }
    }

    private int winCamp(SandTableCamp sandTableCamp1, SandTableCamp sandTableCamp2) {
        if (sandTableCamp1.getResult() == 1 && sandTableCamp2.getResult() == 0) {
            return sandTableCamp1.getCamp();
        } else if (sandTableCamp2.getResult() == 1 && sandTableCamp1.getResult() == 0) {
            return sandTableCamp2.getCamp();
        } else if (sandTableCamp1.getResult() == 2 && sandTableCamp2.getResult() == 2) {
            return 0;
        } else {
            return 0;
        }
    }

    private void syncRoundOverToCamps(SandTableGroup group, SandTableCamp camp1, SandTableCamp camp2) {
        GamePb4.SyncSandTableRoundOverRs.Builder builder = GamePb4.SyncSandTableRoundOverRs.newBuilder();
        builder.setRound(group.round);
        builder.setCamp1(camp1.getCamp());
        builder.setCamp2(camp2.getCamp());
        builder.setCamp1Score(camp1.getFlag());
        builder.setCamp2Score(camp2.getFlag());
        builder.setWinCamp(winCamp(camp1, camp2));
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSandTableRoundOverRs.EXT_FIELD_NUMBER, GamePb4.SyncSandTableRoundOverRs.ext, builder.build()).build();
//        playerService.syncMsgToCamp(msg, camp1.getCamp(), camp2.getCamp());
        playerService.syncMsgToAll(msg);
    }

    public void openBegin() {
        try {
            SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
            for (int camp : Constant.Camp.camps) {
                SandTableCamp sandTableCamp = sandTableContest.getSandTableCamp(camp);
                List<LinePlayer> playerList = new ArrayList<>();
                sandTableCamp.getLines().values().forEach(o -> o.list.forEach(o1 -> {
                    //recalc heros fightVal
                    o1.fightVal = CalculateUtil.calcHeroesFightVal(playerDataManager.getPlayer(o1.lordId), o1.heroIds);
                    //setting tmp val
                    Player player = playerDataManager.getPlayer(o1.lordId);
                    if (Objects.nonNull(player)) {
                        o1.tmpLv = player.lord.getLevel();
                        o1.tmpRanks = player.lord.getRanks();
                    }

                    playerList.add(o1);
                }));
                //sorted
                sortedEnrollPlayer(playerList);

                //send enroll mail
                sendEnrollMailInfo(playerList, camp);
            }
            LogUtil.error("沙盘演武报名结束发送阵营邮件完成");
        } catch (Exception e) {
            LogUtil.error("沙盘演武报名结束发送阵营邮件异常, ", e);
        }
    }

    private void sortedEnrollPlayer(List<LinePlayer> playerList) {
//        playerList.forEach(o -> {
//            Player player = playerDataManager.getPlayer(o.lordId);
//            if(Objects.nonNull(player)){
//                o.tmpLv = player.lord.getLevel();
//                o.tmpRanks = player.lord.getRanks();
//            }
//        });
        Collections.sort(playerList, (o1, o2) -> {
            long r1 = o1.tmpRanks;
            long r2 = o2.tmpRanks;

            long f1 = o1.fightVal;
            long f2 = o2.fightVal;

            long lv1 = o1.tmpLv;
            long lv2 = o2.tmpLv;

            // (等级＞战力＞军衔)
            if (lv1 == lv2) {
                if (f1 == f2) {
                    return Long.compare(r2, r1);
                } else if (f1 > f2) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (lv1 > lv2) {
                return -1;
            } else {
                return 1;
            }
        });
    }


    private void sendEnrollMailInfo(List<LinePlayer> players, int camp) {
        CommonPb.SandTableEnrollMailInfo.Builder builder = CommonPb.SandTableEnrollMailInfo.newBuilder();
        builder.setEnrollNum(players.size());
        builder.setCanNum(Constant.SAND_TABLE_1057 * 3);
        if (players != null) {
            players.forEach(o -> {
                Player player = playerDataManager.getPlayer(o.lordId);
                CommonPb.SandTableEnrollPlayerInfo.Builder info_ = CommonPb.SandTableEnrollPlayerInfo.newBuilder();
                info_.setLordNick(player.lord.getNick());
                info_.setLordPortrait(player.lord.getPortrait());
                info_.setLordRank(player.lord.getRanks());
                info_.setLordPower(o.fightVal);
                info_.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
                builder.addPlayerInfo(info_);
            });
        }
        CommonPb.SandTableEnrollMailInfo enrollMailInfo = builder.build();
//        ConcurrentHashMap<Long, Player> campPlayers = playerDataManager.getPlayerByCamp(camp);
        int now = TimeHelper.getCurrentSecond();
        players.forEach(o -> {
            Player p = playerDataManager.getPlayer(o.lordId);
            if (Objects.nonNull(p)) {
                mailDataManager.sendSandTableEnrollMail(p, MailConstant.MOLD_SAND_TABLE_CAMP_ENROLL, now, new Object[]{camp}, enrollMailInfo);
            }
        });
//        campPlayers.values().forEach(o -> {
//            if (o.getCamp() == camp) {
//                mailDataManager.sendSandTableEnrollMail(o, MailConstant.MOLD_SAND_TABLE_CAMP_ENROLL, now, new Object[]{camp}, enrollMailInfo);
//            }
//        });
    }

    private void sendRoundOverWarReportMail(HisMatch.MatchInfo matchInfo, SandTableCamp camp1, SandTableCamp camp2) {
        CommonPb.SandTableRoundOverMailInfo.Builder builder1 = CommonPb.SandTableRoundOverMailInfo.newBuilder();
        builder1.setCamp1(matchInfo.camp1);
        builder1.setCamp2(matchInfo.camp2);
        builder1.setScore1(matchInfo.score1);
        builder1.setScore2(matchInfo.score2);
        builder1.setWinCamp(matchInfo.winCamp);
        for (int line : LINES) {
            CommonPb.SandTableRoundOverMailLineInfo.Builder info_ = CommonPb.SandTableRoundOverMailLineInfo.newBuilder();
            info_.setLine(line);
            info_.setWinCamp(matchInfo.linesResult.get(line));
            info_.setWarReportInfo(buildSandTableWarReportInfo(matchInfo.warReportInfos.get(line)));
            builder1.addRoundOverMailLineInfo(info_.build());
        }
        CommonPb.SandTableRoundOverMailInfo.Builder builder2 = CommonPb.SandTableRoundOverMailInfo.newBuilder();
        builder2.setCamp1(matchInfo.camp2);
        builder2.setCamp2(matchInfo.camp1);
        builder2.setScore1(matchInfo.score2);
        builder2.setScore2(matchInfo.score1);
        builder2.setWinCamp(matchInfo.winCamp);
        for (int line : LINES) {
            CommonPb.SandTableRoundOverMailLineInfo.Builder info_ = CommonPb.SandTableRoundOverMailLineInfo.newBuilder();
            info_.setLine(line);
            info_.setWinCamp(matchInfo.linesResult.get(line));
            info_.setWarReportInfo(reverseBuildSandTableWarReportInfo(matchInfo.warReportInfos.get(line)));
            builder2.addRoundOverMailLineInfo(info_.build());
        }

        int mailId1, mailId2;
        if (matchInfo.winCamp == matchInfo.camp1) {
            mailId1 = MailConstant.MOLD_SAND_TABLE_ROUND_OVER_WIN;
            mailId2 = MailConstant.MOLD_SAND_TABLE_ROUND_OVER_LOSE;
        } else if (matchInfo.winCamp == matchInfo.camp2) {
            mailId1 = MailConstant.MOLD_SAND_TABLE_ROUND_OVER_LOSE;
            mailId2 = MailConstant.MOLD_SAND_TABLE_ROUND_OVER_WIN;
        } else {
            mailId1 = MailConstant.MOLD_SAND_TABLE_ROUND_OVER_DRAW;
            mailId2 = MailConstant.MOLD_SAND_TABLE_ROUND_OVER_DRAW;
        }
        int now = TimeHelper.getCurrentSecond();

        CommonPb.SandTableRoundOverMailInfo roundOverMailInfo1 = builder1.build();
        ConcurrentHashMap<Long, Player> camp1Players = playerDataManager.getPlayerByCamp(matchInfo.camp1);
        Object[] params1 = {matchInfo.camp1, matchInfo.camp1, matchInfo.round, matchInfo.camp2, camp1.getScore()};
        camp1Players.values().forEach(o -> {
            if (Objects.nonNull(o) && o.getCamp() == matchInfo.camp1) {
                mailDataManager.sendSandTableRoundOverMail(o, mailId1, now, params1, roundOverMailInfo1);
            }
        });
        CommonPb.SandTableRoundOverMailInfo roundOverMailInfo2 = builder2.build();
        ConcurrentHashMap<Long, Player> camp2Players = playerDataManager.getPlayerByCamp(matchInfo.camp2);
        Object[] params2 = {matchInfo.camp2, matchInfo.camp2, matchInfo.round, matchInfo.camp1, camp2.getScore()};
        camp2Players.values().forEach(o -> {
            if (o.getCamp() == matchInfo.camp2) {
                mailDataManager.sendSandTableRoundOverMail(o, mailId2, now, params2, roundOverMailInfo2);
            }
        });

//        Object[] params1 = {matchInfo.camp1, matchInfo.camp1, matchInfo.round, matchInfo.camp2, camp1.getScore(), "", 0};
//        int preScore1 = (int) params1[4];
//        camp1Players.values().forEach(o -> {
//            if (Objects.nonNull(o) && o.getCamp() == matchInfo.camp1) {
//                //赛季天赋加成
//                double seasonTalentMultiple = DataResource.getBean(SeasonTalentService.class).
//                        getSeasonTalentEffectValue(o, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND;
//                if (seasonTalentMultiple > 0) {
//                    params1[4] = preScore1 * (1 + seasonTalentMultiple);
//                    params1[5] = DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(o, SeasonConst.TALENT_EFFECT_620);
//                    double params4Double = Double.parseDouble(params1[4] + "");
//                    params1[6] = params4Double - preScore1;
//                } else {
//                    params1[4] = preScore1;
//                    params1[5] = "";
//                    params1[6] = 0;
//                }
//
//                mailDataManager.sendSandTableRoundOverMail(o, mailId1, now, params1, roundOverMailInfo1);
//            }
//        });
//        CommonPb.SandTableRoundOverMailInfo roundOverMailInfo2 = builder2.build();
//        ConcurrentHashMap<Long, Player> camp2Players = playerDataManager.getPlayerByCamp(matchInfo.camp2);
//        Object[] params2 = {matchInfo.camp2, matchInfo.camp2, matchInfo.round, matchInfo.camp1, camp2.getScore(), "", 0};
//        int preScore2 = (int) params2[4];
//        camp2Players.values().forEach(o -> {
//            if (o.getCamp() == matchInfo.camp2) {
//                //赛季天赋加成
//                double seasonTalentMultiple = DataResource.getBean(SeasonTalentService.class).
//                        getSeasonTalentEffectValue(o, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND;
//                if (seasonTalentMultiple > 0) {
//                    params2[4] = preScore2 * (1 + seasonTalentMultiple);
//                    params2[5] = DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(o, SeasonConst.TALENT_EFFECT_620);
//                    double params4Double = Double.parseDouble(params2[4] + "");
//                    params2[6] = params4Double - preScore2;
//                } else {
//                    params2[4] = preScore2;
//                    params2[5] = "";
//                    params2[6] = 0;
//                }
//
//                mailDataManager.sendSandTableRoundOverMail(o, mailId2, now, params2, roundOverMailInfo2);
//            }
//        });
    }

    public void sendCampRankRewardMail(List<HisCampRank.RankInfo> hisInfos) {
        int now = TimeHelper.getCurrentSecond();
        hisInfos.forEach(o -> {
            StaticSandTableAward staticSandTableAward = StaticIniDataMgr.getStaticSandTableAward(1, o.rank);
            if (Objects.nonNull(staticSandTableAward)) {
                int preAwardCount = staticSandTableAward.getSandTableScoreAward();
                Optional.ofNullable(playerDataManager.getPlayerByCamp(o.camp)).ifPresent(pmap -> pmap.values().forEach(p -> {
                    if (Objects.nonNull(p)) {
                        List<CommonPb.Award> mailAwards = new ArrayList<>();
                        //赛季天赋优化， 积分加成
                        double multiple = 1 + (DataResource.getBean(SeasonTalentService.class).
                                getSeasonTalentEffectValue(p, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND);
                        mailAwards.addAll(PbHelper.createMultipleAwardsPb(staticSandTableAward.getAward(), multiple, multipleAwardType));
                        int awardCountBuff = (int) (preAwardCount * multiple);
                        mailDataManager.sendAttachMail(p, mailAwards, MailConstant.MOLD_SAND_TABLE_CAMP_RANK_REWARD, AwardFrom.SAND_TABLE_CAMP_RANK_REWARD, now, o.camp, o.rank, o.camp, o.rank,
                                DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(p, SeasonConst.TALENT_EFFECT_620), awardCountBuff - preAwardCount);
                    }
                }));
            }
        });
    }

    private void multipleSandTableScore(List<CommonPb.Award> mailAwards, double multiple, int awardType) {
        if (ObjectUtils.isEmpty(mailAwards)) {
            return;
        }

        mailAwards.forEach(singleAward -> {
            if (singleAward.getType() != AwardType.SANDTABLE_SCORE) {
                return;
            }

            int count = singleAward.getCount();
            singleAward.toBuilder().setCount((int) (count * multiple));
        });
    }

    private void sendNoFightRewardMail(SandTableContest sandTableContest) {
        int now = TimeHelper.getCurrentSecond();
        StaticSandTableAward staticSandTableAward = StaticIniDataMgr.getStaticSandTableAward(3, 0);
        //加成前沙盘积分
        int preAwardCount = staticSandTableAward.getSandTableScoreAward();
        sandTableContest.getCampLines().values().forEach(o ->
                o.getLines().values().forEach(o1 -> {
                    o1.list.forEach(o2 -> {
                        if (!o2.tmpFight) {
                            Optional.ofNullable(playerDataManager.getPlayer(o2.lordId)).ifPresent(p -> {
                                //赛季天赋优化， 积分加成
                                List<CommonPb.Award> mailAwards = new ArrayList<>();
                                double multiple = 1 + (DataResource.getBean(SeasonTalentService.class).
                                        getSeasonTalentEffectValue(p, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND);
                                mailAwards.addAll(PbHelper.createMultipleAwardsPb(staticSandTableAward.getAward(), multiple, multipleAwardType));
                                int awardCountBuff = (int) (preAwardCount * multiple);
                                mailDataManager.sendAttachMail(p, mailAwards, MailConstant.MOLD_SAND_TABLE_ENROLL_REWARD,
                                        AwardFrom.SAND_TABLE_ENROLL_REWARD, now, Constant.SAND_TABLE_1057,
                                        DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(p, SeasonConst.TALENT_EFFECT_620),
                                        awardCountBuff - preAwardCount);
                            });
                        }
                    });
                }));
    }

    private void sendRoundOverKillingRewardMail(int round, SandTableCamp... sandTableCamp) {
        int now = TimeHelper.getCurrentSecond();
        for (SandTableCamp camp : sandTableCamp) {
            a(round, camp, now);
        }
    }

    private void a(int round, SandTableCamp sandTableCamp, int now) {
        sandTableCamp.getLines().values().forEach(o1 -> o1.list.forEach(o2 -> {
            Player player = playerDataManager.getPlayer(o2.lordId);
            if (Objects.nonNull(player) && o2.tmpFight) {
                StaticSandTableAward staticSandTableAward;
                Integer mailId = null;//邮件id
                AwardFrom awardFromEnum = null;//奖励来源
                if (o2.tmpFought) {
                    //个人击败2名玩家奖励 or 击败1名玩家且自身存活
                    if (o2.killingNum > StaticIniDataMgr.getSandTableKillingRewardMaxParam().getParam()) {
                        staticSandTableAward = StaticIniDataMgr.getSandTableKillingRewardMaxParam();
                        mailId = MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD;//演武个人奖励(您在沙盤演武中擊敗了#num1個領主)
                        awardFromEnum = AwardFrom.SAND_TABLE_PERSONAL_KILLING_REWARD;
                    } else if (o2.killingNum == 1 && o2.isAlive) {
                        staticSandTableAward = StaticIniDataMgr.getSandTableKillingRewardMaxParam();
                        mailId = MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD_TWO;//演武个人奖励(击败敌方一名领主)
                        awardFromEnum = AwardFrom.SAND_TABLE_PERSONAL_KILLING_REWARD_TWO;
                    } else {
                        staticSandTableAward = StaticIniDataMgr.getStaticSandTableAward(2, o2.killingNum);
                        mailId = MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD;//演武个人奖励(您在沙盤演武中擊敗了#num1個領主)
                        awardFromEnum = AwardFrom.SAND_TABLE_PERSONAL_KILLING_REWARD;
                    }
                } else {
                    //未参与战斗
                    staticSandTableAward = StaticIniDataMgr.getStaticSandTableAward(2, 2);
                    mailId = MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD_THREE;//演武个人奖励(敌方无人应战)
                    awardFromEnum = AwardFrom.SAND_TABLE_PERSONAL_KILLING_REWARD_THREE;
                }
                if (Objects.nonNull(staticSandTableAward)) {
                    int preAwardCount = staticSandTableAward.getSandTableScoreAward();
                    //赛季天赋优化， 积分加成
                    double multiple = 1 + (DataResource.getBean(SeasonTalentService.class).
                            getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND);
                    int awardCountBuff = (int) (preAwardCount * multiple);

                    List<CommonPb.Award> mailAwards = new ArrayList<>();
                    mailAwards.addAll(PbHelper.createMultipleAwardsPb(staticSandTableAward.getAward(), multiple, multipleAwardType));
                    //527,528 2个参数    517 4个参数
                    if (mailId == MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD_TWO || mailId == MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD_THREE) {
                        mailDataManager.sendAttachMail(player, mailAwards, mailId, awardFromEnum, now,
                                DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(player, SeasonConst.TALENT_EFFECT_620), awardCountBuff - preAwardCount);
                    } else {
                        mailDataManager.sendAttachMail(player, mailAwards, mailId, awardFromEnum, now,
                                staticSandTableAward.getParam(), staticSandTableAward.getParam(),
                                DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(player, SeasonConst.TALENT_EFFECT_620), awardCountBuff - preAwardCount);
                    }
                }
            }
        }));
    }

    private void overResetHeroState(SandTableContest sandTableContest) {
        try {
            sandTableContest.getCampLines().values().forEach(o -> o.getLines().values().forEach(o1 -> o1.list.forEach(o2 -> {
                Optional.ofNullable(playerDataManager.getPlayer(o2.lordId)).ifPresent(p -> o2.heroIds.forEach(heroId -> {
                    Optional.ofNullable(p.heros.get(heroId)).ifPresent(hero -> hero.setSandTableState(0));
                }));
            })));
        } catch (Exception e) {
            LogUtil.error("最后一轮结束重置玩家出战沙盘英雄状态发生异常, ", e);
        }
    }

    public GamePb4.SandTableShopBuyRs buy(long roleId, int confId) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticSandTableExchange staticSandTableExchange = StaticIniDataMgr.getStaticSandTableExchangeById(confId);
        if (Objects.isNull(staticSandTableExchange)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_SHOP_BUY_NO_CONFIG.getCode(), "沙盘演武兑换失败，StaticSandTableExchange IS NULL，roleId=" + roleId, ", confId=" + confId);
        }

        int boughtTimes = player.getSandTableBought().getOrDefault(confId, 0);
        if (boughtTimes >= staticSandTableExchange.getNumberLimit()) {
            throw new MwException(GameError.SANDTABLE_CONTEST_SHOP_BUY_LIMIT_BUY.getCode(), "沙盘演武兑换失败，达到次数限制，roleId=" + roleId, ", confId=" + confId, ", bought=" + boughtTimes);
        }

        if (staticSandTableExchange.getType() == 2 && campDataManager.getParty(player.getCamp()).getSandTableWinMax() < staticSandTableExchange.getParam()) {
            throw new MwException(GameError.SANDTABLE_CONTEST_SHOP_BUY_LIMIT_BUY.getCode(), "沙盘演武兑换失败，阵营连胜次数条件不足，roleId=" + roleId, ", confId=" + confId, ", bought=" + boughtTimes);
        }
        //检测商品在当前赛季是否可售卖
        seasonService.checkSeasonItem(staticSandTableExchange.getSeasons());

        rewardDataManager.checkAndSubPlayerRes(player, staticSandTableExchange.getExpendProp(), AwardFrom.SAND_TABLE_SHOP_BUY);
        List<CommonPb.Award> getAwards = rewardDataManager.sendReward(player, staticSandTableExchange.getAward(), AwardFrom.SAND_TABLE_SHOP_BUY);

        player.getSandTableBought().put(confId, boughtTimes + 1);

        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();

        GamePb4.SandTableShopBuyRs.Builder resp = GamePb4.SandTableShopBuyRs.newBuilder();
        resp.addAllGotAward(getAwards);
        resp.setShopInfo(buildSandTableShopInfo(player, sandTableContest));
        return resp.build();
    }

    private CommonPb.SandTableShopInfo buildSandTableShopInfo(Player player, SandTableContest sandTableContest) {
        CommonPb.SandTableShopInfo.Builder builder = CommonPb.SandTableShopInfo.newBuilder();
        builder.setScore(player.getSandTableScore());
        Date nextFireDate = QuartzHelper.getNextFireTime(ScheduleManager.getInstance().getSched(), SandTableJob.name_preview, SandTableJob.groupName);
        builder.setNextRefreshStamp((int) (nextFireDate.getTime() / 1000));
        player.getSandTableBought().entrySet().forEach(o -> builder.addBoughtTimes(PbHelper.createTwoIntPb(o.getKey(), o.getValue())));
        Camp camp = campDataManager.getParty(player.getCamp());
        builder.setWinNum(camp.getSandTableWinMax());
        return builder.build();
    }

    public GamePb4.SandTablePlayerFightDetailRs getPlayerFightDetail(long roleId, int hisDate, int round, int onlyId) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (round > 3 || round < 1) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_PLAYER_FIGHT_DETAIL_PARAM_INVALID.getCode(), "沙盘演武, 获取玩家战斗详细, 参数错误，roleId=" + roleId + "，hisDate=" + hisDate + ", round=" + round + ", onlyId=" + onlyId);
        }

        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        if (hisDate <= 0) {
            if (sandTableContest.getMatchDate() == 0)
                hisDate = TimeHelper.getCurrentDay();
            else
                hisDate = sandTableContest.getMatchDate();
        }

        int finalHisDate = hisDate;
        HisMatch hisMatch = sandTableContest.getHisMatches().stream().filter(o -> o.hisDate == finalHisDate).findFirst().orElse(null);
        if (Objects.isNull(hisMatch)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_PLAYER_FIGHT_DETAIL_NODATA.getCode(), "沙盘演武, 获取玩家战斗详细错误, 没有重播数据(HisMatch is NULL), roleId=" + roleId + "，hisDate=" + hisDate + ", round=" + round + ", onlyId=" + onlyId);
        }
        HisMatch.MatchInfo matchInfo = hisMatch.matchInfos.stream().filter(o -> o.round == round).findFirst().orElse(null);
        if (Objects.isNull(matchInfo)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_PLAYER_FIGHT_DETAIL_NODATA.getCode(), "沙盘演武, 获取玩家战斗详细错误, 没有重播数据(MatchInfo is NULL), roleId=" + roleId + "，hisDate=" + hisDate + ", round=" + round + ", onlyId=" + onlyId);
        }

        FightReplay fightReplay = null;
        for (List<FightReplay> tmps : matchInfo.fightReplays.values()) {
            boolean b = false;
            for (FightReplay o : tmps) {
                if (o.onlyIdx == onlyId) {
                    fightReplay = o;
                    b = true;
                    break;
                }
            }
            if (b) break;
        }
        if (Objects.isNull(fightReplay)) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_PLAYER_FIGHT_DETAIL_NODATA.getCode(), "沙盘演武, 获取玩家战斗详细错误, 没有重播数据(FightReplay is NULL), roleId=" + roleId + "，hisDate=" + hisDate + ", round=" + round + ", onlyId=" + onlyId);
        }

        GamePb4.SandTablePlayerFightDetailRs.Builder resp = GamePb4.SandTablePlayerFightDetailRs.newBuilder();
        if (player.getCamp() == matchInfo.camp1) {
            fightReplay.obj1.heroDetails.forEach(o -> resp.addAtkHero(buildRptHero(o)));
            fightReplay.obj2.heroDetails.forEach(o -> resp.addDefHero(buildRptHero(o)));
        } else if (player.getCamp() == matchInfo.camp2) {
            fightReplay.obj2.heroDetails.forEach(o -> resp.addAtkHero(buildRptHero(o)));
            fightReplay.obj1.heroDetails.forEach(o -> resp.addDefHero(buildRptHero(o)));
        } else {
            fightReplay.obj1.heroDetails.forEach(o -> resp.addAtkHero(buildRptHero(o)));
            fightReplay.obj2.heroDetails.forEach(o -> resp.addDefHero(buildRptHero(o)));
        }

        return resp.build();
    }

    private CommonPb.RptHero buildRptHero(FightReplay.FightHeroDetail detail) {
        CommonPb.RptHero.Builder builder = CommonPb.RptHero.newBuilder();
        builder.setHeroId(detail.heroId);
        builder.setHp(detail.hp);
        builder.setKill(detail.kill);
        builder.setLost(detail.lost);
        builder.setType(0);
        builder.setAward(0);
        builder.setHeroDecorated(detail.heroDecorated);
        return builder.build();
    }

    public GamePb4.SandTableGetLinePlayersRs getLinePlayers(long roleId, int line) throws MwException {
        if (!checkOpenByServerId()) {//SANDTABLE_CONTEST_SERVER_NOT_OPEN
            throw new MwException(GameError.SANDTABLE_CONTEST_SERVER_NOT_OPEN.getCode(), "沙盘演武在此服不开放, roleId=" + roleId + ", serverId=" + serverSetting.getServerID() + ", Config=" + JSON.toJSONString(Constant.SAND_TABLE_1058));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        int state = sandTableContest.state();
        if (Arrays.binarySearch(LINES, line) < 0) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_LINE_PLAYERS_PARAM_INVALID.getCode(), "沙盘演武, 获取线路玩家排序列表, 参数无效, roleId=" + roleId + ", line=" + line + ", state=" + state);
        }
        if (state == SandTableContest.STATE_NOTOPEN) {
            throw new MwException(GameError.SANDTABLE_CONTEST_GET_LINE_PLAYERS_NO_OPEN.getCode(), "沙盘演武, 获取线路玩家排序列表, 功能未开放, roleId=" + roleId + ", line=" + line + ", state=" + state);
        }
        GamePb4.SandTableGetLinePlayersRs.Builder resp = GamePb4.SandTableGetLinePlayersRs.newBuilder();
        SandTableCamp.LineObject lineObject = sandTableContest.getSandTableCamp(player.getCamp()).getLine(line);
        lineObject.list.sort((o1, o2) -> o2.fightVal - o1.fightVal);
        int size;
        if (state == SandTableContest.STATE_PREVIEW) {
            size = lineObject.list.size();
        } else if (state == SandTableContest.STATE_OPEN) {
            size = lineObject.list.size() > Constant.SAND_TABLE_1057 ? Constant.SAND_TABLE_1057 : lineObject.list.size();
        } else {
            size = lineObject.list.size();
        }
//        int order = size / Constant.SAND_TABLE_1041 > 0 ? Constant.SAND_TABLE_1041 : size;
//        int order2 = order + 1;
//        int tmpOrder = 0;
        for (LinePlayer linePlayer : lineObject.list) {
//            if(order <= 0){
//                tmpOrder = order2;
//                order2 ++;
//            }else {
//                tmpOrder = order;
//            }
            if (size <= 0) {
                break;
            }
            resp.addLinePlayerInfo(buildSandTableLinePlayerInfo(linePlayer, size));
//            order --;
            size--;
        }
        return resp.build();
    }

    private CommonPb.SandTableLinePlayerInfo buildSandTableLinePlayerInfo(LinePlayer linePlayer, int order) {
        CommonPb.SandTableLinePlayerInfo.Builder builder = CommonPb.SandTableLinePlayerInfo.newBuilder();
        builder.setLordId(linePlayer.lordId);
        builder.setLordNick(playerDataManager.getPlayer(linePlayer.lordId).lord.getNick());
        builder.setFightVal(linePlayer.fightVal);
        builder.setFightOrder(order < 0 ? 0 : order);
        return builder.build();
    }

    public boolean checkOpenByServerId() {
        boolean check = false;
        int serverId = serverSetting.getServerID();
        if (!ListUtils.isBlank(Constant.SAND_TABLE_1058)) {
            for (List<Integer> tmps : Constant.SAND_TABLE_1058) {
                if (serverId >= tmps.get(0) && serverId <= tmps.get(1)) {
                    check = true;
                    break;
                }
            }
        } else {
            check = true;
        }
        return check;
    }

    public void resetContestDate4LoadSystem() {
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        if (StringUtils.isBlank(sandTableContest.getPreviewCron()) || !sandTableContest.getPreviewCron().equals(Constant.SAND_TABLE_PREVIEW)) {
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), SandTableJob.name_preview, SandTableJob.groupName);
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), SandTableJob.name_open, SandTableJob.groupName);
            QuartzHelper.removeJob(ScheduleManager.getInstance().getSched(), SandTableJob.name_end, SandTableJob.groupName);

            ScheduleManager.getInstance().initSandTableContest();
            LogUtil.error("重载System配表 沙盘演武的配置有变化");
        }
        LogUtil.error("重载System配表的沙盘演武处理, " + JSON.toJSONString(sandTableContest));
    }

    // <editor-fold desc="自己测试用的方法" defaultstate="collapsed">

    /**
     * GM命令调用，自定分组
     *
     * @param g11
     * @param g12
     * @param g21
     * @param g22
     * @param g31
     * @param g32
     */
    @Deprecated
    public void matchGroup1(int g11, int g12, int g21, int g22, int g31, int g32) {
        this.matchGroup(g11, g12, g21, g22, g31, g32);
    }

    /**
     * 设置时间当前天开放
     *
     * @param player
     */
    @Deprecated
    public void settingOpenDate(Player player) {
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        sandTableContest.setPreviewBeginDate(now.getTime());
        now.set(Calendar.HOUR_OF_DAY, 18);
        sandTableContest.setOpenBeginDate(now.getTime());
        now.set(Calendar.HOUR_OF_DAY, 19);
        sandTableContest.setOpenEndDate(now.getTime());
        now.set(Calendar.HOUR_OF_DAY, 20);
        sandTableContest.setExchangeEndDate(now.getTime());
        sandTableContest.setMatchDate(TimeHelper.getCurrentDay());
    }

    @Deprecated
    public void reOpen() throws ParseException {
        //clear player buy times
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        playerDataManager.getPlayers().values().forEach(o -> o.getSandTableBought().clear());

        GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
        //clear data
        sandTableContest.clearData();
        //setting time

        String[] arr_ = Constant.SAND_TABLE_PREVIEW.split(" ");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, TimeHelper.getWeekByCron(arr_[5]));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(arr_[2]));
        c.set(Calendar.MINUTE, Integer.parseInt(arr_[1]));
        c.set(Calendar.SECOND, Integer.parseInt(arr_[0]));

        sandTableContest.setPreviewBeginDate(c.getTime());
        CronExpression cronExpression = new CronExpression(Constant.SAND_TABLE_OPEN_END.get(0));
        sandTableContest.setOpenBeginDate(cronExpression.getNextValidTimeAfter(sandTableContest.getPreviewBeginDate()));
        cronExpression = new CronExpression(Constant.SAND_TABLE_OPEN_END.get(1));
        sandTableContest.setOpenEndDate(cronExpression.getNextValidTimeAfter(sandTableContest.getOpenBeginDate()));
        int exchangeEndStamp = (int) (sandTableContest.getOpenEndDate().getTime() / 1000 + 3600);
        sandTableContest.setExchangeEndDate(new Date(exchangeEndStamp * 1000L));
        //setting match Date
        sandTableContest.setMatchDate(TimeHelper.getDay(sandTableContest.getOpenBeginDate()));
        //grouping
        SandTableContestService sandTableContestService = DataResource.ac.getBean(SandTableContestService.class);
        sandTableContestService.matchGroup();

        //sync to client
        sandTableContestService.syncSandTablePreview(sandTableContest);
    }

    // </editor-fold>

}
