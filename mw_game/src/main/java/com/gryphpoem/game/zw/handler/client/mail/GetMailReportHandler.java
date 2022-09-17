package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AsyncGameHandler;
import com.gryphpoem.game.zw.manager.MailReportDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.MailReportDao;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

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
        if (mail.getReportStatus() == MailConstant.EXPIRED_REPORT) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "邮件战报已过期, roleId:", getRoleId(), ", keyId: ", req.getMailKeyId());
        }

        // 先从缓存中查, 若未找到则在数据库中查
        CommonPb.Report report = DataResource.ac.getBean(MailReportDataManager.class).getReport(getRoleId(), req.getMailKeyId());
        if (CheckNull.isNull(report)) {
            DbMailReport dbMailReport = DataResource.ac.getBean(MailReportDao.class).selectMailReport(getRoleId(), req.getMailKeyId());
            if (Objects.nonNull(dbMailReport) && !ObjectUtils.isEmpty(dbMailReport.getReport())) {
                report = CommonPb.Report.parseFrom(dbMailReport.getReport());
            }
        }

        GamePb5.GetMailReportRs.Builder builder = GamePb5.GetMailReportRs.newBuilder();
        if (Objects.nonNull(report)) {
            builder.setReport(report);
        }
        builder.setMailKeyId(req.getMailKeyId());
        sendMsgToPlayer(PbHelper.createRsBase(GamePb5.GetMailReportRs.EXT_FIELD_NUMBER, GamePb5.GetMailReportRs.ext, builder.build()));
    }
}
