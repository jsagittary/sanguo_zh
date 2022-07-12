package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-07-11 16:50
 */
public class CombatInfo implements GamePb<CommonPb.CombatInfoPb> {
    private Date updateDate;
    private Map<Integer, Map<Integer, Integer>> countMap = new HashMap<>();
    public Map<Integer, Map<Integer, Integer>> getCountMap() {
        return countMap;
    }

    public CombatInfo() {
    }

    public int getCount(int awardType, int id) {
        resetDaily();
        Map<Integer, Integer> awardCountMap = countMap.get(awardType);
        if (CheckNull.isEmpty(awardCountMap))
            return 0;
        return awardCountMap.getOrDefault(id, 0);
    }

    public void updateCount(int awardType, int id, int count) {
        resetDaily();
        Map<Integer, Integer> awardCountMap = countMap.get(awardType);
        if (CheckNull.isNull(awardCountMap)) {
            awardCountMap = new HashMap<>();
            countMap.put(awardType, awardCountMap);
        }
        awardCountMap.merge(id, count, Integer::sum);
        if (CheckNull.isNull(updateDate))
            updateDate = new Date();
    }

    public void resetDaily() {
        if (CheckNull.isNull(updateDate)) return;
        if (!DateHelper.isToday(updateDate)) {
            this.countMap.clear();
            this.updateDate = new Date();
        }
    }

    public void dseCombatInfoPb(CommonPb.CombatInfoPb pb) {
        if (CheckNull.isNull(pb))
            return;

        if (pb.hasUpdateDate()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(pb.getUpdateDate());
            this.updateDate = c.getTime();
        }
        if (CheckNull.nonEmpty(pb.getDropList())) {
            pb.getDropList().forEach(award -> {
                Map<Integer, Integer> awardCountMap = countMap.get(award.getType());
                if (CheckNull.isNull(awardCountMap)) {
                    awardCountMap = new HashMap<>();
                    countMap.put(award.getType(), awardCountMap);
                }
                awardCountMap.merge(award.getId(), award.getCount(), Integer::sum);
            });
        }
        resetDaily();
    }

    @Override
    public CommonPb.CombatInfoPb createPb(boolean isSaveDb) {
        CommonPb.CombatInfoPb.Builder builder = CommonPb.CombatInfoPb.newBuilder();
        if (Objects.nonNull(updateDate)) {
            builder.setUpdateDate(updateDate.getTime());
        }
        if (CheckNull.nonEmpty(countMap)) {
            countMap.entrySet().forEach(entry -> {
                if (CheckNull.nonEmpty(entry.getValue())) {
                    entry.getValue().forEach((id, count) -> {
                        builder.addDrop(PbHelper.createAwardPb(entry.getKey(), id, count));
                    });
                }
            });
        }
        return builder.build();
    }
}
