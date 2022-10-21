package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.game.zw.buff.IFightBuffWork;
import com.gryphpoem.game.zw.buff.IFightEffect;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.util.ClassUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.annotation.BuffEffectType;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.push.util.CheckNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.List;
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
    private ConcurrentHashMap<Integer, Class<? extends IFightEffect>> effectClazzMap = new ConcurrentHashMap<>();
    /**
     * buff触发时机集合
     */
    private ConcurrentHashMap<Integer, IFightBuffWork> buffWorkMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void load() {
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
                buffWorkMap.put(iFightBuffWork.effectTiming(), iFightBuffWork);
                continue;
            }

            BuffEffectType wayAnn = (BuffEffectType) ann;
            switch (wayAnn.buffEffect()) {
                case FightConstant.BuffEffect.BUFF:
                    buffClazzMap.put(wayAnn.type(), clazz);
                    break;
                case FightConstant.BuffEffect.EFFECT:
                    effectClazzMap.put(wayAnn.type(), clazz);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 判断buff是否可以释放
     *
     * @param attacker
     * @param defender
     * @param fightLogic
     * @param timing
     * @param params
     * @return
     */
    public boolean buffCanRelease(Force attacker, Force defender, FightLogic fightLogic, int timing, Object... params) {
        IFightBuffWork work;
        if ((work = buffWorkMap.get(timing)) != null) {
            return work.buffCanEffect(attacker, defender, fightLogic, timing, params);
        }

        return true;
    }
}
