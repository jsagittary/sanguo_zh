package com.gryphpoem.game.zw.resource.dao.impl.s;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.dao.sqlMap.s.StaticIniMapper;
import com.gryphpoem.game.zw.resource.domain.s.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xwind
 * @date 2021/7/7
 */
public class StaticIniDao extends BaseDao {

    public List<StaticWarFireBuffCross> selectStaticWarFireBuffCross(){
        return getMapper(StaticIniMapper.class).selectWarFireBuffCross();
    }
    public Map<Integer,StaticSmallGame> selectSmallGameMap(){
        return getMapper(StaticIniMapper.class).selectSmallGameMap();
    }
    public List<StaticSummerCharge> selectStaticSummerChargeList(){
        return getMapper(StaticIniMapper.class).selectStaticSummerChargeList();
    }

    public List<StaticSummerTurnplate> selectStaticSummerTurnplateList(){
        return getMapper(StaticIniMapper.class).selectStaticSummerTurnplateList();
    }

    public Map<Integer, StaticSummerCastle> selectStaticSummerCastleMap(){
        return getMapper(StaticIniMapper.class).selectStaticSummerCastleMap();
    }

    public List<StaticAnniversaryTurntable> selectStaticAnniversaryTurntableList(){
        return getMapper(StaticIniMapper.class).selectStaticAnniversaryTurntableList();
    }

    public List<StaticAnniversaryEgg> selectStaticAnniversaryEggList(){
        return getMapper(StaticIniMapper.class).selectStaticAnniversaryEggList();
    }

    public List<StaticRandomLibrary> selectStaticRandomLibraryList(){
        return getMapper(StaticIniMapper.class).selectStaticRandomLibraryList();
    }

    public Map<Integer, StaticActSkinEncore> selectStaticActSkinEncore(){
        return getMapper(StaticIniMapper.class).selectStaticActSkinEncore();
    }

    public List<StaticFishBaitHerocombination> selectStaticFishBaitHerocombinationList(){
        return getMapper(StaticIniMapper.class).selectStaticFishBaitHerocombinationList();
    }

    public List<StaticFishBait> selectStaticFishBaitList(){
        return getMapper(StaticIniMapper.class).selectStaticFishBaitList();
    }

    public Map<Integer,StaticFishResults> selectStaticFishResultsMap(){
        return getMapper(StaticIniMapper.class).selectStaticFishResultsMap();
    }

    public Map<Integer,StaticFishattribute> selectStaticFishattributeMap(){
        return getMapper(StaticIniMapper.class).selectStaticFishattributeMap();
    }

    public List<StaticFishProficiency> selectStaticFishProficiencyList() {
        return getMapper(StaticIniMapper.class).selectStaticFishProficiencyList();
    }

    public Map<Integer,StaticFishShop> selectStaticFishShopMap(){
        return getMapper(StaticIniMapper.class).selectStaticFishShopMap();
    }

    public List<StaticAutumnTurnplate> selectStaticAutumnTurnplateList(){
        return getMapper(StaticIniMapper.class).selectStaticAutumnTurnplateList();
    }

    public List<StaticActivityCrossPlan> selectStaticActivityPlanCross(){
        return getMapper(StaticIniMapper.class).selectStaticActivityPlanCross();
    }

    public List<StaticMusicFestivalBoxOffice> selectStaticMusicFestivalBoxOfficeList() {
        return getMapper(StaticIniMapper.class).selectStaticMusicFestivalBoxOfficeList();
    }

    public Map<Integer, StaticMusicFestivalBoxOfficeParam> selectStaticMusicFestivalBoxOfficeParamMap() {
        return getMapper(StaticIniMapper.class).selectStaticMusicFestivalBoxOfficeParamMap();
    }

    public Map<Integer, StaticCreativeOffice> selectStaticCreativeOffice(){
        return getMapper(StaticIniMapper.class).selectStaticCreativeOffice();
    }

    public Map<Integer, StaticCreativeOfficeAward> selectStaticCreativeOfficeAward(){
        return getMapper(StaticIniMapper.class).selectStaticCreativeOfficeAward();
    }

    public List<StaticTotem> selectStaticTotemList(){
        return getMapper(StaticIniMapper.class).selectStaticTotemList();
    }

    public List<StaticTotemLink> selectStaticTotemLinkList(){
        return getMapper(StaticIniMapper.class).selectStaticTotemLinkList();
    }

    public List<StaticTotemDrop> selectStaticTotemDropList(){
        return getMapper(StaticIniMapper.class).selectStaticTotemDropList();
    }

    public List<StaticTotemUp> selectStaticTotemUpList(){
        return getMapper(StaticIniMapper.class).selectStaticTotemUpList();
    }


    public Map<Integer, StaticTreasureWare> selectStaticTreasureWareMake() {
        return getMapper(StaticIniMapper.class).selectStaticTreasureWareMake();
    }

    public Map<Integer, StaticTreasureWareSpecial> selectStaticTreasureWareSpecial() {
        return getMapper(StaticIniMapper.class).selectStaticTreasureWareSpecial();
    }

    public List<StaticTreasureWareLevel> selectStaticTreasureWareLevel() {
        return getMapper(StaticIniMapper.class).selectStaticTreasureWareLevel();
    }

    public Map<Integer, StaticTreasureCombat> selectStaticTreasureCombat() {
        return getMapper(StaticIniMapper.class).selectStaticTreasureCombat();
    }

    public List<StaticTreasureCombatBuff> selectTreasureCombatBuffs() {
        return getMapper(StaticIniMapper.class).selectTreasureCombatBuffs();
    }

    public List<StaticTreasureWareProfile> selectTreasureWareProfile() {
        return getMapper(StaticIniMapper.class).selectTreasureWareProfile();
    }

    public Map<Integer,StaticFireworks> selectStaticFireworksMap() {
        return getMapper(StaticIniMapper.class).selectStaticFireworksMap();
    }

    public List<StaticFireworks> selectStaticFireworksList(){
        return getMapper(StaticIniMapper.class).selectStaticFireworksList();
    }

    public List<StaticFishing> selectStaticFishingList(){
        return getMapper(StaticIniMapper.class).selectStaticFishingList();
    }

    public List<StaticFishingLv> selectStaticFishingLvList() {
        return getMapper(StaticIniMapper.class).selectStaticFishingLvList();
    }

    public Map<Integer, StaticCrossGamePlayPlan> selectStaticCrossGamePlanList() {
        return getMapper(StaticIniMapper.class).selectStaticCrossGamePlanList();
    }

    public Map<Integer, StaticCrossGroup> selectStaticCrossGroup() {
        return getMapper(StaticIniMapper.class).selectStaticCrossGroup();
    }

    public Map<Integer, StaticCrossGamePlayPlan> selectStaticCrossGamePlanMap(Set<Integer> list) {
        return getMapper(StaticIniMapper.class).selectStaticCrossGamePlanMap(list);
    }
}
