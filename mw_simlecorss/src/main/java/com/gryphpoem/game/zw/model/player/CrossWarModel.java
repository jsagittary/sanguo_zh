package com.gryphpoem.game.zw.model.player;

/**
 * @ClassName CrossWarModel.java
 * @Description 玩家跨服的一些数据
 * @author QiuKun
 * @date 2019年5月28日
 */
public class CrossWarModel extends BasePlayerModel {

    private int killNum = 0;// 本轮的杀敌数

    @Override
    public PlayerModelType getModelType() {
        return PlayerModelType.CROSS_WAR_MODEL;
    }

    public int getKillNum() {
        return killNum;
    }

    public void setKillNum(int killNum) {
        this.killNum = killNum;
    }

    public void addKillNum(int num) {
        if (num > 0) {
            this.killNum += num;
        }
    }
}
