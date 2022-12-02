package com.gryphpoem.game.zw.gameplay.local.world.dominate;

import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description: 雄踞一方城池
 * Author: zhangpeng
 * createTime: 2022-11-22 17:27
 */
public class DominateSideCity extends City implements GamePb<SerializePb.SerDominateSideCity> {
    //当前归属阵营
    private int holdCamp;
    //开始占领时间戳
    private int startHold;
    // 活动是否结束
    private boolean isOver;
    // 担任过都督列表
    private LinkedList<DominateSideGovernor> governorList;
    // 阵营占领记录   CampRankData.value(占领时间)  CampRankData.data 影响力值
    private Map<Integer, CampRankData> campRankDataMap = new HashMap<>();
    //当前的防御部队
    private LinkedList<Turple<Long, Integer>> defendList = new LinkedList<>();
    // 当前队伍占领记录时间戳 <roleId, <armyKeyId, 时间戳>>
    private HashMap<Long, HashMap<Integer, Long>> holdArmyTime = new HashMap<>();

    public DominateSideCity(City city) {
        setCityId(city.getCityId());
        setCamp(city.getCamp());
        setStatus(city.getStatus());
        setCloseTime(city.getCloseTime());
        setAttackCamp(city.getAttackCamp());
        setProtectTime(city.getProtectTime());
        setCityLv(city.getCityLv());
        setOwnerId(city.getOwnerId());
        setBeginTime(city.getBeginTime());
        setEndTime(city.getEndTime());
        setProduced(city.getProduced());
        setFinishTime(city.getFinishTime());
        setCampaignTime(city.getCampaignTime());
        getCampaignList().addAll(city.getCampaignList());
        setExtraReward(city.getExtraReward());
        this.setFormList(city.getFormList());
        setName(city.getName());
        setExp(city.getExp());
        setAtkBeginTime(city.getAtkBeginTime());
        setNextDevTime(city.getNextDevTime());
        getAttackRoleId().addAll(city.getAttackRoleId());
        getFirstKillReward().putAll(city.getFirstKillReward());
        setBuildingExp(city.getBuildingExp());
        setLeaveOver(city.getLeaveOver());

        init();
    }

    public int getHoldCamp() {
        return holdCamp;
    }

    public int getStartHold() {
        return startHold;
    }

    public Map<Integer, CampRankData> getCampRankDataMap() {
        return campRankDataMap;
    }

    public LinkedList<DominateSideGovernor> getGovernorList() {
        return governorList;
    }

    public LinkedList<Turple<Long, Integer>> getDefendList() {
        return defendList;
    }

    public HashMap<Long, HashMap<Integer, Long>> getHoldArmyTime() {
        return holdArmyTime;
    }

    public void setHoldCamp(int holdCamp, int now) {
        this.holdCamp = holdCamp;
        CampRankData campRankData = campRankDataMap.get(this.holdCamp);
        campRankData.time = now;
    }

    public void setHoldCamp(int holdCamp) {
        this.holdCamp = holdCamp;
    }

    public void setStartHold(int startHold) {
        this.startHold = startHold;
    }

    public void init() {
        for (int camp : Constant.Camp.camps) {
            campRankDataMap.put(camp, new CampRankData(camp, 0, 0, 0));
        }
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }

    public long holdTime(long roleId, int armyKeyId) {
        HashMap<Integer, Long> map;
        if ((map = this.holdArmyTime.get(roleId)) != null) {
            return map.getOrDefault(armyKeyId, 0l);
        }
        return 0l;
    }

    public void settleCampOccupyTime(int now) {
        if (this.holdCamp > 0 && this.startHold > 0) {
            CampRankData campRankData = this.campRankDataMap.get(holdCamp);
            if (CheckNull.isNull(campRankData)) return;

            int occupyTime = now - startHold;
            if (occupyTime > 0) {
                campRankData.value += occupyTime;
            }
        }
    }

    public int getCampOccupyTime(int now, int camp, List<Integer> config) {
        CampRankData campRankData = this.campRankDataMap.get(camp);
        int hisInfluence = campRankData.data;
        if (camp == this.holdCamp && CheckNull.nonEmpty(config)) {
            // 当前战令的时间
            int occupyTime = now - startHold;
            if (occupyTime > 0) {
                // 本次占领时间 / 15秒 = 增加的势力值 * 1000 (这里记录万分比)
                int addInfluence = (occupyTime / config.get(0)) * config.get(1);
                // 添加势力值
                hisInfluence += addInfluence;
            }
        }
        return hisInfluence;
    }

    public boolean joinDefendList(Turple<Long, Integer> tpl) {
        return defendList.offerFirst(tpl);
    }

    public void joinHoldArmy(long roleId, int armyKeyId, long nowMills) {
        this.holdArmyTime.computeIfAbsent(roleId, m -> new HashMap<>()).putIfAbsent(armyKeyId, nowMills);
    }

    /**
     * 变更阵营持有者
     *
     * @param atkCamp
     * @param now
     */
    public void changeCampHolder(int atkCamp, int now, List<Integer> config) {
        this.holdArmyTime.clear();
        if (this.getCamp() != Constant.Camp.NPC && this.startHold > 0 && CheckNull.nonEmpty(config)) {
            // 历史占领的时间
            CampRankData preCampRankData = this.campRankDataMap.get(this.getCamp());
            // 本次占领的时间
            int occupyTime = this.startHold > 0 && now > this.startHold ? now - this.startHold : 0;
            if (occupyTime > 0) {
                // 本次占领时间 / 15秒 = 增加的势力值 * 1000 (这里记录万分比)
                int addInfluence = (occupyTime / config.get(0)) * config.get(1);
                // 历史的势力值
                preCampRankData.data += addInfluence;
            }

            preCampRankData.value += occupyTime;

            // 扣除前占领阵营的势力值
            int influence = preCampRankData.data;
            if (influence > 0) {
                influence = (int) (influence * (1 - (config.get(2) / Constant.TEN_THROUSAND)));
                // 更新扣除后的势力值
                preCampRankData.data = influence;
            }
        }

        setHoldCamp(atkCamp, now);
        setStartHold(now);
    }

    public long removeHolder(long roleId, int armyKeyId) {
        HashMap<Integer, Long> map;
        if ((map = this.holdArmyTime.get(roleId)) != null) {
            return map.remove(armyKeyId);
        }
        return 0l;
    }

    public void reset() {
        this.setOver(false);
        this.setCamp(Constant.Camp.NPC);
        this.holdCamp = Constant.Camp.NPC;
        this.startHold = 0;
        this.campRankDataMap.clear();
        this.defendList.clear();
        this.holdArmyTime.clear();
        this.setOwnerId(0l);
    }

    public DominateSideCity(SerializePb.SerDominateSideCity city) {
        super(city.getCity());
        setOver(city.getIsOver());
        setHoldCamp(city.getHoldCamp());
        setStartHold(city.getStartHold());
        this.campRankDataMap = new HashMap<>();
        if (CheckNull.nonEmpty(city.getRankInfoList())) {
            city.getRankInfoList().forEach(campRankInfo -> {
                CampRankData campRankData = new CampRankData();
                campRankData.dser(campRankInfo);
                campRankDataMap.put(campRankInfo.getCamp(), campRankData);
            });
        }
        this.governorList = new LinkedList<>();
        if (CheckNull.nonEmpty(city.getGovernorList())) {
            city.getGovernorList().forEach(pb -> {
                this.governorList.addFirst(new DominateSideGovernor(pb));
            });
        }
        this.defendList = new LinkedList<>();
        if (CheckNull.nonEmpty(city.getDefendList())) {
            city.getDefendList().forEach(de -> {
                this.defendList.add(new Turple<>(de.getV1(), de.getV2()));
            });
        }
        this.holdArmyTime = new HashMap<>();
        city.getArmyList().forEach(army -> {
            if (army.getHoldTimeCount() > 0) {
                army.getHoldTimeList().forEach(holdArmyTime -> {
                    this.holdArmyTime.computeIfAbsent(army.getRoleId(), m -> new HashMap<>()).
                            computeIfAbsent(holdArmyTime.getV1(), l -> holdArmyTime.getV2());
                });
            }
        });
    }

    @Override
    public SerializePb.SerDominateSideCity createPb(boolean isSaveDb) {
        SerializePb.SerDominateSideCity.Builder builder = SerializePb.SerDominateSideCity.newBuilder();
        builder.setCity(PbHelper.createCityPb(this));
        builder.setHoldCamp(this.holdCamp);
        builder.setStartHold(this.startHold);
        builder.setIsOver(this.isOver);
        builder.addAllRankInfo(this.campRankDataMap.values().stream().map(CampRankData::ser).collect(Collectors.toList()));
        if (CheckNull.nonEmpty(this.governorList)) {
            builder.addAllGovernor(this.governorList.stream().map(g -> g.createPb(true)).collect(Collectors.toList()));
        }
        if (CheckNull.nonEmpty(this.defendList)) {
            builder.addAllDefend(this.defendList.stream().map(t -> PbHelper.createLongIntPb(t.getA(), t.getB())).collect(Collectors.toList()));
        }
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
}
