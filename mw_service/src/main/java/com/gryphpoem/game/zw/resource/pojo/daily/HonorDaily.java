package com.gryphpoem.game.zw.resource.pojo.daily;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @author: ZhouJie
 * @date: Create in 2018-08-07 16:28
 * @description: 荣耀日常
 * @modified By:
 */
public class HonorDaily {

    private Date beginDate;

    private LinkedList<IHonorDailyReport> dailyReports = new LinkedList<>();

    // 日报上限
    public static final int MAX_REPORTS_NUM = 20;

    public HonorDaily() {
    }

    public HonorDaily(SerializePb.SerHonorDaily honorDaily) {
        this();
        this.beginDate = TimeHelper.getDate(new Long(honorDaily.getCreateTime()));
        // 战斗类型
        honorDaily.getDailyReportsList().forEach(report -> {
            dailyReports.add(new DailyReport(report));
        });
        // 其他类型
        honorDaily.getReports2List().forEach(rp -> {
            dailyReports.add(new HonorReport2(rp));
        });
        // 排序
        dailyReports.sort(Comparator.comparingInt(IHonorDailyReport::getCreateTime));
    }

    public SerializePb.SerHonorDaily ser() {
        SerializePb.SerHonorDaily.Builder builder = SerializePb.SerHonorDaily.newBuilder();
        if (!CheckNull.isNull(beginDate)) {
            builder.setCreateTime((int) (beginDate.getTime() / TimeHelper.MINUTE_S));
        }
        builder.addAllDailyReports(dailyReports.stream().filter(s -> s.isDailyReportIns()).map(s -> (DailyReport) s)
                .map(DailyReport::ser).collect(Collectors.toList()));
        // 其他类型
        List<CommonPb.HonorReport2> honorReport2List = dailyReports.stream().filter(s -> s.isHonorReport2Ins())
                .map(s -> (HonorReport2) s).map(HonorReport2::ser).collect(Collectors.toList());
        builder.addAllReports2(honorReport2List);

        return builder.build();
    }

    /**
     * 添加日报信息(战斗类型)
     * 
     * @param atkLord
     * @param defLord
     * @param win
     * @param atkRank
     * @param defRank
     * @param type 
     */
    public DailyReport addHonorReports(Lord atkLord, Lord defLord, boolean win, int atkRank, int defRank, int type) {
        DailyReport dailyReport = new DailyReport();
        dailyReport.setWin(win);
        if(type == WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE){
         dailyReport.setHonorType(2);
        }else{
            dailyReport.setHonorType(1);
        	LogUtil.debug("荣耀战斗日报类型热更成功");
        }
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        dailyReport.setCreateTime(TimeHelper.getCurrentSecond());
        dailyReport.setAtk(new DailyReport.ReportInfo(atkRank, atkLord, playerDataManager.getPlayer(atkLord.getLordId()).getDressUp().getCurPortraitFrame()));
        dailyReport.setDef(new DailyReport.ReportInfo(defRank, defLord, playerDataManager.getPlayer(defLord.getLordId()).getDressUp().getCurPortraitFrame()));
        // 达到日报上限,删除首条
        addIHonorDailyReport(dailyReport);
        return dailyReport;
    }

    /**
     * 添加日报信息(其他类型)
     * 
     * @param player
     * @param condId
     * @param rank
     * @param param
     * @return
     */
    public HonorReport2 addHonorReport2(Player player, int condId, int rank, List<String> param) {
        HonorReport2 hr = new HonorReport2();
        hr.setCondId(condId);
        hr.setCreateTime(TimeHelper.getCurrentSecond());
        hr.setRole(new DailyReport.ReportInfo(rank, player.lord, player.getDressUp().getCurPortraitFrame()));
        if (!CheckNull.isEmpty(param)) {
            hr.setParam(param);
        }
        addIHonorDailyReport(hr);
        return hr;
    }

    private void addIHonorDailyReport(IHonorDailyReport hr) {
        // 达到日报上限,删除首条
        if (dailyReports.size() >= MAX_REPORTS_NUM) {
            dailyReports.removeFirst();
        }
        dailyReports.addLast(hr);
    }

    /**
     * 获取DailyReport数据(战斗类型)
     * 
     * @param atkCamp
     * @param player
     * @return
     */
    public List<DailyReport> getDailReports(int atkCamp, Player player) {
        List<DailyReport> list = new ArrayList<>();
        for (IHonorDailyReport ie : dailyReports) {
            if (ie.isDailyReportIns()) {
                DailyReport e = (DailyReport) ie;
                if (e.getAtk().getCamp() == atkCamp && !e.isWin()) {// 进攻方是我 但是输了 不要~~
                    continue;
                }
                if (atkCamp != e.getAtk().getCamp() && atkCamp != e.getDef().getCamp()) {// 我既不是进攻方 也不是防守方 不要~~
                    continue;
                }
                if (e.getCreateTime() < player.getMixtureDataById(PlayerConstant.RYRB_LOCK_TIME)) {// 战报是在我解锁该功能之前的 不要~~
                    continue;
                }
                list.add(e);
            }
        }
        return list;
    }

    /**
     * 获取HonorReport2数据(其他通用类型)
     * 
     * @param player
     * @return
     */
    public List<HonorReport2> getHonorReport2List(Player player) {
        final int time = player.getMixtureDataById(PlayerConstant.RYRB_LOCK_TIME);
        return dailyReports.stream().filter(hdr -> hdr.getCreateTime() >= time && hdr.isHonorReport2Ins())
                .map(hdr -> (HonorReport2) hdr).collect(Collectors.toList());
    }

    /*------------------------一堆get set------------------------*/
    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public LinkedList<IHonorDailyReport> getDailyReports() {
        return dailyReports;
    }
    /*------------------------一堆get set------------------------*/

}
