package com.gryphpoem.game.zw.resource.pojo.function;

import java.util.Collection;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @Description 功能解锁条件的一些简单实现和封装
 * @author TanDonghai
 * @date 创建时间：2017年6月7日 下午7:21:58
 *
 */
public abstract class AbstractCondition implements ICondition {
    protected StaticFunctionCondition config;// 解锁条件配置信息
    
    protected boolean mustHaveParam;

    protected AbstractCondition(StaticFunctionCondition config) throws MwException {
        setConfig(config);

        // 检查参数
        checkConditionParam();

        // 解析参数
        parseParam();
    }

    /**
     * 检查解锁配置条件信息参数是否正确
     * 
     * @throws MwException
     */
    protected void checkConditionParam() throws MwException {
        if (null == config || null == config.getParam()) {
            throw new MwException("功能解锁条件参数未配置, config:" + config);
        }
    }

    @Override
    public boolean mustHaveParam() {
        return mustHaveParam;
    }

    @Override
    public void checkFunction(Object param) throws MwException {
        if (null == param && mustHaveParam()) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "功能解锁玩家条件参数未获取到， unlockType:", getUnlockType());
        }

        if (!reachCondition(param)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "功能未解锁， unlockType:", getUnlockType(),
                    ", condition:", config.getParam(), ", param:", param);
        }
    }

    /**
     * 返回被比较的参数是否大于或等于条件参数
     * 
     * @param compareNum
     * @param conditionNum
     * @return
     */
    protected boolean greaterThanCondition(int compareNum, int conditionNum) {
        return compareNum >= conditionNum;
    }

    /**
     * 返回被比较的参数是否大于或等于条件参数
     * 
     * @param compareNum
     * @param conditionNum
     * @return
     */
    protected boolean greaterThanCondition(long compareNum, long conditionNum) {
        return compareNum >= conditionNum;
    }

    /**
     * 返回集合中是否包含传入的参数
     * 
     * @param col 集合
     * @param obj 被比较参数
     * @return
     */
    protected <T> boolean contains(Collection<T> col, T obj) {
        if (CheckNull.isEmpty(col)) {
            return false;
        }
        return col.contains(obj);
    }

    /**
     * 判断传入参数是否与条件参数相等(equals)
     * 
     * @param compare
     * @param condition
     * @return
     */
    protected boolean equalsCondition(Object compare, Object condition) {
        if (null == compare) {
            return false;
        }
        return compare.equals(condition);
    }

    public StaticFunctionCondition getConfig() {
        return config;
    }

    public void setConfig(StaticFunctionCondition config) {
        this.config = config;
    }

    public int getUnlockType() {
        return config.getUnlockType();
    }

    public void setMustHaveParam(boolean mustHaveParam) {
        this.mustHaveParam = mustHaveParam;
    }
}
