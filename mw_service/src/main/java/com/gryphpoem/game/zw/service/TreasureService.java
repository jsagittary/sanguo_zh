package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1.GetTreasureRs;
import com.gryphpoem.game.zw.pb.GamePb1.TreasureOpenRs;
import com.gryphpoem.game.zw.pb.GamePb1.TreasureTradeRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.p.ResourceMult;
import com.gryphpoem.game.zw.resource.domain.p.Treasure;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasure;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 聚宝盆
 * 
 * @author tyler
 *
 */
@Service
public class TreasureService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;

    /**
     * 获取聚宝盆信息
     * 
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetTreasureRs getTreasure(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Treasure treasure = player.treasure;
        GetTreasureRs.Builder builder = GetTreasureRs.newBuilder();
        if (treasure == null || treasure.getIdStatus().size() == 0) {
            randomTreasure(player);
            treasure = player.treasure;
            treasure.setUpdTime(TimeHelper.getCurrentSecond());
        }
        refreshShop(player);
        // 隔天判断
        if (!treasure.getIdStatus().isEmpty()) {
            for (Entry<Integer, Integer> kv : treasure.getIdStatus().entrySet()) {
                builder.addIdStatus(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
            }
        }

        checkRed(treasure);
        builder.setEndTime(treasure.getEndTime());
        builder.setStatus(treasure.getStatus());
        builder.setResTime(treasure.getResTime());
        builder.setRed(treasure.isRed());
        return builder.build();
    }

    /**
     * 聚宝盆CD时间计算 推送消息
     */
    public void treasureCdTimer() {
       /* int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().forEach(p -> {
            if (p.treasure != null && now > p.treasure.getEndTime()
                    && !p.hasPushRecord(String.valueOf(PushConstant.TREASURE_CD_COMPLETE))) {
                p.putPushRecord(PushConstant.TREASURE_CD_COMPLETE, PushConstant.PUSH_HAS_PUSHED);
                PushMessageUtil.pushMessage(p.account, PushConstant.TREASURE_CD_COMPLETE);
            }
        });
*/
    }

    /**
     * 刷新每日
     * 
     * @param player
     */
    void refreshShop(Player player) {
        Treasure treasure = player.treasure;
        int nowDay = TimeHelper.getCurrentDay();
        int lastDay = TimeHelper.getDay(treasure.getUpdTime());
        LogUtil.debug(player.roleId + ",聚宝盆上次刷新时间=" + treasure.getUpdTime() + ",now=" + nowDay + ",status="
                + treasure.getIdStatus());
        if (nowDay != lastDay) {
            treasure.setEndTime(TimeHelper.getCurrentSecond()); // 刷新CD
            // 如果未购买或者只翻牌的需要刷新
            if (treasure.getStatus() == 0) {
                randomTreasure(player);
                treasure.setUpdTime(TimeHelper.getCurrentSecond());
            } else {
                // 如果已购买则只调整顺序
                treasure.setUpdTime(TimeHelper.getCurrentSecond());
                treasure.setStatus(0);
                // 打乱排序
                breakRank(treasure);
            }
        }
    }

    /**
     * 打乱顺序(忽略9号位)
     * 
     * @param treasure
     */
    private void breakRank(Treasure treasure) {
        LinkedHashMap<Integer, Integer> newStatus = new LinkedHashMap<>();
        int[] a = { 1, 2, 3, 4, 5, 6, 7, 8 };
        int nTemp;
        for (int i = 0; i < 8; i++) {
            int nPos = (int) (Math.random() * 10 % 8);
            nTemp = a[i];
            a[i] = a[nPos];
            a[nPos] = nTemp;
        }
        Object[] keys = treasure.getIdStatus().keySet().toArray();
        for (int index : a) {
            Integer key = (Integer) keys[index - 1];
            newStatus.put(key, treasure.getIdStatus().get(key));
        }
        Integer key = (Integer) keys[9 - 1];
        newStatus.put(key, treasure.getIdStatus().get(key));
        treasure.setIdStatus(newStatus);
    }

    /**
     * 随机取出聚宝盆奖励(乱序)
     * 
     * @param player
     */
    private void randomTreasure(Player player) {
        Treasure treasure = player.treasure;
        if (treasure == null) {
            treasure = new Treasure();
            player.treasure = treasure;
        }
        treasure.getIdStatus().clear();
        processRandom(treasure, player.lord.getLevel(), StaticBuildingDataMgr.getTypeTreasureMap(1), 7, false);
        processRandom(treasure, player.lord.getLevel(), StaticBuildingDataMgr.getTypeTreasureMap(2), 1, false);
        processRandom(treasure, player.lord.getLevel(), StaticBuildingDataMgr.getTypeTreasureMap(3), 1, true);
        // 打乱排序
        breakRank(treasure);
    }

    /**
     * 随机处理
     * 
     * @param treasure
     * @param roleLv 需要角色等级
     * @param map 随机池
     * @param num 随机几个
     */
    private void processRandom(Treasure treasure, int roleLv, Map<Integer, StaticTreasure> map, int num, boolean open) {
        Map<Integer, StaticTreasure> aMap = new HashMap<>();
        int totalWight = 0;
        for (StaticTreasure staticTreasure : map.values()) {
            if (staticTreasure.getLv() > roleLv) {
                continue;
            }
            totalWight += staticTreasure.getPro();
            aMap.put(staticTreasure.getId(), staticTreasure);
        }

        int cnt = 0;
        int random = 0;
        int temp = 0;
        int totalCnt = 0;
        while (true) {
            if (totalWight <= 0) {
                break;
            }
            random = RandomHelper.randomInSize(totalWight);
            temp = 0;
            int key = 0;
            for (StaticTreasure s : aMap.values()) {
                temp += s.getPro();
                if (temp >= random) {
                    treasure.getIdStatus().put(s.getId(), open ? 1 : 0);
                    totalWight -= s.getPro();
                    key = s.getId();
                    cnt++;
                    if (cnt >= num) {
                        return;
                    }
                    break;
                }
            }
            aMap.remove(key);
            totalCnt++;
            if (totalCnt > 80) {
                break;
            }
        }
    }

    /**
     * 聚宝盆开启
     * 
     * @param roleId
     * @param id
     * @param buy
     * @return
     * @throws MwException
     */
    public TreasureOpenRs treasureOpen(Long roleId, int id, boolean buy) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Treasure treasure = player.treasure; 
        int now = TimeHelper.getCurrentSecond();
        TreasureOpenRs.Builder builder = TreasureOpenRs.newBuilder();
        if (buy) {
            if (treasure.getStatus() == 1) {
                throw new MwException(GameError.TREASURE_HAS_BUY.getCode(), "聚宝盆已购买, roleId:", roleId);
            }
            Integer key = (Integer) treasure.getIdStatus().keySet().toArray()[id - 1];
            treasure.getIdStatus().put(key, 1);

            StaticTreasure staticTreasure = StaticBuildingDataMgr.getTreasureMap(key);
            rewardDataManager.checkAndSubPlayerRes(player, staticTreasure.getCost(), AwardFrom.TREASURE_OPEN);
            treasure.setStatus(1);
            builder.addAllAward(rewardDataManager.addAwardDelaySync(player, staticTreasure.getReward(), null,
                    AwardFrom.TREASURE_OPEN));
            playerDataManager.createRoleOpt(player, Constant.OptId.id_4, builder.getAward(0).getType() + "",
                    builder.getAward(0).getId() + "", builder.getAward(0).getCount() + "");
            // 明天预览
            randomTreasure(player);
        } else {
            if (treasure.getEndTime() > 0 && treasure.getEndTime() > now) {
                throw new MwException(GameError.TREASURE_TIME.getCode(), "聚宝盆打开CD中, roleId:", roleId);
            }
            //判断触发次数
            int maxStatus = Constant.TRIGGER_GIFT_TREASURE_OPEN_PROBABILITY.get(0).get(1);
            int val = player.getMixtureDataById(PlayerConstant.MARKET_TRIGGER_TIMES);
            //概率
            int probability = Constant.TRIGGER_GIFT_TREASURE_OPEN_PROBABILITY.get(0).get(0);
            if (val<maxStatus && RandomHelper.isHitRangeIn10000(probability)){
                //翻牌触发礼包
                activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_TREASURE_OPEN, player);
                //更新次数
                player.setMixtureData(PlayerConstant.MARKET_TRIGGER_TIMES,val+1);
            }
            
            treasure.setEndTime(now + Constant.TREASURE_OPEN_TIME);
            Integer key = (Integer) treasure.getIdStatus().keySet().toArray()[id - 1];
            treasure.getIdStatus().put(key, 1);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_TREASURE_OPEN_CNT, 1);
            taskDataManager.updTask(player, TaskType.COND_TREASURE, 1);
        }

        if (treasure.getIdStatus().size() > 0) {
            for (Entry<Integer, Integer> kv : treasure.getIdStatus().entrySet()) {
                builder.addIdStatus(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
            }
        }
        // 移除推送
        // player.removePushRecord(PushConstant.TREASURE_CD_COMPLETE);
        builder.setGold(player.lord.getGold());
        builder.setEndTime(treasure.getEndTime());
        builder.setStatus(treasure.getStatus());
        return builder.build();
    }

    /**
     * 资源兑换
     * 
     * @param roleId
     * @param costId
     * @param gainId
     * @return
     */
    public TreasureTradeRs treasureTrade(Long roleId, int costId, int gainId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Treasure treasure = player.treasure;
        if (treasure == null) {
            treasure = new Treasure();
            player.treasure = treasure;
        }
        checkRed(treasure);
        int now = TimeHelper.getCurrentSecond();
        if (treasure.isRed()) {
            throw new MwException(GameError.TREASURE_TIME.getCode(), "资源兑换冷却中, roleId:", roleId);
        }

        Resource resource = player.resource;
        long out = 0;
        ResourceMult resourceMult = buildingDataManager.getResourceMult(player);
        if (costId == AwardType.Resource.ELE) {
            out = resourceMult.getElec();
        } else if (costId == AwardType.Resource.FOOD) {
            out = resourceMult.getFood();
        } else if (costId == AwardType.Resource.OIL) {
            out = resourceMult.getOil();
        }
        rewardDataManager.subResource(player, costId, out, AwardFrom.TREASURE_TRADE);// , "资源兑换"

        LogUtil.debug(roleId + ",资源兑换out=" + out + ",gain=" + out / 2);
        TreasureTradeRs.Builder builder = TreasureTradeRs.newBuilder();
        rewardDataManager.addAward(player, AwardType.RESOURCE, gainId, (int) (out / 2), AwardFrom.TREASURE_TRADE);
        builder.setAward(PbHelper.createAwardPb(AwardType.RESOURCE, gainId, (int) (out / 2)));

        //v0:单次兑换减少的CD时间, v1:兑换累计时间增加的上限
        int[] values = seasonTalentService.getSeasonTalentEffect503Value(player);
        treasure.setResTime((Math.max(treasure.getResTime(), now)) + Constant.TREASURE_TRADE_TIME - values[0] -
                seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_614));// Constant.TREASURE_TRADE_TIME
        if (treasure.getResTime() - now > Constant.TREASURE_TRADE_MAX_TIME + values[1]) {// Constant.TREASURE_TRADE_MAX_TIME
            treasure.setRed(true);
        }

        builder.setResource(PbHelper.createCombatPb(resource));
        builder.setResTime(treasure.getResTime());
        builder.setRed(treasure.isRed());
        return builder.build();
    }

    /**
     * 兑换状态检查
     * 
     * @param treasure
     */
    private void checkRed(Treasure treasure) {
        int now = TimeHelper.getCurrentSecond();
        if (now >= treasure.getResTime()) {
            treasure.setRed(false);
        }
    }
}
