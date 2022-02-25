package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.pb.CommonPb.MapForce.Builder;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRs;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipWorldData;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName AirshipMapEntity.java
 * @Description 飞艇
 * @author QiuKun
 * @date 2019年4月22日
 */
public class AirshipMapEntity extends BaseWorldEntity {

    private final AirshipWorldData airshipWorldData;

    public AirshipMapEntity(int pos, AirshipWorldData airshipWorldData) {
        super(pos, WorldEntityType.AIRSHIP);
        this.airshipWorldData = airshipWorldData;
    }

    @Override
    protected int getCfgId() {
        return airshipWorldData.getId();
    }

    @Override
    public Builder toMapForcePb(CrossWorldMap cMap) {
        Builder mapForcePb = super.toMapForcePb(cMap);
        mapForcePb.setParam(airshipWorldData.getId());
        mapForcePb.setSeqId(airshipWorldData.getKeyId());
        return mapForcePb;
    }

    public AirshipWorldData getAirshipWorldData() {
        return airshipWorldData;
    }

    @Override
    public void attackPos(AttackParamDto param) throws MwException {
        Player invokePlayer = param.getInvokePlayer();
        CrossWorldMap cMap = param.getCrossWorldMap();
        WorldService worldService = DataResource.ac.getBean(WorldService.class);

        Army army = checkAndCreateArmy(param, invokePlayer, ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP,
                this);
        army.setTargetId(airshipWorldData.getId());

        BaseArmy baseArmy = BaseArmy.baseArmyFactory(army);
        baseArmy.setArmyPlayerHeroState(cMap.getMapMarchArmy(), ArmyConstant.ARMY_STATE_MARCH);
        cMap.getMapMarchArmy().addArmy(baseArmy);
        worldService.removeProTect(invokePlayer, AwardFrom.ATTACK_AIRSHIP,pos); // 移除罩子
        
        cMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(invokePlayer.lord.getPos(), MapCurdEvent.UPDATE),
                MapEvent.mapEntity(pos, MapCurdEvent.UPDATE));

        // 填充返回值
        AttackCrossPosRs.Builder builder = param.getBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));
    }

    /**
     * 返回飞艇中的全部部队
     * 
     * @param cMap
     */
    public static void  returnAllArmyAuto(AirshipWorldData airshipWorldData,CrossWorldMap cMap) {
        airshipWorldData.getJoinRoles().values().stream().flatMap(l -> l.stream()).forEach(br -> {
            long roleId = br.getRoleId();
            int keyId = br.getKeyId();
            MapMarch mapMarchArmy = cMap.getMapMarchArmy();
            PlayerArmy playerArmy = mapMarchArmy.getPlayerArmyMap().get(roleId);
            if (playerArmy != null) {
                BaseArmy baseArmy = playerArmy.getArmy().get(keyId);
                if (baseArmy != null) {
                    baseArmy.normalRetreatArmy(mapMarchArmy);
                    cMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.UPDATE));
                }
            }
        });
    }
}
