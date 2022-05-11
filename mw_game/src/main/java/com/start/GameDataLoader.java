package com.start;

import com.gryphpoem.game.zw.server.AppGameServer;
import com.gryphpoem.game.zw.server.HotUpdateService;
import com.hotfix.GameAgent;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.server.HotfixService;
import com.gryphpoem.game.zw.service.LoadService;
import com.gryphpoem.game.zw.service.WorldScheduleService;

/**
 * @ClassName GameDataLoader.java
 * @Description 游戏数据统一加载类
 * @author TanDonghai
 * @date 创建时间：2017年3月17日 上午11:29:20
 *
 */
public class GameDataLoader {

    private static GameDataLoader instance = new GameDataLoader();

    private GameDataLoader() {
    }

    public static GameDataLoader getIns() {
        return instance;
    }

    /**
     * 游戏启动时，按次序加载数据
     * 
     * @throws MwException
     */
    public void loadGameData() throws MwException {
        // 加载配置文件数据
        loadFileData();

        // 加载常量数据
        loadConstant();

        // 加载玩家相关数据
        loadPlayerData();
    }

    private void loadFileData() {
        LogUtil.start("**********开始加载配置文件数据**********");
        // 加载配置文件
        LogUtil.start("**********加载配置文件数据完成**********");
    }

    private void loadConstant() throws MwException {
        LogUtil.start("**********开始加载配置表数据**********");
        try {
            LoadService loadService = AppGameServer.ac.getBean(LoadService.class);
            loadService.loadAll();
            loadService.checkValid();
        } catch (Exception e) {
            throw new MwException("加载配置表数据出错", e);
        }
        LogUtil.start("**********配置表数据加载完成**********");
    }

    private void loadPlayerData() throws MwException {
        LogUtil.start("**********开始加载用户相关数据**********");
        try {
            // 加载小号数据
            AppGameServer.ac.getBean(SmallIdManager.class).init();
            LogUtil.start("加载完成：小号数据");

            // 加载机器人
            AppGameServer.ac.getBean(RobotDataManager.class).load();
            LogUtil.start("加载完成：机器人");

            // 加载全局公用数据
            AppGameServer.ac.getBean(GlobalDataManager.class).init();
            LogUtil.start("加载完成：global数据");

            // 加载世界地图相关数据
            AppGameServer.ac.getBean(WorldDataManager.class).init();
            LogUtil.start("加载完成：世界地图数据");

            // 加载玩家角色数据
            AppGameServer.ac.getBean(PlayerDataManager.class).init();
            LogUtil.start("加载完成：角色相关数据");

            // 加载军团数据
            AppGameServer.ac.getBean(CampDataManager.class).init();
            LogUtil.start("加载完成：军团相关数据");

            // 加载聊天数据
            AppGameServer.ac.getBean(ChatDataManager.class).init();
            LogUtil.start("加载完成：玩家聊天数据");

            // 加载活动数据
            AppGameServer.ac.getBean(ActivityDataManager.class).init();
            LogUtil.start("加载完成：活动数据");

            // 排行榜
            AppGameServer.ac.getBean(RankDataManager.class).init();
            LogUtil.start("加载完成：排行数据");

            // 世界进度
            AppGameServer.ac.getBean(WorldScheduleService.class).init();
            LogUtil.start("加载完成：世界进程数据");

            // 加载机器人数据
            AppGameServer.ac.getBean(RobotDataManager.class).init();
            LogUtil.start("加载完成：机器人数据");

            AppGameServer.ac.getBean(HotfixService.class).init();
            AppGameServer.ac.getBean(HotUpdateService.class).init();
            LogUtil.start("服务器热更已启动");
            
            // 本地跨服数据加载
            AppGameServer.ac.getBean(CrossWorldMapDataManager.class).init();
            LogUtil.start("本地跨服数据加载");
            
            LogUtil.start("服务器热更钩子 game-agent : "+ GameAgent.inst);
        } catch (Exception e) {
            throw new MwException("加载玩家数据失败", e);
        }
        LogUtil.start("**********用户相关数据加载完成**********");
    }
}
