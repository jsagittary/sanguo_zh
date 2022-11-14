package com.gryphpoem.game.zw.util;

import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pojo.p.Force;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-14 19:08
 */
public class FightPbUtil {
    /**
     * 创建开场pb
     *
     * @param force
     * @param target
     * @return
     */
    public static BattlePb.BattlePreparationStage.Builder createBattlePreparationStagePb(Force force, Force target) {
        BattlePb.BattlePreparationStage.Builder builder = BattlePb.BattlePreparationStage.newBuilder();
        builder.setTotalArms(createDataInt(force.hp, target.hp));
        builder.addArrangeArms(createDataInt(force.curLine + 1, force.count));
        builder.addArrangeArms(createDataInt(target.curLine + 1, target.count));
        return builder;
    }

    public static BattlePb.BothBattleEntityPb.Builder createBothBattleEntityPb(Force force, Force target) {
        BattlePb.BothBattleEntityPb.Builder builder = BattlePb.BothBattleEntityPb.newBuilder();
        
    }

    public static BattlePb.BattleEntityPb.Builder createBattleEntityPb(Force force) {
        BattlePb.BattleEntityPb.Builder builder = BattlePb.BattleEntityPb.newBuilder();
        return builder;
    }

    public static BattlePb.DataInt createDataInt(int v1, int v2) {
        return BattlePb.DataInt.newBuilder().setV1(v1).setV2(v2).build();
    }
}
