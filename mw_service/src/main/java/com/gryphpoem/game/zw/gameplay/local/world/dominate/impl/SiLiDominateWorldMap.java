package com.gryphpoem.game.zw.gameplay.local.world.dominate.impl;

import com.gryphpoem.game.zw.gameplay.local.world.dominate.abs.TimeLimitDominateMap;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.service.GameService;

/**
 * Description: 司隶雄踞一方
 * Author: zhangpeng
 * createTime: 2022-11-22 16:02
 */
public class SiLiDominateWorldMap extends TimeLimitDominateMap {

    private static class InstanceHolder {
        private static final SiLiDominateWorldMap INSTANCE = new SiLiDominateWorldMap(
                WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE);
    }

    public static final SiLiDominateWorldMap getInstance() {
        return SiLiDominateWorldMap.InstanceHolder.INSTANCE;
    }

    public SiLiDominateWorldMap(int worldFunction) {
        super(worldFunction);
    }

    @Override
    public void initSchedule() {

    }

    @Override
    public WorldPb.BaseWorldFunctionPb createPb(boolean isSaveDb) {
        return null;
    }

    @Override
    public void close() {

    }

    public void handleOnStartup() {

    }
}
