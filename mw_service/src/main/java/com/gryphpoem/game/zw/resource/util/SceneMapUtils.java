package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkinStar;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 行军时间计算
 * @Description
 * @Author zhangdh
 * @Date 2021-04-26 10:52
 */
public final class SceneMapUtils {

    /**
     * 计算玩家行军到目标坐标需要的时间 行军时间（秒）=8*（|X差|+|Y差|）*（1-行军加速_科技[%])*(1-行军加速_道具[%])/(1+军曹官加成[%]） 向上取整
     *
     * @param player 玩家
     * @param pos1   出发点
     * @param pos2   目标点
     * @return
     */
    public static int marchTime(Player player, int pos1, int pos2) {
        int distance = MapHelper.calcDistance(pos1, pos2);

        int time = distance * Constant.MARCH_TIME_RATIO;
        double speed = calcSpeed(player);
        double marchCd = marchCd(player);
        int finalTime = Math.max((int) Math.ceil((time / (1+speed)) * marchCd), 1);
        LogUtil.debug(String.format("行军距离 :%d, 行军初始时间 :%d, 加速后时间 :%d", distance, time, finalTime));
        return finalTime;
    }

    private static double calcSpeed(Player player) {
        TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
        // 科技加成
        double addRatio = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_6);
        // 柏林官员
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_MARCH_TIME);
        // 赛季天赋:行军加速
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_301);
        return (addRatio  + berlinJobEffect + seasonTalentEffect) / Constant.TEN_THROUSAND;
    }

    private static double marchCd(Player player) {
        // buff加成
        Effect effect = player.getEffect().get(EffectConstant.WALK_SPEED);
        double addRatio1 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.WALK_SPEED_HIGHT);
        // 军曹官
        double addRatio2 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.PREWAR_WALK_SPEED);
        // 柏林战前buff
        double addRatio3 = effect != null ? effect.getEffectVal() : 0;
        // 皮肤Buff加成
        List<StaticCastleSkin> staticCastleSkinList = player.getOwnCastleSkin().stream().map(StaticLordDataMgr::getCastleSkinMapById).filter(staticCastleSkin -> staticCastleSkin.getEffectType() == 4).collect(Collectors.toList());
        int skinAdd = 0;
        for (StaticCastleSkin o : staticCastleSkinList) {
            int star = player.getCastleSkinStarById(o.getId());
            StaticCastleSkinStar staticCastleSkinStar = StaticLordDataMgr.getCastleSkinStarById(o.getId() * 100 + star);
            skinAdd += staticCastleSkinStar.getEffectVal();
        }
        int addRatio5 = skinAdd;

        return (1 - addRatio1 / Constant.TEN_THROUSAND)
                * (1 - addRatio2 / Constant.TEN_THROUSAND)
                * (1 - addRatio3 / Constant.TEN_THROUSAND)
                * (1 - addRatio5 / Constant.TEN_THROUSAND);
    }
}
