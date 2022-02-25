package com.gryphpoem.game.zw.resource.pojo.activity;

/**
 * 任务类型
 *  taskType:   任务类型
 *  handle:     是否在接取时就处理进度，为true时不需要参数
 * @author xwind
 * @date 2021/3/2
 */
public enum ETask {
    FIGHT_REBEL(1,false),//攻打叛军
    FIGHT_ELITE_REBEL(2,false),//攻打精英叛军
    JOIN_CITY_WAR(3,false),//参加城战
    PASS_BARRIER(4,false),//成功通关关卡，包含扫荡
    PASS_EXPEDITION(5,false),//帝国远征
    CONSUME_ITEM(6,false),//道具消耗 [道具ID,数量]
    BUILD_UP(7,true),//升级建筑,xx建筑xx级
    TECHNOLOGY_UP(8,true),//升级科技,xx科技xx级
    MAKE_EQUIP(9,false),//打造装备
    REFORM_EQUIP(10,false),//改造装备
    HERO_TRAINING(11,false),//英雄特训
    GET_ITEM(12,false),//获得道具
    ARTIFACT_UP(13,true),//升级神器,xx神器xx级
    BEAUTY_GIFT(14,false),//佳人送礼
    MAKE_ARMY(15,false),//造兵 [兵种,数量] [0,100000]
    COLLECT_RES(16,false),//采集资源
    CONSUME_DIAMOND(17,false),//消耗钻石 [数量] [1000]
    BEAUTY_INTIMACY(18,true),//佳人好感度,xx佳人达到xx好感度
    DAILY_LOGIN(19,false),//每日登录
    CITY_FIRSTKILLED(20,false),//城池首杀
    ORNAMENT_COUNT(21,true),//配饰合成，拥有至少1个4级配饰
    APPOINTMENT(22,false),//约会
    TRAINING_HIGH(23,false),//高级特训次数
    RECHARGE_DIAMOND(24,false),//充值钻石
    TRAINING_LOW(25,false),//初级特训次数
    TITLE_LV(26,true),//爵位等级
    FINISHED_DAILYTASK(27,false),//完成每日日任务X个
    OWN_BEAUTY(28,true),//拥有佳人X个X星
    OWN_HERO(29,true),//拥有英雄X个X品质
    BUILD_CAMP(30,false),//建设阵营
    HITFLY_PLAYER(31,false),//击飞玩家主城
    PLAYER_LV(32,true),//玩家等级
    PLAYER_POWER(33,true),//玩家战力
    TRADE_TIMES(34,false),//船务进行X次X品质交易
    KILLED_NUMBER(35,false),//杀敌数量
    DEATH_NUMBER(36,false),//阵亡数量
    JOIN_ACTIVITY(37,false),//参加活动 [活动类型,次数] 1沙盘演武 2战火燎原 3圣域争霸
    GET_HERO(38,true),//获得指定英雄 [英雄配置id][2000]
    HERO_UPSTAR(39,true),//xx英雄升到x军职 [2000,2,0] [英雄id,大段,小段]
    FINISHED_TASK(40,true),//完成指定类型的任务xx次 [1,10]完成宏伟宝库10次 赛季期间累计的个数
    HERO_UPSKILL(41,true),//xx英雄xx技能升到xx级 [2000,1001,2]
    GET_TASKAWARD(42,true),//领取指定类型任务奖励xx次 [1,10]完成宏伟宝库10次 赛季期间累计的次数
    ARMY_MAK_LOST(43,false),//造兵或孙兵 [数量][2000]
    HERO_LEVELUP(44,true),//xx英雄升级到xx级 [英雄id,等级]
    GOLDEN_AUTUMN_GET_RESOURCE(45, false),//获得任意资源(param中0(任意资源)、1(黄金)、2(木材)、3(粮食)、4(矿石))
    GOLDEN_AUTUMN_FISHING(46, false),//在码头钓鱼
    GOLDEN_AUTUMN_CATCH_FISH(47, false),//在码头钓到鱼
    /**
     * x个徒弟等级达到y级[x,y]
     */
    APPRENTICE_LEVEL_MAKE_IT(48, true),
    /**
     *累计获得x个永久主城装扮（含初始）
     */
    CASTLE_SKIN_NUM(49, true),
    /**
     * 单次消耗钻石达到x以上
     */
    FIRST_USE_DIAMOND(50, false),
    /**
     * 累计登陆天数
     */
    LOGIN_DAYS_SUM(51, true),
    ;
    private int taskType;
    private boolean handle;

    ETask(int taskType,boolean handle) {
        this.taskType = taskType;
        this.handle = handle;
    }

    public int getTaskType() {
        return taskType;
    }


    public boolean isHandle() {
        return handle;
    }

    public static ETask getByType(int type){
        for (ETask value : values()) {
            if(value.getTaskType() == type){
                return value;
            }
        }
        return null;
    }


}
