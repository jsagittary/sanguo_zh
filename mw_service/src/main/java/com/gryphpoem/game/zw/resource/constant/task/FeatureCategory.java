package com.gryphpoem.game.zw.resource.constant.task;

/**
 * @author xwind
 * @date 2021/4/21
 */
public enum  FeatureCategory {
    SAND_TABLE(1),//沙盘
    WAR_FIRE(2),//战火
    BERLIN(3)//圣域
    ;

    int category;

    FeatureCategory(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }
}
