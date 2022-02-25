/**   
 * @Title: SavePartyTimer.java    
 * @Package com.game.server.timer    
 * @Description:   
 * @author ZhangJun   
 * @date 2015年9月12日 下午4:39:08    
 * @version V1.0   
 */
package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.ActivityTriggerService;

/**
 * @ClassName: SavePartyTimer
 * @Description: 
 * @author ZhangJun
 * @date 2015年9月12日 下午4:39:08
 * 
 */
public class SaveActivityTimer extends TimerEvent {

	public SaveActivityTimer() {
		super(-1, 10000);
	}

	/**
	 * Overriding: action
	 * 
	 * @see ICommand#action()
	 */
	@Override
	public void action() {
		//Auto-generated method stub
		AppGameServer.ac.getBean(ActivityService.class).saveActivityTimerLogic();
		AppGameServer.ac.getBean(ActivityTriggerService.class).checkTriggerByEndTime();
	}

}
