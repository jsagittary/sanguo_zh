package com.gryphpoem.game.zw.server.timer;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.timer.TimerEvent;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.*;

/**
 * @ClassName PushTimer.java
 * @Description 推送相关定时器
 * @author QiuKun
 * @date 2018年4月17日
 */
public class PushMsgTimer extends TimerEvent {

    public PushMsgTimer() {
        super(-1, 60000); // 1分钟检测一次
    }

    @Override
    public void action() throws MwException {
        // 科技升级定时任务
        AppGameServer.ac.getBean(TechService.class).techUpTimerLogic();
        // 造兵队列推送
        AppGameServer.ac.getBean(FactoryService.class).armyQueTimer();
        // 化工厂队列推送
        AppGameServer.ac.getBean(ChemicalService.class).chemicalQueTimer();
        // 装备洗髓满值推送
        AppGameServer.ac.getBean(EquipService.class).washEquipTimer();
        // 聚宝盆CD满值推送
        AppGameServer.ac.getBean(TreasureService.class).treasureCdTimer();
        // 特工亲密度满时进行推送
        // AppGameServer.ac.getBean(CiaService.class).interactionCntTimer();
        // 离线时间超出24小时推送
        AppGameServer.ac.getBean(PlayerService.class).offLineTimeTimer();
        // 爱丽丝到达进行推送
        AppGameServer.ac.getBean(PlayerService.class).aliceArriveTimer();
    }

}
