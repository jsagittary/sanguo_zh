package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.ActJob;
import com.gryphpoem.game.zw.quartz.jobs.FireworksJob;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticFireworks;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 烟花庆典
 *
 * @author xwind
 * @date 2021/12/17
 */
@Service
public class Year2022FireworkService extends AbsActivityService implements GmCmdService {

    private int[] actTypes = {ActivityConst.ACT_NEWYEAR_2022_FIREWORK};

    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ChatDataManager chatDataManager;

    public GamePb5.FireworkLetoffRs letOff(long roleId, int confId, int actType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = super.checkAndGetActivityBase(player, actType);
        if (!super.isOpenStage(activityBase)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "燃放烟花活动未开放", confId));
        }
        Activity activity = super.checkAndGetActivity(player, actType);
        StaticFireworks staticFireworks = StaticDataMgr.getFireworks(confId);
        if (Objects.isNull(staticFireworks)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "燃放烟花配置找不到", confId));
        }

        if (staticFireworks.getActivityId() != activityBase.getActivityId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "燃放烟花活动id不匹配", confId, activityBase.getActivityId()));
        }

        int limit = StaticDataMgr.fireworksLimit(activity.getActivityId());
        int used = activity.getSaveMap().getOrDefault(TimeHelper.getCurrentDay(), 0);
        if (limit == 0 || (limit > 0 && used >= limit)) {
            throw new MwException(GameError.FIREWORKS_TIMES_LIMIT.getCode(), GameError.err(roleId, "燃放烟花达到今日次数限制"));
        }

        AtomicReference<StaticArea> reference = new AtomicReference<>();
        globalDataManager.getGameGlobal().getAreaMap().entrySet().forEach(entry -> {
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(entry.getKey());
            if (Objects.nonNull(staticArea) && (entry.getValue().isOpen() || entry.getValue().canPass())) {
                if (Objects.isNull(reference.get())) {
                    reference.set(staticArea);
                } else {
                    if (reference.get().getOpenOrder() < staticArea.getOpenOrder()) {
                        reference.set(staticArea);
                    }
                }
            }
        });
        if (Objects.isNull(reference.get())) {
            throw new MwException(GameError.SERVER_EXCEPTION.getCode(), GameError.err(roleId, "燃放烟花获取当前最大开放区域报错"));
        }
        StaticCity maxCity;
        if (reference.get().getOpenOrder() == WorldConstant.AREA_ORDER_3) {
            maxCity = StaticWorldDataMgr.getMaxTypeCityByArea(reference.get().getArea());
        } else {
            maxCity = StaticWorldDataMgr.getMaxTypeCityByArea(player.lord.getArea());
        }

        rewardDataManager.checkAndSubPlayerRes(player, staticFireworks.getCost(), AwardFrom.FIRE_WORKS_LETOFF);
        activity.getSaveMap().merge(TimeHelper.getCurrentDay(), 1, Integer::sum);

        //跑马灯
        chatDataManager.sendSysChat(ChatConst.FIREWORKS_LETOFF1, player.pCampMailTime, 0, player.getCamp(), player.lord.getNick());

        LogLordHelper.commonLog("firework", AwardFrom.FIRE_WORKS_LETOFF, player, confId);

        List<Integer> cityList = new ArrayList<>();
        cityList.add(maxCity.getCityId());
        this.syncFireworkLetoff(actType, confId, cityList, player);

        GamePb5.FireworkLetoffRs.Builder resp = GamePb5.FireworkLetoffRs.newBuilder();
        resp.setActType(actType);
        resp.setInfo(buildFireworkInfo(activity));
        return resp.build();
    }

    private CommonPb.FireworkInfo buildFireworkInfo(Activity activity) {
        CommonPb.FireworkInfo.Builder builder = CommonPb.FireworkInfo.newBuilder();
        int limit = StaticDataMgr.fireworksLimit(activity.getActivityId());
        if (limit < 0) {
            builder.setTimes(-1);
        } else {
            int used = activity.getSaveMap().getOrDefault(TimeHelper.getCurrentDay(), 0);
            int left = limit - used;
            left = left < 0 ? 0 : left;
            builder.setTimes(left);
        }
        return builder.build();
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setFireworkInfo(buildFireworkInfo(activity));
        return builder;
    }

    @Override
    protected int[] getActivityType() {
        return actTypes;
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {
        this.systemLetoff(activityType, activityId);
    }

    public void systemLetoff(int activityType, int activityId) {
        int idx = RandomHelper.randomInSize(StaticDataMgr.getFireworksList(activityId).size());
        StaticFireworks staticFireworks = StaticDataMgr.getFireworksList(activityId).get(idx);
        Set<Area> area1s = new HashSet<>();
        Set<Area> area2s = new HashSet<>();
        Set<Area> area3s = new HashSet<>();
        globalDataManager.getGameGlobal().getAreaMap().entrySet().forEach(entry -> {
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(entry.getKey());
            if (Objects.nonNull(staticArea) && (entry.getValue().isOpen() || entry.getValue().canPass())) {
                if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
                    area1s.add(entry.getValue());
                } else if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {
                    area2s.add(entry.getValue());
                } else {
                    area3s.add(entry.getValue());
                }
            }
        });

        List<Integer> cityList = null;
        if (!area3s.isEmpty()) {
            cityList = area3s.stream().map(o -> {
                StaticCity staticCity = StaticWorldDataMgr.getMaxTypeCityByArea(o.getArea());
                return staticCity.getCityId();
            }).collect(Collectors.toList());
        } else if (!area2s.isEmpty()) {
            cityList = area2s.stream().map(o -> {
                StaticCity staticCity = StaticWorldDataMgr.getMaxTypeCityByArea(o.getArea());
                return staticCity.getCityId();
            }).collect(Collectors.toList());
        } else {
            cityList = area1s.stream().map(o -> {
                StaticCity staticCity = StaticWorldDataMgr.getMaxTypeCityByArea(o.getArea());
                return staticCity.getCityId();
            }).collect(Collectors.toList());
        }
        if (ListUtils.isNotBlank(cityList)) {
            chatDataManager.sendSysChat(ChatConst.FIREWORKS_LETOFF, 0, 0, cityList.toArray());
            syncFireworkLetoff(activityType, staticFireworks.getId(), cityList, null);
            LogUtil.debug(String.format("系统放烟花,烟花配置=%s,城池=%s", staticFireworks.getId(), ListUtils.toString(cityList)));
        }
    }

//    private Chat sendSysChat(int chatId, int campOrArea, Object... param) {
//        StaticChat sChat = StaticChatDataMgr.getChatMapById(chatId);
//        Chat chat = null;
//        if (sChat != null) {
//            int channel = sChat.getChannel();
//            chat = chatDataManager.createSysChat(chatId, param);
//            if (ChatConst.CHANNEL_WORLD == channel) {// 世界
//                sendCampChat(chat,1,0);
//                sendCampChat(chat,2,0);
//                sendCampChat(chat,3,0);
////            } else if (ChatConst.CHANNEL_CAMP == channel) {// 本阵营
////                sendCampChat(chat, campOrArea, 0);
////            } else if (ChatConst.CHANNEL_AREA == channel) {// 本区域
////                sendAreaChat(chat, campOrArea);
//            } else {
//                LogUtil.error("聊天配置表出错 chatId:", chatId);
//            }
//        } else {
//            LogUtil.error("聊天配置表出错 chatId:", chatId);
//        }
//        return chat;
//    }

//    public void sendCampChat(Chat chat, int camp, int area) {
//        CommonPb.Chat b = null;
//        if (area > 0) {
//            b = chatDataManager.addAreaChat(chat, area);
//        } else {
//            b = chatDataManager.addCampChat(chat, camp);
//        }
//
//        GamePb3.SyncChatRs.Builder chatBuilder = GamePb3.SyncChatRs.newBuilder();
//        chatBuilder.setChat(b);
//        BasePb.Base.Builder builder = PbHelper.createSynBase(GamePb3.SyncChatRs.EXT_FIELD_NUMBER, GamePb3.SyncChatRs.ext, chatBuilder.build());
//        long sendChatRoleId = 0L;
//        if (chat instanceof RoleChat) {
//            RoleChat rc = (RoleChat) chat;
//            sendChatRoleId = rc.getPlayer().roleId;
//        }
//        ConcurrentHashMap<Long, Player> campMap = playerDataManager.getPlayerByCamp(camp);
//        if (!CheckNull.isEmpty(campMap)) {
//            Player player;
//            Iterator<Player> it = campMap.values().iterator();
//            while (it.hasNext()) {
//                player = it.next();
//                if (null != player && player.ctx != null && player.isLogin) {
//                    if (area > 0 && player.lord.getArea() != area) {
//                        continue;
//                    }
//                    if (sendChatRoleId > 0 && player.isInBlacklist(sendChatRoleId)) {// 过滤到黑名单
//                        continue;
//                    }
//                    MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
//                }
//            }
//        }
//    }

    private void addSystemLetoffJob(ActivityBase activityBase, Date now) {
        if (Objects.nonNull(activityBase)) {
            int actType = activityBase.getActivityType();
            int actId = activityBase.getActivityId();
            Date fireDate = activityBase.getBeginTime();
            for (Integer i : StaticDataMgr.getFireworksInterval()) {
                fireDate = TimeHelper.afterSecondTime1(fireDate, i);
                if (fireDate.after(now)) {
                    QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), ActJob.NAME_FWLETOFF + actType + "_" + actId + "_" + i, ActJob.GROUP_FIREWORKS, FireworksJob.class, fireDate);
                }
            }
        }
    }

    private void syncFireworkLetoff(int actType, int confId, List<Integer> cityId, Player player) {
        GamePb5.SyncFireworkLetoffRs.Builder builder = GamePb5.SyncFireworkLetoffRs.newBuilder();
        builder.setActType(actType);
        builder.setConfId(confId);
        builder.addAllCityId(cityId);
        builder.setRoleId(Objects.isNull(player) ? 0 : player.roleId);
        builder.setRolePos(Objects.isNull(player) ? 0 : player.lord.getPos());
        BasePb.Base msg = PbHelper.createSynBase(GamePb5.SyncFireworkLetoffRs.EXT_FIELD_NUMBER, GamePb5.SyncFireworkLetoffRs.ext, builder.build()).build();
        playerService.syncMsgToAll(msg);
    }

    public void sendChat(int minute) {
        chatDataManager.sendSysChat(ChatConst.FIREWORKS_PREVIEW, 0, 0, minute);
    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }

    @Override
    protected void addOtherJob(ActivityBase activityBase, Date now) {
        QuartzHelper.removeJobByGroup(ScheduleManager.getInstance().getSched(), ActJob.GROUP_FIREWORKS);
        StaticDataMgr.getFireworksPreview().forEach(i -> {
            Date date = TimeHelper.beforeMinuteTime(activityBase.getBeginTime(), i);
            if (date.after(now)) {
                QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), ActJob.NAME_FWPRE + activityBase.getActivityType() + "_" + activityBase.getActivityId() + "_" + i, ActJob.GROUP_FIREWORKS, FireworksJob.class, date);
            }
        });
        this.addSystemLetoffJob(activityBase, now);
    }

    @GmCmd("firework")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        int actType = Integer.parseInt(params[0]);
        ActivityBase activityBase = super.checkAndGetActivityBase(player, actType);
        if (params[1].equalsIgnoreCase("debug")) {
            StaticDataMgr.getFireworksPreview().forEach(i -> {
                Date date = TimeHelper.beforeMinuteTime(activityBase.getBeginTime(), i);
                LogUtil.error("2022放烟花活动预告跑马灯时刻Date=" + date + ",Job=" + ScheduleManager.getInstance().getJobDetail(ActJob.GROUP_FIREWORKS, ActJob.NAME_FWPRE + i));
            });
            String beginName = ScheduleManager.getInstance().getActBeginJobName(actType, activityBase.getActivityId(), activityBase.getPlanKeyId());
            LogUtil.error("2022放烟花活动开始时刻Date=" + activityBase.getBeginTime() + ",Job=" + ScheduleManager.getInstance().getJobDetail("ActBegin", beginName));
            int actId = activityBase.getActivityId();
            Date fireDate = activityBase.getBeginTime();
            for (Integer i : StaticDataMgr.getFireworksInterval()) {
                fireDate = TimeHelper.afterSecondTime1(fireDate, i);
                LogUtil.error("2022放烟花活动系统燃放时刻Date=" + fireDate + ",Job=" + ScheduleManager.getInstance().getJobDetail(ActJob.GROUP_FIREWORKS, ActJob.NAME_FWLETOFF + actType + "_" + actId));
            }
        }
        if (params[1].equalsIgnoreCase("clearjob")) {
            QuartzHelper.removeJobByGroup(ScheduleManager.getInstance().getSched(), ActJob.GROUP_FIREWORKS);
        }
        if (params[1].equalsIgnoreCase("letoff")) {
            int num = Integer.parseInt(params[2]);
            int confId = Integer.parseInt(params[3]);
            Stream.iterate(0, i -> i + 1).limit(num).forEach(j -> systemLetoff(actType, activityBase.getActivityId()));
        }
        if (params[1].equalsIgnoreCase("playerletoff")) {
            int confId = Integer.parseInt(params[2]);
            AtomicReference<StaticArea> reference = new AtomicReference<>();
            globalDataManager.getGameGlobal().getAreaMap().entrySet().forEach(entry -> {
                StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(entry.getKey());
                if (Objects.nonNull(staticArea) && (entry.getValue().isOpen() || entry.getValue().canPass())) {
                    if (Objects.isNull(reference.get())) {
                        reference.set(staticArea);
                    } else {
                        if (reference.get().getOpenOrder() < staticArea.getOpenOrder()) {
                            reference.set(staticArea);
                        }
                    }
                }
            });
            if (Objects.nonNull(reference.get())) {
                playerDataManager.getPlayers().values().forEach(p -> {
                    StaticCity maxCity;
                    if (reference.get().getOpenOrder() == WorldConstant.AREA_ORDER_3) {
                        maxCity = StaticWorldDataMgr.getMaxTypeCityByArea(reference.get().getArea());
                    } else {
                        maxCity = StaticWorldDataMgr.getMaxTypeCityByArea(p.lord.getArea());
                    }
                    //跑马灯
                    chatDataManager.sendSysChat(ChatConst.FIREWORKS_LETOFF1, p.pCampMailTime, 0, p.getCamp(), p.lord.getNick());
                    List<Integer> cityList = new ArrayList<>();
                    cityList.add(maxCity.getCityId());
                    this.syncFireworkLetoff(actType, confId, cityList, p);
                });
            }
        }
    }
}
