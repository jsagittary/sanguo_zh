package com.gryphpoem.game.zw.gameplay.local.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipListRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipListRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipPersonData;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipWorldData;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName CrossAirshipService.java
 * @Description
 * @author QiuKun
 * @date 2019年4月23日
 */
@Component
public class CrossAirshipService {

    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private CrossWorldMapService crossWorldMapService;
    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 刷新飞艇
     */
    public void refreshAirship() {
        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
        if (cMap != null) {
            cMap.refreshAirship();
        }
    }

    /**
     * 获取飞艇列表
     * 
     * @param player
     * @param req
     * @return
     * @throws MwException
     */
    public GetAirshipListRs getAirshipList(Player player, GetAirshipListRq req) throws MwException {
        int areaId = player.lord.getArea();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(player, areaId);
        Map<Integer, AirshipWorldData> airshipMap = cMap.getMapEntityGenerator().getAirshipMap();
        GetAirshipListRs.Builder builder = GetAirshipListRs.newBuilder();
        // 列表
        if (!req.getAreaIdList().isEmpty()) {
            for (AirshipWorldData aswd : airshipMap.values()) {
                builder.addAirshipList(
                        PbHelper.createAirshipShowClientPb(aswd, player.lord.getCamp(), false, playerDataManager));
            }
        }
        // 详情
        int airshipKeyId = req.getAirshipKeyId(); // 具体的飞艇keyId

        if (airshipKeyId > 0) {
            AirshipWorldData airshipWorldData = airshipMap.get(airshipKeyId);
            if (airshipWorldData != null) {
                builder.setAirshipDetail(PbHelper.createAirshipShowClientPb(airshipWorldData, player.lord.getCamp(),
                        true, playerDataManager));
            }
        }
        AirshipPersonData airshipPersonData = player.getAndCreateAirshipPersonData();
        airshipPersonData.refresh();
        builder.setKillAwardCnt(airshipPersonData.getKillAwardCnt());
        builder.setAttendAwardCnt(airshipPersonData.getAttendAwardCnt());
        return builder.build();
    }
}
