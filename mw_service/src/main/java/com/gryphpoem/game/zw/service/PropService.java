package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.pb.GamePb1.BuyBuildRs;
import com.gryphpoem.game.zw.pb.GamePb1.BuyPropRs;
import com.gryphpoem.game.zw.pb.GamePb1.GetPropsRs;
import com.gryphpoem.game.zw.pb.GamePb1.UsePropRs;
import com.gryphpoem.game.zw.pb.GamePb1.UsePropRs.Builder;
import com.gryphpoem.game.zw.pb.GamePb4.SyncBuffRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.Mentor;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticRandomProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticWeightBoxProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.session.SeasonService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import com.gryphpoem.game.zw.service.totem.TotemService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author TanDonghai
 * @ClassName PropService.java
 * @Description 道具相关
 * @date 创建时间：2017年3月27日 下午8:28:59
 */
@Service
public class PropService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;

    private static final Integer PURPLE_PROP_TYPE = 68;

    private static final Integer ORANGE_PROP_TYPE = 69;

    @Autowired
    private PlayerService playerService;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private SeasonTalentService seasonTalentService;
    @Autowired
    private TotemService totemService;

    /**
     * 获取玩家背包的道具
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPropsRs getProps(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb1.GetPropsRs.Builder builder = GamePb1.GetPropsRs.newBuilder();
        for (Prop prop : player.props.values()) {
            if (prop.getCount() == 0 && Arrays.binarySearch(PROP_ID_USETIMES, prop.getPropId()) < 0) {
                continue;
            }
            refreshProp(prop);

            builder.addProp(PbHelper.createPropPb(prop));
        }
        builder.setTotemDataInfo(totemService.buildTotemDataInfo(player));
        // 返回所有装备信息的协议
        return builder.build();
    }

    static final int[] PROP_ID_USETIMES = {20001};

    /**
     * 道具使用
     *
     * @param roleId
     * @param propId
     * @param count
     * @throws MwException
     */
    public UsePropRs useProp(long roleId, int propId, int count, String params) throws MwException {
        if (count <= 0 || count > 100) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "道具使用,数量错误, roleId:" + roleId + ",count=" + count);
        }

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
        if (staticProp == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "道具使用,无此配置道具, roleId:" + roleId + ",propId=" + propId);
        }
        if (staticProp.getUseLv() > player.lord.getLevel()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "玩家等级不够, roleId:", roleId, ",propId=", propId,
                    ", lv:", player.lord.getLevel());
        }
        int needVip = staticProp.getUseVip();
        int vip = player.lord.getVip();
        if (needVip > 0 && vip < needVip) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "玩家VIP等级不够, roleId:", roleId, ",propId=", propId, ", vip:", vip);
        }
        Prop prop = player.props.get(propId);
        if (prop == null || prop.getCount() == 0) {
            throw new MwException(GameError.NO_PROP.getCode(), "道具使用,无此道具, roleId:" + roleId + ",count=" + count);
        }
        // 在新地图中, 禁止使用保护罩
        if (player.lord.getArea() == CrossWorldMapConstant.CROSS_MAP_ID && (propId == PropConstant.PROTECT_PROP_4500 || propId == PropConstant.PROTECT_PROP_4501 || propId == PropConstant.PROTECT_PROP_4502)) {
            throw new MwException(GameError.WAR_FIRE_CANT_USE_PROTECT.getCode(), "战火燎原地图中, 禁止使用保护罩, roleId:" + roleId + ",count=" + count);
        }
        //赛季道具检查
        if (!seasonService.checkPropUse(staticProp)) {
            throw new MwException(GameError.SEASON_PROP_NOT_USE.getCode(), GameError.SEASON_PROP_NOT_USE.errMsg(roleId, propId));
        }

        // 次数限制
        checkPropUseCount(player, prop, count);
        // 如果道具为装备，验证装备背包
        List<List<Integer>> rewardList = staticProp.getRewardList();
        if (rewardList != null && rewardList.size() > 0) {
            List<Integer> e = rewardList.get(0);
            Integer type = e.get(1);
            if (PURPLE_PROP_TYPE.equals(type) || ORANGE_PROP_TYPE.equals(type)) {
                rewardDataManager.checkBagCnt(player, count);
            }
        }
        // 检测特殊道具箱子
        checkSpecialEquip(player, staticProp, count);
        // 效果道具检测,有更高效果的buff时不能使用低级效果buff
        checkEffectProp(player, staticProp);
        if (staticProp.getPropType() == PropConstant.PropType.TREASURE_LOCK_BOX) {//带锁的宝箱
            checkAndSubTreasureBox(player, staticProp, prop, count);
        } else {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, count, AwardFrom.USE_PROP);
        }

        UsePropRs.Builder builder = UsePropRs.newBuilder();
        ChangeInfo change = ChangeInfo.newIns();
        List<CommonPb.Award> listAward = new ArrayList<>();
        if (staticProp.getPropType() == PropConstant.PropType.CHOOSE_PROP_TYPE) {
            //自选宝箱跑马灯判断
            useChoosePropTreasure(count, staticProp, player, prop, params, roleId, propId, listAward, change);
        } else if (staticProp.getPropType() == PropConstant.PropType.CHOOSE_HERO_FRAGMENT_LIMIT_HAVE) {
            // 自选已拥有的武将碎片
            useChooseHeroFragment(count, staticProp, player, prop, params, roleId, propId, listAward, change);
        } else if (staticProp.getPropType() == PropConstant.PropType.ADD_TREASURE_WARE_PROP) {
            DataResource.getBean(TreasureCombatService.class).useProp(count, player, staticProp.getRewardList(), propId, listAward, change);
        } else if (staticProp.getPropType() == PropConstant.PropType.SUPPER_EQUIP_CHIP) {
            // 神器碎片出售处理
            useSuperEquipChip(count, staticProp, player, listAward, params);
        } else {
            for (int i = 0; i < count; i++) {
                if (!CheckNull.isEmpty(staticProp.getRewardList())) {
                    listAward.addAll(rewardDataManager.addAwardDelaySync(player, staticProp.getRewardList(), change,
                            AwardFrom.USE_PROP));
                }
            }
        }

        // 检测道具是否需要发送跑马灯
        if (!ObjectUtils.isEmpty(listAward))
            checkAndSendSysChat(propId, player, listAward);

        if (count == 1 || checkAwardType(listAward)) {
            // 暂时只有使用数量为1的时候, 允许限制奖励转换
            builder.addAllAward(listAward);
        } else {
            Map<Integer, Map<Integer, Integer>> typeMap = new HashMap<>();
            Map<Integer, Integer> idMap = null;
            for (CommonPb.Award award : listAward) {
                idMap = typeMap.computeIfAbsent(award.getType(), k -> new HashMap<>());
                Integer val = idMap.get(award.getId());
                val = val == null ? 0 : val;
                idMap.put(award.getId(), val + award.getCount());
            }
            for (Entry<Integer, Map<Integer, Integer>> entry : typeMap.entrySet()) {
                for (Entry<Integer, Integer> kv : entry.getValue().entrySet()) {
                    builder.addAward(
                            CommonPb.Award.newBuilder().setType(entry.getKey()).setId(kv.getKey()).setCount(kv.getValue()));
                }
            }
        }
        // 随机材料箱子比较处理
        if (staticProp.getPropType() == PropConstant.PropType.RANDMON_PROP_TYPE) {
            StaticRandomProp sRandomProp = StaticPropDataMgr.getRandomPropById(staticProp.getPropId());
            if (sRandomProp != null) {
                for (int i = 0; i < count; i++) {
                    // 比较那个找出物品数量最小
                    int minPropId = sRandomProp.getCompare().stream().map(pId -> {
                        Prop p = player.props.get(pId);
                        int cnt = p == null ? 0 : p.getCount();
                        return new Turple<>(pId, cnt);
                    }).min(Comparator.comparingInt(Turple::getB)).get().getA();

                    int minPropCnt = RandomHelper.randomInArea(sRandomProp.getRandomNum().get(0),
                            sRandomProp.getRandomNum().get(1));
                    builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.PROP, minPropId, minPropCnt,
                            AwardFrom.USE_PROP));
                }
            }
        } else if (staticProp.getPropType() == PropConstant.PropType.WEIGHT_CNT_BOX_PROP_TYPE) {// 权重次数箱
            StaticWeightBoxProp sWeightBoxProp = StaticPropDataMgr.getWeightBoxPropMapById(staticProp.getPropId());
            if (sWeightBoxProp != null) {
                for (int i = 0; i < count; i++) {
                    List<Integer> maxMin = RandomUtil.getWeightByList(sWeightBoxProp.getRandomNum(), w -> w.get(2));
                    if (!CheckNull.isEmpty(maxMin)) {
                        int cnt = RandomHelper.randomInArea(maxMin.get(0), maxMin.get(1) + 1);
                        if (cnt > 0) {
                            builder.addAward(rewardDataManager.addAwardSignle(player, sWeightBoxProp.getReward().get(0),
                                    sWeightBoxProp.getReward().get(1), cnt, AwardFrom.USE_PROP));
                        }
                    }
                }
            }
        } else if (staticProp.getPropType() == PropConstant.PropType.ONHOOK_CARD_TYPE) {//挂机卡
            playerService.activateMonthCard(player, FunCard.CARD_TYPE[8], staticProp.getDuration() * count);
        }

        boolean hasEffect = processEffect(player, staticProp);
        if (hasEffect) {
            for (Entry<Integer, Effect> kv : player.getEffect().entrySet()) {
                builder.addEffect(PbHelper.createEffectPb(kv.getValue()));
            }
        }
        updatePorpUseCountAndDate(prop, count);
        builder.setProp(PbHelper.createPropPb(prop));
        builder.setCount(prop.getCount());
        // 填充equip值
        fillEquipVal(player, builder, listAward);
        return builder.build();
    }

    /**
     * 出售多余的神器碎片
     *
     * @param count      要出售的数量
     * @param staticProp 道具配置
     * @param player     玩家信息
     * @param listAward  奖励列表
     */
    private void useSuperEquipChip(int count,
                                   StaticProp staticProp,
                                   Player player,
                                   List<Award> listAward,
                                   Object params) {
        // 判断玩家该神器是否已合成
        int propId = staticProp.getPropId();
        Integer superEquipType = StaticPropDataMgr.getSuperEquipType(propId);
        SuperEquip superEquip = player.supEquips.get(superEquipType);
        if (superEquip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "神器还未打造, roleId:", player.roleId, ", type:", superEquipType);
        }

        // 判断玩家要出售的碎片数量是否足够，主逻辑已校验

        // 兑换奖励
        List<List<Integer>> rewardList = staticProp.getRewardList();
        if (CheckNull.isEmpty(rewardList)) {
            throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(player.roleId, propId));
        }

        List<Integer> reward = null;
        List<List<Integer>> rewardArr;
        for (List<Integer> tmpReward : rewardList) {
            if (CheckNull.isEmpty(tmpReward) || tmpReward.size() < 3) {
                throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(player.roleId, propId));
            }
            if (tmpReward.get(1) == propId) {
                reward = tmpReward;
                break;
            }
        }
        if (ObjectUtils.isEmpty(reward)) {
            throw new MwException(GameError.CHOOSE_PROP_ERROR.getCode(), GameError.CHOOSE_PROP_ERROR.errMsg(player.roleId, propId));
        }

        rewardArr = new ArrayList<>();
        rewardArr.add(reward);
        listAward.addAll(rewardDataManager.sendReward(player, rewardArr, count, AwardFrom.USE_PROP));

        // 扣减神器碎片数量
        rewardDataManager.subProp(player, propId, count, AwardFrom.USE_PROP, params);
    }

    /**
     * 校验不合并的奖励类型项
     *
     * @param listAward
     * @return
     */
    private boolean checkAwardType(List<CommonPb.Award> listAward) {
        if (ObjectUtils.isEmpty(listAward))
            return false;

        for (CommonPb.Award award : listAward) {
            if (ArrayUtils.contains(NOT_COMBINE_AWARD, award.getType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 不合并相同id奖励类型
     */
    private static final int[] NOT_COMBINE_AWARD = {AwardType.TREASURE_WARE};

    /**
     * 使用自选箱道具
     * @param count
     * @param staticProp
     * @param player
     * @param prop
     * @param params
     * @param roleId
     * @param propId
     * @param listAward
     * @param change
     * @throws MwException
     */
    private void useChoosePropTreasure(int count, StaticProp staticProp, Player player, Prop prop,
                                       String params, long roleId, int propId, List<CommonPb.Award> listAward,
                                       ChangeInfo change) throws MwException {
        //因跑马灯在此判断，因此将判断加在这里
        if (count != 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱使用非一个, roleId: ", player.roleId,
                    "usedCount: ", prop.getCount(), ", count = ", count);
        }

        Integer choosePropId;
        try {
            choosePropId = Integer.parseInt(params);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱参数错误, roleId:", player.roleId,
                    "usedCount", prop.getCount(), ", params=" + params);
        }
        if (ObjectUtils.isEmpty(choosePropId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱参数错误, roleId:", player.roleId,
                    "usedCount", prop.getCount(), ", params=" + params);
        }

        if (CheckNull.isEmpty(staticProp.getRewardList())) {
            throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
        }

        List<Integer> reward = null;
        List<List<Integer>> rewardArr;
        for (List<Integer> tmp : staticProp.getRewardList()) {
            if (CheckNull.isEmpty(tmp) || tmp.size() < 3) {
                throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
            }
            if (tmp.get(1) == choosePropId.intValue()) {
                reward = tmp;
                break;
            }
        }
        if (ObjectUtils.isEmpty(reward)) {
            throw new MwException(GameError.CHOOSE_PROP_ERROR.getCode(), GameError.CHOOSE_PROP_ERROR.errMsg(roleId, propId));
        }

        rewardArr = new ArrayList<>();
        rewardArr.add(reward);
        listAward.addAll(rewardDataManager.addAwardDelaySync(player, rewardArr, change,
                AwardFrom.USE_PROP));
    }

    /**
     * 选择武将碎片自选箱
     *
     * @param count
     * @param staticProp
     * @param player
     * @param prop
     * @param params
     * @param roleId
     * @param propId
     * @param listAward
     * @param change
     * @throws MwException
     */
    private void useChooseHeroFragment(int count, StaticProp staticProp, Player player, Prop prop,
                                       String params, long roleId, int propId, List<CommonPb.Award> listAward,
                                       ChangeInfo change) throws MwException {
        //因跑马灯在此判断，因此将判断加在这里
//        if (count != 1) {
//            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱使用非一个, roleId: ", player.roleId,
//                    "usedCount: ", prop.getCount(), ", count = ", count);
//        }

        Integer choosePropId;
        try {
            choosePropId = Integer.parseInt(params);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱参数错误, roleId:", player.roleId,
                    "usedCount", prop.getCount(), ", params=" + params);
        }
        if (ObjectUtils.isEmpty(choosePropId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱参数错误, roleId:", player.roleId,
                    "usedCount", prop.getCount(), ", params=" + params);
        }

        // 必须拥有此武将才可以领取碎片
        Hero hero = player.heros.get(choosePropId);
        if (CheckNull.isNull(hero)) {
            throw new MwException(GameError.HERO_NOT_FOUND, String.format("hero not found, heroId:%d, roleId:%d", choosePropId, player.roleId));
        }
        if (CheckNull.isEmpty(staticProp.getRewardList())) {
            throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
        }

        List<Integer> reward = null;
        List<List<Integer>> rewardArr;
        for (List<Integer> tmp : staticProp.getRewardList()) {
            if (CheckNull.isEmpty(tmp) || tmp.size() < 3) {
                throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
            }
            if (tmp.get(1) == choosePropId.intValue()) {
                reward = tmp;
                break;
            }
        }
        if (ObjectUtils.isEmpty(reward)) {
            throw new MwException(GameError.CHOOSE_PROP_ERROR.getCode(), GameError.CHOOSE_PROP_ERROR.errMsg(roleId, propId));
        }

        rewardArr = new ArrayList<>();
        rewardArr.add(reward);
        listAward.addAll(rewardDataManager.sendReward(player, rewardArr, count, AwardFrom.USE_PROP));
    }

    private void checkAndSubTreasureBox(Player player, StaticProp sProp, Prop prop, int useCount) throws MwException {
        if (prop.getCount() < useCount) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), String.format("roleId :%d, 宝箱 :%d 剩余数量 :%d 异常使用数量 :%d !!!",
                    player.getLordId(), prop.getPropId(), prop.getCount(), useCount));
        }
        //开宝箱消耗的资源
        List<Integer> keyCost = sProp.getKey();
        if (sProp.getKey() == null || keyCost.size() != 3) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, 宝箱 :%d 未配置钥匙消耗数量, 或者配置异常", player.getLordId(), prop.getPropId()));
        }
        int costKeyId = keyCost.get(1);
        int costKeyCount = keyCost.get(2) * useCount;
        Prop keyProp = player.props.get(costKeyId);
        if (keyProp == null || keyProp.getCount() < costKeyCount) {//消耗钥匙数量不足
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), String.format("roleId :%d, 宝箱 :%d 钥匙ID :%d, 数量不足(%d -> %d) !!!",
                    player.getLordId(), prop.getPropId(), costKeyId, keyProp == null ? 0 : keyProp.getCount(), costKeyCount));
        }
        List<List<Integer>> openTreasureBoxCost = new ArrayList<>();
        openTreasureBoxCost.add(Arrays.asList(AwardType.PROP, prop.getPropId(), useCount));
        openTreasureBoxCost.add(Arrays.asList(keyCost.get(0), costKeyId, costKeyCount));
        rewardDataManager.checkAndSubPlayerRes(player, openTreasureBoxCost, true, AwardFrom.USE_PROP);
    }

    /**
     * 检测并发送道具广播
     *
     * @param propId    道具id
     * @param player    玩家对象
     * @param listAward 奖励列表
     */
    private void checkAndSendSysChat(int propId, Player player, List<Award> listAward) {
        Optional.ofNullable(StaticPropDataMgr.getPropChatByPropId(propId))
                // 根据道具id获取跑马灯配置
                .ifPresent(propChats -> {
                    propChats.forEach(sPropChat -> {
                        List<Integer> item = sPropChat.getItem();
                        if (!CheckNull.isEmpty(item) && item.size() == 3) {
                            int type = item.get(0);
                            int id = item.get(1);
                            int count = item.get(2);
                            // 比较道具获取的奖励
                            if (listAward.stream().anyMatch(aw -> aw.getType() == type && aw.getId() == id && aw.getCount() == count)) {
                                int chatId = sPropChat.getChatId();
                                int propChatId = sPropChat.getId();
                                chatDataManager.sendSysChat(chatId, player.lord.getCamp(), 0,
                                        player.lord.getNick(), propChatId);
                            }
                        }
                    });
                });
    }

    /**
     * 填充装备
     *
     * @param player
     * @param builder
     */
    private void fillEquipVal(Player player, final Builder builder, List<Award> listAward) {
        List<Award> awardList = listAward;
        awardList.stream().filter(award -> award.getType() == AwardType.EQUIP && award.hasKeyId())
                .map(award -> player.equips.get(award.getKeyId())).filter(equip -> equip != null)
                .forEach(equip -> builder.addEquip(PbHelper.createEquipPb(equip)));
    }

    /**
     * 检测特殊道具箱子
     *
     * @param player
     * @param staticProp
     * @param count
     * @throws MwException
     */
    private void checkSpecialEquip(Player player, StaticProp staticProp, int count) throws MwException {
        if (staticProp.getPropType() == PropConstant.PropType.MENTOR_BOX_PROP_TYPE) {// 教官箱子
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.UNLOCK_TYPE_MENTOR)) {
                throw new MwException(GameError.MENTOR_IS_UNLOCK.getCode(), "使用道具时, 教官装备功能未解锁, roleId:", player.roleId);
            }
            // 获得教官
            Mentor mentor = player.getMentorInfo().getMentors().get(MentorConstant.MENTOR_TYPE_1);
            if (mentor == null) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "使用道具时, 教官不存在, roleId:", player.roleId);
            }
            // duration 表示教官的等级
            int lv = staticProp.getDuration();
            if (mentor.getLv() < lv) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "使用道具时, 教官等级不足, roleId:", player.roleId,
                        ", propMentorLv:", lv, ", mentorLv", mentor.getLv());
            }
            // 特殊道具背包判断
            rewardDataManager.checkMentorEquipBag(player, count);
        } else if (staticProp.getPropType() == PropConstant.PropType.PLANE_BOX_PROP_TYPE) {
            // 飞机箱子
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.UNLOCK_TYPE_PLANE_MENTOR)) {
                throw new MwException(GameError.FUNCTION_LOCK.getCode(), "使用道具时,空军司令未解锁, roleId:", player.roleId);
            }
            Mentor mentor = player.getMentorInfo().getMentors().get(MentorConstant.MENTOR_TYPE_2);
            if (mentor == null) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "使用道具时, 飞机教官不存在, roleId:", player.roleId);
            }
            int lv = staticProp.getDuration();
            if (mentor.getLv() < lv) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "使用道具时, 飞机教官等级不足, roleId:", player.roleId,
                        ", propMentorLv:", lv, ", mentorLv", mentor.getLv());
            }
            // 特殊道具背包判断
            rewardDataManager.checkMentorEquipBag(player, count);
        }
    }

    /**
     * 更新道具次数日期更新
     *
     * @param prop
     * @param count
     */
    private void updatePorpUseCountAndDate(Prop prop, int count) {
        if (isRefreshPorp(prop.getPropId())) {
            prop.addUseCount(count);
            prop.setUseTime(TimeHelper.getCurrentSecond());
        }
    }

    /**
     * 刷新道具使用次数隔天刷新
     *
     * @param prop
     */
    private void refreshProp(Prop prop) {
        if (isRefreshPorp(prop.getPropId())) {
            Date now = new Date();
            Date preDate = new Date(prop.getUseTime() * 1000L);
            if (!DateUtils.isSameDay(now, preDate)) {
                prop.setUseCount(0);
            }
        }
    }

    /**
     * 是否是需要每日刷新的使用次数的道具
     *
     * @param propId
     * @return true 是
     */
    public boolean isRefreshPorp(int propId) {
        return PropConstant.PROP_ADD_POWER == propId;
    }

    /**
     * 检测道具使用次数是否满足
     *
     * @param player
     * @param prop
     * @param count
     * @throws MwException
     */
    private void checkPropUseCount(Player player, Prop prop, int count) throws MwException {
        if (isRefreshPorp(prop.getPropId())) {
            refreshProp(prop);
            int max = prop.getUseCount() + count;
            if (max > Constant.USE_PROP_ADD_POWER_MAX) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "体力丹道具使用已经超过使用上限, roleId:", player.roleId,
                        "usedCount", prop.getCount(), ",needUse=" + count);
            }
        }
    }

    /**
     * 检测效果道具
     *
     * @param player
     * @param staticProp
     * @throws MwException
     */
    private void checkEffectProp(Player player, StaticProp staticProp) throws MwException {
        if (staticProp.getAttrs() == null || staticProp.getAttrs().isEmpty() || staticProp.getAttrs().size() < 2) {
            return;
        }
        int effectType = staticProp.getAttrs().get(0);
        int val = staticProp.getAttrs().get(1);
        Effect effect = player.getEffect().get(effectType);
        if (effect != null && effect.getEffectVal() > val) {
            throw new MwException(GameError.YOUR_HAVE_BETTER_EFFECT.getCode(), "你已经拥有更好的buff效果, roleId:",
                    player.roleId);
        }
    }

    public boolean processEffect(Player player, StaticProp staticProp) {
        if (staticProp.getAttrs() == null || staticProp.getAttrs().isEmpty() || staticProp.getAttrs().size() < 2) {
            return false;
        }
        int effectType = staticProp.getAttrs().get(0);
        int val = staticProp.getAttrs().get(1);
        int now = TimeHelper.getCurrentSecond();

        Effect effect = player.getEffect().get(effectType);
        if (effect != null) {
            if (val > effect.getEffectVal()) {// 获得属性更强的buff,覆盖原来的buff
                effect.setEndTime(now + staticProp.getDuration());
                effect.setEffectVal(val);
            } else if (effect.getEffectVal() == val) {
                effect.setEndTime(effect.getEndTime() + staticProp.getDuration());
            }
        } else {
            effect = new Effect(effectType, val, now + staticProp.getDuration());
        }
        player.getEffect().put(effectType, effect);
        switch (effectType) {
            case EffectConstant.PROTECT:
                effect.setEffectVal(0); // 保护罩效果值为0
                seasonTalentService.execSeasonTalentEffect501(player, staticProp, effect);
                // 通知地图其他玩家有罩子
                noticeMap(player);
                break;
            case EffectConstant.ATK_MUT:
            case EffectConstant.DEF_MUT:
            case EffectConstant.WALK_SPEED:
            case EffectConstant.WALK_SPEED_HIGHT:
            case EffectConstant.ARM_CREATE_SPEED:
            case EffectConstant.BUILD_SPEED:
            case EffectConstant.PREWAR_LEAD:
            case EffectConstant.PREWAR_WALK_SPEED:
            case EffectConstant.PREWAR_ATTACK_EXT:
                CalculateUtil.reCalcAllHeroAttr(player);
                break;
            case EffectConstant.BUILD_CNT:
                buildingService.addAtuoBuild(player);// 触发自动建造
                break;
            case EffectConstant.BANDIT_GOLD_BUFFER:
            case EffectConstant.BANDIT_WOOD_BUFFER:
                break;
            default:
                return false;
        }
        return true;
    }

    private void noticeMap(Player player) {
        if (player.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            int mapId = player.lord.getArea();
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
            if (cMap != null) {
                cMap.publishMapEvent(MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
            }
        } else {
            List<Integer> posList = new ArrayList<>();
            posList.add(player.lord.getPos());
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        }
    }

    /**
     * 道具购买
     *
     * @param roleId
     * @param propId
     * @param num
     * @return
     * @throws MwException
     */
    public BuyPropRs buyProp(Long roleId, int propId, int num) throws MwException {
        if (num <= 0) {
            num = 1;
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
        if (staticProp == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "道具购买,无此配置道具, roleId:" + roleId + ",propId=" + propId);
        }
        int needGold = staticProp.getPrice() * num;
        if (needGold <= 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "道具购买,无此配置道具, roleId:" + roleId + ",propId=" + propId);
        }
        // 检查玩家金币是否足够
        rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "道具购买");
        // 扣除相关金币
        rewardDataManager.subGold(player, needGold, AwardFrom.PROP_BUY, propId);

        rewardDataManager.addAward(player, AwardType.PROP, propId, num, AwardFrom.PROP_BUY, propId);
        BuyPropRs.Builder builder = BuyPropRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setProps(TwoInt.newBuilder().setV1(propId).setV2(player.props.get(propId).getCount()));
        return builder.build();
    }

    /**
     * 购买建造队列
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public BuyBuildRs buyBuild(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int propId = PropConstant.PROP_ID_BUILD_1;
        if (type != 1) {
            propId = PropConstant.PROP_ID_BUILD_2;
        }
        StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
        if (staticProp == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "道具购买,无此配置道具, roleId:" + roleId + ",propId=" + propId);
        }
        int now = TimeHelper.getCurrentSecond();
        Integer cnt = player.trophy.get(TrophyConstant.TROPHY_2);
        cnt = cnt != null ? cnt : 0;
        // 扣金币
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                staticProp.getPrice(), AwardFrom.BUY_BUILD);
        // 根据次数返还金币
        player.trophy.put(TrophyConstant.TROPHY_2, cnt + 1);
        if (cnt < Constant.BUY_BUILD_GAIN_GOLD.size()) {
            int gold = Constant.BUY_BUILD_GAIN_GOLD.get(cnt);
            Award en = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, gold);
            List<CommonPb.Award> awards = new ArrayList<CommonPb.Award>();
            awards.add(en);
            if (cnt == 0) {
                mailDataManager.sendAttachMail(player, awards, MailConstant.BUY_BUILD_1, AwardFrom.BUY_BUILD_MAIL, now,
                        gold, gold);
            } else {
                mailDataManager.sendAttachMail(player, awards, MailConstant.BUY_BUILD_2, AwardFrom.BUY_BUILD_MAIL, now,
                        gold, gold);
            }
        }

        BuyBuildRs.Builder builder = BuyBuildRs.newBuilder();

        // 加效果
        boolean hasEffect = processEffect(player, staticProp);
        if (hasEffect) {
            builder.setEffect(PbHelper.createEffectPb(player.getEffect().get(EffectConstant.BUILD_CNT)));
            // 触发自动建造
        }
        builder.setGold(player.lord.getGold());
        builder.setBuyBuildCnt(cnt + 1);
        return builder.build();
    }

    /**
     * 同步buff
     *
     * @param player
     * @param effects
     */
    public void syncBuffRs(Player player, Effect... effects) {
        if (player.isLogin && player.ctx != null && effects != null && effects.length > 0) {
            SyncBuffRs.Builder b = SyncBuffRs.newBuilder();
            for (Effect e : effects) {
                b.addEffect(PbHelper.createEffectPb(e));
            }
            // 推送
            Base.Builder msg = PbHelper.createSynBase(SyncBuffRs.EXT_FIELD_NUMBER, SyncBuffRs.ext, b.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }

    }

    /**
     * 合成道具
     *
     * @param roleId
     * @param propId
     */
    public GamePb1.SyntheticPropRs syntheticProp(long roleId, int propId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
        if (staticProp == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "合成道具,无此配置道具, roleId:", roleId, ", propId:", propId);
        }
        if (staticProp.getPropType() != 9) { // 碎片数量，适用于propType=9
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), "合成道具,道具不能合成, roleId:", roleId, ", propId:",
                    propId);
        }
        rewardDataManager.checkPropIsEnough(player, propId, staticProp.getChip(), "道具合成");
        rewardDataManager.subProp(player, propId, staticProp.getChip(), AwardFrom.SYNTHETIC_PROP);// , "道具合成"

        GamePb1.SyntheticPropRs.Builder builder = GamePb1.SyntheticPropRs.newBuilder();
        List<List<Integer>> rewardList = staticProp.getRewardList();
        if (!CheckNull.isEmpty(rewardList)) {
            builder.addAllAward(rewardDataManager.addAwardDelaySync(player, rewardList, null, AwardFrom.SYNTHETIC_PROP_AWARD));
        }
        return builder.build();
    }
}
