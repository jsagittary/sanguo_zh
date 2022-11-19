package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.constant.FightConstant;
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
    /**
     * 是否是进攻方
     */
    private boolean attack;
    /**
     * 主将还是副将 {@link com.gryphpoem.game.zw.constant.FightConstant.HeroType}
     */
    private int heroType;

    @Override
    public int compareTo(FightEntity o) {
        if (o.getSpeed() > this.speed) {
            return 1;
        } else if (o.getSpeed() == this.speed) {
            if (this.attack) {
                // 当前实例为进攻方
                if (o.attack) {
                    // 比较方也是进攻方时
                    return this.heroType == FightConstant.HeroType.PRINCIPAL_HERO ? -1 : 1;
                } else {
                    // 比较方也是防守方
                    return -1;
                }
            } else {
                // 当前实例为防守方
                if (o.attack) {
                    // 比较方为进攻方
                    return 1;
                } else {
                    // 比较方也为防守方
                    return this.heroType == FightConstant.HeroType.PRINCIPAL_HERO ? -1 : 1;
                }
            }
        } else {
            return -1;
        }
    }
}
