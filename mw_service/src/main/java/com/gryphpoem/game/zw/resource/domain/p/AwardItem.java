package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @author xwind
 * @date 2021/3/17
 */
public class AwardItem {
    private int type;
    private int id;
    private int count;

    public AwardItem(){}

    public AwardItem(int type, int id, int count) {
        this.type = type;
        this.id = id;
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "AwardItem{" +
                "type=" + type +
                ", id=" + id +
                ", count=" + count +
                '}';
    }
}
