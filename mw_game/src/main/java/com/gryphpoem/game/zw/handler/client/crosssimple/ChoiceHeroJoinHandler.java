package com.gryphpoem.game.zw.handler.client.crosssimple;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.pb.GamePb5.ChoiceHeroJoinRq;

/**
 * @ClassName ChoiceHeroJoinHandler.java
 * @Description 选择将领到跨服
 * @author QiuKun
 * @date 2019年5月16日
 */
public class ChoiceHeroJoinHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ChoiceHeroJoinRq req = getMsg().getExtension(ChoiceHeroJoinRq.ext);
        PlayerForCrossService service = getService(PlayerForCrossService.class);
        service.choiceHeroJoin(getRoleId(), req);
    }

}
