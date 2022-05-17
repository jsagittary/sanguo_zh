package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.FeatureCategory;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.BerlinRecord;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.party.Official;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.HelpShengYuService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-16 19:20
 * @description: 柏林会战业务处理
 * @modified By:
 */

@Service
public class BerlinWarService {

    @Autowired
    private WarService warService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private HeroService heroService;

    @Autowired
    private FightService fightService;

    @Autowired
    private CampDataManager campDataManager;

    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private WarDataManager warDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private SolarTermsDataManager solarTermsDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private HonorDailyDataManager honorDailyDataManager;

    @Autowired
    private ChatDataManager chatDataManager;

    @Autowired
    private CampService campService;

    @Autowired
    private BattlePassDataManager battlePassDataManager;

    @Autowired
    private RoyalArenaService royalArenaService;

    @Autowired
    private MedalDataManager medalDataManager;

    @Autowired
    private CastleSkinProcessService castleSkinProcessService;

    @Autowired
    private DressUpDataManager dressUpDataManager;

    @Autowired
    private ActivityTriggerService activityTriggerService;
    @Autowired
    private HelpShengYuService helpShengYuService;

    /**
     * 获取柏林会战信息
     *
     * @param roleId 角色Id
     * @return 柏林会战的信息
     * @throws MwException 自定义异常
     */
    public GamePb4.BerlinInfoRs berlinInfo(Long roleId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.BerlinInfoRs.Builder builder = GamePb4.BerlinInfoRs.newBuilder();
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "获取柏林信息时,柏林会战对象未初始化");
        }
        Turple<Integer, Long> curWinner = BerlinWar.getCurWinner();
        if (curWinner != null) {
            Player curWinnerPlayer = playerDataManager.getPlayer(curWinner.getB());
            if (curWinnerPlayer != null) {
                builder.setKingNick(curWinnerPlayer.lord.getNick());
                builder.setKingCamp(curWinnerPlayer.lord.getCamp());
            }
        }
        builder.setStatus(berlinWar.getStatus());
        if (!CheckNull.isNull(berlinWar.getPreViewDate()) && !CheckNull.isNull(berlinWar.getBeginDate()) && !CheckNull
                .isNull(berlinWar.getEndDate())) {
            builder.setPreViewDate((int) (berlinWar.getPreViewDate().getTime() / TimeHelper.SECOND_MS));
            builder.setBeginDate((int) (berlinWar.getBeginDate().getTime() / TimeHelper.SECOND_MS));
            builder.setEndDate((int) (berlinWar.getEndDate().getTime() / TimeHelper.SECOND_MS));
        }
        BerlinCityInfo cityInfo = berlinWar.getBerlinCityInfo();
        if (!CheckNull.isNull(cityInfo)) {
            builder.setCamp(cityInfo.getCamp());
            builder.addAllOccupyTime(cityInfo.getCampOccupy());
            builder.setWinOfCountdown(cityInfo.getWinOfCountdown());
        }
        builder.setNow(TimeHelper.getCurrentSecond());
        builder.setUnLock(checkUnLock());
        builder.setScheduleId(berlinWar.getScheduleId());
        builder.setBbf(berlinWar.serBBF());
        return builder.build();
    }

    /**
     * 获取城池或者据点信息
     *
     * @param roleId 角色Id
     * @param rq     请求参数
     * @throws MwException 自定义异常
     */
    public GamePb4.BerlinCityInfoRs berlinCityInfo(Long roleId, GamePb4.BerlinCityInfoRq rq) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.BerlinCityInfoRs.Builder builder = GamePb4.BerlinCityInfoRs.newBuilder();
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "获取柏林信息时,柏林会战对象未初始化");
        }
        int cityId = rq.getCityId();
        BerlinCityInfo cityInfo;
        cityInfo = berlinWar.getCityInfoByCityId(cityId);
        if (CheckNull.isNull(cityInfo)) {
            throw new MwException(GameError.BERLIN_CITY_INFO_NOT_INIT.getCode(), "获取柏林据点信息时,berlinCityInfo对象未初始化");
        }
        builder.setCamp(cityInfo.getCamp());
        Integer time = cityInfo.getStatusTime().get(cityInfo.getCamp());
        int occuptTime = CheckNull.isNull(time) ? 0 : time;
        int now = TimeHelper.getCurrentSecond();
        occuptTime += now - cityInfo.getLastOccupyTime();
        builder.setOccupyTime(occuptTime);
        builder.setAtkArmy(cityInfo.getAtkArm());
        builder.setDefArmy(cityInfo.getDefArm());
        BerlinRoleInfo roleInfo = berlinWar.getRoleInfo(roleId);
        builder.setInfo(PbHelper.createBerlinRoleInfo(roleInfo));
        getHeroBattleOrder(roleId, player, builder, cityInfo, roleInfo);
        return builder.build();
    }

    /**
     * 柏林将领数据(将领在柏林的战斗队列)
     *
     * @param roleId   角色Id
     * @param player   玩家对象
     * @param builder  Builder构造器
     * @param cityInfo 城池信息
     * @param roleInfo 柏林玩家信息
     */
    private void getHeroBattleOrder(Long roleId, Player player, BerlinCityInfoRs.Builder builder,
                                    BerlinCityInfo cityInfo, BerlinRoleInfo roleInfo) {
        // 上阵将领的状态在柏林会战
        player.getAllOnBattleHeros().stream().filter(hero -> hero.getState() == ArmyConstant.ARMY_BERLIN_WAR)
                .forEach(hero -> {
                    CommonPb.BerlinHeroInfo.Builder info = CommonPb.BerlinHeroInfo.newBuilder();
                    BerlinForce force = cityInfo.getRoleQueue().stream().filter(Force::alive)
                            .filter(f -> f.ownerId == roleId && f.id == hero.getHeroId()).findFirst().orElse(null);
                    if (CheckNull.isNull(force)) {
                        return;
                    }
                    info.setHeroId(hero.getHeroId());
                    info.setCnt(roleInfo.getCntByType(hero.getHeroId()));
                    int atkOrDef = player.lord.getCamp() == cityInfo.getCamp() ?
                            WorldConstant.BERLIN_DEF :
                            WorldConstant.BERLIN_ATK;
                    List<BerlinForce> heroQueue = cityInfo.getRoleQueue().stream().filter(Force::alive)
                            .filter(f -> f.getAtkOrDef() == atkOrDef)
                            .sorted(Comparator.comparingInt(BerlinForce::getImmediatelyTime)
                                    .thenComparing(Comparator.comparingInt(BerlinForce::getAddMode).reversed())
                                    .thenComparingLong(BerlinForce::getAddTime))
                            .collect(Collectors.toList());
                    if (CheckNull.isEmpty(heroQueue)) {
                        return;
                    }
                    int index = heroQueue.indexOf(force);
                    if (index == -1) {
                        return;
                    }
                    info.setStatus(getImmediatelyStatus(cityInfo, force));
                    info.setOrder(index + 1);
                    builder.addHeroInfo(info);
                });
    }

    /**
     * 获取将领立即出击状态
     *
     * @param cityInfo 城池信息
     * @param force    军队
     * @return 0 不可以, 1 可以发起,2 已经发起
     */
    private int getImmediatelyStatus(BerlinCityInfo cityInfo, BerlinForce force) {
        int status;
        if (cityInfo.isNpcCity() || StaticBerlinWarDataMgr.getBerlinSettingById(cityInfo.getCityId()).getType()
                == StaticBerlinWarDataMgr.BATTLEFRONT_TYPE) {
            status = 0;
        } else if (force.getImmediatelyTime() > 0) {
            status = 2;
        } else {
            status = 1;
        }
        return status;
    }

    /**
     * 将领立即出击
     *
     * @param roleId 角色Id
     * @param heroId 将领Id
     * @throws MwException 自定义异常
     */
    public void immediatelyAttack(Long roleId, int heroId) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 将领是否存在
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        if (hero.getState() != ArmyConstant.ARMY_BERLIN_WAR) {
            throw new MwException(GameError.BERLIN_HERO_STATUS_ERROR.getCode(), "将领立即出击时,将领状态错误 roleId:", roleId,
                    ", heroId:", heroId, ", status:", hero.getState());
        }
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "将领立即出击时,柏林会战对象未初始化");
        }

        BerlinCityInfo cityInfo = berlinWar.getBerlinCityInfo();
        BerlinForce force = cityInfo.getRoleQueue().stream().filter(Force::alive)
                .filter(f -> f.ownerId == roleId && f.id == hero.getHeroId()).findFirst().orElse(null);
        if (!CheckNull.isNull(force)) {
            int immediatelyTime = force.getImmediatelyTime();
            if (immediatelyTime > 0) {
                throw new MwException(GameError.BERLIN_HERO_STATUS_ERROR.getCode(), "将领立即出击时,将领已经立即出击 roleId:", roleId,
                        ", heroId:", heroId, ", immediatelyTime:", immediatelyTime);
            }
            BerlinRoleInfo roleInfo = berlinWar.getRoleInfo(roleId);
            int cnt = roleInfo.getCntByType(heroId);
            int consume = WorldConstant.getConsumeByCnt(WorldConstant.BERLIN_IMMEDIATELY_CNT_CONSUME, cnt);
            if (consume > 0) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, consume,
                        AwardFrom.IMMEDIATELY_ATTACK);
                roleInfo.updateCnt(heroId, cnt + 1);
                force.setImmediatelyTime(now);

                //更新助力圣域活动
                helpShengYuService.updateProgress(player, consume);
            }
        }
    }

    /**
     * 立即恢复复活CD
     *
     * @param roleId 角色Id
     * @return 返回的协议
     * @throws MwException 自定义异常
     */
    public ResumeImmediatelyRs resumeImmediately(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "立即恢复将领CD时,柏林会战对象未初始化");
        }
        BerlinRoleInfo roleInfo = berlinWar.getRoleInfo(roleId);
        int atkCD = roleInfo.getAtkCD();
        if (atkCD == 0 || atkCD <= now) {
            throw new MwException(GameError.BERLIN_RESURRECTION_IS_END.getCode(), "立即恢复将领CD时, 没有可恢复的CD, atkCD:", atkCD);
        }
        int resuCNt = roleInfo.getStatus(BerlinWarConstant.RoleInfo.RESURRECTION_CNT);
        // 根据次数获取消耗的金币
        int consume = WorldConstant.getConsumeByCnt(WorldConstant.BERLIN_RESURRECTION_CNT_CONSUME, resuCNt);
        if (consume > 0) {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, consume,
                    AwardFrom.RESUME_IMMEDIATELY);
            roleInfo.updateStatus(BerlinWarConstant.RoleInfo.RESURRECTION_CNT, resuCNt + 1);
            roleInfo.setAtkCD(0);
            roleInfo.setFreeCDTime(now + WorldConstant.FREE_BERLIN_RESURRECTION_CD_TIME); // 免CD时间节点
        }
        //更新助力圣域活动
        helpShengYuService.updateProgress(player, consume);

        ResumeImmediatelyRs.Builder builder = ResumeImmediatelyRs.newBuilder();
        builder.setResurrectionCnt(roleInfo.getStatus(BerlinWarConstant.RoleInfo.RESURRECTION_CNT));
        return builder.build();
    }

    /**
     * 战前buff
     *
     * @param roleId 角色Id
     * @param req    请求参数
     * @return 返回协议
     */
    public PrewarBuffRs prewarBuff(long roleId, PrewarBuffRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "获取或升级战前buff时, 柏林会战对象未初始化");
        }
        BerlinRoleInfo roleInfo = berlinWar.getRoleInfo(roleId);
        if (req.hasType() && req.hasCostType()) {
            if (!DateHelper.isInTime(new Date(), berlinWar.getPrewarBuffDate(), berlinWar.getEndDate())) {
                throw new MwException(GameError.BERLIN_PREWAR_BUFF_NOT_OPEN.getCode(), "获取或升级战前buff时, 不在战前buff的开放时间");
            }
            // buff类型
            int type = req.getType();
            // 花费类型: 1 资源消耗, 2 金币消耗
            int costType = req.getCostType();
            // 资源购买次数
            int cnt = roleInfo.getCntByType(type);
            // 等级
            int level = roleInfo.getStatus(type);
            StaticPrewarBuff OldPrewarBuff = StaticBerlinWarDataMgr.getPrewarBuff(type, level);
            if (CheckNull.isNull(OldPrewarBuff)) {
                throw new MwException(GameError.BERLIN_PREWAR_BUFF_NOT_CONF.getCode(), "升级战前buff时, 配置未找到. type:", type,
                        ", level:", level);
            }
            if (type == EffectConstant.PREWAR_ATTACK_EXT) {
                // 上次的获胜阵营不可购买穿甲buff
                Turple<Integer, Long> curWinner = BerlinWar.getCurWinner();
                if (Objects.nonNull(curWinner)) {
                    Player winner = playerDataManager.getPlayer(curWinner.getB());
                    if (Objects.nonNull(winner)) {
                        if (player.lord.getCamp() == winner.getCamp()) {
                            throw new MwException(GameError.BERLIN_PREWAR_BUFF_CANT_BUY.getCode(), "升级战前buff时, 获胜阵营不可购买. roleId:", roleId, ", level:", level);
                        }
                    }
                }
            }
            if (costType == 1) {
                if (cnt >= WorldConstant.BERLIN_PREWAR_BUFF_BUY_RES_CNT) {
                    throw new MwException(GameError.BERLIN_RES_BUY_CNT_MAX.getCode(), "升级战前buff时, 资源升级已达上限, max:",
                            WorldConstant.BERLIN_PREWAR_BUFF_BUY_RES_CNT);
                }
                List<List<Integer>> resourceCost = OldPrewarBuff.getResourceCost();
                if (CheckNull.isEmpty(resourceCost)) {
                    throw new MwException(GameError.BERLIN_PREWAR_BUFF_LEVEL_MAX.getCode(), "升级战前buff时, 已升到最大等级. type:",
                            type, ", level:", level);
                }
                rewardDataManager.checkAndSubPlayerRes(player, resourceCost, AwardFrom.BERLIN_PREWAR_BUFF_BUY);
                roleInfo.updateCnt(type, cnt + 1);
            } else {
                if (OldPrewarBuff.getGoldCost() == 0) {
                    throw new MwException(GameError.BERLIN_PREWAR_BUFF_LEVEL_MAX.getCode(), "升级战前buff时, 已升到最大等级. type:",
                            type, ", level:", level);
                }
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                        OldPrewarBuff.getGoldCost(), AwardFrom.BERLIN_PREWAR_BUFF_BUY);
                //更新助力圣域活动
                helpShengYuService.updateProgress(player, OldPrewarBuff.getGoldCost());
            }
            // buff升级成功
            if (RandomHelper.isHitRangeIn10000(OldPrewarBuff.getProbalility()) || costType == 2) {
                StaticPrewarBuff NewPrewarBuff = StaticBerlinWarDataMgr.getPrewarBuff(type, level + 1);
                if (Objects.isNull(NewPrewarBuff)) {
                    throw new MwException(GameError.BERLIN_PREWAR_BUFF_NOT_CONF.getCode(), "升级战前buff时, 配置未找到. type:", type, ", level:", level + 1);
                }
                // 更新战前buff
                updatePrewarBuff(player, berlinWar, roleInfo, NewPrewarBuff);
                // 检测解锁行军
                checkWalkSpeed(roleInfo, player, berlinWar);
            }
        }
        PrewarBuffRs.Builder builder = PrewarBuffRs.newBuilder();
        builder.addBuffStatus(PbHelper.createTwoIntPb(EffectConstant.PREWAR_ATK, roleInfo.getStatus(EffectConstant.PREWAR_ATK)));
        builder.addBuffStatus(PbHelper.createTwoIntPb(EffectConstant.PREWAR_DEF, roleInfo.getStatus(EffectConstant.PREWAR_DEF)));
        builder.addBuffStatus(PbHelper.createTwoIntPb(EffectConstant.PREWAR_LEAD, roleInfo.getStatus(EffectConstant.PREWAR_LEAD)));
        builder.addBuffStatus(PbHelper.createTwoIntPb(EffectConstant.PREWAR_WALK_SPEED, roleInfo.getStatus(EffectConstant.PREWAR_WALK_SPEED)));
        builder.addBuffStatus(PbHelper.createTwoIntPb(EffectConstant.PREWAR_ATTACK_EXT, roleInfo.getStatus(EffectConstant.PREWAR_ATTACK_EXT)));

        builder.addResCostCnt(PbHelper.createTwoIntPb(EffectConstant.PREWAR_ATK, roleInfo.getCntByType(EffectConstant.PREWAR_ATK)));
        builder.addResCostCnt(PbHelper.createTwoIntPb(EffectConstant.PREWAR_DEF, roleInfo.getCntByType(EffectConstant.PREWAR_DEF)));
        builder.addResCostCnt(PbHelper.createTwoIntPb(EffectConstant.PREWAR_LEAD, roleInfo.getCntByType(EffectConstant.PREWAR_LEAD)));
        builder.addResCostCnt(PbHelper.createTwoIntPb(EffectConstant.PREWAR_WALK_SPEED, roleInfo.getCntByType(EffectConstant.PREWAR_WALK_SPEED)));
        builder.addResCostCnt(PbHelper.createTwoIntPb(EffectConstant.PREWAR_ATTACK_EXT, roleInfo.getCntByType(EffectConstant.PREWAR_ATTACK_EXT)));
        return builder.build();
    }

    /**
     * 更新战前Buff
     *
     * @param player     玩家对象
     * @param berlinWar  柏林会战对象
     * @param roleInfo   柏林玩家信息
     * @param prewarBuff 战前buff对象
     */
    private void updatePrewarBuff(Player player, BerlinWar berlinWar, BerlinRoleInfo roleInfo,
                                  StaticPrewarBuff prewarBuff) {
        roleInfo.updateStatus(prewarBuff.getType(), prewarBuff.getLevel());
        player.getEffect().put(prewarBuff.getType(), new Effect(prewarBuff.getType(), prewarBuff.getEffect(),
                (int) (berlinWar.getEndDate().getTime() / TimeHelper.SECOND_MS)));
        if (prewarBuff.getType() != EffectConstant.PREWAR_WALK_SPEED) {
            CalculateUtil.reCalcBattleHeroAttr(player);
        }
    }

    /**
     * 检测行军Buff自动触发
     *
     * @param roleInfo  柏林玩家信息
     * @param player    玩家对象
     * @param berlinWar 柏林会战对象
     */
    private void checkWalkSpeed(BerlinRoleInfo roleInfo, Player player, BerlinWar berlinWar) {
        int walkLevel = roleInfo.getStatus(EffectConstant.PREWAR_WALK_SPEED);
        // 行军buff已经触发
        if (walkLevel > 0) {
            return;
        }
        StaticPrewarBuff marchBuff = StaticBerlinWarDataMgr.getPrewarBuff(EffectConstant.PREWAR_WALK_SPEED, 1);
        if (!CheckNull.isNull(marchBuff)) {
            List<Integer> need = marchBuff.getNeed();
            boolean flag = true;
            for (Integer buffId : need) {
                if (!flag) {
                    break;
                }
                StaticPrewarBuff buff = StaticBerlinWarDataMgr.getPrewarBuffById(buffId);
                int level = roleInfo.getStatus(buff.getType());
                if (level < buff.getLevel()) {
                    flag = false;
                }
            }
            // 需要的等级都达到了
            if (flag) {
                updatePrewarBuff(player, berlinWar, roleInfo, marchBuff);
            }
        }
    }

    /**
     * 获取最近战况信息
     *
     * @param req    请求参数
     * @param roleId 玩家Id
     * @throws MwException 自定义异常
     */
    public GamePb4.RecentlyBerlinReportRs recentlyReport(long roleId, RecentlyBerlinReportRq req) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.RecentlyBerlinReportRs.Builder builder = GamePb4.RecentlyBerlinReportRs.newBuilder();
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "获取柏林信息时,柏林会战对象未初始化");
        }
        int timestamp = req.getTimestamp();
        // 获取指定战况
        if (timestamp > 0) {
            CommonPb.Report report = berlinWar.getReportByTimestamp(timestamp);
            if (CheckNull.isNull(report)) {
                throw new MwException(GameError.BERLIN_REPORT_NOT_FOUND.getCode(), "获取柏林战报时,战况未找到,timestamp:",
                        timestamp);
            }
            builder.addRecentlyReport(report);
        } else {
            berlinWar.getReports().stream().sorted(Comparator.comparingInt(CommonPb.Report::getTime).reversed())
                    .limit(10).forEach(builder::addRecentlyReport);
            berlinWar.getReports().stream().filter(report -> {
                CommonPb.RptAtkPlayer atkPlayer = report.getRptPlayer();
                CommonPb.RptMan attack = atkPlayer.getAttack();
                CommonPb.RptMan defMan = atkPlayer.getDefMan();
                return roleId == attack.getRoleId() || !CheckNull.isNull(defMan) && roleId == defMan.getRoleId();
            }).sorted(Comparator.comparingInt(CommonPb.Report::getTime).reversed()).limit(10).forEach(builder::addMyReport);
        }
        return builder.build();
    }

    /**
     * 获取柏林排行榜数据
     *
     * @param roleId 角色Id
     * @throws MwException 自定义异常
     */
    public GamePb4.GetBerlinRankRs berlinRankInfo(long roleId) throws MwException {
        GamePb4.GetBerlinRankRs.Builder builder = GamePb4.GetBerlinRankRs.newBuilder();
        playerDataManager.checkPlayerIsExist(roleId);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "获取柏林信息时,柏林会战对象未初始化");
        }
        List<StaticBerlinWarAward> sAward = StaticBerlinWarDataMgr
                .getBerlinWarAwardByType(WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
        if (CheckNull.isEmpty(sAward)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "排行活动未开启配置错误 roleId:", roleId, ", rankType:",
                    WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
        }
        LinkedList<ActRank> streakRank = berlinWar.getPlayerRanks(WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
        final int[] tmpRank = {1};
        streakRank.stream().sorted(Comparator.comparingLong(ActRank::getRankValue).reversed()).limit(15)
                .forEach(actRank -> {
                    long lordId = actRank.getLordId();
                    Player rankPlayer = playerDataManager.getPlayer(lordId);
                    if (CheckNull.isNull(rankPlayer)) {
                        return;
                    }
                    StaticBerlinWarAward rankAward = StaticBerlinWarDataMgr.findStreakRankAward(tmpRank[0]++);
                    if (CheckNull.isNull(rankAward)) {
                        return;
                    }
                    Lord lord = rankPlayer.lord;
                    builder.addRankData(
                            PbHelper.createRankDataPb(lord, (int) actRank.getRankValue(), rankAward.getAward(), rankPlayer.getDressUp().getCurPortraitFrame()));
                });
        ActRank myRank = berlinWar.getPlayerRank(WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY, roleId);
        int rank = CheckNull.isNull(myRank) ? 0 : myRank.getRank();

        int integral = 0; // 当前可获得军费
        StaticBerlinWarAward streakRankAward = StaticBerlinWarDataMgr.findStreakRankAward(rank);
        // 连续击杀可获取军费
        if (!CheckNull.isNull(streakRankAward)) {
            integral += streakRankAward.getAward();
        }
        // 累积击杀可获取军费
        ActRank killRank = berlinWar.getPlayerRank(WorldConstant.BERLIN_RANK_KILL_ARMY_CNT, roleId);
        if (!CheckNull.isNull(killRank)) {
            StaticBerlinWarAward killRankAward = StaticBerlinWarDataMgr.findKillRankAward(killRank.getRankValue());
            if (!CheckNull.isNull(killRankAward)) {
                integral += killRankAward.getAward();
            }
        }
        builder.setRank(rank);
        builder.setRankSize(streakRank.size());
        builder.setIntegral(integral);
        return builder.build();
    }

    /**
     * 获取累积击杀数据
     *
     * @param roleId 角色Id
     * @throws MwException 自定义异常
     */
    public GamePb4.GetBerlinIntegralRs berlinIntegral(long roleId) throws MwException {
        GamePb4.GetBerlinIntegralRs.Builder builder = GamePb4.GetBerlinIntegralRs.newBuilder();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "获取柏林信息时 ,柏林会战对象未初始化");
        }
        BerlinRecord record = berlinWar.getBerlinRecord(roleId);
        int integral = 0;
        ActRank streakRank = berlinWar.getPlayerRank(WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY, roleId);
        final int rankNum = CheckNull.isNull(streakRank) ? 0 : streakRank.getRank();
        StaticBerlinWarAward streakAward = StaticBerlinWarDataMgr.findStreakRankAward(rankNum);
        if (!CheckNull.isNull(streakAward)) {
            integral += streakAward.getAward();
        }
        ActRank armyRank = berlinWar.getPlayerRank(WorldConstant.BERLIN_RANK_KILL_ARMY_CNT, roleId);
        long killArmy = 0;
        int killAward = 0;
        if (!CheckNull.isNull(armyRank)) {
            killArmy = armyRank.getRankValue();
            StaticBerlinWarAward armyAward = StaticBerlinWarDataMgr.findKillRankAward(killArmy);
            if (!CheckNull.isNull(armyAward)) {
                killAward = armyAward.getAward();
            }
        }
        builder.setIntegral(integral + killAward);
        builder.setHaveIntegral(player.getMilitaryExpenditure());
        builder.setKillArmy(killArmy);
        builder.setKillAward(killAward);
        builder.setExploit(record.getExploit());
        return builder.build();
    }

    /**
     * 加入柏林会战的据点战斗
     *
     * @param roleId 角色Id
     * @param req    请求参数
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GamePb4.AttackBerlinWarRs attackBerlinWar(long roleId, GamePb4.AttackBerlinWarRq req) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "获取柏林信息时,柏林会战对象未初始化");
        }
        if (berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN) {
            throw new MwException(GameError.BERLIN_WAR_NOT_OPEN.getCode(), "加入柏林会战时,活动未开启, status:",
                    berlinWar.getStatus());
        }
        if (player.lord.getArea() != WorldConstant.AREA_TYPE_13) {
            throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "不在皇城不能参与柏林会战, roleId:", roleId,
                    ", my area:", player.lord.getArea(), ", target area:", WorldConstant.AREA_TYPE_13);
        }
        int atkType = req.getAtkType();
        int cityId = req.getCityId();
        BerlinCityInfo cityInfo = berlinWar.getCityInfoByCityId(cityId);
        if (CheckNull.isNull(cityInfo)) {
            throw new MwException(GameError.BERLIN_CITY_INFO_NOT_INIT.getCode(), "加入柏林会战时,据点信息没找到, cityId:", cityId);
        }

        StaticBerlinWar staticBerlin = StaticBerlinWarDataMgr.getBerlinSettingById(cityId);
        int armyType = staticBerlin.getType() == StaticBerlinWarDataMgr.BERLIN_TYPE ?
                ArmyConstant.ARMY_TYPE_BERLIN_WAR :
                ArmyConstant.ARMY_TYPE_BATTLE_FRONT_WAR;

        BerlinRoleInfo roleInfo = berlinWar.getRoleInfo(roleId);
        // 攻打柏林才有复活CD
        if (roleInfo.getAtkCD() > now) {
            throw new MwException(GameError.BERLIN_RESURRECTION_NOT_END.getCode(), "加入柏林会战时,将领复活CD中, 复活时间:",
                    roleInfo.getAtkCD(), ", now:", now);
        }

        ArrayList<Integer> heroIdList = new ArrayList<>();
        // 检查出征将领信息
        int reqHeroId = req.getHeroId();
        heroIdList.add(reqHeroId);
        worldService.checkFormHeroBerlin(player, heroIdList);

        int armCount = 0;
        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }

        // 行军时间
        int marchTime = worldService.marchTime(player, cityInfo.getPos());
        if (staticBerlin.getType() == StaticBerlinWarDataMgr.BERLIN_TYPE) {
            double cityBuffer = worldDataManager
                    .getCityBuffer(PbHelper.createTwoIntPb(cityInfo.getCityId(), cityInfo.getCamp()),
                            WorldConstant.CityBuffer.MILITARY_RESTRICTED_ZONES, player.roleId);
            if (cityBuffer > 0) {
                marchTime = (int) Math.ceil(marchTime * (1 + cityBuffer));
            }
        }

        // 计算时间是否赶得上
        long endTime = berlinWar.getEndDate().getTime() / TimeHelper.SECOND_MS;
        if (now + marchTime > endTime) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "加入据点,赶不上时间, roleId:", roleId, ", pos:",
                    cityInfo.getPos() + ",行军时间=" + (now + marchTime) + ",结束倒计时=" + endTime);
        }

        // 计算补给
        int needFood = worldService.checkMarchFood(player, marchTime, armCount);
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);
        // 强袭加入扣金币
        if (atkType == WorldConstant.BERLIN_ATTACK_TYPE_PRESS) {
            // 根据强袭次数来消耗金币
            int pressCnt = roleInfo.getStatus(BerlinWarConstant.RoleInfo.PRESS_CNT);
            int consume = WorldConstant.getConsumeByCnt(WorldConstant.BERLIN_PRESS_CNT_CONSUME, pressCnt);
            if (consume > 0) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, consume,
                        AwardFrom.ATK_POS);
                roleInfo.updateStatus(BerlinWarConstant.RoleInfo.PRESS_CNT, pressCnt + 1);

                //更新助力圣域活动
                helpShengYuService.updateProgress(player, consume);
            }
        }

        List<CommonPb.TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
        }

        Army army = new Army(player.maxKey(), armyType, cityInfo.getPos(), ArmyConstant.ARMY_STATE_MARCH, form,
                marchTime, now + marchTime, player.getDressUp());
        army.setBattleId(atkType);
        army.setLordId(roleId);
        army.setTargetId(cityId);
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        player.armys.put(army.getKeyId(), army);

        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // 推送区数据改变
        List<Integer> posList = new ArrayList<>();
        posList.add(cityInfo.getPos());
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, roleId,
                Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        GamePb4.AttackBerlinWarRs.Builder builder = GamePb4.AttackBerlinWarRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setAtkArmy(cityInfo.getAtkArm());
        builder.setDefArmy(cityInfo.getDefArm());
        builder.setPressCnt(roleInfo.getStatus(BerlinWarConstant.RoleInfo.PRESS_CNT));

        //Berlin派兵埋点
        LogLordHelper.commonLog("BerlinWarAttack", AwardFrom.BERLIN_WAR_ATTACK, player, player.lord.getFight(), reqHeroId);

        //task
        TaskService.handleTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.BERLIN.getCategory());
        ActivityDiaoChanService.completeTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.BERLIN.getCategory());
        TaskService.processTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.BERLIN.getCategory());

        return builder.build();
    }

    /**
     * 柏林会战结算逻辑
     */
    public void colsingTimeLogic() {
        berlinClosingAward();
    }

    /**
     * 检测柏林占领时间,如果达到30分钟.则活动结束
     */
    public void checkWinOfOccupyTime() {
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            LogUtil.error("前线阵地对柏林炮轰， BerlinWar对象未初始化");
            return;
        }
        if (berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN) {
            LogUtil.error("柏林会战活动未开启");
            return;
        }
        BerlinCityInfo berlinCityInfo = berlinWar.getBerlinCityInfo();
        if (CheckNull.isNull(berlinCityInfo)) {
            LogUtil.error("柏林会战的柏林对象未初始化");
        }

        // 检测柏林战斗狂热
        checkBattleFrenzy(berlinWar);

        // 检测占领时间
        if (checkWinOfOccupyTime(berlinCityInfo)) {
            berlinWar.setStatus(WorldConstant.BERLIN_STATUS_CLOSE);
            berlinWar.setEndDate(new Date());
            berlinClosingAward();
        }
    }

    /**
     * 检测柏林占领时间是否达到30分钟
     *
     * @param berlinCityInfo 柏林的对象
     * @return 是否结束活动
     */
    private boolean checkWinOfOccupyTime(BerlinCityInfo berlinCityInfo) {
        boolean flag = false;
        for (int camp : Constant.Camp.camps) {
            int influence = berlinCityInfo.getCampInfluence(camp);
            // 阵营的势力值达到百分之百, 活动结束(这里存储的是万分比)
            if (influence >= Constant.TEN_THROUSAND) {
                flag = true;
            }
            // 之前的提前结束方法
            /*int occupyTime = berlinCityInfo.getCampOccupyTime(camp);
            // 柏林会战,柏林占领了30分钟,活动结束
            if (occupyTime >= WorldConstant.BERLIN_WIN_OF_TIME) {
                flag = true;
            }*/
        }
        return flag;
    }

    /**
     * 柏林会战活动结束,军费奖励发放
     */
    private void berlinClosingAward() {
        // 移除柏林会战本周期定时器
        ScheduleManager.getInstance().removeBerlinJob();
        int now = TimeHelper.getCurrentSecond();
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            LogUtil.error("创建柏林会战Fighter， BerlinWar对象未初始化");
            return;
        }
        if (berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_CLOSE) {
            LogUtil.error("柏林会战活动未结束");
            return;
        }
        LogUtil.error("柏林会战结束, 现在时间: ", DateHelper.getDateFormat1().format(new Date(now * 1000L)), ", 开启时间: ",
                DateHelper.getDateFormat1().format(berlinWar.getBeginDate()), ", 结束时间: ",
                DateHelper.getDateFormat1().format(berlinWar.getEndDate()), ", 预显示时间: ",
                DateHelper.getDateFormat1().format(berlinWar.getPreViewDate()));
        // 清除柏林会战购买过buff的玩家
        // cleanRoleBerlinPreBuff(berlinWar.getRoleInfos());
        // 清除柏林玩家数据
        berlinWar.getRoleInfos().clear();
        // 清除本周的日期信息
        berlinWar.setAtkCD(0);
        // 清除官职
        berlinWar.getBerlinJobs().clear();
        // 清除战况
        berlinWar.getReports().clear();
        LogUtil.common("-----------------清空柏林会战官职-----------------");
        BerlinCityInfo berlinInfo = berlinWar.getBerlinCityInfo();
        if (CheckNull.isNull(berlinInfo)) {
            LogUtil.error("柏林会战， BerlinInfo对象未初始化");
            return;
        }
        City berlin = worldDataManager.getCityById(berlinInfo.getCityId());
        if (CheckNull.isNull(berlin)) {
            LogUtil.error("柏林City对象未初始化");
        }

        // 柏林会战获胜条件,按照阵营的占领时间计算
        // int camp = berlinInfo.getCamp();
        // int influence = berlinInfo.getCampInfluence(camp);
        // int occupyTime = berlinInfo.getCampOccupyTime(camp);
        // for (int tmp : Constant.Camp.camps) {
        //     if (tmp == camp) {
        //         continue;
        //     }
        //     int tmpInfluence = berlinInfo.getCampInfluence(tmp);
        //     if (tmpInfluence > influence) {
        //         // 比较势力值
        //         camp = tmp;
        //     } else if (tmpInfluence == influence) {
        //         int val = berlinInfo.getCampOccupyTime(tmp);
        //         // 势力值相同, 比较占领时间
        //         if (val > occupyTime) {
        //             camp = tmp;
        //         }
        //     }
        // }

        List<CommonPb.CampOccupy> campOccupy = berlinInfo.getCampOccupy();
        CommonPb.CampOccupy winCampOc = campOccupy.stream()
                // 打印柏林会战结束时，各个阵营的势力值和战令时间
                .peek(co -> LogUtil.common("柏林会战结束时阵营结算，camp：", co.getCamp(), ", Influence: ", co.getInfluence(), ", Time: ", co.getTime()))
                .max(Comparator.comparingInt(CommonPb.CampOccupy::getInfluence).thenComparingInt(CommonPb.CampOccupy::getTime)).orElse(null);
        if (winCampOc == null) {
            LogUtil.error("柏林会战结束时阵营结算， winCampOc对象为null");
            return;
        }

        // 获胜的阵营
        int winCamp = winCampOc.getCamp();
        berlinInfo.setCamp(winCamp);
        berlin.setCamp(winCamp);


        // 更新战令的任务进度
        playerDataManager.getPlayerByCamp(winCamp).values().stream()
                .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, BuildingType.ENTER_AREA_13_COND))
                .forEach(p -> battlePassDataManager.updTaskSchedule(p.roleId, TaskType.COND_BERLIN_WIN_CNT, 1));
        // 清除NPC,玩家将领返回
        berlinInfo.getCityDef().clear();
        retreatBerlinArmy(now, berlinInfo);
        berlinWar.getBattlefronts().values().forEach(battleFront -> {
            battleFront.getCityDef().clear();
            retreatBerlinArmy(now, battleFront);
        });

        Official official = null;
        Camp party = campDataManager.getParty(winCamp);
        // 获胜方总司令
        if (!CheckNull.isNull(party)) {
            official = party.getOfficials().stream().filter(e -> e.getJob() == PartyConstant.Job.KING).findFirst()
                    .orElse(null);
        }
        if (!CheckNull.isNull(official)) {
            LogUtil.common("--------------霸主换届处理  , official:", official);
            // 将获胜方总司令加入到历史的获胜者
            chatDataManager.sendSysChat(ChatConst.CHAT_BERLIN_CLOSE, 0, 0, winCamp, official.getNick());
            changeBerlinWinner(now, berlinWar, official.getRoleId());
            try {
                Player officialP = playerDataManager.checkPlayerIsExist(official.getRoleId());
                StaticCity berlinCity = StaticWorldDataMgr.getCityMap().get(berlin.getCityId());
                if (!CheckNull.isNull(berlinCity)) {
                    // 添加并且检测军团补给
                    campService.addAndCheckPartySupply(officialP, PartyConstant.SupplyType.JOIN_BERLIN_WAR, berlinCity.getType());
                }
            } catch (MwException e) {
                LogUtil.error("添加并且检测军团补给", e);
            }
        }

        // 柏林发送军费奖励
        berlinSendReward(now, berlinWar, berlinInfo, winCamp);

        berlinWar.setStatus(WorldConstant.BERLIN_STATUS_CLOSE);
    }

    /**
     * 清除柏林会战购买过buff的玩家
     */
    private void cleanRoleBerlinPreBuff(Map<Long, BerlinRoleInfo> berlin) {
        for (Entry<Long, BerlinRoleInfo> kv : berlin.entrySet()) {
            Long roleId = kv.getKey();
            BerlinRoleInfo bri = kv.getValue();
            if (bri.getStatusData().size() > 0) {
                Player player = playerDataManager.getPlayer(roleId);
                if (player != null) {
                    Map<Integer, Effect> playerEffect = player.getEffect();
                    for (int effectType : bri.getStatusData().keySet()) {
                        playerEffect.remove(effectType);
                    }
                    // 重新计算战斗力
                    CalculateUtil.reCalcAllHeroAttr(player);

                    String effectTypeStr = bri.getStatusData().keySet().stream().map(Object::toString)
                            .collect(Collectors.joining(",", "[", "]"));
                    LogUtil.common("柏林会战结束清除玩家buff roleId:", player.roleId, ", 清除属性有:", effectTypeStr);
                }
            }
        }
    }

    /**
     * 柏林霸主换届
     *
     * @param now             现在的时间
     * @param berlinWar       柏林会战对象
     * @param curWinnerRoleId 当前的获胜者
     */
    void changeBerlinWinner(int now, BerlinWar berlinWar, final long curWinnerRoleId) {
        // 获得上一届霸主
        Turple<Integer, Long> perWinner = BerlinWar.getCurWinner();
        // 添加本届霸主
        Turple<Integer, Long> curWinner;
        berlinWar.getHistoryWinner().add(curWinner = new Turple<>(now, curWinnerRoleId));

        // 获得霸主头像
        Set<Integer> winerPortrait = StaticLordDataMgr.getPortraitByUnlock(StaticPortrait.UNLOCK_TYPE_WINNER).stream()
                .map(StaticPortrait::getId).collect(Collectors.toSet()); // 霸主头像

        SyncBerlinWinnerRs.Builder builder = SyncBerlinWinnerRs.newBuilder();
        builder.setCurWinnerRoleId(curWinner.getB());
        if (perWinner != null && !perWinner.getB().equals(curWinner.getB())) {
            // 通知旧霸主 你被取代了, 删除旧霸主头像,大地图特权显示
            Player perWinnerPlayer = playerDataManager.getPlayer(perWinner.getB());
            if (perWinnerPlayer != null) {
                StaticLordDataMgr.getPortraitByUnlock(StaticPortrait.UNLOCK_TYPE_WINNER)
                        .stream()
                        .map(StaticPortrait::getId)
                        .forEach(portrait -> dressUpDataManager.subDressUp(perWinnerPlayer, AwardType.PORTRAIT, portrait, 0, AwardFrom.COMMON));
                // 移除霸主城堡皮肤
                dressUpDataManager.subDressUp(perWinnerPlayer, AwardType.CASTLE_SKIN, StaticCastleSkin.BERLIN_WINNER_SKIN_ID, 0, AwardFrom.COMMON);
                // 移除霸主头像日志
                LogLordHelper.commonLog("berlinWinnerPortrait", AwardFrom.BERLIN_WINNER, perWinnerPlayer, 0);
                // 给旧霸主推送
                builder.setPreWinnerRoleId(perWinnerPlayer.roleId);
                builder.setPreWinnerPortrait(perWinnerPlayer.lord.getPortrait());
                syncBerlinWinner(perWinnerPlayer, builder);
            }
        }
        Player curWinnerPlayer = playerDataManager.getPlayer(curWinnerRoleId);
        if (perWinner == null || !perWinner.getB().equals(curWinner.getB())) { // 上一届不是自己
            // 通知新霸主
            // 加上头像
            for (int portraitId : winerPortrait) {
                rewardDataManager.addAward(curWinnerPlayer, AwardType.PORTRAIT, portraitId, 1, AwardFrom.BERLIN_WINNER);
            }
            // 加上城堡皮肤
            rewardDataManager.addAward(curWinnerPlayer, AwardType.CASTLE_SKIN, Constant.BERLIN_WINNER_SKIN_ID, 1, AwardFrom.BERLIN_WINNER);
        }
        //增加限时称号
        StaticTitle staticTitle = StaticLordDataMgr.getTitleMapById(StaticCastleSkin.BERLIN_WINNER_TITLE_ID);
        rewardDataManager.addAward(curWinnerPlayer, AwardType.TITLE, staticTitle.getId(), Math.toIntExact(staticTitle.getDuration()), AwardFrom.BERLIN_WINNER);
        // 通知新任霸主
        syncBerlinWinner(curWinnerPlayer, builder);
        //给新任霸主发送决战指令邮件奖励
        List<Award> awards = new ArrayList<>(PbHelper.createAwardsPb(WorldConstant.BERLIN_OVERLORD_COMPENSATION_AWARD));
        mailDataManager
                .sendAttachMail(curWinnerPlayer, awards, MailConstant.DECISIVE_BATTLE_AWARD, AwardFrom.BERLIN_WINNER,
                        TimeHelper.getCurrentSecond());
    }

    /**
     * 柏林发送军费奖励
     *
     * @param now        现在的时间
     * @param berlinWar  柏林会战对象
     * @param berlinInfo 柏林对象
     * @param berlinCamp 柏林获胜方
     */
    private void berlinSendReward(int now, BerlinWar berlinWar, BerlinCityInfo berlinInfo, int berlinCamp) {

        // Key: roleId, Val: 军费
        Map<Long, Integer> reward = new HashMap<>();

        // 柏林参战人员
        HashSet<Long> joinBerlinWar = berlinWar.getJoinBerlinWar();

        if (berlinCamp != Constant.Camp.NPC) {
            // 柏林占领军费获取计算
            berlinCityAward(berlinInfo, berlinCamp, joinBerlinWar, reward, now, false);

            // 前线阵地军费获取计算
            berlinWar.getBattlefronts().values().forEach(battleFront -> berlinCityAward(battleFront, 0, joinBerlinWar, reward, now, true));
        }

        // 更新参与玩家的战令任务进度
        joinBerlinWar.stream().map(playerDataManager::getPlayer)
                .filter(p -> !CheckNull.isNull(p))
                .forEach(p -> {
                    battlePassDataManager.updTaskSchedule(p.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, WorldConstant.CITY_TYPE_KING);
                    royalArenaService.updTaskSchedule(p.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, WorldConstant.CITY_TYPE_KING);
                });

        // 连续击杀排行榜奖励军费
        LinkedList<ActRank> streatRank = berlinWar.getPlayerRanks(WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
        List<StaticBerlinWarAward> streatRankAwards = StaticBerlinWarDataMgr
                .getBerlinWarAwardByType(WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
        if (CheckNull.isEmpty(streatRankAwards)) {
            LogUtil.error("柏林会战连续击杀排行榜奖励未配置");
            return;
        }
        final int[] sRank = {1};
        streatRank.stream().sorted(Comparator.comparingLong(ActRank::getRankValue).reversed()).forEach(actRank -> {
            long lordId = actRank.getLordId();
            Player player = playerDataManager.getPlayer(lordId);
            if (CheckNull.isNull(player)) {
                LogUtil.error("连续击杀排行榜奖励军费,玩家不存在roleId: ", lordId);
                return;
            }
            StaticBerlinWarAward streakRankAward = StaticBerlinWarDataMgr.findStreakRankAward(sRank[0]++);
            if (!CheckNull.isNull(streakRankAward)) {
                int val = reward.getOrDefault(player.roleId, 0);
                LogUtil.debug("柏林会战军费奖励,[连续杀敌] roleId:", player.roleId, ", 排名名次:", sRank[0] - 1, ", 连续杀敌:",
                        actRank.getRankValue(), ", 获得的军费:", streakRankAward.getAward());

                reward.put(player.roleId, val + streakRankAward.getAward());
                LogLordHelper.commonLog("berlinStreakKillAward", AwardFrom.BERLIN_WAR_ATTACK, player, sRank[0] - 1, actRank.getRankValue(), streakRankAward.getAward());
            }
        });

        // 累积击杀排行榜奖励军费
        LinkedList<ActRank> killRank = berlinWar.getPlayerRanks(WorldConstant.BERLIN_RANK_KILL_ARMY_CNT);
        List<StaticBerlinWarAward> killRankAwards = StaticBerlinWarDataMgr
                .getBerlinWarAwardByType(WorldConstant.BERLIN_RANK_KILL_ARMY_CNT);
        if (CheckNull.isEmpty(killRankAwards)) {
            LogUtil.error("柏林会战累积击杀排行榜奖励未配置");
            return;
        }
        killRank.forEach(actRank -> {
            long rankValue = actRank.getRankValue();
            long lordId = actRank.getLordId();
            Player player = playerDataManager.getPlayer(lordId);
            if (CheckNull.isNull(player)) {
                LogUtil.error("累积击杀排行榜奖励军费,玩家不存在roleId: ", lordId);
            }
            StaticBerlinWarAward berlinWarAward = StaticBerlinWarDataMgr.findKillRankAward(rankValue);
            if (!CheckNull.isNull(berlinWarAward)) {
                int val = reward.getOrDefault(player.roleId, 0);
                LogUtil.debug("柏林会战军费奖励,[累积杀敌] roleId:", player.roleId, ", 累积杀敌:", actRank.getRankValue(), ", 获得的军费:",
                        berlinWarAward.getAward());
                reward.put(player.roleId, val + berlinWarAward.getAward());
                LogLordHelper.commonLog("berlinKillAward", AwardFrom.BERLIN_WAR_ATTACK, player, actRank.getRankValue(), berlinWarAward.getAward());
            }
        });

        try {
            Date origin = new Date();
            Date nextWeek = DateHelper.afterDayTimeDate(origin, 8);
            List<String> beginCronDate = WorldConstant
                    .getBerlinCronInfo(WorldConstant.BERLIN_CRON_DATE, WorldConstant.BERLIN_BEGIN_CRON);
            List<String> beginCronWeek = WorldConstant
                    .getBerlinCronInfo(WorldConstant.BERLIN_CRON_WEEK, WorldConstant.BERLIN_BEGIN_CRON);
            if (!CheckNull.isEmpty(beginCronDate) && !CheckNull.isEmpty(beginCronWeek)) {
                String beginTime = beginCronDate.get(0);
                String endTime = beginCronDate.get(1);
                int week = Integer.parseInt(beginCronWeek.get(0));
                long beginDate = DateHelper.afterStringTime(nextWeek, beginTime, week).getTime() / TimeHelper.SECOND_MS;
                long endDate = DateHelper.afterStringTime(nextWeek, endTime, week).getTime() / TimeHelper.SECOND_MS;
                // 给全服玩家发送占领柏林的邮件
                playerDataManager.getPlayers().values().forEach(player -> {
                    int preAward = reward.getOrDefault(player.roleId, 0);
                    BerlinRecord record = berlinWar.getBerlinRecord(player.roleId);
                    int exploit = record.getExploit(); // 获取的军功

                    //赛季天赋优化， 积分加成
                    int awardCountBuff = (int) (preAward * (1 + (DataResource.getBean(SeasonTalentService.class).
                            getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND)));

                    // 记录军费增加
                    LogLordHelper.commonLog("expenditure", AwardFrom.BERLIN_WAR_ATTACK, player.account, player.lord, player.getMilitaryExpenditure(), awardCountBuff);
                    // 军费奖励邮件
                    player.addMilitaryExpenditure(awardCountBuff);

                    mailDataManager
                            .sendNormalMail(player, MailConstant.MOLD_BERLIN_WAR_REWARD, now, berlinCamp,
                                    preAward, preAward, exploit, DataResource.getBean(SeasonTalentService.class).
                                            getSeasonTalentIdStr(player, SeasonConst.TALENT_EFFECT_620), awardCountBuff - preAward);
                    //上报数数
                    EventDataUp.credits(player.account, player.lord, player.getMilitaryExpenditure(), awardCountBuff, CreditsConstant.BERLIN, AwardFrom.BERLIN_WAR_ATTACK);

                    List<CommonPb.Award> awards = new ArrayList<>();
                    // 发送占领方,和失败方邮件奖励
                    if (player.lord.getCamp() == berlinCamp) {
                        List<Integer> sucAward = WorldConstant.BERLIN_JOIN_BATTLE_REWARD.get(0);
                        if (CheckNull.isEmpty(sucAward)) {
                            return;
                        }
                        awards.add(PbHelper.createAwardPb(sucAward.get(0), sucAward.get(1), sucAward.get(2)));
                        if (!CheckNull.isEmpty(awards)) {
                            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_BERLIN_SUC_CAMP,
                                    AwardFrom.BERLIN_SUC_AWARD, now);
                        }
                    } else {
                        List<Integer> failAward = WorldConstant.BERLIN_JOIN_BATTLE_REWARD.get(1);
                        if (CheckNull.isEmpty(failAward)) {
                            return;
                        }
                        awards.add(PbHelper.createAwardPb(failAward.get(0), failAward.get(1), failAward.get(2)));
                        if (!CheckNull.isEmpty(awards)) {
                            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_BERLIN_FAIL_CAMP,
                                    AwardFrom.BERLIN_FAIL_AWARD, now);
                        }
                    }
                    // 活动结束邮件
                    mailDataManager.sendNormalMail(player, MailConstant.MOLD_NEXT_BERLIN_PREVIEW, now, beginDate, endDate);

                    //berlin结束后奖励埋点
                    LogLordHelper.commonLog("BerlinWarJoinAward", AwardFrom.BERLIN_WAR_JOINAWARD, player, preAward);
                });
            }
        } catch (Exception e) {
            LogUtil.error(e, "发送柏林会战奖励出错!");
        }

    }

    /**
     * 柏林城池占领后军费结算
     *
     * @param berlinInfo    柏林城池对象
     * @param berlinCamp    柏林当前阵营
     * @param joinBerlinWar 参与者
     * @param reward        奖励
     * @param now           现在的时间
     * @param filterJoinWar 过滤参战人员
     */
    private void berlinCityAward(BerlinCityInfo berlinInfo, int berlinCamp, HashSet<Long> joinBerlinWar,
                                 Map<Long, Integer> reward, int now, boolean filterJoinWar) {
        // 柏林会战获取阵营,所有玩家增加军费
        StaticBerlinWar staticBerlin = StaticBerlinWarDataMgr.getBerlinSettingById(berlinInfo.getCityId());
        if (Objects.isNull(staticBerlin)) {
            LogUtil.error("柏林会战配置错误");
            return;
        }
        // 当前阵营参与柏林会战的玩家发送奖励
        int awardCamp = berlinCamp == 0 ? berlinInfo.getCamp() : berlinCamp;
        if (awardCamp == Constant.Camp.NPC) {
            return;
        }
        playerDataManager.getPlayerByCamp(awardCamp)
                .values()
                .forEach(player -> {
                    int val = reward.getOrDefault(player.roleId, 0);
                    LogUtil.debug("柏林会战军费奖励,[柏林城池] roleId:", player.roleId, ", cityId:", berlinInfo.getCityId(), ", 获得的军费:",
                            staticBerlin.getAward());
                    reward.put(player.roleId, val + staticBerlin.getAward());
                    LogLordHelper.commonLog("berlinCityAward", AwardFrom.BERLIN_WAR_ATTACK, player, berlinInfo.getCityId(), staticBerlin.getAward());
                });
    }

    /**
     * 通知霸主
     *
     * @param player  玩家对象
     * @param builder SyncBerlinWinnerRs的构造器
     */
    private void syncBerlinWinner(Player player, SyncBerlinWinnerRs.Builder builder) {
        Base.Builder msg = PbHelper
                .createSynBase(SyncBerlinWinnerRs.EXT_FIELD_NUMBER, SyncBerlinWinnerRs.ext, builder.build());
        if (player != null && player.isLogin && player.ctx != null) {
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 返回柏林活动所有将领
     */
    public void retreatBerlinAllArmy() {
        BerlinWar berlinWar = BerlinWar.getInstance();
        int currentSecond = TimeHelper.getCurrentSecond();
        BerlinCityInfo cityInfo = berlinWar.getBerlinCityInfo();
        if (!CheckNull.isNull(cityInfo)) {
            retreatBerlinArmy(currentSecond, cityInfo);
        }
        berlinWar.getBattlefronts().values().forEach(battleFront -> retreatBerlinArmy(currentSecond, battleFront));
        berlinWar.setStatus(WorldConstant.BERLIN_STATUS_CLOSE);
        // berlinWar.clearBerlinWar();
    }

    /**
     * 返回据点所有玩家将领
     *
     * @param now        现在的时间
     * @param berlinInfo 柏林的城池对象
     */
    private void retreatBerlinArmy(int now, BerlinCityInfo berlinInfo) {
        if (CheckNull.isNull(berlinInfo)) {
            LogUtil.error("撤回玩家将领时,berlinInfo为NUll");
            return;
        }
        // 柏林NPC守军清除
        berlinInfo.getCityDef().clear();
        Iterator<BerlinForce> iterator = berlinInfo.getRoleQueue().iterator();
        while (iterator.hasNext()) {
            // 活动结束,玩家将领返回
            BerlinForce berlinForce = iterator.next();
            retreatDeadArmy(now, berlinForce.ownerId, berlinForce.id);
            iterator.remove();
        }
    }

    /**
     * 返回柏林皇城战行军
     *
     * @param player 玩家对象
     * @param army   行军对象
     * @param type   召回的类型
     * @throws MwException 自定义异常
     */
    void retreatBerlinArmy(Player player, Army army, int type) throws MwException {
        if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // 主动召回
            worldService.retreatArmy(player, army, TimeHelper.getCurrentSecond(), type);
        } else if (army.getState() == ArmyConstant.ARMY_BERLIN_WAR) {
            throw new MwException(GameError.RETREAT_ARMY_NOT_FOUND.getCode(), "撤回部队，未找到部队信息, roleId:", player.roleId,
                    ", keyId:", army.getKeyId());
        }
    }

    /**
     * 返回柏林据点战行军
     *
     * @param player 玩家对象
     * @param army   行军对象
     * @param type   召回类型
     */
    void retreatBattleFrontArmy(Player player, Army army, int type) throws MwException {
        if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // 主动召回
            worldService.retreatArmy(player, army, TimeHelper.getCurrentSecond(), type);
        } else if (army.getState() == ArmyConstant.ARMY_BERLIN_WAR) {
            int cityId = army.getTargetId();
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (CheckNull.isNull(berlinWar)) {
                // 柏林对象未找到，部队返回
                throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "撤回部队, 未找到柏林对象");
            }

            BerlinCityInfo cityInfo = berlinWar.getCityInfoByCityId(cityId);
            if (CheckNull.isNull(cityInfo)) {
                // 据点对象未找到，部队返回
                throw new MwException(GameError.BERLIN_WAR_NOT_INIT.getCode(), "撤回部队, 未找到柏林City, cityId:", cityId);
            }

            // 将领信息
            CommonPb.TwoInt twoInt = army.getHero().get(0);

            // 将领返回, 并重新计算攻防兵力
            if (cityInfo.retreatArmy(player.roleId, twoInt.getV1())) {
                // 主动召回
                worldService.retreatArmy(player, army, TimeHelper.getCurrentSecond(), type);
                // 重新计算攻防兵力
                cityInfo.reCalcuAtkDefArm();
            }
        }
    }

    /**
     * 柏林会战的战斗逻辑
     */
    public void battleTimeLogic() {
        try {
            int now = TimeHelper.getCurrentSecond();
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (CheckNull.isNull(berlinWar)) {
                LogUtil.error("创建柏林会战Fighter， BerlinWar对象未初始化");
                return;
            }
            if (berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN) {
                LogUtil.error("柏林会战活动未开启");
                return;
            }
            BerlinCityInfo berlinInfo = berlinWar.getBerlinCityInfo();
            if (CheckNull.isNull(berlinInfo)) {
                LogUtil.error("创建柏林会战Fighter， BerlinInfo对象未初始化");
                return;
            }
            // 立即出击战斗逻辑
            immediatelyWarLogic(berlinInfo, now, berlinWar);
            // 柏林战斗逻辑
            berlinWarLogic(berlinInfo, now, berlinWar);
            for (BerlinCityInfo battlefrontInfo : berlinWar.getBattlefronts().values()) {
                // 前线战斗逻辑
                battlefrontWarLogic(battlefrontInfo, now, berlinWar);
            }
        } catch (Exception e) {
            LogUtil.error(e, "战争定时处理任务出现异常");
        }
    }

    /**
     * 前线阵地逻辑处理
     */
    public void battleFrontLogic() {
        try {
            int now = TimeHelper.getCurrentSecond();
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (CheckNull.isNull(berlinWar)) {
                LogUtil.error("前线阵地对柏林炮轰， BerlinWar对象未初始化");
                return;
            }
            if (berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN) {
                LogUtil.error("柏林会战活动未开启");
                return;
            }
            BerlinCityInfo berlinInfo = berlinWar.getBerlinCityInfo();
            if (CheckNull.isNull(berlinInfo)) {
                LogUtil.error("前线阵地对柏林炮轰， BerlinInfo柏林对象未初始化");
                return;
            }
            City berlin = worldDataManager.getCityById(berlinInfo.getCityId());
            if (CheckNull.isNull(berlin)) {
                LogUtil.error("柏林City对象未初始化");
                return;
            }
            StaticBerlinWar staticBerlinWar = StaticBerlinWarDataMgr.getBerlinSettingById(berlinInfo.getCityId());
            if (CheckNull.isNull(staticBerlinWar)) {
                LogUtil.error("柏林会战配置错误");
                return;
            }

            for (BerlinCityInfo battleFront : berlinWar.getBattlefronts().values()) {
                StaticBerlinWar staticBattleFront = StaticBerlinWarDataMgr
                        .getBerlinSettingById(battleFront.getCityId());
                if (CheckNull.isNull(staticBattleFront)) {
                    continue;
                }
                // 到达炮击时间
                if (now >= battleFront.getNextAtkTime() && battleFront.getNextAtkTime() != -1) {
                    int hurt = AOEAreaOfEffect(berlinInfo, battleFront);
                    battleFront.setNextAtkTime(now + currentFrontAtkCd());
                    // 阵地炮击推送
                    chatDataManager.sendSysChat(ChatConst.CHAT_BATTLEFRONT_ATK, 0, 0, battleFront.getCamp(),
                            battleFront.getCityId(), hurt
                            // 扩展参数
                            , battleFront.getNextAtkTime());
                    // 检测防守方失败
                    if (checkBerlinDefIsFail(berlinInfo, now, berlinWar, berlin, staticBerlinWar,
                            berlinInfo.isNpcCity(), battleFront.getCamp(), staticBattleFront.getDesc())) {
                        LogUtil.error(battleFront.getCamp(), "阵营的炮塔cityId:", battleFront.getCityId(), ", 占领了柏林");
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e, "炮塔定时处理任务出现异常");
        }
    }

    /**
     * 对所有的防守方造成百分比伤害
     *
     * @return 造成的总伤害
     */
    private int AOEAreaOfEffect(BerlinCityInfo cityInfo, BerlinCityInfo battleFront) {
        BerlinWar berlinwar = globalDataManager.getGameGlobal().getBerlinWar();
        int extHurt = WorldConstant.berlinAoeExtHurt(berlinwar.getScheduleId());
        int hurt = 0;
        cityInfo.atkCampConvertDef();
        LinkedList<Force> cityDef = cityInfo.getCityDef();
        if (!CheckNull.isEmpty(cityDef)) {
            hurt += cityDef.stream().filter(Force::alive).mapToInt(force -> {
                int cityHurt = (int) Math.ceil(force.hp * (currentFrontHurtCoef() / Constant.HUNDRED));
                // 世界进程额外伤害
                cityHurt += extHurt;
                cityHurt = force.count <= cityHurt ? force.count : cityHurt;
                force.lost = cityHurt;
                force.subHp(null);
                LogUtil.debug("柏林会战战斗日志, 城防将:", force.id, ", 剩余血量:", force.hp, ", 被AOE造成伤害:", cityHurt);
                LogLordHelper.otherLog("BerlinBattle", DataResource.ac.getBean(ServerSetting.class).getServerID(), "aoe", battleFront.getCityId(), 0, 0, battleFront.getCityId(), battleFront.getPos(), cityHurt, 0, 0, 0, 0, force.lost, force.count, force.id);
                cityInfo.subDefArm(cityHurt);
                return cityHurt;
            }).sum();
        }
        ArrayList<BerlinForce> roleQueue = cityInfo.getRoleQueue();
        if (!CheckNull.isEmpty(roleQueue)) {
            hurt += roleQueue.stream().filter(berlinForce -> berlinForce.getAtkOrDef() == WorldConstant.BERLIN_DEF)
                    .filter(Force::alive).mapToInt(berlinForce -> {
                        int defHurt = (int) Math.ceil(berlinForce.hp * (currentFrontHurtCoef() / Constant.HUNDRED));
                        // 世界进程额外伤害
                        defHurt += extHurt;
                        defHurt = berlinForce.count <= defHurt ? berlinForce.count : defHurt;
                        berlinForce.lost = defHurt;
                        // 扣除将领的兵力
                        subBattleHeroArm(berlinForce, defHurt, AwardFrom.BERLIN_WAR_ATTACK);
                        // 扣除柏林CityInfo对象中的Force兵力
                        cityInfo.subDefArm(defHurt);
                        berlinForce.subHp(null);
                        LogUtil.debug("柏林会战战斗日志, roleId: ", berlinForce.ownerId, ", 玩家的防守将领:", berlinForce.id, ", 剩余血量:", berlinForce.hp, ", 被AOE造成伤害:", defHurt);
                        LogLordHelper.otherLog("BerlinBattle", DataResource.ac.getBean(ServerSetting.class).getServerID(), "aoe", battleFront.getCityId(), berlinForce.ownerId, 0, battleFront.getCityId(), battleFront.getPos(), defHurt, 0, 0, 0, 0, berlinForce.lost, berlinForce.count, berlinForce.id);
                        // 投石车伤害也计入参战玩家
                        Optional.ofNullable(globalDataManager.getGameGlobal().getBerlinWar()).ifPresent(berlinWar -> berlinWar.getJoinBerlinWar().add(berlinForce.ownerId));
                        return defHurt;
                    }).sum();
        }
        return hurt;
    }

    /**
     * 扣除战斗中兵力消耗
     *
     * @param force  军队
     * @param subArm 消耗
     * @param from   消耗类型
     */
    public void subBattleHeroArm(Force force, int subArm, AwardFrom from) {
        if (CheckNull.isNull(force)) {
            return;
        }

        int lost;
        Hero hero;
        Player player;
        player = playerDataManager.getPlayer(force.ownerId);
        if (player == null) {
            LogUtil.error("扣除兵力，未找到玩家, roleId:", force.ownerId);
            return;
        }
        if (subArm > 0) {
            activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, subArm);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, subArm);
            // 战令的损兵进度
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, subArm);
            hero = player.heros.get(force.id);
            if (null == hero) {
                LogUtil.error("扣除兵力，未找到将领, heroId:", force.id);
                return;
            }

            lost = hero.subArm(subArm);
            ChangeInfo info = ChangeInfo.newIns();
            info.addChangeType(AwardType.HERO_ARM, hero.getHeroId());
            rewardDataManager.syncRoleResChanged(player, info); // 同步兵力

            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (Objects.nonNull(staticHero)) {
                LogLordHelper.heroArm(from, player.account, player.lord, hero.getHeroId(), hero.getCount(), -lost, staticHero.getType(),
                        Constant.ACTION_SUB);
            }
        }
        if (force.killed > 0) {
            // 大杀四方
            activityDataManager.updActivity(player, ActivityConst.ACT_BIG_KILL, force.killed, 0, true);
        }
    }

    /**
     * 前线阵地逻辑处理
     *
     * @param cityId 城池Id
     */
    @Deprecated
    public void battlefrontTimeLogic(int cityId) {
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            LogUtil.error("前线阵地对柏林炮轰， BerlinWar对象未初始化");
            return;
        }
        if (berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN) {
            LogUtil.error("柏林会战活动未开启");
            return;
        }
        BerlinCityInfo berlinInfo = berlinWar.getBerlinCityInfo();
        if (CheckNull.isNull(berlinInfo)) {
            LogUtil.error("前线阵地对柏林炮轰， BerlinInfo柏林对象未初始化");
            return;
        }
        BerlinCityInfo battlefront = berlinWar.getBattlefrontByCityId(cityId);
        if (CheckNull.isNull(battlefront)) {
            LogUtil.error("前线阵地对柏林炮轰, battlefront前线阵地对象未初始化, cityId:", cityId);
            return;
        }
        if (berlinInfo.getCamp() == battlefront.getCamp()) {
            LogUtil.debug("当前前线阵地跟柏林是相同阵营, berlinCamp:", berlinInfo.getCamp(), ", battlefrontCamp:",
                    battlefront.getCamp(), ", battlefrontCityId:", cityId);
        }
//        int hurt = AOEAreaOfEffect(berlinInfo, battleFront);
        // 阵地炮击推送
//        chatDataManager.sendSysChat(ChatConst.CHAT_BATTLEFRONT_ATK, 0, 0, berlinInfo.getCityId(), hurt);
    }

    /**
     * 立即出击战斗逻辑
     *
     * @param berlinInfo 柏林的城池
     * @param now        现在的时间
     * @param berlinWar  柏林会战对象
     */
    private void immediatelyWarLogic(BerlinCityInfo berlinInfo, int now, BerlinWar berlinWar) throws MwException {
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        SyncBasicReportRs.Builder builder = SyncBasicReportRs.newBuilder();
        City berlin = worldDataManager.getCityById(berlinInfo.getCityId());
        if (CheckNull.isNull(berlin)) {
            LogUtil.error("柏林City对象未初始化");
        }
        StaticBerlinWar staticBerlinWar = StaticBerlinWarDataMgr.getBerlinSettingById(berlinInfo.getCityId());
        if (CheckNull.isNull(staticBerlinWar)) {
            LogUtil.error("柏林会战配置错误");
            return;
        }
        // 检测将领死亡,将领返回并移除队列
        checkForceDead(berlinInfo);
        // 检测将领的攻防状态
        berlinInfo.atkCampConvertDef();

        // 立即出击进攻方
        BerlinForce atk = berlinInfo.getImmediatelyForce();
        if (CheckNull.isNull(atk)) {
            LogUtil.debug("本轮没有立即出击对战!");
            return;
        }
        Player atkPlayer = playerDataManager.checkPlayerIsExist(atk.ownerId);
        Lord atkLord = atkPlayer.lord;
        int berlinDef = atk.getCamp() == berlinInfo.getCamp() ? WorldConstant.BERLIN_ATK : WorldConstant.BERLIN_DEF;

        List<CommonPb.BerlinBasicReport> reportRs = new ArrayList<>();

        // 每轮最多打10场
        for (int i = 0; i < BerlinWarConstant.IMMEDIATELY_BATTLE_MAX_ROUND; i++) {
            if (!atk.alive()) {
                break;
            }
            // 立即出击防守方
            BerlinForce def = berlinInfo.getSingleForce(berlinDef);
            if (CheckNull.isNull(def)) {
                LogUtil.error("立即出击没有防守方!");
                break;
            }
            boolean isNpcCity = berlinInfo.isNpcCity();

            // 检测柏林防守失败
            if (atk.getCamp() != berlinInfo.getCamp() && checkBerlinDefIsFail(berlinInfo, now, berlinWar, berlin,
                    staticBerlinWar, isNpcCity, atk.getCamp(), atkLord.getNick())) {
                LogUtil.error("平局情况下, ", atk.getCamp(), "阵营的roleId:", atkLord.getLordId(), ", nick:", atkLord.getNick(),
                        ", 占领了柏林, cityId:", berlinInfo.getCityId());
                break;
            }

            Fighter attacker = createBerlinWarFighter(atk, false);
            Fighter defender = createBerlinWarFighter(def, false);
            LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
            FightLogic fightLogic = new FightLogic(attacker, defender, true, WorldConstant.BATTLE_TYPE_BERLIN_WAR);
            warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
            fightLogic.fight();// 战斗

            //貂蝉任务-杀敌阵亡数量
            ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
            ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

            boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
            HashSet<Long> ids = new HashSet<>();
            // 损兵处理
            if (attacker.lost > 0) {
                subBattleHeroArm(attacker.forces.get(0), attacker.lost, AwardFrom.BERLIN_WAR_ATTACK);
                updArmyRank(attacker, ids);
            }
            if (defender.lost > 0) {
                subBattleHeroArm(defender.forces.get(0), defender.lost, AwardFrom.BERLIN_WAR_ATTACK);
                updArmyRank(defender, ids);
            }
            CommonPb.Record record = fightLogic.generateRecord();

            // 战斗记录
            Player defPlayer = playerDataManager.checkPlayerIsExist(def.ownerId);
            Lord defLord = defPlayer.lord;

            rpt.setResult(atkSuccess);
            rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
            rpt.setRecord(record);
            // 记录发起进攻和防守方的信息
            rpt.setAttack(
                    PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel(),
                            atkLord.getLordId()));
            // 记录双方汇总信息
            rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                    atkLord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));
            rpt.setDefMan(
                    PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel(),
                            defLord.getLordId()));
            rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                    defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));
            addBerlinBattleHeroRpt(attacker, rpt, true);
            addBerlinBattleHeroRpt(defender, rpt, false);

            CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);
            // 记录战况
            berlinWar.getReports().addFirst(report.build());

            int decorated = 0;
            if (defPlayer.heros.containsKey(def.id)) {
                Hero hero = defPlayer.heros.get(def.id);
                decorated = hero.getDecorated();
            }

            // 立即出击战报
            reportRs.add(PbHelper.createBerlinBasicReport(atkSuccess, attacker, defender, defLord, def.id, decorated));

            // 战斗日志
            String sb = "立即战斗日志, " + berlinWarLog(atkLord.getLordId(), attacker, true) + ", " +
                    berlinWarLog(defLord.getLordId(), defender, false);
            LogUtil.debug(sb);

            // 记录参战人员
            berlinWar.getJoinBerlinWar().add(atkLord.getLordId());
            berlinWar.getJoinBerlinWar().add(defLord.getLordId());

            // 重新计算攻防兵力
            berlinInfo.reCalcuAtkDefArm();

            // 更新会战记录
            berlinWar.updateBerlinRecord(atkLord, attacker, now);
            berlinWar.updateBerlinRecord(defLord, defender, now);

            // 进攻方胜利
            if (atkSuccess && atk.getCamp() != berlinInfo.getCamp()) {
                // 检测防守失败
                checkBerlinDefIsFail(berlinInfo, now, berlinWar, berlin, staticBerlinWar, isNpcCity, atk.getCamp(),
                        atkLord.getNick());
            }
            // 检测将领死亡,将领返回并移除队列
            checkForceDead(berlinInfo);

            // 柏林战斗日志
            LogLordHelper.otherLog("BerlinBattle", DataResource.ac.getBean(ServerSetting.class).getServerID(), "fight", atkPlayer.roleId, defPlayer.roleId, atkSuccess, berlinInfo.getCityId(), berlinInfo.getPos(), attacker.hurt, attacker.lost, attacker.total, atk.id, defender.hurt, defender.lost, defender.total, def.id);
        }

        if (!reportRs.isEmpty()) {
            int decorated = 0;
            if (atkPlayer.heros.containsKey(atk.id)) {
                Hero hero = atkPlayer.heros.get(atk.id);
                decorated = hero.getDecorated();
            }
            // 同步立即出击战报
            syncBasicReport(berlinInfo, now, builder, atk, atkLord, reportRs, decorated);
        }

    }

    /**
     * 同步立即出击战报
     *
     * @param berlinInfo 柏林城池
     * @param now        现在的时间
     * @param builder    SyncBasicReportRs构造器
     * @param atk        进攻军队
     * @param atkLord    进攻Lord
     * @param reportRs   战报
     * @param decorated  授勋
     * @throws MwException 自定义异常
     */
    private void syncBasicReport(BerlinCityInfo berlinInfo, int now, SyncBasicReportRs.Builder builder, BerlinForce atk,
                                 Lord atkLord, List<CommonPb.BerlinBasicReport> reportRs, int decorated) throws MwException {
        builder.setHeroId(atk.id);
        BerlinForce force = berlinInfo.getRoleQueue().stream().filter(Force::alive)
                .filter(f -> f.ownerId == atkLord.getLordId() && f.id == atk.id).findFirst().orElse(null);
        int hp = 0;
        if (!CheckNull.isNull(force)) {
            hp = force.hp;
            force.setImmediatelyTime(0);
        }
        builder.setHp(hp);
        builder.setBattleTime(now);
        builder.setNick(atkLord.getNick());
        builder.setCamp(atkLord.getCamp());
        builder.setRoleId(atkLord.getLordId());
        builder.addAllReports(reportRs);
        builder.setDecorated(decorated);
        Player player = playerDataManager.checkPlayerIsExist(atkLord.getLordId());

        // 同步立即出击战报
        if (player != null && player.isLogin && player.ctx != null) {
            Base.Builder msg = PbHelper
                    .createSynBase(SyncBasicReportRs.EXT_FIELD_NUMBER, SyncBasicReportRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 柏林会战战斗逻辑, 占领逻辑
     *
     * @param berlinCityInfo 柏林城池
     */
    private void berlinWarLogic(BerlinCityInfo berlinCityInfo, int now, BerlinWar berlinWar) throws MwException {
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        City berlin = worldDataManager.getCityById(berlinCityInfo.getCityId());
        if (CheckNull.isNull(berlin)) {
            LogUtil.error("柏林City对象未初始化");
        }
        StaticBerlinWar staticBerlinWar = StaticBerlinWarDataMgr.getBerlinSettingById(berlinCityInfo.getCityId());
        if (CheckNull.isNull(staticBerlinWar)) {
            LogUtil.error("柏林会战配置错误");
            return;
        }
        // 检测将领死亡,将领返回并移除队列
        checkForceDead(berlinCityInfo);
        // 检测将领的攻防状态
        berlinCityInfo.atkCampConvertDef();

        boolean isNpcCity = berlinCityInfo.isNpcCity();
        BerlinForce atk = berlinCityInfo.getSingleForce(WorldConstant.BERLIN_ATK);
        if (CheckNull.isNull(atk)) {
            LogUtil.debug("本轮柏林会战没有进攻方");
            return;
        }

        Player atkPlayer = playerDataManager.checkPlayerIsExist(atk.ownerId);
        Lord atkLord = atkPlayer.lord;

        // 默认大一轮, 战斗狂热最多打60场
        int roundCnt = berlinWar.isInBattleFrenzy() ? WorldConstant.BERLIN_BATTLE_FRENZY_MAX_ROUND : 1;
        for (int i = 0; i < roundCnt; i++) {
            if (!atk.alive()) {
                break;
            }
            // 防守方是不是NPC
            boolean defIsNpc = true;
            BerlinForce def = null;
            Force cityDef = berlinCityInfo.getSingleCityDef();
            if (Objects.isNull(cityDef)) {
                defIsNpc = false;
                def = berlinCityInfo.getSingleForce(WorldConstant.BERLIN_DEF);
            }
            if (Objects.isNull(def) && Objects.isNull(cityDef)) {
                LogUtil.error("柏林会战本轮战斗没有防守方");
                break;
            }

            // 检测柏林防守失败
            if (checkBerlinDefIsFail(berlinCityInfo, now, berlinWar, berlin, staticBerlinWar, isNpcCity, atk.getCamp(),
                    atkLord.getNick())) {
                LogUtil.error("平局情况下, ", atk.getCamp(), "阵营的roleId:", atkLord.getLordId(), ", nick:", atkLord.getNick(), ", 占领了柏林, cityId:", berlinCityInfo.getCityId());
                break;
            }


            Fighter attacker = createBerlinWarFighter(atk, defIsNpc);
            Fighter defender = CheckNull.isNull(def) ?
                    createBerlinWarFighter(cityDef, defIsNpc) :
                    createBerlinWarFighter(def, defIsNpc);
            LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
            FightLogic fightLogic = new FightLogic(attacker, defender, true, WorldConstant.BATTLE_TYPE_BERLIN_WAR);
            warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
            fightLogic.fight();// 战斗

            //貂蝉任务-杀敌阵亡数量
            ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
            ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

            boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
            HashSet<Long> ids = new HashSet<>();
            // 损兵处理
            if (attacker.lost > 0) {
                subBattleHeroArm(attacker.forces.get(0), attacker.lost, AwardFrom.BERLIN_WAR_ATTACK);
                updArmyRank(attacker, ids);
            }
            if (defender.lost > 0 && !defIsNpc) {
                subBattleHeroArm(defender.forces.get(0), defender.lost, AwardFrom.BERLIN_WAR_ATTACK);
                updArmyRank(defender, ids);
            }
            CommonPb.Record record = fightLogic.generateRecord();

            // 战斗记录
            int pos = berlinCityInfo.getPos();
            Player defPlayer = null;
            Lord defLord = null;
            if (!defIsNpc) {
                defPlayer = playerDataManager.checkPlayerIsExist(def.ownerId);
                defLord = defPlayer.lord;
            }

            rpt.setResult(atkSuccess);
            rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
            rpt.setRecord(record);
            // 记录发起进攻和防守方的信息
            rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel(),
                    atkLord.getLordId()));
            // 记录双方汇总信息
            rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                    atkLord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));
            if (defIsNpc || CheckNull.isNull(defLord)) {
                rpt.setDefCity(PbHelper.createRptCityPb(berlinCityInfo.getCityId(), pos));
                rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, berlinCityInfo.getCamp(), null, 0, 0));
            } else {
                rpt.setDefMan(
                        PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel(),
                                defLord.getLordId()));
                rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                        defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));
            }
            addBerlinBattleHeroRpt(attacker, rpt, true);
            addBerlinBattleHeroRpt(defender, rpt, false);
            CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);
            // 记录战况
            berlinWar.getReports().addFirst(report.build());

            // 战斗日志
            String sb = "柏林会战战斗日志, " + berlinWarLog(atkLord.getLordId(), attacker, true) + ", " +
                    berlinWarLog(defIsNpc ? berlinCityInfo.getCityId() : defLord != null ? defLord.getLordId() : 0, defender, false);
            LogUtil.debug(sb);

            // 记录参战人员
            berlinWar.getJoinBerlinWar().add(atkLord.getLordId());
            if (!defIsNpc) {
                berlinWar.getJoinBerlinWar().add(defLord != null ? defLord.getLordId() : 0);
            }

            // 重新计算攻防兵力
            berlinCityInfo.reCalcuAtkDefArm();

            // 更新会战记录
            if (!defIsNpc) {
                berlinWar.updateBerlinRecord(atkLord, attacker, now);
                berlinWar.updateBerlinRecord(defLord, defender, now);
            }

            // 进攻方胜利
            if (atkSuccess) {
                // 检测防守失败
                checkBerlinDefIsFail(berlinCityInfo, now, berlinWar, berlin, staticBerlinWar, isNpcCity, atk.getCamp(),
                        atkLord.getNick());
            }
            // 检测将领死亡,将领返回并移除队列
            checkForceDead(berlinCityInfo);
            //上报数数(攻击方)
            EventDataUp.battle(atkPlayer.account, atkPlayer.lord, attacker, "atk", "berlin",
                    String.valueOf(WorldConstant.BATTLE_TYPE_BERLIN_WAR), String.valueOf(fightLogic.getWinState()), atkLord.getLordId(), rpt.getAtkHeroList());
            //上报数数(防守方)
            if (!defIsNpc) {
                defPlayer = playerDataManager.checkPlayerIsExist(def.ownerId);
                EventDataUp.battle(defPlayer.account, defPlayer.lord, defender, "def", "berlin",
                        String.valueOf(WorldConstant.BATTLE_TYPE_BERLIN_WAR), String.valueOf(fightLogic.getWinState()), atkLord.getLordId(), rpt.getDefHeroList());
            }
            // 柏林战斗日志
            LogLordHelper.otherLog("BerlinBattle", DataResource.ac.getBean(ServerSetting.class).getServerID(), "fight", atkPlayer.roleId, defIsNpc ? 0 : defPlayer.roleId, atkSuccess, berlinCityInfo.getCityId(), berlinCityInfo.getPos(), attacker.hurt, attacker.lost, attacker.total, atk.id, defender.hurt, defender.lost, defender.total, defIsNpc ? cityDef.id : def.id);
        }
    }

    /**
     * 柏林日志记录
     *
     * @param lordId  玩家id
     * @param fighter 战斗对象
     * @param isAtk   是否是进攻方
     * @return 字符串
     */
    private String berlinWarLog(long lordId, Fighter fighter, boolean isAtk) {
        StringBuilder sb = new StringBuilder();
        if (!CheckNull.isNull(fighter) && !CheckNull.isEmpty(fighter.getForces())) {
            Force force = fighter.getForces().get(0);
            sb.append(isAtk ? "atk {" : "def {");
            sb.append("roleId: ").append(lordId).append(", heroId:").append(force.id).append(", total:")
                    .append(fighter.getTotal()).append(", hurt:").append(fighter.getHurt()).append(", lost:")
                    .append(fighter.getLost()).append("}");
        }
        return sb.toString();
    }

    /**
     * 检测将领死亡,将领返回并移除队列
     *
     * @param berlinCityInfo 柏林城池
     * @throws MwException 自定义异常
     */
    private void checkForceDead(BerlinCityInfo berlinCityInfo) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        LinkedList<Force> cityDef = berlinCityInfo.getCityDef();
        if (!CheckNull.isEmpty(cityDef)) {
            // 死亡的将领移除队列
            cityDef.removeIf(force -> !force.alive());
        }
        ArrayList<BerlinForce> roleQueue = berlinCityInfo.getRoleQueue();
        if (!CheckNull.isEmpty(roleQueue)) {
            Iterator<BerlinForce> iterator = roleQueue.iterator();
            while (iterator.hasNext()) {
                BerlinForce force = iterator.next();
                // 死亡的将领移除队列
                if (!force.alive()) {
                    Player player = playerDataManager.checkPlayerIsExist(force.ownerId);
                    // 将领返回
                    retreatDeadArmy(now, player.roleId, force.id);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 检测柏林防守方失败
     *
     * @param berlinCityInfo  柏林城池
     * @param now             现在时间
     * @param berlinWar       柏林会战
     * @param berlin          柏林的City
     * @param staticBerlinWar 配置
     * @param isNpcCity       是否是npc
     * @param atkCamp         进攻方阵营
     * @param atkName         进攻方名称
     */
    private boolean checkBerlinDefIsFail(BerlinCityInfo berlinCityInfo, int now, BerlinWar berlinWar, City berlin,
                                         StaticBerlinWar staticBerlinWar, boolean isNpcCity, int atkCamp, String atkName) {
        boolean flag = false;
        // 守军全部死亡
        if (berlinCityInfo.defIsFail()) {
            berlinCityInfo.initCityDef(staticBerlinWar.getFormList());
            // 胜利方将领转成普通防守方将领
            berlinCityInfo.atkCampConvertDef();
            if (!isNpcCity) {
                // 更新阵营的占领时间
                berlinCityInfo.updateCampOccupt(berlinCityInfo.getCamp(), now);
            }
            if (!CheckNull.isNull(berlin)) {
                berlin.setCamp(atkCamp);
            }
            berlinCityInfo.setCamp(atkCamp);
            berlinCityInfo.setLastOccupyTime(now);
            // 检测炮塔状态
            String battleFrontAtkTime = checkBattleFrontAtkTime(berlinWar);
            chatDataManager.sendSysChat(ChatConst.CHAT_OCCUPY_BERLIN, 0, 0,
                    atkCamp,
                    atkName,
                    // 扩展参数
                    berlinCityInfo.getCampOccupyTime(Constant.Camp.EMPIRE),
                    berlinCityInfo.getCampOccupyTime(Constant.Camp.ALLIED),
                    berlinCityInfo.getCampOccupyTime(Constant.Camp.UNION),
                    berlinCityInfo.getWinOfCountdown(),
                    battleFrontAtkTime,
                    berlinCityInfo.getCampInfluence(Constant.Camp.EMPIRE),
                    berlinCityInfo.getCampInfluence(Constant.Camp.ALLIED),
                    berlinCityInfo.getCampInfluence(Constant.Camp.UNION),
                    now
            );
            flag = true;
        }
        return flag;
    }

    /**
     * 检测阵地防守方失败
     *
     * @param berlinCityInfo  柏林城池
     * @param now             现在时间
     * @param berlinWar       柏林会战
     * @param staticBerlinWar 配置
     * @param atkCamp         进攻方阵营
     * @param atkName         进攻方名称
     */
    private boolean checkBattleFrontDefIsFail(BerlinCityInfo berlinCityInfo, int now, BerlinWar berlinWar,
                                              StaticBerlinWar staticBerlinWar, int atkCamp, String atkName) {
        boolean flag = false;
        // 守军全部死亡
        if (berlinCityInfo.defIsFail()) {
            berlinCityInfo.initCityDef(staticBerlinWar.getFormList());
            // 胜利方将领转成普通防守方将领
            berlinCityInfo.atkCampConvertDef();
            berlinCityInfo.setCamp(atkCamp);
            berlinCityInfo.setLastOccupyTime(now);
            // 阵地跟柏林是相同阵营
            if (berlinCityInfo.getCamp() == berlinWar.getBerlinCityInfo().getCamp()) {
                berlinCityInfo.setNextAtkTime(-1);
            } else { // 不同阵营
                berlinCityInfo.setNextAtkTime(now + currentFrontAtkCd());
            }
            chatDataManager
                    .sendSysChat(ChatConst.CHAT_OCCUPY_BATTLEFRONT, 0, 0, atkCamp, atkName, berlinCityInfo.getCityId(),
                            berlinCityInfo.getPos(),
                            // 扩展参数
                            berlinCityInfo.getCityId(), berlinCityInfo.getNextAtkTime());
            flag = true;
        }
        return flag;
    }

    /**
     * 柏林阵营转换的时候, 刷新下次进攻时间
     *
     * @param berlinWar 柏林会战
     * @return 所有阵营的下次炮击时间
     */
    private String checkBattleFrontAtkTime(BerlinWar berlinWar) {
        StringBuffer sb = new StringBuffer();
        int now = TimeHelper.getCurrentSecond();
        if (CheckNull.isNull(berlinWar)) {
            return sb.toString();
        }
        if (berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN) {
            LogUtil.error("柏林会战活动未开启");
            return sb.toString();
        }
        BerlinCityInfo berlinInfo = berlinWar.getBerlinCityInfo();
        if (CheckNull.isNull(berlinInfo)) {
            LogUtil.error("前线阵地对柏林炮轰， BerlinInfo柏林对象未初始化");
            return sb.toString();
        }
        int berlinCamp = berlinInfo.getCamp();
        berlinWar.getBattlefronts().values().forEach(battleFront -> {
            if (battleFront.getCamp() == berlinCamp) { // 相同阵营暂停计时
                battleFront.setNextAtkTime(-1);
            } else {
                if (battleFront.getNextAtkTime() == -1) { // 时间从现在开始重新计时
                    battleFront.setNextAtkTime(now + currentFrontAtkCd());
                }
            }
        });
        List<StaticBerlinWar> sBerlinBattlefront = StaticBerlinWarDataMgr.getBerlinBattlefront();
        if (CheckNull.isEmpty(sBerlinBattlefront)) {
            return sb.toString();
        }
        sBerlinBattlefront
                .forEach(e -> {
                    BerlinCityInfo battleFront = berlinWar.getBattlefrontByCityId(e.getKeyId());
                    sb.append(battleFront.getNextAtkTime()).append("_");
                });
        return sb.toString();
    }

    /**
     * 前线阵地战斗逻辑, 占领逻辑
     *
     * @param berlinCityInfo 柏林城池
     * @param now            现在时间
     * @param berlinWar      柏林会战
     */
    private void battlefrontWarLogic(BerlinCityInfo berlinCityInfo, int now, BerlinWar berlinWar) throws MwException {
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        StaticBerlinWar staticBerlinWar = StaticBerlinWarDataMgr.getBerlinSettingById(berlinCityInfo.getCityId());
        if (CheckNull.isNull(staticBerlinWar)) {
            LogUtil.error("柏林会战配置错误");
            return;
        }
        // 检测将领死亡,将领返回并移除队列
        checkForceDead(berlinCityInfo);
        // 检测将领的攻防状态
        berlinCityInfo.atkCampConvertDef();
        boolean isNpcCity = berlinCityInfo.isNpcCity();
        BerlinForce atk = berlinCityInfo.getSingleForce(WorldConstant.BERLIN_ATK);
        if (CheckNull.isNull(atk)) {
            LogUtil.debug("本轮前线阵地争夺,没有进攻方");
            return;
        }

        Player atkPlayer = playerDataManager.checkPlayerIsExist(atk.ownerId);
        Lord atkLord = atkPlayer.lord;
        // 检测阵地防守失败
        if (checkBattleFrontDefIsFail(berlinCityInfo, now, berlinWar, staticBerlinWar, atk.getCamp(),
                atkLord.getNick())) {
            LogUtil.error("平局情况下, ", atk.getCamp(), "阵营的roleId:", atkLord.getLordId(), ", nick:", atkLord.getNick(),
                    ", 占领了据点, cityId:", berlinCityInfo.getCityId());
            return;
        }

        Force cityDef = berlinCityInfo.getSingleCityDef();
        boolean defIsNpc = true;
        BerlinForce def = null;
        if (CheckNull.isNull(cityDef)) {
            defIsNpc = false;
            def = berlinCityInfo.getSingleForce(WorldConstant.BERLIN_DEF);
        }
        if (CheckNull.isNull(def) && CheckNull.isNull(cityDef)) {
            LogUtil.error("本轮前线阵地争夺,没有防守方");
            return;
        }
        Fighter attacker = createBerlinWarFighter(atk, isNpcCity);
        Fighter defender = CheckNull.isNull(def) ?
                createBerlinWarFighter(cityDef, defIsNpc) :
                createBerlinWarFighter(def, defIsNpc);
        LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, WorldConstant.BATTLE_TYPE_BERLIN_WAR);
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();// 战斗

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        // 记录玩家有改变的资源类型, key:roleId
        HashSet<Long> ids = new HashSet<>();
        // 损兵处理
        if (attacker.lost > 0) {
            subBattleHeroArm(attacker.forces.get(0), attacker.lost, AwardFrom.BERLIN_WAR_ATTACK);
            updArmyRank(attacker, ids);
        }
        if (defender.lost > 0 && !defIsNpc) {
            subBattleHeroArm(defender.forces.get(0), defender.lost, AwardFrom.BERLIN_WAR_ATTACK);
            updArmyRank(defender, ids);
        }
        CommonPb.Record record = fightLogic.generateRecord();

        // 战斗记录
        int pos = berlinCityInfo.getPos();
        Player defPlayer = null;
        Lord defLord = null;
        if (!defIsNpc) {
            defPlayer = playerDataManager.checkPlayerIsExist(def.ownerId);
            defLord = defPlayer.lord;
        }

        rpt.setResult(atkSuccess);
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setRecord(record);
        // 记录发起进攻和防守方的信息
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel(),
                atkLord.getLordId()));
        // 记录双方汇总信息
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));
        if (isNpcCity || CheckNull.isNull(defLord)) {
            rpt.setDefCity(PbHelper.createRptCityPb(berlinCityInfo.getCityId(), pos));
            rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, berlinCityInfo.getCamp(), null, 0, 0));
        } else {
            rpt.setDefMan(
                    PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel(),
                            defLord.getLordId()));
            rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                    defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));
        }
        addBerlinBattleHeroRpt(attacker, rpt, true);
        addBerlinBattleHeroRpt(defender, rpt, false);
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);
        // 记录战况
        berlinWar.getReports().addFirst(report.build());

        // 战斗日志
        String sb = "柏林会战战斗日志, " + berlinWarLog(atkLord.getLordId(), attacker, true) + ", " +
                berlinWarLog(defIsNpc ? berlinCityInfo.getCityId() : defLord != null ? defLord.getLordId() : 0, defender, false);
        LogUtil.debug(sb);

        // 记录参战人员
        berlinWar.getJoinBerlinWar().add(atkLord.getLordId());
        if (!defIsNpc) {
            berlinWar.getJoinBerlinWar().add(defLord != null ? defLord.getLordId() : 0);
        }

        // 重新计算攻防兵力
        berlinCityInfo.reCalcuAtkDefArm();

        // 更新会战记录
        if (!defIsNpc) {
            berlinWar.updateBerlinRecord(atkLord, attacker, now);
            berlinWar.updateBerlinRecord(defLord, defender, now);
        }

        // 进攻方胜利
        if (atkSuccess) {
            // 更新战令的任务进度
            playerDataManager.getPlayerByCamp(atk.getCamp()).values().stream()
                    .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, BuildingType.ENTER_AREA_13_COND))
                    .forEach(p -> battlePassDataManager.updTaskSchedule(p.roleId, TaskType.COND_BERLIN_FRONT_CNT, 1));
            // 守军全部死亡
            checkBattleFrontDefIsFail(berlinCityInfo, now, berlinWar, staticBerlinWar, atk.getCamp(),
                    atkLord.getNick());
        }
        // 检测将领死亡,将领返回并移除队列
        checkForceDead(berlinCityInfo);

        //上报数数(攻击方)
        EventDataUp.battle(atkPlayer.account, atkPlayer.lord, attacker, "atk", "berlin",
                String.valueOf(WorldConstant.BATTLE_TYPE_BERLIN_WAR), String.valueOf(fightLogic.getWinState()), atkLord.getLordId(), rpt.getAtkHeroList());
        //上报数数(防守方)
        if (!defIsNpc) {
            defPlayer = playerDataManager.checkPlayerIsExist(def.ownerId);
            EventDataUp.battle(defPlayer.account, defPlayer.lord, defender, "def", "berlin",
                    String.valueOf(WorldConstant.BATTLE_TYPE_BERLIN_WAR), String.valueOf(fightLogic.getWinState()), atkLord.getLordId(), rpt.getDefHeroList());
        }
        // 柏林战斗日志
        LogLordHelper.otherLog("BerlinBattle", DataResource.ac.getBean(ServerSetting.class).getServerID(), "fight", atkPlayer.roleId, defIsNpc ? 0 : defPlayer.roleId, atkSuccess, berlinCityInfo.getCityId(), berlinCityInfo.getPos(), attacker.hurt, attacker.lost, attacker.total, atk.id, defender.hurt, defender.lost, defender.total, defIsNpc ? cityDef.id : def.id);
    }

    /**
     * 返回死亡的部队
     *
     * @param now    现在时间
     * @param roleId 角色Id
     * @param heroId 将领Id
     */
    private void retreatDeadArmy(int now, Long roleId, int heroId) {
        // 攻防双方撤回部队， 先取参与者，然后遍历参与者的army的BattleId
        Player player = playerDataManager.getPlayer(roleId);
        BerlinWar berlinWar = BerlinWar.getInstance();
        if (Objects.isNull(player)) {
            return;
        }
        if (CheckNull.isEmpty(player.armys)) {
            return;
        }
        for (Map.Entry<Integer, Army> kv : player.armys.entrySet()) {
            Army army = kv.getValue();
            if (army == null || army.getHero().get(0).getV1() != heroId) {
                continue;
            }
            if (army.getState() == ArmyConstant.ARMY_STATE_RETREAT) {
                continue;
            }
            // 柏林会战将领死亡CD
            if (!CheckNull.isNull(berlinWar) && army.getType() == ArmyConstant.ARMY_TYPE_BERLIN_WAR) {
                BerlinRoleInfo roleInfo = berlinWar.getRoleInfo(roleId);
                if (now < roleInfo.getFreeCDTime()) {
                    roleInfo.setAtkCD(0); // 在免CD时间类,CD为0
                } else {
                    // 下次进攻CD
                    roleInfo.setAtkCD(now + WorldConstant.BERLIN_RESURRECTION_CD);
                }
            }
            LogUtil.debug("返还所有参战玩家的部队armyTarget=" + army.getTarget() + ",army=" + army);
            worldService.retreatArmy(player, army, now);
            // 推送
            worldService.synRetreatArmy(player, army, now);
        }
    }

    /**
     * 柏林双方记录
     *
     * @param fighter    战斗对象
     * @param rpt        战报Pb对象
     * @param isAttacker 是否是进攻方
     */
    private void addBerlinBattleHeroRpt(Fighter fighter, CommonPb.RptAtkPlayer.Builder rpt, boolean isAttacker) {
        if (!CheckNull.isNull(fighter) && !CheckNull.isEmpty(fighter.getForces())) {
            Force force = fighter.getForces().get(0);
            long roleId = force.ownerId;
            Player player = playerDataManager.getPlayer(roleId);
            // 军工计算 军工 = (损兵数 / 损兵基数) * 获得军功基数
            int award = (fighter.lost / WorldConstant.BERLIN_LOST_EXPLOIT_NUM.get(0))
                    * WorldConstant.BERLIN_LOST_EXPLOIT_NUM.get(1);
            int heroDecorated = 0;
            if (!CheckNull.isNull(player)) {
                BerlinRecord record = BerlinWar.getInstance().getBerlinRecord(roleId);
                award = warService.addExploit(player, award, null, AwardFrom.BERLIN_WAR_ATTACK); // 加军工
                if (award > 0) { // 记录军工
                    record.addExploit(award);
                }
                Hero hero = player.heros.get(force.id);
                if (Objects.nonNull(hero)) {
                    heroDecorated = hero.getDecorated();
                }
            }
            String owner = playerDataManager.getNickByLordId(force.ownerId);
            RptHero rptHero = PbHelper.createRptHero(force.roleType, fighter.hurt, award, force.id, owner, 0, 0, force.lost, heroDecorated);
            if (isAttacker) {
                rpt.addAtkHero(rptHero);
            } else {
                rpt.addDefHero(rptHero);
            }
        }
    }

    /**
     * 更新损兵排行
     *
     * @param attacker 战斗对象
     * @param ids      参与者
     */
    private void updArmyRank(Fighter attacker, HashSet<Long> ids) {
        ids.clear();
        attacker.forces.forEach(e -> {
            long ownerId = e.ownerId;
            if (!ids.contains(ownerId)) {
                Player player = playerDataManager.getPlayer(ownerId);
                activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
                // 荣耀日报损兵进度
                honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
                // 战令的损兵进度
                battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
            }
        });
    }

    /**
     * 创建柏林会战Fighter对象
     *
     * @param force     军队
     * @param isNpcCity 是否是npc
     * @return 战斗对象
     */
    private Fighter createBerlinWarFighter(Force force, boolean isNpcCity) {
        Fighter fighter = new Fighter();
        fighter.roleType = isNpcCity ? Constant.Role.CITY : Constant.Role.PLAYER;
        if (force.alive()) {
            fighter.addRealForce(force);
            // 加入光环技能
            Player player = playerDataManager.getPlayer(force.ownerId);
            if (!CheckNull.isNull(player)) {
                Hero hero = player.heros.get(force.id);
                if (!CheckNull.isNull(hero)) {
                    if (!hero.isIdle()) {
                        fightService.addMedalAuraSkill(fighter, hero, player);
                        Map<Integer, Integer> attrMap = player.getEffect()
                                .entrySet()
                                .stream()
                                .filter(EffectConstant.BERLIN_PRE_BUFF::contains)
                                .collect(Collectors.toMap(en -> {
                                    int type = en.getKey();
                                    // 只有穿甲是仅在打圣城和炮台生效
                                    if (type == EffectConstant.PREWAR_ATK) {
                                        return Constant.AttrId.ATTACK;
                                    } else if (type == EffectConstant.PREWAR_DEF) {
                                        return Constant.AttrId.DEFEND;
                                    } else if (type == EffectConstant.PREWAR_LEAD) {
                                        return Constant.AttrId.LEAD;
                                    } else if (type == EffectConstant.PREWAR_ATTACK_EXT) {
                                        return Constant.AttrId.ATTACK_EXT;
                                    }
                                    return 0;
                                }, en -> en.getValue().getEffectVal(), Integer::sum));
                        if (!CheckNull.isEmpty(attrMap)) {
                            Map<Integer, Integer> attrMutMap = CalculateUtil.getAttrMutMap(player, hero);
                            CalculateUtil.processFinalAttr(attrMap, attrMutMap);
                            LogUtil.common(String.format("柏林会战生效buff前, role_id: %s, force_id: %s, attr_data: %s", player.roleId, force.id, force.attrData));
                            // 柏林战前buff效果 (实际效果)
                            force.attrData.addValue(attrMutMap);
                            LogUtil.common(String.format("柏林会战生效buff后, role_id: %s, force_id: %s, attr_data: %s", player.roleId, force.id, force.attrData));
                        }
                    }
                }
            }
        }
        int allHp = 0;// 真实血量
        for (Force f : fighter.forces) {
            allHp += f.hp;
        }
        fighter.lost = fighter.total - allHp;// 总损兵
        return fighter;
    }

    /**
     * 初始化柏林会战对象
     *
     * @param now 现在的Date
     */
    public void initBerlinWar(Date now) {
        boolean flag = false;
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            LogUtil.error("开始生成柏林会战");
            berlinWar = BerlinWar.createNewBerlinWar();
            berlinWar.clearBerlinWar();
            globalDataManager.getGameGlobal().setBerlinWar(berlinWar);
            flag = true;
        } else {
            BerlinCityInfo cityInfo = berlinWar.getBerlinCityInfo();
            // Berin没有初始化
            if (CheckNull.isNull(cityInfo) || cityInfo.getCityId() == 0) {
                berlinWar.initBerlinWar();
            }
            Date latestDate = berlinWar.getLastDate();
            if (Objects.isNull(latestDate) || DateHelper.dayiy(latestDate, now) != 1) {
                int currentSecond = TimeHelper.getCurrentSecond();
                retreatBerlinArmy(currentSecond, cityInfo);
                berlinWar.getBattlefronts().values().forEach(battleFront -> retreatBerlinArmy(currentSecond, battleFront));
                // 判断上次开启时间,清除状态
                berlinWar.clearBerlinWar();
                flag = true;
            }
        }
        // 本周首次开放柏林会战
        if (flag) {
            // 检测并刷新进攻时间
            String battleFrontAtkTime = checkBattleFrontAtkTime(berlinWar);
            chatDataManager.sendSysChat(ChatConst.CHAT_BERLIN_OPEN, 0, 0, battleFrontAtkTime);
            // 柏林会战战斗逻辑
            berlinWar.setStatus(WorldConstant.BERLIN_STATUS_OPEN);
/*            // 柏林会战开启在线玩家推送
            playerDataManager.getAllPlayer().entrySet().stream().filter(e -> e.getValue() != null).forEach(e ->
                PushMessageUtil.pushMessage(
                        e.getValue().account,
                        PushConstant.FORT_BATTLE_IS_START,
                        player -> !player.isRobot
                )
            );*/
            // 清除官职
            berlinWar.getBerlinJobs().clear();
            // 记录世界进程阶段
            if (berlinWar.getScheduleId() == 0) {
                int scheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
                // 未初始化
                berlinWar.updateScheduleId(scheduleId);
            }
            LogUtil.error("开启柏林会战, 现在时间: ", DateHelper.getDateFormat1().format(now), ", 开启时间: ",
                    DateHelper.getDateFormat1().format(berlinWar.getBeginDate()), ", 结束时间: ",
                    DateHelper.getDateFormat1().format(berlinWar.getEndDate()), ", 预显示时间: ",
                    DateHelper.getDateFormat1().format(berlinWar.getPreViewDate()));
        }
        berlinWar.setLastDate(now);
    }

    /**
     * 获取柏林战斗状态
     *
     * @return 0 表示活动压根没开启 ,其他状态查看 {@link WorldConstant#BERLIN_STATUS_OPEN}
     */
    int getBerlinwarState() {

        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (berlinWar != null) {
            return berlinWar.getStatus();
        }
        return 0;
    }

    /**
     * 获取柏林官职
     *
     * @param roleId 角色Id
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GetBerlinJobRs getBerlinJob(long roleId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        GetBerlinJobRs.Builder builder = GetBerlinJobRs.newBuilder();

        Turple<Integer, Long> curWinner = BerlinWar.getCurWinner();
        if (curWinner != null) {
            Player p = playerDataManager.getPlayer(curWinner.getB());
            if(Objects.nonNull(p)){
                builder.addBerlinJob(PbHelper.createBerlinJobPb(p, StaticBerlinJob.BOSS_JOB_ID));
            }
        }

        BerlinWar berlinWar = BerlinWar.getInstance();
        if (berlinWar != null) {
            berlinWar.getBerlinJobs().forEach((rId, job) -> {
                Player p = playerDataManager.getPlayer(rId);
                if (p != null)
                    builder.addBerlinJob(PbHelper.createBerlinJobPb(p, job));
            });
        }
        return builder.build();
    }

    /**
     * 任命柏林官职
     *
     * @param roleId 角色Id
     * @param req    请求参数
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public AppointBerlinJobRs appointBerlinJob(long roleId, AppointBerlinJobRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        BerlinWar berlinWar = BerlinWar.getInstance();
        if (berlinWar == null) {
            throw new MwException(GameError.BERLIN_CITY_INFO_NOT_INIT.getCode(), "柏林据点对象未初始化, roleId:", roleId);
        }
        if (berlinWar.getStatus() == WorldConstant.BERLIN_STATUS_OPEN) {
            throw new MwException(GameError.BERLIN_STATUE_ERROR.getCode(), "任命柏林官职, 柏林活动已经开启了, roleId:", roleId);
        }
        Turple<Integer, Long> curWinner = BerlinWar.getCurWinner();
        if (curWinner == null || curWinner.getB() != roleId) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "柏林官员任职没有这个特权, roleId:", roleId);
        }
        final long jobRoleId = req.getRoleId(); // 任命人的RoleId
        final int job = req.getJob();

        if (jobRoleId == roleId) {
            throw new MwException(GameError.BERLIN_DONT_APPOINT_SELF.getCode(), "不能任命自己为官职, roleId:", roleId,
                    ", jobRoleId:", jobRoleId);
        }
        if (berlinWar.getBerlinJobs().containsKey(jobRoleId)) {
            throw new MwException(GameError.BERLIN_DONT_APPOINT_SELF.getCode(), "该玩家已经被任命其他官职, roleId:", roleId,
                    ", jobRoleId:", jobRoleId);
        }

        if (!StaticBerlinWarDataMgr.getBerlinJob().containsKey(job)) {
            throw new MwException(GameError.BERLIN_NO_SUCH_JOB.getCode(), "没有此官职, roleId:", roleId, ", job:", job);
        }
        if (berlinWar.getBerlinJobs().containsValue(job)) {
            throw new MwException(GameError.BERLIN_JOB_APPOINTED.getCode(), "该官职已经被任命, roleId:", roleId, ", job:", job);
        }
        Player jobPlayer = playerDataManager.checkPlayerIsExist(jobRoleId);
        // 任命
        berlinWar.getBerlinJobs().put(jobPlayer.roleId, job);
        LogUtil.common("柏林会战任命官员成功 : 霸主roleId:", roleId, ", jobRoleId:", jobRoleId, ", job:", job);
        // 发公告
        chatDataManager.sendSysChat(ChatConst.CHAT_APPOINT_BERLIN_JOB, 0, 1, jobPlayer.roleId, player.lord.getNick(),
                jobPlayer.lord.getCamp(), jobPlayer.lord.getNick(), job);
        AppointBerlinJobRs.Builder builder = AppointBerlinJobRs.newBuilder();
        builder.setBerlinJob(PbHelper.createBerlinJobPb(jobPlayer, job));
        return builder.build();
    }

    /**
     * 获取的历届霸主
     *
     * @param roleId 角色Id
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GetBerlinWinnerListRs getBerlinWinnerList(long roleId) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        BerlinWar berlinWar = BerlinWar.getInstance();
        if (berlinWar == null) {
            throw new MwException(GameError.BERLIN_CITY_INFO_NOT_INIT.getCode(), "柏林据点对象未初始化, roleId:", roleId);
        }
        GetBerlinWinnerListRs.Builder builder = GetBerlinWinnerListRs.newBuilder();
        List<Turple<Integer, Long>> historyWinner = berlinWar.getHistoryWinner();
        for (Turple<Integer, Long> t : historyWinner) {
            long rId = t.getB();
            int time = t.getA();
            Player winnerP = playerDataManager.getPlayer(rId);
            if (winnerP != null) {
                builder.addBerlinJob(PbHelper.createBerlinJobPb(winnerP, StaticBerlinJob.BOSS_JOB_ID, time));
            }
        }
        return builder.build();
    }

    /**
     * 获取柏林皇城的行军时间
     *
     * @param player    玩家对象
     * @param army      行军对象
     * @param marchTime 行军时间
     * @return 计算后的行军时间
     */
    int getMarchTime(Player player, Army army, int marchTime) {
        if (army.getType() == ArmyConstant.ARMY_TYPE_BERLIN_WAR) {
            StaticCity sCity = StaticWorldDataMgr.getMaxTypeCityByArea(WorldConstant.AREA_TYPE_13);
            if (!CheckNull.isNull(sCity)) {
                City berlin = worldDataManager.getCityById(sCity.getCityId());
                if (!CheckNull.isNull(berlin)) {
                    double cityBuffer = worldDataManager
                            .getCityBuffer(PbHelper.createTwoIntPb(sCity.getCityId(), berlin.getCamp()),
                                    WorldConstant.CityBuffer.MILITARY_RESTRICTED_ZONES, player.roleId);
                    if (cityBuffer > 0) {
                        marchTime = (int) Math.ceil(marchTime * (1 + cityBuffer));
                    }
                }
            }
        }
        return marchTime;
    }

    /**
     * 检测柏林活动是否解锁
     *
     * @return
     */
    public boolean checkUnLock() {
        return worldService.bossDeadState() == 2;
    }


    /**
     * 初始化柏林会战定时器
     */
    public void initBerlinJob() {
        try {
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (CheckNull.isNull(berlinWar)) {
                LogUtil.error("创建柏林会战Fighter， BerlinWar对象未初始化");
                return;
            }
            // 未解锁
            if (!checkUnLock()) {
                return;
            }
            // 服务器时间
            Date nowDate = new Date();
            // 柏林会战开启时间
            Date beginDate = berlinWar.getBeginDate();
            // 未初始化或者已经过了开启时间
            if (Objects.isNull(beginDate) || nowDate.after(beginDate)) {
                initBerlinTime(berlinWar);
            }
            // 还没有到开启时间
            if (Objects.nonNull(berlinWar.getBeginDate()) && nowDate.before(berlinWar.getBeginDate())) {
                initScheduleTime(berlinWar);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 初始化柏林会战时间
     *
     * @param berlinWar 柏林会战
     */
    private void initBerlinTime(BerlinWar berlinWar) {
        // 服务器时间
        Date now = new Date();

        // 柏林会战功能配置
        List<String> beginCronDate = WorldConstant.getBerlinCronInfo(WorldConstant.BERLIN_CRON_DATE, WorldConstant.BERLIN_BEGIN_CRON);
        List<String> beginCronWeek = WorldConstant.getBerlinCronInfo(WorldConstant.BERLIN_CRON_WEEK, WorldConstant.BERLIN_BEGIN_CRON);
        List<String> cronAtkCD = WorldConstant.getBerlinCronInfo(WorldConstant.BERLIN_CRON_ATK_CD, WorldConstant.BERLIN_BEGIN_CRON);
        // 柏林会战预显示配置
        List<String> cronDate = WorldConstant.getBerlinCronInfo(WorldConstant.BERLIN_CRON_DATE, WorldConstant.PRE_VIEW_CRON);
        List<String> cronWeek = WorldConstant.getBerlinCronInfo(WorldConstant.BERLIN_CRON_WEEK, WorldConstant.PRE_VIEW_CRON);

        if (CheckNull.isEmpty(beginCronDate) || CheckNull.isEmpty(beginCronWeek) || CheckNull.isEmpty(cronAtkCD) || CheckNull.isEmpty(cronDate) || CheckNull.isEmpty(cronWeek)) {
            return;
        }
        // 开启时间配置
        String beginTime = beginCronDate.get(0);
        String endTime = beginCronDate.get(1);
        int beginWeek = Integer.parseInt(beginCronWeek.get(0));
        int atkCD = Integer.parseInt(cronAtkCD.get(0));
        String preViewTime = cronDate.get(0);
        int preWeek = Integer.parseInt(cronWeek.get(0));
        // 开启时间
        Date beginDate = DateHelper.afterStringTime(now, beginTime, beginWeek);
        // 结束时间
        Date endDate = DateHelper.afterStringTime(now, endTime, beginWeek);
        // 预显示时间
        Date preViewDate = DateHelper.afterStringTime(now, preViewTime, preWeek);

        // 过了开启时间
        if (now.after(beginDate)) {
            return;
        }

        berlinWar.setPreViewDate(preViewDate);
        berlinWar.setBeginDate(beginDate);
        berlinWar.setEndDate(endDate);
        // 攻击CD间隔
        berlinWar.setAtkCD(atkCD);
    }

    /**
     * 加入柏林会战定时任务
     *
     * @param berlinWar 柏林会战
     */
    private void initScheduleTime(BerlinWar berlinWar) {
        ScheduleManager scheduleManager = ScheduleManager.getInstance();
        if (CheckNull.isNull(scheduleManager)) {
            return;
        }
        Date now = new Date();
        if (!CheckNull.isNull(berlinWar.getBeginDate()) && !CheckNull.isNull(berlinWar.getEndDate()) && berlinWar.getAtkCD() != 0) {
            // 每周5的20点-21点 每3秒执行一次
            if (!DateHelper.isAfterTime(now, berlinWar.getEndDate())) {
                scheduleManager.addOrModifyDefultJob(DefultJob.createDefult("BerlinWarJob"), (job) -> {
                    // 初始化柏林会战,或者清除柏林会战状态
                    initBerlinWar(now);
                    battleTimeLogic();
                }, berlinWar.getBeginDate(), berlinWar.getEndDate(), berlinWar.getAtkCD());
            }
            // 活动开启时间内, 每1秒执行一次,炮轰操作
            if (!DateHelper.isAfterTime(now, berlinWar.getEndDate())) {
                scheduleManager.addOrModifyDefultJob(DefultJob.createDefult("battlefrontWarLogic"), (job) -> {
                    initBerlinWar(now);
                    battleFrontLogic(); // 炮轰逻辑
                    checkWinOfOccupyTime(); // 检测柏林占领时间
                }, berlinWar.getBeginDate(), berlinWar.getEndDate(), 1);
            }
            // 预显示定时器
            if (!DateHelper.isAfterTime(now, berlinWar.getPreViewDate())) {
                scheduleManager.addOrModifyDefultJob(DefultJob.createDefult("BerlinWarPreViewJob"), (job) -> {
                    LogUtil.world("---------------------柏林会战预显示---------------------");
                    // 柏林预显示
                    activityTriggerService.berlinWarPreViewTriggerGift();
                }, berlinWar.getPreViewDate());
            }
            // 每周5的21点01秒 执行一次,结算操作
            Date closeDate = TimeHelper.getDate(new Long(TimeHelper.afterSecondTime(berlinWar.getEndDate(), 1)));
            if (!DateHelper.isAfterTime(now, closeDate)) {
                scheduleManager.addOrModifyDefultJob(DefultJob.createDefult("BerlinWarColsingJob"), (job) -> {
                    // 柏林会战计算逻辑
                    berlinWar.setStatus(WorldConstant.BERLIN_STATUS_CLOSE);
                    colsingTimeLogic();
                }, closeDate);
            }
        }
    }


    /**
     * 圣域记录的世界进程阶段
     *
     * @return 世界进程阶段
     */
    private int currentOpenSchedule() {
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (Objects.isNull(berlinWar)) {
            return 0;
        }
        return berlinWar.getScheduleId();
    }

    /**
     * 当前世界进程炮塔的进攻cd
     *
     * @return -1 未找到cd配置 other 炮塔的cd时间
     */
    private int currentFrontAtkCd() {
        int openSchedule = currentOpenSchedule();
        if (openSchedule <= 0) {
            return -1;
        }
        if (CheckNull.isEmpty(WorldConstant.BATTLE_FRONT_ATK_CD)) {
            return -1;
        }
        return WorldConstant.BATTLE_FRONT_ATK_CD.stream().filter(conf -> openSchedule >= conf.get(0) && openSchedule <= conf.get(1)).mapToInt(conf -> conf.get(2)).findAny().orElse(-1);
    }

    /**
     * 当前世界进程炮塔的伤害系数
     *
     * @return 炮塔的伤害系数
     */
    private int currentFrontHurtCoef() {
        int openSchedule = currentOpenSchedule();
        if (openSchedule <= 0) {
            return 1;
        }
        if (CheckNull.isEmpty(WorldConstant.BATTLE_FRONT_HURT)) {
            return 1;
        }
        return WorldConstant.BATTLE_FRONT_HURT.stream().filter(conf -> openSchedule >= conf.get(0) && openSchedule <= conf.get(1)).mapToInt(conf -> conf.get(2)).findAny().orElse(1);
    }

    /**
     * 狂热状态检测
     *
     * @param berlinWar 非空, 上层调用判断
     */
    private void checkBattleFrenzy(BerlinWar berlinWar) {
        BerlinBattleFrenzy bbf = berlinWar.getBbf();
        if (berlinWar.getScheduleId() < ScheduleConstant.SCHEDULE_ID_12) {
            // 12进程以后才开放战斗狂热
            return;
        }
        // 当前时间
        int currentSecond = TimeHelper.getCurrentSecond();
        // 狂热状态
        int status = bbf.getStatus();
        // 未触发或者触发结束
        if (status != BerlinBattleFrenzy.BERLIN_BATTLE_FRENZY_STATUS_1) {
            // 当前触发次数
            int count = bbf.getCount();
            if (count + 1 <= WorldConstant.BERLIN_BATTLE_FRENZY_TRIGGER_CONF.size() && berlinWar.currentBFSchedule() >= WorldConstant.BERLIN_BATTLE_FRENZY_TRIGGER_CONF.get(count)) {
                // 达到了触发条件
                BerlinCityInfo berlinCityInfo = berlinWar.getBerlinCityInfo();
                int atkArm = berlinCityInfo.getAtkArm();
                int defArm = berlinCityInfo.getDefArm();
                int exact = Math.abs(Math.subtractExact(atkArm, defArm));
                int bfDuration = StaticBerlinWarDataMgr.findBFDuration(exact);
                if (bfDuration <= 0) {
                    LogUtil.error(String.format("柏林战斗狂热配置未找到, atkArm: %s, defArm: %s, exact: %s", atkArm, defArm, exact));
                    return;
                }
                bbf.setStatus(BerlinBattleFrenzy.BERLIN_BATTLE_FRENZY_STATUS_1);
                bbf.setEndTime(currentSecond + bfDuration);
                bbf.setCount(count + 1);
                bbf.setDuration(bfDuration);
                // 推送战斗狂热状态
                syncBerlinBattleFrenzy(berlinWar);
            }
        } else {
            // 触发中
            if (currentSecond > bbf.getEndTime()) {
                bbf.setStatus(BerlinBattleFrenzy.BERLIN_BATTLE_FRENZY_STATUS_2);
                // 推送战斗狂热状态
                syncBerlinBattleFrenzy(berlinWar);
            }
        }
    }

    /**
     * 同步战斗狂热
     *
     * @param berlinWar 柏林会战
     */
    private void syncBerlinBattleFrenzy(BerlinWar berlinWar) {
        SyncBerlinBattleFrenzyRs.Builder builder = SyncBerlinBattleFrenzyRs.newBuilder();
        builder.setBbf(berlinWar.serBBF());
        builder.addAllOccupyTime(berlinWar.getBerlinCityInfo().getCampOccupy());
        playerDataManager.getPlayers().values().stream().filter(p -> p.isLogin && p.ctx != null).forEach(player -> {
            Base.Builder msg = PbHelper.createRsBase(SyncBerlinBattleFrenzyRs.EXT_FIELD_NUMBER, SyncBerlinBattleFrenzyRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        });
    }
}
