package com.gryphpoem.game.zw.core.handler;

import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.BasePb.Base.Builder;

public abstract class AbsClientHandler extends Handler {

    /**
     * 向客户端发送（玩家操作）错误协议
     * 
     * @param code 错误码
     */
    public void sendErrorMsgToPlayer(int code) {
        Base.Builder baseBuilder = createRsBase(code);
        sendMsgToPlayer(baseBuilder);
    }

    /**
     * 向客户端发送协议
     * 
     * @param baseBuilder
     */
    public abstract void sendMsgToPlayer(Builder baseBuilder);
}
