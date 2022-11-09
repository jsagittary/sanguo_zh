package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.core.common.RoleConstant;
import com.gryphpoem.push.util.CheckNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName Fighter.java
 * @Description 战斗实例类
 * @date 创建时间：2017年3月31日 下午5:06:54
 */
public class Fighter {
    public List<Force> forces = new LinkedList<>();
    public Fighter oppoFighter;// 敌方
    public FightLogic fightLogic;
    public boolean isAttacker = false;
    public int hurt = 0;// 伤害记录
    public int total = 0;// 总兵力记录
    public int lost = 0;// 损失兵力记录
    public int roleType;// 角色类型
    /**
     * 勋章光环, key: roleId, val<medalAuraId, cnt>
     */
    public Map<Long, Map<Integer, Integer>> medalAura = new HashMap<>();
    /**
     * 勋章光环, <roleId, auraInfo> 客户端需要
     */
    public Map<Long, List<AuraInfo>> auraInfos = new HashMap<>();

    public Fighter() {
    }

    public Fighter(int roleType) {
        this.roleType = roleType;
    }

    public void addForce(Force force) {
        if (null != force) {
            forces.add(force);
            force.fighter = this;
            total += force.maxHp;
        }
    }

    public boolean isMyForce(long ownId) {
        return this.forces.stream().filter(force -> force.ownerId == ownId).findFirst().orElse(null) != null;
    }

    public void addRealForce(Force force) {
        if (null != force) {
            forces.add(force);
            force.fighter = this;
            total += force.hp;
        }
    }

    public boolean isPlayer() {
        return roleType == RoleConstant.PLAYER;
    }

    /**
     * 损兵是否给敌方玩家将领加经验
     *
     * @return
     */
    public boolean dropExp() {
        return roleType == RoleConstant.BANDIT || roleType == RoleConstant.CITY;
    }

    /**
     * 获取排在最前面的活着的Force对象
     *
     * @return
     */
    public Force getAliveForce() {
        for (Force force : forces) {
            if (force.alive()) {
                return force;
            }
        }
        return null;
    }

    public int cntDieForce() {
        int cnt = 0;
        for (Force force : forces) {
            if (!force.alive()) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * 获取这个玩家所有参战将领总兵排数
     *
     * @param roleId
     * @return
     */
    @Deprecated
    public int getMaxLine(long roleId) {
        return forces.stream().filter(force -> force.ownerId == roleId).mapToInt(force -> force.maxLine).sum();
    }

    /**
     * 获取这个玩家所有参战将领总死亡兵排数
     *
     * @param roleId
     * @return
     */
    @Deprecated
    public int getDeadLine(long roleId) {
        return forces.stream().filter(force -> force.ownerId == roleId).mapToInt(Force::getDeadLine).sum();
    }

    /**
     * 获取这个玩家所有参战将领击杀兵排数
     *
     * @param roleId
     * @return
     */
    public int getKillLine(long roleId) {
        int killLine = 0;
        Map<Integer, Integer> auraSkill = medalAura.get(roleId);
        if (CheckNull.isNull(auraSkill)) {
            return killLine;
        }
        killLine = auraSkill.getOrDefault(999, 0);
        return killLine;
    }

    public int getHurt() {
        return hurt;
    }

    public int getLost() {
        return lost;
    }

    public List<Force> getForces() {
        return forces;
    }

    public int getTotal() {
        return total;
    }

    public Map<Long, Map<Integer, Integer>> getMedalAura() {
        return medalAura;
    }

    public Map<Long, List<AuraInfo>> getAuraInfos() {
        return auraInfos;
    }

    /**
     * 获取玩家的参战光环
     *
     * @param roleId
     * @return <光环Id,数量>
     */
    public Map<Integer, Integer> getAuraSkill(long roleId) {
        return medalAura.get(roleId);
    }

    /**
     * 获取玩家将领对应的光环
     *
     * @param roleId
     * @return <将领Id,光环Id>
     */
    public List<AuraInfo> getAuraInfo(long roleId) {
        return auraInfos.get(roleId);
    }

    /**
     * 移除玩家的死亡将领光环
     *
     * @param force
     */
    public void subAuraSkill(Force force) {
        if (force.alive()) {
            return;
        }
        long roleId = force.ownerId;
        if (force.roleType == RoleConstant.PLAYER && roleId != 0) {
            Map<Integer, Integer> auraSkill = medalAura.get(roleId);
            List<AuraInfo> auraInfos = this.auraInfos.get(roleId);
            if (CheckNull.isNull(auraSkill) || CheckNull.isEmpty(auraInfos)) {
                return;
            }
            int heroId = force.id;
            AuraInfo auraInfo = auraInfos.stream().filter(info -> info.getHeroId() == heroId).findFirst().orElse(null);
            int auraId = CheckNull.isNull(auraInfo) ? 0 : auraInfo.getMedalAuraId();
            if (auraId <= 0) {
                return;
            }
            int val = auraSkill.get(auraId);
            if (val <= 0) {
                return;
            }
            auraSkill.put(auraId, val - 1);
            medalAura.put(roleId, auraSkill);
        }
    }

    /**
     * 添加击杀排数记录
     *
     * @param force
     */
    public void addKillLine(Force force) {
        long roleId = force.ownerId;
        if (force.roleType == RoleConstant.PLAYER && roleId != 0) {
            Map<Integer, Integer> auraSkill = medalAura.get(roleId);
            if (CheckNull.isNull(auraSkill)) {
                auraSkill = new HashMap<>();
            }
            int val = auraSkill.getOrDefault(999, 0);
            auraSkill.put(999, val + 1);
            medalAura.put(roleId, auraSkill);
        }
    }

    @Override
    public String toString() {
        return "Fighter [hurt=" + hurt + ", total=" + total + ", lost=" + lost + ", roleType=" + roleType + "]";
    }

}
