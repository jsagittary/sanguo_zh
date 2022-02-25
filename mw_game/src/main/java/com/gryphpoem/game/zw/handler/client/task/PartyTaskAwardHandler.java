package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.PartyTaskAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.PartyTaskAwardRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 军团任务领奖
 * 
 * @author tyler
 *
 */
public class PartyTaskAwardHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		PartyTaskAwardRq req = msg.getExtension(PartyTaskAwardRq.ext);
		TaskService taskService = getService(TaskService.class);
		PartyTaskAwardRs resp = taskService.partyTaskAward(getRoleId(), req.getTaskId());
		sendMsgToPlayer(PartyTaskAwardRs.EXT_FIELD_NUMBER, PartyTaskAwardRs.ext, resp);
	}

}
