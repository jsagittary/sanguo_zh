package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.service.MailService;

/**
 * @Author: ZhouJie
 * @Date: Create in 2017-11-15 11:40
 * @Description:
 * @Modified By:
 */
public class LockMailHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        GamePb2.LockMailRq req = msg.getExtension(GamePb2.LockMailRq.ext);
        MailService mailService = getService(MailService.class);
        GamePb2.LockMailRs resp = mailService.lockMail(getRoleId(),req.getKeyIdList());

        if (null != resp) {
            sendMsgToPlayer(GamePb2.LockMailRs.ext, resp);
        }
    }
}
