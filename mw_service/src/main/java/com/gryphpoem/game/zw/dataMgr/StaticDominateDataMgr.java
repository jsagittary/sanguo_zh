package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticBerlinWarAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticDominateWarAward;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 13:38
 */
public class StaticDominateDataMgr extends AbsStaticIniService {
    private static List<StaticDominateWarAward> dominateWarAwardList;


    @Override
    public void load() {
        dominateWarAwardList = staticDataDao.selectStaticDominateWarAwardList();
    }

    public static StaticDominateWarAward findKillRankAward(long rankVal) {
        if (CheckNull.isEmpty(dominateWarAwardList)) {
            return null;
        }
        return dominateWarAwardList.stream().filter(award -> rankVal >= award.getCond().get(0)).min((award1, award2) -> award2.getCond().get(0) - award1.getCond().get(0)).orElse(null);
    }

    public static List<StaticDominateWarAward> getDominateWarAwardByType() {
        if (CheckNull.isEmpty(dominateWarAwardList)) {
            return null;
        }
        GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (Objects.isNull(berlinWar)) {
            return null;
        }
        int scheduleId = berlinWar.getScheduleId();
        // 根据时间进程过滤军费配置
        return dominateWarAwardList.stream().filter(bwa -> scheduleId >= bwa.getSchedule().get(0) && scheduleId <= bwa.getSchedule().get(1)).collect(Collectors.toList());
    }

    @Override
    public void check() {

    }
}
