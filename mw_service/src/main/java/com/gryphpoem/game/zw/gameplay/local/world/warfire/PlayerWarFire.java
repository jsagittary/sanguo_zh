package com.gryphpoem.game.zw.gameplay.local.world.warfire;

import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @program: civilization_zh
 * @description: 战火燎原玩家数据记录
 * @author: zhou jie
 * @create: 2020-12-24 15:33
 */
public class PlayerWarFire {

    /**
     * 未报名
     */
    public static final int NOT_REGISTRY_STATUS = 0;
    /**
     * 报名状态, 表示玩家进入过活动
     */
    public static final int REGISTRY_STATUS = 1;
    /**
     * 玩家id
     */
    private long roleId;
    /**
     * key: buffType
     */
    private Map<Integer, WarFireBuff> buffs = new HashMap<>();
    /**
     * 报名状态
     */
    private int status = NOT_REGISTRY_STATUS;

    /**
     * 玩家积分
     */
    private int score;

    /**
     * 首次占领据点获得的积分
     */
    private int firstOccupyScore;

    /**
     * 杀敌数
     */
    private int killed;

    private LinkedList<WarFireEvent> events = new LinkedList<>();

    public void addWarFireEvent(WarFireEvent evt) {
        events.add(evt);
        while (events.size() >= 50) {
            events.removeFirst();
        }
    }

    /**
     * 据点首杀与据点持续产出积分
     *
     * @param ety
     * @param cityId
     * @param pos
     * @param heroId
     * @param score
     */
    public void addWarFireEvent(int ety, int cityId, int pos, int heroId, int score) {
        WarFireEvent event = new WarFireEvent(ety, cityId, pos, heroId, score);
        addWarFireEvent(event);
    }

    /**
     * 增加战火事件
     *
     * @param ety    事件类型
     * @param cityId 据点ID(矿点ID)
     * @param pos    事件发生地点
     * @param heroId 战斗部队的英雄ID
     * @param killed 杀敌数量
     * @param lost   损兵数量
     */
    public void addWarFireEvent(int ety, int cityId, int pos, int heroId, int killed, int lost, String enemyName, int enemyPos, int enemyHeroId) {
        WarFireEvent event = new WarFireEvent(ety, cityId, pos, heroId, killed, lost, enemyName, enemyPos, enemyHeroId);
        addWarFireEvent(event);
    }

    /**
     * 进攻或者防守玩家主城
     * @param ety
     * @param enemyName
     * @param pos
     * @param killed
     * @param lost
     */
    public void addWarFireEvent(int ety, String enemyName, int pos, int killed, int lost){
        WarFireEvent event = new WarFireEvent(ety, 0, pos, 0, killed, lost, enemyName, 0, 0);
        addWarFireEvent(event);
    }


    /**
     * 商店的兑换记录
     */
    private Map<Integer, Integer> buyRecord = new HashMap<>();

    public PlayerWarFire() {
    }

    public PlayerWarFire(long roleId) {
        this.roleId = roleId;
    }

    public PlayerWarFire(CommonPb.PlayerWarFirePb pb) {
        this();
        this.roleId = pb.getRoleId();
        this.status = pb.getStatus();
        this.score = pb.getScore();
        this.firstOccupyScore = pb.getFirstOccupyScore();
        List<CommonPb.WarFireBuffPb> buffList = pb.getBuffList();
        if (!CheckNull.isEmpty(buffList)) {
            buffList.forEach(buff -> this.buffs.put(buff.getType(), new WarFireBuff(buff)));
        }
        List<CommonPb.TwoInt> buyRecordList = pb.getBuyRecordList();
        if (!CheckNull.isEmpty(buyRecordList)) {
            buyRecordList.forEach(br -> this.buyRecord.put(br.getV1(), br.getV2()));
        }
        this.killed = pb.getKilled();
        List<CommonPb.WarFireEventPb> wfevtList = pb.getWfevtList();
        if (!CheckNull.isEmpty(wfevtList)) {
            wfevtList.forEach(evtpb -> {
                this.events.add(new WarFireEvent(evtpb));
            });
        }
    }

    /**
     * 战火燎原玩家信息
     *
     * @param pwf 战火燎原玩家数据记录
     * @return PlayerWarFirePb
     */
    public static CommonPb.PlayerWarFirePb.Builder toPlayerInfoPb(PlayerWarFire pwf) {
        CommonPb.PlayerWarFirePb.Builder builder = CommonPb.PlayerWarFirePb.newBuilder();
        //报名状态
        builder.setStatus(pwf.getStatus());
        //BUFF
        int now = TimeHelper.getCurrentSecond();
        pwf.getBuffs().values().stream().filter(buff -> buff.getEndTime() > now).forEach(wfb -> builder.addBuff(WarFireBuff.toBuffInfo(wfb)));
        //角色ID
        builder.setRoleId(pwf.getRoleId());
        //玩家积分
        builder.setScore(pwf.getScore());
        //购买记录
        Map<Integer, Integer> buyRecord = pwf.getBuyRecord();
        StaticCrossWorldDataMgr.getStaticWarFireShopMap().values().forEach(shop -> builder.addBuyRecord(PbHelper.createTwoIntPb(shop.getId(), buyRecord.getOrDefault(shop.getId(), 0))));
        //杀敌数量
        builder.setKilled(pwf.getKilled());
        //首杀积分
        builder.setFirstOccupyScore(pwf.getFirstOccupyScore());
        //玩家事件记录
        if (!CheckNull.isEmpty(pwf.getEvents())) {
            pwf.getEvents().forEach(event -> builder.addWfevt(event.ser()));
        }
        return builder;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<Integer, WarFireBuff> getBuffs() {
        return buffs;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<Integer, Integer> getBuyRecord() {
        return buyRecord;
    }

    public void setBuyRecord(Map<Integer, Integer> buyRecord) {
        this.buyRecord = buyRecord;
    }

    public int getKilled() {
        return killed;
    }

    public void setKilled(int killed) {
        this.killed = killed;
    }

    public int getFirstOccupyScore() {
        return firstOccupyScore;
    }

    public void setFirstOccupyScore(int firstOccupyScore) {
        this.firstOccupyScore = firstOccupyScore;
    }

    public List<WarFireEvent> getEvents() {
        return events;
    }
}