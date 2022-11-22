package com.gryphpoem.game.zw.service.dominate.state;

import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.service.dominate.IDominateWorldMapService;
import com.gryphpoem.game.zw.service.dominate.abs.AbsDominateWorldMapService;
import org.springframework.stereotype.Component;

/**
 * Description: 州郡雄踞一方
 * Author: zhangpeng
 * createTime: 2022-11-22 21:26
 */
@Component
public class StateDominateWorldMapService extends AbsDominateWorldMapService {

    @Override
    public int getWorldMapFunction() {
        return WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE;
    }
}
