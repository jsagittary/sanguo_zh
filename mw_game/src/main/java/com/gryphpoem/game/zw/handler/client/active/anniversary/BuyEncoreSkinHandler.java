package com.gryphpoem.game.zw.handler.client.active.anniversary;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.service.activity.anniversary.ActivitySkinEncoreService;

import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-07-27 15:37
 */
public class BuyEncoreSkinHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        GamePb4.BuyEncoreSkinRq req = msg.getExtension(GamePb4.BuyEncoreSkinRq.ext);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(getRoleId());
        ActivitySkinEncoreService service = DataResource.ac.getBean(ActivitySkinEncoreService.class);
        GamePb4.BuyEncoreSkinRs rsp = service.buyEncoreSkin(player, req);
        if (Objects.nonNull(rsp)){
            sendMsgToPlayer(GamePb4.BuyEncoreSkinRs.EXT_FIELD_NUMBER, GamePb4.BuyEncoreSkinRs.ext, rsp);
        }
    }
}
