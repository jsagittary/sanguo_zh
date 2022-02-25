package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticPayPlat.java
 * @Description 支付平台类型
 * @author QiuKun
 * @date 2017年9月25日
 */
public class StaticPayPlat {
    private int id;
    private String plat;// 平台的名称
    private int platCode;// 平台编码
    private int type;// 平台类型,0 android平台, 1 ios平台

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public int getPlatCode() {
        return platCode;
    }

    public void setPlatCode(int platCode) {
        this.platCode = platCode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "StaticPayPlat [id=" + id + ", plat=" + plat + ", platCode=" + platCode + ", type=" + type + "]";
    }

}
