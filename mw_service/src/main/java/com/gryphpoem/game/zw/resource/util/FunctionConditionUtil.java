package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunctionCondition;
import com.gryphpoem.game.zw.resource.pojo.function.AbstractCondition;
import com.gryphpoem.game.zw.resource.pojo.function.condition.CityLvCondition;
import com.gryphpoem.game.zw.resource.pojo.function.condition.CombatCondition;
import com.gryphpoem.game.zw.resource.pojo.function.condition.IpCondition;
import com.gryphpoem.game.zw.resource.pojo.function.condition.PartyLvCondition;
import com.gryphpoem.game.zw.resource.pojo.function.condition.RoleLvCondition;
import com.gryphpoem.game.zw.resource.pojo.function.condition.TaskCondition;
import com.gryphpoem.game.zw.resource.pojo.function.condition.TechLvCondition;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;

/**
 * @Description 功能解锁条件工具类
 * @author TanDonghai
 * @date 创建时间：2017年6月19日 下午12:38:17
 *
 */
public class FunctionConditionUtil {
    /**
     * 根据功能解锁条件配置信息，创建具体的条件判断实例
     * 
     * @param config
     * @return 返回条件对象，当条件类型未找到时，会返回NULL
     * @throws MwException 初始化条件对象时，可能抛出异常
     */
    public static AbstractCondition getCondition(StaticFunctionCondition config) throws MwException {
        int type = config.getUnlockType();
        switch (type) {
            case FunctionConstant.UNLOCK_TYPE_IP:// IP白名单
                return new IpCondition(config);

            case FunctionConstant.UNLOCK_TYPE_ROLE_LV:// 玩家等级
                return new RoleLvCondition(config);

            case FunctionConstant.UNLOCK_TYPE_CITY_LV:// 司令部等级
                return new CityLvCondition(config);

            case FunctionConstant.UNLOCK_TYPE_TASK:// 任务
                return new TaskCondition(config);

            case FunctionConstant.UNLOCK_TYPE_COMBAT:// 关卡
                return new CombatCondition(config);

            case FunctionConstant.UNLOCK_TYPE_TECH_LV:// 科研所等级
                return new TechLvCondition(config);

            case FunctionConstant.UNLOCK_TYPE_PARTY_LV:// 军团等级
                return new PartyLvCondition(config);

            default:
                break;
        }
        return null;
    }

    /**
     * 根据功能解锁条件类型返回角色对应的数据
     * 
     * @param roleId
     * @param type 条件类型
     * @return 当找不到角色信息或条件类型时，会返回null
     */
    public static Object getConditionCheckData(long roleId, int type) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.getPlayer(roleId);
        if (null == player) {
            return null;
        }

        switch (type) {
            case FunctionConstant.UNLOCK_TYPE_IP:// IP
                return ChannelUtil.getIp(player.ctx);

            case FunctionConstant.UNLOCK_TYPE_ROLE_LV:// 玩家等级
                return player.lord.getLevel();

            case FunctionConstant.UNLOCK_TYPE_CITY_LV:// 司令部等级
                return player.building.getCommand();

            case FunctionConstant.UNLOCK_TYPE_TASK:// 任务
                return player.chapterTask.getOpenTasks().keySet();

            case FunctionConstant.UNLOCK_TYPE_COMBAT:// 关卡
                return player.combats.keySet();

            case FunctionConstant.UNLOCK_TYPE_TECH_LV:// 科研所等级
                return player.building.getTech();

            case FunctionConstant.UNLOCK_TYPE_PARTY_LV:// 军团等级
                CampDataManager campDataManager = DataResource.ac.getBean(CampDataManager.class);
                Camp camp = campDataManager.getParty(player.lord.getCamp());
                return camp.getPartyLv();

            default:
                break;
        }
        return null;
    }
}
