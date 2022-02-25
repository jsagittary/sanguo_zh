package com.gryphpoem.game.zw.push;

import com.gryphpoem.game.zw.core.Server;
import com.gryphpoem.game.zw.core.executor.NonOrderedQueuePoolExecutor;

/**
 * @Description 消息推送服务
 * @author TanDonghai
 * @date 创建时间：2017年9月4日 下午9:57:39
 *
 */
public class PushServer extends Server {
    private NonOrderedQueuePoolExecutor pushExcutor = new NonOrderedQueuePoolExecutor(10);

    public PushServer() {
        super("PushServer");
    }

    @Override
    public String getGameType() {
        return "push";
    }

    @Override
    protected void stop() {

    }

    public void addMessage(PushMessage message) {
        pushExcutor.execute(new PushWorker(message));
    }
}
