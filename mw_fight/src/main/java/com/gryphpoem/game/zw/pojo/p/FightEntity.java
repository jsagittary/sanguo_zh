package com.gryphpoem.game.zw.pojo.p;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Description: 战斗实体
 * Author: zhangpeng
 * createTime: 2022-11-05 17:32
 */
@Data
@AllArgsConstructor
public class FightEntity implements Comparable<FightEntity> {
    /**
     * 玩家id
     */
    private long ownId;
    /**
     * 武将id
     */
    private int heroId;
    /**
     * 武将速度
     */
    private int speed;

    @Override
    public int compareTo(FightEntity o) {
        return o.getSpeed() > this.speed ? -1 : 1;
    }
}
