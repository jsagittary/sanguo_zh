package com.gryphpoem.game.zw.resource.domain.s;

import lombok.Data;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 16:01
 */
@Data
public class StaticHeroSkill {
    private int id;
    private int skillGroupId;
    private int skillId;
    private int level;
    private List<List<Integer>> skillEffect;
    private List<List<Integer>> buff;
    /**
     * 技能能量槽上限
     */
    private int energyUpperLimit;
    /**
     * 释放技能时需要能量
     */
    private int releaseNeedEnergy;
    /**
     * 技能登场时能量
     */
    private int debutEnergy;
    /**
     * 技能是否跟随技能等级成长  <技能主题效果是否成长, buff挂载概率>
     */
    private List<Integer> whetherGrow;
}
