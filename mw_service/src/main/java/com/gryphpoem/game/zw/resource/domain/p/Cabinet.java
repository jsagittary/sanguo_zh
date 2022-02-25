package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.SerializePb.SerCabinet;
import com.gryphpoem.game.zw.resource.constant.Constant;

/**
 * @ClassName Cabinet.java
 * @Description 内阁相关保存数据
 * @author QiuKun
 * @date 2017年7月17日
 */
public class Cabinet {

    private int prePlanId; // 上一个内阁天策府点兵id,没有为0
    private int curPlanId; // 当前内阁天策府点兵id
    private int leadStep;// 当前点兵任务下,击杀点兵统领的个数
    private boolean isFinsh;// 是否完成当前点兵任务,true 为完成,如果未完成effect加成就读取上一个点兵的id
    private boolean isCreateLead;// 当前点兵任务是否已经创建过点兵统领,true已经创建过
    private boolean isLvFinish; // 当前点兵级别任务是否完成,true完成

    public Cabinet() {
        init();
    }

    private void init() {
        setCurPlanId(Constant.CABINET_INIT_ID);
        setPrePlanId(0);
        setLeadStep(0);
        setFinsh(false);
        setLvFinish(false);
        setCreateLead(false);
    }

    public Cabinet(SerCabinet ser) {
        if (ser.hasCurPlanId()) {
            setCurPlanId(ser.getCurPlanId());
        } else {
            init();
        }
        if (ser.hasLeadStep()) {
            setLeadStep(ser.getLeadStep());
        }
        if (ser.hasIsFinsh()) {
            setFinsh(ser.getIsFinsh());
        }
        if (ser.hasIsCreateLead()) {
            setCreateLead(ser.getIsCreateLead());
        }
        if (ser.hasPrePlanId()) {
            setPrePlanId(ser.getPrePlanId());
        }
        if (ser.hasIsLvFinish()) {
            setLvFinish(ser.getIsLvFinish());
        }
    }

    /**
     * 获取可以加层的点兵id
     * 
     * @return 如果返回0 说明没有加成的点兵id
     */
    public int getEffectPlanId() {
        return isFinsh ? curPlanId : prePlanId;
    }

    public int getPrePlanId() {
        return prePlanId;
    }

    public void setPrePlanId(int prePlanId) {
        this.prePlanId = prePlanId;
    }

    public int getCurPlanId() {
        return curPlanId;
    }

    public void setCurPlanId(int curPlanId) {
        this.curPlanId = curPlanId;
    }

    public int getLeadStep() {
        return leadStep;
    }

    public void setLeadStep(int leadStep) {
        this.leadStep = leadStep;
    }

    public boolean isFinsh() {
        return isFinsh;
    }

    public void setFinsh(boolean isFinsh) {
        this.isFinsh = isFinsh;
    }

    public boolean isCreateLead() {
        return isCreateLead;
    }

    public void setCreateLead(boolean isCreateLead) {
        this.isCreateLead = isCreateLead;
    }

    public boolean isLvFinish() {
        return isLvFinish;
    }

    public void setLvFinish(boolean isLvFinish) {
        this.isLvFinish = isLvFinish;
    }

}
