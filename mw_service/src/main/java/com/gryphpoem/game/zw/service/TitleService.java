package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticTitle;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.TitleEntity;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangzxy
 * @date 创建时间:2021/12/16
 * @Description
 */
@Service
public class TitleService implements LoginService, GmCmdService {

	@Autowired
	private PlayerDataManager playerDataManager;

	@Autowired
	private DressUpDataManager dressUpDataManager;
	
	@Autowired
	private RewardDataManager rewardDataManager;
	/**
	 * 处理任务进度
	 * @param player
	 * @param eTask
	 * @param params
	 */
	public void processTask(Player player, ETask eTask, int... params) {
		Map<Integer, BaseDressUpEntity> dressUpByType = dressUpDataManager.getDressUpByType(player, AwardType.TITLE);
		List<StaticTitle> titleList = StaticLordDataMgr.getTitleTypeMapByTypeId(eTask.getTaskType());
		if (null == titleList || titleList.isEmpty()) {
			return;
		}
		AtomicBoolean unlock = new AtomicBoolean(false);
		titleList.forEach(title -> {
			if (null == title.getTaskParam() || title.getTaskParam().isEmpty()) {
				return;
			}
			//如果玩家装扮上没有，加入进去一个未解锁的称号。
			if (null == dressUpByType.get(title.getId())) {
				TitleEntity titleEntity = new TitleEntity(title.getId(), false);
				dressUpByType.put(title.getId(), titleEntity);
			}
			TitleEntity titleEntity = (TitleEntity) dressUpByType.get(title.getId());
			if (checkTaskCondition(player, titleEntity, eTask, title, params)) {
				boolean b = checkFinishTaskUnlock(player, titleEntity, eTask, title);
				if (b && !titleEntity.isPermanentHas()) {
					if (title.getDuration() > 0) {
						rewardDataManager.addAward(player, AwardType.TITLE, title.getId(), Math.toIntExact(title.getDuration()), AwardFrom.TITLE_GET_FOR_TASK);
					}else {
						titleEntity.setPermanentHas(true);
						titleEntity.setDuration(0);
						// 同步变动
						dressUpDataManager.syncDressUp(player, titleEntity, DressUpDataManager.CREATE_EVENT);
						LogLordHelper.dressUp(player, AwardFrom.TITLE_GET_FOR_TASK, titleEntity, "CreateTitle", 0, params);
					}
				}
				if (!unlock.get() && b == true) {
					unlock.set(true);
				}
			}
		});
		if (unlock.get()) {
			//解锁了需要重新计算战斗力
			CalculateUtil.reCalcAllHeroAttr(player);
		}
	}

	/**
	 * 刷新所有任务称号的状态
	 */
	public void refreshAllTaskTitle(Player player) {
		Map<Integer, BaseDressUpEntity> dressUpByType = dressUpDataManager.getDressUpByType(player, AwardType.TITLE);
		Map<Integer, List<StaticTitle>> titleTaskMap = StaticLordDataMgr.getTitleTaskMap();
		titleTaskMap.values().forEach(titleList -> {
			titleList.forEach(title -> {
				if (null == dressUpByType.get(title.getId())) {
					TitleEntity titleEntity = new TitleEntity(title.getId(), false);
					dressUpByType.put(title.getId(), titleEntity);
				}
				if (null != title.getTaskId() && title.getTaskId() > 0) {
					//如果玩家装扮上没有，加入进去一个未解锁的称号。
					TitleEntity titleEntity = (TitleEntity) dressUpByType.get(title.getId());
					boolean b = checkFinishTaskUnlock(player, titleEntity, ETask.getByType(title.getTaskId()), title);
					//解锁了
					if (titleEntity.isPermanentHas()) {
						//但是重新刷新没有解锁,将状态变为未解锁
						if (!b) {
							titleEntity.setPermanentHas(false);
							if(titleEntity.getDuration()>0){
								dressUpDataManager.subDressUp(player, AwardType.TITLE, title.getId(), titleEntity.getDuration(), AwardFrom.TITLE_LOSE_FOR_TASK);
							}else{
								dressUpDataManager.subDressUp(player, AwardType.TITLE, title.getId(), 0, AwardFrom.TITLE_LOSE_FOR_TASK);
							}
						}
					} else {
						//但是重新刷新解锁,将状态变为解锁
						if (b) {
							if (title.getDuration() > 0) {
								rewardDataManager.addAward(player, AwardType.TITLE, title.getId(), Math.toIntExact(title.getDuration()), AwardFrom.TITLE_GET_FOR_TASK);
							}else{
								titleEntity.setPermanentHas(true);
								titleEntity.setDuration(0);
								// 同步变动
								dressUpDataManager.syncDressUp(player, titleEntity, DressUpDataManager.CREATE_EVENT);
								LogLordHelper.dressUp(player, AwardFrom.TITLE_GET_FOR_TASK, titleEntity, "CreateTitle", 0);
							}
						}
					}
				}
			});
		});
	}

	private boolean checkTaskCondition(Player player, TitleEntity titleEntity, ETask eTask, StaticTitle title, int... params) {
		boolean b = false;
		//原本数量大于等于任务需求数量直接跳过,任务属于不需要计数
		if (titleEntity.getProgress() >= title.getTaskParam().get(0) && !eTask.isHandle()) {
			return true;
		}
		long countLong = 0L;
		switch (eTask) {
			case FIGHT_REBEL:
			case FIGHT_ELITE_REBEL:
			case DAILY_LOGIN:
			case APPOINTMENT:
			case BUILD_CAMP:
			case FINISHED_DAILYTASK:
				titleEntity.setProgress(titleEntity.getProgress() + 1);
				b = true;
				break;
			case FIRST_USE_DIAMOND:
				if (params[0] >= title.getTaskParam().get(0)) {
					titleEntity.setProgress(title.getTaskParam().get(0));
					b = true;
				}
				break;
			case CITY_FIRSTKILLED:
				titleEntity.setProgress(1);
				b = true;
				break;
			case PASS_BARRIER:
			case PASS_EXPEDITION:
			case CONSUME_DIAMOND:
			case RECHARGE_DIAMOND:
			case DEATH_NUMBER:
			case KILLED_NUMBER:
			case GOLDEN_AUTUMN_CATCH_FISH:
				titleEntity.setProgress(titleEntity.getProgress() + params[0]);
				b = true;
				break;
			case APPRENTICE_LEVEL_MAKE_IT:
				countLong = player.apprentices.values().stream().filter(apprentice -> {
					if (apprentice.getRelation() == 1 && apprentice.getStaus() == 1) {
						Player playerApprentice = playerDataManager.getPlayer(apprentice.getLordId());
						if (playerApprentice.lord.getLevel() >= title.getTaskParam().get(1)) {
							return true;
						}
					}
					return false;
				}).count();
				if (countLong > titleEntity.getProgress()) {
					titleEntity.setProgress(countLong);
					b = true;
				}
				break;
			case CASTLE_SKIN_NUM:
				Map<Integer, BaseDressUpEntity> dressUpByType = dressUpDataManager.getDressUpByType(player, AwardType.CASTLE_SKIN);
				countLong = dressUpByType.values().stream().filter(castleSkin -> {
					if (castleSkin.isPermanentHas() || castleSkin.getDuration() > 0) {
						return true;
					}
					return false;
				}).count();
				if (countLong > titleEntity.getProgress()) {
					titleEntity.setProgress(countLong);
					b = true;
				}
				break;
			case LOGIN_DAYS_SUM:
				int loginDays = player.account.getLoginDays();
				if (loginDays != titleEntity.getProgress()) {
					titleEntity.setProgress(loginDays);
					b = true;
				}
				break;
			default:
		}
		return b;
	}

	@Override
	public void afterLogin(Player player) {
		//玩家登陆后刷新一下任务进度。非增加计数类型
		for (ETask value : ETask.values()) {
			if (value.isHandle()) {
				processTask(player, value);
			}
		}
		refreshAllTaskTitle(player);
	}

	/**
	 * 检查是否完成任务，是否解锁
	 * @param player
	 * @param eTask
	 */
	public boolean checkFinishTaskUnlock(Player player, TitleEntity titleEntity, ETask eTask, StaticTitle title) {
		boolean unlock = false;
		int count;
		switch (eTask) {
			case FIGHT_REBEL:
			case FIGHT_ELITE_REBEL:
			case DAILY_LOGIN:
			case APPOINTMENT:
			case BUILD_CAMP:
			case CITY_FIRSTKILLED:
			case GOLDEN_AUTUMN_CATCH_FISH:
			case FINISHED_DAILYTASK:
			case APPRENTICE_LEVEL_MAKE_IT:
			case CASTLE_SKIN_NUM:
			case FIRST_USE_DIAMOND:
			case LOGIN_DAYS_SUM:
				count = (int) (titleEntity.getProgress() / title.getTaskParam().get(0));
				if (count > 0) {
					unlock = true;
				}
				break;
			default:
		}
		return unlock;
	}

	@GmCmd("dressUpNew")
	@Override
	public void handleGmCmd(Player player, String... params) throws Exception {
		if (params[0].equalsIgnoreCase("getDressUpDataRq")) {
			int type = Integer.parseInt(params[1]);
			DressUpService service = DataResource.ac.getBean(DressUpService.class);
			GamePb4.GetDressUpDataRs resp = service.getDressUp(player.getLordId(), type);
			if (resp != null) {
				System.out.println(resp.toString());
			}
		}
		int titleId = Integer.parseInt(params[1]);
		Map<Integer, BaseDressUpEntity> dressUpByType = dressUpDataManager.getDressUpByType(player, AwardType.TITLE);
		StaticTitle title = StaticLordDataMgr.getTitleMapById(titleId);
		if (params[0].equalsIgnoreCase("titleAdd")) {
			//针对主城皮肤称号做跳过处理
			if(titleId==107){
				return;
			}
			//如果玩家装扮上没有，加入进去一个未解锁的称号。
			if (null == dressUpByType.get(title.getId())) {
				TitleEntity titleEntity = new TitleEntity(title.getId(), false);
				dressUpByType.put(title.getId(), titleEntity);
			}
			TitleEntity titleEntity = (TitleEntity) dressUpByType.get(title.getId());
			if (null != title.getTaskId() && title.getTaskId() > 0) {
				if (title.getTaskParam() != null && title.getTaskParam().size() > 0) {
					Integer taskSum = title.getTaskParam().get(title.getTaskParam().size() - 1);
					titleEntity.setProgress(taskSum);
				}
				boolean b = checkFinishTaskUnlock(player, titleEntity, ETask.getByType(title.getTaskId()), title);
				if (b) {
					titleEntity.setPermanentHas(true);
					titleEntity.setDuration(0);
					// 同步变动
					dressUpDataManager.syncDressUp(player, titleEntity, DressUpDataManager.CREATE_EVENT);
					CalculateUtil.reCalcAllHeroAttr(player);
				}
			} else {
				if (title.getDuration()>0){
					rewardDataManager.addAward(player, AwardType.TITLE, title.getId(), Math.toIntExact(title.getDuration()), AwardFrom.GM_SEND);
				}else{
					titleEntity.setPermanentHas(true);
					titleEntity.setDuration(0);
					// 同步变动
					dressUpDataManager.syncDressUp(player, titleEntity, DressUpDataManager.CREATE_EVENT);
					CalculateUtil.reCalcAllHeroAttr(player);
				}
			}
		} else if (params[0].equalsIgnoreCase("titleSub")) {
			//如果玩家装扮上没有，加入进去一个未解锁的称号。
			if (null == dressUpByType.get(title.getId())) {
				TitleEntity titleEntity = new TitleEntity(title.getId(), false);
				dressUpByType.put(title.getId(), titleEntity);
			}
			TitleEntity titleEntity = (TitleEntity) dressUpByType.get(title.getId());
			titleEntity.setProgress(0);
			if (title.getDuration()>0){
				dressUpDataManager.subDressUp(player, AwardType.TITLE, title.getId(), titleEntity.getDuration(), AwardFrom.GM_SEND);
			}else{
				dressUpDataManager.subDressUp(player, AwardType.TITLE, title.getId(), 0, AwardFrom.TITLE_LOSE_FOR_TASK);
			}
		}
	}

}
