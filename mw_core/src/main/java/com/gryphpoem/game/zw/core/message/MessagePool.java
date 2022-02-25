package com.gryphpoem.game.zw.core.message;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.handler.AbsHttpHandler;
import com.gryphpoem.game.zw.core.handler.AbsInnerHandler;
import com.gryphpoem.game.zw.core.intercept.InterceptAspect;
import com.gryphpoem.game.zw.core.intercept.InterceptType;
import com.gryphpoem.game.zw.core.intercept.MessageInterceptorManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;

public class MessagePool {
    private static MessagePool ins = new MessagePool();

    private IMessagePool agent;

    private MessagePool() {
    }

    public static MessagePool getIns() {
        return ins;
    }

    public void setAgentMessagePool(final IMessagePool agent) {
        this.agent = agent;
    }

    public AbsClientHandler getClientHandler(int id) throws InstantiationException, IllegalAccessException {
        return agent.getClientHandler(id);
    }

    public AbsHttpHandler getHttpHandler(int id) throws InstantiationException, IllegalAccessException {
        return agent.getHttpHandler(id);
    }

    public AbsInnerHandler getInnerHandler(int id) throws InstantiationException, IllegalAccessException {
        return agent.getInnerHandler(id);
    }

    public int getRsCmd(int rqCmd) {
        Integer rsCmd = agent.getRsCmd(rqCmd);
        return null == rsCmd ? 0 : rsCmd;
    }

    /**
     * 获取协议号对应的功能解锁id
     * 
     * @param cmd
     * @return 如果协议号没有注册，返回0
     */
    private int getFunctionUnlockId(int cmd) {
        Integer param = agent.getFunctionUnlockId(cmd);
        return null == param ? 0 : param;
    }

    /**
     * 获取拦截器对应的参数信息
     * 
     * @param type 拦截类型
     * @param cmd
     * @return 当未找到对应信息时，会返回null
     */
    public Object getInterceptParam(InterceptType type, int cmd) {
        switch (type) {
            case FUNCTION_UNLOCK:// 功能解锁
                return getFunctionUnlockId(cmd);

            default:
                break;
        }
        return null;
    }

    /**
     * 协议是否需要执行拦截逻辑
     * 
     * @param type
     * @param cmd
     * @return
     */
    public boolean needIntercept(InterceptType type, int cmd) {
        switch (type) {
            case FUNCTION_UNLOCK:// 功能解锁
                return getFunctionUnlockId(cmd) != 0;

            default:
                break;
        }
        return false;
    }

    /**
     * 执行协议相关拦截操作
     * 
     * @param aspect
     * @param roleId
     * @param msg
     * @throws MwException
     */
    public void doMessageIntercept(InterceptAspect aspect, long roleId, Base msg) throws MwException {
        MessageInterceptorManager.getIns().doIntercept(aspect, roleId, msg);
    }
}
