package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;

/**
 * @author QiuKun
 * @ClassName StaticRebelRound.java
 * @Description 匪军攻击轮次
 * @date 2018年10月24日
 */
public class StaticRebelRound {
    /**
     * 模板1
     */
    public static final int TEMPLATE_1 = 1;
    /**
     * 模板2
     */
    public static final int TEMPLATE_2 = 2;
    /**
     * 模板3
     */
    public static final int TEMPLATE_3 = 3;
    /**
     * 模板4
     */
    public static final int TEMPLATE_4 = 4;
    /**
     * 模板5
     */
    public static final int TEMPLATE_5 = 5;
    /**
     * 模板6
     */
    public static final int TEMPLATE_6 = 6;

    private int id;
    private int template;// 模板
    private int round;// 轮次
    private int credit;// 得到的叛乱积分
    private int interval;// 间隔时间(秒值)
    private List<List<Integer>> award;// 给予的资源奖励 格式[[type,id,count]]
    private List<List<Integer>> from; // 阵型 格式[npcId]

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTemplate() {
        return template;
    }

    public void setTemplate(int template) {
        this.template = template;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<List<Integer>> getFrom() {
        return from;
    }

    public void setFrom(List<List<Integer>> from) {
        this.from = from;
    }

    /**
     * 获取兵力
     *
     * @return
     */
    public int getArmCnt() {
        int cnt = 0;
        for (List<Integer> npcIdList : from) {
            if (CheckNull.isEmpty(npcIdList)) continue;
            StaticNpc sNpc = StaticNpcDataMgr.getNpcMap().get(npcIdList.get(0));
            if (sNpc != null) {
                Integer arm = sNpc.getAttr().get(FightCommonConstant.AttrId.LEAD);
                if (arm != null) {
                    cnt += arm.intValue();
                }
            }
        }
        return cnt;
    }

    @Override
    public String toString() {
        return "StaticRebelRound [id=" + id + ", template=" + template + ", round=" + round + ", credit=" + credit
                + ", interval=" + interval + ", award=" + award + ", from=" + from + "]";
    }

}
