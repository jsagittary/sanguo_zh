package com.gryphpoem.game.zw.service.activity;

import com.alibaba.nacos.common.utils.MapUtils;
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
import com.gryphpoem.game.zw.resource.util.ListUtils;
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
    private Map<Long, StaticActQuestionnaire> playerQuestionnaireMap = new ConcurrentHashMap<>();

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
        Map<Integer, Map<Integer, StaticActQuestionnaire>> configMap = StaticActivityDataMgr.getStaticActQuestionnaireMap();
        if (CheckNull.isEmpty(configMap)) {
            playerQuestionnaireMap.remove(player.lord.getLordId());
            return builder;
        }
        if (CheckNull.isEmpty(configMap.get(activity.getActivityId())) ||
                CheckNull.isNull(configMap.get(activity.getActivityId()).get(player.account.getPlatNo()))) {
            playerQuestionnaireMap.remove(player.lord.getLordId());
            return builder;
        }

        StaticActQuestionnaire config = configMap.get(activity.getActivityId()).get(player.account.getPlatNo());
        CommonPb.QuestionnaireActData dataPb = createQuestionnaireActDataPb(player, configMap.get(activity.getActivityId()).get(player.account.getPlatNo()),
                activity.getActivityType());
        if (Objects.nonNull(dataPb)) {
            playerQuestionnaireMap.put(player.lord.getLordId(), config);
            builder.setQuestionnaireInfo(dataPb);
        } else {
            playerQuestionnaireMap.remove(player.lord.getLordId());
        }
        return builder;
    }

    @PostConstruct
    public void init() {
        EventBus.getDefault().register(this);
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
        EventBus.getDefault().post(new Events.SyncQuestionnaireEvent(ActivityConst.ACT_QUESTIONNAIRE,
                CheckNull.isNull(dataMap.get(activityBase.getActivityId())) ?
                        new HashMap<>() : dataMap.get(activityBase.getActivityId())));
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
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(event.actType);
        if (CheckNull.isNull(activityBase))
            return;
        if (CheckNull.isEmpty(event.newConfigMap) && CheckNull.isEmpty(playerQuestionnaireMap))
            return;

        Collection<Player> onlinePlayers = playerDataManager.getAllOnlinePlayer().values();
        GamePb5.SyncQuestionnaireActInfoRs.Builder builder = GamePb5.SyncQuestionnaireActInfoRs.newBuilder();
        CommonPb.Activity activityPb = PbHelper.createActivityPb(activityBase, true, 0);
        onlinePlayers.forEach(player -> {
            List<Integer> channels = activityBase.getPlan().getChannel();
            StaticActQuestionnaire pConfig = playerQuestionnaireMap.get(player.lord.getLordId());
            StaticActQuestionnaire newConfig = event.newConfigMap.get(player.account.getPlatNo());
            builder.setActId(activityBase.getActivityId());
            if (pConfig == null) {
                if (channels.contains(player.account.getPlatNo()) && Objects.nonNull(newConfig)) {
                    builder.setStatus(QUESTIONNAIRE_NEW);
                    builder.setInfo(newConfig.createPb(false));
                    builder.setActivity(activityPb);
                }
            } else {
                if (!channels.contains(player.account.getPlatNo()) || CheckNull.isNull(newConfig)) {
                    builder.setStatus(QUESTIONNAIRE_DELETE);
                } else if (!newConfig.equals(pConfig)) {
                    builder.setStatus(QUESTIONNAIRE_UPDATE);
                    builder.setInfo(newConfig.createPb(false));
                }
            }
            if (builder.hasStatus()) {
                if (builder.getStatus() == QUESTIONNAIRE_DELETE) {
                    playerQuestionnaireMap.remove(player.lord.getLordId());
                } else {
                    playerQuestionnaireMap.put(player.lord.getLordId(), newConfig);
                }
                BasePb.Base msg = PbHelper.createSynBase(GamePb5.SyncQuestionnaireActInfoRs.EXT_FIELD_NUMBER, GamePb5.SyncQuestionnaireActInfoRs.ext, builder.build()).build();
                DataResource.ac.getBean(PlayerService.class).syncMsgToPlayer(msg, player);
            }

            builder.clear();
        });
    }

    /**
     * 创建问卷调查详情
     *
     * @param player
     * @param actType
     * @return
     */
    private CommonPb.QuestionnaireActData createQuestionnaireActDataPb(Player player, StaticActQuestionnaire config, int actType) {
        if (CheckNull.isNull(player) || CheckNull.isNull(player.account) || CheckNull.isNull(config))
            return null;
        if (ObjectUtils.isEmpty(getActivityType()) || !ArrayUtils.contains(getActivityType(), actType)) {
            return null;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (CheckNull.isNull(activityBase) || CheckNull.isNull(activityBase.getPlan()) ||
                CheckNull.isEmpty(activityBase.getPlan().getChannel()) || !activityBase.getPlan().getChannel().contains(player.account.getPlatNo()))
            return null;

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
        return actBase.getPlan().getChannel().contains(player.account.getPlatNo());
    }
}
