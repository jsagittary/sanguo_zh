package com.gryphpoem.game.zw.handler.inner;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.InnerHandler;

/**
 * 直接转到客户端
 * 
 * @author 柳建星
 * @date 2019/03/26
 */
public class ClientRoutHandler extends InnerHandler {

    @Override
    public void action() throws MwException {
        directTranspondClient();
    }
}
