package com.start;

import com.gryphpoem.game.zw.core.util.FileUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.server.AppGameServer;

import java.io.IOException;

public class ServerStart {

    public static void main(String[] args) {
        setDefaultUncaughtExceptionHandler();
        LogUtil.start("......GAME SERVER STARTING......");
        new Thread(AppGameServer.getInstance()).start();
        terminateForWindows();
    }

    /**
     * 未捕获异常处理
     */
    public static void setDefaultUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> LogUtil.error(t, e));
    }

    public static void terminateForWindows() {
        if (FileUtil.isWindows()) {
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
