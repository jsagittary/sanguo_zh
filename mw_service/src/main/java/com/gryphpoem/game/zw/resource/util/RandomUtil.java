package com.gryphpoem.game.zw.resource.util;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

public class RandomUtil {

    /**
     * 一个列表随机中取出n个元素
     * 
     * @param originList
     * @param n
     * @return
     */
    public static <T> List<T> getListRandom(List<T> originList, int n) {
        List<T> resList = new ArrayList<>();
        if (originList == null || originList.isEmpty()) {
            return resList;
        }
        int size = originList.size();
        if (size < n) {
            resList.addAll(originList);
            return resList;
        }
        Set<Integer> distinctSet = new HashSet<>();
        for (int i = 0; i < n; i++) {
            int index = RandomHelper.randomInSize(size);
            if (distinctSet.contains(index)) {
                i--;// 如果重复再来一次
            } else {
                resList.add(originList.get(index));
                distinctSet.add(index);
            }
        }
        return resList;
    }

    /**
     * 一个列表随机中按权重取出n个元素
     *
     * @param list
     * @param count
     * @param weightPos 权重字段所在的位置,从0开始
     * @return 返回命中的元素,返回null 说明格式有错误
     */
    public static List<List<Integer>> getListRandomWeight(List<List<Integer>> list, int count,int weightPos) {
        List<List<Integer>> resList = new ArrayList<>();
        if (CheckNull.isEmpty(list) || weightPos < 0) {
            // 参数格式有错误
            return null;
        }
        int size = list.size();
        if (size <= count) {
            for (List<Integer> wc : list){
                List<Integer> notwgt = new ArrayList<>();
                for (int i = 0; i < weightPos; i++) {
                    notwgt.add(wc.get(i));
                }
                resList.add(notwgt);
            }
            return resList;            
        }
        int weightSum = 0;
        // 计算权重总值
        for (List<Integer> wc : list) {
            if (wc.size() != weightPos + 1) {
                return null;
            }
            weightSum += wc.get(weightPos);
        }
        if (weightSum <= 0) {
            return null;
        }
        List<List<Integer>> copyList = new ArrayList<>();
        copyList.addAll(list); //为了不影响原来的奖励列表
        while (count>0) {
            int n = RandomHelper.randomInSize(weightSum); // [0,weightSum)
            int m = 0;
            int x = 0; //已经命中的权重
            List<Integer> rmWc = null; //已经命中的奖励
            for (List<Integer> wc : copyList) {
                if (m <= n && n < m + wc.get(weightPos)) {
                    // 返回不含有权重数据的集合
                    List<Integer> notWeight = new ArrayList<>();
                    for (int i = 0; i < weightPos; i++) {
                        notWeight.add(wc.get(i));
                    }
                    resList.add(notWeight);
                    x = wc.get(weightPos);
                    rmWc = wc;
                }
                m += wc.get(weightPos);
            }
            if(rmWc!=null) {
                copyList.remove(rmWc); //从拷贝的列表中移除已经命中的奖励
            }
            weightSum-=x;
            count--;
        }
        return resList;
    }

    /**
     * 随机[[key,pro]]
     * 
     * @param map 随机源
     * @param rmKey 不包含其中的key
     * @return
     */
    public static int getKeyByMap(Map<Integer, Integer> map, int rmKey) {
        int totalWight = 0;
        for (Entry<Integer, Integer> kv : map.entrySet()) {
            if (rmKey == kv.getKey()) {
                continue;
            }
            totalWight += kv.getValue();
        }
        int random = RandomHelper.randomInSize(totalWight);

        int temp = 0;
        for (Entry<Integer, Integer> kv : map.entrySet()) {
            if (rmKey == kv.getKey()) {
                continue;
            }
            temp += kv.getValue();
            if (temp >= random) {
                return kv.getKey();
            }
        }
        return 0;
    }

    public static <T> T randomByWeight(List<T> list,Function<T,Integer> weight){
        return getWeightByList(list,weight);
    }

    /**
     * 带泛型的获取权重
     * 
     * @param list
     * @param weight
     * @return
     */
    public static <T> T getWeightByList(List<T> list, Function<T, Integer> weight) {
        if (CheckNull.isEmpty(list)) {
            // 参数格式有错误
            return null;
        }
        int weightSum = 0;
        // 计算权重总值
        for (T wc : list) {
            weightSum += weight.apply(wc);
        }

        if (weightSum <= 0) {
            return null;
        }

        int n = RandomHelper.randomInSize(weightSum); // [0,weightSum)
        int m = 0;
        for (T wc : list) {
            // 命中
            if (m <= n && n < m + weight.apply(wc)) {
                return wc;
            }
            m += weight.apply(wc);
        }
        return null;
    }

    public static List<Integer> randomAwardByWeight(List<List<Integer>> list){
        if(ListUtils.isBlank(list)){
            return null;
        }
        int tweight = 0;
        for (List<Integer> integers : list) {
            tweight += integers.get(3);
        }
        int n = RandomHelper.randomInSize(tweight);
        int m = 0;
        for (List<Integer> o : list) {
            // 命中
            if (m <= n && n < m + o.get(3)) {
                return o;
            }
            m += o.get(3);
        }
        return null;
    }

    public static int randomAwardIdxByWeight(List<List<Integer>> list){
        if(ListUtils.isBlank(list)){
            return -1;
        }
        int tweight = 0;
        for (List<Integer> integers : list) {
            tweight += integers.get(3);
        }
        int n = RandomHelper.randomInSize(tweight);
        int m = 0;
        int idx = 0;
        for (List<Integer> o : list) {
            // 命中
            if (m <= n && n < m + o.get(3)) {
                return idx;
            }
            m += o.get(3);
            idx ++;
        }
        return -1;
    }

    /**
     * 随机[[key,val,pro]]
     * 
     * @param list 随机源
     * @param rmKey 不包含其中的key
     * @return
     */
    public static List<Integer> getAwardByRandomId(List<List<Integer>> list, int rmKey) {
        int totalWight = 0;
        for (List<Integer> obj : list) {
            if (obj.size() < 3) {
                return null;
            }
            if (rmKey == obj.get(0)) {
                continue;
            }
            totalWight += obj.get(3);
        }
        int random = RandomHelper.randomInSize(totalWight);

        int temp = 0;
        for (List<Integer> obj : list) {
            if (rmKey == obj.get(0)) {
                continue;
            }
            temp += obj.get(3);
            if (temp >= random) {
                return obj;
            }
        }
        return null;
    }

    /**
     * 获取带有权重的随机值 [[val,weight],[val,weight]]
     * 
     * @param list 随机列表
     * @return 如果返回 null 说明参数格式有错误
     */
    public static Integer getRandomByWeight(List<List<Integer>> list) {
        if (CheckNull.isEmpty(list)) {
            // 参数格式有错误
            return null;
        }
        int weightSum = 0;
        // 计算权重总值
        for (List<Integer> wc : list) {
            if (wc.size() != 2) {
                // 参数格式有错误
                return null;
            }
            weightSum += wc.get(1);
        }

        if (weightSum <= 0) {
            return 0;
        }

        int n = RandomHelper.randomInSize(weightSum); // [0,weightSum)
        int m = 0;
        for (List<Integer> wc : list) {
            if (m <= n && n < m + wc.get(1)) {
                return wc.get(0);
            }
            m += wc.get(1);
        }

        return 0;
    }

    /**
     * 获取带有权重的随机值,必须最后一个值为权重[[val1,val2,weight],[val1,val2,weight]]
     * 
     * @param list
     * @param weightPos 权重字段所在的位置,从0开始
     * @param returnHasWeight 返回命中的元素中是否含有权重字段,false为不含有
     * @return 返回命中的元素,返回null 说明格式有错误
     */

    public static List<Integer> getRandomByWeight(List<List<Integer>> list, int weightPos, boolean returnHasWeight) {
        if (CheckNull.isEmpty(list) || weightPos < 0) {
            // 参数格式有错误
            return null;
        }
        int weightSum = 0;
        // 计算权重总值
        for (List<Integer> wc : list) {
            if (wc.size() != weightPos + 1) {
                return null;
            }
            weightSum += wc.get(weightPos);
        }
        if (weightSum <= 0) {
            return null;
        }
        int n = RandomHelper.randomInSize(weightSum); // [0,weightSum)
        int m = 0;
        for (List<Integer> wc : list) {
            if (m <= n && n < m + wc.get(weightPos)) {
                // 命中
                if (returnHasWeight) {
                    return wc;
                } else {
                    // 返回不含有权重数据的集合
                    List<Integer> notWeight = new ArrayList<>();
                    for (int i = 0; i < weightPos; i++) {
                        notWeight.add(wc.get(i));
                    }
                    return notWeight;
                }
            }
            m += wc.get(weightPos);
        }
        return null;
    }

    /**
     * 获取带有权重的随机值,必须最后一个值为权重[[val1,val2,weight],[val1,val2,weight]]
     * 
     * @param list
     * @param weightPos 权重字段所在的位置,从0开始
     * @param returnHasWeight 返回命中的元素中是否含有权重字段,false为不含有
     * @param wSum 设定的权重总值
     * @return 返回命中的元素,返回null 说明格式有错误
     */

    public static List<Integer> getRandomByWeightAndRatio(List<List<Integer>> list, int weightPos,
            boolean returnHasWeight, int wSum) {
        if (CheckNull.isEmpty(list) || weightPos < 0) {
            // 参数格式有错误
            return null;
        }
        int weightSum = wSum;
        if (weightSum <= 0) {
            return null;
        }
        int n = RandomHelper.randomInSize(weightSum); // [0,weightSum)
        int m = 0;
        for (List<Integer> wc : list) {
            if (m <= n && n < m + wc.get(weightPos)) {
                // 命中
                if (returnHasWeight) {
                    return wc;
                } else {
                    // 返回不含有权重数据的集合
                    List<Integer> notWeight = new ArrayList<>();
                    for (int i = 0; i < weightPos; i++) {
                        notWeight.add(wc.get(i));
                    }
                    return notWeight;
                }
            }
            m += wc.get(weightPos);
        }
        return null;
    }

    /**
     * 随机生成几个 互斥的数, min<= Num <max
     * 
     * @param cnt 生成随机数的个数
     * @param min
     * @param max
     * @return
     */
    public static List<Integer> getRandomNums(int cnt, int min, int max) {
        List<Integer> numArr = new ArrayList<>();
        if (min < 0 || max < 1 || cnt < 1 || min > max) {
            return numArr;
        }
        int realCnt = Math.min(cnt, max - min);
        for (int i = 0; i < realCnt;) {
            int temp = RandomHelper.randomInArea(min, max);
            if (!numArr.contains(temp)) {
                numArr.add(temp);
                i++;
            }
        }
        return numArr;
    }

    /**
     * 随机生成几个 互斥的数, min<= Num <max
     * 
     * @param cnt
     * @param max
     * @return
     */
    public static List<Integer> getRandomNums(int cnt, int max) {
        return getRandomNums(cnt, 0, max);
    }

    public static int randomIntIncludeEnd(int start,int end){
        return RandomUtils.nextInt(start,end + 1);
    }

    public static int randomIntExcludeEnd(int start,int end){
        return RandomUtils.nextInt(start,end);
    }

    public static <T> List<T> randomList(List<T> list,int n){
        List<T> rlist = new ArrayList<>();
        if(ListUtils.isBlank(list)){
            return rlist;
        }
        if(list.size() <= n){
            rlist.addAll(list);
            return rlist;
        }
        List<T> tmps = new ArrayList<>(list);
        for(int i=0;i<n;i++){
            int idx = randomIntExcludeEnd(0,tmps.size());
            rlist.add(tmps.remove(idx));
        }
        return rlist;
    }
}
