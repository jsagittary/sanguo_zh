package com.gryphpoem.game.zw.resource.dao.sqlMap.s;

import com.gryphpoem.game.zw.resource.dao.handle.ElementServerTypeHandler;
import com.gryphpoem.game.zw.resource.dao.handle.ListIntTypeHandler;
import com.gryphpoem.game.zw.resource.dao.handle.ListListLongTypeHandler;
import com.gryphpoem.game.zw.resource.dao.handle.ListListTypeHandler;
import com.gryphpoem.game.zw.resource.dao.handle.MapIntTypeHandler;
import com.gryphpoem.game.zw.resource.domain.s.StaticActSkinEncore;
import com.gryphpoem.game.zw.resource.domain.s.StaticActTreasureWareJourney;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivityCrossPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticAnniversaryEgg;
import com.gryphpoem.game.zw.resource.domain.s.StaticAnniversaryTurntable;
import com.gryphpoem.game.zw.resource.domain.s.StaticAutumnTurnplate;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePvp;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacter;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacterReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticCreativeOffice;
import com.gryphpoem.game.zw.resource.domain.s.StaticCreativeOfficeAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGroup;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawCardWeight;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicCrop;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicOrder;
import com.gryphpoem.game.zw.resource.domain.s.StaticFireworks;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishBait;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishBaitHerocombination;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishProficiency;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishResults;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishattribute;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishing;
import com.gryphpoem.game.zw.resource.domain.s.StaticFishingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticGeneration;
import com.gryphpoem.game.zw.resource.domain.s.StaticHappiness;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyAttr;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroBiographyShow;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.domain.s.StaticMusicFestivalBoxOffice;
import com.gryphpoem.game.zw.resource.domain.s.StaticMusicFestivalBoxOfficeParam;
import com.gryphpoem.game.zw.resource.domain.s.StaticRandomLibrary;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelic;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelicFraction;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelicShop;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.domain.s.StaticSmallGame;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerCastle;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerCharge;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerTurnplate;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotem;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemDrop;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemLink;
import com.gryphpoem.game.zw.resource.domain.s.StaticTotemUp;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureCombatBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureWare;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureWareLevel;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureWareProfile;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureWareSpecial;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuffCross;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xwind
 * @date 2021/7/7
 */
public interface StaticIniMapper {

    @Select("select * from s_generation")
    @Results({
            @Result(column = "duration", property = "duration", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticGeneration> selectStaticGenerationList();

    @Select("select * from s_war_fire_buff_cross")
    List<StaticWarFireBuffCross> selectWarFireBuffCross();

    @Select("select * from s_small_game")
    @MapKey("id")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class),
            @Result(column = "extAward", property = "extAward", typeHandler = ListListTypeHandler.class),
    })
    Map<Integer, StaticSmallGame> selectSmallGameMap();

    @Select("select * from s_summer_charge")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class)
    })
    List<StaticSummerCharge> selectStaticSummerChargeList();

    @Select("select * from s_summer_turnplate")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class)
    })
    List<StaticSummerTurnplate> selectStaticSummerTurnplateList();

    @Select("select * from s_summer_castle")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class)
    })
    @MapKey("id")
    Map<Integer, StaticSummerCastle> selectStaticSummerCastleMap();

    @Select("select * from s_act_super_turnplate")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class)
    })
    List<StaticAnniversaryTurntable> selectStaticAnniversaryTurntableList();

    @Select("select * from s_anniversary_egg")
    List<StaticAnniversaryEgg> selectStaticAnniversaryEggList();

    @Select("select * from s_random_library")
    @Results({
            @Result(column = "lv", property = "lv", typeHandler = ListIntTypeHandler.class),
            @Result(column = "awardList", property = "awardList", typeHandler = ListListTypeHandler.class)
    })
    List<StaticRandomLibrary> selectStaticRandomLibraryList();

    @MapKey("id")
    @Select({"select * from s_act_skin_encore"})
    Map<Integer, StaticActSkinEncore> selectStaticActSkinEncore();

    @Select("select * from s_fish_bait_herocombination")
    @Results({
            @Result(column = "personnel", property = "personnel", typeHandler = ListListTypeHandler.class),
            @Result(column = "heroLV", property = "heroLV", typeHandler = ListIntTypeHandler.class),
            @Result(column = "collectionResult", property = "collectionResult", typeHandler = ListListTypeHandler.class)
    })
    List<StaticFishBaitHerocombination> selectStaticFishBaitHerocombinationList();

    @Select("select * from s_fish_bait")
    @Results({
            @Result(column = "fishID", property = "fishID", typeHandler = ListListTypeHandler.class)
    })
    List<StaticFishBait> selectStaticFishBaitList();

    @Select("select * from s_fish_results")
    @MapKey("colorID")
    Map<Integer, StaticFishResults> selectStaticFishResultsMap();

    @Select("select * from s_fish_attribute")
    @Results({
            @Result(column = "reward", property = "reward", typeHandler = ListListTypeHandler.class),
            @Result(column = "sizeChange", property = "sizeChange", typeHandler = ListIntTypeHandler.class)
    })
    @MapKey("fishID")
    Map<Integer, StaticFishattribute> selectStaticFishattributeMap();

    @Select("select * from s_fish_proficiency")
    @Results({
            @Result(column = "multipleCrit", property = "multipleCrit", typeHandler = ListIntTypeHandler.class),
            @Result(column = "sizeUP", property = "sizeUP", typeHandler = ListListTypeHandler.class),
            @Result(column = "goalUP", property = "goalUP", typeHandler = ListListTypeHandler.class)
    })
    List<StaticFishProficiency> selectStaticFishProficiencyList();

    @Select("select * from s_fish_shop")
    @Results({
            @Result(column = "awardList", property = "awardList", typeHandler = ListListTypeHandler.class),
            @Result(column = "expendProp", property = "expendProp", typeHandler = ListListTypeHandler.class)
    })
    @MapKey("id")
    Map<Integer, StaticFishShop> selectStaticFishShopMap();


    @Select("select * from s_activity_cross_plan")
    @Results({
            @Result(column = "serverId", property = "serverId", typeHandler = ListListTypeHandler.class)
    })
    List<StaticActivityCrossPlan> selectStaticActivityPlanCross();


    @Select("select * from s_autumn_turnplate")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class)
    })
    List<StaticAutumnTurnplate> selectStaticAutumnTurnplateList();

    @Select("select * from s_box_office")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class)
    })
    List<StaticMusicFestivalBoxOffice> selectStaticMusicFestivalBoxOfficeList();

    @Select("select * from s_box_office_param")
    @MapKey("activityId")
    @Results({
            @Result(column = "points", property = "points", typeHandler = ListIntTypeHandler.class),
            @Result(column = "payId", property = "payId", typeHandler = MapIntTypeHandler.class),
            @Result(column = "goldCost", property = "goldCost", typeHandler = MapIntTypeHandler.class),
    })
    Map<Integer, StaticMusicFestivalBoxOfficeParam> selectStaticMusicFestivalBoxOfficeParamMap();

    @Select("select * from s_creative_office")
    @MapKey("id")
    @Results({
            @Result(column = "award", property = "awardList", typeHandler = ListListTypeHandler.class),
            @Result(column = "param", property = "params", typeHandler = ListIntTypeHandler.class)
    })
    Map<Integer, StaticCreativeOffice> selectStaticCreativeOffice();

    @Select("select * from s_creative_office_award")
    @MapKey("id")
    @Results({
            @Result(column = "param", property = "params", typeHandler = ListIntTypeHandler.class),
            @Result(column = "award", property = "awards", typeHandler = ListListTypeHandler.class)
    })
    Map<Integer, StaticCreativeOfficeAward> selectStaticCreativeOfficeAward();

    @Select("select * from s_totem")
    @Results({
            @Result(column = "beakNeed", property = "beakNeed", typeHandler = ListListTypeHandler.class),
    })
    List<StaticTotem> selectStaticTotemList();

    @Select("select * from s_totem_link")
    @Results({
            @Result(column = "attr", property = "attr", typeHandler = MapIntTypeHandler.class),
    })
    List<StaticTotemLink> selectStaticTotemLinkList();

    @Select("select * from s_totem_drop")
    @Results({
            @Result(column = "range", property = "range", typeHandler = ListIntTypeHandler.class),
            @Result(column = "group", property = "group", typeHandler = ListListTypeHandler.class),
    })
    List<StaticTotemDrop> selectStaticTotemDropList();

    @Select("select * from s_totem_up")
    @Results({
            @Result(column = "attr1", property = "attr1", typeHandler = MapIntTypeHandler.class),
            @Result(column = "attr2", property = "attr2", typeHandler = MapIntTypeHandler.class),
            @Result(column = "attr3", property = "attr3", typeHandler = MapIntTypeHandler.class),
            @Result(column = "attr4", property = "attr4", typeHandler = MapIntTypeHandler.class),
            @Result(column = "attr5", property = "attr5", typeHandler = MapIntTypeHandler.class),
            @Result(column = "attr6", property = "attr6", typeHandler = MapIntTypeHandler.class),
            @Result(column = "attr7", property = "attr7", typeHandler = MapIntTypeHandler.class),
            @Result(column = "attr8", property = "attr8", typeHandler = MapIntTypeHandler.class),
            @Result(column = "upNeed", property = "upNeed", typeHandler = ListListTypeHandler.class),
            @Result(column = "analysis", property = "analysis", typeHandler = ListListTypeHandler.class),
    })
    List<StaticTotemUp> selectStaticTotemUpList();

    @Select("select * from s_treasure_make")
    @MapKey("id")
    @Results({
            @Result(column = "consume", property = "consume", typeHandler = ListListTypeHandler.class),
            @Result(column = "typeProb", property = "typeProb", typeHandler = ListListTypeHandler.class),
            @Result(column = "numProb", property = "numProb", typeHandler = ListListTypeHandler.class),
            @Result(column = "attrInit", property = "attrInit", typeHandler = ListListTypeHandler.class),
            @Result(column = "specialProb", property = "specialProb", typeHandler = ListListTypeHandler.class),
            @Result(column = "mini", property = "mini", typeHandler = ListIntTypeHandler.class)
    })
    Map<Integer, StaticTreasureWare> selectStaticTreasureWareMake();

    @Select("select * from s_treasure_special")
    @MapKey("id")
    @Results({
            @Result(column = "attrSpecial", property = "attrSpecial", typeHandler = ListListTypeHandler.class),
            @Result(column = "resolve", property = "resolve", typeHandler = ListListTypeHandler.class)
    })
    Map<Integer, StaticTreasureWareSpecial> selectStaticTreasureWareSpecial();

    @Select("select * from s_treasure_level")
    @Results({
            @Result(column = "consume", property = "consume", typeHandler = ListListTypeHandler.class),
            @Result(column = "attr", property = "attr", typeHandler = MapIntTypeHandler.class),
            @Result(column = "resolve", property = "resolve", typeHandler = ListListTypeHandler.class)
    })
    List<StaticTreasureWareLevel> selectStaticTreasureWareLevel();

    @Select("select * from s_treasure_combat")
    @MapKey("combatId")
    @Results({
            @Result(column = "minuteAward", property = "minuteAward", typeHandler = ListListTypeHandler.class),
            @Result(column = "minuteRandomAward", property = "minuteRandomAward", typeHandler = ListListTypeHandler.class),
            @Result(column = "form", property = "form", typeHandler = ListIntTypeHandler.class),
            @Result(column = "firstAward", property = "firstAward", typeHandler = ListListTypeHandler.class),
            @Result(column = "sectionAward", property = "sectionAward", typeHandler = ListListTypeHandler.class)
    })
    Map<Integer, StaticTreasureCombat> selectStaticTreasureCombat();

    @Select("select * from s_treasure_combat_buff")
    @Results({
            @Result(column = "attr", property = "attr", typeHandler = MapIntTypeHandler.class),
    })
    List<StaticTreasureCombatBuff> selectTreasureCombatBuffs();

    @Select("select * from s_treasure_profile")
    List<StaticTreasureWareProfile> selectTreasureWareProfile();

    @Select("select * from s_fireworks")
    @MapKey("id")
    @Results({
            @Result(column = "cost", property = "cost", typeHandler = ListListTypeHandler.class)
    })
    Map<Integer, StaticFireworks> selectStaticFireworksMap();

    @Select("select * from s_fireworks")
    @Results({
            @Result(column = "cost", property = "cost", typeHandler = ListListTypeHandler.class)
    })
    List<StaticFireworks> selectStaticFireworksList();

    @Select("select * from s_fishing")
    List<StaticFishing> selectStaticFishingList();

    @Select("select * from s_fishing_lv")
    @Results({
            @Result(column = "fishId", property = "fishId", typeHandler = ListListTypeHandler.class),
            @Result(column = "wave", property = "wave", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticFishingLv> selectStaticFishingLvList();

    @Select("select * from s_gameplay_cross_plan")
    @MapKey("keyId")
    Map<Integer, StaticCrossGamePlayPlan> selectStaticCrossGamePlanList();

    @Select("select * from s_group_cross")
    @MapKey("group")
    @Results({
            @Result(column = "red", property = "red", typeHandler = ElementServerTypeHandler.class),
            @Result(column = "yellow", property = "yellow", typeHandler = ElementServerTypeHandler.class),
            @Result(column = "blue", property = "blue", typeHandler = ElementServerTypeHandler.class)
    })
    Map<Integer, StaticCrossGroup> selectStaticCrossGroup();

    @Select({"<script>", "select", " * ", "FROM s_gameplay_cross_plan",
            "WHERE  `group` IN  " +
                    "<foreach item='item' index='index' collection='list' open='(' separator=',' close=')'> #{item} </foreach>" +
                    "</script>"})
    @MapKey("keyId")
    Map<Integer, StaticCrossGamePlayPlan> selectStaticCrossGamePlanMap(@Param("list") Set<Integer> list);

    @MapKey("diff")
    @Select("select * from s_pvp_battle")
    Map<Integer, StaticBattlePvp> selectStaticBattlePvpMap();

    @MapKey("id")
    @Results({
            @Result(column = "serverIdList", property = "serverIdList", typeHandler = ListListTypeHandler.class)
    })
    @Select("select * from s_hero_search_plan")
    Map<Integer, StaticDrawHeoPlan> selectStaticDrawHeoPlanMap();

    @Results({
            @Result(column = "weight", property = "weight", typeHandler = ListListTypeHandler.class),
            @Result(column = "serverId", property = "serverIdList", typeHandler = ListListTypeHandler.class)
    })
    @Select("select * from s_hero_search_weight")
    List<StaticDrawCardWeight> selectStaticDrawCardWeightList();

    @Select("select * from s_act_treasure_ware_journey")
    @Results({
            @Result(column = "awardList", property = "awardList", typeHandler = ListListTypeHandler.class),
            @Result(column = "param", property = "params", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticActTreasureWareJourney> selectStaticActTreasureWareJourney();

    @Select("select * from s_hero_biography_attr")
    @Results({
            @Result(column = "attr", property = "attr", typeHandler = ListListTypeHandler.class)
    })
    List<StaticHeroBiographyAttr> selectStaticHeroBiographyAttrList();

    @MapKey("id")
    @Select("select * from s_hero_biography_show")
    @Results({
            @Result(column = "heroId", property = "heroId", typeHandler = ListIntTypeHandler.class)
    })
    Map<Integer, StaticHeroBiographyShow> selectStaticHeroBiographyShowMap();

    @Select("select * from s_relic")
    @Results({
            @Result(column = "era", property = "era", typeHandler = ListIntTypeHandler.class),
            @Result(column = "area", property = "area", typeHandler = ListListTypeHandler.class)
    })
    List<StaticRelic> selectStaticRelicList();

    @Select("select * from s_relic_shop")
    @Results({
            @Result(column = "award", property = "award", typeHandler = ListListTypeHandler.class),
            @Result(column = "partyAward", property = "partyAward", typeHandler = ListListTypeHandler.class),
            @Result(column = "area", property = "area", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticRelicShop> selectStaticRelicShopList();

    @Select("select * from s_relic_fraction")
    @Results({
            @Result(column = "area", property = "area", typeHandler = ListIntTypeHandler.class)
    })
    @MapKey("id")
    Map<Integer, StaticRelicFraction> selectStaticRelicFractionMap();

    @Select("select * from sim_choose")
    @Results({
            @Result(column = "reward", property = "rewardList", typeHandler = ListListTypeHandler.class),
            @Result(column = "miniGame", property = "miniGame", typeHandler = ListIntTypeHandler.class),
            @Result(column = "buff", property = "buff", typeHandler = ListListTypeHandler.class),
            @Result(column = "characterFix", property = "characterFix", typeHandler = ListListTypeHandler.class)
    })
    List<StaticSimulatorChoose> selectStaticSimulatorChooseList();

    @Select("select * from sim_beginnerguide")
    @Results({
            @Result(column = "choose", property = "choose", typeHandler = ListListLongTypeHandler.class),
            @Result(column = "delay", property = "delay", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticSimulatorStep> selectStaticSimulatorStepList();

    @Select("select * from sim_lord_character")
    @Results({
            @Result(column = "range", property = "range", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticCharacter> selectStaticCharacterList();

    @Select("select * from sim_character_reward")
    @Results({
            @Result(column = "need", property = "need", typeHandler = ListListTypeHandler.class),
            @Result(column = "reward", property = "reward", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticCharacterReward> selectStaticCharacterRewardList();

    @Select("select * from sim_city")
    @Results({
            @Result(column = "buildLv", property = "buildLv", typeHandler = ListListTypeHandler.class),
            @Result(column = "open", property = "open", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticSimCity> selectStaticSimCityList();

    @Select("select * from s_homecity_cell")
    @Results({
            @Result(column = "route", property = "route", typeHandler = ListIntTypeHandler.class),
            @Result(column = "bindCell", property = "bindCellList", typeHandler = ListIntTypeHandler.class),
            @Result(column = "neighbor", property = "neighborCellList", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticHomeCityCell> selectStaticHomeCityCellList();

    @Select("select * from s_homecity_foundation")
    @Results({
            @Result(column = "cell", property = "cellList", typeHandler = ListIntTypeHandler.class),
            @Result(column = "buildType", property = "buildType", typeHandler = ListIntTypeHandler.class)
    })
    List<StaticHomeCityFoundation> selectStaticHomeCityFoundationList();

    @Select("select * from s_economic_order")
    @Results({
            @Result(column = "needLordLv", property = "needLordLv", typeHandler = ListIntTypeHandler.class),
            @Result(column = "place", property = "place", typeHandler = ListIntTypeHandler.class),
            @Result(column = "orderDemand1", property = "orderDemand1", typeHandler = ListListTypeHandler.class),
            @Result(column = "orderDemand2", property = "orderDemand2", typeHandler = ListListTypeHandler.class),
            @Result(column = "orderDemand3", property = "orderDemand3", typeHandler = ListListTypeHandler.class),
            @Result(column = "reward1", property = "reward1", typeHandler = ListListTypeHandler.class),
            @Result(column = "number1", property = "number1", typeHandler = ListIntTypeHandler.class),
            @Result(column = "reward2", property = "reward2", typeHandler = ListListTypeHandler.class),
            @Result(column = "number2", property = "number2", typeHandler = ListIntTypeHandler.class),
            @Result(column = "specialReward", property = "specialReward", typeHandler = ListListTypeHandler.class)
    })
    List<StaticEconomicOrder> selectStaticEconomicOrderList();

    @Select("select * from s_economic_crop")
    @Results({
            @Result(column = "needBuildingLv", property = "needBuildingLv", typeHandler = MapIntTypeHandler.class)
    })
    List<StaticEconomicCrop> selectStaticEconomicCropList();

    @Select("select * from s_happiness")
    @Results({
            @Result(column = "range", property = "range", typeHandler = ListIntTypeHandler.class),
            @Result(column = "effective", property = "effective", typeHandler = ListListTypeHandler.class)
    })
    List<StaticHappiness> selectStaticHappinessList();
}
