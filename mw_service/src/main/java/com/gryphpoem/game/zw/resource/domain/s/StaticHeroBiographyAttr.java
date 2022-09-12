package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 16:47
 */
public class StaticHeroBiographyAttr {
    private int id;
    private int type;
    private List<List<Integer>> attr;
    private int activeNum;
    private int activeGrade;
    private int level;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public int getActiveNum() {
        return activeNum;
    }

    public void setActiveNum(int activeNum) {
        this.activeNum = activeNum;
    }

    public int getActiveGrade() {
        return activeGrade;
    }

    public void setActiveGrade(int activeGrade) {
        this.activeGrade = activeGrade;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
