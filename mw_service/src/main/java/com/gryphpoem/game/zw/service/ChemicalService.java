package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.BattlePassDataManager;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.ChemicalQue;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.pb.GamePb1.ChemicalExpandRs;
import com.gryphpoem.game.zw.pb.GamePb1.ChemicalFinishRs;
import com.gryphpoem.game.zw.pb.GamePb1.ChemicalRecruitRs;
import com.gryphpoem.game.zw.pb.GamePb1.GetChemicalRs;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.SeasonConst;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.TechConstant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Chemical;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticChemical;
import com.gryphpoem.game.zw.resource.domain.s.StaticChemicalExpand;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.BuildingState;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 渡口
 *
 * @author tyler
 */
@Service
public class ChemicalService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;
    @Autowired
    private WorldScheduleService worldScheduleService;

    /**
     * 获取渡口信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetChemicalRs getChemical(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.building.getFerry() < 1) {
            throw new MwException(GameError.BUILDING_NOT_CREATE.getCode(), "roleId:", roleId, "渡口还未建造");
        }
        GetChemicalRs.Builder builder = GetChemicalRs.newBuilder();
        Chemical chemical = player.chemical;
        if (chemical == null) {
            chemical = new Chemical();
            player.chemical = chemical;
        }
        if (chemical.getExpandLv() == 0) {
            chemical.setExpandLv(1);
        }
        buildingDataManager.addHumanPerSecond(player);
        // 重新分配队列
        if (!chemical.getPosQue().isEmpty()) {
            // 重新计算队列
            int now = TimeHelper.getCurrentSecond();
            int queCnt = 0;
            for (Entry<String, ChemicalQue> kv : chemical.getPosQue().entrySet()) {
                if (!kv.getValue().getClonePos() && kv.getValue().getEndTime() > now) {
                    queCnt++;
                }
            }
            if (queCnt > 0) {
                int countryPeople = worldDataManager.getCountryPeople(player.lord.getCamp());
                int human = (int) player.resource.getHuman();
                human = human / queCnt;
                LogUtil.debug("剩余渡口队列=" + queCnt);
                processQue(chemical, roleId, human, countryPeople, player);
            }
        }

        if (chemical.getPosQue() != null) {
            for (Entry<String, ChemicalQue> que : chemical.getPosQue().entrySet()) {
                builder.addQue(que.getValue());
            }
        }
        builder.setExpandLv(chemical.getExpandLv());
        builder.setMyNum(player.resource.getHuman());
        builder.setCountryNum(worldDataManager.getCountryPeople(player.lord.getCamp()));
        builder.setMaxPos(getMaxPos(player, StaticBuildingDataMgr.getChemicalExpandMap(chemical.getExpandLv())));
        return builder.build();
    }

    /**
     * 渡口生产
     *
     * @param roleId
     * @param pos
     * @param id
     * @return
     * @throws MwException
     */
    public ChemicalRecruitRs chemicalRecruit(long roleId, int pos, int id, int itemId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        buildingDataManager.checkBuildingIsCreate(BuildingType.FERRY, player);
        // 检查建筑是否在升级
        if (buildingDataManager.checkBuildIsUpping(player, BuildingType.FERRY)) {
            throw new MwException(GameError.BUILD_IS_UPPING.getCode(), "roleId:", roleId, " 当前建筑正在升级");
        }
        Chemical chemical = player.chemical;
        if (chemical == null) {
            chemical = new Chemical();
            player.chemical = chemical;
        }

        StaticChemical staticChemical = StaticBuildingDataMgr.getChemicalMap(id);
        if (staticChemical == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "渡口生产时，找不到配置, roleId:" + roleId + ",id=" + id);
        }

        // 判断队列大小
        StaticChemicalExpand chemicalExpand = StaticBuildingDataMgr.getChemicalExpandMap(chemical.getExpandLv());
        // 队列总数 = 扩展数 + 渡口等级 + 赛季天赋优化加成
        int maxPos = getMaxPos(player, chemicalExpand);
        if (pos <= 0 || pos > maxPos) {
            throw new MwException(GameError.CHEMICAL_ON_TIME.getCode(),
                    "渡口生产时,队列已满, roleId:" + roleId + ",pos=" + pos);
        }

        if (chemical.getPosQue().get(pos + "_") != null && chemical.getPosQue().get(pos + "") != null) {
            throw new MwException(GameError.CHEMICAL_ON_TIME.getCode(),
                    "渡口生产时,正在生产中, roleId:" + roleId + ",pos=" + pos);
        }

        //打造红装检查世界进程
        int currSchedule = worldScheduleService.getCurrentSchduleId();
        if (staticChemical.getId() == 4 && currSchedule < Constant.DOCK_RED_MATERIAL_SCHEDULE) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "船坞生产红色材料世界进程不符合", id, currSchedule));
        }

        // 检查材料够不
        rewardDataManager.checkPlayerResIsEnough(player, staticChemical.getCost(), "渡口生产时");

        List<CommonPb.Award> awards = rewardDataManager.getRandomAward(player, staticChemical.getReward(), 1);
        if (awards == null || awards.get(0).getId() <= 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "渡口生产时，找不到配置, roleId:" + roleId + ",id=" + id + ",rewardId=" + staticChemical.getReward());
        }
        CommonPb.Award award = awards.get(0);

        List<List<Integer>> list = staticChemical.getCostItem();
        boolean hasItem = false;
        if (list != null && list.size() > 0) {
            for (List<Integer> needItem : list) {
                if (needItem.get(1) == itemId) {
                    rewardDataManager.checkPropIsEnough(player, needItem.get(1), needItem.get(2), "渡口生产");
                    rewardDataManager.subProp(player, needItem.get(1), needItem.get(2), AwardFrom.CHEMICAL_RECRUIT);
                    hasItem = true;
                    break;
                }
            }
        }
        if (!hasItem) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(),
                    "渡口生产时，资源不足, roleId:" + roleId + ",id=" + id);
        }

        rewardDataManager.checkAndSubPlayerRes(player, staticChemical.getCost(), AwardFrom.CHEMICAL_RECRUIT);

        ChemicalRecruitRs.Builder builder = ChemicalRecruitRs.newBuilder();
        // int human = (int) player.resource.getHuman();
        BuildingState buildingState = player.getBuildingData().get(BuildingType.FERRY);
        int human = 0;
        if (buildingState != null) {
            human = buildingState.getResidentCnt(); // 人口
        }
        if (human == 0) {
            throw new MwException(GameError.PARAM_ERROR, "渡口生产订单时, 当前渡口没有居民, 无法生产, roleId:%s", roleId);
        }
        int now = TimeHelper.getCurrentSecond();
        int queCnt = 0;
        for (Entry<String, ChemicalQue> kv : chemical.getPosQue().entrySet()) {
            if (!kv.getValue().getClonePos() && kv.getValue().getEndTime() > now) {
                queCnt++;
            }
        }
        int propId = award.getId();
        StaticProp sProp = StaticPropDataMgr.getPropMap(propId);
        if (!CheckNull.isNull(sProp)) {
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_CHEMICAL_RECRUIT_CNT, sProp.getQuality());
            // 装备物资
            activityDataManager.updActivity(player, ActivityConst.ACT_EQUIP_MATERIAL, award.getCount(), sProp.getQuality(), true);

            //貂蝉任务-船坞进行X次X品质交易
            ActivityDiaoChanService.completeTask(player, ETask.TRADE_TIMES, sProp.getQuality());
            TaskService.processTask(player, ETask.TRADE_TIMES, sProp.getQuality());
        }

        // 人口平分队列， 减少时间公式=(需要时间/ ( 人口 / (队列+1) ) )
        // int period = 0;
        int countryPeople = worldDataManager.getCountryPeople(player.lord.getCamp());
        if (human == 0 || queCnt == 0) {
            addQue(chemical, staticChemical, human, id, pos, award, countryPeople, player);
        } else if (chemical.getPosQue().get(pos + "") != null) {
            // human = chemical.getPosQue().get(pos + "").getPeople();
            addQue(chemical, staticChemical, human, id, pos, award, countryPeople, player);
        } else {
            // 所有时间重新计算
            // human = human / (queCnt + 1);
            processQue(chemical, roleId, human, countryPeople, player);
            // 增加队列
            addQue(chemical, staticChemical, human, id, pos, award, countryPeople, player);
        }

        if (chemical.getPosQue() != null) {
            for (Entry<String, ChemicalQue> que : chemical.getPosQue().entrySet()) {
                builder.addQue(que.getValue());
            }
        }
        builder.setGold(player.lord.getGold());
        builder.setResource(PbHelper.createCombatPb(player.resource));
        taskDataManager.updTask(player, TaskType.COND_CHEMICAL, 1);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_CHEMICAL, 1);
        return builder.build();
    }

    /**
     * 获得赛季天赋加成的最大孔位
     *
     * @param player
     * @param chemicalExpand
     * @return
     */
    private int getMaxPos(Player player, StaticChemicalExpand chemicalExpand) {
        StaticBuildingInit sBuildingInit = StaticBuildingDataMgr.getBuildingInitMapById(BuildingType.FERRY);
        int maxNum = StaticBuildingDataMgr.getMaxChemicalNum() + sBuildingInit.getMaxLv();
        int seasonTalentBuff = chemicalExpand.getNum() + player.building.getFerry() + DataResource.getBean(SeasonTalentService.class).
                getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_618);
        return seasonTalentBuff >= maxNum ? maxNum : seasonTalentBuff;
    }

    private void processQue(Chemical chemical, long roleId, int human, int countryPeople, Player player) {
        ChemicalQue q = null;
        StaticChemical oldStaticChemical = null;
        int num = activityDataManager.getActProductionNum(player);
        int period = 0;
        // 先计算正在生产的队列,再计算预备队列
        int queNum = (int) chemical.getPosQue().values().stream().filter(p -> !p.getClonePos()).count();
        for (Entry<String, ChemicalQue> que : chemical.getPosQue().entrySet()) {
            q = que.getValue();
            if (q.getClonePos()) {
                continue;
            }
            // int add = isAdd ? (human - q.getPeople()) : (q.getPeople() - human);
            int add = Math.abs(human - q.getPeople());
            // if (add == 0) {
            // continue;
            // }
            oldStaticChemical = StaticBuildingDataMgr.getChemicalMap(q.getSid());
            LogUtil.debug("渡口队列材料id=" + q.getSid() + "," + oldStaticChemical);
            // period = add * 6 * q.getId();
            // period = (oldStaticChemical.getTime() / (add + countryPeople)) * 3600;
            // period = getPeriod(oldStaticChemical, add, countryPeople);
            // chemical.getPosQue().put(que.getKey(),
            // makeChemicalQue(q.getId(), q.getPos(), period, q.getEndTime() + period - q.getPeriod(), human,
            // q.getClonePos(), q.getSid(), q.getCount(), q.getStartTime()));
            // LogUtil.debug(roleId + ",渡口招募增减时间=" + add + ",新参加人数=" + human + ",原人数=" + q.getPeople() + ",新时间=" +
            // period
            // + ",原时间=" + q.getPeriod() + ",结束时间=" + (q.getEndTime() + period - q.getPeriod()));

            // 新的重新计算
            // period = getPeriod(oldStaticChemical, human, countryPeople, player);
            period = getPeriod(oldStaticChemical, player, queNum);
            LogUtil.debug("渡口时间生产加速前的时间= " + period);
            period *= num / Constant.TEN_THROUSAND;
            LogUtil.debug("渡口时间生产加速后的时间= " + period + "加速比例万分之:" + num);
            int newEndTime = q.getStartTime() + period;
            chemical.getPosQue().put(que.getKey(), makeChemicalQue(q.getId(), q.getPos(), period, newEndTime, human,
                    q.getClonePos(), q.getSid(), q.getCount(), q.getStartTime()));

            LogUtil.debug("roleId: ", roleId, ",渡口时间重新调整 ,新的人数=", human, ",原人数=", q.getPeople(), ",新时间=", period,
                    ",原时间=", q.getPeriod(), ", 新结束时间=", newEndTime, ", 原来结束时间=", q.getEndTime(), " 开始时间=",
                    q.getStartTime());
        }

        // 重新计算预备队列
        queNum = (int) chemical.getPosQue().values().stream().filter(ChemicalQue::getClonePos).count();
        for (Entry<String, ChemicalQue> que : chemical.getPosQue().entrySet()) {
            q = que.getValue();
            if (!q.getClonePos()) {
                continue;
            }
            int add = Math.abs(human - q.getPeople());
            /*if (add == 0) {
                continue;
            }*/
            oldStaticChemical = StaticBuildingDataMgr.getChemicalMap(q.getSid());
            LogUtil.debug("渡口队列材料id=" + q.getSid() + "," + oldStaticChemical);
            // 新的重新计算
            // period = getPeriod(oldStaticChemical, human, countryPeople, player);
            period = getPeriod(oldStaticChemical, player, queNum);
            LogUtil.debug("预备队列渡口时间= " + period);
            period *= num / Constant.TEN_THROUSAND;
            LogUtil.debug("预备队列渡口时间= " + period + "加速比例万分之:" + num);
            ChemicalQue chemicalQue = chemical.getPosQue().get(q.getPos());// 主队列
            if (chemicalQue == null) continue;

            chemical.getPosQue().put(que.getKey(),
                    makeChemicalQue(q.getId(), q.getPos(), period, chemicalQue.getEndTime() + period, human,
                            q.getClonePos(), q.getSid(), q.getCount(), chemicalQue.getEndTime()));
            LogUtil.debug("roleId: ", roleId, ",预备队列渡口时间重新调整 ,新的人数=", human, ",原人数=", q.getPeople(), ",新时间=", period,
                    ",原时间=", q.getPeriod(), ", 新开始时间=", chemicalQue.getEndTime() + period, ", 原来开始时间=",
                    q.getStartTime());
        }

    }

    // 生产蓝色材料时间（单位/秒） =（1+11*2650/(2650+人口））*3600 向上取整
    // 生产紫色材料时间（单位/秒） =（2+10*8450/（8450+人口））*3600 向上取整
    // 生产橙色材料时间（单位/秒） =（3+9*10900/（10900+人口））*3600 向上取整
    private void addQue(Chemical chemical, StaticChemical staticChemical, int human, int id, int pos,
                        CommonPb.Award award, int countryPeoPle, Player player) {
        int propId = award.getId();
        int count = award.getCount();
        int now = TimeHelper.getCurrentSecond();
        int num = activityDataManager.getActProductionNum(player);
        ChemicalQue.Builder newQue = ChemicalQue.newBuilder();
        // int period = (staticChemical.getTime() - human * 6) * id;
        // int period = (staticChemical.getTime() / ((human == 0 ? 1 : human) + countryPeoPle)) * 3600;
        int queNum = (int) chemical.getPosQue().values().stream().filter(tmp -> !tmp.getClonePos()).count();
        // int period = getPeriod(staticChemical, (human == 0 ? 1 : human), countryPeoPle, player);
        int period = getPeriod(staticChemical, player, queNum);
        LogUtil.debug("生产加速前的时间= " + period);
        period *= num / Constant.TEN_THROUSAND;
        LogUtil.debug("生产加速后的时间= " + period + "加速比例万分之:" + num);
        if (chemical.getPosQue().get(pos + "") == null) {
            newQue.setPos(pos).setId(propId).setCount(count).setPeople(human).setPeriod(period).setEndTime(now + period)
                    .setClonePos(false).setSid(id).setStartTime(now).build();
            chemical.getPosQue().put(pos + "", newQue.build());
        } else {
            int startTime = now > chemical.getPosQue().get(pos + "").getEndTime() ? now
                    : chemical.getPosQue().get(pos + "").getEndTime();
            newQue.setPos(pos).setId(propId).setCount(count).setPeople(human).setPeriod(period)
                    .setEndTime(startTime + period).setClonePos(true).setSid(id).setStartTime(startTime).build();
            chemical.getPosQue().put(pos + "_", newQue.build());
        }
        LogUtil.debug("渡口招募 新参加人数=" + human + ",新时间=" + period + ",是否预定位置=" + newQue.getClonePos() + ",结束时间="
                + newQue.getEndTime());
    }

    private int getPeriod(StaticChemical staticChemical, int human, int countryPeoPle, Player player) {
        double period = 0;
        //化工 科技加成  获取加成比例
        double proportion = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_22);
        //赛季天赋
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_403) / Constant.TEN_THROUSAND;
        if (staticChemical.getId() == 1) {
            period = (1 + 11 * 2650.0f / (2650 + human + countryPeoPle)) * 3600f * (1.0 - proportion - seasonTalentEffect);
        } else if (staticChemical.getId() == 2) {
            period = (2 + 10 * 8450.0f / (8450 + human + countryPeoPle)) * 3600f * (1.0 - proportion - seasonTalentEffect);
        } else if (staticChemical.getId() == 3) {
            period = (3 + 9 * 10900.0f / (10900 + human + countryPeoPle)) * 3600f * (1.0 - proportion - seasonTalentEffect);
        } else if (staticChemical.getId() == 4) {
            period = (12 + 24 * 13350.0f / (13350 + human + countryPeoPle)) * 3600f * (1.0 - proportion - seasonTalentEffect);
        } else {
            period = (staticChemical.getTime() / human + countryPeoPle) * 3600f * (1.0 - proportion - seasonTalentEffect);
        }
        return (int) period;
    }

    /**
     * 三国3 新公式计算<br>
     * 生产蓝色材料时间（单位/秒） = 8 * 3600 * (1 - 科技减免) * (1 - 农民数量 * 单个农民减免万分比)<br>
     * 生产紫色材料时间（单位/秒） = 10 * 3600 * (1 - 科技减免) * (1 - 农民数量 * 单个农民减免万分比)<br>
     * 生产橙色材料时间（单位/秒） = 12 * 3600 * (1 - 科技减免) * (1 - 农民数量 * 单个农民减免万分比)<br>
     * 生产红色材料时间（单位/秒） = 36 * 3600 * (1 - 科技减免) * (1 - 农民数量 * 单个农民减免万分比)<br>
     *
     * @param staticChemical
     * @param player
     * @return
     */
    public int getPeriod(StaticChemical staticChemical, Player player, int queNum) {
        BuildingState buildingState = player.getBuildingData().get(BuildingType.FERRY);
        int residentCnt = 0;
        if (buildingState != null) {
            residentCnt = buildingState.getResidentCnt(); // 人口
        }

        double period = 0;
        // 化工 科技加成  获取加成比例
        double proportion = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_22);
        // 赛季天赋
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_403) / Constant.TEN_THROUSAND;
        // 居民加成
        double residentEffect = residentCnt * Constant.SINGLE_RESIDENT_REDUCE_WHARF_PRODUCT_TIME_COEFFICIENT / Constant.TEN_THROUSAND;

        if (staticChemical.getId() == 1) {
            period = 8 * 3600 * 3600f * (1.0 - proportion - seasonTalentEffect) * (1.0 - residentEffect);
        } else if (staticChemical.getId() == 2) {
            period = 10 * 3600 * 3600f * (1.0 - proportion - seasonTalentEffect) * (1.0 - residentEffect);
        } else if (staticChemical.getId() == 3) {
            period = 12 * 3600 * 3600f * (1.0 - proportion - seasonTalentEffect) * (1.0 - residentEffect);
        } else if (staticChemical.getId() == 4) {
            period = 36 * 3600 * 3600f * (1.0 - proportion - seasonTalentEffect) * (1.0 - residentEffect);
        } else {
            period = (1.0 * staticChemical.getTime() / queNum) * 3600f * (1.0 - proportion - seasonTalentEffect) * (1.0 - residentEffect);
        }
        return (int) period;
    }

    /**
     * 更新渡口的生产时间<br>
     * 渡口的时间更新, 以余下的时间为基础进行计算
     *
     * @param player
     */
    public void updateFerryProductTime(Player player, int effect) {
        int now = TimeHelper.getCurrentSecond();
        for (ChemicalQue que : player.chemical.getPosQue().values()) {
            int startTime = que.getStartTime();
            int endTime = que.getEndTime();
            if (endTime < now) {
                continue;
            }
            int remainingTime = new Double(Math.ceil((endTime - now) * (1 - effect / Constant.TEN_THROUSAND))).intValue();
            endTime = now + remainingTime;
            ChemicalQue.Builder queBuilder = que.toBuilder()
                    .setPeriod(endTime - startTime)
                    .setEndTime(endTime);
            player.chemical.getPosQue().put(que.getPos() + "", queBuilder.build());
        }
        synFerryQueInfo(player);
    }

    /**
     * 向客户端同步渡口队列信息
     *
     * @param player
     */
    private void synFerryQueInfo(Player player) {
        GamePb1.SynFerryQueInfoRs.Builder builder = GamePb1.SynFerryQueInfoRs.newBuilder();
        builder.addAllQueue(player.chemical.getPosQue().values());
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynFerryQueInfoRs.EXT_FIELD_NUMBER, GamePb1.SynFerryQueInfoRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 扩建
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public ChemicalExpandRs chemicalExpand(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        buildingDataManager.checkBuildingIsCreate(BuildingType.FERRY, player);
        Chemical chemical = player.chemical;
        if (chemical == null) {
            chemical = new Chemical();
            player.chemical = chemical;
        }

        StaticChemicalExpand chemicalExpand = StaticBuildingDataMgr.getChemicalExpandMap(chemical.getExpandLv() + 1);
        if (chemicalExpand == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "渡口扩建时，找不到配置, roleId:" + roleId + ",lv=" + chemical.getExpandLv());
        }
        rewardDataManager.checkAndSubPlayerRes(player, chemicalExpand.getCost(), AwardFrom.CHEMICAL_EXPAND);
        chemical.setExpandLv(chemical.getExpandLv() + 1);

        ChemicalExpandRs.Builder builder = ChemicalExpandRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setExpandLv(chemical.getExpandLv());
        builder.setMaxPos(getMaxPos(player, StaticBuildingDataMgr.getChemicalExpandMap(chemical.getExpandLv())));
        return builder.build();
    }

    /**
     * 渡口推送 定时器
     */
    public void chemicalQueTimer() {
/*        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().forEach(p -> {
            if (p.chemical != null && !p.hasPushRecord(String.valueOf(PushConstant.CHEMICAL_COMPLETE))
                    && hasChemicalQueComplete(p.chemical, now)) {
                p.putPushRecord(PushConstant.CHEMICAL_COMPLETE, PushConstant.PUSH_HAS_PUSHED);
                // PushMessageUtil.pushMessage(p.account, PushConstant.CHEMICAL_COMPLETE);
            }
        });*/
    }

    private boolean hasChemicalQueComplete(Chemical chemical, int now) {
        if (chemical.getPosQue().isEmpty()) {
            return false;
        }
        for (ChemicalQue q : chemical.getPosQue().values()) {
            if (now >= q.getEndTime()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 完成
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public ChemicalFinishRs chemicalFinish(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Chemical chemical = player.chemical;
        if (chemical == null || chemical.getPosQue() == null || chemical.getPosQue().isEmpty()) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "渡口领取时，找不到队列, roleId:" + roleId + ",lv=" + chemical.getExpandLv());
        }
        ChemicalFinishRs.Builder builder = ChemicalFinishRs.newBuilder();
        int now = TimeHelper.getCurrentSecond();
        Iterator<String> it = chemical.getPosQue().keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            ChemicalQue v = chemical.getPosQue().get(key);
            if (v != null && now >= v.getEndTime()) {
                int keyId = rewardDataManager.addAward(player, AwardType.PROP, v.getId(), v.getCount(),
                        AwardFrom.CHEMICAL_FINISH);
                builder.addAward(PbHelper.createAwardPb(AwardType.PROP, v.getId(), v.getCount(), keyId));
                LogUtil.debug("========endTime=====" + key);
                it.remove();
            }
        }

        Map<String, ChemicalQue> map = null;
        if (chemical.getPosQue() != null) {
            // 预备移动到前面
            Iterator<String> it2 = chemical.getPosQue().keySet().iterator();
            while (it2.hasNext()) {
                String key = it2.next();
                if (key.indexOf("_") > 0) {
                    String frontPos = key.substring(0, key.indexOf("_"));
                    if (chemical.getPosQue().get(frontPos) == null) {
                        ChemicalQue v = chemical.getPosQue().get(key);
                        if (map == null) {
                            map = new HashMap<>();
                        }
                        map.put(frontPos, makeChemicalQue(v.getId(), v.getPos(), v.getPeriod(), v.getEndTime(),
                                v.getPeople(), false, v.getSid(), v.getCount(), v.getStartTime()));
                        it2.remove();
                    }
                }
            }
        }

        if (map != null) {
            chemical.getPosQue().putAll(map);
        }

        if (!chemical.getPosQue().isEmpty()) {
            // 重新计算队列
            int queCnt = 0;
            for (Entry<String, ChemicalQue> kv : chemical.getPosQue().entrySet()) {
                if (!kv.getValue().getClonePos() && kv.getValue().getEndTime() > now) {
                    queCnt++;
                }
            }
            if (queCnt > 0) {
                int countryPeople = worldDataManager.getCountryPeople(player.lord.getCamp());
                int human = (int) player.resource.getHuman();
                human = human / queCnt;
                LogUtil.debug("剩余渡口队列=" + queCnt);
                processQue(chemical, roleId, human, countryPeople, player);
            }
            for (Entry<String, ChemicalQue> que : chemical.getPosQue().entrySet()) {
                builder.addQue(que.getValue());
            }
        }
        // 移除推送
        // player.removePushRecord(PushConstant.CHEMICAL_COMPLETE);
        return builder.build();
    }

    private ChemicalQue makeChemicalQue(int id, int pos, int period, int endTime, int people, boolean clonePos, int sid,
                                        int count, int startTime) {
        return ChemicalQue.newBuilder().setPos(pos).setId(id).setPeople(people).setPeriod(period).setEndTime(endTime)
                .setClonePos(clonePos).setSid(sid).setCount(count).setStartTime(startTime).build();
    }
}
