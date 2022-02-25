package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossRankRq;
import com.gryphpoem.game.zw.service.CrossRankService;

/**
 * @ClassName GetCrossRankCmd.java
 * @Description 获取跨服排行榜
 * @author QiuKun
 * @date 2019年5月29日
 */

@Cmd(rqCmd = GetCrossRankRq.EXT_FIELD_NUMBER)
public class GetCrossRankCmd extends PlayerBaseCommond {
    @Autowired
    CrossRankService crossRankService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        GetCrossRankRq req = base.getExtension(GetCrossRankRq.ext);
        crossRankService.getCrossRank(player, req);
    }
    
}
