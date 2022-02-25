package com.gryphpoem.game.zw.model.fort;

import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.model.player.CrossHero;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.HeroRevivalService;

/**
 * @ClassName RoleForce.java
 * @Description
 * @author QiuKun
 * @date 2019年5月17日
 */
public class RoleForce implements DelayRun, FortForce {

    private final CrossHero crossHero;
    private final int camp;

    public static RoleForce createInstance(CrossHero crossHero, int camp) {
        return new RoleForce(crossHero, camp);
    }

    public RoleForce(CrossHero crossHero, int camp) {
        this.crossHero = crossHero;
        this.camp = camp;
    }

    public CrossHero getCrossHero() {
        return crossHero;
    }

    public int getCamp() {
        return camp;
    }

    /**
     * 获取兵力
     * 
     * @return
     */
    public int getCount() {
        return crossHero.getCount();
    }

    public boolean isLive() {
        return crossHero.getCount() > 0;
    }

    public void subCount(int lost) {
        int cnt = crossHero.getCount() - lost;
        cnt = cnt < 0 ? 0 : cnt;
        crossHero.setCount(cnt);
        if (cnt <= 0) { // 死亡设置状态
            crossHero.setState(ArmyConstant.ARMY_STATE_CROSS_REVIVAL); // 设置复活中
            int now = TimeHelper.getCurrentSecond();
            crossHero.setRevivalTime(now + Constant.CROSS_REVIVE_HERO_TIME); // 60复活时间
        }
    }

    @Override
    public int deadlineTime() {
        return crossHero.getRevivalTime();
    }

    @Override
    public void deadRun(int runTime, DelayInvokeEnvironment env) {
        if (env instanceof HeroRevivalService) {
            HeroRevivalService service = (HeroRevivalService) env;
            service.roleForceRevival(this);
        }
    }

}
