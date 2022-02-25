package com.gryphpoem.game.zw.resource.constant;

/**
 * @author xwind
 * @date 2021/3/17
 */
public enum EArmyType {
    Infantry(1),//步兵
    Cavalry(2),//骑兵
    Archer(3)//弓兵
    ;

    private int type;

    EArmyType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static EArmyType get(int type){
        for (EArmyType value : values()) {
            if(value.getType() == type){
                return value;
            }
        }
        return null;
    }
}
