package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticCrossFort.java
 * @Description 跨服中的堡垒
 * @author QiuKun
 * @date 2019年5月11日
 */
public class StaticCrossFort {
    /** 大本营堡垒 */
    public static final int TYPE_CAMP = 1;
    /** 普通堡垒 */
    public static final int TYPE_NORMAL = 2;

    private int id; // 堡垒id
    private int type;// 堡垒类型 1 大本营堡垒,2 普通堡垒
    private int camp;// 默认的阵营
    private String name; // 默认的阵营
    private List<Integer> neighbor; // 相邻的堡垒的id 格式[id,id]
    private List<Integer> form; // 阵型 格式:[npcId,npcId]

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getNeighbor() {
        return neighbor;
    }

    public void setNeighbor(List<Integer> neighbor) {
        this.neighbor = neighbor;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    /**
     * 是否为大本营堡垒
     * 
     * @return
     */
    public boolean isCampType() {
        return this.type == TYPE_CAMP;
    }

    @Override
    public String toString() {
        return "StaticCrossFort [id=" + id + ", type=" + type + ", camp=" + camp + ", name=" + name + ", neighbor="
                + neighbor + ", form=" + form + "]";
    }

}
