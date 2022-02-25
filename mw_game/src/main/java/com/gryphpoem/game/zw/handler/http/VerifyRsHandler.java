package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.core.work.WWork;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.BeginGameRs;
import com.gryphpoem.game.zw.pb.HttpPb.VerifyRs;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PlayerService;

import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * @Description 玩家登录验证返回处理
 * @author TanDonghai
 *
 */
public class VerifyRsHandler extends HttpHandler {

	@Override
	public void action() {
		VerifyRs req = msg.getExtension(VerifyRs.ext);
		AppGameServer gameServer = AppGameServer.getInstance();
		Long channelId = req.getChannelId();
		ChannelHandlerContext ctx = gameServer.userChannels.get(channelId);

		if (ctx == null) {
			return;
		}

		if (msg.getCode() != 200) {
			Base.Builder builder = Base.newBuilder();
			builder.setCmd(BeginGameRs.EXT_FIELD_NUMBER);
			builder.setCode(msg.getCode());
			gameServer.connectServer.sendExcutor.addTask(channelId, new WWork(ctx, builder.build()));
			return;
		}

		PlayerService playerService = AppGameServer.ac.getBean(PlayerService.class);
		BeginGameRs message = playerService.verifyRs(req, ctx);
		Base.Builder baseBuilder = createRsBase(GameError.OK.getCode(), BeginGameRs.ext, message);
		AppGameServer.getInstance().sendMsgToGamer(ctx, baseBuilder);
	}
}
