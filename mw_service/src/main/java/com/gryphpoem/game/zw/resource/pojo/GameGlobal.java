package com.gryphpoem.game.zw.resource.pojo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.DominateSideCity;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.SiLiDominateWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.dominate.impl.StateDominateWorldMap;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.*;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.pb.SerializePb.*;
import com.gryphpoem.game.zw.pojo.p.AttrData;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GlobalConstant;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobal;
import com.gryphpoem.game.zw.resource.domain.p.MineData;
import com.gryphpoem.game.zw.resource.domain.p.RedPacket;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.chat.ChatDialog;
import com.gryphpoem.game.zw.resource.pojo.daily.HonorDaily;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.global.WorldSchedule;
import com.gryphpoem.game.zw.resource.pojo.relic.GlobalRelic;
import com.gryphpoem.game.zw.resource.pojo.sandtable.SandTableContest;
import com.gryphpoem.game.zw.resource.pojo.season.GlobalSeasonData;
import com.gryphpoem.game.zw.resource.pojo.world.Altar;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.CabinetLead;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.Gestapo;
import com.gryphpoem.game.zw.resource.pojo.world.GlobalRebellion;
import com.gryphpoem.game.zw.resource.pojo.world.SuperMine;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.GlobalBattlePass;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.dominate.DominateWorldMapService;
import com.gryphpoem.game.zw.util.FightUtil;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author TanDonghai
 * @ClassName GameGlobal.java
 * @Description 公用数据记录
 * @date 创建时间：2017年3月22日 下午7:27:26
 */
public class GameGlobal {

    private int globalId;
    private Map<Integer, Area> areaMap = new ConcurrentHashMap<>();
    private Map<Integer, City> cityMap = new ConcurrentHashMap<>();
    private Map<Integer, Integer> banditMap = new ConcurrentHashMap<>();
    private Map<Integer, MineData> mineMap = new ConcurrentHashMap<>();
    private Map<Integer, Integer> mineResourceMap = new ConcurrentHashMap<>();
    private Map<Integer, Battle> battleMap = new ConcurrentHashMap<>();
    private Map<Integer, Battle> specialBattleMap = new ConcurrentHashMap<>();
    private Map<Integer, Integer> sendChatCnt = new ConcurrentHashMap<>();
    private volatile WorldTask worldTask; // 世界任务
    private Trophy trophy;// 全服成就
    private Map<Integer, CabinetLead> cabinetLeadMap = new ConcurrentHashMap<>();// 点兵统领
    private Map<Integer, Gestapo> gestapoMap = new ConcurrentHashMap<>();// 盖世太保
    private Map<Integer, LightningWarBoss> lightningWarBossMap = new ConcurrentHashMap<>();// 闪电战boss
    private Map<Integer, Altar> altarMap = new ConcurrentHashMap<>();// 圣坛
    // key:camp
    private Map<Integer, List<SuperMine>> superMineCampMap = new ConcurrentHashMap<>(); // 皇城超级矿点

    // 私聊的消息 key为lordId_lordId的字符串, lordId数值小的在前面
    private Map<String, LinkedList<CommonPb.Chat>> privateChat = new ConcurrentHashMap<>();
    // 私聊会话, key lordId
    private Map<Long, Map<Long, ChatDialog>> dialogMap = new ConcurrentHashMap<>();
    // 阵营聊天
    private Map<Integer, LinkedList<CommonPb.Chat>> campChat = new ConcurrentHashMap<>();
    // 区域聊天
    private Map<Integer, LinkedList<CommonPb.Chat>> areaChat = new ConcurrentHashMap<>();
    // 大喇叭世界聊天
    private List<CommonPb.Chat> worldRoleChat = new LinkedList<CommonPb.Chat>();
    /**
     * 王朝遗迹聊天 不用存储到数据库
     */
    private LinkedList<CommonPb.Chat> relicChat = new LinkedList<>();
    // 活动消息
    private Map<Integer, LinkedList<CommonPb.Chat>> activityChat = new ConcurrentHashMap<>();
    // 红包
    private Map<Integer, RedPacket> redPacketMap = new ConcurrentHashMap<>();
    // 柏林会战
    private BerlinWar berlinWar;
    // 荣耀日常数据
    private HonorDaily honorDaily;
    // 匪军叛乱的数据
    private GlobalRebellion globalRebellion;
    // 德意志反攻的数据
    private CounterAttack counterAttack;
    // 所有的飞艇 ,包含刷新中的飞艇
    private List<AirshipWorldData> allAirshipWorldData = new CopyOnWriteArrayList<>();
    // 世界进度
    private GlobalSchedule globalSchedule;
    /**
     * 世界争霸地图city npc 属性调整次数
     */
    private int crossCityNpcChgAttrCnt;
    // 战令功能的相关数据
    private GlobalBattlePass globalBattlePass;
    //沙盘演武
    private SandTableContest sandTableContest;
    //赛季数据
    private GlobalSeasonData globalSeasonData;
    //是否初始化填充采礦數據問題
    public static boolean needInitMineData = false;
    //开启或关闭战报处理
    public static boolean closeExpiredReport = false;
    public Set<Integer> removedActData = new HashSet<>();
    //开启或关闭数数上报打印
    public static boolean openEventDebug = false;

    //标识当前的跨天定时器是否执行过
    public volatile int dayJobRun;
    public volatile boolean dayJobRunning;
    //遗迹
    private GlobalRelic globalRelic = new GlobalRelic();

    /**
     * 杂七杂八的 数据记录<br>
     * key值参考{@link com.gryphpoem.game.zw.resource.constant.GlobalConstant}
     */
    private Map<String, Map<Integer, Integer>> mixtureData = new HashMap<>();

    public GameGlobal() {
        this.globalRebellion = new GlobalRebellion();
        this.counterAttack = new CounterAttack();
        this.globalBattlePass = new GlobalBattlePass();
        this.setSandTableContest(new SandTableContest());
        this.globalSeasonData = new GlobalSeasonData();
    }

    public GlobalBattlePass getGlobalBattlePass() {
        return globalBattlePass;
    }

    public void setGlobalBattlePass(GlobalBattlePass globalBattlePass) {
        this.globalBattlePass = globalBattlePass;
    }

    /**
     * 新区开服不执行dser
     *
     * @return
     */
    public HonorDaily getHonorDaily() {
        if (CheckNull.isNull(honorDaily)) {
            honorDaily = new HonorDaily();
        }
        return honorDaily;
    }

    public void setHonorDaily(HonorDaily honorDaily) {
        this.honorDaily = honorDaily;
    }

    public int getGlobalId() {
        return globalId;
    }

    public void setGlobalId(int globalId) {
        this.globalId = globalId;
    }

    public Map<Integer, Area> getAreaMap() {
        return areaMap;
    }

    public void setAreaMap(Map<Integer, Area> areaMap) {
        this.areaMap = areaMap;
    }

    public Map<Integer, City> getCityMap() {
        return cityMap;
    }

    public void setCityMap(Map<Integer, City> cityMap) {
        this.cityMap = cityMap;
    }

    public Map<Integer, Integer> getBanditMap() {
        return banditMap;
    }

    public void setBanditMap(Map<Integer, Integer> banditMap) {
        this.banditMap = banditMap;
    }

    public Map<Integer, MineData> getMineMap() {
        return mineMap;
    }

    public void setMineMap(Map<Integer, MineData> mineMap) {
        this.mineMap = mineMap;
    }

    public Map<Integer, Integer> getMineResourceMap() {
        return mineResourceMap;
    }

    public void setMineResourceMap(Map<Integer, Integer> mineResourceMap) {
        this.mineResourceMap = mineResourceMap;
    }

    public Map<Integer, Battle> getBattleMap() {
        return battleMap;
    }

    public void setBattleMap(Map<Integer, Battle> battleMap) {
        this.battleMap = battleMap;
    }

    public WorldTask getWorldTask() {
        if (CheckNull.isNull(worldTask))
            worldTask = new WorldTask(StaticWorldDataMgr.getWorldTask(2));
        return worldTask;
    }

    public Map<Integer, CabinetLead> getCabinetLeadMap() {
        return cabinetLeadMap;
    }

    public void setCabinetLeadMap(Map<Integer, CabinetLead> cabinetLeadMap) {
        this.cabinetLeadMap = cabinetLeadMap;
    }

    public Map<Integer, Gestapo> getGestapoMap() {
        return gestapoMap;
    }

    public void setGestapoMap(Map<Integer, Gestapo> gestapoMap) {
        this.gestapoMap = gestapoMap;
    }

    public Map<Integer, Altar> getAltarMap() {
        return altarMap;
    }

    public void setAltarMap(Map<Integer, Altar> altarMap) {
        this.altarMap = altarMap;
    }

    public Map<String, LinkedList<CommonPb.Chat>> getPrivateChat() {
        return privateChat;
    }

    public Map<Long, Map<Long, ChatDialog>> getDialogMap() {
        return dialogMap;
    }

    public Map<Integer, LinkedList<CommonPb.Chat>> getCampChat() {
        return campChat;
    }

    public Map<Integer, LinkedList<CommonPb.Chat>> getAreaChat() {
        return areaChat;
    }

    public List<CommonPb.Chat> getWorldRoleChat() {
        return worldRoleChat;
    }

    public Map<Integer, LinkedList<CommonPb.Chat>> getActivityChat() {
        return activityChat;
    }

    public Map<Integer, LightningWarBoss> getLightningWarBossMap() {
        return lightningWarBossMap;
    }

    public void setLightningWarBossMap(Map<Integer, LightningWarBoss> lightningWarBossMap) {
        this.lightningWarBossMap = lightningWarBossMap;
    }

    public Map<Integer, Battle> getSpecialBattleMap() {
        return specialBattleMap;
    }

    public void setSpecialBattleMap(Map<Integer, Battle> specialBattleMap) {
        this.specialBattleMap = specialBattleMap;
    }

    public Map<Integer, Integer> getSendChatCnt() {
        return sendChatCnt;
    }

    public void setSendChatCnt(Map<Integer, Integer> sendChatCnt) {
        this.sendChatCnt = sendChatCnt;
    }

    public Map<Integer, RedPacket> getRedPacketMap() {
        return redPacketMap;
    }

    public Map<Integer, List<SuperMine>> getSuperMineCampMap() {
        return superMineCampMap;
    }

    public BerlinWar getBerlinWar() {
        return berlinWar;
    }

    public void setBerlinWar(BerlinWar berlinWar) {
        this.berlinWar = berlinWar;
    }

    public GlobalRebellion getGlobalRebellion() {
        return globalRebellion;
    }

    public CounterAttack getCounterAttack() {
        return counterAttack;
    }

    public List<AirshipWorldData> getAllAirshipWorldData() {
        return allAirshipWorldData;
    }

    public int getCrossCityNpcChgAttrCnt() {
        return crossCityNpcChgAttrCnt;
    }

    public void setCrossCityNpcChgAttrCnt(int crossCityNpcChgAttrCnt) {
        this.crossCityNpcChgAttrCnt = crossCityNpcChgAttrCnt;
    }

    public DbGlobal ser() {
        DbGlobal global = new DbGlobal();
        global.setGlobalId(globalId);
        global.setMapArea(serMapArea());
        global.setCity(serCity());
        global.setBandit(serBandit());
        global.setMine(serMine());
        global.setBattle(serBattle());
        global.setWorldTask(serWorldTask());
        global.setCabinetLead(serCabinetLead());
        global.setPrivateChat(serChat());
        global.setTrophy(serTrophy());
        global.setGestapo(serGestapo());
        global.setGlobalExt(serGlobalExt());
        global.setWorldSchedule(serWroldSchedule());
        global.setDominateData(serDominateData());
        return global;
    }

    private byte[] serTrophy() {
        SerGlobalTrophy.Builder ser = SerGlobalTrophy.newBuilder();
        if (trophy != null) {
            ser.setGlod(trophy.getGold().get());
            ser.setSolarTermsStartTime(trophy.getSolarTermsStartTime());
            if (trophy.getRankEquips() != null && trophy.getRankEquips().keySet().size() > 0) {
                for (int quality : trophy.getRankEquips().keySet()) {
                    RankEquip.Builder rankEquip = RankEquip.newBuilder();
                    rankEquip.setQuality(quality);
                    rankEquip.addAllLorId(trophy.getRankEquips().get(quality));
                    ser.addRankEquips(rankEquip);
                }
            }
            for (Integer combatId : trophy.getPassMultCombat()) {
                ser.addPassMultCombat(combatId);
            }
        }
        return ser.build().toByteArray();
    }

    // // 私聊的消息 key为lordId_lordId的字符串, lordId数值小的在前面
    // private Map<String, LinkedList<CommonPb.Chat>> privateChat = new ConcurrentHashMap<>();
    // // 私聊会话, key lordId
    // private Map<Long, Map<Long, ChatDialog>> dialogMap = new ConcurrentHashMap<>();
    private byte[] serChat() {
        SerChat.Builder ser = SerChat.newBuilder();
        for (Entry<String, LinkedList<Chat>> kv : privateChat.entrySet()) {
            DbChatHistory.Builder b = DbChatHistory.newBuilder();
            b.setRoleKey(kv.getKey());
            b.addAllChats(kv.getValue());
            ser.addChatHistory(b.build());
        }

        for (Entry<Long, Map<Long, ChatDialog>> kv : dialogMap.entrySet()) {
            DbChatDialogByLord.Builder dbLord = DbChatDialogByLord.newBuilder();
            dbLord.setRoleId(kv.getKey());
            for (ChatDialog dialog : kv.getValue().values()) {
                DbChatDialog.Builder db = DbChatDialog.newBuilder();
                db.setChat(dialog.getChat());
                db.setState(dialog.getState());
                db.setTargetId(dialog.getTargetId());
                dbLord.addDialog(db.build());
            }
            ser.addChatDialog(dbLord.build());
        }
        for (Chat c : worldRoleChat) {
            ser.addWorldRoleChat(c);
        }
        for (RedPacket rp : redPacketMap.values()) {

            ser.addRedPacket(PbHelper.createRedPacketPb(rp, null));
        }
        //阵营聊天
        for (Entry<Integer, LinkedList<Chat>> kv : campChat.entrySet()) {
            LinkedList<Chat> chatList = kv.getValue();
            List<Chat> systemNoticelist = new ArrayList<>();
            for (Chat chat : chatList) {
                if (chat.getChatId() > 0 && chat.getLordId() == 0) {
                    systemNoticelist.add(chat);
                }
            }
            if (systemNoticelist.size() > ChatConst.SYSTEM_NOTICE_NUM) {
                List<Chat> newNoticelist = new ArrayList<>();
                for (Chat c1 : systemNoticelist) {
                    if (ChatConst.SAVE_CHATID_LIST.contains(c1.getChatId())) {
                        newNoticelist.add(0, c1);
                    } else {
                        newNoticelist.add(c1);
                    }
                }
                newNoticelist = newNoticelist.subList(ChatConst.SYSTEM_NOTICE_NUM, newNoticelist.size());
                for (Chat ct : newNoticelist) {
                    chatList.remove(ct);
                }
            }
            DbCampChat.Builder b = DbCampChat.newBuilder();
            b.setCamp(kv.getKey());
            b.addAllChats(chatList);
            ser.addCampChat(b.build());
        }
        //区域聊天
        for (Entry<Integer, LinkedList<Chat>> kv : areaChat.entrySet()) {
            DbAreaChat.Builder b = DbAreaChat.newBuilder();
            b.setArea(kv.getKey());
            b.addAllChats(kv.getValue());
            ser.addAreaChat(b.build());
        }
        //活动消息
        for (Entry<Integer, LinkedList<Chat>> kv : activityChat.entrySet()) {
            DbActivityChat.Builder b = DbActivityChat.newBuilder();
            b.setActivityId(kv.getKey());
            b.addAllChats(kv.getValue());
            ser.addActivityChat(b.build());
        }
        return ser.build().toByteArray();
    }


    private byte[] serCabinetLead() {
        SerCabinetLead.Builder ser = SerCabinetLead.newBuilder();
        for (CabinetLead lead : cabinetLeadMap.values()) {
            ser.addCabinetLead(PbHelper.createCabinetLeadPb(lead));
        }
        return ser.build().toByteArray();
    }

    private byte[] serGlobalExt() {
        SerGlobalExt.Builder ser = SerGlobalExt.newBuilder();
        for (Entry<Integer, LightningWarBoss> entry : lightningWarBossMap.entrySet()) {
            ser.addBoss(PbHelper.createLightningWarBossPb(entry.getKey(), entry.getValue()));
        }
        ser.addAllSendChatCnt(PbHelper.createTwoIntListByMap(sendChatCnt));
        if (!CheckNull.isNull(berlinWar)) {
            ser.setBerlinWar(berlinWar.ser());
        }
        if (!CheckNull.isNull(honorDaily)) {
            ser.setHonorDaily(honorDaily.ser());
        }
        if (globalRebellion != null) {
            ser.setGlobalRebellion(globalRebellion.ser(true));
        }
        if (!CheckNull.isNull(counterAttack)) {
            ser.setCounterAttack(counterAttack.ser());
        }
        ser.setCrossCityNpcChgAttrCnt(crossCityNpcChgAttrCnt);
        for (Entry<String, Map<Integer, Integer>> entry : mixtureData.entrySet()) {
            ser.addMixtureData(PbHelper.createMixtureData(entry.getKey(), entry.getValue()));
        }
        if (!CheckNull.isNull(globalBattlePass)) {
            ser.setGlobalBattlePass(globalBattlePass.ser());
        }
        if (Objects.nonNull(sandTableContest)) {
            ser.setSandTableContest(sandTableContest.ser());
        }
        if (Objects.nonNull(globalSeasonData)) {
            ser.setSerSeasonGlobalData(globalSeasonData.ser());
        }
        if (CheckNull.nonEmpty(removedActData)) {
            ser.addAllRemovedActData(this.removedActData);
        }
        ser.setSerGlobalRelic(this.globalRelic.ser());
        ser.setFightId(FightUtil.FIGHT_ID_GENERATOR);
        return ser.build().toByteArray();
    }

    public void deDominateData(byte[] data) throws InvalidProtocolBufferException {
        if (data == null)
            return;

        SerializePb.SerDominateData ser = SerializePb.SerDominateData.parseFrom(data);
        DataResource.ac.getBean(DominateWorldMapService.class).setSer(ser);
    }


    public byte[] serDominateData() {
        SerializePb.SerDominateData.Builder ser = SerializePb.SerDominateData.newBuilder();
        SerializePb.SerStateDominateWorldMap pb = StateDominateWorldMap.getInstance().createWorldMapPb(true);
        if (Objects.nonNull(pb)) {
            ser.setStateMap(pb);
        }
        SerializePb.SerSiLiDominateWorldMap pb_ = SiLiDominateWorldMap.getInstance().createWorldMapPb(true);
        if (Objects.nonNull(pb_)) {
            ser.setSiLiMap(pb_);
        }
        return ser.build().toByteArray();
    }

    private byte[] serGestapo() {
        SerGestapo.Builder ser = SerGestapo.newBuilder();
        for (Gestapo gestapo : gestapoMap.values()) {
            ser.addGestapo(PbHelper.createGestapoPb(gestapo));
        }
        for (AirshipWorldData aswd : allAirshipWorldData) {
            ser.addAirship(PbHelper.createAirshipDBPb(aswd));
        }
        altarMap.forEach((k, v) -> ser.addAltar(v.ser()));
        return ser.build().toByteArray();
    }

    private byte[] serWorldTask() {
        SerWorldTask.Builder ser = SerWorldTask.newBuilder();
        if (worldTask == null) {
            worldTask = new WorldTask(StaticWorldDataMgr.getWorldTask(2));
        }
        ser.setWorldTaskId(worldTask.getWorldTaskId().get());
        if (worldTask.getWorldTaskMap() != null && !worldTask.getWorldTaskMap().isEmpty()) {
            ser.addAllWorldTask(worldTask.getWorldTaskMap().values());
        }
        if (worldTask.getDefender() != null) {
            for (com.gryphpoem.game.zw.pojo.p.Force force : worldTask.getDefender().getForces()) {
                ser.addForce(CommonPb.Force.newBuilder().setNpcId(force.id).setHp(force.hp).setCurLine(force.curLine));
            }
        }
        LogUtil.debug("序列化世界任务,当前id=" + ser.getWorldTaskId() + ",worldTask=" + worldTask);
        return ser.build().toByteArray();
    }

    /**
     * 序列化世界进程
     *
     * @return
     */
    private byte[] serWroldSchedule() {
        SerWroldSchedule.Builder ser = SerWroldSchedule.newBuilder();
        if (!CheckNull.isNull(this.globalSchedule)) {
            ser.setCurrentScheduleId(this.globalSchedule.getCurrentScheduleId());
            for (Entry<Integer, WorldSchedule> en : this.globalSchedule.getScheduleMap().entrySet()) {
                ser.addWorldSchedule(en.getValue().ser(null, true));
            }
            LogUtil.debug("序列化世界进度,当前id=" + ser.getCurrentScheduleId());
        }
        return ser.build().toByteArray();
    }

    private byte[] serMapArea() {
        SerArea.Builder ser = SerArea.newBuilder();
        for (Area area : areaMap.values()) {
            ser.addArea(PbHelper.createAreaPb(area));
        }
        return ser.build().toByteArray();
    }

    private byte[] serCity() {
        SerCity.Builder ser = SerCity.newBuilder();
        for (City city : cityMap.values()) {
            if (city instanceof DominateSideCity) {
                ser.addDominateCity(((DominateSideCity) city).createPb(true));
            } else {
                ser.addCity(PbHelper.createCityPb(city));
            }
        }
        return ser.build().toByteArray();
    }

    private byte[] serBandit() {
        SerBandit.Builder ser = SerBandit.newBuilder();
        for (Entry<Integer, Integer> entry : banditMap.entrySet()) {
            ser.addBandit(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        return ser.build().toByteArray();
    }

    private byte[] serMine() {
        SerMine.Builder ser = SerMine.newBuilder();
        for (Entry<Integer, MineData> entry : mineMap.entrySet()) {
            ser.addNewMinePb(PbHelper.createMineDataPb(entry.getKey(), entry.getValue()));
        }
        for (Entry<Integer, Integer> entry : mineResourceMap.entrySet()) {
            ser.addResource(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        superMineCampMap.values().stream().flatMap(list -> list.stream())
                .forEach(sm -> ser.addSuperMine(PbHelper.createSuperMineDbPb(sm)));
        return ser.build().toByteArray();
    }

    private byte[] serBattle() {
        SerBattle.Builder ser = SerBattle.newBuilder();
        for (Battle battle : battleMap.values()) {
            ser.addBattle(PbHelper.createBattlePOPb(battle));
        }
        for (Battle battle : specialBattleMap.values()) {
            ser.addSpecialBattle(PbHelper.createBattlePOPb(battle));
        }
        return ser.build().toByteArray();
    }

    public void dser(DbGlobal global) throws InvalidProtocolBufferException {
        this.globalId = global.getGlobalId();
        dserArea(global.getMapArea());
        dserCity(global.getCity());
        dserBandit(global.getBandit());
        dserMine(global.getMine());
        dserBattle(global.getBattle());
        dserWorldTask(global.getWorldTask());
        dserCabinetLead(global.getCabinetLead());
        dserChat(global.getPrivateChat());
        dserTrophy(global.getTrophy());
        dserGestapo(global.getGestapo());
        dserGlobalExt(global.getGlobalExt());
        dserWorldSchedule(global.getWorldSchedule());
        deDominateData(global.getDominateData());
    }

    private void dserTrophy(byte[] data) throws InvalidProtocolBufferException {
        if (data == null) {
            return;
        }
        SerGlobalTrophy ser = SerGlobalTrophy.parseFrom(data);
        this.trophy = new Trophy(ser);
    }

    private void dserChat(byte[] data) throws InvalidProtocolBufferException {
        if (data == null) return;
        SerChat ser = SerChat.parseFrom(data);
        for (CommonPb.DbChatHistory h : ser.getChatHistoryList()) {
            LinkedList<CommonPb.Chat> list = new LinkedList<>();
            list.addAll(h.getChatsList());
            privateChat.put(h.getRoleKey(), list);
        }

        for (CommonPb.DbChatDialogByLord d : ser.getChatDialogList()) {
            Map<Long, ChatDialog> map = new HashMap<>();
            for (DbChatDialog dbDialog : d.getDialogList()) {
                map.put(dbDialog.getTargetId(),
                        new ChatDialog(dbDialog.getTargetId(), dbDialog.getState(), dbDialog.getChat()));
            }
            dialogMap.put(d.getRoleId(), map);
        }
        for (Chat c : ser.getWorldRoleChatList()) {
            worldRoleChat.add(c);
        }
        for (CommonPb.DbCampChat cc : ser.getCampChatList()) {
            LinkedList<CommonPb.Chat> list = new LinkedList<>();
            list.addAll(cc.getChatsList());
            campChat.put(cc.getCamp(), list);
        }
        for (CommonPb.DbAreaChat ac : ser.getAreaChatList()) {
            LinkedList<CommonPb.Chat> list = new LinkedList<>();
            list.addAll(ac.getChatsList());
            areaChat.put(ac.getArea(), list);
        }
        for (CommonPb.DbActivityChat acc : ser.getActivityChatList()) {
            LinkedList<CommonPb.Chat> list = new LinkedList<>();
            list.addAll(acc.getChatsList());
            activityChat.put(acc.getActivityId(), list);
        }

        int maxRedPackId = 1;// 找出最大值
        for (CommonPb.RedPacket r : ser.getRedPacketList()) {
            redPacketMap.put(r.getId(), new RedPacket(r));
            if (maxRedPackId < r.getId()) {
                maxRedPackId = r.getId();
            }
        }
        RedPacket.ID_KEY = maxRedPackId;
    }

    private void dserCabinetLead(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }
        SerCabinetLead ser = SerCabinetLead.parseFrom(data);
        for (CommonPb.CabinetLead lead : ser.getCabinetLeadList()) {
            cabinetLeadMap.put(lead.getPos(), new CabinetLead(lead));
        }
    }

    private void dserGestapo(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }
        SerGestapo ser = SerGestapo.parseFrom(data);
        for (CommonPb.Gestapo gestapo : ser.getGestapoList()) {
            gestapoMap.put(gestapo.getPos(), new Gestapo(gestapo));
        }
        for (CommonPb.Airship airship : ser.getAirshipList()) {
            allAirshipWorldData.add(new AirshipWorldData(airship));
        }
        for (CommonPb.Altar altar : ser.getAltarList()) {
            altarMap.put(altar.getPos(), new Altar(altar));
        }
    }

    private void dserGlobalExt(byte[] data) throws InvalidProtocolBufferException {
        if (data == null) {
            return;
        }
        SerGlobalExt ser = SerGlobalExt.parseFrom(data);
        for (SerLightningWarBoss serBoss : ser.getBossList()) {
            LightningWarBoss boss = new LightningWarBoss(serBoss);
            lightningWarBossMap.put(serBoss.getId(), boss);
            boss.setFighter(createDserBossFighter(serBoss.getForceList()));
        }
        for (CommonPb.TwoInt twoInt : ser.getSendChatCntList()) {
            sendChatCnt.put(twoInt.getV1(), twoInt.getV2());
        }
        if (CheckNull.isNull(berlinWar)) {
            berlinWar = new BerlinWar(ser.getBerlinWar());
        }
        if (CheckNull.isNull(honorDaily)) {
            honorDaily = new HonorDaily(ser.getHonorDaily());
        }
        if (ser.hasGlobalRebellion()) {
            globalRebellion.dser(ser.getGlobalRebellion());
        }
        if (ser.hasCounterAttack()) {
            counterAttack.dser(ser.getCounterAttack());
            counterAttack.setFighter(createDserBossFighter(ser.getCounterAttack().getForceList()));
        }
        if (ser.hasCrossCityNpcChgAttrCnt()) {
            crossCityNpcChgAttrCnt = ser.getCrossCityNpcChgAttrCnt();
        }
        for (SerMixtureData serMixtureData : ser.getMixtureDataList()) {
            HashMap<Integer, Integer> val = new HashMap<>();
            for (TwoInt twoInt : serMixtureData.getValueList()) {
                val.put(twoInt.getV1(), twoInt.getV2());
            }
            mixtureData.put(serMixtureData.getKey(), val);
        }
        if (ser.hasGlobalBattlePass()) {
            globalBattlePass.dser(ser.getGlobalBattlePass());
        }
        if (ser.hasSandTableContest()) {
            sandTableContest.deser(ser.getSandTableContest());
        }
        if (ser.hasSerSeasonGlobalData()) {
            globalSeasonData.deser(ser.getSerSeasonGlobalData());
        }
        if (CheckNull.nonEmpty(ser.getRemovedActDataList())) {
            removedActData.addAll(ser.getRemovedActDataList());
        }
        if (ser.hasSerGlobalRelic()) {
            globalRelic.dser(ser.getSerGlobalRelic());
        }
        FightUtil.FIGHT_ID_GENERATOR = ser.getFightId();
    }

    private void dserWorldTask(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }
        SerWorldTask ser = SerWorldTask.parseFrom(data);
        if (worldTask == null) {
            worldTask = new WorldTask(StaticWorldDataMgr.getWorldTask(2));
        }
        int curTaskId = ser.getWorldTaskId();
        LogUtil.debug("begin加载世界任务数据,当前id=" + curTaskId);
        if (curTaskId > 0) {
            worldTask.getWorldTaskId().set(curTaskId);
            for (CommonPb.WorldTask pb : ser.getWorldTaskList()) {
                worldTask.getWorldTaskMap().put(pb.getTaskId(), pb);
            }
            worldTask.setDefender(createDserBossFighter(ser.getForceList()));
            // 更新修复当前boss血量
            Fighter bossDef = worldTask.getDefender();
            LogUtil.debug("BOSS_FIGHTER:", bossDef);
            if (bossDef != null) {
                CommonPb.WorldTask oldWorldTaskData = worldTask.getWorldTaskMap().get(curTaskId);
                if (oldWorldTaskData != null) {
                    CommonPb.WorldTask.Builder builder = CommonPb.WorldTask.newBuilder();
                    builder.setTaskId(oldWorldTaskData.getTaskId());
                    builder.setTaskCnt(oldWorldTaskData.getTaskCnt());
                    builder.setCamp(oldWorldTaskData.getCamp());
                    builder.setHp(bossDef.total - bossDef.lost);// boss的真实血量
                    CommonPb.WorldTask boosTask = builder.build();
                    worldTask.getWorldTaskMap().put(curTaskId, boosTask);
                    LogUtil.debug("加载修复世界BOSS:", boosTask);
                }
            }
        }
        LogUtil.debug("end加载世界任务数据,当前id=" + ser.getWorldTaskId());
    }


    private void dserWorldSchedule(byte[] worldSchedule) throws InvalidProtocolBufferException {
        if (null == worldSchedule) {
            return;
        }
        SerWroldSchedule ser = SerWroldSchedule.parseFrom(worldSchedule);
        globalSchedule = new GlobalSchedule(ser);
        LogUtil.debug("end加载世界进度数据,当前id=" + ser.getCurrentScheduleId());
    }

    private void dserArea(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }
        SerArea ser = SerArea.parseFrom(data);
        for (CommonPb.Area area : ser.getAreaList()) {
            areaMap.put(area.getArea(), new Area(area));
        }
    }

    private void dserCity(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerCity ser = SerCity.parseFrom(data);
        for (CommonPb.City city : ser.getCityList()) {
            cityMap.put(city.getCityId(), new City(city));
        }
        for (SerializePb.SerDominateSideCity city : ser.getDominateCityList()) {
            cityMap.put(city.getCity().getCityId(), new DominateSideCity(city));
        }
    }

    private void dserBandit(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerBandit ser = SerBandit.parseFrom(data);
        for (CommonPb.TwoInt bandit : ser.getBanditList()) {
            banditMap.put(bandit.getV1(), bandit.getV2());
        }
    }

    private void dserMine(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }
        SerMine ser = SerMine.parseFrom(data);
        if (!ObjectUtils.isEmpty(ser.getNewMinePbList())) {
            for (CommonPb.MineDataPb mine : ser.getNewMinePbList()) {
                mineMap.put(mine.getPos(), MineData.serMineData(mine));
            }
        } else if (!ObjectUtils.isEmpty(ser.getMineList())) {
            needInitMineData = true;
            for (CommonPb.TwoInt mine : ser.getMineList()) {
                mineMap.put(mine.getV1(), MineData.serMineData(mine.getV2()));
            }
        }

        for (CommonPb.TwoInt bandit : ser.getResourceList()) {
            mineResourceMap.put(bandit.getV1(), bandit.getV2());
        }
        for (CommonPb.SuperMine sm : ser.getSuperMineList()) {
            int camp = sm.getCamp();
            List<SuperMine> list = superMineCampMap.get(camp);
            if (list == null) {
                list = new ArrayList<>();
                superMineCampMap.put(camp, list);
            }
            list.add(new SuperMine(sm));
        }
    }

    private void dserBattle(byte[] data) throws InvalidProtocolBufferException {
        if (null == data) {
            return;
        }

        SerBattle ser = SerBattle.parseFrom(data);
        for (CommonPb.BattlePO battle : ser.getBattleList()) {
            battleMap.put(battle.getBattleId(), new Battle(battle));
        }
        for (CommonPb.BattlePO battle : ser.getSpecialBattleList()) {
            specialBattleMap.put(battle.getBattleId(), new Battle(battle));
        }
    }

    /**
     * boss反序列化回内存中是创建的fighter对象, 与新创建的fighter不一样
     *
     * @param npcIdList
     * @return
     */
    private Fighter createDserBossFighter(List<CommonPb.Force> npcIdList) {
        if (null == npcIdList) {
            return null;
        }
        Fighter fighter = new Fighter();
        for (CommonPb.Force pb : npcIdList) {
            com.gryphpoem.game.zw.pojo.p.Force force = createBossNpcForce(pb.getNpcId(), pb.getHp());
            force.roleType = Constant.Role.CITY;
            fighter.addForce(force);
            LogUtil.debug("反序列化BOSS_NPC :", force);
        }
        fighter.roleType = Constant.Role.BANDIT;
        int allHp = 0; // boss的真实血量
        for (com.gryphpoem.game.zw.pojo.p.Force f : fighter.forces) {
            allHp += f.hp;
        }
        fighter.lost = fighter.total - allHp;// 总损兵
        return fighter;
    }

    /**
     * 创建bossNpc的势力
     *
     * @param npcId
     * @param curHp 当前血量
     * @return
     */
    private com.gryphpoem.game.zw.pojo.p.Force createBossNpcForce(int npcId, int curHp) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        com.gryphpoem.game.zw.pojo.p.Force force = new com.gryphpoem.game.zw.pojo.p.Force(attrData, npc.getArmType(), npc.getLine(), npcId);
        force.hp = curHp;
        force.totalLost = force.maxHp - force.hp; // 总损兵
        int tmpTotalLost = force.totalLost;
        // 计算当前是第几排,从0开始
        int curLine = 0;// 当前第几排
        int cnt = 4;// 防止死循环
        while (tmpTotalLost >= force.lead && cnt-- > 0) {
            curLine++;
            tmpTotalLost -= force.lead;
        }
        force.count = force.lead - tmpTotalLost;// 本排兵剩余数量
        force.curLine = curLine;
        return force;
    }

    public Trophy getTrophy() {
        if (trophy == null) {
            trophy = new Trophy();
        }
        return trophy;
    }

    public GlobalSchedule getGlobalSchedule() {
        return globalSchedule;
    }

    public void setGlobalSchedule(GlobalSchedule globalSchedule) {
        this.globalSchedule = globalSchedule;
    }

    public Map<String, Map<Integer, Integer>> getMixtureData() {
        return mixtureData;
    }

    public void cleanMixtureData(List<String> cleanList) {
        for (String key : cleanList) {
            mixtureData.remove(key);
        }
    }

    public Map<Integer, Integer> getMixtureDataById(String globalConstantKey) {
        Map<Integer, Integer> map = mixtureData.get(globalConstantKey);
        if (CheckNull.isNull(map)) {
            map = new HashMap<>();
        }
        return map;
    }

    /**
     * 根据key和阵营取属性值
     *
     * @param key  {@link GlobalConstant } 中定义的key值
     * @param camp {@link Constant.Camp} 中定义的阵营
     * @return 返回null, 说明这个key不区分阵营, {@link GlobalConstant} 中不包含这个key
     */
    public Map<Integer, Integer> getMixtureDataById(String key, int camp) {
        if (GlobalConstant.needDistinguishCampKey(key)) {
            Map<Integer, Integer> map = mixtureData.get(key + camp);
            if (CheckNull.isNull(map)) {
                map = new HashMap<>();
            }
            return map;
        }
        return null;
    }

    public void setMixtureData(String globalConstantKey, Map<Integer, Integer> val) {
        mixtureData.put(globalConstantKey, val);
    }

    public SandTableContest getSandTableContest() {
        return sandTableContest;
    }

    public void setSandTableContest(SandTableContest sandTableContest) {
        this.sandTableContest = sandTableContest;
    }

    public GlobalSeasonData getGlobalSeasonData() {
        return globalSeasonData;
    }

    public GlobalRelic getGlobalRelic() {
        return globalRelic;
    }

    public LinkedList<Chat> getRelicChat() {
        return relicChat;
    }

    public void setRelicChat(LinkedList<Chat> relicChat) {
        this.relicChat = relicChat;
    }
}
