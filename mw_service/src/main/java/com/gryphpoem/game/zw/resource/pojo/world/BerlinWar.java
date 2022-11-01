package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticBerlinWarDataMgr;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.IntLong;
import com.gryphpoem.game.zw.pb.CommonPb.Report;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.p.BerlinRecord;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticBerlinJob;
import com.gryphpoem.game.zw.resource.domain.s.StaticBerlinWar;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.WorldScheduleService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-16 15:53
 * @description: 柏林会战
 * @modified By:
 */
public class BerlinWar {

    private BerlinCityInfo berlinCityInfo; // 柏林会战信息
    private Map<Integer, BerlinCityInfo> battlefronts = new HashMap<>(); // 前线战地
    private LinkedList<Report> reports = new LinkedList<>(); // 最近战况
    private Map<Long, BerlinRecord> berlinRecord = new HashMap<>(); // 柏林会战记录, key: roleId
    private Date lastDate; // 最近开启时间
    private List<Turple<Integer, Long>> historyWinner = new ArrayList<>(); // 历史的获胜者,a:时间 b:roleId
    private int status; // 柏林会战状态, 1 预显示, 2 开启, 3 关闭
    private static final int MAX_RANK_NUM = 1000000; // 排行榜上限
    private HashSet<Long> joinBerlinWar = new HashSet<>(); // 参战玩家
    // 玩家排行榜数据 key: killType,不会进行存储,在服务器初始化时,会重新计算赋值
    private Map<Integer, LinkedList<ActRank>> ranks = new ConcurrentHashMap<>();
    // 柏林官职 key:roleId, value:job
    private Map<Long, Integer> berlinJobs = new HashMap<>();
    // 柏林玩家数据 key: roleId , val: 玩家数据
    private Map<Long, BerlinRoleInfo> roleInfos = new HashMap<>();

    private Date beginDate;
    private Date endDate;
    private Date preViewDate;
    private int atkCD;
    // 记录开启时的世界进程阶段, 用来计算战前buff和积分结算
    private int scheduleId;
    // 柏林战斗狂热
    private BerlinBattleFrenzy bbf = new BerlinBattleFrenzy();

    private BerlinWar() {
    }

    public static BerlinWar createNewBerlinWar() {
        return new BerlinWar();
    }

    public BerlinWar(SerializePb.SerBerlinWar berlinWar) {
        this();
        this.berlinCityInfo = new BerlinCityInfo(berlinWar.getBerlinCity());
        berlinWar.getBattleFrontList().forEach(e -> {
            this.battlefronts.put(e.getCityId(), new BerlinCityInfo(e));
        });
        reports.addAll(berlinWar.getReportsList().stream().sorted(Comparator.comparingInt(Report::getTime))
                .collect(Collectors.toList()));
        berlinWar.getRecordsList().forEach(e -> {
            berlinRecord.put(e.getRoleId(), new BerlinRecord(e));
        });
        this.lastDate = TimeHelper.getDate(new Long(berlinWar.getLastDate()));
        berlinWar.getHistoryWinnerList().forEach(e -> {
            historyWinner.add(new Turple<Integer, Long>(e.getV1(), e.getV2()));
        });
        this.status = berlinWar.getStatus();
        this.beginDate = TimeHelper.getDate(new Long(berlinWar.getBeginDate()));
        this.endDate = TimeHelper.getDate(new Long(berlinWar.getEndDate()));
        this.preViewDate = TimeHelper.getDate(new Long(berlinWar.getPreViewDate()));
        this.atkCD = berlinWar.getAtkCD();
        // 反序列后更新排行榜
        berlinRecord.forEach((roleId, record) -> {
            if (record.getKillCnt() > 0) {
                addPlayerRank(roleId, record.getKillCnt(), record.getKillRankTime(),
                        WorldConstant.BERLIN_RANK_KILL_ARMY_CNT);
            }
            if (record.getKillStreak() > 0) {
                addPlayerRank(roleId, record.getKillStreak(), record.getStreakRankTime(),
                        WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
            }
        });
        List<IntLong> berliJobsList = berlinWar.getBerliJobsList();
        if (!CheckNull.isEmpty(berliJobsList)) {
            for (IntLong il : berliJobsList) {
                this.berlinJobs.put(il.getV2(), il.getV1());
            }
        }
        List<Long> joinBerlinWarList = berlinWar.getJoinBerlinWarList();
        if (!CheckNull.isEmpty(joinBerlinWarList)) {
            this.joinBerlinWar.addAll(joinBerlinWarList);
        }
        List<SerializePb.SerBerlinRoleInfo> infosList = berlinWar.getRoleInfosList();
        if (!CheckNull.isEmpty(infosList)) {
            for (SerializePb.SerBerlinRoleInfo serRoleInfo : infosList) {
                this.roleInfos.put(serRoleInfo.getRoleId(), new BerlinRoleInfo(serRoleInfo));
            }
        }
        this.bbf = new BerlinBattleFrenzy(berlinWar.getBbf());
        this.scheduleId = berlinWar.getScheduleId();
    }

    /**
     * 更新柏林会战记录
     *
     * @param now
     */
    public void updateBerlinRecord(Lord lord, Fighter fighter, int now) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        if (CheckNull.isNull(lord)) {
            return;
        }
        if (CheckNull.isNull(fighter)) {
            return;
        }
        if (getStatus() != WorldConstant.BERLIN_STATUS_OPEN) {
            return;
        }
        Force force = fighter.getForces().get(0);
        if (CheckNull.isNull(force)) {
            return;
        }
        long lordId = lord.getLordId();
        // 获取柏林会战记录数据
        BerlinRecord berlinRecord = getBerlinRecord(lordId);
        // 记录杀敌数
        berlinRecord.addKillCnt(fighter.hurt, now);

        // 将领没死,记录连杀
        if (force.alive()) {
            berlinRecord.addTempKill(fighter.hurt, now);
            // 连续击杀兵力排行榜
            addPlayerRank(lordId, berlinRecord.getKillStreak(), now, WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
            LogUtil.debug("柏林会战排行榜变动,[连续杀敌] roleId:", lordId, ", 连续杀敌:", berlinRecord.getKillStreak());
        } else {
            berlinRecord.clearTempKill();
        }
        // 玩家总击杀兵力排行榜
        addPlayerRank(lordId, berlinRecord.getKillCnt(), now, WorldConstant.BERLIN_RANK_KILL_ARMY_CNT);
        // 柏林杀敌日志埋点
        Optional.ofNullable(playerDataManager.getPlayer(lordId)).ifPresent(player -> LogLordHelper.commonLog("berlinKill", AwardFrom.BERLIN_WAR_ATTACK, player, fighter.hurt, berlinRecord.getKillCnt(), berlinRecord.getKillStreak()));
        LogUtil.debug("柏林会战排行榜变动,[累积杀敌] roleId:", lordId, ", 累积杀敌:", berlinRecord.getKillCnt());
    }

    public void addPlayerRank(long lordId, int value, int now, int rankType) {
        LinkedList<ActRank> killArmyRank = getPlayerRanks(rankType);
        addPlayerRank(killArmyRank, lordId, rankType, (long) value, MAX_RANK_NUM, ActivityConst.DESC, now);
    }

    private void addPlayerRank(LinkedList<ActRank> rankList, long lordId, int type, Long value, int maxRank, int order,
                               int rankTime) {
        int time = rankTime;
        if (time <= 0) {
            time = TimeHelper.getCurrentSecond();
        }

        int size = rankList.size();
        if (size == 0) {
            rankList.add(new ActRank(lordId, type, value, time));
            return;
        } else if (maxRank != 0 && size >= maxRank) {// 排名已满,则比较最末名
            ActRank actRank = rankList.getLast();
            if (order == ActivityConst.ASC) {
                if (actRank.getRankValue() < value) {// 升序比最末名大,则不进入排名
                    return;
                }
            } else if (order == ActivityConst.DESC) {
                if (actRank.getRankValue() > value) {// 降序比最末名小,则不进入排名
                    return;
                }
            }
        }

        boolean flag = false;
        for (ActRank next : rankList) {
            if (order == ActivityConst.ASC) {
                if (next.getLordId() == lordId) {
                    if (next.getRankValue() > value) {
                        next.setRankValue(value);
                        next.setRankTime(time);// 更新排行信息时，更新上榜时间，当排行数据相同时，将通过比较最后更新时间来排行
                    }
                    flag = true;
                    break;
                }
            } else if (order == ActivityConst.DESC) {
                if (next.getLordId() == lordId) {
                    if (next.getRankValue() < value) {
                        next.setRankValue(value);
                        next.setRankTime(time);
                    }
                    flag = true;
                    break;
                }
            }
        }

        if (!flag) {// 新晋排名玩家
            rankList.add(new ActRank(lordId, type, value, time));
        }

        if (order == ActivityConst.ASC) {// 升序排序
            rankList.sort(Comparator.comparingLong(ActRank::getRankValue).thenComparingInt(ActRank::getRankTime));
        } else if (order == ActivityConst.DESC) {// 降序
            rankList.sort(
                    Comparator.comparingLong(ActRank::getRankValue).reversed().thenComparingInt(ActRank::getRankTime));
        }

        // 将超出排名的最末名删掉
        if (maxRank != 0 && rankList.size() > maxRank) {
            rankList.removeLast();
        }
    }

    /**
     * 获取某个类型的排行榜
     *
     * @param type
     * @return
     */
    public LinkedList<ActRank> getPlayerRanks(int type) {
        // 如果没有刷新则刷新数据
        return ranks.computeIfAbsent(type, k -> new LinkedList<>());
    }

    /**
     * 获取玩家在某个排名榜的名次
     *
     * @param lordId
     * @return
     */
    public ActRank getPlayerRank(int type, long lordId) {
        LinkedList<ActRank> playerRanks = getPlayerRanks(type);
        if (playerRanks.size() == 0) {
            return null;
        }
        int rank = 1;
        Iterator<ActRank> it = playerRanks.iterator();
        while (it.hasNext()) {
            ActRank next = it.next();
            if (next.getLordId() == lordId) {
                next.setRank(rank);
                return next;
            }
            rank++;
        }
        return null;
    }

    /**
     * 初始化柏林会战
     */
    public void initBerlinWar() {
        int now = TimeHelper.getCurrentSecond();
//        setLastDate(new Date());
        LogUtil.error("初始化柏林会战, 现在时间: ", DateHelper.getDateFormat1().format(now));
        StaticBerlinWar staticBerlinWar = StaticBerlinWarDataMgr.getBerlinSetting();
        if (CheckNull.isNull(staticBerlinWar)) {
            LogUtil.error("初始化柏林会战出错,s_berlin_war表没有配置柏林");
            return;
        }
        if (CheckNull.isNull(berlinCityInfo)) {
            berlinCityInfo = new BerlinCityInfo();
        }
        // 初始化柏林会战
        berlinCityInfo.setCityId(staticBerlinWar.getKeyId());
        berlinCityInfo.setPos(staticBerlinWar.getCityPos());
        List<CityHero> npcForm = staticBerlinWar.getFirstFormList();
        berlinCityInfo.initCityDef(npcForm);
        berlinCityInfo.setNextAtkTime(-1);
        berlinCityInfo.setLastOccupyTime(now);
        List<StaticBerlinWar> battlefronts = StaticBerlinWarDataMgr.getBerlinBattlefront();
        if (CheckNull.isEmpty(battlefronts)) {
            LogUtil.error("初始化柏林会战出错,s_berlin_war表没有配置前线阵地");
            return;
        }
        // 初始化前线阵地
        battlefronts.forEach(battleFront -> {
            int cityId = battleFront.getKeyId();
            BerlinCityInfo battlefront = this.battlefronts.get(cityId);
            if (CheckNull.isNull(battlefront)) {
                battlefront = new BerlinCityInfo();
            }
            battlefront.setCityId(battleFront.getKeyId());
            battlefront.initCityDef(battleFront.getFirstFormList());
            battlefront.setPos(battleFront.getCityPos());
            battlefront.setNextAtkTime(-1);
            battlefront.setLastOccupyTime(now);
            this.battlefronts.put(cityId, battlefront);
        });
    }

    /**
     * 获取指定战况
     *
     * @param timestamp
     * @return
     */
    public Report getReportByTimestamp(int timestamp) {
        return reports.stream().filter(report -> report.getTime() == timestamp).findFirst().orElse(null);
    }

    /**
     * 重置柏林会战状态
     */
    public void clearBerlinWar() {
        int now = TimeHelper.getCurrentSecond();
        this.ranks.clear();
        this.reports.clear();
        this.berlinRecord.clear();
        this.joinBerlinWar.clear();
        this.bbf.clear();
        StaticBerlinWar staticBerlinWar = StaticBerlinWarDataMgr.getBerlinSetting();
        if (CheckNull.isNull(staticBerlinWar)) {
            LogUtil.error("初始化柏林会战出错,s_berlin_war表没有配置柏林");
            return;
        }
        if (CheckNull.isNull(berlinCityInfo)) {
            berlinCityInfo = new BerlinCityInfo();
        }
        // 清除并初始化柏林
        berlinCityInfo.clearAndInit(false, staticBerlinWar);
        List<StaticBerlinWar> battlefronts = StaticBerlinWarDataMgr.getBerlinBattlefront();
        if (CheckNull.isEmpty(battlefronts)) {
            LogUtil.error("初始化柏林会战出错,s_berlin_war表没有配置前线阵地");
            return;
        }
        // 初始化前线阵地
        // 初始化前线阵地
        battlefronts.forEach(sBattleFront -> {
            int cityId = sBattleFront.getKeyId();
            BerlinCityInfo battlefront = this.battlefronts.computeIfAbsent(cityId, (k) -> new BerlinCityInfo());
            // 清除并初始化前线阵地
            battlefront.clearAndInit(this.berlinCityInfo.getCamp() != battlefront.getCamp(), sBattleFront);
        });
    }

    /**
     * 根据cityId获取据点信息
     *
     * @param cityId
     * @return
     */
    public BerlinCityInfo getCityInfoByCityId(int cityId) {
        BerlinCityInfo cityInfo;
        if (berlinCityInfo.getCityId() == cityId) {
            cityInfo = berlinCityInfo;
        } else {
            cityInfo = battlefronts.get(cityId);
        }
        return cityInfo;
    }

    /**
     * 序列化
     *
     * @return
     */
    public SerializePb.SerBerlinWar ser() {
        SerializePb.SerBerlinWar.Builder builder = SerializePb.SerBerlinWar.newBuilder();
        builder.setBerlinCity(berlinCityInfo.ser());
        battlefronts.values().forEach(battlefront -> builder.addBattleFront(battlefront.ser()));
        if (!CheckNull.isNull(getLastDate())) {
            builder.setLastDate((int) (getLastDate().getTime() / TimeHelper.SECOND_MS));
        }

        historyWinner.forEach(e -> builder.addHistoryWinner(PbHelper.createIntLongPc(e.getA(), e.getB())));
        builder.addAllReports(reports);
        berlinRecord.forEach((key, value) -> builder.addRecords(value.ser(key)));
        builder.setStatus(getStatus());
        if (!CheckNull.isNull(getBeginDate())) {
            builder.setBeginDate((int) (getBeginDate().getTime() / TimeHelper.SECOND_MS));
        }
        if (!CheckNull.isNull(getEndDate())) {
            builder.setEndDate((int) (getEndDate().getTime() / TimeHelper.SECOND_MS));
        }
        if (!CheckNull.isNull(getPreViewDate())) {
            builder.setPreViewDate((int) (getPreViewDate().getTime() / TimeHelper.SECOND_MS));
        }
        builder.setAtkCD(getAtkCD());
        berlinJobs.forEach((roleId, job) -> builder.addBerliJobs(PbHelper.createIntLongPc(job, roleId)));
        builder.addAllJoinBerlinWar(getJoinBerlinWar());
        if (!CheckNull.isEmpty(roleInfos)) {
            for (Map.Entry<Long, BerlinRoleInfo> entry : roleInfos.entrySet()) {
                SerializePb.SerBerlinRoleInfo.Builder info = entry.getValue().ser();
                info.setRoleId(entry.getKey());
                builder.addRoleInfos(info);
            }
        }
        builder.setScheduleId(this.scheduleId);
        builder.setBbf(serBBF());
        return builder.build();
    }

    public List<Turple<Integer, Long>> getHistoryWinner() {
        return historyWinner;
    }

    public static BerlinWar getInstance() {
        return DataResource.ac.getBean(GlobalDataManager.class).getGameGlobal().getBerlinWar();
    }

    /**
     * 获取当前霸主信息
     *
     * @return
     */
    public static Turple<Integer, Long> getCurWinner() {
        BerlinWar bl = getInstance();
        if (bl != null && !CheckNull.isEmpty(bl.historyWinner)) {
            List<Turple<Integer, Long>> hWinner = bl.historyWinner;
            return hWinner.get(hWinner.size() - 1);
        }
        return null;
    }

    /**
     * 判断某个roleId是否是霸主
     *
     * @param roleId
     * @return
     */
    public static boolean isCurWinner(long roleId) {
        Turple<Integer, Long> curWinner = getCurWinner();
        if (curWinner != null) {
            return curWinner.getB() == roleId;
        }
        return false;
    }

    /**
     * 获取柏林官职的buff
     *
     * @param roleId
     * @param buffType
     * @return
     */
    public static int getBerlinBuffVal(long roleId, int buffType) {
        BerlinWar berlinWar = BerlinWar.getInstance();
        if (berlinWar == null) return 0;
        if (BerlinWar.isCurWinner(roleId)) { // 是霸主的情况
            StaticBerlinJob sJob = StaticBerlinWarDataMgr.getBerlinJob().get(StaticBerlinJob.BOSS_JOB_ID);
            if (sJob == null) return 0;
            // 圣域争霸活动开启时重置霸主buff失效
            if (berlinWar.getStatus() == WorldConstant.BERLIN_STATUS_OPEN) {
                return 0;
            } else {
                return sJob.getBuffValByBuffType(buffType);
            }
        } else {
            Integer job = berlinWar.getBerlinJobs().get(roleId);
            if (job == null) return 0;// 没有官职
            StaticBerlinJob sJob = StaticBerlinWarDataMgr.getBerlinJob().get(job);
            return sJob.getBuffValByBuffType(buffType);
        }
    }

    public LinkedList<Report> getReports() {
        return reports;
    }

    public BerlinCityInfo getBerlinCityInfo() {
        return berlinCityInfo;
    }

    public Map<Integer, BerlinCityInfo> getBattlefronts() {
        return battlefronts;
    }

    public BerlinCityInfo getBattlefrontByCityId(int cityId) {
        return battlefronts.get(cityId);
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public int getStatus() {
        Date now = new Date();
        if (Objects.nonNull(beginDate) && Objects.nonNull(endDate) && Objects.nonNull(preViewDate)) {
            if (now.after(preViewDate) && now.before(beginDate)) {
                return WorldConstant.BERLIN_STATUS_PRE_DISPLAY;
            } else if (now.after(beginDate) && now.before(endDate)) {
                return WorldConstant.BERLIN_STATUS_OPEN;
            }
        }
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * @return 战前buff时间
     */
    public Date getPrewarBuffDate() {
        return new Date(
                (beginDate.getTime() / TimeHelper.SECOND_MS - WorldConstant.BERLIN_PREWAR_BUFF_TIME * TimeHelper.MINUTE)
                        * TimeHelper.SECOND_MS);
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getPreViewDate() {
        return preViewDate;
    }

    public void setPreViewDate(Date preViewDate) {
        this.preViewDate = preViewDate;
    }

    public int getAtkCD() {
        return atkCD;
    }

    public void setAtkCD(int atkCD) {
        this.atkCD = atkCD;
    }

    public Map<Long, Integer> getBerlinJobs() {
        return berlinJobs;
    }

    public HashSet<Long> getJoinBerlinWar() {
        return joinBerlinWar;
    }

    public void setJoinBerlinWar(HashSet<Long> joinBerlinWar) {
        this.joinBerlinWar = joinBerlinWar;
    }

    /**
     * 获取柏林会战记录数据
     *
     * @param roleId
     * @return
     */
    public BerlinRecord getBerlinRecord(long roleId) {
        BerlinRecord berlinRecord = this.berlinRecord.get(roleId);
        if (CheckNull.isNull(berlinRecord)) {
            berlinRecord = new BerlinRecord();
            this.berlinRecord.put(roleId, berlinRecord);
        }
        return berlinRecord;
    }

    /**
     * 柏林玩家数据
     *
     * @param roleId
     * @return
     */
    public BerlinRoleInfo getRoleInfo(long roleId) {
        BerlinRoleInfo roleInfo = roleInfos.get(roleId);
        if (CheckNull.isNull(roleInfo)) {
            roleInfo = new BerlinRoleInfo();
            roleInfos.put(roleId, roleInfo);
        }
        return roleInfo;
    }

    public Map<Long, BerlinRoleInfo> getRoleInfos() {
        return roleInfos;
    }


    /**
     * 更新世界进程阶段
     *
     * @param scheduleId 世界进程阶段
     */
    public void updateScheduleId(int scheduleId) {
        if (scheduleId > this.scheduleId) {
            this.scheduleId = scheduleId;
        }
    }

    public int getScheduleId() {
        // 未初始化
        if (scheduleId == 0) {
            int scheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
            updateScheduleId(scheduleId);
        }
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * 当前战斗狂热的进度值
     *
     * @return 进度值
     */
    public int currentBFSchedule() {
        if (getScheduleId() < ScheduleConstant.SCHEDULE_ID_12) {
            return 0;
        }
        BerlinCityInfo berlinCityInfo = getBerlinCityInfo();
        // 阵营的势力值
        Map<Integer, Integer> campInfluence = Arrays.stream(Constant.Camp.camps).boxed().collect(Collectors.toMap(Function.identity(), berlinCityInfo::getCampInfluence));
        // 最高势力值的阵营
        int maxCamp = campInfluence.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).orElse(0);
        // 其它阵营势力值之和除以2
        int otherCampValSum = campInfluence.entrySet().stream().filter(en -> en.getKey() != maxCamp).mapToInt(Map.Entry::getValue).sum() / 2;
        // 最高的势力值
        Integer maxCampVal = campInfluence.getOrDefault(maxCamp, 0);
        if (maxCampVal > otherCampValSum) {
            return maxCampVal - otherCampValSum;
        }
        return 0;
    }

    public BerlinBattleFrenzy getBbf() {
        return bbf;
    }

    /**
     * 序列化战斗狂热
     *
     * @return 战斗狂热
     */
    public CommonPb.BerlinBattleFrenzy serBBF() {
        CommonPb.BerlinBattleFrenzy.Builder builder = CommonPb.BerlinBattleFrenzy.newBuilder();
        builder.setSchedule(currentBFSchedule());
        builder.setCount(bbf.getCount());
        builder.setEndTime(bbf.getEndTime());
        builder.setStatus(bbf.getStatus());
        builder.setDuration(bbf.getDuration());
        return builder.build();
    }

    /**
     * 当前是否处于战斗狂热阶段
     *
     * @return
     */
    public boolean isInBattleFrenzy() {
        return Objects.nonNull(bbf) && bbf.getStatus() == BerlinBattleFrenzy.BERLIN_BATTLE_FRENZY_STATUS_1 && bbf.getEndTime() >= TimeHelper.getCurrentSecond();
    }
}
