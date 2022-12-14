package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonWeekIntegralService;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.pb.GamePb2.SyncChangeInfoRs;
import com.gryphpoem.game.zw.pb.GamePb3.SyncChatRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.chat.Chat;
import com.gryphpoem.game.zw.resource.pojo.chat.SystemChat;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.season.SeasonTalent;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.resource.util.random.RewardRandomUtil;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;
import com.gryphpoem.game.zw.service.activity.MusicFestivalCreativeService;
import com.gryphpoem.game.zw.service.fish.FishingService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;
import com.gryphpoem.game.zw.service.totem.TotemService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ????????????
 * <p>
 * 1????????????: send ??????????????????????????????????????????; add ????????????????????????????????????
 * <p>
 * 2 ????????? Object... param ?????????????????????,?????????????????????????????????????????????
 *
 * @author TanDonghai
 * @ClassName RewardDataManager.java
 * @Description ???????????????????????????
 * @date ???????????????2017???3???28??? ??????2:55:28
 */
@Component
public class RewardDataManager {

    private static final int MENTOR_EQUIP_CNT = 500;
    @Autowired
    ActivityTriggerService activityTriggerService;
    @Autowired
    CastleSkinProcessService castleSkinProcessService;
    @Autowired
    private RankDataManager rankDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private WarPlaneDataManager warPlaneDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private MentorDataManager mentorDataManager;
    @Autowired
    private CampService campService;
    @Autowired
    private WorldWarSeasonDailyRestrictTaskService restrictTaskService;
    @Autowired
    private WorldWarSeasonWeekIntegralService worldWarSeasonWeekIntegralService;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private ActivityRobinHoodService activityRobinHoodService;
    @Autowired
    private ActivityDiaoChanService activityDiaoChanService;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private DressUpDataManager dressUpDataManager;
    @Autowired
    private FishingService fishingService;
    @Autowired
    private MusicFestivalCreativeService musicFestivalCreativeService;
    @Autowired
    private TitleService titleService;
    @Autowired
    private DrawCardService drawCardService;
    @Autowired
    private TreasureWareService treasureWareService;
    @Autowired
    private HeroService heroService;

    /**
     * ????????????
     *
     * @param awardList
     * @return ??????null
     */
    public static List<List<Integer>> mergeAward(List<List<Integer>> awardList) {
        if (CheckNull.isEmpty(awardList)) {
            return null;
        }
        Map<String, List<Integer>> awardMap = new HashMap<>();
        for (List<Integer> award : awardList) {
            if (award.size() >= 3) {
                String key = award.get(0) + "_" + award.get(1);
                if (awardMap.containsKey(key)) {
                    List<Integer> aw = awardMap.get(key);
                    int mSize = aw.get(2) + award.get(2);
                    aw.set(2, mSize);
                    awardMap.put(key, aw);
                } else {
                    List<Integer> a = new ArrayList<>(award);
                    awardMap.put(key, a);
                }
            }
        }
        return awardMap.values().stream().collect(Collectors.toList());
    }

    /**
     * ????????????
     *
     * @param awardList
     * @return
     */
    public static List<Award> mergeAward2(List<Award> awardList) {
        if (CheckNull.isEmpty(awardList)) {
            return null;
        }
        Map<String, Integer> awardMap = awardList.stream()
                .collect(Collectors.toMap((award) -> award.getType() + "_" + award.getId(), Award::getCount, Integer::sum));
        if (CheckNull.isEmpty(awardMap)) {
            return null;
        }
        return awardMap.entrySet().stream().map((en) -> {
            String[] split = en.getKey().split("_");
            int count = en.getValue();
            return PbHelper.createAwardPb(Integer.parseInt(split[0]), Integer.parseInt(split[1]), count);
        }).collect(Collectors.toList());
    }

    @SafeVarargs
    public static List<List<Integer>> mergeAward(List<List<Integer>>... awards) {
        Map<String, List<Integer>> awardMap = new HashMap<>();
        for (List<List<Integer>> awardList : awards) {
            for (List<Integer> award : awardList) {
                if (award.size() >= 3) {
                    String key = award.get(0) + "_" + award.get(1);
                    if (awardMap.containsKey(key)) {
                        List<Integer> aw = awardMap.get(key);
                        int mSize = aw.get(2) + award.get(2);
                        aw.set(2, mSize);
                        awardMap.put(key, aw);
                    } else {
                        List<Integer> a = new ArrayList<>(award);
                        awardMap.put(key, a);
                    }
                }
            }
        }
        return new ArrayList<>(awardMap.values());
    }

    /**
     * ????????????(???????????????)
     *
     * @param player
     * @param rewardList
     * @param from
     * @param param
     * @return
     */
    public List<CommonPb.Award> sendReward(Player player, List<List<Integer>> rewardList, AwardFrom from,
                                           Object... param) {
        return sendReward(player, rewardList, 1, from, param);
    }

    /**
     * ???????????? ?????????
     *
     * @param player
     * @param rewardList
     * @param num
     * @param from
     * @param param
     * @return
     */
    public List<CommonPb.Award> sendReward(Player player, List<List<Integer>> rewardList, int num, AwardFrom from,
                                           Object... param) {
        if (CheckNull.isEmpty(rewardList)) {
            return null;
        }

        ChangeInfo change = ChangeInfo.newIns();
        List<CommonPb.Award> awards = new ArrayList<>();
        for (List<Integer> list : rewardList) {
            if (CheckNull.isEmpty(list) || list.size() < 3) {
                LogUtil.error("??????????????????????????????????????????????????????, list:" + list);
                continue;
            }
            Award addAwardSignle = addAwardSignle(player, list, num, from, param);
            if (addAwardSignle == null) {
                continue;
            }
            awards.add(addAwardSignle);
            // ??????????????????????????????????????????
            change.addChangeType(addAwardSignle.getType(), addAwardSignle.getId());
        }

        // ????????????????????????????????????
        syncRoleResChanged(player, change);
        return awards;
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @return
     */
    public CommonPb.Award sendRewardSignle(Player player, int type, int id, int count, AwardFrom from,
                                           Object... param) {
        CommonPb.Award award = addAwardSignle(player, type, id, count, from, param);
        if (award != null) {
            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(award.getType(), award.getId());
            syncRoleResChanged(player, change);
        }
        return award;
    }

    /**
     * ????????????????????????(???????????? ChangeInfo ?????????null,?????????????????????????????????????????????)
     *
     * @param player
     * @param rewardList
     * @param change
     * @param from
     * @return
     */
    public List<CommonPb.Award> addAwardDelaySync(Player player, List<List<Integer>> rewardList, ChangeInfo change,
                                                  AwardFrom from, Object... param) {
        if (CheckNull.isEmpty(rewardList)) {
            return Collections.EMPTY_LIST;
        }
        List<CommonPb.Award> awards = new ArrayList<>();
        for (List<Integer> list : rewardList) {
            if (CheckNull.isEmpty(list) || list.size() < 3) {
                LogUtil.error("??????????????????????????????????????????????????????, list:" + list);
                continue;
            }
            Award addAwardSignle = addAwardSignle(player, list, 1, from, param);
            if (addAwardSignle == null) {
                continue;
            }
            awards.add(addAwardSignle);
            if (change != null) {
                // ??????????????????????????????????????????
                change.addChangeType(list.get(0), list.get(1));
            }
        }
        return awards;
    }

    /**
     * ??????CommonPb.Award????????????????????????
     *
     * @param player
     * @param awardList
     * @param from
     * @param param
     */
    public void sendRewardByAwardList(Player player, List<CommonPb.Award> awardList, AwardFrom from, Object... param) {
        if (null == player || CheckNull.isEmpty(awardList)) {
            return;
        }
        ChangeInfo change = ChangeInfo.newIns();
        for (Award award : awardList) {
            addAward(player, award.getType(), award.getId(), award.getCount(), from, param);
            // ??????????????????????????????????????????
            change.addChangeType(award.getType(), award.getId());
        }
        // ????????????????????????????????????
        syncRoleResChanged(player, change);
    }

    /**
     * ??????????????????(??????????????????,???????????????)
     *
     * @param player
     * @param rewardList
     * @param from
     */
    public CommonPb.Award addAwardSignle(Player player, List<Integer> rewardList, AwardFrom from, Object... param) {
        return addAwardSignle(player, rewardList, 1, from, param);
    }

    /**
     * ??????????????????(??????????????????,?????????)
     *
     * @param player
     * @param rewardList
     * @param num        ??????
     * @param from
     * @return
     */
    public CommonPb.Award addAwardSignle(Player player, List<Integer> rewardList, int num, AwardFrom from,
                                         Object... param) {
        if (null == player || CheckNull.isEmpty(rewardList)) {
            return null;
        }
        if (rewardList.size() < 3) {
            return null;
        }
        int type = rewardList.get(0);
        int id = rewardList.get(1);
        int count = rewardList.get(2) * num;
        // ??????4???????????????????????????
        if (rewardList.size() == 4) {
            if (!RandomHelper.isHitRangeIn10000(rewardList.get(3))) {
                return null;
            }
        }
        return addAwardSignle(player, type, id, count, from, param);
    }

    /**
     * ????????????????????????(??????????????????)
     *
     * @param player
     * @param type   ??????
     * @param id
     * @param count  ??????
     * @param from
     * @param param
     * @return null??????????????????
     */
    public CommonPb.Award addAwardSignle(Player player, int type, int id, int count, AwardFrom from, Object... param) {
        if (type == AwardType.RANDOM) {
            return addRandomAward(player, id, count, from);
        } else {
//            int keyId = addAward(player, type, id, count, from, param);
//            return PbHelper.createAwardPb(type, id, count, keyId);
            return addAwardBase_(player, type, id, count, from, param);
        }
    }

    /**
     * ????????????????????????,?????????key???(?????????????????????)
     *
     * @param player ??????
     * @param type   ???????????????AwardType?????????????????????
     * @param id     ?????????????????????id
     * @param count  ??????
     * @param from   ?????????????????????????????????
     * @return
     */
    public int addAward(Player player, int type, int id, int count, AwardFrom from, Object... param) {
        return addAwardBase(player, type, id, count, from, param);
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param keyId
     * @param from
     * @param param
     * @return
     */
    public int addMailAward(Player player, int type, int id, int count, int keyId, AwardFrom from, Object... param) throws MwException {
        return addAwardBase_(player, type, id, count, keyId, from, param).getKeyId();
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @param param
     * @return ?????????key???
     */
    private Award addAwardBase_(Player player, int type, int id, int count, int keyId, AwardFrom from, Object... param) throws MwException {
        Award.Builder award = Award.newBuilder().setType(type).setId(id).setCount(count);
        // ??????
        List<Award> convert = new ArrayList<>();
        switch (type) {
            case AwardType.MONEY:
            case AwardType.MONEY_PECENT:
                addMoney(player, id, count, from, param);
                break;
            case AwardType.RESOURCE:
                modifyResource(player, id, count, 0, from, param);
                break;
            case AwardType.HERO:
                addHero(player, id, from, param);
                break;
            case AwardType.HERO_DESIGNATED_GRADE:
                addHeroDesignatedGrade(player, id, count, from, param);
                break;
            case AwardType.PROP:
                addProp(player, id, (int) count, from, param);
                break;
            case AwardType.EQUIP:
                List<Integer> keyIds = addEquip(player, id, count, from, param);
                if (!CheckNull.isEmpty(keyIds)) {
                    award.setKeyId(keyIds.get(0));
                }
                break;
            case AwardType.ARMY:
                addArmyAward(player, id, count, from, param);
                break;
            case AwardType.SPECIAL:
                addSpecialAward(player, id, (int) count, from, param);
                break;
            case AwardType.PORTRAIT:
                // ??????
                addPortrait(player, id, count, convert, from, param);
                break;
            case AwardType.STONE:
                // ????????????
                addStone(player, id, (int) count, from, param);
                break;
            case AwardType.CHAT_BUBBLE:
                // ???????????????
                addChatBubble(player, id, count, convert, from, param);
                break;
            case AwardType.PLANE:
                // ??????
                addPlane(player, id, from, param);
                break;
            case AwardType.PLANE_CHIP:
                // ????????????
                addPlaneChip(player, id, count, from, param);
                break;
            case AwardType.MEDAL:
                // ??????
                addMedal(player, id, 0, count, from, param);
                break;
            case AwardType.MENTOR_EQUIP:
                // ????????????
                addMentorEquip(player, id, count, from, param);
                break;
            case AwardType.JEWEL:
                // ????????????
                addEquipJewel(player, id, count, from, param);
                break;
            case AwardType.CASTLE_SKIN:
                // ????????????
                addCastleSkin(player, id, count, convert, from, param);
                break;
            case AwardType.SANDTABLE_SCORE:
                // ????????????
                addSandTableScore(player, count, from, param);
                break;
            case AwardType.PORTRAIT_FRAME:
                // ?????????
                addPortraitFrame(player, id, count, convert, from, param);
                break;
            case AwardType.NAMEPLATE:
                // ??????
                addNameplate(player, id, count, convert, from, param);
                break;
            case AwardType.MARCH_SPECIAL_EFFECTS:
                // ????????????
                addMarchLine(player, id, count, convert, from, param);
                break;
            case AwardType.TREASURE_WARE:
                //????????????
                addTreasureWareInMail(player, id, count, keyId, from, param);
                break;
            //????????????
            case AwardType.TITLE:
                addTitle(player, id, count, convert, from, param);
                break;
            // ??????????????????
            case AwardType.HERO_FRAGMENT:
                operationHeroFragment(player, id, count, from, true, false, param);
                break;
            default:
                break;
        }
        // ??????????????????
        if (!convert.isEmpty()) {
            award.addAllConvert(convert);
        }

        return award.build();
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     * @param operation
     * @param param
     */
    private void operationSystemCount(Player player, int id, int count, AwardFrom from, boolean operation, Object... param) {
        if (count == 0)
            return;

        switch (id) {
            case AwardType.Special.DRAW_PERMANENT_CARD_COUNT:
                operationDrawPermanentCardCount(player, id, count, from, operation, param);
                break;
            default:
                break;
        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     * @param operation
     * @param param
     */
    private void operationDrawPermanentCardCount(Player player, int id, int count, AwardFrom from, boolean operation, Object... param) {
        if (count == 0)
            return;
        count = Math.abs(count);
        count = operation ? count : -count;
        player.getDrawCardData().setOtherFreeCount(player.getDrawCardData().getOtherFreeCount() + count);
    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     * @param operation
     * @param param
     */
    private void operationHeroFragment(Player player, int id, int count, AwardFrom from, boolean operation, boolean sync, Object... param) {
        if (count == 0)
            return;

        count = Math.abs(count);
        count = operation ? count : -count;
        player.getDrawCardData().getFragmentData().merge(id, count, Integer::sum);
        if (sync) {
            ChangeInfo changeInfo = ChangeInfo.newIns();
            changeInfo.addChangeType(AwardType.HERO_FRAGMENT, id);
            syncRoleResChanged(player, changeInfo);
        }

        // ??????????????????
        int curCount = player.getDrawCardData().getFragmentData().get(id);
        LogLordHelper.heroFragment(
                from,
                player.account,
                player.lord,
                id,
                count > 0 ? Constant.ACTION_ADD : Constant.ACTION_SUB,
                count,
                curCount,
                curCount
        );
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param id
     * @param gradeKeyId
     * @param from
     * @param operation
     * @param sync
     * @param param
     */
    private void operationDesignatedHeroGradeFragment(Player player, int id, int gradeKeyId, AwardFrom from, boolean operation, boolean sync, Object... param) {
        if (gradeKeyId <= 0)
            return;
        Integer costFragment = StaticHeroDataMgr.heroUpgradeCostFragment(id, gradeKeyId);
        if (CheckNull.isNull(costFragment)) {
            LogUtil.error(String.format("heroId:%d, gradeKeyId:%d, found no fragment", id, gradeKeyId));
            return;
        }

        costFragment = Math.abs(costFragment);
        costFragment = operation ? costFragment + HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS :
                -costFragment - HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS;
        player.getDrawCardData().getFragmentData().merge(id, costFragment, Integer::sum);
        if (sync) {
            ChangeInfo changeInfo = ChangeInfo.newIns();
            changeInfo.addChangeType(AwardType.HERO_FRAGMENT, id);
            syncRoleResChanged(player, changeInfo);
        }

        // ??????????????????
        int curCount = player.getDrawCardData().getFragmentData().get(id);
        LogLordHelper.heroFragment(
                from,
                player.account,
                player.lord,
                id,
                costFragment > 0 ? Constant.ACTION_ADD : Constant.ACTION_SUB,
                costFragment,
                curCount,
                curCount
        );
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @param param
     * @return ?????????key???
     */
    private int addAwardBase(Player player, int type, int id, int count, AwardFrom from, Object... param) {
        return addAwardBase_(player, type, id, count, from, param).getKeyId();
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @param param
     * @return ?????????key???
     */
    private Award addAwardBase_(Player player, int type, int id, int count, AwardFrom from, Object... param) {
        Award.Builder award = Award.newBuilder().setType(type).setId(id).setCount(count);
        // ??????
        List<Award> convert = new ArrayList<>();
        switch (type) {
            case AwardType.MONEY:
            case AwardType.MONEY_PECENT:
                addMoney(player, id, count, from, param);
                break;
            case AwardType.RESOURCE:
                modifyResource(player, id, count, 0, from, param);
                break;
            case AwardType.HERO:
                addHero(player, id, from, param);
                break;
            case AwardType.HERO_DESIGNATED_GRADE:
                addHeroDesignatedGrade(player, id, count, from, param);
                break;
            case AwardType.PROP:
                addProp(player, id, (int) count, from, param);
                break;
            case AwardType.EQUIP:
                List<Integer> keyIds = addEquip(player, id, count, from, param);
                if (!CheckNull.isEmpty(keyIds)) {
                    award.setKeyId(keyIds.get(0));
                }
                break;
            // case AwardType.RANDOM:
            // addRandomAward(player, id, count, from);
            // break;
            case AwardType.ARMY:
                addArmyAward(player, id, count, from, param);
                break;
            case AwardType.SPECIAL:
                addSpecialAward(player, id, (int) count, from, param);
                break;
            case AwardType.PORTRAIT:
                // ??????
                addPortrait(player, id, count, convert, from, param);
                break;
            case AwardType.STONE:
                // ????????????
                addStone(player, id, (int) count, from, param);
                break;
            case AwardType.CHAT_BUBBLE:
                // ???????????????
                addChatBubble(player, id, count, convert, from, param);
                break;
            case AwardType.PLANE:
                // ??????
                addPlane(player, id, from, param);
                break;
            case AwardType.PLANE_CHIP:
                // ????????????
                addPlaneChip(player, id, count, from, param);
                break;
            case AwardType.MEDAL:
                // ??????
                addMedal(player, id, 0, count, from, param);
                break;
            case AwardType.MENTOR_EQUIP:
                // ????????????
                addMentorEquip(player, id, count, from, param);
                break;
            case AwardType.JEWEL:
                // ????????????
                addEquipJewel(player, id, count, from, param);
                break;
            case AwardType.CASTLE_SKIN:
                // ????????????
                addCastleSkin(player, id, count, convert, from, param);
                break;
            case AwardType.SANDTABLE_SCORE:
                // ????????????
                addSandTableScore(player, count, from, param);
                break;
            case AwardType.PORTRAIT_FRAME:
                // ?????????
                addPortraitFrame(player, id, count, convert, from, param);
                break;
            case AwardType.NAMEPLATE:
                // ??????
                addNameplate(player, id, count, convert, from, param);
                break;
            case AwardType.MARCH_SPECIAL_EFFECTS:
                // ????????????
                addMarchLine(player, id, count, convert, from, param);
                break;
            case AwardType.TREASURE_WARE:
                //????????????
                List<TreasureWare> treasureWareIds = addTreasureWare(player, id, count, from, param);
                if (!CheckNull.isEmpty(treasureWareIds)) {
                    award.setKeyId(treasureWareIds.get(0).getKeyId());
                    award.addAllParam(treasureWareIds.stream().map(TreasureWare::getProfileId).collect(Collectors.toList()));
                    award.addAllExtParam(treasureWareIds.stream().map(tw -> Objects.isNull(tw.getSpecialId()) ? 0 : tw.getSpecialId()).collect(Collectors.toList()));
                }
                break;
            case AwardType.TOTEM:
                addTotem(player, id, count, from, param);
                break;
            case AwardType.TITLE:
                addTitle(player, id, count, convert, from, param);
                break;
            case AwardType.HERO_FRAGMENT:
                operationHeroFragment(player, id, count, from, true, false, param);
                break;
            default:
                break;
        }
        // ??????????????????
        if (!convert.isEmpty()) {
            award.addAllConvert(convert);
        }

        return award.build();
    }

    private void addTitle(Player player, int id, int count, List<Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("??????id??????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }

        StaticTitle title = StaticLordDataMgr.getTitleMapById(id);
        if (null == title) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.TITLE, id, count, convert, from, param);
        // ???????????????????????????????????????
        CalculateUtil.reCalcAllHeroAttr(player);
    }

    @Autowired
    private TotemService totemService;

    private void addTotem(Player player, int id, int count, AwardFrom awardFrom, Object... params) {
        totemService.addTotemsAndSync(player, ListUtils.createItems(0, id, count), awardFrom);
    }

//    private void addTotemChip(Player player,int totemChipId,int count,AwardFrom awardFrom,Object...params){
//        if(Objects.isNull(player) || totemChipId <= 0 || count <= 0 || Objects.isNull(awardFrom)){
//            LogUtil.error("??????????????????????????????",player,totemChipId,count,awardFrom);
//            return;
//        }
//        StaticProp staticProp = StaticPropDataMgr.getPropMap().get(totemChipId);
//        if (null == staticProp) {
//            LogUtil.error("??????????????????id????????????????????????, roleId:", player.roleId, ", propId:", totemChipId, ", from:", awardFrom.getCode());
//            return;
//        }
//        player.getTotemData().newTotemChip(totemChipId,count);
//        int curCount = (int) getRoleResByType(player,AwardType.TOTEM_CHIP,totemChipId);
//        LogLordHelper.prop(awardFrom,player.account,player.lord,totemChipId,curCount,count,1,params);
//    }

    /**
     * ??????????????????(????????????????????????????????????????????????)
     *
     * @param player
     * @param treasureWareId
     * @param count
     * @param from
     * @param param
     * @return
     */
    private List<TreasureWare> addTreasureWare(Player player, int treasureWareId, int count, AwardFrom from, Object... param) {
        List<TreasureWare> treasureWares = DataResource.getBean(TreasureWareService.class).
                getTreasureWare(player, treasureWareId, count, TimeHelper.getCurrentSecond(), from, param);
        if (ObjectUtils.isEmpty(treasureWares)) {
            LogUtil.error("???????????????????????????, treasureWareId: ", treasureWareId, ", lordId: ", player.lord.getLordId());
            return null;
        }

        return treasureWares;
    }

    /**
     * ??????????????????(????????????????????????????????????)
     *
     * @param player
     * @param treasureWareId
     * @param from
     * @param param
     */
    private void addTreasureWareInMail(Player player, int treasureWareId, int count, int keyId, AwardFrom from, Object... param) throws MwException {
        StaticTreasureWare staticTreasureWare = StaticTreasureWareDataMgr.getStaticTreasureWare(treasureWareId);
        if (CheckNull.isNull(staticTreasureWare)) {
            LogUtil.error("?????????????????????, staticTreasureWareId: ", treasureWareId,
                    ", lordId: ", player.lord.getLordId(), ", from:", from.getCode());
            return;
        }
        int remainBagCnt = DataResource.getBean(TreasureWareService.class).remainBagCnt(player);
        if (remainBagCnt < count) {
            throw new MwException(GameError.MAX_TREASURE_WARE_STORE.getCode(), "??????????????????, roleId: " + player.roleId + ", remainBagCnt= "
                    + remainBagCnt + ", bagCnt: " + player.common.getTreasureWareCnt() + ", cnt= " + count);
        }

        TreasureWare treasureWare = keyId > 0 ? player.treasureWares.get(keyId) : null;
        if (CheckNull.isNull(treasureWare) || treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_MAIL) {
            LogUtil.error("?????????????????????, staticTreasureWareId: ", treasureWareId,
                    ", lordId: ", player.lord.getLordId(), ", keyId: ", keyId,
                    ", from:", from.getCode(), ", status: ", treasureWare == null ? -1 : treasureWare.getStatus());
            throw new MwException(GameError.NOT_OWNED_TREASURE_WARE.getCode(), "?????????????????????, staticTreasureWareId: ", treasureWareId,
                    ", lordId: ", player.lord.getLordId(), ", keyId: ", keyId,
                    ", from:", from.getCode(), ", status: ", treasureWare == null ? -1 : treasureWare.getStatus());
        }

        treasureWare.setStatus(TreasureWareConst.TREASURE_IN_USING);

        int quality = treasureWare.getQuality();
        // ???????????????id
        int profileId = StaticTreasureWareDataMgr.getProfileId(treasureWareService.getAttrType(treasureWare), quality, treasureWare.getSpecialId());
        // ???????????????????????????
        LogLordHelper.treasureWare(
                from,
                player.account,
                player.lord,
                treasureWareId,
                keyId,
                Constant.ACTION_ADD,
                profileId,
                quality,
                treasureWare.logAttrs(),
                CheckNull.isNull(treasureWare.getSpecialId()) ? -1 : treasureWare.getSpecialId(),
                param
        );
    }

    /**
     * ????????????
     *
     * @param player   ??????
     * @param id       ??????id
     * @param duration ????????????
     * @param convert  ????????????
     * @param from     ????????????
     * @param param    ??????
     */
    private void addMarchLine(Player player, int id, int duration, List<Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("????????????id??????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        if (id == Constant.DEFAULT_MARCH_LINE_ID) {
            // ????????????????????????
            return;
        }
        StaticMarchLine sMarchLine = StaticLordDataMgr.getMarchLine(id);
        if (Objects.isNull(sMarchLine)) {
            LogUtil.error("????????????id????????????????????????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.MARCH_SPECIAL_EFFECTS, id, duration, convert, from, param);
        // ?????????????????????????????????????????????????????????????????????
        if (sMarchLine.getEffectType() == StaticMarchLine.EFFECT_TYPE_HERO_ATTR) {
            CalculateUtil.reCalcAllHeroAttr(player);
        }
    }

    private void addSandTableScore(Player player, int count, AwardFrom from, Object... param) {
        if (count > 0) {
            player.setSandTableScore(player.getSandTableScore() + count);
            // ????????????
            LogLordHelper.sandTableScore(from, player, count, param);
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     * @param param
     */
    private void addEquipJewel(Player player, int id, int count, AwardFrom from, Object... param) {
        if (count <= 0 || count > 10000) {
            LogUtil.error("????????????????????????, roleId:", player.roleId, ", jewelId:", id, ", count:", count, ", from:",
                    from.getCode());
            return;
        }

        StaticJewel sJewel = StaticPropDataMgr.getJewelByLv(id);
        if (CheckNull.isNull(sJewel)) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", equipId:", id, ", from:", from.getCode());
            return;
        }

        EquipJewel jewel = player.equipJewel.get(id);
        if (CheckNull.isNull(jewel)) {
            jewel = new EquipJewel();
            jewel.setJewelId(id);
            jewel.setCount(count);
            player.equipJewel.put(id, jewel);
        } else {
            jewel.addCount(count);
        }
        // ????????????????????????
        LogLordHelper.jewel(from, player.account, player.lord, id, jewel.getCount(), count, Constant.ACTION_ADD, param);
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param equipId
     * @param count
     * @param from
     * @param param
     * @return
     */
    public List<Integer> addMentorEquip(Player player, int equipId, int count, AwardFrom from, Object... param) {
        if (count <= 0 || count > 10000) {
            LogUtil.error("????????????????????????, roleId:", player.roleId, ", equipId:", equipId, ", count:", count, ", from:",
                    from.getCode());
            return null;
        }

        StaticMentorEquip sEquip = StaticMentorDataMgr.getsMentorEquipIdMap(equipId);
        if (CheckNull.isNull(sEquip)) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", equipId:", equipId, ", from:", from.getCode());
            return null;
        }

        List<Integer> keyList = new ArrayList<>();
        MentorEquip equip;
        MentorInfo mentorInfo = player.getMentorInfo();
        for (int i = 0; i < count; i++) {
            equip = new MentorEquip();
            equip.setKeyId(player.maxKey());
            equip.setType(sEquip.getType());
            equip.setEquipId(equipId);
            equip.setLv(sEquip.getOrder());
            equip.setStarLv(sEquip.getGearLevel());
            equip.setMentorType(sEquip.getMentorType());
            mentorInfo.getEquipMap().put(equip.getKeyId(), equip);

            // ????????????
            int attack = sEquip.getAttack();
            if (attack > 0) {
                Map<Integer, Integer> attr = equip.getAttr();
                attr.put(Constant.AttrId.ATTACK, attack);
                equip.setAttr(attr);
            }

            // ????????????
            int defenseHigh = sEquip.getDefenseHigh();
            int defenseLow = sEquip.getDefenseLow();
            if (defenseHigh > 0 && defenseLow > 0) {
                int defense = RandomUtils.nextInt(defenseLow, defenseHigh + 1);
                if (defense > 0) {
                    Map<Integer, Integer> extAttr = equip.getExtAttr();
                    extAttr.put(Constant.AttrId.DEFEND, defense);
                }
            }
            keyList.add(equip.getKeyId());

            Map<Integer, Integer> sumMap = new HashMap<>();
            for (Entry<Integer, Integer> en : equip.getAttr().entrySet()) {
                CalculateUtil.addAttrValue(sumMap, en.getKey(), en.getValue());
            }
            for (Entry<Integer, Integer> en : equip.getExtAttr().entrySet()) {
                CalculateUtil.addAttrValue(sumMap, en.getKey(), en.getValue());
            }

            int fight = CalculateUtil.reCalcFight(sumMap);
            equip.setFight(fight);

            // ???????????????????????????
            LogLordHelper.mentorEquip(from, player.account, player.lord, equipId, count);

            // ??????????????????????????????
            mentorDataManager.checkBetterEquip(player, equip);
        }
        return keyList;
    }

    /**
     * ????????????
     *
     * @param player
     * @param medalId
     * @param goodsId
     * @param count
     * @param from
     * @param param
     * @return
     */
    public ArrayList<Medal> addMedal(Player player, int medalId, int goodsId, int count, AwardFrom from,
                                     Object... param) {
        ArrayList<Medal> medals = new ArrayList<>();
        if (medalId <= 0 && goodsId <= 0) {
            LogUtil.error("????????????????????????, roleId:", player.roleId, ", medalId:", medalId, ", goodsId:", goodsId, ", count:",
                    count, ", from:", from.getCode());
            return medals;
        }
        // ??????????????????
        StaticMedalGoods sMedalGoods = null;
        if (goodsId > 0 && goodsId < Integer.MAX_VALUE) {
            // ????????????????????????
            sMedalGoods = StaticMedalDataMgr.getMedalGoodsById(goodsId);
            if (sMedalGoods == null) {
                LogUtil.error("????????????id?????????, ????????????, roleId:", player.roleId, ", goodsId:", goodsId, ", from:",
                        from.getCode());
                return medals;
            }
        }
        medalId = !CheckNull.isNull(sMedalGoods) ? sMedalGoods.getMedalId() : medalId;
        // ????????????
        StaticMedal staticmedal = StaticMedalDataMgr.getMedalById(medalId);
        if (staticmedal == null) {
            LogUtil.error("??????id?????????, ????????????, roleId:", player.roleId, ", medalId:", medalId, ", from:", from.getCode());
            return medals;
        }

        Medal medal = null;
        for (int i = 0; i < count; i++) {
            try {
                medal = medalDataManager.initMedal(sMedalGoods, staticmedal);
            } catch (MwException e) {
                LogUtil.error("?????????????????????, ????????????, roleId:", player.roleId, ", medalId:", medalId, ", from:",
                        from.getCode());
                return medals;
            }
            medal.setKeyId(player.maxKey());
            player.medals.put(medal.getKeyId(), medal);
            medals.add(medal);
            // ???????????????????????????
            LogLordHelper.medal(from, player.account, player.lord, medal.getMedalId(), medal.getKeyId(),
                    Constant.ACTION_ADD, param);
        }
        // ?????????????????????
        syncMedal(player, medals);
        return medals;
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param medals
     */
    public void syncMedal(Player player, ArrayList<Medal> medals) {
        if (player.isLogin && player.ctx != null && !CheckNull.isEmpty(medals)) {
            GamePb1.SyncMedalRs.Builder b = GamePb1.SyncMedalRs.newBuilder();
            for (Medal m : medals) {
                b.addMedal(PbHelper.createMedalPb(m));
            }
            // ??????
            Base.Builder msg = PbHelper.createSynBase(GamePb1.SyncMedalRs.EXT_FIELD_NUMBER, GamePb1.SyncMedalRs.ext,
                    b.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * ??????????????????
     *
     * @param player ????????????
     * @param chipId ??????id
     * @param count  ??????
     * @param from   ????????????
     * @param param  ??????
     */
    private void addPlaneChip(Player player, int chipId, int count, AwardFrom from, Object[] param) {

        StaticPlaneTransform planeTransform = StaticWarPlaneDataMgr.getPlaneTransformById(chipId);
        if (CheckNull.isNull(planeTransform)) {
            LogUtil.error("????????????Id????????????????????????, roleId:", player.roleId, ", chipId:", chipId, ", from:", from.getCode());
            return;
        }

        // ?????????????????????
        int planeType = planeTransform.getPlaneType();

        // ???????????????
        List<List<Integer>> transforms = planeTransform.getTransform();
        if (CheckNull.isEmpty(transforms)) {
            LogUtil.error("????????????????????????????????????????????????, roleId:", player.roleId, ", chipId:", chipId, ", from:", from.getCode());
            return;
        }

        // ????????????
        WarPlane warPlane = player.warPlanes.get(planeType);

        // ????????????
        PlaneChip planeChip = player.getPlaneChip(chipId);

        // ??????????????????????????????
        if (!CheckNull.isNull(warPlane)) {
            // ?????????????????????????????????
            StaticPlaneUpgrade maxLv = StaticWarPlaneDataMgr
                    .getPlaneMaxLvByFilter(plane -> planeType == plane.getPlaneType() && plane.getNextId() == 0
                            && CheckNull.isEmpty(plane.getReformNeed()));
            if (CheckNull.isNull(maxLv)) {
                LogUtil.error("???????????????????????????????????????????????????, roleId:", player.roleId, ", chipId:", chipId, ", planeType:",
                        planeType, ", from:", from.getCode());
                return;
            }
            if (maxLv.getPlaneId() == warPlane.getPlaneId()) { // ??????????????????????????????????????????, ?????????????????????, ?????????????????????
                for (List<Integer> transform : transforms) {
                    addAwardSignle(player, transform, count, AwardFrom.PLANE_CHIP_TRANSFORM);
                }
            } else {
                planeChip.addChipCnt(count);
                // ??????????????????
                LogLordHelper.planeChip(from, player.account, player.lord, chipId, planeChip.getCnt(), count,
                        Constant.ACTION_SUB);
            }
        } else {
            planeChip.addChipCnt(count);
            // ??????????????????
            LogLordHelper.planeChip(from, player.account, player.lord, chipId, planeChip.getCnt(), count,
                    Constant.ACTION_SUB);
        }
    }

    /**
     * ?????????????????????
     *
     * @param player  ????????????
     * @param planeId ??????id
     * @param from    ????????????
     * @param param   ??????
     */
    private void addPlane(Player player, int planeId, AwardFrom from, Object[] param) {
        try {

            StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
            if (CheckNull.isNull(sPlaneUpgrade)) {
                LogUtil.error("??????Id????????????????????????, roleId:", player.roleId, ", planeId:", planeId, ", from:", from.getCode());
                return;
            }

            StaticPlaneInit planeInit = StaticWarPlaneDataMgr.getPlaneInitByType(sPlaneUpgrade.getPlaneType());
            if (CheckNull.isNull(planeInit)) {
                LogUtil.error("??????Type????????????????????????, roleId:", player.roleId, ", planeType:", sPlaneUpgrade.getPlaneType(),
                        ", from:", from.getCode());
                return;
            }

            // ??????????????????
            activityDataManager.updActivity(player, ActivityConst.ACT_WAR_PLANE_SEARCH, 1, planeInit.getQuality(), true);

            WarPlane plane = player.checkWarPlaneIsExist(planeId);
            if (!CheckNull.isNull(plane)) {
                List<Integer> decompose = planeInit.getDecompose();
                if (CheckNull.isEmpty(decompose)) {
                    LogUtil.error("??????????????????, ????????????, roleId:", player.roleId, ", planeId:", planeId, ", planeType:",
                            sPlaneUpgrade.getPlaneType(), ", from:", from.getCode());
                    return;
                }
                // ??????????????????
                addPlaneChip(player, decompose.get(1), decompose.get(2), from, param);
                return;
            }

            // ??????????????????
            plane = warPlaneDataManager.createPlane(planeInit);

            // ??????????????????????????????
            player.warPlanes.put(plane.getType(), plane);

            // ???????????????????????????
            LogLordHelper.plane(from, player.account, player.lord, planeId, Constant.ACTION_ADD, param);
        } catch (MwException e) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", planeId:", planeId, ", from:", from.getCode());
            return;
        }
    }

    /**
     * ?????????????????????8?????????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     */
    private void addSpecialAward(Player player, int id, int count, AwardFrom from, Object... param) {
        if (count > 0) {
            switch (id) {
                case AwardType.Special.BUILD_SPEED:
                    addBuildSpeed(player, count, from);
                    break;
                case AwardType.Special.ARM_SPEED:
                    addArmSpeed(player, count, from);
                    break;
                case AwardType.Special.BAPTIZE:
                    addBaptizeCnt(player, count, from, param);
                    break;
                case AwardType.Special.HERO_WASH:
                    addHeroWashCnt(player, count, from);
                    break;
                case AwardType.Special.TECH_SPEED:
                    addTechSpeed(player, count, from);
                    break;
                case AwardType.Special.INTERACTION_CNT:
                    addInteractionCnt(player, count, from);
                    break;
                // ????????????
                case AwardType.Special.HERO_EXP:
                    break;
                // ????????????
                case AwardType.Special.PLANE_EXP:
                    break;
                // ???????????????
                case AwardType.Special.BUILD_LEVEL_UP:
                    buildingService.buildingLvUpImmediately(player, count);
                    break;
                case AwardType.Special.DIAOCHAN_SCORE:
                    activityDiaoChanService.updateBiyueScore(player, count, from, param);
                    break;
                case AwardType.Special.SEASON_SCORE:
                    seasonService.updateSeasonScore(player, count, from, param);
                    break;
                case AwardType.Special.SEASON_TALENT_STONE:
                    addSeasonRunesStone(player, count, from, param);
                    break;
                case AwardType.Special.ACT_MUSIC_FESTIVAL_CREATIVE_SCORE:
                    musicFestivalCreativeService.updateCreativeScore(player, count, from);
                    break;
                case AwardType.Special.SHENG_WU:
                    player.addMilitaryExpenditure(count);
                    LogLordHelper.commonLog("expenditure", from, player.account, player.lord, player.getMilitaryExpenditure(), count);
                    break;
                case AwardType.Special.DRAW_PERMANENT_CARD_COUNT:
                    operationSystemCount(player, id, count, from, true, param);
                    break;
                default:
            }
        }
    }

    /**
     * ???????????????
     *
     * @param player
     * @param add
     * @param from
     * @param param
     */
    private void addSeasonRunesStone(Player player, int add, AwardFrom from, Object[] param) {
        if (add > 0) {
            SeasonTalent runes = player.getPlayerSeasonData().getSeasonTalent();
            int have = runes.getRemainStone();
            runes.setRemainStone(have + add);
            LogLordHelper.seasonTalentStone(from, player.account, player.lord, have, add, param);
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param count
     * @param from
     */
    private void addHeroWashCnt(Player player, int count, AwardFrom from) {
//        player.common.setWashCount(player.common.getWashCount() + count);
        LogLordHelper.commonLog("addHeroWashCnt", from, player, count);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param count
     * @param from
     */
    private void addBaptizeCnt(Player player, int count, AwardFrom from, Object... params) {
        player.common.setBaptizeCnt(player.common.getBaptizeCnt() + count);
        LogLordHelper.equipBaptizeNew(from, player, count, Constant.ACTION_ADD, "addBaptizeCnt", params);
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param count
     * @param from
     */
    private void addInteractionCnt(Player player, int count, AwardFrom from) {
        Cia cia = player.getCia();
        if (cia != null) {
            cia.addInteractionCnt(count);
            LogLordHelper.commonLog("addInteractionCnt", from, player, count);
        }

    }

    /**
     * ????????????
     *
     * @param player
     * @param count  ??????
     * @param from
     */
    private void addTechSpeed(Player player, int count, AwardFrom from) {
        TechQue que = player.getCanSpeedTechQue();
        if (que == null) {// ????????????????????????
            return;
        }
        que.setFreeOtherCnt(WorldConstant.SPEED_TYPE_ACQUISITE);
        que.setParam(count);
        LogLordHelper.commonLog("addTechSpeed", from, player, count);
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param count
     * @param from
     */
    private void addArmSpeed(Player player, int count, AwardFrom from) {
        ArmQue que = player.getCanAddSpeedArmQue();
        if (null == que) {// ???????????????????????????????????????????????????????????????
            return;
        }
        que.setFree(WorldConstant.SPEED_TYPE_ACQUISITE);
        que.setParam(count);
        LogLordHelper.commonLog("addArmSpeed", from, player, count);
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param count
     * @param from
     */
    private void addBuildSpeed(Player player, int count, AwardFrom from) {
        BuildQue que = player.getCanAddSpeedBuildQue();
        if (null == que) {// ???????????????????????????????????????????????????????????????
            return;
        }
        que.setFree(WorldConstant.SPEED_TYPE_ACQUISITE);
        que.setParam(count);
        LogLordHelper.commonLog("addBuildSpeed", from, player, count);
    }

    private void addArmyAward(Player player, int id, long add, AwardFrom from, Object... param) {
        Resource resource = player.resource;
        switch (id) {
            case AwardType.Army.FACTORY_1_ARM:
                resource.setArm1(resource.getArm1() + add);
                break;
            case AwardType.Army.FACTORY_2_ARM:
                resource.setArm2(resource.getArm2() + add);
                break;
            case AwardType.Army.FACTORY_3_ARM:
                resource.setArm3(resource.getArm3() + add);
                break;
            default:
                break;
        }
        if (add != 0) {
            // playerDataManager.autoAddArmy(player); ????????????:???????????????,?????????????????????????????????????????????,??????????????????
            LogLordHelper.army(from, player.account, player.lord, player.resource, id, add, param);
        }
    }

    /*-----------------------------------------????????????end-------------------------------------------*/

    /**
     * ?????????????????????, AwardType.RANDOM ????????? (????????????????????????)
     *
     * @param player
     * @param rewardId ??????id
     * @param count    ???????????????
     * @param from
     */
    private CommonPb.Award addRandomAward(Player player, int rewardId, int count, AwardFrom from) {
        StaticReward staticReward = StaticRewardDataMgr.getRewardMap().get(rewardId);
        if (null == staticReward) {
            LogUtil.error("??????id?????????, rewardId:", rewardId);
            return null;
        }

        if (CheckNull.isEmpty(staticReward.getRewardStr())) {
            return null;
        }

        int probablity;
        List<Integer> randomIdList = new ArrayList<>();
        for (Entry<Integer, Integer> entry : staticReward.getRewardStr().entrySet()) {
            probablity = entry.getValue();
            if (probablity >= Constant.TEN_THROUSAND) {
                randomIdList.add(entry.getKey());
            } else {
                if (RandomHelper.isHitRangeIn10000(probablity)) {
                    randomIdList.add(entry.getKey());
                }
            }
            if (randomIdList.size() >= count) {// ?????????????????????????????????
                break;
            }
        }
        for (Integer randomId : randomIdList) {
            return reward(player, randomId, from);
        }
        return null;
    }

    /**
     * ??????randomId??????????????????
     *
     * @param player
     * @param randomId
     * @param from
     * @return
     */
    private CommonPb.Award reward(Player player, int randomId, AwardFrom from) {
        StaticRewardRandom srr = StaticRewardDataMgr.getRandomMap().get(randomId);
        if (null == srr) {
            LogUtil.error("random????????????, randomId:", randomId);
            return null;
        }

        if (CheckNull.isEmpty(srr.getRandomStr())) {
            return null;
        }

        // ??????????????????
        List<Integer> awardList = RewardRandomUtil.getAwardByRandomId(randomId);
        // return addAward(player, awardList, from); ???????????????????????????????????????????????????????????????????????????
        if (null == player || CheckNull.isEmpty(awardList)) {
            return null;
        }

        if (awardList.size() < 3) {
            return null;
        }

        int type = awardList.get(0);
        int id = awardList.get(1);
        int count = awardList.get(2);
        return addAwardSignle(player, type, id, count, from);
    }

    /**
     * ?????????????????????(?????????????????????????????????)
     *
     * @param player
     * @param rewardId ??????id
     * @param count    ???????????????
     */
    public List<CommonPb.Award> getRandomAward(Player player, int rewardId, long count) {
        StaticReward staticReward = StaticRewardDataMgr.getRewardMap().get(rewardId);
        if (null == staticReward) {
            LogUtil.error("??????id?????????, rewardId:", rewardId);
            return null;
        }

        if (CheckNull.isEmpty(staticReward.getRewardStr())) {
            return null;
        }

        int probablity;
        List<Integer> randomIdList = new ArrayList<>();
        for (Entry<Integer, Integer> entry : staticReward.getRewardStr().entrySet()) {
            probablity = entry.getValue();
            if (probablity >= Constant.TEN_THROUSAND) {
                randomIdList.add(entry.getKey());
            } else {
                if (RandomHelper.isHitRangeIn10000(probablity)) {
                    randomIdList.add(entry.getKey());
                }
            }

            if (randomIdList.size() >= count) {// ?????????????????????????????????
                break;
            }
        }

        List<CommonPb.Award> list = new ArrayList<>();
        for (Integer randomId : randomIdList) {
            Award pbAward = getRewardRandom(player, randomId);
            if (Objects.nonNull(pbAward)) {
                list.add(pbAward);
            }
        }
        return list;
    }

    /**
     * ??????randomId??????????????????
     *
     * @param player
     * @param randomId
     * @return
     */
    private CommonPb.Award getRewardRandom(Player player, int randomId) {
        StaticRewardRandom srr = StaticRewardDataMgr.getRandomMap().get(randomId);
        if (null == srr) {
            LogUtil.error("random????????????, randomId:", randomId);
            return null;
        }

        if (CheckNull.isEmpty(srr.getRandomStr())) {
            return null;
        }

        List<Integer> awardList = RewardRandomUtil.getAwardByRandomId(randomId);
        if (awardList.size() < 3) {
            return null;
        }
        return PbHelper.createAwardPb(awardList.get(0), awardList.get(1), awardList.get(2));
    }

    /**
     * ????????????
     *
     * @param player
     * @param moneyType
     * @param count
     * @param from
     */
    private void addMoney(Player player, int moneyType, long count, AwardFrom from, Object... param) {
        switch (moneyType) {
            case AwardType.Money.EXP:
                addRoleExp(player, count, from, param);
                break;
            case AwardType.Money.VIP_EXP:
                addVipExp(player, count, from, param);
                break;
            case AwardType.Money.GOLD:
                addGold(player, (int) count, from, param);
                break;
            case AwardType.Money.ACT:
                addPower(player, (int) count, from, param);
                break;
            case AwardType.Money.EXPLOIT:
                addExploit(player, (int) count, from, param);
                break;
            case AwardType.Money.HERO_TOKEN:
                addHeroToken(player, (int) count, from, param);
                break;
            case AwardType.Money.CREDIT:
                addCredit(player, (int) count, from, param);
                break;
            case AwardType.Money.HONOR:
                addHonor(player, (int) count, from, param);
                break;
            case AwardType.Money.GOLD_BAR:
                addGoldBar(player, (int) count, from, param);
                break;
            case AwardType.Money.MENTOR_BILL:
                addMentorBill(player, (int) count, from, param);
                break;
            case AwardType.Money.GOLD_INGOT:
                addGoldIngot(player, (int) count, from, param);
                break;
            case AwardType.Money.WAR_FIRE_COIN:
                addWarFireCoin(player, (int) count, from, param);
                break;
            case AwardType.Money.FISH_SCORE:
                addFishScore(player, (int) count, from, param);
                break;
            case AwardType.Money.TREASURE_WARE_GOLDEN:
                addTreasureWareGolden(player, count, from, true, param);
                break;
            case AwardType.Money.TREASURE_WARE_DUST:
                addTreasureWareDust(player, count, from, true, param);
                break;
            case AwardType.Money.TREASURE_WARE_ESSENCE:
                addTreasureWareEssence(player, count, from, true, param);
                break;
            case AwardType.Money.CROSS_WAR_FIRE_COIN:
                addCrossWarFireCoin(player, (int) count, from, true, param);
                break;
            case AwardType.Money.ANCIENT_BOOK:
                addAncientBook(player, (int) count, from, param);
                break;
            default:
                break;
        }
    }

    /**
     * ????????????
     */
    private void addAncientBook(Player player, int add, AwardFrom from, Object[] param) {
        if (add > 0) {
            player.lord.setAncientBook(player.lord.getAncientBook() + add);
            LogLordHelper.ancientBook(from, player.account, player.lord, player.lord.getAncientBook(), add, param);
        }
    }

    private void addFishScore(Player player, int add, AwardFrom from, Object[] param) {
        if (add > 0) {
            player.getFishingData().setScore(player.getFishingData().getScore() + add);
            LogLordHelper.commonLog("fishScore", from, player, player.getFishingData().getScore(), add);
        }
    }

    private void addWarFireCoin(Player player, int add, AwardFrom from, Object[] param) {
        if (add > 0) {
            int have = player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE);
            player.setMixtureData(PlayerConstant.WAR_FIRE_PRICE, have + add);
            LogLordHelper.warFireCoin(from, player.account, player.lord, have, add, param);
        }
    }

    private void addCrossWarFireCoin(Player player, int add, AwardFrom from, boolean operation, Object[] param) {
        if (add == 0) {
            return;
        }

        add = Math.abs(add);
        add = operation ? add : -add;

        int have = player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE);
        player.setMixtureData(PlayerConstant.CROSS_WAR_FIRE_PRICE, have + add);
        LogLordHelper.warFireCoin(from, player.account, player.lord, have, add, param);
        //????????????
        if (ArrayUtils.contains(HAS_INFO2_AWARD_FROM, from)) {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.CROSS_WAR_FIRE_COIN,
                    add, player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE), Arrays.toString(new Object[]{param[0]}), Arrays.toString(new Object[]{param[1]}));
        } else {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.CROSS_WAR_FIRE_COIN,
                    add, player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE), Arrays.toString(param), "");
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param add
     * @param from
     * @param param
     */
    private void addGoldIngot(Player player, int add, AwardFrom from, Object[] param) {
        if (add > 0) {
            int have = player.getMixtureDataById(PlayerConstant.GOLD_INGOT);
            player.setMixtureData(PlayerConstant.GOLD_INGOT, have + add);
            LogLordHelper.goldIngot(from, player.account, player.lord, have, add, param);
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param add
     * @param from
     * @param param
     */
    private void addMentorBill(Player player, int add, AwardFrom from, Object[] param) {
        if (add > 0) {
            int have = player.getMixtureDataById(PlayerConstant.MENTOR_BILL);
            player.setMixtureData(PlayerConstant.MENTOR_BILL, have + add);
            LogLordHelper.mentorBill(from, player.account, player.lord, have, add, param);
        }
    }

    /**
     * @param player
     * @param add
     * @param from
     * @param param
     * @return void
     * @Title: addGoldBar
     * @Description: ?????? ??????
     */
    private void addGoldBar(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            player.lord.setGoldBar(player.lord.getGoldBar() + add);
            LogLordHelper.goldBar(from, player.account, player.lord, player.lord.getGoldBar(), add, param);
        }
    }

    /**
     * @param player
     * @param add
     * @param from
     * @param param
     * @return void
     * @Title: addHonor
     * @Description: ????????????
     */
    private void addHonor(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            player.lord.setHonor(player.lord.getHonor() + add);
            LogLordHelper.honor(from, player.account, player.lord, player.lord.getHonor(), add, param);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.HONOR, add, player.lord.getHonor(), Arrays.toString(param), "");
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param add
     * @param from
     */
    private void addCredit(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            player.lord.setCredit(player.lord.getCredit() + add);
            LogLordHelper.credit(from, player.account, player.lord, player.lord.getCredit(), add, param);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.CREDIT, add, player.lord.getCredit(), Arrays.toString(param), "");
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param add
     * @param from
     */
    private void addHeroToken(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            player.lord.setHeroToken(player.lord.getHeroToken() + add);
            LogLordHelper.heroToken(from, player.account, player.lord, player.lord.getHeroToken(), add, param);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.HERO_TOKEN, add, player.lord.getHeroToken(), Arrays.toString(param), "");
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param add
     * @param from
     */
    private void addPower(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            player.lord.setPower(player.lord.getPower() + add);
            LogLordHelper.power(from, player.account, player.lord, player.lord.getPower(), add);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.ACT, add, player.lord.getPower(), Arrays.toString(param), "");
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param add
     * @param from
     */
    private void addExploit(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            player.lord.setExploit(player.lord.getExploit() + add);
            LogLordHelper.exploit(from, player.account, player.lord, player.lord.getExploit(), add, param);
            rankDataManager.setExploit(player.lord);
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_EXPLOIT_CNT, add);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_EXPLOIT_CNT, add);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.EXPLOIT, add, player.lord.getExploit(), Arrays.toString(param), "");
        }
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param add
     * @param from
     */
    private void addGold(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            Lord lord = player.lord;
            lord.setGold(lord.getGold() + add);
            lord.setGoldGive(lord.getGoldGive() + add);
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_GOLD_CNT, add);
            LogLordHelper.gold(from, player.account, lord, add, 0, param);
        }
    }


    /*************************** ?????????????????? start *************************************/

    /**
     * ??????????????????
     *
     * @param player
     * @param count
     */
    private void addRoleExp(Player player, long count, AwardFrom from, Object... param) {
        // StaticLordDataMgr.addExp(player.lord, count);

        if (count > 0) {
            addExp(player, count, from);
            // ??????????????????
            LogLordHelper.exp(from, player.account, player.lord, count, param);
        }
        // worldDataManager.openPos(player);
    }

    private void addVipExp(Player player, long count, AwardFrom from, Object... param) {
        if (count > 0) {
            Lord lord = player.lord;
            lord.setVipExp((int) (lord.getVipExp() + count));
            vipDataManager.processVip(player);
            // ??????????????????
            LogLordHelper.vipExp(from, player.account, player.lord, count, param);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.VIP_EXP, count, player.lord.getVipExp(), Arrays.toString(param), "");
        }
    }

    private boolean addExp(Player player, long add, AwardFrom from) {
        Lord lord = player.lord;
        int startLv = lord.getLevel();
        int lv, preLv;
        lv = preLv = lord.getLevel();

        boolean up = false;
        long exp = lord.getExp() + add;
        StaticLordLv staticLordLv = null;
        List<Integer> lvThroughList = null;// ??????????????????????????????????????????
        while (true) {
            staticLordLv = StaticLordDataMgr.getStaticLordLv(lv + 1);
            if (staticLordLv == null || lv >= Constant.MAX_ROLE_LV) {
                exp = 0;// ???????????????????????????????????????
                break;
            }

            if (exp >= staticLordLv.getNeedExp()) {
                up = true;
                exp -= staticLordLv.getNeedExp();
                lv++;
                if (lvThroughList == null) {
                    lvThroughList = new ArrayList<>();
                }
                lvThroughList.add(lv);
                continue;
            } else {
                break;
            }
        }
        lord.setLevel(lv);
        lord.setExp(exp);

        // ????????????45??? ????????????????????? Constant.ROLE_45_DIGIT ????????? ?????????????????????
        if (startLv < Constant.ROLE_GRADE_45 && lv >= Constant.ROLE_GRADE_45) {
            // ??????????????????????????????
            int now = TimeHelper.getCurrentSecond();
            player.setMixtureData(PlayerConstant.RYRB_LOCK_TIME, now);

            // ??????????????????45???????????? ??????
            int count = playerDataManager.getRoleGrade45();
            if (count < Constant.ROLE_45_DIGIT) {// ??????????????? ???count+1 ?????????45??????????????????
                playerDataManager.setRoleGrade45(count + 1);
                chatDataManager.sendSysChat(ChatConst.CHAT_GRADE_IPGRADING_45, player.lord.getCamp(), 0, player.lord.getCamp(),
                        player.lord.getNick(), count + 1);
            }
        }

        if (up) {
            rankDataManager.setRoleLv(lord);
            // ??????????????????
            // sendReward(player, staticLordLv.getRewards(), AwardFrom.LV_UP_REWARD, "????????????");
            DbMasterApprentice apprentice = player.master;
            Player master = null;
            try {
                master = CheckNull.isNull(apprentice) ? null : playerDataManager.checkPlayerIsExist(apprentice.getLordId());
            } catch (MwException e) {
                LogUtil.error(e, "?????????????????????player??????, roleId: ", apprentice.getLordId());
            }
            for (int i = startLv + 1; i <= lord.getLevel(); i++) {
                staticLordLv = StaticLordDataMgr.getStaticLordLv(i); // ????????????,???????????????????????????
                //????????????30???,????????????????????????????????????
                if (i == Constant.AUTO_SENDMAIL_LEVEL && lord.getTopup() >= Constant.AUTO_SENDMAIL_VIP_LV && player.getMixtureDataById(PlayerConstant.SEND_CHANNEL_MAIL) == 0) {
                    sendChannelMail(player);
                }
                if (!CheckNull.isEmpty(staticLordLv.getRewards())) {
                    mailDataManager.sendAttachMail(player,
                            PbHelper.createAwardsPb(StaticLordDataMgr.getStaticLordLv(i).getRewards()),
                            MailConstant.MOLD_PARTY_LV_REWARD, AwardFrom.LV_UP_REWARD, TimeHelper.getCurrentSecond(), i,
                            i);
                }
                if (!CheckNull.isNull(master)) {
                    int finalLv = i;
                    List<Integer> config = ActParamConstant.ACT_TUTOR_RANK_CONF.stream().filter(conf -> conf.get(0) == finalLv).findFirst().orElse(null);
                    if (!CheckNull.isEmpty(config)) {
                        // ??????????????????
                        activityDataManager.updRankActivity(master, ActivityConst.ACT_TUTOR_RANK, config.get(1));
                    }
                }
            }
            buildingDataManager.refreshSourceData(player);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_LEVEL);
            taskDataManager.updTask(player, TaskType.COND_LORD_LV, 1);

            activityTriggerService.roleLevelUpTriggerGift(player, lvThroughList);

            //???????????????????????????????????????
            activityDiaoChanService.handleTodayTask(player);

            //????????????-????????????
            ActivityDiaoChanService.completeTask(player, ETask.PLAYER_LV);
            TaskService.processTask(player, ETask.PLAYER_LV);
            if (null != player.master) {
                Player masterPlayer = playerDataManager.getPlayer(master.getLordId());
                //???????????????????????????????????????????????????????????????????????????
                if (masterPlayer.isLogin) {
                    //??????-x?????????????????????y???
                    titleService.processTask(masterPlayer, ETask.APPRENTICE_LEVEL_MAKE_IT);
                }
            }
            //??????????????????, ????????????????????????
            LogLordHelper.gameLog(LogParamConstant.LEVEL_UP, player, from, preLv, lv);
            // ????????????????????????
            EventBus.getDefault().post(new Events.ActLevelUpEvent(lord.getLordId(), preLv, lv));
            // ??????????????????????????????????????????????????????
            StaticFunctionOpen sOpen = StaticFunctionDataMgr.getOpenById(FunctionConstant.CITY_EVENT);
            if (sOpen != null) {
                // ????????????
                int cityEventNeedLv = sOpen.getLv();
                if (lvThroughList.contains(cityEventNeedLv)) {
                    // ??????????????????, ????????????
                    DataResource.ac.getBean(LifeSimulatorService.class).assignCityEventToPlayerJob(player);
                }
            }
        }
        // ????????????????????????
        playerDataManager.syncRoleInfo(player);
        if (lvThroughList != null) {// ?????????????????????????????????
            if (lvThroughList.contains(ActParamConstant.ACT_ALL_CHARGE_LORD_LV)) {
                activityDataManager.syncActChange(player, ActivityConst.ACT_ALL_CHARGE);
            }
            List<Integer> finalLvThroughList = lvThroughList;
            Optional.ofNullable(StaticFunctionDataMgr.getOpenById(FunctionConstant.ACT_ACT_BRAVEST_ARMY))
                    .ifPresent(sOpen -> {
                        int sLv = sOpen.getLv();
                        if (finalLvThroughList.contains(sLv)) {
                            activityDataManager.syncActChange(player, ActivityConst.ACT_BRAVEST_ARMY);
                        }
                    });

            if (lvThroughList.contains(ActParamConstant.ACT_VIP_LORD_LV)) {
                activityDataManager.syncActChange(player, ActivityConst.ACT_VIP);
            }
            if (lvThroughList.contains(ActParamConstant.ACT_ATK_CITY_LEVEL.get(0))) {
                activityDataManager.syncActChange(player, ActivityConst.ACT_ATTACK_CITY_NEW);
            }
            activityDataManager.syncActChange(player, ActivityConst.ACT_LEVEL);
            if (lvThroughList.contains(ActParamConstant.ACT_CAMP_FIGHT_RANK_JOIN_COND.get(0) + 1)) {
                activityDataManager.upLvPlusActCampFightRank(player);
            }
        }
        return up;
    }

    /**
     * ????????????30???,????????????????????????????????????
     */
    public void sendChannelMail(Player player) {
        List<StaticChannelMail> channelMailList = StaticMailDataMgr.getChannelMailList();
        // ??????????????????
        player.setMixtureData(PlayerConstant.SEND_CHANNEL_MAIL, 1);
        for (StaticChannelMail staticChannelMail : channelMailList) {
            if (player.account.getPlatNo() == staticChannelMail.getChannelId() && player.account.getChildNo() == staticChannelMail.getChildId()) {
                mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(staticChannelMail.getRewards()), MailConstant.MOLD_GM_UPDATE_EXPLAIN, AwardFrom.LV_UP_REWARD, TimeHelper.getCurrentSecond(),
                        staticChannelMail.getContent(), staticChannelMail.getDetail());
                break;
            }
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param chatId
     * @param campOrArea ?????????????????????????????? camp,???????????????areaId
     * @param myCnt      ??????????????????
     * @param param
     */
    public void sendSysChat(int chatId, int campOrArea, int myCnt, Object... param) {
        StaticChat sChat = StaticChatDataMgr.getChatMapById(chatId);
        if (sChat != null) {
            int channel = sChat.getChannel();
            Chat chat = myCnt > 0 ? createWithParamSysChat(chatId, myCnt, param) : createSysChat(chatId, param);
            if (ChatConst.CHANNEL_WORLD == channel) {// ??????
                sendWorldChat(chat);
            } else {
                LogUtil.error("????????????????????? chatId:", chatId);
            }
        } else {
            LogUtil.error("????????????????????? chatId:", chatId);
        }

    }

    /*************************** ?????????????????? end *************************************/

    /**
     * ??????????????????id????????????????????????
     *
     * @param chatId
     * @param myCnt  ?????????????????? myParam[0~myCnt] ; chat???param[myCnt~??????]
     * @param param
     * @return
     */
    private Chat createWithParamSysChat(int chatId, int myCnt, Object... param) {
        String[] params = null;
        SystemChat systemChat = new SystemChat();
        if (!CheckNull.isEmpty(param)) {
            if (myCnt > 0) {
                params = new String[myCnt];
                for (int i = 0; i < myCnt; i++) {
                    params[i] = String.valueOf(param[i]);
                }
                systemChat.setMyParam(params);
            }
            params = new String[param.length - myCnt];
            for (int i = myCnt; i < param.length; i++) {
                params[i - myCnt] = String.valueOf(param[i]);
            }
        }
        systemChat.setChatId(chatId);
        systemChat.setTime(TimeHelper.getCurrentSecond());
        systemChat.setParam(params);
        return systemChat;
    }

    /**
     * ??????????????????id????????????????????????
     *
     * @param chatId
     * @param param
     * @return
     */
    private Chat createSysChat(int chatId, Object... param) {
        return createWithParamSysChat(chatId, 0, param);
    }

    /**
     * ????????????????????????
     *
     * @param chat
     */
    private void sendWorldChat(Chat chat) {
        sendWorldChat(chat, false);
    }

    /**
     * ????????????????????????
     *
     * @param chat
     * @param isRole ?????????????????? true?????????
     */
    private void sendWorldChat(Chat chat, boolean isRole) {
        CommonPb.Chat b = isRole ? chatDataManager.addWorldRoleChat(chat) : chatDataManager.addWorldChat(chat);
        SyncChatRs.Builder chatBuilder = SyncChatRs.newBuilder();
        chatBuilder.setChat(b);
        Base.Builder builder = PbHelper.createSynBase(SyncChatRs.EXT_FIELD_NUMBER, SyncChatRs.ext, chatBuilder.build());

        Player player;
        Iterator<Player> it = playerDataManager.getAllOnlinePlayer().values().iterator();
        while (it.hasNext()) {
            player = it.next();
            if (player.ctx != null) {
                MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
            }
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param propId
     * @param count
     * @param from
     */
    private void addProp(Player player, int propId, int count, AwardFrom from, Object... param) {
        if (count <= 0 || count > Integer.MAX_VALUE) {
            LogUtil.error("????????????????????????, roleId:", player.roleId, ", propId:", propId, ", count:", count, ", from:",
                    from.getCode());
            return;
        }

        StaticProp staticProp = StaticPropDataMgr.getPropMap().get(propId);
        if (null == staticProp) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", propId:", propId, ", from:", from.getCode());
            return;
        }

        Prop prop = player.props.get(propId);
        if (null == prop) {
            prop = new Prop();
            prop.setPropId(propId);
            prop.setCount(count);
            player.props.put(propId, prop);
        } else {
            prop.setCount(prop.getCount() + count);
        }

        // ??????????????????
        if (propId == PropConstant.WORLD_WAR_INTEGRAL) {
            worldWarSeasonWeekIntegralService.addWorldWarIntegral(player, count);
        }

        //??????????????????
        fishingService.handleBaitAltas(player, propId);

        // ????????????????????????
        LogLordHelper.prop(from, player.account, player.lord, propId, prop.getCount(), count, Constant.ACTION_ADD,
                param);
        taskDataManager.updTask(player, TaskType.COND_506, count, propId);
    }

    /**
     * ????????????
     *
     * @param player
     * @param portraitId
     * @param from
     */
    private void addPortrait(Player player, int portraitId, long count, List<Award> convert, AwardFrom from, Object... param) {
        if (portraitId <= 0) {
            LogUtil.error("??????id??????, roleId:", player.roleId, ", portraitId:", portraitId, ", from:", from.getCode());
            return;
        }
        StaticPortrait staticPortrait = StaticLordDataMgr.getPortrait(portraitId);
        if (null == staticPortrait) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", portraitId:", portraitId, ", from:",
                    from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.PORTRAIT, portraitId, count, convert, from, param);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param id
     * @param from
     * @param param
     */
    private void addCastleSkin(Player player, int id, long count, List<CommonPb.Award> convert, AwardFrom from, Object... param) {
        if (id <= 0) {
            LogUtil.error("????????????id??????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }

        if (id == StaticCastleSkin.DEFAULT_SKIN_ID) { // ????????????????????????
            return;
        }
        StaticCastleSkin castleSkinCfg = StaticLordDataMgr.getCastleSkinMapById(id);
        if (null == castleSkinCfg) {
            LogUtil.error("????????????id????????????????????????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.CASTLE_SKIN, id, count, convert, from, param);
        // ?????????????????????????????????????????????????????????????????????
        if (castleSkinCfg.getEffectType() == StaticCastleSkin.EFFECT_TYPE_HERO_ATTR) {
            CalculateUtil.reCalcAllHeroAttr(player);
        }
    }

    /**
     * ???????????????
     *
     * @param player ????????????
     * @param id     ??????id
     * @param count  1 ???????????????, ??????1????????????????????????(???)
     * @param from   ??????
     * @param param  ??????
     */
    private void addPortraitFrame(Player player, int id, int count, List<CommonPb.Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("?????????id??????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        if (id == Constant.DEFAULT_PORTRAIT_FRAME_ID) {
            // ????????????????????????
            return;
        }
        StaticPortraitFrame sPortraitFrame = StaticLordDataMgr.getPortraitFrame(id);
        if (Objects.isNull(sPortraitFrame)) {
            LogUtil.error("?????????id????????????????????????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.PORTRAIT_FRAME, id, count, convert, from, param);
    }

    /**
     * ????????????
     *
     * @param player ????????????
     * @param id     ??????id
     * @param count  1 ???????????????, ??????1????????????????????????(???)
     * @param from   ??????
     * @param param  ??????
     */
    private void addNameplate(Player player, int id, int count, List<CommonPb.Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("??????id??????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        if (id == Constant.DEFAULT_NAME_PLATE_ID) {
            // ????????????????????????
            return;
        }
        StaticNameplate sNameplate = StaticLordDataMgr.getNameplate(id);
        if (Objects.isNull(sNameplate)) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.NAMEPLATE, id, count, convert, from, param);
    }

    /**
     * ????????????
     *
     * @param player
     * @param chatBubbleId
     * @param from
     * @param param
     */
    public void addChatBubble(Player player, int chatBubbleId, int count, List<CommonPb.Award> convert, AwardFrom from, Object... param) {
        if (chatBubbleId <= 0) {
            LogUtil.error("????????????id??????, roleId:", player.roleId, ", chatBubbleId:", chatBubbleId, ", from:",
                    from.getCode());
            return;
        }
        StaticChatBubble sChatBubble = StaticLordDataMgr.getChatBubbleMapById(chatBubbleId);
        if (null == sChatBubble) {
            LogUtil.error("????????????id????????????????????????, roleId:", player.roleId, ", chatBubbleId:", chatBubbleId, ", from:",
                    from.getCode());
            return;
        }
        int bubbleType = sChatBubble.getType();
        if (bubbleType != StaticChatBubble.TYPE_AWARD && bubbleType != StaticChatBubble.TYPE_ACT_AWARD) {
            LogUtil.error("??????????????????????????????????????????, roleId:", player.roleId, ", chatBubbleId:", chatBubbleId, ", from:",
                    from.getCode());
            return;
        }
//        player.getChatBubbles().add(chatBubbleId);
        dressUpDataManager.addDressUp(player, AwardType.CHAT_BUBBLE, chatBubbleId, count, convert, from, param);
    }

    /*-----------------------------------------????????????end-------------------------------------------*/

    /*-----------------------------------------????????????start-------------------------------------------*/

    /**
     * ????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     */
    private void addStone(Player player, int id, int count, AwardFrom from, Object... param) {
        if (count <= 0 || count > Integer.MAX_VALUE) {
            LogUtil.error("????????????????????????, roleId:", player.roleId, ", id:", id, ", count:", count, ", from:",
                    from.getCode());
            return;
        }
        StaticStone sStone = StaticPropDataMgr.getStoneMapById(id);
        if (null == sStone) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        Map<Integer, Stone> stones = player.getStoneInfo().getStones();
        Stone stone = stones.get(id);
        if (null == stone) {
            stone = new Stone(id);
            stone.setCnt(count);
            stones.put(id, stone);
        } else {
            stone.addStoneCntAndGet(count);
        }
        if (from != AwardFrom.MOUNTING_STONE) {
            activityTriggerService.stoneUpLv(player, sStone.getLv());
        }
        // ????????????????????????
        LogLordHelper.stone(from, player.account, player.lord, id, stone.getCnt(), count, Constant.ACTION_ADD, param);

        //????????????-????????????
        ActivityDiaoChanService.completeTask(player, ETask.ORNAMENT_COUNT);
        //????????????-??????x?????????????????????
//        ActivityDiaoChanService.completeTask(player, ETask.ORNAMENT_COUNT);
        TaskService.processTask(player, ETask.ORNAMENT_COUNT);
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param stoneImproveId
     * @param from
     * @param param
     * @return
     */
    public int addStoneImprove(Player player, int stoneImproveId, AwardFrom from, Object... param) {
        StaticStoneImprove sStoneImprov = StaticPropDataMgr.getStoneImproveById(stoneImproveId);
        if (sStoneImprov == null) {
            LogUtil.error("????????????Id??????, roleId:", player.roleId, ", stoneImproveId:", stoneImproveId, ", from:",
                    from.getCode());
            return -1;
        }
        StoneImprove stoneImprove = new StoneImprove();
        stoneImprove.setKeyId(player.maxKey());
        stoneImprove.setStoneImproveId(stoneImproveId);
        stoneImprove.setExp(0);
        stoneImprove.setHoleIndex(0);
        player.getStoneInfo().getStoneImproves().put(stoneImprove.getKeyId(), stoneImprove);
        LogLordHelper.stoneImprove(from, player.account, player.lord, stoneImproveId, stoneImprove.getKeyId(),
                Constant.ACTION_ADD, param);
        return stoneImprove.getKeyId();
    }

    /**
     * ????????????
     *
     * @param player
     * @param equipId
     * @param count
     * @param from
     */
    public List<Integer> addEquip(Player player, int equipId, long count, AwardFrom from, Object... param) {
        if (count <= 0 || count > 10000) {
            LogUtil.error("????????????????????????, roleId:", player.roleId, ", equipId:", equipId, ", count:", count, ", from:",
                    from.getCode());
            return null;
        }

        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equipId);
        if (null == staticEquip) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", equipId:", equipId, ", from:", from.getCode());
            return null;
        }
        boolean ringEquip = EquipConstant.isRingEquip(staticEquip.getEquipPart());
        List<Integer> keyList = new ArrayList<>();
        Equip equip;
        for (int i = 0; i < count; i++) {
            equip = ringEquip ? new Ring() : new Equip();
            equip.setKeyId(player.maxKey());
            equip.setEquipId(equipId);
            player.equips.put(equip.getKeyId(), equip);

            // ??????????????????
            StaticEquipQualityExtra staticEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                    .get(staticEquip.getQuality());
            List<Turple<Integer, Integer>> attrLv = equip.getAttrAndLv();
            // ??????????????????????????????????????????
            if (staticEquip.getExtra() != null && staticEquip.getExtra().size() >= 2) {
                if (staticEquipQualityExtra != null && staticEquipQualityExtra.getExtraNum() > 0) {
                    for (int j = 0; j < staticEquipQualityExtra.getExtraNum() + 1; j++) {
                        attrLv.add(new Turple<>(staticEquip.getExtra().get(0), staticEquip.getExtra().get(1)));
                    }
                }
            } else {// ???????????????
                if (staticEquipQualityExtra != null && staticEquipQualityExtra.getExtraNum() > 0) {
                    for (int j = 0; j < staticEquipQualityExtra.getExtraNum(); j++) {
                        attrLv.add(new Turple<>(Constant.ATTRS[RandomUtils.nextInt(0, Constant.ATTRS.length)],
                                staticEquipQualityExtra.getExtraLv()));
                    }
                }
            }
            //???????????????????????????????????????????????????????????????, ???????????????????????????
            if (staticEquip.getQuality() >= EquipConstant.EQUIP_AUTO_LOCK) {
                equip.setEquipLocked(2);
            } else {
                equip.setEquipLocked(1);
            }
            keyList.add(equip.getKeyId());
            // ???????????????????????????
            LogLordHelper.equip(from, player.account, player.lord, equipId, equip.getKeyId(), Constant.ACTION_ADD
                    , LogUtil.obj2ShortStr(equip.getAttrAndLv()), param);
        }
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_EQUIP, staticEquip.getQuality());
        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_EQUIP, 1, staticEquip.getQuality());

        return keyList;
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @param param
     * @return
     */
    public Hero sendHeroAward(Player player, int type, int id, int count, AwardFrom from,
                              Object... param) {
        Hero hero = null;
        switch (type) {
            case AwardType.HERO:
                hero = addHero(player, id, from, param);
                break;
            case AwardType.HERO_DESIGNATED_GRADE:
                hero = addHeroDesignatedGrade(player, id, count, from, param);
                break;
        }

        return hero;
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param heroId
     * @param from
     */
    public Hero addHero(Player player, int heroId, AwardFrom from, Object... param) {
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        if (null == staticHero) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
            return null;
        }

        // ?????????????????????????????????????????????????????????
        Hero hero = player.heros.get(heroId);
        if (!drawCardService.checkHero(player, hero, from)) {
            return null;
        }

        try {
            if (null != hero) {
                // ?????????????????????, ????????????????????????
                drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);
                // ???????????????????????????
                operationHeroFragment(player, heroId, HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS, AwardFrom.SAME_TYPE_HERO, true, true, param);
                LogUtil.error("??????????????????????????????????????????, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
                return drawCardService.containAwardFrom(from) ? hero : null;
            }

            hero = new Hero();
            hero.setHeroId(heroId);
            hero.setHeroType(staticHero.getHeroType());
            hero.setQuality(staticHero.getQuality());
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            List<Integer> appoint = StaticHeroDataMgr.getInitHeroAppoint(heroId);

            if (CheckNull.nonEmpty(appoint)) {
                int minLv = appoint.get(0); // ????????????????????????
                int maxLv = appoint.get(1); // ????????????????????????
                int lordLevel = player.lord.getLevel(); // ??????????????????
                int targetLevel = Math.min(Math.max(minLv, maxLv), lordLevel);// ????????????????????????
                hero.setLevel(targetLevel);
            }

            // ???????????????????????????
            hero.initHeroGrade();
            //?????????????????????????????????????????????
            if (staticHero.getSeason() > 0) {
                Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> skillMap = StaticHeroDataMgr.getHeroSkill(heroId);
                if (Objects.nonNull(skillMap)) {
                    for (Entry<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> entry : skillMap.entrySet()) {
                        int skillId = entry.getKey();
                        int initLv = entry.getValue().firstKey();
                        hero.getSkillLevels().put(skillId, initLv);
                    }
                }
                hero.setCgyStage(1);
                hero.setCgyLv(0);

                //???????????????????????????
                chatDataManager.sendSysChat(ChatConst.SEASON_GET_HERO, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), hero.getHeroId());
            }
            CalculateUtil.processAttr(player, hero);
            player.heros.put(heroId, hero);
            // ?????????????????????, ????????????????????????
            drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);

            // ???????????????????????????
            LogLordHelper.hero(from, player.account, player.lord, heroId, Constant.ACTION_ADD, param);

            //????????????-????????????X???X??????
            ActivityDiaoChanService.completeTask(player, ETask.OWN_HERO);
            TaskService.processTask(player, ETask.OWN_HERO);
            //?????? - ??????????????????
            TaskService.handleTask(player, ETask.GET_HERO);
            ActivityDiaoChanService.completeTask(player, ETask.GET_HERO);
            TaskService.processTask(player, ETask.GET_HERO);

            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO_QUALITY_UPGRADE_CNT);
            taskDataManager.updTask(player, TaskType.COND_26, 1, hero.getHeroId());
            taskDataManager.updTask(player, TaskType.COND_27, 1, hero.getType());
            taskDataManager.updTask(player, TaskType.COND_514, 1, hero.getLevel());
            if (!ActParamConstant.ACT_TRIGGER_HERO_IGNORE.contains(staticHero.getHeroId())) {
                // ?????????????????????????????????
                activityTriggerService.awardHeroTriggerGift(player, hero.getQuality());
            }
            //????????????????????????????????????
            activityTriggerService.getAnyHero(player, staticHero.getHeroType());
            return hero;
        } catch (Exception e) {
            LogUtil.error(String.format("add hero error, player:%d, heroId:%d, e:%s", player.roleId, heroId, e));
            return hero;
        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param heroId
     * @param from
     * @param param
     * @return
     */
    public Hero addHeroDesignatedGrade(Player player, int heroId, int gradeKeyId, AwardFrom from, Object... param) {
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        if (null == staticHero) {
            LogUtil.error("??????id????????????????????????, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
            return null;
        }

        // ?????????????????????????????????????????????????????????
        Hero hero = player.heros.get(heroId);
        if (!drawCardService.checkHero(player, hero, from)) {
            return null;
        }

        try {
            if (null != hero) {
                // ?????????????????????, ????????????????????????
                drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);
                // ???????????????????????????
                operationDesignatedHeroGradeFragment(player, heroId, gradeKeyId, AwardFrom.SAME_TYPE_HERO, true, true, param);
                LogUtil.error("??????????????????????????????????????????????????????, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
                return drawCardService.containAwardFrom(from) ? hero : null;
            }

            StaticHeroUpgrade staticData = StaticHeroDataMgr.getStaticHeroUpgrade(gradeKeyId);
            if (CheckNull.isNull(staticData)) {
                LogUtil.error("heroId:%d, ??????????????????, gradeKeyId:%d", heroId, gradeKeyId);
                return null;
            }

            hero = new Hero();
            hero.setHeroId(heroId);
            hero.setHeroType(staticHero.getHeroType());
            hero.setQuality(staticHero.getQuality());
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            List<Integer> appoint = StaticHeroDataMgr.getInitHeroAppoint(heroId);

            if (CheckNull.nonEmpty(appoint)) {
                int minLv = appoint.get(0); // ????????????????????????
                int maxLv = appoint.get(1); // ????????????????????????
                int lordLevel = player.lord.getLevel(); // ??????????????????
                int targetLevel = Math.min(Math.max(minLv, maxLv), lordLevel);// ????????????????????????
                hero.setLevel(targetLevel);
            }

            // ???????????????????????????
            hero.setGradeKeyId(gradeKeyId);
            //?????????????????????????????????????????????
            if (staticHero.getSeason() > 0) {
                Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> skillMap = StaticHeroDataMgr.getHeroSkill(heroId);
                if (Objects.nonNull(skillMap)) {
                    for (Entry<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> entry : skillMap.entrySet()) {
                        int skillId = entry.getKey();
                        int initLv = entry.getValue().firstKey();
                        hero.getSkillLevels().put(skillId, initLv);
                    }
                }
                hero.setCgyStage(1);
                hero.setCgyLv(0);

                //???????????????????????????
                chatDataManager.sendSysChat(ChatConst.SEASON_GET_HERO, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), hero.getHeroId());
            }
            CalculateUtil.processAttr(player, hero);
            player.heros.put(heroId, hero);
            // ?????????????????????, ????????????????????????
            drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);

            // ???????????????????????????
            LogLordHelper.hero(from, player.account, player.lord, heroId, Constant.ACTION_ADD, param);

            //????????????-????????????X???X??????
            ActivityDiaoChanService.completeTask(player, ETask.OWN_HERO);
            TaskService.processTask(player, ETask.OWN_HERO);
            //?????? - ??????????????????
            TaskService.handleTask(player, ETask.GET_HERO);
            ActivityDiaoChanService.completeTask(player, ETask.GET_HERO);
            TaskService.processTask(player, ETask.GET_HERO);

            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO_QUALITY_UPGRADE_CNT);
            taskDataManager.updTask(player, TaskType.COND_26, 1, hero.getHeroId());
            taskDataManager.updTask(player, TaskType.COND_27, 1, hero.getType());
            taskDataManager.updTask(player, TaskType.COND_514, 1, hero.getLevel());
            if (!ActParamConstant.ACT_TRIGGER_HERO_IGNORE.contains(staticHero.getHeroId())) {
                // ?????????????????????????????????
                activityTriggerService.awardHeroTriggerGift(player, hero.getQuality());
            }
            //????????????????????????????????????
            activityTriggerService.getAnyHero(player, staticHero.getHeroType());
            return hero;
        } catch (Exception e) {
            LogUtil.error(String.format("add hero error, player:%d, heroId:%d, e:%s", player.roleId, heroId, e));
            return hero;
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param keyId
     * @param from
     */
    public void subEquip(Player player, int keyId, AwardFrom from) {
        Equip equip = player.equips.remove(keyId);
        if (equip != null) {
            // ???????????????????????????
            LogLordHelper.equip(from, player.account, player.lord, equip.getEquipId(), equip.getKeyId(),
                    Constant.ACTION_SUB, LogUtil.obj2ShortStr(equip.getAttrAndLv()));
        }

    }

    /**
     * ????????????
     *
     * @param player
     * @param keyId
     * @param from
     */
    public void subTreasureWare(Player player, int keyId, AwardFrom from, Object... param) {
        TreasureWare treasureWare = player.treasureWares.get(keyId);
        if (CheckNull.isNull(treasureWare)) {
            LogUtil.error(String.format("sub treasureWare is empty, lordId: %d, keyId: %d, from: %s", player.lord.getLordId(), keyId, from));
            return;
        }


        if (treasureWare.getQuality() >= TreasureWareConst.PURPLE_QUALITY) {
            treasureWare.setStatus(TreasureWareConst.TREASURE_HAS_DECOMPOSED);
            treasureWare.setDecomposeTime(TimeHelper.getCurrentSecond());
        } else {
            player.treasureWares.remove(keyId);
        }

        // ?????????????????????????????????
        DataResource.logicServer.addCommandByType(() -> {
            // ???????????????id
            int profileId = StaticTreasureWareDataMgr.getProfileId(treasureWareService.getAttrType(treasureWare), treasureWare.getQuality(), treasureWare.getSpecialId());
            // ???????????????????????????
            LogLordHelper.treasureWare(
                    from,
                    player.account,
                    player.lord,
                    treasureWare.getEquipId(),
                    treasureWare.getKeyId(),
                    Constant.ACTION_SUB,
                    profileId,
                    treasureWare.getQuality(),
                    treasureWare.logAttrs(),
                    CheckNull.isNull(treasureWare.getSpecialId()) ? -1 : treasureWare.getSpecialId(),
                    param
            );

            // ??????????????????
            EventDataUp.treasureCultivate(player, treasureWare, AwardFrom.TREASURE_WARE_TRAIN, treasureWareService.getAttrType(treasureWare));
        }, DealType.BACKGROUND);
    }

    private void modifyResource(Player player, int type, int id, long count, float factor, AwardFrom from) {
        switch (type) {
            case AwardType.RESOURCE:
                modifyResource(player, id, count, factor, from);
                break;
            case AwardType.ARMY:
                modifyArmyResource(player, id, count, factor, from);
                break;
        }
    }

    private void modifyResource(Player player, int id, long add, float factor, AwardFrom from, Object... param) {
        Resource resource = player.resource;
        if (factor > 0) {
            add *= (1 + factor);
        }
        switch (id) {
            case AwardType.Resource.ELE:
                resource.setElec(resource.getElec() + add);
                if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
                    //????????????-????????????- ???????????? 2(??????)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 2, Long.valueOf(add).intValue());
                }

                break;
            case AwardType.Resource.FOOD:
                resource.setFood(resource.getFood() + add);
                if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_SUPPLY_RANK, add);
                    // ???????????? ??????????????????????????????
                    restrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_PLUNDER_RESOURCE_CNT,
                            (int) add);
                    //????????????-????????????- ???????????? 3(??????)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 3, Long.valueOf(add).intValue());
                }
                break;
            case AwardType.Resource.OIL:
                resource.setOil(resource.getOil() + add);
                if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
                    //????????????-????????????- ???????????? 1(??????)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 1, Long.valueOf(add).intValue());
                }
                break;
            case AwardType.Resource.ORE:
                resource.setOre(resource.getOre() + add);
                if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_ORE_RANK, add);
                    //????????????-????????????- ???????????? 4(??????)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 4, Long.valueOf(add).intValue());
                }
                break;
            case AwardType.Resource.HUMAN:
                // ?????????????????????????????????????????????
                if (buildingDataManager.checkBuildingLock(player, BuildingType.CHEMICAL_PLANT)) {
                    resource.setHuman(resource.getHuman() + add);
                    resource.setHumanTime(TimeHelper.getCurrentSecond());
                }
                break;
            case AwardType.Resource.URANIUM:
                resource.setUranium(resource.getUranium() + add);
                break;
            default:
                break;
        }
        // ??????????????????
        if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_RESOURCE_CNT, (int) add, id);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_RESOURCE_CNT, (int) add, id);
        }
        // // ????????????????????????????????????,????????????
        // ChangeInfo change = ChangeInfo.newIns();
        // change.addChangeType(AwardType.RESOURCE, id);
        // syncRoleResChanged(player, change);
        if (add != 0) {
            LogLordHelper.resource(from, player.account, player.lord, player.resource, id, add, param);
        }
        if (add < 0 && AwardFrom.FIGHT_DEF != from) {
            // 10001 = 1 * 10000 + 1
            int key = AwardType.RESOURCE * 10000 + id;
            activityDataManager.updActivity(player, ActivityConst.ACT_RESOUCE_SUB, Math.abs(add), key, true);
        }

    }

    public void modifyArmyResource(Player player, int type, long add, float factor, AwardFrom from, Object... param) {
        Resource resource = player.resource;
        if (factor > 0) {
            add *= (1 + factor);
        }
        switch (type) {
            case AwardType.Army.FACTORY_1_ARM:
                resource.setArm1(resource.getArm1() + add);
                break;
            case AwardType.Army.FACTORY_2_ARM:
                resource.setArm2(resource.getArm2() + add);
                break;
            case AwardType.Army.FACTORY_3_ARM:
                resource.setArm3(resource.getArm3() + add);
                break;
            default:
                break;
        }
        if (add != 0) {
            LogLordHelper.army(from, player.account, player.lord, player.resource, type, add, param);
        }
    }

    /**
     * ?????????
     *
     * @param player
     * @param upNeedResource
     * @param factor         ???????????????
     * @param from
     */
    public void modifyResource(Player player, List<List<Integer>> upNeedResource, float factor, AwardFrom from) {
        if (upNeedResource == null || upNeedResource.size() == 0) {
            return;
        }
        for (List<Integer> needRes : upNeedResource) {
            modifyResource(player, needRes.get(0), needRes.get(1), -needRes.get(2), factor, from);
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param propId
     * @param count  ????????????????????????????????????
     * @param from
     * @throws MwException
     */
    public void subProp(Player player, int propId, int count, AwardFrom from, Object... param) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " ??????????????????????????????????????????, roleId:", player.roleId,
                    ", propId:", propId, ", need:", count);
        }

        // ??????id????????????0, ?????????
        if (propId <= 0) {
            return;
        }

        Prop prop = player.props.get(propId);
        if (null == prop || prop.getCount() < count) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), " ?????????????????????????????????, roleId:", player.roleId,
                    ", propId:", propId, ", need:", count, ", have:", null == prop ? 0 : prop.getCount());
        }

        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_USE_PROP, 1, propId);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_USE_PROP, 1, propId);

        //????????????-????????????
        ActivityDiaoChanService.completeTask(player, ETask.CONSUME_ITEM, propId, count);
        TaskService.processTask(player, ETask.CONSUME_ITEM, propId, count);

        // ????????????
        prop.setCount(prop.getCount() - count);

        // ??????????????????
        LogLordHelper.prop(from, player.account, player.lord, propId, prop.getCount(), count, Constant.ACTION_SUB,
                param);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     * @throws MwException
     */
    private void subPlaneChip(Player player, int id, int count, AwardFrom from) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " ??????????????????????????????????????????, roleId:", player.roleId, ", id:",
                    id, ", need:", count);
        }

        PlaneChip planeChip = player.getPlaneChip(id);
        if (CheckNull.isNull(planeChip) || planeChip.getCnt() < count) {
            throw new MwException(GameError.PLANE_CHIP_NOT_ENOUGH.getCode(), " ????????????????????????, roleId:", player.roleId,
                    ", id:", id, ", need:", count, ", have:", planeChip.getCnt());
        }
        planeChip.subChipCnt(count);
        // ??????????????????
        LogLordHelper.planeChip(from, player.account, player.lord, id, planeChip.getCnt(), count, Constant.ACTION_SUB);
    }

    /**
     * ????????????
     *
     * @param player
     * @param id
     * @param count
     * @param from
     * @throws MwException
     */
    public void subStone(Player player, int id, int count, AwardFrom from) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " ??????????????????????????????????????????, roleId:", player.roleId, ", id:",
                    id, ", need:", count);
        }

        Stone stone = player.getStoneInfo().getStones().get(id);
        if (null == stone || stone.getCnt() < count) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), " ?????????????????????????????????, roleId:", player.roleId, ", id:",
                    id, ", need:", count, ", have:", null == stone ? 0 : stone.getCnt());
        }
        stone.subStoneCntAndGet(count);
        // ??????????????????
        LogLordHelper.stone(from, player.account, player.lord, id, stone.getCnt(), count, Constant.ACTION_SUB);
    }

    /**
     * ?????????????????? <br />
     * <b>??????: ????????????????????????????????????????????????, ??????????????????????????????<b/>
     *
     * @param player ????????????
     * @param id     ????????????
     * @param count  ???????????????
     * @param from   ??????
     * @throws MwException ???????????????
     */
    private void subEquipJewel(Player player, int id, int count, AwardFrom from) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " ??????????????????????????????????????????, roleId:", player.roleId, ", id:",
                    id, ", need:", count);
        }

        EquipJewel equipJewel = player.equipJewel.get(id);
        if (CheckNull.isNull(equipJewel) || equipJewel.canUseCnt() < count) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), " ?????????????????????????????????, roleId:", player.roleId, ", id:",
                    id, ", need:", count, ", have:", null == equipJewel ? 0 : equipJewel.getCount(), ", canUse:",
                    null == equipJewel ? 0 : equipJewel.canUseCnt());
        }
        equipJewel.subCount(count);
        // ??????????????????
        LogLordHelper.jewel(from, player.account, player.lord, id, equipJewel.getCount(), count, Constant.ACTION_SUB);
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param resourceType ????????????
     * @param count        ???????????????????????????
     * @param from
     * @throws MwException
     */
    public void subResource(Player player, int resourceType, long count, AwardFrom from) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " ??????????????????????????????????????????, roleId:", player.roleId,
                    ", resourceType:", resourceType, ", need:", count);
        }

        checkResourceIsEnough(player.resource, resourceType, count);

        modifyResource(player, resourceType, -count, 0, from);

    }

    public void subArmyResource(Player player, int resourceType, long count, AwardFrom from) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " ??????????????????????????????????????????, roleId:", player.roleId,
                    ", resourceType:", resourceType, ", need:", count);
        }

        checkArmyIsEnough(player.resource, resourceType, count);

        modifyArmyResource(player, resourceType, -count, 0, from);
    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param moneyType ???????????????
     * @param count     ?????????????????????
     * @param from
     * @param param
     */
    public void subMoney(Player player, int moneyType, long count, AwardFrom from, Object... param) {
        if (null == player) {
            return;
        }

        switch (moneyType) {
            case AwardType.Money.EXP:
                break;
            case AwardType.Money.GOLD:
                subGold(player, (int) count, from, param);
                break;
            case AwardType.Money.ACT:
                subPower(player, (int) count, from);
                break;
            case AwardType.Money.EXPLOIT:
                subExploit(player, (int) count, from);
                break;
            case AwardType.Money.HERO_TOKEN:
                subHeroToken(player, (int) count, from);
                break;
            case AwardType.Money.CREDIT:
                subCredit(player, (int) count, from);
                break;
            case AwardType.Money.HONOR:
                subHonor(player, (int) count, from);
                break;
            case AwardType.Money.GOLD_BAR:
                subGoldBar(player, (int) count, from);
                break;
            case AwardType.Money.MENTOR_BILL:
                subMentorBill(player, (int) count, from);
                break;
            case AwardType.Money.GOLD_INGOT:
                subGoldIngot(player, (int) count, from);
                break;
            case AwardType.Money.WAR_FIRE_COIN:
                subWarFireCoin(player, (int) count, from);
                break;
            case AwardType.Money.FISH_SCORE:
                subFishScore(player, (int) count, from);
                break;
            case AwardType.Money.TREASURE_WARE_GOLDEN:
                addTreasureWareGolden(player, count, from, false, param);
                break;
            case AwardType.Money.TREASURE_WARE_DUST:
                addTreasureWareDust(player, count, from, false, param);
                break;
            case AwardType.Money.TREASURE_WARE_ESSENCE:
                addTreasureWareEssence(player, count, from, false, param);
                break;
            case AwardType.Money.CROSS_WAR_FIRE_COIN:
                addCrossWarFireCoin(player, (int) count, from, false, param);
                break;
            case AwardType.Money.ANCIENT_BOOK:
                subAncientBook(player, (int) count, from);
                break;
            default:
                break;
        }
    }

    private void addTreasureWareDust(Player player, long sub, AwardFrom from, boolean add, Object... param) {
        sub = Math.abs(sub);
        sub = add ? sub : -sub;
        long cur = player.lord.getTreasureWareDust() + sub;
        cur = cur < 0 ? 0 : cur;
        player.lord.setTreasureWareDust(cur);

        String paramString = Arrays.toString(param);
        LogLordHelper.commonLog("treasureWareMaterial", from, player, AwardType.Money.TREASURE_WARE_DUST, sub, player.lord.getTreasureWareDust(), paramString);
        //????????????
        if (ArrayUtils.contains(HAS_INFO2_AWARD_FROM, from)) {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_GOLDEN, sub, player.lord.getTreasureWareDust(),
                    paramString, "");
        } else {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_GOLDEN, sub, player.lord.getTreasureWareDust(),
                    paramString, "");
        }
    }

    private void addTreasureWareEssence(Player player, long sub, AwardFrom from, boolean add, Object... param) {
        sub = Math.abs(sub);
        sub = add ? sub : -sub;
        long cur = player.lord.getTreasureWareEssence() + sub;
        cur = cur < 0 ? 0 : cur;
        player.lord.setTreasureWareEssence(cur);

        String paramString = Arrays.toString(param);
        LogLordHelper.commonLog("treasureWareMaterial", from, player, AwardType.Money.TREASURE_WARE_ESSENCE, sub, player.lord.getTreasureWareEssence(), paramString);
        //????????????
        if (ArrayUtils.contains(HAS_INFO2_AWARD_FROM, from)) {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_DUST, sub, player.lord.getTreasureWareEssence(),
                    paramString, "");
        } else {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_DUST, sub, player.lord.getTreasureWareEssence(),
                    paramString, "");
        }
    }

    private void addTreasureWareGolden(Player player, long sub, AwardFrom from, boolean add, Object... param) {
        sub = Math.abs(sub);
        sub = add ? sub : -sub;
        long cur = player.lord.getTreasureWareGolden() + sub;
        cur = cur < 0 ? 0 : cur;
        player.lord.setTreasureWareGolden(cur);

        String paramString = Arrays.toString(param);
        LogLordHelper.commonLog("treasureWareMaterial", from, player, AwardType.Money.TREASURE_WARE_GOLDEN, sub, player.lord.getTreasureWareGolden(), paramString);
        //????????????
        if (ArrayUtils.contains(HAS_INFO2_AWARD_FROM, from)) {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_ESSENCE, sub,
                    player.lord.getTreasureWareGolden(), paramString, "");
        } else {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_ESSENCE, sub,
                    player.lord.getTreasureWareGolden(), paramString, "");
        }

    }

    /**
     * ????????????
     * @param player
     * @param sub
     * @param from
     */
    private void subAncientBook(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            int curr = player.lord.getAncientBook();
            int left = curr - sub;
            curr = Math.max(left, 0);
            player.lord.setAncientBook(curr);
            LogLordHelper.commonLog("ancientBook", from, player, player.lord.getAncientBook(), -sub);
        }
    }

    private void subFishScore(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            int curr = player.getFishingData().getScore();
            int left = curr - sub;
            curr = left < 0 ? 0 : left;
            player.getFishingData().setScore(curr);
            LogLordHelper.commonLog("fishScore", from, player, player.getFishingData().getScore(), -sub);
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param sub
     * @param from
     */
    private void subWarFireCoin(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            int have = player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE);
            player.setMixtureData(PlayerConstant.WAR_FIRE_PRICE, have - sub);
            LogLordHelper.warFireCoin(from, player.account, player.lord, have, -sub);
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param sub
     * @param from
     */
    private void subGoldIngot(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            int have = player.getMixtureDataById(PlayerConstant.GOLD_INGOT);
            player.setMixtureData(PlayerConstant.GOLD_INGOT, have - sub);
            LogLordHelper.goldIngot(from, player.account, player.lord, have, -sub);
        }
    }

    /**
     * ???????????????
     *
     * @param player
     * @param sub
     * @param from
     */
    private void subMentorBill(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            int have = player.getMixtureDataById(PlayerConstant.MENTOR_BILL);
            player.setMixtureData(PlayerConstant.MENTOR_BILL, have - sub);
            LogLordHelper.mentorBill(from, player.account, player.lord, have, -sub);
        }
    }

    /**
     * @param player
     * @param sub
     * @param from
     * @return void
     * @Title: subGoldBar
     * @Description: ?????????
     */
    private void subGoldBar(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            player.lord.setGoldBar(player.lord.getGoldBar() - sub);
            LogLordHelper.goldBar(from, player.account, player.lord, player.lord.getGoldBar(), -sub);
        }
    }

    /**
     * @param player
     * @param sub
     * @param from
     * @return void
     * @Title: subHonor
     * @Description: ?????????
     */
    private void subHonor(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            player.lord.setHonor(player.lord.getHonor() - sub);
            LogLordHelper.honor(from, player.account, player.lord, player.lord.getHonor(), -sub);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.HONOR, -sub, player.lord.getHonor(), "[]", "");
        }
    }

    /**
     * ?????????
     *
     * @param player
     * @param sub
     * @param from
     */
    private void subCredit(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            player.lord.setCredit(player.lord.getCredit() - sub);
            LogLordHelper.credit(from, player.account, player.lord, player.lord.getCredit(), -sub);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.CREDIT, -sub, player.lord.getCredit(), "[]", "");
        }
    }

    /**
     * ?????????q
     *
     * @param player
     * @param sub
     * @param from
     */
    private void subHeroToken(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            player.lord.setHeroToken(player.lord.getHeroToken() - sub);
            LogLordHelper.heroToken(from, player.account, player.lord, player.lord.getHeroToken(), -sub);
            rankDataManager.setExploit(player.lord);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.HERO_TOKEN, -sub, player.lord.getHeroToken(), "[]", "");
        }
    }

    /*-----------------------------------------????????????end-------------------------------------------*/

    /*-----------------------------------------checkAndsub??????end-------------------------------------------*/

    /**
     * ?????????
     *
     * @param player
     * @param sub
     * @param from
     */
    public void subPower(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            if (player.lord.getPower() >= Constant.POWER_MAX) {
                player.lord.setPowerTime(TimeHelper.getCurrentSecond());
            }
            player.lord.setPower(player.lord.getPower() - sub);
            LogLordHelper.power(from, player.account, player.lord, player.lord.getPower(), -sub);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.ACT, -sub, player.lord.getPower(), "[]", "");
        }
    }

    /**
     * ?????????
     *
     * @param player
     * @param sub
     * @param from
     */
    public void subExploit(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            player.lord.setExploit(player.lord.getExploit() - sub);
            LogLordHelper.exploit(from, player.account, player.lord, player.lord.getExploit(), -sub);
            rankDataManager.setExploit(player.lord);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.EXPLOIT, -sub, player.lord.getExploit(), "[]", "");
        }
    }

    /**
     * ?????????
     *
     * @param player
     * @param sub
     * @param from
     * @param param
     */
    public void subGold(Player player, int sub, AwardFrom from, Object... param) {
        subGold(player, sub, true, from, param);
    }

    /**
     * ????????????
     *
     * @param player  ????????????
     * @param sub     ????????????
     * @param from    ????????????
     * @param joinAct ????????????
     * @param param   ??????
     */
    public void subGold(Player player, int sub, boolean joinAct, AwardFrom from, Object... param) {
        if (sub <= 0) {
            return;
        }

        Lord lord = player.lord;
        if (sub > lord.getGold()) {
            sub = lord.getGold();
        }
        lord.setGold(lord.getGold() - sub);
        lord.setGoldCost(lord.getGoldCost() + sub);

        if (joinAct) { // ????????????
            activityDataManager.updActivity(player, ActivityConst.ACT_COST_GOLD, sub, 0, true);
            activityDataManager.updActivity(player, ActivityConst.ACT_ORE_TURNPLATE, sub, 0, true);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_COST_GOLD, sub);
            activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_COST_GOLD, sub);

            try {
                // ????????????
                activityDataManager.updRankActivity(player, ActivityConst.ACT_CONSUME_GOLD_RANK, sub);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }

        if (!ArrayUtils.contains(NOT_COUNTED_IN_CONSUMPTION, from)) {
            //????????????-????????????
            ActivityDiaoChanService.completeTask(player, ETask.CONSUME_DIAMOND, sub);
            TaskService.processTask(player, ETask.CONSUME_DIAMOND, sub);

            TaskService.handleTask(player, ETask.CONSUME_DIAMOND, sub);
            titleService.processTask(player, ETask.FIRST_USE_DIAMOND, sub);
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_GOLD_CNT, sub);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_SUB_GOLD_CNT, sub);
            // ??????????????????????????????
            campService.receiveCostGold(player, sub);
            // ?????????????????????????????????
            activityDataManager.updGlobalActivity(player, ActivityConst.ACT_BRAVEST_ARMY, sub, player.lord.getCamp());
        }

        LogLordHelper.gold(from, player.account, lord, -sub, 0, param);
    }

    /**
     * ???????????????????????????
     */
    static final AwardFrom[] NOT_COUNTED_IN_CONSUMPTION = new AwardFrom[]{AwardFrom.JOIN_IN_BIDDING_AUCTION};

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????<br>
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param subList ???????????????????????????????????????????????????List??????????????????3
     * @param from
     * @throws MwException
     */
    public void subPlayerResHasChecked(Player player, List<List<Integer>> subList, boolean sync, AwardFrom from,
                                       Object... param) throws MwException {
        if (null == player || CheckNull.isEmpty(subList)) {
            return;
        }

        ChangeInfo change = ChangeInfo.newIns();
        for (List<Integer> list : subList) {
            if (list.get(2) == 0) {
                continue;
            }
            subPlayerResHasChecked(player, list.get(0), list.get(1), list.get(2), from, param);
            // ??????????????????????????????????????????
            change.addChangeType(list.get(0), list.get(1));
        }
        if (sync) {
            // ????????????????????????????????????
            syncRoleResChanged(player, change);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param subList
     * @param from
     * @param param
     * @return
     * @throws MwException
     */
    public List<CommonPb.ChangeInfo> subPlayerResHasCheckedAndNoSync(Player player, List<List<Integer>> subList, int num, AwardFrom from,
                                                                     Object... param) throws MwException {
        if (null == player || CheckNull.isEmpty(subList)) {
            return null;
        }

        ChangeInfo change = ChangeInfo.newIns();
        for (List<Integer> list : subList) {
            if (list.get(2) == 0) {
                continue;
            }
            subPlayerResHasChecked(player, list.get(0), list.get(1), list.get(2) * num, from, param);
            // ??????????????????????????????????????????
            change.addChangeType(list.get(0), list.get(1));
        }

        if (player.isRobot || null == change || change.isEmpty() || player.ctx == null) {
            return null;
        }

        int type;
        int id;
        long count;
        List<CommonPb.ChangeInfo> list = new ArrayList<>(change.getChangeLen());
        for (int i = 0; i < change.getChangeLen(); i++) {
            type = change.getType(i);
            id = change.getId(i);
            count = getRoleResByType(player, type, id);

            list.add(createChangeInfoPb(type, id, count));
        }

        return list;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????<br>
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param subList ???????????????????????????????????????????????????List??????????????????3
     * @param num     ??????
     * @param from
     * @throws MwException
     */
    public void subPlayerResHasChecked(Player player, List<List<Integer>> subList, int num, AwardFrom from,
                                       Object... param) throws MwException {
        if (null == player || CheckNull.isEmpty(subList)) {
            return;
        }

        ChangeInfo change = ChangeInfo.newIns();
        for (List<Integer> list : subList) {
            if (list.get(2) == 0) {
                continue;
            }
            subPlayerResHasChecked(player, list.get(0), list.get(1), list.get(2) * num, from, param);
            // ??????????????????????????????????????????
            change.addChangeType(list.get(0), list.get(1));
        }
        // ????????????????????????????????????
        syncRoleResChanged(player, change);
    }

    /**
     * @param player
     * @param type
     * @param id
     * @param num
     * @param from
     * @param param
     * @return void
     * @throws MwException
     * @Title: subPlayerResHasChecked
     * @Description: ??????????????????
     */
    public void subPlayerResHasChecked(Player player, int type, int id, int num, AwardFrom from, Object... param)
            throws MwException {
        if (null == player || num == 0) {
            return;
        }
        switch (type) {
            case AwardType.MONEY:
                subMoney(player, id, num, from, param);
                break;
            case AwardType.RESOURCE:
                subResource(player, id, num, from);
                break;
            case AwardType.ARMY:
                subArmyResource(player, id, num, from);
                break;
            case AwardType.PROP:
                subProp(player, id, num, from, param);
                break;
            case AwardType.STONE:
                subStone(player, id, num, from);
                break;
            case AwardType.PLANE_CHIP:
                subPlaneChip(player, id, num, from);
                break;
            case AwardType.JEWEL:
                subEquipJewel(player, id, num, from);
                break;
            case AwardType.SANDTABLE_SCORE:
                subSandTableScore(player, id, num, from, param);
                break;
            case AwardType.SPECIAL:
                subSpecial(player, id, num, from, param);
                break;
            // ??????????????????
            case AwardType.HERO_FRAGMENT:
                operationHeroFragment(player, id, num, from, false, false, param);
                break;
            default:
                break;
        }
    }

//    private void subTotemChip(Player player,int id,int num,AwardFrom awardFrom,Object...params){
//        TotemChip totemChip = player.getTotemData().getTotemChip(id);
//        if(Objects.nonNull(totemChip)){
//            totemChip.setCount(totemChip.getCount() - num);
//            if(totemChip.getCount() <= 0){
//                player.getTotemData().getTotemChipMap().remove(id);
//            }
//            int curCount = (int) getRoleResByType(player,AwardType.TOTEM_CHIP,id);
//            LogLordHelper.prop(awardFrom,player.account,player.lord,id,curCount,num,0,params);
//        }
//    }

    public void subSpecial(Player player, int specialType, long count, AwardFrom from, Object... param) throws MwException {
        if (null == player) {
            return;
        }
        switch (specialType) {
            case AwardType.Special.SEASON_TALENT_STONE:
                subSeasonTalentStone(player, (int) count, from, param);
                break;
            case AwardType.Special.DRAW_PERMANENT_CARD_COUNT:
                operationSystemCount(player, specialType, (int) count, from, false, param);
                break;
            default:
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, specialType :%d not found !!!", player.getLordId(), specialType));
        }
    }

    /**
     * ???????????????
     *
     * @param player
     * @param sub
     * @param from
     * @param params
     */
    private void subSeasonTalentStone(Player player, int sub, AwardFrom from, Object[] params) {
        if (sub > 0) {
            SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
            int have = talent.getRemainStone();
            talent.setRemainStone(have - sub);
            LogLordHelper.seasonTalentStone(from, player.account, player.lord, have, -sub, params);
        }
    }

    ;

    /**
     * @param player
     * @param id     subtype
     * @param num
     * @param from
     */
    private void subSandTableScore(Player player, int id, int num, AwardFrom from, Object... param) {
        if (num > 0) {
            player.setSandTableScore(player.getSandTableScore() - num);
            LogLordHelper.sandTableScore(from, player, -num, param);
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param subList
     * @param from
     * @param param   ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @throws MwException
     */
    public void checkAndSubPlayerRes(Player player, List<List<Integer>> subList, AwardFrom from, Object... param)
            throws MwException {
        checkAndSubPlayerRes(player, subList, true, from, param);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param subList
     * @param num     ??????
     * @param from
     * @param param   ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @throws MwException
     */
    public void checkAndSubPlayerRes(Player player, List<List<Integer>> subList, int num, AwardFrom from,
                                     Object... param) throws MwException {
        // ??????????????????
        checkPlayerResIsEnough(player, subList, num);

        // ????????????
        subPlayerResHasChecked(player, subList, num, from, param);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param subList
     * @param sync
     * @param from    ?????????????????????????????????
     * @param param   ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @throws MwException
     */
    public void checkAndSubPlayerRes(Player player, List<List<Integer>> subList, boolean sync, AwardFrom from,
                                     Object... param) throws MwException {
        // ??????????????????
        checkPlayerResIsEnough(player, subList);
        // ????????????
        subPlayerResHasChecked(player, subList, sync, from, param);
    }

    /**
     * ?????????????????????????????????,?????????????????????(??????????????????)
     *
     * @param player
     * @param type
     * @param id
     * @param from
     * @param param
     * @throws MwException
     */
    public void checkAndSubPlayerAllRes(Player player, int type, int id, AwardFrom from, Object... param)
            throws MwException {
        int num = new Long(getRoleResByType(player, type, id)).intValue();
        if (num > 0) {
            checkAndSubPlayerRes(player, type, id, num, from, true, param);
        }
    }

    /*-----------------------------------------checkAndsub??????end-------------------------------------------*/

    /*-----------------------------------------??????????????????start-------------------------------------------*/

    /**
     * ????????????????????????????????????(???????????????)
     *
     * @param player
     * @param type
     * @param id
     * @param num
     * @param from
     * @param param
     * @throws MwException
     */
    public void checkAndSubPlayerResHasSync(Player player, int type, int id, int num, AwardFrom from, Object... param)
            throws MwException {
        checkAndSubPlayerRes(player, type, id, num, from, true, param);
    }

    /**
     * ????????????????????????????????????(?????????????????????)
     *
     * @param player
     * @param type
     * @param id
     * @param num
     * @param from
     * @param syn
     * @param param
     * @throws MwException
     */
    public void checkAndSubPlayerRes(Player player, int type, int id, int num, AwardFrom from, boolean syn,
                                     Object... param) throws MwException {
        // ??????????????????
        checkPlayerResIsEnough(player, type, id, num);
        // ????????????
        subPlayerResHasChecked(player, type, id, num, from, param);
        if (syn) {
            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(type, id);
            // ????????????????????????????????????
            syncRoleResChanged(player, change);
        }

    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param num
     * @param from
     * @param param
     * @throws MwException
     */
    public void subPlayerResCanSubCount(Player player, int type, int id, int num, AwardFrom from, Object... param) throws MwException {
        // ??????????????????
        long playerResCanSubNum = getPlayerResCanSubNum(player, type, id, num);
        // ????????????
        subPlayerResHasChecked(player, type, id, (int)playerResCanSubNum, from, param);
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(type, id);
        // ????????????????????????????????????
        syncRoleResChanged(player, change);
    }

    public void subAndSyncProp(Player player, int type, int propId, int count, AwardFrom from, Object... params) throws MwException {
        subPlayerResHasChecked(player, type, propId, count, from, params);
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(type, propId);
        // ????????????????????????????????????
        syncRoleResChanged(player, change);
    }

    public void checkAndSubPlayerRes4List(Player player, List<Integer> subList, AwardFrom from, Object... param)
            throws MwException {

        if (null == player || CheckNull.isEmpty(subList) || subList.size() < 3) {
            LogUtil.error("???????????????????????????????????????????????????????????????????????????, needList:", subList);
            return;
        }

        if (subList.get(2) == 0) {
            return;
        }
        // ??????????????????
        checkPlayerResIsEnough(player, subList.get(0), subList.get(1), subList.get(2));

        // ????????????
        subPlayerResHasChecked(player, subList.get(0), subList.get(1), subList.get(2), from, param);

        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(subList.get(0), subList.get(1));
        // ????????????????????????????????????
        syncRoleResChanged(player, change);
    }

    /**
     * ??????????????????????????? false ??????????????????
     *
     * @param player
     * @param type   ??????
     * @param id     ??????
     * @param add    ??????
     * @return
     */
    public boolean checkResource(Player player, int type, int id, long add) {
        if ((type != AwardType.RESOURCE && type != AwardType.ARMY) || add <= 0) {
            return false;
        }
        if (type == AwardType.RESOURCE) {
            Resource resource = player.resource;
            switch (id) {
                case AwardType.Resource.ELE:
                    return resource.getElec() >= add;
                case AwardType.Resource.FOOD:
                    return resource.getFood() >= add;
                case AwardType.Resource.OIL:
                    return resource.getOil() >= add;
                case AwardType.Resource.ORE:
                    return resource.getOre() >= add;
                case AwardType.Resource.HUMAN:
                    return resource.getHuman() >= add;
                case AwardType.Resource.URANIUM:
                    return resource.getUranium() >= add;
            }
        } else if (type == AwardType.ARMY) {
            Resource resource = player.resource;
            switch (id) {
                case AwardType.Army.FACTORY_1_ARM:
                    return resource.getArm1() >= add;
                case AwardType.Army.FACTORY_2_ARM:
                    return resource.getArm2() >= add;
                case AwardType.Army.FACTORY_3_ARM:
                    return resource.getArm3() >= add;
            }
        }

        return false;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param needList ???????????????????????????
     * @param message  ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @throws MwException
     */
    public void checkPlayerResIsEnough(Player player, List<List<Integer>> needList, String... message)
            throws MwException {
        if (null == player || CheckNull.isEmpty(needList)) {
            return;
        }

        for (List<Integer> list : needList) {
            if (CheckNull.isEmpty(list) || list.size() < 3) {
                LogUtil.error(message, "???????????????????????????????????????????????????????????????????????????, needList:", needList);
                return;
            }

            if (list.get(2) == 0) {
                continue;
            }

            checkPlayerResIsEnough(player, list.get(0), list.get(1), list.get(2), message);
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param needList
     * @return
     */
    public boolean checkPlayerResourceIsEnough(Player player, List<List<Integer>> needList) {
        if (null == player || CheckNull.isEmpty(needList)) {
            return false;
        }
        try {
            for (List<Integer> list : needList) {
                if (CheckNull.isEmpty(list) || list.size() < 3) {
                    LogUtil.error("???????????????????????????????????????????????????????????????????????????, needList:", needList);
                    return false;
                }
                if (list.get(2) == 0) {
                    continue;
                }
                checkPlayerResIsEnough(player, list.get(0), list.get(1), list.get(2));
            }
            return true;
        } catch (Exception e) {
            LogUtil.error("?????????????????????????????????,???????????????,needList:", needList);
        }
        return false;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param needList
     * @param num      ??????
     * @param message
     * @throws MwException
     */
    public void checkPlayerResIsEnough(Player player, List<List<Integer>> needList, int num, String... message)
            throws MwException {
        if (null == player || CheckNull.isEmpty(needList)) {
            return;
        }

        for (List<Integer> list : needList) {
            if (CheckNull.isEmpty(list) || list.size() < 3) {
                LogUtil.error(message, "???????????????????????????????????????????????????????????????????????????, needList:", needList);
                return;
            }

            if (list.get(2) == 0) {
                continue;
            }

            checkPlayerResIsEnough(player, list.get(0), list.get(1), list.get(2) * num, message);
        }
    }

    /**
     * @param player
     * @param type
     * @param id
     * @param num
     * @param message
     * @return void
     * @throws MwException
     * @Title: checkPlayerResIsEnough
     * @Description: ?????? ????????????????????????
     */
    public void checkPlayerResIsEnough(Player player, int type, int id, int num, String... message) throws MwException {
        if (null == player || num == 0) {
            return;
        }

        switch (type) {
            case AwardType.MONEY:
                checkMoneyIsEnough(player, id, num, message);
                break;
            case AwardType.RESOURCE:
                checkResourceIsEnough(player.resource, id, num, message);
                break;
            case AwardType.ARMY:
                checkArmyIsEnough(player.resource, id, num, message);
                break;
            case AwardType.PROP:
                checkPropIsEnough(player, id, num, message);
                break;
            case AwardType.STONE:
                checkStoneIsEnought(player, id, num, message);
                break;
            case AwardType.PLANE_CHIP:
                checkChipIsEnought(player, id, num, message);
                break;
            case AwardType.JEWEL:
                checkEquipJewel(player, id, num, message);
                break;
            case AwardType.SANDTABLE_SCORE:
                checkSandTableScoreEnought(player, id, num, message);
                break;
            case AwardType.SPECIAL:
                checkSpecialIsEnough(player, id, num, message);
                break;
            case AwardType.HERO_FRAGMENT:
                checkHeroFragmentIsEnough(player, id, num, message);
                break;
            default:
                break;
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param num
     * @return
     */
    public long getPlayerResCanSubNum(Player player, int type, int id, int num) {
        switch (type) {
            case AwardType.MONEY:
                return getCanSubMoneyNum(player, id, num);
            case AwardType.RESOURCE:
                return getCanSubResourceNum(player.resource, id, num);
            case AwardType.ARMY:
                return getCanSubArmyNum(player.resource, id, num);
            case AwardType.PROP:
                return getCanSubPropNum(player, id, num);
            case AwardType.STONE:
                return getCanSubStoneNum(player, id, num);
            case AwardType.PLANE_CHIP:
                return getCanSubChipNum(player, id, num);
            case AwardType.JEWEL:
                return getCanSubEquipJewelNum(player, id, num);
            case AwardType.SANDTABLE_SCORE:
                return getCanSubSandTableScoreNum(player, num);
            case AwardType.SPECIAL:
                return getCanSubSpecialNum(player, id, num);
            case AwardType.HERO_FRAGMENT:
                return getCanSubHeroFragmentNum(player, id, num);
            default:
                return 0L;
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param heroId
     * @param need
     * @param message
     * @throws MwException
     */
    private void checkHeroFragmentIsEnough(Player player, int heroId, long need, String... message) throws MwException {
        Integer ownNum = player.getDrawCardData().getFragmentData().getOrDefault(heroId, 0);
        if (CheckNull.isNull(ownNum) || ownNum < need) {
            throw new MwException(GameError.NOT_ENOUGH_HERO_FRAGMENTS, String.format("player:%d, not enough hero fragments, ownNum:%d, need:%d, heroId:%d, message:%s",
                    player.roleId, ownNum, need, heroId, message));
        }
    }

    /**
     * ??????????????????????????????????????????
     * @param player
     * @param heroId
     * @param need
     * @throws MwException
     */
    private long getCanSubHeroFragmentNum(Player player, int heroId, long need) throws MwException {
        Integer ownNum = player.getDrawCardData().getFragmentData().getOrDefault(heroId, 0);
        return Math.min(ownNum, need);
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param specialId
     * @param need
     * @param message
     * @throws MwException
     */
    private void checkSpecialIsEnough(Player player, int specialId, long need, String... message) throws MwException {
        switch (specialId) {
            case AwardType.Special.SEASON_TALENT_STONE:
                checkSeasonTalentStoneIsEnough(player, need);
                break;
            default:
                throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, String.format("roleId :%d, type :%d, id :%d, not found",
                        player.getLordId(), AwardType.SPECIAL, specialId));
        }
    }

    private long getCanSubSpecialNum(Player player, int specialId, long need) throws MwException {
        switch (specialId) {
            case AwardType.Special.SEASON_TALENT_STONE:
                return getCanSubSeasonTalentStoneNum(player, need);
            default:
                return 0;
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param need
     * @throws MwException
     */
    private void checkSeasonTalentStoneIsEnough(Player player, long need) throws MwException {
        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        int have = talent.getRemainStone();
        if (have < need) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), String.format("roleId :%d, ????????????????????? need :%d, have :%d",
                    player.getLordId(), need, have));
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param player
     * @param need
     * @return
     * @throws MwException
     */
    private long getCanSubSeasonTalentStoneNum(Player player, long need) throws MwException {
        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        int have = talent.getRemainStone();
        return Math.min(have, need);
    }

    private void checkSandTableScoreEnought(Player player, int id, int num, String... message) throws MwException {
        if (player.getSandTableScore() < num) {
            throw new MwException(GameError.SANDTABLE_CONTEST_SCORE_NOT_ENOUGHT.getCode(), message, " ????????????????????????, roleId:", player.getLordId(),
                    ", type:", id, ", need:", num, ", have:", player.getSandTableScore());
        }
    }

    private int getCanSubSandTableScoreNum(Player player, int need) throws MwException {
        return Math.min(player.getSandTableScore(), need);
    }

    /**
     * ????????????????????????????????????
     *
     * @param player  ????????????
     * @param id      ????????????
     * @param num     ??????
     * @param message ??????
     * @throws MwException ???????????????
     */
    private void checkEquipJewel(Player player, int id, int num, String[] message) throws MwException {
        if (null == player) {
            return;
        }
        EquipJewel equipJewel = player.equipJewel.get(id);
        if (null == equipJewel || equipJewel.canUseCnt() < num) {
            throw new MwException(GameError.STONE_NOT_ENOUGH.getCode(), message, " ??????????????????, roleId:", player.roleId,
                    ", id:", id, ", need:", num, ", have:", null == equipJewel ? 0 : equipJewel.getCount(),
                    ", canUseCnt:", equipJewel.canUseCnt());
        }
    }

    /**
     * ??????????????????????????????????????????
     * @param player
     * @param id
     * @param need
     * @return
     * @throws MwException
     */
    private int getCanSubEquipJewelNum(Player player, int id, int need) throws MwException {
        EquipJewel equipJewel = player.equipJewel.get(id);
        if (null != equipJewel) {
            return Math.min(equipJewel.canUseCnt(), need);
        } else {
            return 0;
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param message
     * @throws MwException
     */
    private void checkChipIsEnought(Player player, int id, int count, String[] message) throws MwException {
        if (null == player) {
            return;
        }
        PlaneChip planeChip = player.getPlaneChip(id);
        if (CheckNull.isNull(planeChip) || planeChip.getCnt() < count) {
            throw new MwException(GameError.PLANE_CHIP_NOT_ENOUGH.getCode(), message, " ????????????????????????, roleId:",
                    player.roleId, ", id:", id, ", need:", count, ", have:", planeChip.getCnt());
        }
    }

    /**
     * ??????????????????????????????????????????
     * @param player
     * @param id
     * @param need
     * @return
     * @throws MwException
     */
    private int getCanSubChipNum(Player player, int id, int need) throws MwException {
        PlaneChip planeChip = player.getPlaneChip(id);
        return Math.min(planeChip.getCnt(), need);
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param moneyType
     * @param need
     * @param message   ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @throws MwException
     */
    public void checkMoneyIsEnough(Player player, int moneyType, int need, String... message) throws MwException {
        Lord lord = player.lord;
        long count = 0;
        int code = GameError.MONEY_NOT_ENOUGH.getCode();
        switch (moneyType) {
            case AwardType.Money.EXP:
                count = lord.getExp();
                break;
            case AwardType.Money.VIP_EXP:
                count = lord.getVip();
                break;
            case AwardType.Money.GOLD:
                count = lord.getGold();
                code = GameError.GOLD_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.ACT:
                count = lord.getPower();
                code = GameError.ACT_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.EXPLOIT:
                count = lord.getExploit();
                code = GameError.EXPLOIT_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.HERO_TOKEN:
                count = lord.getHeroToken();
                code = GameError.HERO_TOKEN_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.CREDIT:
                count = lord.getCredit();
                code = GameError.CREDIT_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.HONOR:
                count = lord.getHonor();
                code = GameError.HONOR_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.GOLD_BAR:
                count = lord.getGoldBar();
                code = GameError.GOLD_BAR_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.MENTOR_BILL:
                count = player.getMixtureDataById(PlayerConstant.MENTOR_BILL);
                code = GameError.MENTOR_BILL_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.GOLD_INGOT:
                count = player.getMixtureDataById(PlayerConstant.GOLD_INGOT);
                code = GameError.GOLD_INGOT_NOT_ENOUGH.getCode();
                break;
            case AwardType.Money.WAR_FIRE_COIN:
                count = player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE);
                break;
            case AwardType.Money.FISH_SCORE:
                count = player.getFishingData().getScore();
                break;
            case AwardType.Money.TREASURE_WARE_GOLDEN:
                count = player.lord.getTreasureWareGolden();
                break;
            case AwardType.Money.TREASURE_WARE_DUST:
                count = player.lord.getTreasureWareDust();
                break;
            case AwardType.Money.TREASURE_WARE_ESSENCE:
                count = player.lord.getTreasureWareEssence();
                break;
            case AwardType.Money.CROSS_WAR_FIRE_COIN:
                count = player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE);
                break;
            case AwardType.Money.ANCIENT_BOOK:
                count = player.lord.getAncientBook();
                break;
            default:
                break;
        }
        if (count < need) {
            throw new MwException(code, message, " ??????????????????, roleId:", lord.getLordId(), ", type:", moneyType, ", need:",
                    need, ", have:", count);
        }
    }

    /**
     * ????????????????????????????????????
     * @param player
     * @param moneyType
     * @param need
     * @return
     * @throws MwException
     */
    public long getCanSubMoneyNum(Player player, int moneyType, int need) throws MwException {
        Lord lord = player.lord;
        long count = 0;
        switch (moneyType) {
            case AwardType.Money.EXP:
                count = lord.getExp();
                break;
            case AwardType.Money.VIP_EXP:
                count = lord.getVip();
                break;
            case AwardType.Money.GOLD:
                count = lord.getGold();
                break;
            case AwardType.Money.ACT:
                count = lord.getPower();
                break;
            case AwardType.Money.EXPLOIT:
                count = lord.getExploit();
                break;
            case AwardType.Money.HERO_TOKEN:
                count = lord.getHeroToken();
                break;
            case AwardType.Money.CREDIT:
                count = lord.getCredit();
                break;
            case AwardType.Money.HONOR:
                count = lord.getHonor();
                break;
            case AwardType.Money.GOLD_BAR:
                count = lord.getGoldBar();
                break;
            case AwardType.Money.MENTOR_BILL:
                count = player.getMixtureDataById(PlayerConstant.MENTOR_BILL);
                break;
            case AwardType.Money.GOLD_INGOT:
                count = player.getMixtureDataById(PlayerConstant.GOLD_INGOT);
                break;
            case AwardType.Money.WAR_FIRE_COIN:
                count = player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE);
                break;
            case AwardType.Money.FISH_SCORE:
                count = player.getFishingData().getScore();
                break;
            case AwardType.Money.TREASURE_WARE_GOLDEN:
                count = player.lord.getTreasureWareGolden();
                break;
            case AwardType.Money.TREASURE_WARE_DUST:
                count = player.lord.getTreasureWareDust();
                break;
            case AwardType.Money.TREASURE_WARE_ESSENCE:
                count = player.lord.getTreasureWareEssence();
                break;
            case AwardType.Money.CROSS_WAR_FIRE_COIN:
                count = player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE);
                break;
            case AwardType.Money.ANCIENT_BOOK:
                count = player.lord.getAncientBook();
                break;
            default:
                break;
        }
        return Math.min(count, need);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param resource
     * @param resourceType
     * @param need
     * @param message      ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @throws MwException
     */
    private void checkResourceIsEnough(Resource resource, int resourceType, long need, String... message)
            throws MwException {
        long count = 0;
        switch (resourceType) {
            case AwardType.Resource.OIL:
                count = resource.getOil();
                break;
            case AwardType.Resource.ELE:
                count = resource.getElec();
                break;
            case AwardType.Resource.FOOD:
                count = resource.getFood();
                break;
            case AwardType.Resource.ORE:
                count = resource.getOre();
                break;
            case AwardType.Resource.HUMAN:
                count = resource.getHuman();
                break;
            case AwardType.Resource.URANIUM:
                count = resource.getUranium();
                break;
            default:
                throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " ??????????????????, roleId:",
                        resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
        if (count < need) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " ??????????????????, roleId:",
                    resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param resource
     * @param resourceType
     * @param need
     * @return
     * @throws MwException
     */
    private long getCanSubResourceNum(Resource resource, int resourceType, long need)
            throws MwException {
        long count = 0;
        switch (resourceType) {
            case AwardType.Resource.OIL:
                count = resource.getOil();
                break;
            case AwardType.Resource.ELE:
                count = resource.getElec();
                break;
            case AwardType.Resource.FOOD:
                count = resource.getFood();
                break;
            case AwardType.Resource.ORE:
                count = resource.getOre();
                break;
            case AwardType.Resource.HUMAN:
                count = resource.getHuman();
                break;
            case AwardType.Resource.URANIUM:
                count = resource.getUranium();
                break;
        }
        return Math.min(count, need);
    }

    /*-----------------------------------------??????????????????end-------------------------------------*/

    private void checkArmyIsEnough(Resource resource, int resourceType, long need, String... message)
            throws MwException {
        long count = 0;
        switch (resourceType) {
            case AwardType.Army.FACTORY_1_ARM:
                count = resource.getArm1();
                break;
            case AwardType.Army.FACTORY_2_ARM:
                count = resource.getArm2();
                break;
            case AwardType.Army.FACTORY_3_ARM:
                count = resource.getArm3();
                break;
            default:
                throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " ??????????????????, roleId:",
                        resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
        if (count < need) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " ??????????????????, roleId:",
                    resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param resource
     * @param resourceType
     * @param need
     * @return
     * @throws MwException
     */
    private long getCanSubArmyNum(Resource resource, int resourceType, long need)
            throws MwException {
        long count = 0;
        switch (resourceType) {
            case AwardType.Army.FACTORY_1_ARM:
                count = resource.getArm1();
                break;
            case AwardType.Army.FACTORY_2_ARM:
                count = resource.getArm2();
                break;
            case AwardType.Army.FACTORY_3_ARM:
                count = resource.getArm3();
                break;
        }
        return Math.min(count, need);
    }

    /*-----------------------------------------??????start-------------------------------------------*/

    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param propId
     * @param count
     * @param message ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @throws MwException
     */
    public void checkPropIsEnough(Player player, int propId, int count, String... message) throws MwException {
        if (null == player) {
            return;
        }
        if (propId <= 0) {
            return;
        }
        Prop prop = player.props.get(propId);
        if (null == prop || prop.getCount() < count) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), message, " ??????????????????, roleId:", player.roleId,
                    ", propId:", propId, ", need:", count, ", have:", null == prop ? 0 : prop.getCount());
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param propId
     * @param need
     * @return
     * @throws MwException
     */
    public int getCanSubPropNum(Player player, int propId, int need) throws MwException {
        Prop prop = player.props.get(propId);
        if (null == prop) {
            return 0;
        } else {
            return Math.min(prop.getCount(), need);
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param id
     * @param count
     * @param message
     * @throws MwException
     */
    public void checkStoneIsEnought(Player player, int id, int count, String... message) throws MwException {
        if (null == player) {
            return;
        }
        Stone stone = player.getStoneInfo().getStones().get(id);
        if (null == stone || stone.getCnt() < count) {
            throw new MwException(GameError.STONE_NOT_ENOUGH.getCode(), message, " ??????????????????, roleId:", player.roleId,
                    ", id:", id, ", need:", count, ", have:", null == stone ? 0 : stone.getCnt());
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param id
     * @param need
     * @return
     * @throws MwException
     */
    public int getCanSubStoneNum(Player player, int id, int need) throws MwException {
        Stone stone = player.getStoneInfo().getStones().get(id);
        if (null == stone) {
            return 0;
        } else {
            return Math.min(stone.getCnt(), need);
        }
    }

    /**
     * ?????????????????????????????????bool???
     *
     * @param player
     * @param propId
     * @param count
     * @return
     */
    public boolean propIsEnough(Player player, int propId, int count) {
        if (null == player) {
            return false;
        }

        Prop prop = player.props.get(propId);
        if (null == prop || prop.getCount() < count) {
            return false;
        }
        return true;
    }

    /*-----------------------------------------????????????end-------------------------------------------*/
    /*-----------------------------------------????????????start-------------------------------------------*/

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param player
     * @param awardType
     * @param id
     * @return
     */
    public long getRoleResByType(Player player, int awardType, int id) {
        long count = 0;
        switch (awardType) {
            case AwardType.MONEY:
                switch (id) {
                    case AwardType.Money.EXP:
                        count = player.lord.getExp();
                        break;
                    case AwardType.Money.VIP_EXP:
                        count = player.lord.getVipExp();
                        break;
                    case AwardType.Money.GOLD:
                        count = player.lord.getGold();
                        break;
                    case AwardType.Money.ACT:
                        count = player.lord.getPower();
                        break;
                    case AwardType.Money.EXPLOIT:
                        count = player.lord.getExploit();
                        break;
                    case AwardType.Money.HERO_TOKEN:
                        count = player.lord.getHeroToken();
                        break;
                    case AwardType.Money.CREDIT:
                        count = player.lord.getCredit();
                        break;
                    case AwardType.Money.WAR_FIRE_COIN:
                        count = player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE);
                        break;
                    case AwardType.Money.FISH_SCORE:
                        count = player.getFishingData().getScore();
                        break;
                    case AwardType.Money.CROSS_WAR_FIRE_COIN:
                        count = player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE);
                        break;
                    case AwardType.Money.TREASURE_WARE_GOLDEN:
                        count = player.lord.getTreasureWareGolden();
                        break;
                    case AwardType.Money.TREASURE_WARE_DUST:
                        count = player.lord.getTreasureWareDust();
                        break;
                    case AwardType.Money.TREASURE_WARE_ESSENCE:
                        count = player.lord.getTreasureWareEssence();
                        break;
                    case AwardType.Money.ANCIENT_BOOK:
                        count = player.lord.getAncientBook();
                        break;
                    default:
                        break;
                }
                break;
            case AwardType.RESOURCE:
                switch (id) {
                    case AwardType.Resource.OIL:
                        count = player.resource.getOil();
                        break;
                    case AwardType.Resource.ELE:
                        count = player.resource.getElec();
                        break;
                    case AwardType.Resource.FOOD:
                        count = player.resource.getFood();
                        break;
                    case AwardType.Resource.ORE:
                        count = player.resource.getOre();
                        break;
                    case AwardType.Resource.HUMAN:
                        count = player.resource.getHuman();
                        break;
                    case AwardType.Resource.URANIUM:
                        count = player.resource.getUranium();
                        break;
                    default:
                        break;
                }
                break;
            case AwardType.HERO:
                switch (id) {
                    case AwardType.Army.FACTORY_1_ARM:
                        count = player.resource.getArm1();
                        break;
                    case AwardType.Army.FACTORY_2_ARM:
                        count = player.resource.getArm2();
                        break;
                    case AwardType.Army.FACTORY_3_ARM:
                        count = player.resource.getArm3();
                        break;
                    default:
                        break;
                }
            case AwardType.PROP:
                Prop prop = player.props.get(id);
                count = prop != null ? prop.getCount() : 0;
                break;
            case AwardType.STONE:
                Stone stone = player.getStoneInfo().getStones().get(id);
                count = stone != null ? stone.getCnt() : 0;
                break;
            case AwardType.EQUIP:
                break;
            case AwardType.RANDOM:
                break;
            case AwardType.ARMY:
                switch (id) {
                    case AwardType.Army.FACTORY_1_ARM:
                        count = player.resource.getArm1();
                        break;
                    case AwardType.Army.FACTORY_2_ARM:
                        count = player.resource.getArm2();
                        break;
                    case AwardType.Army.FACTORY_3_ARM:
                        count = player.resource.getArm3();
                        break;
                    default:
                        break;
                }
                break;
            case AwardType.SPECIAL:
                switch (id) {
                    case AwardType.Special.BUILD_SPEED:
                        count = Optional.ofNullable(player.getCanAddSpeedBuildQue()).map(BuildQue::getParam).orElse(0);
                        break;
                    case AwardType.Special.ARM_SPEED:
                        count = Optional.ofNullable(player.getCanAddSpeedArmQue()).map(ArmQue::getParam).orElse(0);
                        break;
                    case AwardType.Special.BAPTIZE:
                        count = player.common.getBaptizeCnt();
                        break;
                    case AwardType.Special.HERO_WASH:
//                        count = player.common.getWashCount();
                        break;
                    case AwardType.Special.TECH_SPEED:
                        count = Optional.ofNullable(player.getCanSpeedTechQue()).map(TechQue::getParam).orElse(0);
                        break;
                    case AwardType.Special.INTERACTION_CNT:
                        count = Optional.ofNullable(player.getCia()).map(Cia::getInteractionCnt).orElse(0);
                        break;
                    case AwardType.Special.BUILD_LEVEL_UP:
                        count = Optional.ofNullable(player.building).map(Building::getCommand).orElse(0);
                        break;
                    case AwardType.Special.DIAOCHAN_SCORE:
                        count = activityDiaoChanService.getBiyueScore(player);
                        break;
                    case AwardType.Special.SEASON_SCORE:
                        count = seasonService.getSeasonScore(player);
                        break;
                    case AwardType.Special.SEASON_TALENT_STONE:
                        count = player.getPlayerSeasonData().getSeasonTalent().getRemainStone();
                        break;
                    case AwardType.Special.ACT_MUSIC_FESTIVAL_CREATIVE_SCORE:
                        count = musicFestivalCreativeService.getMusicCrtScore(player);
                        break;
                    case AwardType.Special.SHENG_WU:
                        count = player.getMilitaryExpenditure();
                        break;
                }
                break;
            case AwardType.HERO_ARM:
                // ??????????????????
                Hero hero = player.heros.get(id);
                if (null != hero) {
                    count = hero.getCount();
                }
                break;
            case AwardType.PLANE_CHIP:
                PlaneChip chip = player.palneChips.get(id);
                if (!CheckNull.isNull(chip)) {
                    count = chip.getCnt();
                }
                break;
            case AwardType.MENTOR_EQUIP:
                break;
            case AwardType.JEWEL:
                EquipJewel jewel = player.equipJewel.get(id);
                if (!CheckNull.isNull(jewel)) {
                    // ?????????????????????????????????
                    count = jewel.canUseCnt();
                }
                break;
            case AwardType.SANDTABLE_SCORE:
                count = player.getSandTableScore();
                break;
            case AwardType.HERO_FRAGMENT:
                count = player.getDrawCardData().getFragmentData().getOrDefault(id, 0);
            default:
                break;
        }
        return count;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param change
     */
    public void syncRoleResChanged(Player player, ChangeInfo change) {
        if (null == player || player.isRobot || null == change || change.isEmpty() || player.ctx == null) {
            return;
        }

        SyncChangeInfoRs.Builder builder = SyncChangeInfoRs.newBuilder();
        int type;
        int id;
        long count;
        for (int i = 0; i < change.getChangeLen(); i++) {
            type = change.getType(i);
            id = change.getId(i);
            count = getRoleResByType(player, type, id);
            builder.addInfo(createChangeInfoPb(type, id, count));
        }

        Base.Builder msg = PbHelper.createSynBase(SyncChangeInfoRs.EXT_FIELD_NUMBER, SyncChangeInfoRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param player
     * @param changeInfo
     */
    public void syncRoleResChanged(Player player, CommonPb.ChangeInfo changeInfo) {
        if (null == player || player.isRobot || null == changeInfo || player.ctx == null) {
            return;
        }

        SyncChangeInfoRs.Builder builder = SyncChangeInfoRs.newBuilder();
        builder.addInfo(changeInfo);

        Base.Builder msg = PbHelper.createSynBase(SyncChangeInfoRs.EXT_FIELD_NUMBER, SyncChangeInfoRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    public CommonPb.ChangeInfo createChangeInfoPb(int type, int id, long count) {
        CommonPb.ChangeInfo.Builder builder = CommonPb.ChangeInfo.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setCount(count);
        return builder.build();
    }

    /**
     * ???????????????
     *
     * @param player
     * @throws MwException
     */
    public void checkBagCnt(Player player) throws MwException {
        int cnt = 0;
        for (Equip e : player.equips.values()) {
            if (e.getHeroId() > 0) {
                continue;
            }
            cnt++;
        }
        if (cnt >= player.common.getBagCnt()) {
            throw new MwException(GameError.MAX_EQUIP_STORE.getCode(), "??????????????????, roleId:" + player.roleId + ",equipSize="
                    + player.equips.size() + ",bagCnt=" + player.common.getBagCnt() + ",cnt=" + cnt);
        }
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param needCnt ????????????????????????
     * @throws MwException
     */
    public void checkBagCnt(Player player, int needCnt) throws MwException {
        int cnt = 0;
        for (Equip e : player.equips.values()) {
            if (e.getHeroId() > 0) {
                continue;
            }
            cnt++;
        }
        cnt += needCnt;
        if (cnt > player.common.getBagCnt()) {
            throw new MwException(GameError.MAX_EQUIP_STORE.getCode(), "????????????????????????, roleId:", player.roleId,
                    ",equipSize=", player.equips.size(), ",bagCnt=", player.common.getBagCnt(), ",cnt=", cnt,
                    ", needCnt:", needCnt);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param awardList
     * @throws MwException
     */
    public void checkBag(Player player, List<List<Integer>> awardList) throws MwException {
        int size = awardList.size();
        int equipCnt = 0;
        for (int i = 0; i < size; i++) {
            List<Integer> e = awardList.get(i);
            int type = e.get(0);
            if (type == AwardType.EQUIP) {
                equipCnt++;
            }
        }
        if (equipCnt > 0) {
            checkBagCnt(player, equipCnt);
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param needCnt ????????????????????????
     * @throws MwException
     */
    public void checkMentorEquipBag(Player player, int needCnt) throws MwException {
        int cnt = 0;
        MentorInfo mentorInfo = player.getMentorInfo();
        int hasCnt = (int) mentorInfo.getEquipMap().values().stream().filter(me -> me.getMentorId() == 0).count();
        cnt = hasCnt + needCnt;
        if (cnt > MENTOR_EQUIP_CNT) {
            throw new MwException(GameError.MAX_EQUIP_STORE.getCode(), "????????????????????????, roleId:", player.roleId, ",hasCnt=",
                    hasCnt, ",cnt=", cnt, ", needCnt:", needCnt);
        }
    }

    private static final AwardFrom[] HAS_INFO2_AWARD_FROM = new AwardFrom[]{AwardFrom.TREASURE_ON_HOOK_AWARD};




    /*-----------------------------------------????????????end-------------------------------------------*/
}
