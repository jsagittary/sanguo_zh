package com.gryphpoem.game.zw.gameplay.local.world.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.army.AttackWFCityArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFire;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 战火燎原城池扩展
 *
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-28 14:31
 */
public class WFCityMapEntity extends CityMapEntity {

    public static final int WF_CITY_STATUS_0 = 0;
    public static final int WF_CITY_STATUS_1 = 1;
    public static final int WF_CITY_STATUS_2 = 2;

    /**
     * 状态：0 中立，1 占领中, 2 驻守中
     */
    private int status;
    /**
     * 如果状态是1的话, 这里是成功占领的时间
     */
    private int occupyTime;

    /**
     * 上一次结算据点积分时间, status = WF_CITY_STATUS_2 时 0 表示为首次获得积分
     */
    private int lastScoreTime;

    /**
     * 记录行军到本城池的行军部队, 这里做了缓存而已
     */
    private Queue<AttackWFCityArmy> queue;

    //1血阵营
    private int firstBloodCamp;
    //1血玩家列表
    private List<String> firstBloodRoleNames;

    /**
     * key, 1 - 3 记录的是三个阵营的最近分享时间
     */
    private Map<Integer, Integer> statusMap = new HashMap<>();


    public WFCityMapEntity(int pos, City city) {
        super(pos, city);
        queue = new PriorityQueue<>(Comparator.comparing(cityArmy -> cityArmy.getArmy().getEndTime()));
        firstBloodRoleNames = new ArrayList<>(10);
    }

    @Override
    public CommonPb.AreaCity toAreaCityPb(Player invokePlayer, CrossWorldMap crossWorldMap) {
        return super.toAreaCityPb(invokePlayer, crossWorldMap).toBuilder().setExt(toWarFireCityExtPb(false)).build();
    }

    /**
     * 获取状态
     *
     * @return 状态：0 中立，1 占领中, 2 驻守中
     */
    public int getStatus() {
        int now = TimeHelper.getCurrentSecond();
        // 过了占领时间, 驻守中
        if (occupyTime == 0) {
            // 中立
            return WF_CITY_STATUS_0;
        } else if (now >= occupyTime) {
            // 占领完成
            return WF_CITY_STATUS_2;
        } else {
            // 占领中
            return WF_CITY_STATUS_1;
        }
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getOccupyTime() {
        return occupyTime;
    }

    public void setOccupyTime(int occupyTime) {
        // 重置状态为占领中
        this.status = WF_CITY_STATUS_1;
        this.occupyTime = occupyTime;
    }

    public Queue<AttackWFCityArmy> getQueue() {
        return queue;
    }

    public CommonPb.WarFireMapEntityExt toWarFireCityExtPb(boolean isSer) {
        CommonPb.WarFireMapEntityExt.Builder builder = CommonPb.WarFireMapEntityExt.newBuilder();
        City city = super.getCity();
        builder.setStatus(getStatus());
        builder.setOccupyTime(occupyTime);
        // 城池的阵营所属
        int cityCamp = city.getCamp();
        if (!isSer) {
            // 城池部队扩展
            queue.stream().map(army -> army.toArmyPb(cityCamp)).forEach(builder::addArmy);
        }
        builder.setLastScoreTime(lastScoreTime);
        builder.setFirstBloodCamp(firstBloodCamp);
        if (!CheckNull.isEmpty(firstBloodRoleNames)) {
            firstBloodRoleNames.forEach(builder::addFirstBloodNick);
        }
        if (!CheckNull.isEmpty(statusMap)) {
            statusMap.forEach((k, v) -> builder.addStatue(PbHelper.createTwoIntPb(k, v)));
        }
        return builder.build();
    }

    @Override
    public CommonPb.MapCity toMapCityPb(Player invokePlayer, CrossWorldMap crossWorldMap) {
        return super.toMapCityPb(invokePlayer, crossWorldMap).toBuilder().setExt(toWarFireCityExtPb(false)).build();
    }

    /**
     * 序列化
     *
     * @return
     */
    @Override
    public CommonPb.MapEntityPb.Builder toDbData() {
        CommonPb.MapEntityPb.Builder dbData = super.toDbData();
        dbData.setCity(PbHelper.createCityPb(super.getCity()));
        dbData.setCityExt(toWarFireCityExtPb(true));
        return dbData;
    }

    /**
     * 对城池发起进攻
     *
     * @param param
     * @throws MwException
     */
    @Override
    public void attackPos(AttackParamDto param) throws MwException {
        Player player = param.getInvokePlayer();
        long roleId = player.roleId;
        int camp = player.lord.getCamp();

        City city = this.getCity();
        int cityId = city.getCityId();
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        if (city.isProtect()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "此城池还未开放, roleId:", roleId, ", cityId:", cityId, ", protectTime:", city.getProtectTime());
        }
        // 本阵营的部队
        List<AttackWFCityArmy> armyByCamp = getArmyByCamp(Collections.singletonList(camp));
        // 城池部队上限
        int joinCnt = armyByCamp.size();
        if (joinCnt >= WorldConstant.WAR_FIRE_CITY_ARMY_MAX) {
            throw new MwException(GameError.WAR_FIRE_ATK_POS_COUNT_MAX.getCode(), "城池大于了最大人数, roleId:", roleId, ", cityId:", cityId, ", joinCnt:", joinCnt);
        }
        if (param.getHeroIdList().size() > 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "超出了单次派将数量, roleId:", roleId, ", cityId:", cityId, ", heroCnt:", param.getHeroIdList().size());
        }
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        // 检测并扣除战斗消耗
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, param.getNeedFood(), AwardFrom.ATK_POS);
        // 创建部队
        BaseArmy baseArmy = createBaseArmy(param, player);
        // 事件通知
        crossWorldMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
        // 填充返回值
        AttackCrossPosRs.Builder builder = param.getBuilder();
        builder.setArmy(param.getArmy());
    }

    /**
     * 创建行军部队
     *
     * @param param  参数
     * @param player 玩家对象
     * @return 部队
     */
    private BaseArmy createBaseArmy(AttackParamDto param, Player player) {
        int marchTime = param.getMarchTime();
        List<CommonPb.TwoInt> form = param.getHeroIdList().stream().map(heroId -> {
            Hero hero = player.heros.get(heroId);
            return PbHelper.createTwoIntPb(heroId, hero.getCount());
        }).collect(Collectors.toList());
        int now = TimeHelper.getCurrentSecond();
        int battleTime = now + marchTime;
        Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_WF_ATK_CITY, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime,
                battleTime, player.getDressUp());
        army.setLordId(player.lord.getLordId());
        army.setBattleTime(battleTime);
        army.setOriginPos(player.lord.getPos());
        // 添加行军路线
        BaseArmy baseArmy = BaseArmy.baseArmyFactory(army);
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        baseArmy.setArmyPlayerHeroState(crossWorldMap.getMapMarchArmy(), ArmyConstant.ARMY_STATE_MARCH);
        crossWorldMap.getMapMarchArmy().addArmy(baseArmy);
        // 加入缓存
        queue.add((AttackWFCityArmy) baseArmy);
        param.setArmy(PbHelper.createArmyPb(baseArmy.getArmy(), false));
        return baseArmy;
    }

    /**
     * 获取指定阵营的部队
     *
     * @param camps 阵营
     * @return 部队
     */
    public List<AttackWFCityArmy> getArmyByCamp(List<Integer> camps) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        return queue.stream().filter(queue -> {
            Army army = queue.getArmy();
            long lordId = army.getLordId();
            Player p = playerDataManager.getPlayer(lordId);
            // 过滤同阵营的
            return Objects.nonNull(p) && camps.contains(p.lord.getCamp());
        }).collect(Collectors.toList());
    }

    public List<AttackWFCityArmy> getArmysInCity(Player player) {
        return queue.stream()
                .filter(wfcArmy -> wfcArmy.getLordId() == player.getLordId())
                .collect(Collectors.toList());
    }

    /**
     * 占领的阵营变换
     *
     * @param camp 新的占领阵营
     * @return 占领时间
     */
    public int changeOccupy(int camp, CrossWorldMap cMap) {
        // 服务器时间
        int now = TimeHelper.getCurrentSecond();
        // 设置城池的所属阵营
        City city = getCity();
        // 历史阵营
        int oldCamp = city.getCamp();
        // 变换阵营
        city.setCamp(camp);
        // 当前战火燎原配置
        int cityId = city.getCityId();
        StaticWarFire curWarFire = StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityId);
        if (Objects.nonNull(curWarFire)) {
            // 配置的占领时间
            int occupyTime = curWarFire.getOccupyTime();
            // 当前阵营所有占领的城池, 所拥有的减占领时间buff
            List<List<Integer>> buffs = cMap.getCityMap().values()
                    .stream()
                    .filter(cityEntity -> {
                        if (cityEntity instanceof WFCityMapEntity) {
                            // 战火燎原城池, 并且是当前的阵营的所属, 已经过了等待占领的时间
                            WFCityMapEntity wfCityEntity = (WFCityMapEntity) cityEntity;
                            return now > wfCityEntity.getOccupyTime() && wfCityEntity.getCity().getCamp() == camp;
                        }
                        return false;
                    })
                    .map(cityEntity -> StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityEntity.getCity().getCityId()))
                    .filter(swf -> Objects.nonNull(swf) && !CheckNull.isEmpty(swf.getBuff()))
                    .flatMap(swf -> swf.getBuff().stream().filter(buff -> buff.get(0) == StaticWarFire.BUFF_TYPE_1))
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(buffs)) {
                int buffVal = buffs.stream().mapToInt(buff -> buff.get(1)).sum();
                // 减少占领时间
                occupyTime = (int) Math.ceil(occupyTime * (buffVal / Constant.TEN_THROUSAND));
            }

            // 占领城池buff
            List<List<Integer>> curBuffs = curWarFire.getBuff();
            // 历史的等待占领时间
            int oldTime = getOccupyTime();
            if (oldCamp != 0) {
                // 阵营占领时长埋点
                LogLordHelper.otherLog("warFireOccupy", DataResource.ac.getBean(ServerSetting.class).getServerID(), cityId, oldCamp, camp, now - oldTime);
            }
            if (!CheckNull.isEmpty(curBuffs) && curBuffs.stream().anyMatch(buff -> buff.get(0) == StaticWarFire.BUFF_TYPE_3 || buff.get(0) == StaticWarFire.BUFF_TYPE_4)) {
                if (now > oldTime) {
                    // 去掉阵营的属性buff
                    cMap.getPlayerMap().values().stream().map(PlayerMapEntity::getPlayer).filter(p -> p.lord.getCamp() == oldCamp).forEach(CalculateUtil::reCalcAllHeroAttr);
                }
            }
            // 设置占领时间
            this.setOccupyTime(occupyTime + now);
            // 新的占领阵营. 待占领时间到达后需要重新计算属性
            cMap.getGlobalWarFire().addSchedule(this);
        }
        // 变换阵营
        city.setCamp(camp);
        return curWarFire.getOccupyTime();
    }

    /**
     * 清除战火燎原城池的状态
     */
    @Override
    public void clearStateToInit() {
        super.clearStateToInit();
        status = WF_CITY_STATUS_0;
        occupyTime = 0;
        queue.clear();
        lastScoreTime = 0;
        firstBloodCamp = 0;
        firstBloodRoleNames = new ArrayList<>();
        statusMap.clear();
    }

    public int getLastScoreTime() {
        return lastScoreTime;
    }

    public void setLastScoreTime(int lastScoreTime) {
        this.lastScoreTime = lastScoreTime;
    }

    public int getFirstBloodCamp() {
        return firstBloodCamp;
    }

    public List<String> getFirstBloodRoleNames() {
        return firstBloodRoleNames;
    }

    public void setFirstBloodCamp(int firstBloodCamp) {
        this.firstBloodCamp = firstBloodCamp;
    }

    public Map<Integer, Integer> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map<Integer, Integer> statusMap) {
        this.statusMap = statusMap;
    }
}
