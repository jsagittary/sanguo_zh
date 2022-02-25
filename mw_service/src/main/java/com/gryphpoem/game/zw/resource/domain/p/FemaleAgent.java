package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.CiaConstant;


/**
 * @author QiuKun
 * @ClassName FemaleAgent.java
 * @Description 女特工
 * @author QiuKun
 * @date 2018年6月5日
 */
public class FemaleAgent {

    private int id; // 特工id
    private int quality;// 特工品质,品质未0说明没有获得
    private int attrVal;// 基础属性值
    private int skillVal;// 技能数值 万分比
    private int exp; // 升至下一等级,当前经验
    private int status; // 解锁状态 0 未解锁 1 可解锁 2 已解锁
    private int appointmentCnt; //每日约会次数
    private int dailyFree;// 每日免费次数
    private int star; //星级

    public FemaleAgent(CommonPb.FemaleAgent ser) {
        setId(ser.getId());
        setQuality(ser.getQuality());
        setAttrVal(ser.getAttrVal());
        setSkillVal(ser.getSkillVal());
        setExp(ser.getExp());
        setStatus(ser.getStatus());
        setAppointmentCnt(ser.getAppointmentCnt());
        setStar(ser.getStar());
        setDailyFree(ser.getDailyFree());
    }

    public FemaleAgent(int id, int quality) {
        this.id = id;
        this.quality = quality;
    }

    public FemaleAgent(int id) {
        this.id = id;
        this.status = CiaConstant.AGENT_UNLOCK_STATUS_0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getAttrVal() {
        return attrVal;
    }

    public void setAttrVal(int attrVal) {
        this.attrVal = attrVal;
    }

    public int getSkillVal() {
        return skillVal;
    }

    public void setSkillVal(int skillVal) {
        this.skillVal = skillVal;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAppointmentCnt() {
        return appointmentCnt;
    }

    public void setAppointmentCnt(int appointmentCnt) {
        this.appointmentCnt = appointmentCnt;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getDailyFree() {
        return dailyFree;
    }

    public void setDailyFree(int dailyFree) {
        this.dailyFree = dailyFree;
    }

    @Override
    public String toString() {
        return "FemaleAgent{" +
                "id=" + id +
                ", quality=" + quality +
                ", attrVal=" + attrVal +
                ", skillVal=" + skillVal +
                ", exp=" + exp +
                ", status=" + status +
                ", appointmentCnt=" + appointmentCnt +
                ", dailyFree=" + dailyFree +
                ", star=" + star +
                '}';
    }

}
