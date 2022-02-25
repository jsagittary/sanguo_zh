package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.TaskInfoRq;
import com.gryphpoem.game.zw.pb.GamePb3.TaskInfoRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 获取单个任务
 * 
 * @author tyler
 *
 */
public class TaskInfoHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        TaskInfoRq req = msg.getExtension(TaskInfoRq.ext);
        TaskInfoRs resp = getService(TaskService.class).taskInfo(getRoleId(), req.getTaskId());
        sendMsgToPlayer(TaskInfoRs.EXT_FIELD_NUMBER, TaskInfoRs.ext, resp);
    }
}
