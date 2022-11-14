package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacter;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacterReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicCrop;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicOrder;
import com.gryphpoem.game.zw.resource.domain.s.StaticHappiness;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.pojo.simulator.LifeSimulatorInfo;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private static List<StaticHomeCityCell> staticHomeCityCellList; // 主城地图格
    private static List<StaticHomeCityFoundation> staticHomeCityFoundationList; // 主城地基
    private static Map<Integer, List<Integer>> cellFoundationMap; // 格子对应可解锁的地基
    private static List<StaticEconomicOrder> staticEconomicOrderList; // 经济订单
    private static List<StaticEconomicCrop> staticEconomicCropList; // 经济作物
    private static List<StaticHappiness> staticHappinessList; // 幸福度

    @Override
    public void load() {
        staticSimulatorChooseList = staticIniDao.selectStaticSimulatorChooseList();
        staticSimulatorStepList = staticIniDao.selectStaticSimulatorStepList();
        staticCharacterList = staticIniDao.selectStaticCharacterList();
        staticCharacterRewardList = staticIniDao.selectStaticCharacterRewardList();
        staticSimCityList = staticIniDao.selectStaticSimCityList();
        staticHomeCityCellList = staticIniDao.selectStaticHomeCityCellList();
        staticHomeCityFoundationList = staticIniDao.selectStaticHomeCityFoundationList();
        cellFoundationMap = new HashMap<>();
        for (StaticHomeCityCell staticCell : staticHomeCityCellList) {
            Integer cellId = staticCell.getId();
            List<Integer> foundationIdList = cellFoundationMap.get(cellId);
            if (CheckNull.isEmpty(foundationIdList)) {
                foundationIdList = new ArrayList<>();
            }
            for (StaticHomeCityFoundation staticFoundation : staticHomeCityFoundationList) {
                if (staticFoundation.getCellList().contains(cellId) && !foundationIdList.contains(staticFoundation.getId())) {
                    foundationIdList.add(staticFoundation.getId());
                }
            }
            cellFoundationMap.put(cellId, foundationIdList);
        }
        staticEconomicOrderList = staticIniDao.selectStaticEconomicOrderList();
        staticEconomicCropList = staticIniDao.selectStaticEconomicCropList();
        staticHappinessList = staticIniDao.selectStaticHappinessList();
    }

    @Override
    public void check() {

    }

    public static List<StaticEconomicOrder> getStaticEconomicOrderList() {
        return staticEconomicOrderList;
    }

    public static List<StaticEconomicCrop> getStaticEconomicCropList() {
        return staticEconomicCropList;
    }

    public static StaticEconomicCrop getStaticEconomicCropByPropId(int propId) {
        return staticEconomicCropList.stream().filter(p -> Objects.equals(p.getPropId(), propId)).findFirst().orElse(null);
    }

    public static List<Integer> getFoundationIdListByCellId(int cellId) {
        return cellFoundationMap.get(cellId);
    }

    public static StaticHomeCityCell getStaticHomeCityCellById(int id) {
        return staticHomeCityCellList.stream().filter(tmp -> Objects.equals(id, tmp.getId())).findFirst().orElse(null);
    }

    public static StaticHomeCityFoundation getStaticHomeCityFoundationById(int id) {
        return staticHomeCityFoundationList.stream().filter(tmp -> Objects.equals(id, tmp.getId())).findFirst().orElse(null);
    }

    public static StaticSimulatorChoose getStaticSimulatorChoose(int id) {
        return staticSimulatorChooseList.stream().filter(staticSimulatorChoose -> staticSimulatorChoose.getId() == id).findFirst().orElse(null);
    }

    public static StaticSimulatorStep getStaticSimulatorStep(long id) {
        return staticSimulatorStepList.stream().filter(staticSimulatorStep -> staticSimulatorStep.getId() == id).findFirst().orElse(null);
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
            if (player.lord.getLevel() < needLordLv) {
                continue;
            }
            List<List<Integer>> needBuildLvList = staticSimCity.getBuildLv();
            boolean checkBuildLv = needBuildLvList.stream().anyMatch(tmp -> DataResource.ac.getBean(BuildingDataManager.class).checkBuildingLv(player, tmp));
            if (checkBuildLv) {
                continue;
            }
            List<Integer> open = staticSimCity.getOpen();
            boolean checkBuildOrNpcHasBind = lifeSimulatorInfoList.stream().anyMatch(tmp -> tmp.getBindType() == open.get(0) && tmp.getBindId() == open.get(1));
            if (checkBuildOrNpcHasBind) {
                continue;
            }
            canRandomSimCityList.add(staticSimCity);
        }
        return canRandomSimCityList;
    }
}
