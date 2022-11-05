package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName AttrData.java
 * @Description 战斗属性
 * @date 创建时间：2017年3月31日 下午5:18:21
 */
public class AttrData {
    public int attack;//攻击
    public int defend;//防御
    public int lead;//兵力
    public int line;//兵排
    public int speed;//速度
    public int critical;//暴击伤害 倍
    public int criticalChance;//暴击概率
    public int criticalDef;//免爆值
    public int atkTown;// 攻坚
    public int defTown;// 据守
    public int atkExt;// 穿甲
    public int defExt;// 防护
    public int evade;//闪避概率
    public int dmgInc;//增伤 damage increase
    public int dmgDec;//减伤 damage decrease
    public int moreInfantryDamage;//对步兵增伤
    public int moreCavalryDamage;//对骑兵增伤
    public int moreArcherDamage;//对弓兵增伤
    public double lessInfantryMut;//对步兵减伤
    public double lessCavalryMut;//对骑兵减伤
    public double lessArcherMut;//对弓兵减伤
    public int moreInfantryAttack;//对步兵攻击提升
    public int moreInfantryAttackExt;//对步兵破甲提升
    public int moreCavalryAttack;//对骑兵攻击提升
    public int moreCavalryAttackExt;//对骑兵破甲提升
    public int moreArcherAttack;//对弓兵攻击提升
    public int moreArcherAttackExt;//对弓兵破甲提升

    public AttrData(CommonPb.SerAttrData attrData) {
        this.attack = attrData.getAttack();
        this.defend = attrData.getDefend();
        this.lead = attrData.getLead();
        this.line = attrData.getLead();
        this.critical = attrData.getCrit();
        this.criticalChance = attrData.getCritChance();
        this.criticalDef = attrData.getCritDef();
        this.atkTown = attrData.getAtkTown();
        this.defTown = attrData.getDefTown();
        this.atkExt = attrData.getAtkExt();
        this.defExt = attrData.getDefExt();
        this.evade = attrData.getEvade();
    }

    public AttrData(int attack, int defend, int lead, int line, int critical, int criticalDef) {
        this.attack = attack;
        this.defend = defend;
        this.lead = lead;
        this.line = line;
        this.critical = critical;
        this.criticalDef = criticalDef;
    }

    public AttrData(Map<Integer, Integer> attrMap) {
        for (Map.Entry<Integer, Integer> entry : attrMap.entrySet()) {
            addValue(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 阵型难度系数
     *
     * @param attrMap
     * @param coef
     */
    public AttrData(Map<Integer, Integer> attrMap, int coef) {
        for (Map.Entry<Integer, Integer> entry : attrMap.entrySet()) {
            int val = entry.getValue();
            int key = entry.getKey();
            if (FightCommonConstant.AttrId.LEAD != key) {
                val = (int) Math.ceil(val * coef / FightConstant.HUNDRED);
            }
            addValue(key, val);
        }
    }

    /**
     * 添加属性
     *
     * @param attrList
     */
    public void addAttrValue(List<CommonPb.TwoInt> attrList) {
        if (CheckNull.isEmpty(attrList)) {
            return;
        }

        attrList.forEach(twoInt -> {
            addValue(twoInt.getV1(), twoInt.getV2());
        });
    }

    public void addValue(Map<Integer, Integer> attrMap) {
        if (attrMap == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> entry : attrMap.entrySet()) {
            addValue(entry.getKey(), entry.getValue());
        }
    }

    public void addValue(int attrId, int value) {
        switch (attrId) {
            case FightCommonConstant.AttrId.ATTACK:
                this.attack += value;
                break;
            case FightCommonConstant.AttrId.DEFEND:
                this.defend += value;
                break;
            case FightCommonConstant.AttrId.LEAD:
                this.lead += value;
                break;
            case FightCommonConstant.AttrId.LINE:
                this.line += value;
                break;
            case FightCommonConstant.AttrId.CRIT:
                this.critical += value;
                break;
            case FightCommonConstant.AttrId.CRIT_CHANCE:
                this.criticalChance += value;
                break;
            case FightCommonConstant.AttrId.CRITDEF:
                this.criticalDef += value;
                break;
            case FightCommonConstant.AttrId.ATTACK_EXT:
                this.atkExt += value;
                break;
            case FightCommonConstant.AttrId.DEFEND_EXT:
                this.defExt += value;
                break;
            case FightCommonConstant.AttrId.ATTACK_TOWN:
                this.atkTown += value;
                break;
            case FightCommonConstant.AttrId.DEFEND_TOWN:
                this.defTown += value;
                break;
            case FightCommonConstant.AttrId.EVADE:
                this.evade += value;
                break;
            case FightCommonConstant.AttrId.DMG_INC:
                this.dmgInc += value;
                break;
            case FightCommonConstant.AttrId.DMG_DEC:
                this.dmgDec += value;
                break;
            case FightCommonConstant.AttrId.MORE_INFANTRY_DAMAGE:
                this.moreInfantryDamage += value;
                break;
            case FightCommonConstant.AttrId.MORE_ARCHER_DAMAGE:
                this.moreArcherDamage += value;
                break;
            case FightCommonConstant.AttrId.MORE_CAVALRY_DAMAGE:
                this.moreCavalryDamage += value;
                break;
            case FightCommonConstant.AttrId.ATK_MUT:
                this.attack += (int) (this.attack * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.DEF_MUT:
                this.defend += (int) (this.defend * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.LEAD_MUT:
                this.lead += (int) (this.lead * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.LESS_INFANTRY_MUT:
                this.lessInfantryMut += value / FightConstant.TEN_THOUSAND;
                break;
            case FightCommonConstant.AttrId.LESS_CAVALRY_MUT:
                this.lessCavalryMut += value / FightConstant.TEN_THOUSAND;
                break;
            case FightCommonConstant.AttrId.LESS_ARCHER_MUT:
                this.lessArcherMut += value / FightConstant.TEN_THOUSAND;
                break;
            case FightCommonConstant.AttrId.MORE_INFANTRY_ATTACK:
                this.moreInfantryAttack += value;
                break;
            case FightCommonConstant.AttrId.MORE_INFANTRY_ATTACK_EXT:
                this.moreInfantryAttackExt += value;
                break;
            case FightCommonConstant.AttrId.MORE_CAVALRY_ATTACK:
                this.moreCavalryAttack += value;
                break;
            case FightCommonConstant.AttrId.MORE_CAVALRY_ATTACK_EXT:
                this.moreCavalryAttackExt += value;
                break;
            case FightCommonConstant.AttrId.MORE_ARCHER_ATTACK:
                this.moreArcherAttack += value;
                break;
            case FightCommonConstant.AttrId.MORE_ARCHER_ATTACK_EXT:
                this.moreArcherAttackExt += value;
                break;
            case FightCommonConstant.AttrId.SPEED:
                this.speed += value;
            default:
                break;
        }
    }

    /**
     * 属性万分比
     *
     * @param attrId
     * @param value
     */
    public void addRatioValue(int attrId, int value) {
        switch (attrId) {
            case FightCommonConstant.AttrId.ATTACK:
            case FightCommonConstant.AttrId.ATK_MUT:
                this.attack += (int) (this.attack * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.DEFEND:
            case FightCommonConstant.AttrId.DEF_MUT:
                this.defend += (int) (this.defend * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.LEAD:
            case FightCommonConstant.AttrId.LEAD_MUT:
                this.lead += (int) (this.lead * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.LINE:
                this.line += (int) (this.line * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.CRIT:
                this.critical += (int) (this.critical * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.CRIT_CHANCE:
                this.criticalChance += (int) (this.criticalChance * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.CRITDEF:
                this.criticalDef += (int) (this.criticalDef * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.ATTACK_EXT:
                this.atkExt += (int) (this.atkExt * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.DEFEND_EXT:
                this.defExt += (int) (this.defExt * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.ATTACK_TOWN:
                this.atkTown += (int) (this.atkTown * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.DEFEND_TOWN:
                this.defTown += (int) (this.defTown * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.EVADE:
                this.evade += (int) (this.evade * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.DMG_INC:
                this.dmgInc += (int) (this.dmgInc * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.DMG_DEC:
                this.dmgDec += (int) (this.dmgDec * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.MORE_INFANTRY_DAMAGE:
                this.moreInfantryDamage += (int) (this.moreInfantryDamage * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.MORE_ARCHER_DAMAGE:
                this.moreArcherDamage += (int) (this.moreArcherDamage * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.MORE_CAVALRY_DAMAGE:
                this.moreCavalryDamage += (int) (this.moreCavalryDamage * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.LESS_INFANTRY_MUT:
                this.lessInfantryMut += (int) (this.lessInfantryMut * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.LESS_CAVALRY_MUT:
                this.lessCavalryMut += (int) (this.lessCavalryMut * value / FightConstant.TEN_THOUSAND);
                break;
            case FightCommonConstant.AttrId.LESS_ARCHER_MUT:
                this.lessArcherMut += (int) (this.lessArcherMut * value / FightConstant.TEN_THOUSAND);
                break;
            default:
                break;
        }
    }

    public static void addValue(int[] attrArray, List<CommonPb.TwoInt> attrList) {
        if (ObjectUtils.isEmpty(attrArray) || ObjectUtils.isEmpty(attrList)) {
            return;
        }

        attrList.forEach(attr -> {
            switch (attr.getV1()) {
                case FightCommonConstant.AttrId.ATTACK:
                    attrArray[FightConstant.ATTR_ATTACK] += attr.getV2();
                    break;
                case FightCommonConstant.AttrId.DEFEND:
                    attrArray[FightConstant.ATTR_DEFEND] += attr.getV2();
                    break;
                case FightCommonConstant.AttrId.LEAD:
                    attrArray[FightConstant.ATTR_LEAD] += attr.getV2();
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public String toString() {
        return "AttrData{" + "attack=" + attack + ", defend=" + defend + ", lead=" + lead + ", line=" + line + ", critical="
                + critical + ", criticalChance=" + criticalChance + ", criticalDef=" + criticalDef + ", atkTown=" + atkTown + ", defTown="
                + defTown + ", atkExt=" + atkExt + ", defExt=" + defExt + ", evade=" + evade + '}';
    }

    public CommonPb.SerAttrData ser() {
        CommonPb.SerAttrData.Builder builder = CommonPb.SerAttrData.newBuilder();
        builder.setAttack(this.attack);
        builder.setDefend(this.defend);
        builder.setLead(this.lead);
        builder.setLine(this.line);
        builder.setCrit(this.critical);
        builder.setCritChance(this.criticalChance);
        builder.setCritDef(this.criticalDef);
        builder.setAtkTown(this.atkTown);
        builder.setDefTown(this.defTown);
        builder.setAtkExt(this.atkExt);
        builder.setDefExt(this.defExt);
        builder.setEvade(this.evade);
        return builder.build();
    }
}
