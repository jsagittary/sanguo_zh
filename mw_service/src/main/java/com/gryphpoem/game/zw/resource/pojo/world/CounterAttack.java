package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticCounterAttack;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-09 11:05
 * @description: 德意志反攻的数据
 * @modified By:
 */
public class CounterAttack extends CycleData {

    /**
     * 城池id
     */
    private int cityId;
    /**
     * 坐标
     */
    private int pos;
    /**
     * 血量
     */
    private Fighter fighter;
    /**
     * 当前反攻状态, 1 boss防守, 2 boss进攻, 3 boss死亡
     */
    private int status;
    /**
     * 当前是第几个boss
     */
    private int currentBoss;
    /**
     * 当前是第几轮进攻
     */
    private int currentAtkCnt;
    /**
     * NPC进攻阵型
     */
    private List<CityHero> atkList;
    /**
     * 本次被攻击的玩家Id
     */
    private Map<Integer, Set<Long>> campHitRoleId = new HashMap<>();
    /**
     * 参战玩家
     */
    private Set<Long> joinBattleRole = new HashSet<>();
    /**
     * 军火库记录, <roleId, <shopid(), count>>
     */
    private Map<Long, Map<Integer, Integer>> counterShop = new HashMap<>();

    /**
     * 获取玩家的军火库购买记录
     *
     * @param roleId
     * @return
     */
    public Map<Integer, Integer> getCounterShop(long roleId) {
        Map<Integer, Integer> shopCount = counterShop.get(roleId);
        if (CheckNull.isNull(shopCount)) {
            shopCount = new HashMap<>();
            counterShop.put(roleId, shopCount);
        }
        return shopCount;
    }

    public Map<Long, Map<Integer, Integer>> getCounterShop() {
        return counterShop;
    }

    /**
     * 根据进攻次数获取当前进攻
     *
     * @return
     */
    public List<CityHero> getAtkFormList() {
        return atkList;
    }

    /**
     * 根据次数获取NPC阵型
     *
     * @param cnt
     */
    public void initAtkForm(int cnt) {
        if (null == atkList) {
            atkList = new ArrayList<>();
        }
        atkList.clear();
        StaticCounterAttack config = StaticWorldDataMgr
                .getCounterAttackTypeMapByCond(WorldConstant.COUNTER_ATK_ATK, cnt);
        if (!CheckNull.isNull(config)) {
            atkList = config.getNpcForm();
        }
        currentAtkCnt = cnt;
    }

    /**
     * Fighter对象是否初始化,血量是否为零
     *
     * @return
     */
    public boolean isNotInitOrDead() {
        return CheckNull.isNull(fighter) || currentHp() == 0;
    }

    /**
     * 当前血量
     *
     * @return
     */
    public int currentHp() {
        int currentHp = fighter.getTotal() - fighter.getLost();
        return currentHp <= 0 ? 0 : currentHp;
    }

    /**
     * 重置数据
     */
    public void reset() {
        this.cityId = 0;
        this.pos = 0;
        this.status = 0;
        this.currentBoss = 0;
        this.currentAtkCnt = 0;
        super.reset();
        this.campHitRoleId.clear();
        this.joinBattleRole.clear();
    }

    public CommonPb.CounterAtkData.Builder createCounterAttackPb() {
        CommonPb.CounterAtkData.Builder builder = CommonPb.CounterAtkData.newBuilder();
        builder.setCycleData(super.createCycleDataPb());
        builder.setCityId(cityId);
        builder.setStatus(status);
        builder.setCurrentBoss(currentBoss);
        return builder;
    }

    public void incrAtkCnt() {
        this.currentAtkCnt++;
    }

    public int incrBossCnt() {
        return ++this.currentBoss;
    }

    public int getCurrentAtkCnt() {
        return currentAtkCnt;
    }

    public int getCurrentBoss() {
        return currentBoss;
    }

    public int getCityId() {
        return cityId;
    }

    public Set<Long> getJoinBattleRole() {
        return joinBattleRole;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Fighter getFighter() {
        return fighter;
    }

    public void setFighter(Fighter fighter) {
        this.fighter = fighter;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 根据阵营获取被选中的玩家
     *
     * @param camp
     * @return
     */
    public Set<Long> getCampHitRoleId(int camp) {
        Set<Long> roleIds = campHitRoleId.get(camp);
        if (CheckNull.isNull(roleIds)) {
            roleIds = new HashSet<>();
            campHitRoleId.put(camp, roleIds);
        }
        return roleIds;
    }

    /**
     * 清除被选中的玩家
     */
    public void clearCampHitRoles() {
        this.campHitRoleId.clear();
    }

    /**
     * 获取所有被选中的玩家
     *
     * @return
     */
    public Set<Long> getCampHitRoleId() {
        return campHitRoleId.values().stream().flatMap(roles -> roles.stream()).collect(Collectors.toSet());
    }

    /**
     * 序列化
     *
     * @return
     */
    public SerializePb.SerCounterAttack ser() {
        SerializePb.SerCounterAttack.Builder builder = SerializePb.SerCounterAttack.newBuilder();
        builder.setCycleData(super.createCycleDataPb());
        builder.setCityId(cityId);
        builder.setPos(pos);
        builder.setStatus(status);
        if (!CheckNull.isEmpty(campHitRoleId)) {
            for (Map.Entry<Integer, Set<Long>> en : campHitRoleId.entrySet()) {
                int camp = en.getKey();
                for (Long roleId : en.getValue()) {
                    builder.addRoleInfo(PbHelper.createIntLongPc(camp, roleId));
                }
            }
        }
        builder.setCurrentBoss(currentBoss);
        builder.setCurrentAtkCnt(currentAtkCnt);
        if (!CheckNull.isNull(getFighter())) {
            for (Force force : getFighter().getForces()) {
                builder.addForce(
                        CommonPb.Force.newBuilder().setNpcId(force.id).setHp(force.hp).setCurLine(force.curLine));
            }
        }
        if (!CheckNull.isEmpty(joinBattleRole)) {
            builder.addAllJoinBattleRole(joinBattleRole);
        }
        if (!CheckNull.isEmpty(counterShop)) {
            for (Map.Entry<Long, Map<Integer, Integer>> en : counterShop.entrySet()) {
                SerializePb.SerCounterShop.Builder shopBuilder = SerializePb.SerCounterShop.newBuilder();
                shopBuilder.setRoleId(en.getKey());
                for (Map.Entry<Integer, Integer> shopInfo : en.getValue().entrySet()) {
                    shopBuilder.addShopInfo(PbHelper.createTwoIntPb(shopInfo.getKey(), shopInfo.getValue()));
                }
                builder.addShopInfo(shopBuilder.build());
            }
        }
        return builder.build();
    }

    /**
     * 反序列化
     *
     * @param ser
     */
    public void dser(SerializePb.SerCounterAttack ser) {
        super.dser(ser.getCycleData());
        this.cityId = ser.getCityId();
        this.pos = ser.getPos();
        this.status = ser.getStatus();
        if (!CheckNull.isEmpty(ser.getRoleInfoList())) {
            List<CommonPb.IntLong> roleInfos = ser.getRoleInfoList();
            for (CommonPb.IntLong intLong : roleInfos) {
                int camp = intLong.getV1();
                Set<Long> roles = this.campHitRoleId.get(camp);
                if (CheckNull.isNull(roles)) {
                    roles = new HashSet<>();
                    this.campHitRoleId.put(camp, roles);
                }
                roles.add(intLong.getV2());
            }
        }
        this.currentBoss = ser.getCurrentBoss();
        this.currentAtkCnt = ser.getCurrentAtkCnt();
        if (!CheckNull.isEmpty(ser.getJoinBattleRoleList())) {
            this.joinBattleRole.addAll(ser.getJoinBattleRoleList());
        }
        if (!CheckNull.isEmpty(ser.getShopInfoList())) {
            for (SerializePb.SerCounterShop shop : ser.getShopInfoList()) {
                long roleId = shop.getRoleId();
                Map<Integer, Integer> shopInfo = counterShop.get(roleId);
                if (CheckNull.isNull(shopInfo)) {
                    shopInfo = new HashMap<>();
                    counterShop.put(roleId, shopInfo);
                }
                if (!CheckNull.isEmpty(shop.getShopInfoList())) {
                    for (CommonPb.TwoInt twoInt : shop.getShopInfoList()) {
                        shopInfo.put(twoInt.getV1(), twoInt.getV2());
                    }
                }
            }
        }
    }
}
