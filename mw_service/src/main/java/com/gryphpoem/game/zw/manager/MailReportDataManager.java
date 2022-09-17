package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.aspect.ClientThreadMode;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.dao.impl.p.MailReportDao;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.server.SaveMailReportServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 17:27
 */
@Component
public class MailReportDataManager implements DelayInvokeEnvironment {
    @Autowired
    private MailReportDao mailReportDao;

    /** 战报临时存储 存在MAIN线程中执行, 删除在*/
    private ConcurrentHashMap<Long, ConcurrentHashMap<Integer, CommonPb.Report>> reportMap = new ConcurrentHashMap<>();
    /** 需要删除的战报延迟队列 所有操作放在background线程队列中执行*/
    private DelayQueue<DbMailReport> expireReportQueue = new DelayQueue<>(this);

    /**
     * 添加战报
     *
     * @param roleId
     * @param mailKeyId
     * @param report
     */
    @ClientThreadMode
    public void addReport(long roleId, int mailKeyId, CommonPb.Report report) {
        reportMap.computeIfAbsent(roleId, m -> new ConcurrentHashMap<>()).computeIfAbsent(mailKeyId, r -> report);
        SaveMailReportServer.getIns().saveData(new DbMailReport(roleId, mailKeyId, report.toByteArray()));
    }

    /**
     * 添加即将删除的战报进入延迟队列
     *
     * @param dbMailReport
     */
    @ClientThreadMode(threadMode = DealType.BACKGROUND)
    public void addRemoveDelayQueue(DbMailReport dbMailReport) {
        if (CheckNull.isNull(dbMailReport)) return;
        this.expireReportQueue.add(dbMailReport);
    }

    /**
     * 移除战报
     *
     * @param removeList
     */
    @ClientThreadMode
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
    @ClientThreadMode
    public void removeOneReport(long roleId, int mailKeyId) {
        ConcurrentHashMap<Integer, CommonPb.Report> reportMap_ = reportMap.get(roleId);
        if (CheckNull.isNull(reportMap_)) return;
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
        if (CheckNull.isNull(reportMap_)) return null;
        return reportMap_.get(mailKeyId);
    }


    @Override
    public DelayQueue getDelayQueue() {
        return expireReportQueue;
    }
}
