package com.gryphpoem.game.zw.start;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.ServerType;
import com.gryphpoem.game.zw.server.CrossServer;

/**
 * @ClassName Start.java
 * @Description 跨服程序启动
 * @author QiuKun
 * @date 2019年4月29日
 */
public class CrossStart {

    public static void main(String[] args) {
        ServerType.selfServerType = ServerType.SERVER_TYPE_CORSS;
        setDefaultUncaughtExceptionHandler();
        LogUtil.start("begin corss server!!!");
        new Thread(CrossServer.getIns()).start();
        terminateForWindows();
    }

    /** 未捕获异常处理 */
    public static void setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LogUtil.error(t, e);
            }
        });
    }

    public static void terminateForWindows() {
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
            System.out.println("press ENTER to call System.exit() and run the shutdown routine.");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}
