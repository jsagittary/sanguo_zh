package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb.ForbiddenRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

/**
 * 
 * @Description 封号操作
 * @author TanDonghai
 *
 */
public class ForbiddenRqHandler extends HttpHandler {

	@Override
	public void action() {
		final ForbiddenRq req = msg.getExtension(ForbiddenRq.ext);

		AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);

				int forbiddenId = req.getForbiddenId();
				if (req.hasNick()) {
					String nick = req.getNick();
					int time = Integer.parseInt(String.valueOf(req.getTime()));// 注意 int 时间搓 到 2038 年
					toolService.forbiddenLogic(forbiddenId, nick, time);
				} else if (req.hasLordId()) {
					long lordId = req.getLordId();
					int time = Integer.parseInt(String.valueOf(req.getTime()));// 注意 int 时间搓 到 2038 年
					toolService.forbiddenLogic(forbiddenId, lordId, time);
				}

			}
		}, DealType.PUBLIC);
	}
}
