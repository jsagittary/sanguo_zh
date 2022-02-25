package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb5.OpFortHeroRq;
import com.gryphpoem.game.zw.service.FortressService;

/**
 * @ClassName OpFortHeroCmd.java
 * @Description 玩家将领操作
 * @author QiuKun
 * @date 2019年5月21日
 */
@Cmd(rqCmd = OpFortHeroRq.EXT_FIELD_NUMBER)
public class OpFortHeroCmd extends PlayerBaseCommond {

    @Autowired
    private FortressService fortressService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        OpFortHeroRq req = base.getExtension(OpFortHeroRq.ext);
        fortressService.opFortHero(player, req);
    }

}
