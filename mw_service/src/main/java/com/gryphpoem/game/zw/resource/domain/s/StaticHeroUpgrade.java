package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-20 10:09
 */
public class StaticHeroUpgrade {
    private int keyId;
    private int heroId;
    private int grade;
    private List<List<Integer>> attr;
    private List<List<Integer>> consume;
    private List<List<Integer>> condition;
    private int level;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public List<List<Integer>> getCondition() {
        return condition;
    }

    public void setCondition(List<List<Integer>> condition) {
        this.condition = condition;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
