package com.gryphpoem.game.zw.resource.pojo.fish;

public class Altas {
    private int stamp;//秒
    private int id;//配置id
    private boolean isNew;//是否新获得

    public Altas(int stamp, int id) {
        this.stamp = stamp;
        this.id = id;
    }

    public Altas(){}

    public int getStamp() {
        return stamp;
    }

    public void setStamp(int stamp) {
        this.stamp = stamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    @Override
    public String toString() {
        return "Altas{" +
                "stamp=" + stamp +
                ", id=" + id +
                ", isNew=" + isNew +
                '}';
    }
}
