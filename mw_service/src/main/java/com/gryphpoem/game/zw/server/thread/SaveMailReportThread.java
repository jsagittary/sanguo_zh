package com.gryphpoem.game.zw.server.thread;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.dao.impl.p.FightRecordDao;
import com.gryphpoem.game.zw.resource.domain.p.DbFightRecord;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description: 战报插入与删除
 * Author: zhangpeng
 * createTime: 2022-09-15 17:15
 */
public class SaveMailReportThread extends SaveThread {
    /** 保存queue*/
    private LinkedBlockingQueue<DbFightRecord> saveQueue = new LinkedBlockingQueue<>();
    /** 删除queue*/
    private LinkedBlockingQueue<DbFightRecord> removeQueue = new LinkedBlockingQueue<>();
    private static int MAX_SIZE = 20000;

    public SaveMailReportThread(String threadName) {
        super(threadName);
    }

    @Override
    public void run() {
        stop = false;
        done = false;
        while (!stop || saveQueue.size() > 0 || removeQueue.size() > 0) {
            DbFightRecord saveReport = null;
            DbFightRecord removeReport = null;
            synchronized (this) {
                Object o = saveQueue.poll();
                if (o != null) {
                    saveReport = (DbFightRecord) o;
                }

                o = removeQueue.poll();
                if (o != null) {
                    removeReport = (DbFightRecord) o;
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
                FightRecordDao dao = DataResource.ac.getBean(FightRecordDao.class);
                if (saveQueue.size() > MAX_SIZE) {
                    saveQueue.clear();
                }

                if (saveReport != null) {
                    try {
                        dao.replaceFightRecord(saveReport);
                        if (logFlag) {
                            saveCount++;
                        }
                    } catch (Exception e) {
                        LogUtil.error("Role Exception:" + saveReport, e);
                        LogUtil.warn("MailReport save Exception:" + saveReport);
                        LogUtil.common(String.format("停服保存玩家战报数据失败, keyId:%d" , saveReport.getKeyId()));
                        this.add(saveReport);
                    }
                }

                if (removeQueue.size() > MAX_SIZE) {
                    removeQueue.clear();
                }
                if (removeReport != null) {
                    try {
                        dao.deleteFightRecord(removeReport);
                        if (logFlag) {
                            saveCount++;
                        }
                    } catch (Exception e) {
                        LogUtil.error("Role Exception:" + removeReport, e);
                        LogUtil.warn("MailReport save Exception:" + removeReport);
                        LogUtil.common(String.format("停服删除玩家战报数据失败, keyId:%d" , removeReport.getKeyId()));
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
            DbFightRecord dbFightRecord = (DbFightRecord) object;
            synchronized (this) {
                this.saveQueue.add(dbFightRecord);
                LogUtil.save("保存DbMailReport事件插入, keyId: "+ dbFightRecord.getKeyId());
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            DbFightRecord dbFightRecord = (DbFightRecord) obj;
            synchronized (this) {
                this.removeQueue.add(dbFightRecord);
                LogUtil.save("删除DbMailReport事件, keyId: " + dbFightRecord.getKeyId());
                this.saveQueue.remove(obj);
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }
}
