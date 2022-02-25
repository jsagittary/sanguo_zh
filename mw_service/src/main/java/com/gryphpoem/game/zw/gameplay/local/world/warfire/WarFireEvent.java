package com.gryphpoem.game.zw.gameplay.local.world.warfire;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-01-14 17:34
 */
public class WarFireEvent {
    /*
    1.时间点 您的英雄【黄忠】（名称带品质） 成功占领【据点名称】[100,100]时，获得首次个人资源2000
    2.时间点 您的英雄【黄忠】（名称带品质） 占领【据点名称】[100,100]时,获得200个人资源/分钟加成
    3.时间点 您的英雄【黄忠】（名称带品质）对【据点名称】[100,100]内【玩家名称】[100,100]的【拉美西斯二世】（名称带品质）发起进攻，杀敌99999，损兵99999
    4.时间点 您的英雄【黄忠】（名称带品质）对【据点名称】[100,100]发起驻守，杀敌99999，损兵99999
    5.时间点 您的英雄【黄忠】（名称带品质）对【玩家名称】[100,100]的【拉美西斯二世】（名称带品质）占领的【资源点名称】[100,100] 发起进攻，杀敌99999，损兵99999
    6.时间点 您的英雄【黄忠】（名称带品质）在采集【资源点名称】[100,100]时，被【玩家名称】[100,100]的【拉美西斯二世】（名称带品质）攻击，杀敌99999，损兵99999
    7.时间点 您对【玩家名称】[100,100]的主城发起进攻，杀敌99999，损兵99999
    8.时间点 您的主城被【玩家名称】[100,100]进攻，杀敌99999，损兵99999
    9.时间点 您的英雄【黄忠】（名称带品质）占领【资源点名称】[100,100]时,获得200个人资源/分钟加成
     */
    public static int ETY_CITY_FIRST_BLOOD = 1;//1-据点首杀
    public static int ETY_CITY_OUTPUT = 2;//2-据点占领(1分钟)

    public static int ETY_CITY_ATTACKER = 3;//3-据点进攻杀敌
    public static int ETY_CITY_DEFENDER = 4;//4-据点防守杀敌
    public static int ETY_MINE_ATTACKER = 5;//5-矿点进攻杀敌
    public static int ETY_MINE_DEFENDER = 6;//6-矿点防守杀敌
    public static int ETY_PLAYER_ATTACKER = 7;//7-玩家主城进攻杀敌
    public static int ETY_PLAYER_DEFENDER = 8;//8-玩家主城防守杀敌
    public static int ETY_MINE_OUTPUT = 9;//9-矿点持续产出


    private int ety;//事件类型
    private int entityId;//据点ID,或者矿点ID
    private int pos;//事件发生位置
    private int heroId;//将领ID
    private int killed;//杀敌数量
    private int lost;//损兵数量
    private String enemyName;
    private int enemyPos;
    private int enemyHeroId;
    private int score;//首杀积分

    private int time;//事件发生时间, 单位: sec

    /**
     * 首杀
     *
     * @param ety
     * @param entityId
     * @param pos
     * @param heroId
     * @param score
     */
    public WarFireEvent(int ety, int entityId, int pos, int heroId, int score) {
        this.ety = ety;
        this.entityId = entityId;
        this.heroId = heroId;
        this.pos = pos;
        this.score = score;
        this.time = TimeHelper.getCurrentSecond();
    }

    /**
     * 杀敌
     *
     * @param ety
     * @param entityId
     * @param pos
     * @param heroId
     * @param killed
     * @param lost
     */
    public WarFireEvent(int ety, int entityId, int pos, int heroId, int killed, int lost, String enemyName, int enemyPos, int enemyHeroId) {
        this.ety = ety;
        this.entityId = entityId;
        this.heroId = heroId;
        this.pos = pos;
        this.killed = killed;
        this.lost = lost;
        this.enemyName = enemyName;
        this.enemyPos = enemyPos;
        this.enemyHeroId = enemyHeroId;
        this.time = TimeHelper.getCurrentSecond();
    }

    public WarFireEvent(CommonPb.WarFireEventPb pb) {
        this.ety = pb.getEty();
        this.time = pb.getTime();
        this.pos = pb.getPos();
        this.entityId = pb.getEntityId();
        this.heroId = pb.getHeroId();
        this.killed = pb.getKilled();
        this.lost = pb.getLost();
        this.score = pb.getScore();
        this.enemyName = pb.getEnemyName();
        this.enemyPos = pb.getEnemyPos();
        this.enemyHeroId = pb.getEnemyHeroId();
    }

    public CommonPb.WarFireEventPb ser() {
        CommonPb.WarFireEventPb.Builder builder = CommonPb.WarFireEventPb.newBuilder();
        builder.setEty(ety);
        builder.setTime(time);
        builder.setEntityId(entityId);
        builder.setPos(pos);
        builder.setHeroId(heroId);
        builder.setKilled(killed);
        builder.setLost(lost);
        builder.setScore(score);
        builder.setEnemyHeroId(enemyHeroId);
        builder.setEnemyPos(enemyPos);
        if (Objects.nonNull(enemyName)){
            builder.setEnemyName(enemyName);
        }
        return builder.build();
    }


    public static WarFireEvent deSer(CommonPb.WarFireEventPb pb) {
        return new WarFireEvent(pb);
    }

    public int getKilled() {
        return killed;
    }

    public void setKilled(int killed) {
        this.killed = killed;
    }

    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getEty() {
        return ety;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getHeroId() {
        return heroId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
