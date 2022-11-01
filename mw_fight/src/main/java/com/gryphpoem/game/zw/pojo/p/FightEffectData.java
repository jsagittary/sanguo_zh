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
}
