package com.gryphpoem.game.zw.handler.client.common;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.service.BannerService;

/**
 * ClassName: GetBannerHandler
 * Date:      2020/12/9 9:41
 * author     shi.pei
 */
public class GetBannerHandler extends ClientHandler {
    @Override
    public void action() throws MwException {
        BannerService service = getService(BannerService.class);
        GamePb4.GetBannerRq req = msg.getExtension(GamePb4.GetBannerRq.ext);
        GamePb4.GetBannerRs resp = service.getBannerInfo(req);
        sendMsgToPlayer(GamePb4.GetBannerRs.EXT_FIELD_NUMBER, GamePb4.GetBannerRs.ext, resp);
    }
}
