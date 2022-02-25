package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base.Builder;
import com.gryphpoem.game.zw.pb.GamePb3.GetPaySerialIdRs;
import com.gryphpoem.game.zw.pb.HttpPb.PayApplyRs;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticPay;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PayService;

/**
 * @ClassName PayApplyRsHandler.java
 * @Description 订单号生成返回处理
 * @author QiuKun
 * @date 2017年7月22日
 */
public class PayApplyRsHandler extends HttpHandler {

    @Override
    public void action() throws MwException {
        Builder base = null;
        PayApplyRs req = msg.getExtension(PayApplyRs.ext);
        long roleId = req.getRoleId();
        int code = msg.getCode();
        LogUtil.debug("PayApplyRs: roleId:", roleId, ", code:", code);
        if (code == GameError.OK.getCode()) {
            PayService server = AppGameServer.ac.getBean(PayService.class);
            GetPaySerialIdRs res = server.payApplyRsProcess(req.getSerialId(), roleId, req.getPayType(),
                    req.getPlatName());
            base = PbHelper.createRsBase(GetPaySerialIdRs.EXT_FIELD_NUMBER, GetPaySerialIdRs.ext, res);
        } else {
            base = PbHelper.createErrorBase(GetPaySerialIdRs.EXT_FIELD_NUMBER, msg.getCode(), 0);
        }
        PlayerDataManager playerDataManager = AppGameServer.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.getPlayer(roleId);
        if (null != player && player.ctx != null && player.isLogin) {
            AppGameServer.getInstance().sendMsgToGamer(player.ctx, base);
            //数据上报
            StaticPay sPay = StaticVipDataMgr.getStaticPayByPayId(req.getPayType());
            EventDataUp.orderCreate(player.account,player.lord,req.getSerialId(),sPay.getPrice(),req.getPayType());
        }
    }

}
