package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.DailyAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.DailyAwardRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * @ClassName DailyAwardHandler.java
 * @Description 日常活跃度领奖
 * @author QiuKun
 * @date 2018年5月15日
 */
public class DailyAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        DailyAwardRq req = msg.getExtension(DailyAwardRq.ext);
        TaskService service = getService(TaskService.class);
        DailyAwardRs resp = service.dailyAward(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(DailyAwardRs.EXT_FIELD_NUMBER, DailyAwardRs.ext, resp);
    }

}
