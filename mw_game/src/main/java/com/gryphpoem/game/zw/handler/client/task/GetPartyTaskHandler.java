package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetPartyTaskRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 获取军团任务
 * 
 * @author tyler
 *
 */
public class GetPartyTaskHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetPartyTaskRs resp = getService(TaskService.class).getPartyTask(getRoleId());
		sendMsgToPlayer(GetPartyTaskRs.EXT_FIELD_NUMBER, GetPartyTaskRs.ext, resp);
	}

}
