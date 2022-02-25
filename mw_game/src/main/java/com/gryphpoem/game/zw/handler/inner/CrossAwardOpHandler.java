package com.gryphpoem.game.zw.handler.inner;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.InnerHandler;
import com.gryphpoem.game.zw.crosssimple.service.PlayerForCrossService;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRq;
import com.gryphpoem.game.zw.pb.CrossPb.CrossAwardOpRs;

/**
 * @ClassName CrossAwardOpHandler.java
 * @Description 处理跨服扣资源
 * @author QiuKun
 * @date 2019年5月21日
 */
public class CrossAwardOpHandler extends InnerHandler {

    @Override
    public void action() throws MwException {
        CrossAwardOpRs req = getMsg().getExtension(CrossAwardOpRs.ext);
        PlayerForCrossService service = getService(PlayerForCrossService.class);
        CrossAwardOpRq res = service.crossAwardOp(getLordId(), req, (code) -> {
            if (req.getCmd() > 0) {
                // 向玩家发送错误码
                Base.Builder baseBuilder = Base.newBuilder();
                baseBuilder.setCmd(req.getCmd());
                baseBuilder.setCode(code);
                sendMsgToPlayer(getLordId(), baseBuilder);
            }
        });
        if (res != null && !req.getRollBack()) { // 回滚就发送结果
            sendMsgToCrossServer(CrossAwardOpRq.EXT_FIELD_NUMBER, CrossAwardOpRq.ext, res);
        }
    }

}
