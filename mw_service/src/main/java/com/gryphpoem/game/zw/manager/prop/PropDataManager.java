package com.gryphpoem.game.zw.manager.prop;

import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ClassUtil;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 15:15
 */
@Component
public class PropDataManager {

    private ConcurrentHashMap<Integer, AbstractUseProp> usePropMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void load() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Set<Class<?>> classes = ClassUtil.getClasses(AbstractUseProp.class.getPackage());
        if (CheckNull.isEmpty(classes)) return;
        for (Class clazz : classes) {
            List<Class<?>> superClasses = ClassUtil.getSuperClass(clazz);
            if (CheckNull.isEmpty(superClasses)) continue;
            if (!superClasses.contains(AbstractUseProp.class)) continue;
            AbstractUseProp abstractUseProp = (AbstractUseProp) clazz.getConstructor(null).newInstance();
            usePropMap.put(abstractUseProp.propType(), abstractUseProp);
        }
    }

    /**
     * 获取使用道具逻辑
     *
     * @param propType
     * @return
     */
    public AbstractUseProp useProp(int propType) {
        AbstractUseProp useProp;
        if ((useProp = usePropMap.get(propType)) == null) {
            return usePropMap.get(-1);
        }
        return useProp;
    }
}
