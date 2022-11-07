package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.push.util.CheckNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        LOCAL.get().setBattleLogic(DataResource.ac.getBean(BattleLogic.class));
    }

    public void setRecordData(CommonPb.Record.Builder recordData) {
        LOCAL.get().setRecordData(recordData);
    }

    public CommonPb.Record.Builder getRecordData() {
        FightContext context = LOCAL.get();
        return context.getRecordData();
    }

    public Force getAttacker() {
        FightContext context = LOCAL.get();
        return context.getAttacker();
    }

    public void setAttacker(Force attacker) {
        FightContext context = LOCAL.get();
        context.setAttacker(attacker);
    }

    public Force getDefender() {
        FightContext context = LOCAL.get();
        return context.getDefender();
    }

    public void setDefender(Force defender) {
        FightContext context = LOCAL.get();
        context.setDefender(defender);
    }

    public int getRoundNum() {
        return LOCAL.get().getRoundNum();
    }

    public void setFightId(long fightId) {
        LOCAL.get().setFightId(fightId);
    }

    public void setRecordFlag(boolean recordFlag) {
        LOCAL.get().setRecordFlag(recordFlag);
    }

    public boolean isRecordFlag() {
        return LOCAL.get().isRecordFlag();
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
        if (CheckNull.isEmpty(fightEntityList)) {
            fightEntityList = new ArrayList<>();
            setFightEntity(fightEntityList);
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

