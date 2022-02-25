package com.gryphpoem.game.zw.resource.util;

/**
 * @Description String类型帮助类
 * @author TanDonghai
 * @date 创建时间：2017年9月7日 下午6:27:40
 *
 */
public class StringHelper {
    private StringHelper() {
    }

    /**
     * 将传入参数合并为一个字符串，每个元素间用下划线（_）分割
     * 
     * @param args
     * @return
     */
    public static String mergeToKey(Object... args) {
        if (CheckNull.isEmpty(args)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                sb.append("|");
            }
            Object object = args[i];
            sb.append(object);
        }
        return sb.toString();
    }

}
