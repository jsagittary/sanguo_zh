package com.gryphpoem.game.zw.handler.client.prop;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.BattleFailMessageRs;
import com.gryphpoem.game.zw.service.DecisiveBattleService;


/*****
 * 决战失败消息协议
 * @author zhuJianJian
 *
 */
public class BattleFailMessageHandler extends ClientHandler{

	@Override
	public void action() throws MwException {
		DecisiveBattleService battleService = getService(DecisiveBattleService.class);
		BattleFailMessageRs res = battleService.getBattleFailMessage(getRoleId());
		sendMsgToPlayer(BattleFailMessageRs.ext, res);
	}

}
