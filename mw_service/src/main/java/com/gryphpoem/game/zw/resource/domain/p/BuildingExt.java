package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName BuildingExt.java
 * @Description 非源建筑的额外属性
 * @author QiuKun
 * @date 2017年10月28日
 */
public class BuildingExt {
    private int id;         // 建筑的id
    private int type;       // 建筑类型,非资源建筑的type等于id
    private boolean unlock; // 是否已解锁 true为解锁
    private int unLockTime; // 解锁时间

    public BuildingExt() {
    }

    public BuildingExt(int id) {
        this.id = id;
    }

    public BuildingExt(int id, int type) {
        this.id = id;
        this.type = type;
    }

    public BuildingExt(int id, int type, boolean unlock) {
        this.id = id;
        this.type = type;
        this.unlock = unlock;
    }

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

    public boolean isUnlock() {
        return unlock;
    }

    public void setUnlock(boolean unlock) {
        this.unlock = unlock;
    }

    public int getUnLockTime() {
        return unLockTime;
    }

    public void setUnLockTime(int unLockTime) {
        this.unLockTime = unLockTime;
    }

    @Override
    public String toString() {
        return "BuildingExt [id=" + id + ", type=" + type + ", unlock=" + unlock + "]";
    }

}
