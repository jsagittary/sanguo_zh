package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.crosssimple.service.CrossDataService;
import com.gryphpoem.game.zw.manager.BattlePassDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xwind
 * @date 2021/5/12
 */
@Service
public class GameJobService {
    @Autowired
    private PlayerService playerService;
    @Autowired
    private RebelService rebelService;
    @Autowired
    private CounterAtkService counterAtkService;
    @Autowired
    private BerlinWarService berlinWarService;
    @Autowired
    private AirshipService airshipService;
    @Autowired
    private WorldScheduleService wiWorldScheduleService;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private CrossDataService crossDataService;
    @Autowired
    private ActivityTriggerService activityTriggerService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private GlobalDataManager globalDataManager;

    /**
     * 跨天0:0:1执行逻辑
     */
    public void execAcrossTheDayJob(){
        globalDataManager.getGameGlobal().dayJobRunning = true;
        Java8Utils.invokeNoExceptionICommand(() -> {
            //处理玩家数据
            playerService.acrossTheDayProcess();
        });

        Java8Utils.invokeNoExceptionICommand(() -> {
            // 处理匪军叛乱
            rebelService.initRebellion(false);
        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 处理反攻德意志
            counterAtkService.initCounterAtk();
        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 处理柏林会战
            berlinWarService.initBerlinJob();
        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 飞艇检测
            airshipService.onAcrossTheDayRun();
        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 世界进程检测结束
            wiWorldScheduleService.triggerCheckWorldScheduleEnd();
        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 世界进程检测结束
            crossWorldMapDataManager.onAcrossTheDayRun();
        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 跨服初始化
            crossDataService.initAndRefresh(false);
        });
//        Java8Utils.invokeNoExceptionICommand(() -> {
//            // 转点执行时间触发活动事件
//            activityTriggerService.checkTimeTriggerActivity();
//        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 转点执行时间触发清除事件
            battlePassDataManager.clearTaskAndData();
        });
        Java8Utils.invokeNoExceptionICommand(() -> {
            // 转点执行时间触发清除事件
            royalArenaService.clearTaskAndData();
        });
        globalDataManager.getGameGlobal().dayJobRun = TimeHelper.getCurrentDay();
        globalDataManager.getGameGlobal().dayJobRunning = false;
        LogUtil.error("quartz执行转点任务------执行完毕: " + globalDataManager.getGameGlobal().dayJobRun);
    }

    /**
     * Timer中检查，quartz默认的misfire=60s，所以当天若过了misfire但还没有执行job，则在此处执行一次
     * 注：job是丢到主线程队列中执行，会有延迟，因此需要将条件中的lostSecond设置的大一点
     *     Timer中一定会被执行到。通过dayJonRun和dayJobRunning控制，服务器启动时默认dayJobRun=当天，表示当天执行过
     */
    public void checkAcrossTheDayJob(){
//        GameGlobal gameGlobal = globalDataManager.getGameGlobal();
//        if(gameGlobal.dayJobRun != TimeHelper.getCurrentDay() && TimeHelper.getDayLostSecond() > 90 && !gameGlobal.dayJobRunning){
//            this.execAcrossTheDayJob();
//            System.out.println("########################################checkAcrossTheDayJob");
//        }
    }
}
