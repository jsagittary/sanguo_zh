package com.gryphpoem.game.zw.core.intercept;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.pb.BasePb.Base;

/**
 * @Description 游戏协议前置拦截器接口定义
 * @author TanDonghai
 * @date 创建时间：2017年6月19日 上午10:26:28
 *
 */
public interface IMessageIntercept {
    /**
     * 获取拦截类型
     * 
     * @return
     */
    InterceptType getInterceptType();

    /**
     * 执行拦截逻辑
     * 
     * @param roleId 角色id
     * @param msg 协议数据
     * @param param 对应的拦截参数
     */
    void doIntercept(long roleId, Base msg, Object param) throws MwException;
}
