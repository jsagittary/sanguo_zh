package com.gryphpoem.game.zw.task;

import com.gryphpoem.game.zw.network.session.SessionGroup;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;

/**
 * @ClassName SendMsgTask.java
 * @Description 发送消息
 * @author QiuKun
 * @date 2019年5月14日
 */
public class SendMsgTask extends PoolAbstractTask {

    private static final Recycler<SendMsgTask> RECYCLER = new Recycler<SendMsgTask>() {
        @Override
        protected SendMsgTask newObject(Handle handle) {
            return new SendMsgTask(handle);
        }
    };
    private SessionGroup group;
    private Base msg;

    public static SendMsgTask newInstance(SessionGroup group, Base msg) {
        SendMsgTask task = RECYCLER.get();
        task.reuse(group, msg);
        return task;
    }

    private final void reuse(SessionGroup group, Base msg) {
        reuse();
        this.group = group;
        this.msg = msg;
    }

    public SendMsgTask(Handle handle) {
        super(handle);
    }

    @Override
    public void work() {
        if (group != null && msg != null) {
            group.write(msg);
        }
    }

    @Override
    protected Recycler<?> recycler() {
        return RECYCLER;
    }

}
