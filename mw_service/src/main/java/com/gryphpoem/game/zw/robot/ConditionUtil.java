package com.gryphpoem.game.zw.robot;

import java.util.Collection;

import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @Description 简单的条件比较工具类
 * @author TanDonghai
 * @date 创建时间：2017年9月18日 上午10:57:29
 *
 */
public class ConditionUtil {
    private ConditionUtil() {
    }

    /**
     * 返回被比较的参数是否大于或等于条件参数
     * 
     * @param compareNum
     * @param conditionNum
     * @return
     */
    public static boolean greaterThanCondition(int compareNum, int conditionNum) {
        return compareNum >= conditionNum;
    }

    /**
     * 返回被比较的参数是否大于或等于条件参数
     * 
     * @param compareNum
     * @param conditionNum
     * @return
     */
    public static boolean greaterThanCondition(long compareNum, long conditionNum) {
        return compareNum >= conditionNum;
    }

    /**
     * 返回集合中是否包含传入的参数
     * 
     * @param col 集合
     * @param obj 被比较参数
     * @return
     */
    public static <T> boolean contains(Collection<T> col, T obj) {
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
    public static boolean equalsCondition(Object compare, Object condition) {
        if (null == compare) {
            return false;
        }
        return compare.equals(condition);
    }
}
