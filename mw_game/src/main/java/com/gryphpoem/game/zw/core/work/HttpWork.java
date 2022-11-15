package com.gryphpoem.game.zw.core.work;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.net.HttpServer;
import com.gryphpoem.game.zw.core.util.HttpUtils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.MessageUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;

public class HttpWork extends AbstractWork {
    private Base msg;
    private HttpServer httpServer;
    private String accountServerUrl;

    @Override
    public void run() {
        try {
            byte[] result = HttpUtils.sendPbByte(this.accountServerUrl, msg.toByteArray());
            if (result == null) {
                LogUtil.error("发送账号服数据失败[" + this.accountServerUrl + "]msg-->" + msg);
                return;
            }
            if (result.length <= 0) {
                LogUtil.error("请求账号服获取的数据为空[" + this.accountServerUrl + "]msg-->" + msg);
                return;
            }
            int len = MessageUtil.getInt(result);
            byte[] data = new byte[len];
            System.arraycopy(result, 4, data, 0, len);

            Base rs = Base.parseFrom(data, DataResource.getRegistry());
            httpServer.doPublicCommand(rs);

        } catch (Exception e) {
            LogUtil.error("HttpWork send to url exception", e);
        }
    }

    /**
     * @param msg
     */
    public HttpWork(HttpServer httpServer, Base msg, String accountServerUrl) {
        super();
        this.msg = msg;
        this.httpServer = httpServer;
        this.accountServerUrl = accountServerUrl;
    }

}
