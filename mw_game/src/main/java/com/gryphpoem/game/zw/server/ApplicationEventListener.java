package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.util.LogUtil;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 11:54
 */
@Component
public class ApplicationEventListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextClosedEvent) {
            while (!AppGameServer.getInstance().allSaveDone()) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    LogUtil.error("appGameServer 还未保存完, 睡眠3s");
                }
            }
        }
    }
}
