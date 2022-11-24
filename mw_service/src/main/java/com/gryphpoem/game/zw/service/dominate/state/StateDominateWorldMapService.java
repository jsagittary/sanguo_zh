package com.gryphpoem.game.zw.service.dominate.state;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.service.dominate.abs.AbsDominateWorldMapService;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Description: 州郡雄踞一方
 * Author: zhangpeng
 * createTime: 2022-11-22 21:26
 */
@Component
public class StateDominateWorldMapService extends AbsDominateWorldMapService {

    @Override
    protected void doRecoverArmy(Player player, Army retreatArmy, Map<Integer, Integer> recoverMap, Map<Long, ChangeInfo> changeMap) {
        for (CommonPb.TwoInt twoInt : retreatArmy.getHero()) {
            Hero hero = player.heros.get(twoInt.getV1());
            Integer recoverHp = recoverMap.remove(hero.getHeroId());
            if (Objects.isNull(recoverHp) || recoverHp <= 0) continue;
            hero.addArm(recoverHp);
            if (Objects.nonNull(changeMap)) {
                ChangeInfo info = changeMap.computeIfAbsent(player.roleId, k -> ChangeInfo.newIns());
                info.addChangeType(AwardType.HERO_ARM, hero.getHeroId());
            }
        }
    }

    @Override
    protected void attackFightSuccess(FightLogic fightLogic, DominateSideCity sideCity, CommonPb.Report.Builder report, Player attackPlayer, Army army, Player defendPlayer, int nowSec) {
        army.setState(ArmyConstant.ARMY_STATE_STATE_DOMINATE_HOLDER);
        // 连杀广播
        int killCnt = StateDominateWorldMap.getInstance().incContinuousKillCnt(army.getLordId(), sideCity.getCityId());
        continuousKillBroadcast(attackPlayer, killCnt);

        String atkNick = attackPlayer.lord.getNick();
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(sideCity.getCityId());
        mailDataManager.sendReportMail(attackPlayer, report, MailConstant.MOLD_DOMINATE_ATTACK_SUCCESS, null, nowSec,
                atkNick, defendPlayer.getCamp(), defendPlayer.lord.getLevel(), defendPlayer.lord.getNick(),//标题参数
                staticCity.getCityPos(), defendPlayer.lord.getNick(), defendPlayer.lord.getPos());//内容参数
        mailDataManager.sendReportMail(defendPlayer, report, MailConstant.MOLD_DOMINATE_ATTACK_DEFEND_FAIL, null, nowSec,
                defendPlayer.lord.getNick(), attackPlayer.getCamp(), attackPlayer.lord.getLevel(), attackPlayer.lord.getNick(),
                staticCity.getCityPos(), attackPlayer.lord.getNick(), attackPlayer.lord.getPos());
    }

    @Override
    protected void attackFightFailure(FightLogic fightLogic, DominateSideCity sideCity, CommonPb.Report.Builder report, Player attackPlayer, Army army, Player defendPlayer, int nowSec) {
        StateDominateWorldMap.getInstance().clearContinuousKillCnt(army.getLordId(), sideCity.getCityId());

        //进攻战斗失败
        Lord atkLord = attackPlayer.lord;
        Lord defLord = defendPlayer.lord;
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(sideCity.getCityId());
        mailDataManager.sendReportMail(attackPlayer, report, MailConstant.MOLD_DOMINATE_ATTACK_FAIL, null, nowSec,
                attackPlayer.lord.getNick(), defendPlayer.getCamp(), defLord.getLevel(), defLord.getNick(),
                staticCity.getCityPos(), defLord.getNick(), defLord.getPos());
        //防守战斗成功
        mailDataManager.sendReportMail(defendPlayer, report, MailConstant.MOLD_DOMINATE_ATTACK_DEFEND_SUCCESS, null, nowSec,
                defLord.getNick(), atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(),
                staticCity.getCityPos(), atkLord.getNick(), atkLord.getPos());
    }

    @Override
    protected void afterOccupation(DominateSideCity sideCity, Player attackPlayer) {
        StateDominateWorldMap.getInstance().clearContinuousKillCnt(attackPlayer.getLordId(), sideCity.getCityId());
    }

    @Override
    public GamePb8.GetDominateRankRs getDominateRank(long roleId, GamePb8.GetDominateRankRq req) {
        throw new MwException(GameError.PARAM_ERROR, "州城雄踞一方没有排行榜功能");
    }

    @Override
    public int getWorldMapFunction() {
        return WorldPb.WorldFunctionDefine.STATES_AND_COUNTIES_DOMINATE_VALUE;
    }
}
