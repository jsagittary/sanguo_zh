package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.List;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName EquipConstant.java
 * @Description 装备相关
 * @date 创建时间：2017年3月27日 下午5:21:03
 */
public class EquipConstant {

    /**
     * 装备洗练等级概率(万分比)
     */
    public static Map<Integer, Integer> EQUIP_LV_PROBABILITY;
    /**
     * 装备洗练技能种类随机(权重)
     */
    public static List<List<Integer>> EQUIP_SKILL_PROBABILITY;
    /**
     * 戒指洗练等级概率(万分比)
     */
    public static Map<Integer, Integer> RING_LV_PROBABILITY;
    /**
     * 戒指洗练技能种类随机(权重)
     */
    public static List<List<Integer>> RING_SKILL_PROBABILITY;
    /**
     * 装备可以镶嵌宝石数量
     */
    public static int EQUIP_JEWEL_MAX;
    /**
     * 强化几次可以提升资质
     */
    public static int UP_ATTR_COUNT;
    /**
     * 戒指改造消耗
     */
    public static List<List<Integer>> RING_BAPTIZE_COST;
    /**
     * 戒指超级改造消耗
     */
    public static List<List<Integer>> RING_SUPER_BAPTIZE_COST;
    /**
     * 戒指洗练保底配置
     */
    public static Map<Integer, Integer> RING_BAPTIZE_MIN_UPLV_CONF;

    /**
     * 装备自动锁定
     */
    public static int EQUIP_AUTO_LOCK;

    /**
     * 加载装备配置常量
     */
    public static void loadSystem() {
        RING_LV_PROBABILITY = SystemTabLoader.getMapIntSystemValue(SystemId.RING_LV_PROBABILITY, "[[]]");
        RING_SKILL_PROBABILITY = SystemTabLoader.getListListIntSystemValue(SystemId.RING_SKILL_PROBABILITY, "[[]]");
        EQUIP_LV_PROBABILITY = SystemTabLoader.getMapIntSystemValue(SystemId.EQUIP_LV_PROBABILITY, "[[]]");
        EQUIP_SKILL_PROBABILITY = SystemTabLoader.getListListIntSystemValue(SystemId.EQUIP_SKILL_PROBABILITY, "[[]]");
        EQUIP_JEWEL_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_JEWEL_MAX, 1);
        UP_ATTR_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.UP_ATTR_COUNT, 2);
        RING_BAPTIZE_COST = SystemTabLoader.getListListIntSystemValue(SystemId.RING_BAPTIZE_COST, "[[]]");
        RING_SUPER_BAPTIZE_COST = SystemTabLoader.getListListIntSystemValue(SystemId.RING_SUPER_BAPTIZE_COST, "[[]]");
        RING_BAPTIZE_MIN_UPLV_CONF = SystemTabLoader.getMapIntSystemValue(SystemId.RING_BAPTIZE_MIN_UPLV_CONF, "[[]]");
        EQUIP_AUTO_LOCK = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_AUTO_LOCK, 6);
    }

    /**
     * 装备部位：主武器
     */
    public static final int PART_MAIN_WEAPON = 1;
    /**
     * 装备部位：副武器
     */
    public static final int PART_SIDE_WEAPON = 2;
    /**
     * 装备部位：头盔
     */
    public static final int PART_HEAD = 3;
    /**
     * 装备部位：战服
     */
    public static final int PART_CLOTH = 4;
    /**
     * 装备部位：工具
     */
    public static final int PART_TOOL = 5;
    /**
     * 装备部位：系统
     */
    public static final int PART_SYSTEM = 6;
    /**
     * 装备部位：左戒指
     */
    public static final int PART_RING_LEFT = 7;
    /**
     * 装备部位：右戒指
     */
    public static final int PART_RING_RIGHT = 8;

    /**
     * 判断是否是戒指
     *
     * @param part
     * @return
     */
    public static boolean isRingEquip(int part) {
        return part == PART_RING_RIGHT || part == PART_RING_LEFT;
    }

    /**
     * 判断将领装备拥有率是否偏低的阈值（该值为百分数），如果低于该值，判定为偏低
     */
    public static final int MIDDLE_OWN_RATE = 50;
    /**
     * 能触发4技能装备的最低品质
     */
    public static final int EQUIP_4_SKILL_QUALITY = 4;

}
