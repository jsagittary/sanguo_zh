package com.gryphpoem.game.zw.handler.client.active.music;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.activity.MusicFestivalCreativeService;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-10-29 22:11
 */
public class MusicCrtOfficeFinishTaskHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.FinishCrtTaskRq req  = msg.getExtension(GamePb4.FinishCrtTaskRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        MusicFestivalCreativeService service = DataResource.ac.getBean(MusicFestivalCreativeService.class);
        GamePb4.FinishCrtTaskRs rsp = service.finishTask(player, req);
        sendMsgToPlayer(GamePb4.FinishCrtTaskRs.EXT_FIELD_NUMBER, GamePb4.FinishCrtTaskRs.ext, rsp);
    }
}
