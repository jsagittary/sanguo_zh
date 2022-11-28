package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.push.util.CheckNull;

import java.util.List;

/**
 * @author QiuKun
 * @ClassName NpcForce.java
 * @Description npc的阵型, 为了记录不能回满血的npc
 * @date 2019年1月17日
 */
public class NpcForce {
    private int npcId;// npc将领id
    private int hp;// 当前血量，兵力
    private int curLine;// 当前战斗的是第几排兵，从0开始
    private List<Integer> deputyNpcIdList;// 副将npcId列表

    public NpcForce(int npcId, int hp, int curLine, List<Integer> deputyNpcIdList) {
        this.npcId = npcId;
        this.hp = hp;
        this.curLine = curLine;
        this.deputyNpcIdList = deputyNpcIdList;
    }

    public NpcForce(int npcId, int hp, List<Integer> deputyNpcIdList) {
        this(npcId, hp, 0, deputyNpcIdList);
    }

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getCurLine() {
        return curLine;
    }

    public void setCurLine(int curLine) {
        this.curLine = curLine;
    }

    public boolean alive() {
        return hp > 0;
    }

    public CommonPb.Force toForcePb() {
        CommonPb.Force.Builder builder = CommonPb.Force.newBuilder();
        builder.setNpcId(npcId);
        builder.setCurLine(curLine);
        builder.setHp(hp);
        if (!CheckNull.isEmpty(this.deputyNpcIdList))
            builder.addAllDeputyNpc(this.deputyNpcIdList);
        return builder.build();
    }

    @Override
    public String toString() {
        return "NpcForce [npcId=" + npcId + ", hp=" + hp + ", curLine=" + curLine + "]";
    }

}
