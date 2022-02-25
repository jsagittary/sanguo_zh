package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticSignInDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.SiginInfo;
import com.gryphpoem.game.zw.resource.domain.s.StaticActLogin;
import com.gryphpoem.game.zw.resource.domain.s.StaticActSign;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到
 */
@Service
public class SignInService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private MailDataManager mailDataManager;

    /**
     * 获取签到信息
     *
     * @param roleId
     * @return
     */
    public GamePb4.GetSignInInfoRs getSignInInfoRq(long roleId, int activityType) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", activityType);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", activityType);
        }

        // 初始化和清除签到数据
        SiginInfo siginInfo = clearInitData(player, activity);

        GamePb4.GetSignInInfoRs.Builder builder = GamePb4.GetSignInInfoRs.newBuilder();
        builder.setSignInfo(PbHelper.createSignInfoPb(siginInfo));
        return builder.build();

    }

    /**
     * 初始化和清除签到数据
     * @param player    玩家对象
     * @param activity  活动对象
     */
    private SiginInfo clearInitData(Player player, Activity activity) {

        if (CheckNull.isNull(activity)) {
            return null;
        }

        SiginInfo siginInfo;
        if (activity.getActivityType() == ActivityConst.ACT_SIGIN) {
            siginInfo = player.siginInfo;
            if (siginInfo.getActivityId() != activity.getActivityId()) {
                siginInfo.setActivityId(activity.getActivityId());
            }
            /*// 如果不是同一个月
            if (!DateHelper.isMonth(new Date(siginInfo.getDate()), new Date())) {
                siginInfo.setLevel(player.lord.getLevel());
                siginInfo.setTimes(0);
            }*/
            
            if(siginInfo.getPage()==0){
                siginInfo.setPage(1);
            }
            
            StaticActSign signConfig = StaticSignInDataMgr
                    .getSignConfig(siginInfo.getPage(), siginInfo.getLevel(), siginInfo.getTimes() + 1);
            //如果下一次的签到配置找不到，开始下一轮签到
            if(signConfig==null && siginInfo.getSignIn()==0){
                siginInfo.setLevel(player.lord.getLevel());
                siginInfo.setTimes(0);
                int page = siginInfo.getPage();
                //下一页签到配置
                StaticActSign nextSignConfig = StaticSignInDataMgr
                        .getSignConfig(page+1, siginInfo.getLevel(), siginInfo.getTimes() + 1);
                if (nextSignConfig == null) {
                    siginInfo.setPage(page);
                }else {
                    siginInfo.setPage(page+1);
                }
                
            }
        } else {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
            if (activityBase == null) {
                return null;
            }
            int keyId = activityBase.getPlan().getKeyId();
            siginInfo = player.signInfoMap.get(activity.getActivityType());
            // 不同档位或者s_actvity_plan表中的keyId不同, 就清除数据
            if (siginInfo == null || siginInfo.getActivityId() != activity.getActivityId() || keyId != siginInfo.getKeyId()) {
                siginInfo = new SiginInfo();
                siginInfo.setKeyId(keyId);
                siginInfo.setActivityId(activity.getActivityId());
                player.signInfoMap.put(activity.getActivityType(), siginInfo);
            }
        }

        if (siginInfo.getDate() == 0) {
            siginInfo.setDate(System.currentTimeMillis());
        }

        if (siginInfo.getLevel() == 0) {
            siginInfo.setLevel(player.lord.getLevel());
        }

        // 如果不是同一天
        if (!DateHelper.isToday(new Date(siginInfo.getDate()))) {
            siginInfo.setDoubleReward(0);
            siginInfo.setSignIn(0);
            siginInfo.setDate(System.currentTimeMillis());
        }
        //为避免重复获得。判断奖励是不是武将，并且已经获得。修改为已签到状态
        if(checkHasPet(player,siginInfo,activity) && siginInfo.getSignIn() == 0) {
            siginInfo.setSignIn(1);
            siginInfo.setTimes(siginInfo.getTimes() + 1);
        }
        

        return siginInfo;
    }

    /**
     * 签到领取奖励
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb4.GetSignInRewardRs getSignInRewardRq(long roleId, int activityType) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", activityType);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (activityBase == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", activityType);
        }
        // 初始化和清除签到数据
        SiginInfo siginInfo = clearInitData(player, activity);

        if (siginInfo.getSignIn() == 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "没有奖励可以领取, roleId:", roleId, ", times:", siginInfo.getTimes());
        }

        List<List<Integer>> signInAward = getSignInAward(siginInfo, player, activityBase);
        GamePb4.GetSignInRewardRs.Builder builder = GamePb4.GetSignInRewardRs.newBuilder();

        for (List<Integer> e : signInAward) {
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            int itemKey = rewardDataManager.addAward(player, type, itemId, count, AwardFrom.SIGN_IN_REWARD);
            builder.addAward(PbHelper.createAwardPb(type, itemId, count, itemKey));
        }

        builder.setSignInfo(PbHelper.createSignInfoPb(siginInfo));
        return builder.build();

    }

    /**
     * 获取签到奖励
     * @param siginInfo     签到信息
     * @param player        玩家对象
     * @param activityBase  活动基础信息
     * @return              奖励
     * @throws MwException  自定义异常
     */
    private List<List<Integer>> getSignInAward(SiginInfo siginInfo, Player player, ActivityBase activityBase) throws MwException {

        List<List<Integer>> reward = new ArrayList<>();

        if (activityBase.getActivityType() == ActivityConst.ACT_SIGIN) {
//            String month = new SimpleDateFormat("yyyy-MM").format(new Date());
            StaticActSign signConfig = StaticSignInDataMgr
                    .getSignConfig(siginInfo.getPage(), siginInfo.getLevel(), siginInfo.getTimes() + 1);

            if (signConfig == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "没有配置签到奖励, roleId:", player.roleId, ", times:",
                        siginInfo.getTimes(), " page:" + siginInfo.getPage(), " level:", siginInfo.getLevel());
            }

            //普通奖励
            if (siginInfo.getSignIn() == 0) {
                reward.addAll(new ArrayList<>(signConfig.getAward()));
            }

            //满足vip直接翻倍
            if (signConfig.getVip() != 0 && player.lord.getVip() >= signConfig.getVip()
                    && siginInfo.getDoubleReward() == 0) {
                reward.addAll(new ArrayList<>(signConfig.getAward()));
            }

            //如果满足vip直接双倍
            if (signConfig.getVip() != 0 && player.lord.getVip() >= signConfig.getVip()
                    && siginInfo.getDoubleReward() == 0) {
                siginInfo.setDoubleReward(1);
            }
        } else {
            int activityId = siginInfo.getActivityId();
            // 开启距离现在多少天
            // int dayiy = DateHelper.dayiy(activityBase.getBeginTime(), new Date());
            // 签到配置
            StaticActLogin sActLogin = StaticActivityDataMgr.getActLogin(activityId, siginInfo.getTimes() + 1);
            if (sActLogin == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "没有配置签到奖励, roleId:", player.roleId, ", times:",
                        siginInfo.getTimes(), " level:", siginInfo.getLevel());
            }

            //普通奖励
            if (siginInfo.getSignIn() == 0) {
                reward.addAll(new ArrayList<>(sActLogin.getAwardList()));
            }
        }


        //修改状态
        if (siginInfo.getSignIn() == 0) {
            siginInfo.setSignIn(1);
            siginInfo.setTimes(siginInfo.getTimes() + 1);
        }

        siginInfo.setDate(System.currentTimeMillis());

        // 检测背包是否已满
        try {
            rewardDataManager.checkBag(player, reward);
        } catch (MwException e) {
            LogUtil.common(String.format("role_id: %s, sign_info: %s, activity_id: %s, activity_type: %s", player.roleId, siginInfo, activityBase.getActivityId(), activityBase.getActivityType()));
            List<List<Integer>> mailAward = reward.stream().filter(award -> award.get(0) == AwardType.EQUIP).collect(Collectors.toList());
            if (!CheckNull.isEmpty(mailAward)) {
                mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(mailAward), MailConstant.MOLD_OUT_OF_RANGE_AWARD,
                        AwardFrom.OUT_OF_RANGE_AWARD, TimeHelper.getCurrentSecond());
            }
            // 过滤装备
            reward = reward.stream().filter(award -> award.get(0) != AwardType.EQUIP).collect(Collectors.toList());
        }

        return reward;
    }

    /**
     * 红点
     * @param player    玩家对象
     * @param activity  活动对象
     * @return  红点
     */
    public int getRedPoint(Player player, Activity activity) {
        SiginInfo siginInfo = clearInitData(player, activity);
        return siginInfo.getSignIn() == 0 ? 1 : 0;
    }

    /**
     * 检查奖励，如果是武将，且已经拥有,返回true
     */
    private boolean checkHasPet(Player player, SiginInfo siginInfo,Activity activity){
        if (activity.getActivityType()==ActivityConst.ACT_SIGN_IN_NEW) {
            StaticActLogin sActLogin = StaticActivityDataMgr.getActLogin(siginInfo.getActivityId(), siginInfo.getTimes() + 1);
            if (sActLogin != null) {
                List<List<Integer>> signInAward = sActLogin.getAwardList();
                if (signInAward.get(0).get(0) == AwardType.HERO && player.heros.get(signInAward.get(0).get(1)) != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
