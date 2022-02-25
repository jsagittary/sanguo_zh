package com.gryphpoem.game.zw.resource.pojo.sandtable;

import com.alibaba.fastjson.annotation.JSONField;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;
import java.util.stream.Collectors;

public class SandTableContest {

    public static final int STATE_PREVIEW = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_EXCHANGE = 3;
    public static final int STATE_NOTOPEN = 0;

    private Date previewBeginDate;
    private Date openBeginDate;
    private Date openEndDate;
    private Date exchangeEndDate;

    private Map<Integer, SandTableCamp> campLines = new HashMap<>(3);

    private Map<Integer,SandTableGroup> matchGroup = new HashMap<>(3);

    private volatile int round = 0x1;

    @JSONField(serialize = false)
    private List<HisCampRank> hisCampRanks = new ArrayList<>();
    @JSONField(serialize = false)
    private List<HisMatch> hisMatches = new ArrayList<>();

    private int preWinCamp;

    private int matchDate;

    private String previewCron;
    private String openBeginCron;
    private String openEndCron;

    public SandTableContest(){
        for(int camp : Constant.Camp.camps){
            SandTableCamp sandTableCamp = getSandTableCamp(camp);
            campLines.put(camp,sandTableCamp);
        }
    }

    public void clearData(){
        previewBeginDate = null;
        openBeginDate = null;
        openEndDate = null;
        exchangeEndDate = null;

        campLines.clear();
        for(int camp : Constant.Camp.camps){
            SandTableCamp sandTableCamp = getSandTableCamp(camp);
            campLines.put(camp,sandTableCamp);
        }
        matchGroup.clear();

        round = 0x1;

        matchDate = 0;
    }

    public Map<Integer, SandTableCamp> getCampLines() {
        return campLines;
    }

    public SandTableCamp getSandTableCamp(int camp){
        SandTableCamp sandTableLines = campLines.get(camp);
        if(sandTableLines == null){
            sandTableLines = new SandTableCamp();
            sandTableLines.setCamp(camp);
            campLines.put(camp,sandTableLines);
        }
        return sandTableLines;
    }

    public void addHisMatch(){
        HisMatch hisMatch = new HisMatch();
        hisMatch.hisDate = matchDate;
        for(int i=1;i<4;i++){
            SandTableGroup sandTableGroup = matchGroup.get(i);
            HisMatch.MatchInfo matchInfo = new HisMatch.MatchInfo();
            matchInfo.round = sandTableGroup.round;
            matchInfo.state = sandTableGroup.state;
            matchInfo.time = sandTableGroup.beginTime;
            matchInfo.camp1 = sandTableGroup.camp1;
            matchInfo.camp2 = sandTableGroup.camp2;
            hisMatch.matchInfos.add(matchInfo);
        }

        List<HisMatch> delList = hisMatches.stream().filter(o -> o.hisDate == hisMatch.hisDate).collect(Collectors.toList());
        hisMatches.removeAll(delList);

        hisMatches.add(hisMatch);
    }

    public int state(){
        if(previewBeginDate == null)
            return STATE_NOTOPEN;
        Date now = new Date();
        if(now.after(previewBeginDate) && now.before(openBeginDate)){
            return STATE_PREVIEW;
        }else if(now.after(openBeginDate) && now.before(openEndDate)){
            return STATE_OPEN;
        }else if(now.after(openEndDate) && now.before(exchangeEndDate)){
            return STATE_EXCHANGE;
        }else {
            return STATE_NOTOPEN;
        }
    }

    public Map<Integer, SandTableGroup> getMatchGroup() {
        return matchGroup;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public List<HisCampRank> getHisCampRanks() {
        return hisCampRanks;
    }

    public void setHisCampRanks(List<HisCampRank> hisCampRanks) {
        this.hisCampRanks = hisCampRanks;
    }

    public List<HisMatch> getHisMatches() {
        return hisMatches;
    }

    public void setHisMatches(List<HisMatch> hisMatches) {
        this.hisMatches = hisMatches;
    }

    public Date getPreviewBeginDate() {
        return previewBeginDate;
    }

    public void setPreviewBeginDate(Date previewBeginDate) {
        this.previewBeginDate = previewBeginDate;
    }

//    public void setPreviewBeginDate0(Date previewBeginDate) throws Exception{
//        setPreviewBeginDate(previewBeginDate);
//        CronExpression cronExpression = new CronExpression(Constant.SAND_TABLE_OPEN_END.get(0));
//        setOpenBeginDate(cronExpression.getNextValidTimeAfter(getPreviewBeginDate()));
//        cronExpression = new CronExpression(Constant.SAND_TABLE_OPEN_END.get(1));
//        setOpenEndDate(cronExpression.getNextValidTimeAfter(getOpenBeginDate()));
//        int exchangeEndStamp = (int) (getOpenEndDate().getTime()/1000 + 3600);
//        setExchangeEndDate(new Date(exchangeEndStamp * 1000L));
//    }

    public Date getOpenBeginDate() {
        return openBeginDate;
    }

    public void setOpenBeginDate(Date openBeginDate) {
        this.openBeginDate = openBeginDate;
    }

    public Date getOpenEndDate() {
        return openEndDate;
    }

    public void setOpenEndDate(Date openEndDate) {
        this.openEndDate = openEndDate;
    }

    public Date getExchangeEndDate() {
        return exchangeEndDate;
    }

    public void setExchangeEndDate(Date exchangeEndDate) {
        this.exchangeEndDate = exchangeEndDate;
    }

    public SerializePb.SerSandTableContest ser(){
        SerializePb.SerSandTableContest.Builder ser = SerializePb.SerSandTableContest.newBuilder();
        if(previewBeginDate!=null){
            ser.setPreviewBeginStamp((int) (previewBeginDate.getTime()/1000));
        }
        if(openBeginDate!=null){
            ser.setOpenBeginStamp((int) (openBeginDate.getTime()/1000));
        }
        if(openEndDate!=null){
            ser.setOpenEndStamp((int) (openEndDate.getTime()/1000));
        }
        if(exchangeEndDate!=null){
            ser.setExchangeEndStamp((int) (exchangeEndDate.getTime()/1000));
        }
        campLines.entrySet().forEach(o -> ser.addCamps(serSandTableCamp(o.getValue())));
        matchGroup.entrySet().forEach(o -> ser.addMatchGroup(serSandTableGroup(o.getValue())));
        ser.setRound(round);
        hisCampRanks.forEach(o -> ser.addHisCampRanks(serHisCampRank(o)));
        hisMatches.forEach(o -> ser.addHisMatches(serHisMatch(o)));
        ser.setPreWinCamp(this.preWinCamp);
        ser.setMatchDate(this.matchDate);
        return ser.build();
    }

    private SerializePb.SerSandTableCamp serSandTableCamp(SandTableCamp sandTableCamp){
        SerializePb.SerSandTableCamp.Builder builder = SerializePb.SerSandTableCamp.newBuilder();
        builder.setCamp(sandTableCamp.getCamp());
        sandTableCamp.getLines().entrySet().forEach(o -> {
            builder.addLines(serLineObject(o.getValue()));
        });
        builder.setResult(sandTableCamp.getResult());
        builder.setScore(sandTableCamp.getScore());
        builder.setFlag(sandTableCamp.getFlag());
        builder.setKilled(sandTableCamp.getKilled());
        builder.setTscore(sandTableCamp.getTscore());
        builder.setTflag(sandTableCamp.getTflag());
        builder.setTkilled(sandTableCamp.getTkilled());
        return builder.build();
    }

    private SerializePb.SerLineObject serLineObject(SandTableCamp.LineObject lineObject){
        SerializePb.SerLineObject.Builder builder = SerializePb.SerLineObject.newBuilder();
        lineObject.list.forEach(o -> builder.addList(serLinePlayer(o)));
        builder.setResult(lineObject.result);
        builder.setKilled(lineObject.killed);
        builder.setLine(lineObject.line);
        return builder.build();
    }

    private SerializePb.SerLinePlayer serLinePlayer(LinePlayer linePlayer){
        SerializePb.SerLinePlayer.Builder builder = SerializePb.SerLinePlayer.newBuilder();
        builder.setLordId(linePlayer.lordId);
        builder.addAllHeroIds(linePlayer.heroIds);
        builder.setFightTimes(linePlayer.fightTimes);
        builder.setFightVal(linePlayer.fightVal);
        builder.setLine(linePlayer.line);
        builder.setKillingNum(linePlayer.killingNum);
        return builder.build();
    }

    private SerializePb.SerSandTableGroup serSandTableGroup(SandTableGroup sandTableGroup){
        SerializePb.SerSandTableGroup.Builder builder = SerializePb.SerSandTableGroup.newBuilder();
        builder.setRound(sandTableGroup.round);
        builder.setState(sandTableGroup.state);
        builder.setBeginTime(sandTableGroup.beginTime);
        builder.setCamp1(sandTableGroup.camp1);
        builder.setCamp2(sandTableGroup.camp2);
        return builder.build();
    }

    private SerializePb.SerHisCampRank serHisCampRank(HisCampRank hisCampRank){
        SerializePb.SerHisCampRank.Builder builder = SerializePb.SerHisCampRank.newBuilder();
        builder.setHisDate(hisCampRank.hisDate);
        hisCampRank.hisInfos.forEach(o -> builder.addHisInfo(serRankInfo(o)));
        return builder.build();
    }

    private SerializePb.SerRankInfo serRankInfo(HisCampRank.RankInfo rankInfo){
        SerializePb.SerRankInfo.Builder builder = SerializePb.SerRankInfo.newBuilder();
        builder.setRank(rankInfo.rank);
        builder.setCamp(rankInfo.camp);
        builder.setScore(rankInfo.score);
        builder.setFlag(rankInfo.flag);
        builder.setKilled(rankInfo.killed);
        return builder.build();
    }

    private SerializePb.SerHisMatch serHisMatch(HisMatch hisMatch){
        SerializePb.SerHisMatch.Builder builder = SerializePb.SerHisMatch.newBuilder();
        builder.setHisDate(hisMatch.hisDate);
        hisMatch.matchInfos.forEach(o -> builder.addMatchInfos(serMatchInfo(o)));
        return builder.build();
    }

    private SerializePb.SerMatchInfo serMatchInfo(HisMatch.MatchInfo matchInfo){
        SerializePb.SerMatchInfo.Builder builder = SerializePb.SerMatchInfo.newBuilder();
        builder.setRound(matchInfo.round);
        builder.setState(matchInfo.state);
        builder.setTime(matchInfo.time);
        builder.setCamp1(matchInfo.camp1);
        builder.setCamp2(matchInfo.camp2);
        builder.setScore1(matchInfo.score1);
        builder.setScore2(matchInfo.score2);
        builder.setWinCamp(matchInfo.winCamp);
        builder.setIdxGen(matchInfo.idxGen.get());
        matchInfo.fightReplays.entrySet().forEach(o -> builder.addFightReplays(serFightReplayLine(o.getKey(),o.getValue())));
        matchInfo.linesResult.entrySet().forEach(o -> builder.addLinesResult(PbHelper.createTwoIntPb(o.getKey(),o.getValue())));
        matchInfo.warReportInfos.entrySet().forEach(o -> builder.addWarReportInfos(serWarReportInfoMap(o.getKey(),o.getValue())));
        return builder.build();
    }

    private SerializePb.SerWarReportInfoMap serWarReportInfoMap(int k, HisMatch.WarReportInfo v){
        SerializePb.SerWarReportInfoMap.Builder builder = SerializePb.SerWarReportInfoMap.newBuilder();
        builder.setLine(k);
        builder.setInfo(serWarReportInfo(v));
        return builder.build();
    }

    private SerializePb.SerWarReportInfo serWarReportInfo(HisMatch.WarReportInfo info){
        SerializePb.SerWarReportInfo.Builder info_ = SerializePb.SerWarReportInfo.newBuilder();
        info_.setTotalNum1(info.totalNum1);
        info_.setLeftNum1(info.leftNum1);
        info_.setTotalNum2(info.totalNum2);
        info_.setLeftNum2(info.leftNum2);
        return info_.build();
    }

    private SerializePb.SerFightReplayLine serFightReplayLine(int k ,List<FightReplay> v){
        SerializePb.SerFightReplayLine.Builder builder = SerializePb.SerFightReplayLine.newBuilder();
        builder.setLine(k);
        v.forEach(o -> builder.addReplay(serFightReplay(o)));
        return builder.build();
    }

    private SerializePb.SerFightReplay serFightReplay(FightReplay fightReplay){
        SerializePb.SerFightReplay.Builder builder = SerializePb.SerFightReplay.newBuilder();
        builder.setObj1(serFightObject(fightReplay.obj1));
        builder.setObj2(serFightObject(fightReplay.obj2));
        builder.setOnlyIdx(fightReplay.onlyIdx);
        builder.setInfo(serWarReportInfo(fightReplay.warReportInfo));
        return builder.build();
    }

    private SerializePb.SerFightObject serFightObject(FightReplay.FightObject fightObject){
        SerializePb.SerFightObject.Builder builder = SerializePb.SerFightObject.newBuilder();
        builder.setLordId(fightObject.lordId);
        builder.setLordNick(fightObject.lordNick);
        builder.setAttackTimes(fightObject.attackTimes);
        builder.setIsWin(fightObject.isWin);
        fightObject.heroDetails.forEach(o -> builder.addHeroDetails(serFightHeroDetail(o)));
        return builder.build();
    }

    private SerializePb.SerFightHeroDetail serFightHeroDetail(FightReplay.FightHeroDetail detail){
        SerializePb.SerFightHeroDetail.Builder builder = SerializePb.SerFightHeroDetail.newBuilder();
        builder.setHeroId(detail.heroId);
        builder.setKill(detail.kill);
        builder.setLost(detail.lost);
        builder.setHp(detail.hp);
        builder.setHeroDecorated(detail.heroDecorated);
        return builder.build();
    }

    public void deser(SerializePb.SerSandTableContest serSandTableContest){
        this.setPreviewBeginDate(DateHelper.parseDate(serSandTableContest.getPreviewBeginStamp()));
        this.setOpenBeginDate(DateHelper.parseDate(serSandTableContest.getOpenBeginStamp()));
        this.setOpenEndDate(DateHelper.parseDate(serSandTableContest.getOpenEndStamp()));
        this.setExchangeEndDate(DateHelper.parseDate(serSandTableContest.getExchangeEndStamp()));
        //camps
        if(!ListUtils.isBlank(serSandTableContest.getCampsList())){
            serSandTableContest.getCampsList().forEach(o -> {
                SandTableCamp sandTableCamp = getSandTableCamp(o.getCamp());
                sandTableCamp.setCamp(o.getCamp());
                if(!ListUtils.isBlank(o.getLinesList())){
                    o.getLinesList().forEach(o1 -> {
                        SandTableCamp.LineObject lineObject = sandTableCamp.getLine(o1.getLine());
                        lineObject.deser(o1);
                    });
                }
                sandTableCamp.setResult(o.getResult());
                sandTableCamp.setScore(o.getScore());
                sandTableCamp.setFlag(o.getFlag());
                sandTableCamp.setKilled(o.getKilled());
                sandTableCamp.setTscore(o.getTscore());
                sandTableCamp.setTflag(o.getTflag());
                sandTableCamp.setTkilled(o.getTkilled());
            });
        }
        //group
        if(!ListUtils.isBlank(serSandTableContest.getMatchGroupList())){
            serSandTableContest.getMatchGroupList().forEach(o -> {
                SandTableGroup sandTableGroup = new SandTableGroup();
                sandTableGroup.deser(o);
                matchGroup.put(o.getRound(),sandTableGroup);
            });
        }
        //curr round
        this.round = serSandTableContest.getRound();
        //his camp rank
        if(!ListUtils.isBlank(serSandTableContest.getHisCampRanksList())){
            serSandTableContest.getHisCampRanksList().forEach(o -> {
                HisCampRank hisCampRank = new HisCampRank();
                hisCampRank.deser(o);
                hisCampRanks.add(hisCampRank);
            });
        }
        //his match
        if(!ListUtils.isBlank(serSandTableContest.getHisMatchesList())){
            serSandTableContest.getHisMatchesList().forEach(o -> {
                HisMatch hisMatch = new HisMatch();
                hisMatch.deser(o);
                hisMatches.add(hisMatch);
            });
        }
        setPreWinCamp(serSandTableContest.getPreWinCamp());
        setMatchDate(serSandTableContest.getMatchDate());
    }

    public int getPreWinCamp() {
        return preWinCamp;
    }

    public void setPreWinCamp(int preWinCamp) {
        this.preWinCamp = preWinCamp;
    }

    public int getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(int matchDate) {
        this.matchDate = matchDate;
    }

    public String getPreviewCron() {
        return previewCron;
    }

    public void setPreviewCron(String previewCron) {
        this.previewCron = previewCron;
    }

    public String getOpenBeginCron() {
        return openBeginCron;
    }

    public void setOpenBeginCron(String openBeginCron) {
        this.openBeginCron = openBeginCron;
    }

    public String getOpenEndCron() {
        return openEndCron;
    }

    public void setOpenEndCron(String openEndCron) {
        this.openEndCron = openEndCron;
    }
}
