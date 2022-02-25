package com.gryphpoem.game.zw.core.util;

import com.gryphpoem.cross.constants.GameServerConst;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-11-26 15:01
 */
public class JVMUtil {
    private static final long MB = 1024 * 1024;
    private static final String FGC = "PS MarkSweep";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");

    public static int getFGCCount() {
        List<GarbageCollectorMXBean> garbageList = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean garbage : garbageList) {
            String gcName = garbage.getName();
            if (gcName.equals(FGC)) {
                return (int) garbage.getCollectionCount();
            }
        }
        return -1;
    }

    public static int memoryUseRatio() {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage headMemory = memory.getHeapMemoryUsage();
        int max = (int) (headMemory.getMax() / MB);
        int use = (int) (headMemory.getUsed() / MB);
        return (int) (100d * use / max);
    }

    public static void collectMemoryInfo(Map<String, String> paramMap) {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        MemoryUsage headMemory = memory.getHeapMemoryUsage();
        int max = (int) (headMemory.getMax() / MB);
        int use = (int) (headMemory.getUsed() / MB);
        paramMap.put(GameServerConst.MEM_USE, String.valueOf(use));
        paramMap.put(GameServerConst.MEM_MAX, String.valueOf(max));
        double ratio = 100d * use / max;
        paramMap.put(GameServerConst.MEM_RATIO, DECIMAL_FORMAT.format(ratio));
    }

    public static void collectGarbageInfo(Map<String, String> paramMap) {
        List<GarbageCollectorMXBean> garbageList = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean garbage : garbageList) {
            String gcName = garbage.getName();
            if (gcName.equals(FGC)) {
                paramMap.put(GameServerConst.FGC_COUNT, String.valueOf(garbage.getCollectionCount()));
                paramMap.put(GameServerConst.FGC_TIME, String.valueOf(garbage.getCollectionTime()));
            }
        }
    }

}
