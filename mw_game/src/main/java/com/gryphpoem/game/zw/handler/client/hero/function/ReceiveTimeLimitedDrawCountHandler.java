package com.gryphpoem.game.zw.handler.client.hero.function;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.plan.TimeLimitedDrawCardFunctionService;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 16:27
 */
public class ReceiveTimeLimitedDrawCountHandler extends ClientHandler {
    @Override
    public void action() throws Exception {
        GamePb5.ReceiveTimeLimitedDrawCountRs rsp = DataResource.ac.getBean(TimeLimitedDrawCardFunctionService.class).receiveTimeLimitedDrawCount(getRoleId(),
                msg.getExtension(GamePb5.ReceiveTimeLimitedDrawCountRq.ext));
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb5.ReceiveTimeLimitedDrawCountRs.ext, rsp);
        }
    }
}
