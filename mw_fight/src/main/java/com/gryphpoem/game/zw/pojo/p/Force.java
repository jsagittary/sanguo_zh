package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.listener.impl.SessionListener;
import com.gryphpoem.game.zw.manager.FightManager;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName Force.java
 * @Description 记录战斗中单个将领或npc的战斗信息
 * @date 创建时间：2017年3月31日 下午5:14:08
 */
public class Force implements SessionListener {
    public int id;//将领id
    public int hp;// 当前血量，兵力
    public int maxHp;// 总血量，兵力
    public int armType;// 兵种类型
    public int lead;// 单排兵力
    public int curLine;// 当前战斗的是第几排兵，从1开始
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
    public int intensifyLv; // 兵种强化等级
    public int effect; // 强化克制比
    public int isBcs;//是否有勋章  闪击奇兵 特技   0否  1是
    public int isIronBas;//是否有勋章  铜墙铁壁 特技  0否 1是
    // 对敌方单个武将普攻次数
    public int sessionAttackCount;
    // 对敌方单个武将普攻伤害
    public int sessionAttackDamage;
    // 回合损兵
    public int roundLost;

    /**
     * 主将技能列表
     */
    public List<SimpleHeroSkill> skillList = new ArrayList<>();
    /**
     * 战斗buff集合 主将的buff列表
     */
    public LinkedList<IFightBuff> buffList = new LinkedList<>();
    /**
     * 副将列表
     */
    public ArrayList<FightAssistantHero> assistantHeroList = new ArrayList<>(1);
    /**
     * 战斗中的buff与效果
     */
    private FightBuffEffect fightBuffEffect;

    public Force() {
    }

    public Force(AttrData attrData, int armType, int line) {
        this.attrData = attrData;
        this.armType = armType;
        this.curLine = 1;
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
        this.curLine = 1;
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
        this.assistantHeroList = new ArrayList<>();
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
        this.curLine = 1;
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
                        ass.getHeroId() == heroId).filter(ass -> !CheckNull.isEmpty(ass.getSkillList())).
                map(ass -> ass.getSkillList()).findFirst().orElse(null);
    }

    public void setSkillList(int heroId, List<SimpleHeroSkill> skillList) {
        if (CheckNull.isEmpty(skillList)) return;
        if (heroId == this.id) {
            this.skillList = skillList;
            return;
        }
        if (CheckNull.isEmpty(this.assistantHeroList)) return;
        FightAssistantHero assistantHero = this.assistantHeroList.stream().filter(ass -> ass.getHeroId() == heroId).
                findFirst().orElse(null);
        if (Objects.nonNull(assistantHero)) {
            assistantHero.setSkillList(skillList);
        }
    }

    public int armyTypeLv(int heroId) {
        return this.intensifyLv;
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
        return hp > 0;
    }

    /**
     * 计算伤害能造成的最大真是损兵量，当伤害高于当前排兵力时，只会扣完本排兵力，不会影响后排兵力
     *
     * @param hurt
     * @return
     * @see FightLogic#(Force, int) 如果需要考虑FightBuff
     */
    public int hurt(int hurt) {
        if (count <= hurt) {
            lost = count;
        } else {
            lost = hurt;
        }

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
    public void subHp(Force force) {
        LogUtil.fight(String.format("进攻方角色id: %d, 防守方角色id: %d, 防守方当前兵排剩余血量: %d, 当前兵排: %d, 最大兵排: %d, <<<<<<战斗最终伤害>>>>>>: %d",
                force == null ? 0 : force.ownerId, this.ownerId, count, curLine, maxLine, lost));

        if (count <= lost) {
            hp -= count;
            count = 0;
        } else {
            count -= lost;
            hp -= lost;
        }

        totalLost += lost;
        lost = 0;
    }

    /**
     * 切换兵排
     *
     * @return
     */
    public boolean switchPlatoon() {
        if (count > 0) {
            return false;
        }

        if (curLine + 1 > maxLine)
            return false;
        count = lead;
        this.curLine++;

        LogUtil.fight(String.format("防守方角色id: %d, 当前兵排: %d, 最大兵排: %d, <<<<<<切换兵排>>>>>>",
                this.ownerId, count, curLine, maxLine));
        return true;
    }

    /**
     * 在真实扣兵计算前，获取损兵后的死亡排数
     *
     * @return
     */
    public int getDeadLine() {
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
        return this.armType;
    }

    public void addAttackDamage(int damage, int heroId) {
        if (heroId == this.id) {
            this.sessionAttackDamage += damage;
            return;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            FightAssistantHero assistantHero = this.assistantHeroList.stream().filter(ass ->
                    Objects.nonNull(ass) && ass.getHeroId() == heroId).findFirst().orElse(null);
            if (Objects.nonNull(assistantHero)) {
                assistantHero.setSessionAttackDamage(assistantHero.getSessionAttackDamage() + damage);
            }
        }
    }

    public int getAttackKilled(int heroId) {
        if (heroId == this.id) {
            return this.sessionAttackDamage;
        }
        if (!CheckNull.isEmpty(this.assistantHeroList)) {
            FightAssistantHero assistantHero = this.assistantHeroList.stream().filter(ass ->
                    Objects.nonNull(ass) && ass.getHeroId() == heroId).findFirst().orElse(null);
            if (Objects.nonNull(assistantHero)) {
                return assistantHero.getSessionAttackDamage();
            }
        }

        return 0;
    }

    public void addRoundLost(int hurt) {
        if (this.roundLost >= this.count)
            return;

        this.roundLost += hurt;
        if (this.roundLost > this.count)
            this.roundLost = this.count;
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
                ", intensifyLv=" + intensifyLv +
                ", effect=" + effect +
                ", isBcs=" + isBcs +
                ", isIronBas=" + isIronBas +
                ", skillList=" + skillList +
                ", buffList=" + buffList +
                ", assistantHeroList=" + (CheckNull.isEmpty(assistantHeroList) ? "" :
                assistantHeroList.stream().map(ass -> ass.getHeroId()).collect(Collectors.toList()).toString()) +
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
                ", intensifyLv=" + intensifyLv +
                ", effect=" + effect +
                ", isBcs=" + isBcs +
                ", isIronBas=" + isIronBas +
                ", treasureWare =" + 1 +
                '}';
    }

    @Override
    public void sessionEnd() {
        this.sessionAttackDamage = 0;
        this.sessionAttackCount = 0;
    }
}
