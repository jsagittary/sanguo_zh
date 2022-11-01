package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 人生模拟器步骤
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/27 15:19
 */
public class StaticSimulatorStep {

    private long id;

    private int type; // 模拟器类型，同一组模拟器定义type一致

    private long nextId; // 下一步id

    private List<List<Long>> choose; // 本步展示的选择, [[选项id, 跳转步id, 选择此项的百分比]], 没有不填, 支持多个, 如是最后一步, 跳转步id填0

    private int delay; // 仅限于周期城镇事件, 触发的下个步骤延迟时间。填1为次日8点触发, 填2则加一天, 依次类推

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getNextId() {
        return nextId;
    }

    public void setNextId(long nextId) {
        this.nextId = nextId;
    }

    public List<List<Long>> getChoose() {
        return choose;
    }

    public void setChoose(List<List<Long>> choose) {
        this.choose = choose;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

}
