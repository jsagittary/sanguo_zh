package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticCounterAttack;
import com.gryphpoem.game.zw.resource.domain.s.StaticCounterAttackShop;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-08 16:30
 * @description: 柏林闪电战(反攻德意志)
 * @modified By:
 */
@Service("counterAtkService")
public class CounterAtkService extends BaseAwkwardDataManager {

    @Autowired
    private WarService warService;
    @Autowired
    private FightService fightService;
    @Autowired
    private WallService wallService;
    @Autowired
    private WorldService worldService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private SeasonTalentService seasonTalentService;

    /**
     * 获取反攻德意志信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GetCounterAttackRs getCounterAttack(long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.GetCounterAttackRs.Builder builder = GamePb4.GetCounterAttackRs.newBuilder();
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        CommonPb.CounterAtkData.Builder counterAttackPb = counterAttack.createCounterAttackPb();
        Set<Long> campHitRoleId = counterAttack.getCampHitRoleId(player.lord.getCamp());
        if (!CheckNull.isEmpty(campHitRoleId)) {
            campHitRoleId.forEach(role -> {
                Player p = playerDataManager.getPlayer(role);
                if (!CheckNull.isNull(p)) {
                    counterAttackPb.addRoleInfo(PbHelper.createHitRoleInfo(p));
                }
            });
        }
        builder.setData(counterAttackPb.build());
        builder.setCredit(player.getMixtureDataById(PlayerConstant.COUNTER_ATK_CREDIT));
        builder.setUnLock(checkUnLock());
        Map<Integer, Integer> counterAtkNextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.COUNTER_ATK_NEXT_OPEN_TIME);
        int counterAtkNextTime = counterAtkNextOpenMap.getOrDefault(0, 0);
        builder.setNextOpen(counterAtkNextTime);
        return builder.build();
    }

    /**
     * 进攻反攻boss
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.AttackCounterBossRs attackCounterBoss(GamePb4.AttackCounterBossRq req, long roleId)
            throws MwException {

        List<Integer> heroIdList = req.getHeroIdList();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        if (counterAttack.getStatus() == WorldConstant.COUNTER_ATK_BOSS_STATUS_DEAD) { // 所有BOSS已死亡
            throw new MwException(GameError.COUNTER_ATK_STATUS_IS_END.getCode(), "反攻德意志功能本轮已经结束, roleId: ", roleId);
        }

        int camp = player.lord.getCamp();
        int battleId = req.getBattleId();
        Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
        if (CheckNull.isNull(battle)) {
            throw new MwException(GameError.BATTLE_NOT_FOUND.getCode(), "战争信息未找到, roleId:", roleId, ", battleId:",
                    battleId);
        }

        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(battle.getPos()));
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        // 城战区域检测
        worldService.checkArea(player, targetSArea, mySArea);

        int pos = battle.getPos();
        int endTime = battle.getBattleTime();
        int now = TimeHelper.getCurrentSecond();

        // 检查出征将领信息
        worldService.checkFormHero(player, heroIdList);
        int armCount = 0;
        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }
        // 行军时间
        int marchTime = worldService.marchTime(player, pos);
        if (now + marchTime > endTime) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "加入据点,赶不上时间, roleId:", roleId, ", pos:",
                    pos + ",行军时间=" + (now + marchTime) + ",结束倒计时=" + endTime);
        }

        // 计算补给
        int needFood = worldService.checkMarchFood(player, marchTime, armCount);
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        List<CommonPb.TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
        }

        // 添加兵力到进攻方
        battle.updateAtkBoss(camp, armCount);
        battle.getAtkRoles().add(roleId);

        Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_COUNTER_BOSS_DEF, pos,
                ArmyConstant.ARMY_STATE_MARCH, form, marchTime - 1, now + marchTime - 1, player.getDressUp());
        army.setLordId(roleId);
        army.setBattleId(battle.getBattleId());
        army.setBattleTime(battle != null ? battle.getBattleTime() : 0);
        army.setOriginPos(player.lord.getPos());
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        player.armys.put(army.getKeyId(), army);
        HashSet<Integer> set = player.battleMap.get(pos);
        if (set == null) {
            set = new HashSet<>();
            player.battleMap.put(pos, set);
        }
        set.add(battle.getBattleId());

        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // 推送区数据改变
        List<Integer> posList = new ArrayList<>();
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, roleId,
                Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));

        GamePb4.AttackCounterBossRs.Builder builder = GamePb4.AttackCounterBossRs.newBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));
        return builder.build();
    }

    /**
     * 获取反攻商店信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GetCounterAtkShopRs getCounterAtkShop(long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();

        GamePb4.GetCounterAtkShopRs.Builder builder = GamePb4.GetCounterAtkShopRs.newBuilder();
        Map<Integer, Integer> counterShop = counterAttack.getCounterShop(roleId);
        if (!CheckNull.isEmpty(counterShop)) {
            for (Map.Entry<Integer, Integer> en : counterShop.entrySet()) {
                builder.addStatus(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
            }
        }
        builder.setCredit(player.getMixtureDataById(PlayerConstant.COUNTER_ATK_CREDIT));
        return builder.build();
    }

    /**
     * 购买反攻商店的商品
     *
     * @param id     商品id
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.BuyCounterAtkAwardRs buyCounterAtkAward(int id, long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticCounterAttackShop shopConf = StaticWorldDataMgr.getCounterAttackShopMap(id);
        if (CheckNull.isNull(shopConf)) {
            throw new MwException(GameError.COUNTER_CONFIG_ERROR.getCode(), "获取反攻商店配置信息出错, shopId: ", id, ", roleId: ",
                    roleId);
        }

        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        GamePb4.BuyCounterAtkAwardRs.Builder builder = GamePb4.BuyCounterAtkAwardRs.newBuilder();

        Map<Integer, Integer> counterShop = counterAttack.getCounterShop(roleId);

        if (!CheckNull.isNull(counterShop)) {
            int cnt = counterShop.getOrDefault(id, 0);
            if (cnt >= shopConf.getNum()) {
                throw new MwException(GameError.COUNTER_BUY_COUNT_MAX.getCode(), "购买反攻商店商品已达次数上限, shopId: ", id,
                        ", roleId: ", roleId, ", cnt: ", cnt);
            }
            int redit = player.getMixtureDataById(PlayerConstant.COUNTER_ATK_CREDIT);
            if (redit < shopConf.getPrice()) {
                throw new MwException(GameError.COUNTER_BUY_COUNT_MAX.getCode(), "购买反攻商店商品, 积分不足, price: ",
                        shopConf.getPrice(), ", have: ", redit, ", roleId: ", roleId);
            }
            int lost = redit - shopConf.getPrice();
            counterShop.put(id, cnt + 1);
            player.setMixtureData(PlayerConstant.COUNTER_ATK_CREDIT, lost);
            builder.setAward(rewardDataManager.addAwardSignle(player, shopConf.getAward(), AwardFrom.COUNTER_SHOP_BUY));
            LogLordHelper.commonLog("counterAtkCredit", AwardFrom.COUNTER_SHOP_BUY, player, redit, shopConf.getPrice());
            //上报数数
            EventDataUp.credits(player.account,player.lord,player.getMixtureDataById(PlayerConstant.COUNTER_ATK_CREDIT),-shopConf.getPrice(), CreditsConstant.COUNTER_ATK,AwardFrom.COUNTER_SHOP_BUY);
            
        }

        if (!CheckNull.isEmpty(counterShop)) {
            for (Map.Entry<Integer, Integer> en : counterShop.entrySet()) {
                builder.addStatus(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
            }
        }
        builder.setCredit(player.getMixtureDataById(PlayerConstant.COUNTER_ATK_CREDIT));
        return builder.build();
    }

    /**
     * 部队到达处理
     *
     * @param player
     * @param army
     * @param now
     */
    public void marchEndBossDefArmy(Player player, Army army, int now) {
        int battleId = army.getBattleId();
        LogUtil.debug("玩家参加战斗id, roleId:", player.roleId, ", battleId:", battleId);
        Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
        LogUtil.debug("玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // 战斗对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_STATE_COUNTER_BOSS_DEF);

        Hero hero;
        List<Integer> heroIdList = new ArrayList<>();
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_COUNTER_BOSS_DEF);
            heroIdList.add(hero.getHeroId());
        }
        worldService.addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(), true);
    }

    /**
     * 德意志反攻驻防部队到达处理
     *
     * @param player
     * @param army
     * @param now
     */
    public void marchEndBossAtkHelpArmy(Player player, Army army, int now) {
        int pos = army.getTarget();
        Player target = worldDataManager.getPosData(pos);
        Integer battleIdObj = army.getBattleId();
        int battleId = battleIdObj == null ? 0 : battleIdObj.intValue();
        Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
        if (battle != null && target != null) {
            army.setState(ArmyConstant.ARMY_STATE_COUNTER_BOSS_ATK_HELP);
            List<Integer> heroIdList = new ArrayList<>();
            for (CommonPb.TwoInt twoInt : army.getHero()) {
                Hero hero = player.heros.get(twoInt.getV1());
                hero.setState(ArmyConstant.ARMY_STATE_COUNTER_BOSS_ATK_HELP);
                heroIdList.add(hero.getHeroId());
            }
            worldService.addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(), false);
        } else {
            // 目标丢失
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(),
                    xy.getB(), xy.getA(), xy.getB());
            worldService.retreatArmyByDistance(player, army, now);
        }
    }

    /**
     * 检测都城条件是否满足
     *
     * @return 是否满足
     */
    private boolean checkHomeCond() {
        boolean homeCond = true;
        for (int camp : Constant.Camp.camps) {
            City city = worldDataManager.checkHasHome(camp);
            if (CheckNull.isNull(city)) {
                return false;
            }
        }
        return homeCond;
    }

    /**
     * 检测柏林会战条件是否满足
     *
     * @return 是否满足
     */
    public boolean checkBerlinWarCond() {
        BerlinWar berlin = BerlinWar.getInstance();
        return !CheckNull.isNull(berlin.getLastDate()) ? berlin.getLastDate().getTime() > 0 ? true : false : false;
    }

    /**
     * 当月第几周的条件
     *
     * @return true满足条件
     */
    private boolean weekOfMonthCond(Date date) {
        List<List<Integer>> timeCfg = WorldConstant.COUNTER_ATTACK_BERLIN_TIME_CFG;
        if (CheckNull.isEmpty(timeCfg)) {
            return false;
        }
        List<Integer> wOfMCfg = timeCfg.get(0);// 当月的第周几
        int weekOfMonth = TimeHelper.getWeekOfMonth(date);
        return wOfMCfg.contains(weekOfMonth);
    }

    /**
     * 检测解锁条件是否满足
     *
     * @return
     */
    public boolean checkUnLock() {
        return checkHomeCond() && checkBerlinWarCond() && Constant.COUNTER_ATK_FUNCTION_LOCK == 1 /*&& weekOfMonthCond(new Date())*/;
    }

    /**
     * 反攻失败
     *
     * @return 是否失败
     */
    private boolean counterAtkFail() {
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        boolean fail = false;
        if (!CheckNull.isNull(counterAttack)) {
            StaticCounterAttack config = StaticWorldDataMgr
                    .getCounterAttackTypeMapByCond(WorldConstant.COUNTER_ATK_DEF, counterAttack.getCurrentBoss() + 1);
            if (counterAttack.isNotInitOrDead() && CheckNull.isNull(config)) {
                fail = true;
            }
        }
        return fail;
    }

    /**
     * 首次开放
     *
     * @return
     */
    private boolean firstOpenCounterAtk() {
        Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.COUNTER_ATK_NEXT_OPEN_TIME);
        // 没有下一次的开启时间
        if (nextOpenMap.getOrDefault(0, 0) == 0) {
            // 当前的时间
            Date today = new Date();
            // 开启时间
            Date openTimeDate = TimeHelper.getDayOfWeekByDate(today, DateHelper.TUESDAY);
            if (today.after(openTimeDate)) {
                openTimeDate = TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 7, 20, 0, 0);
            } else {
                // 客户端需要显示倒计时
                openTimeDate = TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 0, 20, 0, 0);
            }
            LogUtil.debug("------德意志反攻首次开放时间：", DateHelper.formatDateMiniTime(openTimeDate), "-------");
            // 初始化德意志反攻
            int value = TimeHelper.dateToSecond(openTimeDate);
            nextOpenMap.put(0, value);
            globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.COUNTER_ATK_NEXT_OPEN_TIME, nextOpenMap);
            return true;
        }
        return false;
    }

    public void initCounterAtk() {
        Date nowDate = new Date();
        if (!checkUnLock())
            return;
        // 首次开放, 更新后第一个周二开启
        if (firstOpenCounterAtk()) {
            return;
        }
        Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.COUNTER_ATK_NEXT_OPEN_TIME);
        // 下次的开放时间
        int nextTime = nextOpenMap.getOrDefault(0, 0);
        Date curOpen = TimeHelper.secondToDate(nextTime);
        // 下次开启时间距离今天有多久
        int dayiy = DateHelper.dayiy(curOpen, nowDate);
        if (dayiy == 1) {
            // 还没过本次的德意志反攻, 初始化本次的德意志反攻(为了修复, 重启以后本次开放的问题)
            initSchedTime(curOpen);
        } else if (dayiy > 1) {
            if (nextTime != 0) {
                // 初始化本次, 记录下次的开发时间, 下次要28天后开放
                Date nextOpen = TimeHelper.getSomeDayAfterOrBerfore(curOpen, worldScheduleService.globalActNextOpenTemplate(), 20, 0, 0);
                nextOpenMap.put(0, TimeHelper.dateToSecond(nextOpen));
                globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.COUNTER_ATK_NEXT_OPEN_TIME, nextOpenMap);
                LogUtil.debug("------反攻德意志下次开启的时间：", DateHelper.formatDateMiniTime(nextOpen), "-------");
            }
        } else {
            // 还没到开启时间
            LogUtil.debug("------没到反攻德意志下次开启的时间：", DateHelper.formatDateMiniTime(TimeHelper.secondToDate(nextTime)), "-------");
        }
    }


    /**
     * 初始化计划任务
     *
     * @param openDate 开放时间
     */
    private void initSchedTime(Date openDate) {
        List<List<Integer>> timeCfg = WorldConstant.COUNTER_ATTACK_BERLIN_TIME_CFG;
        if (CheckNull.isEmpty(timeCfg)) {
            return;
        }
        int nowTime = TimeHelper.getCurrentSecond();
        int preViewTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(openDate, 0, 0, 0, 0).getTime() / 1000); // 预显示的时间
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        if (preViewTime != counterAttack.getCurPreViewTime() && !(nowTime >= counterAttack.getCurPreViewTime()
                && nowTime <= counterAttack.getCurEndTime())) {
            City city = worldDataManager.checkHasHome(Constant.Camp.NPC); // 反攻的城池
            if (CheckNull.isNull(city)) {
                return;
            }
            StaticCity sCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            if (CheckNull.isNull(sCity)) {
                LogUtil.error("反攻德意志未找到配置 CityId:", city.getCityId());
                return;
            }

            counterAttack.reset(); // 重置数据
            counterAttack.incrOpenCnt(); // 开启几次

            counterAttack.setCityId(city.getCityId());
            counterAttack.setPos(sCity.getCityPos());

            // 初始化boss
            StaticCounterAttack config = StaticWorldDataMgr
                    .getCounterAttackTypeMapByCond(WorldConstant.COUNTER_ATK_DEF, counterAttack.getCurrentBoss());
            if (CheckNull.isNull(config)) {
                return;
            }
            Fighter fighter = createCounterAtkFighter(config.getNpcForm());
            counterAttack.setFighter(fighter);

            int hourCfg = timeCfg.get(1).get(1);// 几点
            int roundStartTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(openDate, 0, hourCfg, 0, 0).getTime()
                    / 1000);
            int duringTimeCfg = timeCfg.get(1).get(2);// 持续多久
            int endTime = roundStartTime + duringTimeCfg; // 结束时间

            counterAttack.setCurPreViewTime(preViewTime);
            counterAttack.setCurRoundStartTime(roundStartTime);
            counterAttack.setCurRoundEndTime(endTime);
            counterAttack.setCurEndTime(endTime);
        }
        // 初始化定时器
        initSchedTime(counterAttack);
    }

    /**
     * 初始化定时器
     *
     * @param counterAttack
     */
    private void initSchedTime(CounterAttack counterAttack) {
        ScheduleManager scheduleManager = ScheduleManager.getInstance();
        if (CheckNull.isNull(scheduleManager)) {
            return;
        }
        List<List<Integer>> timeCfg = WorldConstant.COUNTER_ATTACK_BERLIN_TIME_CFG;
        if (CheckNull.isNull(timeCfg)) {
            return;
        }
        Date now = new Date();
        int intervalTime = timeCfg.get(2).get(0);
        Date beginTime = TimeHelper.secondToDate(counterAttack.getCurRoundStartTime());
        Date endTime = TimeHelper.secondToDate(counterAttack.getCurRoundEndTime());
        // Begin-End 根据 intervalTime 周期性执行
        if (!DateHelper.isAfterTime(now, beginTime)) {
            scheduleManager.addOrModifyDefultJob(DefultJob.createDefult(WorldConstant.BL_LW_START_CALLBACK_NAME),
                    (job) -> roundCallBack(counterAttack), beginTime, endTime, intervalTime);
        }
        // EndTime 执行
        if (!DateHelper.isAfterTime(now, endTime)) {
            scheduleManager.addOrModifyDefultJob(DefultJob.createDefult(WorldConstant.BL_LW_END_CALLBACK_NAME),
                    (job) -> endCallBack(counterAttack), endTime);
        }
        if (now.after(beginTime) && now.before(endTime)) { // 在Begin-End期间
            endCallBack(counterAttack); // 手动关闭活动
        }
    }

    /**
     * Begin-End 根据 intervalTime 周期性执行
     *
     * @param counterAttack
     */
    private void roundCallBack(CounterAttack counterAttack) {
        LogUtil.debug("---------------------反攻德意志roundCallBack---------------------");
        if (CheckNull.isNull(counterAttack)
                || counterAttack.getStatus() == WorldConstant.COUNTER_ATK_BOSS_STATUS_DEAD) {
            return;
        }
        int curRound = counterAttack.incrCurRound();
        int atkOrDef = curRound % 2;
        int cityId = counterAttack.getCityId();
        if (!counterAttack.isNotInitOrDead()) {
            if (curRound == 0) {
                City city = worldDataManager.getCityById(cityId);
                city.setStatus(WorldConstant.CITY_STATUS_BATTLE); // 设置City战斗状态
            }
            createBossDefBattle(counterAttack, TimeHelper.getCurrentSecond());
        }
        switch (atkOrDef) {
            case WorldConstant.COUNTER_ATK_DEF: // Boss防守阶段 8:00 0 8:20 2
                counterAttack.setStatus(WorldConstant.COUNTER_ATK_BOSS_STATUS_DEF);
                chatDataManager.sendSysChat(ChatConst.CHAT_COUNTER_ATK_BOSS_DEF, 0, 0, cityId, counterAttack.getPos());
                if (curRound != 0) {
                    counterAttack.incrAtkCnt(); // 进攻次数+1
                }
                break;
            case WorldConstant.COUNTER_ATK_ATK: // Boss进攻阶段 8:10 1 8:30 3
                counterAttack.setStatus(WorldConstant.COUNTER_ATK_BOSS_STATUS_ATK);
                chatDataManager.sendSysChat(ChatConst.CHAT_COUNTER_ATK_BOSS_ATK, 0, 0);
                counterAttack.initAtkForm(counterAttack.getCurrentAtkCnt()); // 初始化进攻阵型
                npcHitPlayers(counterAttack); // NPC进攻玩家
                break;
        }
        // 同步当前反攻德意志状态
        syncCounterAttackRs(counterAttack);
        // 同步地图改变
        syncWroldChange(counterAttack);
    }

    // 同步地图改变
    private void syncWroldChange(CounterAttack counterAttack) {
        List<Integer> posList = new ArrayList<>();
        posList.add(counterAttack.getPos());
        if (!CheckNull.isEmpty(counterAttack.getCampHitRoleId())) {
            posList.addAll(counterAttack.getCampHitRoleId().stream()
                    .filter(roleId -> !CheckNull.isNull(playerDataManager.getPlayer(roleId)))
                    .map(role -> playerDataManager.getPlayer(role).lord.getPos()).collect(Collectors.toList()));
        }
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
    }

    /**
     * 创建反攻战BOSS防守Battle
     *
     * @param counterAttack
     * @param now
     */
    private void createBossDefBattle(CounterAttack counterAttack, int now) {
        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_COUNTER_ATK);
        battle.setBattleType(WorldConstant.COUNTER_ATK_DEF);
        battle.setBattleTime(now + WorldConstant.COUNTER_ATTACK_BERLIN_TIME_CFG.get(2).get(0) - 2);
        battle.setBeginTime(now);
        battle.setPos(counterAttack.getPos());
        battle.setDefCamp(Constant.Camp.NPC);
        battle.setDefArm(counterAttack.currentHp());
        warDataManager.addSpecialBattle(battle); // 添加特殊战斗
    }

    /**
     * 同步当前反攻德意志状态
     *
     * @param counterAttack
     */
    public void syncCounterAttackRs(CounterAttack counterAttack) {
        playerDataManager.getAllOnlinePlayer().values().stream().forEach(player -> {
            GamePb4.SyncCounterAttackRs.Builder builder = GamePb4.SyncCounterAttackRs.newBuilder();
            CommonPb.CounterAtkData.Builder counterAttackPb = counterAttack.createCounterAttackPb();
            Set<Long> campHitRoleId = counterAttack.getCampHitRoleId(player.lord.getCamp());
            if (!CheckNull.isEmpty(campHitRoleId)) {
                campHitRoleId.forEach(role -> {
                    Player p = playerDataManager.getPlayer(role);
                    if (!CheckNull.isNull(p)) {
                        counterAttackPb.addRoleInfo(PbHelper.createHitRoleInfo(p));
                    }
                });
            }
            builder.setData(counterAttackPb.build());
            BasePb.Base.Builder baseBuilder = PbHelper
                    .createSynBase(GamePb4.SyncCounterAttackRs.EXT_FIELD_NUMBER, GamePb4.SyncCounterAttackRs.ext,
                            builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, baseBuilder.build(), player.roleId));
        });
    }

    /**
     * 活动结束时调用
     *
     * @param counterAttack
     */
    private void endCallBack(CounterAttack counterAttack) {
        LogUtil.debug("---------------------反攻德意志endCallBack---------------------");
        // 活动已经结束
        if (counterAttack.getStatus() == WorldConstant.COUNTER_ATK_BOSS_STATUS_DEAD) {
            return;
        }
        ScheduleManager.getInstance().removeCounterAtkJob();
        int now = TimeHelper.getCurrentSecond();
        counterAttack.setStatus(WorldConstant.COUNTER_ATK_BOSS_STATUS_DEAD);
        // 埋点boss进攻玩家的第几波
        LogLordHelper.otherLog("counterAtkEnd", DataResource.ac.getBean(ServerSetting.class).getServerID(), counterAttack.getCurrentAtkCnt());
        LogUtil.error("反攻德意志结束, 现在时间: ", DateHelper.getDateFormat1().format(new Date()), ", 开启时间: ",
                DateHelper.getDateFormat1().format(counterAttack.getCurRoundStartTime()), ", 结束时间: ",
                DateHelper.getDateFormat1().format(counterAttack.getCurRoundEndTime()), ", 预显示时间: ",
                DateHelper.getDateFormat1().format(counterAttack.getCurPreViewTime()));
        counterAttack.getCounterShop().clear(); // 清除军火库的购买数量
        // 活动结束给所有人发积分
        counterAttack.getJoinBattleRole().stream().forEach(roleId -> {
            int credit = WorldConstant.COUNTER_ATTACK_CREDIT.get(3).get(0);
            sendCreditAward(credit, roleId);
            Player player = playerDataManager.getPlayer(roleId);
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_COUNTER_BOSS_JOIN_BATTLE, now, credit);
        });
        if (counterAtkFail()) { // 没有下一个boss了
            chatDataManager.sendSysChat(ChatConst.CHAT_COUNTER_ATK_END_BOSS_DEAD, 0, 0);
            playerDataManager.getPlayers().values().stream().forEach(p -> {
                int credit = WorldConstant.COUNTER_ATTACK_CREDIT.get(4).get(0);
                sendCreditAward(credit, p.roleId);
                mailDataManager.sendNormalMail(p, MailConstant.MOLD_COUNTER_BOSS_JOIN_AWARD, now, credit);
            });
        } else {
            chatDataManager.sendSysChat(ChatConst.CHAT_COUNTER_ATK_END_BOSS_NOT_DEAD, 0, 0);
        }
        // 活动结束清除city状态
        int cityId = counterAttack.getCityId();
        City city = worldDataManager.getCityById(cityId);
        city.setStatus(WorldConstant.CITY_STATUS_CALM); // 设置City战斗状态
        // 同步当前反攻德意志状态
        syncCounterAttackRs(counterAttack);
        // 同步地图改变
        syncWroldChange(counterAttack);
    }

    /**
     * NPC进攻玩家
     *
     * @param counterAttack
     */
    private void npcHitPlayers(CounterAttack counterAttack) {
        selectHitPlayer(counterAttack); // 挑选本次被进攻的玩家
        Set<Long> hitRoles = counterAttack.getCampHitRoleId();
        if (CheckNull.isEmpty(hitRoles)) {
            return;
        }
        int curRound = counterAttack.getCurrentAtkCnt(); // 进攻波数
        List<CityHero> npcForm = counterAttack.getAtkFormList();
        if (CheckNull.isEmpty(npcForm)) {
            LogUtil.error("反攻德意志配置错误, curRound", curRound);
            return;
        }
        int atkArm = 0;
        for (CityHero cityHero : npcForm) {
            atkArm += cityHero.getCurArm();
        }
        int now = TimeHelper.getCurrentSecond();
        int interval = WorldConstant.COUNTER_ATTACK_BERLIN_TIME_CFG.get(2).get(0);
        int battleTime = now + interval;

        for (Long roleId : hitRoles) {
            Player player = playerDataManager.getPlayer(roleId);
            // 对方开启自动补兵
            playerDataManager.autoAddArmy(player);
            int defArm = 0;
            for (Hero hero : player.getAllOnBattleHeros()) {
                defArm += hero.getCount();
            }
            if (!CheckNull.isNull(player)) {
                Battle battle = new Battle();
                battle.setType(WorldConstant.BATTLE_TYPE_COUNTER_ATK);
                battle.setBattleType(WorldConstant.COUNTER_ATK_ATK);
                battle.setBattleTime(battleTime - 1);
                battle.setBeginTime(now);
                battle.setDefencerId(player.roleId);
                int pos = player.lord.getPos();
                battle.setPos(pos);
                battle.setDefencer(player);
                battle.setAtkCamp(Constant.Camp.NPC);
                battle.setDefCamp(player.lord.getCamp());
                battle.addAtkArm(atkArm);// 进攻方兵力
                battle.addDefArm(defArm);// 防守方兵力
                warDataManager.addSpecialBattle(battle);// 加入特殊战斗容器
                HashSet<Integer> set = player.battleMap.get(pos);// 加入到玩家身上
                if (set == null) {
                    set = new HashSet<>();
                    player.battleMap.put(pos, set);
                }
                set.add(battle.getBattleId());
                worldService.syncAttackRole(player, null, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1,
                        WorldConstant.BATTLE_TYPE_COUNTER_ATK, curRound, counterAttack.getCityId());
            }
        }
    }

    /**
     * 每10分钟, 每个阵营挑选10个玩家.主动发起战斗(从皇城找在线玩家, 等于大于45级)
     *
     * @param counterAttack
     */
    private void selectHitPlayer(CounterAttack counterAttack) {
        if (CheckNull.isNull(counterAttack)) {
            return;
        }
        //  每10分钟, 每个阵营挑选10个玩家.主动发起战斗(从皇城找)
        counterAttack.clearCampHitRoles();
        List<Integer> areaList = StaticWorldDataMgr.getOrderOpenAreaList(WorldConstant.AREA_ORDER_3);
        if (CheckNull.isEmpty(areaList)) {
            return;
        }
        List<Player> players = playerDataManager.getPlayers().values().stream()
                .filter(p -> p.lord.getArea() == WorldConstant.AREA_TYPE_13).collect(Collectors.toList());
        Collections.shuffle(players); // 打乱顺序
        for (int camp : Constant.Camp.camps) {
            Set<Long> campHitRoleId = counterAttack.getCampHitRoleId(camp);
            campHitRoleId.addAll(players.stream() // 在线的玩家
                    .filter(p -> p.lord.getCamp() == camp && p.lord.getLevel() > WorldConstant.ATTACK_STATE_NEED_LV
                            && p.isLogin).map(p -> p.roleId).distinct().limit(WorldConstant.COUNTER_ATTACK_CAMP_HIT_CNT)
                    .collect(Collectors.toSet()));
            int need = WorldConstant.COUNTER_ATTACK_CAMP_HIT_CNT - campHitRoleId.size();
            if (need > 0) { // 补充不在线的玩家
                campHitRoleId.addAll(players.stream()
                        .filter(p -> p.lord.getCamp() == camp && p.lord.getLevel() > WorldConstant.ATTACK_STATE_NEED_LV
                                && !p.isLogin).map(p -> p.roleId).distinct()
                        .limit(need).collect(Collectors.toSet()));
            }
        }
    }

    /**
     * 反攻德意志BOSS
     *
     * @param npcForm
     * @return
     */
    public Fighter createCounterAtkFighter(List<CityHero> npcForm) {
        int coef = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.FORM_COEF_OF_DIFFICULTY)
                .getOrDefault(GlobalConstant.CoefDifficulty.COUNTER_ATTACK, 100);
        LogUtil.debug("反攻德意志当前难度系数, coef:", coef);
        Fighter fighter = new Fighter();
        Force force;
        // 城池NPC守军
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                if (cityHero.getCurArm() <= 0)
                    continue;
                force = fightService.createCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm(), coef);
                force.roleType = Constant.Role.CITY;
                fighter.addForce(force);
            }
        }
        return fighter;
    }

    /**
     * 反攻德意志的战斗处理
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     */
    public void processBattleLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) {
        int battleType = battle.getBattleType();
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        removeBattleIdSet.add(battle.getBattleId());
        if (battleType == WorldConstant.COUNTER_ATK_ATK) { // BOSS 进攻
            bossAtkBattleLogic(battle, now, counterAttack);
        } else if (battleType == WorldConstant.COUNTER_ATK_DEF) { // BOSS 防守
            bossDefBattleLogic(battle, now, counterAttack, removeBattleIdSet);
        }
    }

    /**
     * BOSS防守玩家战斗处理
     *
     * @param battle
     * @param now
     * @param counterAttack
     * @param removeBattleIdSet
     */
    private void bossDefBattleLogic(Battle battle, int now, CounterAttack counterAttack,
                                    Set<Integer> removeBattleIdSet) {
        if (CheckNull.isEmpty(battle.getAtkList())) {
            LogUtil.error("反攻德意志, BOSS防守没有进攻方");
            return;
        }
        int sendMailTime = now + 2;
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        Fighter defender = fightService.createBossNpcForce(counterAttack.getFighter());
        LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);

        // 战斗前记录
        int beforeTotal = defender.getTotal();// 总血量
        int beforeLost = defender.getLost();// 总损失兵力

        fightLogic.fight();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker,false,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,false,true);

        boolean atkSuc = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // 记录玩家获得的积分
        Map<Long, Integer> creditMap = new HashMap<>();
        if (attacker.lost > 0) {
            // 扣兵处理
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.COUNTER_BOSS_ATK);
            // 玩家损兵统计
            Map<Long, Integer> roleLost = attacker.forces.stream()
                    .filter(f -> f.totalLost > 0 && f.roleType == Constant.Role.PLAYER).collect(Collectors
                            .toMap(f -> f.ownerId, f -> f.totalLost, (existVal, newVal) -> existVal + newVal));
            // 计算积分
            addCredit(creditMap, roleLost, WorldConstant.COUNTER_ATTACK_CREDIT.get(0), counterAttack);
        }

        List<CommonPb.BattleRole> atkList = battle.getAtkList();

        //执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);

        // Report战报
        CommonPb.RptAtkPlayer.Builder rpt = createBossDefRptBuilderPb(counterAttack, attacker, defender, fightLogic,
                beforeTotal, beforeLost, atkSuc);
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), sendMailTime);

        // 发送参与邮件
        sendBossDefBattleMail(battle, sendMailTime, counterAttack, recoverArmyAwardMap, creditMap, report);

        // 发送反攻德意志的积分
        sendCreditAawrd(creditMap);

        // 扣掉Battle中的损兵
        counterAttack.setFighter(defender);
        battle.subDefArm(defender.lost);

        StaticCounterAttack config = StaticWorldDataMgr
                .getCounterAttackTypeMapByCond(WorldConstant.COUNTER_ATK_DEF, counterAttack.getCurrentBoss());
        Turple<Integer, Integer> cityXY = MapHelper.reducePos(counterAttack.getPos());
        if (atkSuc && !CheckNull.isNull(config)) { // 进攻成功
            int credit = config.getScore(); // 获得的积分
            CommonPb.Round lastRound = fightLogic.getLastRound();
            if (!CheckNull.isNull(lastRound)) {
                CommonPb.Action actionB = lastRound.getActionB();
                long targetRoleId = actionB.getTargetRoleId();
                if (targetRoleId != 0) {
                    Player player = playerDataManager.getPlayer(targetRoleId); // 最后的击杀玩家
                    if (!CheckNull.isNull(player)) {
                        sendCreditAward(credit, player.roleId); // 发送积分
                        Object[] params = {counterAttack.getCityId(), cityXY.getA(), cityXY.getB(), credit};
                        if (counterAtkFail()) {
                            mailDataManager
                                    .sendNormalMail(player, MailConstant.MOLD_COUNTER_BOSS_DEF_END, sendMailTime, params);
                        } else {
                            mailDataManager
                                    .sendNormalMail(player, MailConstant.MOLD_COUNTER_BOSS_DEF_DEAD, sendMailTime, params);
                        }
                    }
                }
            }

            if (counterAtkFail()) { // 没有下一个boss了
                List<Integer> battleIds = warDataManager.getSpecialBattleMap().values().stream()
                        .filter(b -> b.getType() == WorldConstant.BATTLE_TYPE_COUNTER_ATK).map(b -> b.getBattleId())
                        .distinct().collect(Collectors.toList());
                if (!CheckNull.isEmpty(battleIds)) {
                    removeBattleIdSet.addAll(battleIds); // 移除后续
                }
                // 增加难度系数
                difficultyCoef();
                // 活动结束
                endCallBack(counterAttack);
            } else { // 当前boss死掉了, 生成下一个
                config = StaticWorldDataMgr
                        .getCounterAttackTypeMapByCond(WorldConstant.COUNTER_ATK_DEF, counterAttack.incrBossCnt());
                if (!CheckNull.isNull(config)) {
                    counterAttack.setFighter(createCounterAtkFighter(config.getNpcForm()));
                }
            }
        }

        // 日志记录
        warService.logBattle(battle, fightLogic.getWinState(),attacker,defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
        // 部队返回
        warService.retreatBattleArmy(battle, sendMailTime);
    }

    /**
     * 增加难度系数
     */
    private void difficultyCoef() {
        Map<Integer, Integer> coefMap = globalDataManager.getGameGlobal()
                .getMixtureDataById(GlobalConstant.FORM_COEF_OF_DIFFICULTY);
        int coef = coefMap
                .getOrDefault(GlobalConstant.CoefDifficulty.COUNTER_ATTACK, 100);
        coef = (int) Math.floor(coef * (WorldConstant.COUNTER_ATK_FORM_COEF / Constant.HUNDRED));
        coefMap.put(GlobalConstant.CoefDifficulty.COUNTER_ATTACK, coef);
        LogUtil.error("反攻德意志加强后的难度系数, coef:", coef);
        // 埋点boss的难度系数
        LogLordHelper.otherLog("counterAtkDifficulty", DataResource.ac.getBean(ServerSetting.class).getServerID(), coef);
        globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.FORM_COEF_OF_DIFFICULTY, coefMap);
    }

    /**
     * BOSS进攻玩家战斗处理
     *
     * @param battle
     * @param now
     * @param counterAttack
     */
    private void bossAtkBattleLogic(Battle battle, int now, CounterAttack counterAttack) {
        int sendMailTime = now + 1;
        warService.addCityDefendRoleHeros(battle);// 防守者,兵力 添加
        Fighter attacker = createCounterAtkFighter(
                counterAttack.getAtkFormList()); // 当前进攻的阵型
        Fighter defender = fightService.createCampBattleDefencer(battle, null);
        LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker,false,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,false,true);

        boolean defSuc = !(fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS);

        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        if (defender.lost > 0) { // 扣兵处理
            warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.COUNTER_BOSS_ATK);
        }
        // 记录玩家获得的积分
        Map<Long, Integer> creditMap = new HashMap<>();
        if (defender.hurt > 0) {
            // 玩家杀敌统计
            Map<Long, Integer> roleKill = defender.forces.stream()
                    .filter(f -> f.killed > 0 && f.roleType == Constant.Role.PLAYER).collect(Collectors
                            .toMap(f -> f.ownerId, f -> f.killed, (existVal, newVal) -> existVal + newVal));
            // 计算积分
            addCredit(creditMap, roleKill, WorldConstant.COUNTER_ATTACK_CREDIT.get(1), counterAttack);
        }

        long defRoleId = battle.getDefencerId();
        Player defPlayer = playerDataManager.getPlayer(defRoleId);

        // 城墙部队返回
        wallService.retreatArmy(defPlayer.lord.getPos(), !defSuc, battle.getDefencer());

        // 回兵逻辑
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

        // 战报信息
        int currentAtkCnt = counterAttack.getCurrentAtkCnt();
        CommonPb.RptAtkBandit.Builder rpt = fightService
                .createRptBuilderPb(currentAtkCnt, attacker, defender, fightLogic, defSuc, defPlayer);
        CommonPb.Report.Builder report = worldService.createAtkBanditReport(rpt.build(), sendMailTime);

        // 防守成功, 所有参与防守的玩家获得防守成功积分
        if (defSuc) {
            Set<Long> defRoles = battle.getDefList().stream().map(br -> br.getRoleId()).distinct()
                    .collect(Collectors.toSet());
            int add = WorldConstant.COUNTER_ATTACK_CREDIT.get(2).get(0);
            if (add > 0) {
                for (Long roleId : defRoles) {
                    Player player = playerDataManager.getPlayer(roleId);
                    if (!CheckNull.isNull(player)) {
                        int credit = creditMap.getOrDefault(roleId, 0);
                        creditMap.put(roleId, credit + add);
                    }
                }
            }
        } else { // 防守失败(之前是要设置失火)
            // if (BuildingDataManager.getBuildingLv(BuildingType.WALL, defPlayer) > 0) {// 城墙建起之后才会有失火状态
            //     defPlayer.setFireState(true);// 设置失火状态
            // }
        }

        // 发送积分奖励
        sendCreditAawrd(creditMap);

        // 发送邮件
        sendBossAtkBattleMail(battle, report, defSuc, creditMap, sendMailTime, recoverArmyAwardMap, currentAtkCnt);
        LogLordHelper.commonLog("counterAtkBattle", AwardFrom.COUNTER_BOSS_ATK, defPlayer, defSuc);

        // 日志记录
        warService.logBattle(battle, fightLogic.getWinState(),attacker,defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
        // 部队返回
        warService.retreatBattleArmy(battle, sendMailTime);
    }

    /**
     * 发送反攻德意志的积分
     *
     * @param creditMap
     */
    private void sendCreditAawrd(Map<Long, Integer> creditMap) {
        if (CheckNull.isEmpty(creditMap)) {
            return;
        }
        for (Map.Entry<Long, Integer> en : creditMap.entrySet()) {
            long roleId = en.getKey();
            int add = en.getValue();
            sendCreditAward(add, roleId);
        }
    }

    /**
     * 发送积分奖励
     *
     * @param add
     * @param roleId
     */
    public void sendCreditAward(int add, long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (CheckNull.isNull(player) || add <= 0) {
            return;
        }
        int credit = player.getMixtureDataById(PlayerConstant.COUNTER_ATK_CREDIT);
        player.setMixtureData(PlayerConstant.COUNTER_ATK_CREDIT, add + credit);
        LogLordHelper.commonLog("counterAtkCredit", AwardFrom.COUNTER_FIGHT_AWARD, player, credit, add);
        //上报数数
        EventDataUp.credits(player.account,player.lord,add + credit,add, CreditsConstant.COUNTER_ATK,AwardFrom.COUNTER_FIGHT_AWARD);
    }

    /**
     * 计算玩家积分
     *
     * @param creditMap
     * @param force
     * @param cond
     * @param counterAttack
     */
    private void addCredit(Map<Long, Integer> creditMap, Force force, creditLogicCond cond,
                           CounterAttack counterAttack) {
        if (force.roleType == Constant.Role.CITY) {
            return;
        }
        long roleId = force.ownerId;
        Player player = playerDataManager.getPlayer(roleId);
        if (player == null) {
            LogUtil.error("计算积分，未找到玩家, roleId:", roleId);
            return;
        }
        counterAttack.getJoinBattleRole().add(roleId); // 加入参战队列
        int credit = creditMap.getOrDefault(roleId, 0);
        creditMap.put(roleId, credit + cond.calculateCredit(force));
    }

    /**
     * 计算玩家获得的积分
     *
     * @param creditMap
     * @param cntMap
     * @param creditConf
     * @param counterAttack
     */
    public void addCredit(Map<Long, Integer> creditMap, Map<Long, Integer> cntMap, List<Integer> creditConf,
                          CounterAttack counterAttack) {
        if (CheckNull.isEmpty(cntMap) || CheckNull.isEmpty(creditConf)) {
            return;
        }
        int radio = creditConf.get(0);
        int val = creditConf.get(1);
        cntMap.entrySet().stream().forEach(en -> {
            long roleId = en.getKey();
            Integer cnt = en.getValue();
            if (cnt <= 0) {
                LogUtil.error("计算积分，玩家未杀敌 roleId:", roleId, ", cnt:", cnt);
                return;
            }
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.error("计算积分，未找到玩家, roleId:", roleId);
                return;
            }
            counterAttack.getJoinBattleRole().add(roleId); // 加入参战队列
            int oldVal = creditMap.getOrDefault(roleId, 0);

            int credit = (int) Math.floor(cnt / creditConf.get(0) * creditConf.get(1));
            creditMap.put(roleId, oldVal + credit);
        });

    }

    /**
     * 部队返回
     *
     * @param player
     * @param army
     * @param type
     */
    public void retreatBossAtkHelpArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        Integer battleId = army.getBattleId();
        if (battleId != null) {
            Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
            if (battle != null) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                battle.updateArm(camp, -armCount);
                worldService.removeBattleArmy(battle, player.roleId, army.getKeyId(), false); // 移除兵力
            }
        }
        worldService.retreatArmy(player, army, now, type);
    }

    /**
     * 积分计算逻辑
     */
    private interface creditLogicCond {

        int calculateCredit(Force force);
    }

    /**
     * 创建boss防守的战报
     *
     * @param counterAttack
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param beforeTotal
     * @param beforeLost
     * @param atkSuc
     * @return
     */
    private CommonPb.RptAtkPlayer.Builder createBossDefRptBuilderPb(CounterAttack counterAttack, Fighter attacker,
                                                                    Fighter defender, FightLogic fightLogic, int beforeTotal, int beforeLost, boolean atkSuc) {
        // 战斗记录
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setResult(atkSuc);
        // 记录双方汇总信息
        rpt.setDefCity(PbHelper.createRptCityPb(counterAttack.getCityId(), counterAttack.getPos()));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, 0, null, 0, 0));
        rpt.setDefSum(PbHelper.createRptSummary(beforeTotal - beforeLost, defender.lost - beforeLost, 0, null, -1, -1));
        for (Force force : attacker.forces) {
            CommonPb.RptHero rptHero = fightService.forceToRptHeroNoExp(force);
            if (rptHero != null) {
                rpt.addAtkHero(rptHero);
            }
        }
        for (Force force : defender.forces) {
            rpt.addDefHero(
                    PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force.id, null, 0, 0, force.lost));
        }
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setRecord(record);
        return rpt;
    }

    /**
     * 发送BOSS进攻邮件
     *
     * @param battle
     * @param report
     * @param defSuc
     * @param creditMap
     * @param now
     * @param recoverArmyAwardMap 损兵恢复
     * @param currentAtkCnt       当前进攻轮数
     */
    private void sendBossAtkBattleMail(Battle battle, CommonPb.Report.Builder report, boolean defSuc,
                                       Map<Long, Integer> creditMap, int now, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap,
                                       int currentAtkCnt) {
        List<Player> defPlayers = battle.getDefList().stream().map(br -> br.getRoleId()).distinct()
                .map(rId -> playerDataManager.getPlayer(rId)).filter(p -> p != null).collect(Collectors.toList());
        defPlayers.stream().forEach(p -> {
            int credit = creditMap.getOrDefault(p.roleId, 0);
            String nick = p.lord.getNick();
            Turple<Integer, Integer> xy = MapHelper.reducePos(p.lord.getPos());
            Object[] params = {nick, currentAtkCnt, xy.getA(), xy.getB(), credit};
            if (defSuc) { // 玩家防守成功
                mailDataManager.sendReportMail(p, report, MailConstant.MOLD_COUNTER_BOSS_ATK_FAIL, null, now,
                        recoverArmyAwardMap, params);
            } else {
                mailDataManager.sendReportMail(p, report, MailConstant.MOLD_COUNTER_BOSS_ATK_SUCC, null, now,
                        recoverArmyAwardMap, params);
            }
        });
    }

    /**
     * 发送BOSS防守邮件
     *
     * @param battle
     * @param now
     * @param counterAttack
     * @param recoverArmyAwardMap
     * @param creditMap
     * @param report
     */
    private void sendBossDefBattleMail(Battle battle, int now, CounterAttack counterAttack,
                                       Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, Map<Long, Integer> creditMap,
                                       CommonPb.Report.Builder report) {
        List<Player> atkPlayerList = battle.getAtkList().stream().map(br -> br.getRoleId()).distinct()
                .map(rId -> playerDataManager.getPlayer(rId)).filter(p -> p != null).collect(Collectors.toList());
        atkPlayerList.forEach(p -> {
            int credit = creditMap.getOrDefault(p.roleId, 0);
            Turple<Integer, Integer> xy = MapHelper.reducePos(p.lord.getPos());
            Turple<Integer, Integer> cityXY = MapHelper.reducePos(counterAttack.getPos());
            Object[] params = {p.lord.getNick(), p.lord.getNick(), xy.getA(), xy.getB(), counterAttack.getCityId(),
                    cityXY.getA(), cityXY.getB(), credit, counterAttack.getCurrentAtkCnt()};
            mailDataManager
                    .sendReportMail(p, report, MailConstant.MOLD_COUNTER_BOSS_DEF_JOIN, null, now, recoverArmyAwardMap,
                            params);
        });
    }

}
