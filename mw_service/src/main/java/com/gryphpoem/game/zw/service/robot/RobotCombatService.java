package com.gryphpoem.game.zw.service.robot;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCombatDataMgr;
import com.gryphpoem.game.zw.pb.GamePb2.DoCombatRs;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.Constant.CombatType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CombatFb;
import com.gryphpoem.game.zw.resource.domain.s.StaticCombat;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.CombatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author TanDonghai
 * @Description 机器人自动进攻副本服务类
 * @date 创建时间：2017年10月20日 下午3:46:43
 */
@Service
public class RobotCombatService {

    @Autowired
    private CombatService combatService;

    /**
     * 自动攻打副本，返回本次攻打是否成功
     *
     * @param player
     * @return
     */
    public boolean autoDoCombat(Player player) {
        // 检查是否有上阵将领，没有则跳过
        if (!player.isOnBattle()) {
            return false;
        }

        // 计算下一个可以攻打的副本
        StaticCombat nextCombat = getNextCombat(player);
        if (null == nextCombat) {
            return false;
        }

        // 检查是否有体力，没有体力则跳过
        if (player.lord.getPower() < nextCombat.getCnt()) {
            return false;
        }

        //  如果体力很多，但是又一直打不过当前的副本，是否需要先扫荡当前的已通关副本，这个作为后期优化内容

        try {
            // 攻打副本逻辑
            DoCombatRs rs = combatService.doCombat(player.roleId, nextCombat.getCombatId(), false,
                    getPlayerAllOnBattleHeroIds(player));
            if (rs.getResult() > 0) {
                // 如果攻打胜利，返回true
                return true;
            }
        } catch (MwException e) {
            LogUtil.robot(e, "自动攻打副本出错, roleId:", player.roleId, ", combatId:", nextCombat.getCombatId());
        }
        return false;
    }

    /**
     * 获取玩家所有上阵将领的id
     *
     * @param player
     * @return
     */
    private List<Integer> getPlayerAllOnBattleHeroIds(Player player) {
        List<Integer> list = new ArrayList<>();
//        for (int heroId : player.heroBattle) {
//            if (heroId > 0 && player.heros.get(heroId) != null) {
//                list.add(heroId);
//            }
//        }
        return list;
    }

    /**
     * 获取下一个可以攻打的副本
     *
     * @param player
     * @return
     */
    private StaticCombat getNextCombat(Player player) {
        Optional<StaticCombat> minOpenedCombat = StaticCombatDataMgr.getCombatMap().values().stream()
                .filter(c -> combatCanFight(player, c)).min((c1, c2) -> (c1.getCombatId() - c2.getCombatId()));
        return minOpenedCombat.isPresent() ? minOpenedCombat.get() : null;
    }

    /**
     * 判断副本是否可以攻打
     *
     * @param player
     * @param combat
     * @return
     */
    private boolean combatCanFight(Player player, StaticCombat combat) {
        // 普通副本
        if (combat.getType() == Constant.CombatType.type_1) {
            // 已通过的普通副本，不能再攻打
            if (player.combats.containsKey(combat.getCombatId())) {
                return false;
            } else {
                // 前置关卡已通过，则可以攻打
                return combat.getPreId() <= 0 || player.combats.containsKey(combat.getPreId());
            }
        } else {// 高级副本
            CombatFb fb = player.combatFb.get(combat.getCombatId());
            if (null == fb) {
                return combat.getPreId() <= 0 || player.combats.containsKey(combat.getPreId());
            } else {
                return specialCombatCanFreeFight(fb, combat.getType());
            }
        }
    }

    /**
     * 判断高级副本是否可以免费攻打
     *
     * @param combatFb
     * @param combatType
     * @return
     */
    private boolean specialCombatCanFreeFight(CombatFb combatFb, int combatType) {
        if (null == combatFb || combatFb.getStatus() > 0) {
            return false;
        }

        boolean result = false;
        switch (combatType) {
            case CombatType.type_2:
                result = TimeHelper.getCurrentSecond() <= combatFb.getEndTime() && combatFb.getCnt() > 0;
                break;

            case CombatType.type_3:
            case CombatType.type_7:
                result = combatFb.getCnt() > 0;
                break;

            case CombatType.type_4:
                result = combatFb.getGain() <= 0;
                break;

            case CombatType.type_5:
            case CombatType.type_6:
            case CombatType.type_8:
                result = true;
                break;

            default:
                break;
        }
        return result;
    }

}
