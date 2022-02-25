package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.handler.http.GmHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.CreateRoleRq;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 
 * @Description 创建服务器角色
 * @author TanDonghai
 *
 */
public class CreateRoleHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		CreateRoleRq req = msg.getExtension(CreateRoleRq.ext);
		PlayerService playerService = getService(PlayerService.class);

		Base.Builder baseBuilder = playerService.createRole(req, getRoleId(), req.getHeroSkin());
		sendMsgToPlayer(baseBuilder);
		
		//向账号服注册此游戏角色
		PlayerDataManager playerDataManager = AppGameServer.ac.getBean(PlayerDataManager.class);
		Player newPlayer = playerDataManager.getPlayer(getRoleId());
		GmHandler.sendRoleToAccount(newPlayer);
	}

}
