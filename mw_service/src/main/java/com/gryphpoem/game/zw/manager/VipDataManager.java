package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticVip;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VipDataManager {
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private DressUpDataManager dressUpDataManager;

    /**
     * 根据类型找到值(-1表示异常)
     *
     * @param vip
     * @param type
     * @return
     */
    public int getNum(int vip, int type, Object... objects) {
        StaticVip staticVip = StaticVipDataMgr.getVipMap(vip);
        if (CheckNull.isNull(staticVip)) {
            return -1;
        }
        switch (type) {
            case VipConstant.BUY_ACT:
                return staticVip.getBuyAct();
            case VipConstant.FACTORY_RECRUIT:
                return staticVip.getFactoryRecruit();
            case VipConstant.FREE_BUILD_TIME:
                Player player = (Player) objects[0];
                return (int) (staticVip.getFreeBuildTime() * (1 + (DataResource.getBean(SeasonTalentService.class).
                        getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_613) / Constant.TEN_THROUSAND)));
            case VipConstant.RETREAT:
                return staticVip.getRetreat();
            case VipConstant.WIPE:
                return staticVip.getWipe();
            case VipConstant.RESOURCE_GAIN:
                return staticVip.getProductionGain();
            case VipConstant.EQUIP_FORGE:
                return staticVip.getSpeedForgeTime();
        }
        return -1;
    }

    /**
     * 是否开启VIP功能
     *
     * @param vip
     * @param type
     * @return
     */
    public boolean isOpen(int vip, int type) {
        return getNum(vip, type) > 0;
    }

    public void setVip(Player player,int vip){
        player.lord.setVip(vip);
        dressUpDataManager.checkVipChatBubble(player);
    }

    /**
     * 返回最新VIP等级
     *
     * @param player
     * @return
     */
    public int processVip(Player player) {
        int oldVip = player.lord.getVip();
        int vip = StaticVipDataMgr.calcVip(player.lord.getVipExp());
        if (vip > player.lord.getVip()) {
//            player.lord.setVip(vip);
            setVip(player,vip);
            if (vip > 0) {
                // chatService.sendWorldChat(chatService.createSysChat(SysChatId.BECOME_VIP, lord.getNick(), "" + vip));
            }
            if (vip == 5) {
                int now = TimeHelper.getCurrentSecond();
                int endTime = now + (4 * TimeHelper.HOUR_S);
                if (endTime > now) {
                    player.setMixtureData(PlayerConstant.FB_END_TIME, endTime);
                    playerDataManager.syncMixtureData(player);
                }
            }
            // upVip = true;
            processGlobalVipCnt(vip, oldVip);
            activityDataManager.syncAllPlayerActChange(player, ActivityConst.ACT_VIP);// 精英部队tip通知
        }
        return player.lord.getVip();
    }

    /**
     * 统计全服VIP个数
     *
     * @param vip
     */
    private void processGlobalVipCnt(int vip, int oldVip) {
        if (globalDataManager.getGameGlobal().getTrophy() == null) {
            return;
        }

        Map<Integer, Integer> vipMap = globalDataManager.getGameGlobal().getTrophy().getVipCnt();
        Integer v = vipMap.get(vip);
        v = v != null ? v : 0;
        vipMap.put(vip, v + 1);

        Integer oldCnt = vipMap.get(oldVip);
        oldCnt = oldCnt != null ? oldCnt : 1;
        oldCnt--;
        oldCnt = oldCnt <= 0 ? 0 : oldCnt;// 不让减少到0
        vipMap.put(oldVip, oldCnt);
    }

    /**
     * 某个VIP人数+1
     *
     * @param vip
     */
    public void incrementVipCnt(int vip) {
        Map<Integer, Integer> vipMap = globalDataManager.getGameGlobal().getTrophy().getVipCnt();
        Integer v = vipMap.get(vip);
        v = v != null ? v : 0;
        vipMap.put(vip, v + 1);
    }

    public int getCollectMineCount(int vip) {
        StaticVip staticVip = StaticVipDataMgr.getVipMap(vip);
        if (staticVip == null) {
            return 0;
        }
        return staticVip.getKillFriend();
    }

}
