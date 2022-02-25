package com.gryphpoem.game.zw.core.util;

import com.gryphpoem.push.util.CheckNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 提供一些Java反射相关的方法
 *
 * @author TanDonghai
 * @date 创建时间：2017年7月5日 下午5:59:48
 */
public abstract class ReflectUtil {

    /**
     * 类中是否有指定方法（不查找父类）
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static boolean containsMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (null == clazz || CheckNull.isNullTrim(methodName)) {
            return false;
        }

        try {
            return clazz.getMethod(methodName, parameterTypes) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 反射执行静态方法
     *
     * @param clazz
     * @param methodName
     * @param params
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... params)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] paramClasses = null;
        if (!CheckNull.isEmpty(params)) {
            paramClasses = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                paramClasses[i] = param.getClass();
            }
        }
        Method m = clazz.getMethod(methodName, paramClasses);
        return m.invoke(null, params);
    }

    /**
     * 反射执行方法
     *
     * @param obj
     * @param methodName
     * @param params
     * @return
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object obj, String methodName, Object... params) throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?>[] paramClasses = null;
        if (!CheckNull.isEmpty(params)) {
            paramClasses = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                paramClasses[i] = param.getClass();
            }
        }
        Method m = obj.getClass().getMethod(methodName, paramClasses);
        return m.invoke(obj, params);
    }

    /**
     * 获取类中的静态参数的值，包括常量
     *
     * @param className 类全名（包含包路径的全名）
     * @param paramName 参数名
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public static Object getStaticField(String className, String paramName) throws NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        if (CheckNull.isNullTrim(className)) {
            throw new IllegalArgumentException("传入的类名为空");
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = loader.loadClass(className);
        return getStaticField(clazz, paramName);
    }

    /**
     * 获取类中的静态变量的值，包括常量
     *
     * @param clazz 类
     * @param paramName 参数名
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object getStaticField(Class<?> clazz, String paramName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        return getField(clazz, paramName, null);// 获取静态变量或常量的值，不需要传入对象
    }

    /**
     * 获取类或对象中的某个参数的值
     *
     * @param clazz 类
     * @param paramName 参数
     * @param obj 类的实例对象
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object getField(Class<?> clazz, String paramName, Object obj)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (null == clazz) {
            if (null == obj) {
                throw new IllegalArgumentException("类不能为空");
            }

            clazz = obj.getClass();
        } else if (null != obj) {
            if (!clazz.getName().equals(obj.getClass().getName())) {
                throw new IllegalArgumentException("传入的类和实例对象不匹配");
            }
        }

        if (CheckNull.isNullTrim(paramName)) {
            throw new IllegalArgumentException("参数名不能为空, class:" + clazz.getName() + ", paramName:" + paramName);
        }

        Field field = clazz.getField(paramName);
        return field.get(obj);
    }
}
