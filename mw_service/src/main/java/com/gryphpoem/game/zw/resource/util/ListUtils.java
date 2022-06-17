package com.gryphpoem.game.zw.resource.util;

import com.alibaba.fastjson.JSONArray;
import com.gryphpoem.game.zw.core.util.LogUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ListUtils {

    /**
     * 判断指定的值在区间列表中的位置
     * @param num 指定值
     * @param numList numList: [[1],[2,10],[100,1000]]
     * @return 指定值所在区间, 如果未找到返回-1
     */
    public static int getInListIndex(int num, List<List<Integer>> numList) {
        Objects.requireNonNull(numList);
        for (int i = 0; i < numList.size(); i++) {
            List<Integer> ids = numList.get(i);
            if (ids != null) {
                if (ids.size() == 1) {
                    if (ids.get(0) == num) return i;
                } else if (ids.size() == 2) {
                    if (ids.get(0) <= num && num <= ids.get(1)) return i;
                }
            }
        }
        return -1;
    }

    /**
     * 判断指定数字是否在列表中<br/>
     * 通常用于 判断指定服务器ID 时候在列表中, 指定赛季是否在列表中
     * @param
     * @param numList: [[1],[2,10],[100,1000]]
     * @return
     */
    public static boolean isInList(int num, List<List<Integer>> numList) {
        Objects.requireNonNull(numList);
        for (List<Integer> ids : numList) {
            if (ids != null) {
                if (ids.size() == 1) {
                    if (ids.get(0) == num) return true;
                } else if (ids.size() == 2) {
                    if (ids.get(0) <= num && num <= ids.get(1)) return true;
                }
            }
        }
        return false;
    }

    public static boolean isBlank(List list){
        return list == null || list.isEmpty();
    }

    public static boolean isNotBlank(List list){
        return !isBlank(list);
    }

    public static String toString(List list){
        if(isBlank(list))
            return "null";
        return Arrays.toString(list.toArray());
    }

    public static String toListString(List<List> list) {
        if (isBlank(list))
            return "[]";

        int iMax = list.size() - 1;
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            if (isBlank(list.get(i))) {
                b.append("null");
                continue;
            }
            b.append(Arrays.toString(list.get(i).toArray()));
            if (i == iMax)
                return b.toString();
            b.append(", ");
        }
    }

    public static boolean contains(int[] array,int val){
        if (Objects.isNull(array) || array.length <= 0) {
            return false;
        }
        return ArrayUtils.contains(array,val);
    }

    public static List<List<Integer>> getListList(String jsonStr) {
        List<List<Integer>> listList = new ArrayList<>();
        if (StringUtils.isBlank(jsonStr)) {
            return listList;
        }
        try {
            JSONArray arrays = JSONArray.parseArray(jsonStr);
            for (int i = 0; i < arrays.size(); i++) {
                List<Integer> list = new ArrayList<Integer>();
                JSONArray array = arrays.getJSONArray(i);
                for (int j = 0; j < array.size(); j++) {
                    list.add(array.getInteger(j));
                }
                listList.add(list);
            }
        } catch (Exception e) {
            LogUtil.error("解析错误: jsonStr:", jsonStr);
        }
        return listList;
    }

    public static List<List<Integer>> createItems(int type, int id, int count){
        List<List<Integer>> list = new ArrayList();
        List<Integer> list_ = new ArrayList();
        list_.add(type);
        list_.add(id);
        list_.add(count);
        list.add(list_);
        return list;
    }

    public static List<Integer> createItem(int type,int id,int count){
        List<Integer> list = new ArrayList<>();
        list.add(type);
        list.add(id);
        list.add(count);
        return list;
    }

    public static <T> List<T> createList(T... t){
        List<T> list = new ArrayList();
        if(Objects.nonNull(t)){
            Arrays.stream(t).forEach(o -> list.add(o));
        }
        return list;
    }

    public static List<List<Integer>> createAwards(List<List<Integer>> list,int multiple) {
        if (isBlank(list)) {
            return Collections.EMPTY_LIST;
        }
        if (multiple <= 0) {
            return list;
        }
        List<List<Integer>> list0 = new ArrayList<>();
        list.forEach(tmps -> {
            if (!tmps.isEmpty()) {
                List<Integer> tmps_ = new ArrayList<>(tmps);
                tmps_.set(2, tmps_.get(2) * multiple);
                list0.add(tmps_);
            }
        });
        return list0;
    }
}
