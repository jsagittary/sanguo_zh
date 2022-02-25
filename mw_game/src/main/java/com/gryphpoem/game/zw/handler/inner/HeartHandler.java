package com.gryphpoem.game.zw.handler.inner;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.InnerHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.CrossPb.HeartRs;

/**
 * @ClassName HeartHandler.java
 * @Description
 * @author QiuKun
 * @date 2019年5月8日
 */
public class HeartHandler extends InnerHandler {

    @Override
    public void action() throws MwException {
         HeartRs h = getMsg().getExtension(HeartRs.ext);
         LogUtil.debug("跨服的时间 : " + h.getTime());
    }

}
