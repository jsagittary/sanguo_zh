package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.GetDailyTaskRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * @ClassName GetDailyTaskHandler.java
 * @Description 获取日常任务
 * @author QiuKun
 * @date 2018年5月15日
 */
public class GetDailyTaskHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        // GetDailyTaskRq req = msg.getExtension(GetDailyTaskRq.ext);
        TaskService service = getService(TaskService.class);
        GetDailyTaskRs resp = service.getDailyTask(getRoleId());
        if (resp != null) sendMsgToPlayer(GetDailyTaskRs.EXT_FIELD_NUMBER, GetDailyTaskRs.ext, resp);
    }

}
