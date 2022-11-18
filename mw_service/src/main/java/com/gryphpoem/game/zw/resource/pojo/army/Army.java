package com.gryphpoem.game.zw.resource.pojo.army;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.dressup.DressUp;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.*;

/**
 * @author TanDonghai
 * @ClassName Army.java
 * @Description 行军队列
 * @date 创建时间：2017年3月31日 下午3:44:28
 */
public class Army {
    private int keyId;
    /**
     * 行军类型
     */
    private int type;
    /**
     * 起点坐标
     */
    private int originPos = -1;
    /**
     * 目标坐标
     */
    private int target;
    /**
     * 当前状态
     */
    private volatile int state;
    /**
     * 记录将领id, v1为将领id, v2为兵力不可信任   【chenqi:只会有一个将领】
     */
    private List<CommonPb.PartnerHeroIdPb> hero;
    /**
     * 预计耗时
     */
    private int duration;
    /**
     * 预期结束时间
     */
    private int endTime;
    /**
     * 部队采集到的资源，服务端用
     */
    private List<Award> grab;
    /**
     * 战斗id
     */
    private Integer battleId;
    /**
     * 如果目标是流寇或矿点，记录流寇或矿点的id
     */
    private int targetId;
    /**
     * 属于玩家id
     */
    private long lordId;
    /**
     * 目标玩家, 如果是驻防部队也有此值,如果是盖世太保就是盖世太保的id
     */
    private long tarLordId;
    /**
     * 客户端用
     */
    private int battleTime;
    /**
     * 采集的时长,用于加皇城经验
     */
    private int collectTime;
    /**
     * 名城Buffer使用, v1为CiytId, v2为CityCamp
     */
    private TwoInt originCity;
    /**
     * 客户端使用, 勋章
     */
    private List<CommonPb.Medal> heroMedals;
    /**
     * 行军消耗, 用于异常行军, 返回消耗
     */
    private List<CommonPb.Award> marchConsume;

    /**
     * 赛季天赋优化，驻城守军属性加成
     */
    private List<TwoInt> seasonTalentAttr;

    /**
     * 子类型,  举个栗子
     * type = 10, 拜访圣坛
     * subType = 1, 金币拜访
     * subType = 2, 钻石拜访
     */
    private int subType;

    /**
     * 行军特效id, 在创建部队的时候确定
     */
    private int marchLineId;
    private Map<Integer, Integer> recoverMap;//伤病恢复记录
    private Map<Integer, Integer> totalLostHpMap;//将领损兵记录. k:将领ID, v:累计损兵记录

    public Army() {
    }

    public Army(int keyId, int type, int target, int state, List<CommonPb.PartnerHeroIdPb> hero, int duration, int endTime, DressUp dressUp) {
        setKeyId(keyId);
        setType(type);
        setTarget(target);
        setState(state);
        setHero(hero);
        setDuration(duration);
        setEndTime(endTime);
        if (Objects.nonNull(dressUp)) {
            // 记录行军特效
            marchLineId = dressUp.getCurMarchEffect();
        }
    }

    public Army(CommonPb.Army army) {
        setKeyId(army.getKeyId());
        setType(army.getType());
        setTarget(army.getTarget());
        setState(army.getState());
        setDuration(army.getDuration());
        setEndTime(army.getEndTime());
        setHero(army.getHeroList());
        setGrab(army.getGrabList());
        setLordId(army.getLordId());
        setBattleTime(army.getBattleTime());
        setTarLordId(army.getTarLordId());
        if (army.hasBattleId()) {
            setBattleId(army.getBattleId());
        }
        if (army.hasId()) {
            setTargetId(army.getId());
        }
        if (army.hasOriginPos()) {
            setOriginPos(army.getOriginPos());
        }
        if (!CheckNull.isEmpty(army.getMarchConsumeList())) {
            marchConsume = army.getMarchConsumeList();
        }
        if (!CheckNull.isEmpty(army.getSeasonTalentAttrList())) {
            seasonTalentAttr = army.getSeasonTalentAttrList();
        }
        subType = army.getSubType();
        marchLineId = army.getMarchLine();
    }

    public List<TwoInt> getSeasonTalentAttr() {
        return seasonTalentAttr;
    }

    public void setSeasonTalentAttr(List<TwoInt> seasonTalentAttr) {
        this.seasonTalentAttr = seasonTalentAttr;
    }

    /**
     * 驻防结束，清除赛季天赋属性
     */
    public void clearSeasonAttr() {
        Optional.ofNullable(this.seasonTalentAttr).ifPresent(seasonTalentAttr -> seasonTalentAttr.clear());
    }

    public long getTarLordId() {
        return tarLordId;
    }

    public void setTarLordId(long tarLordId) {
        this.tarLordId = tarLordId;
    }

    public int getBeginTime() {
        return endTime - duration;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<CommonPb.PartnerHeroIdPb> getHero() {
        return hero;
    }

    public void setHero(List<CommonPb.PartnerHeroIdPb> hero) {
        this.hero = hero;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public List<Award> getGrab() {
        return grab;
    }

    public void setGrab(List<Award> grab) {
        this.grab = grab;
    }

    public Integer getBattleId() {
        return battleId;
    }

    public void setBattleId(Integer battleId) {
        this.battleId = battleId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public boolean isRetreat() {
        return getState() == ArmyConstant.ARMY_STATE_RETREAT;
    }

    public boolean isCollect() {
        return getState() == ArmyConstant.ARMY_STATE_COLLECT;
    }

    public boolean isGuard() {
        return getState() == ArmyConstant.ARMY_STATE_GUARD;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getBattleTime() {
        return battleTime;
    }

    public void setBattleTime(int battleTime) {
        this.battleTime = battleTime;
    }

    public int getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(int collectTime) {
        this.collectTime = collectTime;
    }

    public int getOriginPos() {
        return originPos;
    }

    public void setOriginPos(int originPos) {
        this.originPos = originPos;
    }

    public TwoInt getOriginCity() {
        return originCity;
    }

    public void setOriginCity(TwoInt originCity) {
        this.originCity = originCity;
    }

    public List<CommonPb.Medal> getHeroMedals() {
        return heroMedals;
    }

    public void setHeroMedals(List<CommonPb.Medal> heroMedals) {
        this.heroMedals = heroMedals;
    }

    /**
     * 获取部队总兵力
     *
     * @return
     */
    public int getArmCount() {
        int count = 0;
        for (CommonPb.PartnerHeroIdPb twoInt : hero) {
            count += twoInt.getCount();
        }
        return count;
    }

    public List<Award> getMarchConsume() {
        return marchConsume;
    }

    public void setMarchConsume(List<Award> marchConsume) {
        this.marchConsume = marchConsume;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public int getMarchLineId() {
        return marchLineId;
    }

    public int getHeroLeadCount() {
        if (CheckNull.isEmpty(hero))
            return 0;
        return hero.stream().mapToInt(CommonPb.PartnerHeroIdPb::getCount).sum();
    }

    public Map<Integer, Integer> getAndCreateIfAbsentTotalLostHpMap() {
        if (totalLostHpMap == null) totalLostHpMap = new HashMap<>();
        return totalLostHpMap;
    }

    public Map<Integer, Integer> getRecoverMap() {
        return recoverMap;
    }

    public Map<Integer, Integer> getAndCreateIfAbsentRecoverMap() {
        if (recoverMap == null) recoverMap = new HashMap<>();
        return recoverMap;
    }

    public void setRecoverMap(Map<Integer, Integer> recoverMap) {
        this.recoverMap = recoverMap;
    }

    public void setHeroState(Player player, int state) {
        if (CheckNull.isEmpty(this.getHero())) return;
        for (CommonPb.PartnerHeroIdPb twoInt : this.getHero()) {
            Hero hero = player.heros.get(twoInt.getPrincipleHeroId());
            hero.setState(state);
            if (!CheckNull.isEmpty(twoInt.getDeputyHeroIdList())) {
                twoInt.getDeputyHeroIdList().forEach(id -> {
                    Hero hero_ = player.heros.get(id);
                    hero_.setState(state);
                });
            }
        }
    }

    public boolean inArmy(int heroId) {
        if (CheckNull.isEmpty(this.getHero())) return false;
        for (CommonPb.PartnerHeroIdPb twoInt : this.getHero()) {
            if (CheckNull.isNull(twoInt)) continue;
            if (heroId == twoInt.getPrincipleHeroId()) return true;
            if (CheckNull.nonEmpty(twoInt.getDeputyHeroIdList())) {
                if (Objects.nonNull(twoInt.getDeputyHeroIdList().stream().filter(
                        heroId_ -> heroId_ == heroId).findFirst().orElse(null))) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Army{" +
                "keyId=" + keyId +
                ", type=" + type +
                ", originPos=" + originPos +
                ", target=" + target +
                ", state=" + state +
                ", hero=" + hero +
                ", duration=" + duration +
                ", endTime=" + endTime +
                ", grab=" + grab +
                ", battleId=" + battleId +
                ", targetId=" + targetId +
                ", lordId=" + lordId +
                ", tarLordId=" + tarLordId +
                ", battleTime=" + battleTime +
                ", collectTime=" + collectTime +
                ", originCity=" + originCity +
                ", heroMedals=" + heroMedals +
                ", marchConsume=" + marchConsume +
                ", subType=" + subType +
                ", marchLineId=" + marchLineId +
                '}';
    }

}
