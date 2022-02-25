package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.constant.Constant;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaticTreasureWareLevel {
    /**
     * 主键id
     */
    private int id;
    /**
     * 品质
     */
    private int quality;
    /**
     * 等级
     */
    private int level;
    /**
     * 消耗
     */
    private List<List<Integer>> consume;
    /**
     * 属性
     */
    private List<List<Integer>> attr;
    /**
     * 分解材料
     */
    private List<List<Integer>> resolve;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public List<List<Integer>> getResolve() {
        return resolve;
    }

    public void setResolve(List<List<Integer>> resolve) {
        this.resolve = resolve;
    }

    /**
     * 获取对应属性id的属性值
     *
     * @param attrId
     * @return
     */
    public List<Integer> getAttrById(int attrId) {
        if (ObjectUtils.isEmpty(this.attr)) {
            return null;
        }

        return this.attr.stream().filter(list -> list.get(0) == attrId).findFirst().get();
    }

    public List<List<Integer>> getStrengthAttr(List<Integer> attrIds, int attrPercentage) {
        if (ObjectUtils.isEmpty(attr) || ObjectUtils.isEmpty(attrIds)) {
            return null;
        }

        List<List<Integer>> result = new ArrayList<>();
        attr.forEach(list -> {
            if (!attrIds.contains(list.get(0)))
                return;

            List<Integer> newAttr = new ArrayList<>(list);
            Integer value = newAttr.remove(1);
            newAttr.add(value);
            result.add(newAttr);
        });

        return result;
    }
}
