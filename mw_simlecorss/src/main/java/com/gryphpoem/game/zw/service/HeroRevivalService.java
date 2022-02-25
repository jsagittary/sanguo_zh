package com.gryphpoem.game.zw.service;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.mgr.PlayerMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.fort.RoleForce;
import com.gryphpoem.game.zw.model.player.CrossHero;
import com.gryphpoem.game.zw.pb.GamePb5.SyncFortHeroRs;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.util.CrossWarFinlishClear;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName HeroRevivalService.java
 * @Description 将领复活处理
 * @author QiuKun
 * @date 2019年5月24日
 */
@Component
public class HeroRevivalService implements DelayInvokeEnvironment, CrossWarFinlishClear {

    // 复活队列
    private DelayQueue<RoleForce> revivalQueue = new DelayQueue<>(this);

    @Autowired
    private PlayerMgr playerMgr;

    @Override
    @SuppressWarnings("rawtypes")
    public DelayQueue getDelayQueue() {
        return revivalQueue;
    }

    public void runSecProcess(JobExecutionContext context) {
        this.runSec();
    }

    public RoleForce findRoleForce(long lordId, int heroId) {
        return revivalQueue.getQueue().stream()
                .filter(r -> r.getCrossHero().getHeroId() == heroId && r.getCrossHero().getLordId() == lordId)
                .findFirst().orElse(null);
    }

    public RoleForce removeRoleForceFromQueue(long lordId, int heroId) {
        RoleForce roleForce = findRoleForce(lordId, heroId);
        if (roleForce != null) {
            revivalQueue.remove(roleForce);
        }
        return roleForce;
    }

    public boolean addRevivalQueue(RoleForce r) {
        return revivalQueue.add(r);
    }

    /**
     * 将领复活操作
     * 
     * @param roleForce
     */
    public void roleForceRevival(RoleForce roleForce) {
        CrossHero crossHero = roleForce.getCrossHero();
        crossHero.setRevivalTime(0);
        crossHero.setRevivalCnt(0);
        crossHero.setState(ArmyConstant.ARMY_STATE_IDLE); // 设置空闲
        CrossPlayer crossPlayer = playerMgr.getPlayer(crossHero.getLordId());
        SyncFortHeroRs.Builder builder = SyncFortHeroRs.newBuilder();
        builder.setHero(crossHero.toFortHeroPb());
        PbMsgUtil.sendOkMsgToPlayer(crossPlayer, SyncFortHeroRs.EXT_FIELD_NUMBER, SyncFortHeroRs.ext, builder.build());
        // 在跨服中提出该将领
        crossPlayer.getHeroModel().getHeros().remove(crossHero.getHeroId());

    }

    @Override
    public void clear() {
        revivalQueue.clearQueue();
        LogUtil.debug("将领复活队列清除");
    }
}
