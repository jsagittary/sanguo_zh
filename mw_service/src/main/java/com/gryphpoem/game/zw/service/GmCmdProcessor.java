package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收集被{@link GmCmd}注解的方法
 * @author xwind
 * @date 2021/7/28
 */
@Component
public class GmCmdProcessor implements BeanPostProcessor {
    private Map<String,Relation> relationMap = new HashMap<>();
    private List<String> errors = new ArrayList<>();

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String s) throws BeansException {
        if(bean instanceof GmCmdService || bean instanceof GmService){
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
            if (methods != null) {
                for (Method method : methods) {
                    GmCmd gmCmd = AnnotationUtils.findAnnotation(method, GmCmd.class);
                    if (null != gmCmd) {
                        Relation relation = new Relation(bean,method);
                        if(relationMap.containsKey(gmCmd.value())){
                            LogUtil.gm(String.format("Gm命令=%s已存在,%s",gmCmd.value(),relationMap.get(gmCmd.value())));
                            errors.add(gmCmd.value());
                        }else {
                            relationMap.put(gmCmd.value(),relation);
                        }
                    }
                }
            }
        }
        return bean;
    }

    public Relation getRelation(String cmd) {
        return relationMap.get(cmd);
    }

    public class Relation {
        private Object ins;
        private Method method;
        private Relation(Object ins,Method method){
            this.ins = ins;
            this.method = method;
        }
        public void invoke(Player player,String... params) throws InvocationTargetException, IllegalAccessException {
            this.method.invoke(ins,player,params);
        }

        @Override
        public String toString() {
            return "Relation{" +
                    "ins=" + ins +
                    ", method=" + method +
                    '}';
        }
    }

    public void checkErrors() throws Exception {
        if(!errors.isEmpty()){
            throw new Exception(String.format("存在重复的GM命令, %s", ListUtils.toString(errors)));
        }
    }
}
