package com.gryphpoem.game.zw.service.totem;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.totem.Totem;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.LoginService;
import com.gryphpoem.game.zw.service.PlayerService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 阵法图腾
 *
 * @author xwind
 * @date 2021/11/18
 */
@Service
public class TotemService implements GmCmdService, LoginService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;

    /**
     * 图腾合成
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb5.TotemSyntheticRs totemSynthetic(long roleId, GamePb5.TotemSyntheticRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionIsOpen(player);
        List<Integer> totemChipIds = req.getTotemChipIdList();
        if (ListUtils.isBlank(totemChipIds)) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "合成图腾参数错误,碎片id为空", req.getType(), ListUtils.toString(totemChipIds)));
        }
        GamePb5.TotemSyntheticRs.Builder resp = GamePb5.TotemSyntheticRs.newBuilder();
        if (req.getType() == 1) {//单个合成 合成多个
            int count = req.getCount();
            if (count <= 0) {
                throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "合成图腾参数错误,单个合成数量错误", req.getType(), count, ListUtils.toString(totemChipIds)));
            }
            Prop prop = player.props.get(totemChipIds.get(0));
            if (Objects.isNull(prop) || prop.getCount() <= 0) {
                throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "合成图腾错误 碎片不存在或数量为0", req.getType(), count, ListUtils.toString(totemChipIds)));
            }
            StaticProp staticProp = StaticPropDataMgr.getPropMap(prop.getPropId());
            if (Objects.isNull(staticProp)) {
                throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "合成图腾错误 碎片配置不存在", req.getType(), count, ListUtils.toString(totemChipIds)));
            }
            List<List<Integer>> consumeList = new ArrayList<>();
            int needChipNum = count * staticProp.getChip();
            if (staticProp.getQuality() == Constant.Quality.red) {//红色碎片合成不能使用万能碎片
                consumeList.add(ListUtils.createItem(AwardType.PROP, prop.getPropId(), needChipNum));
            } else {
                int diffChipNum = needChipNum - prop.getCount();
                if (diffChipNum > 0) {
                    Prop wnProp = player.props.get(Constant.TOTEM_UNIVERSAL_CHIP_ID);
                    if (Objects.isNull(wnProp) || wnProp.getCount() < diffChipNum) {
                        throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "合成图腾错误 碎片不足", req.getType(), count, ListUtils.toString(totemChipIds)));
                    }
                    consumeList.add(ListUtils.createItem(AwardType.PROP, wnProp.getPropId(), diffChipNum));
                    consumeList.add(ListUtils.createItem(AwardType.PROP, prop.getPropId(), needChipNum - diffChipNum));
                } else {
                    consumeList.add(ListUtils.createItem(AwardType.PROP, prop.getPropId(), needChipNum));
                }
            }
            rewardDataManager.checkAndSubPlayerRes(player, consumeList, 1, AwardFrom.TOTEM_SYNTHETIC_TOTEM);
            List<CommonPb.TotemInfo> getTotems = this.addTotemsAndSync(player, ListUtils.createItems(0, staticProp.getRewardList().get(0).get(1), staticProp.getRewardList().get(0).get(2) * count), AwardFrom.TOTEM_SYNTHETIC_TOTEM);
            resp.addAllGetTotems(getTotems);
        } else {//一键合成
            List<List<Integer>> consumeList = new ArrayList<>();
            List<List<Integer>> getList = new ArrayList<>();
            for (Integer totemChipId : totemChipIds) {
                if (totemChipId == Constant.TOTEM_UNIVERSAL_CHIP_ID) {
                    continue;
                }
                Prop totemChip = player.props.get(totemChipId);
                if (Objects.nonNull(totemChip)) {
                    StaticProp staticProp = StaticPropDataMgr.getPropMap(totemChipId);
                    if (Objects.nonNull(staticProp) && totemChip.getCount() >= staticProp.getChip()) {
                        int getCount = totemChip.getCount() / staticProp.getChip();
                        if (getCount > 0) {
                            consumeList.add(ListUtils.createItem(AwardType.PROP, totemChipId, getCount * staticProp.getChip()));
                            getList.add(ListUtils.createItem(AwardType.TOTEM, staticProp.getRewardList().get(0).get(1), getCount));
                        }
                    }
                }
            }
            if (ListUtils.isNotBlank(consumeList)) {
                rewardDataManager.subPlayerResHasChecked(player, consumeList, true, AwardFrom.TOTEM_SYNTHETIC_TOTEM);
            }
            if (ListUtils.isNotBlank(getList)) {
                List<CommonPb.TotemInfo> getTotems = this.addTotemsAndSync(player, getList, AwardFrom.TOTEM_SYNTHETIC_TOTEM);
                resp.addAllGetTotems(getTotems);
            }
        }
        return resp.build();
    }

    public List<CommonPb.TotemInfo> addTotemsAndSync(Player player, List<List<Integer>> totems, AwardFrom awardFrom) {
        List<CommonPb.TotemInfo> list = new ArrayList<>(totems.size());
        totems.forEach(o -> Stream.iterate(0, i -> i + 1).limit(o.get(2)).forEach(j -> {
            Totem totem = addTotem(player, o.get(1), awardFrom);
            if (Objects.nonNull(totem)) {
                list.add(totem.ser());
            }
        }));
        this.syncTotemInfoRs(player, list, 1);
        return list;
    }

//    private List<CommonPb.TotemInfo> syncAddTotems(Player player, Totem...totems){
//        List<CommonPb.TotemInfo> list = new ArrayList<>(totems.length);
//        Stream.iterate(0,i->i+1).limit(totems.length).forEach(j -> list.add(totems[j].ser()));
//        this.syncTotemInfoRs(player,list,1);
//        return list;
//    }

    public void subTotemsAndSync(Player player, List<Totem> totems, AwardFrom awardFrom) {
        List<CommonPb.TotemInfo> list = new ArrayList<>(totems.size());
        totems.forEach(o -> list.add(subTotem(player, o, awardFrom).ser()));
        this.syncTotemInfoRs(player, list, 2);
    }

    private Totem addTotem(Player player, int totemId, AwardFrom awardFrom) {
        StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totemId);
        if (Objects.nonNull(staticTotem)) {
            Totem totem = player.getTotemData().newTotem(player.maxKey(), totemId);
            LogLordHelper.totem(awardFrom, player, totemId, totem.getTotemKey(), staticTotem.getQuality(), totem.getStrengthen(), totem.getResonate(), 1);
            return totem;
        } else {
            LogUtil.common(String.format("roleId=%s添加图腾totemId=%s没有配置", player.roleId, totemId));
        }
        return null;
    }

    private Totem subTotem(Player player, Totem totem, AwardFrom awardFrom) {
        player.getTotemData().getTotemMap().remove(totem.getTotemKey());
        StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
        LogLordHelper.totem(awardFrom, player, totem.getTotemId(), totem.getTotemKey(), staticTotem.getQuality(), totem.getStrengthen(), totem.getResonate(), -1);
        return totem;
    }

    public void updTotemsAndSync(Player player, Totem... totems) {
        List<CommonPb.TotemInfo> list = new ArrayList<>(totems.length);
        for (Totem totem : totems) {
            list.add(totem.ser());
        }
        this.syncTotemInfoRs(player, list, 3);
    }

    private void syncTotemInfoRs(Player player, List<CommonPb.TotemInfo> list, int type) {
        if (ListUtils.isNotBlank(list)) {
            GamePb5.SyncTotemInfoRs.Builder builder = GamePb5.SyncTotemInfoRs.newBuilder();
            builder.addAllTotems(list);
            builder.setType(type);
            BasePb.Base msg = PbHelper.createSynBase(GamePb5.SyncTotemInfoRs.EXT_FIELD_NUMBER, GamePb5.SyncTotemInfoRs.ext, builder.build()).build();
            playerService.syncMsgToPlayer(msg, player);
        }
    }

    /**
     * 图腾分解
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb5.TotemDecomposeRs totemDecompose(long roleId, GamePb5.TotemDecomposeRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionIsOpen(player);
        List<Integer> totemKeys = req.getTotemKeyList();
        GamePb5.TotemDecomposeRs.Builder resp = GamePb5.TotemDecomposeRs.newBuilder();
        List<List<Integer>> getList = new ArrayList<>();
        List<Totem> subList = new ArrayList<>();
        for (Integer key : totemKeys) {
            Totem totem = player.getTotemData().getTotem(key);
            if (Objects.isNull(totem)) {
                LogUtil.common(String.format("roleId=%s,分解图腾不存在,key=%s", roleId, key));
                continue;
            }
            if (totem.getLock() == 2) {
                LogUtil.common(String.format("roleId=%s,分解图腾被锁,totem=%s", roleId, totem));
                continue;
            }
            if (totem.getHeroId() > 0) {
                LogUtil.common(String.format("roleId=%s,分解图腾被穿戴,totem=%s", roleId, totem));
                continue;
            }
            StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
            if (Objects.isNull(staticTotem)) {
                LogUtil.common(String.format("roleId=%s,分解图腾StaticTotem配置不存在,totem=%s", roleId, totem));
                continue;
            }
            StaticTotemUp staticTotemUp1 = StaticTotemDataMgr.getStaticTotemUp(1, staticTotem.getQuality(), totem.getStrengthen());
            StaticTotemUp staticTotemUp2 = StaticTotemDataMgr.getStaticTotemUp(2, staticTotem.getQuality(), totem.getResonate());
            if (Objects.isNull(staticTotemUp1) || Objects.isNull(staticTotemUp2)) {
                LogUtil.common(String.format("roleId=%s,分解图腾StaticTotemUp配置不存在,totem=%s", roleId, totem));
                continue;
            }
            if (ListUtils.isNotBlank(staticTotemUp1.getAnalysis())) {
                getList.addAll(staticTotemUp1.getAnalysis());
            }
            if (ListUtils.isNotBlank(staticTotemUp2.getAnalysis())) {
                getList.addAll(staticTotemUp2.getAnalysis());
            }
            subList.add(totem);
        }
        if (ListUtils.isNotBlank(subList)) {
            this.subTotemsAndSync(player, subList, AwardFrom.TOTEM_DECOMPOSE_TOTEM);
        }
        if (ListUtils.isNotBlank(getList)) {
            List<CommonPb.Award> getAwards = rewardDataManager.sendReward(player, getList, AwardFrom.TOTEM_DECOMPOSE_TOTEM);
//        resp.addAllAwards(PbHelper.mergeAwards(getAwards));
            resp.addAllAwards(getAwards);
        }
        return resp.build();
    }

    /**
     * 图腾强化
     *
     * @param roleId
     * @param totemKey
     * @param runeNum
     * @return
     * @throws MwException
     */
    public GamePb5.TotemStrengthenRs totemStrengthen(long roleId, int totemKey, int runeNum) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionIsOpen(player);
        Totem totem = player.getTotemData().getTotem(totemKey);
        if (Objects.isNull(totem)) {
            throw new MwException(GameError.TOTEM_NO_EXIST.getCode(), GameError.err(roleId, "图腾强化 图腾不存在", totemKey));
        }
        StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
        if (Objects.isNull(staticTotem)) {
            throw new MwException(GameError.TOTEM_NO_CONFIG.getCode(), GameError.err(roleId, "图腾强化 图腾配置不存在", totem));
        }
        StaticTotemUp staticTotemUp = StaticTotemDataMgr.getStaticTotemUp(1, staticTotem.getQuality(), totem.getStrengthen());
        if (Objects.isNull(staticTotemUp)) {
            throw new MwException(GameError.TOTEM_NO_CONFIG.getCode(), GameError.err(roleId, "图腾强化 当前等级配置不存在", totem));
        }
        StaticTotemUp staticTotemUpNext = StaticTotemDataMgr.getStaticTotemUp(1, staticTotem.getQuality(), totem.getStrengthen() + 1);
        if (Objects.isNull(staticTotemUpNext) || ListUtils.isBlank(staticTotemUp.getUpNeed())) {
            throw new MwException(GameError.TOTEM_STRENGTHEN_MAX.getCode(), GameError.err(roleId, "图腾强化 下级配置不存在已达到最大等级", totem));
        }
        if (runeNum > Constant.TOTEM_QH_MATERIAL_MAX) {
            throw new MwException(GameError.TOTEM_STRENGTHEN_RUNES_LIMIT.getCode(), GameError.err(roleId, "图腾强化 符文使用达到上限"));
        }
        StaticVip staticVip = StaticVipDataMgr.getVipMap(player.lord.getVip());
        int prob = staticTotemUp.getProb() + staticVip.getTotemUp();
        //做一个安全处理，防止多余消耗材料
        if (prob >= 10000) {//基础概率100%则不消耗符文石
            runeNum = 0;
        } else {//基础概率不足100%则计算达到100%实际需要的符文石数量
            int probDiff = Math.abs(prob - 10000);
            int c1 = probDiff / Constant.TOTEM_QH_MATERIAL_PROB;
            int c2 = probDiff % Constant.TOTEM_QH_MATERIAL_PROB;
            if (c2 > 0) {
                c1++;
            }
            if (c1 > Constant.TOTEM_QH_MATERIAL_MAX) {
                c1 = Constant.TOTEM_QH_MATERIAL_MAX;
            }
            if (runeNum > c1) {
                runeNum = c1;
            }
        }
        if (runeNum > 0) {
            rewardDataManager.checkPlayerResIsEnough(player, ListUtils.createItems(AwardType.PROP, 37001, runeNum), "图腾强化消耗符文石");
        }

        rewardDataManager.checkPlayerResIsEnough(player, staticTotemUp.getUpNeed(), "图腾强化消耗");

        GamePb5.TotemStrengthenRs.Builder resp = GamePb5.TotemStrengthenRs.newBuilder();
        prob += runeNum * Constant.TOTEM_QH_MATERIAL_PROB;
        if (RandomHelper.isHitRangeIn10000(prob)) {
            totem.setStrengthen(staticTotemUpNext.getLv());
            this.updTotemsAndSync(player, totem);
            if (totem.getHeroId() > 0) {
                Hero hero = player.heros.get(totem.getHeroId());
                CalculateUtil.processAttr(player, hero);
            }
            resp.setResult(1);
            LogLordHelper.totem(AwardFrom.TOTEM_STRENGTHEN, player, totem.getTotemId(), totem.getTotemKey(), staticTotem.getQuality(), totem.getStrengthen(), totem.getResonate(), 0, 37001, runeNum);
        } else {
            resp.setResult(0);
        }
        rewardDataManager.checkAndSubPlayerRes(player, staticTotemUp.getUpNeed(), AwardFrom.TOTEM_STRENGTHEN);
        if (runeNum > 0)
            rewardDataManager.checkAndSubPlayerRes(player, ListUtils.createItems(AwardType.PROP, 37001, runeNum), AwardFrom.TOTEM_STRENGTHEN, totemKey, totem.getStrengthen(), resp.getResult());
        return resp.build();
    }

    public CommonPb.TotemDataInfo buildTotemDataInfo(Player player) {
        CommonPb.TotemDataInfo.Builder builder = CommonPb.TotemDataInfo.newBuilder();
        player.getTotemData().getTotemMap().values().forEach(o -> builder.addTotems(o.ser()));
//        player.getTotemData().getTotemChipMap().values().forEach(o -> {
//            if(o.getCount() > 0) builder.addTotemChips(o.ser());
//        });
        return builder.build();
    }

    /**
     * 图腾共鸣
     *
     * @param roleId
     * @param totemKey
     * @return
     * @throws MwException
     */
    public GamePb5.TotemResonateRs totemResonate(long roleId, int totemKey) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionIsOpen(player);
        Totem totem = player.getTotemData().getTotem(totemKey);
        if (Objects.isNull(totem)) {
            throw new MwException(GameError.TOTEM_NO_EXIST.getCode(), GameError.err(roleId, "图腾共鸣 图腾不存在", totemKey));
        }
        StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
        if (Objects.isNull(staticTotem)) {
            throw new MwException(GameError.TOTEM_NO_CONFIG.getCode(), GameError.err(roleId, "图腾共鸣 图腾配置不存在", totem));
        }
        StaticTotemUp staticTotemUp = StaticTotemDataMgr.getStaticTotemUp(2, staticTotem.getQuality(), totem.getResonate());
        if (Objects.isNull(staticTotemUp)) {
            throw new MwException(GameError.TOTEM_NO_CONFIG.getCode(), GameError.err(roleId, "图腾共鸣 当前等级配置不存在", totem));
        }
        StaticTotemUp staticTotemUpNext = StaticTotemDataMgr.getStaticTotemUp(2, staticTotem.getQuality(), totem.getResonate() + 1);
        if (Objects.isNull(staticTotemUpNext) || ListUtils.isBlank(staticTotemUp.getUpNeed())) {
            throw new MwException(GameError.TOTEM_RESONATE_MAX.getCode(), GameError.err(roleId, "图腾共鸣 下级配置不存在已达到最大等级", totem));
        }

        GamePb5.TotemResonateRs.Builder resp = GamePb5.TotemResonateRs.newBuilder();

        rewardDataManager.checkAndSubPlayerRes(player, staticTotemUp.getUpNeed(), AwardFrom.TOTEM_RESONATE);

        if (RandomHelper.isHitRangeIn10000(staticTotemUp.getProb())) {
            totem.setResonate(staticTotemUpNext.getLv());
            this.updTotemsAndSync(player, totem);
            if (totem.getHeroId() > 0) {
                Hero hero = player.heros.get(totem.getHeroId());
                CalculateUtil.processAttr(player, hero);
            }
            resp.setResult(1);
            LogLordHelper.totem(AwardFrom.TOTEM_RESONATE, player, totem.getTotemId(), totem.getTotemKey(), staticTotem.getQuality(), totem.getStrengthen(), totem.getResonate(), 0);
        } else {
            resp.setResult(0);
        }

        return resp.build();
    }

    /**
     * 图腾突破
     *
     * @param roleId
     * @param totemKey
     * @return
     * @throws MwException
     */
    public GamePb5.TotemBreakRs totemBreak(long roleId, int totemKey) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionIsOpen(player);
        Totem totem = player.getTotemData().getTotem(totemKey);
        if (Objects.isNull(totem)) {
            throw new MwException(GameError.TOTEM_NO_EXIST.getCode(), GameError.err(roleId, "图腾突破 图腾不存在", totemKey));
        }
        StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
        if (Objects.isNull(staticTotem)) {
            throw new MwException(GameError.TOTEM_NO_CONFIG.getCode(), GameError.err(roleId, "图腾突破 图腾配置不存在", totem));
        }
        if (staticTotem.getBreakId() <= 0) {
            throw new MwException(GameError.TOTEM_NO_CONFIG.getCode(), GameError.err(roleId, "图腾突破 配置不可突破", totem));
        }
        StaticTotem staticTotemNext = StaticTotemDataMgr.getStaticTotem(staticTotem.getBreakId());
        if (Objects.isNull(staticTotemNext)) {
            throw new MwException(GameError.TOTEM_NO_CONFIG.getCode(), GameError.err(roleId, "图腾突破 突破后的图腾配置不存在", totem));
        }
        StaticTotemUp staticTotemUpNext = StaticTotemDataMgr.getStaticTotemUp(1, staticTotem.getQuality(), totem.getStrengthen() + 1);
        if (Objects.nonNull(staticTotemUpNext)) {
            throw new MwException(GameError.TOTEM_BREAK_NOT.getCode(), GameError.err(roleId, "图腾突破 强化等级未达到满级", totem));
        }
        if (staticTotem.getArmType() != staticTotemNext.getArmType()) {
            throw new MwException(GameError.TOTEM_CONFIG_ERROR.getCode(), GameError.err(roleId, "图腾突破 突破后的图腾兵种和当前兵种不匹配", totem));
        }
        if (staticTotem.getPlace() != staticTotemNext.getPlace()) {
            throw new MwException(GameError.TOTEM_CONFIG_ERROR.getCode(), GameError.err(roleId, "图腾突破 突破后的图腾位置和当前位置不匹配", totem));
        }
        rewardDataManager.checkAndSubPlayerRes(player, staticTotem.getBeakNeed(), AwardFrom.TOTEM_BREAK);
        Totem newTotem = this.addTotem(player, staticTotemNext.getId(), AwardFrom.TOTEM_BREAK);
        newTotem.setResonate(totem.getResonate());

        GamePb5.TotemBreakRs.Builder resp = GamePb5.TotemBreakRs.newBuilder();
        if (totem.getHeroId() > 0) {
            Hero hero = player.heros.get(totem.getHeroId());
            if (Objects.nonNull(hero)) {
                hero.setTotemKey(staticTotem.getPlace(), newTotem.getTotemKey());
                newTotem.setHeroId(hero.getHeroId());
                resp.setHero(PbHelper.createHeroPb(hero, player));
                CalculateUtil.processHeroAttr(player, hero);
            }
            totem.setHeroId(0);
        }
        this.subTotem(player, totem, AwardFrom.TOTEM_BREAK);
//        this.subTotemsAndSync(player,ListUtils.createList(totem),AwardFrom.TOTEM_BREAK);
//        this.syncAddTotems(player,newTotem);
        resp.setTotemKey(newTotem.getTotemKey());
        resp.setBeforeTotem(totem.ser());
        resp.setAfterTotem(newTotem.ser());
        return resp.build();
    }

    /**
     * 图腾加锁 解锁
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb5.TotemLockRs totemLock(long roleId, GamePb5.TotemLockRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionIsOpen(player);
        List<Integer> totemKeys = req.getTotemKeyList();
        int lock = req.getLock();
        if (lock < 1 || lock > 2 || totemKeys.isEmpty()) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾加锁解锁错误 参数错误"));
        }
        List<Totem> totemList = new ArrayList<>();
        totemKeys.forEach(key -> {
            Totem totem = player.getTotemData().getTotem(key);
            if (Objects.nonNull(totem)) {
                totem.setLock(lock);
                totemList.add(totem);
            }
        });
        Totem[] totems = new Totem[totemList.size()];
        Stream.iterate(0, i -> i + 1).limit(totemList.size()).forEach(j -> totems[j] = totemList.get(j));
        this.updTotemsAndSync(player, totems);
        GamePb5.TotemLockRs.Builder resp = GamePb5.TotemLockRs.newBuilder();
        return resp.build();
    }

    /**
     * 图腾装上 卸下
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb5.TotemPutonRs totemPuton(long roleId, GamePb5.TotemPutonRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkFunctionIsOpen(player);
        int heroId = req.getHeroId();
        int totemKey = req.getTotemKey();
        int type = req.getType();
        int place = req.getPlace();
        if (type < 0 || type > 1) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 参数错误type", type));
        }
        if (place < 1 || place > 8) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 参数错误place", place));
        }
        Hero hero = player.heros.get(heroId);
        if (Objects.isNull(hero)) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 参数错误heroId", heroId));
        }
        Totem totem = player.getTotemData().getTotem(totemKey);
        if (Objects.isNull(totem)) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 参数错误totemKey", totemKey));
        }
        int unLockLv = Constant.TOTEM_UNLOCK_PLACE_LV.get(place - 1);
        if (hero.getLevel() < unLockLv) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 此位置未解锁", hero.getLevel(), place));
        }
        StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
        StaticTotemUp staticTotemUp1 = StaticTotemDataMgr.getStaticTotemUp(1, staticTotem.getQuality(), totem.getStrengthen());
        StaticTotemUp staticTotemUp2 = StaticTotemDataMgr.getStaticTotemUp(2, staticTotem.getQuality(), totem.getResonate());
        if (Objects.isNull(staticTotem) || Objects.isNull(staticTotemUp1) || Objects.isNull(staticTotemUp2)) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 图腾配置未找到", totem, staticTotem, staticTotemUp1, staticTotemUp2));
        }
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (Objects.isNull(staticHero)) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 将领配置未找到", heroId));
        }
        if (staticHero.getType() != staticTotem.getArmType()) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 图腾将领兵种不符合", totem, heroId));
        }
        if (place != staticTotem.getPlace()) {
            throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 图腾位置不符合", totem, place));
        }

        GamePb5.TotemPutonRs.Builder resp = GamePb5.TotemPutonRs.newBuilder();

        if (type == 0) {
            int heroTotemKey = hero.getTotemKey(place);
            hero.setTotemKey(place, 0);
            Totem currTotem = player.getTotemData().getTotem(heroTotemKey);
            if (Objects.nonNull(currTotem)) {
                currTotem.setHeroId(0);
//                this.updTotemsAndSync(player,currTotem);
                resp.setTotemOff(currTotem.ser());
            }
        } else {
            if (totem.getHeroId() > 0) {
                throw new MwException(GameError.TOTEM_PARAM_ERROR.getCode(), GameError.err(roleId, "图腾穿戴 图腾已被装上", totem));
            }
            // 非空闲状态
            if (!hero.isIdle() && hero.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "图腾穿戴 将领不在空闲中, roleId:", roleId, ", heroId:",
                        heroId, ", state:", hero.getState());
            }
            int heroTotemKey = hero.getTotemKey(place);
            if (heroTotemKey > 0) {
                hero.setTotemKey(place, 0);
                Totem currTotem = player.getTotemData().getTotem(heroTotemKey);
                if (Objects.nonNull(currTotem)) {
                    currTotem.setHeroId(0);
//                    this.updTotemsAndSync(player,currTotem);
                    resp.setTotemOff(currTotem.ser());
                }
            }
            hero.setTotemKey(place, totemKey);
            totem.setHeroId(heroId);
//            this.updTotemsAndSync(player,totem);
            resp.setTotemOn(totem.ser());
        }

        CalculateUtil.processAttr(player, hero);

        resp.setHero(PbHelper.createHeroPb(hero, player));
        resp.setPlace(place);
        return resp.build();
    }

    public List<CommonPb.Award> dropTotem(Player player, int type, AwardFrom awardFrom) {
        if (Objects.isNull(player) || type < 1 || type > 2 || Objects.isNull(awardFrom)) {
            return Collections.EMPTY_LIST;
        }
        List<StaticTotemDrop> staticTotemDropList = StaticTotemDataMgr.getStaticTotemDropList().stream()
                .filter(o -> o.getType() == type && o.getRange().get(0) <= player.lord.getLevel() && o.getRange().get(1) >= player.lord.getLevel())
                .collect(Collectors.toList());
        List<List<Integer>> dropList = new ArrayList<>();
        staticTotemDropList.forEach(o -> {
            if (RandomHelper.isHitRangeIn10000(o.getProb())) {
                List<Integer> hitList = RandomUtil.randomAwardByWeight(o.getGroup());
                dropList.add(ListUtils.createItem(hitList.get(0), hitList.get(1), hitList.get(2)));
            }
        });
        if (dropList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return rewardDataManager.sendReward(player, dropList, awardFrom);
    }

    private void checkFunctionIsOpen(Player player) throws MwException {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_TOTEM))
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), GameError.err(player.roleId, "检查图腾功能未解锁"));
    }

    @GmCmd("totem")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        //totem addtotem totemId 增加图腾
        //totem addchip chipId num 增加碎片
        //totem cleartotem 清理所有图腾
        //totem clearchip 清理所有碎片
        //totem qianghuaMAX 所有图腾强化等级到上限
        //totem qianghua key lv 指定图腾强化到指定等级
        if (params[0].equalsIgnoreCase("addtotem")) {
            int totemId = Integer.parseInt(params[1]);
            this.addTotemsAndSync(player, ListUtils.createItems(0, totemId, 1), AwardFrom.DO_SOME);
        }
//        if(params[0].equalsIgnoreCase("addchip")){
//            int chipId = Integer.parseInt(params[1]);
//            int num = Integer.parseInt(params[2]);
//            rewardDataManager.sendReward(player,ListUtils.createItems(AwardType.TOTEM_CHIP,chipId,num),AwardFrom.DO_SOME);
//        }
        if (params[0].equalsIgnoreCase("cleartotem")) {
            this.subTotemsAndSync(player, new ArrayList<>(player.getTotemData().getTotemMap().values()), AwardFrom.DO_SOME);
            player.heros.values().forEach(hero -> Stream.iterate(1, i -> i + 1).limit(hero.getTotem().length - 1).forEach(j -> hero.getTotem()[j] = 0));
        }
        if (params[0].equalsIgnoreCase("clearchip")) {
            player.props.values().stream().filter(o -> {
                StaticProp staticProp = StaticPropDataMgr.getPropMap(o.getPropId());
                if (Objects.nonNull(staticProp) && staticProp.getPropType() == AwardType.TOTEM_CHIP) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList()).forEach(o1 -> {
                try {
                    rewardDataManager.subPlayerResHasChecked(player, ListUtils.createItems(AwardType.PROP, o1.getPropId(), o1.getCount()), true, AwardFrom.DO_SOME);
                } catch (MwException e) {
                    e.printStackTrace();
                }
            });
        }
        if (params[0].equalsIgnoreCase("qianghuaMAX")) {
            player.getTotemData().getTotemMap().values().forEach(totem -> {
                StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
                List<StaticTotemUp> staticTotemUpList = StaticTotemDataMgr.getStaticTotemUpList(1, staticTotem.getQuality());
                totem.setStrengthen(staticTotemUpList.get(staticTotemUpList.size() - 1).getLv());
                this.updTotemsAndSync(player, totem);
            });
        }
        if (params[0].equalsIgnoreCase("qianghua")) {
            int totemKey = Integer.parseInt(params[1]);
            int qhlv = Integer.parseInt(params[2]);
            Totem totem = player.getTotemData().getTotem(totemKey);
            if (Objects.nonNull(totem)) {
                StaticTotem staticTotem = StaticTotemDataMgr.getStaticTotem(totem.getTotemId());
                List<StaticTotemUp> staticTotemUpList = StaticTotemDataMgr.getStaticTotemUpList(1, staticTotem.getQuality());
                int maxLv = staticTotemUpList.get(staticTotemUpList.size() - 1).getLv();
                qhlv = qhlv > maxLv ? maxLv : qhlv;
                totem.setStrengthen(qhlv);
                this.updTotemsAndSync(player, totem);
            }
        }
    }

    @Override
    public void afterLogin(Player player) {
        //检查修正图腾-英雄的绑定数据
        player.getTotemData().getTotemMap().values().forEach(totem -> {
            if (totem.getHeroId() > 0) {
                Hero hero = player.heros.get(totem.getHeroId());
                if (Objects.isNull(hero) || !ArrayUtils.contains(hero.getTotem(), totem.getTotemKey())) {
                    totem.setHeroId(0);
                }
            }
        });
    }
}
