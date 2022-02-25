package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticRingStrengthen;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-20 16:10
 * @description: 戒指, 可以强化, 镶嵌宝石
 * @modified By:
 */
public class Ring extends Equip {

    /**
     * 强化等级
     */
    private int lv;
    /**
     * 当前等级强化次数
     */
    private int count;
    /**
     * 升级概率
     */
    private int upProbability;
    /**
     * 成长值
     */
    private int exp;
    /**
     * 宝石
     */
    private List<Integer> jewels = new ArrayList<>();
    /**
     * 附加属性
     */
    private Map<Integer, Integer> extAttr = new HashMap<>();

    /**
     * 初始化戒指构造
     */
    public Ring() {
        super();
        this.lv = 0;
        this.count = 0;
        this.exp = 0;
        final StaticRingStrengthen ringConf = StaticPropDataMgr.getRingConfByLv(this.getEquipId(), this.lv);
        if (!CheckNull.isNull(ringConf)) {
            this.upProbability = ringConf.getRealBaseProbability();
        }
    }

    /**
     * 反序列化装备构造
     * @param equip
     */
    public Ring(CommonPb.Equip equip) {
        super(equip);
        if (!equip.hasExta()) {
            return;
        }
        CommonPb.EquipExtr exta = equip.getExta();
        this.lv = exta.getLv();
        this.count = exta.getUpCnt();
        this.upProbability = exta.getUpProbability();
        List<Integer> ringStones = exta.getRingStoneList();
        if (!CheckNull.isEmpty(ringStones)) {
            ringStones.forEach(jewel -> this.jewels.add(jewel));
        }
        List<CommonPb.TwoInt> attr = exta.getAttrList();
        if (!CheckNull.isEmpty(attr)) {
            attr.forEach(en -> this.extAttr.put(en.getV1(), en.getV2()));
        }
    }

    /**
     * 戒指升级
     * @param conf
     */
    public void upLv(StaticRingStrengthen conf) {
        this.lv = conf.getLevel();
        this.count = 0;
        this.upProbability = conf.getRealBaseProbability();
        this.exp = 0;
        this.extAttr.clear();
    }

    /**
     * 戒指添加经验
     * @param add
     */
    public void addExp(int add) {
        this.count++;
        this.exp += add;
    }

    /**
     * 戒指镶嵌宝石
     * @param jewel
     */
    public void upJewel(int jewel) {
        this.jewels.add(jewel);
    }

    /**
     * 戒指卸下宝石
     * @param jewel
     */
    public void downJewel(int jewel) {
        this.jewels.remove(Integer.valueOf(jewel));
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 获取直接晋升概率
     * @return
     */
    public int getUpProbability() {
        if (count > 0) {
            StaticRingStrengthen ringConf = StaticPropDataMgr.getRingConfByLv(this.getEquipId(), this.lv);
            List<Integer> realUpProbability = ringConf.getRealUpProbability();
            if (!CheckNull.isNull(ringConf) && !CheckNull.isEmpty(realUpProbability)) {
                int baseRadio = realUpProbability.get(0);
                int up = realUpProbability.get(1);
                int cnt = count / baseRadio;
                return cnt > 0 ? upProbability + up * cnt : upProbability;
            }
        }
        return upProbability;
    }

    public void setUpProbability(int upProbability) {
        this.upProbability = upProbability;
    }

    public List<Integer> getJewels() {
        return jewels;
    }

    public void setJewels(List<Integer> jewels) {
        this.jewels = jewels;
    }

    public Map<Integer, Integer> getExtAttr() {
        return extAttr;
    }

    public void setExtAttr(Map<Integer, Integer> extAttr) {
        this.extAttr = extAttr;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

}
