package com.gryphpoem.game.zw.gameplay.local.world.dominate;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.Turple;

import java.util.*;

/**
 * Description: 雄踞一方城池
 * Author: zhangpeng
 * createTime: 2022-11-22 17:27
 */
public class DominateSideCity extends City {
    //当前归属阵营
    private int holdCamp;
    //开始占领时间戳
    private int startHold;
    // 活动是否结束
    private boolean isOver;
    // 担任过都督列表
    private LinkedList<DominateSideGovernor> governorList;
    // 阵营占领记录
    private Map<Integer, CampRankData> campRankDataMap = new HashMap<>();
    //当前的防御部队
    private LinkedList<Turple<Long, Integer>> defendList = new LinkedList<>();
    // 当前队伍占领记录时间戳 <roleId, <armyKeyId, 时间戳>>
    private HashMap<Long, HashMap<Integer, Long>> holdArmyTime = new HashMap<>();

    public DominateSideCity() {
    }

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

    public void setHoldCamp(int holdCamp) {
        this.holdCamp = holdCamp;
    }

    public void setStartHold(int startHold) {
        this.startHold = startHold;
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

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }
}
