package com;

import com.gryphpoem.game.zw.server.AppGameServer;
import com.start.ServerStart;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

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
    public static void main(String[] args) {
        SpringApplication.run(SanGuoApplication.class, args);
        // 移除不需要的shutDownHook
        AppGameServer.removeSpringShutdownHook();
        AppGameServer.removeDubboShutdownHook();
        new Thread(AppGameServer.ac.getBean(AppGameServer.class)).start();
        ServerStart.terminateForWindows();
    }
}
