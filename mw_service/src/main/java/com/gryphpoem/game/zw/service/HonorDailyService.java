package com.gryphpoem.game.zw.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.HonorDailyDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RankDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb.HonorReport2;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HonorDailyConstant;
import com.gryphpoem.game.zw.resource.constant.PartyConstant;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.pojo.daily.DailyReport;
import com.gryphpoem.game.zw.resource.pojo.daily.HonorDaily;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

/**
 * @author: ZhouJie
 * @date: Create in 2018-08-07 16:27
 * @description: 荣耀日常
 * @modified By:
 */
@Service
public class HonorDailyService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RankDataManager rankDataManager;
    @Autowired
    private HonorDailyDataManager honorDailyDataManager;

    /**
     * 获取荣耀日报详情
     * 
     * @param id
     * @param roleId
     * @throws MwException
     */
    public GamePb4.GetHonorReportsRs getHonorReports(long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.GetHonorReportsRs.Builder builder = GamePb4.GetHonorReportsRs.newBuilder();
        // 判断是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.RYRB)) {
            return builder.build();
        }
        HonorDaily honorDaily = honorDailyDataManager.getHonorDaily();
        if (CheckNull.isNull(honorDaily)) {
            throw new MwException(GameError.HONOR_DAILY_NOT_INIT.getCode(), "荣耀日常对象未初始化");
        }
        if (type == 2) {
            // 清除未读红点
            player.setMixtureData(PlayerConstant.HONOR_REPORT_TIPS, 0);
        }
        List<DailyReport> dailReportList = honorDaily.getDailReports(player.lord.getCamp(), player);
        builder.addAllReports(PbHelper.createHonorReports(dailReportList));
        // 非战斗类型
        List<HonorReport2> honorReport2List = honorDaily.getHonorReport2List(player).stream().map(r -> r.ser())
                .collect(Collectors.toList());
        builder.addAllReports2(honorReport2List);
        if (player.getMixtureDataById(PlayerConstant.HONOR_REPORT_TIPS) != 0) {
            int size = honorReport2List.size() + dailReportList.size();// 重新修改红点数
            player.setMixtureData(PlayerConstant.HONOR_REPORT_TIPS, size);
        }

        builder.setTips(player.getMixtureDataById(PlayerConstant.HONOR_REPORT_TIPS));
        return builder.build();
    }

    /**
     * 添加并检测荣耀日报(其他类型)
     * 
     * @param player
     * @param condId
     * @param params
     */
    public void addAndCheckHonorReport2s(Player player, int condId, String... params) {
        honorDailyDataManager.addAndCheckHonorReport2s(player, condId, 1, params);
    }

    /*------------------------战斗相关日报 start--------------------------*/
    /**
     * 添加检测荣耀日报(战斗类型)
     * 
     * @param type
     * 
     * @param atkLord
     * @param defLord
     * @param status
     */
    public void addAndCheckHonorReports(Player atk, Player def, boolean win, int type) {
        if (CheckNull.isNull(atk) || CheckNull.isNull(def)) {
            return;
        }
        /* Map<Integer, Army> armys = atk.armys;*/
        Lord atkLord = atk.lord;
        Lord defLord = def.lord;
        int atkRank = rankDataManager.getMyRankByTypeAndScop(Constant.RankType.type_6, atkLord,
                RankDataManager.CAMP_SCOPE);
        int defRank = rankDataManager.getMyRankByTypeAndScop(Constant.RankType.type_6, defLord,
                RankDataManager.CAMP_SCOPE);
        // 添加战斗日报
        if (checkPartyRank(true, atkRank) && checkPartyRank(false, defRank)) {
            addHonorDaily(atkLord, defLord, win, atkRank, defRank, type);
        }
        // 普通荣耀日报添加
        // 击飞他人
        if (win && honorDailyDataManager.checkHonorDailyCondLordLv(def.lord.getLevel(), HonorDailyConstant.COND_ID_4,
                2)) {
            addAndCheckHonorReport2s(atk, HonorDailyConstant.COND_ID_4);
        }

        // 击飞帝国
        if (win && def.lord.getCamp() == Constant.Camp.EMPIRE && honorDailyDataManager
                .checkHonorDailyCondLordLv(def.lord.getLevel(), HonorDailyConstant.COND_ID_6, 2)) {
            addAndCheckHonorReport2s(atk, HonorDailyConstant.COND_ID_6);
        }
        // 击飞联军
        if (win && def.lord.getCamp() == Constant.Camp.ALLIED && honorDailyDataManager
                .checkHonorDailyCondLordLv(def.lord.getLevel(), HonorDailyConstant.COND_ID_7, 2)) {
            addAndCheckHonorReport2s(atk, HonorDailyConstant.COND_ID_7);
        }
        // 击飞盟军
        if (win && def.lord.getCamp() == Constant.Camp.UNION && honorDailyDataManager
                .checkHonorDailyCondLordLv(def.lord.getLevel(), HonorDailyConstant.COND_ID_5, 2)) {
            addAndCheckHonorReport2s(atk, HonorDailyConstant.COND_ID_5);
        }
        // 被击飞
        if (win && honorDailyDataManager.checkHonorDailyCondLordLv(atk.lord.getLevel(), HonorDailyConstant.COND_ID_8,
                2)) {
            addAndCheckHonorReport2s(def, HonorDailyConstant.COND_ID_8);
        }
    }

    /**
     * 决战战报
     * 
     * @param atk
     * @param def
     * @param win
     * @param type
     */
    public void addAndCheckBattleHonorReports(Player atk, Player def, boolean win, int type) {
        Lord atkLord = atk.lord;
        Lord defLord = def.lord;
        int atkRank = rankDataManager.getMyRankByTypeAndScop(Constant.RankType.type_6, atkLord,
                RankDataManager.CAMP_SCOPE);
        int defRank = rankDataManager.getMyRankByTypeAndScop(Constant.RankType.type_6, defLord,
                RankDataManager.CAMP_SCOPE);
        addHonorDaily(atkLord, defLord, win, atkRank, defRank, type);
    }

    /**
     * 日报红点同步(战斗类型)
     * 
     * @param target
     * @param mail
     */
    private void syncHonorReport(Player target, DailyReport dailyReport) {
        if (target != null && target.isLogin && target.ctx != null) {
            GamePb4.SyncHonorReportRs.Builder builder = GamePb4.SyncHonorReportRs.newBuilder();
            builder.setReports(dailyReport.ser());
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb4.SyncHonorReportRs.EXT_FIELD_NUMBER,
                    GamePb4.SyncHonorReportRs.ext, builder.build());
            MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
        }
    }

    /**
     * 检测日报排名筛选
     * 
     * @param atkOrDef
     * @param rank
     * @return
     */
    private boolean checkPartyRank(boolean atkOrDef, int rank) {
        boolean flag = false;
        List<Integer> ranks;
        if (atkOrDef) {
            ranks = PartyConstant.HONOR_DAILY_RANK.get(0);
        } else {
            ranks = PartyConstant.HONOR_DAILY_RANK.get(1);
        }
        if (!CheckNull.isEmpty(ranks) && rank >= ranks.get(0) && rank <= ranks.get(1)) {
            flag = true;
        }
        return flag;
    }

    /**
     * 添加荣耀日报
     * 
     * @param atkLord
     * @param defLord
     * @param win
     * @param atkRank
     * @param defRank
     * @param type
     */
    private void addHonorDaily(Lord atkLord, Lord defLord, boolean win, int atkRank, int defRank, int type) {
        HonorDaily honorDaily = honorDailyDataManager.getHonorDaily();
        if (!CheckNull.isNull(honorDaily)) {
            if (CheckNull.isNull(honorDaily.getBeginDate())) {
                honorDaily.setBeginDate(new Date());
            }
            DailyReport dailyReport = honorDaily.addHonorReports(atkLord, defLord, win, atkRank, defRank, type);
            // 更新阵营玩家的未读红点 进攻方输的不更新
            if (win) {
                updateReportTipsByCamp(atkLord.getCamp(), dailyReport);
            }
            if (atkLord.getCamp() != defLord.getCamp()) {// 阵营不同的情况才会发,避免推送多次
                updateReportTipsByCamp(defLord.getCamp(), dailyReport);
            }
        }
    }

    /**
     * 更新阵营玩家的红点状态
     * 
     * @param camp
     * @param dailyReport
     */
    private void updateReportTipsByCamp(int camp, DailyReport dailyReport) {
        playerDataManager.getPlayerByCamp(camp).values().forEach(player -> {
            if (player.lord.getLevel() < Constant.ROLE_GRADE_45) {
                return;
            }
            if (honorDailyDataManager.updateReportTips(player)) {
                syncHonorReport(player, dailyReport);
            }
        });
    }
    /*------------------------战斗相关日报 end--------------------------*/

    public HonorDaily getHonorDaily() {
        return honorDailyDataManager.getHonorDaily();
    }

}
