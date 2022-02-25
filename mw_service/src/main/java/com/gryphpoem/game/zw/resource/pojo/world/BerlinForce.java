package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.fight.AttrData;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-20 14:47
 * @description: 柏林会战将领详细信息
 * @modified By:
 */
public class BerlinForce extends Force {

    private int atkOrDef;           // 进攻方 1, 防守方 2
    private int addMode;            // 普通加入 1, 强袭加入 2
    private long addTime;           // 加入时间
    private int immediatelyTime;    // 立即出击时间

    public BerlinForce(AttrData attrData, int type, int count, int lead, int heroId, int atkOrDef, int addMode,
            long addTime, int camp, long roleId, int intensifyLv, int effect, int immediatelyTime) {
        super(attrData, type, count, lead, heroId, roleId);
        super.camp = camp;
        super.intensifyLv = intensifyLv;
        super.effect = effect;
        this.atkOrDef = atkOrDef;
        this.addMode = addMode;
        this.addTime = addTime;
        this.immediatelyTime = immediatelyTime;
    }

    public BerlinForce(BerlinForce berlinForce) {

    }

    public int getAtkOrDef() {
        return atkOrDef;
    }

    public void setAtkOrDef(int atkOrDef) {
        this.atkOrDef = atkOrDef;
    }

    public int getAddMode() {
        return addMode;
    }

    public void setAddMode(int addMode) {
        this.addMode = addMode;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public int getImmediatelyTime() {
        return immediatelyTime;
    }

    public void setImmediatelyTime(int immediatelyTime) {
        this.immediatelyTime = immediatelyTime;
    }

    @Override public String toString() {
        return "BerlinForce{" + "id=" + id + ", hp=" + hp + ", maxHp=" + maxHp + ", atkOrDef=" + atkOrDef + ", armType="
                + armType + ", lead=" + lead + ", addMode=" + addMode + ", curLine=" + curLine + ", addTime=" + addTime
                + ", maxLine=" + maxLine + ", count=" + count + ", killed=" + killed + ", lost=" + lost + ", totalLost="
                + totalLost + ", attrData=" + attrData + ", fighter=" + fighter + ", addExp=" + addExp + ", ownerId="
                + ownerId + ", nick='" + nick + '\'' + ", hasFight=" + hasFight + ", camp=" + camp + ", roleType="
                + roleType + ", skillId=" + skillId + '}';
    }

    /**
     * 序列化
     *
     * @return
     */
    public CommonPb.BerlinForce ser() {
        CommonPb.BerlinForce.Builder builder = CommonPb.BerlinForce.newBuilder();
        builder.setAtkOrDef(getAtkOrDef());
        builder.setAddMode(getAddMode());
        builder.setAddTime(getAddTime());
        builder.setAttrData(attrData.ser());
        builder.setArmType(this.armType);
        builder.setTotalCount(this.hp);
        builder.setLead(this.lead);
        builder.setId(this.id);
        builder.setCamp(this.camp);
        builder.setOwnId(this.ownerId);
        builder.setIntensifyLv(this.intensifyLv);
        builder.setEffect(this.effect);
        builder.setImmediatelyTime(this.immediatelyTime);
        return builder.build();
    }
}
