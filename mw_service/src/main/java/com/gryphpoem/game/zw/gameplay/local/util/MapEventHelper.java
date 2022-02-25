package com.gryphpoem.game.zw.gameplay.local.util;

import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.PlayerMapEntity;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @ClassName MapEventHelper.java
 * @Description
 * @author QiuKun
 * @date 2019年3月23日
 */
public class MapEventHelper {

    /**
     * 地图改变推的PB
     * 
     * @param invokePlayer
     * @param event
     * @param cMap
     * @return 没找到就返回null
     */
    public static CommonPb.MapChgEventPb transformMapChgEventPb(Player invokePlayer, MapEvent event,
            CrossWorldMap cMap) {
        CommonPb.MapChgEventPb.Builder builder = CommonPb.MapChgEventPb.newBuilder();
        builder.setEventType(event.getEventType().getValue());
        builder.setCurdEvent(event.getCurdEvent().getValue());
        builder.setPos(event.getPos());

        // 填充相关数据
        if (event.getEventType() == MapEventType.MAP_LINE) { // 行军线
            long armyRoleId = event.getArmyRoleId();
            int armyKey = event.getArmyKey();
            builder.setArmyLordId(armyRoleId);
            builder.setKeyId(armyKey);
            if (MapCurdEvent.CREATE == event.getCurdEvent() || MapCurdEvent.UPDATE == event.getCurdEvent()) {
                PlayerArmy playerArmy = cMap.getMapMarchArmy().getPlayerArmyMap().get(armyRoleId);
                if (playerArmy == null) return null;
                BaseArmy baseArmy = playerArmy.getArmy().get(armyKey);
                if (baseArmy == null) return null;
                builder.setMarch(baseArmy.toMapLinePb(cMap.getMapMarchArmy()));
            }
        } else if (event.getEventType() == MapEventType.MAP_ENTITY || event.getEventType() == MapEventType.MAP_AREA) { // 地图点,区域的点
            if (MapCurdEvent.CREATE == event.getCurdEvent() || MapCurdEvent.UPDATE == event.getCurdEvent()) {
                BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(event.getPos());
                if (baseWorldEntity == null) return null;
                if (baseWorldEntity.getType() == WorldEntityType.CITY) { // city类型
                    CityMapEntity cityMapEntity = (CityMapEntity) baseWorldEntity;
                    if (event.getEventType() == MapEventType.MAP_ENTITY) {
                        builder.setMapCity(cityMapEntity.toMapCityPb(invokePlayer, cMap));
                    } else if (event.getEventType() == MapEventType.MAP_AREA) {
                        builder.setAreaCity(cityMapEntity.toAreaCityPb(invokePlayer, cMap));
                    }
                } else { // 其他的force
                    if (event.getEventType() == MapEventType.MAP_ENTITY) {
                        builder.setMapForce(baseWorldEntity.toMapForcePb(cMap));
                    } else if (event.getEventType() == MapEventType.MAP_AREA) {
                        if (baseWorldEntity.getType() == WorldEntityType.PLAYER) {
                            PlayerMapEntity p = (PlayerMapEntity) baseWorldEntity;
                            builder.setAreaForce(p.toAreaForcePb());
                        }
                    }
                }
            }
        }
        return builder.build();
    }
}
