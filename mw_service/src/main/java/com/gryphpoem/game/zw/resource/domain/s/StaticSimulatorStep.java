package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 人生模拟器步骤
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/27 15:19
 */
public class StaticSimulatorStep {

    private Long id;

    private Integer type; // 模拟器类型，同一组模拟器定义type一致

    private Long nextId; // 下一步id

    private List<List<Long>> choose; // 本步展示的选择, [[选项id, 跳转步id, 选择此项的百分比]], 没有不填, 支持多个, 如是最后一步, 跳转步id填0

    private Integer delay; // 仅限于周期城镇事件, 触发的下个步骤延迟时间。填1为次日8点触发, 填2则加一天, 依次类推

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }

    public List<List<Long>> getChoose() {
        return choose;
    }

    public void setChoose(List<List<Long>> choose) {
        this.choose = choose;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

}
