package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb.ModPropRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

public class ModPropRqHandler extends HttpHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		//Auto-generated method stub
		ModPropRq req = msg.getExtension(ModPropRq.ext);
		modProp(req, this);
//		GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
//		toolService.modProp(req, this);
	}
	
	public void modProp(final ModPropRq req, final HttpHandler handler) {
        AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
            @Override
            public void action() {
                long lordId = req.getLordId();
                int type = req.getType();
                String porps = req.getProps();
                GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
                toolService.modPropLogic(lordId, type, porps);
            }
        }, DealType.MAIN);

    }
}
