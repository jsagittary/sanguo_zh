package com.gryphpoem.game.zw.resource.pojo.world.battlepass;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticBattlePassDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticBattlePassLv;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 战令的个人数据
 *
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-04 11:29
 */
public class BattlePassPersonInfo {

    /**
     * 普通奖励领取 key
     */
    public static final int AWARD_NORMAL_KEY = 0;
    /**
     * 战令进阶领取 key
     */
    public static final int AWARD_SPECIAL_KEY = 1;

    /**
     * 对应global里面的唯一Id, 用于清除个人数据的
     */
    private int key;

    /**
     * 之前对应配置表里的Key
     * 后来改成对应s_battlepass_plan里的planId
     */
    private int staticKey;

    /**
     * 战令的任务进度, 这里的数据会被清除
     */
    private Map<Integer, battlePassTask> passTaskMap = new HashMap<>();
    /**
     * 战令的奖励领取进度
     * key: lv
     * value:
     * <key 0 普通奖励 key | 1 战令进阶奖励 , 0 未领取 | 1 已领取>
     */
    private Map<Integer, Map<Integer, Integer>> passAwardStat = new HashMap<>();

    /**
     * 经验
     */
    private int exp;
    /**
     * 战令等级
     */
    private int lv;
    /**
     * 是否购买进阶
     * 0 未购买 1 已购买
     */
    private int advanced;

    /**
     * 是否领取了每日经验
     * 每日清除 0 未领取 1 已领取
     */
    private int dailyExp;

    /**
     * 清除数据
     *
     * @param key    战令的唯一id
     * @param planId 战令的模板id
     */
    public void clearData(int key, int planId) {
        // 记录本次开放的key
        this.key = key;
        this.staticKey = planId;
        this.passTaskMap.clear();
        this.passAwardStat.clear();
        this.exp = 0;
        this.lv = 0;
        this.advanced = 0;
    }


    /**
     * 升级
     *
     * @return 升级后的等级
     */
    public int levelUp() {
        return ++this.lv;
    }


    /**
     * 清除任务数据
     *
     * @param needRefreshKey 需要清除的任务key
     */
    public void refreshTaskAndData(List<Integer> needRefreshKey) {
        if (CheckNull.isEmpty(needRefreshKey)) {
            return;
        }
        // StaticBattlePassDataMgr.getTasksByPlanKey(this.staticKey)有可能空指针
        Optional.ofNullable(StaticBattlePassDataMgr.getTasksByPlanKey(this.staticKey)).ifPresent(sPassTasks -> {
            needRefreshKey.forEach(key -> {
                if (key == GlobalBattlePass.REFRESH_TIME_KEY_1) {
                    this.dailyExp = 0;
                }
                // 清除任务记录
                sPassTasks.stream()
                        .filter(sPassTask -> sPassTask.getType() == key)
                        .forEach(sPassTask -> {
                            Optional.ofNullable(this.passTaskMap.remove(sPassTask.getId()))
                                    .ifPresent(removeTask -> {
                                        int status = removeTask.getStatus();
                                        if (status == TaskType.TYPE_STATUS_FINISH) {
                                            Optional.ofNullable(StaticBattlePassDataMgr.getTaskById(this.staticKey, removeTask.getTaskId()))
                                                    .ifPresent(sTaskConf -> {
                                                        // 添加经验
                                                        addExp(sTaskConf.getAward());
                                                    });
                                        }
                                    });
                        });
            });
        });

    }

    /**
     * 战令加经验
     *
     * @param addExp 添加的经验
     * @return 返回实际增加经验
     */
    public int addExp(int addExp) {
        // 最大的等级
        int maxLv = StaticBattlePassDataMgr.getMaxLvByPlanKey(this.staticKey);

        int add = 0;
        while (addExp > 0 && this.getLv() < maxLv) {
            StaticBattlePassLv curLvConf = StaticBattlePassDataMgr.getLvAwardByPlanKey(this.staticKey, this.getLv());
            if (!CheckNull.isNull(curLvConf)) {
                int need = curLvConf.getNeedExp();
                if (need > 0) {
                    if (this.getExp() + addExp >= need) {
                        add += need - this.getExp();
                        addExp -= need - this.getExp();
                        this.setExp(need);
                        this.levelUp();
                    } else {
                        add += addExp;
                        this.setExp(this.getExp() + addExp);
                        addExp = 0;
                    }
                } else {
                    // 设置经验为本级经验上限
                    StaticBattlePassLv lvConf = StaticBattlePassDataMgr.getLvAwardByPlanKey(this.staticKey, this.getLv());
                    if (!CheckNull.isNull(lvConf)) {
                        int max = lvConf.getNeedExp();
                        add += max - this.getExp();
                        this.setExp(max);
                        break;
                    }

                }
            }
        }
        return add;
    }

    /**
     * 领取当前可以领取的战令奖励
     *
     * @param player
     * @return 合并后的奖励
     */
    public List<List<Integer>> receiveBPAward(Player player) {
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        // 奖励的领取状态
        Map<Integer, Map<Integer, Integer>> passAwardStat = this.getPassAwardStat();
        // 当前的战令等级
        int curLv = this.getLv();
        // 是否进阶
        boolean isAdvanced = this.getAdvanced() == 1;
        // plan配置表里的静态key
        int staticKey = this.getStaticKey();
        // 所有的奖励
        List<List<Integer>> allAward = new ArrayList<>();
        Stream.iterate(1, i -> ++i).limit(curLv).forEach(lv -> {
            // 获取当前等级领取的map
            Map<Integer, Integer> statMap = passAwardStat.computeIfAbsent(lv, map -> new HashMap<>());
            // 配置
            StaticBattlePassLv sPassLv = StaticBattlePassDataMgr.getLvAwardByPlanKey(staticKey, lv);
            if (CheckNull.isNull(sPassLv)) {
                return;
            }
            boolean award = false;
            // 没有领取普通奖励
            if (statMap.getOrDefault(BattlePassPersonInfo.AWARD_NORMAL_KEY, 0) == 0) {
                allAward.addAll(sPassLv.getNormalAward());
                statMap.put(BattlePassPersonInfo.AWARD_NORMAL_KEY, 1);
                award = true;
            }
            // 特殊奖励没有领取
            if (isAdvanced && statMap.getOrDefault(BattlePassPersonInfo.AWARD_SPECIAL_KEY, 0) == 0) {
                allAward.addAll(sPassLv.getSpecialAward());
                statMap.put(BattlePassPersonInfo.AWARD_SPECIAL_KEY, 1);
                award = true;
            }
            if (award) {
                activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_BATTLE_PASS_TASK_CNT, 1);
            }
        });
        // 合并奖励
        return RewardDataManager.mergeAward(allAward);
    }

    public int getDailyExp() {
        return dailyExp;
    }

    public void setDailyExp(int dailyExp) {
        this.dailyExp = dailyExp;
    }

    public int getStaticKey() {
        return staticKey;
    }

    public int getKey() {
        return key;
    }

    public Map<Integer, battlePassTask> getPassTaskMap() {
        return passTaskMap;
    }

    public void setPassTaskMap(Map<Integer, battlePassTask> passTaskMap) {
        this.passTaskMap = passTaskMap;
    }

    public Map<Integer, Map<Integer, Integer>> getPassAwardStat() {
        return passAwardStat;
    }

    public void setPassAwardStat(Map<Integer, Map<Integer, Integer>> passAwardStat) {
        this.passAwardStat = passAwardStat;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getAdvanced() {
        return advanced;
    }

    public void setAdvanced(int advanced) {
        this.advanced = advanced;
    }

    /**
     * 战令玩家个人数据序列化
     *
     * @return 序列化对象
     */
    public SerializePb.SerBattlePassPersonInfo ser() {
        SerializePb.SerBattlePassPersonInfo.Builder builder = SerializePb.SerBattlePassPersonInfo.newBuilder();
        builder.setKey(this.key);
        builder.setStaticKey(this.staticKey);
        builder.setExp(this.exp);
        builder.setLv(this.lv);
        builder.setAdvanced(this.advanced);
        builder.setDailyExp(this.dailyExp);
        if (!CheckNull.isEmpty(this.passTaskMap)) {
            builder.addAllTask(this.passTaskMap.values().stream().map(battlePassTask::ser).collect(Collectors.toList()));
        }
        if (!CheckNull.isEmpty(this.passAwardStat)) {
            this.passAwardStat.forEach((k, v) -> {
                SerializePb.SerPassAwardStat.Builder awardStat = SerializePb.SerPassAwardStat.newBuilder();
                awardStat.setLv(k);
                if (!CheckNull.isEmpty(v)) {
                    v.forEach((key, stat) -> awardStat.addAwardStat(PbHelper.createTwoIntPb(key, stat)));
                }
                builder.addAwardStat(awardStat.build());
            });
        }
        return builder.build();

    }

    /**
     * 反序列化对象数据
     *
     * @param ser 序列化对象
     */
    public void dser(SerializePb.SerBattlePassPersonInfo ser) {
        this.key = ser.getKey();
        this.staticKey = ser.getStaticKey();
        this.exp = ser.getExp();
        this.lv = ser.getLv();
        this.advanced = ser.getAdvanced();
        this.dailyExp = ser.getDailyExp();
        for (SerializePb.SerBattlePassTask serTask : ser.getTaskList()) {
            this.passTaskMap.put(serTask.getTaskId(), new battlePassTask(serTask));
        }
        for (SerializePb.SerPassAwardStat serPassAwardStat : ser.getAwardStatList()) {
            int lv = serPassAwardStat.getLv();
            Map<Integer, Integer> awardStatMap = this.passAwardStat.computeIfAbsent(lv, map -> new HashMap<>());
            for (CommonPb.TwoInt twoInt : serPassAwardStat.getAwardStatList()) {
                awardStatMap.put(twoInt.getV1(), twoInt.getV2());
            }
        }
    }
}