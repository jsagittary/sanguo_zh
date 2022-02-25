package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-15 16:28
 * @description: 闪电战Boss
 * @modified By:
 */
public class LightningWarBoss {
    private int id;
    private int pos;            // 坐标
    private Fighter fighter;    // 血量
    private int lastFightTime;  // 上次战斗时间
    private int status;         // 状态

    public LightningWarBoss() {
    }

    public LightningWarBoss(SerializePb.SerLightningWarBoss boss) {
        setId(boss.getCityId());
        setPos(boss.getPos());
        setStatus(boss.getStatus());
        setLastFightTime(boss.getLastFightTime());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Fighter getFighter() {
        return fighter;
    }

    public void setFighter(Fighter fighter) {
        this.fighter = fighter;
    }

    public int getLastFightTime() {
        return lastFightTime;
    }

    public void setLastFightTime(int lastFightTime) {
        this.lastFightTime = lastFightTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Fighter对象是否初始化,血量是否为零
     * @return
     */
    public boolean isNotInitOrDead() {
        return CheckNull.isNull(fighter) || currentHp() == 0;
    }

    /**
     * 当前血量
     * @return
     */
    public int currentHp() {
        return fighter.getTotal() - fighter.getLost() <= 0 ? 0 : fighter.getTotal() - fighter.getLost();
    }
}
