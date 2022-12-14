package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTreasureWareDataMgr;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureWare;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureWareLevel;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureWareSpecial;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.attr.TreasureWareAttrItem;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.resource.util.game.TreasureWareUtil;
import com.gryphpoem.game.zw.resource.util.pb.TreasureWarePbUtil;
import com.gryphpoem.game.zw.service.activity.AbsRankActivityService;
import com.gryphpoem.game.zw.service.activity.AbsTurnPlatActivityService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.gryphpoem.game.zw.resource.util.NumberUtil.TEN_THOUSAND_DOUBLE;

/**
 * ????????????service
 */
@Service
public class TreasureWareService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private ActivityTriggerService activityTriggerService;

    /**
     * ??????????????????
     *
     * @param player
     * @param req
     * @return
     */
    public GamePb4.TreasureWareSaveTrainRs saveTrainAttr(Player player, GamePb4.TreasureWareSaveTrainRq req) {
        int keyId = req.getKeyId();
        TreasureWare tw = player.treasureWares.get(keyId);
        if (Objects.isNull(tw)) {
            throw new MwException(GameError.PARAM_ERROR,
                    String.format("???????????????, roleId :%d, keyId :%d not found !!!", player.getLordId(), keyId));
        }
        TreasureWareAttrItem trainAttr = tw.getTrainAttr();
        if (Objects.isNull(trainAttr)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("???????????????????????????!!! roleId :%d, keyId :%d", player.getLordId(), keyId));
        }
        StaticTreasureWare stw = StaticTreasureWareDataMgr.getStaticTreasureWare(tw.getEquipId());
        if (Objects.isNull(stw)) {
            throw new MwException(GameError.NO_CONFIG, String.format("??????????????????!!! roleId :%d, keyId :%d, cfgId :%d", player.getLordId(), keyId, tw.getEquipId()));
        }
        //????????????????????????????????????????????????????????????????????????
        if (req.getSaveType() == 1) {
            StaticTreasureWareLevel sLevel = StaticTreasureWareDataMgr.getStaticTreasureWareLevel(stw.getQuality(), tw.getLevel());
            if (Objects.isNull(sLevel)) {
                throw new MwException(GameError.NO_CONFIG, String.format("????????????????????????!!! roleId :%d, keyId :%d, cfgId :%d, tw.lv :%d",
                        player.getLordId(), keyId, tw.getEquipId(), tw.getLevel()));
            }

            int lvAttrValue = sLevel.getAttr().getOrDefault(trainAttr.getAttrId(), 0);
            if (lvAttrValue > 0) {
                int oldAttrValue = trainAttr.getValue();
                trainAttr.setValue(trainAttr.getInitValue() + (int) (lvAttrValue * (trainAttr.getPercent() / TEN_THOUSAND_DOUBLE)));
                LogUtil.debug(String.format("roleId :%d, keyId :%d, ?????????????????? :%d, ?????????????????????????????? :%d", player.getLordId(), keyId, oldAttrValue, trainAttr.getValue()));
            }
            trainAttr.setIndex(trainAttr.getTrainTargetIndex());
            tw.getAttrs().put(trainAttr.getIndex(), trainAttr);

            // ???????????????????????????
            LogLordHelper.saveTreasureWareTrain(AwardFrom.TREASURE_WARE_TRAIN_SAVE, player, tw);
        }
        tw.setTrainAttr(null);
        int stage = calcTreasureWareStage(player, tw, stw);
        tw.setRank(stage);
        if (tw.isOnEquip()) {
            Hero hero = player.heros.get(tw.getHeroId());
            if (Objects.nonNull(hero)) {
                CalculateUtil.processAttr(player, hero);
            }
        }
        GamePb4.TreasureWareSaveTrainRs.Builder builder = GamePb4.TreasureWareSaveTrainRs.newBuilder();
        builder.setTreasureWare(tw.createPb(false));
        //????????????
        TaskService.processTask(player, ETask.TRAIN_QUALITY_AND_2SAME_ANY_ATTR_TREASURE_WARE, keyId);

        // ????????????????????????
        EventDataUp.treasureCultivate(player, tw, AwardFrom.TREASURE_WARE_TRAIN, getAttrType(tw));

        return builder.build();
    }

    /**
     * ????????????
     *
     * @param player ??????
     * @param req    req
     * @return ????????????
     */
    public GamePb4.TreasureWareTrainRs trainTreasureWare(Player player, GamePb4.TreasureWareTrainRq req) {
        int majorKeyId = req.getMajorKeyId();
        int matKeyId = req.getMatKeyId();
        long roleId = player.getLordId();
        if (majorKeyId <= 0 || matKeyId <= 0 || majorKeyId == matKeyId) {
            throw new MwException(GameError.PARAM_ERROR,
                    String.format("????????????????????????, roleId :%d, majorKeyId :%d, matKeyId :%d ", roleId, majorKeyId, matKeyId));
        }
        TreasureWare major = player.treasureWares.get(majorKeyId);
        TreasureWare material = player.treasureWares.get(matKeyId);
        //???????????????
        if (Objects.isNull(major) || Objects.isNull(material)) {
            throw new MwException(GameError.PARAM_ERROR,
                    String.format("???????????????, roleId :%d, majorKeyId :%d, matKeyId :%d not found !!!", roleId, majorKeyId, matKeyId));
        }

        //???????????????????????????
        if (Objects.nonNull(major.getTrainAttr())) {
            throw new MwException(GameError.PARAM_ERROR, String.format("?????????????????????????????????. roleId :%d, majorKeyId :%d", roleId, majorKeyId));
        }

        //??????????????????????????????
        if (material.getEquipLocked() == TreasureWareConst.TREASURE_WARE_LOCKED) {
            throw new MwException(GameError.TREASURE_WARE_LOCKED.getCode(), "???????????????  roleId:", roleId, ", treasureWareId: ",
                    material.getEquipId());
        }
        //????????????????????????????????????????????? ??????
        if (material.isOnEquip()) {
            throw new MwException(GameError.TREASURE_WARE_HAS_ON_HERO.getCode(), "????????????????????????????????????, roleId: ", roleId,
                    ", ????????????????????????id:", material.getHeroId());
        }

        //?????????????????????????????????????????????
        if (Objects.nonNull(material.getTrainAttr())) {
            throw new MwException(GameError.TREASURE_WARE_MATERIAL_NOT_FINISH_TRAIN,
                    String.format("???????????????????????????????????????, roleId :%d, material keyId :%d", roleId, matKeyId));
        }

        //????????????
        StaticTreasureWare majorConfig = StaticTreasureWareDataMgr.getStaticTreasureWare(major.getEquipId());
        StaticTreasureWare materialConfig = StaticTreasureWareDataMgr.getStaticTreasureWare(material.getEquipId());
        if (Objects.isNull(majorConfig) || Objects.isNull(materialConfig)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("roleId :%d, major treasure ware id :%d, material treasure ware id :%d config not found !!!",
                    roleId, major.getEquipId(), material.getEquipId()));
        }

        //?????????????????????
        if (majorConfig.getQuality() != materialConfig.getQuality()) {
            throw new MwException(GameError.PARAM_ERROR, String.format("??????????????????????????????, roleId :%d major cfg :%d, material cfg :%d",
                    roleId, majorConfig.getId(), materialConfig.getId()));
        }


        //????????????
        rewardDataManager.subTreasureWare(player, matKeyId, AwardFrom.TREASURE_WARE_TRAIN, JSON.toJSONString(material));

        //????????????
        TreasureWareAttrItem trainAttr = TreasureWareUtil.train(player, major, material);
        major.setTrainAttr(trainAttr);
        //??????????????????
        LogLordHelper.trainTreasureWare(AwardFrom.TREASURE_WARE_TRAIN, player.account, player.lord, material, major, trainAttr);
        LogUtil.debug("roleId: ", roleId, ", ????????????????????????: ", JSON.toJSONString(material), " ?????????????????????: ", JSON.toJSONString(major));
        GamePb4.TreasureWareTrainRs.Builder builder = GamePb4.TreasureWareTrainRs.newBuilder();
        builder.setTrainAttr(TreasureWarePbUtil.createTreasureWareAttrItemPb(trainAttr));
        builder.setTargetIndex(trainAttr.getTrainTargetIndex());
        TaskService.processTask(player, ETask.TRAIN_QUALITY_AND_COUNT_TREASURE_WARE, 1, majorConfig.getQuality());
        taskDataManager.updTask(player, TaskType.COND_529, 1);

        return builder.build();
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param quality
     * @return
     */
    public GamePb4.MakeTreasureWareRs makeTreasureWare(long roleId, int quality, int count, String condition) throws MwException {
        if ((count != 1 && count != 10) || quality <= 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), " ????????????, quality: ", quality, ", count: ", count);
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkOpenTreasureWare(player);
        int firstMakeCount = player.getMakeTreasureWare().getMakeCount(quality);
//        int makeCondition = getCondition(quality, player.getMakeTreasureWare().getMakeCount(quality), condition);

        List<StaticTreasureWare> qualityList = StaticTreasureWareDataMgr.getQualityTreasureWare(quality);
        if (ObjectUtils.isEmpty(qualityList)) {
            throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), " ??????????????????????????????, quality: ", quality);
        }
        if (quality >= TreasureWareConst.ANCIENT_QUALITY || quality <= TreasureWareConst.BLUE_QUALITY) {
            throw new MwException(GameError.TREASURE_WARE_CANNOT_MAKE.getCode(), " ????????????????????????, quality: ", quality);
        }
        //??????????????????????????????
        rewardDataManager.checkPlayerResIsEnough(player, qualityList.get(0).getConsume(), count, "????????????");

        //?????????????????????????????????????????????
        int season = seasonService.getLastSeason();
        if (quality < TreasureWareConst.RED_QUALITY) {
            season = 0;
        }
        List<StaticTreasureWare> randomList = StaticTreasureWareDataMgr.filterTreasureWare(quality, season, player.getTreasureCombat().getCurCombatId());
        if (ObjectUtils.isEmpty(randomList)) {
            throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "???????????????????????????, quality: ",
                    quality, ", maxPlayerCheckpoint: ", player.getTreasureCombat().getCurCombatId(), ", season: ", season);
        }
        int remainBagCnt = remainBagCnt(player);
        if (count > remainBagCnt) {
            throw new MwException(GameError.MAX_TREASURE_WARE_STORE.getCode(), "????????????, ????????????, quality: ",
                    quality, ", count: ", count, ", remainBagCnt: ", remainBagCnt);
        }

        int putInBag = 0;
        int makeCount = 0;
        int specialMakeCount = 0;
        int now = TimeHelper.getCurrentSecond();
        List<TreasureWare> treasureWareList = null;
        try {
            StaticTreasureWare randomTreasureWare = randomList.get(0);
            if (CheckNull.isNull(randomTreasureWare)) {
                throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "?????????????????????, quality: ",
                        quality, ", maxPlayerCheckpoint: ", player.getTreasureCombat().getCurCombatId(), ", season: ", season);
            }
//            if (firstMakeCount <= 0 && CheckNull.isNull(randomTreasureWare.checkSpecialId(makeCondition))) {
//                throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), " ????????????, firstMake: ", firstMakeCount, ", condition: ", makeCondition);
//            }

            for (int i = 0; i < count; i++) {
                //????????????
                TreasureWare treasureWare = basicAttr(randomTreasureWare, player.getTreasureCombat().getCurCombatId(), season, player, now);
                //????????????
                exclusiveAttr(player, treasureWare, randomTreasureWare, player.getTreasureCombat().getCurCombatId(), season, 0);

                putInBag = putInTreasureWare(remainBagCnt, putInBag, treasureWare, player, randomTreasureWare, null, AwardFrom.TREASURE_WARE_MAKE);

                treasureWareList = treasureWareList == null ? new ArrayList<>() : treasureWareList;
                treasureWareList.add(treasureWare);
                // treasureWare.setProfileId(StaticTreasureWareDataMgr.getProfileId(getAttrType(treasureWare), treasureWare.getQuality(), treasureWare.getSpecialId()));

                makeCount++;
//                makeCondition = 0;
                if (Objects.nonNull(treasureWare.getSpecialId()))
                    specialMakeCount++;
            }
        } finally {
            if (makeCount > 0) {
                taskDataManager.updTask(player, TaskType.COND_TREASURE_WARE_MAKE_COUNT, makeCount);
                //??????????????????????????????
                StaticTreasureWare staticTreasureWare = StaticTreasureWareDataMgr.getStaticTreasureWare(treasureWareList.get(0).getEquipId());
                rewardDataManager.subPlayerResHasChecked(player, staticTreasureWare.getConsume(), makeCount,
                        AwardFrom.MAKE_TREASURE_WARE_ON_CONSUME);
                activityTriggerService.makeTreasureWare(player, quality);
                player.getMakeTreasureWare().updateMakeCount(quality);
                TaskService.processTask(player, ETask.MAKE_QUALITY_AND_COUNT_TREASURE_WARE, quality, makeCount);
                TaskService.processTask(player, ETask.MAKE_QUALITY_AND_COUNT_AND_SPECIAL_TREASURE_WARE, quality, specialMakeCount);
                AbsRankActivityService.updateRankList(player, makeCount, quality);
                AbsTurnPlatActivityService.updateDrawCount(player, makeCount, quality);
            }
        }

        GamePb4.MakeTreasureWareRs.Builder builder = GamePb4.MakeTreasureWareRs.newBuilder().
                setFirstMakeTw(player.getMakeTreasureWare().getMakeCount(quality));
        Optional.ofNullable(treasureWareList).ifPresent(list -> list.forEach(tw -> builder.addTreasureWare(tw.createPb(false))));
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param randomTreasureWare
     * @param maxPlayerCheckpoint
     * @param season
     * @param player
     * @throws MwException
     */
    private TreasureWare basicAttr(StaticTreasureWare randomTreasureWare, int maxPlayerCheckpoint, int season, Player player, int now) throws MwException {
        TreasureWare treasureWare = createTreasureWare(randomTreasureWare);

        List<List<Integer>> attrIds = randomTreasureWare.getRandomAttrIds();
        if (ObjectUtils.isEmpty(attrIds)) {
            throw new MwException(GameError.RANDOM_ATTR_ID_ERROR.getCode(), "??????????????????ID??????, quality: ",
                    randomTreasureWare.getQuality(), ", maxPlayerCheckpoint: ", maxPlayerCheckpoint, ", season: ", season,
                    ", treasureWareId:", randomTreasureWare.getId());
        }

        //????????????, ???????????????????????????????????????
        int attrIndex = 1;
        if (randomTreasureWare.getType() == TreasureWareConst.ANCIENT_TREASURE_WARE) {
            for (List<Integer> attr : attrIds) {
                TreasureWareAttrItem attrItem = new TreasureWareAttrItem(attr.get(0), attr.get(1));
                int attrStage = getMaxAttrStage(randomTreasureWare);//????????????
                attrItem.setStage(attrStage);
                attrItem.setPercent((int) Constant.TEN_THROUSAND);
                attrItem.setIndex(attrIndex++);
                treasureWare.getAttrs().put(attrItem.getIndex(), attrItem);
            }
        } else {
            for (List<Integer> attr : attrIds) {
                int[] proportion = randomTreasureWare.getRandomRank();
                int percent = proportion[2] + RandomHelper.randomInSize(proportion[1] - proportion[2] + 1);
                int initValue = (int) Math.ceil(attr.get(1) * (percent / TEN_THOUSAND_DOUBLE));
                TreasureWareAttrItem attrItem = new TreasureWareAttrItem(attr.get(0), initValue);
                attrItem.setStage(proportion[0]);
                attrItem.setPercent(percent);
                attrItem.setIndex(attrIndex++);
                treasureWare.getAttrs().put(attrItem.getIndex(), attrItem);
            }
        }
        int treasureWareStage = calcTreasureWareStage(player, treasureWare, randomTreasureWare);
        treasureWare.setRank(treasureWareStage);
        treasureWare.setGetTime(now);
        return treasureWare;
    }

    private int getMaxAttrStage(StaticTreasureWare randomTreasureWare) {
        List<List<Integer>> numProb = randomTreasureWare.getNumProb();
        if (CheckNull.nonEmpty(numProb)) {
            return numProb.stream().mapToInt(iter -> iter.get(0)).max().orElse(1);
        } else {
            throw new MwException(GameError.CONFIG_FORMAT_ERROR.getCode(),
                    String.format("treasure ware id :%d, numProb config format error", randomTreasureWare.getId()));
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @return ????????????
     */
    private int calcTreasureWareStage(Player player, TreasureWare tw, StaticTreasureWare stw) {
        if (CheckNull.isEmpty(tw.getAttrs())) {
            LogUtil.error(String.format("roleId :%d, treasure ware id :%d , stw.id :%d, attrs not initialize",
                    player.getLordId(), tw.getKeyId(), stw.getId()));
            return 1;
        } else {
            List<List<Integer>> stageRangList;
            if (stw.getQuality() >= Constant.Quality.orange) {
                stageRangList = Constant.TREASURE_WARE_HIGHER_STAGE;
            } else {
                stageRangList = Constant.TREASURE_WARE_LOWER_STAGE;
            }
            int totalAttrStage = tw.getAttrs().values().stream()
                    .mapToInt(TreasureWareAttrItem::getStage).sum();
            int index = ListUtils.getInListIndex(totalAttrStage, stageRangList);
            if (index > 0) {
                return index + 1;
            } else {
                LogUtil.error(String.format("roleId :%d, tw.id :%d, stw.keyId :%d, totalAttrStage :%d, index calc error :%s",
                        player.getLordId(), tw.getKeyId(), stw.getId(), totalAttrStage, JSON.toJSONString(stageRangList)));
            }
        }
        return 1;
    }


    /**
     * ??????????????????
     *
     * @param treasureWare
     * @param randomTreasureWare
     * @param maxPlayerCheckpoint
     * @param season
     * @return
     * @throws MwException
     */
    private void exclusiveAttr(Player player, TreasureWare treasureWare, StaticTreasureWare randomTreasureWare, int maxPlayerCheckpoint, int season, Integer makeCondition) throws MwException {
        Integer specialId = makeCondition > 0 ? makeCondition : randomTreasureWare.getSpecialId(addProbability(randomTreasureWare, player));
        if (Objects.nonNull(specialId)) {
            StaticTreasureWareSpecial staticTreasureWareSpecial = StaticTreasureWareDataMgr.getTreasureWareSpecial(specialId);
            if (CheckNull.isNull(staticTreasureWareSpecial)) {
                throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "????????????????????????ID??????, quality: ",
                        randomTreasureWare.getQuality(), ", maxPlayerCheckpoint: ", maxPlayerCheckpoint, ", season: ", season,
                        ", treasureWareId:", randomTreasureWare.getId(), ", specialId:", specialId);
            }

            treasureWare.setSpecialId(specialId);
            for (List<Integer> specialAttr : staticTreasureWareSpecial.getAttrSpecial()) {
                treasureWare.getSpecialAttr().add(new Turple<>(specialAttr.get(0), specialAttr.get(1)));
            }

            //???????????????????????????id????????????
            player.treasureWareIdMakeCount.remove(randomTreasureWare.getId());
        } else {
            if (randomTreasureWare.getSpecialAttr() > 0) {
                int count = player.treasureWareIdMakeCount.getOrDefault(randomTreasureWare.getId(), 0) + 1;
                player.treasureWareIdMakeCount.put(randomTreasureWare.getId(), count);
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param randomTreasureWare
     * @param player
     * @return
     */
    private int addProbability(StaticTreasureWare randomTreasureWare, Player player) {
        int count = player.treasureWareIdMakeCount.getOrDefault(randomTreasureWare.getId(), 0);
        if (ObjectUtils.isEmpty(randomTreasureWare.getMini()) || count <= randomTreasureWare.getMini().get(0))
            return 0;

        return randomTreasureWare.getMini().get(1) * (count - randomTreasureWare.getMini().get(0));
    }

    /**
     * ??????(?????????)??????(??????????????????)
     *
     * @param player
     * @param treasureWareId
     * @param count
     * @param now
     * @return
     */
    public List<TreasureWare> getTreasureWare(Player player, int treasureWareId, int count, int now, AwardFrom from, Object... param) {
        StaticTreasureWare staticTreasureWare = StaticTreasureWareDataMgr.getStaticTreasureWare(treasureWareId);
        if (CheckNull.isNull(staticTreasureWare)) {
            LogUtil.error("?????????????????????, treasureWareId: ", treasureWareId, ", lordId: ", player.lord.getLordId());
            return null;
        }

        int putInBag = 0;
        TreasureWare treasureWare;
        List<TreasureWare> treasureWares = null;
        int season = seasonService.getLastSeason();
        int remainBagCnt = remainBagCnt(player);
        List<CommonPb.Award> treasureWareMailList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                //????????????
                treasureWare = basicAttr(staticTreasureWare, player.getTreasureCombat().getCurCombatId(), season, player, now);
                //????????????
                exclusiveAttr(player, treasureWare, staticTreasureWare, player.getTreasureCombat().getCurCombatId(), season, 0);
            } catch (MwException e) {
                LogUtil.error(e);
                return treasureWares;
            }

            putInBag = putInTreasureWare(remainBagCnt, putInBag, treasureWare, player, staticTreasureWare, treasureWareMailList, AwardFrom.USE_PROP);
            treasureWares = treasureWares == null ? new ArrayList<>() : treasureWares;
            treasureWares.add(treasureWare);
            // treasureWare.setProfileId(StaticTreasureWareDataMgr.getProfileId(getAttrType(treasureWare), treasureWare.getQuality(), treasureWare.getSpecialId()));
        }

        if (treasureWareMailList.size() > 0) {
            mailDataManager.sendAttachMail(player, treasureWareMailList, MailConstant.MOLD_OUT_OF_RANGE_AWARD,
                    AwardFrom.MAKE_TREASURE_WARE_IN_MAIL, now, treasureWareMailList.get(0).getKeyId(), treasureWareMailList.size());
        }

        return treasureWares;
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param treasureWare
     * @return
     */
    public int getAttrType(TreasureWare treasureWare) {
        if (ObjectUtils.isEmpty(treasureWare.getAttrs()))
            return -1;

        int attackType = 0;
        int defenceType = 0;

        for (TreasureWareAttrItem turPle : treasureWare.getAttrs().values()) {
            if (Constant.TREASURE_WARE_DEFENCE_ATTR_TYPE.contains(turPle.getAttrId())) {
                defenceType++;
            }
            if (Constant.TREASURE_WARE_ATTACK_ATTR_TYPE.contains(turPle.getAttrId())) {
                attackType++;
            }
        }

        if (attackType >= TreasureWareConst.OVER_ATTR_NUM) {
            return TreasureWareConst.ATTACK_TYPE;
        }
        if (defenceType >= TreasureWareConst.OVER_ATTR_NUM) {
            return TreasureWareConst.DEFENCE_TYPE;
        }

        return TreasureWareConst.ANY_TYPE;
    }

    /**
     * ????????????
     *
     * @param remainBagCnt
     * @param putInBag
     * @param treasureWare
     * @param player
     * @param staticTreasureWare
     * @param from
     * @return
     */
    private int putInTreasureWare(int remainBagCnt,
                                  int putInBag,
                                  TreasureWare treasureWare,
                                  Player player,
                                  StaticTreasureWare staticTreasureWare,
                                  List<CommonPb.Award> treasureWareMailList,
                                  AwardFrom from) {
        treasureWare.setKeyId(player.maxKey());
        int quality = treasureWare.getQuality();
        int attrType = getAttrType(treasureWare);
        int profileId = StaticTreasureWareDataMgr.getProfileId(attrType, quality, treasureWare.getSpecialId());

        if (remainBagCnt > putInBag) {
            treasureWare.setStatus(TreasureWareConst.TREASURE_IN_USING);
            putInBag++;

            // ???????????????????????????
            LogLordHelper.treasureWare(
                    from,
                    player.account,
                    player.lord,
                    treasureWare.getEquipId(),
                    treasureWare.getKeyId(),
                    Constant.ACTION_ADD,
                    profileId,
                    quality,
                    treasureWare.logAttrs(),
                    CheckNull.isNull(treasureWare.getSpecialId()) ? -1 : treasureWare.getSpecialId()
            );
        } else {
            //?????????
            treasureWare.setStatus(TreasureWareConst.TREASURE_IN_MAIL);
            treasureWareMailList = CheckNull.isNull(treasureWareMailList) ? new ArrayList<>() : treasureWareMailList;
            treasureWareMailList.add(PbHelper.createAwardPbWithParam(
                    AwardType.TREASURE_WARE,
                    staticTreasureWare.getId(),
                    1,
                    treasureWare.getKeyId(),
                    profileId
            ));
        }

        treasureWare.setProfileId(profileId);
        player.treasureWares.put(treasureWare.getKeyId(), treasureWare);
        taskDataManager.updTask(player, TaskType.COND_535, 1, treasureWare.getQuality());
        Optional.ofNullable(treasureWare.getSpecialAttr()).ifPresent(e -> {
            if (CheckNull.nonEmpty(e))
                taskDataManager.updTask(player, TaskType.COND_536, 1, treasureWare.getQuality());
        });

        // ????????????????????????
        EventDataUp.treasureCultivate(player, treasureWare, from, attrType);

        return putInBag;
    }

    /**
     * ??????????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GetTreasureWaresRs getTreasureWares(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb4.GetTreasureWaresRs.Builder builder = GamePb4.GetTreasureWaresRs.newBuilder();
        if (StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_TREASURE_WARE) &&
                !ObjectUtils.isEmpty(player.treasureWares.values())) {
            player.treasureWares.values().forEach(treasureWare -> {
                if (treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_USING)
                    return;
                builder.addTreasureWare(treasureWare.createPb(false));
            });
        }

        builder.setTreasureWareCnt(player.common.getTreasureWareCnt());
        builder.setBuyTreasureWareBagCnt(player.common.getBuyTreasureWareBagCnt());
        return builder.build();
    }

    /**
     * ????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.TreasureWareBagExpandRs bagExpand(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkOpenTreasureWare(player);
        GamePb4.TreasureWareBagExpandRs.Builder builder = GamePb4.TreasureWareBagExpandRs.newBuilder();

        // ?????????????????????????????????
        checkAndExpandBag(player);

        builder.setCount(player.common.getTreasureWareCnt());
        builder.setBuyTreasureWareBagCnt(player.common.getBuyTreasureWareBagCnt());
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param player
     * @throws MwException
     */
    public void checkAndExpandBag(Player player) throws MwException {
        if (player.common.getBuyTreasureWareBagCnt() >= Constant.BUY_TREASURE_WARE_BAG_RULE.get(0)) {
            throw new MwException(GameError.OVER_BUY_TREASURE_WARE_BAG_CNT.getCode(), "??????????????????????????????, roleId:", player.roleId, ", ????????????:",
                    player.common.getBuyTreasureWareBagCnt());
        }

        int needGold = (player.common.getBuyTreasureWareBagCnt() + 1) * Constant.BUY_TREASURE_WARE_BAG_RULE.get(1);
        int add = Constant.BUY_TREASURE_WARE_BAG_RULE.get(2);
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                AwardFrom.TREASURE_WARE_BAG_EXPAND, player.common.getBuyTreasureWareBagCnt(), player.common.getTreasureWareCnt());
        player.common.setBuyTreasureWareBagCnt(player.common.getBuyTreasureWareBagCnt() + 1);
        player.common.setTreasureWareCnt(player.common.getTreasureWareCnt() + add);
    }

    /**
     * ?????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.OnTreasureWareRs onTreasureWare(long roleId, GamePb4.OnTreasureWareRq req) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkOpenTreasureWare(player);

        int heroId = req.getHeroId();
        // ????????????????????????
        Hero hero = player.heros.get(heroId);
        if (null == hero) {
            throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "???????????????????????????????????????, roleId:", player.roleId,
                    ", heroId:", heroId);
        }
        if (req.getType() != TreasureWareConst.TREASURE_WARE_DOWN &&
                req.getType() != TreasureWareConst.TREASURE_WARE_ON &&
                req.getType() != TreasureWareConst.TREASURE_WARE_REPLACE) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "?????????????????????, roleId:", roleId, ", type:", req.getType());
        }

        // ???????????????
        if (!hero.isIdle() && hero.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "onTreasureWare????????????????????????, roleId:", roleId, ", heroId:",
                    heroId, ", state:", hero.getState());
        }

        int treasureWareKeyId = req.getKeyId();
        // ????????????????????????
        TreasureWare treasureWare = player.treasureWares.get(treasureWareKeyId);
        if (null == treasureWare || treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_USING) {
            throw new MwException(GameError.NOT_OWNED_TREASURE_WARE.getCode(), "????????????????????????, roleId:", roleId, ", treasureWareKeyId:",
                    treasureWareKeyId);
        }

        // ????????????
        GamePb4.OnTreasureWareRs.Builder builder = GamePb4.OnTreasureWareRs.newBuilder();
        List<CommonPb.TreasureWare> list = new ArrayList<>();
        if (req.getType() == TreasureWareConst.TREASURE_WARE_ON) {
            // ?????????????????????????????????
            if (treasureWare.isOnEquip()) {
                throw new MwException(GameError.TREASURE_WARE_HAS_ON_HERO.getCode(), "????????????????????????????????????, roleId:", roleId,
                        ", treasureWareKeyId:", treasureWareKeyId, ", ????????????????????????id:", treasureWare.getHeroId());
            }

            // ????????????????????????
            checkTreasureWareConfig(roleId, treasureWare.getEquipId());

            // ??????????????????????????????????????????
            int keyId = hero.getTreasureWare() == null ? -1 : hero.getTreasureWare();
            if (keyId > 0) {
                // ???????????????????????????????????????
                downEquip(player, hero, keyId, list, true);
            }

            // ??????????????????
            heroOnTreasureWare(player, hero, treasureWareKeyId, list, true);
        } else if (req.getType() == TreasureWareConst.TREASURE_WARE_DOWN) {
            // ???????????????????????????????????????
            if (!hero.hasEquipTreasureWare(treasureWareKeyId)) {
                throw new MwException(GameError.TREASURE_WARE_NOT_ON_HERO.getCode(), "??????????????????????????????, roleId:", roleId, ", heroId:",
                        heroId, ", treasureWareKeyId:", treasureWareKeyId);
            }

            // ??????????????????
            downEquip(player, hero, treasureWareKeyId, list, true);
        } else if (req.getType() == TreasureWareConst.TREASURE_WARE_REPLACE) {
            Hero tgtHero = player.heros.get(treasureWare.getHeroId());
            if (CheckNull.isNull(tgtHero)) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "????????????????????????, roleId:", roleId, ", heroId:",
                        treasureWare.getHeroId(), ", type:", req.getType());
            }

            //??????????????????
            Integer srcKeyId = null;
            TreasureWare srcTreasureWare = player.treasureWares.get(hero.getTreasureWare());
            if (Objects.nonNull(srcTreasureWare)) {
                srcKeyId = hero.getTreasureWare();
                downEquip(player, hero, srcKeyId, list, false);
            }

            downEquip(player, tgtHero, treasureWareKeyId, list, false);
            //??????????????????
            if (Objects.nonNull(srcKeyId)) {
                heroOnTreasureWare(player, tgtHero, srcKeyId, list, true);
            }
            heroOnTreasureWare(player, hero, treasureWareKeyId, list, true);

            CalculateUtil.processAttr(player, tgtHero);
            builder.addHero(PbHelper.createHeroPb(tgtHero, player));
        }

        // ?????????????????????????????????
        CalculateUtil.processAttr(player, hero);
        builder.addHero(PbHelper.createHeroPb(hero, player));
        builder.addAllTreasureWares(list);
        LogUtil.debug("??????hero?????????: " + hero);
        return builder.build();
    }


    /**
     * ????????????
     */
    public GamePb4.TreasureWareLockedRs treasureWareLocked(Long roleId, GamePb4.TreasureWareLockedRq rq) throws MwException {
        if (rq.getKeyId() < 0 || (rq.getTreasureWareLocked() != TreasureWareConst.TREASURE_WARE_UNLOCKED
                && rq.getTreasureWareLocked() != TreasureWareConst.TREASURE_WARE_LOCKED)) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "?????????????????????, roleId:", roleId, ", keyId:", rq.getKeyId());
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkOpenTreasureWare(player);

        TreasureWare treasureWare = player.treasureWares.get(rq.getKeyId());
        if (treasureWare == null || treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_USING) {
            throw new MwException(GameError.NOT_OWNED_TREASURE_WARE.getCode(), "?????????,??????????????????, roleId:", roleId, ", keyId:", rq.getKeyId());
        }
        checkTreasureWareConfig(roleId, treasureWare.getEquipId());

        if (rq.getTreasureWareLocked() != TreasureWareConst.TREASURE_WARE_UNLOCKED) {
            treasureWare.setEquipLocked(TreasureWareConst.TREASURE_WARE_LOCKED);
        } else {
            treasureWare.setEquipLocked(TreasureWareConst.TREASURE_WARE_UNLOCKED);
        }

        GamePb4.TreasureWareLockedRs.Builder treasureWareLockedRs = GamePb4.TreasureWareLockedRs.newBuilder();
        treasureWareLockedRs.setKeyId(rq.getKeyId());
        treasureWareLockedRs.setTreasureWareLocked(treasureWare.getEquipLocked());
        return treasureWareLockedRs.build();
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.TreasureWareBatchDecomposeRs treasureWareDecompose(Long roleId, GamePb4.TreasureWareBatchDecomposeRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkOpenTreasureWare(player);

        if (ObjectUtils.isEmpty(req.getKeyIdList()) && !req.hasQuality() && !req.hasLevel()) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "?????????????????????, roleId:", roleId);
        }
        if (req.hasQuality() && (req.getQuality() < TreasureWareConst.BLUE_QUALITY || req.getQuality() > TreasureWareConst.ANCIENT_QUALITY)) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "?????????????????????, roleId:", roleId, ", quality: ", req.getQuality());
        }
        if (req.hasLevel() && req.getLevel() < 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "?????????????????????, roleId:", roleId, ", level: ", req.getLevel());
        }

        StaticTreasureWareLevel staticTreasureWareLevel;
        GamePb4.TreasureWareBatchDecomposeRs.Builder builder = null;
        List<Integer> keyIdList = req.getKeyIdList();
        if (ObjectUtils.isEmpty(keyIdList)) {
            keyIdList = filterTreasureWare(player, req);
        } else {
            for (Integer keyId : keyIdList) {
                checkBeforeDecompose(roleId, keyId, player);
            }
        }

        if (ObjectUtils.isEmpty(keyIdList)) {
            throw new MwException(GameError.NO_TREASURE_WARE_TO_DECOMPOSE.getCode(), "???????????????????????????, roleId:", roleId, ", req:",
                    req);
        }

        List<List<Integer>> allAwards = new ArrayList<>();
        for (Integer keyId : keyIdList) {
            TreasureWare treasureWare = player.treasureWares.get(keyId);
            staticTreasureWareLevel = StaticTreasureWareDataMgr.getStaticTreasureWareLevel(treasureWare.getQuality(), treasureWare.getLevel());

            // ????????????????????????
            EventDataUp.treasureCultivate(player, treasureWare, AwardFrom.TREASURE_WARE_DECOMPOSE, getAttrType(treasureWare));

            //????????????
            rewardDataManager.subTreasureWare(player, keyId, AwardFrom.TREASURE_WARE_DECOMPOSE,
                    treasureWare.getQuality(), treasureWare.getLevel(), req.hasQuality() ? req.getQuality() : -1, req.hasLevel() ? req.getLevel() : -1);

            builder = builder == null ? GamePb4.TreasureWareBatchDecomposeRs.newBuilder() : builder;
            allAwards.addAll(staticTreasureWareLevel.getResolve());

            builder.addKeyId(treasureWare.getKeyId());
        }

        builder.addAllAward(rewardDataManager
                .addAwardDelaySync(player, RewardDataManager.mergeAward(allAwards), null, AwardFrom.TREASURE_WARE_DECOMPOSE));
        return builder.build();
    }

    /**
     * ???????????????????????????
     *
     * @param roleId
     * @param keyId
     * @param player
     * @throws MwException
     */
    private void checkBeforeDecompose(long roleId, int keyId, Player player) throws MwException {
        TreasureWare treasureWare = player.treasureWares.get(keyId);
        if (treasureWare == null || treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_USING) {
            throw new MwException(GameError.NOT_OWNED_TREASURE_WARE.getCode(), "?????????,??????????????????, roleId: ", roleId, ", keyId: ",
                    keyId);
        }

        StaticTreasureWareLevel staticTreasureWareLevel = StaticTreasureWareDataMgr.getStaticTreasureWareLevel(treasureWare.getQuality(), treasureWare.getLevel());
        if (CheckNull.isNull(staticTreasureWareLevel)) {
            throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "????????????????????????, roleId: ", roleId,
                    ", quality: ", treasureWare.getQuality(), ", level: ", treasureWare.getLevel());
        }
        if (treasureWare.isOnEquip()) {
            throw new MwException(GameError.TREASURE_WARE_HAS_ON_HERO.getCode(), "????????????????????????????????????, roleId: ", roleId,
                    ", ????????????????????????id:", treasureWare.getHeroId());
        }
        checkTreasureWareConfig(roleId, treasureWare.getEquipId());
        if (treasureWare.getEquipLocked() == TreasureWareConst.TREASURE_WARE_LOCKED) {
            throw new MwException(GameError.TREASURE_WARE_LOCKED.getCode(), "???????????????  roleId:", roleId, ", treasureWareId: ",
                    treasureWare.getEquipId());
        }
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param keyId
     * @return
     * @throws MwException
     */
    public GamePb4.StrengthenTreasureWareRs strengthenTreasureWare(long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        checkOpenTreasureWare(player);

        TreasureWare treasureWare = player.treasureWares.get(keyId);
        if (CheckNull.isNull(treasureWare) || treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_USING) {
            throw new MwException(GameError.NOT_OWNED_TREASURE_WARE.getCode(), "?????????,??????????????????, roleId: ", roleId, ", keyId: ",
                    keyId);
        }

        Hero hero = null;
        if (treasureWare.isOnEquip()) {
            hero = player.heros.get(treasureWare.getHeroId());
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "???????????????????????????????????????, roleId:", player.roleId,
                        ", heroId:", treasureWare.getHeroId());
            }
            // ???????????????
            if (!hero.isIdle() && hero.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "onTreasureWare????????????????????????, roleId:", roleId, ", heroId:",
                        hero.getHeroId(), ", state:", hero.getState());
            }
        }

        checkTreasureWareConfig(roleId, treasureWare.getEquipId());
        Integer nextLevel = StaticTreasureWareDataMgr.getNextStaticTreasureWareLevel(treasureWare.getQuality(), treasureWare.getLevel());
        if (CheckNull.isNull(nextLevel)) {
            throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "?????????, ??????????????????, roleId: ", roleId, ", quality: ",
                    treasureWare.getQuality(), ", level: ", treasureWare.getLevel());
        }
        if (Integer.MAX_VALUE == nextLevel) {
            throw new MwException(GameError.TREASURE_WARE_STRENGTH_MAX_LEVEL.getCode(), "?????????, ?????????????????????, roleId: ", roleId, ", quality: ",
                    treasureWare.getQuality(), ", level: ", treasureWare.getLevel());
        }

        StaticTreasureWareLevel lastStaticTreasureWareLevel = StaticTreasureWareDataMgr.getStaticTreasureWareLevel(treasureWare.getQuality(), treasureWare.getLevel());
        StaticTreasureWareLevel staticTreasureWareLevel = StaticTreasureWareDataMgr.getStaticTreasureWareLevel(treasureWare.getQuality(), nextLevel);
        if (CheckNull.isNull(staticTreasureWareLevel) || CheckNull.isNull(lastStaticTreasureWareLevel)) {
            throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "?????????, ??????????????????, roleId: ", roleId, ", quality: ",
                    treasureWare.getQuality(), ", level: ", nextLevel, ", lastStaticTreasureWareLevel: ", lastStaticTreasureWareLevel == null ?
                    -1 : lastStaticTreasureWareLevel.getLevel());
        }

        // ????????????????????????
        if (player.lord.getLevel() < staticTreasureWareLevel.getNeedLevel()) {
            throw new MwException(GameError.TREASURE_STRENGTHEN_LEVEL_NOT_ENOUGH.getCode(), "????????????, ??????????????????, roleId: ", roleId, ", quality: ",
                    treasureWare.getQuality(), ", level: ", nextLevel, ", specialId: ", treasureWare.getSpecialId());
        }

        StaticTreasureWareSpecial staticTreasureWareSpecial = null;
        if (Objects.nonNull(treasureWare.getSpecialId())) {
            staticTreasureWareSpecial = StaticTreasureWareDataMgr.getTreasureWareSpecial(treasureWare.getSpecialId());
            if (CheckNull.isNull(staticTreasureWareSpecial)) {
                throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "?????????, ????????????????????????, roleId: ", roleId, ", quality: ",
                        treasureWare.getQuality(), ", level: ", nextLevel, ", specialId: ", treasureWare.getSpecialId());
            }
        }
        //??????????????????
        rewardDataManager.checkAndSubPlayerRes(player, lastStaticTreasureWareLevel.getConsume(), AwardFrom.STRENGTH_TREASURE_WARE, treasureWare.getEquipId(), keyId, nextLevel);
        treasureWare.setLevel(nextLevel);
        //????????????????????????
        Map<Integer, Integer> strongAttrMap = staticTreasureWareLevel.getAttr();
        for (Map.Entry<Integer, TreasureWareAttrItem> entry : treasureWare.getAttrs().entrySet()) {
            TreasureWareAttrItem attr = entry.getValue();
            attr.setLevel(nextLevel);
            Integer strongValue = strongAttrMap.getOrDefault(attr.getAttrId(), 0);
            if (strongValue > 0) {
//                LogUtil.error("" + attr.getInitValue() + " " + strongValue + " " + attr.getPercent() + "   " + ((int) Math.ceil(strongValue * (attr.getPercent() / TEN_THOUSAND_DOUBLE))));
                attr.setValue(attr.getInitValue() + (int) Math.ceil(strongValue * (attr.getPercent() / TEN_THOUSAND_DOUBLE)));
            }
        }
        LogLordHelper.strengthTreasureWare(AwardFrom.STRENGTH_TREASURE_WARE, player.account, player.lord, treasureWare.getKeyId(),
                treasureWare.getEquipId(), lastStaticTreasureWareLevel.getLevel(), staticTreasureWareLevel.getLevel());

        //????????????
        if (Objects.nonNull(treasureWare.getSpecialId())) {
            //??????????????????
            StaticTreasureWareSpecial nextSpecialAttr = StaticTreasureWareDataMgr.getStaticTreasureWareSpecial(staticTreasureWareSpecial.getSpecialId(), nextLevel);
            if (Objects.nonNull(nextSpecialAttr)) {
                if (CheckNull.isEmpty(treasureWare.getSpecialAttr()) && CheckNull.nonEmpty(nextSpecialAttr.getAttrSpecial())) {
                    taskDataManager.updTask(player, TaskType.COND_536, 1, treasureWare.getQuality());
                }
                treasureWare.getSpecialAttr().clear();
                for (List<Integer> specialAttr : nextSpecialAttr.getAttrSpecial()) {
                    treasureWare.getSpecialAttr().add(new Turple<>(specialAttr.get(0), specialAttr.get(1)));
                }

                treasureWare.setSpecialId(nextSpecialAttr.getId());
            }
        }

        EventDataUp.treasureCultivate(player, treasureWare, AwardFrom.STRENGTH_TREASURE_WARE, getAttrType(treasureWare));

        //??????????????????
        activityTriggerService.strengthTreasureWare(player, treasureWare.getQuality(), treasureWare.getLevel());

        taskDataManager.updTask(player, TaskType.COND_528, 1);
        taskDataManager.updTask(player, TaskType.COND_532, 1, nextLevel);

        // ?????????????????????????????????
        if (Objects.nonNull(hero)) {
            CalculateUtil.processAttr(player, hero);
        }
        TaskService.processTask(player, ETask.STRENGTH_QUALITY_AND_COUNT_TREASURE_WARE, 1, treasureWare.getQuality(), treasureWare.getLevel());
        GamePb4.StrengthenTreasureWareRs.Builder builder = GamePb4.StrengthenTreasureWareRs.newBuilder();
        builder.setKeyId(keyId);
        builder.setTreasureWare(treasureWare.createPb(false));
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @return
     */
    public List<Turple<Integer, Integer>> getBasicAttr(Player player, Hero hero) {
        if (CheckNull.isNull(hero.getTreasureWare())) {
            return null;
        }

        int keyId = hero.getTreasureWare();
        TreasureWare treasureWare = player.treasureWares.get(keyId);
        if (CheckNull.isNull(treasureWare)) {
            LogUtil.error("?????????????????????, lordId: ", player.lord.getLordId(), ", keyId: ", keyId);
            return null;
        }

        List<Turple<Integer, Integer>> result = new ArrayList<>(treasureWare.getAttrs().size());
        treasureWare.getAttrs().values().forEach(item -> result.add(new Turple<>(item.getAttrId(), item.getValue())));
        return result;
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param hero
     * @param type
     * @param subType
     * @param params
     * @return
     */
    public Object getTreasureWareBuff(Player player, Hero hero, int type, int subType, Object... params) {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_TREASURE_WARE)) {
            LogUtil.debug("?????????????????????, player.level: ", player.lord.getLevel());
            return null;
        }
        if (CheckNull.isNull(player) || CheckNull.isNull(hero) || CheckNull.isNull(hero.getTreasureWare())) {
            LogUtil.debug("????????????, player: ", player == null ? -1 : player.lord.getLordId(), ", hero: ",
                    hero == null ? -1 : hero.getHeroId(), ", keyId: ", hero.getTreasureWare());
            return null;
        }

        int keyId = hero.getTreasureWare();
        TreasureWare treasureWare = player.treasureWares.get(keyId);
        if (CheckNull.isNull(treasureWare)) {
            LogUtil.error("?????????????????????, lordId: ", player.lord.getLordId(), ", keyId: ", keyId);
            return null;
        }

        if (CheckNull.isNull(treasureWare.getSpecialId())) {
            LogUtil.debug("????????????????????????, lordId: ", player.lord.getLordId(), ", keyId: ", keyId);
            return null;
        }
        StaticTreasureWareSpecial staticTreasureWareSpecial = StaticTreasureWareDataMgr.
                getTreasureWareSpecial(treasureWare.getSpecialId());
        if (CheckNull.isNull(staticTreasureWareSpecial)) {
            LogUtil.error("??????????????????????????????????????????, lordId: ", player.lord.getLordId(),
                    ", specialId: ", treasureWare.getSpecialId());
            return null;
        }

        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        if (CheckNull.isNull(staticHero)) {
            LogUtil.error("????????????????????????, lordId: ", player.lord.getLordId(),
                    ", heroId: ", hero.getHeroId());
            return null;
        }
        if (staticTreasureWareSpecial.getHeroType() != 0 &&
                staticTreasureWareSpecial.getHeroType() != staticHero.getHeroType()) {
            LogUtil.debug("?????????????????????????????????, lordId: ", player.lord.getLordId(),
                    ", heroType: ", staticTreasureWareSpecial.getHeroType(),
                    ", staticHeroType: ", staticHero.getHeroType());
            return null;
        }
        if (type != TreasureWareConst.SpecialType.ADD_ATTR && staticTreasureWareSpecial.getType() != type) {
            LogUtil.debug("???????????????????????????, lordId: ", player.lord.getLordId(),
                    ", staticType: ", staticTreasureWareSpecial.getType(), ", type: ", type);
            return null;
        }

        if (staticTreasureWareSpecial.getArmyType() != 0 && staticTreasureWareSpecial.getArmyType() != staticHero.getType()) {
            LogUtil.debug("???????????????????????????????????????, ??????????????????, lordId: ", player.lord.getLordId(),
                    ", staticAramType: ", staticTreasureWareSpecial.getArmyType(), ", heroId: ", staticHero.getHeroId(), ", heroAramType: ", staticHero.getType());
            return null;
        }

        switch (type) {
            case TreasureWareConst.SpecialType.COLLECT_TYPE:
                if (!hero.isOnAcq()) {
                    LogUtil.debug("???????????????????????????????????????, lordId: ", player.lord.getLordId(),
                            ", heroId: ", hero.getHeroId(), ", heroStatus: ", hero.getStatus());
                    return null;
                }
                return staticTreasureWareSpecial.getAttrSpecial().
                        stream().filter(attr -> attr.get(0) == subType).collect(Collectors.toList());
            case TreasureWareConst.SpecialType.SEASON_HERO:
                int skillId = (int) params[0];
                return staticTreasureWareSpecial.getAttrSpecial().
                        stream().filter(attr -> attr.get(0) == subType && skillId == attr.get(1)).collect(Collectors.toList());
            case TreasureWareConst.SpecialType.ADD_ATTR:
                switch (staticTreasureWareSpecial.getType()) {
                    case TreasureWareConst.SpecialType.HERO_TYPE:
                        return staticTreasureWareSpecial.getAttrSpecial();
                    case TreasureWareConst.SpecialType.JANITOR_TYPE:
                        if (!hero.isOnWall()) {
                            LogUtil.debug("??????????????????????????????????????????, lordId: ", player.lord.getLordId(),
                                    ", heroId: ", hero.getHeroId(), ", heroStatus: ", hero.getStatus());
                            return null;
                        }
                        return staticTreasureWareSpecial.getAttrSpecial();
                    default:
                        return null;
                }
            default:
                return null;
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param hero
     * @param type
     * @param subType
     * @param params
     * @return
     */
    public int addSkillBuff(Player player, Hero hero, int type, int subType, Object... params) {
        Object skill = getTreasureWareBuff(player, hero, type, subType, params);
        if (ObjectUtils.isEmpty(skill) || !(skill instanceof List)) {
            return 0;
        }

        List<List<Integer>> skillList = (List<List<Integer>>) skill;
        return skillList.get(0).get(2);
    }

    /**
     * ??????????????????????????????
     */
    public void timedClearDecomposeTreasureWare() {
        LogUtil.common("===start delete expired treasureWare");
        int delTime = TimeHelper.getCurrentSecond() - Constant.DEL_DECOMPOSED_TREASURE_WARE * TimeHelper.DAY_S;
        // ????????????
        Optional.of(playerDataManager.getAllPlayer().values()).ifPresent(players -> {
            players.forEach(p -> {
                p.treasureWares.values().removeIf(treasureWare -> treasureWare.getDecomposeTime() != 0 &&
                        treasureWare.getDecomposeTime() <= delTime);
            });
        });
    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param req
     * @return
     */
    private List<Integer> filterTreasureWare(Player player, GamePb4.TreasureWareBatchDecomposeRq req) {
        if (ObjectUtils.isEmpty(player.treasureWares))
            return null;

        List<Integer> filterTreasureWareList = null;
        for (TreasureWare treasureWare : player.treasureWares.values()) {
            if (treasureWare.isOnEquip() || treasureWare.getStatus() != TreasureWareConst.TREASURE_IN_USING) {
                continue;
            }
            if (ObjectUtils.isEmpty(StaticTreasureWareDataMgr.getStaticTreasureWare(treasureWare.getEquipId())))
                continue;
            if (treasureWare.getEquipLocked() == TreasureWareConst.TREASURE_WARE_LOCKED) {
                continue;
            }
            if (req.hasQuality() && req.getQuality() != treasureWare.getQuality())
                continue;
            if (req.hasLevel() && req.getLevel() <= treasureWare.getLevel())
                continue;
            if (CheckNull.isNull(StaticTreasureWareDataMgr.getStaticTreasureWareLevel(treasureWare.getQuality(), treasureWare.getLevel())))
                continue;

            filterTreasureWareList = filterTreasureWareList == null ? new ArrayList<>() : filterTreasureWareList;
            filterTreasureWareList.add(treasureWare.getKeyId());
        }

        return filterTreasureWareList;
    }


    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param equipKeyId
     * @throws MwException
     */
    public void heroOnTreasureWare(Player player, Hero hero, int equipKeyId, List<CommonPb.TreasureWare> list,
                                   boolean record) throws MwException {
        heroOnTreasureWareIsUpTask(player, hero, equipKeyId, list, true, record);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param treasureWareKeyId
     * @param upTask
     * @throws MwException
     */
    public void heroOnTreasureWareIsUpTask(Player player, Hero hero, int treasureWareKeyId, List<
            CommonPb.TreasureWare> list, boolean upTask, boolean record) throws MwException {
        if (null == hero) {
            return;
        }
        TreasureWare treasureWare = player.treasureWares.get(treasureWareKeyId);
        if (null == treasureWare) {
            return;
        }
        if (treasureWareKeyId <= 0 || treasureWareKeyId >= Integer.MAX_VALUE) {
            LogUtil.error("keyId??????, treasureWareKeyId: ", treasureWareKeyId, ", lordId: ", player.lord.getLordId());
            return;
        }
        // ????????????????????????
        StaticTreasureWare staticTreasureWare = checkTreasureWareConfig(player.lord.getLordId(), treasureWare.getEquipId());
        if (null == staticTreasureWare) {
            return;
        }

        hero.onTreasureWare(treasureWare);
        treasureWare.onEquip(hero.getHeroId());
        taskDataManager.updTask(player, TaskType.COND_527, 1);
        taskDataManager.updTask(player, TaskType.COND_534, 1);
        if (record)
            list.add(treasureWare.createPb());
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param hero
     * @param treasureWareKeyId
     */
    public void downEquip(Player player, Hero hero, int treasureWareKeyId, List<CommonPb.TreasureWare> list,
                          boolean record) {
        if (null == hero) {
            return;
        }
        if (treasureWareKeyId <= 0 || treasureWareKeyId >= Integer.MAX_VALUE) {
            LogUtil.error("keyId??????, treasureWareKeyId: ", treasureWareKeyId, ", lordId: ", player.lord.getLordId());
            return;
        }

        // ??????????????????
        hero.downTreasureWare();
        TreasureWare treasureWare = player.treasureWares.get(treasureWareKeyId);
        if (null != treasureWare) {
            treasureWare.downEquip();// ????????????
            CalculateUtil.returnArmy(player, hero);
            if (record)
                list.add(treasureWare.createPb());
        }
    }

    private StaticTreasureWare checkTreasureWareConfig(long roleId, int twId) throws MwException {
        StaticTreasureWare staticTreasureWare = StaticTreasureWareDataMgr.getStaticTreasureWare(twId);
        if (null == staticTreasureWare) {
            throw new MwException(GameError.TREASURE_WARE_CONFIG_ERROR.getCode(), "???????????????, roleId:", roleId, ", twId:", twId);
        }
        return staticTreasureWare;
    }


    /**
     * ???????????????
     *
     * @param player
     * @throws MwException
     */
    public int remainBagCnt(Player player) {
        int cnt = 0;
        for (TreasureWare e : player.treasureWares.values()) {
            if (e.getStatus() == TreasureWareConst.TREASURE_IN_USING)
                cnt++;
        }

        return player.common.getTreasureWareCnt() - cnt;
    }

    /**
     * ??????????????????????????????
     *
     * @param player
     * @param count
     * @throws MwException
     */
    public void checkTreasureWareBag(Player player, long count) throws MwException {
        int remainBagCnt = DataResource.getBean(TreasureWareService.class).remainBagCnt(player);
        if (remainBagCnt < count) {
            throw new MwException(GameError.MAX_TREASURE_WARE_STORE.getCode(), "??????????????????, roleId: " + player.roleId + ", remainBagCnt= "
                    + remainBagCnt + ", bagCnt: " + player.common.getTreasureWareCnt() + ", cnt= " + count);
        }
    }

    /**
     * ??????????????????
     *
     * @param randomTreasureWare
     * @return
     */
    private TreasureWare createTreasureWare(StaticTreasureWare randomTreasureWare) {
        TreasureWare treasureWare = new TreasureWare();
        treasureWare.setEquipId(randomTreasureWare.getId());
        treasureWare.setQuality(randomTreasureWare.getQuality());
        treasureWare.setLevel(0);
        treasureWare.setEquipLocked(TreasureWareConst.TREASURE_WARE_UNLOCKED);
        return treasureWare;
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param awards
     * @param player
     * @throws MwException
     */
    public void checkMailAward(List<CommonPb.Award> awards, Player player) throws MwException {
        if (CheckNull.isEmpty(awards))
            return;

        long treasureWareCount = awards.stream().filter(award -> award.getType() == AwardType.TREASURE_WARE).count();
        if (treasureWareCount <= 0)
            return;

        checkTreasureWareBag(player, treasureWareCount);
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @throws MwException
     */
    public void checkOpenTreasureWare(Player player) throws MwException {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_TREASURE_WARE)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "?????????????????????, roleId: ", player.roleId, ", level: ", player.lord.getLevel());
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param quality
     * @param makeCount
     * @param condition
     * @return
     * @throws MwException
     */
    public int getCondition(int quality, int makeCount, String condition) throws MwException {
        if ((quality != TreasureWareConst.ORANGE_QUALITY && quality != TreasureWareConst.RED_QUALITY)) {
            if (!CheckNull.isNullTrim(condition)) {
                throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(),
                        " ????????????, firstMake: ", makeCount, ", quality: ", quality, ", condition: ", condition);
            }

            return 0;
        }
        if (makeCount > 0)
            return 0;

        if (CheckNull.isNullTrim(condition))
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), " ????????????, firstMake: ", makeCount, ", condition is empty");
        try {
            return Integer.parseInt(condition);
        } catch (NumberFormatException e) {
            LogUtil.error(e);
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), " ????????????, firstMake: ", makeCount, ", condition: ", condition);
        }
    }

    @GmCmd("treasureWare")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String cmd = params[0];
        if ("make".equalsIgnoreCase(cmd)) {
            String quality = params[1];
            String count = params[2];
            String level = null;
            if (params.length > 3) {
                level = params[3];
            }
            LogUtil.c2sMessage(makeTreasureWare(player.lord.getLordId(), Integer.parseInt(quality), Integer.parseInt(count), "0"), ChannelUtil.getRoleId(player.ctx));
            if (Objects.nonNull(level)) {
                int count_ = Integer.parseInt(count);
                while (count_-- > 0) {
                    TreasureWare treasureWare = player.treasureWares.get(player.getMaxKey() - count_ + 1);
                    if (CheckNull.isNull(treasureWare)) {
                        continue;
                    }
                    treasureWare.setLevel(Integer.parseInt(level) > StaticTreasureWareDataMgr.getMaxLevelByQuality(treasureWare.getQuality()) ?
                            StaticTreasureWareDataMgr.getMaxLevelByQuality(treasureWare.getQuality()) : Integer.parseInt(level));
                }
            }
        }
        if ("getAll".equalsIgnoreCase(cmd)) {
            LogUtil.c2sMessage(getTreasureWares(player.lord.getLordId()), ChannelUtil.getRoleId(player.ctx));
        }
        if ("bagExpand".equalsIgnoreCase(cmd)) {
            LogUtil.c2sMessage(bagExpand(player.lord.getLordId()), ChannelUtil.getRoleId(player.ctx));
        }
        if ("ware".equalsIgnoreCase(cmd)) {
            String heroId = params[1];
            String type = params[2];
            String keyId = params[3];

            GamePb4.OnTreasureWareRq.Builder builder = GamePb4.OnTreasureWareRq.newBuilder();
            builder.setHeroId(Integer.parseInt(heroId));
            builder.setKeyId(Integer.parseInt(keyId));
            builder.setType(Integer.parseInt(type));

            LogUtil.c2sMessage(onTreasureWare(player.lord.getLordId(), builder.build()), ChannelUtil.getRoleId(player.ctx));
        }
        if ("lock".equalsIgnoreCase(cmd)) {
            GamePb4.TreasureWareLockedRq.Builder req = GamePb4.TreasureWareLockedRq.newBuilder();
            req.setKeyId(Integer.parseInt(params[1]));
            req.setTreasureWareLocked(Integer.parseInt(params[2]));
            LogUtil.c2sMessage(treasureWareLocked(player.lord.getLordId(), req.build()), ChannelUtil.getRoleId(player.ctx));
        }
        if ("decompose".equalsIgnoreCase(cmd)) {
            GamePb4.TreasureWareBatchDecomposeRq.Builder req = GamePb4.TreasureWareBatchDecomposeRq.newBuilder();
            List<Integer> list = new ArrayList<>();
            Integer quality = null;
            Integer level = null;
            for (int i = 1; i < params.length; i++) {
                if (params[i].contains(",")) {
                    String[] temp = params[i].split(",");
                    for (String s : temp) {
                        list.add(Integer.parseInt(s));
                    }
                } else {
                    if (Integer.parseInt(params[i]) > 6) {
                        level = Integer.parseInt(params[i]);
                    } else {
                        quality = Integer.parseInt(params[i]);
                    }
                }
            }

            if (!ObjectUtils.isEmpty(list)) {
                req.addAllKeyId(list);
            }
            if (Objects.nonNull(quality)) {
                req.setQuality(quality);
            }
            if (Objects.nonNull(level)) {
                req.setLevel(level);
            }

            LogUtil.c2sMessage(treasureWareDecompose(player.lord.getLordId(), req.build()), ChannelUtil.getRoleId(player.ctx));
        }
        if ("strength".equalsIgnoreCase(cmd)) {
            LogUtil.c2sMessage(strengthenTreasureWare(player.lord.getLordId(), Integer.parseInt(params[1])), ChannelUtil.getRoleId(player.ctx));
        }
        if ("clear".equalsIgnoreCase(cmd)) {
            player.treasureWares.clear();
            for (Hero hero : player.heros.values()) {
                hero.setTreasureWare(null);
            }
        }

        if ("return".equalsIgnoreCase(cmd)) {
            int tWareConfId = Integer.parseInt(params[0]);
            int level = Integer.parseInt(params[1]);
            String attrInit = params[2];
            int specialId = Integer.parseInt(params[3]);

            StaticTreasureWare staticTreasureWare = StaticTreasureWareDataMgr.getStaticTreasureWare(tWareConfId);
            if (CheckNull.isNull(staticTreasureWare)) {
                LogUtil.error("??????????????????id?????????, tWareConfId: ", tWareConfId);
                return;
            }

            TreasureWare treasureWare = createTreasureWare(staticTreasureWare);
            for (String attr : attrInit.split(";")) {
                String[] attrArr = attr.split(",");
                TreasureWareAttrItem attrItem = new TreasureWareAttrItem(Integer.parseInt(attrArr[0]), Integer.parseInt(attrArr[1]));
                treasureWare.getAttrs().put(attrItem.getIndex(), attrItem);
            }
            if (specialId > 0)
                treasureWare.setSpecialId(specialId);
            if (level > 0) {
                treasureWare.setLevel(level);
            }

            int now = TimeHelper.getCurrentSecond();
            List<CommonPb.Award> treasureWareMailList = new ArrayList<>();
            putInTreasureWare(remainBagCnt(player), 1, treasureWare, player, staticTreasureWare, treasureWareMailList, AwardFrom.TREASURE_WARE_GM_RETURN);
            if (treasureWareMailList.size() > 0)
                mailDataManager.sendAttachMail(player, treasureWareMailList, MailConstant.MOLD_OUT_OF_RANGE_AWARD,
                        AwardFrom.MAKE_TREASURE_WARE_IN_MAIL, now, treasureWareMailList.get(0).getKeyId(), treasureWareMailList.size());

            LogUtil.debug("??????????????????, treasureWare: ", treasureWare.toString(), ", lordId: ", player.lord.getLordId());
        }

        if ("returnK".equalsIgnoreCase(cmd)) {
            int twKeyId = Integer.parseInt(params[1]);
            TreasureWare treasureWare = player.treasureWares.get(twKeyId);
            if (Objects.nonNull(treasureWare)) {
                treasureWare.setStatus(TreasureWareConst.TREASURE_IN_USING);
                treasureWare.setDecomposeTime(0);

                int quality = treasureWare.getQuality();
                int attrType = getAttrType(treasureWare);
                int profileId = StaticTreasureWareDataMgr.getProfileId(attrType, quality, treasureWare.getSpecialId());
                // ???????????????????????????
                LogLordHelper.treasureWare(
                        AwardFrom.TREASURE_WARE_GM_RETURN,
                        player.account,
                        player.lord,
                        treasureWare.getEquipId(),
                        treasureWare.getKeyId(),
                        Constant.ACTION_ADD,
                        profileId,
                        quality,
                        treasureWare.logAttrs(),
                        CheckNull.isNull(treasureWare.getSpecialId()) ? -1 : treasureWare.getSpecialId()
                );

                LogUtil.debug("??????????????????, treasureWare: ", treasureWare.toString(), ", lordId: ", player.lord.getLordId());

                // ????????????????????????
                EventDataUp.treasureCultivate(player, treasureWare, AwardFrom.TREASURE_WARE_GM_RETURN, attrType);
            }
        }

        if ("getAllTr".equalsIgnoreCase(cmd)) {
            Map<Integer, List<StaticTreasureWareSpecial>> map = StaticTreasureWareDataMgr.getTypesTreasureWareSpecial();
            List<StaticTreasureWare> qualityList = StaticTreasureWareDataMgr.getQualityTreasureWare(Integer.parseInt(params[1]));
            StaticTreasureWare randomTreasureWare = qualityList.get(0);
            int now = TimeHelper.getCurrentSecond();
            int putInBag = 0;
            int remainBagCnt = remainBagCnt(player);
            for (Integer type : map.keySet()) {
                //????????????
                TreasureWare treasureWare = basicAttr(randomTreasureWare, player.getTreasureCombat().getCurCombatId(), 0, player, now);
                //????????????
                Integer specialId = map.get(type).get(RandomHelper.randomInSize(map.get(type).size())).getId();
                StaticTreasureWareSpecial staticTreasureWareSpecial = StaticTreasureWareDataMgr.getTreasureWareSpecial(specialId);
                treasureWare.setSpecialId(specialId);
                for (List<Integer> specialAttr : staticTreasureWareSpecial.getAttrSpecial()) {
                    treasureWare.getSpecialAttr().add(new Turple<>(specialAttr.get(0), specialAttr.get(1)));
                }
                putInBag = putInTreasureWare(remainBagCnt, putInBag, treasureWare, player, randomTreasureWare, null, AwardFrom.GM_SEND);
            }
        }
        if ("makeSp".equalsIgnoreCase(cmd)) {
            String quality = params[1];
            String specialCnfId = params[2];

            List<StaticTreasureWare> qualityList = StaticTreasureWareDataMgr.getQualityTreasureWare(Integer.parseInt(quality));
            StaticTreasureWare randomTreasureWare = qualityList.get(0);
            int now = TimeHelper.getCurrentSecond();
            int putInBag = 0;
            int remainBagCnt = remainBagCnt(player);

            //????????????
            TreasureWare treasureWare = basicAttr(randomTreasureWare, player.getTreasureCombat().getCurCombatId(), 0, player, now);
            //????????????
            treasureWare.setSpecialId(Integer.parseInt(specialCnfId));
            putInTreasureWare(remainBagCnt, putInBag, treasureWare, player, randomTreasureWare, null, AwardFrom.GM_SEND);
        }
        if ("lvMaxUp".equalsIgnoreCase(cmd)) {
            int keyId = Integer.parseInt(params[1]);
            TreasureWare treasureWare = player.treasureWares.get(keyId);
            treasureWare.setLevel(StaticTreasureWareDataMgr.getMaxLevelByQuality(treasureWare.getQuality()));
            StaticTreasureWareLevel staticTreasureWareLevel = StaticTreasureWareDataMgr.getStaticTreasureWareLevel(treasureWare.getQuality(), treasureWare.getLevel());
            //????????????
            treasureWare.getAttrs().forEach((k, v) -> {
                Map<Integer, Integer> strongAttrs = staticTreasureWareLevel.getAttr();
                Integer cfgValue = strongAttrs.getOrDefault(v.getAttrId(), 0);
                if (cfgValue > 0) {
                    v.setValue(v.getInitValue() + (int) Math.ceil(cfgValue * v.getPercent()));
                }
            });

            if (Objects.nonNull(treasureWare.getSpecialId())) {
                StaticTreasureWareSpecial nextSpecialAttr = StaticTreasureWareDataMgr.getStaticTreasureWareSpecial(StaticTreasureWareDataMgr.
                        getTreasureWareSpecial(treasureWare.getSpecialId()).getSpecialId(), treasureWare.getLevel());
                treasureWare.setSpecialId(nextSpecialAttr.getId());
            }

            if (treasureWare.getHeroId() > 0) {
                Hero hero = player.heros.get(treasureWare.getHeroId());
                if (Objects.nonNull(hero)) {
                    CalculateUtil.processAttr(player, hero);
                }
            }
        }
        if ("addSp".equalsIgnoreCase(cmd)) {
            int keyId = Integer.parseInt(params[1]);
            int specialId = Integer.parseInt(params[2]);
            boolean add = "1".equals(params[3]);

            TreasureWare treasureWare = player.treasureWares.get(keyId);
            if (CheckNull.isNull(treasureWare))
                return;
            StaticTreasureWareSpecial cnf = StaticTreasureWareDataMgr.getTreasureWareSpecial(specialId);
            if (CheckNull.isNull(cnf))
                return;
            if (add) {
                treasureWare.setSpecialId(specialId);
            } else {
                treasureWare.setSpecialId(null);
            }

            if (treasureWare.getHeroId() > 0) {
                Optional.ofNullable(player.heros.get(treasureWare.getHeroId())).ifPresent(hero -> CalculateUtil.processAttr(player, hero));
            }
        }
        if ("resetFs".equalsIgnoreCase(cmd)) {
            player.getMakeTreasureWare().setOrangeFirstMakeTw((byte) 0);
            player.getMakeTreasureWare().setAncientFirstMakeTw((byte) 0);
        }
    }
}