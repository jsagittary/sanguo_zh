package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
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

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-05-25 18:01
 */
@Component
public class ActivityQuestionnaireService extends AbsSimpleActivityService {

    @Autowired
    private PlayerDataManager playerDataManager;

    private Map<Integer, Map<Integer, StaticActQuestionnaire>> questionnaireConfigMap;

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws Exception {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        if (CheckNull.isEmpty(questionnaireConfigMap))
            return builder;
        if (CheckNull.isEmpty(questionnaireConfigMap.get(activity.getActivityId())) ||
                CheckNull.isNull(questionnaireConfigMap.get(activity.getActivityId()).get(player.account.getPlatNo())))
            return builder;
        Optional.ofNullable(createQuestionnaireActDataPb(player, questionnaireConfigMap.get(activity.getActivityId()).get(player.account.getPlatNo()),
                activity.getActivityType())).ifPresent(pb -> builder.setQuestionnaireInfo(pb));
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
        if (CheckNull.isEmpty(dataMap))
            return;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_QUESTIONNAIRE);
        if (CheckNull.isNull(activityBase))
            return;
        // 配置不同则通知客户端
        EventBus.getDefault().post(new Events.SyncQuestionnaireEvent(ActivityConst.ACT_QUESTIONNAIRE,
                CheckNull.isEmpty(questionnaireConfigMap) ? null : questionnaireConfigMap.get(activityBase.getActivityId()),
                dataMap.get(activityBase.getActivityId())));
        questionnaireConfigMap = new HashMap<>(dataMap);
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
        Map<Integer, StaticActQuestionnaire> diffConfigMap = configCompare(event.oldConfigMap, event.newConfigMap);
        if (CheckNull.isEmpty(diffConfigMap))
            return;

        Collection<Player> onlinePlayers = playerDataManager.getAllOnlinePlayer().values();
        GamePb5.SyncQuestionnaireActInfo.Builder builder = GamePb5.SyncQuestionnaireActInfo.newBuilder();
        onlinePlayers.forEach(player -> {
            StaticActQuestionnaire config = diffConfigMap.get(player.account.getPlatNo());
            if (CheckNull.isNull(config))
                return;
            CommonPb.QuestionnaireActData actDataInfo = createQuestionnaireActDataPb(player, config, event.actType);
            if (CheckNull.isNull(actDataInfo))
                return;
            builder.setActId(activityBase.getActivityId());
            builder.setInfo(actDataInfo);
            BasePb.Base msg = PbHelper.createSynBase(GamePb5.SyncQuestionnaireActInfo.EXT_FIELD_NUMBER, GamePb5.SyncQuestionnaireActInfo.ext, builder.build()).build();
            DataResource.ac.getBean(PlayerService.class).syncMsgToPlayer(msg, player);
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
        if (CheckNull.isNull(player) || CheckNull.isNull(player.account))
            return null;
        if (ObjectUtils.isEmpty(getActivityType()) || !ArrayUtils.contains(getActivityType(), actType)) {
            return null;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (CheckNull.isNull(activityBase) || CheckNull.isNull(activityBase.getPlan()) ||
                CheckNull.isEmpty(activityBase.getPlan().getChannel()) || !activityBase.getPlan().getChannel().contains(player.account.getPlatNo()))
            return null;
        if (CheckNull.isEmpty(questionnaireConfigMap))
            return null;

        CommonPb.QuestionnaireActData.Builder builder = CommonPb.QuestionnaireActData.newBuilder();
        builder.setPlatNo(config.getPlatNo());
        builder.setUrl(config.getUrl());
        builder.setDesc(config.getDesc());
        builder.addAllAwards(PbHelper.createAwardsPb(config.getAwards()));
        return builder.build();
    }

    /**
     * 配置是否一致
     *
     * @param oldConfig
     * @param newConfig
     * @return
     */
    private Map<Integer, StaticActQuestionnaire> configCompare(Map<Integer, StaticActQuestionnaire> oldConfig, Map<Integer, StaticActQuestionnaire> newConfig) {
        if (CheckNull.isEmpty(oldConfig))
            return newConfig;

        Collection<StaticActQuestionnaire> oldConfigList = oldConfig.values();
        Collection<StaticActQuestionnaire> newConfigList = newConfig.values();
        Map<Integer, StaticActQuestionnaire> diffMap = new HashMap<>();
        for (StaticActQuestionnaire data : oldConfigList) {
            if (!newConfigList.contains(data))
                diffMap.put(data.getPlatNo(), data);
        }

        return diffMap;
    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_QUESTIONNAIRE};
    }
}
