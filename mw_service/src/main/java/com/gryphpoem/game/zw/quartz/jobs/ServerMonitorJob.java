package com.gryphpoem.game.zw.quartz.jobs;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.lang.management.*;
import java.util.Arrays;
import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-11-10 14:39
 */
public class ServerMonitorJob extends AbsGameJob {

    private static final long MB = 1024 * 1024;
    private static final String FGC = "PS MarkSweep";

    @Override
    protected void process() {
        garbageCollectorInfo();
        memoryInfo();
    }

    private static void garbageCollectorInfo() {
        try {
            List<GarbageCollectorMXBean> garbageList = ManagementFactory.getGarbageCollectorMXBeans();
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String pid = runtime.getName().split("@")[0];
            for (GarbageCollectorMXBean garbage : garbageList) {
                String gcName = garbage.getName();
                long gcCount = garbage.getCollectionCount();
                long gcTime = garbage.getCollectionTime();
                String gcMemName = Arrays.deepToString(garbage.getMemoryPoolNames());
                String gcInfo = String.format("PID: %s, 垃圾收集器: %s, 执行次数: %d, 总花费时间: %d, 执行内存区域: %s",
                        pid, gcName, gcCount, gcTime, gcMemName);
                LogUtil.common(gcInfo);
                if (gcName.equals(FGC) && gcCount > 30) {//参考值计算: 平均14天更新一次, 一天2次FGC
                    long uptime = runtime.getUptime();
                    long upDay = (uptime / TimeHelper.DAY_MS) + 1;
                    long gcCountByDay = gcCount / upDay;
                    if (gcCountByDay > 3) {
                        LogUtil.error2Sentry(String.format("pid: %s, FGC 执行异常!!! 累计执行次数: %d, 总花费时间: %d, 启动后平均每天执行次数: %d", pid, gcCount, gcTime, gcCountByDay));
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private static void memoryInfo() {
        try {
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            MemoryUsage headMemory = memory.getHeapMemoryUsage();
            int init = (int) (headMemory.getInit() / MB);
            int committed = (int) (headMemory.getCommitted() / MB);
            int max = (int) (headMemory.getMax() / MB);
            int use = (int) (headMemory.getUsed() / MB);
            double useRatio = 1.0d * use / max;
            String memInfo = String.format("堆(head)内存使用情况 >>> init(M): %d, commit(M): %d, max(M): %d, use(M): %d, 堆内存使用率: %d/%d = %f",
                    init, committed, max, use, use, max, useRatio);
            if (useRatio > 0.9d) {
                LogUtil.error2Sentry(String.format("服务器堆内内存异常: %s", memInfo));
            } else {
                LogUtil.common(memInfo);
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

//    private static void printMemoryManagerInfo() {
//        try {
//            List<MemoryManagerMXBean> managers = ManagementFactory.getMemoryManagerMXBeans();
//            if (!managers.isEmpty()) {
//                for (MemoryManagerMXBean manager : managers) {
//                    System.out.println("vm内存管理器：名称=" + manager.getName() + ",管理的内存区="
//                            + Arrays.deepToString(manager.getMemoryPoolNames()) + ",ObjectName=" + manager.getObjectName());
//                }
//            }
//        } catch (Exception e) {
//            LogUtil.error("", e);
//        }
//    }

}
