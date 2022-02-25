package com.gryphpoem.game.zw.resource.pojo.robot;

import com.gryphpoem.game.zw.resource.constant.RobotConstant;

/**
 * @Description 机器人状态量记录类，用于记录一些仅需要在内存中存在的记录值
 * @author TanDonghai
 * @date 创建时间：2017年10月17日 下午3:13:20
 *
 */
public class RobotRecord {
    /**
     * 记录是否需要执行自动穿戴装备逻辑
     */
    private boolean needDressEquip;
    /**
     * 记录下次启动招募将领逻辑的时间
     */
    private int nextRecruitHero;
    /**
     * 记录下次启动自动募兵逻辑的时间
     */
    private int nextRecruitArm;
    /**
     * 下次装备改造时间
     */
    private int nextEquipRefit;
    /**
     * 下次自动增加VIP经验的时间
     */
    private int nextAddVipExp;

    /**
     * 记录下次自动增加角色经验的时间
     */
    private int nextAddRoleExp;
    /**
     * 下次自动增加金币的时间
     */
    private int nextAddGold;
    /**
     * 下次攻打副本的时间
     */
    private int nextDoCombat;

    /** 下次启动矿点采集逻辑的时间 */
    private int nextCollectMine;

    /** 下次启动攻打流寇的时间 */
    private int nextAttackBandit;

    public void initData(int now) {
        needDressEquip = true;
        nextRecruitArm = now + RobotConstant.ARM_RECRUIT_DELAY;
        nextAddGold = now;
        nextDoCombat = now;
        nextCollectMine = now;
        nextAttackBandit = now;
    }

    public boolean isNeedRecruitHero(int now) {
        return nextRecruitHero <= now;
    }

    public boolean isNeedRecruitArm(int now) {
        return nextRecruitArm <= now;
    }

    public boolean isNeedEquipRefit(int now) {
        return nextEquipRefit <= now;
    }

    public boolean isNeedAddVipExp(int now) {
        return nextAddVipExp <= now;
    }

    public boolean isNeedAddRoleExp(int now) {
        return nextAddRoleExp <= now;
    }

    public boolean isNeedAddGold(int now) {
        return nextAddGold <= now;
    }

    public boolean isNeedDoCombat(int now) {
        return nextDoCombat <= now;
    }

    public boolean isNeedCollectMine(int now) {
        return nextCollectMine <= now;
    }

    public boolean isNeedAttackBandit(int now) {
        return nextAttackBandit <= now;
    }

    public boolean isNeedDressEquip() {
        return needDressEquip;
    }

    public void setNeedDressEquip(boolean needDressEquip) {
        this.needDressEquip = needDressEquip;
    }

    public int getNextRecruitHero() {
        return nextRecruitHero;
    }

    public void setNextRecruitHero(int nextRecruitHero) {
        this.nextRecruitHero = nextRecruitHero;
    }

    public int getNextRecruitArm() {
        return nextRecruitArm;
    }

    public void setNextRecruitArm(int nextRecruitArm) {
        this.nextRecruitArm = nextRecruitArm;
    }

    public int getNextEquipRefit() {
        return nextEquipRefit;
    }

    public void setNextEquipRefit(int nextEquipRefit) {
        this.nextEquipRefit = nextEquipRefit;
    }

    public int getNextAddVipExp() {
        return nextAddVipExp;
    }

    public void setNextAddVipExp(int nextAddVipExp) {
        this.nextAddVipExp = nextAddVipExp;
    }

    public int getNextAddRoleExp() {
        return nextAddRoleExp;
    }

    public void setNextAddRoleExp(int nextAddRoleExp) {
        this.nextAddRoleExp = nextAddRoleExp;
    }

    public int getNextAddGold() {
        return nextAddGold;
    }

    public void setNextAddGold(int nextAddGold) {
        this.nextAddGold = nextAddGold;
    }

    public int getNextDoCombat() {
        return nextDoCombat;
    }

    public void setNextDoCombat(int nextDoCombat) {
        this.nextDoCombat = nextDoCombat;
    }

    public int getNextCollectMine() {
        return nextCollectMine;
    }

    public void setNextCollectMine(int nextCollectMine) {
        this.nextCollectMine = nextCollectMine;
    }

    public int getNextAttackBandit() {
        return nextAttackBandit;
    }

    public void setNextAttackBandit(int nextAttackBandit) {
        this.nextAttackBandit = nextAttackBandit;
    }

}
