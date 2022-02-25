package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.resource.constant.BuildingType;

public class Resource implements Cloneable {
    private long lordId;
    private long food;// 粮食
    private long elec;// 电
    private long oil;// 油
    private long ore;// 矿石
    private long arm1;// 兵营兵
    private long arm2;// 坦克兵
    private long arm3;// 装甲兵
    private long foodOut;// 当前粮食产出数量
    private long elecOut;//
    private long oilOut;//
    private long oreOut;//
    private int foodOutF;// 当前粮食产出增加百分比
    private int elecOutF;//
    private int oilOutF;//
    private int oreOutF;//
    private long foodMax;// 当前最大可容纳粮食数量
    private long elecMax;//
    private long oilMax;//
    private long oreMax;//
    private int storeF;// 容量额外百分比
    private long tFood;// 玩家获取的粮食总量记录，只增不减
    private long tElec;//
    private long tOil;//
    private long tOre;//
    private long human;// 人口
    private int humanTime;// 上次人口刷新时间
    private int storeTime;// 上次刷新资源秒数
    private long uranium;// 铀

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public long getFood() {
        return food;
    }

    public void setFood(long food) {
        this.food = food;
    }

    public long getElec() {
        return elec;
    }

    public void setElec(long elec) {
        this.elec = elec;
    }

    public long getOil() {
        return oil;
    }

    public void setOil(long oil) {
        this.oil = oil;
    }

    public long getOre() {
        return ore;
    }

    public void setOre(long ore) {
        this.ore = ore;
    }

    public long getFoodOut() {
        return foodOut;
    }

    public void setFoodOut(long foodOut) {
        this.foodOut = foodOut;
    }

    public long getElecOut() {
        return elecOut;
    }

    public void setElecOut(long elecOut) {
        this.elecOut = elecOut;
    }

    public long getOilOut() {
        return oilOut;
    }

    public void setOilOut(long oilOut) {
        this.oilOut = oilOut;
    }

    public long getOreOut() {
        return oreOut;
    }

    public void setOreOut(long oreOut) {
        this.oreOut = oreOut;
    }

    public int getFoodOutF() {
        return foodOutF;
    }

    public void setFoodOutF(int foodOutF) {
        this.foodOutF = foodOutF;
    }

    public int getElecOutF() {
        return elecOutF;
    }

    public void setElecOutF(int elecOutF) {
        this.elecOutF = elecOutF;
    }

    public int getOilOutF() {
        return oilOutF;
    }

    public void setOilOutF(int oilOutF) {
        this.oilOutF = oilOutF;
    }

    public int getOreOutF() {
        return oreOutF;
    }

    public void setOreOutF(int oreOutF) {
        this.oreOutF = oreOutF;
    }

    public long getFoodMax() {
        return foodMax;
    }

    public void setFoodMax(long foodMax) {
        this.foodMax = foodMax;
    }

    public long getElecMax() {
        return elecMax;
    }

    public void setElecMax(long elecMax) {
        this.elecMax = elecMax;
    }

    public long getOilMax() {
        return oilMax;
    }

    public void setOilMax(long oilMax) {
        this.oilMax = oilMax;
    }

    public long getOreMax() {
        return oreMax;
    }

    public void setOreMax(long oreMax) {
        this.oreMax = oreMax;
    }

    public int getStoreF() {
        return storeF;
    }

    public void setStoreF(int storeF) {
        this.storeF = storeF;
    }

    public long gettFood() {
        return tFood;
    }

    public void settFood(long tFood) {
        this.tFood = tFood;
    }

    public long gettElec() {
        return tElec;
    }

    public void settElec(long tElec) {
        this.tElec = tElec;
    }

    public long gettOil() {
        return tOil;
    }

    public void settOil(long tOil) {
        this.tOil = tOil;
    }

    public long gettOre() {
        return tOre;
    }

    public void settOre(long tOre) {
        this.tOre = tOre;
    }

    public int getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(int storeTime) {
        this.storeTime = storeTime;
    }

    public long getArm1() {
        return arm1;
    }

    public void setArm1(long arm1) {
        this.arm1 = arm1;
    }

    public long getArm2() {
        return arm2;
    }

    public void setArm2(long arm2) {
        this.arm2 = arm2;
    }

    public long getArm3() {
        return arm3;
    }

    public void setArm3(long arm3) {
        this.arm3 = arm3;
    }

    public long getHuman() {
        return human;
    }

    public void setHuman(long human) {
        this.human = human;
    }

    public int getHumanTime() {
        return humanTime;
    }

    public void setHumanTime(int humanTime) {
        this.humanTime = humanTime;
    }

    public long getUranium() {
        return uranium;
    }

    public void setUranium(long uranium) {
        this.uranium = uranium;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getArmByTrain(int type) {
        long arm = 0;
        switch (type) {
            case BuildingType.TRAIN_FACTORY_1:
                arm = getArm1();
                break;
            case BuildingType.TRAIN_FACTORY_2:
                arm = getArm2();
                break;
            case BuildingType.TRAIN_FACTORY_3:
                arm = getArm3();
                break;
        }
        return arm;
    }
}
