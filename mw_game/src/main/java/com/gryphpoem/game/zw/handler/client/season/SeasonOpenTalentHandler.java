package com.gryphpoem.game.zw.handler.client.season;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

public class SeasonOpenTalentHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        SeasonTalentService service = getService(SeasonTalentService.class);
        GamePb4.OpenTalentRq req = msg.getExtension(GamePb4.OpenTalentRq.ext);
        GamePb4.OpenTalentRs resp = service.openTalent(player, req);
        if (resp != null) {
            sendMsgToPlayer(GamePb4.OpenTalentRs.EXT_FIELD_NUMBER, GamePb4.OpenTalentRs.ext, resp);
        }
    }
}
