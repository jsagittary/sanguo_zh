package com.gryphpoem.game.zw.handler.client.account;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.OffLineIncomeRs;
import com.gryphpoem.game.zw.service.PlayerService;
/**
 * 
* @ClassName: GetOffLineIncome
* @Description: 获取玩家离线收益
* @author chenqi
* @date 2018年8月16日
*
 */
public class GetOffLineIncome extends ClientHandler {
	
    @Override
    public void action() throws MwException {
    	PlayerService playerService = getService(PlayerService.class);
    	OffLineIncomeRs resp = playerService.getOffLineIncome(getRoleId());
		sendMsgToPlayer(OffLineIncomeRs.ext, resp);
    }
}
