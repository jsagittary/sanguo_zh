package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1.AutoAddArmyRs;
import com.gryphpoem.game.zw.pb.GamePb2.GetArmyRs;
import com.gryphpoem.game.zw.pb.GamePb2.ReplenishRq;
import com.gryphpoem.game.zw.pb.GamePb2.ReplenishRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName ArmyService.java
 * @Description 军队相关
 * @date 创建时间：2017年3月30日 下午4:59:13
 */
@Service
public class ArmyService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private WorldService worldService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private AirshipService airshipService;

    /**
     * 获取行军队列
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetArmyRs getArmy(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 返回协议
        GetArmyRs.Builder builder = GetArmyRs.newBuilder();

        for (Army army : player.armys.values()) {
            builder.addArmy(PbHelper.createArmyPb(army, false));
        }
        return builder.build();
    }

    /**
     * 补兵
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public ReplenishRs replenish(long roleId, ReplenishRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        List<Integer> heroIdList = req.getHeroIdList();
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "补兵没有传入将领id, roleId:", roleId);
        }

        // 检查传入的将领id是否正确
        for (Integer heroId : heroIdList) {

            if (!player.heros.containsKey(heroId)) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "补兵，玩家没有这个将领, roleId:", roleId,
                        "heroId:" + heroId);
            }
            Hero hero = player.heros.get(heroId);
            if (hero == null) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "补兵，玩家没有这个将领, roleId:", roleId,
                        "heroId:" + heroId);
            }
            if (hero.getState() != ArmyConstant.ARMY_STATE_IDLE) {
                throw new MwException(GameError.HERO_BREAK_BATLLE.getCode(), "补兵，将领不在空闲状态, roleId:", roleId,
                        "heroId:" + heroId, ", state:", hero.getState());
            }
            if (!player.isOnBattleHero(heroId) && !player.isOnAcqHero(heroId) && !player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "补兵，将领未上阵, roleId:", roleId,
                        "heroId:" + heroId);
            }

        }

        // 返回协议
        ReplenishRs.Builder builder = ReplenishRs.newBuilder();
        int max; // 记录将领带兵量上限
        int need; // 记录需要补兵的数量
        int add; // 记录最终真实补兵数量
        int total; // 记录玩家兵营中当前拥有的兵力
        Hero hero;
        int armType; // 记录将领所属兵种
        StaticHero staticHero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            max = hero.getAttr()[HeroConstant.ATTR_LEAD];
            if (hero.getCount() >= max) {
                LogUtil.debug("兵力已满，跳过max=" + max, ",roleId=", roleId);
                // continue;// 兵力已满，跳过
            }

            // 获取将领对应类型的兵力
            staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
            if (null == staticHero) {
                LogUtil.error("玩家补兵，将领未配置, roleId:", roleId, ", heroId:", heroId);
                continue;
            }

            armType = staticHero.getType();
            total = playerDataManager.getArmCount(player.resource, armType);
            if (total <= 0) {
                // 补兵时兵力不足
                activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_REPLENISH_INSUFFICIENT, player);
                LogUtil.debug("兵不够补，跳过max=" + max, ",roleId=", roleId, ",armType=", armType);
                if (heroIdList.size() == 1) {
                    throw new MwException(GameError.ARM_COUNT_ERROR.getCode(), "补兵, 兵力不足, roleId:", roleId, "heroId:",
                            heroId);
                }
                // continue;
            }

            need = max - hero.getCount();
            if (total <= need) {
                add = total;
            } else {
                add = need;
            }

            if (hero.getCount() >= max || total <= 0) {
                add = 0;
            }

            // 扣除兵力
            if (add > 0) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.ARMY, armType, add,
                        AwardFrom.REPLENISH);
                // rewardDataManager.subArmyResource(player, armType, add, AwardFrom.REPLENISH, "补兵");
                // 增加武将兵力
                hero.setCount(hero.getCount() + add);
                // LogLordHelper.heroArm(AwardFrom.REPLENISH, player.account, player.lord, heroId, hero.getCount(), add, armType,
                //         Constant.ACTION_ADD);

                // 上报玩家兵力变化
//                LogLordHelper.playerArm(
//                        AwardFrom.REPLENISH,
//                        player,
//                        armType,
//                        Constant.ACTION_ADD,
//                        add
//                );
            }

            builder.addHero(PbHelper.createTwoIntPb(heroId, hero.getCount()));
        }
        return builder.build();
    }

    /**
     * 自动补兵开关
     *
     * @param roleId
     * @param status
     * @return
     * @throws MwException
     */
    public AutoAddArmyRs autoAddArmy(Long roleId, int status) throws MwException {
        status = status > 0 ? 1 : 0;
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        player.common.setAutoArmy(status);
        AutoAddArmyRs.Builder builder = AutoAddArmyRs.newBuilder();
        if (status > 0) {
            playerDataManager.autoAddArmy(player);
        }
        builder.setStatus(status);
        return builder.build();
    }

    /**
     * 单独将领自动补兵
     *
     * @param player
     * @param hero
     * @throws MwException
     */
    public void autoAddArmySingle(Player player, Hero hero) throws MwException {
        if (hero == null) {
            return;
        }
        int max = hero.getAttr()[HeroConstant.ATTR_LEAD];
        if (hero.getCount() >= max) {
            return;// 兵力已满，跳过
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (null == staticHero) {
            return;
        }
        int armType = staticHero.getType();// 获取将领对应类型的兵力
        int total = playerDataManager.getArmCount(player.resource, armType);
        if (total <= 0) {
            return;// 没有此类兵种
        }
        int need = max - hero.getCount();
        int add = total <= need ? total : need; // 记录最终真实补兵数量
        // 扣除兵力
        rewardDataManager.subArmyResource(player, armType, add, AwardFrom.REPLENISH);
        // 增加武将兵力
        hero.setCount(hero.getCount() + add);
        // LogLordHelper.heroArm(AwardFrom.REPLENISH, player.account, player.lord, hero.getHeroId(), hero.getCount(), add, armType,
        //         Constant.ACTION_ADD);

        // 上报玩家兵力变化
//        LogLordHelper.playerArm(
//                AwardFrom.REPLENISH,
//                player, armType,
//                Constant.ACTION_ADD,
//                add
//        );
    }

    /**
     * 创建部队并检查和扣除行军消耗
     *
     * @param player
     * @param pos        目标位置
     * @param heroIdList
     * @param now
     * @param armyType
     * @return
     * @throws MwException
     */
    public Army checkAndcreateArmy(Player player, int pos, List<Integer> heroIdList, int now, int armyType)
            throws MwException {
        int armCount = 0;
        List<TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
            armCount += hero.getCount();
        }
        int marchTime = worldService.marchTime(player, pos);
        marchTime = airshipService.getAirShipMarchTime(player, marchTime);
        int needFood = worldService.checkMarchFood(player, marchTime, armCount); // 检查补给
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);
        Army army = new Army(player.maxKey(), armyType, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime,
                now + marchTime, player.getDressUp());
        army.setLordId(player.roleId);
        army.setOriginPos(player.lord.getPos());
        // 名城buffer记录
        army.setOriginCity(worldDataManager.checkCityBuffer(player.lord.getPos()));
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));
        return army;
    }

    /**
     * 添加行军线并修改将领状态
     *
     * @param player
     * @param army
     * @param pos
     */
    public void addMarchAndChangeHeroState(Player player, Army army, int pos) {
        player.armys.put(army.getKeyId(), army);
        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);
        // 改变行军状态
        for (TwoInt h : army.getHero()) {
            int heroId = h.getV1();
            Hero hero = player.heros.get(heroId);
            if (hero != null) hero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }
        // 区域变化推送
        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
    }

    public void updateServerArmyCount(int type, int count) throws MwException {
        //更新全服玩家兵力
        for (Map.Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
            Player player0 = entry.getValue();
            if (0 == type) {
                rewardDataManager.sendRewardSignle(player0, AwardType.ARMY, AwardType.Army.FACTORY_1_ARM, count, AwardFrom.DO_SOME);
                rewardDataManager.sendRewardSignle(player0, AwardType.ARMY, AwardType.Army.FACTORY_2_ARM, count, AwardFrom.DO_SOME);
                rewardDataManager.sendRewardSignle(player0, AwardType.ARMY, AwardType.Army.FACTORY_3_ARM, count, AwardFrom.DO_SOME);
            } else if (1 == type) {
                rewardDataManager.sendRewardSignle(player0, AwardType.ARMY, AwardType.Army.FACTORY_1_ARM, count, AwardFrom.DO_SOME);
            } else if (2 == type) {
                rewardDataManager.sendRewardSignle(player0, AwardType.ARMY, AwardType.Army.FACTORY_2_ARM, count, AwardFrom.DO_SOME);
            } else if (3 == type) {
                rewardDataManager.sendRewardSignle(player0, AwardType.ARMY, AwardType.Army.FACTORY_3_ARM, count, AwardFrom.DO_SOME);
            }
            for (int heroId : player0.heroBattle) {
                if (heroId > 0) {
                    Hero hero0 = player0.heros.get(heroId);
                    if (Objects.nonNull(hero0)) {
                        autoAddArmySingle(player0, hero0);
                    }
                }
            }
        }
    }
}
