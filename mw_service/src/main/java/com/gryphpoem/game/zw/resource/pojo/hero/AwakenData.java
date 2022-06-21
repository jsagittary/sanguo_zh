package com.gryphpoem.game.zw.resource.pojo.hero;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @program: zombie_trunk
 * @description: 觉醒功能
 * @author: zhou jie
 * @create: 2019-10-22 15:50
 */
public class AwakenData {

    /**
     * 激活状态
     */
    private int status;
    /**
     * 基因进化的状态
     */
    private Map<Integer, Integer> evolutionGene = new HashMap<>(5);
    /**
     * 天赋下标
     */
    private int index;

    public AwakenData(CommonPb.AwakenData awakenData) {
        this();
        this.status = awakenData.getStatus();
        for (CommonPb.TwoInt twoInt : awakenData.getEvolutionGeneList()) {
            this.evolutionGene.put(twoInt.getV1(), twoInt.getV2());
        }
        this.index = awakenData.getIndex();
    }

    public AwakenData() {
    }

    public AwakenData(int index) {
        this.index = index;
    }

    public Map<Integer, Integer> getEvolutionGene() {
        return evolutionGene;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 是否激活了
     *
     * @return
     */
    public boolean isActivate() {
        return this.status == 1;
    }

    /**
     * 未进化的索引
     *
     * @return 当前部位
     */
    public int curPart() {
        return Stream.iterate(HeroConstant.AWAKEN_PART_MIN, i -> ++i).limit(HeroConstant.AWAKEN_PART_MAX).filter(part -> evolutionGene.getOrDefault(part, 0) == 0).sorted().findFirst().orElse(0);
    }

    /**
     * 进化的索引
     *
     * @return 下一个进化
     */
    public int lastPart() {
        return Stream.iterate(HeroConstant.AWAKEN_PART_MIN, i -> ++i).limit(HeroConstant.AWAKEN_PART_MAX).filter(part -> evolutionGene.getOrDefault(part, 0) == 1).max(Comparator.comparingInt(Integer::intValue)).orElse(0);
    }

    /**
     * 将基因重置
     */
    public void clearEvolution() {
        Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> evolutionGene.put(part, 0));
    }


    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}