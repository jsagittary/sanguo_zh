package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.SolarTermsDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.constant.TechConstant;
import com.gryphpoem.game.zw.resource.constant.TreasureWareConst;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @ClassName MineService.java
 * @Description ????????????
 * @author TanDonghai
 * @date ???????????????2017???4???11??? ??????6:50:22
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
                if (guard.getEndTime() <= now) {// ?????????????????????????????????
                    // ????????????
                    resource = calcCollect(guard.getPlayer(), guard.getArmy(), now);
                    // ????????????
                    worldService.finishCollect(guard, now);
                    // ??????????????????
                    updateMine(pos, resource);
                    its.remove();// ??????????????????????????????
                }
            } catch (Exception e) {
                // ????????????
                worldService.finishCollect(guard, now);
                its.remove();// ??????????????????????????????
                LogUtil.error(e, "????????????????????????, guard:", guard, "pos:", pos);
            }
        }
    }

    /**
     * ??????????????????
     * 
     * @param pos
     * @param resource
     */
    public void updateMine(int pos, int resource) {
        StaticMine sMine = worldDataManager.getMineByPos(pos);
        int time = calcMineGuardTime(worldDataManager.getMineByPos(pos), resource);
        LogUtil.debug("pos:", pos, ", mine:", sMine, ", ????????????:", resource, ", ??????????????????:", time);
        if (calcMineGuardTime(worldDataManager.getMineByPos(pos), resource) < Constant.MINE_RM_MIN_TIME) {// ??????????????????????????????????????????
            // worldDataManager.refreshMine(pos);
            worldDataManager.removeMine(pos);
        } else {
            // ???????????????????????????
            worldDataManager.putMineResource(pos, resource);
        }
    }

    /**
     * ?????????????????????????????? (???)
     * 
     * @param sMine
     * @param cnt ????????????
     * @return
     */
    public static int calcMineGuardTime(StaticMine sMine, int cnt) {
        if (sMine == null) {
            return 0;
        }
        double speed = sMine.getSpeed() / 3600.0;// ???????????????
        if (speed == 0) {
            return 0;
        }
        return (int) (cnt / speed);
    }

    /**
     * ???????????????????????????
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
     * ????????????????????????????????????
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
     * ??????????????????
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
     * ?????????????????????????????????
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
        // ??????????????????
        Guard guard = worldDataManager.getGuardByPos(pos);
        int startTime = guard.getBeginTime();
        Date startDate = new Date(startTime * 1000L);
        int total = worldDataManager.getMineResource(pos);
        int resource = calcMineResource(player, pos, now);// ???????????????????????????

        // ??????????????????????????????
        List<CommonPb.Award> grab = new ArrayList<>();
        int add = total - resource; // ????????????????????????
        add = calcGainCollectEffectCnt(player, add, staticMine.getMineType(), startDate, army);
        grab.add(PbHelper.createAwardPb(staticMine.getReward().get(0).get(0), staticMine.getReward().get(0).get(1),
                add));

        // ??????????????????????????????????????????
        List<CommonPb.Award> collectDrop = activityDataManager.getCollectDrop(player, staticMine.getMineId(), now - army.getBeginTime(), StaticActBandit.ACT_HIT_DROP_TYPE_4);
        grab.addAll(collectDrop);

        army.setGrab(grab);

        LogUtil.common("??????????????????, grab:", grab.get(0));
        EventDataUp.troop(player, 1, staticMine.getMineType(), staticMine.getLv(), now - startTime, grab);
        return resource;
    }

    /**
     * ??????????????????????????????
     * 
     * @param player
     * @param gainCnt ???????????????
     * @param mineType
     * @param startDate
     * @param army
     * @return
     */
    public int calcGainCollectEffectCnt(Player player, final int gainCnt, final int mineType, Date startDate,
            Army army) {
        int resGainCnt = gainCnt;
        if (mineType == StaticMine.MINE_TYPE_GOLD) {// ?????? ???????????????
        } else if (mineType == StaticMine.MINE_TYPE_URANIUM) { // ??????
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// ????????????
            // ????????????, ??????????????????????????????
            double medalEffect = medalDataManager.getUraniumCollectEffect(player, army.getHero().get(0).getV1());
            resGainCnt = (int) (gainCnt * (1.0 + skinEffect + medalEffect));
        } else { // ????????????
            double techEffect = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_24); // ????????????
            double mixEffect = getCollectEffect(player, mineType, startDate, army); // ????????????
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// ????????????
            // (?????????????????? * (1.0 + ????????????) * (1.0 + ?????????????????? + ???????????? + ???????????? + ???????????? + ????????????????????????) * (1 + ????????????)) ????????????
            resGainCnt = (int) (gainCnt * (1.0 + techEffect) * (1.0 + mixEffect) * (1  + skinEffect));
        }
        return resGainCnt;
    }

    /**
     * ?????????????????? ,?????????????????????
     * 
     * @param startDate
     * @param mineType
     * @return
     */
    private double getCollectEffect(Player player, int mineType, Date startDate, Army army) {
        double solarTermsEffect = solarTermsDataManager.getCollectBoundByType(mineType, startDate)
                / Constant.TEN_THROUSAND;// ??????????????????
        double actEffect = activityDataManager.getActCollectNum() / Constant.TEN_THROUSAND;// ????????????
        double cityBuffer = worldDataManager.getCityBuffer(army.getOriginCity(), mineType, army.getLordId());// ????????????
        double medalNum = medalDataManager.logisticService(player, army.getHero().get(0).getV1());// ????????????????????? ?????? ???????????? ???????????????
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_405) / Constant.TEN_THROUSAND; //????????????????????????
        return actEffect + solarTermsEffect + cityBuffer + medalNum + seasonTalentEffect;
    }

    /**
     * ?????????????????????
     * 
     * @param startDate
     * @param mineType
     * @return
     */
    public boolean hasCollectEffect(Player player, int mineType, Date startDate, Army army) {
        if (mineType == StaticMine.MINE_TYPE_GOLD) {// ?????? ???????????????
            return false;
        } else if (mineType == StaticMine.MINE_TYPE_URANIUM) { // ??????
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// ????????????
            // ????????????, ??????????????????????????????
            double medalEffect = medalDataManager.getUraniumCollectEffect(player, army.getHero().get(0).getV1());
            return skinEffect + medalEffect > 0.0;
        } else { // ????????????
            double mixEffect = getCollectEffect(player, mineType, startDate, army); // ????????????
            double skinEffect = CastleSkinService.getSkinCollectEffect(player, mineType);// ????????????
            return mixEffect + skinEffect > 0.0;
        }
    }

    /**
     * ????????????
     * 
     * @param player
     * @param now
     */
    public void autoCollectMine(Player player, int now) {
        // ????????????????????????????????????
        Hero hero = getIdleCollectHero(player);
        if (null == hero) {
            return;
        }

        // ???????????????????????????????????????????????????????????????
        int minePos = getBestMine(player.lord.getPos());

        // ?????????
        if (minePos > 0) {
            int mineId = worldDataManager.getMineByPos(minePos).getMineId();
            int marchTime = worldService.marchTime(player, minePos);
            int needFood = worldService.getNeedFood(marchTime, hero.getCount());
            try {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD,
                        needFood, AwardFrom.ATK_POS);
            } catch (MwException e) {
                // ?????????????????????
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

            // ??????????????????
            March march = new March(player, army);
            worldDataManager.addMarch(march);

            // ??????????????????
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);

            // ??????????????????
            List<Integer> posList = new ArrayList<>();
            posList.add(minePos);
            posList.add(player.lord.getPos());
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, player.roleId,
                    Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     * 
     * @param pos
     * @return
     */
    private int getBestMine(int pos) {
        Set<Integer> blocks = MapHelper.getBlockInArea(MapHelper.getAreaIdByPos(pos));
        Map<Integer, Integer> mineMap = new HashMap<>();
        for (Integer block : blocks) {
            // ????????????
            Map<Integer, Integer> map = worldDataManager.getMineInBlock(block);
            if (null != map) {
                mineMap.putAll(map);
            }
        }

        // ???????????????????????????????????????
        Optional<Integer> maxResourceMinePos = mineMap.keySet().stream()
                .filter(minPos -> null == worldDataManager.getGuardByPos(minPos)).max(this::mineResourceCompare);
        if (maxResourceMinePos.isPresent()) {
            return maxResourceMinePos.get();
        }
        return -1;
    }

    /**
     * ??????????????????
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
