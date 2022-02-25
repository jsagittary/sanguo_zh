package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.server.SendMsgServer;

public class MsgDataManager {
    // private LinkedBlockingQueue<Msg> msg_queue = new LinkedBlockingQueue<Msg>();
    private static MsgDataManager ins = new MsgDataManager();

    private MsgDataManager() {
    }

    public static MsgDataManager getIns() {
        return ins;
    }

    public void add(Msg msg) {
        // msg_queue.add(msg);
        if (SendMsgServer.getIns() != null) {
            SendMsgServer.getIns().synMsgToPlayer(msg);
        }
    }

    // public LinkedBlockingQueue<Msg> getMsg_queue() {
    // return msg_queue;
    // }
    //
    // public void setMsg_queue(LinkedBlockingQueue<Msg> msg_queue) {
    // this.msg_queue = msg_queue;
    // }
}
