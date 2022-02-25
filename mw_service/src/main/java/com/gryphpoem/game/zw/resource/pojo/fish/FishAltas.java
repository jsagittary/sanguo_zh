package com.gryphpoem.game.zw.resource.pojo.fish;

public class FishAltas extends Altas {
    private int size;

    public FishAltas(int stamp, int id, int size,boolean isNew) {
        super(stamp, id);
        this.size = size;
        super.setNew(isNew);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "FishAltas{" +
                "size=" + size +
                "} " + super.toString();
    }
}
