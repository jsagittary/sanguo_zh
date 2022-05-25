package com.gryphpoem.game.zw.resource.domain;

import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.resource.domain.p.UploadCrossDataType;

import java.util.List;

/**
 * @ClassName Events.java
 * @Description EventBus的事件类集合
 * @author QiuKun
 * @date 2017年9月14日
 */
public interface Events {

    /**
     * 玩家登陆登出时间
     */
    public static class PlayerLoginLogoutEvent {
        public Player player;
        public boolean isLogin = false; // 默认是退出登录

        public PlayerLoginLogoutEvent(Player player, boolean isLogin) {
            this.player = player;
            this.isLogin = isLogin;
        }

    }

    /**
     * 删除地图上某个玩家的焦点
     * 
     * @author QiuKun
     *
     */
    public static class RmMapFocusEvent {
        public Player player;

        public RmMapFocusEvent(Player player) {
            this.player = player;
        }
    }

    /**
     * 添加区焦点
     * 
     * @author QiuKun
     *
     */
    public static class AddAreaChangeEvent {
        public Player player;
        public int areaId;

        public AddAreaChangeEvent(Player player, int areaId) {
            this.player = player;
            this.areaId = areaId;
        }
    }

    /**
     * 地图区发生改变进行推送
     * 
     * @author QiuKun
     *
     */
    public static class AreaChangeNoticeEvent {
        /** 刷新map数据 */
        public final static int MAP_TYPE = 1;
        /** 刷新战斗线的数据 */
        public final static int LINE_TYPE = 2;
        /** 刷新map和area数据 */
        public final static int MAP_AND_AREA_TYPE = 3;
        /** 客户端清缓刷新 */
        public final static int CLEAR_CACHE_TYPE = 4;
        /** 刷新map和战斗线路 */
        public final static int MAP_AND_LINE_TYPE = 5;
        /** 刷新Map和Area和战斗线路的数据 */
        public final static int MAP_AND_AREA_AND_LINE = 6;
        public List<Integer> posList; // 改变的坐标
        /**
         * 1 刷新map数据, 2 刷新战斗线的数据, 3 刷新map和area数据 , 4 客户端清缓刷新, 5 刷新map和战斗线路, 6 刷新Map和Area和战斗线路的数据
         */
        public int type;
        public long excludeId; // 排除的id

        public AreaChangeNoticeEvent(List<Integer> posList, int type) {
            this.posList = posList;
            this.type = type;
        }

        public AreaChangeNoticeEvent(List<Integer> posList, long excludeId, int type) {
            this.posList = posList;
            this.type = type;
            this.excludeId = excludeId;
        }
    }

    /**
     * 某个玩家战斗力发生改变时
     * 
     * @author QiuKun
     *
     */
    public static class FightChangeEvent {
        public Player player;

        public FightChangeEvent(Player player) {
            this.player = player;
        }

    }

    /**
     *  更新跨服相关数据
     *
     */
    public static class CrossPlayerChangeEvent {
        /** 更新跨服状态*/
        public UploadCrossDataType uploadType;
        /** 玩家id*/
        public List<Long> roleIds;
        /** 玩法*/
        public CrossFunction function;

        public CrossPlayerChangeEvent(int mainType, int subType, CrossFunction crossFunction, List<Long> roleIds) {
            this.function = crossFunction;
            this.roleIds = roleIds;
            this.uploadType = new UploadCrossDataType(mainType, subType);
        }
    }

    /**
     * 同步问卷调查事件
     */
    public static class SyncQuestionnaireEvent {
        /** 活动类型*/
        public int actType;

        public SyncQuestionnaireEvent(int actType) {
            this.actType = actType;
        }
    }
}
