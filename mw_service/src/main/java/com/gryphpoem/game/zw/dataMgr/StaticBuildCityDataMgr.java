package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacter;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacterReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.pojo.simulator.LifeSimulatorInfo;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 城建开荒配置
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/14 10:44
 */
@Service
public class StaticBuildCityDataMgr extends AbsStaticIniService {

    private static List<StaticSimulatorChoose> staticSimulatorChooseList; // 人生模拟器选项配置
    private static List<StaticSimulatorStep> staticSimulatorStepList; // 人生模拟器步骤配置
    private static List<StaticCharacter> staticCharacterList; // 性格
    private static List<StaticCharacterReward> staticCharacterRewardList; // 性格奖励
    private static List<StaticSimCity> staticSimCityList; // 性格奖励
    @Override
    public void load() {
        staticSimulatorChooseList = staticIniDao.selectStaticSimulatorChooseList();
        staticSimulatorStepList = staticIniDao.selectStaticSimulatorStepList();
        staticCharacterList = staticIniDao.selectStaticCharacterList();
        staticCharacterRewardList = staticIniDao.selectStaticCharacterRewardList();
        staticSimCityList = staticIniDao.selectStaticSimCityList();
    }

    @Override
    public void check() {

    }

    public static StaticSimulatorChoose getStaticSimulatorChoose(int id) {
        return staticSimulatorChooseList.stream().filter(staticSimulatorChoose -> staticSimulatorChoose.getId() == id).findFirst().orElse(null);
    }

    public static StaticSimulatorStep getStaticSimulatorStep(long id) {
        return staticSimulatorStepList.stream().filter(staticSimulatorStep -> staticSimulatorStep.getId() == id).findFirst().orElse(null);
    }

    public static List<StaticCharacter> getStaticCharacterList() {
        return staticCharacterList;
    }

    public static List<Integer> getCharacterRange(int id) {
        StaticCharacter staticCharacter = staticCharacterList.stream().filter(tmp -> tmp.getId() == id).findFirst().orElse(null);
        if (staticCharacter != null && CheckNull.nonEmpty(staticCharacter.getRange())) {
            return staticCharacter.getRange();
        } else {
            return null;
        }
    }

    public static List<StaticCharacterReward> getStaticCharacterRewardList() {
        return staticCharacterRewardList;
    }

    public static List<StaticSimCity> getStaticSimCityList() {
        return staticSimCityList;
    }

    public static List<StaticSimCity> getCanRandomSimCityList(Player player) {
        List<LifeSimulatorInfo> lifeSimulatorInfoList = player.getCityEvent().getLifeSimulatorInfoList();
        List<StaticSimCity> canRandomSimCityList = new ArrayList<>();
        for (StaticSimCity staticSimCity : staticSimCityList) {
            int needLordLv = staticSimCity.getLordLv();
            // 领主等级不满足，则不随机该事件
            if (player.lord.getLevel() < needLordLv) {
                continue;
            }
            List<List<Integer>> needBuildLvList = staticSimCity.getBuildLv();
            // 城镇事件要求的所有建筑等级任一不满足，则不随机该事件
            boolean checkBuildLv = needBuildLvList.stream().allMatch(tmp -> DataResource.ac.getBean(BuildingDataManager.class).checkBuildingLv(player, tmp));
            if (!checkBuildLv) {
                continue;
            }
            List<Integer> open = staticSimCity.getOpen();
            // 如果已有的城镇事件已经绑定过对应的建筑或NPC，则不随机该事件
            boolean checkBuildOrNpcHasBind = lifeSimulatorInfoList.stream().anyMatch(tmp -> tmp.getBindType() == open.get(0) && tmp.getBindId() == open.get(1));
            if (checkBuildOrNpcHasBind) {
                continue;
            }
            canRandomSimCityList.add(staticSimCity);
        }
        return canRandomSimCityList;
    }
}
