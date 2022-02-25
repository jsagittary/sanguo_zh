package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb.ModNameRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

public class ModNameRqHandler extends HttpHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		//Auto-generated method stub
		ModNameRq req = msg.getExtension(ModNameRq.ext);
		modName(req, this);
//		GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
//		toolService.modName(req, this);
	}
	
	public void modName(final ModNameRq req, final HttpHandler handler) {
        AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
            @Override
            public void action() {
                long lordId = req.getLordId();
                String porps = req.getName();
                GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
                toolService.modNameLogic(lordId, porps);
            }
        }, DealType.MAIN);

    }
}
