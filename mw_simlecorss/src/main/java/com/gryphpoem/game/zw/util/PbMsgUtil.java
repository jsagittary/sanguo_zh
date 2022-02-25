package com.gryphpoem.game.zw.util;

import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.mgr.SessionMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.BasePb.Base.Builder;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.server.CrossServer;

/**
 * @ClassName PbMsgUtil.java
 * @Description
 * @author QiuKun
 * @date 2019年5月13日
 */
public abstract class PbMsgUtil {

    public static <T> Base.Builder okBase(int cmd, long lordId, GeneratedExtension<Base, T> ext, T msg) {
        return PbCrossUtil.createBase(cmd, GameError.OK.getCode(), lordId, ext, msg);
    }

    public static <T> void sendMsgToPlayer(CrossPlayer crossPlayer, int cmd, int code, GeneratedExtension<Base, T> ext,
            T msg) {
        SessionMgr sessionMgr = CrossServer.ac.getBean(SessionMgr.class);
        Base base = PbCrossUtil.createBase(cmd, code, crossPlayer.getLordId(), ext, msg).build();
        sessionMgr.sendMsg(base, crossPlayer.getMainServerId());
    }

    public static <T> void sendOkMsgToPlayer(CrossPlayer crossPlayer, int cmd, GeneratedExtension<Base, T> ext, T msg) {
        sendMsgToPlayer(crossPlayer, cmd, GameError.OK.getCode(), ext, msg);
    }

    public static void sendErrToPlayerLog(CrossPlayer crossPlayer, int cmd, MwException e) {
        LogUtil.error(e.getMessage(), e);
        SessionMgr sessionMgr = CrossServer.ac.getBean(SessionMgr.class);
        Builder errBuilder = PbCrossUtil.createBase(cmd, e.getCode(), crossPlayer.getLordId(), null, null);
        sessionMgr.sendMsg(errBuilder.build(), crossPlayer.getMainServerId());
    }
}
