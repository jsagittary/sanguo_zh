package com.gryphpoem.game.zw.handler.client.active;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb4.ThreeRebateRq;
import com.gryphpoem.game.zw.pb.GamePb4.ThreeRebateRs;
import com.gryphpoem.game.zw.service.activity.ActivityService;

/***
 * @data Create in 2018-11-13 19:18
 * @author ZhuJianJian
 * @description 三倍返利活动
 */
public class ThreeRebateHandler extends ClientHandler{

	@Override
	public void action() throws MwException {
		  ThreeRebateRq req = msg.getExtension(ThreeRebateRq.ext);
		  ActivityService service = getService(ActivityService.class);
		  ThreeRebateRs rs =  service.getThreeRebate(req,getRoleId());
		  if (null != rs) {
	            sendMsgToPlayer(ThreeRebateRs.ext,rs);
	        }
	    }


}
