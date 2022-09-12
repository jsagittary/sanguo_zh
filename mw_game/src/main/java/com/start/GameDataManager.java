package com.start;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.SuperMineDataManager;
import com.gryphpoem.game.zw.manager.WarDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.PlayerHero;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.service.BerlinWarService;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * @ClassName GameDataManager.java
 * @Description 所有数据加载完成后，数据检查，数据管理，或临时bug数据修复等功能的集中处理类
 * @author TanDonghai
 * @date 创建时间：2017年3月17日 下午4:10:29
 *
 */
public class GameDataManager {
	private static GameDataManager instance = new GameDataManager();

    private GameDataManager() {
    }

    public static GameDataManager getIns() {
        return instance;
    }

    /**
     * 数据加载完成后，数据处理逻辑入口
     * 
     * @throws MwException
     */
    public void dataHandle() throws MwException {
        LogUtil.start("数据处理逻辑开始");
        // 处理数据加载完成后，数据检查、计算、整理等相关操作
        calculateLogic();

        // 处理常驻的数据修复或检查逻辑
        usualLogic();

        // 处理临时BUG等只使用一次的逻辑，使用后不需要常驻的注释掉
        repairGameDataLogic();

        //非bug處理，處理礦點數據填充
        fillingMineData();

        // 处理开服时充值足够，没有专属客服数据
        handleActDedicatedCustomer();

        //处理被合服删除的活动
        handleRemovedAct();

        // 填充武将列传初始信息
        fillingInitHeroBiography();

        LogUtil.start("数据处理逻辑结束");
    }

    /**
     * 处理开服时充值足够，没有专属客服数据
     *
     * @throws MwException
     */
    public void handleActDedicatedCustomer() throws MwException {
        PlayerDataManager playerDataManager = DataResource.getBean(PlayerDataManager.class);
        if (CheckNull.isEmpty(playerDataManager.getAllPlayer())) {
            return;
        }

        int actType = ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (activityBase == null) {
            return;
        }

        long now = TimeHelper.getCurrentSecond() * 1l;
        Date beginTime = activityBase.getBeginTime();
        int begin = TimeHelper.getDay(beginTime);
        playerDataManager.getAllPlayer().values().forEach(player -> {
            if (player.lord.getTopup() < ActParamConstant.ACT_DEDICATED_CUSTOMER_SERVICE_CONF.get(0).get(0)) {
                return;
            }
            Activity activity = player.activitys.get(actType);
            if (activity == null) {
                activity = new Activity(activityBase, begin);
                player.activitys.put(actType, activity);
                activity.setEndTime(TimeHelper.getCurrentDay());
            } else {
                activity.isReset(begin, player);// 是否重新设置活动
                activity.autoDayClean(activityBase);
            }
            Long time = activity.getStatusCnt().getOrDefault(0, 0L);
            if (time == 0) {
                activity.getStatusCnt().put(0, now);
            }
        });
    }

    public void fillingMineData() throws MwException {
        if (!GameGlobal.needInitMineData)
            return;

        PlayerDataManager playerDataManager = DataResource.getBean(PlayerDataManager.class);
        if (ObjectUtils.isEmpty(playerDataManager.getAllPlayer())) {
            return;
        }

        WorldDataManager worldDataManager = DataResource.getBean(WorldDataManager.class);
        for (Player player : playerDataManager.getAllPlayer().values()) {
            if (ObjectUtils.isEmpty(player.armys))
                continue;

            for (Army army : player.armys.values()) {
                if (army.getType() != ArmyConstant.ARMY_TYPE_COLLECT || army.getState() != ArmyConstant.ARMY_STATE_MARCH)
                    continue;

                worldDataManager.addCollectMine(player.lord.getLordId(), army.getTarget());
            }
        }
    }

    public void handleRemovedAct() throws MwException {
        StaticActivityDataMgr.handleRemovedAct();
    }

    /**
     * 需要等到所有玩家数据加载完成后，才能并且必须马上执行的逻辑，注册在这里按次序执行
     * 
     * @throws MwException
     */
    private void calculateLogic() throws MwException {
        try {
            WorldDataManager worldDataManager = AppGameServer.ac.getBean(WorldDataManager.class);
            // 计算空余的位置（世界地图中的坐标）集合
            worldDataManager.calcFreePostList();
            LogUtil.start("计算空余坐标逻辑执行完成");

            // 补充世界进图的流寇、矿点
            worldDataManager.refreshAllBandit(WorldConstant.REFRESH_TYPE_BANDIT_2);
            worldDataManager.refreshAllMine(WorldConstant.REFRESH_TYPE_MINE_1);
            LogUtil.start("补充世界地图流寇和矿点执行完成");

            worldDataManager.loadArmy();
            LogUtil.start("行军路线载入完成");

			// 已经不需要检测炮点
			// AppGameServer.ac.getBean(WorldService.class).checkBerlinBuilingHasOther();
			// LogUtil.start("检测炮点,军事禁区空余点完成");

            AppGameServer.ac.getBean(WarDataManager.class).initBattle();
            LogUtil.start("城战、阵营战等战斗信息初始化");

            // 新开区域的检查
            worldDataManager.initNewWorldData();
            LogUtil.start("初始化新区域的配置,完成");

            AppGameServer.ac.getBean(SuperMineDataManager.class).loadSuperMineArmy();
            LogUtil.common("超级矿点加载完成");

        } catch (Exception e) {
            throw new MwException("数据处理逻辑出错", e);
        }
    }

    /**
     * 一些偶然出现的，非必现问题的修复，需要常驻的逻辑，在这里统一执行
     * 
     * @throws MwException
     */
    private void usualLogic() throws MwException {
        try {
            // 错误玩家pos重新随机
            AppGameServer.ac.getBean(WorldDataManager.class).randomEmptyPosInArea(1);
            LogUtil.start("错误玩家坐标重新随机逻辑执行完成");
            BerlinWarService berlinWarService = AppGameServer.ac.getBean(BerlinWarService.class);
            berlinWarService.retreatBerlinAllArmy();
        } catch (Exception e) {
            throw new MwException("常驻的数据修复或检查逻辑出错", e);
        }
    }

	/**
	 * 临时BUG，必须马上解决线上问题的修复逻辑，在这里执行
	 * 
	 * @throws MwException
	 */
	private void repairGameDataLogic() throws MwException {
		try {
			// 2018-01-09 修改战斗力公式后,强制刷新全服所有玩家战斗力
			// AppGameServer.ac.getBean(PlayerService.class).reCalcFightAllPlayer();
			// AppGameServer.ac.getBean(PlayerService.class).checkHasHeroGivePortrait();
			// LogUtil.start("修复全服玩家头像......");
			
			AppGameServer.ac.getBean(WorldScheduleService.class).clearBossPos();
			LogUtil.start("检测世界boss位置是否被占用完毕");

//            HeroService heroSrv = DataResource.ac.getBean(HeroService.class);
//            Map<String, Player> players = DataResource.ac.getBean(PlayerDataManager.class).getAllPlayer();
//            for (Map.Entry<String, Player> entry : players.entrySet()) {
//                heroSrv.checkAndRepaireHero(entry.getValue(), false);
//            }

		} catch (Exception e) {
			throw new MwException("临时修复逻辑出错", e);
		}
	}

    /**
     * 填充武将列传初始化信息
     *
     * @throws MwException
     */
	private void fillingInitHeroBiography() throws MwException {
        Optional.ofNullable(DataResource.ac.getBean(PlayerDataManager.class).getAllPlayer()).ifPresent(allPlayer -> {
            allPlayer.values().forEach(player -> {
                if (CheckNull.isNull(player)) return;
                if (Objects.nonNull(player.playerHero)) return;
                player.playerHero = new PlayerHero(player.roleId);
            });
        });
    }
}
