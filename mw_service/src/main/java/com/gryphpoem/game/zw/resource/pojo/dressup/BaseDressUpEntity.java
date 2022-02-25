package com.gryphpoem.game.zw.resource.pojo.dressup;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.List;


/**
 * @description:
 * @author: zhou jie
 * @time: 2021/3/4 11:47
 */
public abstract class BaseDressUpEntity {

    /**
     * 对应装扮类型<br/>
     * {@link com.gryphpoem.game.zw.resource.constant.AwardType}
     */
    private final int type;

    /**
     * 装扮id, 每个类型有自己的定义</br>
     */
    private int id;

    /**
     * 是否永久拥有
     */
    private boolean permanentHas;

    /**
     * 拥有时间
     */
    private int duration;

    /**
     * 初始化构造
     *
     * @param type 类型
     * @param id   配置id
     */
    BaseDressUpEntity(int type, int id) {
        this.type = type;
        this.id = id;
        this.permanentHas = false;
        this.duration = 0;
    }

    /**
     * 初始化构造
     *
     * @param type         类型
     * @param id           配置id
     * @param permanentHas 是否永久拥有
     */
    BaseDressUpEntity(int type, int id, boolean permanentHas) {
        this.type = type;
        this.id = id;
        this.permanentHas = permanentHas;
        this.duration = 0;
    }

    /**
     * 初始化构造
     *
     * @param type     类型
     * @param id       配置id
     * @param duration 持续时间(秒), 如果为1, 则为永久获得
     */
    BaseDressUpEntity(int type, int id, int duration) {
        this.type = type;
        this.id = id;
        // 添加拥有时间
        addDuration(duration);
    }

    /**
     * 反序列化构造
     *
     * @param data 数据
     */
    public BaseDressUpEntity(CommonPb.DressUpEntity data) {
        this.type = data.getType();
        this.id = data.getId();
        this.permanentHas = data.getPermanentHas();
        this.duration = data.getDuration();
    }

    /**
     * 添加拥有时间
     *
     * @param addTime 拥有时间
     */
    public void addDuration(int addTime) {
        if (addTime == 1) {
            // 这里跟策划约定, 如果持续时间是1的话, 就是永久生效
            setPermanentHas(true);
            this.duration = 0;
        } else {
            if (this.duration <= 0) {
                this.duration = TimeHelper.getCurrentSecond() + addTime;
            } else {
                this.duration += addTime;
            }
        }
    }


    /**
     * 序列化装扮数据
     *
     * @return 装扮数据
     */
    public CommonPb.DressUpEntity.Builder toData() {
        CommonPb.DressUpEntity.Builder builder = CommonPb.DressUpEntity.newBuilder();
        builder.setType(type);
        builder.setId(id);
        builder.setPermanentHas(permanentHas);
        builder.setDuration(duration);
        return builder;
    }

    public void subDuration(int subTime) {
        this.duration -= subTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPermanentHas() {
        return permanentHas;
    }

    public void setPermanentHas(boolean permanentHas) {
        this.permanentHas = permanentHas;
    }

    public int getDuration() {
        return duration;
    }

    /**
     * 获取转换道具
     *
     * @return 转换道具
     */
    public abstract List<List<Integer>> convertProps();

    @Override
    public String toString() {
        return "BaseDressUpEntity{" +
                "type=" + type +
                ", id=" + id +
                ", permanentHas=" + permanentHas +
                ", duration=" + duration +
                '}';
    }
}
