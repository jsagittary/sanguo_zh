package com.gryphpoem.game.zw.resource.pojo.function;

import com.gryphpoem.game.zw.core.exception.MwException;

/**
 * @Description 功能解锁接口定义
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午7:58:37
 *
 */
public interface ICondition {

    /** 解锁条件参数分隔符 */
    String SEPARATOR = ",";

    /**
     * 根据各自的条件类型，解析参数字符串，生成对应的参数信息
     * 
     * @throws MwException
     */
    void parseParam() throws MwException;

    /**
     * 是否必须有参数
     * 
     * @return
     */
    boolean mustHaveParam();

    /**
     * 功能解锁条件检查，如果未通过，抛出{@link MwException}异常
     * 
     * @param param 待检查参数
     * @throws MwException
     */
    void checkFunction(Object param) throws MwException;

    /**
     * 判断功能解锁条件是否达成
     * 
     * @param param 传入待检查参数
     * @return 条件符合，返回true
     * @throws MwException
     */
    boolean reachCondition(Object param) throws MwException;
}
