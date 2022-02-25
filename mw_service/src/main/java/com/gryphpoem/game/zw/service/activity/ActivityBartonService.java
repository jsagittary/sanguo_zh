package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.GamePb4.ActBartonBuyRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetActBartonRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.ActBarton;
import com.gryphpoem.game.zw.resource.domain.p.ActBartonItem;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBarton;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by pengshuo on 2019/4/12 17:26
 * <br>Description: 巴顿活动*改
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
@Service
public class ActivityBartonService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;
    /** 初始折扣 */
    private final static int INIT_DISCOUNT = 100;
    /** 最大折扣 */
    private final static int MAX_DISCOUNT = 50;
    /** 每次购买物品折扣 */
    private final static int INIT_STEP_DISCOUNT = 10;
    /** 获取 */
    private final static int GET = 1;
    /** 刷新 */
    private final static int FRESH = 2;

    /**
     * 获取活动数据
     * @param activityId
     * @param lordId
     * @param fresh
     * @return
     * @throws MwException
     */
    public GetActBartonRs getActBartonRs(int activityId,long lordId,int fresh)throws MwException{
        if(fresh == GET){
            return get(activityId,lordId);
        } else if(fresh == FRESH){
            return fresh(activityId,lordId);
        }
        throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "参数错误 roleId:",lordId);
    }

    /**
     * 获取活动数据
     * @param activityId activityId
     * @param lordId lordId
     * @return GetActBartonRs
     * @throws MwException MwException
     */
    private GetActBartonRs get(int activityId,long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_BARTON_DISCOUNT);
        if(activityBase == null || activityBase.getPlan() == null || activityBase.getPlan().getActivityId() != activityId){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        int keyId = activityBase.getPlan().getKeyId();
        int begin = TimeHelper.getDay(activityBase.getBeginTime());
        ActBarton actBarton = Optional.ofNullable(player.actBarton.get(keyId)).map(barton -> {
            // 如果 两次活动开始时间不同，清除活动数据
            if(barton.getBeginTime() != begin){
                barton = new ActBarton();
            }
            return barton;
        }).orElse(new ActBarton());
        if(actBarton.getBartonItems().isEmpty()){
            // 格子数据
            Map<Integer, ActBartonItem> items = bartonItemsRefresh(activityId);
            if (CheckNull.isEmpty(items)) {
                throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(), "活动配置有错误");
            }
            actBarton.setBartonItems(items);
            actBarton.setActivityId(activityId);
            actBarton.setBeginTime(begin);
            actBarton.setRefreshCount(ActParamConstant.BARTON_TOTAL_REFRESH_COUNT);
            // 存放
            player.actBarton.put(keyId,actBarton);
        }
        GetActBartonRs.Builder builder = GetActBartonRs.newBuilder();
        builder.setActivityId(activityId);
        actBarton.getBartonItems().values().forEach(e->builder.addItems(e.dser()));
        // 剩余刷新次数
        builder.setRefreshCount(actBarton.getRefreshCount());
        // 刷新所需金币
        builder.setRefreshGold(ActParamConstant.BARTON_REFRESH_COST_GOLD);
        return builder.build();
    }

    /**
     * 刷新奖励
     * @param activityId
     * @param lordId
     * @return
     */
    private GetActBartonRs fresh(int activityId, long lordId) throws MwException{
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_BARTON_DISCOUNT);
        if(activityBase == null || activityBase.getPlan() == null || activityBase.getPlan().getActivityId() != activityId){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        int keyId = activityBase.getPlan().getKeyId();
        ActBarton actBarton = Optional.ofNullable(player.actBarton.get(keyId)).orElse(new ActBarton());
        // 检测是否初始化过物品
        if (CheckNull.isEmpty(actBarton.getBartonItems())) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动没有初始化 roleId:",lordId);
        }
        // 是否最大化
        if(actBarton.getRefreshCount() <= 0){
            throw new MwException(GameError.REFRESH_CNT_IS_OVER.getCode(),
                    "刷新次数已达到上限 roleId ", lordId, "count:", actBarton.getRefreshCount());
        }
        // 扣除金币
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY,
                AwardType.Money.GOLD, ActParamConstant.BARTON_REFRESH_COST_GOLD, AwardFrom.ACT_BARTON_GOLD_FRESH);
        // 可刷新次数-1
        actBarton.setRefreshCount(actBarton.getRefreshCount() - 1);
        // 刷新格子操作 已经招募过,或者信物数量达到,都不允许购买信物
        Map<Integer,ActBartonItem> items = bartonItemsRefresh(activityId);
        actBarton.setBartonItems(items);
        GetActBartonRs.Builder builder = GetActBartonRs.newBuilder();
        // 每个格子的数据
        actBarton.getBartonItems().values().forEach(e->builder.addItems(e.dser()));
        // 刷新剩余次数
        builder.setRefreshCount(actBarton.getRefreshCount());
        // 当前付费刷新的费用
        builder.setRefreshGold(
                ActParamConstant.BARTON_REFRESH_COST_GOLD
        );
        return builder.build();
    }

    /**
     * 商品购买
     * @param activityId
     * @param lordId
     * @param keyId
     * @return
     * @throws MwException
     */
    public ActBartonBuyRs actBartonBuy(int activityId, long lordId, int keyId) throws MwException{
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_BARTON_DISCOUNT);
        if(activityBase == null || activityBase.getPlan() == null || activityBase.getPlan().getActivityId() != activityId){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        int activityKeyId = activityBase.getPlan().getKeyId();
        ActBarton actBarton = Optional.ofNullable(player.actBarton.get(activityKeyId)).orElse(new ActBarton());
        if(actBarton.getBartonItems().isEmpty()){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "戒指活动没有初始化 roleId:", lordId);
        }
        ActBartonItem item = actBarton.getBartonItems().get(keyId);
        if (item.isPurchased()) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "该商品已经购买过");
        }
        // 检测并扣除金币
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                item.getDiscountPrice(), AwardFrom.ACT_BARTON_BUY);
        //奖励物品
        Award award = rewardDataManager.addAwardSignle(player, item.getAward(), AwardFrom.ACT_BARTON_BUY);
        if (CheckNull.isNull(award)) {
            throw new MwException(GameError.ACTIVITY_CONFIG_ERR.getCode(),"巴顿活动商品购买失败 roleId:",lordId,",keyId:",keyId);
        }
        // 设置已购买
        item.setPurchased(true);
        // 对其他商品进行打折
        actBarton.getBartonItems().values().forEach(e->{
            // 原价
            int price = e.getPrice();
            // 折扣
            int discount = e.getDiscount() - INIT_STEP_DISCOUNT;
            // 最大折扣不超过50%
            if(discount < MAX_DISCOUNT){
                discount = MAX_DISCOUNT;
            }
            int discountPrice = (int) ((discount * 1.0f) / 100 * (price * 1.0f));
            e.setDiscountPrice(discountPrice);
            e.setDiscount(discount);
        });
        // 返回
        ActBartonBuyRs.Builder builder = ActBartonBuyRs.newBuilder();
        builder.setAward(award);
        actBarton.getBartonItems().values().forEach(e->builder.addItems(e.dser()));
        return builder.build();
    }

    /**
     * 刷新格子数据
     * @param activityId
     * @return
     */
    private Map<Integer, ActBartonItem> bartonItemsRefresh(int activityId){
        List<StaticActBarton> list = StaticActivityDataMgr.getActBartonList(activityId);
        if(list == null || list.isEmpty()){
            return null;
        }
        Map<Integer, ActBartonItem> items = new LinkedHashMap<>(6);
        for (StaticActBarton e : list) {
            // 权重随机
            List<Integer> award = RandomUtil.getRandomByWeight(e.getAwardList(), 3, false);
            if (CheckNull.isEmpty(award)) {
                // 格式错误
                LogUtil.error("巴顿活动awardList格式错误",e.getAwardList());
                return null;
            }
            ActBartonItem item = new ActBartonItem();
            item.setActivityId(activityId);
            item.setKeyId(e.getKeyId());
            item.setCond(e.getCond());
            item.setAward(award);
            item.setPrice(e.getPrice());
            item.setDiscount(INIT_DISCOUNT);
            item.setDiscountPrice(e.getPrice());
            item.setPurchased(false);
            items.put(e.getKeyId(),item);
        }
        return items;
    }

}
