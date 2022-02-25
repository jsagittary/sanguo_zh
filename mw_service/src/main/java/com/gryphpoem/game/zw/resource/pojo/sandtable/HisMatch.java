package com.gryphpoem.game.zw.resource.pojo.sandtable;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.ListUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HisMatch {
    public int hisDate;
    public List<MatchInfo> matchInfos = new ArrayList<>();

    public static class MatchInfo {
        public int round;
        public int state;//0未开始 1已结束
        public int time;
        public int camp1;
        public int camp2;
        public int score1;
        public int score2;
        public int winCamp;//胜利阵营 0平
        public AtomicInteger idxGen = new AtomicInteger(1);
        public Map<Integer, List<FightReplay>> fightReplays = new HashMap<>(3);//key: 1-3
        public Map<Integer, Integer> linesResult = new HashMap<>(3);//value 胜利方阵营id 0平局 key: 1-3
        public Map<Integer, WarReportInfo> warReportInfos = new HashMap<>(3);//key: 1-3

        public MatchInfo() {
            fightReplays.put(1, new ArrayList<>());
            fightReplays.put(2, new ArrayList<>());
            fightReplays.put(3, new ArrayList<>());
        }

        public void deser(SerializePb.SerMatchInfo serMatchInfo) {
            this.round = serMatchInfo.getRound();
            this.state = serMatchInfo.getState();
            this.time = serMatchInfo.getTime();
            this.camp1 = serMatchInfo.getCamp1();
            this.camp2 = serMatchInfo.getCamp2();
            this.score1 = serMatchInfo.getScore1();
            this.score2 = serMatchInfo.getScore2();
            this.winCamp = serMatchInfo.getWinCamp();
            this.idxGen.set(serMatchInfo.getIdxGen()==0?1:serMatchInfo.getIdxGen());
            if (!ListUtils.isBlank(serMatchInfo.getFightReplaysList())) {
                serMatchInfo.getFightReplaysList().forEach(o -> {
                    List<FightReplay> replayList = this.fightReplays.get(o.getLine());
                    if (!ListUtils.isBlank(o.getReplayList())) {
                        o.getReplayList().forEach(o1 -> {
                            FightReplay fightReplay = new FightReplay();
                            fightReplay.deser(o1);
                            replayList.add(fightReplay);
                        });
                    }
                });
            }
            Optional.ofNullable(serMatchInfo.getLinesResultList()).ifPresent(tmp -> tmp.forEach(o -> {
                linesResult.put(o.getV1(),o.getV2());
            }));
            Optional.ofNullable(serMatchInfo.getWarReportInfosList()).ifPresent(tmp -> tmp.forEach(o -> {
                WarReportInfo warReportInfo = new WarReportInfo();
                warReportInfo.deser(o.getInfo());
                warReportInfos.put(o.getLine(),warReportInfo);
            }));
        }
    }

    public static class WarReportInfo {
        public int totalNum1;
        public int leftNum1;
        public int totalNum2;
        public int leftNum2;

        public WarReportInfo(){}

        public WarReportInfo(int totalNum1, int leftNum1, int totalNum2, int leftNum2) {
            this.totalNum1 = totalNum1;
            this.leftNum1 = leftNum1;
            this.totalNum2 = totalNum2;
            this.leftNum2 = leftNum2;
        }

        public void deser(SerializePb.SerWarReportInfo serWarReportInfo){
            this.totalNum1 = serWarReportInfo.getTotalNum1();
            this.leftNum1 = serWarReportInfo.getLeftNum1();
            this.totalNum2 = serWarReportInfo.getTotalNum2();
            this.leftNum2 = serWarReportInfo.getLeftNum2();
        }
    }

    public void deser(SerializePb.SerHisMatch serHisMatch) {
        this.hisDate = serHisMatch.getHisDate();
        if (!ListUtils.isBlank(serHisMatch.getMatchInfosList())) {
            serHisMatch.getMatchInfosList().forEach(o -> {
                MatchInfo matchInfo = new MatchInfo();
                matchInfo.deser(o);
                matchInfos.add(matchInfo);
            });
        }
    }
}
