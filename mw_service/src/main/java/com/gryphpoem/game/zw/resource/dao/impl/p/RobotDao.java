package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.Robot;

/**
 * @Description 机器人相关
 * @author TanDonghai
 * @date 创建时间：2017年9月20日 下午2:03:13
 *
 */
public class RobotDao extends BaseDao {

    public Map<Long, Robot> selectRobotMap() {
        return getSqlSession().selectMap("RobotDao.selectRobotMap", "roleId");
    }

    public void insertRobot(Robot robot) {
        getSqlSession().insert("RobotDao.insertRobot", robot);
    }
    
    public void updateRobot(Robot robot) {
        getSqlSession().update("RobotDao.updateRobot", robot);
    }
}
