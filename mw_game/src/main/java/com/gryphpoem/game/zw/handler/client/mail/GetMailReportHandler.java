package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AsyncGameHandler;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.util.CheckNull;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-12 18:45
 */
public class GetMailReportHandler extends AsyncGameHandler {
    @Override
    public void action() throws Exception {
        GamePb5.GetMailReportRq req = msg.getExtension(GamePb5.GetMailReportRq.ext);
        Player player = DataResource.ac.getBean(PlayerDataManager.class).checkPlayerIsExist(getRoleId());
        Mail mail = player.mails.get(req.getMailKeyId());
        if (CheckNull.isNull(mail)) {
            throw new MwException(GameError.MAIL_NOT_EXIST.getCode(), "获取邮件列表为空, roleId:", getRoleId());
        }
    }
}
