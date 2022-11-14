package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 人生模拟器选项
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/27 9:48
 */
public class StaticSimulatorChoose {

    private int id; // 主键id

    // private String des; // 文本描述

    private List<List<Integer>> rewardList; // 道具数量变化, [[大类, 小类, 数量, 1(新增)或0(扣减)]]

    private List<Integer> miniGame; // 小游戏接口, [类型, 关卡]

    private int combatId; // 进入的战斗关卡配置

    private List<List<Integer>> buff; // 获得的buff, [类型,增益,持续时间]

    private List<List<Integer>> characterFix; // 此选项对性格值的影响, [[性格id,影响值,1]], 1增加, 2减少

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getRewardList() {
        return rewardList;
    }

    public void setRewardList(List<List<Integer>> rewardList) {
        this.rewardList = rewardList;
    }

    public List<Integer> getMiniGame() {
        return miniGame;
    }

    public void setMiniGame(List<Integer> miniGame) {
        this.miniGame = miniGame;
    }

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public List<List<Integer>> getBuff() {
        return buff;
    }

    public void setBuff(List<List<Integer>> buff) {
        this.buff = buff;
    }

    public List<List<Integer>> getCharacterFix() {
        return characterFix;
    }

    public void setCharacterFix(List<List<Integer>> characterFix) {
        this.characterFix = characterFix;
    }
}
