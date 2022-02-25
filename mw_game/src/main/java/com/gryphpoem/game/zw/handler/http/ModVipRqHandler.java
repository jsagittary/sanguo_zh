package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb.ModVipRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

public class ModVipRqHandler extends HttpHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		//Auto-generated method stub
		ModVipRq req = msg.getExtension(ModVipRq.ext);
		modVip(req, this);
//		GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
//		toolService.modVip(req, this);
	}
	
	public void modVip(final ModVipRq req, final HttpHandler handler) {
        AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
            @Override
            public void action() {
                long lordId = req.getLordId();
                int type = req.getType();
                int value = req.getValue();
                GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
                toolService.modVipLogic(lordId, type, value);
            }
        }, DealType.MAIN);

    }
}
