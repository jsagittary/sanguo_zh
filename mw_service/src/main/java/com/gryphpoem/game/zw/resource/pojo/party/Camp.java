package com.gryphpoem.game.zw.resource.pojo.party;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.PartyJob;
import com.gryphpoem.game.zw.pb.CommonPb.PartyLog;
import com.gryphpoem.game.zw.pb.SerializePb.SerPartyExt;
import com.gryphpoem.game.zw.pb.SerializePb.SerPartyHonorRank;
import com.gryphpoem.game.zw.pb.SerializePb.SerPartyJob;
import com.gryphpoem.game.zw.pb.SerializePb.SerPartyLog;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.constant.PartyConstant;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName Camp.java
 * @Description 军团信息
 * @date 创建时间：2017年4月25日 下午3:39:36
 */
public class Camp {

    /**
     * 军团阵营
     */
    private int camp;

    /**
     * 军团等级
     */
    private int partyLv;

    /**
     * 军团本级经验
     */
    private int partyExp;

    /**
     * 军团当前状态（官员相关），0 未开启官员功能，1 官员投票中，2 已投票结束
     */
    private int status;

    /**
     * 当前状态结束时间
     */
    private int endTime;

    /**
     * 军团公告
     */
    private String slogan;

    /** 阵营留言板 qq号 */
    private String qq;

    /** 阵营留言板 wx号 */
    private String wx;

    /**
     * 最后修改军团公告的玩家的id
     */
    private String author;

    /**
     * 军团建设次数
     */
    private int build;
    /**
     * 城战次数
     */
    private int cityBattle;

    /**
     * 阵营战次数
     */
    private int campBattle;

    /**
     * 每日刷新时间
     */
    private int refreshTime;

    /**
     * 每周城战次数排行榜
     */
    private LinkedList<PartyHonorRank> cityRank = new LinkedList<>();

    /**
     * 每周阵营战次数排行榜
     */
    private LinkedList<PartyHonorRank> campRank = new LinkedList<>();

    /**
     * 每周建设次数排行榜
     */
    private LinkedList<PartyHonorRank> buildRank = new LinkedList<>();

    /**
     * 军团现任官员列表
     */
    private List<Official> officials = new ArrayList<>();

    /**
     * 军团日志
     */
    private LinkedList<PartyLog> log = new LinkedList<>();

    /**
     * 军团内玩家战力排行榜，内存数据，不存入数据库
     */
    private LinkedList<PartyFightRank> fightRank = new LinkedList<>();
    private final FightRankComparator fightRankComparator = new FightRankComparator();
    private final HonorRankComparator honorRankComparator = new HonorRankComparator();

    /**
     * 军团选举排行信息
     */
    private LinkedList<PartyElection> electionList = new LinkedList<>();
    private final PartyElectionCompator electionCompator = new PartyElectionCompator();

    private int honorSettleTime = 0;

    /**
     * 点兵统领的级别
     */
    private int cabinetLeadLv = 1;

    /**
     * 点兵统领当前的经验(非总经验)
     */
    private long cabinetLeadExp;

    /**
     * 沙盘连胜次数
     */
    private int sandTableWin;
    private int sandTableWinMax;

    /**
     * 礼包系统消息满足条件发送状态,不进行数据库存储
     * true已经发过
     */
    public boolean[] chatHonorRewardState = new boolean[3];

    /**
     * 首次开启军团选举的时间
     */
    private volatile int firstOpenJobTime = Integer.MAX_VALUE;

    /**
     * 军团超级补给
     */
    private PartySuperSupply partySuperSupply = new PartySuperSupply();

    /**
     * 军团补给
     */
    private List<PartySupply> partySupplies = new ArrayList<>(500);

    /**
     * 军团的礼包记录
     */
    private SupplyRecord supplyRecord = new SupplyRecord();

    /** 世界争霸阵营积分排行 */
    private int worldWarRankingIntegral;

    /** 世界争霸城市征战积分 */
    private int worldWarAttackCityIntegral;

    public SupplyRecord getSupplyRecord() {
        return supplyRecord;
    }

    /**
     * 获取玩家可以领取的补给
     * @param roleId
     * @return
     */
    public List<PartySupply> getPartySupplies(long roleId) {
        int now = TimeHelper.getCurrentSecond();
        return this.partySupplies.stream().filter(ps -> ps.getEndTime() > now && ps.canAward(roleId))
                .sorted(Comparator.comparingInt(PartySupply::getEndTime)).collect(Collectors.toList());
    }

    /**
     * 获取玩家可以领取的最低级超级补给
     * @param roleId
     * @return
     */
    public PartySupply getPartySupply(long roleId) {
        List<PartySupply> partySupplies = getPartySupplies(roleId);
        if (!CheckNull.isEmpty(partySupplies)) {
            return partySupplies.get(0);
        }
        return null;
    }

    /**
     * 根据keyId获取补给
     * @param keyId
     * @return
     */
    public PartySupply getPartySupply(int keyId) {
        int now = TimeHelper.getCurrentSecond();
        return this.partySupplies.stream().filter(ps -> ps.getEndTime() > now && ps.getKey() == keyId).findFirst()
                .orElse(null);
    }

    public Camp() {
    }

    public Camp(DbParty db) {
        this();
        setCamp(db.getCamp());
        setPartyLv(db.getPartyLv());
        setPartyExp(db.getPartyExp());
        setStatus(db.getStatus());
        setEndTime(db.getEndTime());
        setSlogan(db.getSlogan());
        setQq(db.getQq());
        setWx(db.getWx());
        setAuthor(db.getAuthor());
        setBuild(db.getBuild());
        setCityBattle(db.getCityBattle());
        setCampBattle(db.getCampBattle());
        setRefreshTime(db.getRefreshTime());
        try {
            dser(db);
        } catch (InvalidProtocolBufferException e) {
            LogUtil.error("从数据库解析Party对象出错", e);
        }
    }

    private void dser(DbParty db) throws InvalidProtocolBufferException {
        dserCityBattleRank(db.getCityRank());
        dserCampBattleRank(db.getCampRank());
        dserBuildRank(db.getBuildRank());
        dserOfficials(db.getOfficials());
        dserPartyLog(db.getLog());
        dsesrPartyExt(db.getExt());
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public void build() {
        build++;
    }

    public void campBattle() {
        campBattle++;
    }

    public void cityBattle() {
        cityBattle++;
    }

    /**
     * 军团是否开启了官员功能
     *
     * @return
     */
    public boolean isOpenJob() {
        return status >= PartyConstant.PARTY_STATUS_ELECT;
    }

    /**
     * 开启军团选举
     */
    public void openElect() {
        setStatus(PartyConstant.PARTY_STATUS_ELECT);
        setEndTime(TimeHelper.getCurrentSecond() + PartyConstant.PARTY_ELECT_TIME);
    }

    /**
     * 关闭军团选举
     */
    public void closeElect(int endTime) {
        setStatus(PartyConstant.PARTY_STATUS_ELECT_END);
        setEndTime(endTime);
    }

    /**
     * 当前是否处于选举中
     *
     * @return
     */
    public boolean isInEceltion() {
        return status == PartyConstant.PARTY_STATUS_ELECT;
    }

    /**
     * 当前是否处于官员任期内
     *
     * @return
     */
    public boolean isInOffice() {
        return status == PartyConstant.PARTY_STATUS_ELECT_END;
    }

    /**
     * 获取下次荣誉排行榜结算时间
     *
     * @return
     */
    public int getHonorSettleTime() {
        if (isInOffice()) {
            if (honorSettleTime <= 0) {
                honorSettleTime = TimeHelper.getTodayZone(endTime);
            }
            return honorSettleTime;
        }
        return 0;
    }

    /**
     * 获取玩家在军团战力排行榜上的排名，如果未上榜，返回0
     *
     * @param roleId
     * @return
     */
    public int getPlayerRank(long roleId) {
        for (int i = 0; i < fightRank.size(); i++) {
            if (fightRank.get(i).getRoleId() == roleId) {
                return ++i;
            }
        }
        return 0;
    }

    /**
     * 点兵统领加上经验
     *
     * @param roleId
     * @param exp
     */
    public void addCabinetLeadExp(long roleId, long exp) {
        cabinetLeadExp += exp;
    }

    /**
     * 添加官员选举投票，并更新排行
     *
     * @param roleId
     * @param addVote
     */
    public void addElectVote(long roleId, int addVote) {
        PartyElection election = getElectionByRoleId(roleId);

        if (null == election) {
            LogUtil.error("添加官员选举投票，找不到被投票官员的数据, roelId:", roleId);
            return;
        }

        election.addVote(addVote);
        Collections.sort(electionList, electionCompator);
    }

    /**
     * 获取玩家的官职信息
     *
     * @param roleId
     * @return
     */
    public Official getOfficialByRoleId(long roleId) {
        for (Official official : officials) {
            if (official.getRoleId() == roleId) {
                return official;
            }
        }
        return null;
    }

    /**
     * 获取军团内军长的数量
     *
     * @return
     */
    public int getGeneralNum() {
        int num = 0;
        for (Official official : officials) {
            if (official.getJob() == PartyConstant.Job.GENERAL) {
                num++;
            }
        }
        return num;
    }

    /**
     * 获取玩家的选举数据
     *
     * @param roleId
     * @return
     */
    public PartyElection getElectionByRoleId(long roleId) {
        PartyElection election = null;
        if (!electionList.isEmpty()) {
            for (PartyElection elect : electionList) {
                if (elect.getRoleId() == roleId) {
                    election = elect;
                    break;
                }
            }
        }
        return election;
    }

    /**
     * 初始化军团官员选举排行数据时，添加单个玩家数据
     *
     * @param roleId
     * @param nick
     * @param lv
     * @param fight
     */
    public void addEclection(long roleId, String nick, int lv, long fight, int ranks) {
        if (lv < PartyConstant.PARTY_JOB_MIN_LV) {
            // 小于一定级别不然进入
            return;
        }
        PartyElection election = getElectionByRoleId(roleId);

        if (null == election) {
            election = new PartyElection(roleId, nick, lv, fight, 0, ranks);
            electionList.add(election);
        } else {
            election.setNick(nick);
            election.setLv(lv);
            election.setFight(fight);
            election.setRanks(ranks);
        }
        Collections.sort(electionList, electionCompator);
        if (electionList.size() > PartyConstant.PARTY_ELECT_RANK_NUM) {
            electionList.removeLast();
        }
    }

    /**
     * 添加或更新军团战力排行记录
     *
     * @param roleId
     * @param fight
     */
    public void addFightRank(long roleId, long fight) {
        PartyFightRank rank = null;
        for (PartyFightRank partyRank : fightRank) {
            if (partyRank.getRoleId() == roleId) {
                rank = partyRank;
            }
        }

        if (null == rank) {
            rank = new PartyFightRank();
            rank.setRoleId(roleId);
            fightRank.add(rank);
        }
        rank.setFight(fight);

        Collections.sort(fightRank, fightRankComparator);
    }

    /**
     * 添加荣誉排行榜数据
     *
     * @param rankType
     * @param roleId
     * @param nick
     * @param count
     * @param now
     */
    public void addPartyHonorRank(int rankType, long roleId, String nick, int count, int now) {
        LinkedList<PartyHonorRank> rankList = getHonorRankByType(rankType);
        if (null != rankList) {
            PartyHonorRank rank = null;
            if (!rankList.isEmpty()) {
                for (PartyHonorRank partyHonorRank : rankList) {
                    if (partyHonorRank.getRoleId() == roleId) {
                        rank = partyHonorRank;
                        break;
                    }
                }
            }

            if (rank == null) {
                rank = new PartyHonorRank();
                rank.setRankType(rankType);
                rank.setRoleId(roleId);
                rank.setNick(nick);
                rankList.add(rank);
            }
            rank.addCount(1);
            rank.setRankTime(now);

            Collections.sort(rankList, honorRankComparator);
        }
    }

    /**
     * 根据荣誉排行榜类型和玩家id获取玩家的排名信息
     *
     * @param rankType
     * @param roleId
     * @return
     */
    public PartyHonorRank getPartyHonorRank(int rankType, long roleId) {
        LinkedList<PartyHonorRank> rankList = getHonorRankByType(rankType);
        if (null != rankList) {
            int order = 0;
            for (PartyHonorRank rank : rankList) {
                order++;
                if (rank.getRoleId() == roleId) {
                    rank.setRank(order);
                    return rank;
                }
            }
        }

        return null;
    }

    private LinkedList<PartyHonorRank> getHonorRankByType(int rankType) {
        LinkedList<PartyHonorRank> rankList = null;
        switch (rankType) {
            case PartyConstant.RANK_TYPE_CITY:
                rankList = cityRank;
                break;
            case PartyConstant.RANK_TYPE_CAMP:
                rankList = campRank;
                break;
            case PartyConstant.RANK_TYPE_BUILD:
                rankList = buildRank;
                break;
            default:
                break;
        }
        return rankList;
    }

    private void dserCityBattleRank(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerPartyHonorRank ser = SerPartyHonorRank.parseFrom(data);
        for (com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank rank : ser.getRankList()) {
            cityRank.add(new PartyHonorRank(rank, PartyConstant.RANK_TYPE_CITY));
        }

        Collections.sort(cityRank, honorRankComparator);
    }

    private void dserCampBattleRank(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerPartyHonorRank ser = SerPartyHonorRank.parseFrom(data);
        for (com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank rank : ser.getRankList()) {
            campRank.add(new PartyHonorRank(rank, PartyConstant.RANK_TYPE_CAMP));
        }

        Collections.sort(campRank, honorRankComparator);
    }

    private void dserBuildRank(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerPartyHonorRank ser = SerPartyHonorRank.parseFrom(data);
        for (com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank rank : ser.getRankList()) {
            buildRank.add(new PartyHonorRank(rank, PartyConstant.RANK_TYPE_BUILD));
        }

        Collections.sort(buildRank, honorRankComparator);
    }

    private void dserOfficials(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerPartyJob ser = SerPartyJob.parseFrom(data);
        for (PartyJob job : ser.getJobList()) {
            officials.add(new Official(job));
        }
        for (com.gryphpoem.game.zw.pb.CommonPb.PartyElection elect : ser.getElectList()) {
            electionList.add(new PartyElection(elect));
        }
    }

    private void dserPartyLog(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerPartyLog ser = SerPartyLog.parseFrom(data);
        log.addAll(ser.getLogList());
    }

    private void dsesrPartyExt(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }
        SerPartyExt ser = SerPartyExt.parseFrom(data);
        cabinetLeadExp = ser.hasCabinetLeadExp() ? ser.getCabinetLeadExp() : 0;
        cabinetLeadLv = ser.hasCabinetLeadLv() ? ser.getCabinetLeadLv() : StaticBuildingDataMgr.getCabinetMinLv();
        firstOpenJobTime = ser.hasFirstOpenJobTime() ? ser.getFirstOpenJobTime() : Integer.MAX_VALUE;
//        if (ser.hasSupplyRecord()) {
//            this.supplyRecord.dSer(ser.getSupplyRecord());
//        }
//        if (ser.hasSuperSupply()) {
//            this.partySuperSupply.dSer(ser.getSuperSupply());
//        }
//        List<CommonPb.PartySupply> supplyList = ser.getPartySupplyList();
//        if (!CheckNull.isEmpty(supplyList)) {
//            for (CommonPb.PartySupply supply : supplyList) {
//                this.partySupplies.add(new PartySupply(supply));
//            }
//        }
        if(ser.hasWorldWarAttackCityIntegral()){
            this.worldWarAttackCityIntegral = ser.getWorldWarAttackCityIntegral();
        }
        if(ser.hasWorldWarRankingIntegral()){
            this.worldWarRankingIntegral = ser.getWorldWarRankingIntegral();
        }
        this.setSandTableWin(ser.getSandTableWin());
        this.setSandTableWinMax(ser.getSandTableWinMax());
    }

    public DbParty ser() {
        DbParty db = new DbParty();
        db.setCamp(camp);
        db.setPartyLv(partyLv);
        db.setPartyExp(partyExp);
        db.setStatus(status);
        db.setEndTime(endTime);
        db.setSlogan(slogan);
        db.setQq(qq);
        db.setWx(wx);
        db.setAuthor(author);
        db.setBuild(build);
        db.setCityBattle(cityBattle);
        db.setCampBattle(campBattle);
        db.setCityRank(serCityBattleRank());
        db.setCampRank(serCampBattleRank());
        db.setBuildRank(serBuildRank());
        db.setOfficials(serOfficials());
        db.setLog(serPartyLog());
        db.setRefreshTime(refreshTime);
        db.setExt(serPartyExt());
        return db;
    }

    private byte[] serCityBattleRank() {
        SerPartyHonorRank.Builder ser = SerPartyHonorRank.newBuilder();
        for (PartyHonorRank rank : cityRank) {
            ser.addRank(rank.ser());
        }
        return ser.build().toByteArray();
    }

    private byte[] serCampBattleRank() {
        SerPartyHonorRank.Builder ser = SerPartyHonorRank.newBuilder();
        for (PartyHonorRank rank : campRank) {
            ser.addRank(rank.ser());
        }
        return ser.build().toByteArray();
    }

    private byte[] serBuildRank() {
        SerPartyHonorRank.Builder ser = SerPartyHonorRank.newBuilder();
        for (PartyHonorRank rank : buildRank) {
            ser.addRank(rank.ser());
        }
        return ser.build().toByteArray();
    }

    private byte[] serOfficials() {
        SerPartyJob.Builder ser = SerPartyJob.newBuilder();
        for (Official official : officials) {
            ser.addJob(official.ser());
        }
        for (PartyElection elect : electionList) {
            ser.addElect(elect.ser());
        }
        return ser.build().toByteArray();
    }

    private byte[] serPartyLog() {
        SerPartyLog.Builder ser = SerPartyLog.newBuilder();
        ser.addAllLog(log);
        return ser.build().toByteArray();
    }

    private byte[] serPartyExt() {
        SerPartyExt.Builder ser = SerPartyExt.newBuilder();
        ser.setCabinetLeadExp(cabinetLeadExp);
        ser.setCabinetLeadLv(cabinetLeadLv <= 0 ? StaticBuildingDataMgr.getCabinetMinLv() : cabinetLeadLv);
        ser.setFirstOpenJobTime(firstOpenJobTime);
//        ser.setSupplyRecord(this.supplyRecord.ser());
//        for (PartySupply supply : this.partySupplies) {
//            ser.addPartySupply(PbHelper.createPartySupplyPb(supply, true));
//        }
//        ser.setSuperSupply(PbHelper.createSuperSupplyPb(this.partySuperSupply));
        ser.setWorldWarRankingIntegral(this.worldWarRankingIntegral);
        ser.setWorldWarAttackCityIntegral(this.worldWarAttackCityIntegral);
        ser.setSandTableWin(sandTableWin);
        ser.setSandTableWinMax(sandTableWinMax);
        return ser.build().toByteArray();
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getPartyLv() {
        return partyLv;
    }

    public void setPartyLv(int partyLv) {
        this.partyLv = partyLv;
    }

    public int getPartyExp() {
        return partyExp;
    }

    public void setPartyExp(int partyExp) {
        this.partyExp = partyExp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWx() {
        return wx;
    }

    public void setWx(String wx) {
        this.wx = wx;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public int getCityBattle() {
        return cityBattle;
    }

    public void setCityBattle(int cityBattle) {
        this.cityBattle = cityBattle;
    }

    public int getCampBattle() {
        return campBattle;
    }

    public void setCampBattle(int campBattle) {
        this.campBattle = campBattle;
    }

    public LinkedList<PartyFightRank> getFightRank() {
        return fightRank;
    }

    public LinkedList<PartyHonorRank> getCityRank() {
        return cityRank;
    }

    public LinkedList<PartyHonorRank> getCampRank() {
        return campRank;
    }

    public LinkedList<PartyHonorRank> getBuildRank() {
        return buildRank;
    }

    public List<Official> getOfficials() {
        return officials;
    }

    public void setOfficials(List<Official> officials) {
        this.officials = officials;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public LinkedList<PartyLog> getLog() {
        return log;
    }

    public LinkedList<PartyElection> getElectionList() {
        return electionList;
    }

    public void setElectionList(LinkedList<PartyElection> electionList) {
        this.electionList = electionList;
    }

    public int getCabinetLeadLv() {
        return cabinetLeadLv;
    }

    public void setCabinetLeadLv(int cabinetLeadLv) {
        this.cabinetLeadLv = cabinetLeadLv;
    }

    public long getCabinetLeadExp() {
        return cabinetLeadExp;
    }

    public void setCabinetLeadExp(long cabinetLeadExp) {
        this.cabinetLeadExp = cabinetLeadExp;
    }

    public int getFirstOpenJobTime() {
        return firstOpenJobTime;
    }

    public void setFirstOpenJobTime(int firstOpenJobTime) {
        this.firstOpenJobTime = firstOpenJobTime;
    }

    public PartySuperSupply getPartySuperSupply() {
        return partySuperSupply;
    }

    public void setPartySuperSupply(PartySuperSupply partySuperSupply) {
        this.partySuperSupply = partySuperSupply;
    }

    public List<PartySupply> getPartySupplies() {
        return partySupplies;
    }

    public void setPartySupplies(List<PartySupply> partySupplies) {
        this.partySupplies = partySupplies;
    }

    public int getWorldWarRankingIntegral() {
        return worldWarRankingIntegral;
    }

    public void setWorldWarRankingIntegral(int worldWarRankingIntegral) {
        this.worldWarRankingIntegral = worldWarRankingIntegral;
    }

    public int getWorldWarAttackCityIntegral() {
        return worldWarAttackCityIntegral;
    }

    public void setWorldWarAttackCityIntegral(int worldWarAttackCityIntegral) {
        this.worldWarAttackCityIntegral = worldWarAttackCityIntegral;
    }

    @Override
    public String toString() {
        return "Camp{" +
                "camp=" + camp +
                ", partyLv=" + partyLv +
                ", partyExp=" + partyExp +
                ", status=" + status +
                ", endTime=" + endTime +
                ", slogan='" + slogan + '\'' +
                ",qq='" + qq +
                ", wx='" + wx + '\'' +
                ", author='" + author + '\'' +
                ", build=" + build +
                ", cityBattle=" + cityBattle +
                ", campBattle=" + campBattle +
                ", refreshTime=" + refreshTime +
                ", cityRank=" + cityRank +
                ", campRank=" + campRank +
                ", buildRank=" + buildRank +
                ", officials=" + officials +
                ", log=" + log +
                ", fightRank=" + fightRank +
                ", fightRankComparator=" + fightRankComparator +
                ", honorRankComparator=" + honorRankComparator +
                ", electionList=" + electionList +
                ", electionCompator=" + electionCompator +
                ", honorSettleTime=" + honorSettleTime +
                ", cabinetLeadLv=" + cabinetLeadLv +
                ", cabinetLeadExp=" + cabinetLeadExp +
                ", chatHonorRewardState=" + Arrays.toString(chatHonorRewardState) +
                ", firstOpenJobTime=" + firstOpenJobTime +
                ", partySuperSupply=" + partySuperSupply +
                ", partySupplies=" + partySupplies +
                ", supplyRecord=" + supplyRecord +
                ", worldWarRankingIntegral=" + worldWarRankingIntegral +
                ", worldWarAttackCityIntegral=" + worldWarAttackCityIntegral +
                '}';
    }

    public int getSandTableWin() {
        return sandTableWin;
    }

    public void setSandTableWinAndMax(int sandTableWin){
        this.sandTableWin = sandTableWin;
        if(sandTableWin > sandTableWinMax){
            this.sandTableWinMax = sandTableWin;
        }
    }

    public void setSandTableWin(int sandTableWin) {
        this.sandTableWin = sandTableWin;
    }

    public int getSandTableWinMax() {
        return sandTableWinMax;
    }

    public void setSandTableWinMax(int sandTableWinMax) {
        this.sandTableWinMax = sandTableWinMax;
    }
}

/**
 * 军团战力排行榜排序
 */
class FightRankComparator implements Comparator<PartyFightRank> {

    @Override public int compare(PartyFightRank o1, PartyFightRank o2) {
        return o1.compareTo(o2);
    }
}

/**
 * 军团荣誉排行榜排序
 */
class HonorRankComparator implements Comparator<PartyHonorRank> {

    @Override public int compare(PartyHonorRank o1, PartyHonorRank o2) {
        return o1.compareTo(o2);
    }
}

/**
 * 军团选举排序
 */
class PartyElectionCompator implements Comparator<PartyElection> {

    @Override public int compare(PartyElection o1, PartyElection o2) {
        return o1.compareTo(o2);
    }
}