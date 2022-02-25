package com.gryphpoem.game.zw.core;

import com.gryphpoem.game.zw.core.handler.DealType;

/**
 * @ClassName AbsLogicServer.java
 * @Description
 * @author QiuKun
 * @date 2018年3月26日
 */
public abstract class AbsLogicServer implements Runnable {

    public void addCommandByType(ICommand command, DealType dealType) {

    }

    public void addCommandByMainType(ICommand command) {
        addCommandByType(command, DealType.MAIN);
    }

    /**
     * 获取线程
     * 
     * @param dealType
     */
    public abstract Thread getThreadByDealType(DealType dealType);
}
