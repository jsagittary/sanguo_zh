package com.gryphpoem.game.zw.cmd.player;

import org.springframework.beans.factory.annotation.Autowired;

import com.gryphpoem.game.zw.cmd.base.Cmd;
import com.gryphpoem.game.zw.cmd.base.PlayerBaseCommond;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.executor.DelayTaskWheelTimer.DelayTask;
import com.gryphpoem.game.zw.mgr.ExecutorPoolMgr;
import com.gryphpoem.game.zw.mgr.SessionMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRs;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName CrossAwardOpCmd.java
 * @Description 扣除资源,回调数据
 * @author QiuKun
 * @date 2019年5月21日
 */
@Cmd(rqCmd = CrossAwardOpRq.EXT_FIELD_NUMBER)
public class CrossAwardOpCmd extends PlayerBaseCommond {

    @Autowired
    private SessionMgr sessionMgr;

    @Override
    public void execute(CrossPlayer player, Base base) throws Exception {
        CrossAwardOpRq msg = base.getExtension(CrossAwardOpRq.ext);
        long taskId = msg.getTaskId();
        // 有回调任务
        if (taskId > 0) {
            DelayTask task = ExecutorPoolMgr.getIns().getDelayTask(taskId);
            if (task != null) {
                if (msg.getSuccess()) {// 游戏服处理成功,执行任务
                    task.execute();
                }
            } else {// 回调任务 已经无效
                if (msg.getSuccess()) {// 游戏服处理成功,回滚扣除的资源
                    CrossAwardOpRs.Builder rollBack = CrossAwardOpRs.newBuilder();
                    rollBack.setTaskId(msg.getTaskId());
                    rollBack.setRollBack(true);
                    rollBack.setReqAwards(msg.getReqAwards());
                    sessionMgr.sendMsg(PbMsgUtil.okBase(CrossAwardOpRs.EXT_FIELD_NUMBER, player.getLordId(),
                            CrossAwardOpRs.ext, rollBack.build()).build(), player.getMainServerId());
                }
                LogUtil.debug("等待游戏服处理资源超时，任务已经取消");
            }
        }
    }

}
