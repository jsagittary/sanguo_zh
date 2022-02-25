package com.gryphpoem.game.zw.service.fish;

/**
 * @author xwind
 * @date 2021/8/6
 */
public interface FishingConst {
    int TEAM_STATE_NON = 0;//未派遣
    int TEAM_STATE_DOING = 1;//采集中
    int TEAM_STATE_GET = 2;//可领奖
    int TEAM_STATE_GOT = 3;//已领奖

    int ROD_STATE_NON = 0;//空闲中
    int ROD_STATE_DOING = 1;//钓鱼中
}
