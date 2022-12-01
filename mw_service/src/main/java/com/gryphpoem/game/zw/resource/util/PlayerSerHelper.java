package com.gryphpoem.game.zw.resource.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb.SerMail;
import com.gryphpoem.game.zw.pb.SerializePb.SerReport;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.MailData;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.MailReportMap;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName PlayerMailSerHelper.java
 * @Description 玩家序列化 与反序列化帮助类
 * @date 2019年4月18日
 */
public class PlayerSerHelper {

    public static MailData serMailData(Player player) {
        MailData mailData = new MailData();
        mailData.setLordId(player.lord.getLordId());
        Map<Integer, Mail> mails = player.mails;

        Map<Integer, SerMail.Builder> tempSerMailMap = new HashMap<>(16);
        for (int i = 0; i <= MailData.MASK; i++) {
            tempSerMailMap.put(i, SerMail.newBuilder());
        }
        SerReport.Builder reportsBuilder = SerReport.newBuilder();
        for (Mail mail : mails.values()) {
            int idx = mail.getKeyId() & MailData.MASK;
            tempSerMailMap.get(idx).addMail(PbHelper.saveMailPb(mail, player));
            MailReportMap mailReportMap = player.getMailReportMap(mail.getMoldId());
            if (Objects.nonNull(mailReportMap)) {
                CommonPb.Report report = mailReportMap.getReport(mail.getKeyId());
                if (Objects.nonNull(report))
                    reportsBuilder.addReport(report);
            }
        }

        mailData.setMails(tempSerMailMap.get(0).build().toByteArray());
        mailData.setMails1(tempSerMailMap.get(1).build().toByteArray());
        mailData.setMails2(tempSerMailMap.get(2).build().toByteArray());
        mailData.setMails3(tempSerMailMap.get(3).build().toByteArray());
        mailData.setMails4(tempSerMailMap.get(4).build().toByteArray());
        mailData.setMails5(tempSerMailMap.get(5).build().toByteArray());
        mailData.setMails6(tempSerMailMap.get(6).build().toByteArray());
        mailData.setMails7(tempSerMailMap.get(7).build().toByteArray());
        mailData.setMails8(tempSerMailMap.get(8).build().toByteArray());
        mailData.setMails9(tempSerMailMap.get(9).build().toByteArray());
        mailData.setMails10(tempSerMailMap.get(10).build().toByteArray());
        mailData.setMails11(tempSerMailMap.get(11).build().toByteArray());
        mailData.setMails12(tempSerMailMap.get(12).build().toByteArray());
        mailData.setMails13(tempSerMailMap.get(13).build().toByteArray());
        mailData.setMails14(tempSerMailMap.get(14).build().toByteArray());
        mailData.setMails15(tempSerMailMap.get(15).build().toByteArray());
        mailData.setReports(reportsBuilder.build().toByteArray());
        return mailData;
    }

    /**
     * 反序列化玩家邮件数据
     *
     * @param player
     * @param data
     * @throws InvalidProtocolBufferException
     */
    public static void dserMailData(Player player, MailData data) throws InvalidProtocolBufferException {
        Map<Integer, Mail> mails = player.mails;
        mails.clear();
        dserMails(data.getMails(), mails);
        dserMails(data.getMails1(), mails);
        dserMails(data.getMails2(), mails);
        dserMails(data.getMails3(), mails);
        dserMails(data.getMails4(), mails);
        dserMails(data.getMails5(), mails);
        dserMails(data.getMails6(), mails);
        dserMails(data.getMails7(), mails);
        dserMails(data.getMails8(), mails);
        dserMails(data.getMails9(), mails);
        dserMails(data.getMails10(), mails);
        dserMails(data.getMails11(), mails);
        dserMails(data.getMails12(), mails);
        dserMails(data.getMails13(), mails);
        dserMails(data.getMails14(), mails);
        dserMails(data.getMails15(), mails);

        if (data.getReports() != null) {
            Map<Integer, MailReportMap> mailReportMapMap = player.mailReport;
            mailReportMapMap.clear();
            SerReport ser = SerReport.parseFrom(data.getReports());
            dserReports(mails, player, ser);
        }
    }

    private static void dserMails(byte[] mailsByteData, Map<Integer, Mail> mails)
            throws InvalidProtocolBufferException {
        if (mailsByteData == null) return;
        SerMail ser = SerMail.parseFrom(mailsByteData);
        for (CommonPb.Mail mail : ser.getMailList()) {
            mails.put(mail.getKeyId(), new Mail(mail));
        }
    }

    private static void dserReports(Map<Integer, Mail> mails, Player player, SerReport ser) {
        if (ObjectUtils.isEmpty(ser.getReportList())) {
            return;
        }

        List<CommonPb.Report> list = ser.getReportList().stream().sorted(Comparator.
                comparingInt(CommonPb.Report::getTime)).collect(Collectors.toList());
        for (CommonPb.Report report : list) {
            Mail mail = mails.get(report.getKeyId());
            if (CheckNull.isNull(mail))
                continue;

            player.addReport(mail.getMoldId(), mail.getKeyId(), report, mails);
        }
    }
}
