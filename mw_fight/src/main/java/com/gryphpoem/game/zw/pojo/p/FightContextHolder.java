package com.gryphpoem.game.zw.pojo.p;

import lombok.Data;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-31 15:42
 */
@Data
public class FightContextHolder {
    private static final ThreadLocal<FightContext> threadLocal = new ThreadLocal<>();

    public FightContextHolder() {
        FightContext context = new FightContext();
        threadLocal.set(context);
    }

    public FightContext getContext() {
        return threadLocal.get();
    }

    public void battleEnd() {
        threadLocal.remove();
    }
}
