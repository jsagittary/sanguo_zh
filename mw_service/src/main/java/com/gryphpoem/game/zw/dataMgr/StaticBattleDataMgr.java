package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePvp;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-04-26 16:14
 */
@Component
public class StaticBattleDataMgr extends AbsStaticIniService {

    private static Map<Integer, StaticBattlePvp> battlePvpMap;

    @Override
    public void load() {
        battlePvpMap = staticIniDao.selectStaticBattlePvpMap();
    }

    /**
     * 获取战斗公式参数
     *
     * @param diff
     * @return
     */
    public static StaticBattlePvp getBattlePvp(int diff) {
        Integer minDiff = battlePvpMap.keySet().stream().min(Comparator.comparingInt(Integer::intValue)).orElse(null);
        if (CheckNull.isNull(minDiff))
            return null;
        if (diff <= minDiff) {
            return battlePvpMap.get(minDiff);
        }

        Integer maxDiff = battlePvpMap.keySet().stream().max(Comparator.comparingInt(Integer::intValue)).orElse(null);
        if (CheckNull.isNull(maxDiff))
            return null;
        if (diff >= maxDiff) {
            battlePvpMap.get(maxDiff);
        }

        return battlePvpMap.get(diff);
    }

    @Override
    public void check() {
        if (CheckNull.isEmpty(battlePvpMap)) {
            LogUtil.error("battlePvpMap is empty");
        }
    }
}
