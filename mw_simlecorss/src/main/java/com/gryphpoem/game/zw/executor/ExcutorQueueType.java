package com.gryphpoem.game.zw.executor;

/**
 * @ClassName ExcutorQueueType.java
 * @Description 线程池对应的队列
 * @author QiuKun
 * @date 2019年4月30日
 */
public enum ExcutorQueueType {

    LOGIC_MAIN(1, "游戏逻辑的主队列") {
    },
    MSG_SEND(1, "发消息队列") {
    },
    MSG_RECV(2, "接收息队列") {
    },

    SAVE_1(1, "数据存储队列1,用于保存公共数据") {
    },
    SAVE_2(2, "数据存储队列2") {
    },
    SAVE_3(3, "数据存储队列3") {
    },
    SAVE_4(4, "数据存储队列4") {
    },
    SAVE_5(5, "数据存储队列5") {
    },
    SAVE_6(6, "数据存储队列6") {
    },
    SAVE_7(7, "数据存储队列7") {
    },
    SAVE_8(8, "数据存储队列8") {
    },;

    private int type;
    private String name;

    private ExcutorQueueType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
