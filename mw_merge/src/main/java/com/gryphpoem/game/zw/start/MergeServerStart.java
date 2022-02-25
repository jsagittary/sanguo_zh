package com.gryphpoem.game.zw.start;

import java.lang.Thread.UncaughtExceptionHandler;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.ServerType;
import com.gryphpoem.game.zw.server.MergeServer;

/**
 * @ClassName ServerStart.java
 * @Description 程序入口
 * @author QiuKun
 * @date 2018年8月29日
 */
public class MergeServerStart {

    /**
     * 未捕获异常打印
     */
    public static void setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                LogUtil.error(t, e);
            }
        });
    }

    public static void main(String[] args) {
        ServerType.selfServerType = ServerType.SERVER_TYPE_MERGE;
        setDefaultUncaughtExceptionHandler();
        new Thread(MergeServer.getIns()).start();
    }
}
