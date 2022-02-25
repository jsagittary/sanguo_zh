package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.GamePb1.GetNamesRs;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 
 * @Description 获取随机名字
 * @author TanDonghai
 *
 */
public class GetNamesHandler extends ClientHandler {

	@Override
	public void action() {
		PlayerService playerService = getService(PlayerService.class);
		
		GetNamesRs.Builder builder = GetNamesRs.newBuilder();
		builder.addAllName(playerService.getAvailabelNames());
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCmd(GetNamesRs.EXT_FIELD_NUMBER);
		baseBuilder.setCode(GameError.OK.getCode());
		baseBuilder.setExtension(GetNamesRs.ext, builder.build());
		
		sendMsgToPlayer(baseBuilder);
	}

}
