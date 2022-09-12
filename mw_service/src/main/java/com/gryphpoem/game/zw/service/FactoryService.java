package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.AwardType.Army;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticFactoryExpand;
import com.gryphpoem.game.zw.resource.domain.s.StaticFactoryRecruit;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 兵营招募，领取，募兵加时（扩建），加速
 *
 * @author tyler
 */
@Service
public class FactoryService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private SolarTermsDataManager solarTermsDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private CiaService ciaService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private SeasonTalentService seasonTalentService;

    @Autowired
    private WorldWarSeasonDailyRestrictTaskService worldWarSeasonDailyRestrictTaskService;

    private static final int PARAM_MINUTE_5 = 5;// 招募5分钟为单位

    /**
     * 获取兵营信息
     *
     * @param roleId
     * @return
     */
    public GetFactoryRs getFactoryRs(long roleId, int id) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            GetFactoryRs.Builder builder = GetFactoryRs.newBuilder();
            builder.setId(id);
            Factory factory = createFactoryIfNotExist(player, id);
            Resource resource = player.resource;
            if (factory != null) {
                builder.setExpandLv(factory.getFctExpLv());
                builder.setLv(factory.getFctLv());
                for (ArmQue armQue : factory.getAddList()) {
                    builder.addArmQue(PbHelper.createArmQuePb(armQue));
                }
            }
            if (id == BuildingType.FACTORY_1) {
                builder.setArmNum(resource.getArm1());
            } else if (id == BuildingType.FACTORY_2) {
                builder.setArmNum(resource.getArm2());
            } else if (id == BuildingType.FACTORY_3) {
                builder.setArmNum(resource.getArm3());
            } else if (id == BuildingType.TRAIN_FACTORY_1 || id == BuildingType.TRAIN_FACTORY_2) {
                BuildingExt buildingExt = player.buildingExts.get(id);
                if (!CheckNull.isNull(buildingExt)) {
                    long arm = resource.getArmByTrain(buildingExt.getType());
                    if (arm > 0) {
                        builder.setArmNum(arm);
                    }
                }
            } else {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "获取兵营信息时，加时配置找不到, roleId:" + roleId + ",id=" + id + ",lv=" + factory.getFctLv());
            }
            StaticFactoryRecruit staticFactoryRecruit = StaticBuildingDataMgr.getStaticFactoryRecruit(id,
                    factory.getFctLv());
            if (staticFactoryRecruit == null) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "获取兵营信息时，加时配置找不到, roleId:" + roleId + ",id=" + id + ",lv=" + factory.getFctLv());
            }

            builder.setAddNum(getAddNum(player, staticFactoryRecruit, id));
            return builder.build();
        }
        return null;
    }

    /**
     * 获得能够募兵的上限,包涵各种加成
     *
     * @param player
     * @param staticFactoryRecruit
     * @param buildingId
     * @return
     */

    public int getAddNum(Player player, StaticFactoryRecruit staticFactoryRecruit, int buildingId) {
        int arm = staticFactoryRecruit.getArmNum();
        int armyType = getArmyTypeByBuidingId(player, buildingId);

        // 科技加成
        double techEffect = techDataManager.getTechEffect4BuildingType(player, armyType);

        // 道具加成
        double propEffect = 0.0;
        int now = TimeHelper.getCurrentSecond();
        Effect ef = player.getEffect().get(EffectConstant.ARM_CREATE_SPEED);
        if (ef != null && ef.getEndTime() > now) {
            int val = ef.getEffectVal();// vip加成
            propEffect += (vipDataManager.getNum(player.lord.getVip(), VipConstant.FACTORY_RECRUIT) + val)
                    / Constant.TEN_THROUSAND;
        }

        // 特工加成
        double agentEffect = armyAgentEffect(player, armyType) / Constant.TEN_THROUSAND;
        // 柏林官员加成
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_ARMY)
                / Constant.TEN_THROUSAND;
        // 名城加成
        double cityBuff = worldDataManager.getCityBuffer(worldDataManager.checkCityBuffer(player.lord.getPos()),
                WorldConstant.CityBuffer.ADD_ARMY_RECRUIT, player.roleId);
        //赛季天赋
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_302) / Constant.TEN_THROUSAND;

        // 季节加成
        double solarTermsEffect = solarTermsDataManager.getLevyBoundByType(SolarTermsConstant.SOLAR_TERMS_TYPE_RECRUIT)
                / Constant.TEN_THROUSAND;
        // 活动加成
        double actEffect = activityDataManager.getActRecruitNum() / Constant.TEN_THROUSAND;

        // 募兵数量=募兵加时等级对应募兵数量*（1+科技加成[%]+武卒官加成[%]+特工加成[%]+官职加成[%]+名城加成[%]+赛季加成[%]）*（1+募兵活动加成[%]+季节加成[%]）
        int sumArm = (int) (arm * (1.0 + techEffect + propEffect + agentEffect + berlinJobEffect + cityBuff + seasonTalentEffect)
                * (1.0 + solarTermsEffect + actEffect));

        // 赛季天赋(固定数值，加在最后)
        sumArm += seasonTalentService.getSeasonTalentEffectValueByType(player, SeasonConst.TALENT_EFFECT_603, buildingId);
        return sumArm;
    }

    /**
     * 特工加成效果
     *
     * @param player
     * @param armyType
     * @return
     */
    private int armyAgentEffect(Player player, int armyType) {
        int agentSkill = 0;
        if (armyType == Army.FACTORY_1_ARM) {
            agentSkill = CiaConstant.SKILL_ARMY_1_ACC;
        } else if (armyType == Army.FACTORY_2_ARM) {
            agentSkill = CiaConstant.SKILL_ARMY_2_ACC;
        } else if (armyType == Army.FACTORY_3_ARM) {
            agentSkill = CiaConstant.SKILL_ARMY_3_ACC;
        }
        return ciaService.getAgentSkillVal(player, agentSkill);
    }

    /**
     * 特工消耗补给效果
     *
     * @param player
     * @param buildType
     * @return
     */
    private int armyCostFoodAgent(Player player, int buildType) {
        int agentSkill = 0;
        if (buildType == BuildingType.FACTORY_1) {
            agentSkill = CiaConstant.SKILL_ARMY_1_FOOD_REDUCE;
        } else if (buildType == BuildingType.FACTORY_2) {
            agentSkill = CiaConstant.SKILL_ARMY_2_FOOD_REDUCE;
        } else if (buildType == BuildingType.FACTORY_3) {
            agentSkill = CiaConstant.SKILL_ARMY_3_FOOD_REDUCE;
        }

        return ciaService.getAgentSkillVal(player, agentSkill);
    }

    /**
     * 是否正在造兵的队列
     *
     * @param player
     * @return true 有造兵的队列
     */
    public boolean hasFactoryQue(Player player) {
        for (Factory factory : player.factory.values()) {
            if (!factory.getAddList().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private int coverFactorId(int id) {
        if (id == BuildingType.FACTORY_1) {
            return 1;
        } else if (id == BuildingType.FACTORY_2) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * 招兵队列执行(此处 仅仅只有推送逻辑)
     */
    public void armyQueTimer() {
        /*int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().filter(p -> hasFactoryQue(p)).forEach(p -> {
            p.factory.forEach((fId, factor) -> {
                String pushId = PushConstant.PRODUCT_ARMY_COMPLETE + "_" + fId.toString();
                if (!p.hasPushRecord(pushId)) {// 没有被推送过
                    if (!factor.getAddList().isEmpty()) {
                        ArmQue q = factor.getAddList().get(0);
                        if (now >= q.getEndTime()) {
                            p.putPushRecord(pushId, PushConstant.PUSH_HAS_PUSHED);// 设置已推送
                            String id = "s_army_" + coverFactorId(fId);
                            String name = StaticIniDataMgr.getTextName(id);
                            if (name != null) {
                                // PushMessageUtil.pushMessage(p.account, PushConstant.PRODUCT_ARMY_COMPLETE, name);
                            } else {
                                LogUtil.error("兵名称未找到，跳过消息推送, roleId:", p.roleId, ", id:", id);
                            }
                        }
                    }
                }
            });
        });*/
    }

    /**
     * 兵营招募
     *
     * @param roleId
     * @return
     */
    public FactoryRecruitRs getFactoryRecruitRs(long roleId, int id, int addTime) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            if (id != BuildingType.FACTORY_1 && id != BuildingType.FACTORY_2 && id != BuildingType.FACTORY_3
                    && id != BuildingType.TRAIN_FACTORY_1 && id != BuildingType.TRAIN_FACTORY_2) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "兵营招募时，建筑参数错误, roleId:" + roleId);
            }
            buildingDataManager.checkBuildingIsCreate(id, player);
            // 检查建筑是否在升级
            if (buildingDataManager.checkBuildIsUpping(player, id)) {
                throw new MwException(GameError.BUILD_IS_UPPING.getCode(), "roleId:", roleId, " 当前建筑正在升级");
            }
            Factory factory = createFactoryIfNotExist(player, id);

            // StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(id,
            // BuildingDataManager.getBuildingLv(id, player));
            int type = id;
            if (id == BuildingType.TRAIN_FACTORY_1 || id == BuildingType.TRAIN_FACTORY_2) {
                // 当前募兵建筑扩展
                BuildingExt buildingExt = player.buildingExts.get(id);
                if (CheckNull.isNull(buildingExt)) {
                    throw new MwException(GameError.BUILDEXT_NOT_FOUND.getCode(), "建筑扩展未找到, roleId:", player.roleId,
                            ", buildingId:", id);
                }
                type = BuildingType.getFactoryConvertTrain(buildingExt.getType());
            }

            // 检查当前兵种的容量
            checkArmNum(id, player, factory);

            StaticFactoryRecruit staticFactoryRecruit = StaticBuildingDataMgr.getStaticFactoryRecruit(id,
                    factory.getFctLv());
            int needTime = staticFactoryRecruit.getUpTime();

            int time = addTime * PARAM_MINUTE_5 * TimeHelper.MINUTE;
            if (time > needTime) {
                throw new MwException(GameError.FACTORYRECRUIT_PARAM_ERROR.getCode(), "兵营招募时，时间传入错误, roleId:" + roleId);
            }

            time += TimeHelper.getCurrentSecond();
            for (ArmQue armQue : factory.getAddList()) {
                if (TimeHelper.getCurrentSecond() >= armQue.getEndTime()) {
                    throw new MwException(GameError.UP_RECRUIT_AFTER_GAIN.getCode(),
                            "兵营招募时，领取兵力才能继续募兵, roleId:" + roleId);
                }
                time = armQue.getEndTime() + addTime * PARAM_MINUTE_5 * TimeHelper.MINUTE;
            }

            // 计算募兵总数
            int addNum = getAddNum(player, staticFactoryRecruit, id);

            // 实际募兵= 募兵时间 / 总时间【upTime】 * armNum【一次募兵的上限数】
            int addArm = (int) ((addTime * PARAM_MINUTE_5 * 1.00 * TimeHelper.MINUTE / staticFactoryRecruit.getUpTime())
                    * addNum);
            // 计算需要消耗的补给
            int needFood = this.calcCostFood(player, type, addArm);

            int needOIL = 0;
            rewardDataManager.checkPlayerResIsEnough(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood);

            if (id == BuildingType.TRAIN_FACTORY_1 || id == BuildingType.TRAIN_FACTORY_2) {
                // 消耗燃油= 募兵数量*单兵消耗补给（初始为5，受科技影响）*0.8 [等同于补给消耗 * 消耗燃油系数]
                needOIL = (int) Math.ceil(needFood * (Constant.TRAIN_FACTORY_NORMAL / Constant.TEN_THROUSAND));
                LogUtil.debug(roleId + "兵营招募,addArm=" + addArm + ",needOIL=" + needOIL);
                rewardDataManager.checkPlayerResIsEnough(player, AwardType.RESOURCE, AwardType.Resource.OIL, needOIL);
            }
            LogUtil.debug(roleId + "兵营招募,addArm=" + addArm + ",needFood=" + needFood);
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood, AwardFrom.FACTORYRECRUIT, id);

            // 上面检测过燃油了, 这里直接扣除
            if (needOIL > 0) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.OIL, needOIL, AwardFrom.FACTORYRECRUIT, id);
            }

            ArmQue que = new ArmQue(player.maxKey(), id, addArm, time, addTime, needFood, needOIL);
            factory.getAddList().add(que);
            FactoryRecruitRs.Builder builder = FactoryRecruitRs.newBuilder();
            builder.setId(id);
            for (ArmQue armQue : factory.getAddList()) {
                builder.addArmQue(PbHelper.createArmQuePb(armQue));
            }
            if (factory.getAddList().size() == 1) {
                taskDataManager.updTask(player, TaskType.COND_ARM_CNT, 1, BuildingType.getResourceByBuildingType(id));
                que.setNotExtendQue(true);
            }
            return builder.build();
        }
        return null;
    }

    /**
     * 计算募兵消耗的补给
     *
     * @param player
     * @param type
     * @param addArm
     * @return
     */
    private int calcCostFood(Player player, int type, int addArm) {
        /*
         * 公式: 消耗补给具体值 = 募兵数量 * 消耗系数 (条件 消耗系数>=基础系数)
         * 消耗系数 = (攻击科技系数 > 0 ? 攻击科技系数:基础系数) + 其他加成系数合计
         * 其他加成系数合计 = 强化科技系数 - 特工系数
         */
        // 攻击科技系数
        int atkRadio = this.techDataManager.getFood4BuildingType(player, type);
        // 强化科技系数
        int strengRadio = this.techDataManager.getStrengthenFood4BuildingType(player, type);
        // 特工系数
        int agentRadio = this.armyCostFoodAgent(player, type);
        // 其他加成系数合计
        int otherRadio = strengRadio - agentRadio;
        // 基础系数
        final int baseRadio = Constant.FACTORY_ARM_NEED_FOOD;
        // 消耗系数
        double radio = (atkRadio > 0 ? atkRadio : baseRadio) + otherRadio;
        //天赋优化
        radio *= (Constant.TEN_THROUSAND - (DataResource.getBean(SeasonTalentService.class).getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_605)));
        radio = radio >= (baseRadio * Constant.TEN_THROUSAND) ? radio : (baseRadio * Constant.TEN_THROUSAND); // 保底是基础系数

        LogUtil.debug("---------募兵补给消耗 roleId:", player.roleId, ", atkRadio:", atkRadio, ", strengRadio:", strengRadio,
                ", agentRadio:", agentRadio, ", otherRadio:", otherRadio, ", baseRadio:", baseRadio, ", radio:", radio,
                ", type:", type, ", needFood:", (addArm * radio));
        return (int) (addArm * radio / Constant.TEN_THROUSAND);
    }

    /**
     * 根据建筑id获取兵种类型
     *
     * @param player
     * @param buildingId
     * @return 返回0 说明转换失败
     */
    public static int getArmyTypeByBuidingId(Player player, int buildingId) {
        BuildingExt buildingExt = player.buildingExts.get(buildingId);
        if (CheckNull.isNull(buildingExt)) {
            return 0;
        }
        int type = buildingExt.getType();
        return BuildingType.getResourceByBuildingType(type);
    }

    /**
     * 检查当前兵种的容量
     *
     * @param id
     * @param player
     * @param factory
     * @throws MwException
     */
    private void checkArmNum(int id, Player player, Factory factory) throws MwException {
        // 当前募兵建筑扩展
        BuildingExt buildingExt = player.buildingExts.get(id);
        if (CheckNull.isNull(buildingExt)) {
            throw new MwException(GameError.BUILDEXT_NOT_FOUND.getCode(), "建筑扩展未找到, roleId:", player.roleId,
                    ", buildingId:", id);
        }
        int type = buildingExt.getType();
        if (playerDataManager.getArmCount(player.resource,
                BuildingType.getResourceByBuildingType(type)) >= getAllArmNum(player, id, factory, buildingExt)) {
            throw new MwException(GameError.RECRUIT_ARM_FULL.getCode(), "兵营招募时，兵已达上限, roleId:" + player.roleId
                    + ",nowSize=" + factory.getAddList().size() + ",maxSize=" + factory.getFctExpLv());
        }
    }

    /**
     * 相对应的建筑Id
     *
     * @param player
     * @param buildingId
     * @param buildingExt
     * @return
     * @throws MwException
     */
    private int getExtBuildingId(Player player, int buildingId, BuildingExt buildingExt) throws MwException {
        int type = buildingExt.getType();
        int extBuildingId = BuildingType.getFactoryConvertTrain(type);
        if (BuildingDataManager.getBuildingLv(extBuildingId, player) >= 1
                && buildingDataManager.checkBuildingLock(player, extBuildingId)) { // 判断对应建筑已创建,并且已解锁
            if (extBuildingId == BuildingType.TRAIN_FACTORY_1 || extBuildingId == BuildingType.TRAIN_FACTORY_2) { // 募兵的是兵营
                BuildingExt ext = player.buildingExts.get(extBuildingId);
                if (BuildingType.getFactoryConvertTrain(ext.getType()) == buildingId) { // 训练中心跟当前兵营训练的同一种兵
                    return extBuildingId;
                }
            } else { // 募兵的是训练中心
                return extBuildingId;
            }
        }
        return 0;
    }

    /**
     * 获取当前类型的兵力容量
     *
     * @param player
     * @param buildingId
     * @param factory
     * @param buildingExt
     * @return
     * @throws MwException
     */
    private int getAllArmNum(Player player, int buildingId, Factory factory, BuildingExt buildingExt)
            throws MwException {
        double armNum = 0;
        StaticFactoryExpand staticFactoryExpand = StaticBuildingDataMgr.getStaticFactoryExpand(buildingId,
                factory.getFctExpLv());
        if (staticFactoryExpand == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "兵营招募时，位置不足, roleId:" + player.roleId + ",建筑类型=" + buildingId + ",扩建等级=" + factory.getFctExpLv());
        }
        if (factory.getAddList().size() >= staticFactoryExpand.getBuildNum()) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), "兵营招募时，位置不足, roleId:" + player.roleId
                    + ",nowSize=" + factory.getAddList().size() + ",maxSize=" + factory.getFctExpLv());
        }
        StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(buildingId,
                BuildingDataManager.getBuildingLv(buildingId, player));
        LogUtil.debug(player.roleId + ",扩建等级" + staticFactoryExpand.getCnt() + "招募容量=" + staticFactoryExpand.getArmNum()
                + ",lvId=" + staticBuildingLevel.getId() + "," + staticBuildingLevel.getCapacity().get(0).get(1));
        armNum += staticFactoryExpand.getArmNum() + staticBuildingLevel.getCapacity().get(0).get(1);
        //兵营科技加成
        armNum = getArmyFactoryCapacityWithTechAdd(player, armNum, buildingId);

        int extBuildingId = getExtBuildingId(player, buildingId, buildingExt);
        if (extBuildingId > 0) {
            Factory extFactory = createFactoryIfNotExist(player, extBuildingId);
            StaticFactoryExpand extFactoryExpand = StaticBuildingDataMgr.getStaticFactoryExpand(extBuildingId,
                    extFactory.getFctExpLv());
            if (CheckNull.isNull(extFactoryExpand)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "兵营招募时，位置不足, roleId:" + player.roleId + ",建筑类型="
                        + extBuildingId + ",扩建等级=" + extFactory.getFctExpLv());
            }
            StaticBuildingLv extBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(extBuildingId,
                    BuildingDataManager.getBuildingLv(extBuildingId, player));
            if (Objects.nonNull(extBuildingLevel)) {
                armNum += extFactoryExpand.getArmNum() + extBuildingLevel.getCapacity().get(0).get(1);
                //兵营科技加成
                armNum = getArmyFactoryCapacityWithTechAdd(player, armNum, extBuildingId);
            }
            LogUtil.debug(player.roleId + ",扩建等级" + extFactoryExpand.getCnt() + "招募容量=" + extFactoryExpand.getArmNum()
                    + ",lvId=" + extBuildingLevel.getId() + "," + extBuildingLevel.getCapacity().get(0).get(1));
        }
        return (int)armNum;
    }

    /**
     * 科技对兵营容量上线的加成
     * @param player
     * @param armyNum
     * @param buildingId
     * @return
     */
    private double getArmyFactoryCapacityWithTechAdd(Player player, double armyNum, int buildingId) {
        int techTy = getTechType(buildingId);
        if (techTy > 0) {
            int addRatio = techDataManager.getTechEffect4SingleVal(player, techTy);//增加兵营容量百分比
            armyNum = armyNum * (1 + addRatio / Constant.TEN_THROUSAND);
        }
        return armyNum;
    }

    /**
     * 兵营ID 对应的容量扩展科技类型
     * @param buildingId
     * @return
     */
    private int getTechType(int buildingId){
        switch (buildingId) {
            case BuildingType.FACTORY_1: {
                return TechConstant.TYPE_33;
            }
            case BuildingType.FACTORY_2: {
                return TechConstant.TYPE_35;
            }
            case BuildingType.FACTORY_3: {
                return TechConstant.TYPE_34;
            }
            case BuildingType.TRAIN_FACTORY_1: {
                return TechConstant.TYPE_36;
            }
            default: return 0;
        }
    }

    /**
     * 领取兵力
     *
     * @param roleId
     * @param id     建筑位置
     * @return
     * @throws MwException
     */
    public AddArmRs getAddArmRs(long roleId, int id) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            Factory factory = player.factory.get(id);
            if (factory == null) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "领取兵力时，无募兵队列, roleId:" + roleId);
            }
            int buildingLv = BuildingDataManager.getBuildingLv(id, player);
            if (buildingLv < 1) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "兵营招募领取时，建筑还未建造, roleId:" + roleId,
                        ", buildingId:", id);
            }
            BuildingExt ext = player.buildingExts.get(id);
            if (CheckNull.isNull(ext)) {
                throw new MwException(GameError.BUILDEXT_NOT_FOUND.getCode(), "建筑扩展未找到, roleId:", player.roleId,
                        ", buildingId:", id);
            }
            int type = ext.getType();
            int addNum = 0;
            // 查看结束的
            Iterator<ArmQue> it = factory.getAddList().iterator();
            while (it.hasNext()) {
                ArmQue armQue = it.next();
                if (TimeHelper.getCurrentSecond() >= armQue.getEndTime()) {
                    // 统计预备队列的数量，一个队列完成则任务进度+1
                    if (!armQue.isNotExtendQue()) {
                        taskDataManager.updTask(player, TaskType.COND_ARM_CNT, 1,
                                BuildingType.getResourceByBuildingType(type));
                    }
                    addNum += armQue.getAddArm();
                    it.remove();
                }
            }
            AddArmRs.Builder builder = AddArmRs.newBuilder();
            if (addNum > 0) {
                rewardDataManager.addAward(player, AwardType.ARMY, BuildingType.getResourceByBuildingType(type), addNum,
                        AwardFrom.GAIN_ARM);
                // 更新世界争霸 招募兵力数量
                worldWarSeasonDailyRestrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_RECRUIT_ARMS_CNT, addNum);
                // 招募兵力xx
                royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_RECRUIT_ARMS_CNT, addNum);
                // 更新募兵数量
                activityDataManager.updActivity(player, ActivityConst.ACT_TRAINED_SOLDIERS, addNum, BuildingType.getResourceByBuildingType(type), true);
                // 更新募兵数量
                activityDataManager.updActivity(player, ActivityConst.ACT_TRAINED_SOLDIERS_DAILY, addNum, BuildingType.getResourceByBuildingType(type), true);

                // 上报玩家兵力变化信息
                LogLordHelper.playerArm(
                        AwardFrom.GAIN_ARM,
                        player,
                        BuildingType.getResourceByBuildingType(type),
                        Constant.ACTION_ADD,
                        addNum
                );
            }
            builder.setAddNum(addNum);
            builder.setId(id);
            // 触发一次自动补兵
            playerDataManager.autoAddArmy(player, false);
            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(AwardType.ARMY, BuildingType.getResourceByBuildingType(type));
            rewardDataManager.syncRoleResChanged(player, change);

            taskDataManager.updTask(player, TaskType.COND_ARM_TYPE_CNT, addNum, BuildingType.getResourceByBuildingType(type));
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_ARM_TYPE_CNT, addNum);
            battlePassDataManager.updTaskSchedule(roleId, TaskType.COND_ARM_TYPE_CNT, addNum, BuildingType.getResourceByBuildingType(type));

            //貂蝉任务-造兵
            ActivityDiaoChanService.completeTask(player, ETask.MAKE_ARMY,BuildingType.getResourceByBuildingType(type),addNum);
            //喜悦金秋-日出而作- 训练xx任意等级士兵
            TaskService.processTask(player, ETask.MAKE_ARMY,BuildingType.getResourceByBuildingType(type),addNum);

            TaskService.handleTask(player,ETask.MAKE_ARMY,BuildingType.getResourceByBuildingType(type),addNum);
            TaskService.handleTask(player,ETask.ARMY_MAK_LOST,addNum);
            ActivityDiaoChanService.completeTask(player,ETask.ARMY_MAK_LOST,addNum);
            TaskService.processTask(player,ETask.ARMY_MAK_LOST,addNum);

            playerDataManager.createRoleOpt(player, Constant.OptId.id_3, id + "");
            // // 移除推送记录
            // String pushId = PushConstant.PRODUCT_ARMY_COMPLETE + "_" + id;
            // player.removePushRecord(pushId);

            // 领取后判断第二个开启了
            if (!factory.getAddList().isEmpty()) {
                taskDataManager.updTask(player, TaskType.COND_ARM_CNT, 1, BuildingType.getResourceByBuildingType(type));
            }
            return builder.build();
        }
        return null;
    }

    /**
     * 募兵加时
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public UpRecruitRs getUpRecruitRs(long roleId, int id) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            int buildingLv = BuildingDataManager.getBuildingLv(id, player);
            if (buildingLv < 1) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "兵营招募加时，建筑还未建造, roleId:" + roleId,
                        ", buildingId:", id);
            }
            Factory factory = createFactoryIfNotExist(player, id);
            StaticFactoryRecruit staticFactoryRecruit = StaticBuildingDataMgr.getStaticFactoryRecruit(id,
                    factory.getFctLv() + 1);
            if (staticFactoryRecruit == null) {
                throw new MwException(GameError.UP_RECRUIT_MAX.getCode(), "募兵加时时，已达最高级别, roleId:" + roleId);
            }

            rewardDataManager.checkPlayerResIsEnough(player, staticFactoryRecruit.getCost(), "兵营招募加时");
            rewardDataManager.subPlayerResHasChecked(player, staticFactoryRecruit.getCost(), true,
                    AwardFrom.FACTORYRECRUIT_LV);

            factory.setFctLv(factory.getFctLv() + 1);
            UpRecruitRs.Builder builder = UpRecruitRs.newBuilder();
            builder.setAddNum(getAddNum(player, staticFactoryRecruit, id));
            builder.setId(id);
            builder.setLv(factory.getFctLv());
            taskDataManager.updTask(player,TaskType.COND_508,1);
            return builder.build();
        }
        return null;
    }

    /**
     * 兵营扩建
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public FactoryExpandRs getFactoryExpandRs(long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int buildingLv = BuildingDataManager.getBuildingLv(id, player);
        if (buildingLv < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "兵营扩建是，建筑还未建造, roleId:" + roleId, ", buildingId:",
                    id);
        }
        Factory factory = createFactoryIfNotExist(player, id);
        StaticFactoryExpand staticFactoryExpand = StaticBuildingDataMgr.getStaticFactoryExpand(id,
                factory.getFctExpLv() + 1);
        if (staticFactoryExpand == null) {
            throw new MwException(GameError.EXPAND_MAX.getCode(), "兵营扩建时，已达最高级别, roleId:" + roleId);
        }

        rewardDataManager.checkAndSubPlayerRes4List(player, staticFactoryExpand.getCost(),
                AwardFrom.FACTORYRECRUIT_EXPAND, id);// 兵营扩建

        factory.setFctExpLv(factory.getFctExpLv() + 1);
        FactoryExpandRs.Builder builder = FactoryExpandRs.newBuilder();
        builder.setId(id);
        builder.setExpandLv(factory.getFctExpLv());
        if (BuildingDataManager.isFactoryId(id)) {
            builder.addAllAward(
                    rewardDataManager.sendReward(player, staticFactoryExpand.getGain(), AwardFrom.FACTORY_EXPAND));
        }
        return builder.build();
    }

    /**
     * 获取兵营,如果不存在就初始化
     *
     * @param player
     * @param id
     * @return
     */
    public Factory createFactoryIfNotExist(Player player, int id) {
        Factory factory = player.factory.get(id);
        if (factory == null) {
            factory = new Factory();
            factory.setFctLv(Constant.FACTORY_TIME_NINT_LEVL);
            factory.setFctExpLv(Constant.FACTORY_EXPAND_NINT_LEVL);
            player.factory.put(id, factory);
        }
        return factory;
    }

    /**
     * 招募取消
     *
     * @param roleId
     * @return
     */
    public RecruitCancelRs getRecruitCancelRs(long roleId, int id, int endTime) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        int buildingLv = BuildingDataManager.getBuildingLv(id, player);
        if (buildingLv < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "兵营招募取消，建筑还未建造, roleId:" + roleId, ", buildingId:",
                    id);
        }
        Factory factory = player.factory.get(id);
        if (factory == null || factory.getAddList().isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "招募取消时，无募兵队列, roleId:" + roleId);
        }
        Iterator<ArmQue> it = factory.getAddList().iterator();
        int tmpEntTime = 0;
        int tmpPreEntTime = 0;
        int index = 0;
        int foodBack = 0;
        int oilBack = 0;
        while (it.hasNext()) {
            ArmQue armQue = it.next();
            if (endTime == armQue.getEndTime()) {
                if (index == 0) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "招募取消时，队列传入错误, roleId:" + roleId);
                }
                tmpEntTime = armQue.getEndTime();
                foodBack += armQue.getNeedFood();
                oilBack += armQue.getNeedOIL();
                it.remove();
                continue;
            }
            if (tmpEntTime != 0 && armQue.getEndTime() > tmpEntTime) {
                armQue.setEndTime(armQue.getEndTime() - tmpEntTime + tmpPreEntTime);
            } else {
                tmpPreEntTime = armQue.getEndTime();
            }
            index++;
        }

        // LinkedList<ArmQue> list = factory.getAddList();
        // ArmQue que = null;
        // for (int i = 0; i < list.size(); i++) {
        // que = list.get(i);
        // if (tmpEntTime != 0 && que.getEndTime() > tmpEntTime) {
        // que.setEndTime(que.getEndTime() - tmpEntTime + tmpPreEntTime);
        // } else {
        // tmpPreEntTime = que.getEndTime();
        // }
        // list.set(i, que);
        // }

        // 取消返还补给
        if (foodBack > 0) {
            LogUtil.debug("招募取消返还补给role=" + roleId + ",food=" + foodBack);
            rewardDataManager.addAward(player, AwardType.RESOURCE, AwardType.Resource.FOOD, foodBack,
                    AwardFrom.RECRUIT_CANCEL);
            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
            rewardDataManager.syncRoleResChanged(player, change);
        }

        // 取消返还燃油
        if (oilBack > 0) {
            LogUtil.debug("招募取消返还燃油role=" + roleId + ",oil=" + oilBack);
            rewardDataManager.addAward(player, AwardType.RESOURCE, AwardType.Resource.OIL, oilBack,
                    AwardFrom.RECRUIT_CANCEL);
            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.OIL);
            rewardDataManager.syncRoleResChanged(player, change);
        }

        RecruitCancelRs.Builder builder = RecruitCancelRs.newBuilder();
        builder.setId(id);
        for (ArmQue armQue : factory.getAddList()) {
            builder.addArmQue(PbHelper.createArmQuePb(armQue));
        }
        return builder.build();
    }

    /**
     * 招募加速
     *
     * @param roleId
     * @return
     */
    public RecruitSpeedRs getRecruitSpeedRs(long roleId, int id, int endTime, int itemId, boolean isGoldSpeed,int itemNum)
            throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        int buildingLv = BuildingDataManager.getBuildingLv(id, player);
        if (buildingLv < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "兵营招募加速时，建筑还未建造, roleId:" + roleId, ", buildingId:",
                    id);
        }
        Factory factory = player.factory.get(id);
        if (factory == null || factory.getAddList().isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "招募加速时，无募兵队列, roleId:" + roleId);
        }
        if (itemId != PropConstant.ITEM_ID_REDUCE_TIME_1 && itemId != PropConstant.ITEM_ID_REDUCE_TIME_2
                && itemId != PropConstant.ITEM_ID_REDUCE_TIME_3 && itemId != -1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "招募加速时，加速道具ID错误, roleId:" + roleId + ", itemId=" + itemId);
        }
        if(itemNum <= 0) itemNum = 1;
        ArmQue que = factory.getAddList().get(0);
        if (que == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "招募加速时，无募兵队列, roleId:" + roleId);
        }

        int now = TimeHelper.getCurrentSecond();

        int second = que.getEndTime() - now;
        if (second <= 0) {
            throw new MwException(GameError.RECRUIT_SPEED.getCode(), "招募加速时，无募兵队列, roleId:" + roleId);
        }
        if (itemId == -1) {// 金币加速
            int needGold = (int) Math.ceil(second * 1.00 / 60);
            // TODO: 2021/11/8 注意！！！ 这里的消耗系数是服务器写死的. 海外版本是 * 8, 国内版本是 * 2
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold * 2,
                    AwardFrom.RECRUIT_SPEED);
        } else {
            StaticProp prop = StaticPropDataMgr.getPropMap(itemId);
            if(Objects.isNull(prop))
                throw new MwException(GameError.NO_CONFIG.getCode(),"招募加速，使用道具加速错误，道具不存在, itemId=" + itemId);
            //计算实际需要几个道具
//            int a_ = prop.getDuration() / second;
//            int b_ = prop.getDuration() % second;
//            itemNum = a_ + (b_>0?1:0);
            if (isGoldSpeed) {
                if (prop.getPrice() <= 0) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "建筑加速时，价格错, roleId:" + roleId);
                }
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                        prop.getPrice() * itemNum, AwardFrom.RECRUIT_SPEED, id);
            } else {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, itemId, itemNum,
                        AwardFrom.RECRUIT_SPEED, id);
            }
            second = Math.min(prop.getDuration() * itemNum, second); // 道具具体加速
        }

        Iterator<ArmQue> it = factory.getAddList().iterator();

        while (it.hasNext()) {
            ArmQue armQue = it.next();
            armQue.setEndTime(armQue.getEndTime() - second);
        }
        // 客户端时间不一致,提前完成
        if (now >= que.getEndTime()) {
            que.setEndTime(que.getEndTime() - 5);
        }

        RecruitSpeedRs.Builder builder = RecruitSpeedRs.newBuilder();
        builder.setId(id);
        for (ArmQue armQue : factory.getAddList()) {
            builder.addArmQue(PbHelper.createArmQuePb(armQue));
        }
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    public void processArmyQue(Player player, int id, int add) throws MwException {
        long roleId = player.lord.getLordId();
        Factory factory = player.factory.get(id);
        if (factory == null || factory.getAddList().isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "招募加速时，无募兵队列, roleId:" + roleId);
        }
        ArmQue que = factory.getAddList().get(0);
        int now = TimeHelper.getCurrentSecond();
        int second = que.getEndTime() - now;
        if (second <= 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "招募加速时，无募兵队列, roleId:" + roleId);
        }
        second = Math.min(add, second); // 道具具体加速

        Iterator<ArmQue> it = factory.getAddList().iterator();

        while (it.hasNext()) {
            ArmQue armQue = it.next();
            armQue.setEndTime(armQue.getEndTime() - second);
            armQue.clearFree();
        }
    }

    /**
     * 快速买兵
     * @param roleId 玩家唯一id
     * @throws MwException 游戏自定义异常
     * @return 当前买兵的次数
     */
    public QuickBuyArmyRs quickBuyArmy(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int buyCnt = player.getMixtureDataById(PlayerConstant.DAILY_QUICK_BUY_ARMY);
        if (buyCnt + 1 > WorldConstant.QUICK_BUY_ARMY_MAX_CNT) {
            throw new MwException(GameError.QUICK_BUY_ARMY_MAX_CNT.getCode(), "快速买兵，当天已达次数上限, roleId:" + roleId);
        }
        // 各个兵营的补兵数量
        Map<Integer, Integer> factoryArmyNum = Stream.of(BuildingType.FACTORY_1, BuildingType.FACTORY_2, BuildingType.FACTORY_3)
                // 只算已解锁的
                .filter(buildingId -> buildingDataManager.checkBuildingLock(player, buildingId) && player.building.getFactoryLvByBuildingId(buildingId) > 0)
                .collect(Collectors.toMap(Integer::intValue, buildingId -> {
                    Factory factory = createFactoryIfNotExist(player, buildingId);
                    BuildingExt buildingExt = player.buildingExts.get(buildingId);
                    if (CheckNull.isNull(buildingExt)) {
                        return 0;
                    }
                    // 兵种上限
                    try {
                        int allArmNum = getAllArmNum(player, buildingId, factory, buildingExt);
                        if (allArmNum > 0) {
                            return (int) (allArmNum * (WorldConstant.QUICK_BUY_ARMY_COEF / Constant.TEN_THROUSAND));
                        }
                    } catch (MwException e) {
                        LogUtil.error(e);
                    }
                    return 0;
                }, (oldV, newV) -> newV));
        // 总共的补兵数量
        int sumArmy = factoryArmyNum.values().stream()
                .mapToInt(Integer::intValue).sum();
        // 根据次数来取消耗配置
        List<Integer> costConf = WorldConstant.QUICK_BUY_ARMY_PRICE.get(buyCnt);
        if (CheckNull.isEmpty(costConf) || costConf.size() < 2) {
            throw new MwException(GameError.QUICK_BUY_ARMY_CONFIG_ERROR.getCode(), "快速买兵，找不到对应的购买次数的配置，或者配置有误, buyCnt: ", buyCnt);
        }
        // 需要消耗的钻石数量
        int needGold = (int) Math.floor(sumArmy * 1.0 / costConf.get(0) * costConf.get(1));
        // 扣除需要消耗的钻石，并同步
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                needGold, AwardFrom.QUICK_BUY_ARMY_COST);
        // 同步资源的变动
        List<CommonPb.Award> showAward = new ArrayList<>();
        factoryArmyNum.forEach((key, value) -> {
            int buildingId = key;
            int addNum = value;
            BuildingExt ext = player.buildingExts.get(buildingId);
            if (!CheckNull.isNull(ext)) {
                int type = ext.getType();
                int resource = BuildingType.getResourceByBuildingType(type);
                showAward.add(rewardDataManager.addAwardSignle(player, AwardType.ARMY, resource, addNum,
                        AwardFrom.QUICK_BUY_ARMY_AWARD));
            }
        });
        // 购买次数+1
        buyCnt = buyCnt + 1;
        player.setMixtureData(PlayerConstant.DAILY_QUICK_BUY_ARMY, buyCnt);
        QuickBuyArmyRs.Builder builder = QuickBuyArmyRs.newBuilder();
        builder.setQuickBuyArmyCnt(buyCnt);
        builder.addAllAward(showAward);
        return builder.build();
    }

    /**
     * 根据TaskType COND_511的类型拿到建筑的容量
     * 508类型；3建筑：1兵营、2马厩、3靶场；250000容量
     */
    public int getAddNumByCondId(Player player, int type) {
        if (Objects.isNull(player)) return 0;
        int buildingId = 0;
        switch (type) {
            case 1:
                buildingId = BuildingType.FACTORY_1;
                break;
            case 2:
                buildingId = BuildingType.FACTORY_2;
                break;
            case 3:
                buildingId = BuildingType.FACTORY_3;
                break;
            default:
                return 0;
        }
        Factory factory = createFactoryIfNotExist(player, buildingId);
        if (Objects.isNull(factory)) return 0;
        StaticFactoryRecruit staticFactoryRecruit = StaticBuildingDataMgr.getStaticFactoryRecruit(buildingId, factory.getFctLv());
        if (Objects.isNull(staticFactoryRecruit)) return 0;
        return getAddNum(player, staticFactoryRecruit, buildingId);
    }
}
