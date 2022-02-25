package com.gryphpoem.game.zw.resource.constant;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-01-21 14:32
 * @Description: 行为埋点
 * @Modified By:
 */
public enum ActionPoint {

    CLICK_VIP_BUTTON_POINT(1,"CLICK_VIP_BUTTON_POINT"), //  点击VIP按钮
    CLICK_FIRSH_GIFT_POINT(2,"CLICK_FIRSH_GIFT_POINT"), //  点击首充礼包
    CLICK_VIP_GIFT_POINT(3,"CLICK_VIP_GIFT_POINT")  //  点击VIP礼包
    ;

    private int code;
    private String msg;

    ActionPoint(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
