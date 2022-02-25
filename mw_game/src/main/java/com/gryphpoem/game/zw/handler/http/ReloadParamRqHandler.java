package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.pb.HttpPb.ReloadParamRq;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmService;

public class ReloadParamRqHandler extends HttpHandler {

    @Override
    public void action(){
        ReloadParamRq req = msg.getExtension(ReloadParamRq.ext);

        GmService toolService = AppGameServer.ac.getBean(GmService.class);
        String str = "";
        if (req.getType() == 1) {
            str = "loadSystem";
        } else if (req.getType() == 2) {
            str = "loadTable";
        } else if (req.getType() == 3) {
            str = "loadBlackWords";   //加载屏蔽词
        }else if (req.getType() == 4) {
            str = "mergeBanner";    //加载合服banner配置
        }
        try {
            toolService.gmSystem(str);
        }catch (Exception e) {
//            builder.setCode(GameError.PARAM_ERROR.getCode());
//            builder.setMsg("error");
            LogUtil.error("后台刷新配置异常!", e);
        }
//        BasePb.Base.Builder baseBuilder = PbHelper.createRqBase(HttpPb.BackReloadParamRq.EXT_FIELD_NUMBER, null, HttpPb.BackReloadParamRq.ext,
//                builder.build());
//        AppGameServer.getInstance().sendMsgToPublic(baseBuilder, 1);

    }
}
