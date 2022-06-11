package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.constant.MergeConstant;
import com.gryphpoem.game.zw.constant.MergeUtils;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.datasource.DynamicDataSource;
import com.gryphpoem.game.zw.domain.ElementServer;
import com.gryphpoem.game.zw.domain.MasterCacheData;
import com.gryphpoem.game.zw.domain.MasterServer;
import com.gryphpoem.game.zw.domain.MergePlayer;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.dao.impl.p.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.Role;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticPortrait;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.party.SupplyRecord;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.CastleSkinProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @ClassName PlayerMergeService.java
 * @Description 玩家合服的处理
 * @author QiuKun
 * @date 2018年9月17日
 */
@Service
public class PlayerMergeService {

    @Autowired
    private LordDao lordDao;
    @Autowired
    private AccountDao accountDao;
    @Autowired
    private DataNewDao dataDao;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private PayDao payDao;
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private BuildingDao buildingDao;
    @Autowired
    private MailDao mailDao;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private MergActivityService mergActivityService;
    @Autowired
    private CastleSkinProcessService castleSkinProcessService;
    @Autowired
    private DressUpDataManager dressUpDataManager;

    /**
     * 保存玩家数据
     * 
     * @param serverData
     */
    public void saveAllPlayer(MasterCacheData serverData) {
        int nThreads = Runtime.getRuntime().availableProcessors() * 2; // CPU核数
                                                                       // * 2
        MasterServer masterServer = serverData.getMasterServer();
        int masterServerId = masterServer.getServerId();
        ConcurrentLinkedQueue<Player> queue = new ConcurrentLinkedQueue<>();
        for (Player p : serverData.getAllPlayer().values()) {
            // saveOnePlayer(p, masterServerId);
            queue.offer(p);
        }
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        List<SavePlayerWork> workList = new ArrayList<>();
        for (int i = 1; i <= nThreads; i++) {
            workList.add(new SavePlayerWork(queue, masterServer, i));
        }
        try {
            executor.invokeAll(workList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        LogUtil.start("-----保存玩家数据完成 serverId:" + masterServerId);
    }

    /**
     * 保存一个玩家数据
     * 
     * @param player
     * @param masterServerId
     */
    public void saveOnePlayer(Player player, int masterServerId) {
        // DefaultTransactionDefinition def = new
        // DefaultTransactionDefinition();
        // def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // DataSourceTransactionManager txManager =
        // (DataSourceTransactionManager) DataResource.ac
        // .getBean("transactionManager");
        // TransactionStatus status = txManager.getTransaction(def);
        try {
            // account 数据保存
            accountDao.insertFullAccount(player.account);
            Role role = new Role(player);
            lordDao.save(role.getLord());
            buildingDao.save(role.getBuilding());
            resourceDao.save(role.getResource());
            mailDao.save(role.getMailData());
            commonDao.save(role.getCommon());
            dataDao.save(role.getData());
            // pay数据保存
            if (player instanceof MergePlayer) {
                MergePlayer p = (MergePlayer) player;
                if (!CheckNull.isEmpty(p.getPayList())) {
                    p.getPayList().forEach(pay -> payDao.createPay(pay));
                }
            }
        } catch (Exception e) {
            // txManager.rollback(status);
            LogUtil.error(e, "玩家数据保存失败 roleId:", player.roleId, ", serverId:", masterServerId);
            return;
        }
        // txManager.commit(status);
    }

    /**
     * 加载主服玩家的数据
     * 
     * @param serverData
     * @throws Exception
     */
    public void loadPlayer(MasterCacheData serverData) throws Exception {
        MasterServer masterServer = serverData.getMasterServer();
        List<ElementServer> composeServer = masterServer.getComposeServer();
        Map<Integer, List<ElementServer>> composeServerByServerId = composeServer.stream()
                .collect(Collectors.groupingBy(ElementServer::getServerId));
        // key:ServerId
        final Map<Integer, Map<Long, Player>> tmpPlayerMap = new ConcurrentHashMap<>();

        // 异步流的方式
        composeServerByServerId.values().parallelStream().forEach(esList -> {
            int serverId = esList.get(0).getServerId();
            Map<Long, Player> loadPlayerMap;
            try {
                loadPlayerMap = loadPlayer(esList);
                tmpPlayerMap.put(serverId, loadPlayerMap);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        // 同步的方式
        // for (Entry<Integer, List<ElementServer>> kv :
        // composeServerByServerId.entrySet()) {
        // // 此处进来的都是同一个服的数据
        // Integer serverId = kv.getKey();
        // List<ElementServer> esList = kv.getValue();
        // // 加载玩家数据
        // Map<Long, Player> loadPlayerMap = loadPlayer(esList);
        // tmpPlayerMap.put(serverId, loadPlayerMap);
        // }
        LogUtil.debug("加载数据驻防数据完成 masterServer:", masterServer);

        // 名字处理
        nameProcess(serverData, masterServer, tmpPlayerMap);
    }

    /**
     * 处理所有玩家数据
     * 
     * @param serverData
     */
    public void allPlayerDataproccess(MasterCacheData serverData) {
        List<Integer> emptyPosList = createEmptyPosList(serverData.getAllPlayer().size());
        // 活动配置数据加载
        int idx = 0;
        for (Player player : serverData.getAllPlayer().values()) {
            int pos = emptyPosList.get(idx++).intValue();
            cleanDataProcess(player, serverData, pos);
            friendDataProcess(player, serverData);
            activityDataProcess(player, serverData);// 活动处理
        }
    }

    private List<Integer> createEmptyPosList(int size) {
        List<StaticArea> sArea = StaticWorldDataMgr.getAreaMap().values().stream()
                .filter(a -> a.getOpenOrder() == WorldConstant.AREA_ORDER_1).collect(Collectors.toList());
        int cntByArea = (int) Math.ceil(size * 1.0 / sArea.size()); // 每个郡的人数
        List<Integer> emptyPosList = new ArrayList<>(size);
        for (StaticArea area : sArea) {
            List<Integer> emptyPos = MapHelper.getAreaAllPos(area.getArea()).stream()
                    .filter(pos -> !StaticWorldDataMgr.isCityPos(pos)).collect(Java8Utils.toShuffledList()).stream()
                    .limit(cntByArea).collect(Collectors.toList());
            emptyPosList.addAll(emptyPos);
        }
        return emptyPosList;
    }

    /**
     * 活动处理
     * 
     * @param player
     * @param serverData
     */
    private void activityDataProcess(Player player, MasterCacheData serverData) {
        for (Iterator<Entry<Integer, Activity>> it = player.activitys.entrySet().iterator(); it.hasNext();) {
            Entry<Integer, Activity> kv = it.next();
            int actType = kv.getKey();
            Activity activity = kv.getValue();
            if (MergeUtils.REATIN_ACT_TYPE.contains(actType)) {
                ActivityBase actBase = serverData.getActivityMap().get(actType);
                mergActivityService.multiAwardActProcess(actType, player, actBase);
                if (actBase != null) {
                    // activityId和时间修改成主服一样
                    mergActivityService.actIdAndBeginTimeUnity(player, activity, actBase);
                }
            } else {
                it.remove();
            }
        }
    }

    /**
     * 好友的处理
     * 
     * 
     * @param player
     */
    private void friendDataProcess(Player player, MasterCacheData serverData) {
        if (player.getBlacklist() != null) {
            player.getBlacklist().clear();// 黑名单清空
        }
        Map<Long, Player> allPlayer = serverData.getAllPlayer();

        for (Iterator<Map.Entry<Long, DbFriend>> it = player.friends.entrySet().iterator(); it.hasNext();) {
            Entry<Long, DbFriend> next = it.next();
            Long roleId = next.getKey();
            if (!allPlayer.containsKey(roleId)) {
                it.remove();
            }
        }
        for (Iterator<Entry<Long, DbMasterApprentice>> it = player.apprentices.entrySet().iterator(); it.hasNext();) {
            Entry<Long, DbMasterApprentice> next = it.next();
            Long roleId = next.getKey();
            if (!allPlayer.containsKey(roleId)) {
                it.remove();
            }
        }
        if (player.master != null && !allPlayer.containsKey(player.master.getLordId())) {
            player.master = null;// 师傅被合到其他服了
        }
    }

    /**
     * 那些数据需要清除
     * 
     * @param player
     */
    private void cleanDataProcess(Player player, MasterCacheData serverData, int pos) {
        Map<Integer, Integer> campMaxLv = serverData.getPartyMap().values().stream().collect(Collectors.toMap(Camp::getCamp, party -> party.getPartySuperSupply().getLv()));
        // 传入目标服务器id
        if (player instanceof MergePlayer) {
            MergePlayer p = (MergePlayer) player;
            p.setToServerId(serverData.getMasterServerId());
            // 处理玩家的超级补给
            SupplyRecord supplyRecord = p.getSupplyRecord();
            supplyRecord.mergeLogic(campMaxLv.getOrDefault(player.lord.getCamp(), 0));
        }
        // 部队清空
        player.armys.clear();
        // 将领状态全部设置空闲
        for (Hero hero : player.heros.values()) {
            hero.setState(ArmyConstant.ARMY_STATE_IDLE);
        }
        // 军团任务清空
        player.partyTask.clear();
        // 参战记录清空
        player.battleMap.clear();
        // 个人资源点已采集次数记录
        player.acquisiteReward.clear();
        player.acquisiteQue.clear();
        // 召唤信息清空
        player.summon = null;
        // 每日攻打流寇上线 清0
        player.setBanditCnt(0);
        // 最后世界发言
        player.lastChats.clear();
        // 离线升级的建筑 key 建筑id
        player.offLineBuilds.clear();
        // 职位清除
        player.lord.clearJob();
        // 决战状态处理
        DecisiveInfo decisiveInfo = player.getDecisiveInfo();
        if (decisiveInfo != null) {
            decisiveInfo.setDecisive(false);
        }
        // 点兵统领的处理
        if (player.cabinet != null) {
            player.cabinet.setLvFinish(true);
            player.cabinet.setLeadStep(4);
        }
        // buff效果清掉
        for (Iterator<Entry<Integer, Effect>> it = player.getEffect().entrySet().iterator(); it.hasNext();) {
            Entry<Integer, Effect> kv = it.next();
            Effect ef = kv.getValue();
            if (MergeUtils.REATIN_BUFF.contains(ef.getEffectType())) continue;
            it.remove();
        }

        // 霸主头像清除
        StaticLordDataMgr.getPortraitByUnlock(StaticPortrait.UNLOCK_TYPE_WINNER)
                .stream()
                .map(StaticPortrait::getId)
                .forEach(portrait -> dressUpDataManager.subDressUp(player, AwardType.PORTRAIT, portrait, 0, AwardFrom.COMMON));
        // 霸主皮肤清除
        dressUpDataManager.subDressUp(player, AwardType.CASTLE_SKIN, StaticCastleSkin.BERLIN_WINNER_SKIN_ID, 0, AwardFrom.COMMON);
        //清除所有限时称号
        Map<Integer, BaseDressUpEntity> dressUpByType = dressUpDataManager.getDressUpByType(player, AwardType.TITLE);
        if (CheckNull.nonEmpty(dressUpByType)) {
            dressUpByType.entrySet().forEach(entry -> {
                if (entry.getValue().getDuration() > 0) {
                    Long duration = StaticLordDataMgr.getTitleMapById(entry.getValue().getId()).getDuration();
                    dressUpDataManager.subDressUp(player, AwardType.TITLE, entry.getValue().getId(), duration, AwardFrom.COMMON);
                }
            });
        }
        // player的 mixtureData数据进行处理
        int maxDayOfMonthKey = PlayerConstant.RECENTLY_PAY + 32;
        for (Iterator<Entry<Integer, Integer>> it = player.getMixtureData().entrySet().iterator(); it.hasNext(); ) {
            Entry<Integer, Integer> next = it.next();
            int keyId = next.getKey();
            if (keyId > PlayerConstant.RECENTLY_PAY && keyId <= maxDayOfMonthKey) continue;
            if (MergeUtils.REATIN_MIXTURE_DATA_KEY.contains(keyId)) continue;
            it.remove();
        }
        // 坐标处理
        player.lord.setPos(-1);
        player.lord.setArea(-1);
        // 重新生成坐标
        if (player.lord.getLevel() >= 3) {
            player.lord.setPos(pos);
            player.lord.setArea(MapHelper.getAreaIdByPos(pos));
            serverData.getUsedPos().add(pos);
        }
    }

    /**
     * 名字处理
     * 
     * @param serverData
     * @param masterServer
     * @param tmpPlayerMap
     */
    private void nameProcess(MasterCacheData serverData, MasterServer masterServer,
            Map<Integer, Map<Long, Player>> tmpPlayerMap) {
        int masterServerId = masterServer.getServerId();
        Set<String> nameSet = null;
        Map<Long, Player> allPlayer = new HashMap<>();
        if (tmpPlayerMap.get(masterServerId) != null) {
            nameSet = tmpPlayerMap.get(masterServerId).values().stream().map(p -> p.lord.getNick())
                    .collect(Collectors.toCollection(HashSet::new));
            allPlayer.putAll(tmpPlayerMap.get(masterServerId));
            tmpPlayerMap.remove(masterServerId);
        } else {
            nameSet = new HashSet<>();
        }
        int now = TimeHelper.getCurrentSecond() + 10800;// 改名邮件发发送的时间
        // 改名卡
        List<Award> renameCard = Collections
                .singletonList(PbHelper.createAwardPb(AwardType.PROP, PropConstant.PROP_RENAME_CARD, 1));
        for (Entry<Integer, Map<Long, Player>> a : tmpPlayerMap.entrySet()) {
            Integer serverId = a.getKey();
            for (Entry<Long, Player> kv : a.getValue().entrySet()) {
                Player p = kv.getValue();
                String nick = p.lord.getNick();
                if (nameSet.contains(nick)) {
                    // 改名
                    String newNick = nick + "@S" + serverId.toString();
                    p.lord.setNick(newNick);
                    // 发改名卡
                    mailDataManager.sendAttachMail(p, renameCard, MergeUtils.RENAME_MAILID, AwardFrom.COMMON, now);
                    LogUtil.common("合服系统改名  masterServerId:", masterServerId, ", serverId:", serverId, ", roleId:",
                            p.roleId, ", srcNick:", nick, ", newNick:", newNick);
                }
                nameSet.add(nick);
                allPlayer.put(p.roleId, p);
            }
        }
        // 存储到serverDataz中
        serverData.setAllPlayer(allPlayer);
        LogUtil.start("主服的重名处理完毕  masterServerId:" + masterServerId + ", 人数:" + allPlayer.size());

    }

    private Map<Long, Player> loadPlayer(List<ElementServer> esList) throws InterruptedException, ExecutionException {
        final int serverId = esList.get(0).getServerId();
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        List<Lord> lordList = loadLord(esList);
        int now = TimeHelper.getCurrentSecond();

        final Map<Long, Player> tmpPlayerMap = new ConcurrentHashMap<>();
        // 创建player数据
        for (Lord lord : lordList) {
            if (!isLordConditions(lord)) {
                continue;
            }
            Player player = new MergePlayer(lord, now, serverId);
            tmpPlayerMap.put(lord.getLordId(), player);
        }

        // 同步的方式
        // loadAccount(tmpPlayerMap, serverId);
        // loadData(tmpPlayerMap, serverId);
        // loadBuilding(tmpPlayerMap, serverId);
        // loadResource(tmpPlayerMap, serverId);
        // loadCommon(tmpPlayerMap, serverId);
        // loadPay(tmpPlayerMap, serverId);

        // 固定线程城池的方式
        ExecutorService execService = Executors.newFixedThreadPool(6);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        tasks.add(() -> loadAccount(tmpPlayerMap, serverId));
        tasks.add(() -> loadData(tmpPlayerMap, serverId));
        tasks.add(() -> loadBuilding(tmpPlayerMap, serverId));
        tasks.add(() -> loadResource(tmpPlayerMap, serverId));
        tasks.add(() -> loadCommon(tmpPlayerMap, serverId));
        tasks.add(() -> loadPay(tmpPlayerMap, serverId));
        execService.invokeAll(tasks);
        execService.shutdown();
        LogUtil.common("异步的方式加载数据完成------ serverId", serverId);

        // CompletableFuture的方式
        // CompletableFuture<Boolean> loadAccount = CompletableFuture
        // .supplyAsync(() -> loadAccount(tmpPlayerMap, serverId));
        // CompletableFuture<Boolean> loadData =
        // CompletableFuture.supplyAsync(() -> loadData(tmpPlayerMap,
        // serverId));
        // CompletableFuture<Boolean> loadBuilding = CompletableFuture
        // .supplyAsync(() -> loadBuilding(tmpPlayerMap, serverId));
        // CompletableFuture<Boolean> loadResource = CompletableFuture
        // .supplyAsync(() -> loadResource(tmpPlayerMap, serverId));
        // CompletableFuture<Boolean> loadCommon =
        // CompletableFuture.supplyAsync(() -> loadCommon(tmpPlayerMap,
        // serverId));
        // CompletableFuture<Boolean> loadPay = CompletableFuture.supplyAsync(()
        // -> loadPay(tmpPlayerMap, serverId));
        //
        // CompletableFuture.allOf(loadAccount, loadData, loadBuilding,
        // loadResource, loadCommon, loadPay)
        // .whenComplete((d, e) -> {
        // if (e == null) {
        // LogUtil.common("异步加载数据执行完毕 serverId:", serverId);
        // } else {
        // LogUtil.error("异步加载数据执行失败 serverId:", serverId);
        // }
        // }).get();
        //
        // LogUtil.common("回到主------ serverId", serverId);

        return tmpPlayerMap;
    }

    // public static CompletableFuture<Boolean>
    // createCompletableFuture(Supplier<Boolean> supplier) {
    // return CompletableFuture.supplyAsync(supplier);
    // }

    /**
     * 充值数据
     * 
     * @param tmpPlayerMap
     */
    private boolean loadPay(Map<Long, Player> tmpPlayerMap, int serverId) {
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        // 充值数据
        tmpPlayerMap.values().stream().forEach(p -> {
            List<Pay> payList = payDao.selectRolePay(p.roleId);
            if (!CheckNull.isEmpty(payList) && p instanceof MergePlayer) {
                MergePlayer p1 = (MergePlayer) p;
                p1.setPayList(payList);
                // LogUtil.common("加载pay数据完成 roleId:", p.roleId);
            }
        });
        return true;
    }

    private boolean loadCommon(Map<Long, Player> tmpPlayerMap, int serverId) {
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        List<Common> list = commonDao.load();
        for (Common common : list) {
            Player player = tmpPlayerMap.get(common.getLordId());
            if (player != null) {
                player.common = common;
            }
        }
        return true;
    }

    private boolean loadResource(Map<Long, Player> tmpPlayerMap, int serverId) {
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        List<Resource> list = resourceDao.load();
        for (Resource resource : list) {
            Player player = tmpPlayerMap.get(resource.getLordId());
            if (player != null) {
                player.resource = resource;
            }
        }
        return true;
    }

    private boolean loadBuilding(Map<Long, Player> tmpPlayerMap, int serverId) {
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        List<Building> list = buildingDao.load();
        for (Building building : list) {
            Player player = tmpPlayerMap.get(building.getLordId());
            if (player != null) {
                player.building = building;
            }
        }
        return true;
    }

    private boolean loadData(Map<Long, Player> tmpPlayerMap, int serverId) {
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        List<DataNew> list = dataDao.loadData();
        for (DataNew data : list) {
            Player player = tmpPlayerMap.get(data.getLordId());
            if (player != null) {
                try {
                    player.dserNewData(data);
                } catch (Exception e) {
                    LogUtil.error(e, "roleId:", data.getLordId());
                }
            }
        }
        return true;
    }

    private boolean loadAccount(Map<Long, Player> tmpPlayerMap, int serverId) {
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(serverId));
        List<Account> list = accountDao.load();
        for (Account account : list) {
            Player player = tmpPlayerMap.get(account.getLordId());
            if (player != null) {
                player.account = account;
            }
        }
        return true;
    }

    /**
     * 一些lord加载后的过滤条件
     * 
     * @param lord
     * @return true表示满足条件
     */
    private boolean isLordConditions(Lord lord) {
        if (CheckNull.isNullTrim(lord.getNick())) {
            return false;
        }
        return true;
    }

    private List<Lord> loadLord(List<ElementServer> esList) {
        List<Lord> lordList = new ArrayList<>();
        for (ElementServer es : esList) {
            lordList.addAll(lordDao.mergeLoad(es.getCamp()));
        }
        return lordList;
        // return
        // lordDao.mergeLoadByCamps(esList.stream().map(ElementServer::getCamp).collect(Collectors.toList()));
    }

}
