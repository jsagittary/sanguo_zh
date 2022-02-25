package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticNightRaid;

/**
 * @ClassName StaticNightRaidMgr.java
 * @Description 夜袭功能
 * @author QiuKun
 * @date 2018年3月1日
 */
public class StaticNightRaidMgr {
    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 夜袭配置
    private static StaticNightRaid nightRaid;

    public static void init() {
        StaticNightRaidMgr.nightRaid = staticDataDao.selectNightRaid().get(0);
    }

    public static StaticNightRaid getNightRaid() {
        return nightRaid;
    }

    /**
     * 获取夜袭兵力恢复效果
     * 
     * @param player
     * @param now
     * @param banditLv
     * @return
     */
    public static double getNightRaidRecArmyEffect(Player player, int now, int banditLv) {
        StaticNightRaid nightRaid = getNightRaid();
        if (nightRaid.hasByBanditLv(banditLv) && nightRaid.isInThisTime(now)
                && getNightRaidBanditCnt(player, now) < nightRaid.getBanditCount()) {
            return nightRaid.getRecoverArmy() / Constant.TEN_THROUSAND;
        }
        return 0.0;
    }

    /**
     * 获取夜袭流寇数量
     * 
     * @return
     */
    public static int getNightRaidBanditCnt(Player player, int now) {
        StaticNightRaid nightRaid = getNightRaid();
        if (!nightRaid.isInThisTime(now)) {
            player.setNightRaidBanditCnt(0);
        }
        return player.getNightRaidBanditCnt();
    }

    /**
     * 累加夜袭流寇数量
     */
    public static void incrNightRaidBandit(Player player, int now, int banditLv) {
        StaticNightRaid nightRaid = getNightRaid();
        if (nightRaid.hasByBanditLv(banditLv) && nightRaid.isInThisTime(now)) {
            int cnt = player.getNightRaidBanditCnt();
            cnt++;
            if (cnt >= nightRaid.getBanditCount()) {
                cnt = nightRaid.getBanditCount();
            }
            player.setNightRaidBanditCnt(cnt);
        }
    }
}
