package com.gryphpoem.game.zw.gameplay.local.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.PlayerMapEntity;
import com.gryphpoem.game.zw.dataMgr.StaticScoutDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb2.ScoutPosRs;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.s.StaticScoutCost;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.HonorDailyService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @ClassName CrossAttackService.java
 * @Description 跨服攻打的业务逻辑
 * @author QiuKun
 * @date 2019年3月22日
 */
@Component
public class CrossAttackService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossWorldMapService crossWorldMapService;
    @Autowired
    private WorldService worldService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private HonorDailyService honorDailyService;

    /**
     * 攻击某个点
     * 
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackCrossPosRs attackCrossPos(long roleId, AttackCrossPosRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        int pos = req.getPos();
        List<Integer> heroIdList = req.getHeroIdList().stream().distinct().collect(Collectors.toList());
        // 1 闪电战，2 奔袭战，3 远征战
        int type = req.hasType() ? req.getType() : WorldConstant.CITY_BATTLE_BLITZ;

        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        if (!cMap.checkPoxIsValid(pos) || cMap.isEmptyPos(pos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "该坐标为空闲坐标，不能攻击或采集, roleId:", roleId, ", pos:", pos);
        }
        WorldEntityType entityType = cMap.getAllMap().get(pos).getType();
        // 检查出征将领信息
        checkFormHeroSupport(player, heroIdList, pos, cMap);
        if (entityType != WorldEntityType.CITY && entityType != WorldEntityType.MINE && entityType != WorldEntityType.PLAYER && cMap.isInSafeArea(player)) {
            throw new MwException(GameError.WAR_FIRE_PLAYER_IN_SAFE_AREA.getCode(), "玩家在安全区，只能攻击或采集, roleId:", roleId, ", pos:", pos);
        }
        // 行军时间
        int marchTime = cMap.marchTime(cMap, player, player.lord.getPos(), pos);
        int armCount = heroIdList.stream().mapToInt(heroId -> {
            Hero hero = player.heros.get(heroId);
            return hero == null ? 0 : hero.getCount();
        }).sum(); // 出兵的总兵力
        // 检查补给
        int needFood = worldService.checkMarchFood(player, marchTime, armCount);

        // 参数
        AttackCrossPosRs.Builder builder = AttackCrossPosRs.newBuilder();
        AttackParamDto param = new AttackParamDto();
        param.setBuilder(builder);
        param.setArmCount(armCount);
        param.setInvokePlayer(player);
        param.setCrossWorldMap(cMap);
        param.setHeroIdList(heroIdList);
        param.setBattleType(type);
        param.setMarchTime(marchTime);
        param.setArmCount(armCount);
        param.setNeedFood(needFood);

        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(pos);
        // 攻击
        baseWorldEntity.attackPos(param);
        return builder.build();
    }

    /**
     * 检查玩家的出征将领
     * 
     * @param player
     * @param heroIdList
     * @param pos
     * @param cMap
     * @throws MwException
     */
    public void checkFormHeroSupport(Player player, List<Integer> heroIdList, int pos, CrossWorldMap cMap)
            throws MwException {
        long roleId = player.roleId;
        playerDataManager.autoAddArmy(player);
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(pos);
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "AttackPos未设置将领, roleId:", roleId);
        }
        // 目标点是否是一个采集点
        boolean isMinePos = baseWorldEntity.getType() == WorldEntityType.MINE;

        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "AttackPos，玩家没有这个将领, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!player.isOnBattleHero(heroId) && !player.isOnAcqHero(heroId) && !player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "AttackPos，将领未上阵,又未在采集队列中上阵 roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "AttackPos，将领没有带兵, roleId:", roleId, ", heroId:",
                        heroId, ", count:", hero.getCount());
            }
            if (!isMinePos) {
                // 如果是一个非采集点,但派出了采集将领
                if (hero.isOnAcq()) {
                    throw new MwException(GameError.ACQ_HERO_NOT_ATTACK.getCode(), "AttackPos，采集将领只能进行采集, roleId:",
                            roleId);
                }
            } else {
                // 如果是一个特攻将领, 但是派出来采集
                if (hero.isCommando()) {
                    throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "AttackPos，特攻将领不能进行采集, roleId:",
                            roleId);
                }
            }
        }
        // 如果目标是采集的时候
        if (isMinePos) {
            if (heroIdList.size() != 1) {
                // 目标为采集的时,只能排出一个将领
                throw new MwException(GameError.COLLECT_WORK_ONLYONE.getCode(), "AttackPos，将领采集时只能有一个, roleId:", roleId,
                        ", heroIdList.size:", heroIdList.size());
            }
            int stateAcqCount = 0; // 有多少个将领正在采集
            for (int heroId : player.heroAcq) {
                Hero h = player.heros.get(heroId);
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            for (int heroId : player.heroBattle) {
                Hero h = player.heros.get(heroId);
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            if (stateAcqCount >= 4) {
                throw new MwException(GameError.COLLECT_HERO_OVER_MAX.getCode(), "AttackPos，当前采集将领已超过上限, roleId:",
                        roleId, ", stateAcqCount:", stateAcqCount);
            }
        }
    }

    /**
     * 侦查
     * 
     * @param player
     * @param pos
     * @param type
     * @return
     * @throws MwException
     */
    public ScoutPosRs scoutPos(Player player, int pos, int type) throws MwException {
        int mapId = player.lord.getArea();
        long roleId = player.lord.getLordId();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, mapId);
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(pos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.PLAYER) {
            throw new MwException(GameError.POS_NO_PLAYER.getCode(), "侦查，目标坐标没有玩家, roleId:,", roleId, ", pos:", pos);
        }
        Player target = ((PlayerMapEntity) baseWorldEntity).getPlayer();

        int cdTime = player.common.getScoutCdTime();
        int now = TimeHelper.getCurrentSecond();
        if (cdTime > now + WorldConstant.SCOUT_CD_MAX_TIME) {
            throw new MwException(GameError.SCOUT_CD_TIME.getCode(), "侦查超过最大允许CD时间, roleId:", roleId, ", pos:", pos,
                    ", cdTime:", cdTime);
        }
        // 触发对方的一次自动补兵
        playerDataManager.autoAddArmy(target);
        int cityLv = target.building.getCommand();

        StaticScoutCost ssc = StaticScoutDataMgr.getScoutCostByCityLv(cityLv);
        if (null == ssc) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "侦查消耗未配置, roleId:,", roleId, ", cityLv:", cityLv);
        }

        List<List<Integer>> costList;
        if (type == WorldConstant.SCOUT_TYPE_PRIMARY) {
            costList = ssc.getPrimary();
        } else if (type == WorldConstant.SCOUT_TYPE_MIDDLE) {
            costList = ssc.getMiddle();
        } else if (type == WorldConstant.SCOUT_TYPE_SENIOR) {
            costList = ssc.getSenior();
        } else {
            throw new MwException(GameError.SCOUT_TYPE_ERROR.getCode(), "侦查类型不正确, roleId:,", roleId, ", type:", type);
        }

        // 检查并扣除消耗
        rewardDataManager.checkAndSubPlayerRes(player, costList, AwardFrom.SCOUT);

        // 记录玩家侦查CD
        if (cdTime < now) {
            cdTime = now;
        }
        cdTime += WorldConstant.SCOUT_CD;
        player.common.setScoutCdTime(cdTime);

        int scoutLv = player.getTechLvById(WorldConstant.SCOUT_TECH_ID);
        int targetLv = target.getTechLvById(WorldConstant.SCOUT_TECH_ID);
        int gap = scoutLv - targetLv;
        gap += WorldConstant.getScoutAddByType(type);// 侦查类型加成

        Mail mail = null;
        Lord tarLord = target.lord;
        Turple<Integer, Integer> tarXy = cMap.posToTurple(pos);
        Lord lord = player.lord;
        Turple<Integer, Integer> xy = cMap.posToTurple(lord.getPos());
        int ret = StaticScoutDataMgr.randomScoutResultByLvGap(gap);
        if (ret != WorldConstant.SCOUT_RET_FAIL) {
            // 通知被侦查方敌人侦查成功
            mailDataManager.sendReportMail(target, null, MailConstant.MOLD_ENEMY_SCOUT_SUCC, null, now, lord.getCamp(),
                    lord.getLevel(), lord.getNick(), xy.getA(), xy.getB(), lord.getCamp(), lord.getLevel(),
                    lord.getNick(), xy.getA(), xy.getB());

            // 推送被侦查消息
            // PushMessageUtil.pushMessage(target.account, PushConstant.ID_SCOUTED, target.lord.getNick(), lord.getNick());

            // 根据侦查结果计算侦察到的信息
            CommonPb.ScoutRes sRes = null;
            CommonPb.ScoutCity city = null;
            List<CommonPb.ScoutHero> sHeroList = null;
            if (ret >= WorldConstant.SCOUT_RET_SUCC1) {// 只获取资源信息
                Resource res = target.resource;
                // 仓库保护
                long[] proRes = buildingDataManager.getProtectRes(target);
                Map<Integer, Integer> canPlunderRes = buildingDataManager.canPlunderScout(target, player, proRes);
                List<TwoInt> canPlunderList = new ArrayList<>();
                for (Entry<Integer, Integer> kv : canPlunderRes.entrySet()) {
                    TwoInt ti = PbHelper.createTwoIntPb(kv.getKey(), kv.getValue());
                    canPlunderList.add(ti);
                }
                sRes = PbHelper.createScoutResPb(proRes[1], proRes[2], proRes[0], res.getOre(), res.getHuman(),
                        canPlunderList);
                if (ret >= WorldConstant.SCOUT_RET_SUCC2) {// 获取资源、城池信息
                    city = PbHelper.createScoutCityPb(target.building.getWall(), tarLord.getFight(),
                            (int) res.getArm1(), (int) res.getArm2(), (int) res.getArm3());
                    if (ret >= WorldConstant.SCOUT_RET_SUCC3) {// 获取资源、城池、将领信息
                        List<Hero> defheros = target.getAllOnBattleHeros();// 玩家所有上阵将领信息
                        sHeroList = new ArrayList<>();
                        int state;
                        int source;
                        for (Hero hero : defheros) {
                            source = WorldConstant.HERO_SOURCE_BATTLE;
                            state = worldService.getScoutHeroState(source, hero.getState());
                            sHeroList.add(PbHelper.createScoutHeroPb(hero, source, state, target));
                        }
                    }
                }
            }
            CommonPb.MailScout scout = PbHelper.createMailScoutPb(sRes, city, sHeroList);
            mail = mailDataManager.sendScoutMail(player, MailConstant.MOLD_SCOUT_SUCC, scout, now, tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB(), tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB());
        } else {// 侦查失败邮件
            mail = mailDataManager.sendScoutMail(player, MailConstant.MOLD_SCOUT_FAIL, null, now, tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB(), tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB());
            // 给敌方发侦查失败邮件
            Turple<Integer, Integer> myXy = MapHelper.reducePos(player.lord.getPos());
            mailDataManager.sendScoutMail(target, MailConstant.MOLD_ENEMY_SCOUT_FAIL, null, now, player.lord.getCamp(),
                    player.lord.getLevel(), player.lord.getNick(), myXy.getA(), myXy.getB(), player.lord.getCamp(),
                    player.lord.getLevel(), player.lord.getNick(), myXy.getA(), myXy.getB());
        }
        honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_10);
        ScoutPosRs.Builder builder = ScoutPosRs.newBuilder();
        builder.setCdTime(player.common.getScoutCdTime());
        builder.setMail(PbHelper.createMailPb(mail, player));
        return builder.build();
    }

}
