package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWarPlaneDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.BuildingExt;
import com.gryphpoem.game.zw.resource.domain.p.MentorInfo;
import com.gryphpoem.game.zw.resource.domain.p.MentorSkill;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneSearch;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneTransform;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneUpgrade;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.chat.Chat;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-12 11:09
 * @description:
 * @modified By:
 */
@Component public class WarPlaneDataManager {


    @Autowired private BuildingDataManager buildingDataManager;

    @Autowired private RewardDataManager rewardDataManager;

    @Autowired private ChatDataManager chatDataManager;

    @Autowired private TechDataManager techDataManager;

    @Autowired private MentorDataManager mentorDataManager;

    private GlobalPlane globalPlane;            // 战机公共数据

    public GlobalPlane getGlobalPlane() {
        if (CheckNull.isNull(globalPlane)) {
            globalPlane = new GlobalPlane();
        }
        return globalPlane;
    }

    /**
     * 改造时检测战机碎片可否转化
     *
     * @param warPlane
     * @param player
     * @param change
     */
    public void checkChipTransform(WarPlane warPlane, Player player, ChangeInfo change) {

        if (CheckNull.isNull(warPlane) || CheckNull.isNull(player)) {
            return;
        }

        int type = warPlane.getType();

        StaticPlaneTransform planeTransform = StaticWarPlaneDataMgr.getPlaneTransformByType(type);
        if (CheckNull.isNull(planeTransform)) {
            return;
        }

        List<List<Integer>> transform = planeTransform.getTransform();
        int chipId = planeTransform.getId();

        PlaneChip planeChip = player.getPlaneChip(chipId);
        int cnt = planeChip.getCnt();
        if (CheckNull.isNull(planeChip) || cnt <= 0 || CheckNull.isEmpty(transform)) {
            return;
        }

        // 当前类型最大等级的战机
        StaticPlaneUpgrade maxLv = StaticWarPlaneDataMgr.getPlaneMaxLvByFilter(
                plane -> type == plane.getPlaneType() && plane.getNextId() == 0 && CheckNull
                        .isEmpty(plane.getReformNeed()));
        if (maxLv.getPlaneId() != warPlane.getPlaneId()) {
            return;
        }

        try {
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.PLANE_CHIP, chipId, cnt,
                    AwardFrom.PLANE_CHIP_TRANSFORM_BY_REMOULD, false);
            change.addChangeType(AwardType.PLANE_CHIP, chipId);
            for (List<Integer> trans : transform) {
                rewardDataManager.addAwardSignle(player, trans, cnt, AwardFrom.PLANE_CHIP_TRANSFORM);
                change.addChangeType(trans.get(0), trans.get(1));
            }
        } catch (MwException e) {
            LogUtil.error(e);
        }

    }

    /**
     * 增加战机经验
     *
     * @param plane  战机
     * @param addExp 添加的经验
     * @param maxLv  最大等级
     * @param player 角色对象
     * @param sPlane 战机配置
     * @return
     */
    public int addPlaneExp(WarPlane plane, int addExp, int maxLv, Player player, StaticPlaneUpgrade sPlane) {
        int add = 0;
        if (CheckNull.isNull(plane) || addExp <= 0 || CheckNull.isNull(sPlane)) {
            return add;
        }
        int preLv = plane.getLevel(); // 之前的战斗力

        while (addExp > 0 && plane.getLevel() < maxLv) {
            int need = StaticWarPlaneDataMgr
                    .getExpByQuality(sPlane.getQuality(), sPlane.getQualityLevel(), plane.getLevel());
            if (need > 0) { //
                if (addExp + plane.getExp() >= need) { // 可加经验 + 当前经验 >= 升到下一级的经验 可升级
                    add += need - plane.getExp();
                    addExp -= need - plane.getExp();
                    plane.levelUp();
                } else { //
                    add += addExp;
                    plane.setExp(plane.getExp() + addExp);
                    addExp = 0;
                }
            } else {
                // 设置经验为本级经验上限
                int max = StaticWarPlaneDataMgr
                        .getExpByQuality(sPlane.getQuality(), sPlane.getQualityLevel(), plane.getLevel() - 1);
                add += max - plane.getExp();
                plane.setExp(max);
                break;
            }
        }

        // 战机有将领佩戴, 并且等级发生变化
        int heroId = plane.getHeroId();
        if (heroId >= 0 && preLv != plane.getLevel()) {
            Hero hero = player.heros.get(heroId);
            if (!CheckNull.isNull(hero)) {
                CalculateUtil.processAttr(player, hero);
            }
        }
        return add;
    }

    /**
     * 战机寻访
     *
     * @param player
     * @param searchType
     * @return 抽取到的奖励, 返回null为配置错误
     * @throws MwException
     */
    public List<CommonPb.Award> doSearchPlane(Player player, int searchType) throws MwException {
        List<CommonPb.Award> awards = new ArrayList<>();
        List<StaticPlaneSearch> planeSearches = StaticWarPlaneDataMgr.getPlaneSearchesByType(searchType);
        if (!CheckNull.isEmpty(planeSearches)) {
            // 寻访结果
            StaticPlaneSearch search = RandomUtil.getWeightByList(planeSearches, s -> s.getWeight());
            List<List<Integer>> sr = search.getReward();
            if (CheckNull.isNull(search) || CheckNull.isEmpty(sr)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "寻访配置错误   searchType:", searchType);
            }
            GlobalPlane globalPlane = getGlobalPlane();
            sr.forEach(award -> {
                int type = award.get(0);
                if (type == AwardType.PLANE) {
                    int planeId = award.get(1);
                    StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
                    if (!CheckNull.isNull(sPlaneUpgrade)) {
                        int quality = sPlaneUpgrade.getQuality();
                        if (quality >= PlaneConstant.PLANE_SEARCH_CHAT_QUALITY) { // 橙色品质的跑马灯
                            Chat chat = chatDataManager
                                    .sendSysChat(ChatConst.CHAT_PLANE_SEARCH, player.lord.getCamp(), 0,
                                            player.lord.getNick(), planeId);
                            if (!CheckNull.isNull(globalPlane) && !CheckNull.isNull(chat)) {
                                globalPlane.setLastChat(chat);
                            }
                        }
                    }
                }
                awards.add(rewardDataManager.addAwardSignle(player, award, AwardFrom.PLANE_SEARCH_AWARD));
            });
            return awards;
        }
        return null;
    }

    // /**
    //  * 战机替换
    //  * @param upPlane 预上阵战机
    //  * @param swapPlane 被替换战机
    //  * @param hero 预上阵的将领
    //  * @param swapHero 被替换的将领
    //  * @throws MwException
    //  */
    // @Deprecated
    // public void planeSwap(WarPlane upPlane, WarPlane swapPlane, Hero hero, Hero swapHero) throws MwException {
    //     if (!CheckNull.isNull(swapHero) && upPlane.getHeroId() == swapHero.getHeroId()) { // 将预上阵战机下阵
    //         if (swapHero.downPlane(upPlane.getPlaneId())) {
    //             downBattle(upPlane, swapHero);
    //         }
    //     }
    //     if (!CheckNull.isNull(swapPlane) && swapPlane.getHeroId() == hero.getHeroId()) { // 将被替换战机下阵
    //         downBattle(swapPlane, hero);
    //     }
    //     upBattle(upPlane, hero); // 将预上阵战机上阵
    //     if (!CheckNull.isNull(swapHero) && upPlane.getHeroId() == swapHero.getHeroId() && !CheckNull.isNull(swapPlane)) {
    //         upBattle(swapPlane, swapHero);
    //     }
    // }
    //
    // /**
    //  * 战机上阵
    //  * @param plane
    //  * @param hero
    //  */
    // private void upBattle(WarPlane plane, Hero hero) throws MwException {
    //     if (!CheckNull.isNull(plane) && !CheckNull.isNull(hero)) {
    //         if (hero.getWarPlanes().size() >= HeroConstant.HERO_WAR_PLANE_NUM) {
    //             throw new MwException(GameError.SWAP_PLANE_ERROR.getCode(), "上阵战机超出将领可装战机总数", ", num:", hero.getWarPlanes().size());
    //         }
    //         if (hero.upPlane(plane.getPlaneId())) {
    //             plane.upBattle(hero, hero.getPos());
    //         }
    //     }
    // }
    //
    // /**
    //  * 战机下阵
    //  * @param plane
    //  * @param hero
    //  */
    // private void downBattle(WarPlane plane, Hero hero) {
    //     if (!CheckNull.isNull(plane) && !CheckNull.isNull(hero) && hero.downPlane(plane.getPlaneId())) {
    //         plane.downBattle(hero);
    //     }
    // }

    /**
     * 是否已过寻访奖励时间
     *
     * @param player 角色对象
     * @return
     */
    public boolean isAfterSearchAwardTime(Player player) throws MwException {
        boolean isAfter = false;
        // 解锁判断
        if (!buildingDataManager.checkBuildingLock(player, BuildingType.AIR_BASE)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                    "获取空基地信息时，建筑未解锁, roleId:" + player.roleId + ",buildingPos:" + BuildingType.AIR_BASE);
        }
        // 空军基地扩展
        BuildingExt buildingExt = player.buildingExts.get(BuildingType.AIR_BASE);
        int unLockTime = buildingExt.getUnLockTime(); // 解锁时间
        int now = TimeHelper.getCurrentSecond();
        if (unLockTime > 0 && now > unLockTime + PlaneConstant.PLANE_FACTORY_AWARD_TIME.get(0)) {
            isAfter = true;
        }
        return isAfter;
    }

    /**
     * 合成战机
     *
     * @param init
     */
    public WarPlane createPlane(StaticPlaneInit init) {
        WarPlane plane = new WarPlane();
        plane.setType(init.getPlaneType());
        plane.setHeroId(0);
        plane.setExp(0);
        plane.setPos(0);
        plane.setState(PlaneConstant.PLANE_STATE_IDLE);
        plane.setLevel(1);
        plane.setPlaneId(init.getPlaneId());
        return plane;
    }

    /**
     * 检验寻访方式
     *
     * @param searchType
     * @return
     */
    public boolean realSearchType(int searchType) {
        return searchType == PlaneConstant.SEARCH_TYPE_NORMAL || searchType == PlaneConstant.SEARCH_TYPE_SUPER || searchType == PlaneConstant.SEARCH_TYPE_LIMIT;
    }

    /**
     * 检验寻访次数
     *
     * @param countType
     * @return
     */
    public boolean realCountType(int countType) {
        return countType == PlaneConstant.COUNT_TYPE_ONE || countType == PlaneConstant.COUNT_TYPE_TEN;
    }

    /**
     * 检验消耗类型
     *
     * @param costType
     * @return
     */
    public boolean realSearchCostType(int costType) {
        return costType == PlaneConstant.SEARCH_COST_FREE || costType == PlaneConstant.SEARCH_COST_PROP
                || costType == PlaneConstant.SEARCH_COST_GOLD;
    }

    /**
     * 创建战机的Common信息
     *
     * @param plane
     * @param player
     * @return
     */
    public CommonPb.WarPlane createWarPlanePb(WarPlane plane, Player player) {
        MentorInfo info = player.getMentorInfo();
        CommonPb.WarPlane.Builder builder = CommonPb.WarPlane.newBuilder();
        builder.setPlaneId(plane.getPlaneId());
        builder.setLevel(plane.getLevel());
        builder.setExp(plane.getExp());
        builder.setPos(plane.getPos());
        builder.setBattlePos(plane.getBattlePos());
        StaticPlaneUpgrade planeUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(plane.getPlaneId());
        if (!CheckNull.isNull(planeUpgrade)) {
            builder.setQuality(planeUpgrade.getQuality());
            builder.setQualityLv(planeUpgrade.getQualityLevel());
            builder.setSkillId(planeUpgrade.getSkillId());
        }
        StaticPlaneInit sPlaneInit = StaticWarPlaneDataMgr.getPlaneInitByType(plane.getType());
        if (!CheckNull.isNull(sPlaneInit)) {
            int skillType = sPlaneInit.getSkillType();
            MentorSkill skill = info.getSkillMap().get(skillType);
            if (!CheckNull.isNull(skill)) {
                builder.setIsActivate(skill.isActivate());
            }
        }

        return builder.build();
    }

    /**
     * 获取战机解锁位置
     * @param player
     * @return 战机开放的位置
     */
    public int planeOpenSize(Player player) {
        int cnt = 1;    // 默认开放1号位
        for (List<Integer> unlock : HeroConstant.HERO_WAR_PLANE_UNLOCK) {
            int techType = unlock.get(0);
            int lv = unlock.get(1);
            int techLv = techDataManager.getTechLv(player, techType);
            if (techLv >= lv) { // 指定科技完成, 战机栏位开放
                cnt++;
            }
        }
        return cnt;
    }
}
