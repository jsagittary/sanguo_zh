package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActExchange;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 喜悦金秋活动 - 硕果累累
 *
 * @author liuyc
 * @date 2021/9/11
 */
@Service
public class GoldenAutumnFruitfulService extends GoldenAutumnService {

    /**
     * 构建硕果累累数据信息
     * @return
     */
    @Override
    public GeneratedMessage buildGoldenAutumnInfo(Player player, Activity activity) {
        CommonPb.GoldenAutumnFruitful.Builder goldenAutumnFruitful = CommonPb.GoldenAutumnFruitful.newBuilder();
        goldenAutumnFruitful.setBookmark(3);
        Prop earRicProp = player.props.get(PropConstant.PROP_ID_EARRICE);
        for (Integer key : activity.getPropMap().keySet())
        {
            CommonPb.GoldenAutumnFruitfulExcGather.Builder excGather = CommonPb.GoldenAutumnFruitfulExcGather.newBuilder();
            excGather.setGroupId(key);
            excGather.setEarRiceId(PropConstant.PROP_ID_EARRICE);
            excGather.setEarRiceCount(Objects.isNull(earRicProp) ? 0 : earRicProp.getCount());
            excGather.setRedeemedNumber(activity.getPropMap().get(key));
            goldenAutumnFruitful.addExcGather(excGather);
        };
        return goldenAutumnFruitful.build();
    }

    /**
     * 处理兑换
     */
    public GamePb4.GoldenAutumnFruitfulRs handlerFruitful(long roleId, GamePb4.GoldenAutumnFruitfulRq rq) throws MwException {
        if (Objects.isNull(rq) || rq.getActType() == 0 || rq.getGroupId() == 0 || rq.getEarRiceId() ==0 || rq.getEarRiceCount() == 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "参数错误"));
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player, rq.getActType());
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(rq.getActType());
        if (null == activity || !super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "活动未开放", rq.getActType(), activityBase.getStep0()));
        }
        //获取兑换奖励配置
        List<StaticActExchange> actExchangeList = StaticActivityDataMgr.getActExchangeListById(activityBase.getActivityId());
        if (null == actExchangeList || actExchangeList.isEmpty())
        {
            throw new MwException(GameError.NOT_FRUITFUL_EXCHANGE_CONFIG.getCode(), "根据活动类型未匹配到兑换奖励配置! roleId:" + roleId + ",activityType:" + activityBase.getActivityType());
        }
        StaticActExchange staticActExchange = actExchangeList.stream().filter(e -> e.getKeyId() == rq.getGroupId()).findFirst().orElse(null);
        if (Objects.isNull(staticActExchange))
        {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "根据兑换组id未匹配到兑换奖励配置! roleId:" + roleId + ",groupId:" + rq.getGroupId());
        }
        StaticProp seedStaticProp = StaticPropDataMgr.getPropMap(rq.getEarRiceId());//获取稻穗道具基础配置
        if (null == seedStaticProp) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "金秋活动-硕果累累,无此配置道具, roleId:" + roleId + ",earRiceId=" + rq.getEarRiceId());
        }
        //判断传入的果实id与数量是否满足配置组的消耗需求
        if (staticActExchange.getExpendProp().stream().noneMatch(e -> e.get(0) == seedStaticProp.getPropType() && e.get(1) == rq.getEarRiceId()
                && e.get(2) == rq.getEarRiceCount()))
        {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "传入的稻穗id和数量与配置不匹配, roleId:" + roleId + ",earRiceId=" + rq.getEarRiceId());
        }
        //判断是否达到兑换次数限制
        int numberLimit = activity.getPropMap().getOrDefault(rq.getGroupId(), 0);//propMap中key=兑换组id存储兑换次数
        if (numberLimit >= staticActExchange.getNumberLimit())
        {
            throw new MwException(GameError.EXCHANGE_AWARD_MAX.getCode(), "已达到兑换奖励上限, roleId:" + roleId + ",groupId=" + rq.getGroupId());
        }
        //判断兑换等级限制
        if (player.lord.getLevel() < staticActExchange.getLvLimit()) {
            throw new MwException(GameError.EXCHANGE_AWARD_LEVEL_ERR.getCode(), "兑换奖励等级未达到, roleId:", roleId, ", min:", staticActExchange.getLvLimit(), ", level:", player.lord.getLevel());
        }
        //消耗稻穗
        rewardDataManager.checkAndSubPlayerResHasSync(player, seedStaticProp.getPropType(), rq.getEarRiceId(), rq.getEarRiceCount(), AwardFrom.EAR_RICE_CONSUME);
        //同步兑换奖励
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticActExchange.getAwardList(), AwardFrom.FRUITFUL_EXCHANGE);
        //更新兑换奖励次数
        activity.getPropMap().put(rq.getGroupId(), numberLimit + 1);
        //组装协议
        CommonPb.GoldenAutumnFruitfulExcGather.Builder excGather = CommonPb.GoldenAutumnFruitfulExcGather.newBuilder();
        excGather.setGroupId(rq.getGroupId());
        excGather.setEarRiceId(rq.getEarRiceId());
        excGather.setEarRiceCount(player.props.get(rq.getEarRiceId()).getCount());
        excGather.setRedeemedNumber(activity.getPropMap().get(rq.getGroupId()));
        GamePb4.GoldenAutumnFruitfulRs.Builder rs = GamePb4.GoldenAutumnFruitfulRs.newBuilder();
        rs.setActType(rq.getActType());
        rs.setExcGather(excGather);
        Optional.ofNullable(awardList).ifPresent(tmpList -> tmpList.forEach(rs::addGetAward));
        return rs.build();
    }

    // <editor-fold desc="GM命令" defaultstate="collapsed">
    public void test_handlerFruitful(Player player,int actType, int groupId, int count) throws MwException {
        GamePb4.GoldenAutumnFruitfulRq.Builder rq = GamePb4.GoldenAutumnFruitfulRq.newBuilder();
        rq.setActType(actType);
        rq.setGroupId(groupId);
        rq.setEarRiceId(PropConstant.PROP_ID_EARRICE);
        rq.setEarRiceCount(count);
        this.handlerFruitful(player.roleId, rq.build());
    }

    public void test_buildGoldenAutumnInfo(Player player,int actType) throws MwException {
        this.buildGoldenAutumnInfo(player, super.checkAndGetActivity(player, actType));
    }

// </editor-fold>
}