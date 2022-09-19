package com.gryphpoem.game.zw.handler.client.mail;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AsyncGameHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
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
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.util.ObjectUtils;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

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
            throw new MwException(GameError.MAIL_REPORT_EXPIRED.getCode(), "邮件战报已过期, roleId:", getRoleId(), ", keyId: ", req.getMailKeyId());
        }

        // 先从缓存中查, 若未找到则在数据库中查
        DbMailReport dbMailReport = null;
        MailReportDataManager mailReportDataManager = DataResource.ac.getBean(MailReportDataManager.class);
        CommonPb.Report report = mailReportDataManager.getReport(getRoleId(), req.getMailKeyId());
        if (CheckNull.isNull(report)) {
            synchronized (player.getLock()) {
                if (mail.getReportStatus() == MailConstant.EXPIRED_REPORT) {
                    throw new MwException(GameError.MAIL_REPORT_EXPIRED.getCode(), "邮件战报已过期, roleId:", getRoleId(), ", keyId: ", req.getMailKeyId());
                }

                report = mailReportDataManager.getReport(getRoleId(), req.getMailKeyId());
                if (CheckNull.isNull(report)) {
                    dbMailReport = DataResource.ac.getBean(MailReportDao.class).selectMailReport(getRoleId(), req.getMailKeyId());
                    // 若数据库中查找不到战报, 则将战报置为过期战报
                    if (CheckNull.isNull(dbMailReport)) mail.setReportStatus(MailConstant.EXPIRED_REPORT);
                    if (Objects.nonNull(dbMailReport) && !ObjectUtils.isEmpty(dbMailReport.getReport())) {
                        report = CommonPb.Report.parseFrom(dbMailReport.getReport());
                        LongAdder counter = mailReportDataManager.dataBaseCount(getRoleId(), req.getMailKeyId());
                        if (counter.longValue() >= 10) {
                            LogUtil.debug(String.format("roleId:%d, mailKeyId:%d, 已在数据库中查询10次此封邮件!", getRoleId(), req.getMailKeyId()));
                            // 暂时存放在缓存中
                            mailReportDataManager.addReport(getRoleId(), req.getMailKeyId(), report, false);
                            dbMailReport.setExpireTime(TimeHelper.getCurrentSecond() + 3 * TimeHelper.MINUTE);
                            mailReportDataManager.addRemoveDelayQueue(dbMailReport);
                        }
                    }
                }
            }

            if (Objects.nonNull(dbMailReport)) {
                LongAdder counter = mailReportDataManager.dataBaseCount(getRoleId(), req.getMailKeyId());
                if (counter.longValue() < 10) {
                    counter.increment();
                } else {
                    counter.reset();
                }
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
