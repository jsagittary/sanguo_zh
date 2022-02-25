package com.gryphpoem.game.zw.core.handler;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.constant.GameError;
import org.apache.dubbo.remoting.ExecutionException;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.TimeoutException;
import org.apache.dubbo.rpc.RpcException;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-12-20 19:58
 */
public abstract class ClientAsyncHandler extends ClientHandler {

    public <T extends GeneratedMessage> void complete(T rsp, Throwable t) {
        if (t != null) {
            handAsyncInvokeException(t);
            return;
        }
        if (rsp != null) {
            sendMsgToPlayer(rsp);
        }
    }

    public abstract <T> void sendMsgToPlayer(T rsp);

    protected void handAsyncInvokeException(Throwable t) {
        if (t.getCause() instanceof MwException) {
            LogUtil.error(t.getMessage(), t);
            MwException mwe = (MwException) t.getCause();
            sendErrorMsgToPlayer(mwe.getCode());
        } else if (t.getCause() instanceof RemotingException) {
            LogUtil.error(t.getMessage(), t);
            GameError error;
            if (t.getCause() instanceof ExecutionException) {
                error = GameError.INVOKER_FAIL;
            } else if (t.getCause() instanceof TimeoutException) {
                error = GameError.INVOKER_TIMEOUT;
            } else {
                error = GameError.SERVER_CONNECT_EXCEPTION;
            }
            sendErrorMsgToPlayer(error.getCode());
        } else if (t.getCause() instanceof RpcException) {
            LogUtil.error(t.getMessage(), t);
            RpcException e = (RpcException) t.getCause();
            GameError error = GameError.INVOKER_FAIL;
            if (e.getCode() == RpcException.FORBIDDEN_EXCEPTION) {
                error = GameError.SERVER_NOT_FOUND;
            }
            sendErrorMsgToPlayer(error.getCode());
        } else {
            LogUtil.error(this.getClass().getSimpleName() + " Not Hand  Exception -->" + t.getMessage(), t);
            sendErrorMsgToPlayer(GameError.UNKNOWN_ERROR.getCode());
        }
    }
}
