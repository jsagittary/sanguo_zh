package com.gryphpoem.game.zw.rpc.callback.warfire;

import com.gryphpoem.cross.gameplay.warfire.c2g.dto.WarFireCommonBuffVal;
import com.gryphpoem.cross.gameplay.warfire.c2g.dto.WarFireEndAward;
import com.gryphpoem.cross.gameplay.warfire.c2g.service.Cross2GameWarFireService;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CrossWarFireLocalData;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class Cross2GameWarFireServiceImpl implements Cross2GameWarFireService {

    @Override
    public void sendWarFireEndReward(Collection<WarFireEndAward> collection) {
        LogUtil.debug("Cross2GameWarFireServiceImpl sendWarFireEndReward data starting");
        if (ObjectUtils.isEmpty(collection)) {
            LogUtil.error("Cross2GameWarFireServiceImpl sendWarFireEndReward data is empty");
            return;
        }

        Java8Utils.syncMethodInvoke(() -> {
            int nowSec = TimeHelper.getCurrentSecond();
            collection.forEach(warFireEndAward -> {
                if (CheckNull.isNull(warFireEndAward))
                    return;

                try {
                    Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(warFireEndAward.getRoleId());
                    if (CheckNull.isNull(player)) {
                        LogUtil.debug("player not in this server, lordId: ", warFireEndAward.getRoleId());
                        return;
                    }

                    int preAwardCount = warFireEndAward.getCpCnt() + warFireEndAward.getGrCnt();
                    List<CommonPb.Award> finalAwards = createEndAward(player, preAwardCount, warFireEndAward.getGrRank());
                    CommonPb.Award coinAward = finalAwards.stream().filter(award -> award.getType() == AwardType.MONEY && award.getId() == AwardType.Money.CROSS_WAR_FIRE_COIN).findFirst().orElse(null);
                    int awardCountBuff = CheckNull.isNull(coinAward) ? 0 : coinAward.getCount();
                    LogUtil.world(String.format("跨服战火燎原 --- 玩家 :%d, 积分 :%d, 个人奖励 :%d, 阵营奖励 :%d",
                            warFireEndAward.getRoleId(), warFireEndAward.getScore(), warFireEndAward.getGrCnt(), warFireEndAward.getCpCnt()));

                    int killedScore = (warFireEndAward.getKilled() / WorldConstant.CROSS_WAR_FIRE_KILL_SCORE.get(0)) * WorldConstant.CROSS_WAR_FIRE_KILL_SCORE.get(1);
                    Optional.ofNullable(DataResource.getBean(MailDataManager.class).sendAttachMail(player, finalAwards, MailConstant.CROSS_WAR_FIRE_AWARD, AwardFrom.CROSS_WAR_FIRE_END_AWARD, nowSec,
                            //阵营信息
                            warFireEndAward.getForce(),
                            warFireEndAward.getScore() - warFireEndAward.getExtScore(),//结束时获得到的阵营资源
                            warFireEndAward.getCalcVar1(), warFireEndAward.getCalcVar2(), warFireEndAward.getCalcVar3(), warFireEndAward.getExtScore(),
                            warFireEndAward.getScore(), warFireEndAward.getCampRank(), warFireEndAward.getCpCnt(),
                            //个人信息
                            warFireEndAward.getScore() - killedScore,
                            warFireEndAward.getKilled(), killedScore,
                            warFireEndAward.getScore(), warFireEndAward.getGrCnt(),
                            awardCountBuff, DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(player, SeasonConst.TALENT_EFFECT_620), awardCountBuff - warFireEndAward.getGrCnt() - warFireEndAward.getCpCnt())).ifPresent(mail -> mail.setCross(true));
                } catch (Exception e) {
                    LogUtil.error("sendCrossWarFireEndReward error, warFireEndAward: ", warFireEndAward.toString());
                }
            });

            DataResource.getBean(CrossGamePlayService.class).syncLeaveCross(((WarFireEndAward) collection.toArray()[0]).getGamePlanKey());
        });
    }

    @Override
    public void syncCommonBuff(Map<Integer, WarFireCommonBuffVal> map) {
        if (CheckNull.isNull(map)) {
            LogUtil.error("syncCommonBuff map is empty");
            return;
        }

        Optional.ofNullable(DataResource.getBean(PlayerDataManager.class).getAllPlayer().values()).ifPresent(players -> {
            Java8Utils.syncMethodInvoke(() -> {
                Optional.ofNullable(DataResource.getBean(PlayerDataManager.class).getAllPlayer().values()).ifPresent(allPlayer -> {
                    allPlayer.forEach(player -> {
                        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getOpenPlan(player, CrossFunction.CROSS_WAR_FIRE.getFunctionId());
                        if (CheckNull.isNull(plan))
                            return;

                        WarFireCommonBuffVal buffVal = map.get(CrossEntity2Dto.getForce(player,
                                CrossFunction.CROSS_WAR_FIRE.getFunctionId(), DataResource.getBean(ServerSetting.class).getServerID()));
                        if (CheckNull.isNull(buffVal))
                            return;

                        CrossWarFireLocalData data = (CrossWarFireLocalData) player.crossPlayerLocalData.
                                getCrossFunctionData(CrossFunction.CROSS_WAR_FIRE, plan.getKeyId(), true);
                        data.setCityBuff(buffVal.getAttrMap());
                        if (data.isInCross())
                            CalculateUtil.reCalcAllHeroAttr(player);
                    });

                });
            });
        });

    }

    private List<CommonPb.Award> createEndAward(Player player, int preAwardCount, int grRank) {
        //赛季天赋优化， 积分加成
        int awardCountBuff = (int) (preAwardCount * (1 + (DataResource.getBean(SeasonTalentService.class).
                getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND)));
        CommonPb.Award awardPb = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.CROSS_WAR_FIRE_COIN, awardCountBuff);
        List<CommonPb.Award> finalAwards = new ArrayList<>(2);
        finalAwards.add(awardPb);
        if (grRank == 1 && CheckNull.nonEmpty(Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT)) {
            DataResource.getBean(RewardDataManager.class).addAwardSignle(player, Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT.get(0),
                    Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT.get(1), Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT.get(2), AwardFrom.CROSS_WAR_FIRE_END_AWARD);
            finalAwards.add(PbHelper.createAwardPb(Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT.get(0),
                    Constant.CROSS_WAR_FIRE_WINNER_PORTRAIT.get(1), 1));
        }

        return finalAwards;
    }
}
