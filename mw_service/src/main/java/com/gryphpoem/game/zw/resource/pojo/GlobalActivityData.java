package com.gryphpoem.game.zw.resource.pojo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.GamePb4.GetLuckyPoolRankRs;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;
import com.gryphpoem.game.zw.resource.domain.p.SimpleRank;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalActivityData extends Activity {
    private static final int MAX_RANK_NUM = 1000000; // 排行榜上限

    private int goal;// 全服活动值记录
    private int equipTurLuckNums;// 全服装备转盘活动抽奖次数记录
    private int lastSaveTime;// 最终更新的时间
    private AtomicLong topupa = new AtomicLong(0);// 军团充值
    private AtomicLong topupb = new AtomicLong(0);
    private AtomicLong topupc = new AtomicLong(0);
    private byte[] params = null;// 可能为空

    public Map<Integer, LinkedList<ActRank>> getRanks() {
        return ranks;
    }

    // 玩家排行榜数据 key: activityType,不会进行存储,在服务器初始化时,会重新计算赋值
    private Map<Integer, LinkedList<ActRank>> ranks = new ConcurrentHashMap<Integer, LinkedList<ActRank>>();

    private List<SimpleRank> simpleRanks = new LinkedList<>();
    private GetLuckyPoolRankRs.Builder simpleRankPb = GetLuckyPoolRankRs.newBuilder();

    /**
     * 是否是重开活动
     */
    @Override
    public boolean isReset(int begin, Player player) {
        boolean reset = super.isReset(begin, player);
        if (!reset) {
            return false;
        }
        ranks.clear();
        clearGlobal();
        setEquipTurLuckNums(0);

        simpleRanks.clear();
        simpleRankPb = GetLuckyPoolRankRs.newBuilder();
        return true;
    }

    public void clearGlobal() {
        LogUtil.debug("全服活动 clearGlobal =");
        topupa.set(0); // 军团充值
        topupb.set(0);
        topupc.set(0);

        goal = 0;
        params = null;
        if (getActivityType() == ActivityConst.ACT_LUCKY_POOL) {
            goal = ActParamConstant.LUCKY_POOL_1.get(0);
        }
    }

    public GlobalActivityData(ActivityBase activityBase, int begin) {
        super(activityBase, begin);
    }

    public GlobalActivityData(GlobalActivity globalActivity) throws InvalidProtocolBufferException {
        this.setActivityType(globalActivity.getActivityType());
        this.setBeginTime(globalActivity.getActivityTime()); // 活动开始时间
        this.setEndTime(globalActivity.getRecordTime()); // 活动结束时间
        this.goal = globalActivity.getGoal();
        this.params = globalActivity.getParams();
        this.equipTurLuckNums = globalActivity.getEquipTurLuckNums();
        this.setActivityKeyId(globalActivity.getActKeyId());
        this.setStatusMap(new HashMap<Integer, Integer>());
        this.setTopupa(new AtomicLong(globalActivity.getTopupa()));
        this.setTopupb(new AtomicLong(globalActivity.getTopupb()));
        this.setTopupc(new AtomicLong(globalActivity.getTopupc()));
        dserParams();
        LogUtil.debug("init GlobalActivity=" + globalActivity);

    }

    public void dserParams() throws InvalidProtocolBufferException {
        if (params == null) {
            return;
        }
        switch (getActivityType()) {
            case ActivityConst.ACT_LUCKY_POOL:
                simpleRankPb = GetLuckyPoolRankRs.parseFrom(params).toBuilder();
                break;
            default:
                break;
        }
    }

    public void serParams() {
        switch (getActivityType()) {
            case ActivityConst.ACT_LUCKY_POOL:
                if (simpleRankPb != null) {
                    params = simpleRankPb.build().toByteArray();
                }
                break;
            default:
                break;
        }
    }

    public AtomicLong getTopupa() {
        return topupa;
    }

    public void setTopupa(AtomicLong topupa) {
        this.topupa = topupa;
    }

    public AtomicLong getTopupb() {
        return topupb;
    }

    public void setTopupb(AtomicLong topupb) {
        this.topupb = topupb;
    }

    public AtomicLong getTopupc() {
        return topupc;
    }

    public void setTopupc(AtomicLong topupc) {
        this.topupc = topupc;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getEquipTurLuckNums() {
        return equipTurLuckNums;
    }

    public void setEquipTurLuckNums(int equipTurLuckNums) {
        this.equipTurLuckNums = equipTurLuckNums;
    }

    public int getLastSaveTime() {
        return lastSaveTime;
    }

    public void setLastSaveTime(int lastSaveTime) {
        this.lastSaveTime = lastSaveTime;
    }

    /**
     * 根据阵营,获取阵营积分
     * 
     * @param camp
     * @return
     */
    public long getCampValByCamp(int camp) {
        long val = 0;
        if (camp == 1) {
            val = getTopupa().get();
        } else if (camp == 2) {
            val = getTopupb().get();
        } else if (camp == 3) {
            val = getTopupc().get();
        }
        return val;
    }

    public long addCampValByCamp(int camp,int add){
        long val = getCampValByCamp(camp);
        if (camp == 1) {
            val = topupa.addAndGet(add);
        } else if (camp == 2) {
            val = topupb.addAndGet(add);
        } else if (camp == 3) {
            val = topupc.addAndGet(add);
        }
        return val;
    }

    public AtomicLong getCampValByCampAtomic(int camp) {
        AtomicLong val = null;
        if (camp == 1) {
            val = getTopupa();
        } else if (camp == 2) {
            val = getTopupb();
        } else if (camp == 3) {
            val = getTopupc();
        }
        return val;
    }

    /**
     * 通过类型获取排行榜数据
     * 
     * @param type
     * @return
     */
    public LinkedList<ActRank> getPlayerRanks(int type) {
        return this.getPlayerRanks(null, type);
    }

    /**
     * 通过玩家和排行类型获取排行数据
     * 
     * @param player
     * @param type
     * @return
     */
    public LinkedList<ActRank> getPlayerRanks(Player player, int type) {
        type = this.getRefreshRankType(player, type);
        // 如果没有刷新则刷新数据
        LinkedList<ActRank> playerRanks = ranks.get(type);
        if (playerRanks == null) {
            playerRanks = new LinkedList<ActRank>();
            ranks.put(type, playerRanks);
        }
        return playerRanks;
    }

    /**
     * 获取真实的排行榜的类型KEY
     * 
     * @param player
     * @param type
     * @return
     */
    public int getRefreshRankType(Player player, int type) {
        if (player == null) {
            return type;
        }
        if (type == ActivityConst.ACT_CAMP_RANK) {
            // 每个阵营的排行KEY
            return getCampRank(player.lord.getCamp(), type);
        }
        return type;
    }

    /**
     * 阵容排行，必须有三个独立的排行榜KEY
     * 
     * @param camp
     * @param type
     * @return
     */
    public static int getCampRank(int camp, int type) {
        if (type == ActivityConst.ACT_CAMP_RANK) {
            // 每个阵营的排行KEY
            type = 10000000 + 100000 * camp + type;
        }
        return type;
    }

    /**
     * 获取玩家在某个排名榜的名次
     * 
     * @param type 默认0
     * @param lordId
     * @return
     */
    public ActRank getPlayerRank(Player player, int type, long lordId) {
        LinkedList<ActRank> playerRanks = getPlayerRanks(player, type);
        if (playerRanks.size() == 0) {
            return null;
        }
        int rank = 1;
        Iterator<ActRank> it = playerRanks.iterator();
        while (it.hasNext()) {
            ActRank next = it.next();
            if (next.getLordId() == lordId) {
                next.setRank(rank);
                return next;
            }
            rank++;
        }
        return null;
    }

    public LinkedList<ActRank> getPlayerRankList(int type, int page) {
        LinkedList<ActRank> rs = new LinkedList<ActRank>();
        LinkedList<ActRank> playerRanks = getPlayerRanks(type);
        if (playerRanks.size() == 0) {
            return rs;
        }
        int[] pages = { page * 20, (page + 1) * 20 };
        Iterator<ActRank> it = playerRanks.iterator();
        int count = 0;
        while (it.hasNext()) {
            ActRank next = it.next();
            if (count >= pages[0]) {
                rs.add(next);
            }
            if (++count >= pages[1]) {
                break;
            }
        }
        return rs;
    }

    /**
     * 添加排行
     * 
     * @param player
     * @param value
     * @param maxRank {前十名：则为10}
     * @param order 0 从小到大, 1从大到小
     * @param activityType
     */
    public void addPlayerRank(Player player, Long value, int maxRank, int order, int activityType, int time) {
        LinkedList<ActRank> playerRanks = getPlayerRanks(player, activityType);
        addRank(player, playerRanks, player.lord.getLordId(), activityType,
                this.getRefreshRankType(player, activityType), value, maxRank, order, time);
    }

    /**
     * 添加排行 ,默认是从大到小
     * 
     * @param player
     * @param value
     * @param activityType
     * @param time
     */
    public void addPlayerRank(Player player, Long value, int activityType, int time) {
        // 计算排行榜的容量 ,避免内存浪费
        int rankCapacity = MAX_RANK_NUM;
        // ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        // if (activityBase != null) {
        // int capacity = StaticActivityDataMgr.getRankCapacityByActivityId(activityBase.getActivityId());
        // if (capacity != 0) rankCapacity = capacity;
        // }
        addPlayerRank(player, value, rankCapacity, ActivityConst.DESC, activityType, time);
    }

    /**
     * 
     * @Title: addEquipTurLuckNums
     * @Description: 装备转盘-增加全服抽奖次数
     * @return void
     */
    public void addEquipTurLuckNums() {
        equipTurLuckNums++;
    }

    /**
     * 
     * @Title: addInitEquipTurLuckNums
     * @Description: 装备转盘-服务器重启时，初始化全服金币抽奖次数
     * @param num
     * @return void
     */
    public void addInitEquipTurLuckNums(int num) {
        equipTurLuckNums += num;
    }

    /**
     * 
     * @param lordId
     * @param type 活动类型
     * @param value
     * @param maxRank 最大排名值 {前十名：则为10}
     * @param order 0 从小到大, 1从大到小
     * @param rankTime 上榜时间，如果传入的该值<=0，默认为当前时间
     */
    private void addRank(Player player, LinkedList<ActRank> rankList, long lordId, int type, int converType, Long value,
            int maxRank, int order, int rankTime) {
        int time = rankTime;
        if (time <= 0) {
            time = TimeHelper.getCurrentSecond();
        }
        // 检查是否达到上榜条件
        if (checkActRankCond(type, value)) return;

        if (type == ActivityConst.ACT_CAMP_RANK) {
            // 开服阵营特殊处理
            type = converType;
        }

        int size = rankList.size();
        if (size == 0) {// 排名为空 直接进入排行
            rankList.add(new ActRank(lordId, type, value, time));
            return;
        } else if (maxRank != 0 && size >= maxRank) {// 排名已满,则比较最末名
            ActRank actRank = rankList.getLast();
            if (order == ActivityConst.ASC) {
                if (actRank.getRankValue() < value) {// 升序比最末名大,则不进入排名
                    return;
                }
            } else if (order == ActivityConst.DESC) {
                if (actRank.getRankValue() > value) {// 降序比最末名小,则不进入排名
                    return;
                }
            }
        }
        boolean flag = false;
        for (ActRank next : rankList) {// 查询自己是否 已在排行中 并 更新数据
            if (order == ActivityConst.ASC) {
                if (next.getLordId() == lordId) {
                    if (next.getRankValue() > value) {
                        next.setRankValue(value);
                        next.setRankTime(time);// 更新排行信息时，更新上榜时间，当排行数据相同时，将通过比较最后更新时间来排行
                    }
                    flag = true;
                    break;
                }
            } else if (order == ActivityConst.DESC) {
                if (next.getLordId() == lordId) {
                    if (next.getRankValue() < value) {
                        next.setRankValue(value);
                        next.setRankTime(time);
                    }
                    flag = true;
                    break;
                }
            }
        }

        if (!flag) {// 新晋排名玩家
            rankList.add(new ActRank(lordId, type, value, time));
        }

        if (order == ActivityConst.ASC) {// 升序排序
            rankList.sort(new PlayerRankAsc());
        } else if (order == ActivityConst.DESC) {// 降序
            rankList.sort(new PlayerRankDesc());
        }

        // 将超出排名的最末名删掉
        if (maxRank != 0 && rankList.size() > maxRank) {
            rankList.removeLast();
        }
    }

    /**
     * 检测排行活动的上榜条件
     * @param type
     * @param value
     * @return
     */
    private boolean checkActRankCond(int type, Long value) {
        // 排行榜上榜条件
        if (type == ActivityConst.ACT_LUCKY_TURNPLATE && value <= ActParamConstant.LUCKY_TRUNPLATE_RANKINGS_CONDITION) {// 幸运转盘上榜条件
            return true;
        } else if (type == ActivityConst.FAMOUS_GENERAL_TURNPLATE
                && value <= ActParamConstant.FAMOUS_GENERAL_TRUNPLATE_RANKINGS_CONDITION) {// 名将转盘上榜条件
            return true;
        } else if (type == ActivityConst.ACT_EQUIP_TURNPLATE
                && value <= ActParamConstant.EQUIP_TRUNPLATE_RANKINGS_CONDITION) {// 装备转盘上榜条件
            return true;
        } else if (type == ActivityConst.ACT_PAY_RANK && value <= ActParamConstant.PAY_RANKINGS_CONDITION) {// 充值排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_ARMY_RANK && value <= ActParamConstant.TROOPS_RANKINGS_CONDITION) {// 兵力排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_CAMP_BATTLE_RANK
                && value <= ActParamConstant.CITY_WAR_RANKINGS_CONDITION) {// 攻城战排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_CITY_BATTLE_RANK
                && value <= ActParamConstant.CAMP_WAR_RANKINGS_CONDITION) {// 营地战排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_PARTY_BUILD_RANK && value <= ActParamConstant.BUIDING_RANKINGS_CONDITION) {// 建设排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_REMOULD_RANK && value <= ActParamConstant.REFORM_RANKINGS_CONDITION) {// 改造排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_FORGE_RANK && value <= ActParamConstant.FORGE_RANKINGS_CONDITION) {// 打造排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_SUPPLY_RANK && value <= ActParamConstant.SUPPLY_RANKINGS_CONDITION) {// 补给排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_ORE_RANK && value <= ActParamConstant.ORE_RANKINGS_CONDITION) {// 矿石排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_PRESENT_GIFT_RANK && value <= ActParamConstant.GIFT_RANKINGS_CONDITION) {// 礼物排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_CAMP_RANK && value <= ActParamConstant.ACT_CAMP_RANK) {// 阵营排行上榜条件
            return true;
        } else if ((type == ActivityConst.ACT_PAY_RANK_NEW || type == ActivityConst.ACT_PAY_RANK_V_3 || type == ActivityConst.ACT_MERGE_PAY_RANK)
                && value <= ActParamConstant.PAY_RANKINGS_CONDITION_NEW) {// 新充值排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_CAMP_FIGHT_RANK
                && value <= ActParamConstant.ACT_CAMP_FIGHT_RANK_JOIN_COND.get(1)) {// 阵营战力排行上榜条件
            return true;
        } else if (type == ActivityConst.ACT_CONSUME_GOLD_RANK && value <= ActParamConstant.ACT_CONSUME_GOLD_RANK) {// 消耗排行上榜所需额度
            return true;
        } else if (type == ActivityConst.ACT_TUTOR_RANK && value <= ActParamConstant.ACT_TUTOR_CONDITION_RANK) {
            return true;
        } else if (type == ActivityConst.ACT_ROYAL_ARENA && value <= ActParamConstant.ACT_ROYAL_ARENA_RANK.get(0).get(0)) {
            return true;
        } else if(type == ActivityConst.ACT_DIAOCHAN || type == ActivityConst.ACT_SEASON_HERO){
            ActivityDiaoChanService activityDiaoChanService = DataResource.ac.getBean(ActivityDiaoChanService.class);
            int limit = activityDiaoChanService.getRankLimit(type);
            if(value < limit){
                return true;
            }
        } else if (type == ActivityConst.ACT_MAGIC_TREASURE_WARE) {
            return false;
        }
        //最后的else是用于检查貂蝉活动的每日排行
        else if(type > 100000000){
            ActivityDiaoChanService activityDiaoChanService = DataResource.ac.getBean(ActivityDiaoChanService.class);
            int limit = activityDiaoChanService.getDayRankLimit(type);
            if(value < limit){
                return true;
            }
        }
        return false;
    }

    private int checkDiaoChanDayRankType(int type){
        ActivityDiaoChanService activityDiaoChanService = DataResource.ac.getBean(ActivityDiaoChanService.class);
        for(int i =1;i<=50;i++){
            int tmp = activityDiaoChanService.getDayRankKey(type,i);
            if(tmp == type){
                return i;
            }
        }
        return -1;
    }

    public GlobalActivity copyData() {
        GlobalActivity entity = new GlobalActivity();
        entity.setActivityType(this.getActivityType());// 活动ID
        entity.setActivityTime(this.getBeginTime());// 该活动开启时间
        entity.setRecordTime(this.getEndTime());// 记录时间
        entity.setGoal(this.goal);
        entity.setEquipTurLuckNums(this.equipTurLuckNums);
        entity.setTopupa(topupa.get());
        entity.setTopupb(topupb.get());
        entity.setTopupc(topupc.get());
        entity.setParams(this.simpleRankPb.build().toByteArray());
        entity.setActKeyId(this.getActivityKeyId());
        // LogUtil.debug("copyData GlobalActivity=" + entity);
        return entity;
    }

    public void addLuckyPoolRank(SimpleRank rank) {
        simpleRanks.add(rank);
        simpleRankPb.addRanks(rank.serialize());
        if (simpleRanks.size() > 200) {
            simpleRanks.subList(0, 50).clear();

            simpleRankPb = GetLuckyPoolRankRs.newBuilder();
            simpleRanks.forEach(sr -> simpleRankPb.addRanks(sr.serialize()));
        }
    }

    public GetLuckyPoolRankRs.Builder getSimpleRankPb(long date) {
        if (date == 0) {
            return simpleRankPb;
        } else {
            GetLuckyPoolRankRs.Builder part = GetLuckyPoolRankRs.newBuilder();
            simpleRanks.forEach(sr -> {
                if (sr.getDate() > date) {
                    part.addRanks(sr.serialize());
                }
            });
            return part;
        }
    }
}

class PlayerRankDesc implements Comparator<ActRank> {
    @Override
    public int compare(ActRank o1, ActRank o2) {
        if (o1.getRankValue() < o2.getRankValue()) {
            return 1;
        } else if (o1.getRankValue() > o2.getRankValue()) {
            return -1;
        } else {
            // if (o1.getLordId() > o2.getLordId()) {
            // return 1;
            // } else if (o1.getLordId() < o2.getLordId()) {
            // return -1;
            // }
            // 数值相等的情况下，不再比较id，比较上榜时间，先上榜排在前面
            if (o1.getRankTime() > o2.getRankTime()) {
                return 1;
            } else if (o1.getRankTime() < o2.getRankTime()) {
                return -1;
            }
        }
        return 0;
    }
}

class PlayerRankAsc implements Comparator<ActRank> {
    @Override
    public int compare(ActRank o1, ActRank o2) {
        if (o1.getRankValue() > o2.getRankValue()) {
            return 1;
        } else if (o1.getRankValue() < o2.getRankValue()) {
            return -1;
        } else {
            // if (o1.getLordId() < o2.getLordId()) {
            // return -1;
            // }
            // 数值相等的情况下，不再比较id，比较上榜时间，先上榜排在前面
            if (o1.getRankTime() > o2.getRankTime()) {
                return 1;
            } else if (o1.getRankTime() < o2.getRankTime()) {
                return -1;
            }
        }
        return 0;
    }
}

class PartyRankDesc implements Comparator<ActRank> {
    @Override
    public int compare(ActRank o1, ActRank o2) {
        if (o1.getRankValue() < o2.getRankValue()) {
            return 1;
        } else if (o1.getRankValue() > o2.getRankValue()) {
            return -1;
        } else {
            // if (o1.getPartyId() > o2.getPartyId()) {
            // return 1;
            // } else if (o1.getPartyId() < o2.getPartyId()) {
            // return -1;
            // }
            // 数值相等的情况下，不再比较id，比较上榜时间，先上榜排在前面
            if (o1.getRankTime() > o2.getRankTime()) {
                return 1;
            } else if (o1.getRankTime() < o2.getRankTime()) {
                return -1;
            }
        }
        return 0;
    }
}

class PartyRankAsc implements Comparator<ActRank> {
    @Override
    public int compare(ActRank o1, ActRank o2) {
        if (o1.getRankValue() > o2.getRankValue()) {
            return 1;
        } else if (o1.getRankValue() < o2.getRankValue()) {
            return -1;
        } else {
            // if (o1.getPartyId() < o2.getPartyId()) {
            // return -1;
            // }
            // 数值相等的情况下，不再比较id，比较上榜时间，先上榜排在前面
            if (o1.getRankTime() > o2.getRankTime()) {
                return 1;
            } else if (o1.getRankTime() < o2.getRankTime()) {
                return -1;
            }
        }
        return 0;
    }

}