package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.manager.prop.PropDataManager;
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
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.session.SeasonService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import com.gryphpoem.game.zw.service.totem.TotemService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author TanDonghai
 * @ClassName PropService.java
 * @Description ????????????
 * @date ???????????????2017???3???27??? ??????8:28:59
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
    @Autowired
    private PropDataManager propDataManager;

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
     * ???????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPropsRs getProps(long roleId) throws MwException {
        // ????????????????????????
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
        // ?????????????????????????????????
        return builder.build();
    }

    static final int[] PROP_ID_USETIMES = {20001};

    /**
     * ????????????
     *
     * @param roleId
     * @param propId
     * @param count
     * @throws MwException
     */
    public UsePropRs useProp(long roleId, int propId, int count, String params) throws MwException {
        if (count <= 0 || count > 100) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "????????????,????????????, roleId:" + roleId + ",count=" + count);
        }

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
        if (staticProp == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????,??????????????????, roleId:" + roleId + ",propId=" + propId);
        }
        if (staticProp.getUseLv() > player.lord.getLevel()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "??????????????????, roleId:", roleId, ",propId=", propId,
                    ", lv:", player.lord.getLevel());
        }
        int needVip = staticProp.getUseVip();
        int vip = player.lord.getVip();
        if (needVip > 0 && vip < needVip) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "??????VIP????????????, roleId:", roleId, ",propId=", propId, ", vip:", vip);
        }
        Prop prop = player.props.get(propId);
        if (prop == null || prop.getCount() == 0) {
            throw new MwException(GameError.NO_PROP.getCode(), "????????????,????????????, roleId:" + roleId + ",count=" + count);
        }
        // ???????????????, ?????????????????????
        if (player.lord.getArea() == CrossWorldMapConstant.CROSS_MAP_ID && (propId == PropConstant.PROTECT_PROP_4500 || propId == PropConstant.PROTECT_PROP_4501 || propId == PropConstant.PROTECT_PROP_4502)) {
            throw new MwException(GameError.WAR_FIRE_CANT_USE_PROTECT.getCode(), "?????????????????????, ?????????????????????, roleId:" + roleId + ",count=" + count);
        }
        //??????????????????
        if (!seasonService.checkPropUse(staticProp)) {
            throw new MwException(GameError.SEASON_PROP_NOT_USE.getCode(), GameError.SEASON_PROP_NOT_USE.errMsg(roleId, propId));
        }

        // ????????????
        checkPropUseCount(player, prop, count);
        // ??????????????????????????????????????????
        List<List<Integer>> rewardList = staticProp.getRewardList();
        if (rewardList != null && rewardList.size() > 0) {
            List<Integer> e = rewardList.get(0);
            Integer type = e.get(1);
            if (PURPLE_PROP_TYPE.equals(type) || ORANGE_PROP_TYPE.equals(type)) {
                rewardDataManager.checkBagCnt(player, count);
            }
        }
        // ????????????????????????
        checkSpecialEquip(player, staticProp, count);
        // ??????????????????,??????????????????buff???????????????????????????buff
        checkEffectProp(player, staticProp);
        // ??????????????????????????????
        AbstractUseProp absUseProp = propDataManager.useProp(staticProp.getPropType());
        absUseProp.checkUseProp(count, staticProp, player, prop, params, roleId, propId, null, null);

        if (staticProp.getPropType() == PropConstant.PropType.TREASURE_LOCK_BOX) {
            //???????????????
            checkAndSubTreasureBox(player, staticProp, prop, count);
        } else {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, count, AwardFrom.USE_PROP);
        }

        UsePropRs.Builder builder = UsePropRs.newBuilder();
        ChangeInfo change = ChangeInfo.newIns();
        List<CommonPb.Award> listAward = new ArrayList<>();
        List<CommonPb.Award> awardList = absUseProp.useProp(count, staticProp, player, prop, params, roleId, propId, listAward, change);

        if (CheckNull.nonEmpty(listAward)) {
            // ???????????????????????????????????????
            checkAndSendSysChat(propId, player, listAward);
            if (count == 1 || checkAwardType(listAward)) {
                // ???????????????????????????1?????????, ????????????????????????
                builder.addAllAward(listAward);
            } else {
                // ????????????
                Map<Integer, Integer> idMap;
                Map<Integer, Map<Integer, Integer>> typeMap = new HashMap<>();
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
        }

        if (CheckNull.nonEmpty(awardList)) {
            builder.addAllAward(awardList);
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
        // ??????equip???
        fillEquipVal(player, builder, listAward);
        return builder.build();
    }

    /**
     * ?????????????????????????????????
     *
     * @param listAward
     * @return
     */
    private boolean checkAwardType(List<CommonPb.Award> listAward) {
        for (CommonPb.Award award : listAward) {
            if (ArrayUtils.contains(NOT_COMBINE_AWARD, award.getType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * ???????????????id????????????
     */
    private static final int[] NOT_COMBINE_AWARD = {AwardType.TREASURE_WARE};

    private void checkAndSubTreasureBox(Player player, StaticProp sProp, Prop prop, int useCount) throws MwException {
        if (prop.getCount() < useCount) {
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), String.format("roleId :%d, ?????? :%d ???????????? :%d ?????????????????? :%d !!!",
                    player.getLordId(), prop.getPropId(), prop.getCount(), useCount));
        }
        //????????????????????????
        List<Integer> keyCost = sProp.getKey();
        if (sProp.getKey() == null || keyCost.size() != 3) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, ?????? :%d ???????????????????????????, ??????????????????", player.getLordId(), prop.getPropId()));
        }
        int costKeyId = keyCost.get(1);
        int costKeyCount = keyCost.get(2) * useCount;
        Prop keyProp = player.props.get(costKeyId);
        if (keyProp == null || keyProp.getCount() < costKeyCount) {//????????????????????????
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), String.format("roleId :%d, ?????? :%d ??????ID :%d, ????????????(%d -> %d) !!!",
                    player.getLordId(), prop.getPropId(), costKeyId, keyProp == null ? 0 : keyProp.getCount(), costKeyCount));
        }
        List<List<Integer>> openTreasureBoxCost = new ArrayList<>();
        openTreasureBoxCost.add(Arrays.asList(AwardType.PROP, prop.getPropId(), useCount));
        openTreasureBoxCost.add(Arrays.asList(keyCost.get(0), costKeyId, costKeyCount));
        rewardDataManager.checkAndSubPlayerRes(player, openTreasureBoxCost, true, AwardFrom.USE_PROP);
    }

    /**
     * ???????????????????????????
     *
     * @param propId    ??????id
     * @param player    ????????????
     * @param listAward ????????????
     */
    private void checkAndSendSysChat(int propId, Player player, List<Award> listAward) {
        Optional.ofNullable(StaticPropDataMgr.getPropChatByPropId(propId))
                // ????????????id?????????????????????
                .ifPresent(propChats -> {
                    propChats.forEach(sPropChat -> {
                        List<Integer> item = sPropChat.getItem();
                        if (!CheckNull.isEmpty(item) && item.size() == 3) {
                            int type = item.get(0);
                            int id = item.get(1);
                            int count = item.get(2);
                            // ???????????????????????????
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
     * ????????????
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
     * ????????????????????????
     *
     * @param player
     * @param staticProp
     * @param count
     * @throws MwException
     */
    private void checkSpecialEquip(Player player, StaticProp staticProp, int count) throws MwException {
        if (staticProp.getPropType() == PropConstant.PropType.MENTOR_BOX_PROP_TYPE) {// ????????????
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.UNLOCK_TYPE_MENTOR)) {
                throw new MwException(GameError.MENTOR_IS_UNLOCK.getCode(), "???????????????, ???????????????????????????, roleId:", player.roleId);
            }
            // ????????????
            Mentor mentor = player.getMentorInfo().getMentors().get(MentorConstant.MENTOR_TYPE_1);
            if (mentor == null) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "???????????????, ???????????????, roleId:", player.roleId);
            }
            // duration ?????????????????????
            int lv = staticProp.getDuration();
            if (mentor.getLv() < lv) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "???????????????, ??????????????????, roleId:", player.roleId,
                        ", propMentorLv:", lv, ", mentorLv", mentor.getLv());
            }
            // ????????????????????????
            rewardDataManager.checkMentorEquipBag(player, count);
        } else if (staticProp.getPropType() == PropConstant.PropType.PLANE_BOX_PROP_TYPE) {
            // ????????????
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.UNLOCK_TYPE_PLANE_MENTOR)) {
                throw new MwException(GameError.FUNCTION_LOCK.getCode(), "???????????????,?????????????????????, roleId:", player.roleId);
            }
            Mentor mentor = player.getMentorInfo().getMentors().get(MentorConstant.MENTOR_TYPE_2);
            if (mentor == null) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "???????????????, ?????????????????????, roleId:", player.roleId);
            }
            int lv = staticProp.getDuration();
            if (mentor.getLv() < lv) {
                throw new MwException(GameError.MENTOR_NOT_FOUND.getCode(), "???????????????, ????????????????????????, roleId:", player.roleId,
                        ", propMentorLv:", lv, ", mentorLv", mentor.getLv());
            }
            // ????????????????????????
            rewardDataManager.checkMentorEquipBag(player, count);
        }
    }

    /**
     * ??????????????????????????????
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
     * ????????????????????????????????????
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
     * ???????????????????????????????????????????????????
     *
     * @param propId
     * @return true ???
     */
    public boolean isRefreshPorp(int propId) {
        return PropConstant.PROP_ADD_POWER == propId;
    }

    /**
     * ????????????????????????????????????
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
                throw new MwException(GameError.PARAM_ERROR.getCode(), "?????????????????????????????????????????????, roleId:", player.roleId,
                        "usedCount", prop.getCount(), ",needUse=" + count);
            }
        }
    }

    /**
     * ??????????????????
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
            throw new MwException(GameError.YOUR_HAVE_BETTER_EFFECT.getCode(), "????????????????????????buff??????, roleId:",
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
            if (val > effect.getEffectVal()) {// ?????????????????????buff,???????????????buff
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
                effect.setEffectVal(0); // ?????????????????????0
                seasonTalentService.execSeasonTalentEffect501(player, staticProp, effect);
                // ?????????????????????????????????
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
                buildingService.addAtuoBuild(player);// ??????????????????
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
     * ????????????
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
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????,??????????????????, roleId:" + roleId + ",propId=" + propId);
        }
        int needGold = staticProp.getPrice() * num;
        if (needGold <= 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????,??????????????????, roleId:" + roleId + ",propId=" + propId);
        }
        // ??????????????????????????????
        rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "????????????");
        // ??????????????????
        rewardDataManager.subGold(player, needGold, AwardFrom.PROP_BUY, propId);

        rewardDataManager.addAward(player, AwardType.PROP, propId, num, AwardFrom.PROP_BUY, propId);
        BuyPropRs.Builder builder = BuyPropRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setProps(TwoInt.newBuilder().setV1(propId).setV2(player.props.get(propId).getCount()));
        return builder.build();
    }

    /**
     * ??????????????????
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
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????,??????????????????, roleId:" + roleId + ",propId=" + propId);
        }
        int now = TimeHelper.getCurrentSecond();
        Integer cnt = player.trophy.get(TrophyConstant.TROPHY_2);
        cnt = cnt != null ? cnt : 0;
        // ?????????
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                staticProp.getPrice(), AwardFrom.BUY_BUILD);
        // ????????????????????????
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

        // ?????????
        boolean hasEffect = processEffect(player, staticProp);
        if (hasEffect) {
            builder.setEffect(PbHelper.createEffectPb(player.getEffect().get(EffectConstant.BUILD_CNT)));
            // ??????????????????
        }
        builder.setGold(player.lord.getGold());
        builder.setBuyBuildCnt(cnt + 1);
        return builder.build();
    }

    /**
     * ??????buff
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
            // ??????
            Base.Builder msg = PbHelper.createSynBase(SyncBuffRs.EXT_FIELD_NUMBER, SyncBuffRs.ext, b.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }

    }

    /**
     * ????????????
     *
     * @param roleId
     * @param propId
     */
    public GamePb1.SyntheticPropRs syntheticProp(long roleId, int propId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticProp staticProp = StaticPropDataMgr.getPropMap(propId);
        if (staticProp == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????,??????????????????, roleId:", roleId, ", propId:", propId);
        }
        if (staticProp.getPropType() != 9) { // ????????????????????????propType=9
            throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), "????????????,??????????????????, roleId:", roleId, ", propId:",
                    propId);
        }
        rewardDataManager.checkPropIsEnough(player, propId, staticProp.getChip(), "????????????");
        rewardDataManager.subProp(player, propId, staticProp.getChip(), AwardFrom.SYNTHETIC_PROP);// , "????????????"

        GamePb1.SyntheticPropRs.Builder builder = GamePb1.SyntheticPropRs.newBuilder();
        List<List<Integer>> rewardList = staticProp.getRewardList();
        if (!CheckNull.isEmpty(rewardList)) {
            builder.addAllAward(rewardDataManager.addAwardDelaySync(player, rewardList, null, AwardFrom.SYNTHETIC_PROP_AWARD));
        }
        return builder.build();
    }
}
