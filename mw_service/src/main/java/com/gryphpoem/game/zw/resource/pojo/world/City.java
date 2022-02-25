package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.CamppaignRole;
import com.gryphpoem.game.zw.pb.CommonPb.IntLong;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author TanDonghai
 * @ClassName City.java
 * @Description 记录城池信息
 * @date 创建时间：2017年3月22日 下午7:41:56
 */
public class City {

    private int cityId;
    private int cityLv;// 城池当前等级
    private int buildingExp;// 城池经验
    private int camp;// 所属阵营
    private int status;// 状态
    private int closeTime;// 状态结束时间
    private int attackCamp;// 记录当前进攻该城的阵营，没有则置为0
    private long ownerId;// 所属玩家id
    private int beginTime;// 玩家任期开始时间
    private int endTime;// 任期结束时间
    private int produced;// 已生成道具个数
    private int finishTime;// 本次生产完成时间
    private int campaignTime;// 竞选结束时间
    private List<CamppaignRole> campaignList = new CopyOnWriteArrayList<>();// 参与竞选玩家id
    private int extraReward;// 上次发送城主额外奖励的日期
    private List<CityHero> formList;// 城防军阵型
    private int atkBeginTime;// 下次自动攻城开始时间
    private String name = ""; // 城池的名称
    private int exp; // 人口值不是经验值
    private int nextDevTime; // 下次开发时间(已经废弃)
    private int protectTime;// 保护结束时间
    private Set<Long> attackRoleId = new HashSet<>(); // 记录参加攻下城池的玩家
    private Map<Long, Integer> firstKillReward = new HashMap<>();// 首杀奖励 key:roleId, value已经征收的次数
    private int leaveOver;//城主撤离结束时间戳

    public City() {
    }

    public City(CommonPb.City city) {
        setCityId(city.getCityId());
        setCamp(city.getCamp());
        setStatus(city.getStatus());
        setCloseTime(city.getCloseTime());
        setAttackCamp(city.getAttackCamp());
        setProtectTime(city.getProtectTime());
        if (city.hasCityLv()) {
            setCityLv(city.getCityLv());
        }
        if (city.hasOwnerId()) {
            setOwnerId(city.getOwnerId());
        }
        if (city.hasBeginTime()) {
            setBeginTime(city.getBeginTime());
        }
        if (city.hasEndTime()) {
            setEndTime(city.getEndTime());
        }
        if (city.hasProduced()) {
            setProduced(city.getProduced());
        }
        if (city.hasFinishTime()) {
            setFinishTime(city.getFinishTime());
        }
        if (city.hasCampaignTime()) {
            setCampaignTime(city.getCampaignTime());
        }
        campaignList.addAll(city.getRoleList());
        if (city.hasExtraReward()) {
            setExtraReward(city.getExtraReward());
        }
        formList = new ArrayList<>();
        for (TwoInt twoInt : city.getFormList()) {
            formList.add(new CityHero(twoInt.getV1(), twoInt.getV2()));
        }
        if (city.hasName()) {
            setName(city.getName());
        }
        setExp(city.getCityExp());
        setAtkBeginTime(city.getAtkBeginTime());
        setNextDevTime(city.getDevTime());
        if (!CheckNull.isEmpty(city.getAttackRoleIdList())) {
            for (Long roleId : city.getAttackRoleIdList()) {
                attackRoleId.add(roleId);
            }
        }
        if (!CheckNull.isEmpty(city.getFirstKillRewardList())) {
            for (IntLong il : city.getFirstKillRewardList()) {
                firstKillReward.put(il.getV2(), il.getV1());
            }
        }
        if (city.hasBuildingExp()) {
            setBuildingExp(city.getBuildingExp());
        }
        setLeaveOver(city.getLeaveOver());
    }

    /**
     * 获取npc守将阵型
     *
     * @param npcId
     * @return
     */
    public CityHero getCityHero(int npcId) {
        for (CityHero hero : formList) {
            if (hero.getNpcId() == npcId) {
                return hero;
            }
        }
        return null;
    }

    /**
     * 城池当前是否处于战斗状态
     *
     * @return
     */
    public boolean isInBattle() {
        return status == WorldConstant.CITY_STATUS_BATTLE;
    }

    /**
     * 城池当前是否属于NPC
     *
     * @return
     */
    public boolean isNpcCity() {
        return camp == Constant.Camp.NPC;
    }

    /**
     * 获取NPC守军兵力
     *
     * @return
     */
    public int getCurArm() {
        int curArm = 0;
        if (!CheckNull.isEmpty(formList)) {
            for (CityHero hero : formList) {
                curArm += hero.getCurArm();
            }
        }
        return curArm;
    }

    /**
     * 获取总兵力
     *
     * @return
     */
    public int getTotalArm() {
        int totalArm = 0;
        if (!CheckNull.isEmpty(formList)) {
            for (CityHero hero : formList) {
                int npcId = hero.getNpcId();
                StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
                if (npc != null) totalArm += npc.getTotalArm();
            }
        } else { // 空的就读配置form中的
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
            if (staticCity != null) {
                // 城池的阵型配置
                List<Integer> formList = staticCity.getFormList(isNpcCity());
                if (!CheckNull.isEmpty(formList)) {
                    for (Integer npcId : formList) {
                        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
                        if (npc != null) totalArm += npc.getTotalArm();
                    }
                }
            }
        }
        return totalArm;
    }

    /**
     * 征收
     *
     * @param now
     */
    public void levy(int now) {
        produceDecrement();

        if (finishTime <= 0) {// 如果征收前已停止生产，开始生产
            beginProduce(now);
        }
    }

    /**
     * 以生产数量减一
     */
    private void produceDecrement() {
        if (produced > 0) {
            this.produced--;
        }
    }

    /**
     * 开启生产
     *
     * @param now
     */
    public void beginProduce(int now) {
        int num = DataResource.ac.getBean(ActivityDataManager.class).getActCityDrawing();
        finishTime = now + (StaticWorldDataMgr.getCityMap().get(cityId).getProduceTime() / num);
    }

    /**
     * 生产完成逻辑
     */
    public void produceFinish() {
        produceIncrement();
        setFinishTime(0);
    }

    /**
     * 生产出的数量加一
     */
    private void produceIncrement() {
        if (!isProducedFull()) {
            this.produced++;
        }
    }

    /**
     * 产出是否达到上限
     *
     * @return
     */
    public boolean isProducedFull() {
        return produced >= WorldConstant.CITY_MAX_PRODUCE;
    }

    /**
     * 城池开启城主竞选
     *
     * @param now
     */
    public void startCampaign(int now) {
        if (null == campaignList) {
            campaignList = new ArrayList<CamppaignRole>();
        }
        campaignList.clear();

        campaignTime = now + WorldConstant.CAMPAIGN_TIME;
    }

    /**
     * 当前是否处于城主竞选中
     *
     * @return
     */
    public boolean isInCampagin() {
        return campaignTime > 0;
    }

    /**
     * 当前城主竞选是否到了结束的时候
     *
     * @param now
     * @return
     */
    public boolean isCampaignEndTime(int now) {
        if (campaignTime > 0 && campaignTime < now) {
            return true;
        }
        return false;
    }

    /**
     * 玩家是否已经对该城发起过重建
     *
     * @param roleId
     * @return
     */
    public boolean roleHasJoinRebuild(long roleId) {
        for (CamppaignRole role : campaignList) {
            if (role.getRoleId() == roleId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置城主
     *
     * @param ownerId
     * @param now
     */
    public void setOwner(long ownerId, int now) {
        setOwnerId(ownerId);
        setBeginTime(now);
        setEndTime(now + WorldConstant.CITY_OWNER_TIME);

        // 生成NPC守军阵型
        initNpcForm(true);
    }

    /**
     * 初始化城池首杀阵容(没有阵营归属的城池)
     */
    private void initFirstForm() {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            LogUtil.error("生成城池NPC阵型，未找到城池配置信息, cityId:,", cityId);
            return;
        }
        CityHero hero;
        StaticNpc npc;
        if (null == formList) {
            formList = new ArrayList<>();
        }
        formList.clear();
        List<Integer> tmpFormList = staticCity.getFormList(isNpcCity());
        for (Integer npcId : tmpFormList) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            hero = new CityHero(npcId, npc.getTotalArm());
            formList.add(hero);
        }
        LogUtil.debug("初始化NPC守军阵型=" + formList + ",ownerId=" + ownerId + " cityId=" + cityId);
    }

    /**
     * 初始化守军阵型(有阵营归属的)
     *
     * @param addArm 是否加入兵力
     */
    public void initNpcForm(boolean addArm) {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            LogUtil.error("生成城池NPC阵型，未找到城池配置信息, cityId:,", cityId);
            return;
        }

        CityHero hero;
        StaticNpc npc;
        if (null == formList) {
            formList = new ArrayList<>();
        }
        formList.clear();
        List<Integer> tmpFormList = staticCity.getFormList(isNpcCity());
        for (Integer npcId : tmpFormList) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            int arm = addArm ? npc.getTotalArm() : 0;
            // NPC守军阵型,初始化兵力为0,等待城主修复
            hero = new CityHero(npcId, arm);
            formList.add(hero);
        }
        LogUtil.debug("初始化NPC守军阵型=" + formList + ",ownerId=" + ownerId + " cityId=" + cityId);
    }

    /**
     * 恢复到初始状态
     */
    public void clearStateToInit() {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            LogUtil.error("生成城池NPC阵型，未找到城池配置信息, cityId:,", cityId);
            return;
        }
        setCityLv(staticCity.getLv());
        if (staticCity.getType() == WorldConstant.CITY_TYPE_HOME) {
            setCityLv(1);// 都城初始开发
        }
        if (staticCity.getType() != CrossWorldMapConstant.CITY_TYPE_CAMP) {
            setCamp(Constant.Camp.NPC);
        }
        setStatus(WorldConstant.CITY_STATUS_CALM);
        setAttackCamp(0);
        setCloseTime(0);
        setOwnerId(0);
        setBeginTime(0);
        setLeaveOver(0);
        setEndTime(0);
        setProduced(0);
        setFinishTime(0);
        setCampaignTime(0);
        if (campaignList != null) campaignList.clear();
        setExtraReward(0);
        setAtkBeginTime(0);
        setExp(0);
        setBuildingExp(0);
        setProtectTime(0);
        setName("");
        if (attackRoleId != null) attackRoleId.clear();
        firstKillReward.clear();
        // 阵型初始化
        // CityHero hero;
        // StaticNpc npc;
        if (null == formList) {
            formList = new ArrayList<>();
        }
        formList.clear();
        // 初始化城池阵容
        getFormList();
        // for (Integer npcId : staticCity.getForm()) {
        // npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        // hero = new CityHero(npcId, npc.getTotalArm());
        // formList.add(hero);
        // }
        LogUtil.debug("恢复城池到初始化状态 ", ",cityId=" + cityId);
    }


    /**
     * 恢复到初始状态
     */
    public void mergeClearStateToInit(int curScheduleId) {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            LogUtil.error("生成城池NPC阵型，未找到城池配置信息, cityId:,", cityId);
            return;
        }
        setCityLv(staticCity.getLv());
        if (staticCity.getType() == WorldConstant.CITY_TYPE_HOME) {
            setCityLv(1);// 都城初始开发
        }
        if (staticCity.getType() != CrossWorldMapConstant.CITY_TYPE_CAMP) {
            setCamp(Constant.Camp.NPC);
        }
        setStatus(WorldConstant.CITY_STATUS_CALM);
        setAttackCamp(0);
        setCloseTime(0);
        setOwnerId(0);
        setBeginTime(0);
        setLeaveOver(0);
        setEndTime(0);
        setProduced(0);
        setFinishTime(0);
        setCampaignTime(0);
        if (campaignList != null) campaignList.clear();
        setExtraReward(0);
        setAtkBeginTime(0);
        setExp(0);
        setBuildingExp(0);
        setProtectTime(0);
        setName("");
        if (attackRoleId != null) attackRoleId.clear();
        firstKillReward.clear();
        // 阵型初始化
        // CityHero hero;
        // StaticNpc npc;
        if (null == formList) {
            formList = new ArrayList<>();
        }
        formList.clear();
        // 初始化城池阵容
        mergeGetFormList(curScheduleId);
        // for (Integer npcId : staticCity.getForm()) {
        // npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        // hero = new CityHero(npcId, npc.getTotalArm());
        // formList.add(hero);
        // }
        LogUtil.debug("恢复城池到初始化状态 ", ",cityId=" + cityId);
    }

    /**
     * 当前是否有城主
     *
     * @return
     */
    public boolean hasOwner() {
        return ownerId > 0;
    }

    /**
     *
     */
    /**
     * 清除城主信息
     *
     * @param clearProduce 是否清除产出 true为清除产出
     */
    public void cleanOwner(boolean clearProduce) {
        setLeaveOver(-1);
        setOwnerId(0);
        setBeginTime(0);
        setEndTime(0);
        if (clearProduce) {
            setProduced(0);
            if (getCamp() > 0) {
                beginProduce(TimeHelper.getCurrentSecond());
            }
        }
        // 清空城池NPC守军
        formList.clear();
    }

    /**
     * 城池是否处于保护中
     *
     * @return
     */
    public boolean isFree() {
        return status == WorldConstant.CITY_STATUS_FREE;
    }

    /**
     * 城池是否处于保护中
     *
     * @return
     */
    public boolean isProtect() {
        return protectTime > TimeHelper.getCurrentSecond();
    }

    /**
     * 是否是都城
     *
     * @return true是都城
     */
    public boolean isCaptainCity() {
        return WorldConstant.CAPTAIN_CITYIDS.contains(cityId);
    }

    /**
     * 结束城池保护状态
     */
    public void endFree() {
        setStatus(WorldConstant.CITY_STATUS_CALM);
        setCloseTime(0);
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getCityLv() {
        return cityLv;
    }

    public void setCityLv(int cityLv) {
        this.cityLv = cityLv;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(int closeTime) {
        this.closeTime = closeTime;
    }

    public int getAttackCamp() {
        return attackCamp;
    }

    public void setAttackCamp(int attackCamp) {
        this.attackCamp = attackCamp;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getProduced() {
        return produced;
    }

    public void setProduced(int produced) {
        this.produced = produced;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public int getCampaignTime() {
        return campaignTime;
    }

    public void setCampaignTime(int campaignTime) {
        this.campaignTime = campaignTime;
    }

    public List<CamppaignRole> getCampaignList() {
        return campaignList;
    }

    public void setCampaignList(List<CamppaignRole> campaignList) {
        this.campaignList = campaignList;
    }

    public int getExtraReward() {
        return extraReward;
    }

    public void setExtraReward(int extraReward) {
        this.extraReward = extraReward;
    }

    public int getBuildingExp() {
        return buildingExp;
    }

    public void setBuildingExp(int buildingExp) {
        this.buildingExp = buildingExp;
    }

    /**
     * 获取阵容信息,获取不到则初始化
     *
     * @return
     */
    public List<CityHero> getFormList() {
        if (CheckNull.isEmpty(formList) && isNpcCity()) {
            initFirstForm();
        } else if (CheckNull.isEmpty(formList) && !isNpcCity()) {
            initNpcForm( false);
        }
        return formList;
    }


    /**
     * 获取阵容信息,获取不到则初始化
     *
     * @return 阵容信息
     */
    private List<CityHero> mergeGetFormList(int curScheduleId) {
        if (CheckNull.isEmpty(formList) && isNpcCity()) {
            mergeInitFirstForm(curScheduleId);
        } else if (CheckNull.isEmpty(formList) && !isNpcCity()) {
            mergeInitNpcForm(curScheduleId, false);
        }
        return formList;
    }

    /**
     * 合服初始化NPC阵容
     * @param curSchId 世界进程id
     * @param addArm 添加兵力
     */
    private void mergeInitNpcForm(int curSchId, boolean addArm) {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            LogUtil.error("生成城池NPC阵型，未找到城池配置信息, cityId:,", cityId);
            return;
        }

        CityHero hero;
        StaticNpc npc;
        if (null == formList) {
            formList = new ArrayList<>();
        }
        formList.clear();
        List<Integer> tmpFormList = staticCity.getFormList(isNpcCity());
        for (Integer npcId : tmpFormList) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            int arm = addArm ? npc.getTotalArm() : 0;
            // NPC守军阵型,初始化兵力为0,等待城主修复
            hero = new CityHero(npcId, arm);
            formList.add(hero);
        }
        LogUtil.debug("合服初始化NPC守军阵型=" + formList + ",ownerId=" + ownerId + " cityId=" + cityId);
    }

    /**
     * 合服初始化首杀NPC阵容
     * @param curSchId 世界进程id
     */
    private void mergeInitFirstForm(int curSchId) {
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            LogUtil.error("生成城池NPC阵型，未找到城池配置信息, cityId:,", cityId);
            return;
        }
        CityHero hero;
        StaticNpc npc;
        if (null == formList) {
            formList = new ArrayList<>();
        }
        formList.clear();
        List<Integer> tmpFormList = staticCity.getFormList(isNpcCity());
        for (Integer npcId : tmpFormList) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            hero = new CityHero(npcId, npc.getTotalArm());
            formList.add(hero);
        }
        LogUtil.debug("合服初始化NPC守军阵型=" + formList + ",ownerId=" + ownerId + " cityId=" + cityId);
    }

    /**
     * 清除阵营状态,并且初始化阵容配置
     */
    public void clearFormList() {
        if (formList == null) {
            formList = new ArrayList<>();
        }
        formList.clear();
        // 初始化阵容配置
        getFormList();
    }

    public void setFormList(List<CityHero> formList) {
        this.formList = formList;
    }

    public int getAtkBeginTime() {
        return atkBeginTime;
    }

    public void setAtkBeginTime(int atkBeginTime) {
        this.atkBeginTime = atkBeginTime;
    }

    public void setNextAtkBeginTime(int now) {
        this.atkBeginTime = now + WorldConstant.CITY_NPC_FIGHT;
    }

    public int getProtectTime() {
        return protectTime;
    }

    public void setProtectTime(int protectTime) {
        this.protectTime = protectTime;
    }

    /**
     * 都城怪物攻城
     *
     * @param now
     * @return
     */
    public boolean isAtkCity(int now) {
        if (StaticWorldDataMgr.getCityMap().get(cityId).getType() == WorldConstant.CITY_TYPE_HOME) {
            // LogUtil.debug("下次攻城时间=" + cityId + ",atkBeginTime=" + atkBeginTime + ",now=" + now);
        }
        if (StaticWorldDataMgr.getCityMap().get(cityId).getType() == WorldConstant.CITY_TYPE_HOME
                && now > atkBeginTime) {
            setNextAtkBeginTime(now);
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getNextDevTime() {
        return nextDevTime;
    }

    public void setNextDevTime(int nextDevTime) {
        this.nextDevTime = nextDevTime;
    }

    public Set<Long> getAttackRoleId() {
        return attackRoleId;
    }

    public Map<Long, Integer> getFirstKillReward() {
        return firstKillReward;
    }

    /**
     * 是否有首杀奖励
     *
     * @param roleId
     * @return true 拥有首杀奖励
     */
    public boolean hasFirstKillReward(long roleId) {
        return firstKillReward.get(roleId) != null && firstKillReward.get(roleId) == 0;
    }

    @Override
    public String toString() {
        return "City [cityId=" + cityId + ", cityLv=" + cityLv + ", camp=" + camp + ", status=" + status
                + ", closeTime=" + closeTime + ", attackCamp=" + attackCamp + ", ownerId=" + ownerId + ", beginTime="
                + beginTime + ", endTime=" + endTime + ", produced=" + produced + ", finishTime=" + finishTime
                + ", campaignTime=" + campaignTime + ", campaignList=" + campaignList + ", extraReward=" + extraReward
                + ", formList=" + formList + ", atkBeginTime=" + atkBeginTime + ", name=" + name + ", exp=" + exp + "]";
    }

    public int getLeaveOver() {
        return leaveOver;
    }

    public void setLeaveOver(int leaveOver) {
        this.leaveOver = leaveOver;
    }
}
