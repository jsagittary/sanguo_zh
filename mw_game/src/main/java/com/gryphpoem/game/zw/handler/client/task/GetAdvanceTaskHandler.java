package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 个人目标任务
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-08-11 16:52
 */
public class GetAdvanceTaskHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb3.GetAdvanceTaskRq req = msg.getExtension(GamePb3.GetAdvanceTaskRq.ext);
        TaskService taskService = getService(TaskService.class);
        GamePb3.GetAdvanceTaskRs resp = taskService.getAdvanceTask(getRoleId(), req.getTaskIdList());
        sendMsgToPlayer(GamePb3.GetAdvanceTaskRs.EXT_FIELD_NUMBER, GamePb3.GetAdvanceTaskRs.ext, resp);
    }
}