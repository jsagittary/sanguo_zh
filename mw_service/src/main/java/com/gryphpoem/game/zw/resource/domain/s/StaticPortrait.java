package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticPortrait.java
 * @Description 角色头像表
 * @author QiuKun
 * @date 2017年8月3日
 */
public class StaticPortrait {

    /**
     * 免费头像
     */
    public final static int UNLOCK_FREE = 0;
    /**
     * VIP礼包
     */
    public final static int UNLOCK_VIP_GIFT_BUY = 1;
    /**
     * 柏林会战霸主专属头像
     */
    public final static int UNLOCK_TYPE_WINNER = 3;
    /**
     * VIP等级
     */
    public final static int UNLOCK_VIP_LV = 4;
    /**
     * 将领解锁
     */
    public final static int UNLOCK_HERO = 5;
    /**
     * 跨服战火头名解锁
     */
    public final static int UNLOCK_CROSS_WAR_FIRE = 6;

    private int id;
    private int unlock;// 解锁状态 0 初始化解锁, 1 活动获取解锁 3.柏林会战霸主专属头像
    private List<Integer> param;// 对应的参数

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    private List<List<Integer>> consume;// 转换的道具

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUnlock() {
        return unlock;
    }

    public void setUnlock(int unlock) {
        this.unlock = unlock;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "StaticPortrait{" +
                "id=" + id +
                ", unlock=" + unlock +
                ", param=" + param +
                ", consume=" + consume +
                '}';
    }

}
