package com.gryphpoem.game.zw.resource.domain.p;

import java.util.HashMap;
import java.util.Map;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.Stone;
import com.gryphpoem.game.zw.resource.pojo.StoneHole;
import com.gryphpoem.game.zw.resource.pojo.StoneImprove;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

/**
 * @ClassName StoneInfo.java
 * @Description 玩家宝石相关的信息
 * @author QiuKun
 * @date 2018年5月8日
 */
public class StoneInfo {
    // 宝石
    private Map<Integer, Stone> stones = new HashMap<>();
    // 宝石的孔位
    private Map<Integer, StoneHole> stoneHoles = new HashMap<>();
    // 进阶后的宝石
    private Map<Integer, StoneImprove> stoneImproves = new HashMap<>();

    // 今天已攻打宝石副本次数
    private int attackCombatCnt;
    // 今天宝石副本购买次数
    private int buyCombatCnt;

    public StoneInfo() {
    }

    public StoneInfo(CommonPb.StoneInfo ser) {
        if (!CheckNull.isEmpty(ser.getStoneList())) {
            ser.getStoneList().forEach(s -> {
                stones.put(s.getStoneId(), new Stone(s));
            });
        }
        if (!CheckNull.isEmpty(ser.getStoneHoleList())) {
            ser.getStoneHoleList().forEach(s -> {
                stoneHoles.put(s.getHoleIndex(), new StoneHole(s));
            });
        }
        if (!CheckNull.isEmpty(ser.getStoneImproveList())) {
            ser.getStoneImproveList().forEach(s -> {
                StoneImprove stoneImprove = new StoneImprove();
                stoneImprove.dser(s);
                stoneImproves.put(stoneImprove.getKeyId(), stoneImprove);
            });
        }
        if (ser.hasAttackCombatCnt()) {
            attackCombatCnt = ser.getAttackCombatCnt();
        }
        if (ser.hasBuyCombatCnt()) {
            buyCombatCnt = ser.getBuyCombatCnt();
        }
    }

    public CommonPb.StoneInfo ser() {
        CommonPb.StoneInfo.Builder bulider = CommonPb.StoneInfo.newBuilder();
        stones.values().forEach(s -> {
            bulider.addStone(PbHelper.createStonePb(s));
        });
        stoneHoles.values().forEach(s -> {
            bulider.addStoneHole(PbHelper.createStoneHolePb(s));
        });
        stoneImproves.values().forEach(s -> {
            bulider.addStoneImprove(s.ser());
        });

        bulider.setAttackCombatCnt(attackCombatCnt);
        bulider.setBuyCombatCnt(buyCombatCnt);
        return bulider.build();
    }

    public Map<Integer, Stone> getStones() {
        return stones;
    }

    public void setStones(Map<Integer, Stone> stones) {
        this.stones = stones;
    }

    public Map<Integer, StoneHole> getStoneHoles() {
        return stoneHoles;
    }

    public void setStoneHoles(Map<Integer, StoneHole> stoneHoles) {
        this.stoneHoles = stoneHoles;
    }

    public int getAttackCombatCnt() {
        return attackCombatCnt;
    }

    public void setAttackCombatCnt(int attackCombatCnt) {
        this.attackCombatCnt = attackCombatCnt;
    }

    public int getBuyCombatCnt() {
        return buyCombatCnt;
    }

    public void setBuyCombatCnt(int buyCombatCnt) {
        this.buyCombatCnt = buyCombatCnt;
    }

    public void addBuyCombatCnt(int cnt) {
        this.buyCombatCnt += cnt;
    }

    public void addAttackCombatCnt(int cnt) {
        this.attackCombatCnt += cnt;
    }

    public void cleanCnt() {
        this.attackCombatCnt = 0;
        this.buyCombatCnt = 0;
    }

    public Map<Integer, StoneImprove> getStoneImproves() {
        return stoneImproves;
    }

    public void setStoneImproves(Map<Integer, StoneImprove> stoneImproves) {
        this.stoneImproves = stoneImproves;
    }

}
