package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticCrossServerRule.java
 * @Description 跨服服务器规则
 * @author QiuKun
 * @date 2019年5月11日
 */
public class StaticCrossServerRule {
    private int gameServerId; // 游戏服服务器id
    private int crossServerId;// 跨服服务器id

    public int getGameServerId() {
        return gameServerId;
    }

    public void setGameServerId(int gameServerId) {
        this.gameServerId = gameServerId;
    }

    public int getCrossServerId() {
        return crossServerId;
    }

    public void setCrossServerId(int crossServerId) {
        this.crossServerId = crossServerId;
    }

    @Override
    public String toString() {
        return "StaticCrossServerRule [gameServerId=" + gameServerId + ", crossServerId=" + crossServerId + "]";
    }

}
