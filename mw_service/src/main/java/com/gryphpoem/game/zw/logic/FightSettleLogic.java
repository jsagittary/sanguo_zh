package com.gryphpoem.game.zw.logic;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.TechConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCombat;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.HeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author TanDonghai
 * @ClassName FightSettleLogic.java
 * @Description 战斗结算相关逻辑处理
 * @date 创建时间：2017年5月22日 下午5:20:13
 */
@Component
public class FightSettleLogic {

    @Autowired
    private HeroService heroService;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 采集点战斗将领经验奖励逻辑，只计算战斗经验和军工
     *
     * @param player
     * @param forces
     */
    public List<RptHero> mineFightHeroExpReward(Player player, List<Force> forces) {
        List<RptHero> rptList = new ArrayList<>();
        if (!CheckNull.isEmpty(forces)) {
            Hero hero;
            int addExp;
            int roleLv = player.lord.getLevel();
            int count;
            ChangeInfo info = ChangeInfo.newIns();
            for (Force force : forces) {
                hero = player.heros.get(force.id);
                if (null == hero) {
                    LogUtil.error("进攻采集点将领经验结算，将领不存在, roleId:", player.roleId, ", heroId:", force.id);
                    continue;
                }

                // 将领经验 = （杀敌数+损兵数）/2
                addExp = (force.killed + force.totalLost) / 2;
                addExp = heroService.adaptHeroAddExp(player, addExp);

                if (addExp > 0) {
                    addExp = heroService.addHeroExp(hero, addExp, roleLv, player);
                }

                count = force.totalLost;
                // 科技加成
                double add = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_26);
                if (add > 0) {
                    count += count * add;
                }

                if (count > 0) {
                    // 增加玩家军功
                    rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.EXPLOIT, count,
                            AwardFrom.MINE_FIGHT);
                    info.addChangeType(AwardType.MONEY, AwardType.Money.EXPLOIT);
                }

                rptList.add(PbHelper.createRptHero(Constant.Role.PLAYER, force.killed, count, force.id,
                        player.lord.getNick(), hero.getLevel(), addExp, force.lost, hero.getDecorated()));
            }
            rewardDataManager.syncRoleResChanged(player, info);
        }
        return rptList;
    }

    /**
     * 进攻流寇，将领经验奖励结算
     *
     * @param player
     * @param forces
     * @return 返回参战将领对应的战报将领对象集合
     */
    public List<RptHero> banditFightHeroExpReward(Player player, List<Force> forces) {
        List<RptHero> rptList = new ArrayList<>();
        if (null == player || CheckNull.isEmpty(forces)) {
            return rptList;
        }
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        Hero hero;
        int addExp;
        int roleLv = player.lord.getLevel();
        for (Force force : forces) {
            hero = player.heros.get(force.id);
            if (null == hero) {
                LogUtil.error("进攻流寇将领经验结算，将领不存在, roleId:", player.roleId, ", heroId:", force.id);
                continue;
            }

            // 将领经验：将领经验 = （杀敌数+损兵数）/2
            addExp = (force.killed + force.totalLost) / 2;
            addExp = heroService.adaptHeroAddExp(player, addExp);
            if (addExp > 0) {
                addExp *= num; // 活动翻倍
                addExp = heroService.addHeroExp(hero, addExp, roleLv, player);
            }

            rptList.add(PbHelper.createRptHero(Constant.Role.PLAYER, force.killed, 0, force.id, player.lord.getNick(),
                    hero.getLevel(), addExp, force.lost, hero.getDecorated()));
        }
        return rptList;
    }

    /**
     * 副本将领经验奖励结算逻辑
     *
     * @param player
     * @param forces       进攻方将领
     * @param staticCombat 副本配置信息
     * @param wipe         是否是扫荡
     * @param win          是否胜利
     */
    public List<RptHero> combatFightHeroExpReward(Player player, List<Force> forces, StaticCombat staticCombat,
                                                  boolean wipe, boolean win) {
        List<RptHero> rptList = new ArrayList<>();
        if (null == player || CheckNull.isEmpty(forces)) {
            return rptList;
        }
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        Hero hero;
        int addExp = 0;
        int roleLv = player.lord.getLevel();
        for (Force force : forces) {
            hero = player.heros.get(force.id);
            if (null == hero) {
                LogUtil.error("副本将领经验结算，将领不存在, roleId:", player.roleId, ", heroId:", force.id);
                continue;
            }

            try {
                if (null == staticCombat) {
                    LogUtil.error("副本将领经验结算，副本配置信息为空, roleId:", player.roleId);
                    continue;
                }
                // 普通副本只有赢了才加将领经验
                if ((staticCombat.getType() == Constant.CombatType.type_1 ||
                        staticCombat.getType() == Constant.CombatType.type_3) && !win) {
                    continue;
                }

                // 活动翻倍
                if (staticCombat.getType() == Constant.CombatType.type_1 ||
                        staticCombat.getType() == Constant.CombatType.type_3) {
                    if (wipe) {// 扫荡并胜利：将领经验 = 关卡NPC兵力 / 将领个数
                        addExp = staticCombat.getExp(); // / forces.size()
                    } else {// 将领经验 = （杀敌数+损兵数）/2
                        addExp = (force.killed + force.totalLost) / 2;
                        int max = staticCombat.getExp();
                        addExp = max < addExp ? max : addExp;// 上限控制
                    }

                } else {
                    if (wipe & win) {// 扫荡并胜利：将领经验 = 关卡NPC兵力 / 将领个数
                        addExp = staticCombat.getExp(); // / forces.size()
                    } else {// 将领经验 = （杀敌数+损兵数）/2
                        addExp = (force.killed + force.totalLost) / 2;
                        int max = staticCombat.getExp();
                        addExp = max < addExp ? max : addExp;// 上限控制
                    }
                }

                if (hero.getLevel() >= roleLv) {
                    // 将领等级不超过玩家等级，因此将领等级达到玩家等级后将不再获得经验
                    addExp = 0;
                }
                LogUtil.debug("before 副本将领经验奖励结算逻辑===" + addExp);
                if (addExp > 0) {
                    addExp = heroService.adaptHeroAddExp(player, addExp);
                    addExp *= num; // 活动翻倍
                    addExp = heroService.addHeroExp(hero, addExp, roleLv, player);
                }
                LogUtil.debug("end 副本将领经验奖励结算逻辑===" + addExp);
            } finally {
                rptList.add(PbHelper.createRptHero(Constant.Role.PLAYER, force.killed, 0, force.id, player.lord.getNick(),
                        hero.getLevel(), addExp, force.totalLost, hero.getDecorated()));
            }
        }
        return rptList;
    }

    /**
     * 战火之路,不发送奖励,只记录战报
     *
     * @param player
     * @param forces
     * @return
     */
    public List<RptHero> stoneCombatCreateRptHero(Player player, List<Force> forces) {
        List<RptHero> rptList = new ArrayList<>();
        if (null == player || CheckNull.isEmpty(forces)) {
            return rptList;
        }
        Hero hero;
        for (Force force : forces) {
            hero = player.heros.get(force.id);
            if (null == hero) {
                LogUtil.error("副本将领经验结算，将领不存在, roleId:", player.roleId, ", heroId:", force.id);
                continue;
            }
            rptList.add(PbHelper.createRptHero(Constant.Role.PLAYER, force.killed, 0, force.id, player.lord.getNick(),
                    hero.getLevel(), 0, force.totalLost, hero.getDecorated()));
        }
        return rptList;
    }

    /**
     * 创建无奖励的 RptHero
     *
     * @param forces
     * @return
     */
    public List<RptHero> combatCreateRptHeroNoAward(List<Force> forces) {
        List<RptHero> rptList = new ArrayList<>();

        for (Force force : forces) {
            if (force.roleType == Constant.Role.PLAYER) {
                Player player = playerDataManager.getPlayer(force.ownerId);
                if (player != null) {
                    Hero hero = player.heros.get(force.id);
                    if (null == hero) {
                        LogUtil.error("副本将领经验结算，将领不存在, roleId:", player.roleId, ", heroId:", force.id);
                        continue;
                    }
                    rptList.add(PbHelper.createRptHero(Constant.Role.PLAYER, force.killed, 0, force.id,
                            player.lord.getNick(), hero.getLevel(), 0, force.lost, hero.getDecorated()));
                }
            }
        }
        return rptList;
    }

}
