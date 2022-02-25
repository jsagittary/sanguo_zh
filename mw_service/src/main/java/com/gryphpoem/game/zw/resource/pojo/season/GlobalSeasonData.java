package com.gryphpoem.game.zw.resource.pojo.season;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.domain.s.StaticSeasonPlan;
import com.gryphpoem.game.zw.resource.pojo.ActRank;

import java.util.LinkedList;
import java.util.Objects;

/**
 * 赛季全局数据
 * @author xwind
 * @date 2021/4/16
 */
public class GlobalSeasonData {

    private int currSeasonId;
    private int lastSeasonId;
    private int currSeasonTalentId;

    private StaticSeasonPlan staticSeasonPlan;

    /**
     * 不在参与序列化和反序列化
     */
    @JSONField(serialize = false)
    private LinkedList<CampRankData> campRank = new LinkedList<>();
    @JSONField(serialize = false)
    private LinkedList<ActRank> ranks = new LinkedList<>();

    public GlobalSeasonData() {
    }

    public ActRank getRank(long roleId){
        int i=1;
        for (ActRank rank : ranks) {
            if(rank.getLordId() == roleId){
                rank.setRank(i);
                return rank;
            }
            i++;
        }
        return null;
    }

    public int getCurrSeasonId() {
        return currSeasonId;
    }

    public void setCurrSeasonId(int currSeasonId) {
        this.currSeasonId = currSeasonId;
    }

    public int getCurrSeasonTalentId() {
        return currSeasonTalentId;
    }

    public void setCurrSeasonTalentId(int currSeasonTalentId) {
        this.currSeasonTalentId = currSeasonTalentId;
    }

    public SerializePb.SerSeasonGlobalData ser(){
        SerializePb.SerSeasonGlobalData.Builder builder = SerializePb.SerSeasonGlobalData.newBuilder();
        builder.setCurrSeasonId(currSeasonId);
        builder.setLastSeasonId(lastSeasonId);
        builder.setStaticPlan(JSON.toJSONString(staticSeasonPlan));
        builder.setCurrSeasonTalentId(currSeasonTalentId);
//        campRank.forEach(tmp -> builder.addCampRank(buildCampRankInfo(tmp)));
        return builder.build();
    }

    public CommonPb.CampRankInfo buildCampRankInfo(CampRankData campRankData){
        CommonPb.CampRankInfo.Builder builder = CommonPb.CampRankInfo.newBuilder();
        builder.setCamp(campRankData.camp);
        builder.setValue(campRankData.value);
        builder.setTime(campRankData.time);
        builder.setRank(campRankData.rank);
        return builder.build();
    }

    public void deser(SerializePb.SerSeasonGlobalData serData){
        this.currSeasonId = serData.getCurrSeasonId();
        this.lastSeasonId = serData.getLastSeasonId();
        this.currSeasonTalentId = serData.getCurrSeasonTalentId();
        if(Objects.nonNull(serData.getStaticPlan())){
            this.staticSeasonPlan = JSON.parseObject(serData.getStaticPlan(),StaticSeasonPlan.class);
        }
//        serData.getCampRankList().forEach(tmp -> this.campRank.add(new CampRankData(tmp.getCamp(),tmp.getValue(),tmp.getTime(),tmp.getRank())));
    }

    public void clear(){
        this.currSeasonId = 0;
        this.lastSeasonId = 0;
        this.staticSeasonPlan = null;
        campRank.clear();
        ranks.clear();
    }

    public int getLastSeasonId() {
        return lastSeasonId;
    }

    public void setLastSeasonId(int lastSeasonId) {
        this.lastSeasonId = lastSeasonId;
    }

    public StaticSeasonPlan getStaticSeasonPlan() {
        return staticSeasonPlan;
    }

    public void setStaticSeasonPlan(StaticSeasonPlan staticSeasonPlan) {
        this.staticSeasonPlan = staticSeasonPlan;
    }

    public LinkedList<ActRank> getRanks() {
        return ranks;
    }

    public void setRanks(LinkedList<ActRank> ranks) {
        this.ranks = ranks;
    }

    public LinkedList<CampRankData> getCampRank() {
        return campRank;
    }

    public void setCampRank(LinkedList<CampRankData> campRank) {
        this.campRank = campRank;
    }
}
