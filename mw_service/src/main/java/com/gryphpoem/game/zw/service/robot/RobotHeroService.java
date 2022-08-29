package com.gryphpoem.game.zw.service.robot;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Common;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.service.ArmyService;
import com.gryphpoem.game.zw.service.HeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @Description AI机器人将领相关服务类
 * @author TanDonghai
 * @date 创建时间：2017年10月17日 下午7:42:40
 *
 */
@Service
public class RobotHeroService {

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private HeroService heroService;

    @Autowired
    private ArmyService armyService;

    @Autowired
    private PlayerDataManager playerDataManager;

    public void autoRecruitHero(Player player) {
        // 判断良将招募是否开启
        if (player.lord.getLevel() >= HeroConstant.HERO_SEARCH_ROLE_LV) {
            Common common = player.common;
            // 刷新将领寻访数据
//            heroService.refreshHeroSearchData(common);

//            int now = TimeHelper.getCurrentSecond();
//            // 良将寻访有免费次数，使用免费次数执行良将寻访
//            if (common.getHeroCdTime() <= now) {
//                // 扣除免费次数
//                common.setHeroCdTime(now + HeroConstant.NORMAL_SEARCH_CD);
//
//                try {
//                    heroService.doHeroSearch(player, HeroConstant.SEARCH_TYPE_NORMAL, 0);
//                } catch (MwException e) {
//                    LogUtil.robot(e, "机器人良将寻访出错, common:", common.heroSearchToString());
//                }
//            }
//
//            // 判断神将招募是否开启
//            if (common.getSuperProcess() >= Constant.INT_HUNDRED) {
//                common.setSuperFreeNum(1);
//                try {
//                    heroService.doHeroSearch(player, HeroConstant.SEARCH_TYPE_SUPER, 0);
//                } catch (MwException e) {
//                    LogUtil.robot(e, "机器人神将寻访出错, common:", common.heroSearchToString());
//                }
//            }
        }

        // 检查是否有品质更高的将领未上阵，有则上阵
        checkAndAppointBetterBattleHero(player);
    }

    /**
     * 检查是否有更好的将领可以上阵，有则上阵
     * 
     * @param player
     */
    private void checkAndAppointBetterBattleHero(Player player) {
        Turple<Integer, Integer> maxQualityHero = getMaxQualityFaineantHero(player.heros);
        if (maxQualityHero.getA() > 0) {
            Turple<Integer, Integer> minBattleHero = getMinQualityBattleHero(player);
            // 没有将领上阵，或上阵将领中品质最低的将领不如闲置将领，换上限制将领
            if (minBattleHero.getA() <= 0 || minBattleHero.getB() < maxQualityHero.getB()) {
                int pos = HeroConstant.HERO_BATTLE_1;
                Hero hero = player.heros.get(maxQualityHero.getA());
                if (minBattleHero.getA() > 0) {
                    int battleHeroId = player.heroBattle[minBattleHero.getA()];
                    Hero battleHero = player.heros.get(battleHeroId);
                    heroService.swapHeroEquip(player, hero, battleHero);

                    pos = maxQualityHero.getA();
                    // 将领下阵，pos设置为0
                    battleHero.onBattle(0);

                    // 士兵回营
                    int sub = battleHero.getCount();
                    battleHero.setCount(0);
                    StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(battleHero.getHeroId());
                    if (Objects.nonNull(staticHero)) {
                        int armType = staticHero.getType();// 获取将领对应类型的兵力
                        LogLordHelper.heroArm(AwardFrom.HERO_DOWN, player.account, player.lord, battleHeroId,
                                battleHero.getCount(), -sub, staticHero.getType(), Constant.ACTION_ADD);

                        // 上报玩家兵力变化
                        LogLordHelper.playerArm(
                                AwardFrom.HERO_DOWN,
                                player,
                                armType,
                                Constant.ACTION_ADD,
                                -sub,
                                playerDataManager.getArmCount(player.resource, armType)
                        );
                    }
                    rewardDataManager.modifyArmyResource(player, staticHero.getType(), sub, 0, AwardFrom.HERO_DOWN);

                    // 重新计算并更新将领属性
                    CalculateUtil.processAttr(player, battleHero);
                }

                // 将领上阵
                hero.onBattle(pos);
                // 更新已上阵将领队列信息
                player.heroBattle[pos] = hero.getHeroId();

                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, hero);
                // 上阵将领自动补兵
                try {
                    armyService.autoAddArmySingle(player, hero);
                } catch (MwException e) {
                    LogUtil.robot(e, "机器人将领补兵出错, robot:", player.roleId, ", heroId:", hero.getHeroId());
                }

                taskDataManager.updTask(player, TaskType.COND_HERO_UP, 1, hero.getHeroId());
                CalculateUtil.reCalcFight(player);
                taskDataManager.updTask(player, TaskType.COND_28, 1, hero.getType());
            }
        }
    }

    /**
     * 获取上阵将领中品质最低的将领信息
     * 
     * @param player
     * @return 返回将领的上阵位置和将领的品质，如果当前还没有设置过上阵将领，这两个值将会是-1
     */
    private Turple<Integer, Integer> getMinQualityBattleHero(Player player) {
        Hero hero;
        int pos = -1;
        StaticHero staticHero;
        int minQuality = Integer.MAX_VALUE;
        for (int heroId : player.heroBattle) {
            hero = player.heros.get(heroId);
            // 只处理当前处于闲置的上阵将领
            if (null != hero && hero.isIdle()) {
                staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                if (null != staticHero && staticHero.getQuality() < minQuality) {
                    minQuality = staticHero.getQuality();
                    pos = hero.getPos();
                }
            }
        }
        return new Turple<>(pos, minQuality);
    }

    /**
     * 获取无所事事的将领（未上阵）中品质最高的
     * 
     * @param heros
     * @return 返回将领的heroId和品质，如果没有闲置的将领，这两个值将会是-1
     */
    private Turple<Integer, Integer> getMaxQualityFaineantHero(Map<Integer, Hero> heros) {
        int heroId = -1;
        int maxQuality = -1;
        StaticHero staticHero;
        for (Hero hero : heros.values()) {
            if (hero.isFaineant()) {
                staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                if (null != staticHero && staticHero.getQuality() > maxQuality) {
                    maxQuality = staticHero.getQuality();
                    heroId = hero.getHeroId();
                }
            }
        }
        return new Turple<>(heroId, maxQuality);
    }

}
