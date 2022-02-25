package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2.ReadAllMailRq;
import com.gryphpoem.game.zw.pb.GamePb2.ReadAllMailRs;
import com.gryphpoem.game.zw.service.MailService;

/**
 * 
 * @Description 设置邮件全部已读
 * @author TanDonghai
 *
 */
public class ReadAllMailHandler extends ClientHandler {

	@Override
	public void action() throws MwException {
	    ReadAllMailRq req = msg.getExtension(ReadAllMailRq.ext);
		MailService mailService = getService(MailService.class);
		ReadAllMailRs resp = mailService.readAllMail(getRoleId(),req.getKeyIdList());

		if (null != resp) {
			sendMsgToPlayer(ReadAllMailRs.ext, resp);
		}
	}

}
