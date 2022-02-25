package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GainWorldTaskRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 领取世界任务奖励
 * 
 * @author tyler
 *
 */
public class GainWorldTaskHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		// GainWorldTaskRs resp =
		// getService(TaskService.class).gainWorldTaskRq(getRoleId());
		// sendMsgToGamer(GainWorldTaskRs.EXT_FIELD_NUMBER,
		// GainWorldTaskRs.ext, resp);
	}

}
