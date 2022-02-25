package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetMajorTaskRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 任务信息
 * 
 * @author tyler
 *
 */
public class GetMajorTaskHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		GetMajorTaskRs resp = getService(TaskService.class).getMajorTaskRq(getRoleId());
		sendMsgToPlayer(GetMajorTaskRs.EXT_FIELD_NUMBER, GetMajorTaskRs.ext, resp);
	}

}
