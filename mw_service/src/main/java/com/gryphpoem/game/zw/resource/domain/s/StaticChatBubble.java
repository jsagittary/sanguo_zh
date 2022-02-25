package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticChatBubble.java
 * @Description 聊天气泡
 * @author QiuKun
 * @date 2018年8月31日
 */
public class StaticChatBubble {
    /** 免费 */
    public static final int TYPE_FREE = 0;
    /** vip等级 */
    public static final int TYPE_VIP_LV = 1;
    /** 金币购买获得 */
    public static final int TYPE_GOLD = 2;
    /** 通过奖励配置获得 */
    public static final int TYPE_AWARD = 3;
    /** 活动获得 */
    public static final int TYPE_ACT_AWARD = 4;

    private int id; // 气泡ida
    private int type;// 气泡类型 0 免费, 1 vip等级达到获取, 2 金币购买获取， 3 通过奖励配置获得
    private int param;// 类型参数,type=1表示所需vip等级, type=2气泡消耗的金币
    private List<List<Integer>> consume;
    private List<Integer> needprop;

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

    public List<Integer> getNeedprop() {
        return needprop;
    }

    public void setNeedprop(List<Integer> needprop) {
        this.needprop = needprop;
    }

    @Override
    public String toString() {
        return "StaticChatBubble [id=" + id + ", type=" + type + ", param=" + param + "]";
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }
}
