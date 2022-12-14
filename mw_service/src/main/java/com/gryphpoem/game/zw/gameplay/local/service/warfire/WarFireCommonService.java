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
     * ??????????????????buff
     *
     * @param roleId   ??????id
     * @param buffType buff??????
     * @return ?????????buff
     * @throws MwException ???????????????
     */
    public BuyWarFireBuffRs buyWarFireBuff(long roleId, int buffType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
        if (!globalWarFire.canBuyBuff()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", ??????????????????buff????????????");
        }
        // CrossWorldMapService.checkPlayerOnMap(roleId, cMap);
        // if (player.lord.getArea() != cMap.getMapId()) {
        //     throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????,???????????? roleId:", roleId);
        // }
        PlayerWarFire playerWarFire = globalWarFire.getPlayerWarFire(roleId);
        WarFireBuff warFireBuff = playerWarFire.getBuffs().get(buffType);

        StaticWarFireBuff sBuff;
        if (warFireBuff == null) {// ?????????????????????buff
            sBuff = StaticCrossWorldDataMgr.getWarFireBuffByTypeLv(buffType, 1);
        } else {
            sBuff = StaticCrossWorldDataMgr.getWarFireBuffByTypeLv(buffType, warFireBuff.getLv() + 1);
        }
        if (sBuff == null) {
            throw new MwException(GameError.WAR_FIRE_BUFF_LEVEL_MAX.getCode(), "roleId:", roleId, ", ??????????????????buff????????????  buffType:" + buffType);
        }
        // ??????
        int needGood = sBuff.getCost();
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, needGood, AwardFrom.BUY_WAR_FIRE_BUFF, false, sBuff.getBuffId());
        // ???buff
        if (warFireBuff == null) {
            warFireBuff = new WarFireBuff(globalWarFire.getBeginDate(),
                    globalWarFire.getEndDate(),
                    sBuff.getType(),
                    sBuff.getLv());
        } else {
            warFireBuff.setLv(sBuff.getLv());// ?????????
        }
        playerWarFire.getBuffs().put(buffType, warFireBuff); // ??????buff

        if (globalWarFire.getStage() == GlobalWarFire.STAGE_RUNNING && buffType != StaticRebelBuff.BUFF_TYPE_RECOVER_ARMY) {
            // ????????????????????????????????????????????????
            CalculateUtil.reCalcAllHeroAttr(player);
        }
        BuyWarFireBuffRs.Builder builder = BuyWarFireBuffRs.newBuilder();
        builder.setBuff(WarFireBuff.toBuffInfo(warFireBuff));
        builder.setPrice(player.lord.getGold());
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param roleId ??????id
     * @param shopId ??????id
     * @return ???????????????
     */
    public BuyWarFireShopRs buyWarFireShop(long roleId, int shopId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticWarFireShop sShop = StaticCrossWorldDataMgr.getStaticWarFireShopById(shopId);
        if (sShop == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", ??????????????????id????????? shopId:", shopId);
        }

        //??????????????????????????????????????????
        seasonService.checkSeasonItem(sShop.getSeasons());
        //??????????????????
        int currWorldSchedule = worldScheduleService.getCurrentSchduleId();
        if(currWorldSchedule < sShop.getSchedule().get(0) || currWorldSchedule > sShop.getSchedule().get(1)){
            throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"?????????????????????????????????????????????",shopId,currWorldSchedule));
        }

        int lostCredit;
        String logType;
        int warFirePrice;
        int price = sShop.getPrice();
        if (!sShop.isMold()) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
            if (globalWarFire.getStage() == GlobalWarFire.STAGE_OVER) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", ???????????????????????????????????????");
            }

            PlayerWarFire playerWarFire = globalWarFire.getPlayerWarFire(roleId);
            // ?????????????????????
            int buyCnt = playerWarFire.getBuyRecord().getOrDefault(shopId, 0);
            if (buyCnt >= sShop.getNum()) {
                throw new MwException(GameError.WAR_FIRE_SHOP_BUY_UP_LIMIT.getCode(), "roleId:", roleId, ", ?????????????????????????????????????????????, shopId:", shopId);
            }

            // ????????????????????????
            warFirePrice = player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE);
            if (warFirePrice == 0 || warFirePrice < price) {
                throw new MwException(GameError.WAR_FIRE_PRICE_NOT_ENOUGH.getCode(), "roleId:", roleId, ", ????????????????????????????????????");
            }
            lostCredit = warFirePrice - price;
            player.setMixtureData(PlayerConstant.WAR_FIRE_PRICE, lostCredit);
            // ??????????????????
            playerWarFire.getBuyRecord().put(shopId, buyCnt + 1);
            logType = "warFirePrice";
        } else {
            StaticCrossGamePlayPlan plan = DataResource.getBean(CrossGamePlayService.class).checkNotOverConfig(player, CrossFunction.CROSS_WAR_FIRE.getFunctionId(), false);
            CrossWarFireLocalData warFireLocalData = (CrossWarFireLocalData) player.crossPlayerLocalData.getCrossFunctionData(CrossFunction.CROSS_WAR_FIRE, plan.getKeyId(), true);
            // ?????????????????????
            int buyCnt = warFireLocalData.getBuyRecord().getOrDefault(shopId, 0);
            if (buyCnt >= sShop.getNum()) {
                throw new MwException(GameError.WAR_FIRE_SHOP_BUY_UP_LIMIT.getCode(), "roleId:", roleId, ", ?????????????????????????????????????????????, shopId:", shopId);
            }
            // ????????????????????????
            warFirePrice = player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE);
            if (warFirePrice == 0 || warFirePrice < price) {
                throw new MwException(GameError.WAR_FIRE_PRICE_NOT_ENOUGH.getCode(), "roleId:", roleId, ", ????????????????????????????????????");
            }

            lostCredit = warFirePrice - price;
            player.setMixtureData(PlayerConstant.CROSS_WAR_FIRE_PRICE, lostCredit);
            // ??????????????????
            warFireLocalData.getBuyRecord().put(shopId, buyCnt + 1);
            logType = "crossWarFirePrice";
        }

        LogLordHelper.commonLog(logType, AwardFrom.BUY_WAR_FIRE_SHOP, player.account, player.lord, warFirePrice, -price);
        //????????????
        EventDataUp.credits(player.account,player.lord,lostCredit,-price,CreditsConstant.WARFIRE,AwardFrom.BUY_WAR_FIRE_SHOP);
        // ???????????????
        if (sShop.needSendChat()) {
            chatDataManager.sendSysChat(ChatConst.CHAT_WAR_FIRE_SHOP, player.lord.getArea(), 0, player.lord.getNick(), shopId);
        }
        // ?????????
        CommonPb.Award awardSingle = rewardDataManager.addAwardSignle(player, sShop.getAward(), AwardFrom.BUY_WAR_FIRE_SHOP);
        BuyWarFireShopRs.Builder builder = BuyWarFireShopRs.newBuilder();
        builder.setCredit(lostCredit);
        builder.setAward(awardSingle);
        return builder.build();
    }

}
