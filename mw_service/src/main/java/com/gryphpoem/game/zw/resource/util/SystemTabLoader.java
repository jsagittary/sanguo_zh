package com.gryphpoem.game.zw.resource.util;

import com.alibaba.fastjson.JSONArray;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.resource.domain.s.StaticSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName SystemTabLoader.java
 * @Description s_system表加载工具类
 * @author TanDonghai
 * @date 创建时间：2017年3月18日 上午10:33:24
 *
 */
public class SystemTabLoader {

    /**
     * 根据systemId获取对应的值，以int类型返回
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回的默认值
     * @return
     */
    public static int getIntegerSystemValue(int systemId, int defaultVaule) {
        StaticSystem ss = StaticIniDataMgr.getSystemConstantById(systemId);
        if (null != ss) {
            return Integer.valueOf(ss.getValue());
        }
        return defaultVaule;
    }

    /**
     * 根据systemId获取对应的值，以long类型返回
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回的默认值
     * @return
     */
    public static long getLongSystemValue(int systemId, long defaultVaule) {
        StaticSystem ss = StaticIniDataMgr.getSystemConstantById(systemId);
        if (null != ss) {
            return Long.valueOf(ss.getValue());
        }
        return defaultVaule;
    }

    /**
     * 根据systemId获取对应的值，以float类型返回
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回的默认值
     * @return
     */
    public static float getFloatSystemValue(int systemId, float defaultVaule) {
        StaticSystem ss = StaticIniDataMgr.getSystemConstantById(systemId);
        if (null != ss) {
            return Float.valueOf(ss.getValue());
        }
        return defaultVaule;
    }

    /**
     * 根据systemId获取对应的值，以double类型返回
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回的默认值
     * @return
     */
    public static double getDoubleSystemValue(int systemId, double defaultVaule) {
        StaticSystem ss = StaticIniDataMgr.getSystemConstantById(systemId);
        if (null != ss) {
            return Double.valueOf(ss.getValue());
        }
        return defaultVaule;
    }

    /**
     * 根据systemId获取对应的值，以String类型返回
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回的默认值
     * @return
     */
    public static String getStringSystemValue(int systemId, String defaultVaule) {
        StaticSystem ss = StaticIniDataMgr.getSystemConstantById(systemId);
        if (null != ss) {
            return ss.getValue();
        }
        return defaultVaule;
    }

    /**
     * 根据systemId获取对应的值，返回List<Integer>类型
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回由该字符串解析出的List<br>
     *            <tt>特别注意：<tt>如果找不到配置项且传入的字符串为null或空字符串，将会返回null
     * @return
     */
    public static List<Integer> getListIntSystemValue(int systemId, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray arr = JSONArray.parseArray(str);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            list.add(arr.getInteger(i));
        }
        return list;
    }

    /**
     * 根据systemId获取对应的值，返回List<String>类型
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回由该字符串解析出的List
     * @return 如果找不到配置项且传入的字符串为null或空字符串，将会返回null
     */
    public static List<String> getListStringSystemValue(int systemId, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray arr = JSONArray.parseArray(str);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            list.add(arr.getString(i));
        }
        return list;
    }

    /**
     * 根据systemId获取对应的值，返回 嵌套List 类型
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回由该字符串解析出的 嵌套List，<br>
     *            <tt>特别注意：<tt>如果找不到配置项且传入的字符串为null或空字符串，将会返回null
     * @return
     */
    public static List<List<Integer>> getListListIntSystemValue(int systemId, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray arrs = JSONArray.parseArray(str);
        List<List<Integer>> list = new ArrayList<>();
        for (int i = 0; i < arrs.size(); i++) {
            JSONArray a = arrs.getJSONArray(i);
            List<Integer> arr = new ArrayList<>();
            for (int j = 0; j < a.size(); j++) {
                arr.add(a.getInteger(j));
            }
            list.add(arr);
        }
        return list;
    }


    /**
     * 根据systemId获取对应的值，返回 嵌套List 类型
     *
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回由该字符串解析出的 嵌套List，<br>
     *            <tt>特别注意：<tt>如果找不到配置项且传入的字符串为null或空字符串，将会返回null
     * @return
     */
    public static <T> List<List<T>> getListListSystemValue(int systemId, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray arrs = JSONArray.parseArray(str);
        List<List<T>> list = new ArrayList<>();
        for (int i = 0; i < arrs.size(); i++) {
            JSONArray a = arrs.getJSONArray(i);
            List<T> arr = new ArrayList<>();
            for (Object o : a) {
                arr.add((T) o);
            }
            list.add(arr);
        }
        return list;
    }


    public static List<List<List<Integer>>> getListListListIntSystemValue(int systemId, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }
        JSONArray arrs = JSONArray.parseArray(str);
        List<List<List<Integer>>> list = new ArrayList<>();
        for (int i = 0; i < arrs.size(); i++) {
            JSONArray a = arrs.getJSONArray(i);
            List<List<Integer>> arr = new ArrayList<>();
            for (int j = 0; j < a.size(); j++) {
                JSONArray ar = a.getJSONArray(j);
                List<Integer> intArr = new ArrayList<>();
                for (int k = 0; k < ar.size(); k++) {
                    intArr.add(ar.getInteger(k));
                }
                arr.add(intArr);
            }
            list.add(arr);
        }
        return list;
    }

    public static List<List<List<Integer>>> getListListListIntSystemValue(String defaultVaule) {
        JSONArray arrs = JSONArray.parseArray(defaultVaule);
        List<List<List<Integer>>> list = new ArrayList<>();
        for (int i = 0; i < arrs.size(); i++) {
            JSONArray a = arrs.getJSONArray(i);
            List<List<Integer>> arr = new ArrayList<>();
            for (int j = 0; j < a.size(); j++) {
                JSONArray ar = a.getJSONArray(j);
                List<Integer> intArr = new ArrayList<>();
                for (int k = 0; k < ar.size(); k++) {
                    intArr.add(ar.getInteger(k));
                }
                arr.add(intArr);
            }
            list.add(arr);
        }
        return list;
    }

    /**
     * 根据systemId获取对应的值，返回 嵌套Map 类型
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回由该字符串解析出的 map，<br>
     *            <tt>特别注意：<tt>如果找不到配置项且传入的字符串为null或空字符串，将会返回null
     * @return
     */
    public static Map<Integer, Integer> getMapIntSystemValue(int systemId, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray arrs = JSONArray.parseArray(str);
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < arrs.size(); i++) {
            JSONArray a = arrs.getJSONArray(i);
            if (a.size() >= 2) map.put(a.getInteger(0), a.getInteger(1));
        }
        return map;
    }

    /**
     * 根据systemId获取对应的值, 返回 嵌套Map 类型
     * @param systemId
     * @param indexPos     索引位置
     * @param defaultVaule 当表中找不到该配置项时，返回由该字符串解析出的 map，<br>
     *            <tt>特别注意：<tt>如果找不到配置项且传入的字符串为null或空字符串，将会返回null
     * @return
     */
    public static Map<Integer, List<Integer>> getMapListSystemValue(int systemId, int indexPos, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray arrs = JSONArray.parseArray(str);
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < arrs.size(); i++) {
            JSONArray a = arrs.getJSONArray(i);
            int key = a.getInteger(indexPos);
            if (a.size() >= 2) {
                List<Integer> intArr = new ArrayList<>();
                for (int j = indexPos + 1; j < a.size(); j++) {
                    intArr.add(a.getInteger(j));
                }
                map.put(key, intArr);
            }
        }
        return map;
    }
}
