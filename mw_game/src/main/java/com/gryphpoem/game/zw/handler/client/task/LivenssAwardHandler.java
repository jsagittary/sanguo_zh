package com.gryphpoem.game.zw.handler.client.task;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.LivenssAwardRq;
import com.gryphpoem.game.zw.pb.GamePb3.LivenssAwardRs;
import com.gryphpoem.game.zw.service.TaskService;

/**
 * @ClassName LivenssAwardHandler.java
 * @Description 
 * @author QiuKun
 * @date 2018年5月17日
 */
public class LivenssAwardHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        LivenssAwardRq req = msg.getExtension(LivenssAwardRq.ext);
        TaskService service = getService(TaskService.class);
        LivenssAwardRs resp = service.livenssAward(getRoleId(), req);
        if (resp != null) sendMsgToPlayer(LivenssAwardRs.EXT_FIELD_NUMBER, LivenssAwardRs.ext, resp);
    }

}
