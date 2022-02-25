package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.BerlinWarConstant;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticBerlinWar;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.fight.AttrData;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author: ZhouJie
 * @date: Create in 2018-07-18 14:39
 * @description: 柏林会战据点信息
 * @modified By: z
 */
public class BerlinCityInfo {

    private Map<Integer, Integer> statusTime = new HashMap<>();             // 阵营, 占领时间(毫秒), key: camp
    private LinkedList<Force> cityDef = new LinkedList();                   // 城防军
    private ArrayList<BerlinForce> roleQueue = new ArrayList<>();           // 玩家队列
    private int camp;                                                       // 所属阵营
    private int atkArm;                                                     // 进攻方兵力
    private int defArm;                                                     // 防守方兵力
    private int cityId;                                                     // 对应s_city表中cityId
    private int pos;                                                        // 对应的city坐标
    private int lastOccupyTime;                                             // 占领时间
    private int nextAtkTime;                                                // 下次进攻时间

    public BerlinCityInfo() {
    }

    /**
     * 获取将领的出战顺序
     *
     * @param roleId   玩家id
     * @param heroId   将领id
     * @param atkOrDef 进攻方或防守方
     * @return 出战位置
     */
    public int heroIndex(long roleId, int heroId, int atkOrDef) {
        int index = 0;
        List<BerlinForce> heroQueue = getRoleQueue().stream().filter(Force::alive).filter(f -> f.getAtkOrDef() == atkOrDef)
                .sorted(Comparator.comparingInt(BerlinForce::getImmediatelyTime)
                        .thenComparing(Comparator.comparingInt(BerlinForce::getAddMode).reversed())
                        .thenComparingLong(BerlinForce::getAddTime))
                .collect(Collectors.toList());
        if (!CheckNull.isEmpty(heroQueue)) {
            BerlinForce force = heroQueue.stream().filter(Force::alive).filter(f -> f.ownerId == roleId && f.id == heroId).findFirst().orElse(null);
            if (!CheckNull.isNull(force)) {
                index = heroQueue.indexOf(force);
            }
        }
        return index;
    }


    /**
     * 将玩家将领移除队列
     *
     * @param roleId 玩家id
     * @param heroId 将领id
     * @return 是否移除
     */
    public boolean retreatArmy(long roleId, int heroId) {
        boolean flag = false;
        Iterator<BerlinForce> iterator = roleQueue.iterator();
        while (iterator.hasNext()) {
            BerlinForce berlinForce = iterator.next();
            if (berlinForce.ownerId == roleId && heroId == berlinForce.id) {
                flag = true;
                iterator.remove();
            }
        }
        return flag;
    }


    /**
     * 胜利方将领转成普通防守方将领
     */
    public void atkCampConvertDef() {
        if (!CheckNull.isEmpty(roleQueue)) {
            roleQueue.stream()
                    .filter(berlinForce -> berlinForce.getAtkOrDef() == WorldConstant.BERLIN_ATK)
                    .filter(berlinForce -> berlinForce.getCamp() == this.camp)
                    .forEach(berlinForce -> {
                        berlinForce.setAtkOrDef(WorldConstant.BERLIN_DEF);
                        berlinForce.setAddMode(WorldConstant.BERLIN_ATTACK_TYPE_COMMON);
                    });
            reCalcuAtkDefArm();
        }
    }

    /**
     * 重新计算攻防兵力
     */
    public void reCalcuAtkDefArm() {
        if (!CheckNull.isEmpty(roleQueue)) {
            this.atkArm = 0;
            roleQueue.stream().filter(berlinForce -> berlinForce.getAtkOrDef() == WorldConstant.BERLIN_ATK)
                    .forEach(berlinForce ->
                            this.atkArm += berlinForce.hp
                    );
            this.defArm = 0;
            roleQueue.stream().filter(berlinForce -> berlinForce.getAtkOrDef() == WorldConstant.BERLIN_DEF)
                    .forEach(berlinForce ->
                            this.defArm += berlinForce.hp
                    );
            if (!CheckNull.isEmpty(cityDef)) {
                cityDef.forEach(force ->
                        this.defArm += force.hp
                );
            }
        }
    }

    /**
     * 获取立即出击的一个将领
     *
     * @return Force
     */
    public BerlinForce getImmediatelyForce() {
        BerlinForce berlinForce = null;
        // 获取立即出击的一个将领
        if (!CheckNull.isEmpty(roleQueue)) {
            berlinForce = roleQueue.stream()
                    .filter(f -> f.alive() && f.getImmediatelyTime() > 0)
                    .min(
                            Comparator.comparingInt(BerlinForce::getImmediatelyTime)
                                    .thenComparing(Comparator.comparingInt(BerlinForce::getAddMode).reversed())
                                    .thenComparingLong(BerlinForce::getAddTime))
                    .orElse(null);
        }
        return berlinForce;
    }

    /**
     * 获取进攻方或者防守方一个将领
     *
     * @param atkOrDef 进攻方或者防守方
     * @return Force
     */
    public BerlinForce getSingleForce(int atkOrDef) {
        BerlinForce berlinForce = null;
        //  获取进攻方或者防守方一个将领
        if (!CheckNull.isEmpty(roleQueue)) {
            berlinForce = roleQueue.stream()
                    .filter(Force::alive)
                    .filter(e -> e.getAtkOrDef() == atkOrDef)
                    .min(
                            Comparator.comparingInt(BerlinForce::getAddMode)
                                    .reversed()
                                    .thenComparingLong(BerlinForce::getAddTime))
                    .orElse(null);
        }
        return berlinForce;
    }


    /**
     * 选取将领的的比较器
     *
     * @param f1 BerlinForce
     * @param f2 BerlinForce
     * @return 比较
     */
    @Deprecated
    private int berlinFoceCompare(BerlinForce f1, BerlinForce f2) {
        // 优先比较添加方式
        if (f1.getAddMode() > f2.getAddMode()) {
            return 1;
        } else if (f1.getAddTime() > f2.getAddTime()) {
            // 比较加入队列时间
            return -1;
        }
        return 0;
    }


    /**
     * 更新阵营占领时间
     *
     * @param camp 本次占领的阵营
     * @param now  现在的时间
     */
    public void updateCampOccupt(int camp, int now) {
        if (this.camp != Constant.Camp.NPC && this.lastOccupyTime > 0) {
            Map<Integer, Integer> statusTime = this.statusTime;
            // 历史占领的时间
            int hisOccupy = statusTime.getOrDefault(camp, 0);
            // 本次占领的时间
            int occupyTime = this.lastOccupyTime > 0 && now > this.lastOccupyTime ? now - this.lastOccupyTime : 0;
            if (occupyTime > 0) {
                // 本次占领时间 / 15秒 = 增加的势力值 * 1000 (这里记录万分比)
                int addInfluence = (occupyTime / WorldConstant.BERLIN_INFLUENCE_CONF.get(0)) * WorldConstant.BERLIN_INFLUENCE_CONF.get(1);
                // 历史的势力值
                int hisInfluence = statusTime.getOrDefault(BerlinWarConstant.INFLUENCE_VALUE_PREFIX + camp, 0);
                // 更新势力值
                statusTime.put(BerlinWarConstant.INFLUENCE_VALUE_PREFIX + camp, hisInfluence + addInfluence);
            }
            occupyTime = occupyTime + hisOccupy;
            statusTime.put(camp, occupyTime);

            // 扣除前占领阵营的势力值
            int influence = statusTime.getOrDefault(BerlinWarConstant.INFLUENCE_VALUE_PREFIX + camp, 0);
            if (influence > 0) {
                influence = (int) (influence * (1 - (WorldConstant.BERLIN_INFLUENCE_CONF.get(2) / Constant.TEN_THROUSAND)));
                // 更新扣除后的势力值
                statusTime.put(BerlinWarConstant.INFLUENCE_VALUE_PREFIX + camp, influence);
            }
        }
    }

    /**
     * 获取城防将
     *
     * @return Force
     */
    public Force getSingleCityDef() {
        Force force = null;
        if (!CheckNull.isEmpty(cityDef)) {
            force = cityDef.stream().filter(Force::alive).findFirst().orElse(null);
        }
        return force;
    }


    /**
     * 移除城防将
     */
    public void removeCityDef() {
        if (!CheckNull.isEmpty(cityDef)) {
            Force atkForce = cityDef.getFirst();
            if (!atkForce.alive()) {
                cityDef.removeFirst();
            }
        }
    }

    /**
     * 是否是npc城池
     *
     * @return npc城池
     */
    public boolean isNpcCity() {
        return camp == Constant.Camp.NPC;
    }

    /**
     * 初始化NPC守军
     *
     * @param npcForm 配置
     */
    public void initCityDef(List<CityHero> npcForm) {
        Force force;
        // 城池NPC守军
        if (!CheckNull.isEmpty(npcForm)) {
            for (CityHero cityHero : npcForm) {
                if (cityHero.getCurArm() <= 0) continue;
                force = createCityNpcForce(cityHero.getNpcId(), cityHero.getCurArm());
                force.roleType = Constant.Role.CITY;
                addCityDefForce(force);
            }
        }
    }

    /**
     * 添加城防兵
     *
     * @param force Force
     */
    private void addCityDefForce(Force force) {
        if (!CheckNull.isNull(force)) {
            cityDef.addLast(force);
            defArm += force.maxHp;
        }
    }

    /**
     * 扣除防守兵力
     *
     * @param sub 扣除
     */
    public void subDefArm(int sub) {
        defArm -= sub;
    }

    /**
     * 创建NPC的Force对象
     *
     * @param npcId 配置id
     * @param count 兵力
     * @return force
     */
    private Force createCityNpcForce(int npcId, int count) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        return new Force(attrData, npc.getArmType(), count, attrData.lead, npcId, attrData.lead, npc.getLine());
    }


    /**
     * 获取各个阵营的占领时间
     *
     * @return Pb对象
     */
    public List<CommonPb.CampOccupy> getCampOccupy() {
        ArrayList<CommonPb.CampOccupy> campOccupies = new ArrayList<>();
        for (int camp : Constant.Camp.camps) {
            CommonPb.CampOccupy.Builder builder = CommonPb.CampOccupy.newBuilder();
            builder.setCamp(camp);
            builder.setTime(getCampOccupyTime(camp));
            builder.setInfluence(getCampInfluence(camp));
            campOccupies.add(builder.build());
        }
        return campOccupies;
    }

    /**
     * 获取指定阵营占领时间
     *
     * @param camp 指定阵营
     * @return 占领的时间
     */
    public int getCampOccupyTime(int camp) {
        int now = TimeHelper.getCurrentSecond();
        int hisOccupyTime = statusTime.getOrDefault(camp, 0);
        if (camp == this.camp) {
            hisOccupyTime += now - lastOccupyTime;
        }
        return hisOccupyTime;
    }

    /**
     * 获取阵营的势力值
     * @param camp 指定阵营
     * @return 势力值
     */
    public int getCampInfluence(int camp) {
        int now = TimeHelper.getCurrentSecond();
        int hisInfluence = this.statusTime.getOrDefault(BerlinWarConstant.INFLUENCE_VALUE_PREFIX + camp, 0);
        if (camp == this.camp) {
            // 当前战令的时间
            int occupyTime = now - lastOccupyTime;
            if (occupyTime > 0) {
                // 本次占领时间 / 15秒 = 增加的势力值 * 1000 (这里记录万分比)
                int addInfluence = (occupyTime / WorldConstant.BERLIN_INFLUENCE_CONF.get(0)) * WorldConstant.BERLIN_INFLUENCE_CONF.get(1);
                // 添加势力值
                hisInfluence += addInfluence;
            }
        }
        return hisInfluence;
    }

    /**
     * 获取当前阵营获胜的倒计时
     *
     * @return 当前时间戳 + (30分钟胜利时间 - 当前占领的时间) = 获胜的时间点
     */
    public int getWinOfCountdown() {
        int hisInfluence = this.statusTime.getOrDefault(BerlinWarConstant.INFLUENCE_VALUE_PREFIX + camp, 0);
        // 需要多少势力值
        int remain = (int) (Constant.TEN_THROUSAND - hisInfluence);
        // 需要的时间
        int needTime = (remain / WorldConstant.BERLIN_INFLUENCE_CONF.get(1)) * WorldConstant.BERLIN_INFLUENCE_CONF.get(0);
        return lastOccupyTime + needTime;
    }


    public ArrayList<BerlinForce> getRoleQueue() {
        return roleQueue;
    }

    public void setLastOccupyTime(int lastOccupyTime) {
        this.lastOccupyTime = lastOccupyTime;
    }

    public boolean defIsFail() {
        return this.defArm <= 0;
    }

    public Map<Integer, Integer> getStatusTime() {
        return statusTime;
    }

    public LinkedList<Force> getCityDef() {
        return cityDef;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getAtkArm() {
        return atkArm;
    }

    public void setAtkArm(int atkArm) {
        this.atkArm = atkArm;
    }

    public int getDefArm() {
        return defArm;
    }

    public void setDefArm(int defArm) {
        this.defArm = defArm;
    }

    public int getCityId() {
        return cityId;
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

    public int getLastOccupyTime() {
        return lastOccupyTime;
    }

    public int getNextAtkTime() {
        return nextAtkTime;
    }

    public void setNextAtkTime(int nextAtkTime) {
        this.nextAtkTime = nextAtkTime;
    }


    /**
     * 创建bossNpc的势力
     *
     * @param npcId 配置id
     * @param curHp 当前血量
     * @return Force
     */
    private Force createBossNpcForce(int npcId, int curHp) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        Force force = new Force(attrData, npc.getArmType(), npc.getLine(), npcId);
        force.hp = curHp;
        force.totalLost = force.maxHp - force.hp; // 总损兵
        int tmpTotalLost = force.totalLost;
        // 计算当前是第几排,从0开始
        int curLine = 0;// 当前第几排
        int cnt = 4;//防止死循环
        while (tmpTotalLost >= force.lead && cnt-- > 0) {
            curLine++;
            tmpTotalLost -= force.lead;
        }
        force.count = force.lead - tmpTotalLost;// 本排兵剩余数量
        force.curLine = curLine;
        return force;
    }

    /**
     * 序列化
     *
     * @return SerBerlinCityInfo
     */
    public SerializePb.SerBerlinCityInfo ser() {
        SerializePb.SerBerlinCityInfo.Builder builder = SerializePb.SerBerlinCityInfo.newBuilder();
        statusTime.forEach((key, value) -> builder.addStatusTime(PbHelper.createTwoIntPb(key, value)));
        cityDef.forEach(force ->
                builder.addCityDef(CommonPb.Force.newBuilder().setNpcId(force.id).setHp(force.hp).setCurLine(force.curLine))
        );
        roleQueue.forEach(berlinForce ->
                builder.addRoleQueue(berlinForce.ser())
        );
        builder.setCamp(getCamp());
        builder.setAtkArm(getAtkArm());
        builder.setDefArm(getDefArm());
        builder.setCityId(getCityId());
        builder.setPos(getPos());
        builder.setLastOccupyTime(getLastOccupyTime());
        builder.setNextAtkTime(getNextAtkTime());
        return builder.build();
    }


    /**
     * 反序列化
     *
     * @param berlinCity 对象
     */
    public BerlinCityInfo(SerializePb.SerBerlinCityInfo berlinCity) {
        this();
        berlinCity.getStatusTimeList().forEach(e ->
                this.statusTime.put(e.getV1(), e.getV2())
        );
        berlinCity.getCityDefList().forEach(force ->
                this.cityDef.addLast(createBossNpcForce(force.getNpcId(), force.getHp()))
        );
        berlinCity.getRoleQueueList().forEach(berlinForce ->
                this.roleQueue.add(new BerlinForce(new AttrData(berlinForce.getAttrData()), berlinForce.getArmType(),
                        berlinForce.getTotalCount(), berlinForce.getLead(), berlinForce.getId(),
                        berlinForce.getAtkOrDef(), berlinForce.getAddMode(), berlinForce.getAddTime(), berlinForce.getCamp(), berlinForce.getOwnId(),
                        berlinForce.getIntensifyLv(), berlinForce.getEffect(), berlinForce.getImmediatelyTime()))
        );

        this.camp = berlinCity.getCamp();
        this.atkArm = berlinCity.getAtkArm();
        this.defArm = berlinCity.getDefArm();
        this.cityId = berlinCity.getCityId();
        this.pos = berlinCity.getPos();
        this.lastOccupyTime = berlinCity.getLastOccupyTime();
        this.nextAtkTime = berlinCity.getNextAtkTime();
    }

    /**
     * 清除并初始化柏林相关城池数据
     * @param sameCamp 是否相同阵营
     * @param sBerlinWar 柏林城池配置
     */
    void clearAndInit(boolean sameCamp, StaticBerlinWar sBerlinWar) {
        List<CityHero> formList;
        if (this.getCamp() == Constant.Camp.NPC) {
            formList = sBerlinWar.getFirstFormList();
        } else {
            formList = sBerlinWar.getFormList();
        }
        this.setCityId(sBerlinWar.getKeyId());
        this.setPos(sBerlinWar.getCityPos());
        this.setAtkArm(0);
        this.setDefArm(0);
        this.initCityDef(formList);
        if (sameCamp) {
            // 非-1就打
            this.setNextAtkTime(1);
        } else {
            this.setNextAtkTime(-1);
        }
        this.setLastOccupyTime(TimeHelper.getCurrentSecond());
        this.getStatusTime().clear();
    }
}
