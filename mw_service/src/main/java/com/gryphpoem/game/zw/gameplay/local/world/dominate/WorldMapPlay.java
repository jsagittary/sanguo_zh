package com.gryphpoem.game.zw.gameplay.local.world.dominate;

/**
 * Description: 世界地图玩法  本类型暂时只做雄踞一方通用
 * Author: zhangpeng
 * createTime: 2022-11-21 22:19
 */
public interface WorldMapPlay {

    /**
     * 获取地图功能玩法
     *
     * @return 地图名称
     */
    int getWorldMapFunction();

    /**
     * 服务器固定时间为一次执行周期, 执行的具体方法
     */
    void onTick();

    /**
     * 关闭地图
     */
    void close();
}
