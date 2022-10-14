package com.gryphpoem.game.zw.resource.pojo.relic;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

/**
 * @author xwind
 * @date 2022/8/2
 */
public class RelicEntity extends BaseWorldEntity {
    private int area;
    private int block;
    //配置ID
    private int cfgId;
    //阵营占领时长
    private Map<Integer, CampRankData> campRankDataMap = new HashMap<>();
    //当前归属阵营
    private int holdCamp;
    //开始占领时间戳
    private int startHold;
    private int fightId;
    //当前的防御部队. KEY:roleId, VALUE:部队ID
    private LinkedList<Turple<Long, Integer>> defendList = new LinkedList<>();
    // 当前队伍占领记录时间戳 <roleId, <armyKeyId, 时间戳>>
    private HashMap<Long, HashMap<Integer, Long>> holdArmyTime = new HashMap<>();

    public RelicEntity() {
        super();
    }

    public RelicEntity(int cfgId, int pos, WorldEntityType type, int area, int block) {
        super(pos, type);
        this.area = area;
        this.block = block;
        this.cfgId = cfgId;

        this.init();
    }

    public void updCampHoldValue(int now) {
        if (holdCamp > 0 && startHold > 0) {
            CampRankData campRankData = campRankDataMap.get(holdCamp);
            campRankData.value += now - startHold;
        }
    }

    public void init() {
        for (int camp : Constant.Camp.camps) {
            campRankDataMap.put(camp, new CampRankData(camp, 0, 0, 0));
        }
    }

    @Override
    public void attackPos(AttackParamDto param) throws MwException {

    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getArea() {
        return area;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public SerializePb.SerRelicEntity ser() {
        SerializePb.SerRelicEntity.Builder builder = SerializePb.SerRelicEntity.newBuilder();
        builder.setPos(super.getPos());
        builder.setType(super.getType().getType());
        builder.setArea(this.area);
        builder.setBlock(this.block);
        builder.setCfgId(this.cfgId);
        builder.setHoldCamp(this.holdCamp);
        builder.setStartHold(this.startHold);
        builder.setFightId(this.fightId);
        this.campRankDataMap.values().forEach(o -> builder.addCampRankInfo(o.ser()));
        this.defendList.forEach(turple -> builder.addDefends(PbHelper.createLongIntPb(turple.getA(), turple.getB())));
        if (CheckNull.nonEmpty(this.holdArmyTime)) {
            SerializePb.SerRelicHoldArmy.Builder armyPb = SerializePb.SerRelicHoldArmy.newBuilder();
            this.holdArmyTime.entrySet().forEach(en -> {
                armyPb.setRoleId(en.getKey());
                if (CheckNull.nonEmpty(en.getValue())) {
                    en.getValue().entrySet().forEach(en_ -> {
                        armyPb.addHoldTime(PbHelper.createIntLongPc(en_.getKey(), en_.getValue()));
                    });
                }
                builder.addArmy(armyPb.build());
                armyPb.clear();
            });
        }
        return builder.build();
    }

    public void dser(SerializePb.SerRelicEntity serRelicEntity) {
        super.setPos(serRelicEntity.getPos());
        super.setType(WorldEntityType.getWorldEntityTypeByTypeId(serRelicEntity.getType()));
        this.setArea(serRelicEntity.getArea());
        this.setBlock(serRelicEntity.getBlock());
        this.cfgId = serRelicEntity.getCfgId();
        this.holdCamp = serRelicEntity.getHoldCamp();
        this.startHold = serRelicEntity.getStartHold();
        this.fightId = serRelicEntity.getFightId();
        if (serRelicEntity.getCampRankInfoCount() > 0) {
            serRelicEntity.getCampRankInfoList().forEach(o -> {
                CampRankData campRankData = new CampRankData();
                campRankData.dser(o);
                this.campRankDataMap.put(o.getCamp(), campRankData);
            });
        }
        if (serRelicEntity.getDefendsCount() > 0) {
            serRelicEntity.getDefendsList().forEach(o -> this.defendList.add(new Turple<>(o.getV1(), o.getV2())));
        }
        if (serRelicEntity.getArmyCount() > 0) {
            serRelicEntity.getArmyList().forEach(army -> {
                if (army.getHoldTimeCount() > 0) {
                    army.getHoldTimeList().forEach(holdArmyTime -> {
                        this.holdArmyTime.computeIfAbsent(army.getRoleId(), m -> new HashMap<>()).
                                computeIfAbsent(holdArmyTime.getV1(), l -> holdArmyTime.getV2());
                    });
                }
            });
        }
    }

    public Map<Integer, CampRankData> getCampRankDataMap() {
        return campRankDataMap;
    }

    public int getHoldCamp() {
        return holdCamp;
    }

    public void setHoldCamp(int holdCamp) {
        this.holdCamp = holdCamp;
    }

    @Override
    public int getCfgId() {
        return cfgId;
    }

    public LinkedList<Turple<Long, Integer>> getDefendList() {
        return defendList;
    }

    public boolean joinDefendList(Turple<Long, Integer> tpl) {
        return defendList.offerFirst(tpl);
    }

    public boolean isHavingProbe(long roleId) {
        return Objects.nonNull(defendList.stream().filter(o -> o.getA().equals(roleId)).findFirst().orElse(null));
    }

    public int incrAndGetFightId() {
        return fightId++;
    }

    public int getStartHold() {
        return startHold;
    }

    public void setStartHold(int stamp) {
        this.startHold = stamp;
    }

    public void setStartHold0(int stamp) {
        if (holdCamp > 0 && startHold > 0) {
            CampRankData campRankData = campRankDataMap.get(holdCamp);
            campRankData.value += TimeHelper.getCurrentSecond() - startHold;
        }
        this.setStartHold(stamp);
    }

    public void setCfgId(int cfgId) {
        this.cfgId = cfgId;
    }

    public void setCampRankDataMap(Map<Integer, CampRankData> campRankDataMap) {
        this.campRankDataMap = campRankDataMap;
    }

    public int getFightId() {
        return fightId;
    }

    public void setFightId(int fightId) {
        this.fightId = fightId;
    }

    public void setDefendList(LinkedList<Turple<Long, Integer>> defendList) {
        this.defendList = defendList;
    }

    public void joinHoldArmy(long roleId, int armyKeyId, long nowMills) {
        this.holdArmyTime.computeIfAbsent(roleId, m -> new HashMap<>()).putIfAbsent(armyKeyId, nowMills);
    }

    public void clearHold() {
        this.holdArmyTime.clear();
    }

    public long holdTime(long roleId, int armyKeyId) {
        HashMap<Integer, Long> map;
        if ((map = this.holdArmyTime.get(roleId)) != null) {
            return map.getOrDefault(armyKeyId, 0l);
        }
        return 0l;
    }
}
