package com.gryphpoem.game.zw.rpc.callback.map;

import com.gryphpoem.cross.common.CrossResponse;
import com.gryphpoem.cross.gameplay.battle.c2g.dto.*;
import com.gryphpoem.cross.gameplay.battle.c2g.service.GamePlayBattleService;
import com.gryphpoem.cross.gameplay.player.common.CrossPlayer;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticScoutDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.rpc.callback.CrossErrorCallback;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.WallService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class CrossWarFireBattleCallbackServiceImpl extends CrossErrorCallback implements GamePlayBattleService {

    @Override
    public List<CrossPlayer> getDefenderDetails(Set<Long> set, boolean b) {
        if (ObjectUtils.isEmpty(set)) {
            LogUtil.error("cross request data error, roleSet is null");
            return null;
        }

        AsyncContext asyncContext = RpcContext.startAsync();
        Java8Utils.syncMethodInvoke(() -> {
            Player player;
            List<CrossPlayer> list = new ArrayList<>();
            for (Long roleId : set) {
                player = DataResource.getBean(PlayerDataManager.class).getPlayer(roleId);
                if (CheckNull.isNull(player))
                    continue;

                try {
                    DataResource.getBean(PlayerDataManager.class).autoAddArmy(player);
                    DataResource.getBean(WallService.class).processAutoAddArmy(player);
                } catch (Exception e) {
                    LogUtil.error("跨服getDefenderDetails补兵出错 ", e);
                }

                list.add(CrossEntity2Dto.defenceBattle2dto(player, CrossFunction.CROSS_WAR_FIRE.getFunctionId(), b));
            }

            asyncContext.write(list);
        });

        return null;
    }

    @Override
    public void syncAttackMilitarySituation(MilitarySituation gameMilitarySituationDto) {
        if (CheckNull.isNull(gameMilitarySituationDto)) {
            LogUtil.error("gameMilitarySituationDto data is null");
            return;
        }

        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(gameMilitarySituationDto.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.debug("syncAttackMilitarySituation player not in this server, roleId: ", gameMilitarySituationDto.getRoleId());
            return;
        }
        if (!player.isLogin) {
            LogUtil.debug("syncAttackMilitarySituation player not online, roleId: ", gameMilitarySituationDto.getRoleId());
            return;
        }

        GamePb6.SyncGameMilitarySituationRs.Builder builder = GamePb6.SyncGameMilitarySituationRs.newBuilder();
        builder.setSituation(PbCrossUtil.buildGameMilitarySituation(gameMilitarySituationDto));
        if (player.ctx != null) {
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb6.SyncGameMilitarySituationRs.EXT_FIELD_NUMBER, GamePb6.SyncGameMilitarySituationRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    @Override
    public void syncHeroState(PlayerHeroState gameHeroStateDto) {
        if (ObjectUtils.isEmpty(gameHeroStateDto.getHeroIds())) {
            LogUtil.error("gameHeroStateDto heroIds is empty");
            return;
        }

        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(gameHeroStateDto.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.debug("syncHeroState player not in this server, roleId: ", gameHeroStateDto.getRoleId());
            return;
        }

        Java8Utils.syncMethodInvoke(() -> {
            Hero hero;
            Player tmp = DataResource.getBean(PlayerDataManager.class).getPlayer(gameHeroStateDto.getRoleId());
            for (Integer heroId : gameHeroStateDto.getHeroIds()) {
                hero = tmp.heros.get(heroId);
                if (CheckNull.isNull(hero))
                    continue;

                hero.setState(gameHeroStateDto.getState());
            }
        });
    }

    /**
     * 同步玩家战斗结算信息
     * 方法需要做到线程安全
     *
     * @param list
     */
    @Override
    public void syncPlayerFightSummary(List<PlayerFightSummary> list) {
        if (ObjectUtils.isEmpty(list)) {
            LogUtil.error("syncPlayerFightSummary data is null");
            return;
        }

        Java8Utils.syncMethodInvoke(() -> {
            for (PlayerFightSummary summary : list) {
                if (CheckNull.isNull(summary))
                    continue;

                Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(summary.getRoleId());
                if (CheckNull.isNull(player))
                    continue;

                summary.getHeroSummary().forEach(((heroId, heroFightSummary) -> {
                    switch (heroFightSummary.getType()) {
                        case Constant.Role.PLAYER:
                            Hero hero = player.heros.get(heroId);
                            if (CheckNull.isNull(hero)) {
                                LogUtil.error("syncPlayerFightSummary player heroId change! crossHeroId: ", heroId);
                                return;
                            }

                            hero.subArm(heroFightSummary.getLost());
                            //记录玩家兵力变化信息
                            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                            if (Objects.nonNull(staticHero)) {
                                LogLordHelper.filterHeroArm(AwardFrom.CROSS_BATTLE, player.account, player.lord, hero.getHeroId(), hero.getCount(), -heroFightSummary.getLost(),
                                        Constant.ACTION_SUB, staticHero.getType(), hero.getQuality());
                            }
                            DataResource.getBean(WarService.class).addCrossBattleHeroExp(heroFightSummary, player, AwardFrom.CROSS_BATTLE_AWARD);
                            int heroArmyCapacity = hero.getAttr()[HeroConstant.ATTR_LEAD];
                            int addArm = heroFightSummary.getRecover() + hero.getCount() >= heroArmyCapacity
                                    ? heroArmyCapacity - hero.getCount() : heroFightSummary.getRecover();
                            hero.addArm(addArm);//返还兵力
                            //记录玩家兵力变化信息
                            LogLordHelper.filterHeroArm(AwardFrom.CROSS_BATTLE, player.account, player.lord, hero.getHeroId(), hero.getCount(), addArm,
                                    Constant.ACTION_ADD, staticHero.getType(), hero.getQuality());
                            break;
                        case Constant.Role.WALL:
                            int pos = heroFightSummary.getPos();
                            Optional.ofNullable(player.wallNpc.get(pos)).ifPresent(wallNpc -> {
                                LogLordHelper.wallNPCArm(AwardFrom.CROSS_BATTLE, player.account, player.lord, wallNpc.getHeroNpcId(),
                                        wallNpc.getCount(), -wallNpc.subArm(heroFightSummary.getLost()), Constant.ACTION_SUB);
                            });
                            break;
                        default:
                            break;
                    }
                }));
            }
        });
    }

    @Override
    public CrossResponse checkHeroState(PlayerHeroState gameHeroStateDto, List<Integer> requiredHeroStatus) {
        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(gameHeroStateDto.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("checkHeroState player not exist");
            return new CrossResponse(GameError.PARAM_ERROR.getCode());
        }

        LogUtil.debug("checkHeroState params: ", gameHeroStateDto.toString(), ", checkStatus: ", ListUtils.toString(requiredHeroStatus));
        CrossResponse crossResponse = new CrossResponse(GameError.OK.getCode());
        try {
            if (CheckNull.isNull(gameHeroStateDto) || CheckNull.isEmpty(gameHeroStateDto.getHeroIds())) {
                throw new MwException(GameError.PARAM_ERROR);
            }

            Player tmp = DataResource.getBean(PlayerDataManager.class).getPlayer(gameHeroStateDto.getRoleId());
            for (Integer heroId : gameHeroStateDto.getHeroIds()) {
                Hero hero = tmp.heros.get(heroId);
                if (CheckNull.isNull(hero) || hero.getState() != gameHeroStateDto.getState()) {
                    LogUtil.error("crossBattle checkHeroState heroState: ", CheckNull.isNull(hero) ? -1 :
                            hero.getState(), ", checkState: ", gameHeroStateDto.getState());
                    throw new MwException(GameError.PARAM_ERROR);
                }
                if (!requiredHeroStatus.contains(hero.getStatus())) {
                    LogUtil.error("crossBattle checkHeroState heroStatus: ", CheckNull.isNull(hero) ? -1 :
                            hero.getStatus(), ", checkStatus: ", ListUtils.toString(requiredHeroStatus));
                    throw new MwException(GameError.PARAM_ERROR);
                }
            }
        } catch (Exception e) {
            if (e instanceof MwException) {
                crossResponse.setCode(((MwException) e).getCode());
            }

            LogUtil.error("", e);
            crossResponse.setCode(GameError.UNKNOWN_ERROR.getCode());
        }

        return crossResponse;
    }

    @Override
    public ScoutInfoResult invokeScout(InvokeScoutDto invokeScoutDto) {
        Player target = DataResource.getBean(PlayerDataManager.class).getPlayer(invokeScoutDto.getRoleId());
        if (CheckNull.isNull(target)) {
            LogUtil.error("GamePlayBattleService invokeScout player is null, lordId: ", invokeScoutDto.getRoleId());
            return null;
        }
        ScoutInfoResult result = new ScoutInfoResult(true);
        try {
            int scoutLv = invokeScoutDto.getInvokerScoutTechLv();
            int targetLv = target.getTechLvById(WorldConstant.SCOUT_TECH_ID);
            int gap = scoutLv - targetLv;
            gap += WorldConstant.getScoutAddByType(invokeScoutDto.getType());// 侦查类型加成

            Lord tarLord = target.lord;
            int ret = StaticScoutDataMgr.randomScoutResultByLvGap(gap);


            if (ret != WorldConstant.SCOUT_RET_FAIL) {
                // 根据侦查结果计算侦察到的信息
                CommonPb.ScoutRes sRes = null;
                CommonPb.ScoutCity city = null;
                List<CommonPb.ScoutHero> sHeroList = null;
                if (ret >= WorldConstant.SCOUT_RET_SUCC1) {// 只获取资源信息
                    Resource res = target.resource;
                    // 仓库保护
                    long[] proRes = DataResource.getBean(BuildingDataManager.class).getProtectRes(target);
                    Map<Integer, Integer> canPlunderRes = DataResource.getBean(BuildingDataManager.class).
                            canPlunderCrossScout(target, invokeScoutDto.getInvokerStorehouseLv(), proRes);
                    List<CommonPb.TwoInt> canPlunderList = new ArrayList<>();
                    for (Map.Entry<Integer, Integer> kv : canPlunderRes.entrySet()) {
                        CommonPb.TwoInt ti = PbHelper.createTwoIntPb(kv.getKey(), kv.getValue());
                        canPlunderList.add(ti);
                    }
                    sRes = PbHelper.createScoutResPb(proRes[1], proRes[2], proRes[0], res.getOre(), res.getHuman(),
                            canPlunderList);
                    if (ret >= WorldConstant.SCOUT_RET_SUCC2) {// 获取资源、城池信息
                        city = PbHelper.createScoutCityPb(target.building.getWall(), tarLord.getFight(),
                                (int) res.getArm1(), (int) res.getArm2(), (int) res.getArm3());
                        if (ret >= WorldConstant.SCOUT_RET_SUCC3) {
                            // 获取资源、城池、将领信息
                            List<Hero> defenderHeroes = target.getAllOnBattleHeros();
                            // 玩家所有上阵将领信息
                            sHeroList = new ArrayList<>();
                            int state;
                            int source;
                            for (Hero hero : defenderHeroes) {
                                source = WorldConstant.HERO_SOURCE_BATTLE;
                                state = DataResource.getBean(WorldService.class).getScoutHeroState(source, hero.getState());
                                sHeroList.add(PbHelper.createScoutHeroPb(hero, source, state, target));
                            }
                        }
                    }
                }

                CommonPb.MailScout scout = PbHelper.createMailScoutPb(sRes, city, sHeroList);
                result.setMailScout(scout.toByteArray());
            } else {
                // 侦查失败邮件
                result.setScoutResult(false);
            }
        } catch (Exception e) {
            LogUtil.error("cross invokeScout fail, e: ", e);
            result.setScoutResult(false);
        } finally {
            return result;
        }
    }

    @Override
    public void invokeScoutPlayerRs(CrossResponse crossResponse) {
        Java8Utils.syncMethodInvoke(() -> {
            Player player_ = null;
            BasePb.Base.Builder msg = BasePb.Base.newBuilder();
            try {
                player_ = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
                checkPlayerExist(GamePb6.ScoutCrossPosRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player_, msg);
                checkCrossResponse(GamePb6.ScoutCrossPosRs.EXT_FIELD_NUMBER, crossResponse, msg);

                player_.common.setScoutCdTime((Integer) crossResponse.getDto());

                GamePb6.ScoutCrossPosRs.Builder builder = GamePb6.ScoutCrossPosRs.newBuilder();
                builder.setCdTime(player_.common.getScoutCdTime());

                msg = PbHelper.createSynBase(GamePb6.ScoutCrossPosRs.EXT_FIELD_NUMBER,
                        GamePb6.ScoutCrossPosRs.ext, builder.build());
            } catch (Exception e) {
                if (e instanceof MwException) return;
                LogUtil.error(getClass().getSimpleName(), ", method getCrossMarchRs: ", "Not Hand  Exception -->", e);
                msg = PbHelper.createErrorBase(GamePb6.ScoutCrossPosRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
            } finally {
                BasePb.Base base = msg.build();
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player_);
            }
        });
    }
}
