package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticEquipTurnplateConf;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
* @ClassName: EquipTurnplat
* @Description: 装备转盘活动
* @author chenqi
* @date 2018年8月30日
*
 */
public class EquipTurnplat extends Activity {

    /**
     * 免费刷新剩余次数,每天转点前根据VIP等级刷新次数
     */
    private int refreshCount;

    /**
     * 金币抽奖次数
     */
    private int goldCnt;

    /**
     * 特殊道具中奖次数 的下标
     */
    private Set<Integer> winCnt = new HashSet<>();

    /**
     * 特殊道具存在StatusCnt中的999索引位置
     */
    public static final int SPECIAL_SORT = 999;

    public EquipTurnplat(ActivityBase activityBase, int begin, Player player) {
        super(activityBase, begin);
        this.setRefreshCount(getFreeCountByVip(player.lord.getVip()));
    }

    /**
     * 反序列化
     * @param dbActivity
     */
    public EquipTurnplat(SerializePb.DbActivity dbActivity) {
        super(dbActivity);
        CommonPb.DbEquipTurnplat turnplat = dbActivity.getEquipTurnplat();
        setRefreshCount(turnplat.getRefreshCount());
        setGoldCnt(turnplat.getSpecialCnt());
        this.winCnt.addAll(turnplat.getWinCntList());
    }

    /**
     * 序列化
     * 
     * @return
     */
    public CommonPb.DbEquipTurnplat ser() {
        CommonPb.DbEquipTurnplat.Builder builder = CommonPb.DbEquipTurnplat.newBuilder();
        builder.setRefreshCount(refreshCount);
        builder.setSpecialCnt(goldCnt);
        builder.addAllWinCnt(winCnt);
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
        clearEquipTurnplat(getFreeCountByVip(player.lord.getVip()));
        return true;
    }

    /**
     * 当前特殊碎片数量
     * 
     * @param sortId
     * @return
     */
    public int currentSpecialCnt() {
        Long val = getStatusCnt().get(SPECIAL_SORT);
        return val == null ? 0 : val.intValue();
    }

    /**
     * 清除装备转盘数据
     * 
     * @param freeCnt
     */
    private void clearEquipTurnplat(int freeCnt) {
        LogUtil.debug("装备转盘 clearEquipTurnplat");
        refreshCount = freeCnt;
        goldCnt = 0;
        winCnt.clear();
    }

    /**
     * 根据vip获取免费次数
     * 
     * @param vip
     * @return
     */
    private int getFreeCountByVip(int vip) {
        StaticEquipTurnplateConf conf = StaticActivityDataMgr.getEquipTurnPlateListByActId(getActivityId()).get(0);
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
        LogUtil.debug("装备转盘-转点根据vip刷新免费次数, roleId:", player.roleId, ", freeCnt:", freeCountByVip);
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

}
