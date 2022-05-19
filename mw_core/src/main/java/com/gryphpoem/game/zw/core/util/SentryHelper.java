package com.gryphpoem.game.zw.core.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.executor.NonOrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.work.SentryWork;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;

/**
 * @Description Sentry（远程日志监控系统）帮助类
 * @author TanDonghai
 * @date 创建时间：2017年11月2日 下午3:42:18
 *
 */
public class SentryHelper {
    private SentryHelper() {
    }

    private static boolean initialized = false;

    /**
     * 非有序线程池
     */
    private static NonOrderedQueuePoolExecutor sendExcutor = new NonOrderedQueuePoolExecutor(3);

    public static void initSentry(String dsn) {
        if ("test".equals(DataResource.environment)) {
            LogUtil.start("test 环境 Sentry不进行初始化");
            return;
        }
        try {
            LogUtil.start("开始初始化Sentry, dsn:" + dsn);
            SentryClient sentryClient = SentryClientFactory.sentryClient(dsn, null);
            if (null != sentryClient) {
                Sentry.setStoredClient(sentryClient);
                sentryClient.setServerName(DataResource.serverName);
                sentryClient.addTag("game", "sanguo");
                sentryClient.addTag("game_desc", "三国");
                sentryClient.addTag("server", String.format("sanguo_%d", DataResource.serverId));
                sentryClient.addTag("environment", DataResource.environment);
                initialized = true;
            }
        } catch (Exception e) {
            LogUtil.error(e, "Sentry初始化失败, dsn:" + dsn);
        } finally {
            if (initialized) {
                LogUtil.start("Sentry已启动成功, dsn:" + dsn);
            }
        }
    }

    public static void sendToSentry(Throwable throwable) {
        if (initialized) {
            sendExcutor.execute(new SentryWork(throwable));
        }
    }

    public static void sendToSentry(String message) {
        if (initialized) {
            sendExcutor.execute(new SentryWork(message));
        }
    }
}
