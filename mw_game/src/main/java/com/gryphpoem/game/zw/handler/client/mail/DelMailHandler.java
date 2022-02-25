package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.DelMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.DelMailRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
 * @Description 删除邮件
 * @author TanDonghai
 *
 */
public class DelMailHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
		DelMailRq req = msg.getExtension(DelMailRq.ext);
		MailService mailService = getService(MailService.class);
		DelMailRs resp = mailService.delMail(getRoleId(), req);

		if (null != resp) {
			sendMsgToPlayer(DelMailRs.ext, resp);
		}
	}

}
