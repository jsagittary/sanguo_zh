package com.gryphpoem.game.zw.resource.util;

import java.util.List;

/**
 * @ClassName ServerIdHelper.java
 * @Description
 * @author QiuKun
 * @date 2019年3月26日
 */
public abstract class ServerIdHelper {

    /***
     * 判断区服是否包含其中
     * 
     * @param serverIdList
     * @param serverId
     * @return true表是包含
     */
    public static boolean checkServer(List<List<Integer>> serverIdList, int serverId) {
        for (List<Integer> list : serverIdList) {
            // 获取起始id和结束id
            if (list.size() >= 2) {
                int startServerId = list.get(0);
                int endServerId = list.get(1);
                if (startServerId <= endServerId && serverId >= startServerId && serverId <= endServerId) {
                    return true;
                }
            }
        }
        return false;
    }
}
