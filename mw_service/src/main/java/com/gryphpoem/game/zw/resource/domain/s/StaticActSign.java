package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticActSign {

//    CREATE TABLE `s_act_sign` (
//            `id` int(11) NOT NULL COMMENT '唯一值ID',
//            `time` int(11) DEFAULT NULL COMMENT '本月累计签到次数',
//            `award` varchar(255) DEFAULT NULL COMMENT '奖励列表',
//            `level` varchar(255) DEFAULT NULL COMMENT '指挥官等级区间',
//            `vip` varchar(255) DEFAULT NULL COMMENT '填写VIP等级，≥此VIP等级的，可以获得双倍奖励\r\n没有填的表示没有双倍奖励',
//            turn` int(11) DEFAULT NULL COMMENT '轮次，没有新增时，默认读最后一轮',
//    PRIMARY KEY (`id`)
//) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='累计签到表';


    private int id;
    private int time;
    private List<List<Integer>> award;
    private List<List<Integer>> level;
    private int vip;
    private int turn;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<List<Integer>> getLevel() {
        return level;
    }

    public void setLevel(List<List<Integer>> level) {
        this.level = level;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
}
