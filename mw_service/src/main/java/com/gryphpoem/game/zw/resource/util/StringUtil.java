package com.gryphpoem.game.zw.resource.util;

import com.alibaba.fastjson.JSONArray;

import java.util.*;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-09-01 19:35
 */
public final class StringUtil {

    public static List<Integer> string2ListInt(String str) {
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

    public static List<String> string2ListString(String str) {
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

    public static List<List<Integer>> string2ListListInt(String str) {
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray jsonArray = JSONArray.parseArray(str);
        List<List<Integer>> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray a = jsonArray.getJSONArray(i);
            List<Integer> arr = new ArrayList<>();
            for (int j = 0; j < a.size(); j++) {
                arr.add(a.getInteger(j));
            }
            list.add(arr);
        }
        return list;
    }

    public static <T> List<List<T>> getListListSystemValue(String str) {
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

    public static Map<Integer, Integer> string2MapIntInt(String str) {
        if (CheckNull.isNullTrim(str)) {
            return null;
        }

        JSONArray jsonArray = JSONArray.parseArray(str);
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray a = jsonArray.getJSONArray(i);
            if (a.size() >= 2) map.put(a.getInteger(0), a.getInteger(1));
        }
        return map;
    }


    public static String listListInt2String(List<List<Integer>> list) {
        if (CheckNull.isEmpty(list)) return null;
        return JSONArray.toJSONString(list);
    }

    public static String listInt2String(Collection<Integer> list){
        if (CheckNull.isEmpty(list)) return null;
        return JSONArray.toJSONString(list);
    }
}
