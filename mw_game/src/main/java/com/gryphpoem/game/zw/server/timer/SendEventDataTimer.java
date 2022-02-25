package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;

public class SendEventDataTimer extends TimerEvent {

	public SendEventDataTimer() {
		super(-1, 5000);
	}

	@Override
	public void action() {
		EventDataUp.allRequest();
	}

}
