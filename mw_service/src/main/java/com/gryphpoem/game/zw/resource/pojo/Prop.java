package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName Prop.java
 * @Description 道具
 * @author TanDonghai
 * @date 创建时间：2017年3月28日 下午2:31:02
 *
 */
public class Prop {
    private int propId; // 道具id
    private int count;
    private int useCount;// 已经使用次数
    private int useTime;// 最后一次使用时间

    public Prop() {
    }

    public Prop(CommonPb.Prop prop) {
        setPropId(prop.getPropId());
        setCount(prop.getCount());
        setUseCount(prop.getUseCount());
        setUseTime(prop.getUseTime());
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public int getUseTime() {
        return useTime;
    }

    public void setUseTime(int useTime) {
        this.useTime = useTime;
    }

    /**
     * 新增使用次数
     * 
     * @param use
     */
    public void addUseCount(int use) {
        useCount += use;
    }

    @Override
    public String toString() {
        return "Prop [propId=" + propId + ", count=" + count + ", useCount=" + useCount + ", useTime=" + useTime + "]";
    }
}
