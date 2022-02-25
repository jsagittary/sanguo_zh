package com.gryphpoem.game.zw.manager;

import org.springframework.stereotype.Component;

/**
 * @ClassName BaseAwkwardDataManager.java
 * @Description 尴尬的DataManager,为解决DataManager调用不到Service的尴尬情况,可以在此处添加对应的方法,让对应service去继承此类并实现对应的方法
 * @author QiuKun
 * @date 2018年10月31日
 */
@Component("baseAwkwardDataManager")
public class BaseAwkwardDataManager {

    /**
     * RebelService#initRebellion()的方法
     */
    public void initRebellion() {
    }

    public void testInvoke() {
    }
}
