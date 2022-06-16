package com.gryphpoem.game.zw.service.robot;

import com.google.common.collect.Lists;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.EquipQue;
import com.gryphpoem.game.zw.resource.domain.s.StaticEquip;
import com.gryphpoem.game.zw.resource.domain.s.StaticEquipQualityExtra;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.EquipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Description 机器人装备相关服务类
 * @author TanDonghai
 * @date 创建时间：2017年10月17日 下午4:31:56
 *
 */
@Service
public class RobotEquipService {

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private EquipService equipService;

    /**
     * 自动打造装备
     * 
     * @param player
     */
    public void autoEquipForge(Player player) {
        if (null == player || equipService.equipQueIsFull(player)) {
            return;
        }

        int equipId = findOptimizedEquipId(player);
        if (equipId > 0) {
            StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equipId);
            // 扣除消耗
            try {
                rewardDataManager.subPlayerResHasChecked(player, staticEquip.getMaterial(), true, AwardFrom.EQUIP_FORGE);
            } catch (MwException e) {
                LogUtil.robot(e, "自动打造装备扣资源失败, roleId:", player.roleId, ", equipId:", equipId);
            }
            // 开启打造队列
            equipService.beginForgeEquipQue(player, staticEquip);
        }
    }

    /**
     * 打造装备类任务过滤器
     */
    private Predicate<StaticTask> forgeEquipTaskFilter = t -> (t.getCond() == TaskType.COND_EQUIP_BUILD);

    /**
     * 打造装备任务优先级比较器
     */
    private Comparator<StaticTask> equipIdComparator = (t1, t2) -> (t1.getCondId() - t2.getCondId());

    /**
     * 查找最适合当前打造的装备，并返回该装备的id
     * 
     * @param player
     * @return
     */
    private int findOptimizedEquipId(Player player) {
        int equipId = -1;
        List<Integer> curTaskIds = Lists.newArrayList(player.chapterTask.getOpenTasks().keySet());
        List<StaticTask> buildTask = curTaskIds.stream().map(StaticTaskDataMgr::getTaskById)
                .filter(forgeEquipTaskFilter).collect(Collectors.toList());

        Optional<StaticTask> minEquipTask = buildTask.stream().filter(t -> equipCanForge(player, t.getCondId()))
                .min(equipIdComparator);
        // 当前有可以完成的装备打造任务，优先打造任务装备
        if (minEquipTask.isPresent()) {
            equipId = minEquipTask.get().getCondId();
        } else {// 当前没有适合完成的装备打造任务，遍历所有装备，查找当前可以打造的装备，并计算最优先的
            // 检查当前所有的上阵将领是否都有装备，如果都有，过滤掉低于已穿戴装备中品质最低的
            int minQuality = getDressEquipMinQulality(player);
            Optional<StaticEquip> equip = StaticPropDataMgr.getEquipMap().values().stream()
                    .filter(t -> t.getQuality() > minQuality).filter(t -> equipCanForge(player, t.getEquipId()))
                    .min((e1, e2) -> canForgeEquipCompare(player, e1, e2));
            if (equip.isPresent()) {
                equipId = equip.get().getEquipId();
            }
        }

        return equipId;
    }

    /**
     * 获取玩家所有将领已穿戴的装备中的最低品质，如果有将领不是所有部位都已穿戴了装备，将会返回0
     * 
     * @param player
     * @return
     */
    private int getDressEquipMinQulality(Player player) {
        int minQuality = Integer.MAX_VALUE;
        for (Hero hero : player.heros.values()) {
            if (!hero.isIdle()) {
                int quality = getHeroEquipMinQuality(player, hero);
                if (quality < minQuality) {
                    minQuality = quality;
                }
            }
        }
        return minQuality;
    }

    /**
     * 获取将领穿戴的装备中的最低品质，如果该将领不是所有部位都已穿戴了装备，将会返回0
     * 
     * @param player
     * @param hero
     * @return
     */
    private int getHeroEquipMinQuality(Player player, Hero hero) {
        int minQuality = Integer.MAX_VALUE;
        Equip equip;
        StaticEquip staticEquip;
        for (int equipKeyId : hero.getEquip()) {
            equip = player.equips.get(equipKeyId);
            if (null == equip) {
                return 0;
            }

            staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
            if (staticEquip.getQuality() < minQuality) {
                minQuality = staticEquip.getQuality();
            }
        }
        return minQuality;
    }

    /**
     * 计算可打造装备的优先级，优先级越高值越小
     * 
     * @param player
     * @param e1
     * @param e2
     * @return 如果e1的优先级高于e2，返回负数
     */
    private int canForgeEquipCompare(Player player, StaticEquip e1, StaticEquip e2) {
        // 计算上阵将领的装备拥有率
        int compare = compareHeroEquips(player, e1, e2, player.heroBattle);
        // 如果上阵将领穿戴的装备比较不出两件装备的优先级（将领都已穿戴装备），再比较采集将领
        if (compare == 0) {
            compare = compareHeroEquips(player, e1, e2, player.heroAcq);
            // 最后比较防守将领
            if (compare == 0) {
                compare = compareHeroEquips(player, e1, e2, player.heroWall);
                // 比较完所有出战的将领，没有得出结果（即所有将领都已全身穿戴装备），优先品质高的
                if (compare == 0) {
                    return compareNum(e2.getQuality(), e1.getQuality());
                }
            }
        }
        return compare;
    }

    /**
     * 根据将领身上穿戴的装备，比较当前哪件装备打造优先级更高
     * 
     * @param player
     * @param e1
     * @param e2
     * @param heroIds
     * @return
     */
    private int compareHeroEquips(Player player, StaticEquip e1, StaticEquip e2, int[] heroIds) {
        Turple<Integer, Integer> equipOwn = getHeroEquipRate(player, heroIds);
        int ownEquipRate = equipOwn.getA();
        int averageQuality = equipOwn.getB();
        if (ownEquipRate < Constant.INT_HUNDRED) {
            // 装备拥有率偏低，优先打造消耗时间短的
            if (ownEquipRate < EquipConstant.MIDDLE_OWN_RATE) {
                return compareNum(e1.getBuildTime(), e2.getBuildTime());
            } else {// 装备拥有率不低时，优先打造靠近当前平均品质的装备
                return compareNum(Math.abs(e1.getQuality() - averageQuality),
                        Math.abs(e2.getQuality() - averageQuality));
            }
        }
        return 0;
    }

    /**
     * 比较两数大小，前面的数小则返回-1，相等返回0，否则返回1
     * 
     * @param num1
     * @param num2
     * @return 返回值-1，0，1
     */
    private int compareNum(int num1, int num2) {
        if (num1 < num2) {
            return -1;
        } else if (num1 > num2) {
            return 1;
        }
        return 0;
    }

    /**
     * 获取传入将领集合的装备拥有率（百分比），装备平均品质
     * 
     * @param player
     * @param heros
     * @return 如果传入的集合中没有将领，装备拥有率将返回-1
     */
    private Turple<Integer, Integer> getHeroEquipRate(Player player, int[] heros) {
        Hero hero;
        Equip equip;
        int total = 0;
        int ownCount = 0;
        int ownRate = -1;// 记录将领的装备拥有率
        int totalQuality = 0;
        int averageQuality = 0;// 记录将领装备的平均品质
        for (int heroId : heros) {
            hero = player.heros.get(heroId);
            if (null != hero) {
                total += HeroConstant.HERO_EQUIP_NUM;
                for (int equipKeyId : hero.getEquip()) {
                    if (equipKeyId > 0) {
                        equip = player.equips.get(equipKeyId);
                        totalQuality += StaticPropDataMgr.getEquip(equip.getEquipId()).getQuality();
                        ownCount++;
                    }
                }
            }
        }
        if (total > 0) {
            ownRate = ownCount * Constant.INT_HUNDRED / total;
            averageQuality = (int) Math.ceil(totalQuality * 1.0f / total);// 向上取整
        }
        return new Turple<>(ownRate, averageQuality);
    }

    /**
     * 判断装备是否满足打造条件
     * 
     * @param player
     * @param equipId
     * @return
     */
    private boolean equipCanForge(Player player, int equipId) {
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equipId);
        if (null == staticEquip) {
            return false;
        }

        try {
            rewardDataManager.checkPlayerResIsEnough(player, staticEquip.getMaterial());
            return true;
        } catch (MwException e) {
            return false;
        }
    }

    /**
     * 自动收取打造好的装备
     * 
     * @param player
     */
    public void autoGainEquip(Player player) {
        if (null == player || CheckNull.isEmpty(player.equipQue)) {
            return;
        }

        int now = TimeHelper.getCurrentSecond();
        EquipQue buildQue = player.equipQue.get(0);
        if (null == buildQue || now < buildQue.getEndTime()) {
            return;
        }

        try {
            rewardDataManager.checkBagCnt(player);
        } catch (MwException e) {
            // 背包不足，扩充背包或消耗掉部分装备
            if (e.getCode() == GameError.MAX_EQUIP_STORE.getCode()) {
                try {
                    // 优先扩充背包
                    equipService.checkAndExpandBag(player);
                } catch (MwException e1) {
                    // 背包扩充失败，分解掉品质为白色的装备
                    decomposeAssignQualityEquips(player, Constant.Quality.white);
                }
            }
        }

        // 收取装备
        equipService.dealOneQue(player, buildQue);
        player.equipQue.clear();
        taskDataManager.updTask(player, TaskType.COND_EQUIP_BUILD, 1, buildQue.getEquipId());
    }

    /**
     * 分解掉指定品质的闲置装备
     * 
     * @param player
     * @param quality
     */
    private void decomposeAssignQualityEquips(Player player, int quality) {
        // 筛选出对应品质的闲置装备
        List<Equip> unusedEquips = player.equips.values().stream().filter(unusedEquipFilter)
                .filter(e -> StaticPropDataMgr.getEquip(e.getEquipId()).getQuality() == quality)
                .collect(Collectors.toList());

        StaticEquip staticEquip;
        for (Equip equip : unusedEquips) {
            staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
            if (null != staticEquip) {
                // 移除装备
                player.equips.remove(equip.getKeyId());

                // 发送分解获得物品
                rewardDataManager.sendReward(player, staticEquip.getDecompose(), AwardFrom.EQUIP_DECOMPOSE);

                // 额外图纸
                if (player.shop.getVipId().contains(Constant.EQUIP_DECOMPOSE_VIP_BAG)
                        && RandomHelper.isHitRangeIn100(Constant.EQUIP_DECOMPOSE_RATE)
                        && !staticEquip.getDecompose2().isEmpty()) {
                    rewardDataManager.sendReward(player, staticEquip.getDecompose2(), AwardFrom.EQUIP_DECOMPOSE);
                }
            }
        }
    }

    /**
     * 闲置装备过滤器
     */
    private Predicate<Equip> unusedEquipFilter = e -> (e.getHeroId() <= 0);

    /**
     * 闲置装备重要性比较器
     */
    private Comparator<Equip> equipComparator = this::equipCompare;

    /**
     * 自动穿戴装备
     * 
     * @param player
     */
    public void autoDressEquip(Player player) {
        // 筛选出闲置装备，按指定的优先级排序方法排序
        List<Equip> unusedEquips = player.equips.values().stream().filter(unusedEquipFilter).sorted(equipComparator)
                .collect(Collectors.toList());
        for (Equip equip : unusedEquips) {
            // 穿戴装备，如果该装备被成功穿戴，将会返回true
            if (dressEquip(player, equip)) {
                player.equips.remove(equip.getKeyId());
            }
        }
    }

    /**
     * 给玩家所有的上阵将领试穿戴装备
     * 
     * @param player
     * @param unusedEquip 准备试穿戴的闲置装备
     * @return 穿戴成功返回true
     */
    private boolean dressEquip(Player player, Equip unusedEquip) {
        // 先给上阵将领试穿戴，如果上阵将领不需要，再给采集将领试穿戴
        if (!dressToHeros(player, player.heroBattle, unusedEquip)
                && !dressToHeros(player, player.heroAcq, unusedEquip)) {
            // 如果采集将领也不需要，最后给城防将领试穿戴
            return dressToHeros(player, player.heroWall, unusedEquip);
        }
        return true;
    }

    /**
     * 给指定将领试穿戴装备
     * 
     * @param player
     * @param heroIds
     * @param unusedEquip 准备试穿戴的闲置装备
     * @return 穿戴成功返回true
     */
    private boolean dressToHeros(Player player, int[] heroIds, Equip unusedEquip) {
        StaticEquip se = StaticPropDataMgr.getEquip(unusedEquip.getEquipId());
        int part = se.getEquipPart();
        Hero hero;
        Equip equip;
        StaticEquip staticEquip;
        for (int heroId : heroIds) {
            hero = player.heros.get(heroId);
            if (null != hero) {
                int equipKeyId = hero.getEquip()[part];
                equip = player.equips.get(equipKeyId);
                boolean needDress = true;// 记录是否需要穿戴该装备
                if (null != equip) {
                    staticEquip = StaticPropDataMgr.getEquip(equip.getEquipId());
                    if (se.getQuality() <= staticEquip.getQuality()) {
                        needDress = false;// 品质不比当前装备的高，不替换
                    } else {
                        // 卸下当前装备
                        equipService.downEquip(player, hero, equipKeyId);
                    }
                }

                if (needDress) {// 穿戴装备，返回穿戴成功
                    equipService.heroOnEquip(player, hero, part, equipKeyId);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 装备重要性比较，越重要值越小
     * 
     * @param e1
     * @param e2
     * @return
     */
    private int equipCompare(Equip e1, Equip e2) {
        // 优先比较装备品质
        StaticEquip se1 = StaticPropDataMgr.getEquip(e1.getEquipId());
        StaticEquip se2 = StaticPropDataMgr.getEquip(e2.getEquipId());
        if (se1.getQuality() > se2.getQuality()) {
            return -1;
        } else if (se1.getQuality() < se2.getQuality()) {
            return 1;
        } else {// 比较技能总等级
            int lv1 = getEquipTotalSkillLv(e1);
            int lv2 = getEquipTotalSkillLv(e2);
            if (lv1 > lv2) {
                return -1;
            } else if (lv1 < lv2) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * 统计装备所有技能的等级和
     * 
     * @param equip
     * @return
     */
    private int getEquipTotalSkillLv(Equip equip) {
        int totalLv = 0;
        for (Turple<Integer, Integer> turple : equip.getAttrAndLv()) {
            totalLv += turple.getB();
        }
        return totalLv;
    }

    /**
     * 已穿戴装备过滤器
     */
    private Predicate<Equip> dressEquipFilter = e -> e.getHeroId() > 0;

    /**
     * 需要改造的装备（改造等级未满）过滤器
     */
    private Predicate<Equip> needRefitEquipFilter = e -> equipNeedRefit(e);

    /**
     * 自动装备改造
     * 
     * @param player
     * @param now
     */
    public void autoEquipRefit(Player player, int now) {
        equipService.dealBaptizeCnt(player, now);

        if (player.common.getBaptizeCnt() <= 0) {
            // 没有免费改造次数，跳过
            return;
        }

        // 查找当前品质最高的装备，优先改造品质高的装备
        long start = System.nanoTime();
        Optional<Equip> minEquip = player.equips.values().stream().filter(dressEquipFilter).filter(needRefitEquipFilter)
                .min(equipComparator);
        long end = System.nanoTime();
        LogUtil.robot("机器人装备改造，查找需要改造的装备耗时（微秒）:", (end - start) / 1000);
        if (minEquip.isPresent()) {
            start = System.nanoTime();
            Equip equip = minEquip.get();
            int refitCount = player.common.getBaptizeCnt();
            while (refitCount > 0 && equipNeedRefit(equip)) {
                refitCount--;
                equipRefit(equip);
            }
            LogUtil.robot("机器人装备改造，改造装备耗时（微秒）:", (end - start) / 1000, ", refitCnt:",
                    (player.common.getBaptizeCnt() - refitCount));
            player.common.setBaptizeCnt(refitCount);
        }
    }

    /**
     * 装备改造
     * 
     * @param equip
     */
    private void equipRefit(Equip equip) {
        StaticEquip se = StaticPropDataMgr.getEquip(equip.getEquipId());
        StaticEquipQualityExtra extra = StaticPropDataMgr.getQualityMap().get(se.getWashQuality());
        for (int i = 0; i < extra.getExtraNum(); i++) {
            Integer attrId = RandomUtil.getRandomByWeight(EquipConstant.EQUIP_SKILL_PROBABILITY);
            equip.getAttrAndLv().get(i).setA(null == attrId ? Constant.AttrId.ATTACK : attrId);
        }

        // 获取最小等级
        Optional<Turple<Integer, Integer>> min = equip.getAttrAndLv().stream()
                .min(Comparator.comparingInt(p -> p.getB().intValue()));
        if (min.isPresent()) {
            Turple<Integer, Integer> minLv = min.get();
            // 最小等级的下标
            Integer probability = EquipConstant.EQUIP_LV_PROBABILITY.get(minLv.getB() + 1);
            if (probability == null) {
                probability = 0;
            }
            // 加上等级
            minLv.setB(RandomHelper.isHitRangeIn10000(probability) ? minLv.getB() + 1 : minLv.getB());
        }
    }

    /**
     * 判断装备是否还需要改造（洗炼），白色装备不需要，技能等级已满的也不需要
     * 
     * @param equip
     * @return
     */
    private boolean equipNeedRefit(Equip equip) {
        StaticEquip se = StaticPropDataMgr.getEquip(equip.getEquipId());
        if (se.getQuality() == Constant.Quality.white) {
            return false;
        }

        StaticEquipQualityExtra extra = StaticPropDataMgr.getQualityMap().get(se.getQuality());
        int maxLv = extra.getMaxLv();
        for (Turple<Integer, Integer> skill : equip.getAttrAndLv()) {
            if (skill.getB() < maxLv) {
                return true;
            }
        }
        return false;
    }

}
