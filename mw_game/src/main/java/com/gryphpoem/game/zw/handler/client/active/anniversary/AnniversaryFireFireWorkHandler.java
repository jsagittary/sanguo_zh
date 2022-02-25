package com.gryphpoem.game.zw.handler.client.active.anniversary;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.activity.anniversary.ActivityFireWorkService;

import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-07-26 11:41
 */
public class AnniversaryFireFireWorkHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.FireFireWorkRq req = msg.getExtension(GamePb4.FireFireWorkRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        ActivityFireWorkService service = DataResource.ac.getBean(ActivityFireWorkService.class);
        GamePb4.FireFireWorkRs rsp = service.fireWork(player, req);
        if (Objects.nonNull(rsp)){
            sendMsgToPlayer(GamePb4.FireFireWorkRs.EXT_FIELD_NUMBER, GamePb4.FireFireWorkRs.ext, rsp);
        }
    }
}
