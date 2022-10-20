package com.gryphpoem.game.zw.rpc;

import com.gryphpoem.cross.activity.CrossRechargeActivityService;
import com.gryphpoem.cross.activity.dto.ActivityRankDto;
import com.gryphpoem.cross.player.RpcPlayerService;
import com.gryphpoem.cross.player.dto.PlayerLordDto;
import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.executor.NonOrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GmCmdConst;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DtoParser;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 通用rpc 调用处理
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-08-05 20:30
 */
@Service
public class DubboRpcService implements GmCmdService {
    @DubboReference(check = false, lazy = true, cluster = "failfast",
                    methods = {
                        @Method(name = "asyncUpdatePlayerLord", async = true, isReturn = false)
                    })
    private RpcPlayerService rpcPlayerService;
    @DubboReference(check = false, lazy = true, cluster = "failfast")
    private CrossRechargeActivityService crossRechargeActivityService;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 更新玩家数据到跨服服务器
     *
     * @param player
     */
    public void updatePlayerLord2CrossPlayerServer(Player player) {
        try {
            rpcPlayerService.asyncUpdatePlayerLord(DtoParser.toPlayerLordDto(player, serverSetting.getServerID()));
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    @GmCmd(GmCmdConst.crossStressTesting)
    public void handleGmCmd(Player player, String... params) {
        //每50毫秒将50个玩家记录发送给服务器, 1秒发送总量 = (1000/period) * periodSendCount;
        final String providerMethod = params[0];
        final int period = Integer.parseInt(params[1]);//定时发送周期
        final int periodSendCount = Integer.parseInt(params[2]);//每次发送的数量
        final long completedTaskCount = Long.parseLong(params[3]);//累计完成数量
        crossRechargeActivityService.startStressTesting(providerMethod, period, periodSendCount, completedTaskCount);
        LogUtil.common("开始采用集群压测接口 :{}", providerMethod);
    }

}
