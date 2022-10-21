package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.dataMgr.StaticFightDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.FightConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticFightBuff;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 11:13
 * @description: 战斗buff
 * @modified By:
 */
public class FightBuff {

    private int buffId;             // key
    /**
     * buff类型
     * 1.机身固化     触发时可免除对方X次的攻击  不论对方攻击伤害如何
     * 2.防弹涂层     生成护盾，吸收伤害，可抵消XXX次伤害  护盾未破之前，兵力将不会损失
     * 3.紧急装置     受到致命伤害时，避免致死
     * 4.燃烧弹       易伤buff，受到的伤害添加XX%
     * 5.火力核心     必定暴击
     * 6.制导改良     必定命中
     */
    private int buffType;
    private int buffVal;             // buff的数值
    private int continueNum;         // 剩余作用回合数
    private int effectRow;           // 剩余作用兵排数
    public boolean isAtk;            // buff的作用对象
    private int heroId;              //buff释放方的heroId
    public HashMap<Integer, Integer> param = new HashMap<>(); // 临时变量的存储

    public FightBuff() {
    }

    public FightBuff(StaticFightBuff sBuff, int heroId, boolean isAtk) {
        this();
        this.buffId = sBuff.getBuffId();
        this.buffType = sBuff.getBuffType();
        this.buffVal = sBuff.getBuffVal();
        this.continueNum = sBuff.getTimes();
        this.effectRow = sBuff.getRow();
        this.param.put(FightConstant.BuffParam.CONTINUE_NUM, sBuff.getTimes());
        this.param.put(FightConstant.BuffParam.BUFF_VAL, sBuff.getBuffVal());
        this.isAtk = sBuff.getObject() == 0 ? isAtk : !isAtk;
        this.heroId = heroId;
    }

    /**
     * 创建战斗BuffDb
     *
     * @return
     */
    public CommonPb.FightBuff.Builder createFightBuffpb() {
        CommonPb.FightBuff.Builder builder = CommonPb.FightBuff.newBuilder();
        builder.setType(this.buffType);
        builder.setBuffVal(buffVal);
        builder.setContinueNum(this.continueNum);
        builder.setEffectRow(this.effectRow);
        builder.setIsAtk(this.isAtk);
        builder.setBuffId(this.buffId);
        return builder;
    }

    /**
     * 释放buff
     *
     * @param force
     * @param target
     * @param logic
     */
    public void releaseBuff(Force force, Force target, FightLogic logic) {
        if (buffId > 0) { // 释放buff
            StaticFightBuff sBuff = StaticFightDataMgr.getFightBuffMapById(buffId);
            this.subEffectRow(); // 释放的时候, 扣除当前排数
            CommonPb.FightBuff.Builder buffBuilder = this.createFightBuffpb();
            buffBuilder.addParam(PbHelper.createTwoIntPb(CommonPb.FightBuffParam.FIRST_RELEASE_VALUE, 1));  // 新增buff
            // 新的buff会覆盖老
            if (sBuff.getObject() == 0) { // 增益加在自己身上
                force.fightBuff.put(sBuff.getBuffType(), this);
            } else { // 减益加在对方身上
                target.fightBuff.put(sBuff.getBuffType(), this);
            }
            buffBuilder.setCurrentLine(force.curLine);
            logic.buffs.add(buffBuilder.build());
        }
    }

    /**
     * 是否可以触发buff
     *
     * @return
     */
    public boolean canRelease() {
        boolean can = false;
        switch (buffType) {
            case FightConstant.BuffType.BUFF_TYPE_DEF_HURT: // 可抵消XXX次伤害 护盾未破之前，兵力将不会损失
                can = buffVal > 0;
                break;
            case FightConstant.BuffType.BUFF_TYPE_NOT_DEAD: // 受到致命伤害时，避免致死
            case FightConstant.BuffType.BUFF_TYPE_DEF_CNT:  // 免除对方X次的攻击
            case FightConstant.BuffType.BUFF_TYPE_UP_HURT:  // 易伤buff，受到的伤害添加XX%
            case FightConstant.BuffType.BUFF_TYPE_CRIT:     // 必定暴击
            case FightConstant.BuffType.BUFF_TYPE_HIT:      // 必定命中
                can = continueNum > 0;
                break;
        }
        return can;
    }

    /**
     * 释放buff效果后处理
     * 减少作用次数
     *
     * @return
     */
    public CommonPb.FightBuff.Builder releaseBuff() {
        switch (buffType) {
            case FightConstant.BuffType.BUFF_TYPE_DEF_HURT: // 可抵消XXX伤害 护盾未破之前，兵力将不会损失
                break;
            case FightConstant.BuffType.BUFF_TYPE_NOT_DEAD: // 受到致命伤害时，避免致死
            case FightConstant.BuffType.BUFF_TYPE_DEF_CNT:  // 免除对方X次的攻击
            case FightConstant.BuffType.BUFF_TYPE_UP_HURT:  // 易伤buff, 受到的伤害添加XX%
            case FightConstant.BuffType.BUFF_TYPE_CRIT:     // 必定暴击
            case FightConstant.BuffType.BUFF_TYPE_HIT:      // 必定命中
                subContinueNum();                           // 减掉作用回合数
                break;
        }
        CommonPb.FightBuff.Builder builder = createFightBuffpb();
        return builder;
    }

    public int getBuffVal() {
        return buffVal;
    }

    public void setBuffVal(int buffVal) {
        this.buffVal = buffVal;
    }

    public int getEffectRow() {
        return effectRow;
    }

    public int getBuffType() {
        return buffType;
    }

    public int getContinueNum() {
        return continueNum;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    /**
     * 减去作用排数
     */
    public boolean subEffectRow() {
        if (effectRow <= 0) {
            continueNum = 0;
            return false;
        }
        effectRow--;
        if (buffType == FightConstant.BuffType.BUFF_TYPE_NOT_DEAD || buffType == FightConstant.BuffType.BUFF_TYPE_CRIT // 每损失一排兵, 重置buff作用回合数
                || buffType == FightConstant.BuffType.BUFF_TYPE_HIT || buffType == FightConstant.BuffType.BUFF_TYPE_UP_HURT || buffType == FightConstant.BuffType.BUFF_TYPE_DEF_CNT) {
            continueNum = this.param.getOrDefault(FightConstant.BuffParam.CONTINUE_NUM, 0);
        } else if (buffType == FightConstant.BuffType.BUFF_TYPE_DEF_HURT) { // 每损失一排兵, 重置buffVal
            buffVal = this.param.getOrDefault(FightConstant.BuffParam.BUFF_VAL, 0);
        }
        return true;
    }

    /**
     * 减去作用回合
     */
    public void subContinueNum() {
        if (continueNum <= 0) {
            return;
        }
        continueNum--;
    }

    public boolean clearBuff() {
        if (this.continueNum == 0 && this.effectRow == 0)
            return false;

        this.continueNum = 0;
        this.effectRow = 0;
        return true;
    }

    /**
     * 扣除buff效果
     *
     * @param sub
     */
    public void subEffectVal(int sub) {
        buffVal -= sub;
        if (buffVal < 0) {
            buffVal = 0;
        }
    }

}
