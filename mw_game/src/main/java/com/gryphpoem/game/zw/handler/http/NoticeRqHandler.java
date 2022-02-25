package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb.NoticeRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

/**
 * 
 * @Description 公告
 * @author TanDonghai
 *
 */
public class NoticeRqHandler extends HttpHandler {

	@Override
	public void action() {
		final NoticeRq req = msg.getExtension(NoticeRq.ext);

		AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);

				toolService.sendNoticeLogic(req);
			}
		}, DealType.PUBLIC);
	}
}
