package com.gryphpoem.game.zw.core.message;

import java.util.HashMap;

import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.handler.AbsHttpHandler;
import com.gryphpoem.game.zw.core.handler.AbsInnerHandler;

public interface IMessagePool {
    HashMap<Integer, Class<? extends AbsClientHandler>> clientHandlers = new HashMap<Integer, Class<? extends AbsClientHandler>>();
    HashMap<Integer, Class<? extends AbsHttpHandler>> serverHandlers = new HashMap<Integer, Class<? extends AbsHttpHandler>>();
    HashMap<Integer, Class<? extends AbsInnerHandler>> innerHandlers = new HashMap<Integer, Class<? extends AbsInnerHandler>>();
    HashMap<Integer, Integer> rsMsgCmd = new HashMap<Integer, Integer>();
    HashMap<Integer, Integer> funcUnlockMsg = new HashMap<>();// 协议需要检查的功能解锁id, key:cmd, value:functionId

    /**
     * 注册客户端与服务器交互协议、相关处理handler
     * 
     * @param rqCmd 进入协议号
     * @param rsCmd 返回协议号
     * @param handlerClass
     */
    void registerC(int rqCmd, int rsCmd, Class<? extends AbsClientHandler> handlerClass);

    /**
     * 注册游戏服务器与账号服交互协议、相关处理handler
     * 
     * @param rqCmd 进入协议号
     * @param rsCmd 返回协议号
     * @param handlerClass
     */
    void registerH(int rqCmd, int rsCmd, Class<? extends AbsHttpHandler> handlerClass);

    /**
     * 注册游戏服务器与其他服务器的交互协议、相关处理handler
     * 
     * @param rqCmd 协议号
     * @param handlerClass
     */
    void registerI(int rqCmd, Class<? extends AbsInnerHandler> handlerClass);

    /**
     * 注册功能解锁检查参数信息
     * 
     * @param cmd 协议号
     * @param functionId 功能id
     */
    void registerFunctionUnlock(int cmd, int functionId);

    /**
     * 根据注册的信息，获取具体的handler对象
     * 
     * @param cmd 注册的协议号
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    AbsClientHandler getClientHandler(int cmd) throws InstantiationException, IllegalAccessException;

    AbsHttpHandler getHttpHandler(int cmd) throws InstantiationException, IllegalAccessException;

    AbsInnerHandler getInnerHandler(int cmd) throws InstantiationException, IllegalAccessException;

    /**
     * 获取协议号对应的功能id
     * 
     * @param cmd
     * @return 返回功能id，如果该协议没有注册对应的功能解锁检查，会返回NULL
     */
    Integer getFunctionUnlockId(int cmd);

    /**
     * 根据进入协议号，获取返回协议号
     * 
     * @param rqCmd
     * @return
     */
    Integer getRsCmd(int rqCmd);
}