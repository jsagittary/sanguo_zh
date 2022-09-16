package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.server.SaveMailReportServer;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 17:27
 */
@Component
public class MailReportDataManager {
    /** 战报临时存储*/
    private ConcurrentHashMap<Long, ConcurrentHashMap<Integer, CommonPb.Report>> reportMap = new ConcurrentHashMap<>();

    /**
     * 添加战报
     *
     * @param roleId
     * @param mailKeyId
     * @param report
     */
    public void addReport(long roleId, int mailKeyId, CommonPb.Report report) {
        reportMap.computeIfAbsent(roleId, m -> new ConcurrentHashMap<>()).computeIfAbsent(mailKeyId, r -> report);
        SaveMailReportServer.getIns().saveData(new DbMailReport(roleId, mailKeyId, report.toByteArray()));
    }

    /**
     * 移除战报
     *
     * @param removeList
     */
    public void removeReportList(LinkedList<Turple<Long, Integer>> removeList) {
        if (CheckNull.isEmpty(removeList)) return;
        removeList.forEach(remove -> {
            removeOneReport(remove.getA(), remove.getB());
        });
    }

    /**
     * 移除一个战报
     *
     * @param roleId
     * @param mailKeyId
     */
    public void removeOneReport(long roleId, int mailKeyId) {
        ConcurrentHashMap<Integer, CommonPb.Report> reportMap_ = reportMap.get(roleId);
        if (CheckNull.isEmpty(reportMap_)) return;
        reportMap_.remove(mailKeyId);
    }

    /**
     * 获取战报
     *
     * @param roleId
     * @param mailKeyId
     * @return
     */
    public CommonPb.Report getReport(long roleId, int mailKeyId) {
        ConcurrentHashMap<Integer, CommonPb.Report> reportMap_ = reportMap.get(roleId);
        if (CheckNull.isEmpty(reportMap_)) return null;
        return reportMap_.get(mailKeyId);
    }
}
