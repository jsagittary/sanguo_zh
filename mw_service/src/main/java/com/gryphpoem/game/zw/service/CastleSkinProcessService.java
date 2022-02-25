package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by pengshuo on 2019/6/14 16:57
 * <br>Description: 处理限时皮肤
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Component
public class CastleSkinProcessService {

    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;


    /** 移除玩家皮肤 */
//    private void removePlayerTimerCastleSkin(Player player){
//        if(player == null || player.getOwnCastleSkinTime().isEmpty()){
//            return;
//        }
//        int now = TimeHelper.getCurrentSecond();
//        // 需要被移除的限时皮肤
//        Set<Map.Entry<Integer, Integer>> es = player.getOwnCastleSkinTime().entrySet().stream()
//                        .filter(e -> e.getValue() <= now)
//                        .collect(Collectors.toSet());
//        es.forEach(e -> {
//            removePlayerCastleSkin(player, e.getKey());
//        });
//    }

    /**
     * 移除玩家皮肤
     * @param player
     * @param skinId
     */
//    public void removePlayerCastleSkin(Player player, int skinId) {
//        // 当前皮肤id
//        int curSkinId = player.getCurCastleSkin();
//        // 移除玩家皮肤
//        player.getOwnCastleSkinTime().remove(skinId);
//        player.getOwnCastleSkin().remove(skinId);
//        LogUtil.common("移除玩家皮肤 roleId: ",player.lord," current skinId: ",curSkinId," remove skinId: ", skinId);
//        if(curSkinId == skinId){
//            // 移除穿戴皮肤，恢复默认
//            player.setCurCastleSkin(StaticCastleSkin.DEFAULT_SKIN_ID);
//        }
//        // 重新计算战力
//        CalculateUtil.reCalcAllHeroAttr(player);
//        if(player.isLogin){
//            // 登录 推送皮肤更换
//            pushPlayerCastleSkinChange(player);
//        }
//    }


    /** 延时队列 */
//    class RemoveCastleSkinDelayRun implements DelayRun ,DelayInvokeEnvironment{
//
//        protected final Player player;
//
//        protected final int executeTime;
//
//        protected final DelayQueue<RemoveCastleSkinDelayRun> delayQueue;
//
//        public  RemoveCastleSkinDelayRun(Player player,int executeTime){
//            this.player = player;
//            this.executeTime = executeTime;
//            this.delayQueue = new DelayQueue<>(this);
//        }
//
//        @Override
//        public int deadlineTime() {
//            return this.executeTime;
//        }
//
//        @Override
//        public DelayQueue getDelayQueue() {
//            return this.delayQueue;
//        }
//
//        /** 执行移除 */
//        @Override
//        public void deadRun(int runTime, DelayInvokeEnvironment env) {
//            if (env instanceof RemoveCastleSkinDelayRun) {
//                RemoveCastleSkinDelayRun delayRun = (RemoveCastleSkinDelayRun) env;
//                LogUtil.common("执行移除 玩家限时礼包 roleId: ",delayRun.player.roleId," executeTime: ",delayRun.executeTime);
//                // 移除 皮肤
//                removePlayerTimerCastleSkin(delayRun.player);
//            }
//        }
//    }
}


