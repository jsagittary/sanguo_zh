package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import org.springframework.stereotype.Service;

/**
 * @author zhangzxy
 * @date 创建时间:2021/12/6
 * @Description
 */
@Service
public class FirstPayResetService extends AbsActivityService {

	private int[] actTypes = { ActivityConst.ACT_FIRST_PAY_RESET};

	@Override
	protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
		return null;
	}

	@Override
	protected int[] getActivityType() {
		return actTypes;
	}

	@Override
	protected void handleOnBeginTime(int activityType, int activityId, int keyId) {

	}

	@Override
	protected void handleOnEndTime(int activityType, int activityId, int keyId) {
		long serverTime = DateHelper.getServerTime();
		playerDataManager.getPlayers().values().forEach(player -> {
				String firstPayDoubleString = player.firstPayDouble.toString();
				player.firstPayDouble.clear();
				LogUtil.activity("活动类型:" + activityType + "-活动id:" + activityId + "的玩家首充重置,重置时间:" + serverTime + ",玩家id:" + player.getLordId() + ",原本首充信息为:" + firstPayDoubleString);
		});
	}

	@Override
	protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

	}

	@Override
	protected void handleOnDay(Player player) {

	}

}
