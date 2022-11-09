package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author TanDonghai
 * @ClassName Force.java
 * @Description 记录战斗中单个将领或npc的战斗信息
 * @date 创建时间：2017年3月31日 下午5:14:08
 */
public class Force {
    public int id;//将领id
    public int hp;// 当前血量，兵力
    public int maxHp;// 总血量，兵力
    public int armType;// 兵种类型
    public int lead;// 单排兵力
    public int curLine;// 当前战斗的是第几排兵，从0开始
    public int maxLine;// 总兵力排数
    public int count;// 本排兵剩余数量
    public int killed;// 杀敌数
    public int lost;// 记录单次被攻击损兵数
    public int totalLost;// 将领损兵总数
    public AttrData attrData;// 战斗属性
    public Fighter fighter;
    public int addExp;// 如果是玩家将领，记录本次战斗中获得的经验
    public long ownerId;// 如果是玩家将领，记录所属玩家id
    public String nick;
    public boolean hasFight;// 记录是否参与过战斗
    public int camp; // 阵营
    public int roleType; // 1玩家,2流寇NPC,3城池守将NPC,4城池NPC
    public int skillId;// 技能id 0表示 无技能
    private boolean useSkill; // 是否使用过技能,true表示使用过
    public int intensifyLv; // 兵种强化等级
    public int effect; // 强化克制比
    public int isBcs;//是否有勋章  闪击奇兵 特技   0否  1是
    public int isIronBas;//是否有勋章  铜墙铁壁 特技  0否 1是
    /**
     * 主将技能列表
     */
    public List<SimpleHeroSkill> skillList = new ArrayList<>();
    /**
     * 武将当前士气
     */
    public int morale;
    /**
     * 本轮士气值上限
     */
    public int maxRoundMorale;
    /**
     * 战斗buff集合 主将的buff列表
     */
    public LinkedList<IFightBuff> buffList = new LinkedList<>();
    /**
     * 副将列表
     */
    public ArrayList<FightAssistantHero> assistantHeroList;
    /**
     * 战斗中的buff与效果
     */
    private FightBuffEffect fightBuffEffect;

    public Force() {
    }

    public Force(AttrData attrData, int armType, int line) {
        this.attrData = attrData;
        this.armType = armType;
        this.curLine = 0;
        this.maxLine = line;
        this.lead = attrData.lead / line;
        this.count = attrData.lead / line;
        this.maxHp = count * maxLine;
        this.hp = maxHp;
    }

    /**
     * 玩家将领对应的Force初始化
     *
     * @param attrData
     * @param armType
     * @param totalCount 将领总兵力
     * @param lead       单排兵力
     * @param id
     */
    public Force(AttrData attrData, int armType, int totalCount, int lead, int id, long roleId) {
        this.attrData = attrData;
        this.armType = armType;
        this.maxHp = totalCount;
        this.lead = lead;
        this.curLine = 0;
        this.hp = maxHp;
        this.id = id;
        this.ownerId = roleId;

        /**
         * 将领兵力排数计算规则：<br>
         * 当兵力不止一排时，首先满足后排兵力满，首排兵力可以不满
         */
        this.maxLine = (int) Math.ceil(totalCount * 1.0 / lead);// 计算兵力排数，向上取整
        this.count = totalCount % lead;// 计算最前排的兵力
        if (this.count == 0) {
            this.count = lead;
        }
        LogUtil.fight("Force=" + toString());
    }

    /**
     * 新的计算公式 有单排兵改成总兵(NPC对应的Force初始化)
     *
     * @param attrData
     * @param armType
     * @param totalCount 剩余总兵力
     * @param lead
     * @param id
     * @param sCount
     * @param sLine
     */
    public Force(AttrData attrData, int armType, int totalCount, int lead, int id, int sCount, int sLine) {
        int sLead = (int) Math.ceil(sCount * 1.0 / sLine);// 计算单排兵力, 当兵力不能被整除时，向上取整
        this.attrData = attrData;
        this.armType = armType;
        this.maxHp = totalCount;
        this.lead = sLead;
        this.curLine = 0;
        this.hp = maxHp;
        this.id = id;

        /**
         * 将领兵力排数计算规则：<br>
         * 当兵力不止一排时，首先满足后排兵力满，首排兵力可以不满
         */
        this.maxLine = (int) Math.ceil(totalCount * 1.0 / sLead);// 计算兵力排数，向上取整
        this.count = totalCount % sLead;// 计算最前排的兵力
        if (this.count == 0) {
            this.count = sLead;
        }
        LogUtil.fight(
                "Force : 当前兵力=" + totalCount + ",一排多少兵=" + sLead + ",总共多少排=" + maxLine + ",sCount=" + sCount + ",sLine="
                        + sLine);
    }

    /**
     * NPC对应的Force初始化
     *
     * @param attrData
     * @param armType
     * @param line     兵力排数
     * @param id
     */
    public Force(AttrData attrData, int armType, int line, int id) {
        this(attrData, armType, line);
        this.id = id;
    }

    public FightBuffEffect getFightBuffEffect() {
        return fightBuffEffect;
    }

    public void setFightBuffEffect(FightBuffEffect fightBuffEffect) {
        this.fightBuffEffect = fightBuffEffect;
    }

    public boolean isBuffListEmpty() {
        if (!CheckNull.isEmpty(this.buffList)) return false;
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            for (FightAssistantHero assistantHero : this.assistantHeroList) {
                if (!CheckNull.isEmpty(assistantHero.getBuffList()))
                    return false;
            }
        }

        return true;
    }

    /**
     * 释放buff效果
     *
     * @param contextHolder
     * @param timing
     * @param params
     */
    public void releaseBuffEffect(FightContextHolder contextHolder, int timing, Object... params) {
        if (!CheckNull.isEmpty(this.buffList)) {
            buffList.forEach(fightBuff -> {
                fightBuff.releaseEffect(contextHolder, timing, params);
            });
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            for (FightAssistantHero assistantHero : this.assistantHeroList) {
                if (!CheckNull.isEmpty(assistantHero.getBuffList())) {
                    assistantHero.getBuffList().forEach(fightBuff -> {
                        fightBuff.releaseEffect(contextHolder, timing, params);
                    });
                }
            }
        }
    }

    public FightBuffEffect getFightEffectMap(int heroId) {
        if (heroId == this.id) {
            if (this.fightBuffEffect == null)
                this.fightBuffEffect = new FightBuffEffect(this, id);
            return this.fightBuffEffect;
        }
        if (CheckNull.isEmpty(this.assistantHeroList)) return null;
        return this.assistantHeroList.stream().filter(ass ->
                ass.getHeroId() == heroId).map(ass -> ass.getFightBuffEffect()).findFirst().orElse(null);
    }

    public List<SimpleHeroSkill> getSkillList(int heroId) {
        if (heroId == this.id) return this.skillList;
        if (CheckNull.isEmpty(this.assistantHeroList)) return null;
        return this.assistantHeroList.stream().filter(ass ->
                ass.getHeroId() == heroId).map(ass -> ass.getSkillList()).findFirst().orElse(null);
    }

    public int armyTypeLv(int heroId) {
        if (this.id == heroId)
            return this.intensifyLv;
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass ->
                    ass.getHeroId() == heroId).map(ass -> ass.getIntensifyLv()).findFirst().orElse(0);
        }

        return 0;
    }

    public AttrData attrData(int heroId) {
        if (this.id == heroId)
            return this.attrData;
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass ->
                    ass.getHeroId() == heroId).map(ass -> ass.getAttrData()).findFirst().orElse(null);
        }

        return null;
    }

    /**
     * 兵力大于0
     *
     * @return 是否存活
     */
    public boolean alive() {
        return hp > 0 && this.morale > 0;
    }

    /**
     * 计算伤害能造成的最大真是损兵量，当伤害高于当前排兵力时，只会扣完本排兵力，不会影响后排兵力
     *
     * @param hurt
     * @return
     * @see FightLogic#(Force, int) 如果需要考虑FightBuff
     */
    public int hurt(int hurt, Force force, int battleType, float crit) {
        //天赋优化 战斗buff
        //攻击方的伤害加成与防守方伤害减免
//        hurt = FightLogic.seasonTalentBuff(force, this, hurt, battleType);
//        // 计算保底伤害
//        if (battleType != Integer.MIN_VALUE)
//            hurt = FightCalc.calRoundGuaranteedDamage(force, this, hurt, battleType, crit);
//
//        if (count <= hurt) {
//            lost = count;
//        } else {
//            lost = hurt;
//        }

        return lost;
    }

    public LinkedList<IFightBuff> buffList(int heroId) {
        if (heroId == this.id)
            return buffList;
        if (!CheckNull.isEmpty(assistantHeroList)) {
            FightAssistantHero assistantHero = assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).findFirst().orElse(null);
            if (Objects.nonNull(assistantHero))
                return assistantHero.getBuffList();
        }

        return null;
    }

    /**
     * 执行扣兵逻辑
     *
     * @return 是否死亡兵排数
     */
    public boolean subHp(Force force) {
        boolean deadLine = false;

        LogUtil.fight(String.format("进攻方角色id: %d, 防守方角色id: %d, 防守方当前兵排剩余血量: %d, 当前兵排: %d, 最大兵排: %d, <<<<<<战斗最终伤害>>>>>>: %d",
                force == null ? 0 : force.ownerId, this.ownerId, count, curLine + 1, maxLine, lost));

        if (count <= lost) {
            curLine++;
            if (curLine >= maxLine) {
                hp = 0;
                count = 0;
            } else {
                hp -= count;
                count = lead;
                deadLine = true;
            }
        } else {
            count -= lost;
            hp -= lost;
        }
        totalLost += lost;
        lost = 0;
        return deadLine;
    }

    /**
     * 使用jineng
     */
    public void useSkill() {
        this.useSkill = true;
    }

    /**
     * 在真实扣兵计算前，获取损兵后的死亡排数
     *
     * @return
     */
    public int getDeadLine() {
        if (count <= lost) {
            return curLine + 1;
        }
        return curLine;
    }

    public int calcAttack(int heroId) {
        if (this.id == heroId) {
            return attrData.attack;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().attack).findFirst().orElse(null);
        }

        return 0;
    }

    public int calcDefend(int heroId) {
        if (this.id == heroId) {
            return attrData.defend;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().defend).findFirst().orElse(null);
        }

        return 0;
    }

    public int calcCritical(int heroId) {
        if (this.id == heroId) {
            return attrData.critical;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().critical).findFirst().orElse(null);
        }

        return 0;
    }

    public int calCriticalChance(int heroId) {
        if (this.id == heroId) {
            return attrData.criticalChance;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().criticalChance).findFirst().orElse(null);
        }

        return 0;
    }

    public int calcCriticalDef(int heroId) {
        if (this.id == heroId) {
            return attrData.criticalDef;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().criticalDef).findFirst().orElse(null);
        }

        return 0;
    }

    public int calcAtkExt(int heroId) {
        if (this.id == heroId) {
            return attrData.atkExt;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().atkExt).findFirst().orElse(null);
        }

        return 0;
    }

    public int calcDefExt(int heroId) {
        if (this.id == heroId) {
            return attrData.defExt;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().defExt).findFirst().orElse(null);
        }

        return 0;
    }

    public int calcAtkTown(int heroId) {
        if (this.id == heroId) {
            return attrData.atkTown;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().atkTown).findFirst().orElse(null);
        }

        return 0;
    }

    public int calcDefTown(int heroId) {
        if (this.id == heroId) {
            return attrData.defTown;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().defTown).findFirst().orElse(null);
        }

        return 0;
    }

    public int calSpeed(int heroId) {
        if (this.id == heroId) {
            return attrData.speed;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().speed).findFirst().orElse(null);
        }

        return 0;
    }

    public int calLowerEnergyCharging(int heroId) {
        if (this.id == heroId) {
            return attrData.lowerLimitCharging;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().lowerLimitCharging).findFirst().orElse(null);
        }

        return 0;
    }

    public int calUpperEnergyCharging(int heroId) {
        if (this.id == heroId) {
            return attrData.upperChargingLimit;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getAttrData().upperChargingLimit).findFirst().orElse(null);
        }

        return 0;
    }


    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getIntensifyLv() {
        return intensifyLv;
    }

    public void setIntensifyLv(int intensifyLv) {
        this.intensifyLv = intensifyLv;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public void setIsBcs(int isBcs) {
        this.isBcs = isBcs;
    }

    public void setIsIronBas(int isIronBas) {
        this.isIronBas = isIronBas;
    }

    public AttrData getAttrData() {
        return attrData;
    }

    public boolean canReleaseSkill(int heroId) {
        // 检查武将是否有沉默效果
        FightManager fightManager = DataResource.ac.getBean(FightManager.class);
        IFightEffect fightEffect = fightManager.getSkillEffect(FightConstant.EffectLogicId.SILENCE);
        if (CheckNull.isNull(fightEffect)) return true;
        return !(boolean) fightEffect.effectCalculateValue(getFightEffectMap(heroId), FightConstant.EffectLogicId.SILENCE);
    }

    public int armyType(int heroId) {
        if (this.id == heroId)
            return this.armType;
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            return this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).map(ass -> ass.getArmType()).findFirst().orElse(-1);
        }
        return -1;
    }

    @Override
    public String toString() {
        return "Force{" +
                "id=" + id +
                ", hp=" + hp +
                ", maxHp=" + maxHp +
                ", armType=" + armType +
                ", lead=" + lead +
                ", curLine=" + curLine +
                ", maxLine=" + maxLine +
                ", count=" + count +
                ", killed=" + killed +
                ", lost=" + lost +
                ", totalLost=" + totalLost +
                ", attrData=" + attrData +
                ", fighter=" + fighter +
                ", addExp=" + addExp +
                ", ownerId=" + ownerId +
                ", nick='" + nick + '\'' +
                ", hasFight=" + hasFight +
                ", camp=" + camp +
                ", roleType=" + roleType +
                ", skillId=" + skillId +
                ", useSkill=" + useSkill +
                ", intensifyLv=" + intensifyLv +
                ", effect=" + effect +
                ", isBcs=" + isBcs +
                ", isIronBas=" + isIronBas +
                ", skillList=" + skillList +
                ", morale=" + morale +
                ", maxRoundMorale=" + maxRoundMorale +
                ", buffList=" + buffList +
                ", assistantHeroList=" + assistantHeroList +
                '}';
    }

    public String toBattleString() {
        return "Force{" +
                "id=" + id +
                ", hp=" + hp +
                ", maxHp=" + maxHp +
                ", armType=" + armType +
                ", lead=" + lead +
                ", curLine=" + curLine +
                ", maxLine=" + maxLine +
                ", count=" + count +
                ", killed=" + killed +
                ", lost=" + lost +
                ", totalLost=" + totalLost +
                ", addExp=" + addExp +
                ", ownerId=" + ownerId +
                ", hasFight=" + hasFight +
                ", camp=" + camp +
                ", roleType=" + roleType +
                ", skillId=" + skillId +
                ", useSkill=" + useSkill +
                ", intensifyLv=" + intensifyLv +
                ", effect=" + effect +
                ", isBcs=" + isBcs +
                ", isIronBas=" + isIronBas +
                ", treasureWare =" + 1 +
                '}';
    }
}
