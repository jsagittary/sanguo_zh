package com.gryphpoem.game.zw.resource.domain.p;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb.DbCia;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName Cia.java
 * @Description 情报部
 * @author QiuKun
 * @date 2018年6月4日
 */
public class Cia {
    // 女特工
    private Map<Integer, FemaleAgent> femaleAngets = new HashMap<>();
    private int interactionCnt;// 互动剩余次数
    private int lastTime; // 上一次修改的时间

    private int barrageTime;// 记录上一次弹幕发送的时间(不会存库)
    private int barrageId;// 记录上一次发送弹幕的id(不会存库)

    public Cia(int interactionCnt) {
        this.interactionCnt = interactionCnt;
        this.lastTime = TimeHelper.getCurrentSecond();
    }

    public Cia(DbCia ser) {
        setInteractionCnt(ser.getInteractionCnt());
        setLastTime(ser.getLastTime());
        List<CommonPb.FemaleAgent> femaleAgentList = ser.getFemaleAgentList();
        for (CommonPb.FemaleAgent fa : femaleAgentList) {
            femaleAngets.put(fa.getId(), new FemaleAgent(fa));
        }
    }

    public DbCia ser() {
        DbCia.Builder builder = DbCia.newBuilder();
        builder.setInteractionCnt(this.interactionCnt);
        builder.setLastTime(this.lastTime);
        for (FemaleAgent fa : femaleAngets.values()) {
            builder.addFemaleAgent(PbHelper.createFemaleAgent(fa));
        }
        return builder.build();
    }

    public void addInteractionCnt(int cnt) {
        if (cnt > 0) {
            this.interactionCnt += cnt;
            if (this.interactionCnt >= Constant.CIA_INTERACTION_MAX_CNT) {
                this.lastTime = TimeHelper.getCurrentSecond();
            }
        }
    }

    public int getInteractionCnt() {
        return interactionCnt;
    }

    public void setInteractionCnt(int interactionCnt) {
        this.interactionCnt = interactionCnt;
    }

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public Map<Integer, FemaleAgent> getFemaleAngets() {
        return femaleAngets;
    }

    public int getBarrageTime() {
        return barrageTime;
    }

    public void setBarrageTime(int barrageTime) {
        this.barrageTime = barrageTime;
    }

    public int getBarrageId() {
        return barrageId;
    }

    public void setBarrageId(int barrageId) {
        this.barrageId = barrageId;
    }

}
