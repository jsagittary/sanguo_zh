package com.gryphpoem.game.zw.rpc.callback.mail;

import com.gryphpoem.cross.gameplay.mail.c2g.dto.CrossCollectMail;
import com.gryphpoem.cross.gameplay.mail.c2g.dto.CrossMailTemplate;
import com.gryphpoem.cross.gameplay.mail.c2g.dto.CrossReportMail;
import com.gryphpoem.cross.gameplay.mail.c2g.dto.CrossScoutMail;
import com.gryphpoem.cross.gameplay.mail.c2g.service.GameMailService;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class GameMailServiceCallbackServiceImpl implements GameMailService {

    @Override
    public void sendNormalMail(CrossMailTemplate crossMailTemplate) {
        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossMailTemplate.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("sendNormalMail not in this server, roleId: ", crossMailTemplate.getRoleId());
            return;
        }
        if (CheckNull.isNull(crossMailTemplate)) {
            LogUtil.error("GameMailService sendNormalMail mail is null");
            return;
        }

        Java8Utils.syncMethodInvoke(() -> {
            Player tmpPlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(crossMailTemplate.getRoleId());
            if (CheckNull.isNull(tmpPlayer)) {
                LogUtil.error("sendNormalMail not in this server, roleId: ", crossMailTemplate.getRoleId());
                return;
            }

            DataResource.getBean(MailDataManager.class).sendCrossNormalMail(tmpPlayer, crossMailTemplate.getMoldId(), crossMailTemplate.getTime(), crossMailTemplate.getParam());
        });
    }

    @Override
    public void sendReportMail(CrossReportMail reportMail) {
        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(reportMail.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("sendReportMail not in this server, roleId: ", reportMail.getRoleId());
            return;
        }
        if (CheckNull.isNull(reportMail)) {
            LogUtil.error("GameMailService sendReportMail mail is null");
            return;
        }


        CommonPb.Report.Builder report = null;
        List<CommonPb.Award> mailDropList = null;
        Map<Long, List<CommonPb.Award>> mailRecoverList = null;
        try {
            int dropListSize = CheckNull.isEmpty(reportMail.getDrop()) ? 0 : reportMail.getDrop().size();
            if (dropListSize > 0) {
                mailDropList = new ArrayList<>(dropListSize);
                for (byte[] object : reportMail.getDrop()) {
                    mailDropList.add(CommonPb.Award.parseFrom(object));
                }
            }

            int recoverListSize = CheckNull.isEmpty(reportMail.getRecover()) ? 0 : reportMail.getRecover().size();
            if (recoverListSize > 0) {
                mailRecoverList = new HashMap<>();
                for (byte[] object : reportMail.getRecover()) {
                    mailRecoverList.computeIfAbsent(reportMail.getRoleId(), award -> new ArrayList<>()).add(CommonPb.Award.parseFrom(object));
                }
            }

            if (!ObjectUtils.isEmpty(reportMail.getReport())) {
                report = CommonPb.Report.parseFrom(reportMail.getReport()).toBuilder();
            }
        } catch (Exception e) {
            LogUtil.error("parseFrom sendReportMail wrong, e: ", e);
            return;
        }

        CommonPb.Report.Builder finalReport = report;
        List<CommonPb.Award> finalMailDropList = mailDropList;
        Map<Long, List<CommonPb.Award>> finalMailRecoverList = mailRecoverList;
        Java8Utils.syncMethodInvoke(() -> {
            Player tmpPlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(reportMail.getRoleId());
            if (CheckNull.isNull(tmpPlayer)) {
                LogUtil.error("sendNormalMail not in this server, roleId: ", reportMail.getRoleId());
                return;
            }

            DataResource.getBean(MailDataManager.class).sendCrossReportMail(tmpPlayer, finalReport, reportMail.getMoldId(), finalMailDropList, reportMail.getTime(),
                    finalMailRecoverList, reportMail.getParam());
        });
    }

    @Override
    public void sendCollectMail(CrossCollectMail crossCollectMail) {
        Player player = DataResource.getBean(PlayerDataManager.class).getPlayer(crossCollectMail.getRoleId());
        if (CheckNull.isNull(player)) {
            LogUtil.error("sendCollectMail not in this server, roleId: ", crossCollectMail.getRoleId());
            return;
        }
        if (CheckNull.isNull(crossCollectMail)) {
            LogUtil.error("GameMailService sendCollectMail mail is null");
            return;
        }

        CommonPb.Report.Builder report = null;
        CommonPb.MailCollect mailCollectPb = null;
        Map<Long, List<CommonPb.Award>> mailRecoverList = null;
        try {
            if (!ObjectUtils.isEmpty(crossCollectMail.getCollect()))
                mailCollectPb = CommonPb.MailCollect.parseFrom(crossCollectMail.getCollect());
            if (!ObjectUtils.isEmpty(crossCollectMail.getReport())) {
                report = CommonPb.Report.parseFrom(crossCollectMail.getReport()).toBuilder();
            }
            int recoverListSize = CheckNull.isEmpty(crossCollectMail.getRecover()) ? 0 : crossCollectMail.getRecover().size();
            if (recoverListSize > 0) {
                mailRecoverList = new HashMap<>();
                for (byte[] object : crossCollectMail.getRecover()) {
                    mailRecoverList.computeIfAbsent(crossCollectMail.getRoleId(), award ->
                            new ArrayList<>()).add(CommonPb.Award.parseFrom(object));
                }
            }
        } catch (Exception e) {
            LogUtil.error("GameMailService sendCollectMail parseFrom wrong, e: ", e);
        }

        CommonPb.Report.Builder finalReport = report;
        CommonPb.MailCollect finalMailCollectPb = mailCollectPb;
        Map<Long, List<CommonPb.Award>> finalMailRecoverList = mailRecoverList;
        Java8Utils.syncMethodInvoke(() -> {
            Player tmpPlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(crossCollectMail.getRoleId());
            if (CheckNull.isNull(tmpPlayer)) {
                LogUtil.error("sendCollectMail not in this server, roleId: ", crossCollectMail.getRoleId());
                return;
            }

            DataResource.getBean(MailDataManager.class).sendCrossCollectMail(tmpPlayer, finalReport, crossCollectMail.getMoldId(), finalMailCollectPb, crossCollectMail.getTime(),
                    finalMailRecoverList, crossCollectMail.getParam());
        });
    }

    @Override
    public void sendScoutMail(CrossScoutMail crossScoutMail) {
        Player pla = DataResource.getBean(PlayerDataManager.class).getPlayer(crossScoutMail.getRoleId());
        if (CheckNull.isNull(pla)) {
            LogUtil.error("GameMailService sendScoutMail player is null");
            return;
        }

        Java8Utils.syncMethodInvoke(() -> {
            Player tmp = DataResource.getBean(PlayerDataManager.class).getPlayer(crossScoutMail.getRoleId());
            if (CheckNull.isNull(tmp)) {
                LogUtil.error("GameMailService sendScoutMail player is null");
                return;
            }

            CommonPb.MailScout mailScout = null;
            if (Objects.nonNull(crossScoutMail.getScout())) {
                mailScout = CommonPb.MailScout.parseFrom(crossScoutMail.getScout());
            }

            DataResource.getBean(MailDataManager.class).sendCrossScoutMail(tmp,
                    crossScoutMail.getMoldId(), mailScout, crossScoutMail.getTime(), crossScoutMail.getParam());
        });
    }
}
