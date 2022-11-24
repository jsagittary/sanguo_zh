package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 武将品阶对应的内政属性
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/23 11:22
 */
public class StaticHeroGradeInterior {

    private int keyId;

    private int grade; // 品阶

    private int level; // 品阶等级

    private List<List<Integer>> attr; // 内政属性 [建筑ID,属性ID,万分比]

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

}
