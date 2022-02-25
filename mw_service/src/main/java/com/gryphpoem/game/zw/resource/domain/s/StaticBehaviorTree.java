package com.gryphpoem.game.zw.resource.domain.s;

import com.hundredcent.game.ai.btree.base.IBtreeConfig;

/**
 * @Description 行为树模版
 * @author TanDonghai
 * @date 创建时间：2017年9月20日 下午2:26:05
 *
 */
public class StaticBehaviorTree implements IBtreeConfig {
    private int treeId;
    private boolean valid;
    private String treeName;

    @Override
    public int getTreeId() {
        return treeId;
    }

    public void setTreeId(int treeId) {
        this.treeId = treeId;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    @Override
    public String toString() {
        return "StaticBehaviorTree [treeId=" + treeId + ", valid=" + valid + ", treeName=" + treeName + "]";
    }

}
