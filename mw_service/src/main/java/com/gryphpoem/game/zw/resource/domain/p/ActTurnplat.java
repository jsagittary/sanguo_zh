package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticTurnplateConf;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-06-06 10:57
 * @description: 幸运转盘活动
 * @modified By:
 */
public class ActTurnplat extends Activity {

    /**
     * 免费刷新剩余次数,每天转点前根据VIP等级刷新次数
     */
    private int refreshCount;

    /**
     * 金币抽奖次数
     */
    private int goldCnt;

    /**
     * 累计抽奖次数
     */
    private int cnt;

    /**
     * 当日抽将次数
     */
    private int todayCnt;

    /**
     * 特殊道具中奖次数
     */
    private Set<Integer> winCnt = new HashSet<>();
    /**
     * 211活动专用
     */
    private List<Set<Integer>> winCnt211 = new ArrayList<>();

    /**
     * 活动状态map
     */
    private Map<Integer, Integer> statusMap = new HashMap<>();

    /**
     * 特殊道具存在StatusCnt中的999索引位置
     */
    public static final int SPECIAL_SORT = 999;
    /**
     * 207保底下标
     */
    public static final int TURNTABLE_BOTTOM_GUARANTEE_INDEX = -1000;

    public ActTurnplat(ActivityBase activityBase, int begin, Player player) {
        super(activityBase, begin);
        if (activityBase.getStep0() == ActivityConst.OPEN_AWARD)
            return;
        this.setRefreshCount(getFreeCountByVip(player.lord.getVip()));
    }

    /**
     * 反序列化
     * 
     * @param dbActivity
     */
    public ActTurnplat(SerializePb.DbActivity dbActivity) {
        super(dbActivity);
        CommonPb.DbActTurnplat turnplat = dbActivity.getTurnplat();
        setRefreshCount(turnplat.getRefreshCount());
        setGoldCnt(turnplat.getSpecialCnt());
        for (CommonPb.TwoInt twoInt : turnplat.getStatusList()) {
            this.statusMap.put(twoInt.getV1(), twoInt.getV2());
        }
        this.winCnt.addAll(turnplat.getWinCntList());
        setCnt(turnplat.getCnt());
        setTodayCnt(turnplat.getTodayCnt());
        for(CommonPb.ActTurnplatNewYearWinCnt newYearWinCnt : turnplat.getNewYearWinCntList()){
            Set<Integer> set_ = new HashSet<>();
            set_.addAll(newYearWinCnt.getWinCntList());
            this.winCnt211.add(set_);
        }
    }

    /**
     * 序列化
     * 
     * @return
     */
    public CommonPb.DbActTurnplat ser() {
        CommonPb.DbActTurnplat.Builder builder = CommonPb.DbActTurnplat.newBuilder();
        builder.setRefreshCount(refreshCount);
        builder.setSpecialCnt(goldCnt);
        builder.addAllWinCnt(winCnt);
        for (Map.Entry<Integer, Integer> en : this.statusMap.entrySet()) {
            builder.addStatus(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
        }
        builder.setCnt(cnt);
        builder.setTodayCnt(todayCnt);
        for(int i=0;i<winCnt211.size();i++){
            builder.addNewYearWinCnt(buildActTurnplatNewYearWinCnt(i,winCnt211.get(i)));
        }
        return builder.build();
    }

    private CommonPb.ActTurnplatNewYearWinCnt buildActTurnplatNewYearWinCnt(int idx, Set<Integer> set){
        CommonPb.ActTurnplatNewYearWinCnt.Builder builder = CommonPb.ActTurnplatNewYearWinCnt.newBuilder();
        builder.setIdx(idx);
        builder.addAllWinCnt(set);
        return builder.build();
    }

    /**
     * 是否是重开活动
     */
    @Override
    public boolean isReset(int begin, Player player) {
        boolean reset = super.isReset(begin, player);
        if (!reset) {
            return false;
        }
        clearTurnplat(getFreeCountByVip(player.lord.getVip()));
        ChatDataManager chatDataManager = DataResource.ac.getBean(ChatDataManager.class);
        if(this.getActivityType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                || this.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE
                || this.getActivityType() == ActivityConst.ACT_SEASON_TURNPLATE
                || this.getActivityType() == ActivityConst.ACT_MAGIC_TREASURE_WARE){
            chatDataManager.getActivityChat(this.getActivityType()).clear();
        }
        return true;
    }

    /**
     * 当前特殊碎片数量
     * 
     * @return
     */
    public int currentSpecialCnt() {
        Long val = getStatusCnt().get(SPECIAL_SORT);
        return val == null ? 0 : val.intValue();
    }

    /**
     * 清除幸运转盘数据
     * 
     * @param freeCnt
     */
    private void clearTurnplat(int freeCnt) {
        LogUtil.debug("幸运转盘 clearTurnplat");
        refreshCount = freeCnt;
        goldCnt = 0;
        cnt = 0;
        todayCnt = 0;
        winCnt.clear();
        winCnt211.clear();
        statusMap.clear();
    }

    /**
     * 根据vip获取免费次数
     * 
     * @param vip
     * @return
     */
    private int getFreeCountByVip(int vip) {
        StaticTurnplateConf conf = StaticActivityDataMgr.getActTurnPlateListByActId(getActivityId()).get(0);
        if (!CheckNull.isNull(conf)) {
            List<List<Integer>> collect = conf.getFreeCount().stream().filter(e -> vip >= e.get(0) && vip <= e.get(1))
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(collect)) {
                List<Integer> freeCnt = collect.get(0);
                if (!CheckNull.isEmpty(freeCnt)) {
                    return freeCnt.get(2);
                }
            }
        }
        return 0;
    }

    /**
     * 转点根据vip刷新免费次数
     * 
     * @param player
     */
    public void refreshFreeCnt(Player player) {
        int freeCountByVip = getFreeCountByVip(player.lord.getVip());
        setRefreshCount(freeCountByVip);
        setTodayCnt(0);
        LogUtil.debug("转点根据vip刷新免费次数, roleId:", player.roleId, ", freeCnt:", freeCountByVip);
    }

    @Override
    public Map<Integer, Integer> getStatusMap() {
        return statusMap;
    }

    public int getGoldCnt() {
        return goldCnt;
    }

    public void setGoldCnt(int goldCnt) {
        this.goldCnt = goldCnt;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public void setRefreshCount(int refreshCount) {
        this.refreshCount = refreshCount;
    }

    public void subRefreshCount() {
        this.refreshCount--;
    }

    public Set<Integer> getWinCnt() {
        return winCnt;
    }

    public void setWinCnt(Set<Integer> winCnt) {
        this.winCnt = winCnt;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int getTodayCnt() {
        return todayCnt;
    }

    public void setTodayCnt(int todayCnt) {
        this.todayCnt = todayCnt;
    }

    public List<Set<Integer>> getWinCnt211() {
        return winCnt211;
    }

    public void setWinCnt211(List<Set<Integer>> winCnt211) {
        this.winCnt211 = winCnt211;
    }

    public int getSetIdxByCnt(int goldCnt){
        int idx = 0;
        for(Set<Integer> set : winCnt211){
            for(int cnt : set){
                if(cnt == goldCnt){
                    return idx;
                }
            }
            idx ++;
        }
        return -1;
    }
}
