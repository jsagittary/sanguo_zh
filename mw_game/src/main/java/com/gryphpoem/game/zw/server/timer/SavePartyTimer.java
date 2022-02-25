package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.server.AppGameServer;

public class SavePartyTimer extends TimerEvent {

	public SavePartyTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		AppGameServer.ac.getBean(CampDataManager.class).savePartyTimerLogic();
	}

}
