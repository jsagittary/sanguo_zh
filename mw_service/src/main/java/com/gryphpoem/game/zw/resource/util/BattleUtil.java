package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.pojo.p.FightRecord;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-21 19:05
 */
public class BattleUtil {
    public static float addPlayerMilitary(int battleType, Force force) {
        switch (battleType) {
            case WorldConstant.BATTLE_TYPE_CITY:
                return force.totalLost * Constant.SYSTEM_ID_391;
            case WorldConstant.BATTLE_TYPE_CAMP:
                return force.killed * Constant.SYSTEM_ID_390;
            default:
                return force.totalLost * Constant.SYSTEM_ID_392;
        }
    }

    public static int addResourceAward(FightRecord record, int resourceType) {
        switch (resourceType) {
            case AwardType.Resource.OIL:
                return (int) Math.ceil(20000 + (record.getKilled() + record.getLost()) * Constant.SYSTEM_ID_393);
            case AwardType.Resource.ELE:
                return (int) (12000 + (record.getKilled() + record.getLost()) * Constant.SYSTEM_ID_393);
        }

        return 0;
    }
}
