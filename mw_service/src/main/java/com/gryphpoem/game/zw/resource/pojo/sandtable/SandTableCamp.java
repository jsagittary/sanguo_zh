package com.gryphpoem.game.zw.resource.pojo.sandtable;

import com.alibaba.fastjson.annotation.JSONField;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;

import java.util.*;

public class SandTableCamp {
    private int camp;
    @JSONField(serialize = false)
    private Map<Integer, LineObject> lines;
    private int result;
    private int score;
    private int flag;
    private int killed;

    private int tscore;
    private int tflag;
    private int tkilled;

    public SandTableCamp(){
        lines = new HashMap<>(3);
        for(int line : SandTableContestService.LINES){
            LineObject lineObject = lines.get(line);
            if(lineObject == null){
                lineObject = new LineObject();
                lineObject.list = new ArrayList<>();
                lineObject.line = line;
                lines.put(line,lineObject);
            }
        }
    }

    public void resetLinePlayers(){
        lines.values().forEach(o -> {
            o.list.forEach(o1 -> {
                o1.fightTimes = 0;
                o1.killingNum = 0;
                o1.tmpFight = false;
                o1.tmpFought = false;
                o1.isAlive = true;
            });
            o.result = 0;
        });
        result = 0;
    }

    public LinePlayer getMyLine(long roleId){
        LinePlayer linePlayer;
        for(Map.Entry<Integer,LineObject> entry : lines.entrySet()){
            linePlayer = entry.getValue().list.stream().filter(o -> o.lordId == roleId).findFirst().orElse(null);
            if(Objects.nonNull(linePlayer))
                return linePlayer;

        }
        return null;
    }

    public void clearAndSum(){
        this.tflag += this.flag;
        this.tscore += this.score;
        this.tkilled += this.killed;

        this.flag = 0;
        this.score = 0;
        this.killed = 0;
    }

    public void clearData(){
        this.flag = 0;
        this.score = 0;
        this.killed = 0;
        this.result = 0;
        this.tflag = 0;
        this.tscore = 0;
        this.tkilled = 0;
    }

    public boolean removeLinePlayer(LinePlayer linePlayer){
        return lines.get(linePlayer.line).list.remove(linePlayer);
    }

    public LineObject getLine(int line){
        return lines.get(line);
    }

    public Map<Integer, LineObject> getLines() {
        return lines;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getKilled() {
        return killed;
    }

    public void setKilled(int killed) {
        this.killed = killed;
    }

    public int getTscore() {
        return tscore;
    }

    public void setTscore(int tscore) {
        this.tscore = tscore;
    }

    public int getTflag() {
        return tflag;
    }

    public void setTflag(int tflag) {
        this.tflag = tflag;
    }

    public int getTkilled() {
        return tkilled;
    }

    public void setTkilled(int tkilled) {
        this.tkilled = tkilled;
    }

    public static class LineObject {
        public List<LinePlayer> list;
        public int result;
        public int killed;
        public int line;

        public void deser(SerializePb.SerLineObject serLineObject){
            if(!ListUtils.isBlank(serLineObject.getListList())){
                serLineObject.getListList().forEach(o -> {
                    LinePlayer linePlayer = new LinePlayer();
                    linePlayer.deser(o);
                    this.list.add(linePlayer);
                });
            }
            this.result = serLineObject.getResult();
            this.killed = serLineObject.getKilled();
            this.line = serLineObject.getLine();
        }
    }
}
