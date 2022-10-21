package com.gryphpoem.game.zw.data.s;

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
}
