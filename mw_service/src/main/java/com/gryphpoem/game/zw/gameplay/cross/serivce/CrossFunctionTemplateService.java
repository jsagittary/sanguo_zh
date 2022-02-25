package com.gryphpoem.game.zw.gameplay.cross.serivce;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import org.quartz.Scheduler;

import java.util.Date;

public abstract class CrossFunctionTemplateService {
    public abstract GeneratedMessage.Builder<GamePb6.GetCrossPlayerDataRs.Builder> getCrossPlayerData(Player player, int functionId);

    protected abstract int getFunctionId();

    protected abstract void executeSchedule(int planKey, String jobName);

    protected abstract void addSchedule(StaticCrossGamePlayPlan plan, Scheduler scheduler, Date now);
}
