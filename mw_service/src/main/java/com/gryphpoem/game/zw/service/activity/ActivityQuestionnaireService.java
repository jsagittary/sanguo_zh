package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.*;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActQuestionnaire;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.PlayerService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-25 18:01
 */
@Component
public class ActivityQuestionnaireService extends AbsSimpleActivityService {

    @Autowired
    private PlayerDataManager playerDataManager;
    /**
     * 玩家问卷缓存
     */
    private Map<Integer, Map<Long, StaticActQuestionnaire>> playerQuestionnaireMap = new ConcurrentHashMap<>();

    /**
     * 问卷更新
     */
    private static final int QUESTIONNAIRE_UPDATE = 0;
    /**
     * 问卷新增
     */
    private static final int QUESTIONNAIRE_NEW = 1;
    /**
     * 问卷删除
     */
    private static final int QUESTIONNAIRE_DELETE = 2;

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws Exception {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (CheckNull.isNull(activityBase) || CheckNull.isNull(activityBase.getPlan()) ||
                CheckNull.isEmpty(activityBase.getPlan().getChannel()) || !activityBase.getPlan().getChannel().contains(player.account.getPlatNo())) {
            return builder;
        }
        StaticActQuestionnaire config = getConfig(player.lord.getLordId(), activity.getActivityId(), player.account.getPlatNo(), player.lord.getLevel(), activityBase.getPlanKeyId());
        if (CheckNull.isNull(config)) {
            return builder;
        }

        CommonPb.QuestionnaireActData dataPb = createQuestionnaireActDataPb(player, config, activityBase);
        if (Objects.nonNull(dataPb)) {
            getActivityData(activityBase.getPlanKeyId()).put(player.lord.getLordId(), config);
            builder.setQuestionnaireInfo(dataPb);
        } else {
            getActivityData(activityBase.getPlanKeyId()).remove(player.lord.getLordId());
        }
        return builder;
    }

    @PostConstruct
    public void init() {
        EventBus.getDefault().register(this);
    }

    /**
     * 获取玩家已使用问卷配置
     *
     * @param actKeyId
     * @return
     */
    private Map<Long, StaticActQuestionnaire> getActivityData(int actKeyId) {
        Map<Long, StaticActQuestionnaire> dataMap = playerQuestionnaireMap.get(actKeyId);
        if (CheckNull.isEmpty(dataMap)) {
            synchronized (playerQuestionnaireMap) {
                dataMap = playerQuestionnaireMap.get(actKeyId);
                if (CheckNull.isEmpty(dataMap))
                    playerQuestionnaireMap.put(actKeyId, new ConcurrentHashMap<>());
            }
        }

        return playerQuestionnaireMap.get(actKeyId);
    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        ActivityBase ab = StaticActivityDataMgr.getActivityList().stream().filter(activityBase -> activityBase.getPlanKeyId() == keyId).findFirst().orElse(null);
        if (CheckNull.isNull(ab))
            return;
        EventBus.getDefault().post(new Events.SyncQuestionnaireEvent(ab, new HashMap<>(), true));
    }

    /**
     * 获取问卷配置，过滤不能存在配置以及等级不足配置
     * (同步时不可调用此方法)
     *
     * @param roleId
     * @param activityId
     * @param platNo
     * @param lv
     * @return
     */
    private StaticActQuestionnaire getConfig(long roleId, int activityId, int platNo, int lv, int actPlanKeyId) {
        Map<Integer, Map<Integer, StaticActQuestionnaire>> configMap = StaticActivityDataMgr.getStaticActQuestionnaireMap();
        if (CheckNull.isEmpty(configMap)) {
            getActivityData(actPlanKeyId).remove(roleId);
            return null;
        }
        if (CheckNull.isEmpty(configMap.get(activityId)) ||
                CheckNull.isNull(configMap.get(activityId).get(platNo))) {
            getActivityData(actPlanKeyId).remove(roleId);
            return null;
        }
        StaticActQuestionnaire config = configMap.get(activityId).get(platNo);
        if (config.getLv() > lv) {
            getActivityData(actPlanKeyId).remove(roleId);
            return null;
        }
        return config;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void levelUp(Events.ActLevelUpEvent event) {
        Player player = playerDataManager.getPlayer(event.roleId);
        if (CheckNull.isNull(player)) {
            LogUtil.error(String.format("Events.ActLevelUpEvent roleId:%d, not exist", event.roleId));
            return;
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_QUESTIONNAIRE);
        if (CheckNull.isNull(activityBase) || CheckNull.isNull(activityBase.getPlan()) ||
                !activityBase.getPlan().getChannel().contains(player.account.getPlatNo()))
            return;
        if (getActivityData(activityBase.getPlanKeyId()).containsKey(player.lord.getLordId()))
            return;
        StaticActQuestionnaire config = getConfig(player.lord.getLordId(), activityBase.getActivityId(), player.account.getPlatNo(), event.curLevel, activityBase.getPlanKeyId());
        if (CheckNull.isNull(config))
            return;

        GamePb5.SyncQuestionnaireActInfoRs.Builder builder = GamePb5.SyncQuestionnaireActInfoRs.newBuilder();
        builder.setStatus(QUESTIONNAIRE_NEW);
        builder.setInfo(config.createPb(false));
        builder.setActivity(PbHelper.createActivityPb(activityBase, true, 0));
        builder.setActId(activityBase.getActivityId());
        DataResource.ac.getBean(PlayerService.class).syncMsgToPlayer(PbHelper.createSynBase(GamePb5.SyncQuestionnaireActInfoRs.
                EXT_FIELD_NUMBER, GamePb5.SyncQuestionnaireActInfoRs.ext, builder.build()).build(), player);
        getActivityData(activityBase.getPlanKeyId()).put(event.roleId, config);
    }

    /**
     * 启动服务器或刷表时同步配置
     *
     * @param dataMap
     */
    public void syncQuestionnaireConfig(Map<Integer, Map<Integer, StaticActQuestionnaire>> dataMap) {
        if (dataMap == null) dataMap = new HashMap<>();
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_QUESTIONNAIRE);
        if (CheckNull.isNull(activityBase))
            return;
        // 配置不同则通知客户端
        EventBus.getDefault().post(new Events.SyncQuestionnaireEvent(activityBase,
                CheckNull.isNull(dataMap.get(activityBase.getActivityId())) ?
                        new HashMap<>() : dataMap.get(activityBase.getActivityId()), false));
    }

    /**
     * 同步通知问卷配置详情
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void syncQuestionnaireActInfo(Events.SyncQuestionnaireEvent event) {
        if (CheckNull.isEmpty(playerDataManager.getAllOnlinePlayer()))
            return;
        if (CheckNull.isEmpty(event.newConfigMap) && CheckNull.isEmpty(getActivityData(event.activityBase.getPlanKeyId())))
            return;

        Map<Long, StaticActQuestionnaire> playerActData = getActivityData(event.activityBase.getPlanKeyId());
        Collection<Player> onlinePlayers = playerDataManager.getAllOnlinePlayer().values();
        GamePb5.SyncQuestionnaireActInfoRs.Builder builder = GamePb5.SyncQuestionnaireActInfoRs.newBuilder();
        CommonPb.Activity activityPb = PbHelper.createActivityPb(event.activityBase, true, 0);
        onlinePlayers.forEach(player -> {
            List<Integer> channels = event.activityBase.getPlan().getChannel();
            StaticActQuestionnaire pConfig = playerActData.get(player.lord.getLordId());
            StaticActQuestionnaire newConfig = event.newConfigMap.get(player.account.getPlatNo());
            builder.setActId(event.activityBase.getActivityId());
            if (pConfig == null) {
                if (channels.contains(player.account.getPlatNo()) && Objects.nonNull(newConfig) &&
                        player.lord.getLevel() >= newConfig.getLv()) {
                    builder.setStatus(QUESTIONNAIRE_NEW);
                    builder.setInfo(newConfig.createPb(false));
                    builder.setActivity(activityPb);
                }
            } else {
                if (!channels.contains(player.account.getPlatNo()) || CheckNull.isNull(newConfig) ||
                        newConfig.getLv() > player.lord.getLevel() || event.end) {
                    builder.setStatus(QUESTIONNAIRE_DELETE);
                } else if (!newConfig.equals(pConfig)) {
                    builder.setStatus(QUESTIONNAIRE_UPDATE);
                    builder.setInfo(newConfig.createPb(false));
                }
            }
            if (builder.hasStatus()) {
                if (builder.getStatus() == QUESTIONNAIRE_DELETE) {
                    playerActData.remove(player.lord.getLordId());
                } else {
                    playerActData.put(player.lord.getLordId(), newConfig);
                }
                BasePb.Base msg = PbHelper.createSynBase(GamePb5.SyncQuestionnaireActInfoRs.EXT_FIELD_NUMBER, GamePb5.SyncQuestionnaireActInfoRs.ext, builder.build()).build();
                DataResource.ac.getBean(PlayerService.class).syncMsgToPlayer(msg, player);
            }

            builder.clear();
        });

        if (event.end) {
            playerActData.clear();
        }
    }

    /**
     * 创建问卷调查详情
     *
     * @param player
     * @param activityBase
     * @return
     */
    private CommonPb.QuestionnaireActData createQuestionnaireActDataPb(Player player, StaticActQuestionnaire config, ActivityBase activityBase) {
        if (CheckNull.isNull(player) || CheckNull.isNull(player.account) || CheckNull.isNull(config))
            return null;
        if (ObjectUtils.isEmpty(getActivityType()) || !ArrayUtils.contains(getActivityType(), activityBase.getActivityType())) {
            return null;
        }

        return config.createPb(false);
    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_QUESTIONNAIRE};
    }

    @Override
    public boolean inChannel(Player player, ActivityBase actBase) {
        if (CheckNull.isNull(player) || CheckNull.isNull(player.account) || CheckNull.isNull(actBase) || CheckNull.isNull(actBase.getPlan()))
            return false;
        if (CheckNull.isEmpty(actBase.getPlan().getChannel()))
            return false;
        StaticActQuestionnaire config = getConfig(player.lord.getLordId(), actBase.getActivityId(),
                player.account.getPlatNo(), player.lord.getLevel(), actBase.getPlanKeyId());
        if (CheckNull.isNull(config)) {
            return false;
        }
        return actBase.getPlan().getChannel().contains(player.account.getPlatNo());
    }
}
