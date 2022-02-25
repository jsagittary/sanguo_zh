package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetWorldTaskRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 世界任务信息
 * 
 * @author tyler
 *
 */
public class GetWorlTaskHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		// GetWorldTaskRs resp =
		// getService(TaskService.class).getWorldTaskRq(getRoleId());
		// sendMsgToGamer(GetWorldTaskRs.EXT_FIELD_NUMBER, GetWorldTaskRs.ext,
		// resp);
	}

}
