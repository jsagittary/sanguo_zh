package com.gryphpoem.game.zw.service.dominate.abs;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.WorldMapPlay;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.dominate.IDominateWorldMapService;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-22 21:33
 */
public abstract class AbsDominateWorldMapService implements IDominateWorldMapService {

    @Override
    public GamePb8.GetDominateWorldMapInfoRs getDominateWorldMapInfo(long roleId, GamePb8.GetDominateWorldMapInfoRq req) {
        DataResource.ac.getBean(PlayerDataManager.class).checkPlayerIsExist(roleId);
        WorldMapPlay worldMapPlay = null;
        switch (req.getWorldFunction()) {
            case WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE:
                worldMapPlay = StateDominateWorldMap.getInstance();
                break;
            case WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE:
                worldMapPlay = SiLiDominateWorldMap.getInstance();
                break;
        }

        if (CheckNull.isNull(worldMapPlay)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("不能存的地图玩法, req:%d", req.getWorldFunction()));
        }

        GamePb8.GetDominateWorldMapInfoRs.Builder builder = GamePb8.GetDominateWorldMapInfoRs.newBuilder();
        builder.setBaseFunction(worldMapPlay.createPb(false));
        return builder.build();
    }

    @Override
    public GamePb8.AttackDominateCityRs attackDominateCity(long roleId, GamePb8.AttackDominateCityRq req) {
        return null;
    }

    @Override
    public GamePb8.GetDominateDetailRs getDominateDetail(long roleId, GamePb8.GetDominateDetailRq req) {
        return null;
    }

    @Override
    public void syncDominateWorldMapInfo() {

    }
}
