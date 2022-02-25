package com.gryphpoem.game.zw.gameplay.local.service.warfire;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.WarFireBuff;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireBuffRs;
import com.gryphpoem.game.zw.pb.GamePb5.BuyWarFireShopRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CrossWarFireLocalData;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticRebelBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireShop;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-27 15:31
 */
@Service
public class WarFireCommonService {

    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private WorldScheduleService worldScheduleService;

    /**
     * 购买战火燎原buff
     *
     * @param roleId   玩家id
     * @param buffType buff类型
     * @return 购买的buff
     * @throws MwException 自定义异常
     */
    public BuyWarFireBuffRs buyWarFireBuff(long roleId, int buffType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
        if (!globalWarFire.canBuyBuff()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", 购买战火燎原buff时间不对");
        }
        // CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        // if (player.lord.getArea() != cMap.getMapId()) {
        //     throw new MwException(GameError.PARAM_ERROR.getCode(), "你不在对应的地图,不能购买 roleId:", roleId);
        // }
        PlayerWarFire playerWarFire = globalWarFire.getPlayerWarFire(roleId);
        WarFireBuff warFireBuff = playerWarFire.getBuffs().get(buffType);

        StaticWarFireBuff sBuff;
        if (warFireBuff == null) {// 第一次购买这个buff
            sBuff = StaticCrossWorldDataMgr.getWarFireBuffByTypeLv(buffType, 1);
        } else {
            sBuff = StaticCrossWorldDataMgr.getWarFireBuffByTypeLv(buffType, warFireBuff.getLv() + 1);
        }
        if (sBuff == null) {
            throw new MwException(GameError.WAR_FIRE_BUFF_LEVEL_MAX.getCode(), "roleId:", roleId, ", 配置未找到或buff等级已满  buffType:" + buffType);
        }
        // 扣钱
        int needGood = sBuff.getCost();
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, needGood, AwardFrom.BUY_WAR_FIRE_BUFF, false, sBuff.getBuffId());
        // 给buff
        if (warFireBuff == null) {
            warFireBuff = new WarFireBuff(globalWarFire.getBeginDate(),
                    globalWarFire.getEndDate(),
                    sBuff.getType(),
                    sBuff.getLv());
        } else {
            warFireBuff.setLv(sBuff.getLv());// 升一级
        }
        playerWarFire.getBuffs().put(buffType, warFireBuff); // 更新buff

        if (globalWarFire.getStage() == GlobalWarFire.STAGE_RUNNING && buffType != StaticRebelBuff.BUFF_TYPE_RECOVER_ARMY) {
            // 在打的期间重新计算战斗力进行同步
            CalculateUtil.reCalcAllHeroAttr(player);
        }
        BuyWarFireBuffRs.Builder builder = BuyWarFireBuffRs.newBuilder();
        builder.setBuff(WarFireBuff.toBuffInfo(warFireBuff));
        builder.setPrice(player.lord.getGold());
        return builder.build();
    }

    /**
     * 购买战火燎原物品
     *
     * @param roleId 玩家id
     * @param shopId 商品id
     * @return 购买的物品
     */
    public BuyWarFireShopRs buyWarFireShop(long roleId, int shopId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticWarFireShop sShop = StaticCrossWorldDataMgr.getStaticWarFireShopById(shopId);
        if (sShop == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", 战火燎原商品id未找到 shopId:", shopId);
        }

        //检测商品在当前赛季是否可售卖
        seasonService.checkSeasonItem(sShop.getSeasons());
        //检查世界进程
        int currWorldSchedule = worldScheduleService.getCurrentSchduleId();
        if(currWorldSchedule < sShop.getSchedule().get(0) || currWorldSchedule > sShop.getSchedule().get(1)){
            throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"兑换战火燎原商品世界进程不符合",shopId,currWorldSchedule));
        }

        int lostCredit;
        String logType;
        int warFirePrice;
        int price = sShop.getPrice();
        if (!sShop.isMold()) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
            if (globalWarFire.getStage() == GlobalWarFire.STAGE_OVER) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", 兑换战火燎原商品时间不正确");
            }

            PlayerWarFire playerWarFire = globalWarFire.getPlayerWarFire(roleId);
            // 历史购买的次数
            int buyCnt = playerWarFire.getBuyRecord().getOrDefault(shopId, 0);
            if (buyCnt >= sShop.getNum()) {
                throw new MwException(GameError.WAR_FIRE_SHOP_BUY_UP_LIMIT.getCode(), "roleId:", roleId, ", 战火燎原商品已达到最大购买次数, shopId:", shopId);
            }

            // 检测积分是否足够
            warFirePrice = player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE);
            if (warFirePrice == 0 || warFirePrice < price) {
                throw new MwException(GameError.WAR_FIRE_PRICE_NOT_ENOUGH.getCode(), "roleId:", roleId, ", 兑换战火燎原商品积分不足");
            }
            lostCredit = warFirePrice - price;
            player.setMixtureData(PlayerConstant.WAR_FIRE_PRICE, lostCredit);
            // 记录购买次数
            playerWarFire.getBuyRecord().put(shopId, buyCnt + 1);
            logType = "warFirePrice";
        } else {
            StaticCrossGamePlayPlan plan = DataResource.getBean(CrossGamePlayService.class).checkNotOverConfig(player, CrossFunction.CROSS_WAR_FIRE.getFunctionId(), false);
            CrossWarFireLocalData warFireLocalData = (CrossWarFireLocalData) player.crossPlayerLocalData.getCrossFunctionData(CrossFunction.CROSS_WAR_FIRE, plan.getKeyId(), true);
            // 历史购买的次数
            int buyCnt = warFireLocalData.getBuyRecord().getOrDefault(shopId, 0);
            if (buyCnt >= sShop.getNum()) {
                throw new MwException(GameError.WAR_FIRE_SHOP_BUY_UP_LIMIT.getCode(), "roleId:", roleId, ", 战火燎原商品已达到最大购买次数, shopId:", shopId);
            }
            // 检测积分是否足够
            warFirePrice = player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE);
            if (warFirePrice == 0 || warFirePrice < price) {
                throw new MwException(GameError.WAR_FIRE_PRICE_NOT_ENOUGH.getCode(), "roleId:", roleId, ", 兑换战火燎原商品积分不足");
            }

            lostCredit = warFirePrice - price;
            player.setMixtureData(PlayerConstant.CROSS_WAR_FIRE_PRICE, lostCredit);
            // 记录购买次数
            warFireLocalData.getBuyRecord().put(shopId, buyCnt + 1);
            logType = "crossWarFirePrice";
        }

        LogLordHelper.commonLog(logType, AwardFrom.BUY_WAR_FIRE_SHOP, player.account, player.lord, warFirePrice, -price);
        //上报数数
        EventDataUp.credits(player.account,player.lord,lostCredit,-price,CreditsConstant.WARFIRE,AwardFrom.BUY_WAR_FIRE_SHOP);
        // 发送跑马灯
        if (sShop.needSendChat()) {
            chatDataManager.sendSysChat(ChatConst.CHAT_WAR_FIRE_SHOP, player.lord.getArea(), 0, player.lord.getNick(), shopId);
        }
        // 给奖励
        CommonPb.Award awardSingle = rewardDataManager.addAwardSignle(player, sShop.getAward(), AwardFrom.BUY_WAR_FIRE_SHOP);
        BuyWarFireShopRs.Builder builder = BuyWarFireShopRs.newBuilder();
        builder.setCredit(lostCredit);
        builder.setAward(awardSingle);
        return builder.build();
    }

}
