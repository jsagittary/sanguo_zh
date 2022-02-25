package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.CheckTaskRq;
import com.gryphpoem.game.zw.pb.GamePb3.CheckTaskRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 检车客户端可以直接完成的任务
 * 
 * @author tyler
 *
 */
public class CheckTaskHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CheckTaskRq req = msg.getExtension(CheckTaskRq.ext);
        TaskService taskService = getService(TaskService.class);
        CheckTaskRs resp = taskService.checkClientTask(getRoleId(), req);
        sendMsgToPlayer(CheckTaskRs.EXT_FIELD_NUMBER, CheckTaskRs.ext, resp);
    }

}
