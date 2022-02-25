package com.gryphpoem.game.zw.gameplay.local.constant;

/**
 * @ClassName CrossWorldMapConstant.java
 * @Description
 * @author QiuKun
 * @date 2019年3月20日
 */
public interface CrossWorldMapConstant {
    int CROSS_MAP_ID = 26;
    /** 跨服地图宽 */
    int CROSS_MAP_WEIGHT = 50;
    /** 跨服地图高 */
    int CROSS_MAP_HEIGHT = 50;
    /** 地图块的大小 */
    int CROSS_MAP_CELLSIZE = 10;

    /** 跨服的cityArea */
    int CITY_AREA = 26;
    /** openOrder主要读取用于刷流寇矿点使用 */
    int CROSS_AREA_OPEN_ORDER = 4;

    /** 进入新地图 */
    int MOVE_CITY_TYPE_ENTER = 1;
    /** 退出新地图 */
    int MOVE_CITY_TYPE_LEAVE = 2;
    /** 随机世界迁城(在新地图使用) */
    int MOVE_CITY_TYPE_RANDOM = 3;
    /** 高级世界迁城(在新地图使用) */
    int MOVE_CITY_TYPE_POS = 4;

    /** 进入新地图迁城道具 */
    int MOVE_CITY_TYPE_ENTER_PROPID = 5005;
    /** 退出新地图迁城道具 */
    int MOVE_CITY_TYPE_LEAVE_PROPID = 5001;
    /** 新地图随机迁城道具 */
    int MOVE_CITY_TYPE_RANDOM_PROPID = 5005;
    /** 新地图定点迁城道具 */
    int MOVE_CITY_TYPE_POS_PROPID = 5006;

    /** 大本营的city类型 */
    int CITY_TYPE_CAMP = 11;
    /** 最多拥有城池数量 */
    int OWN_CITY_MAX_CNT = 10;
    /** 跨服-纽约城-id */
    int NEW_YORK_CITY_ID = 20025;

    /** 纽约争霸 */
    int NEW_YORK_CITY_PRE_VIEW = 1;
    /** 纽约争霸  可攻打*/
    int NEW_YORK_CITY_ATTACK = 2;
    /** 纽约争霸 结束*/
    int NEW_YORK_CITY_END = 3;

}
