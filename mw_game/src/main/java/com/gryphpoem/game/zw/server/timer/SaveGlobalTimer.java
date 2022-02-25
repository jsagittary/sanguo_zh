package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.server.AppGameServer;

public class SaveGlobalTimer extends TimerEvent {

	public SaveGlobalTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		AppGameServer.ac.getBean(GlobalDataManager.class).saveGlobalTimerLogic();
	}

}
