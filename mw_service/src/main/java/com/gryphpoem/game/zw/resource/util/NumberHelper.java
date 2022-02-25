package com.gryphpoem.game.zw.resource.util;

/**
 * @ClassName NumberUtils.java
 * @Description 数值帮助类
 * @author QiuKun
 * @date 2018年1月5日
 */
public class NumberHelper {

    /**
     * 字符串转int类型
     * 
     * @param str
     * @param def 默认值
     * @return
     */
    public static int strToInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return def;
        }
    }
}
