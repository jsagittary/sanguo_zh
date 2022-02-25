package com.gryphpoem.game.zw.core.util;

/**
 * @ClassName StrUtils.java
 * @Description
 * @author QiuKun
 * @date 2018年6月1日
 */
public class StrUtils {
    private static final char[] lower = { 'a', 'z' };
    private static final char[] upper = { 'A', 'Z' };

    /**
     * 首字母变大写
     *
     * @param str
     * @return
     */
    public static String firstUpperCase(String str) {
        // str = str.substring(0, 1).toUpperCase() + str.substring(1);
        // return str;
        char[] cs = str.toCharArray();
        if (cs[0] >= lower[0] && cs[0] <= lower[1]) {
            cs[0] -= 32;
        }
        return String.valueOf(cs);
    }

    public static String firstLowerCase(String str) {
        char[] cs = str.toCharArray();
        if (cs[0] >= upper[0] && cs[0] <= upper[1]) {
            cs[0] += 32;
        }
        return String.valueOf(cs);
    }
}
