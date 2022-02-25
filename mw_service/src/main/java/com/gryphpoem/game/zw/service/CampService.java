package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.CrossCityService;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.PartyLog;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CampMember;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.party.*;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author
 * @ClassName CampService.java
 * @Description 阵营相关
 */
@Service
public class CampService extends BaseAwkwardDataManager {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private CampDataManager campDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private WorldService worldServices;

    @Autowired
    private WarDataManager warDataManager;

    @Autowired
    private RankDataManager rankDataManager;

    @Autowired
    private ChatDataManager chatDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private BattlePassDataManager battlePassDataManager;

    @Autowired
    private GlobalDataManager globalDataManager;

    /**
     * 世界进程排行榜数据
     */
    @Autowired
    private WorldScheduleRankService worldScheduleRankService;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private CrossCityService crossCityService;

    @Autowired
    private PlayerService playerService;
    @Autowired
    private TitleService titleService;

    /**
     * 获取阵营信息
     *
     * @param roleId 玩家唯一id
     * @return 阵营的详细信息
     * @throws MwException 自定义异常
     */
    public GetPartyRs getCampInfo(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 获取阵营信息
        Camp camp = getCampInfo(roleId, player.lord.getCamp());
        refreshDaily(player);
        GetPartyRs.Builder builder = GetPartyRs.newBuilder();
        builder.setCamp(camp.getCamp());
        builder.setPartyLv(camp.getPartyLv());
        builder.setPartyExp(camp.getPartyExp());
        builder.setSlogan(camp.getSlogan());
        if (!CheckNull.isNullTrim(camp.getQq())) {
            builder.setQq(camp.getQq());
        }
        if (!CheckNull.isNullTrim(camp.getWx())) {
            builder.setWx(camp.getWx());
        }
        builder.setAuthor("");
        String author = camp.getAuthor();
        if (!CheckNull.isNullTrim(author)) {
            long lordId = 0L;
            try {
                lordId = Long.parseLong(author);
            } catch (Exception e) {
                LogUtil.error("获取阵营信息时候, Long转换异常", e);
            }
            Player authorP = playerDataManager.getPlayer(lordId);
            if (authorP != null) {
                builder.setAuthor(authorP.lord.getNick());
            }
        }
        builder.setJobOpen(camp.isOpenJob());
        // 玩家在军团中的信息
        CampMember member = campDataManager.getCampMember(roleId);
        member.refreshData();
        builder.setBuild(member.getBuild());
        builder.setRanks(player.lord.getRanks());
        // builder.setMyRank(camp.getPlayerRank(roleId));// 原来是按照战斗力
        builder.setMyRank(rankDataManager.getMyRank(rankDataManager.complexRankList, player.lord, 2));
        builder.setExploit(player.lord.getExploit());
        builder.setJob(player.lord.getJob());
        // 当天发送的阵营邮件次数
        builder.setSendCnt(player.getMixtureDataById(PlayerConstant.DAILY_SEND_MAIL_CNT));
        return builder.build();
    }

    public Camp getCampInfo(long roleId, int camp) throws MwException {
        Camp party = campDataManager.getParty(camp);
        if (null == party) {
            throw new MwException(GameError.PARTY_NOT_INIT.getCode(), "军团数据未初始化, roleId:", roleId, ", camp:", camp);
        }
        return party;
    }

    /**
     * 阵营建设
     *
     * @param roleId 玩家的唯一id
     * @return 阵营的建设信息
     * @throws MwException 自定义异常
     */
    public PartyBuildRs campBuild(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 查询玩家所属阵营
        Camp camp = getCampInfo(roleId, player.lord.getCamp());
        // 刷新每天的数据
        refreshDaily(player);
        // 检查军团等级 要求可以无限建设加个人军功
        // int maxPartyLv = StaticPartyDataMgr.getMaxPartyLv();
        // if (camp.getPartyLv() >= maxPartyLv) {
        // throw new MwException(GameError.PARTY_LV_MAX.getCode(), "军团等级达到上限, roleId:", roleId, ", lv:",
        // camp.getPartyLv(), ", max:", maxPartyLv);
        // }

        // 检查是否已达上限
        CampMember member = campDataManager.getCampMember(roleId);
        LogUtil.debug("1member.refresh=" + member.getBuildDate());
        member.refreshData();
        LogUtil.debug("2member.refresh=" + member.getBuildDate());
        int build = member.getBuild();
        if (build >= PartyConstant.PARTY_BUILD_MAX) {
            throw new MwException(GameError.PARTY_BUILD_MAX.getCode(), "军团建设达到上限, roleId:", roleId, ", build:", build,
                    ", max:", PartyConstant.PARTY_BUILD_MAX);
        }

        StaticPartyBuild spb = StaticPartyDataMgr.getPartyBuildConfig(camp.getPartyLv(), build + 1);
        if (null == spb) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "军团建设信息未配置, roleId:", roleId, ", lv:",
                    camp.getPartyLv(), ", build:", build + 1);
        }
        if (build > 0) {
            // 检查并扣除消耗
            rewardDataManager.checkAndSubPlayerRes(player, spb.getCost(), AwardFrom.PARTY_BUILD);
        }

        // 更新建设次数
        member.partyBuild();
        LogUtil.debug("3member.refresh=" + member.getBuildDate());
        camp.addPartyHonorRank(PartyConstant.RANK_TYPE_BUILD, player.roleId, player.lord.getNick(), member.getBuild(),
                TimeHelper.getCurrentSecond());

        // 建设排行
        activityDataManager.updRankActivity(player, ActivityConst.ACT_PARTY_BUILD_RANK, 1);
        taskDataManager.updTask(player, TaskType.COND_CAMP_BUILD_38, 1);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_CAMP_BUILD_38, 1);
        // 增加玩家军功
        rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.EXPLOIT, spb.getExploit(),
                AwardFrom.PARTY_BUILD);

        int oldLv = camp.getPartyLv();
        // 更新军团经验
        addCampExp(camp, spb.getPartyExp());
        if (oldLv != camp.getPartyLv()) {
            //不同等级给阵营
            this.syncPartyInfo(camp);
        }

        // 记录军团建设次数
        camp.build();
        // 发送系统消息
        checkHonorRewardAndSendSysChat(camp);

        //貂蝉任务-建设阵营
        ActivityDiaoChanService.completeTask(player, ETask.BUILD_CAMP);
        //喜悦金秋-日出而作- 阵营建设xx次
        TaskService.processTask(player, ETask.BUILD_CAMP);
        //称号-建设阵营
        titleService.processTask(player, ETask.BUILD_CAMP);

        PartyBuildRs.Builder builder = PartyBuildRs.newBuilder();

        builder.setBuild(member.getBuild());
        builder.setPartyLv(camp.getPartyLv());
        builder.setPartyExp(camp.getPartyExp());
        builder.setExploit(player.lord.getExploit());
        return builder.build();
    }

    private void syncPartyInfo(Camp camp) {
        GamePb4.SyncPartyInfoRs.Builder builder = GamePb4.SyncPartyInfoRs.newBuilder();
        builder.setLevel(camp.getPartyLv());
        builder.setExp(camp.getPartyExp());
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncPartyInfoRs.EXT_FIELD_NUMBER, GamePb4.SyncPartyInfoRs.ext, builder.build()).build();
        playerService.syncMsgToCamp(msg, camp.getCamp());
    }

    /**
     * 给阵营加经验
     *
     * @param camp 阵营
     * @param exp  经验
     */
    private void addCampExp(Camp camp, int exp) {
        int lv = camp.getPartyLv();
        int maxLv = StaticPartyDataMgr.getMaxPartyLv();
        do {
            // 当前阵营升级需要的经验
            int need = StaticPartyDataMgr.getPartyNeedExpByIv(lv);
            if (camp.getPartyExp() + exp >= need) {

                exp -= (need - camp.getPartyExp());
                // 升级了就设置成当前等级的经验
                camp.setPartyExp(need);
                if (StaticPartyDataMgr.getPartyNeedExpByIv(camp.getPartyLv() + 1) > 0) {
                    camp.setPartyLv(camp.getPartyLv() + 1);
                }
            } else {
                camp.setPartyExp(camp.getPartyExp() + exp);
                exp = 0;
            }
            lv = camp.getPartyLv();
        } while (exp > 0 && lv < maxLv);
    }

    /**
     * 晋升军阶
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public PromoteRanksRs promoteRanks(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查是否已达上限
        int ranks = player.lord.getRanks();
        int maxRanks = StaticPartyDataMgr.getMaxPartyRanks();
        if (ranks >= maxRanks) {
            throw new MwException(GameError.PARTY_RANKS_MAX.getCode(), "玩家已达最高军阶, roleId:", roleId, ", ranks:", ranks,
                    ", maxRanks:", maxRanks);
        }

        StaticPartyRanks spr = StaticPartyDataMgr.getRanksMap().get(ranks);
        if (null == spr) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "军团军阶信息未配置, roleId:", roleId, ", ranks:", ranks);
        }

        // 检查并扣除资源消耗
        rewardDataManager.checkAndSubPlayerRes(player, spr.getCost(), AwardFrom.PROMOTE_RANKS);

        // 晋升军阶
        player.lord.setRanks(ranks + 1);
        rankDataManager.setRanks(player.lord);

        // 更新玩家所有将领属性
        CalculateUtil.reCalcAllHeroAttr(player);

        //貂蝉任务-爵位等级
        ActivityDiaoChanService.completeTask(player, ETask.TITLE_LV);
        TaskService.processTask(player, ETask.TITLE_LV);

        PromoteRanksRs.Builder builder = PromoteRanksRs.newBuilder();
        builder.setRanks(player.lord.getRanks());
        builder.setExploit(player.lord.getExploit());
        return builder.build();
    }

    /**
     * 修改军团公告
     *
     * @param roleId
     * @param slogan
     * @return
     * @throws MwException
     */
    public ModifySloganRs modifySlogan(long roleId, String slogan) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        // 检查玩家是否有权限
        int job = player.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_SLOGAN;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "没有这个特权, roleId:", roleId, ", job:", job,
                    ", privilege:", privilege);
        }

        // 检查公告
        if (slogan == null || slogan.length() > PartyConstant.PARTY_SLOGAN_LEN) {
            throw new MwException(GameError.PARTY_SLOGAN_ERR.getCode(), "军团公告格式不正确, roleId:", roleId, ", slogan:",
                    slogan);
        }

        slogan = EmojiHelper.filterEmoji(slogan);

        camp.setSlogan(slogan);
        camp.setAuthor(String.valueOf(player.lord.getLordId()));
        // 通知本正营
        chatDataManager.sendSysChat(ChatConst.CHAT_KING_MODIFY_SLOGAN, player.lord.getCamp(), 0, player.lord.getNick());
        ModifySloganRs.Builder builder = ModifySloganRs.newBuilder();
        builder.setSlogan(slogan);
        return builder.build();
    }

    /**
     * 修改留言板
     */
    public ModifySloganRs modifyBbs(long roleId, String qq, String wx) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Camp camp = getCampInfo(roleId, player.lord.getCamp());
        int privilege = PartyConstant.PRIVILEGE_BBS;
        if (!camp.isOpenJob()) {
            // 军团官员未开启 vip5以上的玩家可以更改
            int vip = player.lord.getVip();
            if (vip < VipConstant.VIP_FIVE) {
                throw new MwException(GameError.VIP_NOT_ENOUGH.getCode(), "vip等级不够, roleId:", roleId, ", vip:", vip,
                        ", privilege:", privilege);
            }
        } else {
            // 开启军团官员，总司令、参谋和政委可进行更改
            // 检查玩家是否有权限
            int job = player.lord.getJob();
            if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
                throw new MwException(GameError.NO_PRIVILEGE.getCode(), "没有这个特权, roleId:", roleId, ", job:", job,
                        ", privilege:", privilege);
            }
        }
        camp.setQq(qq);
        camp.setWx(wx);
        ModifySloganRs.Builder builder = ModifySloganRs.newBuilder();
        builder.setQq(qq).setWx(wx);
        return builder.build();
    }

    /**
     * 获取军团城池信息（只获取玩家所在分区的）
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPartyCityRs getPartyCity(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (CrossWorldMapService.isOnCrossMap(player)) {
            return crossCityService.getPartyCity(player);
        }

        int areaId = MapHelper.getAreaIdByPos(player.lord.getPos());
        List<City> cityList = worldDataManager.getCityInArea(areaId);

        GetPartyCityRs.Builder builder = GetPartyCityRs.newBuilder();
        City myCity = worldDataManager.getMyOwnerCity(roleId);
        if (cityList != null) {
            for (City city : cityList) {
                if (city.getCamp() == player.lord.getCamp()) {
                    if (myCity != null && myCity.getCityId() == city.getCityId()) {
                        continue;
                    }
                    builder.addCity(PbHelper.createPartyCityPb(city, playerDataManager));
                }
            }
        }

        if (myCity != null) {
            builder.addCity(PbHelper.createPartyCityPb(myCity, playerDataManager));
        }
        return builder.build();
    }

    /**
     * 获取军团战争信息
     *
     * @param roleId 玩家对象
     * @return 集结战斗
     * @throws MwException 自定义异常
     */
    public GetPartyBattleRs getPartyBattle(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (CrossWorldMapService.isOnCrossMap(player)) {
            return crossCityService.getPartyBattle(player);
        }

        // 集结战斗
        List<Battle> rallyBattleList = warDataManager.getBattlePosMap()
                .values()
                .stream()
                .flatMap(Collection::stream)
                // 过滤需要显示在集结列表的战斗
                .filter(battle -> checkRallyBattle(battle, player))
                .filter(battle -> {
                    int battlePos = battle.getPos();
                    // 过滤区域
                    return getParticipatePlayer(battlePos, player);
                })
                .collect(Collectors.toList());

        List<Battle> superBattleList = getSuperMineBattle(player);
        if (!ObjectUtils.isEmpty(superBattleList))
            rallyBattleList.addAll(superBattleList);

        GetPartyBattleRs.Builder builder = GetPartyBattleRs.newBuilder();

        if (!CheckNull.isEmpty(rallyBattleList)) {
            rallyBattleList.forEach(battle -> {
                CommonPb.Battle battlePb = PbHelper.createBattlePb(battle, worldServices.getDefArmCntByBattle(battle));
                battlePb = addMineInfo(battle, player, battlePb, checkJoinSuperMineBattle(player, battle));
                builder.addBattle(battlePb);
            });
        }

        List<AirshipWorldData> rallyAirshipList = worldDataManager.getAllAirshipWorldData()
                .stream()
                .filter(awd -> checkRallyBattle(awd, player))
                .filter(awd -> {
                    int battlePos = awd.getPos();
                    // 过滤区域
                    return getParticipatePlayer(battlePos, player);
                })
                .collect(Collectors.toList());

        if (!CheckNull.isEmpty(rallyAirshipList)) {
            rallyAirshipList.forEach(awd -> builder.addAirShip(PbHelper.createAirshipShowClientPb(awd, player.lord.getCamp(), true, playerDataManager)));
        }

		//填充Assemble集结信息
        List<Player> pushPlayerList = new ArrayList<>();
        int now = TimeHelper.getCurrentSecond();
        Optional.ofNullable(playerDataManager.getPlayerByCamp(player.getCamp())).ifPresent(map -> {
            map.values().forEach(p -> {
                if (Objects.nonNull(p.summon)) {
                    int endTime = p.summon.getLastTime() + Constant.SUMMON_KEEP_TIME;
                    if (p.summon.getStatus() != 0 && now > endTime) {
                        p.summon.getRespondId().clear();
                        p.summon.setStatus(0);
                        return;
                    }
                    if (p.summon.getStatus() == 1) {
                        pushPlayerList.add(p);
                    }
                }
            });
        });
		if (!pushPlayerList.isEmpty()) {
            for (Player pushPlayer : pushPlayerList) {
                builder.addAssemble(worldServices.syncAssemblyInfo(pushPlayer));
            }
        }

        return builder.build();
    }

    /**
     * 校验当前玩家是否参与超级矿战斗
     *
     * @param player
     * @param battle
     * @return
     */
    private boolean checkJoinSuperMineBattle(Player player, Battle battle) {
        SuperMine superMine = worldDataManager.getSuperMineMap().get(battle.getPos());
        if (CheckNull.isNull(superMine))
            return false;

        boolean joinCollect = false;
        boolean joinHelp = false;
        if (!ObjectUtils.isEmpty(superMine.getCollectArmy())) {
            for (SuperGuard superGuard : superMine.getCollectArmy()) {
                if (CheckNull.isNull(superGuard))
                    continue;
                if (CheckNull.isNull(superGuard.getArmy()))
                    continue;
                if (superGuard.getArmy().getLordId() != player.getLordId())
                    continue;

                joinCollect = true;
                break;
            }
        }
//        if (!ObjectUtils.isEmpty(superMine.getHelpArmy())) {
//            for (Army army : superMine.getHelpArmy()) {
//                if (CheckNull.isNull(army))
//                    continue;
//                if (army.getLordId() != player.getLordId())
//                    continue;
//
//                joinHelp = true;
//                break;
//            }
//        }

        return joinCollect || joinHelp;
    }

    /**
     * 获取超级矿点战斗信息
     *
     * @param player
     * @return
     */
    private List<Battle> getSuperMineBattle(Player player) {
        if (ObjectUtils.isEmpty(worldDataManager.getSuperMineMap().values()))
            return null;

        List<Battle> superBattles = new ArrayList<>();
        worldDataManager.getSuperMineMap().values().forEach(superMine -> {
            if (ObjectUtils.isEmpty(superMine.getBattleIds()))
                return;

            for (Integer battleId : superMine.getBattleIds()) {
                Battle superBattle = warDataManager.getBattleMap().get(battleId);
                if (CheckNull.isNull(superBattle))
                    continue;
                if (!checkRallyBattle(superBattle, player))
                    continue;
                if (!getParticipatePlayer(superBattle.getPos(), player))
                    continue;
                superBattles.add(superBattle);
            }
        });

        return superBattles;
    }

    /**
     * 检测集结战斗
     *
     * @param battle 战斗信息
     * @param player 玩家信息
     * @return 是否显示在集结列表内
     */
    public boolean checkRallyBattle(Battle battle, Player player) {
        if (Objects.isNull(battle) || Objects.isNull(player)) {
            return false;
        }
        if (!battle.isRallyBattleType()) {
            return false;
        }

        int battleType = battle.getType();
        // 玩家阵营
        int camp = player.lord.getCamp();
        // 未开启世界
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_WORLD)) {
            return false;
        }
        switch (battleType) {
            // 打玩家
            case WorldConstant.BATTLE_TYPE_CITY:
                // 闪电战类型, 进攻方收不到集结消息
                if (battle.getBattleType() == WorldConstant.CITY_BATTLE_BLITZ && battle.getAtkCamp() == camp) {
                    return false;
                }
                // 打城池
            case WorldConstant.BATTLE_TYPE_CAMP:
                // 即不是进攻方阵营、也不是防守方阵营
                if (battle.getAtkCamp() != camp && battle.getDefCamp() != camp) {
                    return false;
                }
                // 打玩家和打城的判断等级相同
                if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
                    return false;
                }
                break;
            // 盖世太保战
            case WorldConstant.BATTLE_TYPE_GESTAPO:
                // 低级的盖世太保不发送集结
                if (battle.getBattleType() == Gestapo.GESTAPO_ID_1) {
                    return false;
                }
                // 不是进攻方阵营
                if (battle.getAtkCamp() != camp) {
                    return false;
                }
                if (ActParamConstant.ACT_GESTAPO_LEVEL.get(0) > player.lord.getLevel()) {
                    return false;
                }
                break;
            //超级矿点
            case WorldConstant.BATTLE_TYPE_SUPER_MINE:
                //采集战斗
            case WorldConstant.BATTLE_TYPE_MINE_GUARD:
                if (battle.getDefCamp() != camp) {
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * 检测集结战斗
     *
     * @param awd    飞艇数据
     * @param player 玩家阵营
     * @return 是否显示在集结列表内
     */
    private boolean checkRallyBattle(AirshipWorldData awd, Player player) {
        if (Objects.isNull(awd) || Objects.isNull(player)) {
            return false;
        }
        if (!awd.isLiveStatus()) {
            return false;
        }
        List<CommonPb.BattleRole> joinRole = awd.getJoinRoles().get(player.lord.getCamp());
        // 本阵营没有人参与战斗
        // TODO: 2020/12/7 服务器没有判断攻打飞艇的等级限制
        return !CheckNull.isEmpty(joinRole);
    }

    /**
     * 推送满足条件的集结
     *
     * @param player 玩家信息
     * @param battle 战斗数据
     * @param awd    飞艇数据
     */
    public void syncRallyBattle(Player player, Battle battle, AirshipWorldData awd) {
        if (Objects.isNull(player)) {
            return;
        }
        // 需要推送的玩家
        Collection<Player> syncRole = playerDataManager.getAllOnlinePlayer().values();
        SyncRallyBattleRs.Builder builder = SyncRallyBattleRs.newBuilder();
        // 战斗发生的坐标
        int battlePos = 0;
        if (Objects.nonNull(battle)) {
            if (Objects.isNull(battle.getSponsor())) {
                return;
            }
            builder.setBattle(PbHelper.createBattlePb(battle, worldServices.getDefArmCntByBattle(battle)));
            // 普通战斗需要判断玩家参与条件
            syncRole = syncRole
                    .stream()
                    .filter(p -> checkRallyBattle(battle, p))
                    .collect(Collectors.toList());
            battlePos = battle.getPos();
        }
        if (Objects.nonNull(awd)) {
            builder.setAirShip(PbHelper.createAirshipShowClientPb(awd, player.lord.getCamp(), true, playerDataManager));
            // 过滤阵营
            syncRole = syncRole
                    .stream()
                    .filter(p -> checkRallyBattle(awd, p))
                    // 飞艇只推送发起的阵营玩家
                    .filter(p -> p.lord.getCamp() == player.lord.getCamp())
                    .collect(Collectors.toList());
            battlePos = awd.getPos();
        }
        // 过滤开启世界
        int finalBattlePos = battlePos;
        syncRole = syncRole.stream()
                .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.FUNC_ID_WORLD))
                .filter(p -> getParticipatePlayer(finalBattlePos, p))
                .collect(Collectors.toList());

        // 推送给所有满足条件的在线玩家
        Base.Builder msg = null;
        if (Objects.nonNull(battle)) {
            if (battle.getType() != WorldConstant.BATTLE_TYPE_SUPER_MINE) {
                builder.setBattle(addMineInfo(battle, null, builder.getBattle(), false));
                msg = PbHelper.createRsBase(SyncRallyBattleRs.EXT_FIELD_NUMBER, SyncRallyBattleRs.ext, builder.build());
            }
        } else {
            msg = PbHelper.createRsBase(SyncRallyBattleRs.EXT_FIELD_NUMBER, SyncRallyBattleRs.ext, builder.build());
        }

        if (!CheckNull.isEmpty(syncRole)) {
            for (Player p : syncRole) {
                if (p.isLogin) {
                    if (Objects.nonNull(battle) && battle.getType() == WorldConstant.BATTLE_TYPE_SUPER_MINE) {
                        builder.setBattle(addMineInfo(battle, p, builder.getBattle(), checkJoinSuperMineBattle(p, battle)));
                        msg = PbHelper.createRsBase(SyncRallyBattleRs.EXT_FIELD_NUMBER, SyncRallyBattleRs.ext, builder.build());
                    }
                    MsgDataManager.getIns().add(new Msg(p.ctx, msg.build(), p.roleId));
                } else {
                    // TODO: 2020/12/7 离线推送, 海外需要
                }
            }
        }
    }

    /**
     * 同步战斗集结时，添加矿点信息
     *
     * @param battle
     * @param defPlayer
     * @param builder
     * @return
     */
    private CommonPb.Battle addMineInfo(Battle battle, Player defPlayer, CommonPb.Battle builder, boolean joinSuperMineBattle) {
        if (battle.getType() != WorldConstant.BATTLE_TYPE_MINE_GUARD && battle.getType() != WorldConstant.BATTLE_TYPE_SUPER_MINE) {
            return builder;
        }

        //添加矿点信息
        int now = TimeHelper.getCurrentSecond();
        CommonPb.MineInfo.Builder mineInfo = null;
        CommonPb.Battle.Builder battleBuilder = builder.toBuilder();
        if (battle.getType() == WorldConstant.BATTLE_TYPE_MINE_GUARD) {
            StaticMine staticMine = worldDataManager.getMineByPos(battle.getPos());
            if (Objects.nonNull(staticMine)) {
                mineInfo = CommonPb.MineInfo.newBuilder();
                mineInfo.setMineId(staticMine.getMineId());
                mineInfo.setForceType(WorldConstant.FORCE_TYPE_MINE);
                if (!ObjectUtils.isEmpty(battle.getSponsor().armys)) {
                    for (Army army : battle.getSponsor().armys.values()) {
                        if (CheckNull.isNull(army))
                            continue;
                        if (army.getTarget() != battle.getPos() || now > army.getEndTime() ||
                                army.getType() != ArmyConstant.ARMY_TYPE_COLLECT || army.getState() != ArmyConstant.ARMY_STATE_MARCH)
                            continue;

                        mineInfo.setAttackKeyId(army.getHero().get(0).getV1());
                        break;
                    }
                }

                Guard guard = worldDataManager.getGuardByPos(battle.getPos());
                if (Objects.nonNull(guard) && Objects.nonNull(guard.getArmy())) {
                    mineInfo.setDefKeyId(guard.getArmy().getHero().get(0).getV1());
                }
            }
        }

        if (battle.getType() == WorldConstant.BATTLE_TYPE_SUPER_MINE) {
            SuperMine superMine = worldDataManager.getSuperMineMap().get(battle.getPos());
            if (Objects.nonNull(superMine)) {
                mineInfo = CommonPb.MineInfo.newBuilder();
                mineInfo.setMineId(superMine.getConfigId());
                mineInfo.setForceType(WorldConstant.FORCE_TYPE_SPUER_MINE);
                if (!ObjectUtils.isEmpty(battle.getSponsor().armys)) {
                    for (Army army : battle.getSponsor().armys.values()) {
                        if (CheckNull.isNull(army))
                            continue;
                        if (army.getTarget() != battle.getPos() || now > army.getEndTime() ||
                                army.getType() != ArmyConstant.ARMY_TYPE_ATK_SUPERMINE || army.getState() != ArmyConstant.ARMY_STATE_MARCH)
                            continue;

                        mineInfo.setAttackKeyId(army.getHero().get(0).getV1());
                        break;
                    }
                }

                if (defPlayer.getLordId() == battle.getSponsor().getLordId()) {
                    battleBuilder.setDefName(defPlayer.lord.getNick());
                    mineInfo.setDefKeyId(mineInfo.getAttackKeyId());
                } else {
                    if (!ObjectUtils.isEmpty(superMine.getCollectArmy())) {
                        for (SuperGuard superGuard : superMine.getCollectArmy()) {
                            if (CheckNull.isNull(superGuard) || CheckNull.isNull(superGuard.getArmy()))
                                continue;
                            if (superGuard.getArmy().getLordId() == defPlayer.getLordId()) {
                                mineInfo.setDefKeyId(superGuard.getArmy().getHero().get(0).getV1());
                                break;
                            }
                        }
                    }
                }

                if (joinSuperMineBattle)
                    battleBuilder.setDefName(defPlayer.lord.getNick());
            }
        }

        if (Objects.nonNull(mineInfo)) {
            battleBuilder.setMineInfo(mineInfo.build());
            return battleBuilder.build();
        }

        return builder;
    }

    /**
     * 推送满足条件的集结(取消)
     *
     * @param battle 战斗数据
     * @param awd    飞艇数据
     */
    public void syncCancelRallyBattle(Player player, Battle battle, AirshipWorldData awd) {
        if (Objects.isNull(battle) && Objects.isNull(awd)) {
            return;
        }
        Collection<Player> syncRole = playerDataManager.getAllOnlinePlayer().values();

        SyncRallyBattleRs.Builder builder = SyncRallyBattleRs.newBuilder();
        // 战斗发生的坐标
        int battlePos = 0;
        if (Objects.nonNull(battle)) {
            Player sponsor = battle.getSponsor();
            if (Objects.isNull(sponsor)) {
                return;
            }
            builder.setBattle(CommonPb.Battle.newBuilder().setBattleId(battle.getBattleId()).build());
            // 普通战斗需要判断玩家参与条件
            syncRole = syncRole
                    .stream()
                    .filter(p -> checkRallyBattle(battle, p))
                    .collect(Collectors.toList());
            battlePos = battle.getPos();
        }
        if (Objects.nonNull(awd)) {
            builder.setAirShip(CommonPb.Airship.newBuilder().setId(awd.getId()).setKeyId(awd.getKeyId()).setAreaId(awd.getAreaId()));
            // 过滤阵营
            syncRole = syncRole
                    .stream()
                    .filter(p -> checkRallyBattle(awd, p))
                    .collect(Collectors.toList());
            battlePos = awd.getPos();
        }
        // 过滤开启世界
        int finalBattlePos = battlePos;
        syncRole = syncRole.stream()
                .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.FUNC_ID_WORLD))
                .filter(p -> getParticipatePlayer(finalBattlePos, p))
                .collect(Collectors.toList());
        // 移除状态
        builder.setStatus(1);
        // 推送给所有满足条件的在线玩家
        Base.Builder msg = PbHelper.createRsBase(SyncRallyBattleRs.EXT_FIELD_NUMBER, SyncRallyBattleRs.ext, builder.build());
        if (!CheckNull.isEmpty(syncRole)) {
            syncRole.forEach(p -> {
                if (p.isLogin) {
                    MsgDataManager.getIns().add(new Msg(p.ctx, msg.build(), p.roleId));
                } else {
                    // TODO: 2020/12/7 离线推送, 海外需要
                }
            });

        }

    }

    /**
     * 获取战斗邀请
     *
     * @param roleId    玩家id
     * @param battleId  战斗id
     * @param airshipId 飞艇的id
     * @return 邀请列表
     */
    public GetInvitesBattleRs getInvitesBattle(long roleId, int battleId, int airshipId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (battleId == 0 && airshipId == 0) {
            throw new MwException(GameError.INVITES_BATTLE_PARAM_ERROR.getCode(), "获取战斗邀请参数错误, roleId: ", roleId, ", battleId: ", battleId, ", airshipId: ", airshipId);
        }

        HashSet<Long> filterRole = new HashSet<>();
        filterRole.add(roleId);
        int battlePos = 0;
        int camp = player.lord.getCamp();
        AirshipWorldData airship = null;
        Battle battle = null;
        if (airshipId != 0) {
            airship = worldDataManager.getAllAirshipWorldData().stream()
                    .filter(awd -> awd.getKeyId() == airshipId && awd.isLiveStatus())
                    .findAny()
                    .orElse(null);
            if (Objects.nonNull(airship)) {
                battlePos = airship.getPos();
            }
        }
        if (battleId != 0) {
            battle = warDataManager.getBattleMap().get(battleId);
            if (battle == null) {
                battle = warDataManager.getSpecialBattleMap().get(battleId);
            }
            if (Objects.nonNull(battle)) {
                battlePos = battle.getPos();
                // 发起者
                long sponsorId = battle.getSponsorId();
                if (sponsorId != 0L) {
                    filterRole.add(sponsorId);
                }
                // 防守者
                long defencerId = battle.getDefencerId();
                if (defencerId != 0L) {
                    filterRole.add(defencerId);
                }
            }
        }
        GetInvitesBattleRs.Builder builder = GetInvitesBattleRs.newBuilder();
        if (battlePos != 0 && camp != 0) {
            int finalBattlePos = battlePos;
            List<Player> canInvitesPlayer = playerDataManager.getAllPlayer()
                    .values()
                    .stream()
                    .filter(p -> p.lord.getCamp() == camp && p.lord.getLevel() >= 40 && !filterRole.contains(p.roleId))
                    .filter(p -> getParticipatePlayer(finalBattlePos, p))
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(canInvitesPlayer)) {
                for (Player p : canInvitesPlayer) {
                    builder.addRole(PbHelper.crateInvitesRole(p, airship, battle));
                }
            }
        } else {
            throw new MwException(GameError.INVITES_BATTLE_PARAM_ERROR.getCode(), "获取战斗邀请参数错误, roleId: ", roleId, ", battleId: ", battleId, ", airshipId: ", airshipId);
        }
        return builder.build();
    }

    /**
     * 获取可以邀请的玩家
     *
     * @param battlePos 战斗发生的坐标
     * @param p         过滤的玩家
     * @return 可以邀请的玩家列表
     */
    public boolean getParticipatePlayer(int battlePos, Player p) {
        // 发生战斗的区域
        int BattleArea = MapHelper.getAreaIdByPos(battlePos);
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(BattleArea);
        if (Objects.isNull(staticArea)) {
            return false;
        }
        // 区域的开放顺序
        int openOrder = staticArea.getOpenOrder();
        // 当前的世界进程
        int curScheduleId = worldScheduleService.getCurrentSchduleId();
        // 玩家所在区域
        int area = p.lord.getArea();
        if (curScheduleId <= ScheduleConstant.SCHEDULE_BOOS_1_ID) {
            // 帝国纪元<=5阶段时，仅筛选出本区域
            return area == BattleArea;
        } else {
            // 所有区域的配置
            Collection<StaticArea> areaCollection = StaticWorldDataMgr.getAreaMap().values();
            if (curScheduleId <= ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                // 帝国纪元<=9并且大于5阶级
                if (openOrder == WorldConstant.AREA_ORDER_1) {
                    // openOrder为1的时候, 筛选当前区域
                    return area == BattleArea;
                } else if (openOrder == WorldConstant.AREA_ORDER_2) {
                    // openOrder为2的时候, 筛选openOrder为2的所有区域
                    return areaCollection
                            .stream()
                            .filter(a -> a.getOpenOrder() == WorldConstant.AREA_ORDER_2)
                            .map(StaticArea::getArea)
                            .collect(Collectors.toList())
                            .contains(area);
                }
            } else {
                // openOrder为3的时候, 筛选openOrder为3的所有区域
                return areaCollection
                        .stream()
                        .filter(a -> a.getOpenOrder() == WorldConstant.AREA_ORDER_3 || area == BattleArea)
                        .map(StaticArea::getArea)
                        .collect(Collectors.toList())
                        .contains(area);
            }
        }
        return false;
    }

    /**
     * 发起邀请
     *
     * @param roleId 发起者roleId
     * @param req    邀请参数
     * @return 响应消息
     */
    public MakeInvitesRs makeInvites(long roleId, MakeInvitesRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int battleId = req.getBattleId();
        int airshipId = req.getAirshipId();
        List<Long> invitesRoles = req.getInvitesRoleList();
        if (battleId == 0 && airshipId == 0) {
            throw new MwException(GameError.MAKE_INVITES_PARAM_ERROR.getCode(), "获取战斗邀请参数错误, roleId: ", roleId, ", battleId: ", battleId, ", airshipId: ", airshipId);
        }
        if (CheckNull.isEmpty(invitesRoles)) {
            throw new MwException(GameError.MAKE_INVITES_PARAM_ERROR.getCode(), "获取战斗邀请人为空, roleId: ", roleId, ", battleId: ", battleId, ", airshipId: ", airshipId);
        }

        // 推送协议封装
        SyncInvitesBattleRs.Builder builder = SyncInvitesBattleRs.newBuilder();

        AirshipWorldData airship = null;
        Battle battle = null;
        if (airshipId != 0) {
            airship = worldDataManager.getAllAirshipWorldData().stream()
                    .filter(awd -> awd.getKeyId() == airshipId && awd.isLiveStatus())
                    .findAny()
                    .orElse(null);
            if (Objects.nonNull(airship)) {
                builder.setAirshipId(airshipId);
            }
        }
        if (battleId != 0) {
            battle = warDataManager.getBattleMap().get(battleId);
            if (battle == null) {
                battle = warDataManager.getSpecialBattleMap().get(battleId);
            }
            if (Objects.nonNull(battle)) {
                builder.setBattleId(battleId);
            }
        }

        // 邀请人的数据
        builder.setInvites(PbHelper.crateInvitesRole(player, airship, battle));
        Base.Builder msg = PbHelper.createRsBase(SyncInvitesBattleRs.EXT_FIELD_NUMBER, SyncInvitesBattleRs.ext, builder.build());


        // 被邀请的玩家
        for (Long invitesRole : invitesRoles) {
            Player invitesPlayer = playerDataManager.checkPlayerIsExist(invitesRole);
            boolean invitesFlag = false;
            if (Objects.nonNull(airship)) {
                if (!airship.getInvites().contains(invitesRole) && airship.getJoinRoles().computeIfAbsent(player.lord.getCamp(), k -> new ArrayList<>()).stream().noneMatch(br -> br.getRoleId() == invitesRole)) {
                    invitesFlag = true;
                    // TODO: 2020/12/21 邀请状态, 这里由客户端自己判断
                    // airship.getInvites().add(invitesRole);
                }
            }
            if (Objects.nonNull(battle)) {
                if (!battle.getInvites().contains(invitesRole) && !battle.getAtkRoles().contains(invitesRole)) {
                    invitesFlag = true;
                    // battle.getInvites().add(invitesRole);
                }
            }
            if (invitesFlag) {
                // 推送给所有满足条件的在线玩家
                if (invitesPlayer.isLogin) {
                    MsgDataManager.getIns().add(new Msg(invitesPlayer.ctx, msg.build(), invitesPlayer.roleId));
                } else {
                    // TODO: 2020/12/7 离线推送, 海外需要
                }

            }
        }

        return MakeInvitesRs.newBuilder().build();
    }

    /**
     * 获取军团荣誉数据
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPartyHonorRs getPartyHonor(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Camp camp = getCampInfo(roleId, player.lord.getCamp());
        refreshDaily(player);
        CampMember member = campDataManager.getCampMember(roleId);
        member.refreshData();

        GetPartyHonorRs.Builder builder = GetPartyHonorRs.newBuilder();
        builder.setMyVote(member.getJobVote());
        builder.setBuild(camp.getBuild());
        builder.setCityBattle(camp.getCityBattle());
        builder.setCampBattle(camp.getCampBattle());
        builder.addAllRewardIndex(member.getHonorRewardIndex());

        PartyHonorRank rank;
        Player rankPlayer;
        // 城战次数排行榜
        LinkedList<PartyHonorRank> rankList = camp.getCityRank();
        for (int i = 0; i < PartyConstant.PARTY_HONOR_RANK_LEN; i++) {
            if (i >= rankList.size()) {
                break;
            }
            rank = rankList.get(i);
            rankPlayer = playerDataManager.getPlayer(rank.getRoleId());
            if (rankPlayer != null) {
                builder.addCityRank(createPartyHonorRankPb(rank, i + 1, rankPlayer.lord.getNick()));
            }
        }
        rank = camp.getPartyHonorRank(PartyConstant.RANK_TYPE_CITY, roleId);
        if (null != rank) {
            rankPlayer = playerDataManager.getPlayer(rank.getRoleId());
            if (rankPlayer != null) {
                builder.addCityRank(createPartyHonorRankPb(rank, rank.getRank(), rankPlayer.lord.getNick()));
            }
        }
        // 阵营战次数排行榜
        rankList = camp.getCampRank();
        for (int i = 0; i < PartyConstant.PARTY_HONOR_RANK_LEN; i++) {
            if (i >= rankList.size()) {
                break;
            }
            rank = rankList.get(i);
            rankPlayer = playerDataManager.getPlayer(rank.getRoleId());
            if (rankPlayer != null) {
                builder.addCampRank(createPartyHonorRankPb(rank, i + 1, rankPlayer.lord.getNick()));
            }
        }
        rank = camp.getPartyHonorRank(PartyConstant.RANK_TYPE_CAMP, roleId);
        if (null != rank) {
            rankPlayer = playerDataManager.getPlayer(rank.getRoleId());
            if (rankPlayer != null) {
                builder.addCampRank(createPartyHonorRankPb(rank, rank.getRank(), rankPlayer.lord.getNick()));
            }
        }
        // 建设次数排行榜
        rankList = camp.getBuildRank();
        for (int i = 0; i < PartyConstant.PARTY_HONOR_RANK_LEN; i++) {
            if (i >= rankList.size()) {
                break;
            }
            rank = rankList.get(i);
            rankPlayer = playerDataManager.getPlayer(rank.getRoleId());
            if (rankPlayer != null) {
                builder.addBuildRank(createPartyHonorRankPb(rank, i + 1, rankPlayer.lord.getNick()));
            }
        }
        rank = camp.getPartyHonorRank(PartyConstant.RANK_TYPE_BUILD, roleId);
        if (null != rank) {
            rankPlayer = playerDataManager.getPlayer(rank.getRoleId());
            if (rankPlayer != null) {
                builder.addBuildRank(createPartyHonorRankPb(rank, rank.getRank(), rankPlayer.lord.getNick()));
            }
        }
        return builder.build();
    }

    private static com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank createPartyHonorRankPb(PartyHonorRank rank,
                                                                                           int order, String nick) {
        com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank.Builder builder = com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank
                .newBuilder();
        builder.setRank(order);
        builder.setName(nick);
        builder.setCount(rank.getCount());
        builder.setRoleId(rank.getRoleId());
        return builder.build();
    }

    /**
     * 检测荣耀礼包是否满足条件,如果满足就发送系统消息
     *
     * @param camp
     */
    public void checkHonorRewardAndSendSysChat(Camp camp) {
        for (int i = 0; i <= camp.chatHonorRewardState.length; i++) {
            int index = i + 1;// 礼包类型
            StaticPartyHonorGift gift = StaticPartyDataMgr.getHonorGift(index, camp.getPartyLv());
            if (gift == null) {
                continue;
            }
            if (gift.getBuild() <= camp.getBuild() && gift.getCityBattle() <= camp.getCityBattle()
                    && gift.getCampBattle() <= camp.getCampBattle()) {
                if (!camp.chatHonorRewardState[i]) {
                    // 满足条件,发送系统消息
                    chatDataManager.sendSysChat(ChatConst.CHAT_PARTY_HONOR_REWARD_HIT, camp.getCamp(), 0,
                            camp.getCamp(), index);
                    camp.chatHonorRewardState[i] = true;
                }
            }
        }
    }

    /**
     * 领取军团荣誉礼包
     *
     * @param roleId
     * @param index
     * @return
     * @throws MwException
     */
    public PartyHonorRewardRs partyHonorReward(long roleId, int index) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        // 检查是否可以领取
        StaticPartyHonorGift gift = StaticPartyDataMgr.getHonorGift(index, camp.getPartyLv());
        if (null == gift) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "未找到军团荣誉礼包配置信息, roleId:", roleId, ", index:", index);
        }

        if (gift.getBuild() > camp.getBuild() || gift.getCityBattle() > camp.getCityBattle()
                || gift.getCampBattle() > camp.getCampBattle()) {
            throw new MwException(GameError.PARTY_HONOR_GIFT_NOT_REACH.getCode(), "军团荣誉礼包的条件未达成, roleId:", roleId,
                    ", index:", index, ", build:", camp.getBuild(), ", cityBattle:", camp.getCityBattle(),
                    ", campBattle:", camp.getCampBattle());
        }

        CampMember member = campDataManager.getCampMember(roleId);
        member.refreshData();

        // 检查是否已经领取过
        if (member.getHonorRewardIndex().contains(index)) {
            throw new MwException(GameError.PARTY_HONOR_GIFT_REWARD.getCode(), "军团荣誉礼包已领取过, roleId:", roleId,
                    ", index:", index, ", reward:", member.getHonorRewardIndex());
        }

        // 记录玩家领取
        member.recordHonorGiftReward(index);

        // 发送奖励
        rewardDataManager.sendReward(player, gift.getRewardList(), AwardFrom.PARTY_HONOR_REAWRD);// 领取军团荣誉礼包

        PartyHonorRewardRs.Builder builder = PartyHonorRewardRs.newBuilder();
        builder.addAllRewardIndex(member.getHonorRewardIndex());
        return builder.build();
    }

    /**
     * 获取军团日志信息
     *
     * @param roleId
     * @param page
     * @return
     * @throws MwException
     */
    public GetPartyLogRs getPartyLog(long roleId, int page) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (page < 1 || page > Integer.MAX_VALUE) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "获取军团日志，参数错误, roleId:", roleId, ", page:", page);
        }

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        LinkedList<PartyLog> logList = camp.getLog();

        GetPartyLogRs.Builder builder = GetPartyLogRs.newBuilder();
        builder.setCurPage(page);
        builder.setTotalPage((int) Math.ceil(logList.size() * 1.0 / PartyConstant.PARTY_LOG_PAGE_NUM));
        if (logList.size() > 0) {
            int start = PartyConstant.PARTY_LOG_PAGE_NUM * (page - 1);
            int end = start + PartyConstant.PARTY_LOG_PAGE_NUM;
            if (end > logList.size()) {
                end = logList.size();
            }

            builder.addAllLog(logList.subList(start, end));
        }
        return builder.build();
    }

    /**
     * 获取军团官员信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPartyJobRs getPartyJob(long roleId, boolean isAppoint) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        CampMember member = campDataManager.getCampMember(roleId);
        member.refreshData();

        GetPartyJobRs.Builder builder = GetPartyJobRs.newBuilder();
        builder.setStatus(camp.getStatus());
        builder.setEndTime(camp.getEndTime());
        builder.setMyVote(member.getJobVote());
        builder.setCanvass(member.getCanvass());
        if (camp.isInEceltion()) {
            int count = 0;
            for (PartyElection election : camp.getElectionList()) {
                Player electionPlayer = playerDataManager.getPlayer(election.getRoleId());
                if (electionPlayer == null) continue;
                Lord lord = electionPlayer.lord;
                count++;
                builder.addElection(election.ser(lord.getNick(), lord.getLevel(), lord.getFight(), lord.getRanks()));
                if (count >= PartyConstant.PARTY_ELECT_NUM) {
                    break;// 只显示指定条数
                }
            }
        }
        // 如果是司令对任命官员进行重新排序
        if (player.lord.getJob() == PartyConstant.Job.KING && isAppoint) {

            List<Long> officer = new ArrayList<>();// 官员
            List<Official> noOfficer = new ArrayList<>();// 非官员
            for (Official official : camp.getOfficials()) {
                if (official.getJob() > 0) {
                    officer.add(official.getRoleId());
                } else {
                    noOfficer.add(official);
                }
            }
            int rankOfficCnt = officer.size();// 官员排数量
            // 移除非官员
            camp.getOfficials().removeAll(noOfficer);
            // 添加非官员
            for (Lord lord : rankDataManager.getComplexRank()) {
                if (rankOfficCnt >= PartyConstant.PARTY_ELECT_RANK_NUM) {
                    break;
                }
                if (camp.getCamp() != lord.getCamp()) {
                    continue;
                }
                if (lord.getLevel() < PartyConstant.PARTY_JOB_MIN_LV) {// 小于30级不让看到
                    continue;
                }
                long lordId = lord.getLordId();
                if (officer.contains(lordId)) { // 已经是官员跳过
                    continue;
                }
                camp.getOfficials().add(new Official(lordId, lord.getNick(), 0));
                rankOfficCnt++;
            }
            builder.setAppointFreeCnt(freeAppointFreeCnt(player));
        }
        Lord lord;
        for (Official official : camp.getOfficials()) {
            Player officPlayer = playerDataManager.getPlayer(official.getRoleId());
            if (officPlayer == null) continue;
            lord = officPlayer.lord;
            builder.addJob(official.build(lord.getLevel(), lord.getFight(), lord.getArea(), lord.getNick(),
                    lord.getPortrait(), lord.getRanks(), officPlayer.getDressUp().getCurPortraitFrame()));
        }

        return builder.build();
    }

    /**
     * 军团官员选举投票
     *
     * @param roleId
     * @param targetRoleId
     * @return
     * @throws MwException
     */
    public PartyVoteRs partyVote(long roleId, long targetRoleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        // 检查军团是否开启了选举
        if (!camp.isInEceltion()) {
            throw new MwException(GameError.PARTY_NOT_OPEN_ELECT.getCode(), "玩家投票，军团未开启投票, roleId:", roleId,
                    ", targetRoleId:", targetRoleId, ", status:", camp.getStatus());
        }

        // 检查被投票玩家是否存在
        Player target = playerDataManager.getPlayer(targetRoleId);
        if (null == target) {
            throw new MwException(GameError.PARTY_VOTE_TARGET_NOT_FOUND.getCode(), "玩家投票，被投票玩家不存在, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }

        // 检查被投票玩家是)否有资格
        PartyElection elect = camp.getElectionByRoleId(targetRoleId);
        if (null == elect) {
            throw new MwException(GameError.PARTY_VOTE_TARGET_ERR.getCode(), "玩家投票，被投票玩家未上榜, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }

        // 检查玩家是否有选票
        CampMember member = campDataManager.getCampMember(roleId);
        member.refreshData();
        int vote = member.getJobVote();
        if (member.getJobVote() <= 0) {
            throw new MwException(GameError.PARTY_NOT_VOTE.getCode(), "玩家投票，没有选票, roleId:", roleId, ", targetRoleId:",
                    targetRoleId, ", vote:", vote);
        }
        // 检测玩家等级
        if (player.lord.getLevel() < PartyConstant.PARTY_VOTE_LV) {
            throw new MwException(GameError.PARTY_VOTE_LV.getCode(), "玩家投票，等级不满足, roleId:", roleId, ", targetRoleId:",
                    targetRoleId, ", vote:", vote);
        }

        // 扣除选票
        member.setJobVote(0);

        // 更新选举信息
        camp.addElectVote(targetRoleId, vote);

        // 返回消息
        PartyVoteRs.Builder builder = PartyVoteRs.newBuilder();
        int count = 0;
        for (PartyElection election : camp.getElectionList()) {
            count++;
            Player electionPlayer = playerDataManager.getPlayer(election.getRoleId());
            if (electionPlayer == null) continue;
            Lord lord = electionPlayer.lord;
            builder.addElection(election.ser(lord.getNick(), lord.getLevel(), lord.getFight(), lord.getRanks()));
            if (count >= PartyConstant.PARTY_ELECT_NUM) {
                break;// 只显示指定条数
            }
        }
        return builder.build();
    }

    /**
     * 军团官员选举拉票
     *
     * @param roleId
     * @param targetRoleId
     * @return
     * @throws MwException
     */
    public PartyCanvassRs partyCanvass(long roleId, long targetRoleId) throws MwException {// 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        // 检查军团是否开启了选举
        if (!camp.isInEceltion()) {
            throw new MwException(GameError.PARTY_NOT_OPEN_ELECT.getCode(), "玩家拉票，军团未开启投票, roleId:", roleId,
                    ", targetRoleId:", targetRoleId, ", status:", camp.getStatus());
        }

        // 检查被投票玩家是否存在
        Player target = playerDataManager.getPlayer(targetRoleId);
        if (null == target) {
            throw new MwException(GameError.PARTY_VOTE_TARGET_NOT_FOUND.getCode(), "玩家拉票，被投票玩家不存在, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }

        // 检查被投票玩家是否有资格
        PartyElection elect = camp.getElectionByRoleId(targetRoleId);
        if (null == elect) {
            throw new MwException(GameError.PARTY_VOTE_TARGET_ERR.getCode(), "玩家拉票，被投票玩家未上榜, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }

        // 检查玩家是否有选票
        CampMember member = campDataManager.getCampMember(roleId);
        member.refreshData();
        int vote = member.getJobVote();
        if (member.getJobVote() > 0) {
            throw new MwException(GameError.PARTY_CANVASS_HAVE_VOTE.getCode(), "玩家拉票，还有选票, roleId:", roleId,
                    ", targetRoleId:", targetRoleId, ", vote:", vote);
        }

        // 检查并扣除相应消耗
        int canvass = member.getCanvass() + 1;
        int need = canvass * PartyConstant.PARTY_CANVASS_GOLD;
        if (need >= 100) {
            need = 100;
        }
        // rewardDataManager.subGold(player, need, AwardFrom.PARTY_CANVASS, "军团官员选举拉票");
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, need,
                AwardFrom.PARTY_CANVASS);

        // 记录玩家拉票次数
        member.setCanvass(canvass);

        // 更新选举信息
        camp.addElectVote(targetRoleId, 1);

        // 返回消息
        PartyCanvassRs.Builder builder = PartyCanvassRs.newBuilder();
        int count = 0;
        for (PartyElection election : camp.getElectionList()) {
            count++;
            Player electionPlayer = playerDataManager.getPlayer(election.getRoleId());
            if (electionPlayer == null) continue;
            Lord lord = electionPlayer.lord;
            builder.addElection(election.ser(lord.getNick(), lord.getLevel(), lord.getFight(), lord.getRanks()));
            if (count >= PartyConstant.PARTY_ELECT_NUM) {
                break;// 只显示指定条数
            }
        }
        return builder.build();
    }

    /**
     * 军团官员任命
     *
     * @param roleId
     * @param targetRoleId
     * @param job
     * @return
     * @throws MwException
     */
    public PartyAppointRs partyAppoint(long roleId, long targetRoleId, int job) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        // 检查玩家是否有权限
        int myJob = player.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_COMMAND_APPOINT;
        if (!StaticPartyDataMgr.jobHavePrivilege(myJob, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "官员任命，没有这个特权, roleId:", roleId, ", job:", myJob,
                    ", privilege:", privilege);
        }

        // 检查是否操作的玩家自己
        if (roleId == targetRoleId) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "官员任命，不能操作自己, roleId:", roleId, ", targetRoleId:",
                    targetRoleId);
        }

        // 检查被投票玩家是否存在
        Player target = playerDataManager.getPlayer(targetRoleId);
        if (null == target) {
            throw new MwException(GameError.PARTY_VOTE_TARGET_NOT_FOUND.getCode(), "官员任命，被操作玩家不存在, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }
        // 不是本阵营不能任职
        if (target.lord.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.PARTY_APPOINT_JOB_ERR.getCode(), "不是本阵营不能任职, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }

        if (target.lord.getLevel() < PartyConstant.PARTY_JOB_MIN_LV) {
            throw new MwException(GameError.PARTY_VOTE_TARGET_ERR.getCode(), "官员任命，被操作玩家未上榜, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }
        // 检查被操作玩家是否有资格
        // PartyElection elect = camp.getOfficialByRoleId(targetRoleId);
        // 检查被操作玩家是否已是该职位
        Official official = camp.getOfficialByRoleId(targetRoleId);
        if (null == official) {
            throw new MwException(GameError.PARTY_VOTE_TARGET_ERR.getCode(), "官员任命，被操作玩家未上榜, roleId:", roleId,
                    ", targetRoleId:", targetRoleId);
        }

        if (job == PartyConstant.PARTY_JOB_DISMISS) {
            // if (null == official) {
            // throw new MwException(GameError.ROLE_NO_PARTY_JOB.getCode(), "官员撤职，被操作玩家没有职务, roleId:", roleId,
            // ", targetRoleId:", targetRoleId);
            // }
            if (official.getJob() != PartyConstant.Job.GENERAL) {
                throw new MwException(GameError.ROLE_NOT_GENERAL.getCode(), "玩家不是军长，不能撤职, roleId:", roleId,
                        ", targetRoleId:", targetRoleId, ", targetJob:", official.getJob());
            }

            // 移除官员信息
            official.setJob(0);
            target.lord.clearJob();
        } else {
            // 检查职务是否正确
            if (job != PartyConstant.Job.GENERAL) {
                throw new MwException(GameError.PARTY_APPOINT_JOB_ERR.getCode(), "官员任命，官职不正确, roleId:", roleId,
                        ", targetRoleId:", targetRoleId, ", job:", job);
            }

            if (null != official && official.getJob() > 0) {
                throw new MwException(GameError.ROLE_HAVE_PARTY_JOB.getCode(), "玩家已是官员，不能任命, roleId:", roleId,
                        ", targetRoleId:", targetRoleId, ", targetJob:", official.getJob());
            }

            int generalNum = camp.getGeneralNum();
            if (generalNum >= PartyConstant.MAX_GENERAL) {
                throw new MwException(GameError.GENERAL_LIMIT.getCode(), "军长职务人员已达上限, roleId:", roleId,
                        ", targetRoleId:", targetRoleId, ", generalNum:", generalNum, ", limit:",
                        PartyConstant.MAX_GENERAL);
            }
            if (freeAppointFreeCnt(player) > 0) {// 有免费次数
                player.appointFreeTime = TimeHelper.getCurrentDay();
            } else {
                // 检测金币是否足够
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                        Constant.APPOINT_COST, AwardFrom.APPOINT_COST);
            }

            official.setJob(job);
            target.lord.setJob(job);

            // 记录军团日志
            PartyLogHelper.addPartyLog(target.lord.getCamp(), PartyConstant.LOG_PROMOTE_RANKS, target.lord.getNick(),
                    job);
        }

        // 通知玩家职务变更
        if (target.isLogin) {
            SyncJobChange(target, job);
        }

        PartyAppointRs.Builder builder = PartyAppointRs.newBuilder();
        Lord lord;
        for (Official official2 : camp.getOfficials()) {
            Player officPlayer = playerDataManager.getPlayer(official2.getRoleId());
            if (Objects.isNull(officPlayer)) {
                continue;
            }
            lord = officPlayer.lord;
            builder.addJob(official2.build(lord.getLevel(), lord.getFight(), lord.getArea(), lord.getNick(),
                    lord.getPortrait(), lord.getRanks(), officPlayer.getDressUp().getCurPortraitFrame()));
        }
        return builder.build();
    }

    /**
     * 获取司令任职的免费次数
     *
     * @param player
     * @return
     */
    private int freeAppointFreeCnt(Player player) {
        if (player.lord.getJob() == PartyConstant.Job.KING) {
            int nowDay = TimeHelper.getCurrentDay();
            if (nowDay != player.appointFreeTime) {
                return 1;
            }
        }
        return 0;
    }

    public void SyncJobChange(Player player, int job) {
        SyncJobChangeRs.Builder builder = SyncJobChangeRs.newBuilder();
        builder.setJob(job);
        Base.Builder msg = PbHelper.createSynBase(SyncJobChangeRs.EXT_FIELD_NUMBER, SyncJobChangeRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 开启军团官员功能
     */
    public void openPartyJobDelay() {
        // 开启官员选举,Day+1的中午12点开启
        Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
        int firstOpenTime = TimeHelper.getSomeDayAfter(1, 12, 0, 0);
        for (Camp camp : partyMap.values()) {
            camp.setFirstOpenJobTime(firstOpenTime);
        }
    }

    /**
     * 开启军团选举
     *
     * @param camp
     */
    public void openPartyElect(Camp camp) {
        if (camp.isInEceltion()) {
            return;
        }
        // 开启状态
        camp.openElect();
        // 清空上次选举数据
        camp.getElectionList().clear();
        // 初始票
        // 从军团排行榜获取
        List<Lord> complexRank = rankDataManager.getComplexRank().stream()
                .filter(lord -> lord.getCamp() == camp.getCamp()).collect(Collectors.toList());

        if (CheckNull.isEmpty(complexRank)) {
            return;
        }
        // CampMember member;
        for (Lord lord : complexRank) {
            // member = partyDataManager.getCampMember(lord.getLordId());
            // member.clearLastEceltionData();
            // member.setJobVote(2);
            // 初始化选举排行榜
            camp.addEclection(lord.getLordId(), lord.getNick(), lord.getLevel(), lord.getFight(), lord.getRanks());
        }

        // 给当前正营发邮件
        int now = TimeHelper.getCurrentSecond();
        LogUtil.debug("开启军团选举,给【", camp.getCamp(), "】正营的我玩家发邮件");
        playerDataManager.getPlayerByCamp(camp.getCamp())// Java8 特性
                .forEach((roleId, player) -> mailDataManager.sendNormalMail(player, MailConstant.MOLD_PARTY_JOB_OPEN,
                        now));
    }

    /**
     * 结算所有军团排行数据结算
     */
    public void honorRankSettleAll() {
        LogUtil.debug("===============军团荣誉结算开始执行===============");
        Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
        for (Camp camp : partyMap.values()) {
            honorRankSettle(camp);
            // System.out.println(Thread.currentThread().getId() + " ###########结算: 阵营:" + camp.getCamp() + " 时间:"
            // + DateHelper.displayDateTime());
        }
        LogUtil.debug("===============军团荣誉结算执行结束===============");
    }

    /**
     * 军团荣誉排行榜选票结算
     *
     * @param camp
     */
    public void honorRankSettle(Camp camp) {
        // 清空玩家上次选举投票数据
        ConcurrentHashMap<Long, Player> players = playerDataManager.getPlayerByCamp(camp.getCamp());
        if (CheckNull.isEmpty(players)) {
            // 其他军团无人的时候
            return;
        }
        CampMember member;
        // Player player;
        for (Entry<Long, Player> entry : players.entrySet()) {
            long roleId = entry.getKey();
            member = campDataManager.getCampMember(roleId);
            // 所有玩家+6票,在排行榜里的玩家
            member.addVote(StaticPartyDataMgr.getAllHonorRankOtherCnt());

            // 初始化选举排行榜
            // player = entry.getValue();
            // camp.addEclection(player.roleId, player.lord.getNick(), player.lord.getLevel(), player.lord.getFight(),
            // player.lord.getRanks());
        }

        // 城战排行榜结算
        addElectVoteByRankList(camp.getCityRank());
        // 阵营战排行榜结算
        addElectVoteByRankList(camp.getCampRank());
        // 建设排行榜结算
        addElectVoteByRankList(camp.getBuildRank());

        // 清空排行榜
        camp.getCityRank().clear();
        camp.getCampRank().clear();
        camp.getBuildRank().clear();
    }

    /**
     * 根据玩家在排行榜中的排名，增加玩家选票
     *
     * @param rankList
     */
    private void addElectVoteByRankList(LinkedList<PartyHonorRank> rankList) {
        int order = 0;
        CampMember member;
        StaticPartyHonorRank sphr;
        for (PartyHonorRank rank : rankList) {
            order++;
            sphr = StaticPartyDataMgr.getHonorRank(rank.getRankType(), order);
            member = campDataManager.getCampMember(rank.getRoleId());
            member.addVote(sphr.getReward() - StaticPartyDataMgr.getHonorRankOtherCnt(rank.getRankType()));
        }
    }

    /**
     * 军团官员选举结束，结算
     *
     * @param camp
     */
    public void partyEclectionEnd(Camp camp) {
        if (null == camp) {
            return;
        }
        // 设置军团状态
        int endTime = TimeHelper.getSomeDayAfter(PartyConstant.PARTY_JOB_DATE, PartyConstant.PARTY_JOB_END_HOUR, 0, 0);
        camp.closeElect(endTime);

        Set<Player> needNoticePlayer = new HashSet<>();// 需要通知玩家官职变更
        // 清空上次官员数据
        Player player;
        for (Official official : camp.getOfficials()) {
            player = playerDataManager.getPlayer(official.getRoleId());
            if (player == null) {
                continue;
            }
            player.lord.clearJob();
            LogUtil.debug("清除官员信息, roleId:", player.lord.getLordId(), ", nick:", player.lord.getNick(), ", camp:",
                    player.lord.getCamp());
            needNoticePlayer.add(player);
        }
        camp.getOfficials().clear();
        LogUtil.debug("=============选举结束,清除所有官员信息, camp:", camp.getCamp());
        // 结算并生成官员数据
        int job;
        int rank = 0;
        Official official;
        for (PartyElection elect : camp.getElectionList()) {
            player = playerDataManager.getPlayer(elect.getRoleId());
            if (player == null) {
                continue;
            }
            rank++;
            job = PartyConstant.getJobByRank(rank);
            official = new Official(elect.getRoleId(), elect.getNick(), job);
            camp.getOfficials().add(official);
            player.lord.setJob(job);
            elect.cleanVote();// 清空得票数
            LogUtil.debug("设置官员信息, roleId:", player.lord.getLordId(), ", nick:", player.lord.getNick(), ", camp:",
                    player.lord.getCamp(), " job:", player.lord.getJob());
            // 发竞选成功聊天消息
            if (rank == 1) {
                chatDataManager.sendSysChat(ChatConst.CHAT_KING_ELECTED, player.lord.getCamp(), 0,
                        player.lord.getNick(), player.lord.getCamp());
            } else if (rank == 2) {
                chatDataManager.sendSysChat(ChatConst.CHAT_PRIME_ELECTED, player.lord.getCamp(), 0,
                        player.lord.getNick());
            } else if (rank == 3) {
                chatDataManager.sendSysChat(ChatConst.CHAT_ADVISER_ELECTED, player.lord.getCamp(), 0,
                        player.lord.getNick());
            }
            if (job > 0) {
                needNoticePlayer.add(player);
            }
        }
        LogUtil.debug("=============选举结束,设置完所有的官员, camp:", camp.getCamp());
        // 清空得拥有票数,和拥有票数
        ConcurrentHashMap<Long, Player> players = playerDataManager.getPlayerByCamp(camp.getCamp());
        if (CheckNull.isEmpty(players)) {
            // 军团无人的时候
            return;
        }
        CampMember member;
        for (Entry<Long, Player> entry : players.entrySet()) {
            member = campDataManager.getCampMember(entry.getKey());
            member.clearLastEceltionData();
        }
        // 需要通知玩家官职变更
        for (Player p : needNoticePlayer) {
            SyncJobChange(p, p.lord.getJob());
        }
    }

    /**
     * Gm修复玩家官职
     */
    public void gmFixPartyJob() {
        Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
        for (Camp camp : partyMap.values()) {
            LogUtil.debug("=============开始修复阵营的官职 camp:", camp.getCamp());
            for (Official official : camp.getOfficials()) {
                Player officPlayer = playerDataManager.getPlayer(official.getRoleId());
                if (officPlayer == null) {
                    LogUtil.debug("=====error===未能修复的数据  roleId:", official.getRoleId(), ", camp:", camp.getCamp());
                    continue;
                }
                officPlayer.lord.setJob(official.getJob());
                LogUtil.debug("已经修复阵营的官职数据 roleId:", officPlayer.roleId, ", nick:", officPlayer.lord.getNick(),
                        ", job:", officPlayer.lord.getJob());
            }
            LogUtil.debug("=============完成修复阵营的官职 camp:", camp.getCamp());
        }
    }

    /**
     * GM指令开启军团官员选举
     *
     * @param camp
     */
    public void gmOpenElect(int camp) {
        Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
        for (Camp party : partyMap.values()) {
            // 结算玩家选票数
            // honorRankSettle(camp);
            // 开启军团选举
            openPartyElect(party);
        }
    }

    /**
     * GM指令关闭官员选举
     *
     * @param camp
     */
    public void gmCloseElect(int camp) {
        Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
        for (Camp party : partyMap.values()) {
            if (!party.isInEceltion()) {
                return;
            }
            // 结束选举
            partyEclectionEnd(party);
        }
    }

    /**
     * GM清空官员选举
     */
    public void clearPartyVote() {
        Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
        for (Camp camp : partyMap.values()) {
            camp.setFirstOpenJobTime(Integer.MAX_VALUE);
            camp.setStatus(PartyConstant.PARTY_STATUS_INIT);
            // 清除官员信息
            camp.getOfficials().forEach(official -> {
                long roleId = official.getRoleId();
                Player player = playerDataManager.getPlayer(roleId);
                if (player != null) {
                    player.lord.setJob(0);
                }
            });
            camp.getOfficials().clear();
            camp.getElectionList().clear();
        }
    }

    /**
     * 军团相关定时任务
     */
    public void partyTimeLogic() {
        Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
        int now = TimeHelper.getCurrentSecond();
        for (Camp camp : partyMap.values()) {
            try {
                if (camp.isInEceltion() && camp.getEndTime() <= now) {
                    // 结束选举
                    partyEclectionEnd(camp);
                } else if (camp.isInOffice()) {
                    // int honorSettleTime = camp.getHonorSettleTime();
                    // if (honorSettleTime > 0 && honorSettleTime <= now) {
                    // // 开始荣誉排行榜结算
                    // honorRankSettle(camp);
                    // }

                    if (camp.getEndTime() <= now) {
                        // 开启军团选举
                        openPartyElect(camp);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e, "军团定时任务失败, camp:", camp);
            }

            // 延迟首次开启军团任务
            if (camp.getStatus() == PartyConstant.PARTY_STATUS_INIT
                    && worldScheduleService.getCurrentSchduleId() >= TaskType.WORLD_BOSS_TASK_ID_1
                    && now >= camp.getFirstOpenJobTime()) {
                // // 结算玩家选票数
                // honorRankSettle(camp);
                // 开启军团选举
                openPartyElect(camp);
            }

            // 过期的补给移除掉
            List<PartySupply> removeList = camp.getPartySupplies().stream().filter(ps -> ps.getEndTime() < now)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(removeList)) {
                LogUtil.debug("军团补给过期移除掉, list: ",
                        removeList.stream().map(PartySupply::toString).collect(Collectors.joining(",")));
                camp.getPartySupplies().removeAll(removeList);
            }

        }
    }

    private void refreshDaily(Player player) throws MwException {
        int nowDay = TimeHelper.getCurrentDay();
        Camp camp = getCampInfo(player.roleId, player.lord.getCamp());
        int lastDay = TimeHelper.getDay(camp.getRefreshTime());
        if (nowDay != lastDay) {
            camp.setBuild(0);
            camp.setCityBattle(0);
            camp.setCampBattle(0);
            camp.setRefreshTime(TimeHelper.getCurrentSecond());
            // 重置礼包消息消息状态
            for (int i = 0; i < camp.chatHonorRewardState.length; i++) {
                camp.chatHonorRewardState[i] = false;
            }

        }
    }

    /**
     * 给这个阵营加上同点兵统领经验,如果经验满了就进行升级
     *
     * @param camp
     * @param addExp
     */
    public void addCabinetLeadExp(int camp, long addExp) {
        Camp party = campDataManager.getParty(camp);
        if (null == party) return;
        int maxLv = StaticBuildingDataMgr.getCabinetMaxLv();
        int curLv = party.getCabinetLeadLv();

        // 满级之不累计经验
        while (curLv < maxLv) {
            long need = StaticBuildingDataMgr.getCabinetLvByLv(curLv).getNeedExp();
            long curExp = party.getCabinetLeadExp();
            if (curExp + addExp >= need) {// 升级
                addExp -= (need - curExp);
                party.setCabinetLeadLv(curLv + 1);// 升一级
                party.setCabinetLeadExp(0);// 当前经验清0
            } else {
                party.setCabinetLeadExp(curExp + addExp);
                addExp = 0;
                break;
            }
            curLv = party.getCabinetLeadLv();
        }
    }

    /**
     * 补给大厅的信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb3.SupplyHallRs supplyHall(long roleId) throws MwException {

        GamePb3.SupplyHallRs.Builder builder = GamePb3.SupplyHallRs.newBuilder();

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 玩家的补给记录
        SupplyRecord record = player.getSupplyRecord();
        Camp camp = getCampInfo(roleId, player.lord.getCamp());
        PartySuperSupply supperSupply = camp.getPartySuperSupply();
        // 可以领取的超级补给箱数量
        int awardCnt = record.getAwardLv(supperSupply.getLv());
        builder.setAward(awardCnt);
        builder.setSuperSupply(PbHelper.createSuperSupplyPb(supperSupply));
        List<PartySupply> partySupplies = camp.getPartySupplies(player.roleId);
        if (!CheckNull.isEmpty(partySupplies)) {
            partySupplies.forEach(ps -> builder.addSupplyInfo(PbHelper.createPartySupplyPb(ps, false)));
        }
        return builder.build();
    }

    /**
     * 领取军团补给奖励
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb3.SupplyRewardRs supplyReward(long roleId, GamePb3.SupplyRewardRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 校验客户端参数
        checkSupplyRewardParam(req);

        Camp camp = getCampInfo(roleId, player.lord.getCamp());

        SupplyRewardRs.Builder builder = SupplyRewardRs.newBuilder();

        List<List<Integer>> awardList = null;
        int type = req.getType();
        // 普通补给箱
        if (type == 0) {
            int key = req.getKey();
            PartySupply partySupply = camp.getPartySupply(key);
            if (CheckNull.isNull(partySupply)) {
                throw new MwException(GameError.PARTY_SUPPLY_REWARD_PARAM_ERROR.getCode(),
                        "领取普通补给奖励, partySupply过期了, 或者找不到该补给箱 , key:", key);
            }
            if (!partySupply.canAward(roleId)) {
                throw new MwException(GameError.PARTY_SUPPLY_CANT_AWARD.getCode(), "领取普通补给奖励, 补给箱不可领取");
            }
            StaticPartySupply sPartySupply = StaticPartyDataMgr.getPartySupply(partySupply.getId());
            if (CheckNull.isNull(sPartySupply)) {
                throw new MwException(GameError.PARTY_SUPPLY_CONFIG_ERROR.getCode(), "领取普通补给奖励, 没找到补给箱的配置信息 , id:",
                        partySupply.getId());
            }
            if (!CheckNull.isEmpty(sPartySupply.getAward())) {
                awardList = sPartySupply.getAward();
            }
            partySupply.receiveAward(roleId);
        } else if (type == 1) {
            PartySuperSupply partySuperSupply = camp.getPartySuperSupply();
            int lv = partySuperSupply.getLv();
            if (lv < StaticPartyDataMgr.getMinSuperSupplyLv()) {
                throw new MwException(GameError.PARTY_SUPER_SUPPLY_CANT_AWARD.getCode(),
                        "领取超级补给奖励, 不可领取, 超级补给箱等级不足, lv:", lv);
            }

            SupplyRecord supplyRecord = player.getSupplyRecord();
            int awardLv = supplyRecord.getMinAwardLv(lv);
            if (awardLv <= 0) {
                throw new MwException(GameError.PARTY_SUPER_SUPPLY_CANT_AWARD.getCode(), "领取超级补给奖励, 没有可领取的超级补给箱");
            }
            StaticPartySuperSupply sPartySuperSupply = StaticPartyDataMgr.getPartySuperSupply(lv);
            if (CheckNull.isNull(sPartySuperSupply)) {
                throw new MwException(GameError.PARTY_SUPER_SUPPLY_CONFIG_ERROR.getCode(), "没找到超级补给箱的配置信息, lv:", lv);
            }
            if (!CheckNull.isEmpty(sPartySuperSupply.getAward())) {
                awardList = sPartySuperSupply.getAward();
            }
            supplyRecord.receiveAward(awardLv);
        } else if (type == 2) {
            // 一键领取
            List<Integer> oneKeyAll = PartyConstant.PARTY_SUPPLY_ONE_KEY_ALL;
            List<PartySupply> partySupplies = camp.getPartySupplies(roleId);
            if (!CheckNull.isEmpty(partySupplies)) {
                partySupplies.stream().forEach(ps -> {
                    StaticPartySupply sPartySupply = StaticPartyDataMgr.getPartySupply(ps.getId());
                    if (CheckNull.isNull(sPartySupply)) {
                        return;
                    }
                    if (oneKeyAll.contains(sPartySupply.getType())) {
                        builder.addAllAward(addSupplyReward(player, sPartySupply.getAward(), type));
                        builder.addKey(ps.getKey());
                        ps.receiveAward(roleId);
                    }
                });
            }
        }

        builder.addAllAward(addSupplyReward(player, awardList, type));
        return builder.build();
    }

    /**
     * 添加补给箱奖励
     *
     * @param player
     * @param awardList
     * @param type
     * @return
     */
    public List<CommonPb.Award> addSupplyReward(Player player, List<List<Integer>> awardList, int type) {
        List<CommonPb.Award> awards = new ArrayList<>();
        if (!CheckNull.isEmpty(awardList)) {
            for (List<Integer> list : awardList) {
                if (!CheckNull.isEmpty(list)) {
                    Optional<CommonPb.Award> award = Optional
                            .ofNullable(rewardDataManager.addAwardSignle(player, list, type == 0 || type == 2
                                    ? AwardFrom.PARTY_SUPPLY_AWARD : AwardFrom.PARTY_SUPER_SUPPLY_AWARD));
                    if (award.isPresent()) {
                        awards.add(award.get());
                    }
                }
            }
        }
        return awards;
    }

    /**
     * 检测领取军团补给参数
     *
     * @param req
     * @throws MwException
     */
    private void checkSupplyRewardParam(SupplyRewardRq req) throws MwException {
        int type = req.getType();
        if (type != 0 && type != 1 && type != 2) {
            throw new MwException(GameError.PARTY_SUPPLY_REWARD_PARAM_ERROR.getCode(), "领取军团补给参数错误, type:", type);
        }
        if (type == 0 && !req.hasKey()) {
            throw new MwException(GameError.PARTY_SUPPLY_REWARD_PARAM_ERROR.getCode(), "领取军团补给参数错误, 普通补给箱没有传keyId");
        }
        if (type == 2 && req.hasKey()) {
            throw new MwException(GameError.PARTY_SUPPLY_REWARD_PARAM_ERROR.getCode(),
                    "领取军团补给参数错误, 一件领取不需要传keyId, keyId:", req.getKey());
        }
    }

    /**
     * 添加并且检测军团补给
     *
     * @param player
     * @param supplyType
     * @param param
     */
    public void addAndCheckPartySupply(Player player, int supplyType, int... param) {
        // 功能开放的判断
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_PARTY)
                && supplyType != PartyConstant.SupplyType.PAY_GOLD) {
            return;
        }
        // 功能开放的判断
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_PARTY_SUPPLY)) {
            return;
        }
        // 指定补给控制产出数量(每个阵营上限不共享)
        if (PartyConstant.PARTY_SUPPLY_MAX_CNT_CONF.containsKey(supplyType)) {
            Map<Integer, Integer> status = globalDataManager.getGameGlobal()
                    .getMixtureDataById(GlobalConstant.PARTY_SUPPLY_CONQUER_CITY_CAMP, player.lord.getCamp());
            if (CheckNull.isNull(status)) {
                return;
            }
            int cnt = status.getOrDefault(supplyType, 0);
            int maxCnt = PartyConstant.PARTY_SUPPLY_MAX_CNT_CONF.getOrDefault(supplyType, 0);
            if (cnt >= maxCnt) {
                return;
            } else {
                status.put(supplyType, cnt + 1);
                globalDataManager.getGameGlobal()
                        .setMixtureData(GlobalConstant.PARTY_SUPPLY_CONQUER_CITY_CAMP + player.lord.getCamp(), status);
            }
        }
        PartySupplyHandler handler = partySupplyHandlerMap.get(supplyType);
        if (!CheckNull.isNull(handler)) {
            // 注意: 这里的返回顺序是, 有次数的优先处理, 为了有具体次数, 又有每次的触发条件的补给
            List<StaticPartySupply> partySupplies = handler.execute(player, supplyType, param);
            if (CheckNull.isEmpty(partySupplies)) {
                return;
            }
            for (StaticPartySupply sPartySupply : partySupplies) {
                // 找到对应的补给配置
                if (!CheckNull.isNull(sPartySupply)) {
                    SupplyRecord supplyRecord = null;
                    try {
                        int camp = player.lord.getCamp();
                        Camp party = getCampInfo(player.roleId, camp);
                        if (sPartySupply.actionEverOne()) {
                            // 作用于每个玩家
                            supplyRecord = player.getSupplyRecord();
                        } else {
                            // 作用于整个阵营
                            supplyRecord = party.getSupplyRecord();
                        }
                        if (!CheckNull.isNull(supplyRecord) && checkSupplyActionCnt(sPartySupply, supplyRecord)) {
                            // 补给配置正常, 并且有作用次数
                            int now = TimeHelper.getCurrentSecond();
                            supplyRecord.recordSupply(sPartySupply.getId());
                            // 超级补给增加能量
                            PartySuperSupply superSupply = party.getPartySuperSupply();
                            boolean lvUP = superSupply.addEnergy(sPartySupply.getEnergy());
                            // 新增军团补给箱
                            PartySupply partySupply = new PartySupply(now + PartyConstant.SUPPLY_CONTINUE_TIME,
                                    sPartySupply.getId(), filterCanRecvPlayer(camp));
                            party.getPartySupplies().add(partySupply);
                            // 同步军团补给信息
                            syncPartySupply(partySupply, superSupply, player);
                            // 如果是世界进程第一阶段产生的军团补给,更新军团补给积分排行
/*                            worldScheduleRankService.addOneWorldScheduleRankData(camp, player.lord.getArea(),
                                    sPartySupply.getEnergy());*/
                        }
                    } catch (MwException e) {
                        LogUtil.error(e);
                        return;
                    }
                }
            }

        }
    }

    /**
     * 记录阵营玩家金币消耗
     *
     * @param player
     * @param sub
     */
    public void receiveCostGold(Player player, int sub) {
        try {
            // 功能开放的判断
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_PARTY_SUPPLY)) {
                return;
            }
            Camp camp = getCampInfo(player.roleId, player.lord.getCamp());
            PartySuperSupply superSupply = camp.getPartySuperSupply();
            // 计算消耗金币获得的能量
            if (!CheckNull.isNull(superSupply) && superSupply.addCostGold(sub)) {
                // 同步超级补给箱升级
                syncPartySupply(null, superSupply, player);
            }
        } catch (MwException e) {
            LogUtil.error(e);
        }
    }

    /**
     * 同步军团补给信息
     *
     * @param partySupply
     * @param supperSupply
     * @param player
     */
    public void syncPartySupply(PartySupply partySupply, PartySuperSupply supperSupply, Player player) {
        List<Player> players = new ArrayList<>();
        SyncPartySupplyRs.Builder builder = SyncPartySupplyRs.newBuilder();
        if (!CheckNull.isNull(partySupply)) {
            players = partySupply.getAwardStatus().keySet().stream().map(roleId -> playerDataManager.getPlayer(roleId))
                    .collect(Collectors.toList());
            builder.setSupplyInfo(PbHelper.createPartySupplyPb(partySupply, false));
        }
        if (!CheckNull.isNull(supperSupply)) {
            builder.setSuperSupply(PbHelper.createSuperSupplyPb(supperSupply));
            players = filterCanRecvPlayer(player.lord.getCamp()).stream()
                    .map(roleId -> playerDataManager.getPlayer(roleId)).collect(Collectors.toList());
        }
        Base msg = PbHelper.createSynBase(SyncPartySupplyRs.EXT_FIELD_NUMBER, SyncPartySupplyRs.ext, builder.build())
                .build();
        players.stream().filter(p -> p.ctx != null).forEach(p -> {
            MsgDataManager.getIns().add(new Msg(p.ctx, msg, p.roleId));
        });
    }

    /**
     * 过滤可以领取补给的玩家Id
     *
     * @param camp
     * @return 玩家Id
     */
    private List<Long> filterCanRecvPlayer(int camp) {
        return playerDataManager.getPlayerByCamp(camp).values().stream()
                .filter(p -> StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.FUNC_ID_PARTY))
                .map(p -> p.roleId).collect(Collectors.toList());
    }

    /**
     * 检测补给的作用次数
     *
     * @param sPartySupply
     * @param supplyRecord
     * @return
     */
    private boolean checkSupplyActionCnt(StaticPartySupply sPartySupply, SupplyRecord supplyRecord) {
        boolean canAction = false;
        if (sPartySupply.actionEverTime()) {
            // 作用于每一次
            canAction = true;
        } else {
            // 具体次数
            int count = supplyRecord.supplyStatusByType(sPartySupply.getId());
            if (count < sPartySupply.getCount()) {
                canAction = true;
            }
        }
        return canAction;
    }

    /**
     * 对应的补给类型, 不同的处理handler
     */
    private Map<Integer, PartySupplyHandler> partySupplyHandlerMap = new HashMap<>();

    {
        registerSupplyHandler(PartyConstant.SupplyType.KILL_BANDIT, this::killBanditHandler);
        registerSupplyHandler(PartyConstant.SupplyType.KILL_GESTAPO, this::killGestpaoHandler);
        registerSupplyHandler(PartyConstant.SupplyType.CONQUER_CITY, this::conquerCityHandler);
        registerSupplyHandler(PartyConstant.SupplyType.FIRST_CONQUER_CITY, this::conquerCityHandler);
        registerSupplyHandler(PartyConstant.SupplyType.PAY_GOLD, this::payGoldHandler);
        registerSupplyHandler(PartyConstant.SupplyType.JOIN_BERLIN_WAR, this::joinBerlinWarHandler);
    }

    /**
     * 军团补给处理
     */
    private interface PartySupplyHandler {

        List<StaticPartySupply> execute(Player player, int supplyType, int... param);

    }

    /**
     * 注册补给的Hander
     *
     * @param supplyType
     * @param handler
     */
    private void registerSupplyHandler(int supplyType, PartySupplyHandler handler) {
        partySupplyHandlerMap.put(supplyType, handler);
    }

    /**
     * 击杀匪军处理
     *
     * @param player
     * @param supplyType
     * @param param
     * @return
     */
    private List<StaticPartySupply> killBanditHandler(Player player, int supplyType, int... param) {
        if (param.length < 1) {
            LogUtil.error("击杀匪军检测补给箱的参数个数错误, Len:", param.length);
            return null;
        }
        int lv = param[0];
        List<StaticPartySupply> supplyList = StaticPartyDataMgr.getPartySupplyByType(supplyType);
        if (!CheckNull.isEmpty(supplyList)) {
            return supplyList.stream().filter(ps -> ps.getParam().get(0) == lv)
                    .sorted(Comparator.comparingInt(StaticPartySupply::getCount).reversed())
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 击杀盖世太保处理
     *
     * @param player
     * @param supplyType
     * @param param
     * @return
     */
    private List<StaticPartySupply> killGestpaoHandler(Player player, int supplyType, int... param) {
        if (param.length < 1) {
            LogUtil.error("击杀盖世太保检测补给箱的参数个数错误, Len:", param.length);
            return null;
        }
        int type = param[0];
        List<StaticPartySupply> supplyList = StaticPartyDataMgr.getPartySupplyByType(supplyType);
        if (!CheckNull.isEmpty(supplyList)) {
            return supplyList.stream().filter(ps -> ps.getParam().get(0) == type)
                    .sorted(Comparator.comparingInt(StaticPartySupply::getCount).reversed())
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 攻克城市
     *
     * @param player
     * @param supplyType
     * @param param
     * @return sort的目的是为了, 把有次数的先处理
     */
    private List<StaticPartySupply> conquerCityHandler(Player player, int supplyType, int... param) {
        if (param.length < 1) {
            LogUtil.error("攻克城市指定城市, 检测补给箱的参数个数错误, Len:", param.length);
            return null;
        }
        int type = param[0];
        List<StaticPartySupply> supplyList = StaticPartyDataMgr.getPartySupplyByType(supplyType);
        if (!CheckNull.isEmpty(supplyList)) {
            return supplyList.stream().filter(ps -> ps.getParam().get(0) == type)
                    .sorted(Comparator.comparingInt(StaticPartySupply::getCount).reversed())
                    .collect(Collectors.toList());
        }
        return null;

    }

    /**
     * 充值指定金额处理
     *
     * @param player
     * @param supplyType
     * @param param
     * @return
     */
    private List<StaticPartySupply> payGoldHandler(Player player, int supplyType, int... param) {
        if (param.length < 1) {
            LogUtil.error("充值指定金额, 检测补给箱的参数个数错误, Len:", param.length);
            return null;
        }
        int pay = param[0];
        List<StaticPartySupply> supplyList = StaticPartyDataMgr.getPartySupplyByType(supplyType);
        if (!CheckNull.isEmpty(supplyList)) {
            return supplyList.stream().filter(ps -> {
                List<Integer> params = ps.getParam();
                if (params.size() == 2) {
                    int minPay = params.get(0);
                    int maxPay = params.get(1);
                    if (pay >= minPay && pay <= maxPay) {
                        return true;
                    }
                } else if (params.size() == 1 && params.get(0) == pay) {
                    return true;
                }
                return false;
            }).sorted(Comparator.comparingInt(StaticPartySupply::getCount).reversed()).collect(Collectors.toList());
        }
        return null;

    }

    /**
     * 攻克柏林
     *
     * @param player
     * @param supplyType
     * @param param
     * @return
     */
    private List<StaticPartySupply> joinBerlinWarHandler(Player player, int supplyType, int... param) {
        if (param.length < 1) {
            LogUtil.error("攻克柏林, 检测补给箱的参数个数错误, Len:", param.length);
            return null;
        }
        int type = param[0];
        List<StaticPartySupply> supplyList = StaticPartyDataMgr.getPartySupplyByType(supplyType);
        if (!CheckNull.isEmpty(supplyList)) {
            return supplyList.stream().filter(ps -> ps.getParam().get(0) == type)
                    .sorted(Comparator.comparingInt(StaticPartySupply::getCount).reversed())
                    .collect(Collectors.toList());
        }
        return null;
    }

}
