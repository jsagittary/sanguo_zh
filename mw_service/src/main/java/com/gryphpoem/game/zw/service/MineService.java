package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @ClassName MineService.java
 * @Description 矿点相关
 * @author TanDonghai
 * @date 创建时间：2017年4月11日 下午6:50:22
 *
 */
@Service
public class MineService {
    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private WorldService worldService;

    @Autowired
    private SolarTermsDataManager solarTermsDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private TechDataManager techDataManager;

    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;

    public void mineCollectTimeLogic() {
        int pos = 0;
        Guard guard;
        int resource;
        int now = TimeHelper.getCurrentSecond();
        Iterator<Guard> its = worldDataManager.getMineGuardMap().values().iterator();
        while (its.hasNext()) {
            guard = its.next();
            try {
                pos = guard.getPos();
                if (guard.getEndTime() <= now) {// 采集时间结束，部队返回
                    // 采集结算
                    resource = calcCollect(guard.getPlayer(), guard.getArmy(), now);
                    // 部队返回
                    worldService.finishCollect(guard, now);
                    // 更新矿点信息
                    updateMine(pos, resource);
                    its.remove();// 移除矿点玩家采集信息
                }
            } catch (Exception e) {
                // 部队返回
                worldService.finishCollect(guard, now);
                its.remove();// 移除矿点玩家采集信息
                LogUtil.error(e, "采集定时任务出错, guard:", guard, "pos:", pos);
            }
        }
    }

    /**
     * 更新矿点信息
     * 
     * @param pos
     * @param resource
     */
    public void updateMine(int pos, int resource) {
        StaticMine sMine = worldDataManager.getMineByPos(pos);
        int time = calcMineGuardTime(worldDataManager.getMineByPos(pos), resource);
        LogUtil.debug("pos:", pos, ", mine:", sMine, ", 剩余数量:", resource, ", 矿点剩余时间:", time);
        if (calcMineGuardTime(worldDataManager.getMineByPos(pos), resource) < Constant.MINE_RM_MIN_TIME) {// 资源剩余时间小于某个时间移除
            // worldDataManager.refreshMine(pos);
            worldDataManager.removeMine(pos);
        } else {
            // 更新矿点剩余资源数
            worldDataManager.putMineResource(pos, resource);
        }
    }

    /**
     * 计算矿点可以采集多久 (秒)
     * 
     * @param sMine
     * @param cnt 资源数量
     * @return
     */
    public static int calcMineGuardTime(StaticMine sMine, int cnt) {
        if (sMine == null) {
            return 0;
        }
        double speed = sMine.getSpeed() / 3600.0;// 每秒的速度
        if (speed == 0) {
            return 0;
        }
        return (int) (cnt / speed);
    }

    /**
     * 计算矿点剩余资源数
     * 
     * @param pos
     * @param now
     * @return
     */
    public int calcMineResource(Player player, int pos, int now) {
        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (null == staticMine) {
            return 0;
        }

        int total = worldDataManager.getMineResource(pos);
        Guard guard = worldDataManager.getGuardByPos(pos);
        int time = now - guard.getBeginTime();
        int heroId = guard.getArmy().getHero().get(0).getV1();
        double speed = collectSpeed(staticMine.getSpeed(), pos, heroId, player);
        int resource = (int) (total - Math.floor(speed * time * 1.0 / Constant.HOUR));
        if (resource < 0) {
            resource = 0;
        }
        return resource;
    }

    /**
     * 校验当前矿点是否是钻石矿
     * @param pos
     * @return
     */
    public boolean checkDiamondMine(int pos) {
        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (CheckNull.isNull(staticMine))
            return true;

        return staticMine.getMineType() == StaticMine.MINE_TYPE_GOLD;
    }

    /**
     * 采集时间增益
     * @param maxTime
     * @param heroId
     * @param player
     * @return
     */
    public int collectTime(final int maxTime, int pos, int heroId, Player player) {
        if (checkDiamondMine(pos)) return maxTime;
        Hero hero = player.heros.get(heroId);
        Object buff = DataResource.getBean(TreasureWareService.class).getTreasureWareBuff(player,
                hero, TreasureWareConst.SpecialType.COLLECT_TYPE, TreasureWareConst.SpecialType.CollectType.DURATION);
        if (ObjectUtils.isEmpty(buff) || !(buff instanceof List))
            return maxTime;

        int buffMaxTime = maxTime;
        List<List<Integer>> buffList = (List<List<Integer>>) buff;
        for (List<Integer> list : buffList) {
            buffMaxTime += list.get(1);
        }

        return buffMaxTime;
    }

    public double collectSpeed(final double speed, int pos, int heroId, Player player) {
        if (checkDiamondMine(pos)) return speed;
        Hero hero = player.heros.get(heroId);
        Object buff = DataResource.getBean(TreasureWareService.class).getTreasureWareBuff(player,
                hero, TreasureWareConst.SpecialType.COLLECT_TYPE, TreasureWareConst.SpecialType.CollectType.SPEED);
        if (ObjectUtils.isEmpty(buff) || !(buff instanceof List))
            return speed;

        double buffMaxTime = speed;
        List<List<Integer>> buffList = (List<List<Integer>>) buff;
        for (List<Integer> list : buffList) {
            buffMaxTime *= (1 + (list.get(1) / Constant.TEN_THROUSAND)) ;
        }

        return Math.floor(buffMaxTime);
    }

    /**
     * 计算玩家采集到的资源量
     * 
     * @param player
     * @param army
     * @param now
     * @return
     */
    public int calcCollect(Player player, Army army, int now) {
        int pos = army.getTarget();
        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (staticMine == null) {
            return 0;
        }
        // 开始开机时间
        Guard guard = worldDataManager.getGuardByPos(pos);
        int startTime = guard.getBeginTime();
        Date startDate = new Date(startTime * 1000L);
        int total = worldDataManager.getMineResource(pos);
        int resource = calcMineResource(player, pos, now);// 计算采集后剩余数量

        // 记录部队采集到的资源
        List<CommonPb.Award> grab = new ArrayList<>();
        int add = total - resource; // 已经采集到的数量
        add = calcGainCollectEffectCnt(player, add, staticMine.getMineType(), startDate, army);
        grab.add(PbHelper.createAwardPb(staticMine.getReward().get(0).get(0), staticMine.getReward().get(0).get(1),
                add));
        army.setGrab(grab);
        LogUtil.common("计算矿点采集, grab:", grab.get(0));
        return resource;
    }

    /**
     * 计算采集加成后的数量
     * 
     * @param player
     * @param gainCnt 踩了多少矿
     * @param mineType
     * @param startDate
     * @param army
     * @return
     */
    public int calcGainCollectEffectCnt(Player player, final int gainCnt, final int mineType, Date startDate,
            Army army) {
        int resGainCnt = gainCnt;
        if (mineType == StaticMine.MINE_TYPE_GOLD) {// 金矿 不进行加成
        } else if (mineType == StaticMine.MINE_TYPE_URANIUM) { // 铀矿
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// 皮肤加成
            // 红色勋章, 强化后勤保障特技加成
            double medalEffect = medalDataManager.getUraniumCollectEffect(player, army.getHero().get(0).getV1());
            resGainCnt = (int) (gainCnt * (1.0 + skinEffect + medalEffect));
        } else { // 其他矿石
            double techEffect = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_24); // 科技基础
            double mixEffect = getCollectEffect(player, mineType, startDate, army); // 混合加成
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// 皮肤加成
            // (可采集的资源 * (1.0 + 科技加成) * (1.0 + 天气采集加成 + 活动加成 + 名城加成 + 天书加成 + 赛季天赋优化加成) * (1 + 皮肤加成)) 然后取整
            resGainCnt = (int) (gainCnt * (1.0 + techEffect) * (1.0 + mixEffect) * (1  + skinEffect));
        }
        return resGainCnt;
    }

    /**
     * 获取采集加成 ,一些其他的加成
     * 
     * @param startDate
     * @param mineType
     * @return
     */
    private double getCollectEffect(Player player, int mineType, Date startDate, Army army) {
        double solarTermsEffect = solarTermsDataManager.getCollectBoundByType(mineType, startDate)
                / Constant.TEN_THROUSAND;// 天气采集加成
        double actEffect = activityDataManager.getActCollectNum() / Constant.TEN_THROUSAND;// 活动加成
        double cityBuffer = worldDataManager.getCityBuffer(army.getOriginCity(), mineType, army.getLordId());// 名城加成
        double medalNum = medalDataManager.logisticService(player, army.getHero().get(0).getV1());// 判断将领是否有 穿戴 后勤保障 特技的勋章
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_405) / Constant.TEN_THROUSAND; //赛季天赋优化加成
        return actEffect + solarTermsEffect + cityBuffer + medalNum + seasonTalentEffect;
    }

    /**
     * 是否有采集加成
     * 
     * @param startDate
     * @param mineType
     * @return
     */
    public boolean hasCollectEffect(Player player, int mineType, Date startDate, Army army) {
        if (mineType == StaticMine.MINE_TYPE_GOLD) {// 金矿 不进行加成
            return false;
        } else if (mineType == StaticMine.MINE_TYPE_URANIUM) { // 铀矿
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// 皮肤加成
            // 红色勋章, 强化后勤保障特技加成
            double medalEffect = medalDataManager.getUraniumCollectEffect(player, army.getHero().get(0).getV1());
            return skinEffect + medalEffect > 0.0;
        } else { // 其他矿石
            double mixEffect = getCollectEffect(player, mineType, startDate, army); // 混合加成
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// 皮肤加成
            return mixEffect + skinEffect > 0.0;
        }
    }

    /**
     * 自动采集
     * 
     * @param player
     * @param now
     */
    public void autoCollectMine(Player player, int now) {
        // 获取是否有闲置的采集将领
        Hero hero = getIdleCollectHero(player);
        if (null == hero) {
            return;
        }

        // 获取附近的矿点情况，找出资源最多的空闲矿点
        int minePos = getBestMine(player.lord.getPos());

        // 采集矿
        if (minePos > 0) {
            int mineId = worldDataManager.getMineByPos(minePos).getMineId();
            int marchTime = worldService.marchTime(player, minePos);
            int needFood = worldService.getNeedFood(marchTime, hero.getCount());
            try {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD,
                        needFood, AwardFrom.ATK_POS);
            } catch (MwException e) {
                // 资源不足，跳过
                return;
            }

            List<TwoInt> form = new ArrayList<>();
            form.add(PbHelper.createTwoIntPb(hero.getHeroId(), hero.getCount()));
            Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_COLLECT, minePos,
                    ArmyConstant.ARMY_STATE_MARCH, form, marchTime - 1, now + marchTime - 1, player.getDressUp());
            army.setTargetId(mineId);
            army.setLordId(player.roleId);
            army.setOriginPos(player.lord.getPos());
            Optional.ofNullable(medalDataManager.getHeroMedalByHeroIdAndIndex(player, hero.getHeroId(), MedalConst.HERO_MEDAL_INDEX_0))
                    .ifPresent(medal -> {
                        army.setHeroMedals(Collections.singletonList(PbHelper.createMedalPb(medal)));
                    });

            player.armys.put(army.getKeyId(), army);

            // 添加行军路线
            March march = new March(player, army);
            worldDataManager.addMarch(march);

            // 改变行军状态
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);

            // 区域变化推送
            List<Integer> posList = new ArrayList<>();
            posList.add(minePos);
            posList.add(player.lord.getPos());
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, player.roleId,
                    Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
    }

    /**
     * 获取玩家所在分区内最适合的可以采集的矿点坐标
     * 
     * @param pos
     * @return
     */
    private int getBestMine(int pos) {
        Set<Integer> blocks = MapHelper.getBlockInArea(MapHelper.getAreaIdByPos(pos));
        Map<Integer, Integer> mineMap = new HashMap<>();
        for (Integer block : blocks) {
            // 获取矿点
            Map<Integer, Integer> map = worldDataManager.getMineInBlock(block);
            if (null != map) {
                mineMap.putAll(map);
            }
        }

        // 获取矿点资源最多的矿点坐标
        Optional<Integer> maxResourceMinePos = mineMap.keySet().stream()
                .filter(minPos -> null == worldDataManager.getGuardByPos(minPos)).max(this::mineResourceCompare);
        if (maxResourceMinePos.isPresent()) {
            return maxResourceMinePos.get();
        }
        return -1;
    }

    /**
     * 矿点资源比较
     * 
     * @param pos1
     * @param pos2
     * @return
     */
    private int mineResourceCompare(int pos1, int pos2) {
        int r1 = worldDataManager.getMineResource(pos1);
        int r2 = worldDataManager.getMineResource(pos2);
        return r1 - r2;
    }

    private Hero getIdleCollectHero(Player player) {
        Hero hero;
        for (int heroId : player.heroAcq) {
            hero = player.heros.get(heroId);
            if (null != hero && hero.isIdle()) {
                return hero;
            }
        }
        return null;
    }
}
