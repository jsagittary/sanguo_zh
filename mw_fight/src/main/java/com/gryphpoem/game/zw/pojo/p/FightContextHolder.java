package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.impl.buff.ConditionBuffImpl;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.util.FightPbUtil;
import com.gryphpoem.push.util.CheckNull;

import java.util.*;

/**
 * Description: 战斗上下文持有类
 * Author: zhangpeng
 * createTime: 2022-10-31 15:42
 */
public class FightContextHolder {
    private InnerContextLocal<FightContext> LOCAL;

    public FightContextHolder(Fighter atk, Fighter def) {
        LOCAL = new InnerContextLocal<FightContext>() {

            @Override
            FightContext initialValue() {
                return new FightContext();
            }
        };
        LOCAL.get().setAtkFighter(atk);
        LOCAL.get().setDefFighter(def);
        LOCAL.get().setActionDirection(new ActionDirection());
        LOCAL.get().setBattleLogic(DataResource.ac.getBean(BattleLogic.class));
    }

    public FightContextHolder(Fighter atk, Fighter def, int battleType) {
        LOCAL = new InnerContextLocal<FightContext>() {

            @Override
            FightContext initialValue() {
                return new FightContext();
            }
        };
        LOCAL.get().setAtkFighter(atk);
        LOCAL.get().setDefFighter(def);
        LOCAL.get().setBattleType(battleType);
        LOCAL.get().setActionDirection(new ActionDirection());
        LOCAL.get().setBattleLogic(DataResource.ac.getBean(BattleLogic.class));
    }

    public void setRecordData(BattlePb.BattleRoundPb.Builder recordData) {
        LOCAL.get().setRecordDataPb(recordData);
    }

    public BattlePb.BattleRoundPb.Builder getInitRecordData() {
        FightContext context = LOCAL.get();
        if (context.getRecordDataPb() == null) {
            BattlePb.BattleRoundPb.Builder builder = BattlePb.BattleRoundPb.newBuilder();
            context.setRecordDataPb(builder);
        }

        return context.getRecordDataPb();
    }

    public BattlePb.BattleRoundPb.Builder getRecordData() {
        FightContext context = LOCAL.get();
        return context.getRecordDataPb();
    }

    public ActionDirection getActionDirection() {
        return LOCAL.get().getActionDirection();
    }

    public List<Integer> getAtkHeroList() {
        List<Integer> list = LOCAL.get().getActionDirection().getAtkHeroList();
        if (CheckNull.isNull(list)) {
            list = new ArrayList<>();
            LOCAL.get().getActionDirection().setAtkHeroList(list);
        }

        return list;
    }

    public List<Integer> getDefHeroList() {
        List<Integer> list = LOCAL.get().getActionDirection().getDefHeroList();
        if (CheckNull.isNull(list)) {
            list = new ArrayList<>();
            LOCAL.get().getActionDirection().setDefHeroList(list);
        }

        return list;
    }

    public Force getCurAttacker() {
        return LOCAL.get().getActionDirection().getAtk();
    }

    public int getCurAtkHeroId() {
        return LOCAL.get().getActionDirection().getCurAtkHeroId();
    }

    public int getCurDefHeroId() {
        return LOCAL.get().getActionDirection().getCurDefHeroId();
    }

    public Force getCurDefender() {
        return LOCAL.get().getActionDirection().getDef();
    }

    public void setCurAtkHeroId(int atkHeroId) {
        LOCAL.get().getActionDirection().setCurAtkHeroId(atkHeroId);
    }

    public void setCurDefHeroId(int defHeroId) {
        LOCAL.get().getActionDirection().setCurDefHeroId(defHeroId);
    }

    public void clearActionList() {
        LOCAL.get().getActionDirection().setCurDefHeroId(0);
        LOCAL.get().getActionDirection().setCurAtkHeroId(0);
        LOCAL.get().getActionDirection().getAtkHeroList().clear();
        LOCAL.get().getActionDirection().getDefHeroList().clear();
    }

    public void resetActionDirection(Force atk, Force def, int curAtkHeroId, int curDefHeroId) {
        LOCAL.get().getActionDirection().setAtk(atk);
        LOCAL.get().getActionDirection().setDef(def);
        LOCAL.get().getActionDirection().setCurAtkHeroId(curAtkHeroId);
        LOCAL.get().getActionDirection().setCurDefHeroId(curDefHeroId);
    }

    public int getRoundNum() {
        return LOCAL.get().getRoundNum();
    }

    public long getAtkRoleId() {
        return LOCAL.get().getAtkRoleId();
    }

    public void setAtkRoleId(long atkRoleId) {
        LOCAL.get().setAtkRoleId(atkRoleId);
    }

    public Fighter getAtkFighter() {
        return LOCAL.get().getAtkFighter();
    }

    public Fighter getDefFighter() {
        return LOCAL.get().getDefFighter();
    }

    public List<FightEntity> getFightEntity() {
        return LOCAL.get().getFightEntityList();
    }

    public void setFightEntity(List<FightEntity> list) {
        LOCAL.get().setFightEntityList(list);
    }

    /**
     * 获取排过序的战斗实体
     *
     * @param force
     * @param target
     * @return
     */
    public List<FightEntity> getSortedFightEntity(Force force, Force target) {
        List<FightEntity> fightEntityList = getFightEntity();
        if (CheckNull.isNull(fightEntityList)) {
            fightEntityList = new ArrayList<>();
            setFightEntity(fightEntityList);
        }
        if (fightEntityList.size() <= 0) {
            fillFightEntity(force, fightEntityList);
            fillFightEntity(target, fightEntityList);
        } else {
            fightEntityList.forEach(fe -> {
                Force tmp = fe.getOwnId() == force.ownerId ? force : target;
                fe.setSpeed((int) FightCalc.attributeValue(FightCommonConstant.AttrId.SPEED, tmp, fe.getHeroId()));
            });
        }

        Collections.sort(fightEntityList);
        return fightEntityList;
    }

    private void fillFightEntity(Force force, List<FightEntity> fightEntityList) {
        fightEntityList.add(new FightEntity(force.ownerId, force.id,
                (int) FightCalc.attributeValue(FightCommonConstant.AttrId.SPEED, force, force.id)));
        if (!CheckNull.isEmpty(force.assistantHeroList)) {
            for (FightAssistantHero ass : force.assistantHeroList) {
                if (CheckNull.isNull(ass)) continue;
                fightEntityList.add(new FightEntity(force.ownerId, ass.getHeroId(), (int) FightCalc.attributeValue(FightCommonConstant.AttrId.SPEED, force, ass.getHeroId())));
            }
        }
    }

    public BattleLogic getBattleLogic() {
        return LOCAL.get().getBattleLogic();
    }

    public int getBattleType() {
        return LOCAL.get().getBattleType();
    }

    public Fighter getFighter(Force force) {
        if (getAtkFighter().isMyForce(force.ownerId))
            return getAtkFighter();
        return getDefFighter();
    }

    public HashMap<Integer, List<IFightBuff>> getBuffMap() {
        if (CheckNull.isEmpty(LOCAL.get().getTriggerBuffMap()))
            return null;
        return LOCAL.get().getTriggerBuffMap();
    }

    public void addBuff(IFightBuff fightBuff) {
        if (CheckNull.isNull(LOCAL.get().getTriggerBuffMap())) {
            LOCAL.get().setTriggerBuffMap(new HashMap<>());
        }
        if (!(fightBuff instanceof ConditionBuffImpl)) {
            return;
        }

        if (fightBuff instanceof ConditionBuffImpl) {
            List<Integer> condition;
            if (CheckNull.isNull(fightBuff.getBuffConfig()) || CheckNull.isEmpty(fightBuff.getBuffConfig().
                    getBuffTriggerCondition()) || CheckNull.isEmpty(condition = fightBuff.getBuffConfig().getBuffTriggerCondition().get(0)))
                return;
            LOCAL.get().getTriggerBuffMap().computeIfAbsent(condition.get(1),
                    l -> new LinkedList<>()).add(fightBuff);
        }
    }

    public void removeBuff(IFightBuff fightBuff) {
        if (CheckNull.isNull(LOCAL.get().getTriggerBuffMap())) {
            return;
        }
        if (!(fightBuff instanceof ConditionBuffImpl)) {
            return;
        }

        if (fightBuff instanceof ConditionBuffImpl) {
            List<Integer> condition;
            List<IFightBuff> fightBuffList;
            if (CheckNull.isNull(fightBuff.getBuffConfig()) || CheckNull.isEmpty(fightBuff.getBuffConfig().
                    getBuffTriggerCondition()) || CheckNull.isEmpty(condition = fightBuff.getBuffConfig().getBuffTriggerCondition().get(0)))
                return;
            fightBuffList = LOCAL.get().getTriggerBuffMap().get(condition.get(1));
            if (!CheckNull.isEmpty(fightBuffList)) {
                fightBuffList.remove(fightBuff);
            }
        }
    }

    public List<IFightBuff> getSortedBuff(int timing) {
        Map<Integer, List<IFightBuff>> buffMap;
        if (CheckNull.isEmpty(buffMap = LOCAL.get().getTriggerBuffMap()))
            return null;
        return buffMap.get(timing);
    }

    public void addRoundNum() {
        LOCAL.get().setRoundNum(getRoundNum() + 1);
    }

    public void clearRoundNum() {
        LOCAL.get().setRoundNum(0);
    }


    //********************************************pb****************************************************************

    public BattlePb.BothBattleEntityPb.Builder getInitBothBattleEntityPb() {
        if (LOCAL.get().getBothBattleEntityPb() == null) {
            LOCAL.get().setBothBattleEntityPb(FightPbUtil.createBothBattleEntityPb());
        } else {
            LOCAL.get().getBothBattleEntityPb().clear();
        }

        return LOCAL.get().getBothBattleEntityPb();
    }

    public BattlePb.BothBattleEntityPb.Builder getCurBothBattleEntityPb() {
        return LOCAL.get().getBothBattleEntityPb();
    }

    public BattlePb.BattlePreparationStage.Builder getInitPreparationStagePb(Force atk, Force def) {
        if (LOCAL.get().getPreparationStagePb() == null) {
            LOCAL.get().setPreparationStagePb(FightPbUtil.createBattlePreparationStagePb(atk, def));
        } else {
            LOCAL.get().getPreparationStagePb().clear();
        }

        return LOCAL.get().getPreparationStagePb();
    }

    public BattlePb.BattlePreparationStage.Builder getCurPreparationStagePb() {
        return LOCAL.get().getPreparationStagePb();
    }

    public BattlePb.SkillAction.Builder getInitSkillActionPb() {
        if (LOCAL.get().getSkillActionPb() == null) {
            LOCAL.get().setSkillActionPb(BattlePb.SkillAction.newBuilder());
        } else {
            LOCAL.get().getSkillActionPb().clear();
        }

        return LOCAL.get().getSkillActionPb();
    }

    public BattlePb.SkillAction.Builder getCurSkillActionPb() {
        return LOCAL.get().getSkillActionPb();
    }

    public void clearCurSkillActionPb() {
        LOCAL.get().setSkillActionPb(null);
    }

    public BattlePb.OrdinaryAttackAction.Builder getInitAttackActionPb() {
        if (LOCAL.get().getOrdinaryAttackActionPb() == null) {
            LOCAL.get().setOrdinaryAttackActionPb(BattlePb.OrdinaryAttackAction.newBuilder());
        } else {
            LOCAL.get().getOrdinaryAttackActionPb().clear();
        }

        return LOCAL.get().getOrdinaryAttackActionPb();
    }

    public BattlePb.OrdinaryAttackAction.Builder getCurAttackActionPb() {
        return LOCAL.get().getOrdinaryAttackActionPb();
    }

    public void clearCurAttackActionPb() {
        LOCAL.get().setOrdinaryAttackActionPb(null);
    }

    public BattlePb.SkillAction.Builder getCurEffectSkillActionPb() {
        return LOCAL.get().getEffectSkillActionPb();
    }

    public void setEffectSkillActionPb(BattlePb.SkillAction.Builder builder) {
        LOCAL.get().setEffectSkillActionPb(builder);
    }

    public BattlePb.OrdinaryAttackAction.Builder getCurEffectAttackActionPb() {
        return LOCAL.get().getEffectAttackActionPb();
    }

    public void setEffectAttackActionPb(BattlePb.OrdinaryAttackAction.Builder builder) {
        LOCAL.get().setEffectAttackActionPb(builder);
    }

    public BattlePb.MultiEffectAction.Builder getCurMultiEffectActionPb() {
        return LOCAL.get().getMultiEffectActionPb();
    }

    public void setCurMultiEffectActionPb(BattlePb.MultiEffectAction.Builder builder) {
        LOCAL.get().setMultiEffectActionPb(builder);
    }

    public BattlePb.BattleRoundStage.Builder getInitBattleRoundStagePb() {
        if (LOCAL.get().getBattleRoundStagePb() == null) {
            LOCAL.get().setBattleRoundStagePb(BattlePb.BattleRoundStage.newBuilder());
        } else {
            LOCAL.get().getBattleRoundStagePb().clear();
        }

        return LOCAL.get().getBattleRoundStagePb();
    }

    public BattlePb.BattleRoundStage.Builder getBattleRoundStagePb() {
        return LOCAL.get().getBattleRoundStagePb();
    }

    public BattlePb.BattleSettlementLossStage.Builder getInitBattleSettlementLossStagePb() {
        if (LOCAL.get().getBattleSettlementLoseStagePb() == null) {
            LOCAL.get().setBattleSettlementLoseStagePb(BattlePb.BattleSettlementLossStage.newBuilder());
        } else {
            LOCAL.get().getBattleSettlementLoseStagePb().clear();
        }

        return LOCAL.get().getBattleSettlementLoseStagePb();
    }

    public BattlePb.BattleSettlementLossStage.Builder getBattleSettlementLossStagePb() {
        return LOCAL.get().getBattleSettlementLoseStagePb();
    }

    public BattlePb.BattleRegroupStage.Builder getInitBattleRegroupStagePb() {
        if (LOCAL.get().getBattleRegroupStagePb() == null) {
            LOCAL.get().setBattleRegroupStagePb(BattlePb.BattleRegroupStage.newBuilder());
        } else {
            LOCAL.get().getBattleRegroupStagePb().clear();
        }

        return LOCAL.get().getBattleRegroupStagePb();
    }

    public BattlePb.BattleRegroupStage.Builder getBattleRegroupStagePb() {
        return LOCAL.get().getBattleRegroupStagePb();
    }

    public BattlePb.BattleEndStage.Builder getInitBattleEndStagePb() {
        if (LOCAL.get().getBattleEndStagePb() == null) {
            LOCAL.get().setBattleEndStagePb(BattlePb.BattleEndStage.newBuilder());
        } else {
            LOCAL.get().getBattleEndStagePb().clear();
        }

        return LOCAL.get().getBattleEndStagePb();
    }

    public BattlePb.BattleEndStage.Builder getBattleEndStagePb() {
        return LOCAL.get().getBattleEndStagePb();
    }

    public BattlePb.RoundAction.Builder getInitRoundActionPb() {
        if (LOCAL.get().getRoundActionPb() == null) {
            LOCAL.get().setRoundActionPb(BattlePb.RoundAction.newBuilder());
        } else {
            LOCAL.get().getRoundActionPb().clear();
        }

        return LOCAL.get().getRoundActionPb();
    }

    public BattlePb.RoundAction.Builder getRoundActionPb() {
        return LOCAL.get().getRoundActionPb();
    }

    abstract class InnerContextLocal<T> {

        private T value;

        protected T get() {
            if (value != null) return value;
            value = initialValue();
            return value;
        }

        abstract T initialValue();
    }
}

