package com.gryphpoem.game.zw.handler.client.world;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.CityRenameRq;
import com.gryphpoem.game.zw.pb.GamePb2.CityRenameRs;
import com.gryphpoem.game.zw.service.CityService;

/**
 * 
 * @Description 修改城池名称
 * @author QiuKun
 *
 */
public class CityRenameHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        CityRenameRq req = msg.getExtension(CityRenameRq.ext);
        CityService cityService = getService(CityService.class);
        CityRenameRs resp = cityService.cityRename(getRoleId(), req.getCityId(), req.getName());
        if (null != resp) {
            sendMsgToPlayer(CityRenameRs.ext, resp);
        }
    }

}
