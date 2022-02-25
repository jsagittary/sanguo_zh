package com.gryphpoem.game.zw.mgr;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.executor.DelayTaskWheelTimer.RemoteCallBack;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRs;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName RewardMgr.java
 * @Description 跨服加减资源使用
 * @author QiuKun
 * @date 2019年5月21日
 */
@Component
public class RewardMgr {

    public void addAward(RemoteCallBack callBack, CrossPlayer player, List<Integer> award, AwardFrom from, int cmd) {
        List<List<Integer>> rewardList = new ArrayList<>(1);
        rewardList.add(award);
        addAwardList(callBack, player, rewardList, from, cmd);
    }

    public void subAward(RemoteCallBack callBack, CrossPlayer player, List<Integer> award, AwardFrom from, int cmd) {
        List<List<Integer>> rewardList = new ArrayList<>(1);
        rewardList.add(award);
        subAwardList(callBack, player, rewardList, from, cmd);
    }

    /**
     * 加某些资源
     * 
     * @param callBack
     * @param player
     * @param rewardList
     * @param from
     */
    public void addAwardList(RemoteCallBack callBack, CrossPlayer player, List<List<Integer>> rewardList,
            AwardFrom from, int cmd) {
        opAward(callBack, player, rewardList, from, true, cmd);
    }

    /**
     * 扣某些资源
     * 
     * @param callBack
     * @param player
     * @param rewardList
     * @param from
     */
    public void subAwardList(RemoteCallBack callBack, CrossPlayer player, List<List<Integer>> rewardList,
            AwardFrom from, int cmd) {
        opAward(callBack, player, rewardList, from, false, cmd);
    }

    private void opAward(RemoteCallBack callBack, CrossPlayer player, List<List<Integer>> rewardList, AwardFrom from,
            boolean isAdd, int cmd) {
        long taskId = ExecutorPoolMgr.getIns().addRemoteCallBack(callBack, null);
        CrossAwardOpRs.Builder builder = CrossAwardOpRs.newBuilder();
        builder.setTaskId(taskId);
        builder.setReqAwards(PbCrossUtil.createCrossAwardPb(rewardList, from, isAdd));
        builder.setRollBack(false);
        builder.setCmd(cmd);
        PbMsgUtil.sendOkMsgToPlayer(player, CrossAwardOpRs.EXT_FIELD_NUMBER, CrossAwardOpRs.ext, builder.build());
    }
}
