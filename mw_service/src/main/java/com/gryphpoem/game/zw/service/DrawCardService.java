package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticDrawHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
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
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
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
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private MailDataManager mailDataManager;

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
        List<StaticDrawCardWeight> drawCardPollList = dataMgr.getPermanentDrawCardWeightList(now);
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
        StaticDrawCardWeight weightConfig = dataMgr.checkConfigEmpty(StaticDrawHeroDataMgr.RESIDENT_CARD_DRAW_POOL_ID);
        if (CheckNull.isNull(weightConfig)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId:%d, no draw hero config", roleId));
        }

        DrawCardOperation.DrawCardCount drawCardCount = DrawCardOperation.DrawCardCount.convertTo(req.getCountType());
        DrawCardOperation.DrawCardCostType drawCardCostType = DrawCardOperation.DrawCardCostType.convertTo(req.getCostType());
        if (CheckNull.isNull(drawCardCount) || CheckNull.isNull(drawCardCostType)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, costType:%d, countType:%d", roleId, req.getCostType(), req.getCountType()));
        }

        // 对应抽卡消耗类型扣除资源
        boolean activeDraw = false;
        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型
        switch (drawCardCostType) {
            case FREE:
                int totalFreeCount = drawCardData.getOtherFreeCount() + drawCardData.getFreeCount();
                if (totalFreeCount < drawCardCount.getCount()) {
                    throw new MwException(GameError.FREE_DRAW_CARD_COUNT_NOT_ENOUGH.getCode(), "免费次数不足, roleId:", player.roleId,
                            ", totalFreeCount:", totalFreeCount, ", now:", now);
                }
                // 若玩家点击十连且是免费操作, 则活动任务次数必须足够
                if (DrawCardOperation.DrawCardCount.TEN.equals(drawCardCount) && drawCardData.getOtherFreeCount() < drawCardCount.getCount()) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, costType:%d, countType:%d", roleId, req.getCostType(), req.getCountType()));
                }
                // 扣除次数
                activeDraw = drawCardData.subFreeCount(drawCardCount.getCount());
                break;
            case MONEY:
                Integer goldNum = HeroConstant.PERMANENT_QUEST_GOLD_CONSUMPTION.get(drawCardCount.getType() - 1);
                if (CheckNull.isNull(goldNum) || goldNum <= 0) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "将领寻访玉璧消耗未配置, roleId:", player.roleId,
                            ", costType:", drawCardCostType, ", drawCount:", drawCardCount.getCount());
                }
                // 若是今日首次花钱单抽, 则有折扣
                if (DrawCardOperation.DrawCardCount.ONCE.equals(drawCardCount) && !drawCardData.isTodayFirst(now)) {
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
            CommonPb.SearchHero sh = onceDraw(player, 0, drawCardCount, drawCardCostType, weightConfig, now, activeDraw);
            if (null != sh) {
                builder.addHero(sh);
            }
        }

        taskDataManager.updTask(player, TaskType.COND_997, drawCardCount.getCount());
        taskDataManager.updTask(player, TaskType.COND_1000, drawCardCount.getCount());
        builder.setCount(HeroConstant.DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO - drawCardData.getHeroDrawCount());
        builder.setCdTime((int) (drawCardData.getCdFreeTime() / 1000l));
        builder.setWishHero(PbHelper.createTwoIntPb(drawCardData.getWishHero().getA(), drawCardData.getWishHero().getB()));
        builder.setTodayDiscount(!drawCardData.isTodayFirst(now));
        builder.setOtherFreeNum(drawCardData.getOtherFreeCount());
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
    public CommonPb.SearchHero onceDraw(Player player, int costCount, DrawCardOperation.DrawCardCount drawCardCount,
                                        DrawCardOperation.DrawCardCostType drawCardCostType, StaticDrawCardWeight config, Date now, boolean activeDraw) throws MwException {
        // 记录随机到的奖励信息
        StaticHeroSearch shs = randomPriorityReward(player.roleId, player.getDrawCardData(), now, config, activeDraw);
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
                    if (staticHero.getQuality() == HeroConstant.QUALITY_ORANGE_HERO) {
                        chatDataManager.sendSysChat(ChatConst.CHAT_RECRUIT_HERO, player.lord.getCamp(), 0,
                                player.lord.getNick(), heroId);
                    }
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
     * @param config
     * @return
     * @throws MwException
     */
    private StaticHeroSearch randomPriorityReward(long roleId, DrawCardData drawCardData, Date now, StaticDrawCardWeight config, boolean activeDraw) throws MwException {
        StaticHeroSearch staticData;
        try {
            // 首次抽取必出奖励
            if (!drawCardData.isFirstFinish()) {
                drawCardData.setFirstFinish(true);
                drawCardData.addHeroDrawCount();
                drawCardData.addFragmentDrawCount();
                staticData = dataMgr.getHeroSearchMap().get(HeroConstant.FIRST_DRAW_CARD_HERO_REWARD);
                LogUtil.debug(String.format("drawCard=== player:%d, 首次抽卡：%s, 玩家抽卡信息：%s", roleId, staticData.getRewardList(), drawCardData.toDebugString()));
                return staticData;
            }
            // 当前次数到必出武将次数
            if (drawCardData.getHeroDrawCount() + 1 == HeroConstant.DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO) {
                drawCardData.setHeroDrawCount(0);
                staticData = dataMgr.randomSpecifyType(config, DrawCardRewardType.ORANGE_HERO, now);
                LogUtil.debug(String.format("drawCard===player:%d, 橙色武将保底：%s, 玩家抽卡信息：%s", roleId, staticData.getRewardList(), drawCardData.toDebugString()));
                return staticData;
            }
            // 免费活动次数保底
            if (activeDraw) {
                List<Integer> nextRewardList = drawCardData.getNextRewardList();
                if (CheckNull.nonEmpty(nextRewardList)) {
                    drawCardData.addHeroDrawCount();
                    drawCardData.addFragmentDrawCount();
                    drawCardData.getSpecifyRewardList().add(nextRewardList.get(0));
                    staticData = dataMgr.getHeroSearchMap().get(nextRewardList.get(1));
                    LogUtil.debug(String.format("drawCard===player:%d, 活动次数保底：%s, 玩家抽卡信息：%s", roleId, staticData.getRewardList(), drawCardData.toDebugString()));
                    return staticData;
                }
            }
            // 碎片保底
            if (drawCardData.getFragmentDrawCount() + 1 == HeroConstant.DRAW_ORANGE_HERO_FRAGMENT_GUARANTEED_TIMES) {
                drawCardData.setFragmentDrawCount(0);
                drawCardData.addHeroDrawCount();
                staticData = dataMgr.randomSpecifyType(config, DrawCardRewardType.ORANGE_HERO_FRAGMENT, now);
                LogUtil.debug(String.format("drawCard===player:%d, 碎片保底：%s, 玩家抽卡信息：%s", roleId, staticData.getRewardList(), drawCardData.toDebugString()));
                return staticData;
            }

            // 记录抽取次数
            drawCardData.addHeroDrawCount();
            drawCardData.addFragmentDrawCount();
            // 随机奖励
            staticData = dataMgr.randomReward(config, now);
            LogUtil.debug(String.format("drawCard===player:%d, 随机抽卡：%s, 玩家抽卡信息：%s", roleId, staticData.getRewardList(), drawCardData.toDebugString()));
            return staticData;
        } catch (Exception e) {
            throw e;
        } finally {
            // 若是活动抽卡, 则增加活动抽卡次数
            if (activeDraw)
                drawCardData.addActiveDrawsUsedCount();
            // 记录玩家抽卡次数
            drawCardData.addDrawCount(now);
        }
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
        List<StaticDrawCardWeight> configList = dataMgr.getPermanentDrawCardWeightList(now);
        if (CheckNull.isEmpty(configList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId:%d, no draw hero config", roleId));
        }
        List<Integer> heroIdPoolList = dataMgr.getWishedHeroPool(configList);
        if (CheckNull.isEmpty(heroIdPoolList) || !heroIdPoolList.contains(heroId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, not contain heroId:%d", roleId, heroId));
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
            throw new MwException(GameError.HAVE_NOT_CHOSEN_WISHED_HERO, String.format("roleId:%d, have not chosen wished hero:%s", roleId, drawCardData.getWishHero()));
        }
        if (drawCardData.getWishHero().getB() < HeroConstant.DRAW_CARD_WISH_VALUE_LIMIT) {
            throw new MwException(GameError.WISHED_HERO_VALUE_NOT_ENOUGH, String.format("roleId:%d, wished hero value not enough:%s", roleId, drawCardData.getWishHero()));
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(drawCardData.getWishHero().getA());
        if (CheckNull.isNull(staticHero)) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, wished hero no config:%s", roleId, drawCardData.getWishHero()));
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
        builder.setWishHero(PbHelper.createTwoIntPb(drawCardData.getWishHero().getA(), drawCardData.getWishHero().getB()));
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
     * 兑换武将碎片
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb5.ExchangeHeroFragmentRs exchangeHeroFragment(long roleId, GamePb5.ExchangeHeroFragmentRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (CheckNull.isEmpty(req.getConsumeItemsList())) {
            throw new MwException(GameError.PARAM_ERROR, String.format("exchange Item consume list is empty"));
        }
        List<List<Integer>> consumeList = PbHelper.convertTo(req.getConsumeItemsList());
        if (CheckNull.isEmpty(consumeList)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("exchange Item consume list is empty"));
        }

        rewardDataManager.checkPlayerResIsEnough(player, consumeList);
        List<List<Integer>> awardList = new ArrayList<>();
        Hero hero = player.heros.get(Integer.parseInt(req.getExtData()));
        if (CheckNull.isNull(hero)) {
            throw new MwException(GameError.MUST_OWN_THIS_HERO_BEFORE_REDEMPTION, String.format("roleId:%d, must own this hero before redemption, heroId:%s", req, req.getExtData()));
        }
        for (List<Integer> list : consumeList) {
            List<Integer> config = Constant.EXCHANGE_OF_QUALITY_AND_UNIVERSAL_FRAGMENT.stream().filter(configList ->
                    CheckNull.nonEmpty(configList) && configList.get(1).intValue() == list.get(1).intValue()).findFirst().orElse(null);
            if (CheckNull.isEmpty(config)) {
                continue;
            }
            if (hero.getQuality() != config.get(0)) {
                throw new MwException(GameError.PARAM_ERROR, String.format("quality mismatch, player:%d, req.consumeList:%s", roleId, Arrays.toString(list.toArray())));
            }
            awardList.add(Arrays.asList(AwardType.HERO_FRAGMENT, Integer.parseInt(req.getExtData()), list.get(2)));
        }
        if (CheckNull.isEmpty(awardList)) {
            throw new MwException(GameError.NO_CONFIG, String.format("config not found, player:%d, req.consume:%s", roleId, Arrays.toString(req.getConsumeItemsList().toArray())));
        }

        rewardDataManager.subPlayerResHasChecked(player, consumeList, true, AwardFrom.EXCHANGE_ITEM_CONSUMPTION);
        List<CommonPb.Award> awardPbList = rewardDataManager.sendReward(player, awardList, AwardFrom.EXCHANGE_ITEM_CONSUMPTION);
        GamePb5.ExchangeHeroFragmentRs.Builder builder = GamePb5.ExchangeHeroFragmentRs.newBuilder();
        if (CheckNull.nonEmpty(awardPbList))
            builder.addAllAward(awardPbList);
        return builder.build();
    }

    /**
     * 合成英雄碎片
     *
     * @param roleId
     * @param heroId
     * @return
     */
    public GamePb5.SynthesizingHeroFragmentsRs synthesizingHeroFragments(long roleId, int heroId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        if (CheckNull.isNull(staticHero) || staticHero.getChips() <= 0) {
            throw new MwException(GameError.NO_CONFIG, String.format("player:%d, send not found hero config, heroId:%d", roleId, heroId));
        }
        Hero hero = player.heros.get(heroId);
        if (Objects.nonNull(hero)) {
            throw new MwException(GameError.HERO_EXISTS, String.format("player:%d, has owned this hero:%d", roleId, heroId));
        }
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.HERO_FRAGMENT, heroId, staticHero.getChips(), AwardFrom.SYNTHETIC_HERO);
        rewardDataManager.sendRewardSignle(player, AwardType.HERO, heroId, 1, AwardFrom.SYNTHETIC_HERO);
        if (staticHero.getQuality() == HeroConstant.QUALITY_ORANGE_HERO) {
            chatDataManager.sendSysChat(ChatConst.CHAT_RECRUIT_HERO, player.lord.getCamp(), 0,
                    player.lord.getNick(), heroId);
        }
        GamePb5.SynthesizingHeroFragmentsRs.Builder builder = GamePb5.SynthesizingHeroFragmentsRs.newBuilder();
        builder.setHero(PbHelper.createHeroPb(player.heros.get(heroId), player));
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

    /** 只可获取数组中一次英雄奖励*/
    private AwardFrom[] GOT_ONCE_HERO_AWARD = new AwardFrom[]{AwardFrom.ALICE_AWARD, AwardFrom.SIGN_IN_REWARD};

    /**
     * 校验武将是否可以再获取
     *
     * @param player
     * @param hero
     * @param from
     * @return
     */
    public boolean checkHero(Player player, Hero hero, AwardFrom from) {
        if (CheckNull.isNull(from) || CheckNull.isNull(hero))
            return true;
        if (ArrayUtils.contains(GOT_ONCE_HERO_AWARD, from)) {
            if (!hero.isShowClient()) {
                // 若已获得此英雄, 从活动或任务再次活动此英雄, 则不发送
                LogUtil.error(String.format("has got this hero from others reward, roleId:%d, heroId:%d", player.roleId, hero.getHeroId()));
                return false;
            }
        }
        return true;
    }

    /**
     * 处理不同来源相同武将问题以及处理关平(救援奖励)
     *
     * @param player
     * @param hero
     * @param from
     */
    public void handleRepeatedHeroAndRescueAward(Player player, Hero hero, AwardFrom from) {
        if (CheckNull.isNull(from) || CheckNull.isNull(hero) || !hero.isShowClient())
            return;
        if (ArrayUtils.contains(GOT_ONCE_HERO_AWARD, from)) {
            // 获取这个特殊将领就发送邮件
            if (Constant.ALICE_RESCUE_MISSION_TASK.get(1) == hero.getHeroId()) {
                List<CommonPb.Award> awardsPb = PbHelper.createAwardsPb(Constant.ALICE_RESCUE_MISSION_MAIL_AWARD);
                // 发送救援奖励邮件
                mailDataManager.sendAttachMail(player, awardsPb, MailConstant.MOLD_ALICE_AWARD, AwardFrom.ALICE_AWARD, TimeHelper.getCurrentSecond());
            }

            hero.setShowClient(false);
            // 同步英雄获取变更
            GamePb5.SyncHeroShowStatusRs.Builder builder = GamePb5.SyncHeroShowStatusRs.newBuilder();
            builder.setHero(PbHelper.createHeroPb(hero, player));
            BasePb.Base base = PbHelper.createSynBase(GamePb5.SyncHeroShowStatusRs.EXT_FIELD_NUMBER, GamePb5.SyncHeroShowStatusRs.ext, builder.build()).build();
            DataResource.ac.getBean(PlayerService.class).syncMsgToPlayer(base, player);
        }
    }

    @GmCmd("drawCard")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String cmd = params[0];
        if ("testPro".equalsIgnoreCase(cmd)) {
            Integer count = Integer.parseInt(params[1]);
            if (count <= 0)
                return;
            Date now = new Date();
            StaticDrawCardWeight config = dataMgr.checkConfigEmpty(StaticDrawHeroDataMgr.RESIDENT_CARD_DRAW_POOL_ID);
            if (CheckNull.isNull(config))
                return;
            LogUtil.getLogThread().addCommand(() -> {
                Map<Integer, Integer> awardCount = new HashMap<>();
                Map<Integer, Integer> countMap = new HashMap<>();
                for (int i = 0; i < count; i++) {
                    StaticHeroSearch staticHeroSearch = dataMgr.randomReward(config, now);
                    countMap.merge(staticHeroSearch.getRewardType(), 1, Integer::sum);
                    awardCount.merge(staticHeroSearch.getAutoId(), 1, Integer::sum);
                }
                LogUtil.common(String.format("test drawCard random ----------- player:%d, has random countMap:%s, awardCount:%s", player.roleId, countMap, awardCount));
            });
        }
        if ("addActiveCount".equalsIgnoreCase(cmd)) {
            player.getDrawCardData().setOtherFreeCount(Integer.parseInt(params[1]));
        }
        if ("addFragment".equalsIgnoreCase(cmd)) {
            rewardDataManager.sendRewardSignle(player, AwardType.HERO_FRAGMENT, Integer.parseInt(params[1]), Integer.parseInt(params[2]), AwardFrom.DO_SOME);
        }
        if ("clearFragment".equalsIgnoreCase(cmd)) {
            if (CheckNull.nonEmpty(player.getDrawCardData().getFragmentData())) {
                for (Map.Entry<Integer, Integer> entry : player.getDrawCardData().getFragmentData().entrySet()) {
                    rewardDataManager.subAndSyncProp(player, AwardType.HERO_FRAGMENT, entry.getKey(), entry.getValue(), AwardFrom.DO_SOME);
                }
            }
        }
        if ("clearDrawData".equalsIgnoreCase(cmd)) {
            String fileGm = params[1];
            Field field = player.getDrawCardData().getClass().getDeclaredField(fileGm);
            field.setAccessible(true);
            field.set(player.getDrawCardData(), 0);
        }
    }
}
