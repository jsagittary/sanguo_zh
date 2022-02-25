package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.CrossHeroReviveRq;
import com.gryphpoem.game.zw.service.FortressService;

/**
 * @ClassName CrossHeroReviveCmd.java
 * @Description 将领复活
 * @author QiuKun
 * @date 2019年5月25日
 */
@Cmd(rqCmd = CrossHeroReviveRq.EXT_FIELD_NUMBER)
public class CrossHeroReviveCmd extends PlayerBaseCommond {
    @Autowired
    private FortressService fortressService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        CrossHeroReviveRq req = base.getExtension(CrossHeroReviveRq.ext);
        fortressService.crossHeroRevive(player, req);
    }

}
