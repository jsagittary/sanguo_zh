package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @program: zombie_trunk
 * @description: 英雄进化属性表
 * @author: zhou jie
 * @create: 2019-10-22 16:58
 */
public class StaticHeroEvolve {

    private int id;
    /**
     * 天赋页索引
     */
    private int group;
    /**
     * 天赋部位（紫将：1、2、3、4  橙将：1、2、3、4、5、6）
     */
    private int part;
    /**
     * 升级对应天赋的消耗 [[1,19,50]] 分别表示大类——货币（s_type_define表中的rewardType字段）、细分类型——（s_type_define表中的subType字段）、消耗数量
     */
    private List<List<Integer>> consume;
    /**
     * 对应等级天赋的强化属性 key--属性id，Constant.AttrId；value--属性值
     */
    private Map<Integer, Integer> attr;

    /**
     * 天赋等级
     */
    private int lv;

    /**
     * 天赋页签
     */
    private int page;

    /**
     * 天赋对应加成战力
     */
    private int fight;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getFight() {
        return fight;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }

    @Override
    public String toString() {
        return "StaticHeroEvolve{" +
                "id=" + id +
                ", group=" + group +
                ", part=" + part +
                ", consume=" + consume +
                ", attr=" + attr +
                '}';
    }
}