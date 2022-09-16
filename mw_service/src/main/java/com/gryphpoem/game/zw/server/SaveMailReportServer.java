package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.core.SaveServer;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;
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
        DbParty dbParty = (DbParty) object;
        SaveThread thread = threadPool.get((dbParty.getCamp() % threadNum));
        thread.add(object);
    }

    @Override
    public void removeData(Object obj) {
        DbParty dbParty = (DbParty) obj;
        SaveThread thread = threadPool.get((dbParty.getCamp() % threadNum));
        thread.remove(obj);
    }

    @Override
    public SaveThread createThread(String name) {
        return new SaveMailReportThread(name);
    }
}
