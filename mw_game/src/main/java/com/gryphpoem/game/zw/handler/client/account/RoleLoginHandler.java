package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.handler.http.GmHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.RoleLoginRq;
import com.gryphpoem.game.zw.pb.GamePb1.RoleLoginRs;
import com.gryphpoem.game.zw.pb.HttpPb.SendRoleInfosRq;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PlayerService;

import java.util.Map;

/**
 * 
 * @Description 角色登陆进入游戏
 * @author TanDonghai
 *
 */
public class RoleLoginHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		RoleLoginRq req = msg.getExtension(RoleLoginRq.ext);
		PlayerService playerService = getService(PlayerService.class);
		String packid = null;
		if (req.hasPackId()) {
			packid = req.getPackId();
		}
		Map<String, Object> map = playerService.roleLogin(this.ctx, getRoleId(), packid);
		sendMsgToPlayer(RoleLoginRs.ext, (RoleLoginRs) map.get("roleLoginRs"));

		// 向账号服推送角色信息数据
		SendRoleInfosRq sendRoleInfosRq = (SendRoleInfosRq) map.get("sendRoleInfosRq");
		if (!CheckNull.isNull(sendRoleInfosRq)) {
			Base.Builder baseBuilder = PbHelper.createRqBase(SendRoleInfosRq.EXT_FIELD_NUMBER, null,
					SendRoleInfosRq.ext, sendRoleInfosRq);
			AppGameServer.getInstance().sendMsgToPublic(baseBuilder);
		}
		// 向账号服推送角色信息数据
		PlayerDataManager playerDataManager = (PlayerDataManager) AppGameServer.ac.getBean("playerDataManager");
		Player player = playerDataManager.getPlayer(getRoleId());
		GmHandler.sendRoleToAccount(player);
	}
}
