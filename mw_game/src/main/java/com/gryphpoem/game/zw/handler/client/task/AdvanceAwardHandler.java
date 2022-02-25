package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 个人目标领取
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-08-11 17:04
 */
public class AdvanceAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb3.AdvanceAwardRq req = msg.getExtension(GamePb3.AdvanceAwardRq.ext);
        TaskService taskService = getService(TaskService.class);
        GamePb3.AdvanceAwardRs resp = taskService.advanceAward(getRoleId(), req);
        sendMsgToPlayer(GamePb3.AdvanceAwardRs.EXT_FIELD_NUMBER, GamePb3.AdvanceAwardRs.ext, resp);
    }
}