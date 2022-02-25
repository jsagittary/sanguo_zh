package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticBodyImage.java
 * @Description 玩家形象
 * @author QiuKun
 * @date 2018年9月1日
 */
public class StaticBodyImage {
    /** 免费 */
    public final static int TYPE_FREE = 0;
    /** 拥有某个头像就拥有该形象 */
    public final static int TYPE_PORTRAIT = 1;

    private int id;
    private int type;// 形象类型 0 免费, 1 拥有某个头像就拥有该形
    private int param;// 类型参数,type=1时所需要头像的id值

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

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "StaticBodyImage [id=" + id + ", type=" + type + ", param=" + param + "]";
    }

}
