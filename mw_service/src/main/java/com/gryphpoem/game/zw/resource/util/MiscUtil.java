package com.gryphpoem.game.zw.resource.util;

/**
 * @ClassName MiscUtil.java
 * @Description
 * @author QiuKun
 * @date 2018年6月4日
 */
public abstract class MiscUtil {

    /**
     * 计算刷新次数和上次刷新时间
     * 
     * @param curCnt
     * @param maxCnt
     * @param lastTime
     * @param intervalTime
     * @param nowTime
     * @return 0位置新增的次数 , 1位置刷新时间
     */
    public static int[] calRefreshCntAndTime(final int curCnt, final int maxCnt, final int lastTime,
            final int intervalTime, final int nowTime) {

        int refreshTime = nowTime;
        int limitTime = nowTime - lastTime;
        int count = limitTime / intervalTime;
        if (count > 0) {
            // 加起来超过最大,按照最大值算
            if (curCnt + count >= maxCnt) {
                count = maxCnt - curCnt;
            }
            refreshTime = lastTime + count * intervalTime;
        }

        int[] res = new int[2];
        res[0] = count;
        res[1] = refreshTime;
        return res;
    }
}
