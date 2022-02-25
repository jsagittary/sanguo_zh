package com.gryphpoem.game.zw.handler.http;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.handler.HttpHandler;
import com.gryphpoem.game.zw.pb.HttpPb.ModLordRq;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmToolService;

public class ModLordRqHandler extends HttpHandler {

	/**
	 * Overriding: action
	 * 
	 * @see com.game.server.ICommand#action()
	 */
	@Override
	public void action() {
		//Auto-generated method stub
		ModLordRq req = msg.getExtension(ModLordRq.ext);
		modLord(req, this);
//		GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
//		toolService.modLord(req, this);
	}
	
	public void modLord(final ModLordRq req, final HttpHandler handler) {
        AppGameServer.getInstance().mainLogicServer.addCommand(new ICommand() {
            @Override
            public void action() {
                long lordId = req.getLordId();
                int type = req.getType();
                String keyId = req.getKeyId();
                String value = req.getValue();
                String value2 = req.getValue2();
                GmToolService toolService = AppGameServer.ac.getBean(GmToolService.class);
                toolService.modLordLogic(lordId, type, keyId, value, value2);
            }
        }, DealType.MAIN);

    }
}
