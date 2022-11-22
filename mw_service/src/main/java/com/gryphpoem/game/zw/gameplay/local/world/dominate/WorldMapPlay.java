package com.gryphpoem.game.zw.gameplay.local.world.dominate;

import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;

/**
 * Description: 世界地图玩法  本类型暂时只做雄踞一方通用
 * Author: zhangpeng
 * createTime: 2022-11-21 22:19
 */
public interface WorldMapPlay extends GamePb<WorldPb.BaseWorldFunctionPb> {

    String getWorldMapFunctionName();

    /**
     * 获取地图功能玩法
     *
     * @return 地图名称
     */
    int getWorldMapFunction();

    /**
     * 初始化定时器
     */
    void initSchedule();

    /**
     * 关闭地图
     */
    void close();

    /**
     * 活动状态
     *
     * @return
     */
    int state();
}
