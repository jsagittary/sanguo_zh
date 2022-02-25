package com.gryphpoem.game.zw.mgr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName DataLoaderMgr.java
 * @Description
 * @author QiuKun
 * @date 2019年5月17日
 */
@Component
public class DataLoaderMgr {
    @Autowired
    private FortressMgr fortressMgr;

    public void dataHandle() {

        fortressMgr.init();
        LogUtil.start("加载完成：堡垒相关");
    }
}
