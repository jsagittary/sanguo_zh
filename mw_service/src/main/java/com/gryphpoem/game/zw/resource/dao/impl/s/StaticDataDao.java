package com.gryphpoem.game.zw.resource.dao.impl.s;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.s.*;

import java.util.List;
import java.util.Map;

public class StaticDataDao extends BaseDao {

    public Map<Integer,StaticPortraitFrame> selectPortraitFrameMap() {
        return this.getSqlSession().selectMap("StaticDao.selectPortraitFrameMap","id");
    }

    public Map<Integer,StaticNameplate> selectNameplateMap() {
        return this.getSqlSession().selectMap("StaticDao.selectNameplateMap","id");
    }


    public List<StaticSeasonRank> selectStaticSeasonRankList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticSeasonRankList");
    }

    public Map<Integer,StaticSeasonTaskScore> selectStaticSeasonTaskScoreMap(){
        return this.getSqlSession().selectMap("StaticDao.selectStaticSeasonTaskScoreMap","id");
    }
    public Map<Integer,StaticSeasonTask> selectStaticSeasonTaskMap(){
        return this.getSqlSession().selectMap("StaticDao.selectStaticSeasonTaskMap","taskId");
    }

    public Map<Integer,StaticSeasonTreasury> selectStaticSeasonTreasuryMap(){
        return this.getSqlSession().selectMap("StaticDao.selectStaticSeasonTreasuryMap","taskId");
    }

    public List<StaticSeasonTalentPlan> selectStaticSeasonTalentPlanList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticSeasonTalentPlanList");
    }

    public List<StaticSeasonPlan> selectStaticSeasonPlanList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticSeasonPlanList");
    }

    public Map<Integer, StaticSeasonTalent> selectStaticSeasonTalentMap(){
        return this.getSqlSession().selectMap("StaticDao.selectStaticSeasonTalentMap", "id");
    }

    public List<StaticDiaoChanAward> selectStaticDiaoChanAwardList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticDiaoChanAwardList");
    }

    public List<StaticDiaoChanDay> selectStaticDiaoChanDayList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticDiaoChanDayList");
    }

    public List<StaticDiaoChanDayTask> selectStaticDiaoChanDayTaskList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticDiaoChanDayTaskList");
    }

    public List<StaticDiaoChanRank> selectStaticDiaoChanRankList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticDiaoChanRankList");
    }

    public List<StaticSandTableAward> selectStaticSandTableAwardList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticSandTableAwardList");
    }

    public Map<Integer, StaticSandTableExchange> selectStaticSandTableExchangeMap() {
        return this.getSqlSession().selectMap("StaticDao.selectStaticSandTableExchangeMap", "id");
    }

    public List<StaticIniName> selectName() {
        return getSqlSession().selectList("StaticDao.selectName");
    }

    public StaticIniLord selectLord() {
        return getSqlSession().selectOne("StaticDao.selectLord");
    }

    public Map<Integer, StaticLordLv> selectLordLv() {
        return getSqlSession().selectMap("StaticDao.selectLordLv", "lordLv");
    }

    public Map<Integer, StaticSystem> selectSystemMap() {
        return getSqlSession().selectMap("StaticDao.selectSystemMap", "id");
    }

    public Map<Integer, StaticSystem> selectActParamMap() {
        return getSqlSession().selectMap("StaticDao.selectActParamMap", "id");
    }

    public Map<Integer, StaticMine> selectMineMap() {
        return getSqlSession().selectMap("StaticDao.selectMineMap", "mineId");
    }

    public Map<Integer, StaticArea> selectAreaMap() {
        return getSqlSession().selectMap("StaticDao.selectAreaMap", "area");
    }

    public Map<Integer, StaticCityDev> selectCityDevMap() {
        return getSqlSession().selectMap("StaticDao.selectCityDevMap", "lv");
    }

    public List<StaticAreaRange> selectAreaBlockList() {
        return getSqlSession().selectList("StaticDao.selectAreaBlockList");
    }

    public Map<Integer, StaticCity> selectCityMap() {
        return getSqlSession().selectMap("StaticDao.selectCityMap", "cityId");
    }

    public Map<Integer, StaticBandit> selectBanditMap() {
        return getSqlSession().selectMap("StaticDao.selectBanditMap", "banditId");
    }

    public Map<Integer, StaticBanditArea> selectBanditAreaMap() {
        return getSqlSession().selectMap("StaticDao.selectBanditAreaMap", "id");
    }

    // public Map<Integer, StaticBuilding> selectBuildingMap() {
    // return getSqlSession().selectMap("StaticDao.selectBuildingMap", "buildingId");
    // }

    public Map<Integer, StaticShop> selectShopMap() {
        return getSqlSession().selectMap("StaticDao.selectShopMap", "id");
    }

    public Map<Integer, StaticVip> selectVipMap() {
        return getSqlSession().selectMap("StaticDao.selectVipMap", "vipLv");
    }

    // public Map<Integer, StaticBuildingType> selectBuildingTypeMap() {
    // return getSqlSession().selectMap("StaticDao.selectBuildingTypeMap", "buildingType");
    // }

    public Map<Integer, StaticBuildingInit> selectBuildingInitMap() {
        return getSqlSession().selectMap("StaticDao.selectBuildingInitMap", "buildingId");
    }

    public Map<Integer, StaticCommandMult> selectCommandMultMap() {
        return getSqlSession().selectMap("StaticDao.selectCommandMultMap", "id");
    }

    public Map<Integer, StaticTechLv> selectTechLvMap() {
        return getSqlSession().selectMap("StaticDao.selectTechLvMap", "id");
    }

    public Map<Integer, StaticFactoryRecruit> selectFactoryRecruitMap() {
        return getSqlSession().selectMap("StaticDao.selectFactoryRecruitMap", "id");
    }

    public Map<Integer, StaticFactoryExpand> selectFactoryExpandMap() {
        return getSqlSession().selectMap("StaticDao.selectFactoryExpandMap", "id");
    }

    public Map<Integer, StaticChemical> selectChemicalMap() {
        return getSqlSession().selectMap("StaticDao.selectChemicalMap", "id");
    }

    public Map<Integer, StaticTreasure> selectTreasureMap() {
        return getSqlSession().selectMap("StaticDao.selectTreasureMap", "id");
    }

    public Map<Integer, StaticChemicalExpand> selectChemicalExpandMap() {
        return getSqlSession().selectMap("StaticDao.selectChemicalExpandMap", "cnt");
    }

    public List<StaticBuildingLv> selectBuildingLvList() {
        return getSqlSession().selectList("StaticDao.selectBuildingLvList");
    }

    public Map<Integer, StaticHero> selectHeroMap() {
        return getSqlSession().selectMap("StaticDao.selectHeroMap", "heroId");
    }

    public List<StaticHeroEvolve> selectHeroEvolveList() {
        return getSqlSession().selectList("StaticDao.selectHeroEvolveList");
    }

    public List<StaticHeroClergy> selectHeroClergy() {
        return getSqlSession().selectList("StaticDao.selectHeroClergy");
    }

    public Map<Integer, StaticHeroSeason> selectSeasonHeroMap(){
        return getSqlSession().selectMap("StaticDao.selectSeasonHero", "heroId");
    }

    public List<StaticHeroSeasonSkill> selectHeroSeasonSkill(){
        return getSqlSession().selectList("StaticDao.selectHeroSeasonSkill");
    }

    public Map<Integer, StaticSkillAction> selectSkillAction(){
        return getSqlSession().selectMap("StaticDao.selectSkillAction", "id");
    }

    public Map<Integer, StaticHeroBreak> selectHeroBreakMap() {
        return getSqlSession().selectMap("StaticDao.selectHeroBreakMap", "quality");
    }

    public List<StaticHeroLv> selectHeroLvList() {
        return getSqlSession().selectList("StaticDao.selectHeroLvList");
    }

    public Map<Integer, StaticProp> selectPropMap() {
        return getSqlSession().selectMap("StaticDao.selectPropMap", "propId");
    }

    public List<StaticPropChat> selectPropChatList() {
        return getSqlSession().selectList("StaticDao.selectPropChatList");
    }

    public Map<Integer, StaticEquip> selectEquipMap() {
        return getSqlSession().selectMap("StaticDao.selectEquipMap", "equipId");
    }

    public List<StaticEquipExtra> selectEquipExtraList() {
        return getSqlSession().selectList("StaticDao.selectEquipExtraList");
    }

    public Map<Integer, StaticEquipQualityExtra> selectEquipQualityExtraMap() {
        return getSqlSession().selectMap("StaticDao.selectEquipQualityExtraMap", "quality");
    }

    public Map<Integer, StaticCombat> selectCombatMap() {
        return getSqlSession().selectMap("StaticDao.selectCombatMap", "combatId");
    }

    public Map<Integer, StaticStoneCombat> selectStoneCombatMap() {
        return getSqlSession().selectMap("StaticDao.selectStoneCombatMap", "combatId");
    }

    public Map<Integer, StaticSection> selectSection() {
        return getSqlSession().selectMap("StaticDao.selectSection", "sectionId");
    }

    public List<StaticTask> selectTask() {
        return getSqlSession().selectList("StaticDao.selectTask");
    }

    public List<StaticWorldTask> selectWorldTask() {
        return getSqlSession().selectList("StaticDao.selectWorldTask");
    }

    public Map<Integer, StaticDailyTaskAward> selectStaticDailyTaskAwardMap() {
        return getSqlSession().selectMap("StaticDao.selectStaticDailyTaskAward", "id");
    }

    public Map<Integer, StaticPartyTask> selectPartyTaskMap() {
        return getSqlSession().selectMap("StaticDao.selectPartyTaskMap", "id");
    }

    public Map<Integer, StaticNpc> selectNpcMap() {
        return getSqlSession().selectMap("StaticDao.selectNpcMap", "npcId");
    }

    public Map<Integer, StaticMail> selectMail() {
        return getSqlSession().selectMap("StaticDao.selectMail", "mailId");
    }

    public Map<Integer, StaticReward> selectRewradMap() {
        return getSqlSession().selectMap("StaticDao.selectRewradMap", "rewardId");
    }

    public Map<Integer, StaticRewardRandom> selectRewardRandomMap() {
        return getSqlSession().selectMap("StaticDao.selectRewardRandomMap", "randomId");
    }

    public Map<Integer, StaticScoutCost> selectScoutCostMap() {
        return getSqlSession().selectMap("StaticDao.selectScoutCostMap", "cityLv");
    }

    public Map<Integer, StaticScoutWeight> selectScoutWeightMap() {
        return getSqlSession().selectMap("StaticDao.selectScoutWeightMap", "gap");
    }

    public Map<Integer, StaticChat> selectChat() {
        return getSqlSession().selectMap("StaticDao.selectChat", "chatId");
    }

    public Map<Integer, StaticPartyLv> selectPartyLvMap() {
        return getSqlSession().selectMap("StaticDao.selectPartyLvMap", "lv");
    }

    public List<StaticPartyBuild> selectPartyBuildList() {
        return getSqlSession().selectList("StaticDao.selectPartyBuildList");
    }

    public List<StaticUptBuild> selectUptBuildList() {
        return getSqlSession().selectList("StaticDao.selectUptBuildList");
    }

    public Map<Integer, StaticPartyRanks> selectPartyRanksMap() {
        return getSqlSession().selectMap("StaticDao.selectPartyRanksMap", "ranks");
    }

    public Map<Integer, StaticPartyJob> selectPartyJobMap() {
        return getSqlSession().selectMap("StaticDao.selectPartyJobMap", "job");
    }

    public Map<Integer, StaticPartyHonorGift> selectPartyHonorGiftMap() {
        return getSqlSession().selectMap("StaticDao.selectPartyHonorGiftMap", "id");
    }

    public List<StaticPartyHonorRank> selectPartyHonorRankList() {
        return getSqlSession().selectList("StaticDao.selectPartyHonorRankList");
    }

    public List<StaticPay> selectPay() {
        return getSqlSession().selectList("StaticDao.selectPay");
    }

    public List<StaticPay> selectPayIos() {
        return getSqlSession().selectList("StaticDao.selectPayIos");
    }

    public Map<String, StaticPayPlat> selectPayPlat() {
        return getSqlSession().selectMap("StaticDao.selectPayPlat", "plat");
    }

    public Map<Integer, StaticSuperEquip> selectSuperEquipMap() {
        return getSqlSession().selectMap("StaticDao.selectSuperEquipMap", "type");
    }

    public Map<Integer, StaticSuperEquipLv> selectSuperEquipLvMap() {
        return getSqlSession().selectMap("StaticDao.selectSuperEquipLvMap", "id");
    }

    public Map<Integer, StaticSuperEquipBomb> selectSuperEquipBombMap() {
        return getSqlSession().selectMap("StaticDao.selectSuperEquipBombMap", "lv");
    }

    public Map<Integer, StaticWallHero> selectWallHeroMap() {
        return getSqlSession().selectMap("StaticDao.selectWallHeroMap", "id");
    }

    public Map<Integer, StaticWallHeroLv> selectWallHeroLvMap() {
        return getSqlSession().selectMap("StaticDao.selectWallHeroLvMap", "id");
    }

    public Map<Integer, StaticAcquisition> selectAcquisitionMap() {
        return getSqlSession().selectMap("StaticDao.selectAcquisitionMap", "id");
    }

    public List<StaticActAward> selectActAward() {
        return getSqlSession().selectList("StaticDao.selectActAward");
    }

    public Map<Integer, StaticActivityOpen> selectActivityOpen() {
        return getSqlSession().selectMap("StaticDao.selectActivityOpen", "keyId");
    }

    public List<StaticActivityEffect> selectActivityEffectList() {
        return getSqlSession().selectList("StaticDao.selectActivityEffectList");
    }

    public List<StaticActRank> selectActRankList() {
        return getSqlSession().selectList("StaticDao.selectActRank");
    }

    public List<StaticDay7Act> selectStaticDay7ActList() {
        return getSqlSession().selectList("StaticDao.selectStaticDay7Act");
    }

    public List<StaticActivityPlan> selectStaticActivityPlan() {
        return getSqlSession().selectList("StaticDao.selectStaticActivityPlan");
    }

    public Map<Integer, StaticActivity> selectStaticActivity() {
        return getSqlSession().selectMap("StaticDao.selectStaticActivity", "type");
    }

    public Map<Integer, StaticHeroSearch> selectHeroSearchMap() {
        return getSqlSession().selectMap("StaticDao.selectHeroSearchMap", "autoId");
    }

    public Map<Integer, StaticFunctionUnlock> selectFunctionUnlockMap() {
        return getSqlSession().selectMap("StaticDao.selectFunctionUnlockMap", "functionId");
    }

    public Map<Integer, StaticFunctionCondition> selectFunctionConditionMap() {
        return getSqlSession().selectMap("StaticDao.selectFunctionConditionMap", "conditionId");
    }

    public List<StaticResetTotal> selectRestTotal() {
        return getSqlSession().selectList("StaticDao.selectResetTotal");
    }

    public Map<Integer, StaticCreditShop> selectCreditShopMap() {
        return getSqlSession().selectMap("StaticDao.selectCreditShopMap", "id");
    }

    public Map<Integer, StaticMasterReaward> selectMasterReawardMap() {
        return getSqlSession().selectMap("StaticDao.selectMasterReawardMap", "id");
    }

    public Map<Integer, StaticActBlackhawk> selectActBlackhawkMap() {
        return getSqlSession().selectMap("StaticDao.selectActBlackhawkMap", "keyId");
    }

    public Map<Integer, StaticCabinetPlan> selectCabinetPlanMap() {
        return getSqlSession().selectMap("StaticDao.selectCabinetPlanMap", "id");
    }

    public Map<Integer, StaticCabinetLv> selectCabinetLv() {
        return getSqlSession().selectMap("StaticDao.selectCabinetLv", "lv");
    }

    public Map<Integer, StaticPortrait> selectPortrait() {
        return getSqlSession().selectMap("StaticDao.selectPortrait", "id");
    }

    public List<StaticSmallClear> selectSmallClearList() {
        return getSqlSession().selectList("StaticDao.selectSmallClearList");
    }

    public Map<Integer, StaticGuidAward> selectGuidAward() {
        return getSqlSession().selectMap("StaticDao.selectGuidAward", "cond");
    }

    public List<StaticActivityTime> selectActivityTimeList() {
        return getSqlSession().selectList("StaticDao.selectActivityTimeList");
    }

    public Map<Integer, StaticActGiftpack> selectStaticActGiftpack() {
        return getSqlSession().selectMap("StaticDao.selectStaticActGiftpack", "giftpackId");
    }

    public List<StaticGiftpackPlan> selectStaticGiftpackPlan() {
        return getSqlSession().selectList("StaticDao.selectStaticGiftpackPlan");
    }

    public List<StaticGiftPackTriggerPlan> selectStaticGiftPackTriggerPlan(){
        return getSqlSession().selectList("StaticDao.selectStaticGiftPackTriggerPlan");
    }

    public Map<Integer, StaticFunctionOpen> selectStaticFunctionOpen() {
        return getSqlSession().selectMap("StaticDao.selectStaticFunctionOpen", "typeId");
    }

    public Map<Integer, StaticPushMessage> selectPushMessageMap() {
        return getSqlSession().selectMap("StaticDao.selectPushMessageMap", "pushId");
    }

    public List<StaticPushApp> selectPushAppList() {
        return getSqlSession().selectList("StaticDao.selectPushAppMap");
    }

    public Map<String, StaticText> selectTextMap() {
        return getSqlSession().selectMap("StaticDao.selectTextMap", "id");
    }

    public List<StaticSectiontask> selectSectiontask() {
        return getSqlSession().selectList("StaticDao.selectSectiontask");
    }

    public Map<Integer, StaticGuideBuild> selectGuideBuildMap() {
        return getSqlSession().selectMap("StaticDao.selectGuideBuild", "buildingId");
    }

    public List<StaticRecommend> selectRecommend() {
        return getSqlSession().selectList("StaticDao.selectRecommend");
    }

    public Map<Integer, StaticBehaviorTree> selectBehaviorTreeMap() {
        return getSqlSession().selectMap("StaticDao.selectBehaviorTreeMap", "treeId");
    }

    public List<StaticBtreeNode> selectBtreeNodeList() {
        return getSqlSession().selectList("StaticDao.selectBtreeNodeList");
    }

    public List<StaticSolarTerms> selectSolarTerms() {
        return getSqlSession().selectList("StaticDao.selectSolarTerms");
    }

    public List<StaticDailyReward> selectDailyReward() {
        return getSqlSession().selectList("StaticDao.selectDailyReward");
    }

    public List<StaticWorldRule> selectStaticWorldRule() {
        return getSqlSession().selectList("StaticDao.selectStaticWorldRule");
    }

    public List<StaticTriggerConf> selectStaticTriggerConfList() {
        return getSqlSession().selectList("StaticDao.selectStaticTriggerConfList");
    }

    public List<StaticNightRaid> selectNightRaid() {
        return getSqlSession().selectList("StaticDao.selectNightRaid");
    }

    public Map<Integer, StaticSkill> selectSkill() {
        return getSqlSession().selectMap("StaticDao.selectSkill", "skillId");
    }

    public Map<Integer, StaticGestapoPlan> selectGestapoMap() {
        return getSqlSession().selectMap("StaticDao.selectGestapoMap", "gestapoId");
    }

    public List<StaticActExchange> selectStaticActExchange() {
        return getSqlSession().selectList("StaticDao.selectStaticActExchange");
    }

    public List<StaticAtkCityAct> selectStaticAtkCityAct() {
        return getSqlSession().selectList("StaticDao.selectStaticAtkCityAct");
    }

    public Map<Integer, StaticStone> selectStaticStone() {
        return getSqlSession().selectMap("StaticDao.selectStaticStone", "id");
    }

    public Map<Integer, StaticStoneHole> selectStaticStoneHole() {
        return getSqlSession().selectMap("StaticDao.selectStaticStoneHole", "type");
    }

    public Map<Integer, StaticStoneImprove> selectStaticStoneImprove() {
        return getSqlSession().selectMap("StaticDao.selectStaticStoneImprove", "id");
    }

    public List<StaticLightningWar> selectLightningWar() {
        return getSqlSession().selectList("StaticDao.selectLightningWar");
    }

    public Map<Integer, StaticPromotion> selectPromotionMap() {
        return getSqlSession().selectMap("StaticDao.selectPromotionMap", "promotionId");
    }

    public List<StaticAgent> selectAgentList() {
        return getSqlSession().selectList("StaticDao.selectAgent");
    }

    public Map<Integer, StaticAgentGift> selectAgentGiftMap() {
        return getSqlSession().selectMap("StaticDao.selectAgentGift", "giftId");
    }

    public List<StaticAgentStar> selectAgentStarList() {
        return getSqlSession().selectList("StaticDao.selectAgentStar");
    }

    public List<StaticTurnplateConf> selectTurnplateConf() {
        return getSqlSession().selectList("StaticDao.selectTurnplateConf");
    }

    public List<StaticEquipTurnplateConf> selectEquipTurnplateConf() {
        return getSqlSession().selectList("StaticDao.selectEquipTurnplateConf");
    }

    public Map<Integer, StaticBarrage> selectBarrage() {
        return getSqlSession().selectMap("StaticDao.selectBarrage", "id");
    }

    public Map<Integer, StaticRedPacket> selectRedPacket() {
        return getSqlSession().selectMap("StaticDao.selectRedPacket", "id");
    }
    public Map<Integer, StaticRedPacketMessage> selectRedPacketMessage() {
        return getSqlSession().selectMap("StaticDao.selectRedPacketMessage", "id");
    }

    public Map<Integer, StaticRedpacketList> selectRedpacketList() {
        return getSqlSession().selectMap("StaticDao.selectRedpacketList", "id");
    }

    public List<StaticActPayTurnplate> selectActPayTurnplate() {
        return getSqlSession().selectList("StaticDao.selectActPayTurnplate");
    }

    public List<StaticActOreTurnplate> selectActOreTurnplate() {
        return getSqlSession().selectList("StaticDao.selectActOreTurnplate");
    }

    public Map<Integer, StaticActDaydiscounts> selectActDaydiscountsMap() {
        return getSqlSession().selectMap("StaticDao.selectActDaydiscountsMap", "actGiftId");
    }

    public Map<Integer, StaticRandomProp> selectRandomPropMap() {
        return getSqlSession().selectMap("StaticDao.selectRandomPropMap", "propid");
    }

    public Map<Integer, StaticBerlinWar> selectBerlinWar() {
        return getSqlSession().selectMap("StaticDao.selectBerlinWarMap", "typeId");
    }

    public Map<Integer, StaticSuperMine> selectSuperMineMap() {
        return getSqlSession().selectMap("StaticDao.selectSuperMineMap", "mineId");
    }

    public Map<Integer, StaticCityAward> selectCityAwardMap() {
        return getSqlSession().selectMap("StaticDao.selectCityAwardMap", "num");
    }

    public List<StaticBerlinWarAward> selectBerlinWarAwardList() {
        return getSqlSession().selectList("StaticDao.selectBerlinWarAwardList");
    }

    public Map<Integer, StaticBerlinShop> selectBerlinShopMap() {
        return getSqlSession().selectMap("StaticDao.selectBerlinShopMap", "id");
    }

    public Map<Integer, StaticBerlinJob> selectBerlinJobMap() {
        return getSqlSession().selectMap("StaticDao.selectBerlinJobMap", "job");
    }

    public Map<Integer, StaticBerlinBuff> selectBerlinBuffMap() {
        return getSqlSession().selectMap("StaticDao.selectBerlinBuffMap", "buffId");
    }

    public Map<Integer, StaticHeroDecorated> selectHeroDecoratedMap() {
        return getSqlSession().selectMap("StaticDao.selectHeroDecoratedMap", "cnt");
    }

    public Map<Integer, StaticCityBuffer> selectCityBufferMap() {
        return getSqlSession().selectMap("StaticDao.selectCityBufferMap", "id");
    }

    public Map<Integer, StaticWeightBoxProp> selectWeightBoxPropMap() {
        return getSqlSession().selectMap("StaticDao.selectWeightBoxPropMap", "propid");
    }

    public Map<Integer, StaticHonorDailyCond> selectHonorDailyDondMap() {
        return getSqlSession().selectMap("StaticDao.selectHonorDailyDondMap", "cond");
    }

    public Map<Integer, StaticPrewarBuff> selectBerlinPrewarBuff() {
        return getSqlSession().selectMap("StaticDao.selectBerlinPrewarBuff", "id");
    }

    public Map<Integer, StaticChatBubble> selectChatBubbleMap() {
        return getSqlSession().selectMap("StaticDao.selectChatBubbleMap", "id");
    }

    public Map<Integer, StaticBodyImage> selectBodyImageMap() {
        return getSqlSession().selectMap("StaticDao.selectBodyImageMap", "id");
    }

    public Map<Integer, StaticActMonopoly> selectActMonopolyMap() {
        return getSqlSession().selectMap("StaticDao.selectActMonopolyMap", "keyId");
    }

    public List<StaticActMonopolyCount> selectStaticActMonopolyCountList() {
        return getSqlSession().selectList("StaticDao.selectStaticActMonopolyCountList");
    }

    /******************* 勋章 start **********************/
    public Map<Integer, StaticMedal> selectMedalMap() {
        return getSqlSession().selectMap("StaticDao.selectMedal", "medalId");
    }

    public Map<Integer, StaticMedalGeneralSkill> selectMedalGeneralSkillMap() {
        return getSqlSession().selectMap("StaticDao.selectMedalGeneralSkill", "generalSkillId");
    }

    public Map<Integer, StaticMedalSpecialSkill> selectMedalSpecialSkillMap() {
        return getSqlSession().selectMap("StaticDao.selectMedalSpecialSkill", "specialSkillId");
    }

    public Map<Integer, StaticMedalAuraSkill> selectMedalAuraSkillMap() {
        return getSqlSession().selectMap("StaticDao.selectMedalAuraSkill", "auraSkillId");
    }

    public List<StaticMedalSkillWeight> selectMedalSkillWeightList() {
        return getSqlSession().selectList("StaticDao.selectMedalSkillWeight");
    }

    public List<StaticMedalAttr> selectMedalAttrList() {
        return getSqlSession().selectList("StaticDao.selectMedalAttr");
    }

    public Map<Integer, StaticMedalGoods> selectMedalGoodsMap() {
        return getSqlSession().selectMap("StaticDao.selectMedalGoods", "medalGoodsId");
    }

    public Map<Integer, StaticHonorGoods> selectHonorGoodsMap() {
        return getSqlSession().selectMap("StaticDao.selectHonorGoods", "honorGoodsId");
    }

    public Map<Integer, StaticMedalDonate> selectMedalDonateMap() {
        return getSqlSession().selectMap("StaticDao.selectMedalDonate", "quality");
    }

    /******************* 勋章 end **********************/

    public Map<Integer, StaticRebelBuff> selectRebelBuffMap() {
        return getSqlSession().selectMap("StaticDao.selectRebelBuffMap", "buffId");
    }

    public Map<Integer, StaticRebelRound> selectRebelRoundMap() {
        return getSqlSession().selectMap("StaticDao.selectRebelRoundMap", "id");
    }

    public Map<Integer, StaticRebelShop> selectRebelShopMap() {
        return getSqlSession().selectMap("StaticDao.selectRebelShopMap", "id");
    }

    public Map<Integer, StaticPlaneUpgrade> selectPlaneUpgradeMap() {
        return getSqlSession().selectMap("StaticDao.selectPlaneUpgradeMap", "planeId");
    }

    public List<StaticPlaneLv> selectPlaneLvList() {
        return getSqlSession().selectList("StaticDao.selectPlaneLvList");
    }

    public List<StaticPlaneSearch> selectPlaneSearchList() {
        return getSqlSession().selectList("StaticDao.selectPlaneSearchList");
    }

    public Map<Integer, StaticPlaneSkill> selectPlaneSkillMap() {
        return getSqlSession().selectMap("StaticDao.selectPlaneSkillMap", "skillId");
    }

    public Map<Integer, StaticPlaneInit> selectPlaneInitMap() {
        return getSqlSession().selectMap("StaticDao.selectPlaneInitMap", "planeType");
    }

    public Map<Integer, StaticPlaneTransform> selectPlaneTransform() {
        return getSqlSession().selectMap("StaticDao.selectPlaneTransform", "id");
    }

    public StaticActivityPlan getStaticActivityPlan(int activityType) {
        return this.getSqlSession().selectOne("StaticDao.getByIdActivityPlane", activityType);
    }

    public Map<Integer, StaticCounterAttack> selectCounterAtkMap() {
        return this.getSqlSession().selectMap("StaticDao.selectCounterAtkMap", "autoId");
    }

    public Map<Integer, StaticCounterAttackShop> selectCounterShopMap() {
        return this.getSqlSession().selectMap("StaticDao.selectCounterShopMap", "id");
    }

    public Map<Integer, StaticFunCard> selectFunCardMap() {
        return this.getSqlSession().selectMap("StaticDao.selectFunCardMap", "id");
    }

    public Map<Integer, StaticMentor> selectMentorIdMap() {
        return this.getSqlSession().selectMap("StaticDao.selectMentorIdMap", "id");
    }

    public Map<Integer, StaticMentorSkill> selectMentorSkillIdMap() {
        return this.getSqlSession().selectMap("StaticDao.selectMentorSkillIdMap", "id");
    }

    public Map<Integer, StaticMentorEquip> selectMentorEquipIdMap() {
        return this.getSqlSession().selectMap("StaticDao.selectMentorEquipIdMap", "id");
    }

    public Map<Integer, StaticPitchCombat> selectPitchCombatMap() {
        return this.getSqlSession().selectMap("StaticDao.selectPitchCombatMap", "combatId");
    }

    public Map<Integer, StaticMentorShop> selectMentorShopMap() {
        return this.getSqlSession().selectMap("StaticDao.selectMentorShopMap", "id");
    }

    public Map<Integer, StaticMultCombat> selectMultCombatMap() {
        return this.getSqlSession().selectMap("StaticDao.selectMultCombatMap", "combatId");
    }

    public Map<Integer, StaticMultcombatShop> selectMultcombatShopMap() {
        return this.getSqlSession().selectMap("StaticDao.selectMultcombatShopMap", "id");
    }

    public Map<Integer, StaticSpecialPlan> selectSpecialPlan() {
        return this.getSqlSession().selectMap("StaticDao.selectSpecialPlan", "activityType");
    }

    public Map<Integer, StaticFightSkill> selectFightSkill() {
        return this.getSqlSession().selectMap("StaticDao.selectFightSkill", "skillId");
    }

    public Map<Integer, StaticFightBuff> selectFightBuff() {
        return this.getSqlSession().selectMap("StaticDao.selectFightBuff", "buffId");
    }

    public Map<Integer, StaticActSign> selectStaticActSign() {
        return this.getSqlSession().selectMap("StaticDao.selectStaticActSign", "id");
    }

    public List<StaticServerList> selectStaticServerList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticServerList");
    }

    public Map<Integer, StaticAirship> selectStaticAirshipMap() {
        return this.getSqlSession().selectMap("StaticDao.selectStaticAirshipMap","id");
    }

    public Map<Integer,StaticAirshipArea> selectStaticAirshipAreaList() {
        return this.getSqlSession().selectMap("StaticDao.selectStaticAirshipAreaList","keyId");
    }

    public Map<Integer, StaticAirShipBuff> selectAirShipBuffMap() {
        return this.getSqlSession().selectMap("StaticDao.selectAirShipBuffMap","buffId");
    }

    public Map<Integer, StaticPartySupply> selectPartySupplyMap() {
        return this.getSqlSession().selectMap("StaticDao.selectPartySupplyMap","id");
    }

    public Map<Integer, StaticPartySuperSupply> selectPartySuperSupplyMap() {
        return this.getSqlSession().selectMap("StaticDao.selectPartySuperSupplyMap","lv");
    }

    public Map<Integer,StaticSchedule> selectScheduleMap() {
        return this.getSqlSession().selectMap("StaticDao.selectScheduleMap", "id");
    }

    public Map<Integer,StaticScheduleGoal> selectScheduleGoalMap() {
        return this.getSqlSession().selectMap("StaticDao.selectScheduleGoalMap", "id");
    }

    public Map<Integer, StaticScheduleBoss> selectScheduleBossMap() {
        return this.getSqlSession().selectMap("StaticDao.selectScheduleBossMap", "id");
    }

    public List<StaticScheduleRank> selectScheduleRankList() {
        return this.getSqlSession().selectList("StaticDao.selectScheduleRankMap");
    }

    public Map<Integer, StaticWorldwarOpen>  selectWorldwarOpenMap() {
        return this.getSqlSession().selectMap("StaticDao.selectWorldwarOpenMap", "id");
    }
    public List<StaticWorldWarPlan>  selectWorldWarPlan() {
        return this.getSqlSession().selectList("StaticDao.selectWorldWarPlan");
    }

    public List<StaticRingStrengthen> selectRingStrengthenList() {
        return this.getSqlSession().selectList("StaticDao.selectRingStrengthenMap");
    }

    public Map<Integer, StaticJewel> selectJewelMap() {
        return this.getSqlSession().selectMap("StaticDao.selectJewelMap","level");
    }

    public Map<Integer,StaticWorldWarShop> selectWorldWarShopMap() {
        return this.getSqlSession().selectMap("StaticDao.selectWorldWarShopMap","id");
    }

    public List<StaticWorldWarCampCityAward> selectWorldWarCampCityAwardList() {
        return this.getSqlSession().selectList("StaticDao.selectWorldWarCampCityAwardList");
    }

    public Map<Integer,StaticWorldWarCampRank> selectWorldWarCampRankMap() {
        return this.getSqlSession().selectMap("StaticDao.selectWorldWarCampRankMap","id");
    }

    public List<StaticWorldWarPersonalRank> selectWorldWarPersonalRankList() {
        return this.getSqlSession().selectList("StaticDao.selectWorldWarPersonalRankList");
    }

    public List<StaticWorldWarTask> selectWorldWarTaskList() {
        return this.getSqlSession().selectList("StaticDao.selectWorldWarTaskList");
    }

    public List<StaticWorldWarWeekTask> selectWorldWarWeekTaskList() {
        return this.getSqlSession().selectList("StaticDao.selectWorldWarWeekTaskList");
    }

    public List<StaticWorldWarDailyTask> selectWorldWarDailyTaskList() {
        return this.getSqlSession().selectList("StaticDao.selectWorldWarDailyTaskList");
    }
    public Map<Integer,StaticCastleSkin> selectCastleSkinMap() {
        return this.getSqlSession().selectMap("StaticDao.selectCastleSkinMap","id");
    }

    public Map<Integer,StaticTitle> selectTitleMap() {
        return this.getSqlSession().selectMap("StaticDao.selectTitle","id");
    }
    public List<StaticCastleSkinStar> selectCastleSkinStarList(){
        return this.getSqlSession().selectList("StaticDao.selectCastleSkinStarList");
    }

    public List<StaticActBarton> selectStaticActBartonList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticActBartonList");
    }

    public List<StaticActBandit> selectStaticActBanditList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticActBanditList");
    }

    public List<StaticActLogin> selectStaticActLoginList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticActLoginList");
    }

    public List<StaticNewYorkWarAchievement> selectStaticNewYorkWarAchievement(){
        return this.getSqlSession().selectList("StaticDao.selectStaticNewYorkWarAchievement");
    }

    public List<StaticNewYorkWarCampRank> selectStaticNewYorkWarCampRank(){
        return this.getSqlSession().selectList("StaticDao.selectStaticNewYorkWarCampRank");
    }

    public List<StaticNewYorkWarPersonalRank> selectStaticNewYorkWarPersonalRank(){
        return this.getSqlSession().selectList("StaticDao.selectStaticNewYorkWarPersonalRank");
    }

    public List<StaticFriendMessage> selectStaticFriendMessageList() {
        return getSqlSession().selectList("StaticDao.selectStaticFriendMessageList");
    }

    public List<StaticBlackWords> selectStaticBlackWordsList() {
        return getSqlSession().selectList("StaticDao.selectStaticBlackWordsList");
    }


    public List<StaticScheduleBossRank> selectScheduleBossRankIdMap() {
        return this.getSqlSession().selectList("StaticDao.selectScheduleBossRankIdMap");
    }

    public List<StaticBattlePassPlan> selectBattlePassPlan() {
        return this.getSqlSession().selectList("StaticDao.selectBattlePassPlan");
    }

    public List<StaticBattlePassTask> selectBattlePassTask() {
        return this.getSqlSession().selectList("StaticDao.selectBattlePassTask");
    }


    public List< StaticBattlePassLv> selectBattlePasslv() {
        return this.getSqlSession().selectList("StaticDao.selectBattlePasslv");
    }


    public List<StaticActRobinHood> selectActRobinHood() {
        return this.getSqlSession().selectList("StaticDao.selectActRobinHood");
    }


    public List<StaticTurnplateExtra> selectTurnplateExtra() {
        return this.getSqlSession().selectList("StaticDao.selectTurnplateExtra");
    }


    public List<StaticRoyalArenaTask> selectRoyalArenaTaskMap() {
        return this.getSqlSession().selectList("StaticDao.selectRoyalArenaTaskMap");
    }

    public List<StaticRoyalArenaAward> selectRoyalArenaAwardMap() {
        return this.getSqlSession().selectList("StaticDao.selectRoyalArenaAwardMap");
    }


    public List<StaticEasterAward> selectStaticEasterAwardList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticEasterAwardList");
    }

    public Map<Integer, StaticActHotProduct> selectActHotProductMap() {
        return this.getSqlSession().selectMap("StaticDao.selectActHotProductMap", "keyId");
    }

    public Map<Integer, StaticActAuction> selectActAuctionMap() {
        return this.getSqlSession().selectMap("StaticDao.selectActAuctionMap", "id");
    }

    public Map<Integer, StaticActVoucher> selectActVoucherMap() {
        return this.getSqlSession().selectMap("StaticDao.selectActVoucherMap", "id");
    }

    public List<StaticMergeBanner> selectStaticMergeBannerList() {
        return getSqlSession().selectList("StaticDao.selectStaticMergeBannerList");
    }

    public List<StaticChristmasAward> selectChristmasAwardList(){
        return this.getSqlSession().selectList("StaticDao.selectStaticChristmasAwardList");
    }


    public Map<Integer, StaticWarFire> selectWarFireList() {
        return this.getSqlSession().selectMap("StaticDao.selectWarFireList","id");
    }

    public Map<Integer, StaticWarFireBuff> selectWarFireBuffMap() {
        return getSqlSession().selectMap("StaticDao.selectWarFireBuffMap", "buffId");
    }

    public Map<Integer, StaticWarFireRankGr> selectWarFireRankGr(){
        return this.getSqlSession().selectMap("StaticDao.selectWarFireRankGr","gr");
    }

    public Map<Integer, StaticWarFireRankCamp> selectWarFireRankCamp(){
        return this.getSqlSession().selectMap("StaticDao.selectWarFireRankCamp","rankId");
    }


    public Map<Integer, StaticWarFireRange> selectWarFireRangeMap() {
        return this.getSqlSession().selectMap("StaticDao.selectWarFireRangeMap","id");
    }


    public Map<Integer, StaticWarFireShop> selectWarFireShopMap() {
        return this.getSqlSession().selectMap("StaticDao.selectWarFireShopMap","id");
    }


    public Map<Integer, StaticAltarArea> selectAltarAreaMap() {
        return this.getSqlSession().selectMap("StaticDao.selectAltarAreaMap","areaOrder");
    }


    public Map<Integer, StaticMarchLine> selectMarchLineMap() {
        return this.getSqlSession().selectMap("StaticDao.selectMarchLineMap","id");
    }

    /**
     * 喜悦金秋任务表
     */
    public Map<Integer, StaticActAutumnDayTask> selectActAutumnDayTaskMap() {
        return this.getSqlSession().selectMap("StaticDao.selectActAutumnDayTaskMap","id");
    }

    public List<StaticActQuestionnaire> selectActQuestionnaireList() {
        return this.getSqlSession().selectList("StaticDao.selectActQuestionnaireList");
    }

    public List<StaticBerlinFever> selectBerlinFeverList() {
        return getSqlSession().selectList("StaticDao.selectBerlinFeverList");
    }


    public Map<Integer, StaticHeroSearchExtAward> selectHeroSearchExtAward() {
        return this.getSqlSession().selectMap("StaticDao.selectHeroSearchExtAward","id");
    }

    //---------------------跨服--------------------

    public List<StaticCrossServerRule> selectStaticCrossServerRuleList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticCrossServerRuleList");
    }

    public Map<Integer, StaticCrossFort> selectStaticCrossFortMap() {
        return this.getSqlSession().selectMap("StaticDao.selectStaticCrossFortMap","id");
    }

    public Map<Integer, StaticCrossBuff> selectStaticCrossBuffMap() {
        return this.getSqlSession().selectMap("StaticDao.selectStaticCrossBuffMap","buffId");
    }

    public List<StaticCrossWarRank> selectStaticCrossWarRankList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticCrossWarRankList");
    }

    public Map<Integer, StaticCrossPersonalTrophy> selectStaticCrossPersonalTrophyMap() {
        return this.getSqlSession().selectMap("StaticDao.selectStaticCrossPersonalTrophyMap","id");
    }

    public List<StaticChannelMail> selectChannelMailList() {
        return this.getSqlSession().selectList("StaticDao.selectStaticChannelMailList");
    }

    public Map<Integer, StaticTask> selectTaskNewMap() {
        return getSqlSession().selectMap("StaticDao.selectTaskNewMap", "taskId");
    }

    public Map<Integer, StaticTaskChapter> selectTaskChapterMap() {
        return getSqlSession().selectMap("StaticDao.selectTaskChapterMap", "chapterId");
    }
}
