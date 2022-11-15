package com.gryphpoem.game.zw.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.HttpUtils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.RoleOpt;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.SyncFightChgRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncRoleInfoRs;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.Camp;
import com.gryphpoem.game.zw.resource.dao.impl.p.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.Role;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.domain.s.StaticIniLord;
import com.gryphpoem.game.zw.resource.domain.s.StaticRecommend;
import com.gryphpoem.game.zw.resource.pojo.BuildingState;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PlayerDataManager implements PlayerDM {

    @Autowired
    private AccountDao accountDao;
    @Autowired
    private ResourceDao resourceDao;
    @Autowired
    private LordDao lordDao;
    @Autowired
    private DataNewDao dataDao;
    @Autowired
    private MailDao mailDao;
    @Autowired
    private BuildingDao buildingDao;
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private PlayerHeroDao playerHeroDao;
    @Autowired
    private PayDao payDao;
    @Autowired
    private SmallIdManager smallIdManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private CampDataManager campDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private RankDataManager rankDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private DubboRpcService dubboRpcService;

    public void init() {
        loadAllPlayer();
    }

    // MAP<serverid, MAP<accountKey, Player>>
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Long, Account>> accountCache = new ConcurrentHashMap<>();

    // MAP<roleId, Player>
    private ConcurrentHashMap<Long, Player> playerCache = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Player> onlinePlayer = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Player> allPlayer = new ConcurrentHashMap<>();

    // 新建玩家
    private ConcurrentHashMap<Long, Player> newPlayerCache = new ConcurrentHashMap<>();

    private Set<String> usedNames = Collections.synchronizedSet(new HashSet<String>());

    private LinkedList<Lord> lords = new LinkedList<>();

    // 按阵营分
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Long, Player>> campPlayer = new ConcurrentHashMap<>();

    // 按行政区域分
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Long, Player>> areaPlayer = new ConcurrentHashMap<>();

    /**
     * 记录个阵营玩家人数，阵营id为数组索引，0位为补位
     */
    public int[] campRoleNumArr = new int[]{0, 0, 0, 0};

    // 记录每个渠道最大lordId值 key:PlatNo_serverId, value: 该渠道该服的最大的id数,用于控制roleId
    private Map<String, AtomicInteger> maxLordIdPlatNo = new ConcurrentHashMap<>();

    private int roleGrade45;// 全服到达45级的角色数量

    /**
     * 推送玩家扩展数据
     *
     * @param player 玩家对象
     */
    public void syncMixtureData(Player player) {
        if (player != null && player.isLogin && player.ctx != null) {
            GamePb4.GetMixtureDataRs.Builder builder = GamePb4.GetMixtureDataRs.newBuilder();
            for (Map.Entry<Integer, Integer> kv : player.getMixtureData().entrySet()) {
                if (PlayerConstant.isInShowClientList(kv.getKey())) {// 过滤只需要让显示的东西
                    builder.addMixtureData(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
                }
            }
            Base.Builder base = PbHelper.createSynBase(GamePb4.GetMixtureDataRs.EXT_FIELD_NUMBER, GamePb4.GetMixtureDataRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, base.build(), player.roleId));
        }
    }

    public ConcurrentHashMap<Long, Account> getAccountMap(int serverId) {
        ConcurrentHashMap<Long, Account> map = accountCache.get(serverId);
        if (null == map) {
            map = new ConcurrentHashMap<Long, Account>();
            accountCache.put(serverId, map);
        }
        return map;
    }

    public void loadAllPlayer() {
        for (StaticArea area : StaticWorldDataMgr.getAreaMap().values()) {
            if (area.isOpen()) {
                areaPlayer.put(area.getArea(), new ConcurrentHashMap<>());
            }
        }

        new Load().load();
        LogUtil.start("load all players data!!");

        // worldDataManager.processCityBandit();
        // LogUtil.start("create & load all players bandit data!!");
    }

    public Map<Long, Player> getPlayers() {
        return playerCache;
    }

    /**
     * @param account
     * @return
     */
    public Player createPlayer(Account account) {
        return initPlayerData(account);
    }

    /**
     * 剁小号后创建player
     *
     * @param account
     * @return
     */
    public Player createPlayerAfterCutSmallId(Account account) {
        return initPlayerDataAfterCutSmallId(account);
    }

    public Account getAccount(int serverId, long accountKey) {
        Account account = getAccountMap(serverId).get(accountKey);
        return account;
    }

    public Player getPlayer(Long roleId) {
        return playerCache.get(roleId);
    }

    public Player getPlayerIfNullRandomAcc(Long roleId) {
        Player player = playerCache.get(roleId);
        if (CheckNull.isNull(player)) {
            for (Player temp : playerCache.values()) {
                if (!CheckNull.isNull(temp)) {
                    player = temp;
                    break;
                }
            }
        }
        return player;
    }

    public Player getPlayer(String nick) {
        return allPlayer.get(nick);
    }

    public Player getNewPlayer(Long roleId) {
        return newPlayerCache.get(roleId);
    }

    public Player removeNewPlayer(Long roleId) {
        return newPlayerCache.remove(roleId);
    }

    public void addPlayer(Player player) {
        playerCache.put(player.roleId, player);
        allPlayer.put(player.lord.getNick(), player);
        putCampPlayer(player);
        lords.add(player.lord);
    }

    public void rmPlayer(Player player) {
        if (player == null) {
            return;
        }
        playerCache.remove(player.roleId);
        allPlayer.remove(player.lord.getNick());

        ConcurrentHashMap<Long, Player> map = campPlayer.get(player.lord.getCamp());
        if (null == map) {
            map = new ConcurrentHashMap<>();
            campPlayer.put(player.lord.getCamp(), map);
        }
        map.remove(player.roleId);

        map = areaPlayer.get(player.lord.getArea());
        if (null == map) {
            map = new ConcurrentHashMap<>();
            areaPlayer.put(player.lord.getArea(), map);
        }
        map.remove(player.roleId);
        lords.remove(player.lord);

        removeOnline(player);
    }

    public void addOnline(Player player) {
        onlinePlayer.put(player.lord.getNick(), player);
    }

    public void removeOnline(Player player) {
        onlinePlayer.remove(player.lord.getNick());
    }

    public Player getOnlinePlayer(String nick) {
        return onlinePlayer.get(nick);
    }

    public Map<String, Player> getAllOnlinePlayer() {
        return onlinePlayer;
    }

    public LinkedList<Lord> getAllLord() {
        return lords;
    }

    public void putCampPlayer(Player player) {
        ConcurrentHashMap<Long, Player> map = campPlayer.get(player.lord.getCamp());
        if (null == map) {
            map = new ConcurrentHashMap<>();
            campPlayer.put(player.lord.getCamp(), map);
        }
        map.put(player.roleId, player);
    }

    public ConcurrentHashMap<Long, Player> getPlayerByCamp(int camp) {
        return campPlayer.get(camp);
    }

    public void putAreaPlayer(Player player) {
        if (player.lord.getArea() == -1) {
            return;
        }
        ConcurrentHashMap<Long, Player> map = areaPlayer.get(player.lord.getArea());
        if (null == map) {
            map = new ConcurrentHashMap<>();
            areaPlayer.put(player.lord.getArea(), map);
        }
        map.put(player.roleId, player);
    }

    public void removeAreaPlayer(int pos, Player player) {
        int area = MapHelper.getAreaIdByPos(pos);
        if (player.lord.getArea() != area) {
            ConcurrentHashMap<Long, Player> map = areaPlayer.get(area);
            if (null != map) {
                map.remove(player.roleId);
            }
        }
    }

    public ConcurrentHashMap<Long, Player> getPlayerByArea(int areaId) {
        return areaPlayer.get(areaId);
    }

    public ConcurrentHashMap<Long, Player> getPlayerByAreaList(List<Integer> areaIdList) {
        ConcurrentHashMap<Long, Player> players = new ConcurrentHashMap<>();
        if (!CheckNull.isEmpty(areaIdList)) {
            ConcurrentHashMap<Long, Player> map;
            for (Integer areaId : areaIdList) {
                map = getPlayerByArea(areaId);
                if (!CheckNull.isEmpty(map)) {
                    players.putAll(map);
                }
            }
        }

        return players;
    }

    private Account createAccount(Account account, Player player) {
        account.setLordId(player.roleId);
        accountDao.insertAccount(account);
        player.account = account;

        getAccountMap(account.getServerId()).put(account.getAccountKey(), account);
        return account;
    }

    private Account createAccountAfterCutSmallId(Account account, Player player) {
        account.setLordId(player.roleId);
        player.account = account;
        account.setCreated(0);
        accountDao.updateLordId(account);
        getAccountMap(account.getServerId()).put(account.getAccountKey(), account);
        return account;
    }

    public void recordLogin(Account account) {
        accountDao.recordLoginTime(account);
    }

    /**
     * Method: initPlayerData
     *
     * @Description: 初始化玩家数据 @return @return Player @throws
     */
    private Player initPlayerData(Account account) {
        StaticIniLord staticIniLord = StaticIniDataMgr.getLordIniData();

        Lord lord = createLord(account, staticIniLord);
        if (Objects.isNull(lord)) {
            return null;
        }

        Player player = new Player(lord, TimeHelper.getCurrentSecond());
        // 初始化建筑建筑信息
        Map<Integer, BuildingState> buildingData = player.getBuildingData();
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        List<Integer> foundationData = player.getFoundationData();
        Map<Integer, Integer> buildingInfo = staticIniLord.getBuildingInfo();
        if (CheckNull.nonEmpty(buildingInfo)) {
            buildingInfo.forEach((k, v) -> {
                BuildingState buildingState = new BuildingState(k, v);
                StaticHomeCityFoundation staticHomeCityFoundation = StaticBuildCityDataMgr.getStaticHomeCityFoundationById(v);
                List<Integer> cellList = staticHomeCityFoundation.getCellList();
                for (int i = 0; i < cellList.size(); i++) {
                    List<Integer> cellState = new ArrayList<>(2);
                    cellState.add(1);// 已开垦
                    cellState.add(0);// 没有土匪
                    mapCellData.put(cellList.get(i), cellState);
                }
                if (!foundationData.contains(v)) {
                    foundationData.add(v);
                }
                buildingData.put(k, buildingState);
            });
        } else {
            BuildingState buildingState = new BuildingState(1, 1);
            buildingData.put(1, buildingState);
            StaticHomeCityFoundation staticHomeCityFoundation = StaticBuildCityDataMgr.getStaticHomeCityFoundationById(1);
            List<Integer> cellList = staticHomeCityFoundation.getCellList();
            for (int i = 0; i < cellList.size(); i++) {
                List<Integer> cellState = new ArrayList<>(2);
                cellState.add(1);// 已开垦
                cellState.add(0);// 没有土匪
                mapCellData.put(cellList.get(i), cellState);
            }
            if (!foundationData.contains(1)) {
                foundationData.add(1);
            }
        }

        // 初始化农民配置
        List<Integer> residentCnt = staticIniLord.getResidentCnt();
        List<Integer> residentData = player.getResidentData();
        residentData.clear();
        if (CheckNull.nonEmpty(residentCnt)) {
            residentData.add(residentCnt.get(1)); // 总数
            residentData.add(residentCnt.get(1)); // 空闲数
            residentData.add(residentCnt.get(0)); // 上限
        } else {
            residentData.add(4); // 总数
            residentData.add(4); // 空闲数
            residentData.add(4); // 上限
        }

        // 初始化侦察兵配置
        Map<Integer, Integer> scoutData = player.getScoutData();
        if (staticIniLord.getScoutCnt() > 0) {
            for (int i = 0; i < staticIniLord.getScoutCnt(); i++) {
                scoutData.put(i, 0);
            }
        } else {
            scoutData.put(1, 0);
        }

        createAccount(account, player);
        newPlayerCache.put(player.roleId, player);
        return player;
    }

    /**
     * 剁小号后初始玩家数据
     *
     * @param account
     * @return
     */
    private Player initPlayerDataAfterCutSmallId(Account account) {
        StaticIniLord staticIniLord = StaticIniDataMgr.getLordIniData();

        Lord lord = createLord(account, staticIniLord);
        if (Objects.isNull(lord)) {
            return null;
        }

        Player player = new Player(lord, TimeHelper.getCurrentSecond());
        createAccountAfterCutSmallId(account, player);
        // creatCommonAfterCutSmallId(player);
        newPlayerCache.put(player.roleId, player);
        return player;
    }

    public boolean createFullPlayer(Player player) {
        StaticIniLord staticIniLord = StaticIniDataMgr.getLordIniData();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        DataSourceTransactionManager txManager = (DataSourceTransactionManager) DataResource.ac
                .getBean("transactionManager");
        TransactionStatus status = txManager.getTransaction(def);
        try {
            createBuilding(player);
            createResource(player, staticIniLord);
            createData(player);
            creatCommon(player);
            createMailData(player);
            createPlayerHero(player);
            lordDao.updateLord(player.lord);
            accountDao.updateCreateRole(player.account);
        } catch (Exception ex) {
            txManager.rollback(status);
            LogUtil.error("创建玩家数据失败，回退", ex);
            return false;
        }

        txManager.commit(status);
        CalculateUtil.reCalcFight(player);
        // rankDataManager.setFight(player.lord);
        rankDataManager.setRanks(player.lord);
        rankDataManager.setExploit(player.lord);
        return true;
    }

    private void createResource(Player player, StaticIniLord staticIniLord) {
        buildingDataManager.createResource(player, staticIniLord);
    }

    private void createBuilding(Player player) {
        buildingDataManager.createBuilding(player);
    }

    private Common creatCommon(Player player) {
        Common common = new Common();
        common.setLordId(player.roleId);
        common.setBagCnt(Constant.BAG_INIT_CNT);
        common.setBaptizeCnt(Constant.EQUIP_MAX_BAPTIZECNT);
//        common.setWashCount(WorldConstant.HERO_WASH_FREE_MAX);
        common.setTreasureWareCnt(Constant.TREASURE_WARE_BAG_INIT);
        commonDao.insertCommon(common);
        player.common = common;
        return common;
    }

    private PlayerHero createPlayerHero(Player player) {
        PlayerHero playerHero = new PlayerHero(player.roleId);
        player.playerHero = playerHero;
        playerHeroDao.insertPlayerHero(playerHero.createPb(true));
        return playerHero;
    }

    private Common creatCommonAfterCutSmallId(Player player) {
        Common common = new Common();
        common.setLordId(player.roleId);
        commonDao.updateCommon(common);
        player.common = common;
        return common;
    }

    private Lord createLord(Account account, StaticIniLord staticIniLord) {
        Lord lord = new Lord();
        int now = TimeHelper.getCurrentSecond();
        if (staticIniLord != null) {
            lord.setLevel(staticIniLord.getLevel());
            lord.setVip(staticIniLord.getVip());
            lord.setGold(staticIniLord.getGoldGive());
            lord.setGoldGive(staticIniLord.getGoldGive());
            lord.setNewState(staticIniLord.getNewState());
            lord.setPower(staticIniLord.getPower());
        } else {
            lord.setLevel(1);
            lord.setVip(0);
            lord.setGold(1000);
            lord.setGoldGive(1000);
            lord.setNewState(0);
        }

        if (account.getRecommendCamp() != 0) {
            lord.setCamp(account.getRecommendCamp()); // 推荐阵营
        }
        lord.setPos(-1);
        lord.setArea(-1);
        lord.setPowerTime(now);
        lord.setRefreshTime(now);
        lord.setOffTime(now);
        lord.setOnTime(now);
        // int count = lordDao.selectLordCount();
        // String maxRoleIdKey = AccountHelper.getCurServerMaxLordKey(account.getPlatNo(), account.getServerId());
        // if (count == 0) {// 创建服务器第一位玩家数据，保证角色id全区全服唯一
        //     long firstRoleId = AccountHelper.getFirstRoleId(account.getPlatNo(), account.getServerId(), lord.getCamp());
        //     if (firstRoleId > 0) {
        //         firstRoleId = createFirstRoleId(account, lord, maxRoleIdKey, firstRoleId);
        //         lord.setLordId(firstRoleId);
        //     }
        // } else {
        //     AtomicInteger maxRoleId = maxLordIdPlatNo.get(maxRoleIdKey);
        //     if (maxRoleId == null) {
        //         long firstRoleId = AccountHelper.getFirstRoleId(account.getPlatNo(), account.getServerId(),
        //                 lord.getCamp());
        //         firstRoleId = createFirstRoleId(account, lord, maxRoleIdKey, firstRoleId);
        //         lord.setLordId(firstRoleId);
        //     } else {
        //         long maxId = maxRoleId.incrementAndGet();
        //         lord.setLordId(AccountHelper.createRoleIdByPlatNo(account.getPlatNo(), maxId, account.getServerId(),
        //                 lord.getCamp()));
        //     }
        // }
        Long lordId = createLordId(account, lord);
        if (Objects.isNull(lordId)) {
            // TODO: 2020/8/20 没有获取到生成的lordId
            return null;
        }
        LogUtil.debug("获取RoleId, platNo:", account.getPlatNo(), ", serverId:", account.getServerId(), ", roleId:", lordId);
        lord.setLordId(lordId);
        lordDao.insertLord(lord);
        return lord;
    }

    /**
     * 创建全服唯一的id
     * @param account 账号信息
     * @param lord 角色信息
     * @return long 由创建唯一id服务创建, 这里还有有重复的风险。例如单服单渠道的导入到上限了999999； null 没有获取到lordId 
     */
    private Long createLordId(Account account, Lord lord) {
        Long lordId = null;
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("request_type", "sync_http");
        headerMap.put("command", "createRoleId");
        HashMap<String, String> postBody = new HashMap<>();
        postBody.put("platNo", String.valueOf(account.getPlatNo()));
        postBody.put("serverId", String.valueOf(account.getServerId()));
        postBody.put("recommendCamp", String.valueOf(lord.getCamp()));
        String rs = HttpUtils.sendPost(serverSetting.getAccountServerUrl(), postBody, CharEncoding.UTF_8, headerMap, 1000);
        if (StringUtils.isNotBlank(rs)) {
            lordId = Long.valueOf(rs);
        }
        return lordId;
    }

    private long createFirstRoleId(Account account, Lord lord, String maxRoleIdKey, long firstRoleId) {
        int firstRoleIdByPlatNo = AccountHelper.getIdByLordId(firstRoleId);
        AtomicInteger oldMaxRoleId = maxLordIdPlatNo.putIfAbsent(maxRoleIdKey, new AtomicInteger(firstRoleIdByPlatNo));
        if (oldMaxRoleId != null) {
            firstRoleId = AccountHelper.createRoleIdByPlatNo(account.getPlatNo(), oldMaxRoleId.incrementAndGet(),
                    account.getServerId(), lord.getCamp());
        }
        return firstRoleId;
    }

    private void createData(Player player) {
        player.setMaxKey(0);

        dataDao.insertData(player.serNewData());
    }

    private void createMailData(Player player) {
        mailDao.insertData(player.serMailData());
    }

    /**
     * Method: takeNick
     *
     * @Description: 占用一个名字 @param nick @return @return boolean @throws
     */
    public boolean takeNick(String nick) {
        synchronized (usedNames) {
            if (usedNames.contains(nick)) {
                return false;
            }

            usedNames.add(nick);
            return true;
        }
    }

    public void rename(Player player, String newNick) {
        String nick = player.lord.getNick();
        usedNames.remove(nick);
        onlinePlayer.remove(nick);
        onlinePlayer.put(newNick, player);
        allPlayer.remove(nick);
        allPlayer.put(newNick, player);
        player.lord.setNick(newNick);
        player.immediateSave = true;
        dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
    }

    public boolean canUseName(String nick) {
        return !usedNames.contains(nick);
    }

    private class Load {

        /**
         * Method: load
         *
         * @Description: 从数据库加载所有玩家数据 @return void @throws
         */
        public void load() {
            LogUtil.start("begin load all players data, waiting!!!");
            List<Lord> list = lordDao.load();
            smallIdManager.processSmallIdLogic(list);
            Player player = null;
            int now = TimeHelper.getCurrentSecond();
            int camp = 0;
            for (Lord lord : list) {
                // 获取计算渠道和serverId最大lordId
                loadMaxLordId(lord.getLordId());
                if (lord.getNick() != null) {
                    if (smallIdManager.isSmallId(lord.getLordId())) {
                        continue;
                    }
                    player = new Player(lord, now);
                    addPlayer(player);
                    if (lord.getPos() != -1 && lord.getArea() <= WorldConstant.AREA_MAX_ID) {
                        worldDataManager.putPlayer(player);
                    }
                    usedNames.add(lord.getNick());

                    camp = lord.getCamp();
                    if (camp < Constant.Camp.EMPIRE || camp > Constant.Camp.UNION) {
                        continue;// 玩家还没有注册角色时，会出现这种情况，跳过
                    } else {
                        campRoleNumArr[camp] += 1;
                    }
                    // VIP等级统计
                    vipDataManager.incrementVipCnt(lord.getVip());
                    // 全服满45级的角色数量统计
                    if (lord.getLevel() >= Constant.ROLE_GRADE_45) {
                        roleGrade45++;
                    }
                } else {
                    newPlayerCache.put(lord.getLordId(), new Player(lord, now));
                }

            }
            LogUtil.start("-----------加载每个渠道最大的roleId-----------");
            LogUtil.start("maxLordIdPlatNo:" + maxLordIdPlatNo);
            loadAccount();
            loadData();
            loadBuilding();
            loadResource();
            loadCommon();
            loadMail();
            loadPaySum();
            loadPlayerHero();
            // 刷新勋章商品
            refreshMedalGoods();
            LogUtil.start("done load all players data!!!");
        }

        /**
         * 加载充值玩家的充值总金额
         */
        private void loadPaySum() {
            List<PaySum> list = payDao.selectPaySum();
            for (PaySum ps : list) {
                Player player = playerCache.get(ps.getRoleId());
                if (player != null) {
                    player.setPaySumAmoumt(ps.getSumAmoumt());
                }
            }
        }

        private void loadMaxLordId(long lordId) {
            int platNo = AccountHelper.getPlatNoByLordId(lordId);
            int serverId = AccountHelper.getServerIdByLordId(lordId);
            int id = AccountHelper.getIdByLordId(lordId);
            String key = AccountHelper.getMaxLordKey(platNo, serverId);
            AtomicInteger maxId = maxLordIdPlatNo.get(key);
            if (maxId == null) {
                maxLordIdPlatNo.put(key, new AtomicInteger(id));
            } else {
                int id2 = maxId.get();
                maxId.set(Math.max(id, id2));
            }
        }

        private void loadCommon() {
            List<Common> list = commonDao.load();
            Player player = null;
            for (Common common : list) {
                if (!smallIdManager.isSmallId(common.getLordId())) {
                    player = playerCache.get(common.getLordId());
                    if (player != null) {
                        player.common = common;
                    }
                }
            }
        }

        private void loadPlayerHero() {
            List<DbPlayerHero> list = playerHeroDao.load();
            Player player = null;
            for (DbPlayerHero dbPlayerHero : list) {
                if (!smallIdManager.isSmallId(dbPlayerHero.getLordId())) {
                    player = playerCache.get(dbPlayerHero.getLordId());
                    if (player != null) {
                        try {
                            player.playerHero = new PlayerHero(dbPlayerHero);
                        } catch (InvalidProtocolBufferException e) {
                            LogUtil.error(e, "roleId:", dbPlayerHero.getLordId());
                        }
                    }
                }
            }
        }

        /**
         * Method: loadAccount
         *
         * @Description: 加载账号数据 @return void @throws
         */
        private void loadAccount() {
            List<Account> list = accountDao.load();
            Player player = null;
            for (Account account : list) {
                // 若是小号，直接添加到accountCache中
                if (!smallIdManager.isSmallId(account.getLordId())) {
                    if (account.getCreated() == 1) {
                        player = playerCache.get(account.getLordId());
                        if (player != null) {
                            player.account = account;
                        }

                    } else {
                        player = newPlayerCache.get(account.getLordId());
                        if (player != null) {
                            player.account = account;
                        }
                    }
                }

                getAccountMap(account.getServerId()).put(account.getAccountKey(), account);
            }

            // 获取渠道最大lordId
            // LogUtil.debug("==========加载每个渠道最大的roleId========");
            // List<MaxLordId> maxLordIdList = accountDao.getMaxLordId();
            // maxLordIdList.forEach(mLordId -> {
            // long roleIdByPlatNo = AccountHelper.getPlatLordByLordId(mLordId.getMaxLordId());
            // AtomicLong max = new AtomicLong(roleIdByPlatNo);
            // maxLordIdPlatNo.put(mLordId.getPlatNo(), max);
            // LogUtil.debug(mLordId.toString());
            // });
        }

        private void loadData() {
            List<DataNew> list = dataDao.loadData();
            Player player = null;
            for (DataNew data : list) {
                // 小号不load
                if (!smallIdManager.isSmallId(data.getLordId())) {
                    player = playerCache.get(data.getLordId());
                    if (player != null) {
                        try {
                            player.dserNewData(data);

                            if (player.lord.getPos() != -1) {
                                rankDataManager.load(player);
                            }

                        } catch (InvalidProtocolBufferException e) {
                            LogUtil.error("load player data exception, lordId:" + data.getLordId(), e);
                        }
                    }
                }
                rankDataManager.sort();
            }
        }

        /**
         * @return void
         * @Title: refreshMedalGoods
         * @Description: 初始化所有玩家的勋章商品 不管有没有解锁都初始化
         */
        private void refreshMedalGoods() {
            getPlayers().values().stream().forEach(p -> {
                // 勋章商品刷新
                medalDataManager.initMedalGoods(p, MedalConst.MEDAL_GOODS_SERVER_START_TYPE);
            });
        }

        /**
         * 启动加载邮件
         */
        private void loadMail() {
            Map<Long, MailData> map = mailDao.loadData();
            Player player = null;
            if (map == null || map.isEmpty()) {
                return;
            }
            LogUtil.debug("---------loadMail加载邮件新表数据=" + map.size());
            for (MailData data : map.values()) {
                // 小号不load
                if (!smallIdManager.isSmallId(data.getLordId())) {
                    player = playerCache.get(data.getLordId());
                    if (player != null) {
                        try {
                            PlayerSerHelper.dserMailData(player, data);
                        } catch (InvalidProtocolBufferException e) {
                            LogUtil.error("load player data exception, lordId:" + data.getLordId(), e);
                        }
                    }
                }
            }
        }
    }

    public void updateRole(Role role) {
        lordDao.save(role.getLord());
        buildingDao.save(role.getBuilding());
        resourceDao.save(role.getResource());

        if (role.getSaveMode() == Role.SAVE_MODE_TIMER) {// 优化保存
            mailDao.updateOptimizeData(role.getMailData());
            dataDao.updateOptimize(role.getData());
        } else {
            mailDao.save(role.getMailData());
            dataDao.save(role.getData());
        }

        if (role.getCommon() != null) {
            commonDao.save(role.getCommon());
        } else {
            LogUtil.error("updateRole, 更新Common" + role.getCommon() + ",roleId=" + role.getRoleId());
        }
        if (role.getDbPlayerHero() != null) {
            playerHeroDao.save(role.getDbPlayerHero());
        } else {
            LogUtil.error("updateRole, 更新Common" + role.getCommon() + ",roleId=" + role.getRoleId());
        }
        // 更新军团成员相关信息
        campDataManager.updatePartyMember(role.getRoleId());
    }

    /**
     * Method: loadBuilding
     *
     * @Description: 加载建筑数据 @return void @throws
     */
    private void loadBuilding() {
        List<Building> list = buildingDao.load();
        Player player = null;
        for (Building building : list) {
            if (!smallIdManager.isSmallId(building.getLordId())) {
                player = playerCache.get(building.getLordId());
                if (player != null) {
                    player.building = building;
                }
            }
        }
    }

    /**
     * Method: loadResource
     *
     * @Description: 加载资源数据 @return void @throws
     */
    private void loadResource() {
        List<Resource> list = resourceDao.load();
        Player player = null;
        for (Resource resource : list) {
            if (!smallIdManager.isSmallId(resource.getLordId())) {
                player = playerCache.get(resource.getLordId());
                if (player != null) {
                    player.resource = resource;
                }
            }
        }
    }

    public void logOnlinePlayer() {

    }

    public List<String> generateNames() {
        List<String> names = new ArrayList<String>();
        while (names.size() < 3) {
            String name = StaticIniDataMgr.getManNick();
            if (canUseName(name)) {
                names.add(name);
            }
        }

        while (names.size() < 6) {
            String name = StaticIniDataMgr.getWomanNick();
            if (canUseName(name)) {
                names.add(name);
            }
        }

        return names;
    }

    /**
     * 系统随机生成一个可以使用的男性名称
     *
     * @return
     */
    public String getFreeManName() {
        String name;
        do {
            name = StaticIniDataMgr.getManNick();
        } while (!canUseName(name));
        return name;
    }

    /**
     * 获取当前服务器人数少的阵营
     *
     * @return
     */
    public int getSmallCamp() {
        int camp = Constant.Camp.EMPIRE;
        int roleNum = Integer.MAX_VALUE;
        for (int i = Constant.Camp.EMPIRE; i <= Constant.Camp.UNION; i++) {
            if (campRoleNumArr[i] < roleNum) {
                roleNum = campRoleNumArr[i];
                camp = i;
            }
            LogUtil.debug("当前 camp:", i, ", 人数为:", campRoleNumArr[i]);
        }
        return camp;
    }

    /**
     * 获取多少名以内战斗力和最小的阵营
     *
     * @param num
     * @return
     */
    public int getFightSamllCamp(int num) {
        long[] camps = new long[]{0, Integer.MAX_VALUE, 0, 0};
        int minCamp = 1;
        List<Lord> complexRanks = new ArrayList<>();
        complexRanks.addAll(rankDataManager.getComplexRank());
        for (int i = 1; i <= 3; i++) {
            final int camp = i;
            camps[camp] = complexRanks.stream().filter(lord -> lord.getCamp() == camp).limit(num).mapToLong(lord -> {
                // LogUtil.debug("camp:",camp,", lordId:",lord.getLordId(),", fight:",lord.getFight());
                return lord.getFight();
            }).sum();
            LogUtil.debug("阵营 camp:", camp, " 前【", num, "】,名总战斗力：", camps[camp]);
            if (camps[camp] < camps[minCamp]) {
                minCamp = camp;
            }
        }
        LogUtil.debug("最小战斗力的阵营 camp:", minCamp);
        return minCamp;
    }

    /**
     * 检查角色是否存在，如果不存在，抛出MwException异常
     *
     * @param roleId
     * @return 如果玩家角色存在，返回Player对象
     * @throws MwException
     */
    public Player checkPlayerIsExist(long roleId) throws MwException {
        Player player = getPlayer(roleId);
        if (null == player) {
            StringBuffer message = new StringBuffer();
            message.append("角色不存在, roleId:").append(roleId);
            throw new MwException(GameError.PLAYER_NOT_EXIST.getCode(), message.toString());
        }
        return player;
    }

    /**
     * 保存玩家事件完成记录
     *
     * @param player
     * @param type
     * @param strs
     * @return
     */
    public RoleOpt createRoleOpt(Player player, int type, String... strs) {
        if (player.opts.size() > Constant.ROLE_OPT_NUM) {// 事件数目固定6条记录
            player.opts.poll();
        }
        RoleOpt.Builder opt = RoleOpt.newBuilder();
        opt.setType(type);
        opt.setEndTime(TimeHelper.getCurrentSecond());
        opt.addAllParam(Arrays.asList(strs));
        player.opts.add(opt.build());
        return opt.build();
    }

    /**
     * 恢复体力
     *
     * @param player
     * @param now
     */
    public void restoreProsAndPower(Player player, int now) {
        try {
            Lord lord = player.lord;
            if (!fullPower(lord)) {
                backPower(player, now);
            } else {
                lord.setPowerTime(now);
            }
        }catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 能量是否达到上限
     *
     * @param lord
     * @return
     */
    public boolean fullPower(Lord lord) {
        if (lord.getPower() < Constant.POWER_MAX) {
            return false;
        }
        return true;
    }

    /**
     * 恢复能量
     *
     * @param player
     * @param now
     */
    public void backPower(Player player, int now) {
        Lord lord = player.lord;
        if (lord.getPowerTime() == 0) {
            lord.setPowerTime(now);
        }
        int period = now - lord.getPowerTime();
        int timeInterval = powerBackSecond(player);
        int back = period / timeInterval;
        if (back > 0) {
            int old = lord.getPower();
            int power = lord.getPower() + back;
            power = (power > Constant.POWER_MAX) ? Constant.POWER_MAX : power;
            int add = (power - old) <= 0 ? 0 : (power - old);
            lord.setPowerTime(lord.getPowerTime() + add * timeInterval);
            rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.ACT, add,
                    AwardFrom.RECV_PER_TIME);
            LogUtil.debug("更新体力lord=" + lord.getLordId() + ",add=" + add + ",time=" + lord.getPowerTime());
        }
        // 推送设置
        if (lord.getPower() >= Constant.POWER_MAX) {
            Integer status = player.getPushRecord(PushConstant.ACT_IS_FULL);
            if (null == status || status == PushConstant.ACT_IS_FULL) {// 没有推送过，推送消息
                player.putPushRecord(PushConstant.ACT_IS_FULL, PushConstant.ACT_IS_FULL);
                PushMessageUtil.pushMessage(player.account, PushConstant.ACT_IS_FULL);
            }
        } else {
            player.removePushRecord(PushConstant.ACT_IS_FULL);
        }

    }

    /**
     * 赛季天赋优化增益
     * @param player
     * @return
     */
    private int powerBackSecond(Player player) {
        return (int) (Constant.POWER_BACK_SECOND * (1 - (DataResource.getBean(SeasonTalentService.class).
                getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_615) / Constant.TEN_THROUSAND)));
    }

    /**
     * 下次恢复能量的剩余时间秒数
     *
     * @param player
     * @return
     */
    public int leftBackPowerTime(Player player) {
        restoreProsAndPower(player, TimeHelper.getCurrentSecond());
        if (!fullPower(player.lord)) {
            return player.lord.getPowerTime() + powerBackSecond(player) - TimeHelper.getCurrentSecond();
        }
        return 0;
    }

    public int countVip(int lv) {
        int cnt = 0;
        for (Player player : playerCache.values()) {
            if (player.lord.getLevel() == lv) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * 自动补兵,默认同步
     *
     * @param player
     * @throws MwException
     */
    public void autoAddArmy(Player player) {
        autoAddArmy(player, true);
    }

    /**
     * 自动补兵
     *
     * @param player
     * @param sync   是否同步
     */
    public void autoAddArmy(Player player, boolean sync) {
        if (player != null && player.common != null && player.common.getAutoArmy() > 0
                && techDataManager.isOpen(player, TechConstant.TYPE_19)) {
            int max; // 记录将领带兵量上限
            int need; // 记录需要补兵的数量
            int add; // 记录最终真实补兵数量
            int total; // 记录玩家兵营中当前拥有的兵力
            Hero hero;
            int armType; // 记录将领所属兵种
            StaticHero staticHero;
            ChangeInfo change = ChangeInfo.newIns();
            // 上阵将领 + 采集将领 自动补兵
            int[] heroIds = new int[player.heroBattle.length + player.heroAcq.length + player.heroCommando.length];
            System.arraycopy(player.heroBattle, 0, heroIds, 0, player.heroBattle.length);
            System.arraycopy(player.heroAcq, 0, heroIds, player.heroBattle.length, player.heroAcq.length);
            System.arraycopy(player.heroCommando, 0, heroIds, player.heroBattle.length + player.heroAcq.length, player.heroCommando.length);
            for (int heroId : heroIds) {
                hero = player.heros.get(heroId);
                if (hero == null) {
                    continue;
                }

                if (!hero.isIdle()) {
                    continue;
                }

                max = hero.getAttr()[HeroConstant.ATTR_LEAD];
                if (hero.getCount() >= max) {
                    hero.setCount(max);
                    continue;// 兵力已满，跳过
                }

                // 获取将领对应类型的兵力
                staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                if (null == staticHero) {
                    continue;
                }

                armType = staticHero.getType();
                total = getArmCount(player.resource, armType);
                if (total <= 0) {
                    continue;
                }

                need = max - hero.getCount();
                if (total <= need) {
                    add = total;
                } else {
                    add = need;
                }

                // 扣除兵力
                try {
                    rewardDataManager.subArmyResource(player, armType, add, AwardFrom.REPLENISH);
                } catch (Exception e) {
                    continue;
                }
                // 增加武将兵力
                hero.setCount(hero.getCount() + add);
                // LogLordHelper.heroArm(AwardFrom.REPLENISH, player.account, player.lord, heroId, hero.getCount(), add, armType,
                //         Constant.ACTION_ADD);

                // 上报玩家兵力变化信息
//                LogLordHelper.playerArm(
//                        AwardFrom.REPLENISH,
//                        player, armType,
//                        Constant.ACTION_ADD,
//                        add
//                );

                change.addChangeType(AwardType.ARMY, armType);
                change.addChangeType(AwardType.HERO_ARM, heroId);
            }
            if (sync) {
                rewardDataManager.syncRoleResChanged(player, change);
            }
        }
    }

    /**
     * 根据兵种获取玩家兵力
     *
     * @param resource
     * @param armType
     * @return
     */
    public int getArmCount(Resource resource, int armType) {
        if (null == resource) {
            return 0;
        }

        switch (armType) {
            case ArmyConstant.ARM1:
                return (int) resource.getArm1();
            case ArmyConstant.ARM2:
                return (int) resource.getArm2();
            case ArmyConstant.ARM3:
                return (int) resource.getArm3();
            default:
                break;
        }
        return 0;
    }

    /**
     * 隔天刷新
     *
     * @param player
     */
    public synchronized void refreshDaily(Player player) {
        int nowDay = TimeHelper.getCurrentDay();
        Lord lord = player.lord;
        Common common = player.common;
        int lastDay = TimeHelper.getDay(lord.getRefreshTime());
        if (nowDay != lastDay) {

            LogUtil.debug("每日刷新lord=" + lord.getLordId());
            // 隔天突破经验重置
            lord.setRefreshTime(TimeHelper.getCurrentSecond());
            for (Hero hero : player.heros.values()) {
                hero.setBreakExp(0);
            }

            if (common != null) {
                common.setBuyAct(0);
                common.setRetreat(0);
            }

            // 世界boss攻击次数刷新
            Task task = player.worldTasks.get(TaskType.WORLD_BOSS_TASK_ID_1);
            if (task != null && task.getSchedule() > 0) {
                task.setSchedule(0);
            }
            task = player.worldTasks.get(TaskType.WORLD_BOSS_TASK_ID_2);
            if (task != null && task.getSchedule() > 0) {
                task.setSchedule(0);
            }
        }
    }

    /**
     * 同步角色信息
     *
     * @param player
     */
    public void syncRoleInfo(Player player) {
        SyncRoleInfoRs.Builder builder = SyncRoleInfoRs.newBuilder();
        builder.setLevel(player.lord.getLevel());
        builder.setExp(player.lord.getExp());
        builder.addAllCharacter(PbHelper.createTwoIntListByMap(player.getCharacterData()));
        builder.addAllScoutData(PbHelper.createTwoIntListByMap(player.getScoutData()));
        builder.setFarmerCnt(PbHelper.createTwoIntPb(player.getResidentTotalCnt(), player.getIdleResidentCnt()));
        player.getMapCellData().forEach((cellId, cellState) -> {
            CommonPb.MapCell.Builder mapCell = CommonPb.MapCell.newBuilder();
            mapCell.setCellId(cellId);
            mapCell.addAllState(cellState);
            builder.addMapCellData(mapCell.build());
        });
        builder.addAllFoundationData(player.getFoundationData());
        if (player.ctx != null) {
            Base.Builder msg = PbHelper.createSynBase(SyncRoleInfoRs.EXT_FIELD_NUMBER, SyncRoleInfoRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    public String getNickByLordId(long roleId) {
        Player player = getPlayer(roleId);
        return player != null ? player.lord.getNick() : "";
    }

    /**
     * 同步战斗力
     *
     * @param player
     */
    public void syncFightChange(Player player) {
        SyncFightChgRs.Builder builder = SyncFightChgRs.newBuilder();
        builder.setFight(player.lord.getFight());
        if (player.ctx != null) {
            Base.Builder msg = PbHelper.createSynBase(SyncFightChgRs.EXT_FIELD_NUMBER, SyncFightChgRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 获取阵营奖励的Id
     *
     * @return 获取阵营奖励的KeyId
     */
    public int getRecommendKeyId() {
        int curDay = DateHelper.dayiy(DateHelper.parseDate(serverSetting.getOpenTime()), new Date());// 距今开服多少天
        List<StaticRecommend> list = StaticLordDataMgr.getRecommendCamp();
        for (StaticRecommend r : list) {
            if (r.getBeginDay() <= curDay && r.getEndDay() >= curDay) {
                return r.getKeyId();
            }
        }
        // 没找到符合条件的给第一奖励
        return 1;
    }

    /**
     * 获取推荐的阵营
     *
     * @return 推荐的阵营 camp,keyId
     */
    public Turple<Integer, Integer> getRecommendCamp() {
        int curDay = DateHelper.dayiy(DateHelper.parseDate(serverSetting.getOpenTime()), new Date());// 距今开服多少天
        // 获取配置
        int camp = Camp.EMPIRE;
        int keyId = 1;
        List<StaticRecommend> list = StaticLordDataMgr.getRecommendCamp();
        StaticRecommend sRecommend = null;
        for (StaticRecommend r : list) {
            if (r.getBeginDay() <= curDay && r.getEndDay() >= curDay) {
                sRecommend = r;
                break;
            }
        }
        if (sRecommend != null) {
            keyId = sRecommend.getKeyId();
            if (sRecommend.getType() == StaticRecommend.STRATEGY_TYPE_NUM_SMALL) {
                camp = getSmallCamp();
            } else if (sRecommend.getType() == StaticRecommend.STRATEGY_TYPE_FIGHT_SMALL) {
                camp = getFightSamllCamp(200);
            }
        }
        Turple<Integer, Integer> turple = new Turple<Integer, Integer>(camp, keyId);
        return turple;
    }

    /**
     * 创建角色第几天
     *
     * @param player 玩家对象
     * @return 创角第几天
     */
    public int getCreateRoleDay(Player player, Date now) {
        Date beginTime = TimeHelper.getDateZeroTime(player.account.getCreateDate());
        return DateHelper.dayiy(beginTime, now);
    }

    public int getRoleGrade45() {
        return roleGrade45;
    }

    public void setRoleGrade45(int roleGrade45) {
        this.roleGrade45 = roleGrade45;
    }

    /**
     * 获取所有的玩家
     *
     * @return
     */
    public Map<String, Player> getAllPlayer() {
        return allPlayer;
    }

    
}
