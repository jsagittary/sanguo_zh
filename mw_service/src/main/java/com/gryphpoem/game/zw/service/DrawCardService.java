package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticDrawHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawCardWeight;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSearch;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.tavern.DrawCardData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-11 18:57
 */
@Component
public class DrawCardService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private StaticDrawHeroDataMgr dataMgr;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private HeroService heroService;

    /**
     * 获取抽卡详情
     *
     * @param roleId
     * @return
     */
    public GamePb5.GetDrawHeroCardRs getDrawHeroCard(long roleId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb5.GetDrawHeroCardRs.Builder builder = GamePb5.GetDrawHeroCardRs.newBuilder().setOpen(checkFunctionOpen(player, false));
        if (!builder.getOpen()) {
            return builder.build();
        }

        player.getDrawCardData().refreshData();

        Date now = new Date();
        builder.setCdTime((int) (player.getDrawCardData().getCdFreeTime() / 1000l));
        if (Objects.nonNull(player.getDrawCardData().getWishHero()))
            builder.setWishHero(PbHelper.createTwoIntPb(player.getDrawCardData().getWishHero().getA(), player.getDrawCardData().getWishHero().getB()));
        builder.setNextGetHeroTimes(HeroConstant.DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO - player.getDrawCardData().getHeroDrawCount());
        builder.setFreeNum(player.getDrawCardData().getFreeCount());
        builder.setOtherFreeNum(player.getDrawCardData().getOtherFreeCount());
        List<StaticDrawCardWeight> drawCardPollList = dataMgr.getDrawCardWeightList(now);
        if (CheckNull.nonEmpty(drawCardPollList))
            builder.addAllSearchTypeIds(drawCardPollList.stream().map(StaticDrawCardWeight::getId).collect(Collectors.toSet()));
        builder.setTodayDiscount(!player.getDrawCardData().isTodayFirst(now));
        return builder.build();
    }

    /**
     * 抽卡
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb5.DrawHeroCardRs drawHeroCard(long roleId, GamePb5.DrawHeroCardRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        DrawCardData drawCardData = player.getDrawCardData();
        checkFunctionOpen(player, true);
        // 刷新免费次数
        drawCardData.refreshData();

        Date now = new Date();
        List<StaticDrawCardWeight> configList = dataMgr.getDrawCardWeightList(now);
        if (CheckNull.isEmpty(configList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId:%d, no draw hero config", roleId));
        }

        DrawCardOperation.DrawCardCount drawCardCount = DrawCardOperation.DrawCardCount.convertTo(req.getCountType());
        DrawCardOperation.DrawCardCostType drawCardCostType = DrawCardOperation.DrawCardCostType.convertTo(req.getCostType());
        if (CheckNull.isNull(drawCardCount) || CheckNull.isNull(drawCardCostType)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, costType:%d, countType:%d", roleId, req.getCostType(), req.getCountType()));
        }

        // 对应抽卡消耗类型扣除资源
        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型
        switch (drawCardCostType) {
            case FREE:
                int totalFreeCount = drawCardData.getOtherFreeCount() + drawCardData.getFreeCount();
                if (totalFreeCount < drawCardCount.getCount()) {
                    throw new MwException(GameError.FREE_DRAW_CARD_COUNT_NOT_ENOUGH.getCode(), "免费次数不足, roleId:", player.roleId,
                            ", totalFreeCount:", totalFreeCount, ", now:", now);
                }
                // 扣除次数
                drawCardData.subFreeCount(drawCardCount.getCount());
                break;
            case MONEY:
                Integer goldNum = HeroConstant.PERMANENT_QUEST_GOLD_CONSUMPTION.get(drawCardCount.getType() - 1);
                if (CheckNull.isNull(goldNum) || goldNum <= 0) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "将领寻访玉璧消耗未配置, roleId:", player.roleId,
                            ", costType:", drawCardCostType, ", drawCount:", drawCardCount.getCount());
                }
                // 若是今日首次花钱单抽, 则有折扣
                if (DrawCardOperation.DrawCardCount.ONCE.equals(drawCardCostType) && !drawCardData.isTodayFirst(now)) {
                    goldNum = HeroConstant.DAILY_DRAW_SINGLE_DRAW_DISCOUNT_TO_CONSUME_JADE;
                }

                rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, goldNum, "draw permanent card");
                rewardDataManager.subGold(player, goldNum, AwardFrom.HERO_SUPER_SEARCH);
                drawCardData.setFirstCostMoneyDailyDate(now);
                change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
                break;
        }

        // 通知玩家消耗的资源类型
        rewardDataManager.syncRoleResChanged(player, change);

        GamePb5.DrawHeroCardRs.Builder builder = GamePb5.DrawHeroCardRs.newBuilder();
        // 执行将领寻访逻辑
        for (int i = 0; i < drawCardCount.getCount(); i++) {
            CommonPb.SearchHero sh = onceDraw(player, 0, drawCardCount, drawCardCostType, configList, now);
            if (null != sh) {
                builder.addHero(sh);
            }
        }

        builder.setCount(HeroConstant.DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO - drawCardData.getHeroDrawCount());
        builder.setCdTime((int) (drawCardData.getCdFreeTime() / 1000l));
        builder.setWishHero(PbHelper.createTwoIntPb(drawCardData.getWishHero().getA(), drawCardData.getWishHero().getB()));
        builder.setTodayDiscount(!drawCardData.isTodayFirst(now));
        return builder.build();
    }

    /**
     * 玩家一次抽卡
     *
     * @param player
     * @param costCount
     * @return
     * @throws MwException
     */
    private CommonPb.SearchHero onceDraw(Player player, int costCount, DrawCardOperation.DrawCardCount drawCardCount,
                                         DrawCardOperation.DrawCardCostType drawCardCostType, List<StaticDrawCardWeight> configList, Date now) throws MwException {
        // 记录随机到的奖励信息
        StaticHeroSearch shs = randomPriorityReward(player.getDrawCardData(), now, configList);
        if (CheckNull.isNull(shs) || CheckNull.isEmpty(shs.getRewardList())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "寻访配置错误   drawCardCount:", drawCardCount, ", drawCardCostType:", drawCardCostType);
        }

        DrawCardRewardType rewardType = DrawCardRewardType.convertTo(shs.getRewardType());
        if (CheckNull.isNull(rewardType)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "寻访配置错误   drawCardCount:", drawCardCount, ", drawCardCostType:", drawCardCostType, ", reward:", shs);
        }

        CommonPb.SearchHero.Builder builder = CommonPb.SearchHero.newBuilder().setSearchId(shs.getAutoId());
        switch (rewardType) {
            case ORANGE_HERO:
            case PURPLE_HERO:
                int heroId = shs.getRewardList().get(0).get(1);
                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                if (null == staticHero) {
                    LogUtil.error("将领寻访，寻访到的将领未找到配置信息, heroId:", heroId);
                    return null;
                }

                // 查找玩家是否拥有此英雄
                Hero hero_ = heroService.hasOwnedHero(player, heroId, staticHero);
                if (Objects.nonNull(hero_)) {
                    // 拥有英雄转为碎片
                    rewardDataManager.sendRewardSignle(player, AwardType.HERO_FRAGMENT, heroId,
                            HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS, AwardFrom.HERO_NORMAL_SEARCH);
                } else {
                    rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_NORMAL_SEARCH);
                    // 返回新得到的将领信息
                    Hero hero = player.heros.get(staticHero.getHeroId());
                    builder.setHero(PbHelper.createHeroPb(hero, player));
                }
                break;
            case PROP_REWARD:
            case ORANGE_HERO_FRAGMENT:
            case PURPLE_HERO_FRAGMENT:
                rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_NORMAL_SEARCH);
                break;
        }

        return builder.build();
    }

    /**
     * 按优先级随机或指定奖励, 优先级(由高到低)：首次必出 -> 武将保底 -> 免费活动次数保底 -> 碎片保底
     *
     * @param drawCardData
     * @param configList
     * @return
     * @throws MwException
     */
    private StaticHeroSearch randomPriorityReward(DrawCardData drawCardData, Date now, List<StaticDrawCardWeight> configList) throws MwException {
        // 首次抽取必出奖励
        if (drawCardData.isFirstDraw()) {
            drawCardData.setFirstDraw(false);
            drawCardData.addHeroDrawCount();
            drawCardData.addFragmentDrawCount();
            drawCardData.addDrawCount(now);
            return dataMgr.getHeroSearchMap().get(HeroConstant.FIRST_DRAW_CARD_HERO_REWARD);
        }
        // 当前次数到必出武将次数
        if (drawCardData.getHeroDrawCount() + 1 == HeroConstant.DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO) {
            drawCardData.setHeroDrawCount(0);
            return dataMgr.randomSpecifyType(configList, DrawCardRewardType.ORANGE_HERO);
        }
        // 免费活动次数保底
        if (CheckNull.nonEmpty(drawCardData.getSpecifyRewardList())) {
            Integer specifyRewardId = drawCardData.getSpecifyRewardList().removeFirst();
            if (Objects.nonNull(specifyRewardId)) {
                return dataMgr.getHeroSearchMap().get(specifyRewardId);
            }
        }
        // 碎片保底
        if (drawCardData.getFragmentDrawCount() + 1 == HeroConstant.DRAW_ORANGE_HERO_FRAGMENT_GUARANTEED_TIMES) {
            drawCardData.setFragmentDrawCount(0);
            return dataMgr.randomSpecifyType(configList, DrawCardRewardType.ORANGE_HERO_FRAGMENT);
        }

        // 记录抽取次数
        drawCardData.addHeroDrawCount();
        drawCardData.addFragmentDrawCount();
        drawCardData.addDrawCount(now);
        // 随机奖励
        return dataMgr.randomReward(configList);
    }

    /**
     * 选择心愿武将
     *
     * @param roleId
     * @param heroId
     * @return
     */
    public GamePb5.ChooseNewWishHeroRs chooseNewWishHero(long roleId, int heroId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionOpen(player, true);

        DrawCardData drawCardData = player.getDrawCardData();
        Date now = new Date();
        List<StaticDrawCardWeight> configList = dataMgr.getDrawCardWeightList(now);
        if (CheckNull.isEmpty(configList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId:%d, no draw hero config", roleId));
        }
        List<Integer> heroIdPoolList = dataMgr.getWishedHeroPool(configList);
        if (CheckNull.isEmpty(heroIdPoolList) || !heroIdPoolList.contains(heroId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, has chosen heroId:%d", roleId));
        }

        drawCardData.getWishHero().setA(heroId);
        GamePb5.ChooseNewWishHeroRs.Builder builder = GamePb5.ChooseNewWishHeroRs.newBuilder();
        builder.setHeroId(heroId);
        return builder.build();
    }

    /**
     * 领取心愿英雄
     *
     * @param roleId
     * @return
     */
    public GamePb5.ReceiveNewWishHeoRs receiveNewWishHeo(long roleId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionOpen(player, true);

        DrawCardData drawCardData = player.getDrawCardData();
        if (drawCardData.getWishHero().getA() == 0) {
            throw new MwException(GameError.HAVE_NOT_CHOSEN_WISHED_HERO, String.format("roleId:%d, have not chosen wished hero:%d", roleId, drawCardData.getWishHero()));
        }
        if (drawCardData.getWishHero().getB() < HeroConstant.DRAW_CARD_WISH_VALUE_LIMIT) {
            throw new MwException(GameError.WISHED_HERO_VALUE_NOT_ENOUGH, String.format("roleId:%d, wished hero value not enough:%d", roleId, drawCardData.getWishHero()));
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(drawCardData.getWishHero().getA());
        if (CheckNull.isNull(staticHero)) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, wished hero no config:%d", roleId, drawCardData.getWishHero()));
        }

        Hero hero_ = heroService.hasOwnedHero(player, drawCardData.getWishHero().getA(), staticHero);
        rewardDataManager.sendRewardSignle(player, AwardType.HERO, drawCardData.getWishHero().getA(), 1, AwardFrom.RECEIVE_WISH_HERO_REWARD);
        drawCardData.getWishHero().setA(0);
        drawCardData.getWishHero().setB(0);

        GamePb5.ReceiveNewWishHeoRs.Builder builder = GamePb5.ReceiveNewWishHeoRs.newBuilder();
        if (Objects.nonNull(hero_)) {
            builder.setAward(PbHelper.createAwardPb(AwardType.HERO_FRAGMENT, staticHero.getHeroId(), HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS));
        } else {
            Hero hero = player.heros.get(staticHero.getHeroId());
            builder.setHero(PbHelper.createHeroPb(hero, player));
        }

        return builder.build();
    }

    /**
     * 获取所有武将碎片
     *
     * @param roleId
     * @return
     */
    public GamePb5.GetAllHeroFragmentRs getAllHeroFragment(long roleId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Map<Integer, Integer> fragmentData = player.getDrawCardData().getFragmentData();

        GamePb5.GetAllHeroFragmentRs.Builder builder = GamePb5.GetAllHeroFragmentRs.newBuilder();
        fragmentData.entrySet().forEach(entry -> builder.addAllFragments(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
        return builder.build();
    }

    /**
     * 校验功能开启
     *
     * @param player
     * @throws MwException
     */
    private boolean checkFunctionOpen(Player player, boolean throwError) throws MwException {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_PERMANENT_DRAW_CARD)) {
            if (throwError)
                throw new MwException(GameError.FUNCTION_LOCK, String.format("func permanent draw card locked, roleId:%d", player.lord.getLordId()));
            else
                return false;
        }

        return true;
    }

    @GmCmd("drawCard")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {

    }
}
