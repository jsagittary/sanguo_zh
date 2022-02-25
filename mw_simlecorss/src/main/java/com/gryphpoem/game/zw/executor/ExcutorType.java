package com.gryphpoem.game.zw.executor;

/**
 * @ClassName ExcutorType.java
 * @Description 线程池类型
 * @author QiuKun
 * @date 2019年4月30日
 */
public enum ExcutorType {

    LOGIC("main-logic-pool-", 1) {
    },
    MSG("msg-pool-", 4) {
    },
    SAVE("save-pool-", 8) {
    };

    private String name;
    private int corePoolSize;

    private ExcutorType(String name, int corePoolSize) {
        this.name = name;
        this.corePoolSize = corePoolSize;
    }

    public String getName() {
        return name;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

}
