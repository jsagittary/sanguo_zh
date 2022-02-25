package com.gryphpoem.game.zw.resource.pojo.activity;

import java.io.Serializable;

/**
 * @author xwind
 * @date 2021/12/27
 */
public class Fishing implements Serializable {
    private long key;
    private int cid;
//    private int x;
//    private int y;

    public Fishing(){}

    public Fishing(long key,int cid) {
        this.key = key;
        this.cid = cid;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

//    public int getX() {
//        return x;
//    }
//
//    public void setX(int x) {
//        this.x = x;
//    }
//
//    public int getY() {
//        return y;
//    }
//
//    public void setY(int y) {
//        this.y = y;
//    }
}
