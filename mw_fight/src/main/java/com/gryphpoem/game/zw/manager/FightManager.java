package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.util.ClassUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.event.FightEvent;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightContextHolder;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticEffectRule;
import com.gryphpoem.push.util.CheckNull;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description: buff effect集合类
 * Author: zhangpeng
 * createTime: 2022-10-21 15:56
 */
@Component
public class FightManager {
    /**
     * buff class集合
     */
    private ConcurrentHashMap<Integer, Class<? extends IFightBuff>> buffClazzMap = new ConcurrentHashMap<>();
    /**
     * effect class集合
     */
    private ConcurrentHashMap<Integer, IFightEffect> effectMap = new ConcurrentHashMap<>();
    /**
     * buff触发时机集合
     */
    private ConcurrentHashMap<Integer, IFightBuffWork> buffWorkMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void load() {
        EventBus.getDefault().register(this);

        Set<Class<?>> classes = ClassUtil.getClasses(IFightBuff.class.getPackage());
        if (CheckNull.isEmpty(classes)) return;
        for (Class clazz : classes) {
            if (CheckNull.isNull(clazz))
                continue;
            Annotation ann = clazz.getAnnotation(BuffEffectType.class);
            if (CheckNull.isNull(ann)) {
                // buff触发时机逻辑类
                List<Class<?>> superClasses = ClassUtil.getSuperClass(clazz);
                if (CheckNull.isEmpty(superClasses)) continue;
                if (!superClasses.contains(IFightBuffWork.class)) continue;
                IFightBuffWork iFightBuffWork;
                try {
                    iFightBuffWork = (IFightBuffWork) clazz.getConstructor(null).newInstance();
                } catch (Exception e) {
                    LogUtil.error("", e);
                    continue;
                }
                if (ObjectUtils.isEmpty(iFightBuffWork.effectTiming())) continue;
                Arrays.stream(iFightBuffWork.effectTiming()).forEach(i -> buffWorkMap.put(i, iFightBuffWork));
                continue;
            }

            BuffEffectType wayAnn = (BuffEffectType) ann;
            switch (wayAnn.buffEffect()) {
                case FightConstant.BuffEffect.BUFF:
                    buffClazzMap.put(wayAnn.type(), clazz);
                    break;
                case FightConstant.BuffEffect.EFFECT:
                    IFightEffect fightEffect;
                    try {
                        fightEffect = (IFightEffect) clazz.getConstructor(null).newInstance();
                    } catch (Exception e) {
                        LogUtil.error("", e);
                        break;
                    }
                    if (Objects.nonNull(fightEffect) && !ArrayUtils.isEmpty(fightEffect.effectType())) {
                        Arrays.stream(fightEffect.effectType()).forEach(e -> effectMap.put(e, fightEffect));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 判断buff是否可以释放
     *
     * @param fightBuff
     * @param contextHolder
     * @param timing
     * @param params
     * @return
     */
    public boolean buffCanRelease(IFightBuff fightBuff, FightContextHolder contextHolder, int timing, List<Integer> conditionConfig, Object... params) {
        IFightBuffWork work;
        if ((work = buffWorkMap.get(timing)) != null) {
            return work.buffCanEffect(fightBuff, contextHolder, conditionConfig, params);
        }

        return true;
    }

    /**
     * 效果可否释放
     *
     * @param contextHolder
     * @param rule
     * @param params
     * @return
     */
    public boolean effectCanRelease(FightContextHolder contextHolder, StaticEffectRule rule, Object... params) {
        IFightEffect fightEffect;
        if ((fightEffect = effectMap.get(rule.getEffectLogicId())) != null) {
            return fightEffect.canEffect(contextHolder, rule, params);
        }

        return true;
    }

    /**
     * 创建战斗buff对象
     *
     * @param buffType
     * @param staticBuff
     * @return
     */
    public IFightBuff createFightBuff(int buffType, StaticBuff staticBuff) {
        Class<? extends IFightBuff> clazz = buffClazzMap.get(buffType);
        if (CheckNull.isNull(clazz)) return null;
        try {
            return clazz.getConstructor(StaticBuff.class).newInstance(staticBuff);
        } catch (Exception e) {
            LogUtil.error("", e);
            return null;
        }
    }

    /**
     * 返回技能效果实例
     *
     * @param effectLogicId
     * @return
     */
    public IFightEffect getSkillEffect(int effectLogicId) {
        return effectMap.get(effectLogicId);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void triggerBuffEffect(FightEvent.BuffTriggerEvent event) {
        FightContextHolder contextHolder = event.contextHolder;
        List<IFightBuff> fightBuffList = contextHolder.getSortedBuff(event.timing);
        if (CheckNull.isEmpty(fightBuffList))
            return;

        fightBuffList.forEach(fightBuff -> fightBuff.releaseEffect(contextHolder, event.timing, event.params));
    }
}
