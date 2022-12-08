package com.gryphpoem.game.zw.buff.abs.effect;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.manager.StaticFightManager;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.game.zw.skill.iml.SimpleHeroSkill;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.game.zw.util.FightUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

/**
 * Description: 效果抽象类
 * Author: zhangpeng
 * createTime: 2022-10-20 18:31
 */
public abstract class AbsFightEffect implements IFightEffect {

    /**
     * 计算效果被执行人
     *
     * @param fightBuff
     * @param contextHolder
     * @param effectConfig
     * @return
     */
    protected ActionDirection actionDirection(IFightBuff fightBuff, FightContextHolder contextHolder, List<Integer> effectConfig) {
        FightConstant.BuffObjective atkObj = FightConstant.BuffObjective.convertTo(effectConfig.get(0));
        FightConstant.BuffObjective defObj = FightConstant.BuffObjective.convertTo(effectConfig.get(1));
        if (CheckNull.isNull(atkObj) || CheckNull.isNull(defObj)) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", effectConfig, ", 执行方或被执行方未找到");
            return null;
        }

        ActionDirection actionDirection = new ActionDirection();
        FightUtil.buffEffectActionDirection(fightBuff, contextHolder, atkObj, actionDirection, true);
        FightUtil.buffEffectActionDirection(fightBuff, contextHolder, defObj, actionDirection, false);
        if (Objects.nonNull(fightBuff)) {
            actionDirection.setSkill((SimpleHeroSkill) fightBuff.getSkill());
            actionDirection.setFightBuff(fightBuff);
        } else {
            if (contextHolder.getCurSkillActionPb() != null) {
                actionDirection.setSkillEffect(true);
                actionDirection.setSkill(contextHolder.getActionDirection().getSkill());
            }
        }
        if (CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            LogUtil.error("fightBuff: ", fightBuff, ", effectConfig: ", effectConfig, ", 被执行人为空");
            return null;
        }

        return actionDirection;
    }

    @Override
    public IFightBuff compareTo(List sameIdBuffList, List effectConfig, IFightBuff fightBuff, FightBuffEffect fightBuffEffect, FightContextHolder contextHolder) {
        return (IFightBuff) sameIdBuffList.get(0);
    }

    @Override
    public Object effectCalculateValue(FightBuffEffect fightBuffEffect, int effectLogicId, Object... params) {
        return null;
    }

    @Override
    public void effectiveness(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, int timing, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection)) {
            return;
        }
        if (!CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            if (Objects.nonNull(rule)) {
                for (Integer heroId : actionDirection.getDefHeroList()) {
                    Force def = actionDirection.getDef();
                    FightBuffEffect fbe = def.getFightEffectMap(heroId);
                    FightEffectData data = createFightEffectData(fightBuff, effectConfig_, fbe);
                    fbe.getEffectMap().computeIfAbsent(rule.getEffectLogicId(), m -> new HashMap<>()).
                            computeIfAbsent(effectConfig_.get(2), l -> new ArrayList<>()).add(data);
                    addEffectPb(contextHolder, fightBuff, rule.getEffectLogicId(), timing,
                            FightConstant.EffectStatus.APPEAR, data, fbe, rule);
                }
            }
        }
    }

    @Override
    public void effectRestoration(IFightBuff fightBuff, FightContextHolder contextHolder, List effectConfig, StaticEffectRule rule, Object... params) {
        List<Integer> effectConfig_ = effectConfig;
        ActionDirection actionDirection = actionDirection(fightBuff, contextHolder, effectConfig_);
        if (CheckNull.isNull(actionDirection)) {
            return;
        }

        if (!CheckNull.isEmpty(actionDirection.getDefHeroList())) {
            for (Integer heroId : actionDirection.getDefHeroList()) {
                Force def = actionDirection.getDef();
                FightBuffEffect fbe = def.getFightEffectMap(heroId);
                Map<Integer, List<FightEffectData>> effectIdMap = fbe.getEffectMap().get(rule.getEffectLogicId());
                if (CheckNull.isEmpty(effectIdMap)) continue;
                List<FightEffectData> effectList = effectIdMap.get(effectConfig_.get(2));
                if (CheckNull.isEmpty(effectList)) continue;
                Iterator<FightEffectData> it = effectList.iterator();
                while (it.hasNext()) {
                    FightEffectData data = it.next();
                    if (CheckNull.isNull(data)) {
                        it.remove();
                        continue;
                    }
                    if (data.getBuffKeyId() == fightBuff.uniqueId()) {
                        it.remove();
                        LogUtil.fight("buff效果失效, 效果持有人: ", def.ownerId, "-", heroId,
                                ", 损失的效果: ", Arrays.toString(effectConfig_.toArray()), ", 消失的效果参数: ", data);
                        addEffectPb(contextHolder, fightBuff, rule.getEffectLogicId(), FightConstant.BuffEffectTiming.BUFF_DISAPPEAR,
                                FightConstant.EffectStatus.DISAPPEAR, null);
                    }
                }
                if (CheckNull.isEmpty(effectList)) {
                    effectIdMap.remove(effectConfig_.get(2));
                    if (CheckNull.isEmpty(effectIdMap)) {
                        fbe.getEffectMap().remove(rule.getEffectLogicId());
                    }
                }
            }
        }
    }

    @Override
    public boolean canEffect(FightContextHolder contextHolder, StaticEffectRule staticEffectRule, Object... params) {
        return true;
    }

    /**
     * 计算技能等级后加成
     *
     * @param originalValue
     * @param fightBuff
     * @return
     */
    protected int skillLvGrow(int originalValue, IFightBuff fightBuff) {
        if (CheckNull.isNull(fightBuff)) return originalValue;
        StaticBuff staticBuff = fightBuff.getBuffConfig();
        if (CheckNull.isNull(staticBuff) || CheckNull.isNull(fightBuff.getSkill())) return originalValue;
        if (staticBuff.getEffectWhetherGrow() == 0 || fightBuff.getSkill() instanceof SimpleHeroSkill == false)
            return originalValue;
        SimpleHeroSkill skill = (SimpleHeroSkill) fightBuff.getSkill();
        return (int) Math.ceil(originalValue * (1 + ((skill.getS_skill().getLevel() - 1) / 9d)));
    }

    /**
     * 添加效果pb
     *
     * @param contextHolder
     * @param fightBuff
     * @param effectLogicId
     * @param timing
     * @param actionType
     */
    protected void addEffectPb(FightContextHolder contextHolder, IFightBuff fightBuff, int effectLogicId, int timing, int actionType, Object... params) {
        BattlePb.CommonEffectAction.Builder builder = BattlePb.CommonEffectAction.newBuilder();
        if (actionType == FightConstant.EffectStatus.APPEAR)
            addPbValue(builder, params);

        SimpleHeroSkill simpleHeroSkill = (SimpleHeroSkill) fightBuff.getSkill();
        BattlePb.BaseEffectAction.Builder basePb = FightPbUtil.createBaseEffectActionPb(BattlePb.CommonEffectAction.effect, builder.build(), effectLogicId,
                FightPbUtil.getActingSize(fightBuff.getBuffGiver(), fightBuff.getBuffGiverId()),
                FightPbUtil.getActingSize(fightBuff.getForce(), fightBuff.getForceId()), timing, actionType,
                simpleHeroSkill.isOnStageSkill(), simpleHeroSkill.getS_skill().getSkillId());
        FightPbUtil.addEffectActionList(contextHolder, basePb);
    }

    /**
     * 计算效果值
     *
     * @param force
     * @param heroId
     * @param effectLogicId
     * @param params
     * @return
     */
    protected double calValue(Force force, int heroId, int effectLogicId, Object... params) {
        return 0;
    }

    /**
     * 创建战斗效果实体
     *
     * @param fightBuff
     * @param effectConfig
     * @param fbe
     * @return
     */
    protected FightEffectData createFightEffectData(IFightBuff fightBuff, List<Integer> effectConfig, FightBuffEffect fbe, Object... params) {
        return null;
    }

    /**
     * 添加效果pb显示值
     *
     * @param builder
     */
    protected void addPbValue(BattlePb.CommonEffectAction.Builder builder, Object... params) {
    }

    /**
     * 比较效果值大小
     *
     * @param actingForce
     * @param actingHeroId
     * @param effectLogicId
     * @param params
     * @return
     */
    protected boolean compareValue(Force actingForce, int actingHeroId, int effectLogicId, Object... params) {
        return false;
    }
}
