package com.gryphpoem.game.zw.gameplay.local.service.newyork;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.newyork.NewYorkPlayerIntegralRank;
import com.gryphpoem.game.zw.gameplay.local.world.newyork.NewYorkWar;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.pb.GamePb5.NewYorkWarAchievementRs;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticNewYorkWarAchievement;
import com.gryphpoem.game.zw.resource.domain.s.StaticNewYorkWarCampRank;
import com.gryphpoem.game.zw.resource.domain.s.StaticNewYorkWarPersonalRank;
import com.gryphpoem.game.zw.resource.pojo.IntegralRank;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by pengshuo on 2019/5/9 9:58
 * <br>Description: 纽约争霸 奖励领取
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class NewYorkWarAwardService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private DressUpDataManager dressUpDataManager;

    /**
     * 玩家终身成就奖励领取
     */
    public NewYorkWarAchievementRs lifeLongAward(long roleId, GamePb5.NewYorkWarAchievementRq req) throws MwException {
        int keyId = req.getKeyId();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Map<Integer, Integer> achievements = Optional.ofNullable(player.newYorkWar.getAchievements()).orElse(new HashMap<>(2));
        // 已领取
        int hasChange = Optional.ofNullable(achievements.get(keyId)).orElse(0);
        StaticNewYorkWarAchievement newYorkWarAchievement = StaticCrossWorldDataMgr.getStaticNewYorkWarAchievement(keyId);
        if (newYorkWarAchievement == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId: ", roleId);
        }
        if (hasChange >= newYorkWarAchievement.getCount()) {
            throw new MwException(GameError.PROMOTION_GIFT_MAX.getCode(), "领取奖励已达上限, roleId: ", roleId);
        }
        int value = newYorkWarAchievement.getCond();
        long have = player.newYorkWar.getMaxAttack();
        if (have < value) {
            throw new MwException(GameError.EXCHANGE_AWARD_NOT_ENOUGH.getCode(), "纽约争霸-成就奖励-领取不满足, roleId: ",
                    roleId, ", need:", value, ", have:", have);
        }
        List<List<Integer>> awardList = newYorkWarAchievement.getAwardList();
        if (awardList == null || awardList.isEmpty()) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "awardList 找不到配置, roleId: ", roleId);
        }
        // 检查玩家背包
        rewardDataManager.checkBag(player, awardList);
        // 发放奖励
        List<CommonPb.Award> awards = rewardDataManager.sendReward(player, awardList, AwardFrom.NEWYORK_WAR_ACHIEVEMENT_AWARD, keyId);
        // 保存以获取状态
        achievements.put(keyId, ++hasChange);
        player.newYorkWar.setAchievements(achievements);
        // 返回结果
        NewYorkWarAchievementRs.Builder builder = NewYorkWarAchievementRs.newBuilder();
        // 对返回结果进行处理
        builder.addAllAward(awards);
        builder.setKeyId(keyId);
        return builder.build();
    }

    /**
     * 发放 纽约争夺战奖励
     */
    public void giveNewYorkWarAward() {
        joinAward();
        joinSuccessAward();
        campRankAward();
        personalRankAward();
    }

    /**
     * 纽约争夺战参与奖
     */
    private void joinAward() {
        try {
            int now = TimeHelper.getCurrentSecond();
            NewYorkWar newYorkWar = crossWorldMapDataManager.getAndCheckNewYorkWar();
            int finalOccupyCamp = newYorkWar.getFinalOccupyCamp();
            newYorkWar.getPlayersIntegral().values().stream()
                    .filter(i -> i.getCamp() != finalOccupyCamp)
                    .forEach(integralRank -> {
                        Player player = playerDataManager.getPlayer(integralRank.getLordId());
                        Optional.ofNullable(player).ifPresent(p -> {
                            StaticCastleSkin skinNewYorkWarEffect = getSkinNewYorkWarEffect(p);
                            List<List<Integer>> awards = WorldConstant.NEWYORK_WAR_JOIN_AWARD;
                            if (skinNewYorkWarEffect == null) {
                                mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                        MailConstant.MOLD_NEWYORK_WAR_JOIN_AWARD, AwardFrom.NEWYORK_WAR_JOIN_AWARD, now
                                );
                            } else {
                                double effect = skinNewYorkWarEffect.getEffectVal() / Constant.TEN_THROUSAND;
                                awards = effectAwards(awards, effect);
                                mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                        MailConstant.MOLD_NEWYORK_WAR_JOIN_AWARD, AwardFrom.NEWYORK_WAR_JOIN_AWARD, now,
                                        skinNewYorkWarEffect.getId()
                                );
                            }
                            // 日志记录
                            LogLordHelper.commonLog("newYorkWar", AwardFrom.NEWYORK_WAR_JOIN_AWARD, p,
                                    integralRank.getValue(), finalOccupyCamp, serverSetting.getServerID(),awards);
                        });
                    });
        } catch (Exception e) {
            LogUtil.error("纽约争夺战参与奖 发放 error ", e.getMessage());
        }
    }

    /**
     * 纽约争夺战优胜奖
     */
    private void joinSuccessAward() {
        try {
            int now = TimeHelper.getCurrentSecond();
            NewYorkWar newYorkWar = crossWorldMapDataManager.getAndCheckNewYorkWar();
            int finalOccupyCamp = newYorkWar.getFinalOccupyCamp();
            newYorkWar.getPlayersIntegral().values().stream()
                    .filter(i -> i.getCamp() == finalOccupyCamp)
                    .forEach(integralRank -> {
                        Player player = playerDataManager.getPlayer(integralRank.getLordId());
                        Optional.ofNullable(player).ifPresent(p -> {
                            StaticCastleSkin skinNewYorkWarEffect = getSkinNewYorkWarEffect(p);
                            List<List<Integer>> awards = WorldConstant.NEWYORK_WAR_JOIN_SUCCESS_AWARD;
                            if (skinNewYorkWarEffect == null) {
                                mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                        MailConstant.MOLD_NEWYORK_WAR_JOIN_SUCCESS_AWARD,
                                        AwardFrom.NEWYORK_WAR_JOIN_AWARD, now,finalOccupyCamp
                                );
                            } else {
                                double effect = skinNewYorkWarEffect.getEffectVal() / Constant.TEN_THROUSAND;
                                awards = effectAwards(awards, effect);
                                mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                        MailConstant.MOLD_NEWYORK_WAR_JOIN_SUCCESS_AWARD,
                                        AwardFrom.NEWYORK_WAR_JOIN_AWARD, now, finalOccupyCamp,
                                        skinNewYorkWarEffect.getId()
                                );
                            }
                            // 日志记录
                            LogLordHelper.commonLog("newYorkWar", AwardFrom.NEWYORK_WAR_JOIN_AWARD, p,
                                    integralRank.getValue(), finalOccupyCamp,serverSetting.getServerID(), awards);
                        });
                    });
        } catch (Exception e) {
            LogUtil.error("纽约争夺战优胜奖 发放 error ", e.getMessage());
        }
    }

    /**
     * 阵营杀敌排行奖励
     */
    private void campRankAward() {
        try {
            NewYorkWar newYorkWar = crossWorldMapDataManager.getAndCheckNewYorkWar();
            // 阵营排行
            List<IntegralRank> ranks = newYorkWar.getCampIntegral().values().stream()
                    .filter(integralRank -> integralRank.getValue() > 0)
                    .sorted(Comparator.comparingLong(IntegralRank::getValue).reversed()
                            .thenComparingInt(IntegralRank::getSecond)
                    ).collect(Collectors.toList());
            List<NewYorkPlayerIntegralRank> playerRanks = new ArrayList<>(newYorkWar.getPlayersIntegral().values());
            // 奖励发放
            int now = TimeHelper.getCurrentSecond();
            Stream.iterate(1, i -> ++i).limit(ranks.size()).forEach(y -> {
                StaticNewYorkWarCampRank rank = StaticCrossWorldDataMgr.getStaticNewYorkWarCampRank(y);
                Optional.ofNullable(rank).ifPresent(r -> {
                    IntegralRank integralRank = ranks.get(y - 1);
                    int camp = integralRank.getCamp();
                    playerRanks.stream().filter(playerRank -> camp == playerRank.getCamp()).forEach(playerRank -> {
                        Player player = playerDataManager.getPlayer(playerRank.getLordId());
                        Optional.ofNullable(player).ifPresent(p -> {
                            // 奖励发放
                            List<List<Integer>> awards = r.getAwardList();
                            StaticCastleSkin skinNewYorkWarEffect = getSkinNewYorkWarEffect(p);
                            if (skinNewYorkWarEffect == null) {
                                mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                        MailConstant.MOLD_NEWYORK_WAR_CAMP_RANK_AWARD, AwardFrom.NEWYORK_WAR_RANK_AWARD,
                                        now, camp, y
                                );
                            } else {
                                double effect = skinNewYorkWarEffect.getEffectVal() / Constant.TEN_THROUSAND;
                                awards = effectAwards(awards, effect);
                                mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                        MailConstant.MOLD_NEWYORK_WAR_CAMP_RANK_AWARD, AwardFrom.NEWYORK_WAR_RANK_AWARD,
                                        now, camp, y, skinNewYorkWarEffect.getId()
                                );
                            }
                            // 日志记录
                            LogLordHelper.commonLog("newYorkWar", AwardFrom.NEWYORK_WAR_RANK_AWARD, p,
                                    integralRank.getValue(), y,serverSetting.getServerID(), awards);
                        });
                    });
                });
            });
        } catch (Exception e) {
            LogUtil.error("纽约争夺阵营杀敌排行奖励 发放 error ", e.getMessage());
        }
    }

    /**
     * 个人杀敌排行奖励 （纽约争霸皮肤加成）
     */
    private void personalRankAward() {
        try {
            NewYorkWar newYorkWar = crossWorldMapDataManager.getAndCheckNewYorkWar();
            // 排行
            List<NewYorkPlayerIntegralRank> ranks = newYorkWar.getPlayersIntegral().values().stream()
                    .filter(i -> i.getValue() >= WorldConstant.NEWYORK_WAR_RANK_MIN_ATTACK)
                    .sorted(Comparator.comparingLong(NewYorkPlayerIntegralRank::getValue).reversed()
                            .thenComparingInt(NewYorkPlayerIntegralRank::getSecond)
                    ).collect(Collectors.toList());
            // 发送
            int now = TimeHelper.getCurrentSecond();
            Stream.iterate(1, i -> ++i).limit(ranks.size()).forEach(y -> {
                // 排名 --> y
                NewYorkPlayerIntegralRank integralRank = ranks.get(y - 1);
                StaticNewYorkWarPersonalRank rank = StaticCrossWorldDataMgr.getStaticNewYorkWarPersonalRank(y);
                Optional.ofNullable(rank).ifPresent(r -> {
                    Player player = playerDataManager.getPlayer(integralRank.getLordId());
                    Optional.ofNullable(player).ifPresent(p -> {
                        // 发送奖励 皮肤加成
                        List<List<Integer>> awards = r.getAwardList();
                        StaticCastleSkin skinNewYorkWarEffect = getSkinNewYorkWarEffect(p);
                        if (skinNewYorkWarEffect == null) {
                            mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                    MailConstant.MOLD_NEWYORK_WAR_PERSONAL_RANK_AWARD, AwardFrom.NEWYORK_WAR_RANK_AWARD, now, y
                            );
                        } else {
                            double effect = skinNewYorkWarEffect.getEffectVal() / Constant.TEN_THROUSAND;
                            awards = effectAwards(awards, effect);
                            mailDataManager.sendAttachMail(p, PbHelper.createAwardsPb(awards),
                                    MailConstant.MOLD_NEWYORK_WAR_PERSONAL_RANK_AWARD, AwardFrom.NEWYORK_WAR_RANK_AWARD,
                                    now, y, skinNewYorkWarEffect.getId()
                            );
                        }
                        LogLordHelper.commonLog("newYorkWar", AwardFrom.NEWYORK_WAR_RANK_AWARD, p,
                                integralRank.getValue(), y,serverSetting.getServerID(), awards);
                    });
                });
            });
        } catch (Exception e) {
            LogUtil.error("纽约争夺个人杀敌排行奖励 发放 error ", e.getMessage());
        }
    }


    /**
     * 获得皮肤的纽约争霸战个人排行奖励加成（拥有即可）
     *
     * @param player
     * @return
     */
    private StaticCastleSkin getSkinNewYorkWarEffect(Player player) {
        Map<Integer, BaseDressUpEntity> castleSkinMap = dressUpDataManager.getDressUpByType(player, AwardType.CASTLE_SKIN);
        if (!CheckNull.isEmpty(castleSkinMap)) {
            castleSkinMap.values()
                    .stream()
                    .map(entity -> StaticLordDataMgr.getCastleSkinMapById(entity.getId()))
                    .filter(castleSkinCfg -> castleSkinCfg != null
                            && castleSkinCfg.getEffectType() == StaticCastleSkin.EFFECT_TYPE_NEW_YORK_WAR)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * 纽约争霸战个人排行奖励，增加20%
     */
    private List<List<Integer>> effectAwards(List<List<Integer>> awards, double v) {
        List<List<Integer>> newAwards = new ArrayList<>();
        if (awards != null) {
            awards.forEach(list ->{
                List<Integer> award = new ArrayList<>();
                award.add(list.get(0));
                award.add(list.get(1));
                award.add(list.get(2) + (int) Math.ceil(list.get(2) * v));
                newAwards.add(award);
            });
        }
        return newAwards;
    }

}
