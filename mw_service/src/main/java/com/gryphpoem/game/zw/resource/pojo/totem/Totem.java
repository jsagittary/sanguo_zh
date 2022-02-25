package com.gryphpoem.game.zw.resource.pojo.totem;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @author xwind
 * @date 2021/11/19
 */
public class Totem {
    private int totemKey;
    private int totemId;
    private int strengthen;//强化
    private int resonate;//共鸣
    private int lock = 1;//2锁 1不锁
    private int heroId;//装备的将领id

    public Totem() {
    }

    public Totem(int totemKey, int totemId) {
        this.totemKey = totemKey;
        this.totemId = totemId;
    }

    public CommonPb.TotemInfo ser(){
        CommonPb.TotemInfo.Builder builder = CommonPb.TotemInfo.newBuilder();
        builder.setTotemKey(totemKey);
        builder.setTotemId(totemId);
        builder.setQhLv(strengthen);
        builder.setGmLv(resonate);
        builder.setLock(lock);
        builder.setHeroId(heroId);
        return builder.build();
    }

    public void dser(CommonPb.TotemInfo totemInfo){
        this.setTotemKey(totemInfo.getTotemKey());
        this.setTotemId(totemInfo.getTotemId());
        this.setStrengthen(totemInfo.getQhLv());
        this.setResonate(totemInfo.getGmLv());
        this.setLock(totemInfo.getLock());
        this.setHeroId(totemInfo.getHeroId());
    }

    public int getTotemKey() {
        return totemKey;
    }

    public void setTotemKey(int totemKey) {
        this.totemKey = totemKey;
    }

    public int getTotemId() {
        return totemId;
    }

    public void setTotemId(int totemId) {
        this.totemId = totemId;
    }

    public int getStrengthen() {
        return strengthen;
    }

    public void setStrengthen(int strengthen) {
        this.strengthen = strengthen;
    }

    public int getResonate() {
        return resonate;
    }

    public void setResonate(int resonate) {
        this.resonate = resonate;
    }

    public int getLock() {
        return lock;
    }

    public void setLock(int lock) {
        this.lock = lock;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    @Override
    public String toString() {
        return "Totem{" +
                "totemKey=" + totemKey +
                ", totemId=" + totemId +
                ", strengthen=" + strengthen +
                ", resonate=" + resonate +
                ", lock=" + lock +
                ", heroId=" + heroId +
                '}';
    }
}
