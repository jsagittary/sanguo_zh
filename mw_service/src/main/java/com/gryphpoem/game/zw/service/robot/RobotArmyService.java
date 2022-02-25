package com.gryphpoem.game.zw.service.robot;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.ArmQue;
import com.gryphpoem.game.zw.resource.domain.p.Factory;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticFactoryExpand;
import com.gryphpoem.game.zw.resource.domain.s.StaticFactoryRecruit;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.FactoryService;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * @Description 机器人部队相关服务类
 * @author TanDonghai
 * @date 创建时间：2017年10月18日 上午11:09:46
 *
 */
@Service
public class RobotArmyService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private BuildingDataManager buildingDataManager;

    @Autowired
    private FactoryService factoryService;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private TechDataManager techDataManager;

    /**
     * 自动招募士兵（生产战车）
     * 
     * @param player
     */
    public void autoRecruitArmy(Player player) {
        factoryRecruit(player, BuildingType.FACTORY_1);
        factoryRecruit(player, BuildingType.FACTORY_2);
        factoryRecruit(player, BuildingType.FACTORY_3);
    }

    /**
     * 根据建筑id，生产对应战车
     * 
     * @param player
     * @param buildingId
     */
    private void factoryRecruit(Player player, int buildingId) {
        if (buildingDataManager.checkBuildIsUpping(player, buildingId)) {
            return;// 建筑升级中，跳过
        }

        Factory factory = factoryService.createFactoryIfNotExist(player, buildingId);

        // 如果有已征兵完成的队列，先收取
        autoGainArmy(player, factory, buildingId);

        StaticFactoryExpand staticFactoryExpand = StaticBuildingDataMgr.getStaticFactoryExpand(buildingId,
                factory.getFctExpLv());
        if (staticFactoryExpand == null) {
            return;// 数据未配置，或位置不足，跳过
        }

        if (factory.getAddList().size() >= staticFactoryExpand.getBuildNum()) {
            return;// 征兵队列已满，跳过
        }

        int armType = BuildingType.getResourceByBuildingType(buildingId);
        StaticBuildingLv buildingLv = StaticBuildingDataMgr.getStaticBuildingLevel(buildingId,
                BuildingDataManager.getBuildingLv(buildingId, player));
        if (buildingLv.getLevel() <= 0) {
            return;
        }

        if (CheckNull.isEmpty(buildingLv.getCapacity())) {
            LogUtil.robot("机器人招募士兵，兵营容量信息为空, robot:", player.roleId, ", buildingLv:", buildingLv);
            return;
        }
        if (playerDataManager.getArmCount(player.resource, armType) >= staticFactoryExpand.getArmNum()
                + buildingLv.getCapacity().get(0).get(1)) {
            return; // 兵力已满，跳过
        }

        StaticFactoryRecruit sfr = StaticBuildingDataMgr.getStaticFactoryRecruit(factory.getFctLv());
        int endTime = 0;
        for (ArmQue armQue : factory.getAddList()) {
            if (armQue.getEndTime() > endTime) {
                endTime = armQue.getEndTime();
            }
        }
        endTime += sfr.getUpTime();

        // 检查是否有足够资源招募
        int addArm = factoryService.getAddNum(player, sfr, buildingId);
        int needFood = addArm * Constant.FACTORY_ARM_NEED_FOOD;
        // 科技消耗系数
        int radio = techDataManager.getFood4BuildingType(player, buildingId);
        if (radio > 0) {
            needFood = addArm * radio;
        }
        try {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.FACTORYRECRUIT);
        } catch (MwException e) {
            return;// 资源不足，跳过
        }
        int needOIL = 0;
        // 开启部队招募
        ArmQue que = new ArmQue(player.maxKey(), buildingId, addArm, endTime, sfr.getUpTime(), needFood, needOIL);
        factory.getAddList().add(que);

        // 更新征兵次数的任务
        taskDataManager.updTask(player, TaskType.COND_ARM_CNT, 1, armType);
    }

    /**
     * 自动收取指定兵营的兵力
     * 
     * @param player
     * @param factory
     * @param buildingId
     */
    private void autoGainArmy(Player player, Factory factory, int buildingId) {
        int addNum = 0;
        Iterator<ArmQue> it = factory.getAddList().iterator();
        int armType = BuildingType.getResourceByBuildingType(buildingId);
        while (it.hasNext()) {
            ArmQue armQue = it.next();
            if (TimeHelper.getCurrentSecond() >= armQue.getEndTime()) {
                addNum += armQue.getAddArm();
                it.remove();
            }
        }

        if (addNum > 0) {
            // 增加兵力
            rewardDataManager.addAward(player, AwardType.ARMY, armType, addNum, AwardFrom.GAIN_ARM);

            // 更新征兵数量的任务
            taskDataManager.updTask(player, TaskType.COND_ARM_TYPE_CNT, addNum, armType);
            playerDataManager.createRoleOpt(player, Constant.OptId.id_3, String.valueOf(buildingId));
        }
    }
}
