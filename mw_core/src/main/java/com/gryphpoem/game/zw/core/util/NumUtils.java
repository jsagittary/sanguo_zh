package com.gryphpoem.game.zw.core.util;

/**
 * @ClassName NumUtils.java
 * @Description
 * @author QiuKun
 * @date 2018年5月25日
 */
public class NumUtils {

    /**
     * 将两个int值转换成long
     * 
     * @param low
     * @param high
     * @return
     */
    public static long combineInt2Long(int low, int high) {
        return ((long) low & 0xFFFFFFFFl) | (((long) high << 32) & 0xFFFFFFFF00000000l);
    }

    /**
     * 将一个long值转换成2个int
     * 
     * @param val
     * @return
     */
    public static int[] separateLong2int(Long val) {
        int[] ret = new int[2];
        ret[0] = (int) (0xFFFFFFFFl & val);
        ret[1] = (int) ((0xFFFFFFFF00000000l & val) >> 32);
        return ret;
    }
}
