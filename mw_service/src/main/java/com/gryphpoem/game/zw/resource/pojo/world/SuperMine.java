package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticSuperMine;
import com.gryphpoem.game.zw.resource.pojo.army.Army;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author QiuKun
 * @ClassName SuperMine.java
 * @Description 超级矿点
 * @date 2018年7月13日
 */
public class SuperMine implements WorldEntity {

    public static int SEQ_ID = 0;
    /**
     * 生产中
     */
    public static final int STATE_PRODUCED = 0;
    /**
     * 停产中
     */
    public static final int STATE_STOP = 1;
    /**
     * 重置中地图不显示
     */
    public static final int STATE_RESET = 2;
    /**
     * 最大采集人数
     */
    public static final int MAX_COLLECT_SIZE = 4;

    private int seqId; // 矿点的编号
    private int camp;// 阵营(不会改变)
    private int pos;// 地图上的位置
    private int state;// 状态
    private int configId; // 对应配置的id
    private int convertRes;// 已结算矿点
    private int capacity;// 总容量
    private int cityId;// 所属某个城池id
    private int nextTime;// 下一次事件触发时间

    private List<SuperGuard> collectArmy = new LinkedList<>(); // 采集部队
    private List<Army> helpArmy = new LinkedList<>();// 助防部队

    private List<Integer> battleIds = new LinkedList<>();// 发生战斗id

    public SuperMine(int capacity) {
        this.seqId = ++SEQ_ID;
        this.capacity = capacity;
    }

    public SuperMine(CommonPb.SuperMine ser) {
        this.seqId = ser.getSeqId();
        this.camp = ser.getCamp();
        this.pos = ser.getPos();
        this.state = ser.getState();
        this.configId = ser.getConfigId();
        this.convertRes = ser.getConvertRes();
        this.capacity = ser.getCapacity();
        this.cityId = ser.getCityId();
        this.nextTime = ser.getNextTime();
        for (CommonPb.SuperGuard sgSer : ser.getCollectArmyList()) {
            collectArmy.add(new SuperGuard(sgSer, this));
        }
        for (CommonPb.Army serArmy : ser.getHelpArmyList()) {
            helpArmy.add(new Army(serArmy));
        }
        for (int battleId : ser.getBattleIdsList()) {
            battleIds.add(battleId);
        }
    }

    /**
     * 加入采集
     *
     * @param player
     * @param army
     * @param now
     */
    public void joinCollect(Player player, Army army, int now) {
        // 部队状态修改
        army.setState(ArmyConstant.ARMY_STATE_COLLECT);
        army.setHeroState(player, ArmyConstant.ARMY_STATE_COLLECT);
        // 添加到采集队列中
        collectArmy.add(SuperGuard.createSuperGuard(army, this, now));
        reCalcAllCollectArmyTime(now);
        if (this.state == STATE_STOP) {// 停产状态
            army.setEndTime(-1);
        }
    }

    /**
     * 重新计算所有矿点采集时间
     *
     * @param now
     * @return true重新计算了返回时间
     */
    public boolean reCalcAllCollectArmyTime(int now) {
        StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineById(configId);
        if (sSm == null) {
            return false;
        }
        int collectedTime = 0; // 已采集的时间
        int canCollectTime = 0;// 还可以采集时间
        for (SuperGuard sg : collectArmy) {
            canCollectTime += sg.furtherCollectTime(now);
            collectedTime += sg.calcCollectedTime(now);
        }
        int collectedRes = (int) Math.floor((collectedTime * 1.0 / Constant.HOUR) * sSm.getSpeed());// 已采集的数量
        int canCollectRes = (int) Math.floor((canCollectTime * 1.0 / Constant.HOUR) * sSm.getSpeed());// 采集将领还未来可以采集的数量
        int remainingCollectRes = capacity - convertRes - collectedRes; // 剩余的数量
        if (canCollectRes > remainingCollectRes) { // 还可以采集 大于 余量 说明不够采集;需要把余下的采集数量进行平分
            int size = collectArmy.size();
            if (size <= 0) return false;
            int canRes = (int) Math.ceil(remainingCollectRes / size); // 平分后的余量
            // 换算成时间
            double speedSec = sSm.getSpeed() / 3600.0;// 每秒的速度
            int durationTime = (int) Math.ceil(canRes / speedSec);// 未来还可以采集多少时间
            collectArmy.forEach(sg -> {
                sg.setCanMaxCollectTime(now, durationTime);
                sg.setArmyTime(now, durationTime);
            });// 设置部队时间
            return true;
        } else {// 余量足够采集
            collectArmy.forEach(sg -> {
                sg.setCanMaxCollectTimeEnoughRes();
                sg.setArmyTimeInEnoughRes(now);
            });

        }
        return false;
    }

    /**
     * 计算矿点剩余量
     *
     * @param now
     * @return
     */
    public int calcCollectRemaining(int now) {
        int remaining = 0;
        if (state == STATE_PRODUCED || state == STATE_STOP) { // 只有停产和重置状态才会有余量
            StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineById(configId);
            if (sSm == null) {
                return 0;
            }
            int allTime = 0;
            for (SuperGuard sg : collectArmy) {
                allTime += sg.calcCollectedTime(now);
            }
            int calcRes = (int) Math.floor((allTime * 1.0 / Constant.HOUR) * sSm.getSpeed()); // 时间 * 速度
            remaining = capacity - convertRes - calcRes;
            if (remaining <= 0) {
                remaining = 0;
            }
        }
        return remaining;
    }

    /**
     * 设置停产状态
     */
    public void setStopState(int now) {
        if (this.state == STATE_PRODUCED) {
            this.state = STATE_STOP;
            for (SuperGuard sg : collectArmy) {
                sg.stopState(now);
            }
            // 设置时间
            this.nextTime = now + Constant.SUPERMINE_STOP_TO_RESET_CD;
        }
    }

    /**
     * 恢复生产状态 停产 -> 生产 (只有重新争夺据点才会有)
     *
     * @param now
     */
    public void setStopToProducedState(int now) {
        if (this.state == STATE_STOP) { // 停产 -> 生产
            for (SuperGuard sg : collectArmy) {
                sg.reProducedState(now);
            }
            this.state = STATE_PRODUCED;
            this.nextTime = 0;
            reCalcAllCollectArmyTime(now);
        }
    }

    /**
     * 恢复生产状态 重置 -> 生产
     *
     * @param now
     * @param pos
     * @param cityId
     */
    public void setResetToProducedState(int now, int pos, StaticSuperMine sSm, int cityId) {
        if (this.state == STATE_RESET) { // 重置 -> 生产
            this.state = STATE_PRODUCED;
            this.pos = pos;
            this.configId = sSm.getMineId();
            this.cityId = cityId;
            this.capacity = sSm.getReward().get(0).get(2);
            this.convertRes = 0;
            this.nextTime = 0;
            this.collectArmy.clear();
            this.helpArmy.clear();
            this.battleIds.clear();
        }
    }

    /**
     * 设置成重置状态
     *
     * @param now
     */
    public void setResetState(int now) {
        this.state = STATE_RESET;
        this.nextTime = now + Constant.SUPERMINE_RESET_TO_PRODUCED_CD;
        this.pos = 0;
        this.configId = 0;
        this.convertRes = 0;
        this.capacity = 0;
        this.cityId = 0;
    }

    /**
     * 创建一个新的矿点
     *
     * @param camp
     * @param cityId
     * @param pos
     * @param sSm
     * @return
     */
    public static SuperMine newSuperMine(int camp, int cityId, int pos, StaticSuperMine sSm) {
        SuperMine sm = new SuperMine(sSm.getReward().get(0).get(2));
        sm.setCamp(camp);
        sm.setCityId(cityId);
        sm.setPos(pos);
        sm.setConfigId(sSm.getMineId());
        sm.setState(STATE_PRODUCED);
        return sm;
    }

    /**
     * 是否能在地图上显示
     *
     * @return
     */
    public boolean isMapShow() {
        return pos > 0 && (this.state == STATE_PRODUCED || this.state == STATE_STOP);
    }

    /**
     * 是否是重置状态
     *
     * @return
     */
    public boolean isResetState() {
        return this.state == STATE_RESET;
    }

    /**
     * 是否在生产中
     *
     * @return
     */
    public boolean isProduceState() {
        return this.state == STATE_PRODUCED;
    }

    /**
     * 是否在停产状态
     *
     * @return
     */
    public boolean isStopState() {
        return this.state == STATE_STOP;
    }

    /**
     * 当有采集部队离开是 需要结算一下资源数
     *
     * @param res
     */
    public void addConvertRes(int res) {
        if (res > 0) {
            this.convertRes += res;
        }
    }

    /**
     * 移除采集部队
     *
     * @param roleId
     * @param keyId
     * @return
     */
    public boolean removeCollectArmy(long roleId, int keyId) {
        Iterator<SuperGuard> it = collectArmy.iterator();
        while (it.hasNext()) {
            SuperGuard sg = it.next();
            if (sg.getArmy().getLordId() == roleId && sg.getArmy().getKeyId() == keyId) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * 移除驻防部队
     *
     * @param roleId
     * @param keyId  部队keyId
     * @return
     */
    public boolean removeHelpArmy(long roleId, int keyId) {
        Iterator<Army> it = helpArmy.iterator();
        while (it.hasNext()) {
            Army am = it.next();
            if (am.getLordId() == roleId && am.getKeyId() == keyId) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取防守方兵力
     *
     * @return
     */
    public int defArmyCnt() {
        int cnt = 0;
        for (Army army : helpArmy) {
            cnt += army.getHero().get(0).getCount();
        }

        for (SuperGuard sg : collectArmy) {
            cnt += sg.getArmy().getHero().get(0).getCount();
        }
        return cnt;
    }

    /*-----------------一堆get set方法-----------------*/

    public int getCamp() {
        return camp;
    }

    public int getSeqId() {
        return seqId;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getNextTime() {
        return nextTime;
    }

    public void setNextTime(int nextTime) {
        this.nextTime = nextTime;
    }

    public List<Army> getHelpArmy() {
        return helpArmy;
    }

    public List<SuperGuard> getCollectArmy() {
        return collectArmy;
    }

    public int getConvertRes() {
        return convertRes;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Integer> getBattleIds() {
        return battleIds;
    }

    @Override
    public String toString() {
        return "SuperMine [seqId=" + seqId + ", camp=" + camp + ", pos=" + pos + ", state=" + state + ", configId="
                + configId + ", convertRes=" + convertRes + ", capacity=" + capacity + ", cityId=" + cityId
                + ", collectArmy=" + collectArmy + ", helpArmy=" + helpArmy + ", nextTime=" + nextTime + "]";
    }

}
