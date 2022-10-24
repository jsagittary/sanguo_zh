package com.gryphpoem.game.zw.resource.pojo.relic;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 遗迹全局数据
 *
 * @author xwind
 * @date 2022/8/2
 */
public class GlobalRelic {
    //遗迹坐标列表
    private Map<Integer, RelicEntity> relicEntityMap = new HashMap<>();
    private Map<Integer, RelicEntity> relicEntityBackMap = new HashMap<>();
    //保护到期时间戳
    private int safeExpire;
    //结束时间戳
    private int overExpire;
    //遗迹战斗ID
    private long fightId;
    private boolean keep;
    // 当前活动世界进程
    private int curScheduleId;
    /**
     * 本次遗迹积分最大的玩家
     */
    private CommonPb.LongInt maxScoreRole;

    public void clear() {
        relicEntityMap.clear();
        relicEntityBackMap.clear();
        safeExpire = 0;
        overExpire = 0;
        fightId = 0;
        keep = false;
        maxScoreRole = null;
        curScheduleId = 0;
    }

    public int state() {
        int nowStamp = TimeHelper.getCurrentSecond();
        if (nowStamp <= safeExpire) {
            return RelicCons.SAFE;
        } else if (nowStamp <= overExpire) {
            return RelicCons.OPEN;
        } else return RelicCons.OVER;
    }

    public SerializePb.SerGlobalRelic ser() {
        SerializePb.SerGlobalRelic.Builder builder = SerializePb.SerGlobalRelic.newBuilder();
        if (relicEntityMap.isEmpty()) {
            relicEntityBackMap.values().forEach(o -> builder.addSerRelicEntity2(o.ser()));
        } else {
            relicEntityMap.values().forEach(o -> {
                SerializePb.SerRelicEntity entity = o.ser();
                builder.addSerRelicEntity(entity);
//                builder.addSerRelicEntity2(entity);
            });
        }
        builder.setSafeExpire(safeExpire);
        builder.setOverExpire(overExpire);
        builder.setKeep(keep);
        builder.setFightId(fightId);
        if (Objects.nonNull(this.maxScoreRole)) {
            builder.setMaxScoreRole(this.maxScoreRole);
        }
        builder.setCurScheduleId(this.curScheduleId);
        return builder.build();
    }

    public void dser(SerializePb.SerGlobalRelic serGlobalRelic) {
        if (serGlobalRelic.getSerRelicEntityCount() > 0) {
            serGlobalRelic.getSerRelicEntityList().forEach(o -> {
                RelicEntity relicEntity = new RelicEntity();
                relicEntity.dser(o);
                relicEntityMap.put(o.getPos(), relicEntity);
                relicEntityBackMap.put(o.getPos(), relicEntity);
            });
        } else {
            if (serGlobalRelic.getSerRelicEntity2Count() > 0) {
                serGlobalRelic.getSerRelicEntity2List().forEach(o -> {
                    RelicEntity relicEntity = new RelicEntity();
                    relicEntity.dser(o);
                    relicEntityBackMap.put(o.getPos(), relicEntity);
                });
            }
        }
        this.safeExpire = serGlobalRelic.getSafeExpire();
        this.overExpire = serGlobalRelic.getOverExpire();
        this.keep = serGlobalRelic.getKeep();
        this.fightId = serGlobalRelic.getFightId();
        this.curScheduleId = serGlobalRelic.getCurScheduleId();
        if (serGlobalRelic.hasMaxScoreRole()) {
            this.maxScoreRole = serGlobalRelic.getMaxScoreRole();
        }
    }

    public long incrAndGetFightId() {
        return fightId;
    }

    public Map<Integer, RelicEntity> getRelicEntityMap() {
        return relicEntityMap;
    }

    public int getSafeExpire() {
        return safeExpire;
    }

    public void setSafeExpire(int safeExpire) {
        this.safeExpire = safeExpire;
    }

    public int getOverExpire() {
        return overExpire;
    }

    public void setOverExpire(int overExpire) {
        this.overExpire = overExpire;
    }

    public boolean isKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    public Map<Integer, RelicEntity> getRelicEntityBackMap() {
        return relicEntityBackMap;
    }

    public long getFightId() {
        return fightId;
    }

    public void setFightId(long fightId) {
        this.fightId = fightId;
    }

    public CommonPb.LongInt getMaxScoreRole() {
        return maxScoreRole;
    }

    public void setMaxScoreRole(CommonPb.LongInt maxScoreRole) {
        this.maxScoreRole = maxScoreRole;
    }

    public int getCurScheduleId() {
        return curScheduleId;
    }

    public void setCurScheduleId(int curScheduleId) {
        this.curScheduleId = curScheduleId;
    }
}
