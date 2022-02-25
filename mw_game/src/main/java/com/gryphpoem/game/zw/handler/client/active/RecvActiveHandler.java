package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.RecvActiveRq;
import com.gryphpoem.game.zw.pb.GamePb3.RecvActiveRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-05-09 14:16
 * @Description: 领取目标任务的活跃度
 * @Modified By:
 */
public class RecvActiveHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        RecvActiveRq rq = msg.getExtension(RecvActiveRq.ext);
        RecvActiveRs resp = getService(ActivityService.class).recvActive(getRoleId(), rq.getKeyId());
        sendMsgToPlayer(RecvActiveRs.EXT_FIELD_NUMBER, RecvActiveRs.ext, resp);
    }
}
