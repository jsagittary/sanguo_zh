package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName DbGlobalData.java
 * @Description 跨服公共数据
 * @author QiuKun
 * @date 2019年6月14日
 */
public class DbGlobalData {
    private int type; // 类型
    private byte[] data;// 数据

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
