package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.SendServer;
import com.gryphpoem.game.zw.core.thread.SendThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.server.thread.SendEventDataThread;

import java.util.Iterator;


public class SendEventDataServer extends SendServer {
	private static SendEventDataServer ins = new SendEventDataServer();

	private SendEventDataServer() {
		super("SEND_EVENT_DATA_SERVER", 10);
	}

	public static SendEventDataServer getIns() {
		return ins;
	}

	public SendThread createThread(String name) {
		return new SendEventDataThread(name);
	}

    @Override
    public void sendData(String body) {
		int type = (int) (Math.random() * (10 - 1 + 1)); //（数据类型）(最小值 + Math.random()*(最大值-最小值+1) );
//		LogUtil.error(type);
		SendThread thread = threadPool.get(Math.abs( (type % threadNum)));
        if (thread == null) {
            LogUtil.error("can not find thread:" + type);
            thread = threadPool.get(0);
        }
        thread.add(type,body);
    }

	public void interruptAll(){
		Iterator<SendThread> it = threadPool.values().iterator();
		while (it.hasNext()) {
			it.next().interrupt();
		}
	}
}
