package com.gryphpoem.game.zw.core.exception;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * @ClassName ExceptionMessage.java
 * @Description 自定义异常消息处理类
 * @author TanDonghai
 * @date 创建时间：2017年3月28日 下午7:09:41
 *
 */
public class ExceptionMessage {
    /**
     * 将数组中的数据拼接为一个字符串
     * 
     * @param message
     * @return
     */
    public static String spliceMessage(Object... message) {
        return lamdaSplice(message);
    }

    /**
     * 使用lamda表达式完成遍历操作，试用lamda
     * 
     * @param message
     * @return
     */
    private static String lamdaSplice(Object... message) {
        StringBuffer sb = new StringBuffer();
        if (null != message) {
            try {
                Stream.of(message).filter(obj -> obj != null).forEach(obj -> appendMessage(sb, obj));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendMessage(StringBuffer sb, Object obj) {
        if (obj.getClass().isArray()) {// 数组
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                appendMessage(sb, Array.get(obj, i));
            }
        } else if (obj instanceof Collection) {// 集合
            for (Object o : (Collection<Object>) obj) {
                appendMessage(sb, o);
            }
        } else {
            sb.append(obj.toString());
        }
    }

    /**
     * 非lamda遍历方式
     * 
     * @param message
     * @return
     */
    static String append(Object... message) {
        StringBuffer sb = new StringBuffer();
        if (null != message) {
            for (Object obj : message) {
                appendMessage(sb, obj);
            }
        }
        return sb.toString();
    }
}
