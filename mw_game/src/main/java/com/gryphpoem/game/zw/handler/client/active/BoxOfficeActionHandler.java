package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.activity.ActivityBoxOfficeService;

import java.util.Objects;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/10/27 17:22
 */
public class BoxOfficeActionHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.MusicFestivalBoxOfficeActionRq req = msg.getExtension(GamePb4.MusicFestivalBoxOfficeActionRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        ActivityBoxOfficeService service = DataResource.ac.getBean(ActivityBoxOfficeService.class);
        GamePb4.MusicFestivalBoxOfficeActionRs rsp = service.boxOfficeAction(player, req);
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb4.MusicFestivalBoxOfficeActionRs.EXT_FIELD_NUMBER, GamePb4.MusicFestivalBoxOfficeActionRs.ext, rsp);
        }
    }
}
