package com.gryphpoem.game.zw.handler.client.chat;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.ShareReportRq;
import com.gryphpoem.game.zw.pb.GamePb3.ShareReportRs;
import com.gryphpoem.game.zw.service.ChatService;

/**
 * 
 * @Description 分享战报
 * @author TanDonghai
 *
 */
public class ShareReportHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		ShareReportRq req = msg.getExtension(ShareReportRq.ext);
		ChatService chatService = getService(ChatService.class);
		ShareReportRs resp = chatService.shareReport(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(ShareReportRs.ext, resp);
		}
	}

}
