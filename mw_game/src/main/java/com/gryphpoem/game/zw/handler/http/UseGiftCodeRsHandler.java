package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.GiftCodeRs;
import com.gryphpoem.game.zw.pb.HttpPb.UseGiftCodeRs;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 
 * @Description 使用礼包兑换码
 * @author TanDonghai
 *
 */
public class UseGiftCodeRsHandler extends HttpHandler {

	@Override
	public void action() {
		final UseGiftCodeRs req = msg.getExtension(UseGiftCodeRs.ext);
		
		AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
			@Override
			public void action() {
				PlayerService playerService = AppGameServer.ac.getBean(PlayerService.class);
				GiftCodeRs resp = playerService.useGiftCodeLogic(req);
				
				Base.Builder baseBuilder = PbHelper.createRsBase(GiftCodeRs.EXT_FIELD_NUMBER, GiftCodeRs.ext, resp);
				PlayerDataManager playerDataManager = AppGameServer.ac.getBean(PlayerDataManager.class);
		        Player player = playerDataManager.getPlayer(req.getLordId());
		        if (null != player && player.ctx != null && player.isLogin) {
		            AppGameServer.getInstance().sendMsgToGamer(player.ctx, baseBuilder);
		        }
//				AppGameServer.getInstance().sendMsgToGamer(ctx, baseBuilder);
			}
		}, DealType.MAIN);
	}

}
