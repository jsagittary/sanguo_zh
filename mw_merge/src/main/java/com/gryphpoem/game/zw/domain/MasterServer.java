package com.gryphpoem.game.zw.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName MergMasterServer.java
 * @Description 合服之后主服的信息
 * @author QiuKun
 * @date 2018年9月7日
 */
public class MasterServer implements CheckLegal {
    private int serverId; // 主服务器
    private List<ElementServer> composeServer; // 本服务器的组成

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public List<ElementServer> getComposeServer() {
        return composeServer;
    }

    public void setComposeServer(List<ElementServer> composeServer) {
        this.composeServer = composeServer;
    }

    /**
     * 是否有某个阵营
     * 
     * @param camp
     * @return
     */
    public boolean hasElementCamp(int camp) {
        if (composeServer != null && !composeServer.isEmpty()) {
            return composeServer.stream().filter(es -> es.getCamp() == camp).count() > 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MergMasterServer [serverId=" + serverId + ", composeServer=" + composeServer + "]";
    }

    @Override
    public boolean checkLegal() {
        if (serverId != 0 && composeServer != null && !composeServer.isEmpty()) {
            // 检查元素是否合法
            for (ElementServer es : composeServer) {
                if (!es.checkLegal()) {
                    LogUtil.error("元素服务器数据有误: ", es);
                    return false;
                }
            }
            // 检查是否有重复
            int distinctSize = (int) composeServer.stream().distinct().count(); // 去重之后的数量
            if (composeServer.size() != distinctSize) {
                LogUtil.error("主服务器的组成的元素服务器有重复  主服务器 serverId:", this.serverId);
                return false;
            }
            // 检查阵营的正确性
            Set<Integer> campSet = composeServer.stream().map(elem -> elem.getCamp()).distinct()
                    .collect(Collectors.toSet());
            for (int camp = 1; camp <= 3; camp++) {
                if (!campSet.contains(camp)) {
                    LogUtil.error("主服务器缺少阵营  camp:", camp, " serverId:", this.serverId);
                    return false;
                }
            }
            // 验证正确
            return true;
        }
        return false;
    }

}
