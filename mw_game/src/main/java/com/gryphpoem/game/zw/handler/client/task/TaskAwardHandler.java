package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.TaskAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.TaskAwardRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 领取任务奖励
 * 
 * @author tyler
 *
 */
public class TaskAwardHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		TaskAwardRq req = msg.getExtension(TaskAwardRq.ext);
		TaskService taskService = getService(TaskService.class);
		TaskAwardRs resp = taskService.taskAwardRq(getRoleId(), req.getTaskId());
		sendMsgToPlayer(TaskAwardRs.EXT_FIELD_NUMBER, TaskAwardRs.ext, resp);
	}

}
