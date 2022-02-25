package com.gryphpoem.game.zw.handler.client.common;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.ClientHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1.DoSomeRq;
import com.gryphpoem.game.zw.pb.GamePb1.DoSomeRs;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.resource.common.ServerConfig;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.GmService;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 执行GM指令
 * @author TanDonghai
 */
public class DoSomeHandler extends ClientHandler {

    @Override
    public void action() throws MwException {
        ServerConfig serverConfig = AppGameServer.ac.getBean(ServerConfig.class);
        //GM开关
        if (serverConfig != null && serverConfig.getGmFlag() != null && "true".equals(serverConfig.getGmFlag())) {
            DoSomeRq req = msg.getExtension(DoSomeRq.ext);
            if(req.getStr().equals("test insert")){
                this.test0();
            }else {
                boolean success = getService(GmService.class).doSome(req, getRoleId());

                DoSomeRs.Builder builder = DoSomeRs.newBuilder();
                builder.setSuccess(success);
                sendMsgToPlayer(DoSomeRs.ext, builder.build());
            }
        }
    }

    private void test0() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(100,100,0, TimeUnit.MILLISECONDS,workQueue);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(()-> LogUtil.error("[test insert] -> workQueue: " + workQueue.size()),5,5, TimeUnit.SECONDS);
        int now = TimeHelper.getCurrentSecond();
        AtomicInteger n = new AtomicInteger();
        for(int i=0;i<30000;i++){
            int idx = i ;
            threadPoolExecutor.execute(() -> {
                try {
                    CommonPb.AccountRoleInfo.Builder ab = CommonPb.AccountRoleInfo.newBuilder();
                    ab.setAccountKey(2000000000 + idx);
                    ab.setCamp(1);
                    ab.setDateRoleCreate(now);
                    ab.setLevel(100);
                    ab.setRoleId(200000000000L+ idx);
                    ab.setRoleName("testinsert" + idx);
                    ab.setServerId(idx+1);
                    ab.setFight(5000);
                    ab.setVip(0);
                    ab.setCommandLv(1);

                    // 推送角色信息给账户服
                    HttpPb.SendAccountRoleRq.Builder push = HttpPb.SendAccountRoleRq.newBuilder();
                    push.addArs(ab);
                    BasePb.Base.Builder baseBuilder = PbHelper.createRqBase(HttpPb.SendAccountRoleRq.EXT_FIELD_NUMBER, null,
                            HttpPb.SendAccountRoleRq.ext, push.build());
                    AppGameServer.getInstance().sendMsgToPublic(baseBuilder);
                }catch (Exception e) {
                    LogUtil.error("",e);
                }
            });
            n.incrementAndGet();
            if(n.get() >= 1000){
                n.set(0);
                try {
                    Thread.sleep(40000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
