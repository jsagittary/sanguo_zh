package com.hundredcent.game.client;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.dao.impl.p.LordDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.SmallIdDao;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.SmallId;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author xwind
 * @date 2021/7/29
 */

@SpringBootTest
public class MultipleClient {
    @Test
    public void test() {
        String serverIp = "127.0.0.1";
        int serverPort = 9201;

        LordDao lordDao = DataResource.getBean(LordDao.class);
        List<Lord> lordList = lordDao.load();
        SmallIdDao smallIdDao = DataResource.getBean(SmallIdDao.class);
        List<SmallId> smallIdList = smallIdDao.load();
        Map<Integer, Integer> countMap = new HashMap<>();
        lordList.forEach(lord -> {
            SmallId smallId = smallIdList.stream().filter(o -> o.getLordId()==lord.getLordId()).findFirst().orElse(null);
            if(smallId == null){
                int count = countMap.getOrDefault(lord.getCamp(),0);
                if(count < 10){
//                ClientThread thread = new ClientThread(new TestClient(serverIp,serverPort),lord.getLordId());
//                thread.start();
                    countMap.put(lord.getCamp(),count+1);
                    TestClient testClient = new TestClient(serverIp,serverPort,lord.getLordId(), DataResource.ac);
                    Thread thread = new Thread(testClient);
                    thread.start();
                }
            }
        });
        ClientLogger.print(">>>>>>>>>>>>>>>>>>" + JSON.toJSONString(countMap));
    }
    public static void main(String[] args) {
        //47.114.190.46:9201
        String serverIp = "172.16.13.101";
        int serverPort = 9201;

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml");
        LordDao lordDao = applicationContext.getBean(LordDao.class);
        List<Lord> lordList = lordDao.load();
        SmallIdDao smallIdDao = applicationContext.getBean(SmallIdDao.class);
        List<SmallId> smallIdList = smallIdDao.load();
        Map<Integer, Integer> countMap = new HashMap<>();
        lordList.forEach(lord -> {
            SmallId smallId = smallIdList.stream().filter(o -> o.getLordId()==lord.getLordId()).findFirst().orElse(null);
            if(smallId == null){
                int count = countMap.getOrDefault(lord.getCamp(),0);
                if(count < 100){
//                ClientThread thread = new ClientThread(new TestClient(serverIp,serverPort),lord.getLordId());
//                thread.start();
                    countMap.put(lord.getCamp(),count+1);
                    TestClient testClient = new TestClient(serverIp,serverPort,lord.getLordId(), applicationContext);
                    Thread thread = new Thread(testClient);
                    thread.start();
                }
            }
        });
        ClientLogger.print(">>>>>>>>>>>>>>>>>>" + JSON.toJSONString(countMap));
//        TestClient testClient = new TestClient(serverIp, serverPort, 100011001412L);
//        Thread thread = new Thread(testClient);
//        thread.start();
    }
}

//class ClientThread extends Thread {
//    TestClient testClient;
//    long roleId;
//
//    ClientThread(TestClient testClient, long roleId) {
//        this.testClient = testClient;
//        this.roleId = roleId;
//    }
//
//    long lastMillis = 0;
//
//    @Override
//    public void run() {
//        Thread t = null;
//        try {
//            t = new Thread(testClient);
//            t.start();
//            while (!testClient.connected) {
//                sleep(1000);
////                ClientLogger.print("连接服务器..." + roleId);
//            }
//            ClientLogger.print("已连接上服务器 -> " + roleId);
//            testClient.BeginGameHandler(roleId);
//            Thread.sleep(RandomUtils.nextInt(500, 1001));
//            while (true) {
//                if (System.currentTimeMillis() - lastMillis >= 1000) {
//                    testClient.GetTimeHandler();
//                    lastMillis = System.currentTimeMillis();
//                }
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            t.interrupt();
//        }
//    }
//}

class TestClient extends BaseClient {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public TestClient(String ip, int port, long roleId,ApplicationContext applicationContext) {
        super(ip, port, roleId, applicationContext);
        scheduledExecutorService.scheduleWithFixedDelay(()->{
            if (super.connected && super.isBeginGame) {
                GetTimeHandler();
            }
        },1000,30,TimeUnit.SECONDS);
    }

    protected void BeginGameHandler(long roleId) {
        GamePb1.BeginGameRq.Builder builder = GamePb1.BeginGameRq.newBuilder();
        builder.setKeyId(roleId);
        builder.setServerId(1);
        builder.setToken("");
        builder.setDeviceNo("");
        builder.setCurVersion("");
        BasePb.Base.Builder baseBuilder = BasePb.Base.newBuilder();
        baseBuilder.setCmd(GamePb1.BeginGameRq.EXT_FIELD_NUMBER);
        baseBuilder.setExtension(GamePb1.BeginGameRq.ext, builder.build());
        sendMsgToServer(baseBuilder);

        GamePb1.BeginGameRs rs = null;
        BasePb.Base rs_ = getMessage(GamePb1.BeginGameRs.EXT_FIELD_NUMBER, 5000);
        if (null != rs_ && rs_.getCmd() == GamePb1.BeginGameRs.EXT_FIELD_NUMBER && rs_.getCode() == GameError.OK.getCode()) {
            rs = rs_.getExtension(GamePb1.BeginGameRs.ext);
        }
        System.out.println("rs:" + rs);
        if (null == rs) {
            ClientLogger.print("开始游戏失败，退出...");
            return;
        }

        ClientLogger.print(roleId + " -> 开始了游戏");

    }

//    @Override
    protected void GetTimeHandler() {
        GamePb1.GetTimeRq.Builder req = GamePb1.GetTimeRq.newBuilder();
        BasePb.Base.Builder reqBase = PbHelper.createRqBase(GamePb1.GetTimeRq.EXT_FIELD_NUMBER, null, GamePb1.GetTimeRq.ext, req.build());
        sendMsgToServer(reqBase);
    }
}
