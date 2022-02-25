package com.gryphpoem.game.zw.dataMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticBehaviorTree;
import com.gryphpoem.game.zw.resource.domain.s.StaticBtreeNode;

/**
 * @Description 机器人相关配置数据
 * @author TanDonghai
 * @date 创建时间：2017年9月16日 下午5:15:20
 *
 */
public class StaticRobotDataMgr {
    private StaticRobotDataMgr() {
    }

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static Map<Integer, StaticBehaviorTree> treeMap;// 行为树简单信息

    private static Map<Integer, StaticBtreeNode> nodeMap;// 行为树的具体节点配置

    private static Map<Integer, List<StaticBtreeNode>> treeNodeMap;// 行为树的所有节点

    public static void init() {
        Map<Integer, StaticBehaviorTree> treeMap = staticDataDao.selectBehaviorTreeMap();
        StaticRobotDataMgr.treeMap = treeMap;

        List<StaticBtreeNode> nodeList = staticDataDao.selectBtreeNodeList();
        Map<Integer, List<StaticBtreeNode>> treeNodeMap = new HashMap<>();
        Map<Integer, StaticBtreeNode> nodeMap = new HashMap<>();
        List<StaticBtreeNode> list;
        for (StaticBtreeNode sbn : nodeList) {
            nodeMap.put(sbn.getNodeId(), sbn);

            list = treeNodeMap.get(sbn.getTreeId());
            if (null == list) {
                list = new ArrayList<>();
                treeNodeMap.put(sbn.getTreeId(), list);
            }
            list.add(sbn);
        }
        StaticRobotDataMgr.nodeMap = nodeMap;
        StaticRobotDataMgr.treeNodeMap = treeNodeMap;
    }

    public static Map<Integer, StaticBehaviorTree> getTreeMap() {
        return treeMap;
    }

    public static Map<Integer, StaticBtreeNode> getNodeMap() {
        return nodeMap;
    }

    public static Map<Integer, List<StaticBtreeNode>> getTreeNodeMap() {
        return treeNodeMap;
    }

}
