package com.gryphpoem.game.zw.handler.client.hero.function;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.service.plan.TimeLimitedDrawCardFunctionService;

import java.util.Objects;

/**
 * 限时寻访活动中，根据累计抽卡次数购买自选箱
 *
 * @Author: GeYuanpeng
 * @Date: 2022/9/26 11:24
 */
public class BuyOptionalBoxFromTimeLimitedDrawCardHandler extends ClientHandler {

    @Override
    public void action() throws Exception {
        GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRs rsp = DataResource.ac.getBean(TimeLimitedDrawCardFunctionService.class)
                .BuyOptionalBoxByDrawCardCount(getRoleId(), msg.getExtension(GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRq.ext));
        if (Objects.nonNull(rsp)) {
            sendMsgToPlayer(GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRs.ext, rsp);
        }
    }

}
