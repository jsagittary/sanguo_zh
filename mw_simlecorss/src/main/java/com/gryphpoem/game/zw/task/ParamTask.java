package com.gryphpoem.game.zw.task;

/**
 * @ClassName ParamTask.java
 * @Description
 * @author QiuKun
 * @date 2019年6月19日
 */
public abstract class ParamTask extends AbstractTask {
    protected Object param;

    public ParamTask(Object param) {
        super();
        this.param = param;
    }

    public Object getParam() {
        return param;
    }

}
