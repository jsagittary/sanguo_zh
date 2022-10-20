package com;

import com.gryphpoem.game.zw.core.util.FileUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.server.AppGameServer;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

import java.io.IOException;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-18 18:58
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@ComponentScan("com.gryphpoem.game.zw")
@EnableDubbo
@EnableConfigurationProperties
@ImportResource(locations = {"classpath:pDaoBean.xml", "classpath:sDaoBean.xml"})
public class SanGuoApplication {
    public static void main(String... args) {
        setDefaultUncaughtExceptionHandler();
        new Thread(AppGameServer.getInstance(args)).start();
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
