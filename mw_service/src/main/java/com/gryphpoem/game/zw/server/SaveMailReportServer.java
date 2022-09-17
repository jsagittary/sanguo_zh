package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.SaveServer;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import com.gryphpoem.game.zw.server.thread.SaveMailReportThread;

/**
 * Description: 保存战报服务
 * Author: zhangpeng
 * createTime: 2022-09-15 15:15
 */
public class SaveMailReportServer extends SaveServer {

    private static final SaveMailReportServer INSTANCE = new SaveMailReportServer();

    private SaveMailReportServer() {
        super("SAVE_MAIL_REPORT", 10);
    }

    public static SaveMailReportServer getIns() {
        return INSTANCE;
    }

    @Override
    public void saveData(Object object) {
        DbMailReport dbMailReport = (DbMailReport) object;
        SaveThread thread = threadPool.get((int) (dbMailReport.getSerializeIdId() % threadNum));
        thread.add(object);
    }

    @Override
    public void removeData(Object obj) {
        DbMailReport dbMailReport = (DbMailReport) obj;
        SaveThread thread = threadPool.get((int) (dbMailReport.getSerializeIdId() % threadNum));
        thread.remove(obj);
    }

    @Override
    public SaveThread createThread(String name) {
        return new SaveMailReportThread(name);
    }

    public void stopServer() {
        try {
            LogUtil.stop("开始保存战报数据...");
            long startMillis = System.currentTimeMillis();
            setLogFlag();
            stop();
            while (!saveDone()) {
                LogUtil.stop(String.format("处理战报数据中, 等待3s, 已耗时=%s", System.currentTimeMillis() - startMillis));
                Thread.sleep(3000);
            }
            LogUtil.stop(String.format("保存战报数据完成, 共处理: %s, 共耗时=%s", allSaveCount(), System.currentTimeMillis() - startMillis));
        } catch (Exception e) {
            LogUtil.error("停服保存战报数据发生错误", e);
        }
    }
}
