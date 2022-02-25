package com.gryphpoem.game.zw.resource.constant.task;

/**
 * 任务类别枚举
 * @author xwind
 * @date 2021/4/20
 */
public enum TaskCategory {
    TREASURY(1),//宝库任务
    ;

    int category;

    TaskCategory(int category) {
        this.category = category;
    }

    public int getCategory() {
        return category;
    }
}
