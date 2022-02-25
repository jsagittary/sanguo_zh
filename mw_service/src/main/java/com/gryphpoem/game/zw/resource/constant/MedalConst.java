package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedalAuraSkill;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author chenqi
 * @ClassName: MedalConst
 * @Description: 勋章 相关常量配置
 * @date 2018年9月11日
 */
public class MedalConst {

    /**
     * 勋章商店刷新上限
     */
    public static int MEDAL_GOODS_REFRESH_MAX;

    /**
     * 勋章商店刷新消耗的金币
     */
    public static int MEDAL_GOODS_REFRESH_GOLD;

    /**
     * 勋章强化消耗的金条
     */
    public static List<List<Integer>> MEDAL_INTENSIFY_GOLD_BAR;

    /**
     * 副本勋章掉落权重
     */
    public static List<List<Integer>> TASK_MEDAL_BURST_WEIGHT;

    /**
     * 勋章捐献金条返还
     */
    public static List<List<Integer>> MEDAL_DONATE_GOLD_BAR_RETURN;

    /**
     * 勋章商店每日刷新时间点
     */
    public static List<Integer> MEDAL_GOODS_REFRESH_EVERYDAY;

    /**
     * 新勋章强化消耗金锭配置
     */
    public static List<List<Integer>> MEDAL_INTENSIFY_GOLD_INGOT;

    /**
     * 新勋章金锭返还配置
     */
    public static List<List<Integer>> MEDAL_DONATE_GOLD_INGOT_RETURN;

    /**
     * 初始化勋章技能 类型值定义
     */
    public interface SkillType {
        int AURA = 1;// 光环技能

        int SPECIAL = 2;// 特技

        int GENERAL = 3;// 普通

        int[] skillType = { AURA, SPECIAL, GENERAL };
    }

    public static void loadSystem() {
        MEDAL_GOODS_REFRESH_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.MEDAL_GOODS_REFRESH_MAX, 100);
        MEDAL_GOODS_REFRESH_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.MEDAL_GOODS_REFRESH_GOLD, 10);
        MEDAL_INTENSIFY_GOLD_BAR = SystemTabLoader.getListListIntSystemValue(SystemId.MEDAL_INTENSIFY_GOLD_BAR, "[[]]");
        TASK_MEDAL_BURST_WEIGHT = SystemTabLoader.getListListIntSystemValue(SystemId.TASK_MEDAL_BURST_WEIGHT, "[[]]");
        MEDAL_DONATE_GOLD_BAR_RETURN = SystemTabLoader
                .getListListIntSystemValue(SystemId.MEDAL_DONATE_GOLD_BAR_RETURN, "[[]]");
        MEDAL_GOODS_REFRESH_EVERYDAY = SystemTabLoader
                .getListIntSystemValue(SystemId.MEDAL_GOODS_REFRESH_EVERYDAY, "[]");
        MEDAL_INTENSIFY_GOLD_INGOT = SystemTabLoader
                .getListListIntSystemValue(SystemId.MEDAL_INTENSIFY_GOLD_INGOT, "[[]]");
        MEDAL_DONATE_GOLD_INGOT_RETURN = SystemTabLoader
                .getListListIntSystemValue(SystemId.MEDAL_DONATE_GOLD_INGOT_RETURN, "[[]]");
    }

    /**
     * 将领可穿戴勋章数量 1号位只能上红色品质的
     */
    public static final int HERO_MEDAL_UP_CNT = 2;
    /**
     * 将领可穿戴勋章索引位0
     */
    public static final int HERO_MEDAL_INDEX_0 = 0;
    /**
     * 将领可穿戴勋章索引位1(只能上红色品质)
     */
    public static final int HERO_MEDAL_INDEX_1 = 1;

    /**
     * 勋章商店 获取类型
     */
    public static final int MEDAL_GOODS_GET_TYPE = 1;

    /**
     * 勋章商店 刷新类型
     */
    public static final int MEDAL_GOODS_REFRESH_TYPE = 2;

    /**
     * 勋章商店  勋章商品类型
     */
    public static final int MEDAL_GOODS_TYPE = 1;

    /**
     * 勋章商店 免费商品类型
     */
    public static final int GRATIS_GOODS_TYPE = 2;

    /**
     * 勋章商店 红色勋章商品类型
     */
    public static final int RED_MEDAL_GOODS_TYPE = 3;

    /**
     * 勋章系统解锁配置
     */
    public static final int MEDAL_SYS_LOCK = 65;

    /**
     * 勋章强化界线  8级以下  每升两级激活或升级技能   8级以后    每升一级激活或升级技能
     */
    public static final int MEDAL_INTENSIFY_LV = 8;

    /**
     * 勋章捐献升级 类型
     */
    public static final int MEDAL_DONATE_UPGRADE_TYPE = 1;

    /**
     * 勋章捐献获得资源 类型
     */
    public static final int MEDAL_DONATE_GET_RESOURCE_TYPE = 2;

    /**
     * 勋章配置万分比
     */
    public static final double MEDAL_CONFIG_PERCENTAGE = 10000.00;

    /**
     * 勋章商品金币刷新类型
     */
    public static final int MEDAL_GOODS_REFRESH_GOLD_TYPE = 1;

    /**
     * 勋章商品定时器刷新类型
     */
    public static final int MEDAL_GOODS_REFRESH_JOB_TYPE = 2;

    /**
     * 勋章商品服务器启动 刷新类型
     */
    public static final int MEDAL_GOODS_SERVER_START_TYPE = 3;

    /**
     * 勋章商品转点 刷新类型
     */
    public static final int MEDAL_GOODS_0_TYPE = 4;

    /**
     * 勋章商品GM 刷新类型
     */
    public static final int MEDAL_GOODS_GM_TYPE = 5;

    /*************************  勋章特技技能id  ******************************/
    /**
     * 白衣天使    +20%伤兵恢复，可与道具的伤兵恢复叠加  ps:该特技除了柏林不生效，其他都生效。 生效规则：战斗结束后，先执行扣兵逻辑，在执行特技，将兵力恢复给将领
     */
    public static final int ANGEL_IN_WHITE = 104;

    /**
     * 以战养战    据点战获胜后生效(打飞人)，可将杀敌数10%转化为本方兵力，无法超过带兵上限  ps: 该特技柏林不生效,其他的玩家打玩家生效
     */
    public static final int SUSTAIN_THE_WAR_BY_MEANS_OF_WAR = 204;

    /**
     * 维和部队    据点战失败后生效，被击飞后保护罩时间增加1小时，多个将领佩戴可累计，每日可生效1次
     */
    public static final int PEACEKEEPING_FORCES = 304;

    /**
     * 奇袭匪军    单个将领出征杀死匪军获得的资源1.5倍
     */
    public static final int A_SURPRISE_ATTACK_ON_THE_BANDIT_ARMY = 404;

    /**
     * 军功显赫    营地战有击杀时默认获得10万威望，半小时生效1次
     */
    public static final int MILITARY_MERIT_IS_PROMINENT = 504;

    /**
     * 后勤保障    采集资源量增加+50%
     */
    public static final int LOGISTIC_SERVICE = 604;

    /**
     * 闪击奇兵     在营地战（打玩家）和  据点战（打城）时， 据守属性可在进攻时生效   【也就是进攻方有该特技，攻击 = 攻击力+攻坚+据守】
     */
    public static final int BLITZ_CURIOUS_SOLDIER = 704;

    /**
     * 铜墙铁壁     在营地战（打玩家）和  据点战（打城）时，攻坚属性可在防守时生效    【也就是防守方有该特技，防御 = 防御力+据守+攻坚】
     */
    public static final int IRON_BASTIONS = 804;

    /**
     * 西点特训    佩戴勋章将领每天获得400体力的副本经验，每日5点生效
     */
    public static final int WESTERN_POINT_SPECIAL_TRAINING = 904;

    /**
     * 强化闪击奇兵 任何战斗中，拒守属性均可在进攻时生效
     */
    public static final int INTENSIFY_BLITZ_CURIOUS_SOLDIER = 1004;
    /**
     * 强化铜墙铁壁 任何战斗中，攻坚属性均可在防守时生效
     */
    public static final int INTENSIFY_IRON_BASTIONS = 1104;

    /**
     * 强化白衣天使 在营地战和攻城战时，50%攻坚属性额外加成到据守属性上
     */
    public static final int INTENSIFY_ANGEL_IN_WHITE = 1204;

    /**
     * 强化以战养战 在营地战和攻城战时，50%据守属性额外加成到攻坚属性上
     */
    public static final int INTENSIFY_SUSTAIN_THE_WAR_BY_MEANS_OF_WAR = 1304;

    /**
     * 强化后勤保障 采集铀矿资源量增加+20%
     */
    public static final int INTENSIFY_LOGISTIC_SERVICE = 1404;

    /**
     * 强化军工显赫 增加1000防护
     */
    public static final int INTENSIFY_MILITARY_MERIT_IS_PROMINENT = 1504;

    /*************************  光环技能id  ******************************/
    /**
     * 战车光环-减伤   战车将领统帅的部队将携带减伤光环，全体上阵将领受到伤害减少5%
     */
    public static final int WAR_CHARIOT_AURA_INJURY = 104;

    /**
     * 强化光环-减伤   增加将领450防御和450防护,全体上阵将领受到伤害减少8%
     */
    public static final int INTENSIFY_WAR_CHARIOT_AURA_INJURY = 1004;

    /**
     * 战车光环-反制   战车将领统帅的部队将携带反制光环，全体上阵将领对火箭伤害增加8%
     */
    public static final int WAR_CHARIOT_AURA_COUNTERSPELL = 204;

    /**
     * 强化光环-反制   增加将领450攻击和450防护,全体上阵将领对火箭伤害增加12%
     */
    public static final int INTENSIFY_WAR_CHARIOT_AURA_COUNTERSPELL = 1104;

    /**
     * 战车光环-绝境   战车将领统帅的部队将携带绝境光环，全体上阵将领每少一排兵伤害增加1%
     */
    public static final int WAR_CHARIOT_AURA_CUL_DE_SAC = 304;

    /**
     * 强化光环-绝境   增加将领450穿甲和4%暴击,全体上阵将领每少一排兵伤害增加2%
     */
    public static final int INTENSIFY_WAR_CHARIOT_AURA_CUL_DE_SAC = 1204;

    /**
     * 坦克光环-幻象   坦克将领统帅的部队将携带幻象光环，全体上阵将领对战车伤害增加8%
     */
    public static final int TANK_AURA_PHANTOM = 404;

    /**
     * 强化光环-幻象   增加将领450防御和4%闪避,全体上阵将领对战车伤害增加12%
     */
    public static final int INTENSIFY_TANK_AURA_PHANTOM = 1304;

    /**
     * 坦克光环-天启   坦克将领统帅的部队将携带天启光环，全体上阵将领对火箭暴击率增加10%
     */
    public static final int TANK_AURA_APOCALYPSE = 504;

    /**
     * 强化光环-天启   增加将领450攻击和4%暴击,全体上阵将领对火箭暴击率增加15%
     */
    public static final int INTENSIFY_TANK_AURA_APOCALYPSE = 1404;

    /**
     * 坦克光环-连战   坦克将领统帅的部队将携带连战光环，全体上阵将领每击杀一排兵，伤害减免1%
     */
    public static final int TANK_AURA_LINE_CHAN = 604;

    /**
     * 强化光环-连战   增加将领450防护和675攻击,全体上阵将领每击杀一排兵,伤害减免2%
     */
    public static final int INTENSIFY_TANK_AURA_LINE_CHAN = 1504;

    /**
     * 火箭光环-蓄势   火箭将领统帅的部队将携带蓄势光环，全体上阵将领每多一排兵待命，伤害1%
     */
    public static final int ROCKET_AURA_ANTICIPATION = 704;

    /**
     * 强化光环-蓄势   增加将领450攻击和4%闪避,全体上阵将领每多一排兵待命,伤害提高2%
     */
    public static final int INTENSIFY_ROCKET_AURA_ANTICIPATION = 1604;

    /**
     * 火箭光环-乱射   火箭将领统帅的部队将携带乱射光环，全体上阵将领对坦克暴击率增加10
     */
    public static final int RECKET_AURA_APOCALYPSE = 804;

    /**
     * 强化光环-乱射   增加将领450攻击和4%暴击,全体上阵将领对坦克暴击率增加15%
     */
    public static final int INTENSIFY_RECKET_AURA_APOCALYPSE = 1704;

    /**
     * 火箭光环-风琴   火箭将领统帅的部队将携带乱射光环，全体上阵将领对坦克伤害增加12%
     */
    public static final int ROCKET_AURA_WIND_HARP = 904;

    /**
     * 强化光环-风琴   增加将领675穿甲和4%闪避,全体上阵将领对坦克伤害增加15%
     */
    public static final int INTENSIFY_ROCKET_AURA_WIND_HARP = 1804;

    /**
     * 获取光环效果
     *
     * @param force
     * @param target
     * @param auras
     * @return
     */
    public static int getAuraSkillNum(Force force, Force target, int[] auras) {
        int meadlAuraSkillNum = 0;
        Fighter atkFighter = force.fighter;
        Fighter defFighter = target.fighter;
        if (CheckNull.isNull(atkFighter) || CheckNull.isNull(defFighter)) {
            return meadlAuraSkillNum;
        }
        Map<Integer, Integer> auraSkillMap = atkFighter.getAuraSkill(force.ownerId);
        if (CheckNull.isNull(auraSkillMap)) {
            return meadlAuraSkillNum;
        }
        for (Map.Entry<Integer, Integer> en : auraSkillMap.entrySet()) {
            if (Arrays.binarySearch(auras, en.getKey()) < 0) {
                continue;
            }
            StaticMedalAuraSkill auraSkillById = StaticMedalDataMgr.getAuraSkillById(en.getKey());
            Integer cnt = en.getValue();
            if (cnt <= 0) {
                continue;
            }
            switch (en.getKey()) {
                // 战车光环-减伤
                case WAR_CHARIOT_AURA_INJURY:
                case INTENSIFY_WAR_CHARIOT_AURA_INJURY:
                    meadlAuraSkillNum += auraSkillById.getSkillEffect() * cnt ;
                    break;
                // 战车光环-绝境
                case WAR_CHARIOT_AURA_CUL_DE_SAC:
                case INTENSIFY_WAR_CHARIOT_AURA_CUL_DE_SAC:
                    // 当前将领的死亡排数
                    int deadLine = force.getDeadLine();
                    if (deadLine > 0) {
                        meadlAuraSkillNum += auraSkillById.getSkillEffect() * deadLine * cnt;
                    }
                    break;
                // 坦克光环-幻象
                case TANK_AURA_PHANTOM:
                case INTENSIFY_TANK_AURA_PHANTOM:
                    if (target.armType == ArmyConstant.ARM1) {
                        meadlAuraSkillNum += auraSkillById.getSkillEffect() * cnt;
                    }
                    break;
                // 战车光环-反制 坦克光环-天启
                case WAR_CHARIOT_AURA_COUNTERSPELL:
                case TANK_AURA_APOCALYPSE:
                case INTENSIFY_WAR_CHARIOT_AURA_COUNTERSPELL:
                case INTENSIFY_TANK_AURA_APOCALYPSE:
                    if (target.armType == ArmyConstant.ARM3) {
                        meadlAuraSkillNum += auraSkillById.getSkillEffect() * cnt;
                    }
                    break;
                // 坦克光环-连战
                case TANK_AURA_LINE_CHAN:
                case INTENSIFY_TANK_AURA_LINE_CHAN:
                    // 对方将领的死亡排数就是我的击杀排数
                    int killLine = target.getDeadLine();
                    if (killLine > 0) {
                        meadlAuraSkillNum += auraSkillById.getSkillEffect() * killLine * cnt;
                    }
                    break;
                // 火箭光环-蓄势
                case ROCKET_AURA_ANTICIPATION:
                case INTENSIFY_ROCKET_AURA_ANTICIPATION:
                    // 当前将领的最大排数 - 死亡排数
                    int curLine = force.maxLine - force.getDeadLine();
                    if (curLine > 0) {
                        meadlAuraSkillNum += auraSkillById.getSkillEffect() * curLine * cnt;
                    }
                    break;
                // 火箭光环-乱射 火箭光环-风琴
                case RECKET_AURA_APOCALYPSE:
                case INTENSIFY_RECKET_AURA_APOCALYPSE:
                case ROCKET_AURA_WIND_HARP:
                case INTENSIFY_ROCKET_AURA_WIND_HARP:
                    if (target.armType == ArmyConstant.ARM2) {
                        meadlAuraSkillNum += auraSkillById.getSkillEffect() * cnt;
                    }
                    break;
                default:
                    break;
            }

        } return meadlAuraSkillNum;
    }

    /**
     * 增伤光环技能
     */
    public static final int[] INCREASE_HURT_AURA = { WAR_CHARIOT_AURA_COUNTERSPELL, WAR_CHARIOT_AURA_CUL_DE_SAC,
            TANK_AURA_PHANTOM, ROCKET_AURA_ANTICIPATION, ROCKET_AURA_WIND_HARP, INTENSIFY_WAR_CHARIOT_AURA_COUNTERSPELL,
            INTENSIFY_WAR_CHARIOT_AURA_CUL_DE_SAC, INTENSIFY_TANK_AURA_PHANTOM, INTENSIFY_ROCKET_AURA_ANTICIPATION, INTENSIFY_ROCKET_AURA_WIND_HARP };
    /**
     * 减伤光环技能
     */
    public static final int[] REDUCE_HURT_AURA = { WAR_CHARIOT_AURA_INJURY, TANK_AURA_LINE_CHAN,
            INTENSIFY_WAR_CHARIOT_AURA_INJURY, INTENSIFY_TANK_AURA_LINE_CHAN };
    /**
     * 增暴击几率光环技能
     */
    public static final int[] INCREASE_CRIT_AURA = { TANK_AURA_APOCALYPSE, RECKET_AURA_APOCALYPSE, INTENSIFY_TANK_AURA_APOCALYPSE, INTENSIFY_RECKET_AURA_APOCALYPSE };
    /**
     * 击杀兵排数Key
     */
    public static final int KILL_LINE_KEY = 999;
}
