package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.PlayerService;

/**
 * 1.buff刷新
 * 2.恢复体力
 * 3.检查内挂
 * 4.周年活动刷新彩蛋
 * 5.丰收转盘刷新
 * 6.幸福度恢复
 * 7.人口恢复
 * @author tyler
 *
 */
public class RestoreTimer extends TimerEvent {
	public RestoreTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		AppGameServer.ac.getBean(PlayerService.class).handlePlayerState();
	}

}
