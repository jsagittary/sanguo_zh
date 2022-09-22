package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.apache.dubbo.remoting.ExecutionException;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.TimeoutException;
import org.apache.dubbo.rpc.RpcException;

/**
 * @author xwind
 * @date 2021/9/3
 */
public abstract class AbsGameService implements GameService {

    @Override
    public void handleOnStartup() throws Exception {

    }

    @Override
    public void handleOnReloadConfig() throws Exception {

    }

    public void handAsyncInvokeException(int cmdCode, Throwable t, Player player) {
        if (t.getCause() instanceof MwException) {
            LogUtil.error(t.getMessage(), t);
            MwException mwe = (MwException) t.getCause();
            sendErrorMsgToPlayer(cmdCode, mwe.getCode(), player);
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
            sendErrorMsgToPlayer(cmdCode, error.getCode(), player);
        } else if (t.getCause() instanceof RpcException) {
            LogUtil.error(t.getMessage(), t);
            RpcException e = (RpcException) t.getCause();
            GameError error = GameError.INVOKER_FAIL;
            if (e.getCode() == RpcException.FORBIDDEN_EXCEPTION) {
                error = GameError.SERVER_NOT_FOUND;
            }
            sendErrorMsgToPlayer(cmdCode, error.getCode(), player);
        } else {
            LogUtil.error(this.getClass().getSimpleName() + " Not Hand  Exception -->" + t.getMessage(), t);
            sendErrorMsgToPlayer(cmdCode, GameError.UNKNOWN_ERROR.getCode(), player);
        }
    }

    public void sendErrorMsgToPlayer(int cmdCode, int code, Player player) {
        BasePb.Base.Builder baseBuilder = PbHelper.createErrorBase(cmdCode, code, 0l);
        DataResource.ac.getBean(PlayerService.class).syncMsgToPlayer(baseBuilder.build(), player);
    }
}
