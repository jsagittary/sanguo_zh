package com.gryphpoem.game.zw.manager;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
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
import com.gryphpoem.game.zw.resource.pojo.hero.TalentData;
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
 * 使用说明
 * <p>
 * 1发奖励时: send 开头的方法给予的奖励是带同步; add 开头的方法都是不带同步的
 * <p>
 * 2 不要在 Object... param 填上无用字符串,此参数为了打印埋点额外参数使用
 *
 * @author TanDonghai
 * @ClassName RewardDataManager.java
 * @Description 奖励相关专用管理类
 * @date 创建时间：2017年3月28日 下午2:55:28
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
     * 合并奖励
     *
     * @param awardList
     * @return 返回null
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
     * 合并奖励
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
     * 发送奖励(默认不翻倍)
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
     * 发送奖励 带倍数
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
                LogUtil.error("发送奖励，奖励列表的格式不正确，跳过, list:" + list);
                continue;
            }
            Award addAwardSignle = addAwardSignle(player, list, num, from, param);
            if (addAwardSignle == null) {
                continue;
            }
            awards.add(addAwardSignle);
            // 记录更改过的玩家游戏资源类型
            change.addChangeType(addAwardSignle.getType(), addAwardSignle.getId());
        }

        // 向客户端同步玩家资源数据
        syncRoleResChanged(player, change);
        return awards;
    }

    /**
     * 给单个奖励带同步
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
     * 发送奖励延迟推送(需要同步 ChangeInfo 不能为null,并且在调用此方法后收到调用同步)
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
                LogUtil.error("发送奖励，奖励列表的格式不正确，跳过, list:" + list);
                continue;
            }
            Award addAwardSignle = addAwardSignle(player, list, 1, from, param);
            if (addAwardSignle == null) {
                continue;
            }
            awards.add(addAwardSignle);
            if (change != null) {
                // 记录更改过的玩家游戏资源类型
                change.addChangeType(list.get(0), list.get(1));
            }
        }
        return awards;
    }

    /**
     * 根据CommonPb.Award对象进行发送奖励
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
            // 记录更改过的玩家游戏资源类型
            change.addChangeType(award.getType(), award.getId());
        }
        // 向客户端同步玩家资源数据
        syncRoleResChanged(player, change);
    }

    /**
     * 获得单个奖励(包含随机奖励,默认不翻倍)
     *
     * @param player
     * @param rewardList
     * @param from
     */
    public CommonPb.Award addAwardSignle(Player player, List<Integer> rewardList, AwardFrom from, Object... param) {
        return addAwardSignle(player, rewardList, 1, from, param);
    }

    /**
     * 获得单个奖励(包含随机奖励,带倍数)
     *
     * @param player
     * @param rewardList
     * @param num        倍数
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
        // 处理4个参数为概率掉落的
        if (rewardList.size() == 4) {
            if (!RandomHelper.isHitRangeIn10000(rewardList.get(3))) {
                return null;
            }
        }
        return addAwardSignle(player, type, id, count, from, param);
    }

    /**
     * 获得奖励单个奖励(包含随机奖励)
     *
     * @param player
     * @param type   类型
     * @param id
     * @param count  数量
     * @param from
     * @param param
     * @return null说明没有获取
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
     * 获得奖励单个奖励,值返回key值(不包含随机奖励)
     *
     * @param player 玩家
     * @param type   奖励类型，AwardType类中定义的类型
     * @param id     细分类型或物品id
     * @param count  数量
     * @param from   奖励来源，用于记录行为
     * @return
     */
    public int addAward(Player player, int type, int id, int count, AwardFrom from, Object... param) {
        return addAwardBase(player, type, id, count, from, param);
    }

    /**
     * 邮件领取物品，添加参数
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
     * 获得奖励单个奖励总入口
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @param param
     * @return 值返回key值
     */
    private Award addAwardBase_(Player player, int type, int id, int count, int keyId, AwardFrom from, Object... param) throws MwException {
        Award.Builder award = Award.newBuilder().setType(type).setId(id).setCount(count);
        // 转换
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
                // 头像
                addPortrait(player, id, count, convert, from, param);
                break;
            case AwardType.STONE:
                // 宝石相关
                addStone(player, id, (int) count, from, param);
                break;
            case AwardType.CHAT_BUBBLE:
                // 聊天气泡框
                addChatBubble(player, id, count, convert, from, param);
                break;
            case AwardType.PLANE:
                // 战机
                addPlane(player, id, from, param);
                break;
            case AwardType.PLANE_CHIP:
                // 战机碎片
                addPlaneChip(player, id, count, from, param);
                break;
            case AwardType.MEDAL:
                // 勋章
                addMedal(player, id, 0, count, from, param);
                break;
            case AwardType.MENTOR_EQUIP:
                // 教官装备
                addMentorEquip(player, id, count, from, param);
                break;
            case AwardType.JEWEL:
                // 装备宝石
                addEquipJewel(player, id, count, from, param);
                break;
            case AwardType.CASTLE_SKIN:
                // 城堡皮肤
                addCastleSkin(player, id, count, convert, from, param);
                break;
            case AwardType.SANDTABLE_SCORE:
                // 沙盘积分
                addSandTableScore(player, count, from, param);
                break;
            case AwardType.PORTRAIT_FRAME:
                // 头像框
                addPortraitFrame(player, id, count, convert, from, param);
                break;
            case AwardType.NAMEPLATE:
                // 铭牌
                addNameplate(player, id, count, convert, from, param);
                break;
            case AwardType.MARCH_SPECIAL_EFFECTS:
                // 行军特效
                addMarchLine(player, id, count, convert, from, param);
                break;
            case AwardType.TREASURE_WARE:
                //添加宝具
                addTreasureWareInMail(player, id, count, keyId, from, param);
                break;
            //添加称号
            case AwardType.TITLE:
                addTitle(player, id, count, convert, from, param);
                break;
            // 添加英雄碎片
            case AwardType.HERO_FRAGMENT:
                operationHeroFragment(player, id, count, from, true, false, param);
                break;
            default:
                break;
        }
        // 转换后的奖励
        if (!convert.isEmpty()) {
            award.addAllConvert(convert);
        }

        return award.build();
    }

    /**
     * 添加系统次数
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
     * 添加常驻抽卡次数
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
     * 增加或减少武将碎片
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

        // 武将碎片变更
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
     * 增加指定武将品阶碎片
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

        // 武将碎片变更
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
     * 获得奖励单个奖励总入口
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @param param
     * @return 值返回key值
     */
    private int addAwardBase(Player player, int type, int id, int count, AwardFrom from, Object... param) {
        return addAwardBase_(player, type, id, count, from, param).getKeyId();
    }

    /**
     * 获得奖励单个奖励总入口
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param from
     * @param param
     * @return 值返回key值
     */
    private Award addAwardBase_(Player player, int type, int id, int count, AwardFrom from, Object... param) {
        Award.Builder award = Award.newBuilder().setType(type).setId(id).setCount(count);
        // 转换
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
                // 头像
                addPortrait(player, id, count, convert, from, param);
                break;
            case AwardType.STONE:
                // 宝石相关
                addStone(player, id, (int) count, from, param);
                break;
            case AwardType.CHAT_BUBBLE:
                // 聊天气泡框
                addChatBubble(player, id, count, convert, from, param);
                break;
            case AwardType.PLANE:
                // 战机
                addPlane(player, id, from, param);
                break;
            case AwardType.PLANE_CHIP:
                // 战机碎片
                addPlaneChip(player, id, count, from, param);
                break;
            case AwardType.MEDAL:
                // 勋章
                addMedal(player, id, 0, count, from, param);
                break;
            case AwardType.MENTOR_EQUIP:
                // 教官装备
                addMentorEquip(player, id, count, from, param);
                break;
            case AwardType.JEWEL:
                // 装备宝石
                addEquipJewel(player, id, count, from, param);
                break;
            case AwardType.CASTLE_SKIN:
                // 城堡皮肤
                addCastleSkin(player, id, count, convert, from, param);
                break;
            case AwardType.SANDTABLE_SCORE:
                // 沙盘积分
                addSandTableScore(player, count, from, param);
                break;
            case AwardType.PORTRAIT_FRAME:
                // 头像框
                addPortraitFrame(player, id, count, convert, from, param);
                break;
            case AwardType.NAMEPLATE:
                // 铭牌
                addNameplate(player, id, count, convert, from, param);
                break;
            case AwardType.MARCH_SPECIAL_EFFECTS:
                // 行军特效
                addMarchLine(player, id, count, convert, from, param);
                break;
            case AwardType.TREASURE_WARE:
                //添加宝具
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
        // 转换后的奖励
        if (!convert.isEmpty()) {
            award.addAllConvert(convert);
        }

        return award.build();
    }

    private void addTitle(Player player, int id, int count, List<Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("称号id非法, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }

        StaticTitle title = StaticLordDataMgr.getTitleMapById(id);
        if (null == title) {
            LogUtil.error("称号id未配置，跳过奖励, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.TITLE, id, count, convert, from, param);
        // 获取称号，重新计算将领属性
        CalculateUtil.reCalcAllHeroAttr(player);
    }

    @Autowired
    private TotemService totemService;

    private void addTotem(Player player, int id, int count, AwardFrom awardFrom, Object... params) {
        totemService.addTotemsAndSync(player, ListUtils.createItems(0, id, count), awardFrom);
    }

//    private void addTotemChip(Player player,int totemChipId,int count,AwardFrom awardFrom,Object...params){
//        if(Objects.isNull(player) || totemChipId <= 0 || count <= 0 || Objects.isNull(awardFrom)){
//            LogUtil.error("增加图腾碎片传参错误",player,totemChipId,count,awardFrom);
//            return;
//        }
//        StaticProp staticProp = StaticPropDataMgr.getPropMap().get(totemChipId);
//        if (null == staticProp) {
//            LogUtil.error("图腾碎片道具id未配置，跳过奖励, roleId:", player.roleId, ", propId:", totemChipId, ", from:", awardFrom.getCode());
//            return;
//        }
//        player.getTotemData().newTotemChip(totemChipId,count);
//        int curCount = (int) getRoleResByType(player,AwardType.TOTEM_CHIP,totemChipId);
//        LogLordHelper.prop(awardFrom,player.account,player.lord,totemChipId,curCount,count,1,params);
//    }

    /**
     * 批量添加宝具(宝具宝箱等未拆开，宝具实例未生成)
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
            LogUtil.error("随机出来的宝具为空, treasureWareId: ", treasureWareId, ", lordId: ", player.lord.getLordId());
            return null;
        }

        return treasureWares;
    }

    /**
     * 邮件宝具添加(已到邮件，宝具实例已生成)
     *
     * @param player
     * @param treasureWareId
     * @param from
     * @param param
     */
    private void addTreasureWareInMail(Player player, int treasureWareId, int count, int keyId, AwardFrom from, Object... param) throws MwException {
        StaticTreasureWare staticTreasureWare = StaticTreasureWareDataMgr.getStaticTreasureWare(treasureWareId);
        if (CheckNull.isNull(staticTreasureWare)) {
            LogUtil.error("没有此宝具配置, staticTreasureWareId: ", treasureWareId,
                    ", lordId: ", player.lord.getLordId(), ", from:", from.getCode());
            return;
        }
        int remainBagCnt = DataResource.getBean(TreasureWareService.class).remainBagCnt(player);
        if (remainBagCnt < count) {
            throw new MwException(GameError.MAX_TREASURE_WARE_STORE.getCode(), "宝具背包已满, roleId: " + player.roleId + ", remainBagCnt= "
                    + remainBagCnt + ", bagCnt: " + player.common.getTreasureWareCnt() + ", cnt= " + count);
        }

        TreasureWare treasureWare = keyId > 0 ? player.treasureWares.get(keyId) : null;
        if (CheckNull.isNull(treasureWare) || treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_MAIL) {
            LogUtil.error("没有生成此宝具, staticTreasureWareId: ", treasureWareId,
                    ", lordId: ", player.lord.getLordId(), ", keyId: ", keyId,
                    ", from:", from.getCode(), ", status: ", treasureWare == null ? -1 : treasureWare.getStatus());
            throw new MwException(GameError.NOT_OWNED_TREASURE_WARE.getCode(), "没有生成此宝具, staticTreasureWareId: ", treasureWareId,
                    ", lordId: ", player.lord.getLordId(), ", keyId: ", keyId,
                    ", from:", from.getCode(), ", status: ", treasureWare == null ? -1 : treasureWare.getStatus());
        }

        treasureWare.setStatus(TreasureWareConst.TREASURE_IN_USING);

        int quality = treasureWare.getQuality();
        // 获取宝具名id
        int profileId = StaticTreasureWareDataMgr.getProfileId(treasureWareService.getAttrType(treasureWare), quality, treasureWare.getSpecialId());
        // 记录玩家获得新宝具
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
     * 行军特效
     *
     * @param player   玩家
     * @param id       配置id
     * @param duration 持续时长
     * @param convert  转换道具
     * @param from     奖励来源
     * @param param    参数
     */
    private void addMarchLine(Player player, int id, int duration, List<Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("行军特效id非法, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        if (id == Constant.DEFAULT_MARCH_LINE_ID) {
            // 默认装扮直接跳出
            return;
        }
        StaticMarchLine sMarchLine = StaticLordDataMgr.getMarchLine(id);
        if (Objects.isNull(sMarchLine)) {
            LogUtil.error("行军特效id未配置，跳过奖励, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.MARCH_SPECIAL_EFFECTS, id, duration, convert, from, param);
        // 如果获取的装扮是加成将领属性，重新计算将领属性
        if (sMarchLine.getEffectType() == StaticMarchLine.EFFECT_TYPE_HERO_ATTR) {
            CalculateUtil.reCalcAllHeroAttr(player);
        }
    }

    private void addSandTableScore(Player player, int count, AwardFrom from, Object... param) {
        if (count > 0) {
            player.setSandTableScore(player.getSandTableScore() + count);
            // 记录变更
            LogLordHelper.sandTableScore(from, player, count, param);
        }
    }

    /**
     * 添加装备宝石
     *
     * @param player
     * @param id
     * @param count
     * @param from
     * @param param
     */
    private void addEquipJewel(Player player, int id, int count, AwardFrom from, Object... param) {
        if (count <= 0 || count > 10000) {
            LogUtil.error("奖励宝石数量非法, roleId:", player.roleId, ", jewelId:", id, ", count:", count, ", from:",
                    from.getCode());
            return;
        }

        StaticJewel sJewel = StaticPropDataMgr.getJewelByLv(id);
        if (CheckNull.isNull(sJewel)) {
            LogUtil.error("装备id未配置，跳过奖励, roleId:", player.roleId, ", equipId:", id, ", from:", from.getCode());
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
        // 记录玩家获得道具
        LogLordHelper.jewel(from, player.account, player.lord, id, jewel.getCount(), count, Constant.ACTION_ADD, param);
    }

    /**
     * 获得新教官装备
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
            LogUtil.error("奖励装备数量非法, roleId:", player.roleId, ", equipId:", equipId, ", count:", count, ", from:",
                    from.getCode());
            return null;
        }

        StaticMentorEquip sEquip = StaticMentorDataMgr.getsMentorEquipIdMap(equipId);
        if (CheckNull.isNull(sEquip)) {
            LogUtil.error("装备id未配置，跳过奖励, roleId:", player.roleId, ", equipId:", equipId, ", from:", from.getCode());
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

            // 基本属性
            int attack = sEquip.getAttack();
            if (attack > 0) {
                Map<Integer, Integer> attr = equip.getAttr();
                attr.put(FightCommonConstant.AttrId.ATTACK, attack);
                equip.setAttr(attr);
            }

            // 附加属性
            int defenseHigh = sEquip.getDefenseHigh();
            int defenseLow = sEquip.getDefenseLow();
            if (defenseHigh > 0 && defenseLow > 0) {
                int defense = RandomUtils.nextInt(defenseLow, defenseHigh + 1);
                if (defense > 0) {
                    Map<Integer, Integer> extAttr = equip.getExtAttr();
                    extAttr.put(FightCommonConstant.AttrId.DEFEND, defense);
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

            // 获得新教官装备日志
            LogLordHelper.mentorEquip(from, player.account, player.lord, equipId, count);

            // 是否获得了更好的装备
            mentorDataManager.checkBetterEquip(player, equip);
        }
        return keyList;
    }

    /**
     * 勋章奖励
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
            LogUtil.error("奖励勋章参数非法, roleId:", player.roleId, ", medalId:", medalId, ", goodsId:", goodsId, ", count:",
                    count, ", from:", from.getCode());
            return medals;
        }
        // 勋章商品配置
        StaticMedalGoods sMedalGoods = null;
        if (goodsId > 0 && goodsId < Integer.MAX_VALUE) {
            // 判断勋章是否存在
            sMedalGoods = StaticMedalDataMgr.getMedalGoodsById(goodsId);
            if (sMedalGoods == null) {
                LogUtil.error("勋章商品id未配置, 跳过奖励, roleId:", player.roleId, ", goodsId:", goodsId, ", from:",
                        from.getCode());
                return medals;
            }
        }
        medalId = !CheckNull.isNull(sMedalGoods) ? sMedalGoods.getMedalId() : medalId;
        // 配置配置
        StaticMedal staticmedal = StaticMedalDataMgr.getMedalById(medalId);
        if (staticmedal == null) {
            LogUtil.error("勋章id未配置, 跳过奖励, roleId:", player.roleId, ", medalId:", medalId, ", from:", from.getCode());
            return medals;
        }

        Medal medal = null;
        for (int i = 0; i < count; i++) {
            try {
                medal = medalDataManager.initMedal(sMedalGoods, staticmedal);
            } catch (MwException e) {
                LogUtil.error("初始化勋章报错, 跳过奖励, roleId:", player.roleId, ", medalId:", medalId, ", from:",
                        from.getCode());
                return medals;
            }
            medal.setKeyId(player.maxKey());
            player.medals.put(medal.getKeyId(), medal);
            medals.add(medal);
            // 记录玩家获得新勋章
            LogLordHelper.medal(from, player.account, player.lord, medal.getMedalId(), medal.getKeyId(),
                    Constant.ACTION_ADD, param);
        }
        // 同步获得新勋章
        syncMedal(player, medals);
        return medals;
    }

    /**
     * 同步获得新勋章
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
            // 推送
            Base.Builder msg = PbHelper.createSynBase(GamePb1.SyncMedalRs.EXT_FIELD_NUMBER, GamePb1.SyncMedalRs.ext,
                    b.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 战机碎片奖励
     *
     * @param player 角色信息
     * @param chipId 碎片id
     * @param count  数量
     * @param from   奖励来源
     * @param param  参数
     */
    private void addPlaneChip(Player player, int chipId, int count, AwardFrom from, Object[] param) {

        StaticPlaneTransform planeTransform = StaticWarPlaneDataMgr.getPlaneTransformById(chipId);
        if (CheckNull.isNull(planeTransform)) {
            LogUtil.error("战机碎片Id未配置，跳过奖励, roleId:", player.roleId, ", chipId:", chipId, ", from:", from.getCode());
            return;
        }

        // 可合成战机类型
        int planeType = planeTransform.getPlaneType();

        // 转化的配置
        List<List<Integer>> transforms = planeTransform.getTransform();
        if (CheckNull.isEmpty(transforms)) {
            LogUtil.error("战机碎片转化奖励未配置，跳过奖励, roleId:", player.roleId, ", chipId:", chipId, ", from:", from.getCode());
            return;
        }

        // 战机信息
        WarPlane warPlane = player.warPlanes.get(planeType);

        // 碎片信息
        PlaneChip planeChip = player.getPlaneChip(chipId);

        // 已经有该类型的战机了
        if (!CheckNull.isNull(warPlane)) {
            // 当前类型最大等级的战机
            StaticPlaneUpgrade maxLv = StaticWarPlaneDataMgr
                    .getPlaneMaxLvByFilter(plane -> planeType == plane.getPlaneType() && plane.getNextId() == 0
                            && CheckNull.isEmpty(plane.getReformNeed()));
            if (CheckNull.isNull(maxLv)) {
                LogUtil.error("战机配置最大等级配置错误，跳过奖励, roleId:", player.roleId, ", chipId:", chipId, ", planeType:",
                        planeType, ", from:", from.getCode());
                return;
            }
            if (maxLv.getPlaneId() == warPlane.getPlaneId()) { // 已经是最大品质和等级的战机了, 获取的全部转化, 拥有的也转化掉
                for (List<Integer> transform : transforms) {
                    addAwardSignle(player, transform, count, AwardFrom.PLANE_CHIP_TRANSFORM);
                }
            } else {
                planeChip.addChipCnt(count);
                // 记录道具变更
                LogLordHelper.planeChip(from, player.account, player.lord, chipId, planeChip.getCnt(), count,
                        Constant.ACTION_SUB);
            }
        } else {
            planeChip.addChipCnt(count);
            // 记录道具变更
            LogLordHelper.planeChip(from, player.account, player.lord, chipId, planeChip.getCnt(), count,
                    Constant.ACTION_SUB);
        }
    }

    /**
     * 给玩家奖励战机
     *
     * @param player  角色信息
     * @param planeId 战机id
     * @param from    奖励来源
     * @param param   参数
     */
    private void addPlane(Player player, int planeId, AwardFrom from, Object[] param) {
        try {

            StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
            if (CheckNull.isNull(sPlaneUpgrade)) {
                LogUtil.error("战机Id未配置，跳过奖励, roleId:", player.roleId, ", planeId:", planeId, ", from:", from.getCode());
                return;
            }

            StaticPlaneInit planeInit = StaticWarPlaneDataMgr.getPlaneInitByType(sPlaneUpgrade.getPlaneType());
            if (CheckNull.isNull(planeInit)) {
                LogUtil.error("战机Type未配置，跳过奖励, roleId:", player.roleId, ", planeType:", sPlaneUpgrade.getPlaneType(),
                        ", from:", from.getCode());
                return;
            }

            // 更新活动进度
            activityDataManager.updActivity(player, ActivityConst.ACT_WAR_PLANE_SEARCH, 1, planeInit.getQuality(), true);

            WarPlane plane = player.checkWarPlaneIsExist(planeId);
            if (!CheckNull.isNull(plane)) {
                List<Integer> decompose = planeInit.getDecompose();
                if (CheckNull.isEmpty(decompose)) {
                    LogUtil.error("战机配置错误, 跳过转换, roleId:", player.roleId, ", planeId:", planeId, ", planeType:",
                            sPlaneUpgrade.getPlaneType(), ", from:", from.getCode());
                    return;
                }
                // 增加战机碎片
                addPlaneChip(player, decompose.get(1), decompose.get(2), from, param);
                return;
            }

            // 初始化新战机
            plane = warPlaneDataManager.createPlane(planeInit);

            // 添加到玩家战机列表中
            player.warPlanes.put(plane.getType(), plane);

            // 记录玩家获得新战机
            LogLordHelper.plane(from, player.account, player.lord, planeId, Constant.ACTION_ADD, param);
        } catch (MwException e) {
            LogUtil.error("战机id未配置，跳过奖励, roleId:", player.roleId, ", planeId:", planeId, ", from:", from.getCode());
            return;
        }
    }

    /**
     * 发送奖励类型为8的特殊类型奖励
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
                // 将领经验
                case AwardType.Special.HERO_EXP:
                    break;
                // 战机经验
                case AwardType.Special.PLANE_EXP:
                    break;
                // 基地直升劵
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
     * 奖励天赋石
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
     * 将领洗髓次数
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
     * 装备洗练次数
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
     * 增加特工互动次数
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
     * 科技加速
     *
     * @param player
     * @param count  秒数
     * @param from
     */
    private void addTechSpeed(Player player, int count, AwardFrom from) {
        TechQue que = player.getCanSpeedTechQue();
        if (que == null) {// 无科研所队列跳过
            return;
        }
        que.setFreeOtherCnt(WorldConstant.SPEED_TYPE_ACQUISITE);
        que.setParam(count);
        LogLordHelper.commonLog("addTechSpeed", from, player, count);
    }

    /**
     * 添加募兵免费加速
     *
     * @param player
     * @param count
     * @param from
     */
    private void addArmSpeed(Player player, int count, AwardFrom from) {
        ArmQue que = player.getCanAddSpeedArmQue();
        if (null == que) {// 当前玩家没有可添加免费加速的队列，跳过奖励
            return;
        }
        que.setFree(WorldConstant.SPEED_TYPE_ACQUISITE);
        que.setParam(count);
        LogLordHelper.commonLog("addArmSpeed", from, player, count);
    }

    /**
     * 添加建造免费加速
     *
     * @param player
     * @param count
     * @param from
     */
    private void addBuildSpeed(Player player, int count, AwardFrom from) {
        BuildQue que = player.getCanAddSpeedBuildQue();
        if (null == que) {// 当前玩家没有可添加免费加速的队列，跳过奖励
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
            // playerDataManager.autoAddArmy(player); 去掉原因:商店购买时,加上兵力会提前推送给客户端兵力,导致兵力加多
            LogLordHelper.army(from, player.account, player.lord, player.resource, id, add, param);
        }
    }

    /*-----------------------------------------增加相关end-------------------------------------------*/

    /**
     * 发送随机类奖励, AwardType.RANDOM 的处理 (只会获取一个奖励)
     *
     * @param player
     * @param rewardId 奖励id
     * @param count    随机总个数
     * @param from
     */
    private CommonPb.Award addRandomAward(Player player, int rewardId, int count, AwardFrom from) {
        StaticReward staticReward = StaticRewardDataMgr.getRewardMap().get(rewardId);
        if (null == staticReward) {
            LogUtil.error("奖励id未配置, rewardId:", rewardId);
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
            if (randomIdList.size() >= count) {// 已随机够数量，退出循环
                break;
            }
        }
        for (Integer randomId : randomIdList) {
            return reward(player, randomId, from);
        }
        return null;
    }

    /**
     * 根据randomId发送随机奖励
     *
     * @param player
     * @param randomId
     * @param from
     * @return
     */
    private CommonPb.Award reward(Player player, int randomId, AwardFrom from) {
        StaticRewardRandom srr = StaticRewardDataMgr.getRandomMap().get(randomId);
        if (null == srr) {
            LogUtil.error("random表未配置, randomId:", randomId);
            return null;
        }

        if (CheckNull.isEmpty(srr.getRandomStr())) {
            return null;
        }

        // 获得随即奖励
        List<Integer> awardList = RewardRandomUtil.getAwardByRandomId(randomId);
        // return addAward(player, awardList, from); 上面已做随即，此方法又进入了单个随即，可能会随不到
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
     * 发送随机类奖励(目前只有化工厂生产使用)
     *
     * @param player
     * @param rewardId 奖励id
     * @param count    随机总个数
     */
    public List<CommonPb.Award> getRandomAward(Player player, int rewardId, long count) {
        StaticReward staticReward = StaticRewardDataMgr.getRewardMap().get(rewardId);
        if (null == staticReward) {
            LogUtil.error("奖励id未配置, rewardId:", rewardId);
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

            if (randomIdList.size() >= count) {// 已随机够数量，退出循环
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
     * 根据randomId发送随机奖励
     *
     * @param player
     * @param randomId
     * @return
     */
    private CommonPb.Award getRewardRandom(Player player, int randomId) {
        StaticRewardRandom srr = StaticRewardDataMgr.getRandomMap().get(randomId);
        if (null == srr) {
            LogUtil.error("random表未配置, randomId:", randomId);
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
     * 奖励货币
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
     * 新增古籍
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
        //上报数数
        if (ArrayUtils.contains(HAS_INFO2_AWARD_FROM, from)) {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.CROSS_WAR_FIRE_COIN,
                    add, player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE), Arrays.toString(new Object[]{param[0]}), Arrays.toString(new Object[]{param[1]}));
        } else {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.CROSS_WAR_FIRE_COIN,
                    add, player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE), Arrays.toString(param), "");
        }
    }

    /**
     * 增加金锭
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
     * 增加钞票
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
     * @Description: 增加 金条
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
     * @Description: 增加荣誉
     */
    private void addHonor(Player player, int add, AwardFrom from, Object... param) {
        if (add > 0) {
            player.lord.setHonor(player.lord.getHonor() + add);
            LogLordHelper.honor(from, player.account, player.lord, player.lord.getHonor(), add, param);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.HONOR, add, player.lord.getHonor(), Arrays.toString(param), "");
        }
    }

    /**
     * 增加积分
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
     * 添加将令
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
     * 添加体力
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
     * 添加军功
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
     * 给玩家添加金币
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


    /*************************** 发送世界消息 start *************************************/

    /**
     * 给玩家加经验
     *
     * @param player
     * @param count
     */
    private void addRoleExp(Player player, long count, AwardFrom from, Object... param) {
        // StaticLordDataMgr.addExp(player.lord, count);

        if (count > 0) {
            addExp(player, count, from);
            // 记录经验变更
            LogLordHelper.exp(from, player.account, player.lord, count, param);
        }
        // worldDataManager.openPos(player);
    }

    private void addVipExp(Player player, long count, AwardFrom from, Object... param) {
        if (count > 0) {
            Lord lord = player.lord;
            lord.setVipExp((int) (lord.getVipExp() + count));
            vipDataManager.processVip(player);
            // 记录经验变更
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
        List<Integer> lvThroughList = null;// 本次加经验刮经历过的等级列表
        while (true) {
            staticLordLv = StaticLordDataMgr.getStaticLordLv(lv + 1);
            if (staticLordLv == null || lv >= Constant.MAX_ROLE_LV) {
                exp = 0;// 等级达到最高级，经验不再加
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

        // 角色到达45级 且在全服中低于 Constant.ROLE_45_DIGIT 位到达 则发送世界消息
        if (startLv < Constant.ROLE_GRADE_45 && lv >= Constant.ROLE_GRADE_45) {
            // 记录荣耀日报解锁时间
            int now = TimeHelper.getCurrentSecond();
            player.setMixtureData(PlayerConstant.RYRB_LOCK_TIME, now);

            // 查询已经到达45级的角色 数量
            int count = playerDataManager.getRoleGrade45();
            if (count < Constant.ROLE_45_DIGIT) {// 则该玩家是 第count+1 位到达45级以上的玩家
                playerDataManager.setRoleGrade45(count + 1);
                chatDataManager.sendSysChat(ChatConst.CHAT_GRADE_IPGRADING_45, player.lord.getCamp(), 0, player.lord.getCamp(),
                        player.lord.getNick(), count + 1);
            }
        }

        if (up) {
            rankDataManager.setRoleLv(lord);
            // 发送升级奖励
            // sendReward(player, staticLordLv.getRewards(), AwardFrom.LV_UP_REWARD, "升级奖励");
            DbMasterApprentice apprentice = player.master;
            Player master = null;
            try {
                master = CheckNull.isNull(apprentice) ? null : playerDataManager.checkPlayerIsExist(apprentice.getLordId());
            } catch (MwException e) {
                LogUtil.error(e, "获取不到师傅的player对象, roleId: ", apprentice.getLordId());
            }
            for (int i = startLv + 1; i <= lord.getLevel(); i++) {
                staticLordLv = StaticLordDataMgr.getStaticLordLv(i); // 重新赋值,获取当前等级的奖励
                //如果升到30级,自动按照当前渠道发送邮件
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
                        // 给导师加积分
                        activityDataManager.updRankActivity(master, ActivityConst.ACT_TUTOR_RANK, config.get(1));
                    }
                }
            }
            buildingDataManager.refreshSourceData(player);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_LEVEL);
            taskDataManager.updTask(player, TaskType.COND_LORD_LV, 1);

            activityTriggerService.roleLevelUpTriggerGift(player, lvThroughList);

            //升级后检查是否开放貂蝉任务
            activityDiaoChanService.handleTodayTask(player);

            //貂蝉任务-领主等级
            ActivityDiaoChanService.completeTask(player, ETask.PLAYER_LV);
            TaskService.processTask(player, ETask.PLAYER_LV);
            if (null != player.master) {
                Player masterPlayer = playerDataManager.getPlayer(master.getLordId());
                //玩家达到等级，向玩家的师父发送任务完成验证（在线）
                if (masterPlayer.isLogin) {
                    //称号-x个徒弟等级达到y级
                    titleService.processTask(masterPlayer, ETask.APPRENTICE_LEVEL_MAKE_IT);
                }
            }
            //三国新增埋点, 记录玩家升级日志
            LogLordHelper.gameLog(LogParamConstant.LEVEL_UP, player, from, preLv, lv);
            // 活动处理玩家升级
            EventBus.getDefault().post(new Events.ActLevelUpEvent(lord.getLordId(), preLv, lv));
            // 城镇事件刷新开启
            StaticFunctionOpen sOpen = StaticFunctionDataMgr.getOpenById(FunctionConstant.CITY_EVENT);
            if (sOpen != null) {
                // 等级条件
                int cityEventNeedLv = sOpen.getLv();
                if (lvThroughList.contains(cityEventNeedLv)) {
                    // 如果条件满足, 立刻开启
                    DataResource.ac.getBean(LifeSimulatorService.class).assignCityEventToPlayerJob(player);
                }
            }
            // 增加经济订单数量上限
            List<Integer> orderTopLimitIncreaseConfig = Constant.ORDER_TOP_LIMIT_INCREASE_CONFIG;
            int orderIniTopLimit = Constant.ORDER_INI_TOP_LIMIT;
            for (Integer canAddLordLv : orderTopLimitIncreaseConfig) {
                // 达到对应需要的领主等级, 且订单数未超过上限
                if (lvThroughList.contains(canAddLordLv) && player.getEconomicOrderMaxCnt() < orderTopLimitIncreaseConfig.size() + orderIniTopLimit) {
                    player.setEconomicOrderMaxCnt(player.getEconomicOrderMaxCnt() + 1);
                }
            }
        }
        // 向客户端同步等级
        playerDataManager.syncRoleInfo(player);
        if (lvThroughList != null) {// 某个具体等级触发的事件
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
     * 如果升到30级,自动按照当前渠道发送邮件
     */
    public void sendChannelMail(Player player) {
        List<StaticChannelMail> channelMailList = StaticMailDataMgr.getChannelMailList();
        // 记录发送状态
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
     * 综合世界和阵营的系统消息
     *
     * @param chatId
     * @param campOrArea 如果是本阵营通道就是 camp,本区域就是areaId
     * @param myCnt      带额外参数的
     * @param param
     */
    public void sendSysChat(int chatId, int campOrArea, int myCnt, Object... param) {
        StaticChat sChat = StaticChatDataMgr.getChatMapById(chatId);
        if (sChat != null) {
            int channel = sChat.getChannel();
            Chat chat = myCnt > 0 ? createWithParamSysChat(chatId, myCnt, param) : createSysChat(chatId, param);
            if (ChatConst.CHANNEL_WORLD == channel) {// 世界
                sendWorldChat(chat);
            } else {
                LogUtil.error("聊天配置表出错 chatId:", chatId);
            }
        } else {
            LogUtil.error("聊天配置表出错 chatId:", chatId);
        }

    }

    /*************************** 发送世界消息 end *************************************/

    /**
     * 根据聊天模版id创建系统聊天对象
     *
     * @param chatId
     * @param myCnt  其他参数个数 myParam[0~myCnt] ; chat的param[myCnt~结束]
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
     * 根据聊天模版id创建系统聊天对象
     *
     * @param chatId
     * @param param
     * @return
     */
    private Chat createSysChat(int chatId, Object... param) {
        return createWithParamSysChat(chatId, 0, param);
    }

    /**
     * 发送世界聊天消息
     *
     * @param chat
     */
    private void sendWorldChat(Chat chat) {
        sendWorldChat(chat, false);
    }

    /**
     * 发送世界聊天消息
     *
     * @param chat
     * @param isRole 是否是大喇叭 true大喇叭
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
     * 奖励道具
     *
     * @param player
     * @param propId
     * @param count
     * @param from
     */
    private void addProp(Player player, int propId, int count, AwardFrom from, Object... param) {
        if (count <= 0 || count > Integer.MAX_VALUE) {
            LogUtil.error("奖励道具数量非法, roleId:", player.roleId, ", propId:", propId, ", count:", count, ", from:",
                    from.getCode());
            return;
        }

        StaticProp staticProp = StaticPropDataMgr.getPropMap().get(propId);
        if (null == staticProp) {
            LogUtil.error("道具id未配置，跳过奖励, roleId:", player.roleId, ", propId:", propId, ", from:", from.getCode());
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

        // 世界争霸积分
        if (propId == PropConstant.WORLD_WAR_INTEGRAL) {
            worldWarSeasonWeekIntegralService.addWorldWarIntegral(player, count);
        }

        //处理鱼饵图鉴
        fishingService.handleBaitAltas(player, propId);

        // 记录玩家获得道具
        LogLordHelper.prop(from, player.account, player.lord, propId, prop.getCount(), count, Constant.ACTION_ADD,
                param);
        taskDataManager.updTask(player, TaskType.COND_506, count, propId);
    }

    /**
     * 添加头像
     *
     * @param player
     * @param portraitId
     * @param from
     */
    private void addPortrait(Player player, int portraitId, long count, List<Award> convert, AwardFrom from, Object... param) {
        if (portraitId <= 0) {
            LogUtil.error("头像id非法, roleId:", player.roleId, ", portraitId:", portraitId, ", from:", from.getCode());
            return;
        }
        StaticPortrait staticPortrait = StaticLordDataMgr.getPortrait(portraitId);
        if (null == staticPortrait) {
            LogUtil.error("头像id未配置，跳过奖励, roleId:", player.roleId, ", portraitId:", portraitId, ", from:",
                    from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.PORTRAIT, portraitId, count, convert, from, param);
    }

    /**
     * 新增城堡皮肤
     *
     * @param player
     * @param id
     * @param from
     * @param param
     */
    private void addCastleSkin(Player player, int id, long count, List<CommonPb.Award> convert, AwardFrom from, Object... param) {
        if (id <= 0) {
            LogUtil.error("城堡皮肤id非法, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }

        if (id == StaticCastleSkin.DEFAULT_SKIN_ID) { // 默认皮肤直接跳出
            return;
        }
        StaticCastleSkin castleSkinCfg = StaticLordDataMgr.getCastleSkinMapById(id);
        if (null == castleSkinCfg) {
            LogUtil.error("城堡皮肤id未配置，跳过奖励, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.CASTLE_SKIN, id, count, convert, from, param);
        // 如果获取的皮肤是加成将领属性，重新计算将领属性
        if (castleSkinCfg.getEffectType() == StaticCastleSkin.EFFECT_TYPE_HERO_ATTR) {
            CalculateUtil.reCalcAllHeroAttr(player);
        }
    }

    /**
     * 新增头像框
     *
     * @param player 玩家对象
     * @param id     配置id
     * @param count  1 为永久获得, 大于1是临时获得的时间(秒)
     * @param from   来源
     * @param param  参数
     */
    private void addPortraitFrame(Player player, int id, int count, List<CommonPb.Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("头像框id非法, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        if (id == Constant.DEFAULT_PORTRAIT_FRAME_ID) {
            // 默认装扮直接跳出
            return;
        }
        StaticPortraitFrame sPortraitFrame = StaticLordDataMgr.getPortraitFrame(id);
        if (Objects.isNull(sPortraitFrame)) {
            LogUtil.error("头像框id未配置，跳过奖励, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.PORTRAIT_FRAME, id, count, convert, from, param);
    }

    /**
     * 新增铭牌
     *
     * @param player 玩家对象
     * @param id     配置id
     * @param count  1 为永久获得, 大于1是临时获得的时间(秒)
     * @param from   来源
     * @param param  参数
     */
    private void addNameplate(Player player, int id, int count, List<CommonPb.Award> convert, AwardFrom from, Object[] param) {
        if (id <= 0) {
            LogUtil.error("铭牌id非法, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        if (id == Constant.DEFAULT_NAME_PLATE_ID) {
            // 默认装扮直接跳出
            return;
        }
        StaticNameplate sNameplate = StaticLordDataMgr.getNameplate(id);
        if (Objects.isNull(sNameplate)) {
            LogUtil.error("铭牌id未配置，跳过奖励, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
            return;
        }
        dressUpDataManager.addDressUp(player, AwardType.NAMEPLATE, id, count, convert, from, param);
    }

    /**
     * 添加聊天
     *
     * @param player
     * @param chatBubbleId
     * @param from
     * @param param
     */
    public void addChatBubble(Player player, int chatBubbleId, int count, List<CommonPb.Award> convert, AwardFrom from, Object... param) {
        if (chatBubbleId <= 0) {
            LogUtil.error("聊天气泡id非法, roleId:", player.roleId, ", chatBubbleId:", chatBubbleId, ", from:",
                    from.getCode());
            return;
        }
        StaticChatBubble sChatBubble = StaticLordDataMgr.getChatBubbleMapById(chatBubbleId);
        if (null == sChatBubble) {
            LogUtil.error("聊天气泡id未配置，跳过奖励, roleId:", player.roleId, ", chatBubbleId:", chatBubbleId, ", from:",
                    from.getCode());
            return;
        }
        int bubbleType = sChatBubble.getType();
        if (bubbleType != StaticChatBubble.TYPE_AWARD && bubbleType != StaticChatBubble.TYPE_ACT_AWARD) {
            LogUtil.error("聊天气泡类型不正确，跳过奖励, roleId:", player.roleId, ", chatBubbleId:", chatBubbleId, ", from:",
                    from.getCode());
            return;
        }
//        player.getChatBubbles().add(chatBubbleId);
        dressUpDataManager.addDressUp(player, AwardType.CHAT_BUBBLE, chatBubbleId, count, convert, from, param);
    }

    /*-----------------------------------------增加相关end-------------------------------------------*/

    /*-----------------------------------------修改相关start-------------------------------------------*/

    /**
     * 添加宝石
     *
     * @param player
     * @param id
     * @param count
     * @param from
     */
    private void addStone(Player player, int id, int count, AwardFrom from, Object... param) {
        if (count <= 0 || count > Integer.MAX_VALUE) {
            LogUtil.error("奖励道宝石量非法, roleId:", player.roleId, ", id:", id, ", count:", count, ", from:",
                    from.getCode());
            return;
        }
        StaticStone sStone = StaticPropDataMgr.getStoneMapById(id);
        if (null == sStone) {
            LogUtil.error("宝石id未配置，跳过奖励, roleId:", player.roleId, ", id:", id, ", from:", from.getCode());
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
        // 记录玩家获得道具
        LogLordHelper.stone(from, player.account, player.lord, id, stone.getCnt(), count, Constant.ACTION_ADD, param);

        //貂蝉任务-配饰合成
        ActivityDiaoChanService.completeTask(player, ETask.ORNAMENT_COUNT);
        //貂蝉任务-合成x个任意等级配饰
//        ActivityDiaoChanService.completeTask(player, ETask.ORNAMENT_COUNT);
        TaskService.processTask(player, ETask.ORNAMENT_COUNT);
    }

    /**
     * 新增进阶的宝石
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
            LogUtil.error("宝石进阶Id非法, roleId:", player.roleId, ", stoneImproveId:", stoneImproveId, ", from:",
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
     * 奖励装备
     *
     * @param player
     * @param equipId
     * @param count
     * @param from
     */
    public List<Integer> addEquip(Player player, int equipId, long count, AwardFrom from, Object... param) {
        if (count <= 0 || count > 10000) {
            LogUtil.error("奖励装备数量非法, roleId:", player.roleId, ", equipId:", equipId, ", count:", count, ", from:",
                    from.getCode());
            return null;
        }

        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equipId);
        if (null == staticEquip) {
            LogUtil.error("装备id未配置，跳过奖励, roleId:", player.roleId, ", equipId:", equipId, ", from:", from.getCode());
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

            // 初始附加属性
            StaticEquipQualityExtra staticEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                    .get(staticEquip.getQuality());
            List<Turple<Integer, Integer>> attrLv = equip.getAttrAndLv();
            // 说明该装备初始化自带配置秘技
            if (staticEquip.getExtra() != null && staticEquip.getExtra().size() >= 2) {
                if (staticEquipQualityExtra != null && staticEquipQualityExtra.getExtraNum() > 0) {
                    for (int j = 0; j < staticEquipQualityExtra.getExtraNum() + 1; j++) {
                        attrLv.add(new Turple<>(staticEquip.getExtra().get(0), staticEquip.getExtra().get(1)));
                    }
                }
            } else {// 按权重随机
                if (staticEquipQualityExtra != null && staticEquipQualityExtra.getExtraNum() > 0) {
                    for (int j = 0; j < staticEquipQualityExtra.getExtraNum(); j++) {
                        attrLv.add(new Turple<>(FightCommonConstant.ATTRS[RandomUtils.nextInt(0, FightCommonConstant.ATTRS.length)],
                                staticEquipQualityExtra.getExtraLv()));
                    }
                }
            }
            //判断获取的打造装备品质是不是为配置的及以上, 如是则设置为已上锁
            if (staticEquip.getQuality() >= EquipConstant.EQUIP_AUTO_LOCK) {
                equip.setEquipLocked(2);
            } else {
                equip.setEquipLocked(1);
            }
            keyList.add(equip.getKeyId());
            // 记录玩家获得新装备
            LogLordHelper.equip(from, player.account, player.lord, equipId, equip.getKeyId(), Constant.ACTION_ADD
                    , LogUtil.obj2ShortStr(equip.getAttrAndLv()), param);
        }
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_EQUIP, staticEquip.getQuality());
        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_EQUIP, 1, staticEquip.getQuality());

        return keyList;
    }

    /**
     * 发送武将奖励
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
     * 给玩家奖励将领
     *
     * @param player
     * @param heroId
     * @param from
     */
    public Hero addHero(Player player, int heroId, AwardFrom from, Object... param) {
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        if (null == staticHero) {
            LogUtil.error("将领id未配置，跳过奖励, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
            return null;
        }

        // 校验玩家是否还可以获得此英雄或英雄碎片
        Hero hero = player.heros.get(heroId);
        if (!drawCardService.checkHero(player, hero, from)) {
            return null;
        }

        try {
            if (null != hero) {
                // 获取没有的武将, 处理救援奖励邮件
                drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);
                // 重复英雄转化为碎片
                operationHeroFragment(player, heroId, HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS, AwardFrom.SAME_TYPE_HERO, true, true, param);
                LogUtil.error("玩家已有该将领类型，跳过奖励, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
                return drawCardService.containAwardFrom(from) ? hero : null;
            }

            hero = new Hero();
            hero.setHeroId(heroId);
            hero.setHeroType(staticHero.getHeroType());
            hero.setQuality(staticHero.getQuality());
            // 设置武将初始等级（武将初始等级自适应配置的最大值与玩家等级的取小
            List<Integer> appoint = StaticHeroDataMgr.getInitHeroAppoint(heroId);

            if (CheckNull.nonEmpty(appoint)) {
                int minLv = appoint.get(0); // 武将初始等级下限
                int maxLv = appoint.get(1); // 武将初始等级上限
                int lordLevel = player.lord.getLevel(); // 玩家当前等级
                int targetLevel = Math.min(Math.max(minLv, maxLv), lordLevel);// 目标要升至的等级
                hero.setLevel(targetLevel);
            }

            // 初始化将领品阶数据
            hero.initHeroGrade();
            // 初始化武将内政属性(武将本身自带的及初始品阶对应)
            Map<Integer, Integer> interiorAttr = hero.getInteriorAttr();
            // 武将本身自带的初始内政属性
            List<List<Integer>> staticHeroInterior = staticHero.getInterior();
            if (CheckNull.nonEmpty(staticHeroInterior)) {
                for (List<Integer> attr : staticHeroInterior) {
                    if (CheckNull.isEmpty(attr) || attr.size() < 3) {
                        continue;
                    }
                    Entry<Integer, Integer> entry = interiorAttr.entrySet().stream()
                            .filter(tmp -> tmp.getKey().intValue() == attr.get(0).intValue())
                            .findFirst()
                            .orElse(null);
                    if (entry == null) {
                        interiorAttr.put(attr.get(0), attr.get(2));
                    } else {
                        int newAttrValue = entry.getValue() + attr.get(2);
                        entry.setValue(newAttrValue);
                        interiorAttr.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            // 初始品阶对应的内政属性
            StaticHeroUpgrade sHeroGrade = StaticHeroDataMgr.getStaticHeroUpgrade(hero.getGradeKeyId());
            StaticHeroGradeInterior sHeroGradeInterior = StaticHeroDataMgr.getStaticHeroGradeInterior(sHeroGrade.getGrade(), sHeroGrade.getLevel());
            if (sHeroGradeInterior != null && CheckNull.nonEmpty(sHeroGradeInterior.getAttr())) {
                for (List<Integer> attr : sHeroGradeInterior.getAttr()) {
                    if (CheckNull.isEmpty(attr) || attr.size() < 3) {
                        continue;
                    }
                    Entry<Integer, Integer> entry = interiorAttr.entrySet().stream()
                            .filter(tmp -> tmp.getKey().intValue() == attr.get(0).intValue())
                            .findFirst()
                            .orElse(null);
                    if (entry == null) {
                        interiorAttr.put(attr.get(0), attr.get(2));
                    } else {
                        int newAttrValue = entry.getValue() + attr.get(2);
                        entry.setValue(newAttrValue);
                        interiorAttr.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            hero.setInteriorAttr(interiorAttr);
            //如果是赛季英雄则初始化英雄技能
            if (staticHero.getSeason() > 0) {
//                Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> skillMap = StaticHeroDataMgr.getHeroSkill(heroId);
//                if (Objects.nonNull(skillMap)) {
//                    for (Entry<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> entry : skillMap.entrySet()) {
//                        int skillId = entry.getKey();
//                        int initLv = entry.getValue().firstKey();
//                        hero.getSkillLevels().put(skillId, initLv);
//                    }
//                }
                hero.setCgyStage(1);
                hero.setCgyLv(0);

                //获得英雄发聊天公告
                chatDataManager.sendSysChat(ChatConst.SEASON_GET_HERO, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), hero.getHeroId());
            }
            // 初始化武将天赋
            if (staticHero.getActivate() == 1 && CheckNull.nonEmpty(staticHero.getEvolveGroup())) {
                for (int i = 1; i <= staticHero.getEvolveGroup().size(); i++) {
                    Integer maxPart = StaticHeroDataMgr.getHeroEvolve(staticHero.getEvolveGroup().get(i - 1)).stream()
                            .map(StaticHeroEvolve::getPart)
                            .distinct()
                            .max(Integer::compareTo)
                            .orElse(null);
                    if (maxPart == null) {
                        throw new MwException(GameError.NO_CONFIG.getCode(), "武将天赋球个数配置错误, heroId: ", heroId);
                    }
                    // 暂时把武将的天赋组个数作为天赋页页数
                    hero.getTalent().put(i, new TalentData(0, i, maxPart));
                }
            }

            CalculateUtil.processAttr(player, hero);
            player.heros.put(heroId, hero);
            // 获取没有的武将, 处理救援奖励邮件
            drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);

            // 记录玩家获得新将领
            LogLordHelper.hero(from, player.account, player.lord, heroId, Constant.ACTION_ADD, param);

            //貂蝉任务-拥有英雄X个X品质
            ActivityDiaoChanService.completeTask(player, ETask.OWN_HERO);
            TaskService.processTask(player, ETask.OWN_HERO);
            //任务 - 获得指定英雄
            TaskService.handleTask(player, ETask.GET_HERO);
            ActivityDiaoChanService.completeTask(player, ETask.GET_HERO);
            TaskService.processTask(player, ETask.GET_HERO);

            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO_QUALITY_UPGRADE_CNT);
            taskDataManager.updTask(player, TaskType.COND_26, 1, hero.getHeroId());
            taskDataManager.updTask(player, TaskType.COND_27, 1, hero.getType());
            taskDataManager.updTask(player, TaskType.COND_514, 1, hero.getLevel());
            if (!ActParamConstant.ACT_TRIGGER_HERO_IGNORE.contains(staticHero.getHeroId())) {
                // 触发第一次增加紫橙将领
                activityTriggerService.awardHeroTriggerGift(player, hero.getQuality());
            }
            //触发获得某英雄后相关礼包
            activityTriggerService.getAnyHero(player, staticHero.getHeroType());
            return hero;
        } catch (Exception e) {
            LogUtil.error(String.format("add hero error, player:%d, heroId:%d, e:%s", player.roleId, heroId, e));
            return hero;
        }
    }

    /**
     * 添加指定品阶武将
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
            LogUtil.error("将领id未配置，跳过奖励, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
            return null;
        }

        // 校验玩家是否还可以获得此英雄或英雄碎片
        Hero hero = player.heros.get(heroId);
        if (!drawCardService.checkHero(player, hero, from)) {
            return null;
        }

        try {
            if (null != hero) {
                // 获取没有的武将, 处理救援奖励邮件
                drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);
                // 重复英雄转化为碎片
                operationDesignatedHeroGradeFragment(player, heroId, gradeKeyId, AwardFrom.SAME_TYPE_HERO, true, true, param);
                LogUtil.error("玩家已有该高级品阶将领类型，跳过奖励, roleId:", player.roleId, ", heroId:", heroId, ", from:", from.getCode());
                return drawCardService.containAwardFrom(from) ? hero : null;
            }

            StaticHeroUpgrade staticData = StaticHeroDataMgr.getStaticHeroUpgrade(gradeKeyId);
            if (CheckNull.isNull(staticData)) {
                LogUtil.error("heroId:%d, 不存在此品阶, gradeKeyId:%d", heroId, gradeKeyId);
                return null;
            }

            hero = new Hero();
            hero.setHeroId(heroId);
            hero.setHeroType(staticHero.getHeroType());
            hero.setQuality(staticHero.getQuality());
            // 设置武将初始等级（武将初始等级自适应配置的最大值与玩家等级的取小
            List<Integer> appoint = StaticHeroDataMgr.getInitHeroAppoint(heroId);

            if (CheckNull.nonEmpty(appoint)) {
                int minLv = appoint.get(0); // 武将初始等级下限
                int maxLv = appoint.get(1); // 武将初始等级上限
                int lordLevel = player.lord.getLevel(); // 玩家当前等级
                int targetLevel = Math.min(Math.max(minLv, maxLv), lordLevel);// 目标要升至的等级
                hero.setLevel(targetLevel);
            }

            // 初始化将领品阶数据
            hero.setGradeKeyId(gradeKeyId);
            //如果是赛季英雄则初始化英雄技能
            if (staticHero.getSeason() > 0) {
//                Map<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> skillMap = StaticHeroDataMgr.getHeroSkill(heroId);
//                if (Objects.nonNull(skillMap)) {
//                    for (Entry<Integer, TreeMap<Integer, StaticHeroSeasonSkill>> entry : skillMap.entrySet()) {
//                        int skillId = entry.getKey();
//                        int initLv = entry.getValue().firstKey();
//                        hero.getSkillLevels().put(skillId, initLv);
//                    }
//                }
                hero.setCgyStage(1);
                hero.setCgyLv(0);

                //获得英雄发聊天公告
                chatDataManager.sendSysChat(ChatConst.SEASON_GET_HERO, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(), hero.getHeroId());
            }
            CalculateUtil.processAttr(player, hero);
            player.heros.put(heroId, hero);
            // 获取没有的武将, 处理救援奖励邮件
            drawCardService.handleRepeatedHeroAndRescueAward(player, hero, from);

            // 记录玩家获得新将领
            LogLordHelper.hero(from, player.account, player.lord, heroId, Constant.ACTION_ADD, param);

            //貂蝉任务-拥有英雄X个X品质
            ActivityDiaoChanService.completeTask(player, ETask.OWN_HERO);
            TaskService.processTask(player, ETask.OWN_HERO);
            //任务 - 获得指定英雄
            TaskService.handleTask(player, ETask.GET_HERO);
            ActivityDiaoChanService.completeTask(player, ETask.GET_HERO);
            TaskService.processTask(player, ETask.GET_HERO);

            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_HERO_QUALITY_UPGRADE_CNT);
            taskDataManager.updTask(player, TaskType.COND_26, 1, hero.getHeroId());
            taskDataManager.updTask(player, TaskType.COND_27, 1, hero.getType());
            taskDataManager.updTask(player, TaskType.COND_514, 1, hero.getLevel());
            if (!ActParamConstant.ACT_TRIGGER_HERO_IGNORE.contains(staticHero.getHeroId())) {
                // 触发第一次增加紫橙将领
                activityTriggerService.awardHeroTriggerGift(player, hero.getQuality());
            }
            //触发获得某英雄后相关礼包
            activityTriggerService.getAnyHero(player, staticHero.getHeroType());
            return hero;
        } catch (Exception e) {
            LogUtil.error(String.format("add hero error, player:%d, heroId:%d, e:%s", player.roleId, heroId, e));
            return hero;
        }
    }

    /**
     * 装备减少
     *
     * @param player
     * @param keyId
     * @param from
     */
    public void subEquip(Player player, int keyId, AwardFrom from) {
        Equip equip = player.equips.remove(keyId);
        if (equip != null) {
            // 记录玩家减少的装备
            LogLordHelper.equip(from, player.account, player.lord, equip.getEquipId(), equip.getKeyId(),
                    Constant.ACTION_SUB, LogUtil.obj2ShortStr(equip.getAttrAndLv()));
        }

    }

    /**
     * 宝具减少
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

        // 添加到后台执行日志打印
        DataResource.logicServer.addCommandByType(() -> {
            // 获取宝具名id
            int profileId = StaticTreasureWareDataMgr.getProfileId(treasureWareService.getAttrType(treasureWare), treasureWare.getQuality(), treasureWare.getSpecialId());
            // 记录玩家减少的宝具
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

            // 宝具事件上报
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
                    //喜悦金秋-日出而作- 获得资源 2(木材)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 2, Long.valueOf(add).intValue());
                }

                break;
            case AwardType.Resource.FOOD:
                resource.setFood(resource.getFood() + add);
                if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_SUPPLY_RANK, add);
                    // 世界争霸 每日计时任务补给数量
                    restrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_PLUNDER_RESOURCE_CNT,
                            (int) add);
                    //喜悦金秋-日出而作- 获得资源 3(粮食)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 3, Long.valueOf(add).intValue());
                }
                break;
            case AwardType.Resource.OIL:
                resource.setOil(resource.getOil() + add);
                if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
                    //喜悦金秋-日出而作- 获得资源 1(黄金)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 1, Long.valueOf(add).intValue());
                }
                break;
            case AwardType.Resource.ORE:
                resource.setOre(resource.getOre() + add);
                if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_ORE_RANK, add);
                    //喜悦金秋-日出而作- 获得资源 4(矿石)
                    TaskService.processTask(player, ETask.GOLDEN_AUTUMN_GET_RESOURCE, 4, Long.valueOf(add).intValue());
                }
                break;
            case AwardType.Resource.HUMAN:
                // 只有化工厂解锁开启了才能有人口
                if (buildingDataManager.checkBuildingLock(player, BuildingType.FERRY)) {
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
        // 只有获得才算
        if (add > 0 && from != AwardFrom.RECRUIT_CANCEL) {
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_RESOURCE_CNT, (int) add, id);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_RESOURCE_CNT, (int) add, id);
        }
        // // 为解决客户端资源同步问题,强制同步
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
     * 扣资源
     *
     * @param player
     * @param upNeedResource
     * @param factor         加成百分比
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
     * 扣除玩家道具
     *
     * @param player
     * @param propId
     * @param count  需要扣除的数量，非负整数
     * @param from
     * @throws MwException
     */
    public void subProp(Player player, int propId, int count, AwardFrom from, Object... param) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " 传入的扣除数量非法，不能扣除, roleId:", player.roleId,
                    ", propId:", propId, ", need:", count);
        }

        // 道具id小于等于0, 不处理
        if (propId <= 0) {
            return;
        }

        Prop prop = player.props.get(propId);
        if (null == prop || prop.getCount() < count) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), " 道具数量不足，不能扣除, roleId:", player.roleId,
                    ", propId:", propId, ", need:", count, ", have:", null == prop ? 0 : prop.getCount());
        }

        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_USE_PROP, 1, propId);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_USE_PROP, 1, propId);

        //貂蝉任务-消耗道具
        ActivityDiaoChanService.completeTask(player, ETask.CONSUME_ITEM, propId, count);
        TaskService.processTask(player, ETask.CONSUME_ITEM, propId, count);

        // 扣除道具
        prop.setCount(prop.getCount() - count);

        // 记录道具变更
        LogLordHelper.prop(from, player.account, player.lord, propId, prop.getCount(), count, Constant.ACTION_SUB,
                param);
    }

    /**
     * 减少战机数量
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
            throw new MwException(GameError.INVALID_PARAM.getCode(), " 传入的扣除数量非法，不能扣除, roleId:", player.roleId, ", id:",
                    id, ", need:", count);
        }

        PlaneChip planeChip = player.getPlaneChip(id);
        if (CheckNull.isNull(planeChip) || planeChip.getCnt() < count) {
            throw new MwException(GameError.PLANE_CHIP_NOT_ENOUGH.getCode(), " 战机碎片数量不足, roleId:", player.roleId,
                    ", id:", id, ", need:", count, ", have:", planeChip.getCnt());
        }
        planeChip.subChipCnt(count);
        // 记录道具变更
        LogLordHelper.planeChip(from, player.account, player.lord, id, planeChip.getCnt(), count, Constant.ACTION_SUB);
    }

    /**
     * 减少宝石
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
            throw new MwException(GameError.INVALID_PARAM.getCode(), " 传入的扣除数量非法，不能扣除, roleId:", player.roleId, ", id:",
                    id, ", need:", count);
        }

        Stone stone = player.getStoneInfo().getStones().get(id);
        if (null == stone || stone.getCnt() < count) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), " 宝石数量不足，不能扣除, roleId:", player.roleId, ", id:",
                    id, ", need:", count, ", have:", null == stone ? 0 : stone.getCnt());
        }
        stone.subStoneCntAndGet(count);
        // 记录道具变更
        LogLordHelper.stone(from, player.account, player.lord, id, stone.getCnt(), count, Constant.ACTION_SUB);
    }

    /**
     * 减少装备宝石 <br />
     * <b>注意: 减少的数量不能小于宝石可用的数量, 而不是宝石拥有的数量<b/>
     *
     * @param player 玩家对象
     * @param id     宝石等级
     * @param count  减少的数量
     * @param from   来源
     * @throws MwException 自定义异常
     */
    private void subEquipJewel(Player player, int id, int count, AwardFrom from) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " 传入的扣除数量非法，不能扣除, roleId:", player.roleId, ", id:",
                    id, ", need:", count);
        }

        EquipJewel equipJewel = player.equipJewel.get(id);
        if (CheckNull.isNull(equipJewel) || equipJewel.canUseCnt() < count) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), " 宝石数量不足，不能扣除, roleId:", player.roleId, ", id:",
                    id, ", need:", count, ", have:", null == equipJewel ? 0 : equipJewel.getCount(), ", canUse:",
                    null == equipJewel ? 0 : equipJewel.canUseCnt());
        }
        equipJewel.subCount(count);
        // 记录道具变更
        LogLordHelper.jewel(from, player.account, player.lord, id, equipJewel.getCount(), count, Constant.ACTION_SUB);
    }

    /**
     * 扣除玩家建筑资源
     *
     * @param player
     * @param resourceType 资源类型
     * @param count        扣除数量，非负整数
     * @param from
     * @throws MwException
     */
    public void subResource(Player player, int resourceType, long count, AwardFrom from) throws MwException {
        if (null == player) {
            return;
        }

        if (count < 0 || count > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), " 传入的扣除数量非法，不能扣除, roleId:", player.roleId,
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
            throw new MwException(GameError.INVALID_PARAM.getCode(), " 传入的扣除数量非法，不能扣除, roleId:", player.roleId,
                    ", resourceType:", resourceType, ", need:", count);
        }

        checkArmyIsEnough(player.resource, resourceType, count);

        modifyArmyResource(player, resourceType, -count, 0, from);
    }

    /**
     * 扣除玩家货币类资源
     *
     * @param player
     * @param moneyType 货币分类型
     * @param count     数量，非负整数
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
        //上报数数
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
        //上报数数
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
        //上报数数
        if (ArrayUtils.contains(HAS_INFO2_AWARD_FROM, from)) {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_ESSENCE, sub,
                    player.lord.getTreasureWareGolden(), paramString, "");
        } else {
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.TREASURE_WARE_ESSENCE, sub,
                    player.lord.getTreasureWareGolden(), paramString, "");
        }

    }

    /**
     * 扣减古籍
     *
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
     * 减少金锭
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
     * 减少金锭
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
     * 扣教官积分
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
     * @Description: 扣金条
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
     * @Description: 扣荣誉
     */
    private void subHonor(Player player, int sub, AwardFrom from) {
        if (sub > 0) {
            player.lord.setHonor(player.lord.getHonor() - sub);
            LogLordHelper.honor(from, player.account, player.lord, player.lord.getHonor(), -sub);
            EventDataUp.otherCurrency(from, player.account, player.lord, AwardType.Money.HONOR, -sub, player.lord.getHonor(), "[]", "");
        }
    }

    /**
     * 扣积分
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
     * 扣将令q
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

    /*-----------------------------------------减少相关end-------------------------------------------*/

    /*-----------------------------------------checkAndsub相关end-------------------------------------------*/

    /**
     * 扣体力
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
     * 扣军功
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
     * 扣金币
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
     * 扣除金币
     *
     * @param player  角色对象
     * @param sub     扣除数量
     * @param from    细分来源
     * @param joinAct 参与活动
     * @param param   参数
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

        if (joinAct) { // 参与活动
            activityDataManager.updActivity(player, ActivityConst.ACT_COST_GOLD, sub, 0, true);
            activityDataManager.updActivity(player, ActivityConst.ACT_ORE_TURNPLATE, sub, 0, true);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_COST_GOLD, sub);
            activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_COST_GOLD, sub);

            try {
                // 消费排行
                activityDataManager.updRankActivity(player, ActivityConst.ACT_CONSUME_GOLD_RANK, sub);
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }

        if (!ArrayUtils.contains(NOT_COUNTED_IN_CONSUMPTION, from)) {
            //貂蝉任务-消耗钻石
            ActivityDiaoChanService.completeTask(player, ETask.CONSUME_DIAMOND, sub);
            TaskService.processTask(player, ETask.CONSUME_DIAMOND, sub);

            TaskService.handleTask(player, ETask.CONSUME_DIAMOND, sub);
            titleService.processTask(player, ETask.FIRST_USE_DIAMOND, sub);
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_GOLD_CNT, sub);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_SUB_GOLD_CNT, sub);
            // 记录阵营玩家金币消耗
            campService.receiveCostGold(player, sub);
            // 勇冠三军金币消耗的进度
            activityDataManager.updGlobalActivity(player, ActivityConst.ACT_BRAVEST_ARMY, sub, player.lord.getCamp());
        }

        LogLordHelper.gold(from, player.account, lord, -sub, 0, param);
    }

    /**
     * 不计入消耗功能数组
     */
    static final AwardFrom[] NOT_COUNTED_IN_CONSUMPTION = new AwardFrom[]{AwardFrom.JOIN_IN_BIDDING_AUCTION};

    /**
     * 按传入参数扣除玩家游戏资源，资源被扣除后，将会向客户端同步信息<br>
     * 特别注意：执行该方法，默认已经检查过玩家资源足够，且传入参数的格式正确
     *
     * @param player
     * @param subList 需要扣除的玩家资源列表，内部嵌套的List的长度不小于3
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
            // 记录更改过的玩家游戏资源类型
            change.addChangeType(list.get(0), list.get(1));
        }
        if (sync) {
            // 向客户端同步玩家资源数据
            syncRoleResChanged(player, change);
        }
    }

    /**
     * 不同步扣除玩家资源
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
            // 记录更改过的玩家游戏资源类型
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
     * 按传入参数扣除玩家游戏资源，资源被扣除后，将会向客户端同步信息<br>
     * 特别注意：执行该方法，默认已经检查过玩家资源足够，且传入参数的格式正确
     *
     * @param player
     * @param subList 需要扣除的玩家资源列表，内部嵌套的List的长度不小于3
     * @param num     倍数
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
            // 记录更改过的玩家游戏资源类型
            change.addChangeType(list.get(0), list.get(1));
        }
        // 向客户端同步玩家资源数据
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
     * @Description: 扣除玩家资源
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
            // 减少英雄碎片
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
     * 扣除天赋石
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
     * 检查并扣除玩家游戏资源，如果不足，将会抛出异常，资源被扣除后，将会向客户端同步信息
     *
     * @param player
     * @param subList
     * @param from
     * @param param   调用该方法的地方，可以选择在游戏资源不足时的提示文字中，插入一些需要打印的信息（比如调用该方法的目的），这些信息将被打印在提示文字的最前面
     * @throws MwException
     */
    public void checkAndSubPlayerRes(Player player, List<List<Integer>> subList, AwardFrom from, Object... param)
            throws MwException {
        checkAndSubPlayerRes(player, subList, true, from, param);
    }

    /**
     * 带翻倍检查并扣除玩家游戏资源，如果不足，将会抛出异常，资源被扣除后，将会向客户端同步信息
     *
     * @param player
     * @param subList
     * @param num     倍数
     * @param from
     * @param param   调用该方法的地方，可以选择在游戏资源不足时的提示文字中，插入一些需要打印的信息（比如调用该方法的目的），这些信息将被打印在提示文字的最前面
     * @throws MwException
     */
    public void checkAndSubPlayerRes(Player player, List<List<Integer>> subList, int num, AwardFrom from,
                                     Object... param) throws MwException {
        // 检查是否足够
        checkPlayerResIsEnough(player, subList, num);

        // 扣除资源
        subPlayerResHasChecked(player, subList, num, from, param);
    }

    /**
     * 检查并扣除玩家游戏资源，如果不足，将会抛出异常，资源被扣除后，将会向客户端同步信息
     *
     * @param player
     * @param subList
     * @param sync
     * @param from    是否同步扣除资源的信息
     * @param param   调用该方法的地方，可以选择在游戏资源不足时的提示文字中，插入一些需要打印的信息（比如调用该方法的目的），这些信息将被打印在提示文字的最前面
     * @throws MwException
     */
    public void checkAndSubPlayerRes(Player player, List<List<Integer>> subList, boolean sync, AwardFrom from,
                                     Object... param) throws MwException {
        // 检查是否足够
        checkPlayerResIsEnough(player, subList);
        // 扣除资源
        subPlayerResHasChecked(player, subList, sync, from, param);
    }

    /**
     * 扣除玩家指定的所有资源,先查询资源数量(有多少扣多少)
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

    /*-----------------------------------------checkAndsub相关end-------------------------------------------*/

    /*-----------------------------------------检测是否充足start-------------------------------------------*/

    /**
     * 检测并扣除玩家指定的资源(默认带同步)
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
     * 检测并扣除玩家指定的资源(可选择是否同步)
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
        // 检查是否足够
        checkPlayerResIsEnough(player, type, id, num);
        // 扣除资源
        subPlayerResHasChecked(player, type, id, num, from, param);
        if (syn) {
            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(type, id);
            // 向客户端同步玩家资源数据
            syncRoleResChanged(player, change);
        }

    }

    /**
     * 扣除玩家可扣除数量的资源
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
        // 检查是否足够
        long playerResCanSubNum = getPlayerResCanSubNum(player, type, id, num);
        // 扣除资源
        subPlayerResHasChecked(player, type, id, (int)playerResCanSubNum, from, param);
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(type, id);
        // 向客户端同步玩家资源数据
        syncRoleResChanged(player, change);
    }

    public void subAndSyncProp(Player player, int type, int propId, int count, AwardFrom from, Object... params) throws MwException {
        subPlayerResHasChecked(player, type, propId, count, from, params);
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(type, propId);
        // 向客户端同步玩家资源数据
        syncRoleResChanged(player, change);
    }

    public void checkAndSubPlayerRes4List(Player player, List<Integer> subList, AwardFrom from, Object... param)
            throws MwException {

        if (null == player || CheckNull.isEmpty(subList) || subList.size() < 3) {
            LogUtil.error("判断玩家道具是否足够时，需求列表的格式不正确，跳过, needList:", subList);
            return;
        }

        if (subList.get(2) == 0) {
            return;
        }
        // 检查是否足够
        checkPlayerResIsEnough(player, subList.get(0), subList.get(1), subList.get(2));

        // 扣除资源
        subPlayerResHasChecked(player, subList.get(0), subList.get(1), subList.get(2), from, param);

        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(subList.get(0), subList.get(1));
        // 向客户端同步玩家资源数据
        syncRoleResChanged(player, change);
    }

    /**
     * 检查资源是否足够。 false 表示资源不足
     *
     * @param player
     * @param type   类型
     * @param id     编号
     * @param add    数量
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
     * 检查玩家的游戏资源（包括货币、建筑资源、道具等）是否足够
     *
     * @param player
     * @param needList 需求集合，比较对象
     * @param message  调用该方法的地方，可以选择在游戏资源不足时的提示文字中，插入一些需要打印的信息（比如调用该方法的目的），这些信息将被打印在提示文字的最前面
     * @throws MwException
     */
    public void checkPlayerResIsEnough(Player player, List<List<Integer>> needList, String... message)
            throws MwException {
        if (null == player || CheckNull.isEmpty(needList)) {
            return;
        }

        for (List<Integer> list : needList) {
            if (CheckNull.isEmpty(list) || list.size() < 3) {
                LogUtil.error(message, "判断玩家道具是否足够时，需求列表的格式不正确，跳过, needList:", needList);
                return;
            }

            if (list.get(2) == 0) {
                continue;
            }

            checkPlayerResIsEnough(player, list.get(0), list.get(1), list.get(2), message);
        }
    }

    /**
     * 检查玩家的游戏资源（包括货币、建筑资源、道具等）是否足够
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
                    LogUtil.error("判断玩家道具是否足够时，需求列表的格式不正确，跳过, needList:", needList);
                    return false;
                }
                if (list.get(2) == 0) {
                    continue;
                }
                checkPlayerResIsEnough(player, list.get(0), list.get(1), list.get(2));
            }
            return true;
        } catch (Exception e) {
            LogUtil.error("判断玩家道具是否足够时,道具不满足,needList:", needList);
        }
        return false;
    }

    /**
     * 带翻倍的检查玩家的游戏资源（包括货币、建筑资源、道具等）是否足够
     *
     * @param player
     * @param needList
     * @param num      倍数
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
                LogUtil.error(message, "判断玩家道具是否足够时，需求列表的格式不正确，跳过, needList:", needList);
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
     * @Description: 检测 玩家资源是否足够
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
     * 获取玩家可扣减的资源数量
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
     * 校验英雄碎片
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
                    player.roleId, ownNum, need, heroId, ObjectUtils.isEmpty(message) ? "" : Arrays.toString(message)));
        }
    }

    /**
     * 获取玩家可扣减的英雄碎片数量
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
     * 检测特殊道具时候足够
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
     * 检测赛季天赋石是否足够
     *
     * @param player
     * @param need
     * @throws MwException
     */
    private void checkSeasonTalentStoneIsEnough(Player player, long need) throws MwException {
        SeasonTalent talent = player.getPlayerSeasonData().getSeasonTalent();
        int have = talent.getRemainStone();
        if (have < need) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), String.format("roleId :%d, 赛季天赋石不足 need :%d, have :%d",
                    player.getLordId(), need, have));
        }
    }

    /**
     * 获取玩家可扣减的赛季天赋石数量
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
            throw new MwException(GameError.SANDTABLE_CONTEST_SCORE_NOT_ENOUGHT.getCode(), message, " 沙盘演武积分不足, roleId:", player.getLordId(),
                    ", type:", id, ", need:", num, ", have:", player.getSandTableScore());
        }
    }

    private int getCanSubSandTableScoreNum(Player player, int need) throws MwException {
        return Math.min(player.getSandTableScore(), need);
    }

    /**
     * 检测玩家装备宝石是否足够
     *
     * @param player  玩家对象
     * @param id      宝石等级
     * @param num     数量
     * @param message 消息
     * @throws MwException 自定义异常
     */
    private void checkEquipJewel(Player player, int id, int num, String[] message) throws MwException {
        if (null == player) {
            return;
        }
        EquipJewel equipJewel = player.equipJewel.get(id);
        if (null == equipJewel || equipJewel.canUseCnt() < num) {
            throw new MwException(GameError.STONE_NOT_ENOUGH.getCode(), message, " 宝石数量不足, roleId:", player.roleId,
                    ", id:", id, ", need:", num, ", have:", null == equipJewel ? 0 : equipJewel.getCount(),
                    ", canUseCnt:", equipJewel.canUseCnt());
        }
    }

    /**
     * 获取玩家可扣减的装备宝石数量
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
     * 检查玩家的战机碎片是否足够
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
            throw new MwException(GameError.PLANE_CHIP_NOT_ENOUGH.getCode(), message, " 战机碎片数量不足, roleId:",
                    player.roleId, ", id:", id, ", need:", count, ", have:", planeChip.getCnt());
        }
    }

    /**
     * 获取玩家可扣减的战机碎片数量
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
     * 检查玩家的货币是否足够
     *
     * @param player
     * @param moneyType
     * @param need
     * @param message   调用该方法的地方，可以选择在货币不足时的提示文字中，插入一些需要打印的信息，这些信息将被打印在提示文字的最前面
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
            throw new MwException(code, message, " 货币数量不足, roleId:", lord.getLordId(), ", type:", moneyType, ", need:",
                    need, ", have:", count);
        }
    }

    /**
     * 获取玩家可扣减的货币数量
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
     * 检查玩家建筑资源是否足够，如果不足，将会抛出异常
     *
     * @param resource
     * @param resourceType
     * @param need
     * @param message      调用该方法的地方，可以选择在建筑资源不足时的提示文字中，插入一些需要打印的信息，这些信息将被打印在提示文字的最前面
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
                throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " 资源数量不足, roleId:",
                        resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
        if (count < need) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " 资源数量不足, roleId:",
                    resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
    }

    /**
     * 获取玩家可扣减的建筑资源数量
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

    /*-----------------------------------------检测是否充足end-------------------------------------*/

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
                throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " 资源数量不足, roleId:",
                        resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
        if (count < need) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), message, " 资源数量不足, roleId:",
                    resource.getLordId(), ", type:", resourceType, ", need:", need, ", have:", count);
        }
    }

    /**
     * 获取玩家可扣减的士兵数量
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

    /*-----------------------------------------同步start-------------------------------------------*/

    /**
     * 检查玩家的道具是否足够，如果不足，将会抛出异常
     *
     * @param player
     * @param propId
     * @param count
     * @param message 调用该方法的地方，可以选择在道具不足时的提示文字中，插入一些需要打印的信息，这些信息将被打印在提示文字的最前面
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
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), message, " 道具数量不足, roleId:", player.roleId,
                    ", propId:", propId, ", need:", count, ", have:", null == prop ? 0 : prop.getCount());
        }
    }

    /**
     * 获取玩家可扣减的道具数量
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
     * 检查玩家的宝石是否足够，如果不足，将会抛出异常
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
            throw new MwException(GameError.STONE_NOT_ENOUGH.getCode(), message, " 宝石数量不足, roleId:", player.roleId,
                    ", id:", id, ", need:", count, ", have:", null == stone ? 0 : stone.getCnt());
        }
    }

    /**
     * 获取玩家可扣减的宝石数量
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
     * 判断道具是否足够，返回bool值
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

    /*-----------------------------------------工具相关end-------------------------------------------*/
    /*-----------------------------------------工具相关start-------------------------------------------*/

    /**
     * 根据奖励类型获取当前剩余玩家资源数量
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
                // 将领当前兵力
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
                    // 获取的是可以使用的数量
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
     * 向客户端同步玩家当前资源（包括玩家所有游戏资源）数量，只同步传入的资源类型
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
     * 向客户端同步玩家当前资源（包括玩家所有游戏资源）数量，只同步传入的资源类型
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
     * 检查背包满
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
            throw new MwException(GameError.MAX_EQUIP_STORE.getCode(), "装备背包满了, roleId:" + player.roleId + ",equipSize="
                    + player.equips.size() + ",bagCnt=" + player.common.getBagCnt() + ",cnt=" + cnt);
        }
    }

    /**
     * 检查背剩余空间
     *
     * @param player
     * @param needCnt 还需要的空间数量
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
            throw new MwException(GameError.MAX_EQUIP_STORE.getCode(), "装备背包容量不足, roleId:", player.roleId,
                    ",equipSize=", player.equips.size(), ",bagCnt=", player.common.getBagCnt(), ",cnt=", cnt,
                    ", needCnt:", needCnt);
        }
    }

    /**
     * 检查背包数量是否已经满
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
     * 检查特殊装备背剩余空间
     *
     * @param player
     * @param needCnt 还需要的空间数量
     * @throws MwException
     */
    public void checkMentorEquipBag(Player player, int needCnt) throws MwException {
        int cnt = 0;
        MentorInfo mentorInfo = player.getMentorInfo();
        int hasCnt = (int) mentorInfo.getEquipMap().values().stream().filter(me -> me.getMentorId() == 0).count();
        cnt = hasCnt + needCnt;
        if (cnt > MENTOR_EQUIP_CNT) {
            throw new MwException(GameError.MAX_EQUIP_STORE.getCode(), "装备背包容量不足, roleId:", player.roleId, ",hasCnt=",
                    hasCnt, ",cnt=", cnt, ", needCnt:", needCnt);
        }
    }

    private static final AwardFrom[] HAS_INFO2_AWARD_FROM = new AwardFrom[]{AwardFrom.TREASURE_ON_HOOK_AWARD};




    /*-----------------------------------------工具相关end-------------------------------------------*/
}
