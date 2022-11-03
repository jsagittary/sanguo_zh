package com.gryphpoem.game.zw.pojo.p;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-01 14:42
 */
@Data
@AllArgsConstructor
public class FightEffectData {
    private long buffKeyId;
    private int buffId;
    /**
     * element:<万分比数值, 固定数值>
     */
    private List<Integer> data;
    /**
     * 效果固定值
     */
    private int value;
    /**
     * 效果施加顺序
     */
    private int index;

    public FightEffectData(long buffKeyId, int buffId, List<Integer> data) {
        this.buffKeyId = buffKeyId;
        this.buffId = buffId;
        this.data = data;
    }
}
