package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.domain.s.StaticActTreasureWareJourney;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaticActTaskDataMgr extends AbsStaticIniService {

    private static Map<Integer, Map<Integer, StaticActTreasureWareJourney>> actTwJourneyMap;

    @Override
    public void load() {
        List<StaticActTreasureWareJourney> actTwJourneyList = staticIniDao.selectStaticActTreasureWareJourney();
        if (CheckNull.nonEmpty(actTwJourneyList)) {
            actTwJourneyMap = new HashMap<>();
            actTwJourneyList.forEach(data -> actTwJourneyMap.computeIfAbsent(data.getActivityId(), a -> new HashMap<>()).put(data.getKeyId(), data));
        }
    }

    public static Collection<StaticActTreasureWareJourney> getActTwJourneyList(int actId) {
        if (!actTwJourneyMap.containsKey(actId))
            return null;
        return actTwJourneyMap.get(actId).values();
    }

    public static StaticActTreasureWareJourney getActTwJourney(int actId, int taskKeyId) {
        if (!actTwJourneyMap.containsKey(actId))
            return null;
        return actTwJourneyMap.get(actId).get(taskKeyId);
    }

    @Override
    public void check() {
        if (CheckNull.isEmpty(actTwJourneyMap)) {
            LogUtil.error("actTwJourneyList is empty");
        }
    }
}
