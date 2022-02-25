package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.HttpPb.SendToMailRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

/**
 * 
 * @Description 发送后台邮件
 * @author TanDonghai
 *
 */
public class SendToMailRqHandler extends HttpHandler {

	@Override
	public void action() {
		final SendToMailRq req = msg.getExtension(SendToMailRq.ext);
		LogUtil.debug("收到GM邮件====" + req);
		AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				int moldId = Integer.parseInt(req.getMoldId());
				String title = req.getTitle();
				String content = req.getContont();
				String award = req.getAward();
				String to = req.getTo();
				int type = req.getType();
				String channelNo = req.getChannelNo();// 0全体 其他 渠道1|渠道2
				String childNo = req.getChildNo();   //子渠道
				int online = req.getOnline();// 0全体 1在线
				String making = req.getMarking();
				int avip = req.getAvip();
                int bvip = req.getBvip();
                int alv = req.getAlv();
                int blv = req.getBlv();
                int camp = req.getCamp();
                String partys = req.getPartys();

				GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
				toolService.sendMailLogic(making, type, channelNo,childNo, online, moldId, title, content, award, to, alv, blv,
						avip, bvip, partys,camp);
			}
		}, DealType.PUBLIC);
	}
}
