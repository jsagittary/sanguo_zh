package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.resource.constant.TreasureWareConst;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class StaticTreasureWare {
    /**
     * 主键id
     */
    private int id;
    /**
     * 类型
     */
    private int type;
    /**
     * 品质
     */
    private int quality;
    /**
     * 赛季
     */
    private int season;
    /**
     * 等级
     */
    private int limitLevel;
    /**
     * 最大等级
     */
    private int maxLevel;
    /**
     * 消耗材料
     */
    private List<List<Integer>> consume;
    /**
     * 宝具基本属性条数
     */
    private int attrNum;
    /**
     * 宝具基本属性类型概率
     */
    private List<List<Integer>> typeProb;
    /**
     * 宝具最高，高，中，下，最差属性级别出现权重 [[类型(最高，高。。。)， 最大比例， 最小比例],...]
     */
    private List<List<Integer>> numProb = new ArrayList<>();
    /**
     * 宝具初始最高属性
     */
    private List<List<Integer>> attrInit;
    /**
     * 宝具专属属性出现概率
     */
    private int specialAttr;
    /**
     * 宝具特殊属性比重 [[specialId, 权重]...]
     */
    private List<List<Integer>> specialProb = new ArrayList<>();
    /**
     * 宝具基本属性类型总权重
     */
    private int initTotalAttrWeight;
    /**
     * 宝具属性级别总权重
     */
    private int numTotalWeight;
    /**
     * 宝具专属属性id总权重
     */
    private int specialTotalWeight;

    /**
     * 对应打造多少次提升专属属性概率
     */
    private List<Integer> mini;

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

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getLimitLevel() {
        return limitLevel;
    }

    public void setLimitLevel(int limitLevel) {
        this.limitLevel = limitLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public int getAttrNum() {
        return attrNum;
    }

    public void setAttrNum(int attrNum) {
        this.attrNum = attrNum;
    }

    public List<List<Integer>> getTypeProb() {
        return typeProb;
    }

    public void setTypeProb(List<List<Integer>> typeProb) {
        this.typeProb = typeProb;
    }

    public List<List<Integer>> getNumProb() {
        return numProb;
    }

    public void setNumProb(List<List<Integer>> numProb) {
        this.numProb = numProb;
    }

    public List<List<Integer>> getAttrInit() {
        return attrInit;
    }

    public void setAttrInit(List<List<Integer>> attrInit) {
        this.attrInit = attrInit;
    }

    public int getSpecialAttr() {
        return specialAttr;
    }

    public void setSpecialAttr(int specialAttr) {
        this.specialAttr = specialAttr;
    }

    public List<List<Integer>> getSpecialProb() {
        return specialProb;
    }

    public void setSpecialProb(List<List<Integer>> specialProb) {
        this.specialProb = specialProb;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public List<Integer> getMini() {
        return mini;
    }

    public void setMini(List<Integer> mini) {
        this.mini = mini;
    }

    /**
     * 初始化权重数值
     */
    public void initData() {
        Optional.ofNullable(this.typeProb).ifPresent(type -> type.forEach(list -> {
            this.initTotalAttrWeight += list.get(1);
        }));
        Optional.ofNullable(this.numProb).ifPresent(rank -> rank.forEach(list -> {
            this.numTotalWeight += list.get(1);
        }));
        Optional.ofNullable(this.specialProb).ifPresent(special -> special.forEach(list -> {
            this.specialTotalWeight += list.get(1);
        }));
    }

//    public int getMaxRank() {
//        Objects.requireNonNull(numProb, String.format("treasure ware id :%d, numProb must not null", id));
//        return numProb.stream().mapToInt(iter -> iter.get(0)).max().orElse(1);
////        return this.numProb.stream().max((a, b) -> {
////            if (a.get(0) > b.get(0)) {
////                return 1;
////            } else return -1;
////        }).get().get(0);
//    }

    public Integer getSpecialId(int addProbability) {
        if (!RandomHelper.isHitRangeIn10000(this.specialAttr + addProbability)) {
            return null;
        }

        // 如果总权重为0, 就重新计算一下总权重
        if (!specialProb.isEmpty() && specialTotalWeight == 0) {
            initData();
        }

        int random = RandomHelper.randomInSize(specialTotalWeight);
        int temp = 0;
        if (specialProb != null) {
            for (List<Integer> list : specialProb) {
                if (list.size() > 1) {
                    temp += list.get(1);
                    if (temp >= random) {
                        LogUtil.debug(
                                "宝具随机专属属性比例总权重: " + specialTotalWeight + ", random=" + random + ", temp=" + temp);
                        return list.get(0);
                    }
                }
            }
        }

        return null;
    }

    public Integer checkSpecialId(int specialId) {
        if (CheckNull.isEmpty(specialProb))
            return null;

        for (List<Integer> list : specialProb) {
            if (list.size() > 1) {
                if (specialId == list.get(0)) {
                    return specialId;
                }
            }
        }

        return null;
    }

    /**
     * 获取基础属性列表
     *
     * @return
     */
    public List<List<Integer>> getRandomAttrIds() {
        if (ObjectUtils.isEmpty(this.typeProb)) {
            return null;
        }

        // 如果总权重为0, 就重新计算一下总权重
        if (!typeProb.isEmpty() && initTotalAttrWeight == 0) {
            initData();
        }

        int attrId;
        int index = 0;
        List<Integer> attrIds = new ArrayList<>(attrNum);
        List<List<Integer>> copyTypeProb = new ArrayList<>(this.typeProb);
        while (index < attrNum) {
            attrId = getRandomAttrId(copyTypeProb);
            if (attrId == -1)
                continue;
            attrIds.add(attrId);
            ++index;
        }

        copyTypeProb.clear();
        for (List<Integer> list : attrInit) {
            if (attrIds.contains(list.get(0))) {
                copyTypeProb.add(list);
            }
        }

        return copyTypeProb;
    }

    /**
     * 获取一次随机基础属性id
     *
     * @param copyTypeProb
     * @return
     */
    private int getRandomAttrId(List<List<Integer>> copyTypeProb) {
        int totalAttrWeight = copyTypeProb.stream().mapToInt(list -> {
            return list.get(1);
        }).sum();

        int temp = 0;
        List<Integer> list;
        int random = RandomHelper.randomInSize(totalAttrWeight);
        if (copyTypeProb != null) {
            Iterator<List<Integer>> it = copyTypeProb.iterator();
            while (it.hasNext()) {
                list = it.next();
                if (list.size() >= 2) {
                    temp += list.get(1);
                    if (temp >= random) {
                        LogUtil.debug(
                                "宝具随机基本属性比例总权重: " + totalAttrWeight + ", random = " + random + ", temp = " + temp);
                        it.remove();
                        return list.get(0);
                    }
                }
            }
        }

        return -1;
    }

    /**
     * 随机出宝具级别
     *
     * @return
     */
    public int[] getRandomRank() {
        if (type == TreasureWareConst.ANCIENT_TREASURE_WARE)
            return null;

        // 如果总权重为0, 就重新计算一下总权重
        if (!numProb.isEmpty() && numTotalWeight == 0) {
            initData();
        }

        int temp = 0;
        int[] proportion = null;
        int random = RandomHelper.randomInSize(numTotalWeight);
        if (numProb != null) {
            for (List<Integer> list : numProb) {
                if (list.size() > 2) {
                    temp += list.get(1);
                    if (temp >= random) {
                        LogUtil.debug(
                                "宝具随机级别比例总权重: " + numTotalWeight + ", random=" + random + ", temp=" + temp);
                        proportion = new int[]{list.get(0), list.get(2), list.get(3)};
                        break;
                    }
                }
            }
        }

        return proportion;
    }
}
