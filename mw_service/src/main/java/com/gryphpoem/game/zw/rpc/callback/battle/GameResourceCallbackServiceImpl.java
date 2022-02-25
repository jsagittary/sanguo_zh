package com.gryphpoem.game.zw.rpc.callback.battle;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.cross.common.CrossResponse;
import com.gryphpoem.cross.gameplay.resource.c2g.dto.PlunderResourceAttacker;
import com.gryphpoem.cross.gameplay.resource.c2g.dto.PlunderResourceDefender;
import com.gryphpoem.cross.gameplay.resource.c2g.dto.SendConsumeDto;
import com.gryphpoem.cross.gameplay.resource.c2g.dto.SendRewardDto;
import com.gryphpoem.cross.gameplay.resource.c2g.service.GameResourceService;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.map.Game2CrossAttackService;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.apache.dubbo.rpc.AsyncContext;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameResourceCallbackServiceImpl implements GameResourceService {

    @Override
    public CrossResponse plunderResource(PlunderResourceAttacker plunderResourceAttacker, PlunderResourceDefender plunderResourceDefender) {
        if (CheckNull.isNull(plunderResourceAttacker) || CheckNull.isNull(plunderResourceDefender)) {
            LogUtil.error("plunderResource data is null");
            return null;
        }

        PlayerDataManager playerDataManager = DataResource.getBean(PlayerDataManager.class);
        Player defender = playerDataManager.getPlayer(plunderResourceDefender.getRoleId());
        if (CheckNull.isNull(defender)) {
            LogUtil.error("plunderResource defender is null");
            return null;
        }

        final AsyncContext asyncContext = RpcContext.startAsync();
        Java8Utils.syncMethodInvoke(() -> {
            List<byte[]> list = null;
            try {
                Player def = DataResource.getBean(PlayerDataManager.class).getPlayer(plunderResourceDefender.getRoleId());
                list = DataResource.getBean(BuildingDataManager.class).dropCrossBattleAward
                        (plunderResourceAttacker.getStorehouseLv(), plunderResourceAttacker.isUnlockChemicalPlant(), def);

                StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan(plunderResourceAttacker.getGamePlanKey());
                DataResource.getBean(Game2CrossAttackService.class).crossPlayerHitFly(def, plan.getActivityType(), plunderResourceDefender.getNewPos(), null);
            } catch (Exception e) {
                asyncContext.write(e);
            }

            CrossResponse response = new CrossResponse(GameError.OK.getCode());
            response.setExt(list);
            asyncContext.write(response);
        });

        return null;
    }

    @Override
    public void sendReward(SendRewardDto sendRewardDto) {
        if (CheckNull.isNull(sendRewardDto) || CheckNull.isEmpty(sendRewardDto.getAwardList())) {
            LogUtil.error("GameResourceService sendReward transfer sendRewardDto");
            return;
        }

        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(sendRewardDto.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("GameResourceService sendRewardDto player is null");
            return;
        }

        Java8Utils.syncMethodInvoke(() -> {
            Player tmp = DataResource.getBean(PlayerDataManager.class).getPlayer(sendRewardDto.getRoleId());
            if (CheckNull.isNull(tmp)) {
                LogUtil.error("GameResourceService player check is null");
                return;
            }

            List<CommonPb.Award> awardList = new ArrayList<>(sendRewardDto.getAwardList().size());
            for (Object data : sendRewardDto.getAwardList()) {
                try {
                    awardList.add(CommonPb.Award.parseFrom((byte[]) data));
                } catch (InvalidProtocolBufferException e) {
                    LogUtil.error("", e);
                    continue;
                }
            }

            DataResource.getBean(RewardDataManager.class).sendRewardByAwardList(tmp, awardList, AwardFrom.getAwardFrom(sendRewardDto.getAwardForm()), sendRewardDto.getGamePlanKey());
        });
    }

    @Override
    public CrossResponse checkResourceIsEnough(SendConsumeDto sendConsumeDto) {
        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(sendConsumeDto.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("checkAndSubResource player not exist");
            return new CrossResponse(GameError.PARAM_ERROR.getCode());
        }

        final AsyncContext asyncContext = RpcContext.startAsync();
        Java8Utils.syncMethodInvoke(() -> {
            CrossResponse crossResponse = new CrossResponse(GameError.OK.getCode());
            try {
                Player player_ = DataResource.getBean(PlayerDataManager.class).getPlayer(sendConsumeDto.getRoleId());
                List<List<Integer>> consumeList = new ArrayList<>(sendConsumeDto.getConsumes().size());
                for (byte[] data : sendConsumeDto.getConsumes()) {
                    List<Integer> consume = new ArrayList<>(3);
                    CommonPb.Award award = CommonPb.Award.parseFrom(data);
                    consume.add(award.getType());
                    consume.add(award.getId());
                    consume.add(award.getCount());
                    consumeList.add(consume);
                }

                DataResource.getBean(RewardDataManager.class).checkPlayerResIsEnough(player_, consumeList, AwardFrom.getAwardFrom(sendConsumeDto.getSubFrom()).toString());
            } catch (Exception e) {
                if (e instanceof MwException) {
                    crossResponse.setCode(((MwException) e).getCode());
                    return;
                }

                crossResponse.setCode(GameError.UNKNOWN_ERROR.getCode());
            } finally {
                asyncContext.write(crossResponse);
            }
        });

        return null;
    }

    @Override
    public CrossResponse checkAndSubResource(SendConsumeDto sendConsumeDto) {
        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(sendConsumeDto.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("checkAndSubResource player not exist");
            return new CrossResponse(GameError.PARAM_ERROR.getCode());
        }

        final AsyncContext asyncContext = RpcContext.startAsync();
        Java8Utils.syncMethodInvoke(() -> {
            CrossResponse crossResponse = new CrossResponse(GameError.OK.getCode());
            try {
                Player player_ = DataResource.getBean(PlayerDataManager.class).getPlayer(sendConsumeDto.getRoleId());
                List<List<Integer>> consumeList = new ArrayList<>(sendConsumeDto.getConsumes().size());
                for (byte[] data : sendConsumeDto.getConsumes()) {
                    List<Integer> consume = new ArrayList<>(3);
                    CommonPb.Award award = CommonPb.Award.parseFrom(data);
                    consume.add(award.getType());
                    consume.add(award.getId());
                    consume.add(award.getCount());
                    consumeList.add(consume);
                }

                DataResource.getBean(RewardDataManager.class).checkAndSubPlayerRes(player_, consumeList, AwardFrom.getAwardFrom(sendConsumeDto.getSubFrom()));
            } catch (Exception e) {
                if (e instanceof MwException) {
                    crossResponse.setCode(((MwException) e).getCode());
                    return;
                }

                crossResponse.setCode(GameError.UNKNOWN_ERROR.getCode());
            } finally {
                asyncContext.write(crossResponse);
            }
        });

        return null;
    }
}
