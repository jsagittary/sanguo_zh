package com.gryphpoem.game.zw.model.player;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.CrossHeroPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;

/**
 * @ClassName CrossHero.java
 * @Description 跨服将领
 * @author QiuKun
 * @date 2019年5月14日
 */
public class CrossHero {
    // 只读属性
    private CrossHeroPb msg;

    // 所有属性
    private final Map<Integer, Integer> attrMap = new HashMap<>();
    // 当前兵力
    private int count;
    // 将领状态 ,同游戏服中 hero的state
    private int state;
    // 所在的堡垒
    private int fortId;
    // 复活时间
    private int revivalTime;
    // 复活次数
    private int revivalCnt;

    private CrossHero() {
    }

    public static CrossHero newInstance(CrossHeroPb pb, int fortId) {
        CrossHero hero = new CrossHero();
        hero.msg = pb;
        Map<Integer, Integer> tmpAttr = pb.getAttrList().stream()
                .collect(Collectors.toMap(TwoInt::getV1, TwoInt::getV2, (oldV, newV) -> newV));
        hero.attrMap.putAll(tmpAttr);
        hero.setCount(pb.getCount());
        hero.setState(ArmyConstant.ARMY_STATE_CROSS);
        hero.setFortId(fortId);
        if(pb.hasRevivalCnt()){
            hero.setRevivalCnt(pb.getRevivalCnt());
        }
        return hero;
    }

    /**
     * 更新将领的属性
     * 
     * @param pb
     */
    public void refreshData(CrossHeroPb pb) {
        if (this.msg.getHeroId() != pb.getHeroId() || this.msg.getLordId() != pb.getLordId()) {
            return;
        }
        this.msg = pb;
        attrMap.clear();
        Map<Integer, Integer> tmpAttr = pb.getAttrList().stream()
                .collect(Collectors.toMap(TwoInt::getV1, TwoInt::getV2, (oldV, newV) -> newV));
        attrMap.putAll(tmpAttr);
    }

    public CrossHeroPb getMsg() {
        return msg;
    }

    public Map<Integer, Integer> getAttrMap() {
        return attrMap;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getLordId() {
        return msg.getLordId();
    }

    public int getHeroId() {
        return msg.getHeroId();
    }

    public int getHeroType() {
        return msg.getHeroType();
    }

    public int getLine() {
        return msg.getLine();
    }

    public int getLead() {
        return msg.getLead();
    }

    public int getIntensifyLv() {
        return msg.getIntensifyLv();
    }

    public int getRestrain() {
        return msg.getRestrain();
    }

    public int getFortId() {
        return fortId;
    }

    public void setFortId(int fortId) {
        this.fortId = fortId;
    }

    public int getRevivalTime() {
        return revivalTime;
    }

    public void setRevivalTime(int revivalTime) {
        this.revivalTime = revivalTime;
    }

    public int getRevivalCnt() {
        return revivalCnt;
    }

    public void setRevivalCnt(int revivalCnt) {
        this.revivalCnt = revivalCnt;
    }

    public CommonPb.FortHeroPb toFortHeroPb() {
        CommonPb.FortHeroPb.Builder builder = CommonPb.FortHeroPb.newBuilder();
        builder.setHeroId(getHeroId());
        builder.setCount(count);
        builder.setRevivalTime(revivalTime);
        builder.setState(state);
        builder.setFortId(fortId);
        builder.setRevivalCnt(revivalCnt);
        return builder.build();
    }
}
