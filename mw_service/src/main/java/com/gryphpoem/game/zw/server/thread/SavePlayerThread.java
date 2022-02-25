package com.gryphpoem.game.zw.server.thread;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.thread.SaveThread;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RobotDataManager;
import com.gryphpoem.game.zw.resource.domain.Role;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SavePlayerThread extends SaveThread {
    // 命令执行队列
    private LinkedBlockingQueue<Long> role_queue = new LinkedBlockingQueue<Long>();

    private HashMap<Long, Role> role_map = new HashMap<Long, Role>();

    private PlayerDataManager playerDataManager;

    private RobotDataManager robotDataManager;

    private static int MAX_SIZE = 10000;

//    private LinkedBlockingQueue<Role> retryQueue = new LinkedBlockingQueue<>();

    public SavePlayerThread(String threadName) {
        super(threadName);
        this.playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        robotDataManager = DataResource.ac.getBean(RobotDataManager.class);
    }

    public void run() {
        stop = false;
        done = false;
        while (!stop || role_queue.size() > 0) {
            Role role = null;
            synchronized (this) {
                Object o = role_queue.poll();
                if (o != null) {
                    long roleId = (Long) o;
                    role = role_map.remove(roleId);
                }
            }
            if (role == null) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    LogUtil.error(threadName + " Wait Exception:" + e.getMessage(), e);
                }
            } else {
                if (role_queue.size() > MAX_SIZE) {
                    role_queue.clear();
                    role_map.clear();
                    LogUtil.error2Sentry(String.format("role_queue.size: %d, roleMap.size: %d 超过保存玩家队列长度限定值: %d, 清空队列!!!",
                            role_queue.size(), role_map.size(), MAX_SIZE));
                }

                try {
                    playerDataManager.updateRole(role);
                    if (logFlag) {
                        saveCount++;
                        LogUtil.common("停服保存玩家成功roleId=" + role.getRoleId());
                    }
                } catch (Exception e) {
                    LogUtil.error("Role Exception:" + role.getRoleId(), e);
                    LogUtil.warn("Role save Exception:" + role.getRoleId());
                    LogUtil.common("停服保存玩家失败roleId=" + role.getRoleId());
                    this.add(role);
//                    retryQueue.add(role);
                }
            }
        }

        // 进程退出时保存所有机器人数据
        try {
            robotDataManager.saveAllRobot();
        } catch (Exception e) {
            LogUtil.error("Save All Robot Exception", e);
        }

        done = true;
    }

    @Override
    public void add(Object object) {
        try {
            Role role = (Role) object;
            synchronized (this) {
                if (!role_map.containsKey(role.getRoleId())) {
                    this.role_queue.add(role.getRoleId());
                }
                this.role_map.put(role.getRoleId(), role);
                LogUtil.debug(String.format("role_queue.size: %d, roleMap.size: %d", role_queue.size(), role_map.size()));
                notify();
            }
        } catch (Exception e) {
            LogUtil.error(threadName + " Notify Exception:" + e.getMessage(), e);
        }
    }

}
