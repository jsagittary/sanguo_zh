package com.gryphpoem.game.zw.handler.client.active.auction;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.activity.ActivityAuctionService;

public class GetMyAuctionRecordHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb4.GetMyAuctionRecordRq req = msg.getExtension(GamePb4.GetMyAuctionRecordRq.ext);
        ActivityAuctionService service = getService(ActivityAuctionService.class);
        GamePb4.GetMyAuctionRecordRs rs = service.getMyAuctionRecord(getRoleId());
        if (!CheckNull.isNull(rs)) {
            sendMsgToPlayer(GamePb4.GetMyAuctionRecordRs.ext, rs);
        }
    }
}
