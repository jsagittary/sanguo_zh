package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;
import java.util.stream.Collectors;

public interface AbsRankActivityService {
    /**
     * 获取排行榜信息
     *
     * @param roleId
     * @param activityType
     * @return
     * @throws MwException
     */
    default CommonPb.ActRankInfo.Builder getActRank(long roleId, int activityType) throws MwException {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "获取排行榜错误，活动未开启(ActivityBase=null), roleId:", roleId, "activityType=" + activityType);
        }
        int actId = activityBase.getActivityId();

        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "获取排行榜错误, 活动未开启(Player.Activity=null) roleId:", roleId, "activityType=" + activityType);
        }

        GlobalActivityData gActDate = activityDataManager.getGlobalActivity(activityType);
        if (gActDate == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "排行活动未开启 roleId:", roleId);
        }
        List<StaticActAward> sActAward = getStaticActAwardList(actId, activityType, roleId);
        if (CheckNull.isEmpty(sActAward)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "排行活动未开启配置错误 roleId:", roleId, ", actId:", actId,
                    ", actType:", activityType);
        }

        CommonPb.ActRankInfo.Builder builder = CommonPb.ActRankInfo.newBuilder();
        addShowRankList(builder, getShowRankList(sActAward.size() + 1, activityType, actId, activity, sActAward, gActDate, player, builder));
        // 奖励
        int myRankSchedule = activityDataManager.getRankAwardSchedule(player, activityType);
        for (StaticActAward e : sActAward) {
            int keyId = e.getKeyId();
            int status = activity.getStatusMap().containsKey(keyId) ? 1 : 0;// 0 未领取奖励 ,1 已领取奖励
            builder.addActivityCond(PbHelper.createActivityCondPb(e, status, myRankSchedule));
        }
        addExtParam(gActDate);
        int awardTime = (int) (activityBase.getAwardBeginTime().getTime() / 1000);
        builder.setAwardTime(awardTime);
        return builder;
    }

    /**
     * 获取客户端显示的排行榜
     *
     * @param showSize
     * @param activityType
     * @param actId
     * @param activity
     * @param sActAward
     * @param gActDate
     * @param player
     * @return
     */
    default List<ActRank> getShowRankList(int showSize, int activityType, int actId, Activity activity, List<StaticActAward> sActAward, GlobalActivityData gActDate, Player player, CommonPb.ActRankInfo.Builder builder) {
        LinkedList<ActRank> rankList = gActDate.getPlayerRanks(player, activityType);
        ActRank myRank = gActDate.getPlayerRank(player, activityType, player.lord.getLordId());// 自己的排行, null说明压根没有排行
        StaticActAward myAward = null;// 自己所在的奖励档位, 如果为null说明在排行榜之外
        // 计算自己在第几档
        if (myRank != null) {
            myAward = StaticActivityDataMgr.findRankAward(actId, myRank.getRank());
        }

        List<ActRank> showRankList = new ArrayList<>();// 客户端显示的排行
        if (rankList.size() <= showSize) {
            // 1.当排行榜人数小于显示个数时
            for (int i = 0; i < rankList.size(); i++) {
                ActRank ar = rankList.get(i);
                ar.setRank(i + 1);
                showRankList.add(ar);
            }
            if (myRank == null) { // 没有档位说明压根没有加入排行
//                if (rankList.size() >= showSize) {// 移除最后一个
//                    showRankList.remove(rankList.size() - 1);
//                }
                myRank = new ActRank(player.lord.getLordId(), activityType,
                        activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0);
//                showRankList.add(new ActRank(player.lord.getLordId(), activityType,
//                        activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0));// 添加自己
            }
        } else {
            // 2. 排行榜人数足够显示时
            // 实际最多能显示的档位
            List<StaticActAward> sRealActAward = sActAward.stream().filter(saa -> rankList.size() >= saa.getParam().get(1))
                    .collect(Collectors.toList());
            Set<Integer> rankingSet = new HashSet<>(); // 去重使用
            if (myAward != null) {
                // 3 当自己有档位时在排行榜中
                for (StaticActAward saa : sRealActAward) {
                    if (saa.getParam().get(0) > myAward.getParam().get(0)) { // 在自己之前的档位显示档位的显示第一名
                        rankingSet.add(saa.getParam().get(1));
                    } else if (saa.getParam().get(0) < myAward.getParam().get(0)) {// 在自己之后的档位显示档位的显示最后一名
                        rankingSet.add(saa.getCond() > rankList.size() ? rankList.size() : saa.getCond());
                    } else {
                        rankingSet.add(myRank.getRank());// 与自己档位相等显示自己名次
                    }
                }
                for (int i = 1; rankingSet.size() < showSize; i++) {
                    rankingSet.add(i);
                }
                rankingSet.stream().sorted(Comparator.comparingInt(i -> i)).forEach(rank -> {
                    ActRank actRank = rankList.get(rank - 1);
                    actRank.setRank(rank);
                    showRankList.add(actRank);
                });
            } else {
                // 4 自己没有档位在排行榜
                for (StaticActAward saa : sRealActAward) {
                    rankingSet.add(saa.getCond() > rankList.size() ? rankList.size() : saa.getCond());
                }
                for (int i = 1; rankingSet.size() < showSize - 1; i++) { // showSize-1要改自己留个位置
                    rankingSet.add(i);
                }
                rankingSet.stream().sorted(Comparator.comparingInt(i -> i)).forEach(rank -> {
                    ActRank actRank = rankList.get(rank - 1);
                    actRank.setRank(rank);
                    showRankList.add(actRank);
                });
                if (CheckNull.isNull(myRank)) {
                    myRank = new ActRank(player.lord.getLordId(), activityType,
                            activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0);
                }
//                showRankList.add(myRank != null ? myRank
//                        : new ActRank(player.lord.getLordId(), activityType,
//                        activity.getStatusCnt().get(0) == null ? 0 : activity.getStatusCnt().get(0), 0)); // 无档位在最后,无档位当可能有名次
            }
        }

        if (Objects.nonNull(myRank))
            builder.setMyRank(PbHelper.createActRank(myRank, player.lord.getNick(),
                    player.lord.getCamp(), player.lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        return showRankList;
    }

    /**
     * 更新排行榜信息
     *
     * @param player
     * @param progress
     * @param param
     */
    static void updateRankList(Player player, long progress, Object... param) {
        Map<String, AbsRankActivityService> resultMap = DataResource.ac.getBeansOfType(AbsRankActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return;

        resultMap.values().forEach(service -> {
            service.upActRankSchedule(player, progress, param);
        });
    }

    static boolean isActRankAct(int actType) {
        Map<String, AbsRankActivityService> resultMap = DataResource.ac.getBeansOfType(AbsRankActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return false;

        for (AbsRankActivityService service : resultMap.values()) {
            if (service instanceof AbsActivityService) {
                AbsActivityService absService = (AbsActivityService) service;
                for (int type : absService.getActivityType()) {
                    if (type == actType)
                        return true;
                }
            }
        }

        return false;
    }

    static boolean loadActRankAct(ActivityBase ab, GlobalActivityData gAct) {
        Map<String, AbsRankActivityService> resultMap = DataResource.ac.getBeansOfType(AbsRankActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return false;

        for (AbsRankActivityService service : resultMap.values()) {
            if (CheckNull.isNull(service))
                continue;

            service.loadRank(ab, gAct);
        }

        return false;
    }

    /**
     * 获取对应活动增长到排行榜上的进度
     *
     * @param player
     * @param progress
     * @param param
     * @return
     */
    void upActRankSchedule(Player player, long progress, Object... param);

    /**
     * 获取配置表排行榜奖励信息
     *
     * @param actId
     * @param activityType
     * @param roleId
     * @return
     */
    List<StaticActAward> getStaticActAwardList(int actId, int activityType, long roleId);

    /**
     * 添加显示排行榜pb信息
     *
     * @param builder
     * @param showRankList
     */
    void addShowRankList(CommonPb.ActRankInfo.Builder builder, List<ActRank> showRankList);

    /**
     * 添加排行榜额外显示信息
     *
     * @param gActDate
     */
    void addExtParam(GlobalActivityData gActDate);

    /**
     * 默认加载排行榜方法
     *
     * @param e
     * @param gAct
     */
    default void loadRank(ActivityBase e, GlobalActivityData gAct) {
        int actType = gAct.getActivityType();
        if (e == null || e.getStep0() == ActivityConst.OPEN_CLOSE) {
            return;
        }
        for (Player player : DataResource.ac.getBean(PlayerDataManager.class).getPlayers().values()) {
            Activity act = DataResource.ac.getBean(ActivityDataManager.class).getActivityInfo(player, actType);
            if (CheckNull.isNull(act))
                continue;

            Long value = act.getStatusCnt().get(0); // 个人的排行榜进度或存在 0 的位置
            Long time = act.getStatusCnt().get(1);
            if (value != null && time != null) {
                int timeInt = time == null ? 0 : time.intValue();
                gAct.addPlayerRank(player, value, actType, timeInt); // 添加玩家
            }
        }
    }

    void sendSettleRankAward(Player player, int now, Activity activity);
}
