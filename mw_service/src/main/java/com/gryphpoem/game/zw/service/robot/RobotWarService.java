package com.gryphpoem.game.zw.service.robot;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBanditDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.ArmyService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description 机器人战争相关服务类
 * @author TanDonghai
 * @date 创建时间：2017年10月20日 下午6:00:27
 *
 */
@Service
public class RobotWarService {
    @Autowired
    private ArmyService armyService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    /**
     * 自动攻击流寇
     * 
     * @param player
     */
    public void autoAttackBandit(Player player) {
        int pos = getBandit(player);
        if (pos <= 0) {
            return;
        }

        Hero hero;
        int armCount = 0;
        boolean noHero = true;
        List<TwoInt> form = new ArrayList<>();
        for (int heroId : player.heroBattle) {
            hero = player.heros.get(heroId);
            if (null != hero) {
                noHero = false;
                if (hero.getCount() == 0) {
                    // 没有兵力，补兵
                    try {
                        armyService.autoAddArmySingle(player, hero);
                    } catch (MwException e) {
                        LogUtil.robot(e, "机器人将领自动补兵出错, roleId:", player.roleId, ", heroId:", hero.getHeroId());
                    }
                }

                form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
                armCount += hero.getCount();
            }
        }

        if (noHero || armCount == 0) {
            // 没有将领，或没有兵力，不能出击
            return;
        }

        int now = TimeHelper.getCurrentSecond();
        int marchTime = worldService.marchTime(player, pos);
        int banditId = worldDataManager.getBanditIdByPos(pos);

        // 检查补给
        int needFood = worldService.getNeedFood(marchTime, armCount);
        try {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);
        } catch (MwException e) {
            return;
        }

        Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_ATK_BANDIT, pos, ArmyConstant.ARMY_STATE_MARCH,
                form, marchTime - 1, now + marchTime - 1, player.getDressUp());
        army.setTargetId(banditId);
        army.setLordId(player.roleId);
        army.setOriginPos(player.lord.getPos());
        
        player.armys.put(army.getKeyId(), army);

        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);
        // 改变行军状态
        for (TwoInt twoInt : form) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        LogUtil.robot("机器人攻击流寇, robot:" + player.roleId + ", begin:" + player.lord.getPos() + ", target:" + pos);

        // 区域变化推送
        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, player.roleId,
                Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
    }

    /**
     * 获取玩家周围的流寇
     * 
     * @param player
     * @return 返回流寇的坐标，如果未找到，返回-1
     */
    private int getBandit(Player player) {
        int banditPos = -1;
        int area = MapHelper.getAreaIdByPos(player.lord.getPos());
        Map<Integer, Integer> banditMap = new HashMap<>();
        Set<Integer> blocks = MapHelper.getBlockInArea(area);
        if (!CheckNull.isEmpty(blocks)) {
            for (Integer block : blocks) {
                Map<Integer, Integer> map = worldDataManager.getBanditInBlock(block);
                if (!CheckNull.isEmpty(map)) {
                    banditMap.putAll(map);
                }
            }
        }

        // 计算玩家可以攻击的流寇最高等级
        Integer banditLv = player.trophy.get(TrophyConstant.TROPHY_1);
        int maxLv = (banditLv != null ? banditLv : 0) + 1;

        // 如果当前有打流寇的任务，优先打任务等级的流寇
        int taskLv = getTaskBanditLv(player);
        if (taskLv > 0 && taskLv <= maxLv) {
            // 找到任务指定的等级中距离最近的流寇位置
            banditPos = banditMap.keySet().stream().filter(p -> isTaskLvBandit(p, taskLv))
                    .min((p1, p2) -> banditCompare(player, p1, p2, false)).orElse(-1);
        } else {
            // 找到离自己最近、等级最低的流寇位置
            banditPos = banditMap.keySet().stream().filter(p -> canFightBandit(p, maxLv))
                    .min((p1, p2) -> banditCompare(player, p1, p2, true)).orElse(-1);
        }

        return banditPos;
    }

    /**
     * 获取玩家当前需要完成的流寇任务的流寇等级
     * 
     * @param player
     * @return 返回任务需要攻打的流寇等级，如果未找到，返回-1
     */
    private int getTaskBanditLv(Player player) {
        Optional<StaticTask> task = player.curMajorTaskIds.stream().map(StaticTaskDataMgr::getTaskById)
                .filter(t -> t.getCond() == TaskType.COND_BANDIT_LV_CNT)
                .min((t1, t2) -> (t1.getCondId() - t2.getCondId()));
        return task.isPresent() ? task.get().getCondId() : -1;
    }

    /**
     * 是否是指定任务等级的流寇
     * 
     * @param banditPos
     * @param taskLv
     * @return
     */
    private boolean isTaskLvBandit(int banditPos, int taskLv) {
        StaticBandit bandit = getBanditByPos(banditPos);
        return null != bandit && bandit.getLv() == taskLv;
    }

    /**
     * 判断该流寇玩家是否可以攻击
     * 
     * @param banditPos
     * @param maxLv
     * @return
     */
    private boolean canFightBandit(int banditPos, int maxLv) {
        StaticBandit bandit = getBanditByPos(banditPos);
        return null != bandit && bandit.getLv() <= maxLv;
    }

    /**
     * 流寇优先级比较器
     * 
     * @param player
     * @param p1
     * @param p2
     * @param advance 距离相等的情况下，是否需要进行等级比较等更多的比较
     * @return
     */
    private int banditCompare(Player player, int p1, int p2, boolean advance) {
        // 优先距离进的
        int rolePos = player.lord.getPos();
        int d1 = MapHelper.calcDistance(rolePos, p1);
        int d2 = MapHelper.calcDistance(rolePos, p2);
        if (d1 < d2) {
            return -1;
        } else if (d1 > d2) {
            return 1;
        } else if (advance) {
            // 相同距离下，优先等级低的
            StaticBandit b1 = getBanditByPos(p1);
            StaticBandit b2 = getBanditByPos(p2);
            // 找不到配置信息的，往后排
            if (null == b1 || null == b2) {
                if (null == b1) {
                    return 1;
                } else {
                    return -1;
                }
            }

            if (b1.getLv() < b2.getLv()) {
                return -1;
            } else if (b1.getLv() > b2.getLv()) {
                return 1;
            }
        }

        return 0;
    }

    private StaticBandit getBanditByPos(int pos) {
        int banditId = worldDataManager.getBanditIdByPos(pos);
        return StaticBanditDataMgr.getBanditById(banditId);
    }
}
