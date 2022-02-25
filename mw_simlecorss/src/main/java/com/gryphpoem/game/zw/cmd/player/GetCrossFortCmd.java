package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb5.GetCrossFortRq;
import com.gryphpoem.game.zw.service.FortressService;

/**
 * @ClassName GetCrossFortCmd.java
 * @Description 获取跨服城堡信息
 * @author QiuKun
 * @date 2019年5月16日
 */
@Cmd(rqCmd = GetCrossFortRq.EXT_FIELD_NUMBER)
public class GetCrossFortCmd extends PlayerBaseCommond {

    @Autowired
    private FortressService fortressService;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        GetCrossFortRq req = base.getExtension(GetCrossFortRq.ext);
        fortressService.getCrossFort(player, req);
    }
}
