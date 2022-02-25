package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.PlaneConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneUpgrade;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @author: ZhouJie
 * @date: Create in 2018-09-28 16:43
 * @description: 战机 注意:战机不会删除,改造直接变动战机Id
 * @modified By:
 */
public class WarPlane {

    private int heroId;             // 将领Id
    private int type;               // 战机type(不会变动)
    private int planeId;            // 战机Id(会变动,改造就是变动planeId)
    private int level;              // 等级
    private int exp;                // 当前等级经验, 只能通过Prop = '飞行员手册'增加经验
    private int heroPos;            // 将领的上阵位置, 1-4
    private int state;              // 战机状态，0 空闲，1 出征，2 采集
    private int battlePos;          // 战机的上阵位置, 1-4

    public WarPlane() {
    }

    public WarPlane(SerializePb.DbWarPlane dbWarPlane) {
        this.heroId = dbWarPlane.getHeroId();
        this.type = dbWarPlane.getType();
        this.planeId = dbWarPlane.getPlaneId();
        this.level = dbWarPlane.getLevel();
        this.exp = dbWarPlane.getExp();
        this.heroPos = dbWarPlane.getPos();
        this.state = dbWarPlane.getState();
        this.battlePos = dbWarPlane.getBattlePos();
    }

    /**
     * 序列化
     * @return
     */
    public SerializePb.DbWarPlane ser() {
        SerializePb.DbWarPlane.Builder builder = SerializePb.DbWarPlane.newBuilder();
        builder.setHeroId(this.heroId);
        builder.setType(this.type);
        builder.setPlaneId(this.planeId);
        builder.setLevel(this.level);
        builder.setExp(this.exp);
        builder.setPos(this.heroPos);
        builder.setState(this.state);
        builder.setBattlePos(this.battlePos);
        return builder.build();
    }

    /**
     * 战机下阵
     * @param hero
     */
    public void downBattle(Hero hero) {
        if (!CheckNull.isNull(hero) && heroId == hero.getHeroId()) {
            this.heroId = 0;
            this.heroPos = 0;
            this.battlePos = 0;
            this.state = PlaneConstant.PLANE_STATE_IDLE;
        }
    }

    /**
     * 战机上阵
     * @param hero      要上阵的将领
     * @param heroPos   要上阵的将领位置
     * @param battlePos 要上的战机队列位置
     */
    public void upBattle(Hero hero, int heroPos, int battlePos) {
        if (!CheckNull.isNull(hero)) {
            this.heroId = hero.getHeroId();
            this.heroPos = heroPos;
            this.battlePos = battlePos;
            this.state = PlaneConstant.PLANE_STATE_BATTLE;
        }
    }

    /**
     * 改造就是替换planeId
     * @param newPlane
     * @return 改造成功
     */
    public boolean remould(StaticPlaneUpgrade newPlane) {
        if (!CheckNull.isNull(newPlane)) {
            setPlaneId(newPlane.getPlaneId());
            return true;
        }
        return false;
    }

    /**
     * 战机升级
     * @return
     */
    public int levelUp() {
        this.level++;
        this.exp = 0;
        return level;
    }

    /**
     * 战机是否处于空闲状态
     * @return
     */
    public boolean isIdle () {
        return state == PlaneConstant.PLANE_STATE_IDLE;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPlaneId() {
        return planeId;
    }

    public void setPlaneId(int planeId) {
        this.planeId = planeId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getPos() {
        return heroPos;
    }

    public void setPos(int pos) {
        this.heroPos = pos;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getBattlePos() {
        return battlePos;
    }

    public void setBattlePos(int battlePos) {
        this.battlePos = battlePos;
    }
}
