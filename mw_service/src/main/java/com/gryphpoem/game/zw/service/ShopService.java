package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.Prop;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.ShopId;
import com.gryphpoem.game.zw.resource.constant.Constant.ShopType;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.AbsGiftBagActivityService;
import com.gryphpoem.game.zw.service.activity.ActivityHelpService;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

/**
 * 商店
 *
 * @author tyler
 */
@Service
public class ShopService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private ActivityHelpService activityHelpService;
    @Autowired
    private BerlinWarService berlinWarService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private DressUpService dressUpService;
    @Autowired
    private DressUpDataManager dressUpDataManager;

    /**
     * 获取商店数据
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetShopRs getShop(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Shop shop = player.shop;
        if (shop == null) {
            shop = new Shop();
            player.shop = shop;
        }
        refreshShop(player);
        GetShopRs.Builder builder = GetShopRs.newBuilder();
        if (shop != null) {
            if (shop.getIdCnt() != null) {
                for (Entry<Integer, Integer> kv : shop.getIdCnt().entrySet()) {
                    builder.addIdCnt(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
                }
            }
            if (shop.getVipId() != null) {
                builder.addAllVipId(shop.getVipId());
            }

            if (shop.getOffId() != null) {
                for (Entry<Integer, Integer> kv : shop.getOffId().entrySet()) {
                    builder.addOffId(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
                }
            }

            if (shop.getFreeCnt() != null) {
                for (Entry<Integer, Integer> kv : shop.getFreeCnt().entrySet()) {
                    builder.addFreeCnt(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
                }
            }
        }

        return builder.build();
    }

    /**
     * 商品购买
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public ShopBuyRs shopBuy(Long roleId, int id, boolean useItem, int num) throws MwException {
        if (num <= 0 || num > 100) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "商品购买时，数量错误, roleId:" + roleId + ",id=" + id);
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Shop shop = player.shop;
        if (shop == null) {
            shop = new Shop();
            player.shop = shop;
        }
        StaticShop staticShop = StaticShopDataMgr.getShopMap(id);
        if (staticShop == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "商品购买时，找不到配置, roleId:" + roleId + ",id=" + id);
        }
        if (id == ShopId.shop_id_7 && player.building.getChemical() < 1) {// 有化工厂才能买人口
            throw new MwException(GameError.BUILDING_NOT_CREATE.getCode(), "roleId:", roleId, "化工厂还未建造");
        }
        refreshShop(player);
        // 等级判断
        // int lordLv = player.lord.getLevel();
        // if (lordLv < staticShop.getNeedRoleLv()) {
        // throw new MwException(GameError.LV_NOT_ENOUGH.getCode(),
        // "商品购买时，玩家等级不够, roleId:" + roleId + ",lordLv=" + lordLv);
        // }

        // 道具代金券 只能买1个
        Integer vipCnt = shop.getIdCnt().get(id);
        Prop leaveorerProp = null;
        if (useItem) {
            if (staticShop.getTradeItemId() <= 0) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "商品购买时，找不到配置, roleId:" + roleId + ",id=" + id);
            }
            rewardDataManager.checkPropIsEnough(player, staticShop.getTradeItemId(), num, "特价商店购买");
            rewardDataManager.subProp(player, staticShop.getTradeItemId(), num, AwardFrom.SHOP_BUY, id);
            // 余下代金券
            com.gryphpoem.game.zw.resource.pojo.Prop prop = player.props.get(staticShop.getTradeItemId());
            if (prop != null) {
                leaveorerProp = PbHelper.createPropPb(prop);
            }
        } else {
            int price = 0;
            if (staticShop.getType() == ShopType.shop_type_1) {
                int tempNum = num;

                if (!CheckNull.isEmpty(staticShop.getVipFreeCnt())
                        && staticShop.getVipFreeCnt().containsKey(player.lord.getVip())) {// 免费购买次数购买
                    Integer myFreeCnt = shop.getFreeCnt().get(id);
                    myFreeCnt = myFreeCnt == null ? 0 : myFreeCnt;
                    Integer cnt = staticShop.getVipFreeCnt().get(player.lord.getVip());
                    cnt = cnt == null ? 0 : cnt;
                    int buyCnt = cnt - myFreeCnt; // 剩余可购买次数
                    buyCnt = buyCnt <= 0 ? 0 : buyCnt;
                    if (buyCnt - num > 0) {
                        shop.getFreeCnt().put(id, myFreeCnt + num);
                        tempNum = tempNum - num;
                    } else {
                        shop.getFreeCnt().put(id, myFreeCnt + buyCnt);
                        tempNum = tempNum - buyCnt;
                    }
                }

                if (tempNum > 0) {
                    if (staticShop.getVipCnt() != null && staticShop.getVipCnt().size() > 0
                            && staticShop.getVipCnt().containsKey(player.lord.getVip())) { // 当有配置了 VIP价格
                        vipCnt = vipCnt == null ? 0 : vipCnt;
                        for (int i = 0; i < tempNum; i++) {
                            if (staticShop.getVipCnt().get(player.lord.getVip()) > vipCnt) {
                                price += staticShop.getVipPrice();
                                vipCnt++;
                            } else {
                                price += staticShop.getPrice();
                            }
                        }
                    } else {// 没有配置vip价格时,走原价
                        for (int i = 0; i < tempNum; i++) {
                            price += staticShop.getPrice();
                        }
                    }
                    // 扣钱
                    if (price <= 0) {
                        throw new MwException(GameError.NO_CONFIG.getCode(),
                                "商品购买时，价格配置错误, roleId:" + roleId + ",id=" + id);
                    }
                    rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, price, "商店购买");
                    rewardDataManager.subGold(player, price, AwardFrom.SHOP_BUY, id);
                }

            } else {
                // 特价处理
                for (int i = 0; i < num; i++) {
                    // 是否特价
                    if (shop.getOffId().containsKey(staticShop.getId())) {
                        price += (int) (staticShop.getPrice() * shop.getOffId().get(staticShop.getId())
                                / Constant.HUNDRED);
                    } else {
                        price += staticShop.getPrice();
                    }
                }
                // 扣钱
                if (price <= 0) {
                    throw new MwException(GameError.NO_CONFIG.getCode(),
                            "商品购买时，价格配置错误, roleId:" + roleId + ",id=" + id);
                }
                rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, price, "商店购买");
                rewardDataManager.subGold(player, price, AwardFrom.SHOP_BUY, id);
            }

        }

        ShopBuyRs.Builder builder = ShopBuyRs.newBuilder();
        // 发送奖励
        if (id == ShopId.shop_id_1) {// 李潮 2017年12月2日 商店购买去掉同步
            List<Integer> minVal = Constant.RES_BUY_BASE_VAL;
            ResourceMult resourceMult = buildingDataManager.getResourceMult(player);
            builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.RESOURCE, id,
                    Math.max(resourceMult.getOil(), minVal.get(0)) * num, AwardFrom.SHOP_BUY, id));
        } else if (id == ShopId.shop_id_2) {
            List<Integer> minVal = Constant.RES_BUY_BASE_VAL;
            ResourceMult resourceMult = buildingDataManager.getResourceMult(player);
            builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.RESOURCE, id,
                    Math.max(resourceMult.getElec(), minVal.get(1)) * num, AwardFrom.SHOP_BUY, id));
        } else if (id == ShopId.shop_id_3) {
            List<Integer> minVal = Constant.RES_BUY_BASE_VAL;
            ResourceMult resourceMult = buildingDataManager.getResourceMult(player);
            builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.RESOURCE, id,
                    Math.max(resourceMult.getFood(), minVal.get(2)) * num, AwardFrom.SHOP_BUY, id));
        } else if (id == ShopId.shop_id_4) {
            List<Integer> minVal = Constant.RES_BUY_BASE_VAL;
            ResourceMult resourceMult = buildingDataManager.getResourceMult(player);
            builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.RESOURCE, id,
                    Math.max(resourceMult.getOre(), minVal.get(3)) * num, AwardFrom.SHOP_BUY, id));
        } else if (id == ShopId.shop_id_5) { // 自动建造
            buildingDataManager.resetAutoBuildCnt(player);// 自动建造重置
            buildingDataManager.syncAutoBuildInfo(player);// 同步自动建造
        } else if (id == ShopId.shop_id_7) {// 人口购买
            StaticBuildingLv buildingLv = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingType.COMMAND,
                    player.building.getCommand());
            int maxHuman = buildingLv != null && buildingLv.getCapacity() != null && buildingLv.getCapacity().size() > 0
                    ? buildingLv.getCapacity().get(0).get(1) : 0;
            int human = (int) (maxHuman * 0.1f);
            builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.RESOURCE, AwardType.Resource.HUMAN,
                    human * num, AwardFrom.SHOP_BUY, id));
        } else {
            builder.addAward(
                    rewardDataManager.addAwardSignle(player, staticShop.getAward(), num, AwardFrom.SHOP_BUY, id));
            // 先去掉 加上同步
            // ChangeInfo change = ChangeInfo.newIns();
            // change.addChangeType(staticShop.getAward().get(0), staticShop.getAward().get(1));
            // rewardDataManager.syncRoleResChanged(player, change);
        }

        builder.setGold(player.lord.getGold());
        // 记录VIP购买次数
        if (vipCnt != null && vipCnt > 0) {
            shop.getIdCnt().put(id, vipCnt);
        }
        if (shop.getIdCnt().containsKey(id)) {
            builder.setIdCnt(TwoInt.newBuilder().setV1(id).setV2(shop.getIdCnt().get(id)));
        }
        // vip免费购买次数
        if (shop.getFreeCnt().containsKey(id)) {
            builder.setFreeCnt(TwoInt.newBuilder().setV1(id).setV2(shop.getFreeCnt().get(id)));
        }
        // 余下代金券
        if (leaveorerProp != null) {
            builder.setLeaveorer(leaveorerProp);
        }
        return builder.build();
    }

    /**
     * 刷新每日特价
     */
    void refreshShop(Player player) {
        Shop shop = player.shop;
        int nowDay = TimeHelper.getCurrentDay();
        int lastDay = TimeHelper.getDay(shop.getRefreshTime());
        LogUtil.debug("roleId:", player.roleId, ", 商店折扣刷新时间     now:" + nowDay + ", lastDay:" + lastDay);
        if (nowDay != lastDay) {
            LogUtil.debug("roleId:", player.roleId, ", ======折扣刷新start=====");
            shop.setRefreshTime(TimeHelper.getCurrentSecond());
            shop.getIdCnt().clear();
            shop.getFreeCnt().clear();// 清除每日 免费次数
            // 刷新每日折扣
            shop.getOffId().clear();
            List<StaticShop> list = StaticShopDataMgr.getTypeShopMap(Constant.ShopType.shop_type_2);
            if (list != null && list.size() > 0) {
                list.addAll(StaticShopDataMgr.getTypeShopMap(Constant.ShopType.shop_type_3));
            }
            processRandom(shop, player.lord.getLevel(), list, Constant.SHOP_OFF_RANDOM_NUM);
            LogUtil.debug("roleId:", player.roleId, ",  ======折扣刷新end===== 折扣商品:" + shop.getOffId());
        }
    }

    private void processRandom(Shop shop, int roleLv, List<StaticShop> list, int num) {
        Map<Integer, StaticShop> aMap = new HashMap<>();
        for (StaticShop staticTreasure : list) {
            if (staticTreasure.getNeedRoleLv() > roleLv) {
                continue;
            }
            aMap.put(staticTreasure.getId(), staticTreasure);
        }

        int cnt = 0;
        int totalCnt = 0;
        while (true) {
            int key = 0;
            StaticShop s = (StaticShop) aMap.values().toArray()[RandomUtils.nextInt(0, aMap.size())];
            shop.getOffId().put(s.getId(), Constant.SHOP_OFF);
            key = s.getId();
            cnt++;
            if (cnt >= num) {
                return;
            }
            aMap.remove(key);
            totalCnt++;
            if (totalCnt > 80) {
                break;
            }
        }
    }

    public VipBuyRs vipBuy(Long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Shop shop = player.shop;
        if (shop == null) {
            shop = new Shop();
            player.shop = shop;
        }
        VipBuyRs.Builder builder = VipBuyRs.newBuilder();
        StaticVip staticVip = StaticVipDataMgr.getVipMap(id);
        if (staticVip == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "购买VIP礼包时，找不到配置, roleId:" + roleId + ",id=" + id);
        }
        // VIP礼包只允许买一次
        if (shop.getVipId().contains(id)) {
            throw new MwException(GameError.SHOP_VIP_HAS_BUY.getCode(),
                    "购买VIP礼包时，已购买礼包, roleId:" + roleId + ",id=" + id);
        }
        // VIP等级未达到
        if (staticVip.getVipLv() > player.lord.getVip()) {
            throw new MwException(GameError.VIP_NOT_ENOUGH.getCode(), "购买VIP礼包时，VIP等级未达到, roleId:" + roleId + ",id=" + id);
        }
        // 背包检测
        rewardDataManager.checkBag(player, staticVip.getReward());
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                staticVip.getShowPrice(), AwardFrom.VIP_GIFT_BUY, id);
        // rewardDataManager.checkMoneyIsEnough(player.lord, AwardType.Money.GOLD, staticVip.getPrice(), "购买VIP礼");
        // rewardDataManager.subMoney(player, AwardType.Money.GOLD, staticVip.getPrice(), AwardFrom.SHOP_BUY, "购买VIP礼");
        shop.getVipId().add(id);
        // 发送奖励
        builder.addAllAward(
                rewardDataManager.addAwardDelaySync(player, staticVip.getReward(), null, AwardFrom.VIP_GIFT_BUY, id));

        int chatId = 0;
        // 购买VIP6-VIP13礼包 招募紫色坦克将领-招募橙色火箭将领 发送世界消息
        if ((staticVip.getVipLv() == VipConstant.VIP_FIVE || staticVip.getVipLv() == VipConstant.VIP_EIGHT)
                && !CheckNull.isEmpty(staticVip.getReward())) {
            chatId = staticVip.getVipLv() == VipConstant.VIP_FIVE ? ChatConst.CHAT_BUY_VIP6 : ChatConst.CHAT_BUY_VIP8;
            if (chatId > 0) {
                for (List<Integer> list : staticVip.getReward()) {
                    if (CheckNull.isEmpty(list) || list.size() < 3) {
                        LogUtil.error("发送奖励，奖励列表的格式不正确，跳过, list:" + list);
                        continue;
                    }
                    int type = list.get(0);
                    int heroId = list.get(1);
                    if (type == AwardType.HERO) {
                        chatDataManager.sendSysChat(chatId, player.lord.getCamp(), 0, player.lord.getCamp(),
                                player.lord.getNick(), heroId);
                        break;
                    }
                }
            }
        } else if (staticVip.getVipLv() > VipConstant.VIP_SIX && !CheckNull.isEmpty(staticVip.getReward())) {
            chatDataManager.sendSysChat(ChatConst.CHAT_VIP_BUY, player.lord.getCamp(), 0, player.lord.getCamp(),
                    player.lord.getNick(), staticVip.getVipLv());
        }

        builder.setGold(player.lord.getGold());
        builder.setVipId(id);
        return builder.build();
    }

    /**
     * 打折礼包购买(金币 军备促销 , 礼包特惠)
     */
    public PromotionPropBuyRs promotionGiftBuy(PromotionPropBuyRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int keyId = req.getId();

        StaticPromotion promotion = StaticActivityDataMgr.getPromotionById(keyId);
        if (CheckNull.isNull(promotion)) {
            throw new MwException(GameError.PROMOTION_CONF_NOT_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:",
                    player.roleId, ", promotionId:", keyId);
        }
        int actType = promotion.getType();

        if (actType == ActivityConst.ACT_PROP_PROMOTION) {
            return processJunbei(player, promotion);// 军备促销
        } else if (actType == ActivityConst.ACT_GIFT_PROMOTION) {
            return processGiftPromotion(player, promotion);// 礼包特惠
        } else if (actType == ActivityConst.ACT_MERGE_PROP_PROMOTION) {
            return processMergePromotion(player, promotion);//合服特卖
        } else if (AbsGiftBagActivityService.isActGiftBagAct(actType)) {
            return AbsGiftBagActivityService.buyActGiftBagByGameMoney(player, promotion);
        }
        throw new MwException(GameError.NO_CONFIG.getCode(), " 打折礼包购买配置不正确 roleId:", player.roleId, " keyId:", keyId);

    }

    /**
     * 处理合服特卖
     *
     * @param player
     * @param promotion
     * @throws MwException
     */
    private PromotionPropBuyRs processMergePromotion(Player player, StaticPromotion promotion) throws MwException {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_MERGE_PROP_PROMOTION);
        if (Objects.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 合服特卖,活动未开启 roleId:", player.roleId,
                    " keyId:", promotion.getPromotionId());
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_MERGE_PROP_PROMOTION);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 合服特卖,活动未开启 roleId:", player.roleId,
                    " keyId:", promotion.getPromotionId());
        }
        int keyId = promotion.getPromotionId();
        int count = activity.getStatusMap().getOrDefault(keyId, 0);
        if (promotion.getCount() > 0 && count >= promotion.getCount()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("合服特卖, roleId :%d, keyId :%d, 已达购买次数上限 [%d / %d] ",
                    player.getLordId(), keyId, count, promotion.getCount()));
        }
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD,
                promotion.getPrice(), AwardFrom.ACT_MERGE_PROMOTION, true, keyId);
        //记录购买次数
        activity.getStatusMap().merge(keyId, 1, Integer::sum);
        //记录行为日志
        LogLordHelper.gift(AwardFrom.ACT_MERGE_PROMOTION, player.account, player.lord, promotion.getPrice(),
                promotion.getPromotionId(), activity.getActivityType());

        PromotionPropBuyRs.Builder builder = PromotionPropBuyRs.newBuilder();
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, promotion.getList(), null,
                AwardFrom.GIFT_PROMOTION_BUY, keyId));
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 礼包特惠
     *
     * @param player
     * @param promotion
     * @return
     * @throws MwException
     */
    private PromotionPropBuyRs processGiftPromotion(Player player, StaticPromotion promotion) throws MwException {
        int keyId = promotion.getPromotionId();
        long roleId = player.roleId;
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_GIFT_PROMOTION);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 礼包特惠,活动未开启 roleId:", player.roleId,
                    " keyId:", keyId);
        }
        Date[] giftPromotionDateArr = activityHelpService.getGiftPromotionDate();
        if (giftPromotionDateArr == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "礼包特惠 ,活动未开启 roleId:", player.roleId,
                    " keyId:", keyId);
        }

        int schedule = activity.getStatusCnt().get(keyId) == null ? 0
                : new Long(activity.getStatusCnt().get(keyId)).intValue();// 当前礼包的购买次数
        if (promotion.getCount() > 0 && schedule >= promotion.getCount()) {
            throw new MwException(GameError.PROMOTION_BUY_MAX.getCode(), "礼包特惠 打折礼包购买次数已达上限, roleId:", roleId, ", cnt:",
                    schedule);
        }
        rewardDataManager.checkBag(player, promotion.getList());
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                promotion.getPrice(), AwardFrom.GIFT_PROMOTION_BUY, keyId);// 购买打折礼包
        activity.getStatusCnt().put(keyId, Long.valueOf(schedule + 1));// 更新礼包的购买次数

        LogLordHelper.gift(AwardFrom.GIFT_PROMOTION_BUY, player.account, player.lord, promotion.getPrice(),
                promotion.getPromotionId(), activity.getActivityType());

        PromotionPropBuyRs.Builder builder = PromotionPropBuyRs.newBuilder();
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, promotion.getList(), null,
                AwardFrom.GIFT_PROMOTION_BUY, keyId));
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 军备促销处理
     *
     * @param player
     * @param promotion
     * @return
     */
    private PromotionPropBuyRs processJunbei(Player player, StaticPromotion promotion) throws MwException {
        int keyId = promotion.getPromotionId();
        long roleId = player.roleId;
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_PROP_PROMOTION);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 打折礼包购买,活动未开启 roleId:", player.roleId);
        }
        GlobalActivityData activityData = activityDataManager.getGlobalActivity(activity.getActivityType());
        if (CheckNull.isNull(activityData)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 打折礼包购买,活动未开启 roleId:", player.roleId);
        }

        StaticActAward sAward = StaticActivityDataMgr.getActAwardById(activity.getActivityId()).get(0);
        if (CheckNull.isNull(sAward)) {
            throw new MwException(GameError.ACT_NOT_AWARD.getCode(), " 军备积分礼包未配置");
        }
        int schedule = activity.getStatusCnt().get(keyId) == null ? 0
                : new Long(activity.getStatusCnt().get(keyId)).intValue();// 当前礼包的购买次数
        if (promotion.getCount() > 0 && schedule >= promotion.getCount()) {
            throw new MwException(GameError.PROMOTION_BUY_MAX.getCode(), " 打折礼包购买次数已达上限, roleId:", roleId, ", cnt:",
                    schedule);
        }

        // 检查背包
        rewardDataManager.checkBag(player, promotion.getList());
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                promotion.getPrice(), AwardFrom.PROMOTION_GIFT_BUY, keyId);// 购买打折礼包

        activity.getStatusCnt().put(keyId, Long.valueOf(schedule + 1));// 更新礼包的购买次数
        // 军备促销更新阵营积分榜
        activityDataManager.updGlobalActivity(player, ActivityConst.ACT_PROP_PROMOTION, promotion.getCond(),
                player.lord.getCamp());

        LogLordHelper.gift(AwardFrom.PROMOTION_GIFT_BUY, player.account, player.lord, promotion.getPrice(),
                promotion.getPromotionId(), activity.getActivityType());

        PromotionPropBuyRs.Builder builder = PromotionPropBuyRs.newBuilder();

        long val = activityData.getCampValByCamp(player.lord.getCamp());// 当前阵营积分
        int awardCnt = activityDataManager.getAwardCnt(sAward, activity, val); // 可领取次数
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, promotion.getList(), null,
                AwardFrom.PROMOTION_GIFT_BUY, keyId));
        builder.setGold(player.lord.getGold());
        builder.setIntegral(new Long(val).intValue());
        builder.setAwardCnt(awardCnt);
        return builder.build();
    }

    /**
     * 触发式礼包购买
     *
     * @param req
     * @param roleId
     * @return
     */
    public TriggerGiftBuyRs triggerGiftBuy(TriggerGiftBuyRq req, Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        TriggerGiftBuyRs.Builder builder = TriggerGiftBuyRs.newBuilder();
        int giftpackId = req.getId();
        StaticActGiftpack giftpack = StaticActivityDataMgr.getActGiftpackMapById(giftpackId);
        if (giftpack == null) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:", player.roleId,
                    ", giftId:", giftpackId);
        }
        StaticTriggerConf triggerConf = StaticActivityDataMgr.getTriggerGiftConfByGiftId(giftpackId);
        if (CheckNull.isNull(triggerConf)) {
            throw new MwException(GameError.TRIGGER_CONF_NO_FOUND.getCode(), " 检测礼包配置,没有该礼包配置 roleId:", player.roleId,
                    ", giftId:", giftpackId);
        }

        // 检查背包空间
        rewardDataManager.checkBag(player, giftpack.getAward());
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, giftpack.getGold(),
                AwardFrom.TRIGGER_GIFT_BUY, false, giftpackId);// 购买触发式礼包

        TriggerGift triggerGift = activityDataManager.getTriggerGiftInfoByGiftId(player, triggerConf.getGiftId(),
                false);
        if (activityDataManager.checkGiftState(triggerGift, triggerConf, giftpack, now)) {
            triggerGift.maxCount();
            triggerGift.setState(ActivityConst.STATUS_HAS_GAIN);
        }
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, giftpack.getAward(), null,
                AwardFrom.TRIGGER_GIFT_BUY, giftpackId));
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 体力购买
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public BuyActRs buyAct(Long roleId, BuyActRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // vip购买体力次数
        if (player.common.getBuyAct() >= vipDataManager.getNum(player.lord.getVip(), VipConstant.BUY_ACT)) {
            throw new MwException(GameError.SHOP_VIP_BUY_CNT.getCode(), "体力购买时，vip次数不够, roleId:" + roleId);
        }

        Integer price = Constant.BUY_ACT_PRICE.get(player.common.getBuyAct());
        if (price == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "体力购买时，vip次数不够, roleId:" + roleId);
        }

        // 判断体力是否已满
        if (player.lord.getPower() >= Constant.POWER_MAX) {
            throw new MwException(GameError.MAX_ACT_STORE.getCode(), "体力购买时，体力已达最大值, roleId:" + roleId);
        }

        if (price > 0) {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, price,
                    AwardFrom.POWER_BUY);// 购买体力
        }

        player.common.setBuyAct(player.common.getBuyAct() + 1);

        rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.ACT, Constant.BUY_ACT_REWARD,
                AwardFrom.POWER_BUY);
        taskDataManager.updTask(player, TaskType.COND_BUY_ACT_40, 1);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_BUY_ACT_40, 1);
        BuyActRs.Builder builder = BuyActRs.newBuilder();
        builder.setAct(player.lord.getPower());
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 获取柏林商店信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetBerlinShopRs getBerlinShop(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetBerlinShopRs.Builder builder = GetBerlinShopRs.newBuilder();
        builder.setMilitaryExpenditure(player.getMilitaryExpenditure());
        return builder.build();
    }

    /**
     * 柏林银行购买
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public BerlinShopBuyRs berlinShopBuy(long roleId, BerlinShopBuyRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int berlinwarState = berlinWarService.getBerlinwarState();
        if (berlinwarState == 0 || berlinwarState == WorldConstant.BERLIN_STATUS_OPEN) {
            throw new MwException(GameError.BERLIN_WAR_NOT_TRUCE_STATE.getCode(), "柏林银行兑换状态不正确, roleId:" + roleId);
        }
        int id = req.getId();
        StaticBerlinShop sBerlinShop = StaticShopDataMgr.getBerlinShopMap().get(id);
        if (sBerlinShop == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "柏林银行物品未找到, roleId:", roleId, ", id:", id);
        }
        int hasCredit = player.getMilitaryExpenditure();
        if (hasCredit < sBerlinShop.getPrice()) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), " 柏林军费资源数量不足, roleId:", roleId, ", need:",
                    sBerlinShop.getPrice(), ", have:", hasCredit);
        }
        int curSchId = worldScheduleService.getCurrentSchduleId();
        if (!sBerlinShop.isInSchedule(curSchId)) {
            throw new MwException(GameError.BUY_BERLIN_SHOP_SCHEDULE_STATUS_ERROR.getCode(), "购买柏林会战世界进程阶段错误, roleId:", roleId, ", id:", id, ", curSchId:", curSchId);
        }
        // 记录柏林商店购买 减掉积分
        LogLordHelper.commonLog("expenditure", AwardFrom.BERLIN_SHOP_BUY, player.account, player.lord, player.getMilitaryExpenditure(), -sBerlinShop.getPrice());
        // 减掉积分
        player.setMilitaryExpenditure(hasCredit - sBerlinShop.getPrice());
        //上报数数
        EventDataUp.credits(player.account, player.lord,player.getMilitaryExpenditure(),-sBerlinShop.getPrice(),CreditsConstant.BERLIN,AwardFrom.BERLIN_SHOP_BUY);
        Award award = rewardDataManager.addAwardSignle(player, sBerlinShop.getAward(), AwardFrom.BERLIN_SHOP_BUY, id);
        BerlinShopBuyRs.Builder builder = BerlinShopBuyRs.newBuilder();
        builder.addAward(award);
        builder.setMilitaryExpenditure(player.getMilitaryExpenditure());
        return builder.build();
    }

    /**
     * 聊天气泡购买
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public ChatBubbleBuyRs changeChatBubble(long roleId, ChatBubbleBuyRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int bubbleId = req.getBubbleId();
        StaticChatBubble sChatBubble = StaticLordDataMgr.getChatBubbleMapById(bubbleId);
        if (sChatBubble == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "聊天气泡配置未配置  bubbleId:", bubbleId, ", roleId:", roleId);
        }
        if (sChatBubble.getType() != StaticChatBubble.TYPE_GOLD) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "气泡类型不是金币购买类型   bubbleId:", bubbleId, ", roleId:",
                    roleId);
        }

//        if (player.getChatBubbles().contains(bubbleId)) {
        if (dressUpDataManager.checkCanBuy(player,AwardType.CHAT_BUBBLE,bubbleId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "已经拥有该气泡哦 bubbleId:", bubbleId, ", roleId:", roleId);
        }
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, sChatBubble.getParam(),
                AwardFrom.CHAT_BUBBLE_BUY, false, bubbleId);
//        player.getChatBubbles().add(bubbleId);
        dressUpDataManager.addDressUp(player,AwardType.CHAT_BUBBLE,bubbleId,1,null,AwardFrom.CHAT_BUBBLE_BUY);
        ChatBubbleBuyRs.Builder builder = ChatBubbleBuyRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setBubbleId(req.getBubbleId());
        return builder.build();
    }

    /**
     * 荣耀演练场副本商店购买
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public BuyMentorShopRs buyMentorShop(long roleId, BuyMentorShopRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        final int type = req.getType();
        final int shopId = req.getShopId();
        StaticMentorShop sMentorShop = StaticShopDataMgr.getMentorShopMap().get(shopId);
        if (sMentorShop == null || sMentorShop.getType() != type) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找到不荣耀演练场商品id shopId:", shopId, ", type:", type,
                    ", roleId:", roleId);
        }
        PitchCombat pitchCombat = player.getPitchCombat(type);
        if (pitchCombat == null || pitchCombat.getCombatPoint() < sMentorShop.getCostPoint()) {
            throw new MwException(GameError.BUY_MENTOR_SHOP_POINT_NOT_ENOUGH.getCode(), "购买积分不足 shopId:", shopId,
                    ", type:", type, ", roleId:", roleId);
        }
        if (sMentorShop.isCombatIdCondEnable()) {
            if (pitchCombat.getHighestCombatId() < sMentorShop.getCombatIdCond()) {
                throw new MwException(GameError.BUY_MENTOR_COMBATID_NOT_COND.getCode(), "副本条件为达到 shopId:", shopId,
                        ", type:", type, ", highestCombatId:", pitchCombat.getHighestCombatId(), ", roleId:", roleId);
            }
        }
        BuyMentorShopRs.Builder builder = BuyMentorShopRs.newBuilder();
        if (sMentorShop.isBuyCntEnable()) {
            int cnt = pitchCombat.getBuyCnt().getOrDefault(shopId, 0);
            if (cnt >= sMentorShop.getBuyCnt()) {
                throw new MwException(GameError.BUY_MENTOR_NOT_BUY_CNT.getCode(), "没有购买次数 shopId:", shopId, ", type:",
                        ", roleId:", roleId);
            }
            int newCnt = cnt + 1;
            pitchCombat.getBuyCnt().put(shopId, newCnt);
            builder.setBuyCnt(PbHelper.createTwoIntPb(shopId, newCnt));
        }
        // 扣积分
        pitchCombat.subCombatPoint(sMentorShop.getCostPoint());
        // 给奖励
        Award award = rewardDataManager.addAwardSignle(player, sMentorShop.getAward(), AwardFrom.BUY_MENTOR_SHOP,
                shopId);
        builder.addAward(award);
        builder.setPoints(pitchCombat.getCombatPoint());
        return builder.build();
    }

}
