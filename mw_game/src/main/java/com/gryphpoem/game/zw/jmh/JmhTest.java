package com.gryphpoem.game.zw.jmh;

import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.bandit.BanditService;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-29 10:56
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 5, time = 1)
@State(Scope.Benchmark)
public class JmhTest {

    private AppGameServer appGameServer;

    private BanditService banditService;

    StringBuilder sb;
    StringBuffer sbu;
    @Param({"2", "10", "100", "1000"})
    private int count;

    @Setup
    public void init() throws InterruptedException {
//        appGameServer = AppGameServer.getInstance();
//        new Thread(appGameServer).start();
//        banditService = appGameServer.ac.getBean(BanditService.class);
    }

    @TearDown
    public void close() {
//        appGameServer.stop();
    }

    @Benchmark
    public void jmhTestSb() throws InterruptedException {
        sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("cat_");
        }
    }

    @Benchmark
    public void jmhTestSbu() throws InterruptedException {
        sbu = new StringBuffer();
        for (int i = 0; i < count; i++) {
            sbu.append("dog_");
        }
    }
}
