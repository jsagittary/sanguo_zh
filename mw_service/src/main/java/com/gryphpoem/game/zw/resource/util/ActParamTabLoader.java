package com.gryphpoem.game.zw.resource.util;

import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.resource.domain.s.StaticSystem;

/**
 * @ClassName ActParamTabLoader.java
 * @Description s_activity_param表加载工具类
 * @author QiuKun
 * @date 2017年12月22日
 *
 */
public class ActParamTabLoader {

    /**
     * 根据systemId获取对应的值，以int类型返回
     * 
     * @param systemId
     * @param defaultVaule 当表中找不到该配置项时，返回的默认值
     * @return
     */
    public static int getIntegerSystemValue(int systemId, int defaultVaule) {
        StaticSystem ss = StaticIniDataMgr.getActParamById(systemId);
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
        StaticSystem ss = StaticIniDataMgr.getActParamById(systemId);
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
        StaticSystem ss = StaticIniDataMgr.getActParamById(systemId);
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
        StaticSystem ss = StaticIniDataMgr.getActParamById(systemId);
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
        StaticSystem ss = StaticIniDataMgr.getActParamById(systemId);
        if (null != ss) {
            return ss.getValue();
        }
        return defaultVaule;
    }

    public static Date getDateValue(int id, String defVal){
        String val = getStringSystemValue(id,defVal);
        return DateHelper.parseDate(val);
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

    public static List<List<String>> getListListStringSystemValue(int systemId, String defaultVaule) {
        String str = getStringSystemValue(systemId, defaultVaule);
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray arrs = JSONArray.parseArray(str);
        List<List<String>> list = new ArrayList<>();
        for (int i = 0; i < arrs.size(); i++) {
            JSONArray a = arrs.getJSONArray(i);
            List<String> arr = new ArrayList<>();
            for (int j = 0; j < a.size(); j++) {
                arr.add(a.getString(j));
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
}
