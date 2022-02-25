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
 * @Date 2021-06-23 11:10
 */
public class SeasonChangeTalentHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        SeasonTalentService service = getService(SeasonTalentService.class);
        GamePb4.ChangeTalentSkillRq req = msg.getExtension(GamePb4.ChangeTalentSkillRq.ext);
        GamePb4.ChangeTalentSkillRs resp = service.changeTalentSkill(player, req);
        if (resp != null){
            sendMsgToPlayer(GamePb4.ChangeTalentSkillRs.EXT_FIELD_NUMBER, GamePb4.ChangeTalentSkillRs.ext, resp);
        }
    }
}
