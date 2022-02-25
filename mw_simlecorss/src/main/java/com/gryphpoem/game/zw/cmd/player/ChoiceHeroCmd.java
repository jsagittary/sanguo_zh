package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.ChoiceHeroRq;
import com.gryphpoem.game.zw.service.CrossPlayerService;

/**
 * @ClassName ChoiceHeroCmd.java
 * @Description 选择将领
 * @author QiuKun
 * @date 2019年5月16日
 */
@Cmd(rqCmd = ChoiceHeroRq.EXT_FIELD_NUMBER)
public class ChoiceHeroCmd extends PlayerBaseCommond {
    @Autowired
    private CrossPlayerService crossPlayerService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        ChoiceHeroRq req = base.getExtension(ChoiceHeroRq.ext);
        crossPlayerService.choiceHero(player, req);
    }

}
