package com.gryphpoem.game.zw.push;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @Description 推送消息工作线程
 * @author TanDonghai
 * @date 创建时间：2017年9月4日 下午8:39:52
 *
 */
public class PushWorker implements Runnable {

    private PushMessage message;

    public PushWorker() {
    }

    public PushWorker(PushMessage message) {
        this.message = message;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try {
            PushUtil.pushMessage(message);
        } catch (Exception e) {
            LogUtil.error("推送消息出错", e);
        } finally {
            long end = System.currentTimeMillis();
            if (end - start > 50) {
                LogUtil.error("推送消息耗费时间过长, " + message + ", cost(毫秒):" + (end - start));
            }
        }

    }

}
