package com.gryphpoem.game.zw.core.handler;

import com.gryphpoem.game.zw.server.AppGameServer;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;

import io.netty.channel.ChannelHandlerContext;

public abstract class InnerHandler extends AbsInnerHandler {

    @Override
    public DealType dealType() {
        return DealType.MAIN;
    }

    public void sendMsgToPlayer(Player player, Base.Builder baseBuilder) {
        AppGameServer.getInstance().sendMsgToGamer(player.ctx, baseBuilder);
    }

    public void sendMsgToPlayer(long roleId, Base.Builder baseBuilder) {
        ChannelHandlerContext roleCtx = DataResource.getRoleChannel(roleId);
        if (roleCtx != null) {
            AppGameServer.getInstance().sendMsgToGamer(roleCtx, baseBuilder);
        }
    }

    public void sendErrorMsgToPlayer(Player player, GameError gameError) {
        Base.Builder baseBuilder = createRsBase(gameError.getCode());
        sendMsgToPlayer(player, baseBuilder);
    }

    public <T> void sendMsgToPlayer(Player player, GeneratedExtension<Base, T> ext, int cmdCode, T msg) {
        Base.Builder baseBuilder = createRsBase(GameError.OK, ext, cmdCode, msg);
        sendMsgToPlayer(player, baseBuilder);
    }

    public <T> void sendMsgToPlayer(Player player, int code, GeneratedExtension<Base, T> ext, int cmdCode, T msg) {
        Base.Builder baseBuilder = createRsBase(code, ext, cmdCode, msg);
        sendMsgToPlayer(player, baseBuilder);
    }

    public <T> Base.Builder createRsBase(GameError gameError, GeneratedExtension<Base, T> ext, int cmdCode, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmdCode);
        baseBuilder.setCode(gameError.getCode());
        if (this.msg.hasParam()) {
            baseBuilder.setParam(this.msg.getParam());
        }
        if (msg != null) {
            baseBuilder.setExtension(ext, msg);
        }

        return baseBuilder;
    }

    public <T> Base.Builder createRsBase(int code, GeneratedExtension<Base, T> ext, int cmdCode, T msg) {
        Base.Builder baseBuilder = Base.newBuilder();
        baseBuilder.setCmd(cmdCode);
        baseBuilder.setCode(code);
        if (this.msg.hasParam()) {
            baseBuilder.setParam(this.msg.getParam());
        }
        if (code == GameError.OK.getCode()) {
            if (msg != null) {
                baseBuilder.setExtension(ext, msg);
            }
        }

        return baseBuilder;
    }

    public <T> void sendMsgToCrossServer(int command, GeneratedExtension<Base, T> ext, T msg) {
        Base.Builder baseBuilder = PbCrossUtil.createBase(command, getLordId(), ext, msg);
        sendMsgToCrossServer(baseBuilder);
    }

    private void sendMsgToCrossServer(Base.Builder baseBuilder) {
        AppGameServer.getInstance().sendMsgToCross(baseBuilder);
    }

    public long getLordId() {
        return msg.hasLordId() ? msg.getLordId() : 0L;
    }

    public <T> T getService(Class<T> c) {
        return AppGameServer.ac.getBean(c);
    }

    /**
     * 直接转发给客户端
     */
    public void directTranspondClient() {
        long lordId = getLordId();
        if (lordId > 0) {
            ChannelHandlerContext roleCtx = DataResource.getRoleChannel(lordId);
            if (roleCtx != null) {
                AppGameServer.getInstance().synMsgToGamer(roleCtx, msg);
            }
        }
    }
}
