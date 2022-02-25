package com.gryphpoem.game.zw.resource.pojo.party;

import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.PartyConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticPartySuperSupply;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-18 17:23
 * @description: 军团超级补给
 * @modified By:
 */
public class PartySuperSupply {

    /**
     * 等级
     */
    private int lv;

    /**
     * 能量
     */
    private int energy;

    /**
     * 消耗金币
     */
    private long costGold;

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public long getCostGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }

    /**
     * 获取阵营当前的能量
     * 能量 = 补给获得的能量 + 消耗金币获得的能量
     *
     * @return
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * 添加阵营消耗, 每消耗100点金币增加5点能量
     *
     * @param add 消耗
     * @return lvUp 是否升级
     */
    public boolean addCostGold(int add) {
        boolean lvUp = false;
        this.costGold += add;
        if (!CheckNull.isEmpty(PartyConstant.COST_GOLD_ENERGY_NUM) && this.costGold > PartyConstant.COST_GOLD_ENERGY_NUM
                .get(0)) {
            long cnt = this.costGold / PartyConstant.COST_GOLD_ENERGY_NUM.get(0);
            lvUp = addEnergy((int) (cnt * PartyConstant.COST_GOLD_ENERGY_NUM.get(1)));
            this.costGold %= PartyConstant.COST_GOLD_ENERGY_NUM.get(0);
        }
        return lvUp;
    }

    /**
     * 新增能量
     *
     * @param add
     * @return lvUp 是否升级
     */
    public boolean addEnergy(int add) {
        boolean lvUp = false;
        while (add > 0 && lv < StaticPartyDataMgr.getMaxSuperSupplyLv()) {
            StaticPartySuperSupply spss = StaticPartyDataMgr.getPartySuperSupply(lv);
            if (CheckNull.isNull(spss)) {
                return lvUp;
            }
            int need = spss.getNeed();
            if (need > 0) {
                if (add + getEnergy() >= need) {
                    add -= need - getEnergy();
                    levelUp();
                    lvUp = true;
                } else {
                    setEnergy(getEnergy() + add);
                    add = 0;
                }
            } else {
                spss = StaticPartyDataMgr.getPartySuperSupply(lv);
                if (!CheckNull.isNull(spss)) {
                    int max = spss.getNeed();
                    setEnergy(max);
                    break;
                }
            }
        }
        return lvUp;
    }

    /**
     * 超级补给升级
     *
     * @return
     */
    private int levelUp() {
        this.lv++;
        this.energy = 0;
        return lv;
    }

    public PartySuperSupply() {
        this.lv = StaticPartyDataMgr.getMinSuperSupplyLv();
    }

    /**
     * 反序列化
     *
     * @param superSupply
     */
    public void dser(CommonPb.PartySuperSupply superSupply) {
        this.lv = superSupply.getLv();
        this.energy = superSupply.getEnergy();
        this.costGold = superSupply.getCostGold();
    }
}
