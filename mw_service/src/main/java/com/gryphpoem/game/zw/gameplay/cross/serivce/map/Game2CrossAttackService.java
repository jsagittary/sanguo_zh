package com.gryphpoem.game.zw.gameplay.cross.serivce.map;

import com.gryphpoem.cross.gameplay.battle.g2c.service.Game2CrossBattleService;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.manger.aop.CrossGameMapDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class Game2CrossAttackService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossGameMapDataMgr crossGameMapDataMgr;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private CrossGamePlayService crossGamePlayService;
    @Autowired
    private Game2CrossBattleService game2CrossBattleService;

    /**
     * ??????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public void newCrossAttackPos(long roleId, GamePb6.CrossWarFireAttackPosRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), true);
        List<Integer> heroIdList = req.getHeroIdList().stream().distinct().collect(Collectors.toList());
        crossGamePlayService.checkHeroUseful(player, heroIdList, true);

        //????????????
        int pos = req.getPos();
        // 1 ????????????2 ????????????3 ?????????
        int type = req.hasType() ? req.getType() : WorldConstant.CITY_BATTLE_BLITZ;

        crossGameMapDataMgr.attackCrossWarFirePos(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()),
                pos, type, req.getFunctionId(), heroIdList, player);
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb6.JoinBattleCrossWarFireRs joinBattleCrossWarFire(long roleId, GamePb6.JoinBattleCrossWarFireRq req) throws MwException {
        return null;
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public void getNewCrossMilitarySituation(long roleId, int functionId) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);
        crossGameMapDataMgr.getCrossMilitarySituation(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()));
    }

    /**
     * ????????????
     *
     * @param defender
     * @param functionId
     * @param newPos
     * @param atkPlayer
     */
    public void crossPlayerHitFly(Player defender, int functionId, int newPos, Player atkPlayer) {
        crossGamePlayService.commonMoveCity(defender, functionId, newPos);

        // ???????????????????????????????????????
        if (BuildingDataManager.getBuildingLv(BuildingType.WALL, defender) > 0) {
            defender.setFireState(true);
        }

        WarService warService = DataResource.ac.getBean(WarService.class);
        BuildingDataManager buildingDataManager = DataResource.ac.getBean(BuildingDataManager.class);
        // ???????????????????????????
        warService.syncRoleMove(defender, newPos);
        // ????????????
        buildingDataManager.SyncCrossRebuild(defender);
    }

    /**
     * ???????????????????????????id?????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public void getNewCrossBattle(long roleId, GamePb6.GetCrossWarFireBattleRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int battleId = req.getBattleId();
        int pos = req.getPos();
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), true);
        if (battleId <= 0 && pos <= 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "???????????????????????????, roleId: ", roleId);
        }
        crossGameMapDataMgr.getCrossWarFireBattle(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), battleId, pos);
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws Exception
     */
    public void scoutCrossPos(long roleId, GamePb6.ScoutCrossPosRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (req.getPos() <= 0 || req.getType() <= 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "???????????????????????????, roleId: ",
                    roleId, ", pos: ", req.getPos(), ", type: ", req.getType());
        }

        int cdTime = player.common.getScoutCdTime();
        int now = TimeHelper.getCurrentSecond();
        if (cdTime > now + WorldConstant.SCOUT_CD_MAX_TIME) {
            throw new MwException(GameError.SCOUT_CD_TIME.getCode(), "????????????????????????CD??????, roleId:", roleId, ", pos:", req.getPos(),
                    ", cdTime:", cdTime);
        }

        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), true);
        game2CrossBattleService.invokeScoutPlayerRq(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), req.getPos(), req.getType());
    }

    @GmCmd("crossAttack")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String crossGmCmd = params[0];
        if ("scout".equalsIgnoreCase(crossGmCmd)) {
            int pos = Integer.parseInt(params[1]);
            int type = Integer.parseInt(params[2]);
            GamePb6.ScoutCrossPosRq.Builder builder =  GamePb6.ScoutCrossPosRq.newBuilder();
            builder.setType(type);
            builder.setPos(pos);
            builder.setFunctionId(CrossFunction.CROSS_WAR_FIRE.getFunctionId());

            scoutCrossPos(player.getLordId(), builder.build());
        }
    }
}
