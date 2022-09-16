//package com.gryphpoem.game.zw.resource.pojo;
//
//import com.gryphpoem.game.zw.pb.CommonPb;
//import com.gryphpoem.game.zw.resource.constant.Constant;
//import com.gryphpoem.game.zw.resource.constant.MailConstant;
//import com.gryphpoem.game.zw.resource.util.CheckNull;
//import org.springframework.util.ObjectUtils;
//import java.util.*;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//public class MailReportMap {
//
//    private Map<Integer, CommonPb.Report> reportMap;
//
//    private LinkedList<Integer> headReport;
//
//    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
//
//    public MailReportMap() {
//        this.reportMap = new HashMap<>();
//        this.headReport = new LinkedList<>();
//    }
//
//    public void addReport(CommonPb.Report report, Map<Integer, Mail> mails) {
//        if (CheckNull.isNull(report))
//            return;
//
//        lock.writeLock().lock();
//        try {
//            this.reportMap.put(report.getKeyId(), report);
//            if (!GameGlobal.closeExpiredReport) {
//                headReport.offer(report.getKeyId());
//                if (headReport.size() > Constant.MAIL_MAX_SAVE_COUNT) {
//                    Integer removeKeyId = headReport.removeFirst();
//                    handleExpiredReport(removeKeyId, mails.get(removeKeyId));
//                }
//            }
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    public CommonPb.Report getReport(int keyId) {
//        lock.readLock().lock();
//        try {
//            return reportMap.get(keyId);
//        } finally {
//            lock.readLock().unlock();
//        }
//    }
//
//    /**
//     * 过期邮件
//     * @param keyIds
//     */
//    public void expiredMail(List<Integer> keyIds) {
//        if (ObjectUtils.isEmpty(keyIds)) {
//            return;
//        }
//
//        lock.writeLock().lock();
//        try {
//            keyIds.forEach(keyId -> {
//                this.reportMap.remove(keyId);
//            });
//
//            headReport.removeAll(keyIds);
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    /**
//     * 过期战报
//     * @param keyIds
//     */
//    public void expiredReport(List<Integer> keyIds, Map<Integer, Mail> mails) {
//        if (ObjectUtils.isEmpty(keyIds) || GameGlobal.closeExpiredReport) {
//            return;
//        }
//
//        lock.writeLock().lock();
//        try {
//            keyIds.forEach(keyId -> {
//                handleExpiredReport(keyId, mails.get(keyId));
//            });
//
//        } finally {
//            lock.writeLock().unlock();
//        }
//    }
//
//    /**
//     * 处理过期战报信息
//     * @param removeReportKeyId
//     * @param mail
//     */
//    private void handleExpiredReport(int removeReportKeyId, Mail mail) {
//        CommonPb.Report builder = this.reportMap.remove(removeReportKeyId);
//        if (CheckNull.isNull(builder)) {
//            return;
//        }
//
//        if (CheckNull.isNull(mail) || MailConstant.EXPIRED_REPORT == mail.getReportStatus()) {
//            return;
//        }
//
//        CommonPb.Report.Builder report = builder.toBuilder();
//        if (report.hasRptBandit()) {
//            CommonPb.RptAtkBandit.Builder rptBandit = report.getRptBandit().toBuilder();
//            if (!ObjectUtils.isEmpty(rptBandit.getRecord())) {
//                CommonPb.Record.Builder record = rptBandit.getRecord().toBuilder();
//                if (!ObjectUtils.isEmpty(record.getRoundList())) {
//                    record.clearRound();
//                }
//                if (!ObjectUtils.isEmpty(record.getAuraList())) {
//                    record.clearAura();
//                }
//
//                rptBandit.setRecord(record.build());
//                report.setRptBandit(rptBandit.build());
//
//                record = null;
//            }
//
//            rptBandit = null;
//        }
//
//        if (report.hasRptPlayer()) {
//            CommonPb.RptAtkPlayer.Builder rptAtkPlayer = report.getRptPlayer().toBuilder();
//            if (!ObjectUtils.isEmpty(rptAtkPlayer.getRecord())) {
//                CommonPb.Record.Builder record = rptAtkPlayer.getRecord().toBuilder();
//                if (!ObjectUtils.isEmpty(record.getRoundList())) {
//                    record.clearRound();
//                }
//                if (!ObjectUtils.isEmpty(record.getAuraList())) {
//                    record.clearAura();
//                }
//
//                rptAtkPlayer.setRecord(record.build());
//                report.setRptPlayer(rptAtkPlayer.build());
//
//                record = null;
//            }
//
//            rptAtkPlayer = null;
//        }
//
//        if (Objects.nonNull(mail)) {
//            mail.setReportStatus(MailConstant.EXPIRED_REPORT);
//        }
//
//        this.reportMap.put(removeReportKeyId, report.build());
//        builder = null;
//        report = null;
//    }
//}
