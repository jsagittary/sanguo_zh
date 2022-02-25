package com.gryphpoem.game.zw.server;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.gryphpoem.game.zw.constant.MergeConstant;
import com.gryphpoem.game.zw.datasource.DynamicDataSource;
import com.gryphpoem.game.zw.domain.MasterServer;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @ClassName SavePlayerWork.java
 * @Description 保存玩家
 * @author QiuKun
 * @date 2018年9月18日
 */
public class SavePlayerWork extends BaseMergeWork {

    private ConcurrentLinkedQueue<Player> queue;
    // 自己服务的id
    private final MasterServer masterServer;

    private final int num;

    public SavePlayerWork(ConcurrentLinkedQueue<Player> queue, MasterServer masterServer, int num) {
        this.queue = queue;
        this.masterServer = masterServer;
        this.num = num;
    }

    @Override
    protected String threadName() {
        return "savePlayerWork-" + num + "-serverid:" + masterServer.getServerId();
    }

    @Override
    protected void work() {
        int masterServerId = masterServer.getServerId();
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getDstDatasourceKey(masterServerId));
        PlayerMergeService pMergeService = MergeServer.ac.getBean(PlayerMergeService.class);
        Player player = null;
        while ((player = queue.poll()) != null) {
            pMergeService.saveOnePlayer(player, masterServerId);
        }
    }

    @Override
    int serverId() {
        return masterServer.getServerId();
    }

}
