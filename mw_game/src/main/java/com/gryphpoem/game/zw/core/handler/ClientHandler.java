package com.gryphpoem.game.zw.core.handler;

import com.gryphpoem.game.zw.server.AppGameServer;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.util.PbHelper;

public abstract class ClientHandler extends AbsClientHandler {

    @Override
    public void sendMsgToPlayer(Base.Builder baseBuilder) {
        super.rsMsg = baseBuilder.build();
        AppGameServer.getInstance().sendMsgToGamer(ctx, baseBuilder);
    }

    public void sendMsgToPublic(Base.Builder baseBuilder) {
        AppGameServer.getInstance().sendMsgToPublic(baseBuilder);
    }

    public Long getRoleId() {
        return ChannelUtil.getRoleId(ctx);
    }

    public DealType dealType() {
        return DealType.MAIN;
    }

    public void sendErrorMsgToPlayer(GameError gameError) {
        Base.Builder baseBuilder = createRsBase(gameError.getCode());
        sendMsgToPlayer(baseBuilder);
    }

    public <T> void sendMsgToPlayer(GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = createRsBase(GameError.OK, ext, msg);
        sendMsgToPlayer(baseBuilder);
    }

    public <T> void sendMsgToPlayer(int command, GeneratedExtension<Base, T> ext, T msg) {
        if (msg != null) {
            Base.Builder baseBuilder = createRsBaseByCmd(command, ext, msg);
            sendMsgToPlayer(baseBuilder);
        }
    }

    public <T> void sendMsgToPlayer(GameError gameError, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = createRsBase(gameError, ext, msg);
        sendMsgToPlayer(baseBuilder);
    }

    public <T> Base.Builder createRsBase(GameError gameError, GeneratedExtension<Base, T> ext, T msg) {
        return super.createRsBase(gameError.getCode(), ext, msg);
    }

    /**
     * 调用该方法代表任务执行成功，返回码默认200
     * 
     * @param cmd
     * @param ext
     * @param msg
     * @return
     */
    public <T> Base.Builder createRsBaseByCmd(int cmd, GeneratedExtension<Base, T> ext, T msg) {
        return super.createRsBase(cmd, GameError.OK.getCode(), ext, msg);
    }

    public <T> T getService(Class<T> c) {
        return AppGameServer.ac.getBean(c);
    }

    public Long getChannelId() {
        return ChannelUtil.getChannelId(ctx);
    }

    /*-----------------发给其他服务器--------------*/
    public <T> void sendMsgToCrossServer(int command, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = PbHelper.createRqBase(command, null, ext, msg);
        AppGameServer.getInstance().sendMsgToCross(baseBuilder, getRoleId());
    }

    public void sendMsgToCrossServer(Base.Builder baseBuilder) {
        AppGameServer.getInstance().sendMsgToCross(baseBuilder);
    }
}
