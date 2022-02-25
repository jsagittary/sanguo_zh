package com.gryphpoem.game.zw.robot.work;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.constant.RobotConstant;
import com.gryphpoem.game.zw.resource.domain.p.Robot;

/**
 * @Description 机器人工作线程
 * @author TanDonghai
 * @date 创建时间：2017年11月15日 下午2:04:08
 *
 */
public class RobotWorker implements Runnable {
    private Robot robot;

    public RobotWorker(Robot robot) {
        this.robot = robot;
    }

    @Override
    public void run() {
        if (null != robot && null != robot.getDefaultTree()) {
            LogUtil.robot("开始执行机器人逻辑, robot:" + robot.getRoleId());
            long start = System.currentTimeMillis();

            robot.getDefaultTree().tick();

            long exeTime = System.currentTimeMillis() - start;
            if (exeTime > RobotConstant.ROBOT_EXECUTE_THRESHOLD) {
                LogUtil.robot("机器人逻辑执行时间cost(毫秒):", exeTime, ", robot:", robot.getRoleId());
            }
        }
    }

}
