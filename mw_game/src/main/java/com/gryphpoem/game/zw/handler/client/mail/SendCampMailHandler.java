package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb3.SendCampMailRq;
import com.gryphpoem.game.zw.pb.GamePb3.SendCampMailRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
* @ClassName: SendCampMailHandler
* @Description: (发送阵营邮件)
* @author chenqi
* @date 2018年8月1日
*
 */
public class SendCampMailHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		SendCampMailRq req = msg.getExtension(SendCampMailRq.ext);
		MailService mailService = getService(MailService.class);
		SendCampMailRs resp = mailService.sendCampMail(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(SendCampMailRs.ext, resp);
		}
	}
	
}
