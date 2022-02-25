package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PlayerService;

public class SavePlayerTimer extends TimerEvent {

	public SavePlayerTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		AppGameServer.ac.getBean(PlayerService.class).saveTimerLogic();
	}

}
