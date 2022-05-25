package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.AbsGameService;
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

    private Map<Integer, List<List<String>>> questionnaireConfigMap;

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws Exception {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        Optional.ofNullable(createQuestionnaireActDataPb(player, null, activity.getActivityType())).ifPresent(pb -> builder.setQuestionnaireInfo(pb));
        return builder;
    }

    @PostConstruct
    public void init() {
        EventBus.getDefault().register(this);
    }

    /**
     * @param config
     */
    public void syncQuestionnaireConfig(List<List<String>> config) {
        if (CheckNull.isEmpty(config))
            return;
        if (CheckNull.isEmpty(questionnaireConfigMap))
            questionnaireConfigMap = new HashMap<>();
        // 配置不同则通知客户端
        if (!configEqual(questionnaireConfigMap.get(ActivityConst.ACT_QUESTIONNAIRE), config)) {
            EventBus.getDefault().post(new Events.SyncQuestionnaireEvent(ActivityConst.ACT_QUESTIONNAIRE, config));
        }
        questionnaireConfigMap.put(ActivityConst.ACT_QUESTIONNAIRE, new ArrayList<>(config));
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
        Collection<Player> onlinePlayers = playerDataManager.getAllOnlinePlayer().values();
        GamePb5.SyncQuestionnaireActInfo.Builder builder = GamePb5.SyncQuestionnaireActInfo.newBuilder();
        onlinePlayers.forEach(player -> {
            ActivityPb.QuestionnaireActData actDataInfo = createQuestionnaireActDataPb(player, event.configList, event.actType);
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
    private ActivityPb.QuestionnaireActData createQuestionnaireActDataPb(Player player, List<List<String>> configList, int actType) {
        if (CheckNull.isNull(player) || CheckNull.isNull(player.account))
            return null;
        if (ObjectUtils.isEmpty(getActivityType()) || !ArrayUtils.contains(getActivityType(), actType)) {
            return null;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (CheckNull.isNull(activityBase) || CheckNull.isNull(activityBase.getPlan()) ||
                CheckNull.isEmpty(activityBase.getPlan().getChannel()) || activityBase.getPlan().getChannel().contains(player.account.getPlatNo()))
            return null;
        if (CheckNull.isEmpty(questionnaireConfigMap))
            return null;

        configList = CheckNull.isEmpty(configList) ? questionnaireConfigMap.get(actType) : configList;
        if (CheckNull.isEmpty(configList))
            return null;
        List<String> configStr = configList.stream().filter(list -> Integer.parseInt(list.get(0)) == player.account.getPlatNo() &&
                Integer.parseInt(list.get(1)) == activityBase.getActivityId()).findFirst().orElse(null);
        if (CheckNull.isEmpty(configStr))
            return null;
        ActivityPb.QuestionnaireActData.Builder builder = ActivityPb.QuestionnaireActData.newBuilder();
        builder.setPlatNo(Integer.parseInt(configStr.get(0)));
        builder.setUrl(configStr.get(2));
        return builder.build();
    }

    /**
     * 配置是否一致
     *
     * @param oldConfig
     * @param newConfig
     * @return
     */
    private boolean configEqual(List<List<String>> oldConfig, List<List<String>> newConfig) {
        if (CheckNull.isEmpty(oldConfig))
            return false;
        if (!ObjectUtils.nullSafeEquals(oldConfig, newConfig))
            return false;

        return Collections.singletonList(oldConfig).toString().equals(Collections.singletonList(newConfig).toString());
    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_QUESTIONNAIRE};
    }
}
