package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticProp.java
 * @Description 道具配置表
 * @author TanDonghai
 * @date 创建时间：2017年3月28日 下午2:07:36
 *
 */
public class StaticProp {
    private int propId;
    private int quality;// 品质
    private int propType;// 道具类型
    private List<List<Integer>> rewardList;// 使用后获得的物品，[[type,id,count]...]
    private int duration;// 使用后持续时间，单位：秒
    private List<Integer> attrs;// 属性
    private int price;// 初始单价
    private int useLv;// 使用道具时玩家最小等级
    private int useVip;// 物品使用vip等级
    private int chip;// 碎片数量，适用于propType=9，自动转换成rewardList里的(一个)道具
    private int season;//
    private List<Integer> key;
    private int batchUse; // 道具是否可批量使用

    public int getChip() {
        return chip;
    }

    public void setChip(int chip) {
        this.chip = chip;
    }

    public List<Integer> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<Integer> attrs) {
        this.attrs = attrs;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getPropType() {
        return propType;
    }

    public void setPropType(int propType) {
        this.propType = propType;
    }

    public List<List<Integer>> getRewardList() {
        return rewardList;
    }

    public void setRewardList(List<List<Integer>> rewardList) {
        this.rewardList = rewardList;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getUseLv() {
        return useLv;
    }

    public void setUseLv(int useLv) {
        this.useLv = useLv;
    }

    public int getUseVip() {
        return useVip;
    }

    public void setUseVip(int useVip) {
        this.useVip = useVip;
    }

    public List<Integer> getKey() {
        return key;
    }

    public void setKey(List<Integer> key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "StaticProp{" +
                "propId=" + propId +
                ", quality=" + quality +
                ", propType=" + propType +
                ", rewardList=" + rewardList +
                ", duration=" + duration +
                ", attrs=" + attrs +
                ", price=" + price +
                ", useLv=" + useLv +
                ", useVip=" + useVip +
                ", chip=" + chip +
                '}';
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public boolean canBatchUse() {
        return this.batchUse == 1;
    }

}
