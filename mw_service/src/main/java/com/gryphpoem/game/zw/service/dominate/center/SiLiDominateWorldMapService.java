package com.gryphpoem.game.zw.service.dominate.center;

import com.gryphpoem.game.zw.core.rank.SimpleRank4SkipSet;
import com.gryphpoem.game.zw.dataMgr.StaticDominateDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.PlayerSiLiDominateFightRecord;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.WorldMapPlay;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb8;
import com.gryphpoem.game.zw.pb.WorldPb;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.dominate.abs.AbsDominateWorldMapService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 10:10
 */
@Component
public class SiLiDominateWorldMapService extends AbsDominateWorldMapService {

    @Override
    public int getWorldMapFunction() {
        return WorldPb.WorldFunctionDefine.SI_LI_DOMINATE_SIDE_VALUE;
    }

    @Override
    public GamePb8.GetDominateRankRs getDominateRank(long roleId, GamePb8.GetDominateRankRq req) {
        playerDataManager.checkPlayerIsExist(roleId);
        WorldMapPlay worldMapPlay = getWorldMapPlay(getWorldMapFunction());
        SiLiDominateWorldMap siLiDominateWorldMap = (SiLiDominateWorldMap) worldMapPlay;
        PlayerSiLiDominateFightRecord record = siLiDominateWorldMap.getPlayerRecord(roleId);

        GamePb8.GetDominateRankRs.Builder builder = GamePb8.GetDominateRankRs.newBuilder();
        SimpleRank4SkipSet<Integer> rankList = siLiDominateWorldMap.getPlayerRanks(0);

        CommonPb.RankItem.Builder rankItemPb = CommonPb.RankItem.newBuilder();
        AtomicInteger rank = new AtomicInteger(0);
        rankList.forEach(rankItem -> {
            Player player_ = playerDataManager.getPlayer(rankItem.getLordId());
            if (CheckNull.isNull(player_)) return;
            rankItemPb.setRank(rank.getAndIncrement());
            rankItemPb.setLordId(rankItem.getLordId());
            rankItemPb.setRankValue((Long) rankItem.getRankValue());
            rankItemPb.addParam(player_.lord.getCamp());
            builder.addRankData(rankItemPb.build());
            rankItemPb.clear();
        });

        builder.setRankSize(rankList.size());
        builder.setMyValue(record.getKillCnt());
        builder.setMyRank(rankList.getRank(roleId));
        builder.setScore(Optional.ofNullable(StaticDominateDataMgr.findKillRankAward(record.getKillCnt())).
                map(award -> award.getAward()).orElse(0));
        return builder.build();
    }

    @Override
    protected void doRecoverArmy(Player player, Army army, Map<Integer, Integer> recoverMap, Map<Long, ChangeInfo> changeMap) {

    }

    @Override
    protected void attackFightSuccess(FightLogic fightLogic, DominateSideCity sideCity, CommonPb.Report.Builder report, Player attackPlayer, Army army, Player defendPlayer, int nowSec) {
        army.setState(ArmyConstant.ARMY_STATE_SI_LI_DOMINATE_HOLDER);
        // 连杀广播
        PlayerSiLiDominateFightRecord record = SiLiDominateWorldMap.getInstance().getPlayerRecord(army.getLordId());
        int killCnt = record.incContinuousKillCnt(sideCity.getCityId());
        continuousKillBroadcast(attackPlayer, killCnt);

        record.addKillCnt(fightLogic.getAttacker().hurt, nowSec);
        SiLiDominateWorldMap.getInstance().addPlayerRank(army.getLordId(), fightLogic.getAttacker().hurt, nowSec, 0);

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
        PlayerSiLiDominateFightRecord record = SiLiDominateWorldMap.getInstance().getPlayerRecord(army.getLordId());
        record.clearContinuousKillCnt(sideCity.getCityId());

        record.addKillCnt(fightLogic.getAttacker().hurt, nowSec);
        SiLiDominateWorldMap.getInstance().addPlayerRank(army.getLordId(), fightLogic.getAttacker().hurt, nowSec, 0);

        // 进攻失败
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
        PlayerSiLiDominateFightRecord record = SiLiDominateWorldMap.getInstance().getPlayerRecord(attackPlayer.getLordId());
        record.clearContinuousKillCnt(sideCity.getCityId());
    }
}
