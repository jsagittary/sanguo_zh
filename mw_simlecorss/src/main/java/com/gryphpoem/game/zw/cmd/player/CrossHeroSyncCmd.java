package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.CrossHeroSyncRq;
import com.gryphpoem.game.zw.service.FortressService;

/**
 * @ClassName CrossHeroSyncCmd.java
 * @Description 将领信息同步
 * @author QiuKun
 * @date 2019年6月12日
 */
@Cmd(rqCmd = CrossHeroSyncRq.EXT_FIELD_NUMBER)
public class CrossHeroSyncCmd extends PlayerBaseCommond {
    
    @Autowired
    private FortressService fortressService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        CrossHeroSyncRq req = base.getExtension(CrossHeroSyncRq.ext);
        fortressService.crossHeroSync(player, req);
    }

}
