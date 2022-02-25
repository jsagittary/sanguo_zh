package com.gryphpoem.game.zw.resource.pojo.daily;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.p.Lord;

/**
 * @author: ZhouJie
 * @date: Create in 2018-08-07 16:31
 * @description: 每日战报
 * @modified By:
 */
public class DailyReport implements IHonorDailyReport {

    private ReportInfo atk;

    private ReportInfo def;

    private boolean isWin;

    private int createTime;
    
    private int honorType;
    

    public DailyReport() {
    }

    public DailyReport(CommonPb.HonorReport report) {
        this();
        this.isWin = report.getIsWin();
        this.createTime = report.getCreateTime();
        this.honorType = report.getHonorType();
        this.atk = new ReportInfo(report.getAtk());
        this.def = new ReportInfo(report.getDef());
      
    }

    public CommonPb.HonorReport ser() {
        CommonPb.HonorReport.Builder builder = CommonPb.HonorReport.newBuilder();
        builder.setIsWin(isWin);
        builder.setCreateTime(createTime);
        builder.setHonorType(honorType);
        builder.setAtk(atk.ser());
        builder.setDef(def.ser());
        return builder.build();
    }

    public static class ReportInfo {
        private long roleId;
        private String nick;
        private int rank;
        private int pos;
        private int camp;
        private int ranks;
        private int portrait;
        private int portraitFrame;

        public ReportInfo() {
        }

        public ReportInfo(CommonPb.HonorReportInfo info) {
            this();
            this.roleId = info.getRoleId();
            this.nick = info.getNick();
            this.rank = info.getRank();
            this.pos = info.getPos();
            this.camp = info.getCamp();
            this.ranks = info.getRanks();
            this.portrait = info.getPortrait();
            this.portraitFrame = info.getPortraitFrame();
        }

        public ReportInfo(int rank, Lord lord, int portraitFrame) {
            this.roleId = lord.getLordId();
            this.nick = lord.getNick();
            this.rank = rank;
            this.pos = lord.getPos();
            this.camp = lord.getCamp();
            this.ranks = lord.getRanks();
            this.portrait = lord.getPortrait();
            this.portraitFrame = portraitFrame;
        }

        public CommonPb.HonorReportInfo ser() {
            CommonPb.HonorReportInfo.Builder builder = CommonPb.HonorReportInfo.newBuilder();
            builder.setRoleId(roleId);
            builder.setNick(nick);
            builder.setRank(rank);
            builder.setPos(pos);
            builder.setCamp(camp);
            builder.setRanks(ranks);
            builder.setPortrait(portrait);
            builder.setPortraitFrame(portraitFrame);
            return builder.build();
        }

        public int getCamp() {
            return camp;
        }
    }

    public ReportInfo getAtk() {
        return atk;
    }

    public void setAtk(ReportInfo atk) {
        this.atk = atk;
    }

    public ReportInfo getDef() {
        return def;
    }

    public void setDef(ReportInfo def) {
        this.def = def;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }
    

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }
    
    
    public int getHonorType() {
        return honorType;
    }

    public void setHonorType(int honorType) {
        this.honorType = honorType;
    }


}
