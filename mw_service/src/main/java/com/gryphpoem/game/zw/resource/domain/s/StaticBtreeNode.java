package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

import com.hundredcent.game.ai.btree.base.INodeConfig;

/**
 * @Description AI行为树节点信息
 * @author TanDonghai
 * @date 创建时间：2017年9月16日 下午4:58:04
 *
 */
public class StaticBtreeNode implements INodeConfig {
    private int nodeId;//
    private String nodeName;// 节点名称，描述
    private int treeId;// 所属的行为树id
    private int nodeType;// 行为树节点类型，0 根节点，1 复合类型节点， 2 条件节点， 3 行为节点， 4 装饰类型节点
    private int subType;// 如果是复合类节点，具体的复合类型（1~8，详见表s_btree_node_composite_type_define），条件节点填写具体的条件节点类型，action节点填写具体的action节点类型，否则为0
    private List<Integer> children;// 子节点id列表，格式：[id1,id2...]
    private String param;// 具体的参数信息

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getTreeId() {
        return treeId;
    }

    public void setTreeId(int treeId) {
        this.treeId = treeId;
    }

    @Override
    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    @Override
    public List<Integer> getChildren() {
        return children;
    }

    public void setChildren(List<Integer> children) {
        this.children = children;
    }

    @Override
    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "StaticBtreeNode [nodeId=" + nodeId + ", nodeName=" + nodeName + ", treeId=" + treeId + ", nodeType="
                + nodeType + ", subType=" + subType + ", children=" + children + ", param=" + param + "]";
    }
}
