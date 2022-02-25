package com.gryphpoem.game.zw.resource.constant;

/**
 * @author xwind
 * @date 2021/4/21
 */
public interface SeasonConst {
    int TREASURY_STATE_DOING = 1;
    int TREASURY_STATE_GENERATED = 2;
    int TREASURY_STATE_GOT = 3;

    int STATE_PRE = 1;
    int STATE_OPEN = 2;
    int STATE_DISPLAY = 3;
    int STATE_NON = 0;

    //百分比
    int PERCENTAGE = 1;
    //固定值
    int FIXED_VALUE = 2;

    //******************************************赛季天赋类******************************************
    int TALENT_CLASSIFIER_0 = 1;//初始天赋
    int TALENT_CLASSIFIER_1 = 1;//进攻天赋
    int TALENT_CLASSIFIER_2 = 2;//防御天赋
    int TALENT_CLASSIFIER_3 = 3;//种田天赋

    int TALENT_TYPE_1 = 1;//普通天赋
    int TALENT_TYPE_2 = 2;//特殊天赋技能

    int TALENT_WAR_FIRE_ATTR = 1;//战火属性buff增益
    int TALENT_BERLIN_ATTR = 2;//圣域属性buff增益

    int TALENT_EFFECT_101 = 101;//属性数值加成
    int TALENT_EFFECT_201 = 201;//伤害增加
    int TALENT_EFFECT_202 = 202;//伤害增加
    int TALENT_EFFECT_301 = 301;//行军速度增加
    int TALENT_EFFECT_302 = 302;//训练兵力增加(万分比)
    int TALENT_EFFECT_303 = 303;//伤兵恢复

    int TALENT_EFFECT_401 = 401;//建筑速度增加
    int TALENT_EFFECT_402 = 402;//科技研究速度增加
    int TALENT_EFFECT_403 = 403;//船坞时间减少
    int TALENT_EFFECT_404 = 404;//英雄经验加成
    int TALENT_EFFECT_405 = 405;//采集加成
    int TALENT_EFFECT_406 = 406;//主城资源增产

    int TALENT_EFFECT_501 = 501;//和平主义, 每天有3次被打增加保护时间240分钟, 和平主义参数配置格式[[4501,2,14400]] 道具ID,生效次数,秒数
    int TALENT_EFFECT_502 = 502;//仓库扩容, 仓库保护容量提升50%
    int TALENT_EFFECT_503 = 503;//市场扩容, [[600],[7200]] 每次兑换时间减少600秒, 最大累计CD时间增加7200秒


    int TALENT_EFFECT_601 = 601;//城战体力消耗减少
    int TALENT_EFFECT_602 = 602;//战斗兵种压制
    int TALENT_EFFECT_603 = 603;//训练兵力增加（固定数量）
    int TALENT_EFFECT_604 = 604;//战火燎原buff增益 圣域buff增益(加成某属性百分比)
    int TALENT_EFFECT_605 = 605;//军饷统筹buff增益
    int TALENT_EFFECT_606 = 606;//攻城略地，(某属性百分比加成到某属性属性上)
    int TALENT_EFFECT_607 = 607;//战斗伤害加成比
    int TALENT_EFFECT_608 = 608;//伤兵回复加成在天书上 原伤兵恢复 303
    int TALENT_EFFECT_609 = 609;//类型2 伤害加成
    int TALENT_EFFECT_610 = 610;//驻防行军速度加成(从被驻防人身上查找天赋)
    int TALENT_EFFECT_611 = 611;//战斗承受伤害减免
    int TALENT_EFFECT_612 = 612;//驻防英雄属性加成(从被驻防人身上查找天赋)
    int TALENT_EFFECT_613 = 613;//vip免费建筑时间
    int TALENT_EFFECT_614 = 614;//treasureService 331行 减去当前固定cd值
    int TALENT_EFFECT_615 = 615;//体力回复1所需要花的时间增益
    int TALENT_EFFECT_616 = 616;//大学 雇佣学者 加速加成
    int TALENT_EFFECT_617 = 617;//兵工厂 雇佣铁匠加速加成
    int TALENT_EFFECT_618 = 618;//兵工厂 增加队列
    int TALENT_EFFECT_619 = 619;//禁卫军属性加成
    int TALENT_EFFECT_620 = 620;//活动积分加成 （战火，圣域，沙盘）

    /**
     * 战斗天赋
     */
    int[] fightEffects = new int[]{
            TALENT_EFFECT_602,//兵种压制
            TALENT_EFFECT_607,//增伤-所有伤害
            TALENT_EFFECT_611,//减伤-所有伤害
            TALENT_EFFECT_609,//增伤-魔法伤害
    };
}
