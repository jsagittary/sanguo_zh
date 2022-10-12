package com.gryphpoem.game.zw.resource.pojo.fight;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBattleDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.PlaneConstant;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePvp;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedalSpecialSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneSkill;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author TanDonghai
 * @ClassName FightCalc.java
 * @Description 战斗数值计算
 * @date 创建时间: 2017年4月1日 下午3:12:07
 */
public class FightCalc {

    /**
     * 计算是否闪避<br>
     * 在 0 - 10000 中随机（因为我们日后的所有的概率效果，最小单位都是 0.01%）<br>
     * 小于等于 500 则 闪避【伤害值为0；发送闪避消息；本次伤害计算结束】 <br>
     * 大于 500 则命中
     *
     * @param force
     * @param target
     * @return
     */
    public static boolean isDodge(Force force, Force target) {
        return RandomHelper.isHitRangeIn10000(Constant.DODGE_PRO + target.attrData.evade);
    }

    /**
     * 判断是否暴击<br>
     * 在 0 - 10000 中随机
     *
     * @param force
     * @param target
     * @param buffVal
     * @return
     */
    public static boolean isCrit(Force force, Force target, int buffVal) {
        int critChance = Constant.CRIT_PRO + MedalConst.getAuraSkillNum(force, target, MedalConst.INCREASE_CRIT_AURA)
                + force.attrData.critChance + buffVal;
        return critChance > 0 && RandomHelper.isHitRangeIn10000(critChance);
    }

    /**
     * 计算暴击倍率<br>
     * 防守方暴击防御小于等于（攻击方）的暴击值，伤害倍率 = 暴击倍率 <br>
     * 防守方暴击防御大于（攻击方）的暴击值，则 伤害倍率 = 1
     *
     * @param force
     * @param target
     * @return
     */
    public static float calcCrit(Force force, Force target) {
        if (force.calcCrit() < target.calcCritDef()) {
            return 1;
        }
        return Constant.CRIT_MULTI;
    }

    /**
     * 兵种克制关系系数
     *
     * @param force
     * @param target
     * @return
     */
    public static double getFinalRestrain(Force force, Force target, int battleType) {
        double restrain;
        int atkHeroLv = 1;          //进攻方 强化等级(默认1级)
        int defHeroLv = 1;          //防守方 强化等级(默认1级)

        // 获取进攻方 强化等级 和  克制值
        if (force.intensifyLv != 0) {
            atkHeroLv = force.intensifyLv;
        }

        // 获取防守方 强化等级 和 克制值
        if (target.intensifyLv != 0) {
            defHeroLv = target.intensifyLv;
        }

        // 若 (兵种阶级 - 对方兵种阶级) < 0, 则为0
        int lvDiff = atkHeroLv - defHeroLv;
        lvDiff = lvDiff < 0 ? 0 : lvDiff;

        /**
         * 克制时	(兵种阶级-对方兵种阶级) * K9 + [基础K8 + (兵种阶级 * K10)]
         * 被克制时	(兵种阶级-对方兵种阶级) * K9 - [基础K8 + (对方兵种阶级 * K10)]
         * 不可制	(兵种阶级-对方兵种阶级) * K9
         */

        restrain = lvDiff * WorldConstant.K9;
        if (haveArmyRestraint(force.armType, target.armType)) {
            restrain = restrain + (WorldConstant.K8 + (atkHeroLv * WorldConstant.K10));
            //赛季天赋兵种克制
            if (FightLogic.checkPvp(force, target)) {
                Player forcePlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
                if (!CheckNull.isNull(forcePlayer)) {
                    double seasonTalentRestrain = (DataResource.getBean(SeasonTalentService.class).
                            getSeasonTalentEffectValue(forcePlayer, SeasonConst.TALENT_EFFECT_602) / Constant.TEN_THROUSAND);
                    LogUtil.fight("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ", " +
                            "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "赛季天赋-以长攻短-加成比例: ", seasonTalentRestrain);
                    restrain += seasonTalentRestrain;
                }
            }
        } else if (haveArmyRestraint(target.armType, force.armType)) {
            // 兵种克制减伤属性加成
            double lessHurtFromArmyRestraint = getLessHurtFromArmyRestraint(force, target);
            restrain = restrain - (WorldConstant.K8 + (defHeroLv * WorldConstant.K10)) - lessHurtFromArmyRestraint;
        }

        return restrain;
    }

    /**
     * 根据兵种克制关系，获取减伤系数
     *
     * @param force
     * @param target
     * @return
     */
    private static double getLessHurtFromArmyRestraint(Force force, Force target) {
        double lessHurt = 0.00;
        if (target.armType == Constant.ArmyType.INFANTRY_ARMY_TYPE && force.armType == Constant.ArmyType.CAVALRY_ARMY_TYPE) {
            lessHurt = target.attrData.lessInfantryMut;
        }

        if (target.armType == Constant.ArmyType.CAVALRY_ARMY_TYPE && force.armType == Constant.ArmyType.ARCHER_ARMY_TYPE) {
            lessHurt = target.attrData.lessCavalryMut;
        }

        if (target.armType == Constant.ArmyType.ARCHER_ARMY_TYPE && force.armType == Constant.ArmyType.INFANTRY_ARMY_TYPE) {
            lessHurt = target.attrData.lessArcherMut;
        }

        return lessHurt;
    }

    /**
     * 兵种之间是否存在克制关系
     * 步克弓，弓克骑，骑克步
     *
     * @param atkArm
     * @param defArm
     * @return
     */
    public static boolean haveArmyRestraint(int atkArm, int defArm) {
        boolean isRestraint = false;
        switch (atkArm) {
            case ArmyConstant.ARM1:
                if (defArm == ArmyConstant.ARM3) {
                    isRestraint = true;
                }
                break;
            case ArmyConstant.ARM2:
                if (defArm == ArmyConstant.ARM1) {
                    isRestraint = true;
                }
                break;
            case ArmyConstant.ARM3:
                if (defArm == ArmyConstant.ARM2) {
                    isRestraint = true;
                }
                break;
        }
        return isRestraint;
    }

    /**
     * 获取最终的攻击力
     *
     * @param force
     * @param target
     * @param battleType
     * @return
     */
    private static double getFinalAtk(Force force, Force target, int battleType) {
        int calcAttack = force.calcAttack();
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        int heroId = force.id;
        if (force.ownerId > 0 && heroId > 0) {
            Player player = playerDataManager.getPlayer(force.ownerId);
            if (player != null) {
                int specialSkill = medalDataManager.getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_1);
                int normalMedalSkill = medalDataManager.getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_0);
                if (normalMedalSkill == MedalConst.BLITZ_CURIOUS_SOLDIER) {
                    StaticMedalSpecialSkill sSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(normalMedalSkill);
                    calcAttack = (calcAttack + sSpecialSkill.getSkillEffect().get(0));
                }
                if (canCalcAtAndDt(battleType, specialSkill)) {
                    calcAttack = (calcAttack + force.calcAtkTown());
                    if (specialSkill == MedalConst.BLITZ_CURIOUS_SOLDIER) {
                        calcAttack = calcAttack + 1000;
                    }
                }
            }
        }
        return calcAttack;
    }

    /**
     * 获取最终的防御力
     *
     * @param force
     * @param target
     * @param battleType
     * @return
     */
    private static double getFinalDef(Force force, Force target, int battleType) {
        int calcDefend = target.calcDefend();
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        int heroId = target.id;
        if (target.ownerId > 0 && heroId > 0) {
            Player player = playerDataManager.getPlayer(target.ownerId);
            if (player != null) {
                int specialSkill = medalDataManager.getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_1);
                int normalMedalSkill = medalDataManager.getHeroSpecialSkill(player, heroId, MedalConst.HERO_MEDAL_INDEX_0);
                if (normalMedalSkill == MedalConst.IRON_BASTIONS) {
                    StaticMedalSpecialSkill sSpecialSkill = StaticMedalDataMgr.getSpecialSkillById(normalMedalSkill);
                    calcDefend = (calcDefend + sSpecialSkill.getSkillEffect().get(0));
                }
                if (canCalcAtAndDt(battleType, specialSkill)) {
                    calcDefend = (calcDefend + target.calcDefTown());
                    if (specialSkill == MedalConst.IRON_BASTIONS) {
                        calcDefend = (calcDefend + 1000);
                    }
                }
            }
        }
        return calcDefend;
    }

    /**
     * 是否计算攻坚和据守
     *
     * @param battleType
     * @param specialSkill
     * @return
     */
    private static boolean canCalcAtAndDt(int battleType, int specialSkill) {
        return (specialSkill == MedalConst.INTENSIFY_IRON_BASTIONS || specialSkill == MedalConst.INTENSIFY_BLITZ_CURIOUS_SOLDIER) || (WorldConstant
                .calcAttackOrDefend(battleType));
    }

    /**
     * 计算伤害
     *
     * @param force  攻击方
     * @param target 防守方
     * @param crit   暴击倍率
     * @return
     */
    public static int calcHurt2(Force force, Force target, float crit, int battleType) {
        Fighter atkFighter = force.fighter;
        Fighter defFighter = target.fighter;
        /**
         * 计算伤害比例<br>
         * 定义: ATK: 攻击方的攻击力，DEF: 防御方的防御力 <br>
         * ATK > DEF 伤害比例 = [（A'攻击-B防御*K1）*K2*（K3*单排当前兵力/单排兵力上限+1-K3)+K4]*[0.9,1.1] 步长0.1 向上取整
         *
         * ATK <= DEF 伤害比例 = 1~10以内整数随机
         */
        float K1 = WorldConstant.K1;
        float K2 = WorldConstant.K2;
        float K3 = WorldConstant.K3;
        float K4 = WorldConstant.K4;
        float K5 = WorldConstant.K5;
        float K6 = WorldConstant.K6;
        float K7 = WorldConstant.K7;
        double hurt1;

        // 攻击 = ( 攻击 * ( 1 + 提升百分比 ) + 强攻 )
        double atk = getFinalAtk(force, target, battleType);
        // 防御 = ( 防御 * ( 1 + 提升百分比 ) + 强防 )
        double def = getFinalDef(force, target, battleType);

        //记录随机数 方便测试
        float hurt1Random;
        int hurt2Random = 0;

        // A攻击 > B防御 * K1 + 10
        if (atk > def * K1 + 10) {
            hurt1Random = RandomUtils.nextFloat(0.9f, 1.2f);
            // [( A攻击 - B防御 * K1) / K2 * ( K3 * 单排当前兵力 / 单排兵力上限 + 1 - K3 ) + K4 ] * [ 0.9 , 1.1 ] 向上取整
            hurt1 = ((atk - def * K1) / K2 * (K3 * force.count / force.lead + 1 - K3) + K4) * hurt1Random;
        } else {
            // 1 ~ 10随机整数
            hurt1Random = RandomHelper.randomInSize(10);
            hurt1 = (hurt1Random + 1);
        }

        // 若类型2伤害结果小于零, 则认为类型2伤害结果为零
        double hurt2 = (force.calcAtkExt() - target.calcDefExt() * K5) / K6 * (K7 * force.count / force.lead + 1 - K7);
        double debugHurt2 = hurt2;
        //天赋优化加成
        if (hurt2 > 0) {
            Player forcePlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
            //是否是pvp
            if (FightLogic.isCityBattle(battleType) && !CheckNull.isNull(forcePlayer)) {
                hurt2 *= (1 + (DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValue(forcePlayer, SeasonConst.TALENT_EFFECT_609) / Constant.TEN_THROUSAND));
            }
        }
        hurt2 = hurt2 < 0 ? 0 : hurt2;

        double hurt;
        double debugHurt;
        // ( 类型1伤害结果 + 类型2伤害结果 ) > 10
        if (hurt1 + hurt2 > 10) {
            debugHurt = hurt1 + hurt2;
            // ( 类型1伤害结果 + 类型2伤害结果 ) * 暴击倍伤 ( 暴击时 )  向上取整
            hurt = (hurt1 + hurt2) * crit;
        } else {
            // 1 ~ 10随机整数 * 暴击倍伤 ( 暴击时 )
            hurt2Random = RandomHelper.randomInSize(10);
            debugHurt = hurt2Random + 1;
            hurt = (hurt2Random + 1) * crit;
        }

        // 计算光环之前的伤害   方便测试
        double beforeHurt = hurt;

        // 光环技能
        if (!CheckNull.isNull(atkFighter) || !CheckNull.isNull(defFighter)) {
            // 光环技能计算后的伤害 = 伤害 * (1 + 光环增伤万分比) * (1 - 对面光环减伤万分比) * (1 + )
            hurt = hurt * (1 + (MedalConst.getAuraSkillNum(force, target, MedalConst.INCREASE_HURT_AURA)
                    / Constant.TEN_THROUSAND)) * (1 - (
                    MedalConst.getAuraSkillNum(target, force, MedalConst.REDUCE_HURT_AURA) / Constant.TEN_THROUSAND));
        }

        //增伤, 减伤计算(赛季天赋)
        hurt = hurt * (1 + (force.attrData.dmgInc - force.attrData.dmgDec));

        // 计算兵种克制关系之前的伤害
        double beforeRestrain = hurt;
        // 兵种克制关系系数
        double finalRestrain = getFinalRestrain(force, target, battleType);
        // 兵种克制后伤害 = 光环技能计算后的伤害 * ( 1 + 克制关系系数 )
        hurt = hurt * (1 + finalRestrain);

        //目前最终伤害 = 兵种克制后增伤 * (1 + 对 特定兵种增益伤害)
        double finalDamageToArms = buffDamageToArms(force, target);
        hurt = hurt * (1 + finalDamageToArms);

        String strType = battleType2String(battleType);
        // 伤害小于1时候, 默认1点伤害
        hurt = hurt < 1 ? 1 : hurt;
        //进攻方类型
        String atkstr = getRoleTypeStr(force.roleType);
        String tarstr = getRoleTypeStr(target.roleType);
        LogUtil.fight("进攻方角色id: ", force.ownerId, ",防守方角色id: ", target.ownerId, ",战斗回合===》战斗类型: ", battleType, strType,
                ",进攻方类型/将领id/将领类型/强化等级: ", atkstr, "/", force.id, "/", force.armType, "/", force.intensifyLv,
                ",进攻方基础攻击/计算后的攻击:", force.calcAttack(), "/", atk, ",防守方类型/将领id/将领类型/强化等级: ", tarstr, "/", target.id,
                "/", target.armType, "/", target.intensifyLv, ",防守方基础防御/计算后的防御: ", target.calcDefend(), "/", def,
                ",进攻方光环增伤: ",
                MedalConst.getAuraSkillNum(force, target, MedalConst.INCREASE_HURT_AURA) / Constant.TEN_THROUSAND,
                ",防守方光环减伤: ",
                MedalConst.getAuraSkillNum(target, force, MedalConst.REDUCE_HURT_AURA) / Constant.TEN_THROUSAND,
                ",伤害类型1: ", hurt1, ", 伤害类型1随机数: ", hurt1Random, "伤害类型2赛季天赋攻心扼吭加成前:", debugHurt2, "赛季天赋攻心扼吭加成:", hurt2 - debugHurt2,
                ", 类型2总伤害(赛季天赋攻心扼吭加成后): ", hurt2, ", 伤害类型2随机数: ", hurt2Random, ", 计算暴击之前: ", debugHurt,
                ",计算光环之前(计算暴击之后): ", beforeHurt, ", 计算光环之后的伤害值: ", beforeRestrain, ",兵种克制关系系数: ", finalRestrain, ", 对防守方当前兵种伤害加成: ", finalDamageToArms, ",最终伤害结果: ",
                hurt);
        return (int) hurt;
    }

    /**
     * 计算保底伤害
     *
     * @param attacker
     * @param defender
     * @return
     */
    public static int calRoundGuaranteedDamage(Force attacker, Force defender, int hurt, int battleType, float crit) {
        if (!FightLogic.checkPvp(attacker, defender) || battleType == Integer.MIN_VALUE)
            return hurt;
        StaticBattlePvp staticData = StaticBattleDataMgr.getBattlePvp(attacker.intensifyLv - defender.intensifyLv);
        if (CheckNull.isNull(staticData))
            return hurt;

        // 保底伤害=(最终伤害增幅(双方兵阶之差)+己方英雄面板攻击*最终伤害增幅系数(双方兵阶之差)/10000)*(K3*单排当前兵力/单排兵力上限+1-K3)*[0.9,1.2])
        float hurt1Random = RandomUtils.nextFloat(0.9f, 1.2f);
        int guaranteedDamage = (int) (((staticData.getDamage() + attacker.attrData.attack *
                (staticData.getDamageParam() / Constant.TEN_THROUSAND)) * (WorldConstant.K3 * attacker.count / attacker.lead + 1 - WorldConstant.K3) * hurt1Random) * crit);
        LogUtil.fight("进攻方角色id: ", attacker.ownerId, ",防守方角色id: ", defender.ownerId, ", " +
                "战斗回合===》战斗类型: ", FightCalc.battleType2String(battleType), "保底伤害随机数: ", hurt1Random, "保底伤害计算: ", guaranteedDamage, ", 暴击倍数: ", crit, "当前最终伤害:", hurt, ", 比对后最终伤害: ", Math.max(guaranteedDamage, hurt));
        return Math.max(guaranteedDamage, hurt);
    }

    /**
     * 攻击方对 特定兵种增益伤害
     *
     * @param force
     * @param target
     * @return
     */
    private static double buffDamageToArms(Force force, Force target) {
        if (CheckNull.isNull(force) || CheckNull.isNull(target))
            return 0d;

        double percentage = 0d;
        switch (target.armType) {
            case Constant.ArmyType.INFANTRY_ARMY_TYPE:
                percentage = force.attrData.moreInfantryDamage;
                break;
            case Constant.ArmyType.ARCHER_ARMY_TYPE:
                percentage = force.attrData.moreArcherDamage;
                break;
            case Constant.ArmyType.CAVALRY_ARMY_TYPE:
                percentage = force.attrData.moreCavalryDamage;
                break;
            default:
                percentage = 0d;
        }

        return percentage / Constant.TEN_THROUSAND;
    }

    public static String getRoleTypeStr(int roleType) {
        String str = "";
        switch (roleType) {
            case 1:
                str = "玩家";
                break;
            case 2:
                str = "流寇NPC";
                break;
            case 3:
                str = "城池守将NPC";
                break;
            case 4:
                str = "城池NPC";
                break;
        }
        return str;
    }

    public static String battleType2String(int battleType) {
        //战斗类型
        String strType = "打副本";
        switch (battleType) {
            case 1:
                strType = "城战 [打玩家]";
                break;
            case 2:
                strType = "国战、阵营战 [打城池]";
                break;
            case 3:
                strType = "盖世太保战";
                break;
            case 4:
                strType = "闪电战";
                break;
            case 5:
                strType = "柏林会战";
                break;
            case 6:
                strType = "超级矿点战斗";
                break;
        }
        return strType;
    }

    /**
     * 计算战机技能伤害
     *
     * @param force      攻击方
     * @param target     防守方
     * @param planeSkill 战机技能
     * @param battleType
     * @return
     */
    public static int calcPlaneSkillHurt(Force force, Force target, StaticPlaneSkill planeSkill, int battleType) {
        int type = planeSkill.getType();
        double hurt = 0.0;
        if (CheckNull.isNull(planeSkill)) {
            return (int) Math.floor(hurt);
        }
        double forceAtk = force.getPlaneAtk(battleType);
        double forceDef = force.getPlaneDef(battleType);
        double targetDef = target.getPlaneDef(battleType);
        switch (type) {
            case PlaneConstant.PLANE_SKILL_1: // 无视防御的固定值伤害
                hurt = planeSkill.getSkillEffect();
                break;

            // 技能伤害 = (技能攻击力-敌方将领最终防御力) / 3
            case PlaneConstant.PLANE_SKILL_2: // 所属将领攻击力x%的伤害
                hurt = ((forceAtk * (planeSkill.getSkillEffect() / Constant.TEN_THROUSAND)) - targetDef)
                        / PlaneConstant.PLANE_SKILL_RADIO;
                break;
            case PlaneConstant.PLANE_SKILL_3: // 所属将领防御力x%的伤害
                hurt = ((forceDef * (planeSkill.getSkillEffect() / Constant.TEN_THROUSAND)) - targetDef)
                        / PlaneConstant.PLANE_SKILL_RADIO;
                break;
            case PlaneConstant.PLANE_SKILL_4: // 所属将领兵力x%的伤害
                hurt = ((force.attrData.lead * (planeSkill.getSkillEffect() / Constant.TEN_THROUSAND)) - targetDef)
                        / PlaneConstant.PLANE_SKILL_RADIO;
                break;
        }
        // 最终伤害值 = 技能伤害 <= 0 ? [1-10]的随机整数值 : 技能伤害 * [0.9f, 1.1f]随机数 (结果向上取整)
        int randomInt = RandomHelper.randomInArea(1, 11);
        float randomFloat = RandomUtils.nextFloat(0.9f, 1.1f);
        double finalHurt = hurt <= 0.0 ? randomInt : hurt * randomFloat;
        LogUtil.fight("战机最终伤害值:", finalHurt, ", 技能伤害:", hurt, ", [1-10]的随机整数值:", randomInt, ", [0.9f, 1.1f]随机数:",
                randomFloat);
        return (int) Math.ceil(finalHurt);
    }


    /**
     * 计算对NPC的伤害可以获得的将领经验
     *
     * @param npcId
     * @param lead
     * @param hurt
     * @return
     */
    public static int calcNpcExp(int npcId, int lead, int hurt) {
        StaticNpc staticNpc = StaticNpcDataMgr.getNpcMap().get(npcId);

        if (null == staticNpc) {
            LogUtil.error("计算NPC经验，npcId为配置, npcId:", npcId);
            return 0;
        }

        return (int) (staticNpc.getExp() * 1.0 / staticNpc.getLine() * hurt / lead);

    }

    public static void main(String[] args) {
        System.out.println(
                ((389 - 220 * 1.0f) / 3.0f * (0 * 100 / 1 + 1 - 0) + 10) * RandomUtils.nextFloat(0.9f, 1.2f) * 1);
    }
}
