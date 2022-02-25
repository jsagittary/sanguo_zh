package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ScorpionActivateRs;
import com.gryphpoem.game.zw.pb.GamePb4.ScorpionActivateRq;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * 蝎王激活
 * @program: zombie
 * @description:
 * @author: zhou jie
 * @create: 2019-08-22 20:48
 */
public class ScorpionActivateRsHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ScorpionActivateRq req = msg.getExtension(ScorpionActivateRq.ext);
        TaskService taskService = getService(TaskService.class);
        ScorpionActivateRs resp = taskService.scorpionActivate(getRoleId(), req);
        sendMsgToPlayer(ScorpionActivateRs.EXT_FIELD_NUMBER, ScorpionActivateRs.ext, resp);
    }
}