package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmService;

import java.util.Objects;

/**
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-09-18 15:50
 */
public class ModifyServerInfoHandler extends HttpHandler {

    @Override
    public void action() throws MwException {
        final HttpPb.ModifyServerInfoRq req = msg.getExtension(HttpPb.ModifyServerInfoRq.ext);

        GmService gmService = AppGameServer.ac.getBean(GmService.class);
        // 修改区服信息
        HttpPb.BackModifyServerInfoRq backModifyServerInfoRq = gmService.gmModifyServerInfo(req);
        if (Objects.nonNull(backModifyServerInfoRq)) {
            BasePb.Base.Builder base = PbHelper.createRqBase(HttpPb.BackModifyServerInfoRq.EXT_FIELD_NUMBER, null,
                    HttpPb.BackModifyServerInfoRq.ext, backModifyServerInfoRq);
            // 发送执行结果到账号服
            AppGameServer.getInstance().sendMsgToPublic(base);
        }
    }
}