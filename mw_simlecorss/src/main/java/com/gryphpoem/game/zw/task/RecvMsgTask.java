package com.gryphpoem.game.zw.task;

import com.gryphpoem.game.zw.cmd.base.Command;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.mgr.PlayerMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.CrossLoginRq;
import com.gryphpoem.game.zw.server.CrossServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;

/**
 * @ClassName RecvMsgTask.java
 * @Description 接收消息任务处理
 * @author QiuKun
 * @date 2019年5月14日
 */
public class RecvMsgTask extends PoolAbstractTask {

    private static final Recycler<RecvMsgTask> RECYCLER = new Recycler<RecvMsgTask>() {
        @Override
        protected RecvMsgTask newObject(Handle handle) {
            return new RecvMsgTask(handle);
        }
    };
    private Base base;
    private Command cmd;
    private ChannelHandlerContext ctx;

    public static RecvMsgTask newInstance(Command cmd, Base base, ChannelHandlerContext ctx) {
        RecvMsgTask task = RECYCLER.get();
        task.reuse(cmd, base, ctx);
        return task;
    }

    private void reuse(Command cmd, Base base, ChannelHandlerContext ctx) {
        reuse();
        this.base = base;
        this.cmd = cmd;
        this.ctx = ctx;
    }

    public RecvMsgTask(Handle recyclerHandle) {
        super(recyclerHandle);
    }

    @Override
    protected Recycler<?> recycler() {
        return RECYCLER;
    }

    @Override
    public void work() {
        if (base != null && cmd != null && ctx != null) {
            try {
                long lordId = base.getLordId(); // base有 lordId,说明是通过游戏服直接转发过来
                if (lordId > 0 && base.getCmd() != CrossLoginRq.EXT_FIELD_NUMBER) {
                    PlayerMgr playerMgr = CrossServer.ac.getBean(PlayerMgr.class);
                    CrossPlayer player = playerMgr.getPlayer(lordId);
                    if (player == null) {
                        LogUtil.error("跨服操作，玩家为空,lordId:" + lordId);
                        return;
                    }
                    cmd.execute(ctx, base, player);
                } else {
                    cmd.execute(ctx, base, null);
                }
            } catch (Throwable e) {
                LogUtil.error(e);
            }
        }
    }
}
