package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticStoneHole.java
 * @Description 宝石孔位
 * @author QiuKun
 * @date 2018年5月7日
 */
public class StaticStoneHole {

    private int type;// 孔位
    private int needRoleLv;// 镶嵌时需要角色等级
    private int stoneType;// 可镶嵌石头的种类

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNeedRoleLv() {
        return needRoleLv;
    }

    public void setNeedRoleLv(int needRoleLv) {
        this.needRoleLv = needRoleLv;
    }

    public int getStoneType() {
        return stoneType;
    }

    public void setStoneType(int stoneType) {
        this.stoneType = stoneType;
    }

    @Override
    public String toString() {
        return "StaticStoneHole [type=" + type + ", needRoleLv=" + needRoleLv + ", stoneType=" + stoneType + "]";
    }

}
