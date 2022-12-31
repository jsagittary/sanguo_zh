package com.gryphpoem.game.zw.resource.constant.task;

import com.google.common.collect.HashBasedTable;
import com.gryphpoem.game.zw.core.util.Turple;

import java.util.Map;

/**
 * desc: TODO
 * author: huangxm
 * date: 2022/5/30 11:46
 **/
public class TaskCone517Type {
    public static final HashBasedTable<Integer, Integer, Integer> condIds = HashBasedTable.create();


    public static int getCondId(int quality, int stage) {
        if (condIds.isEmpty()) ini();
        return condIds.contains(quality, stage) ? condIds.get(quality, stage) : 0;
    }

    public static Turple<Integer, Integer> getQualityAndStageByCondId(int condId) {
        if (condIds.isEmpty()) ini();
        Map<Integer, Map<Integer, Integer>> rowMap = condIds.rowMap();
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : rowMap.entrySet()) {
            int quality = entry.getKey();
            for (Map.Entry<Integer, Integer> entry1 : entry.getValue().entrySet()) {
                int stage = entry1.getKey();
                int id = entry1.getValue();
                if (id == condId) {
                    return new Turple<>(quality, stage);
                }
            }
        }
        return null;
    }

    /**
     * 515类型；1白、2绿、3蓝、4蓝+1、5蓝+2、6紫、7紫+1、8紫+2、9橙、10橙+1、11橙+2、12橙+3、13红、14红+1、15红+2、16红+3、17红+4；
     * 英雄的品质和品阶对应此任务的condId
     * r:品质  c:品阶  v:condId
     */
    public static void ini() {
        condIds.put(1, 0, 1);
        condIds.put(2, 0, 2);
        condIds.put(3, 0, 3);
        condIds.put(3, 1, 4);
        condIds.put(3, 2, 5);
        condIds.put(4, 0, 6);
        condIds.put(4, 1, 7);
        condIds.put(4, 2, 8);
        condIds.put(5, 0, 9);
        condIds.put(5, 1, 10);
        condIds.put(5, 2, 11);
        condIds.put(5, 3, 12);
        condIds.put(6, 0, 13);
        condIds.put(6, 1, 14);
        condIds.put(6, 2, 15);
        condIds.put(6, 3, 16);
        condIds.put(6, 4, 17);
    }
}
