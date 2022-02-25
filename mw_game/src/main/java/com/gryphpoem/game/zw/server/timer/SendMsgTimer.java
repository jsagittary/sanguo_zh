package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;

public class SendMsgTimer extends TimerEvent {
	public SendMsgTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
//		LinkedBlockingQueue<Msg> msg_queue = MsgDataManager.getIns().getMsg_queue();
//		while (msg_queue.size() > 0) {
//			Msg msg = msg_queue.poll();
//			if (msg == null || msg.getCtx() == null) {
//				break;
//			}
//			AppGameServer.getInstance().synMsgToGamer(msg);
//		}
	}
}
