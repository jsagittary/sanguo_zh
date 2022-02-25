package com.gryphpoem.game.zw.resource.domain.s;

/**
 * 活动目录
 * 
 * @author tyler
 *
 */
public class StaticActivity {

    private int type;
    private String name;
    private int clean;
    private int isDisappear; // 领奖完活动是否消失，0不消失，1消失

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClean() {
        return clean;
    }

    public void setClean(int clean) {
        this.clean = clean;
    }

    public int getIsDisappear() {
        return isDisappear;
    }

    public void setIsDisappear(int isDisappear) {
        this.isDisappear = isDisappear;
    }

}
