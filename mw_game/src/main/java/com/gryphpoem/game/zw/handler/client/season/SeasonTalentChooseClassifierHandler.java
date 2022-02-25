package com.gryphpoem.game.zw.handler.client.season;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-06-22 17:51
 */
public class SeasonTalentChooseClassifierHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        SeasonTalentService service = getService(SeasonTalentService.class);
        GamePb4.ChooseClassifierRq req = msg.getExtension(GamePb4.ChooseClassifierRq.ext);
        GamePb4.ChooseClassifierRs resp = service.chooseClassifier(player, req);
        if (resp != null){
            sendMsgToPlayer(GamePb4.ChooseClassifierRs.EXT_FIELD_NUMBER, GamePb4.ChooseClassifierRs.ext, resp);
        }
    }
}
