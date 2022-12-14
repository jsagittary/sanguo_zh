package com.gryphpoem.game.zw.resource.pojo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.*;
import com.gryphpoem.game.zw.pb.SerializePb.*;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GlobalConstant;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobal;
import com.gryphpoem.game.zw.resource.domain.p.MineData;
import com.gryphpoem.game.zw.resource.domain.p.RedPacket;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.chat.ChatDialog;
import com.gryphpoem.game.zw.resource.pojo.daily.HonorDaily;
import com.gryphpoem.game.zw.resource.pojo.fight.AttrData;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
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
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author TanDonghai
 * @ClassName GameGlobal.java
 * @Description ??????????????????
 * @date ???????????????2017???3???22??? ??????7:27:26
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
    private volatile WorldTask worldTask; // ????????????
    private Trophy trophy;// ????????????
    private Map<Integer, CabinetLead> cabinetLeadMap = new ConcurrentHashMap<>();// ????????????
    private Map<Integer, Gestapo> gestapoMap = new ConcurrentHashMap<>();// ????????????
    private Map<Integer, LightningWarBoss> lightningWarBossMap = new ConcurrentHashMap<>();// ?????????boss
    private Map<Integer, Altar> altarMap = new ConcurrentHashMap<>();// ??????
    // key:camp
    private Map<Integer, List<SuperMine>> superMineCampMap = new ConcurrentHashMap<>(); // ??????????????????

    // ??????????????? key???lordId_lordId????????????, lordId?????????????????????
    private Map<String, LinkedList<CommonPb.Chat>> privateChat = new ConcurrentHashMap<>();
    // ????????????, key lordId
    private Map<Long, Map<Long, ChatDialog>> dialogMap = new ConcurrentHashMap<>();
    // ????????????
    private Map<Integer, LinkedList<CommonPb.Chat>> campChat = new ConcurrentHashMap<>();
    // ????????????
    private Map<Integer, LinkedList<CommonPb.Chat>> areaChat = new ConcurrentHashMap<>();
    // ?????????????????????
    private List<CommonPb.Chat> worldRoleChat = new LinkedList<CommonPb.Chat>();
    /**
     * ?????????????????? ????????????????????????
     */
    private LinkedList<CommonPb.Chat> relicChat = new LinkedList<>();
    // ????????????
    private Map<Integer, LinkedList<CommonPb.Chat>> activityChat = new ConcurrentHashMap<>();
    // ??????
    private Map<Integer, RedPacket> redPacketMap = new ConcurrentHashMap<>();
    // ????????????
    private BerlinWar berlinWar;
    // ??????????????????
    private HonorDaily honorDaily;
    // ?????????????????????
    private GlobalRebellion globalRebellion;
    // ????????????????????????
    private CounterAttack counterAttack;
    // ??????????????? ,????????????????????????
    private List<AirshipWorldData> allAirshipWorldData = new CopyOnWriteArrayList<>();
    // ????????????
    private GlobalSchedule globalSchedule;
    /**
     * ??????????????????city npc ??????????????????
     */
    private int crossCityNpcChgAttrCnt;
    // ???????????????????????????
    private GlobalBattlePass globalBattlePass;
    //????????????
    private SandTableContest sandTableContest;
    //????????????
    private GlobalSeasonData globalSeasonData;
    //???????????????????????????????????????
    public static boolean needInitMineData = false;
    //???????????????????????????
    public static boolean closeExpiredReport = false;
    public Set<Integer> removedActData = new HashSet<>();
    //?????????????????????????????????
    public static boolean openEventDebug = false;

    //?????????????????????????????????????????????
    public volatile int dayJobRun;
    public volatile boolean dayJobRunning;
    //??????
    private GlobalRelic globalRelic = new GlobalRelic();

    /**
     * ??????????????? ????????????<br>
     * key?????????{@link com.gryphpoem.game.zw.resource.constant.GlobalConstant}
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
     * ?????????????????????dser
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

    // // ??????????????? key???lordId_lordId????????????, lordId?????????????????????
    // private Map<String, LinkedList<CommonPb.Chat>> privateChat = new ConcurrentHashMap<>();
    // // ????????????, key lordId
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
        //????????????
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
        //????????????
        for (Entry<Integer, LinkedList<Chat>> kv : areaChat.entrySet()) {
            DbAreaChat.Builder b = DbAreaChat.newBuilder();
            b.setArea(kv.getKey());
            b.addAllChats(kv.getValue());
            ser.addAreaChat(b.build());
        }
        //????????????
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
            for (Force force : worldTask.getDefender().getForces()) {
                ser.addForce(CommonPb.Force.newBuilder().setNpcId(force.id).setHp(force.hp).setCurLine(force.curLine));
            }
        }
        LogUtil.debug("?????????????????????,??????id=" + ser.getWorldTaskId() + ",worldTask=" + worldTask);
        return ser.build().toByteArray();
    }

    /**
     * ?????????????????????
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
            LogUtil.debug("?????????????????????,??????id=" + ser.getCurrentScheduleId());
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
            ser.addCity(PbHelper.createCityPb(city));
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

        int maxRedPackId = 1;// ???????????????
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
        LogUtil.debug("begin????????????????????????,??????id=" + curTaskId);
        if (curTaskId > 0) {
            worldTask.getWorldTaskId().set(curTaskId);
            for (CommonPb.WorldTask pb : ser.getWorldTaskList()) {
                worldTask.getWorldTaskMap().put(pb.getTaskId(), pb);
            }
            worldTask.setDefender(createDserBossFighter(ser.getForceList()));
            // ??????????????????boss??????
            Fighter bossDef = worldTask.getDefender();
            LogUtil.debug("BOSS_FIGHTER:", bossDef);
            if (bossDef != null) {
                CommonPb.WorldTask oldWorldTaskData = worldTask.getWorldTaskMap().get(curTaskId);
                if (oldWorldTaskData != null) {
                    CommonPb.WorldTask.Builder builder = CommonPb.WorldTask.newBuilder();
                    builder.setTaskId(oldWorldTaskData.getTaskId());
                    builder.setTaskCnt(oldWorldTaskData.getTaskCnt());
                    builder.setCamp(oldWorldTaskData.getCamp());
                    builder.setHp(bossDef.total - bossDef.lost);// boss???????????????
                    CommonPb.WorldTask boosTask = builder.build();
                    worldTask.getWorldTaskMap().put(curTaskId, boosTask);
                    LogUtil.debug("??????????????????BOSS:", boosTask);
                }
            }
        }
        LogUtil.debug("end????????????????????????,??????id=" + ser.getWorldTaskId());
    }


    private void dserWorldSchedule(byte[] worldSchedule) throws InvalidProtocolBufferException {
        if (null == worldSchedule) {
            return;
        }
        SerWroldSchedule ser = SerWroldSchedule.parseFrom(worldSchedule);
        globalSchedule = new GlobalSchedule(ser);
        LogUtil.debug("end????????????????????????,??????id=" + ser.getCurrentScheduleId());
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
     * boss????????????????????????????????????fighter??????, ???????????????fighter?????????
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
            Force force = createBossNpcForce(pb.getNpcId(), pb.getHp());
            force.roleType = Constant.Role.CITY;
            fighter.addForce(force);
            LogUtil.debug("????????????BOSS_NPC :", force);
        }
        fighter.roleType = Constant.Role.BANDIT;
        int allHp = 0; // boss???????????????
        for (Force f : fighter.forces) {
            allHp += f.hp;
        }
        fighter.lost = fighter.total - allHp;// ?????????
        return fighter;
    }

    /**
     * ??????bossNpc?????????
     *
     * @param npcId
     * @param curHp ????????????
     * @return
     */
    private Force createBossNpcForce(int npcId, int curHp) {
        StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
        AttrData attrData = new AttrData(npc.getAttr());
        Force force = new Force(attrData, npc.getArmType(), npc.getLine(), npcId);
        force.hp = curHp;
        force.totalLost = force.maxHp - force.hp; // ?????????
        int tmpTotalLost = force.totalLost;
        // ????????????????????????,???0??????
        int curLine = 0;// ???????????????
        int cnt = 4;// ???????????????
        while (tmpTotalLost >= force.lead && cnt-- > 0) {
            curLine++;
            tmpTotalLost -= force.lead;
        }
        force.count = force.lead - tmpTotalLost;// ?????????????????????
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
     * ??????key?????????????????????
     *
     * @param key  {@link GlobalConstant } ????????????key???
     * @param camp {@link Constant.Camp} ??????????????????
     * @return ??????null, ????????????key???????????????, {@link GlobalConstant} ??????????????????key
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
