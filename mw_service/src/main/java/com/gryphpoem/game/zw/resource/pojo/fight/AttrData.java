package com.gryphpoem.game.zw.resource.pojo.fight;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    public int crit;//暴击伤害 倍
    public int critChance;//暴击概率
    public int critDef;//免爆值
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

    public AttrData(CommonPb.SerAttrData attrData) {
        this.attack = attrData.getAttack();
        this.defend = attrData.getDefend();
        this.lead = attrData.getLead();
        this.line = attrData.getLead();
        this.crit = attrData.getCrit();
        this.critChance = attrData.getCritChance();
        this.critDef = attrData.getCritDef();
        this.atkTown = attrData.getAtkTown();
        this.defTown = attrData.getDefTown();
        this.atkExt = attrData.getAtkExt();
        this.defExt = attrData.getDefExt();
        this.evade = attrData.getEvade();

    }

    public AttrData(int attack, int defend, int lead, int line, int crit, int critDef) {
        this.attack = attack;
        this.defend = defend;
        this.lead = lead;
        this.line = line;
        this.crit = crit;
        this.critDef = critDef;
    }

    public AttrData(Map<Integer, Integer> attrMap) {
        for (Entry<Integer, Integer> entry : attrMap.entrySet()) {
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
        for (Entry<Integer, Integer> entry : attrMap.entrySet()) {
            int val = entry.getValue();
            int key = entry.getKey();
            if (Constant.AttrId.LEAD != key) {
                val = (int) Math.ceil(val * coef / Constant.HUNDRED);
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
        if (ObjectUtils.isEmpty(attrList)) {
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
        for (Entry<Integer, Integer> entry : attrMap.entrySet()) {
            addValue(entry.getKey(), entry.getValue());
        }
    }

    public void addValue(int attrId, int value) {
        switch (attrId) {
            case Constant.AttrId.ATTACK:
                this.attack += value;
                break;
            case Constant.AttrId.DEFEND:
                this.defend += value;
                break;
            case Constant.AttrId.LEAD:
                this.lead += value;
                break;
            case Constant.AttrId.LINE:
                this.line += value;
                break;
            case Constant.AttrId.CRIT:
                this.crit += value;
                break;
            case Constant.AttrId.CRIT_CHANCE:
                this.critChance += value;
                break;
            case Constant.AttrId.CRITDEF:
                this.critDef += value;
                break;
            case Constant.AttrId.ATTACK_EXT:
                this.atkExt += value;
                break;
            case Constant.AttrId.DEFEND_EXT:
                this.defExt += value;
                break;
            case Constant.AttrId.ATTACK_TOWN:
                this.atkTown += value;
                break;
            case Constant.AttrId.DEFEND_TOWN:
                this.defTown += value;
                break;
            case Constant.AttrId.EVADE:
                this.evade += value;
                break;
            case Constant.AttrId.DMG_INC:
                this.dmgInc += value;
                break;
            case Constant.AttrId.DMG_DEC:
                this.dmgDec += value;
                break;
            case Constant.AttrId.MORE_INFANTRY_DAMAGE:
                this.moreInfantryDamage += value;
                break;
            case Constant.AttrId.MORE_ARCHER_DAMAGE:
                this.moreArcherDamage += value;
                break;
            case Constant.AttrId.MORE_CAVALRY_DAMAGE:
                this.moreCavalryDamage += value;
                break;
            case Constant.AttrId.ATK_MUT:
                this.attack += (int) (this.attack * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.DEF_MUT:
                this.defend += (int) (this.defend * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.LEAD_MUT:
                this.lead += (int) (this.lead * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.LESS_INFANTRY_MUT:
                this.lessInfantryMut += value / Constant.TEN_THROUSAND;
                break;
            case Constant.AttrId.LESS_CAVALRY_MUT:
                this.lessCavalryMut += value  / Constant.TEN_THROUSAND;
                break;
            case Constant.AttrId.LESS_ARCHER_MUT:
                this.lessArcherMut += value  / Constant.TEN_THROUSAND;
                break;
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
            case Constant.AttrId.ATTACK:
            case Constant.AttrId.ATK_MUT:
                this.attack += (int) (this.attack * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.DEFEND:
            case Constant.AttrId.DEF_MUT:
                this.defend += (int) (this.defend * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.LEAD:
            case Constant.AttrId.LEAD_MUT:
                this.lead += (int) (this.lead * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.LINE:
                this.line += (int) (this.line * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.CRIT:
                this.crit += (int) (this.crit * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.CRIT_CHANCE:
                this.critChance += (int) (this.critChance * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.CRITDEF:
                this.critDef += (int) (this.critDef * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.ATTACK_EXT:
                this.atkExt += (int) (this.atkExt * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.DEFEND_EXT:
                this.defExt += (int) (this.defExt * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.ATTACK_TOWN:
                this.atkTown += (int) (this.atkTown * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.DEFEND_TOWN:
                this.defTown += (int) (this.defTown * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.EVADE:
                this.evade += (int) (this.evade * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.DMG_INC:
                this.dmgInc += (int) (this.dmgInc * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.DMG_DEC:
                this.dmgDec += (int) (this.dmgDec * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.MORE_INFANTRY_DAMAGE:
                this.moreInfantryDamage += (int) (this.moreInfantryDamage * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.MORE_ARCHER_DAMAGE:
                this.moreArcherDamage += (int) (this.moreArcherDamage * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.MORE_CAVALRY_DAMAGE:
                this.moreCavalryDamage += (int) (this.moreCavalryDamage * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.LESS_INFANTRY_MUT:
                this.lessInfantryMut += (int) (this.lessInfantryMut * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.LESS_CAVALRY_MUT:
                this.lessCavalryMut += (int) (this.lessCavalryMut * value / Constant.TEN_THROUSAND);
                break;
            case Constant.AttrId.LESS_ARCHER_MUT:
                this.lessArcherMut += (int) (this.lessArcherMut * value / Constant.TEN_THROUSAND);
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
                case Constant.AttrId.ATTACK:
                    attrArray[HeroConstant.ATTR_ATTACK] += attr.getV2();
                    break;
                case Constant.AttrId.DEFEND:
                    attrArray[HeroConstant.ATTR_DEFEND] += attr.getV2();
                    break;
                case Constant.AttrId.LEAD:
                    attrArray[HeroConstant.ATTR_LEAD] += attr.getV2();
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public String toString() {
        return "AttrData{" + "attack=" + attack + ", defend=" + defend + ", lead=" + lead + ", line=" + line + ", crit="
                + crit + ", critChance=" + critChance + ", critDef=" + critDef + ", atkTown=" + atkTown + ", defTown="
                + defTown + ", atkExt=" + atkExt + ", defExt=" + defExt + ", evade=" + evade + '}';
    }

    public CommonPb.SerAttrData ser() {
        CommonPb.SerAttrData.Builder builder = CommonPb.SerAttrData.newBuilder();
        builder.setAttack(this.attack);
        builder.setDefend(this.defend);
        builder.setLead(this.lead);
        builder.setLine(this.line);
        builder.setCrit(this.crit);
        builder.setCritChance(this.critChance);
        builder.setCritDef(this.critDef);
        builder.setAtkTown(this.atkTown);
        builder.setDefTown(this.defTown);
        builder.setAtkExt(this.atkExt);
        builder.setDefExt(this.defExt);
        builder.setEvade(this.evade);
        return builder.build();
    }
}
