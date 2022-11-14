package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 幸福度配置
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/11 11:56
 */
public class StaticHappiness {

    private Integer id;

    /**
     * 范围, [最小值, 最大值]
     */
    private List<Integer> range;

    /**
     * 对应范围内, 对人口与资源的影响, 例：[[0, 1000], [0, 500]]，表示减少10%人口恢复速度，减少5%所有资源生产速度，0、1表示增加或减少，后一位表示万分比)
     */
    private List<List<Integer>> effective;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Integer> getRange() {
        return range;
    }

    public void setRange(List<Integer> range) {
        this.range = range;
    }

    public List<List<Integer>> getEffective() {
        return effective;
    }

    public void setEffective(List<List<Integer>> effective) {
        this.effective = effective;
    }

}
