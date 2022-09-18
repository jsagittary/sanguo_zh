package com.gryphpoem.game.zw.server.thread;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.dao.impl.p.MailReportDao;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description: 战报插入与删除
 * Author: zhangpeng
 * createTime: 2022-09-15 17:15
 */
public class SaveMailReportThread extends SaveThread {
    /** 保存queue*/
    private LinkedBlockingQueue<DbMailReport> saveQueue = new LinkedBlockingQueue<>();
    /** 删除queue*/
    private LinkedBlockingQueue<DbMailReport> removeQueue = new LinkedBlockingQueue<>();
    private static int MAX_SIZE = 20000;

    public SaveMailReportThread(String threadName) {
        super(threadName);
    }

    @Override
    public void run() {
        stop = false;
        done = false;
        while (!stop || saveQueue.size() > 0 || removeQueue.size() > 0) {
            DbMailReport saveReport = null;
            DbMailReport removeReport = null;
            synchronized (this) {
                Object o = saveQueue.poll();
                if (o != null) {
                    saveReport = (DbMailReport) o;
                }

                o = removeQueue.poll();
                if (o != null) {
                    removeReport = (DbMailReport) o;
                }
            }
            if (saveReport == null && removeReport == null) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    LogUtil.error(threadName + " Wait Exception:" + e.getMessage(), e);
                }
            } else {
                MailReportDao dao = DataResource.ac.getBean(MailReportDao.class);
                if (saveQueue.size() > MAX_SIZE) {
                    saveQueue.clear();
                }

                if (saveReport != null) {
                    try {
                        dao.save(saveReport);
                        if (logFlag) {
                            saveCount++;
                        }
                    } catch (Exception e) {
                        LogUtil.error("Role Exception:" + saveReport, e);
                        LogUtil.warn("MailReport save Exception:" + saveReport);
                        LogUtil.common(String.format("停服保存玩家战报数据失败, role:%d, keyId:%d" + saveReport.getLordId(), saveReport.getKeyId()));
                        this.add(saveReport);
                    }
                }

                if (removeQueue.size() > MAX_SIZE) {
                    removeQueue.clear();
                }
                if (removeReport != null) {
                    try {
                        dao.deleteMailReport(removeReport);
                        if (logFlag) {
                            saveCount++;
                        }
                    } catch (Exception e) {
                        LogUtil.error("Role Exception:" + removeReport, e);
                        LogUtil.warn("MailReport save Exception:" + removeReport);
                        LogUtil.common(String.format("停服删除玩家战报数据失败, role:%d, keyId:%d" + removeReport.getLordId(), removeReport.getKeyId()));
                        this.remove(removeReport);
                    }
                }
            }
        }

        done = true;
    }

    @Override
    public void add(Object object) {
        try {
            DbMailReport dbMailReport = (DbMailReport) object;
            synchronized (this) {
                this.saveQueue.add(dbMailReport);
                LogUtil.save("保存DbMailReport事件插入， roleId:" + dbMailReport.getLordId() + ", keyId: " + dbMailReport.getKeyId());
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            DbMailReport dbMailReport = (DbMailReport) obj;
            synchronized (this) {
                this.removeQueue.add(dbMailReport);
                LogUtil.save("删除DbMailReport事件， roleId:" + dbMailReport.getLordId() + ", keyId: " + dbMailReport.getKeyId());
                this.saveQueue.remove(obj);
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }
}
