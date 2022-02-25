package com.gryphpoem.game.zw.rpc.callback.map;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.cross.common.CrossResponse;
import com.gryphpoem.cross.gameplay.map.c2g.dto.CrossMapCloseDto;
import com.gryphpoem.cross.gameplay.map.c2g.dto.GameMapEventDto;
import com.gryphpoem.cross.gameplay.map.c2g.dto.SyncArmyDto;
import com.gryphpoem.cross.gameplay.map.c2g.service.Cross2GameMapService;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.pb.MapEventPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.rpc.callback.CrossErrorCallback;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class Cross2GameMapCallbackServiceImpl extends CrossErrorCallback implements Cross2GameMapService {

    @Override
    public void getCrossMapRs(CrossResponse crossResponse, Set<Integer> var2) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetCrossWarFireMapRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetCrossWarFireMapRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<MapEventPb.MapPoint> mapPoints = new ArrayList<>();
            for (Object data : crossResponse.getExt()) {
                mapPoints.add(MapEventPb.MapPoint.parseFrom((byte[]) data));
            }

            GamePb6.GetCrossWarFireMapRs.Builder builder = GamePb6.GetCrossWarFireMapRs.newBuilder();
            builder.addAllMapPoints(mapPoints);
            builder.addAllBlock(var2);
            builder.setFunctionId(StaticNewCrossDataMgr.getStaticCrossGamePlayPlan(crossResponse.getPlanKey()).getActivityType());
            msg = PbHelper.createSynBase(GamePb6.GetCrossWarFireMapRs.EXT_FIELD_NUMBER,
                    GamePb6.GetCrossWarFireMapRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossMapRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetCrossWarFireMapRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void getCrossMarchRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetCrossWarFireMarchRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetCrossWarFireMarchRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<CommonPb.MapLine> mapLines = new ArrayList<>();
            for (Object data : crossResponse.getExt()) {
                mapLines.add(CommonPb.MapLine.parseFrom((byte[]) data));
            }

            GamePb6.GetCrossWarFireMarchRs.Builder builder = GamePb6.GetCrossWarFireMarchRs.newBuilder();
            builder.addAllMarch(mapLines);
            msg = PbHelper.createSynBase(GamePb6.GetCrossWarFireMarchRs.EXT_FIELD_NUMBER,
                    GamePb6.GetCrossWarFireMarchRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossMarchRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetCrossWarFireMarchRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void getCrossAreaRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetCrossWarFireAreaRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetCrossWarFireAreaRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<MapEventPb.MapPoint> mapPoints = new ArrayList<>();
            for (Object data : crossResponse.getExt()) {
                mapPoints.add(MapEventPb.MapPoint.parseFrom((byte[]) data));
            }

            GamePb6.GetCrossWarFireAreaRs.Builder builder = GamePb6.GetCrossWarFireAreaRs.newBuilder();
            builder.addAllMapPoints(mapPoints);
            msg = PbHelper.createSynBase(GamePb6.GetCrossWarFireAreaRs.EXT_FIELD_NUMBER,
                    GamePb6.GetCrossWarFireAreaRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossAreaRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetCrossWarFireAreaRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void enterCrossMapRs(CrossResponse crossResponse) {
        Java8Utils.syncMethodInvoke(() -> {
            BasePb.Base.Builder msg = BasePb.Base.newBuilder();
            Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            try {
                checkPlayerExist(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
                checkCrossResponse(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, crossResponse, msg);

                Integer newPos = (Integer) crossResponse.getDto();

                //跨服请求完成后的逻辑处理
                int prePos = player.lord.getPos();
                long roleId = crossResponse.getLordId();
                int now = TimeHelper.getCurrentSecond();
                WarService warService = DataResource.getBean(WarService.class);
                WorldService worldService = DataResource.getBean(WorldService.class);
                MailDataManager mailDataManager = DataResource.getBean(MailDataManager.class);
                WorldDataManager worldDataManager = DataResource.getBean(WorldDataManager.class);
                PlayerDataManager playerDataManager = DataResource.getBean(PlayerDataManager.class);
                if (player.battleMap.containsKey(prePos)) {// 改到玩家到达后没发现目标
                    warService.cancelCityBattle(prePos, -1, false, true);
                }

                // 友军的城墙驻防部队返回
                List<Army> guardArrays = worldDataManager.getPlayerGuard(prePos);
                if (!CheckNull.isEmpty(guardArrays)) {
                    for (Army army : guardArrays) {
                        Player tPlayer = playerDataManager.getPlayer(army.getLordId());
                        if (tPlayer == null || tPlayer.armys.get(army.getKeyId()) == null) {
                            continue;
                        }
                        worldService.retreatArmyByDistance(tPlayer, army, now);
                        worldService.synRetreatArmy(tPlayer, army, now);
                        worldDataManager.removePlayerGuard(army.getTarget(), army);
                        int heroId = army.getHero().get(0).getV1();
                        mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                                heroId, player.lord.getNick(), heroId);
                    }
                }

                // 旧地图上移除玩家
                Optional.ofNullable(playerDataManager.getPlayerByArea(player.lord.getArea())).ifPresent(map -> map.remove(roleId));
                worldDataManager.removePlayerPos(prePos, player);
                // 旧地图上推送
                List<Integer> posList = new ArrayList<>(1);
                posList.add(prePos);

                StaticCrossGamePlayPlan gamePlayPlan = null;
                StaticCrossGamePlayPlan crossPlan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan(crossResponse.getPlanKey());
                if (Objects.nonNull(crossPlan)) {
                    gamePlayPlan = StaticNewCrossDataMgr.getOpenPlan(player, crossPlan.getActivityType());
                }
                player.crossPlayerLocalData.enterCross(CrossFunction.CROSS_WAR_FIRE, gamePlayPlan.getKeyId(), newPos);
                EventBus.getDefault().post(new Events.RmMapFocusEvent(player));
                EventBus.getDefault()
                        .post(new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
                player.lord.setArea(NewCrossConstant.CROSS_WAR_FIRE_MAP);
                player.lord.setPos(newPos);
                // 玩家进入新地图埋点
                LogLordHelper.commonLog("newCrossEnter", AwardFrom.WAR_FIRE_ENTER, player, prePos, newPos);

                CalculateUtil.reCalcAllHeroAttr(player);
                GamePb6.CrossWarFireMoveCityRs.Builder builder = GamePb6.CrossWarFireMoveCityRs.newBuilder();
                builder.setPos(player.lord.getPos());
                builder.setArea(player.lord.getArea());
                msg = PbHelper.createSynBase(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER,
                        GamePb6.CrossWarFireMoveCityRs.ext, builder.build());
                DataResource.getBean(CrossGamePlayService.class).afterEnterCrossMap(player, now);
            } catch (Exception e) {
                if (e instanceof MwException) return;
                LogUtil.error(getClass().getSimpleName(), ", method enterCrossMapRs: ", "Not Hand  Exception -->", e);
                msg = PbHelper.createErrorBase(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
            } finally {
                BasePb.Base base = msg.build();
                if (Objects.nonNull(player))
                    DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
                else
                    LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
            }
        });
    }

    @Override
    public void leaveCrossMapRs(CrossResponse crossResponse) {
        Java8Utils.syncMethodInvoke(() -> {
            Player player = null;
            BasePb.Base.Builder msg = BasePb.Base.newBuilder();
            try {
                player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
                checkPlayerExist(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
                checkCrossResponse(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, crossResponse, msg);

                // 玩家进入新地图埋点
                StaticCrossGamePlayPlan staticCrossGamePlayPlan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan(crossResponse.getPlanKey());
                DataResource.getBean(CrossGamePlayService.class).leaveCrossMap(CrossFunction.convertTo(staticCrossGamePlayPlan.getActivityType()), staticCrossGamePlayPlan, player);

                GamePb6.CrossWarFireMoveCityRs.Builder builder = GamePb6.CrossWarFireMoveCityRs.newBuilder();
                builder.setPos(player.lord.getPos());
                builder.setArea(player.lord.getArea());
                msg = PbHelper.createSynBase(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER,
                        GamePb6.CrossWarFireMoveCityRs.ext, builder.build());
            } catch (Exception e) {
                if (e instanceof MwException) return;
                LogUtil.error(getClass().getSimpleName(), ", method leaveCrossMapRs: ", "Not Hand  Exception -->", e);
                msg = PbHelper.createErrorBase(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
            } finally {
                BasePb.Base base = msg.build();
                if (Objects.nonNull(player))
                    DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
                else
                    LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
            }
        });
    }

    @Override
    public void enterLeaveCrossMapRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.EnterLeaveCrossWarFireRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.EnterLeaveCrossWarFireRs.EXT_FIELD_NUMBER, crossResponse, msg);

            GamePb6.EnterLeaveCrossWarFireRs.Builder builder = GamePb6.EnterLeaveCrossWarFireRs.newBuilder();
            msg = PbHelper.createSynBase(GamePb6.EnterLeaveCrossWarFireRs.EXT_FIELD_NUMBER,
                    GamePb6.EnterLeaveCrossWarFireRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method enterLeaveCrossMapRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.EnterLeaveCrossWarFireRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void getCrossMineRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetNewCrossMineRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetNewCrossMineRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<MapEventPb.MapPoint> mapPoints = new ArrayList<>();
            Optional.ofNullable(crossResponse.getExt()).ifPresent(ext -> {
                for (Object object : crossResponse.getExt()) {
                    try {
                        mapPoints.add(MapEventPb.MapPoint.parseFrom((byte[]) object));
                    } catch (InvalidProtocolBufferException e) {
                        LogUtil.error("", e);
                        continue;
                    }
                }
            });

            GamePb6.GetNewCrossMineRs.Builder builder = GamePb6.GetNewCrossMineRs.newBuilder();
            if (!ObjectUtils.isEmpty(mapPoints)) {
                builder.setMine(mapPoints.get(0));
            }
            msg = PbHelper.createSynBase(GamePb6.GetNewCrossMineRs.EXT_FIELD_NUMBER,
                    GamePb6.GetNewCrossMineRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossMineRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetNewCrossMineRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void getCrossArmyRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetCrossWarFireArmyRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetCrossWarFireArmyRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<CommonPb.Army> armies = new ArrayList<>();
            if (!ObjectUtils.isEmpty(crossResponse.getExt())) {
                for (Object data : crossResponse.getExt()) {
                    armies.add(CommonPb.Army.parseFrom((byte[]) data));
                }
            }

            GamePb6.GetCrossWarFireArmyRs.Builder builder = GamePb6.GetCrossWarFireArmyRs.newBuilder();
            builder.addAllArmy(armies);
            msg = PbHelper.createSynBase(GamePb6.GetCrossWarFireArmyRs.EXT_FIELD_NUMBER,
                    GamePb6.GetCrossWarFireArmyRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossArmyRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetCrossWarFireArmyRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void crossAttackPosRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.CrossWarFireAttackPosRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.CrossWarFireAttackPosRs.EXT_FIELD_NUMBER, crossResponse, msg);

            CommonPb.Army army = null;
            if (!ObjectUtils.isEmpty(crossResponse.getExt()))
                army = CommonPb.Army.parseFrom((byte[]) crossResponse.getExt().get(0));

            GamePb6.CrossWarFireAttackPosRs.Builder builder = GamePb6.CrossWarFireAttackPosRs.newBuilder();
            Optional.ofNullable(army).ifPresent(data -> builder.setArmy(data));
            msg = PbHelper.createSynBase(GamePb6.CrossWarFireAttackPosRs.EXT_FIELD_NUMBER,
                    GamePb6.CrossWarFireAttackPosRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method crossAttackPosRs: ", "Not Hand  Exception -->" + e.getMessage(), e);
            msg = PbHelper.createErrorBase(GamePb6.CrossWarFireAttackPosRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void getCrossMilitarySituationRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetCrossWarFireMilitarySituationRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetCrossWarFireMilitarySituationRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<CommonPb.GameMilitarySituationPb> list = new ArrayList<>();
            if (!ObjectUtils.isEmpty(crossResponse.getExt())) {
                for (Object data : crossResponse.getExt()) {
                    list.add(CommonPb.GameMilitarySituationPb.parseFrom((byte[]) data));
                }
            }

            GamePb6.GetCrossWarFireMilitarySituationRs.Builder builder = GamePb6.GetCrossWarFireMilitarySituationRs.newBuilder();
            Optional.ofNullable(list).ifPresent(data -> builder.addAllSituation(data));
            msg = PbHelper.createSynBase(GamePb6.GetCrossWarFireMilitarySituationRs.EXT_FIELD_NUMBER,
                    GamePb6.GetCrossWarFireMilitarySituationRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossMilitarySituationRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetCrossWarFireMilitarySituationRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void retreatCrossArmyRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.RetreatCrossWarFireRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.RetreatCrossWarFireRs.EXT_FIELD_NUMBER, crossResponse, msg);

            GamePb6.RetreatCrossWarFireRs.Builder builder = GamePb6.RetreatCrossWarFireRs.newBuilder();
            List<CommonPb.Army> armies = new ArrayList<>(crossResponse.getExt().size());
            if (!ObjectUtils.isEmpty(crossResponse.getExt())) {
                for (Object data : crossResponse.getExt()) {
                    armies.add(CommonPb.Army.parseFrom((byte[]) data));
                }
            }
            Optional.ofNullable(armies).ifPresent(data -> builder.setArmy(data.get(0)));
            msg = PbHelper.createSynBase(GamePb6.RetreatCrossWarFireRs.EXT_FIELD_NUMBER,
                    GamePb6.RetreatCrossWarFireRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method retreatCrossArmyRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.RetreatCrossWarFireRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void accelerateCrossArmyRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.NewCrossAccelerateArmyRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.NewCrossAccelerateArmyRs.EXT_FIELD_NUMBER, crossResponse, msg);

            GamePb6.NewCrossAccelerateArmyRs.Builder builder = GamePb6.NewCrossAccelerateArmyRs.newBuilder();
            List<CommonPb.Army> armies = new ArrayList<>(crossResponse.getExt().size());
            if (!ObjectUtils.isEmpty(crossResponse.getExt())) {
                for (Object data : crossResponse.getExt()) {
                    armies.add(CommonPb.Army.parseFrom((byte[]) data));
                }
            }
            Optional.ofNullable(armies).ifPresent(data -> builder.setArmy(data.get(0)));
            msg = PbHelper.createSynBase(GamePb6.NewCrossAccelerateArmyRs.EXT_FIELD_NUMBER,
                    GamePb6.NewCrossAccelerateArmyRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method accelerateCrossArmyRs: ", "Not Hand  Exception -->" + e);
            msg = PbHelper.createErrorBase(GamePb6.NewCrossAccelerateArmyRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void crossMoveCityInMapRs(CrossResponse crossResponse) {
        Java8Utils.syncMethodInvoke(() -> {
            Player player = null;
            BasePb.Base.Builder msg = BasePb.Base.newBuilder();
            try {
                player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
                checkPlayerExist(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
                checkCrossResponse(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, crossResponse, msg);

                Integer newPos = (Integer) crossResponse.getDto();
                if (CheckNull.isNull(newPos)) {
                    LogUtil.error("crossMoveCityInMapRs new pos is null, roleId: ", crossResponse.getLordId());
                    throw new Exception("crossMoveCityInMapRs new pos is null");
                }


                Player tmp = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
                StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan(crossResponse.getPlanKey());
                DataResource.getBean(CrossGamePlayService.class).commonMoveCity(tmp, plan.getActivityType(), newPos);


                GamePb6.CrossWarFireMoveCityRs.Builder builder = GamePb6.CrossWarFireMoveCityRs.newBuilder();
                builder.setPos(newPos);
                builder.setArea(player.lord.getArea());
                msg = PbHelper.createSynBase(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER,
                        GamePb6.CrossWarFireMoveCityRs.ext, builder.build());
            } catch (Exception e) {
                if (e instanceof MwException) return;
                LogUtil.error(getClass().getSimpleName(), ", method crossMoveCityInMapRs: ", "Not Hand  Exception -->", e);
                msg = PbHelper.createErrorBase(GamePb6.CrossWarFireMoveCityRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
            } finally {
                BasePb.Base base = msg.build();
                if (Objects.nonNull(player))
                    DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
                else
                    LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
            }
        });
    }

    @Override
    public void getCrossBattleRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetCrossWarFireBattleRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetCrossWarFireBattleRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<CommonPb.Battle> battles = new ArrayList<>();
            if (!ObjectUtils.isEmpty(crossResponse.getExt())) {
                for (Object data : crossResponse.getExt()) {
                    battles.add(CommonPb.Battle.parseFrom((byte[]) data));
                }
            }

            GamePb6.GetCrossWarFireBattleRs.Builder builder = GamePb6.GetCrossWarFireBattleRs.newBuilder();
            Optional.ofNullable(battles).ifPresent(data -> builder.addAllBattle(data));
            msg = PbHelper.createSynBase(GamePb6.GetCrossWarFireBattleRs.EXT_FIELD_NUMBER,
                    GamePb6.GetCrossWarFireBattleRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossBattleRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetCrossWarFireBattleRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void getCrossCityInfoRs(CrossResponse crossResponse) {
        Player player = null;
        BasePb.Base.Builder msg = BasePb.Base.newBuilder();
        try {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossResponse.getLordId());
            checkPlayerExist(GamePb6.GetCrossWarFireCityInfoRs.EXT_FIELD_NUMBER, crossResponse.getLordId(), player, msg);
            checkCrossResponse(GamePb6.GetCrossWarFireCityInfoRs.EXT_FIELD_NUMBER, crossResponse, msg);

            List<CommonPb.MapCity> mapCities = new ArrayList<>();
            if (!ObjectUtils.isEmpty(crossResponse.getExt())) {
                for (Object data : crossResponse.getExt()) {
                    mapCities.add(CommonPb.MapCity.parseFrom((byte[]) data));
                }
            }

            GamePb6.GetCrossWarFireCityInfoRs.Builder builder = GamePb6.GetCrossWarFireCityInfoRs.newBuilder();
            if (!ObjectUtils.isEmpty(mapCities))
                builder.setMapCity(mapCities.get(0));
            msg = PbHelper.createSynBase(GamePb6.GetCrossWarFireCityInfoRs.EXT_FIELD_NUMBER,
                    GamePb6.GetCrossWarFireCityInfoRs.ext, builder.build());
        } catch (Exception e) {
            if (e instanceof MwException) return;
            LogUtil.error(getClass().getSimpleName(), ", method getCrossCityInfoRs: ", "Not Hand  Exception -->", e);
            msg = PbHelper.createErrorBase(GamePb6.GetCrossWarFireCityInfoRs.EXT_FIELD_NUMBER, GameError.UNKNOWN_ERROR.getCode(), 0);
        } finally {
            BasePb.Base base = msg.build();
            if (Objects.nonNull(player))
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(base, player);
            else
                LogUtil.c2sMessage(base, CheckNull.isNull(crossResponse) ? -1 : crossResponse.getLordId());
        }
    }

    @Override
    public void syncCrossMapEvent(GameMapEventDto gameMapEventDto) {
        LogUtil.debug("cross war fire syncCrossMapEvent starting");
        if (CheckNull.isNull(gameMapEventDto)) {
            LogUtil.error("cross war fire syncCrossMapEvent data is null");
            return;
        }
        if (ObjectUtils.isEmpty(gameMapEventDto.getRoleIds()) || ObjectUtils.isEmpty(gameMapEventDto.getMapEvents())) {
            LogUtil.error("cross war fire syncCrossMapEvent roleIds is null or mapEvents is null");
            return;
        }

        GamePb6.SyncCrossWarFireMapEventRs.Builder builder = GamePb6.SyncCrossWarFireMapEventRs.newBuilder();
        for (byte[] data : gameMapEventDto.getMapEvents()) {
            try {
                builder.addMapChgEventInfo(MapEventPb.MapChgEventInfoPb.newBuilder().mergeFrom(data).build());
            } catch (InvalidProtocolBufferException e) {
                LogUtil.error("MapChgEventInfoPb parseFrom data wrong!");
                continue;
            }
        }


        Player player;
        GamePb6.SyncCrossWarFireMapEventRs rsp = builder.build();
        for (Long roleId : gameMapEventDto.getRoleIds()) {
            player = DataResource.getBean(PlayerDataManager.class).getPlayer(roleId);
            if (CheckNull.isNull(player)) {
                continue;
            }
            if (player.isLogin && player.ctx != null) {
                BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb6.SyncCrossWarFireMapEventRs.EXT_FIELD_NUMBER, GamePb6.SyncCrossWarFireMapEventRs.ext, rsp);
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    @Override
    public void syncCrossArmy(SyncArmyDto dto) {
        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(dto.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("syncCrossArmy player is null");
            return;
        }

        CommonPb.Army army = null;
        try {
            army = CommonPb.Army.parseFrom(dto.getArmy());
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error("", e);
        }

        GamePb6.SyncCrossArmyRs.Builder builder = GamePb6.SyncCrossArmyRs.newBuilder();
        Optional.ofNullable(army).ifPresent(data -> builder.setArmy(data));
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb6.SyncCrossArmyRs.EXT_FIELD_NUMBER, GamePb6.SyncCrossArmyRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    @Override
    public void syncCrossMapClose(CrossMapCloseDto dto) {

    }
}
