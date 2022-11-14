package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSONArray;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.AbsClientHandler;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.BattlePassDataManager;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RankDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.SmallIdManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.VipDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.ChemicalQue;
import com.gryphpoem.game.zw.pb.CommonPb.OffLineBuild;
import com.gryphpoem.game.zw.pb.GamePb1.BeginGameRs;
import com.gryphpoem.game.zw.pb.GamePb1.CreateRoleRq;
import com.gryphpoem.game.zw.pb.GamePb1.CreateRoleRs;
import com.gryphpoem.game.zw.pb.GamePb1.GetLordRs;
import com.gryphpoem.game.zw.pb.GamePb1.GetTimeRs;
import com.gryphpoem.game.zw.pb.GamePb1.GiftCodeRs;
import com.gryphpoem.game.zw.pb.GamePb1.RoleLoginRs;
import com.gryphpoem.game.zw.pb.GamePb1.SeachPlayerRq;
import com.gryphpoem.game.zw.pb.GamePb1.SeachPlayerRs;
import com.gryphpoem.game.zw.pb.GamePb1.SetGuideRq;
import com.gryphpoem.game.zw.pb.GamePb1.SetGuideRs;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeChatBubbleRq;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeChatBubbleRs;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeLordNameRs;
import com.gryphpoem.game.zw.pb.GamePb4.ChangeSignatureRs;
import com.gryphpoem.game.zw.pb.GamePb4.CompareNotesRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetMixtureDataRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetMonthCardRs;
import com.gryphpoem.game.zw.pb.GamePb4.JoinCommunityRs;
import com.gryphpoem.game.zw.pb.GamePb4.OffLineIncomeRs;
import com.gryphpoem.game.zw.pb.GamePb4.OnHookGetAwardRs;
import com.gryphpoem.game.zw.pb.GamePb4.OnHookGetInfoRs;
import com.gryphpoem.game.zw.pb.GamePb4.OnHookOperateRs;
import com.gryphpoem.game.zw.pb.GamePb4.OnHookReplenishRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncForceUpdateRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncLoginStateRs;
import com.gryphpoem.game.zw.pb.GamePb4.SyncOnHookDataRs;
import com.gryphpoem.game.zw.pb.HttpPb.SendRoleInfosRq;
import com.gryphpoem.game.zw.pb.HttpPb.UseGiftCodeRq;
import com.gryphpoem.game.zw.pb.HttpPb.UseGiftCodeRs;
import com.gryphpoem.game.zw.pb.HttpPb.VerifyRs;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.EArmyType;
import com.gryphpoem.game.zw.resource.constant.EffectConstant;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.GlobalConstant;
import com.gryphpoem.game.zw.resource.constant.LoginConstant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.PartyConstant;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.constant.PushConstant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.TrophyConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.AccountDao;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.Role;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ArmQue;
import com.gryphpoem.game.zw.resource.domain.p.BuildingExt;
import com.gryphpoem.game.zw.resource.domain.p.Chemical;
import com.gryphpoem.game.zw.resource.domain.p.DecisiveInfo;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.EquipQue;
import com.gryphpoem.game.zw.resource.domain.p.Factory;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.p.PlayerOnHook;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.p.TargetServerCamp;
import com.gryphpoem.game.zw.resource.domain.p.Tech;
import com.gryphpoem.game.zw.resource.domain.p.TechLv;
import com.gryphpoem.game.zw.resource.domain.p.TechQue;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticChatBubble;
import com.gryphpoem.game.zw.resource.domain.s.StaticGuidAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticIniLord;
import com.gryphpoem.game.zw.resource.domain.s.StaticPay;
import com.gryphpoem.game.zw.resource.domain.s.StaticRecommend;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.domain.s.StaticTechLv;
import com.gryphpoem.game.zw.resource.pojo.BuildingState;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.daily.HonorDaily;
import com.gryphpoem.game.zw.resource.util.AccountHelper;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.ChatHelper;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.EmojiHelper;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.NumberHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.PushMessageUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import com.gryphpoem.game.zw.server.SavePlayerServer;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.activity.ActivityTemplateService;
import com.gryphpoem.game.zw.service.activity.AnniversaryEggService;
import com.gryphpoem.game.zw.service.plan.DrawCardPlanTemplateService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PlayerService implements GmCmdService {

    @Autowired
    private AccountDao accountDao;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private SmallIdManager smallIdManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RankDataManager rankDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private PropService propService;
    @Autowired
    private HonorDailyService honorDailyService;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private WorldWarSeasonDailyRestrictTaskService dailyRestrictTaskService;
    @Autowired
    private CiaService ciaService;
    @Autowired
    private WarService warService;
    @Autowired
    private CastleSkinService castleSkinService;
    @Autowired
    private ActivityDiaoChanService activityDiaoChanService;
    @Autowired
    private MarchService marchService;
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private DressUpService dressUpService;
    @Autowired
    private List<LoginService> loginServices;
    @Autowired
    private AnniversaryEggService anniversaryEggService;
    @Autowired
    private ActivityTemplateService activityTemplateService;
    @Autowired
    private DubboRpcService dubboRpcService;
    @Autowired
    private List<RefreshTimerService> refreshTimerServices;
    @Autowired
    private TreasureCombatService treasureCombatService;
    @Autowired
    private DrawCardPlanTemplateService drawCardPlanTemplateService;
    @Autowired
    private TitleService titleService;
    private String initName;
    @Autowired
    private TreasureChallengePlayerService challengePlayerService;
    @Autowired
    private LifeSimulatorService lifeSimulatorService;

    /**
     * 账号服务器的验证返回处理
     *
     * @param req
     * @param playerCtx
     * @return
     */
    public BeginGameRs verifyRs(VerifyRs req, ChannelHandlerContext playerCtx) {
        int platNo = req.getPlatNo();
        int childNo = req.getChildNo();
        String platId = req.getPlatId();
        long keyId = req.getKeyId();
        int serverId = req.getServerId();
        // String curVersion = req.getCurVersion();
        String deviceNo = req.getDeviceNo();
        BeginGameRs.Builder builder = BeginGameRs.newBuilder();
        Date now = new Date();
        Turple<Integer, Integer> campTurple = null;
        Account account = playerDataManager.getAccount(serverId, keyId);
        if (account == null) {
            // 禁止创角
            if (serverSetting.isForbidCreateRole()) {
                builder.setState(LoginConstant.BEGIN_STATE_FORBID_CREATE);
                return builder.build();
            }
            account = new Account();
            account.setServerId(serverId);
            account.setAccountKey(keyId);
            account.setPlatId(platId);
            account.setPlatNo(platNo);
            account.setChildNo(childNo);
            account.setDeviceNo(deviceNo);
            account.setLoginDays(1);
            account.setCreateDate(new Date());
            account.setLoginDate(new Date());
            account.setPublisher(StringUtils.isEmpty(req.getPublisher()) ? "" : req.getPublisher());
            campTurple = playerDataManager.getRecommendCamp(); // 设置推荐阵营奖励
            LogUtil.debug("=====推荐的阵营===== camp:", campTurple.getA(), ", 奖励的 keyId:", campTurple.getB());
            account.setRecommendCamp(campTurple.getA());// 设置推荐阵营
            Player player = playerDataManager.createPlayer(account);
            if (Objects.isNull(player)) {
                builder.setState(LoginConstant.BEGIN_STATE_ERROR);
                return builder.build();
            } else {
                LogLordHelper.logRegister(account);
            }
        } else {
            Account dbAccount = accountDao.selectAccountByKeyId(account.getKeyId());
            if (dbAccount != null) {
                // 若是小号，走创建流程,但不创建account
                if (smallIdManager.isSmallId(dbAccount.getLordId())) {
                    campTurple = playerDataManager.getRecommendCamp(); // 设置推荐阵营奖励
                    account.setRecommendCamp(campTurple.getA());// 设置推荐阵营
                    Player player = playerDataManager.createPlayerAfterCutSmallId(account);
                    if (Objects.isNull(player)) {
                        builder.setState(LoginConstant.BEGIN_STATE_ERROR);
                        return builder.build();
                    }
                } else {
                    account.setIsGm(dbAccount.getIsGm());
                    account.setIsGuider(dbAccount.getIsGuider());
                    account.setWhiteName(dbAccount.getWhiteName());
                    account.setLordId(dbAccount.getLordId());
                }

                Date loginDate = account.getLoginDate();
                if (!DateHelper.isSameDate(now, loginDate)) {
                    account.setLoginDays(account.getLoginDays() + 1);
                }
            }

            account.setDeviceNo(deviceNo);
            account.setChildNo(childNo);
            account.setLoginDate(now);
            playerDataManager.recordLogin(account);

            // Player player = playerDataManager.getPlayer(account.getLordId());
            // if (player != null) {
            // player.connected = true;
            // }

            // 获取推荐奖励
            Player newPlayer = playerDataManager.getNewPlayer(account.getLordId());
            if (newPlayer != null) {
                int camp = newPlayer.lord.getCamp();
                int key = playerDataManager.getRecommendKeyId();
                campTurple = new Turple<Integer, Integer>(camp, key);
            }

        }

        if (AccountHelper.isForbid(account)) {
            builder.setState(LoginConstant.BEGIN_STATE_FORBID);
            builder.setTime(TimeHelper.getCurrentSecond());
            builder.addParam(String.valueOf(account.getForbid()));
            return builder.build();
        }

        if (account.getCreated() == LoginConstant.ROLE_CREATED) {// 角色已创建
            builder.setState(LoginConstant.BEGIN_STATE_CREATED);
        } else {
            builder.setState(LoginConstant.BEGIN_STATE_NOT_CREATE);
            // builder.addAllName(playerDataManager.generateNames());
            if (campTurple != null) {
                builder.setCamp(campTurple.getA());
                builder.setKeyId(campTurple.getB());
            }
        }

        DataResource.registerRoleChannel(playerCtx, account.getLordId());
        builder.setTime(TimeHelper.getCurrentSecond());
        return builder.build();
    }

    /**
     * 名字是否不合法（创角时调用）
     *
     * @param nick
     * @return true表示不合法
     */
    private boolean nickIsIllegal(String nick) {
        return CheckNull.isNullTrim(nick) || nick.length() >= 16 || EmojiHelper.containsEmoji(nick)
                || EmojiHelper.containsSpecialSymbol(nick);
    }

    /**
     * 名字是否不合法（修改昵称时调用）
     *
     * @param nick
     * @return true表示不合法
     */
    private boolean ReNickIsIllegal(String nick) {
        return CheckNull.isNullTrim(nick) || nick.length() >= 16 || EmojiHelper.containsEmoji(nick)
                || EmojiHelper.ReNickContainsSpecialSymbol(nick);
    }

    /**
     * 强制更新
     *
     * @param type
     * @param param
     */
    public void forceUpdate(int type, String param) {
        SyncForceUpdateRs.Builder builder = SyncForceUpdateRs.newBuilder();
        builder.setType(type);
        builder.setParam(param);
        Base.Builder base = PbHelper.createSynBase(SyncForceUpdateRs.EXT_FIELD_NUMBER, SyncForceUpdateRs.ext,
                builder.build());
        playerDataManager.getAllOnlinePlayer().values().forEach(player -> {
            MsgDataManager.getIns().add(new Msg(player.ctx, base.build(), player.roleId));
        });
    }

    /**
     * 检测兵发放特殊领头像
     */
    @Deprecated
    public void checkHasHeroGivePortrait() {
//        playerDataManager.getPlayers().values().forEach(p -> {
//            Constant.USE_HERO_PORTRAIT.forEach((heroId, portraitId) -> {
//                if (p.heros.containsKey(heroId) && !p.portraits.contains(portraitId)) {// 有将领没头像
//                    rewardDataManager.addAward(p, AwardType.PORTRAIT, portraitId, 1, AwardFrom.VIP_GIFT_BUY);
//                }
//            });
//        });
    }


    /**
     * 玩家创建角色
     *
     * @param req
     * @param roleId
     * @param heroSkin 某个将领的皮肤
     * @return
     * @throws MwException
     */
    public Base.Builder createRole(CreateRoleRq req, long roleId, int heroSkin) throws MwException {
        int state;
        CreateRoleRs.Builder builder = CreateRoleRs.newBuilder();
        Player newPlayer = playerDataManager.getNewPlayer(roleId);
        if (newPlayer == null) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "创建角色时，用户信息不存在, roleId:" + roleId);
        }
        int tjCamp = 0;
        if (req.hasCampAward()) {
            // 优先选择客户端传过来的推荐阵营(合服需求)
            tjCamp = req.getCampAward();
        }
        int now = TimeHelper.getCurrentSecond();
        if (newPlayer.account.getCreated() == LoginConstant.ROLE_CREATED) {
            state = LoginConstant.CREATE_STATE_CREATED;
        } else {
            int camp = req.getCamp();// 玩家选择的阵营
            if (camp < 0 || camp > Constant.Camp.UNION) {
                throw new MwException(GameError.NO_CAMP.getCode(), "创建角色，阵营不存在, roleId:" + roleId + ", camp:" + camp);
            }
            // 校验阵营是否符合服规则
            camp = checkAndTransformCamp(newPlayer.account.getServerId(), camp);

            String nick = "";
            if (req.hasNick()) {
                nick = req.getNick();
            } else {
                // nick = playerDataManager.getFreeManName();// 随机一个姓名
                nick = newPlayer.getName();
            }
            if (nickIsIllegal(nick)) {
                throw new MwException(GameError.NICK_NAME_ERR.getCode(), "昵称格式错误 roleId:", roleId, ", name:", nick);
            }
            nick = nick.trim();
            if (!playerDataManager.canUseName(nick)) {
                // throw new MwException(GameError.SAME_NICK.getCode(), "昵称有相同");
                // 获取一个不会重复的名称
                nick = newPlayer.getName(now);
            }

            if (playerDataManager.takeNick(nick)) {
                // 领主初始属性配置
                StaticIniLord ini = StaticIniDataMgr.getLordIniData();
                // 创号初始模拟器的奖励及性格影响
                boolean isEnd = false;
                List<CommonPb.LifeSimulatorStep> lifeSimulatorStepList = req.getLifeSimulatorStepList();
                List<List<Integer>> finalRewardList = new ArrayList<>();
                List<List<Integer>> finalCharacterFixList = new ArrayList<>();
                int portrait = ini.getPortrait();
                for (CommonPb.LifeSimulatorStep lifeSimulatorStep : lifeSimulatorStepList) {
                    int chooseId = lifeSimulatorStep.getChooseId();
                    if (chooseId > 0) {
                        StaticSimulatorChoose sSimulatorChoose = StaticBuildCityDataMgr.getStaticSimulatorChoose(chooseId);
                        // 性格值变化
                        List<List<Integer>> characterFix = sSimulatorChoose.getCharacterFix();
                        finalCharacterFixList.addAll(characterFix);
                        List<List<Integer>> rewardList = sSimulatorChoose.getRewardList();
                        List<Integer> portraitAward = rewardList.stream().filter(tmp -> tmp.size() == 4 && tmp.get(0) == AwardType.PORTRAIT).findFirst().orElse(null);
                        if (portraitAward != null) {
                            portrait = portraitAward.get(1); // 头像类型的奖励, 小类即其具体id, 对应s_portrait的id
                        }
                        finalRewardList.addAll(rewardList);
                        // TODO 更新buff增益
                        List<List<Integer>> buff = sSimulatorChoose.getBuff();
                    }
                    int stepId = lifeSimulatorStep.getStepId();
                    StaticSimulatorStep staticSimulatorStep = StaticBuildCityDataMgr.getStaticSimulatorStep(stepId);
                    // 根据配置, 判断模拟器是否结束
                    if (!isEnd) {
                        Long nextId = staticSimulatorStep.getNextId();
                        List<List<Long>> staticChooseList = staticSimulatorStep.getChoose();
                        boolean isExistForwardStep = staticChooseList.stream().anyMatch(temp -> temp.get(0) == (long) chooseId && temp.get(1) == 0L);
                        if (nextId == null && isExistForwardStep) {
                            isEnd = true;
                        }
                    }
                }
                int recommendCamp = tjCamp != 0 ? tjCamp : newPlayer.lord.getCamp();// 优先选择客户端传过来的推荐阵营(合服需求)
                newPlayer.account.setCreated(LoginConstant.ROLE_CREATED);
                newPlayer.account.setCreateDate(new Date());
                newPlayer.lord.setPortrait(portrait);
                newPlayer.lord.setSex(ini.getSex());
                newPlayer.lord.setNick(nick);
                newPlayer.lord.setCamp(camp);
                newPlayer.lord.setPower(ini.getPower());
                newPlayer.lord.setRanks(ini.getRanks());
                newPlayer.lord.setOnTime(TimeHelper.getCurrentSecond());

                if (playerDataManager.createFullPlayer(newPlayer)) {
                    state = LoginConstant.CREATE_STATE_SUCCESS;

                    Player player = playerDataManager.getNewPlayer(roleId);
                    if (player == null) {
                        LogUtil.error("changeNewPlayer {" + roleId + "} error");
                        return null;
                    }

                    playerDataManager.removeNewPlayer(roleId);
                    playerDataManager.addPlayer(newPlayer);
                    Account account = newPlayer.account;
                    LogLordHelper.logRegister(account);
                    // 发送注册初始奖励
                    rewardDataManager.sendReward(player, Constant.REGISTER_REWARD, AwardFrom.REGISTER_REWARD);
                    if (!Constant.MAIL_FOR_CREATE_ROLE.isEmpty()) {
                        for (List<Integer> param : Constant.MAIL_FOR_CREATE_ROLE) {
                            if (param.size() > 1) {
                                // 附件内容
                                List<CommonPb.Award> awardList = new ArrayList<Award>();
                                Integer item_type = param.get(1);
                                Integer item_id = param.get(2);
                                Integer item_num = param.get(3);
                                CommonPb.Award en = PbHelper.createAwardPb(item_type, item_id, item_num);
                                awardList.add(en);
                                mailDataManager.sendAttachMail(player, awardList, param.get(0),
                                        AwardFrom.CREATE_ROLE_AWARD, now);
                            } else {
                                mailDataManager.sendNormalMail(player, param.get(0), now);
                            }
                        }
                    }
                    // 阵营推荐奖励
                    if (camp == recommendCamp) {
                        StaticRecommend sr = StaticLordDataMgr
                                .getRecommendCampById(playerDataManager.getRecommendKeyId());
                        List<Award> awards = PbHelper.createAwardsPb(sr.getAward());
                        // 给推荐择阵营的玩家发放奖励
                        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_RECOMMEND_CAMP_REWARD,
                                AwardFrom.RECOMMEND_CAMP_REWARD, now, camp, camp);
                    }
                    // 特殊将领皮肤设置
                    newPlayer.heroSkin = heroSkin;

                    // 玩家将模拟器玩完, 再发送奖励
                    if (isEnd) {
                        // 更新性格值并发送对应奖励
                        if (CheckNull.nonEmpty(finalCharacterFixList)) {
                            if (CheckNull.isEmpty(player.getCharacterData())) {
                                player.setCharacterData(new HashMap<>(6));
                            }
                            if (CheckNull.isEmpty(player.getCharacterRewardRecord())) {
                                player.setCharacterRewardRecord(new HashMap<>(8));
                            }
                            for (List<Integer> characterChange : finalCharacterFixList) {
                                Integer index = characterChange.get(0);
                                Integer value = characterChange.get(1);
                                Integer addOrSub = characterChange.get(0);
                                lifeSimulatorService.updateCharacterData(player.getCharacterData(), index, value, addOrSub);
                            }
                            lifeSimulatorService.checkAndSendCharacterReward(player);
                        }
                        // 更新对应奖励变化
                        if (CheckNull.nonEmpty(finalRewardList)) {
                            for (List<Integer> reward : finalRewardList) {
                                Integer awardType = reward.get(0);
                                Integer awardId = reward.get(1);
                                Integer awardCount = reward.get(2);
                                Integer addOrSub = reward.get(3);
                                switch (addOrSub) {
                                    case 1:
                                        rewardDataManager.sendRewardSignle(player, awardType, awardId, awardCount, AwardFrom.SIMULATOR_CHOOSE_REWARD, "");
                                        break;
                                    case 0:
                                        rewardDataManager.checkAndSubPlayerRes(player, awardType, awardId, awardCount, AwardFrom.SIMULATOR_CHOOSE_REWARD, true, "");
                                        break;
                                }
                            }
                        }
                    }

                    playerDataManager.campRoleNumArr[camp]++;
                    // mailDataManager.sendNormalMail(player, MailConstant.MOLD_MAIL_WELLCOME,
                    // TimeHelper.getCurrentSecond());
                    // mailDataManager.sendNormalMail(player, MailConstant.MOLD_MAIL_WELLCOME2,
                    // TimeHelper.getCurrentSecond());
                } else {
                    newPlayer.account.setCreated(LoginConstant.ROLE_NOT_CREATE);
                    LogUtil.error("createFullPlayer {" + newPlayer.roleId + "} error");
                    throw new MwException(GameError.SERVER_EXCEPTION.getCode(), "创建角色数据失败, roleId:" + roleId);
                }
            } else {
                state = LoginConstant.CREATE_STATE_FAIL;
            }
        }

        builder.setState(state);
        return PbHelper.createRsBase(CreateRoleRs.EXT_FIELD_NUMBER, CreateRoleRs.ext, builder.build());
    }

    /**
     * 检测阵营是否合法
     *
     * @param serverId
     * @param camp
     * @return
     * @throws MwException
     */
    private int checkAndTransformCamp(final int serverId, final int camp) throws MwException {
        int tCamp = camp;
        List<TargetServerCamp> allowJoinServerIdCampList = serverSetting.getAllowJoinServerIdCampList();
        // 0 随机阵营
        if (camp == 0) {
            TargetServerCamp ts = allowJoinServerIdCampList.stream().filter(tsc -> tsc.getOriginServerId() == serverId)
                    .findAny().orElse(null);
            if (ts == null) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "随机选择阵营不合法  serverId:", serverId, ", camp:",
                        camp, ", allowServerIdCamp:", serverSetting.getAllowJoinServerCampStr());
            }
            tCamp = ts.getCamp();
        } else {
            long count = allowJoinServerIdCampList.stream()
                    .filter(tsc -> tsc.getOriginServerId() == serverId && tsc.getCamp() == camp).count();
            if (count <= 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "选择阵营不合法  serverId:", serverId, ", camp:", camp,
                        ", allowServerIdCamp:", serverSetting.getAllowJoinServerCampStr());
            }
        }
        return tCamp;
    }

    /**
     * 玩家角色登录
     *
     * @param playerCtx
     * @param roleId
     * @return
     * @throws MwException
     */
    public Map<String, Object> roleLogin(ChannelHandlerContext playerCtx, long roleId, String packId)
            throws MwException {
        Map<String, Object> map = new HashMap<String, Object>();

        Player player = playerDataManager.getPlayer(roleId);
        if (player == null) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "玩家还未创建角色，参数错误，不能登录");
        }

        if (player.account.getCreated() != LoginConstant.ROLE_CREATED) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "玩家还未创建角色，参数错误，不能登录");
        }
        player.setPackId(packId);
        RoleLoginRs.Builder builder = RoleLoginRs.newBuilder();
        builder.setState(player.lord.getOnTime());
        ChannelHandlerContext preCtx = player.ctx;
        player.ctx = playerCtx;
        /*int currentDay = TimeHelper.getCurrentDay();
        int lastOLDay = TimeHelper.getDay(player.lord.getOnTime());*/

        taskDataManager.updTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);
        taskDataManager.updTask(player, TaskType.COND_LOGIN_36, 1);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_LOGIN_36, 1);
        dailyRestrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_LOGIN_36, 1);

        int today = TimeHelper.getCurrentDay();
        for (FunCard fc : player.funCards.values()) {
            if (today == fc.getLastTime()) {
                dailyRestrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_MONTH_CARD_STATE_45, 1);
            }
        }

        if (player.isLogin) {
            if (preCtx != null) {
                // 同步
                syncLoginState(preCtx, roleId, 1);
                preCtx.close();
            }
        } else {
            player.setLogin(true);
            playerDataManager.addOnline(player);
            sendOnlineChat(player.lord);
            // 当天第一次登陆,开启在线奖励
            /*Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ONLINE_GIFT);
            if (lastOLDay != currentDay && activity != null && CheckNull.isEmpty(activity.getStatusCnt())) {
                activityDataManager.updActivity(player, ActivityConst.ACT_ONLINE_GIFT, TimeHelper.getCurrentSecond(),
                        0);
                activity.getPropMap().put(0, player.lord.getLevel());
            }*/
            int nowDay = TimeHelper.getCurrentDay();
            int lastDay = TimeHelper.getDay(player.lord.getOnTime());
            if (nowDay != lastDay) {
                //隔天登陆了
                titleService.processTask(player, ETask.LOGIN_DAYS_SUM);
            }
            player.logIn();
            // 获取角色相关信息 用于推送给账号服
            SendRoleInfosRq.Builder roleInfoBuilder = SendRoleInfosRq.newBuilder();
            roleInfoBuilder.setAccountKey(player.account.getAccountKey());
            roleInfoBuilder.setRoleId(player.roleId);
            roleInfoBuilder.setServerId(player.account.getServerId());
            roleInfoBuilder.setLevel(String.valueOf(player.lord.getLevel()));
            roleInfoBuilder.setRoleName(player.lord.getNick());
            roleInfoBuilder.setServerName(serverSetting.getServerName());
            map.put("sendRoleInfosRq", roleInfoBuilder.build());
        }
        //登录成功后，初始化皮肤星数(之前已经获得了皮肤)
//        castleSkinService.loginAfter(player);
        //登陆成功后执行的方法
        Optional.ofNullable(loginServices).ifPresent(services -> services.forEach(service -> service.afterLogin(player)));
        //登录成功后，重新计算战力(之前已经穿戴了宝具，但是增加宝具战力对比后没有宝具的战力数据)
        CalculateUtil.reCalcAllHeroAttr(player);


        // 发送登陆奖励邮件
        sendLoginRewardMail(player, TimeHelper.getCurrentSecond());
        player.enterWorldCnt = 0;// 本次登陆进入世界次数清零
        //同步40级以上玩家数据到rpc-player 服务器
        dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        // 封测送vip
        // Calendar now = Calendar.getInstance();
        // int day = now.get(Calendar.DAY_OF_MONTH);
        // int vip = player.lord.getVip();
        // if (vip < day - 4) {
        // player.lord.setVip(day - 4);
        // }
        //打印玩家登录日志
        LogLordHelper.logLogin(player);
        //打印玩家基本信息日志
        LogLordHelper.logLord(player);
        LogLordHelper.commonLog("fightingChange", AwardFrom.COMMON, player, player.lord.getFight());
        map.put("roleLoginRs", builder.build());
        return map;
    }

    /**
     * 推送提下线的通知
     *
     * @param preCtx
     * @param roleId
     * @param state  1 账号在其他地方有登陆
     */
    public void syncLoginState(ChannelHandlerContext preCtx, long roleId, int state) {
        SyncLoginStateRs.Builder builder = SyncLoginStateRs.newBuilder();
        builder.setState(state);
        Base.Builder msg = PbHelper.createSynBase(SyncLoginStateRs.EXT_FIELD_NUMBER, SyncLoginStateRs.ext,
                builder.build());
        // MsgDataManager.getIns().add(new Msg(preCtx, msg.build(), roleId));
        // LogUtil.c2sMessage(msg, roleId);
        preCtx.writeAndFlush(msg);
    }

    /**
     * 转点处理
     */
    public void acrossTheDayProcess() {
        try {
            int now = TimeHelper.getCurrentSecond();
            LocalDateTime localDateTime = LocalDateTime.now();
            // 清除每日荣耀战报
            Java8Utils.invokeNoExceptionICommand(() -> {
                HonorDaily honorDaily = honorDailyService.getHonorDaily();
                if (!CheckNull.isNull(honorDaily)) {
                    honorDaily.getDailyReports().clear();
                }
            });
            // 清除每日公用数据
            Java8Utils.invokeNoExceptionICommand(() -> {
                globalDataManager.getGameGlobal().cleanMixtureData(GlobalConstant.getCleanList());
            });

            // 玩家数据转点处理
            for (Player p : playerDataManager.getPlayers().values()) {
                try {
                    if (p.isLogin) {
                        Date nowDate = new Date();
                        Date loginDate = p.account.getLoginDate();
                        if (!DateHelper.isSameDate(nowDate, loginDate)) {
                            p.account.setLoginDays(p.account.getLoginDays() + 1);
                        }
                        p.account.setLoginDate(nowDate);
                        playerDataManager.recordLogin(p.account);
                        titleService.processTask(p, ETask.LOGIN_DAYS_SUM);
                        Java8Utils.invokeNoExceptionICommand(() -> sendLoginRewardMail(p, now));
                        LogLordHelper.logLogin(p);// 打印登陆日志
                    }
                    p.setCollectMineCount(0);//清空每日攻打同阵营采集点
                    p.setBanditCnt(0); // 清空流寇上限
                    p.setMedalGoodsRefNum(0);// 重置每日勋章商店刷新次数
                    p.setPeacekeepingForcesNum(0);// 重置每日勋章-维和部队 触发次数
                    Java8Utils.invokeNoExceptionICommand(() -> p.getStoneInfo().cleanCnt());// 清理宝石副本购买次数 和 宝石攻打次数
                    Java8Utils.invokeNoExceptionICommand(() -> {
                        List<Integer> cleanList = PlayerConstant.getCleanList();
                        // 加入星期的充值记录清除
                        cleanList.add(localDateTime.getDayOfWeek().getValue() + PlayerConstant.RECENTLY_PAY);
                        p.cleanMixtureData(cleanList);
                    });// 清除数据
                    Java8Utils.invokeNoExceptionICommand(() -> taskDataManager.refreshDailyTask(p));// 重置日常任务
                    Java8Utils.invokeNoExceptionICommand(
                            () -> taskDataManager.updTask(p, TaskType.COND_MONTH_CARD_STATE_45, 1));
                    Java8Utils.invokeNoExceptionICommand(() -> taskDataManager.updTask(p, TaskType.COND_LOGIN_36, 1));
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.checkAndSendPay7DayMail(p));// 7日充值未领取奖励发送
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.refreshTurnplateCnt(p, ActivityConst.ACT_LUCKY_TURNPLATE));
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.refreshTurnplateCnt(p, ActivityConst.ACT_LUCKY_TURNPLATE_NEW));
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.refreshTurnplateCnt(p, ActivityConst.ACT_SEASON_TURNPLATE));
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.refreshTurnplateCnt(p, ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR));
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.refreshTurnplateCnt(p, ActivityConst.FAMOUS_GENERAL_TURNPLATE));
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.refreshEquipTurnplateCnt(p));

//                    Java8Utils.invokeNoExceptionICommand(() -> activityService.refreshData4AcrossDay(p,ActivityConst.ACT_MERGE_PROP_PROMOTION));

                    Java8Utils.invokeNoExceptionICommand(() ->
                            dailyRestrictTaskService.updatePlayerDailyRestrictTask(p, TaskType.COND_LOGIN_36, 1));
                    int today = TimeHelper.getCurrentDay();
                    for (FunCard fc : p.funCards.values()) {
                        if (today == fc.getLastTime()) {
                            Java8Utils.invokeNoExceptionICommand(() ->
                                    dailyRestrictTaskService.updatePlayerDailyRestrictTask(p, TaskType.COND_MONTH_CARD_STATE_45, 1));
                        }
                    }
                    // 勋章商品刷新
                    if (StaticFunctionDataMgr.funcitonIsOpen(p, MedalConst.MEDAL_SYS_LOCK)) {
                        medalDataManager.initMedalGoods(p, MedalConst.MEDAL_GOODS_0_TYPE);
                    }
                    // 删除无效好友
                    Java8Utils.invokeNoExceptionICommand(() -> FriendService.rmInvalidFriend(p));
                    // 情报部转点处理
                    Java8Utils.invokeNoExceptionICommand(() -> ciaService.refreshCntAndTime(p));

                    //处理貂蝉每日排行未领取奖励
                    Java8Utils.invokeNoExceptionICommand(() -> activityDiaoChanService.handleAcrossDay(p));

                    //处理赛季宝库任务奖励
                    seasonService.resetTreasuryDataInResetTime(p);
                    seasonService.resetTreasuryDataInAwardTime(p);

                    //活动跨天处理
                    activityTemplateService.execActivityDay(p);
                    // 宝具副本处理
                    treasureCombatService.acrossTheDayProcess(p);
                    // 宝具挑战玩家处理
                    challengePlayerService.acrossTheDayProcess(p);
                    // 功能计划过天处理
                    Java8Utils.invokeNoExceptionICommand(() -> drawCardPlanTemplateService.execFunctionDay(p));
                    // 周期性城镇事件刷新
                    lifeSimulatorService.assignCityEventToPlayerJob(p);
                } catch (Exception e) {
                    LogUtil.error("roleId: ", p.roleId, ", 转点定时任务出错: ", e);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 发送登陆奖励
     *
     * @param player
     */
    private void sendLoginRewardMail(Player player, int now) {
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LOGIN_EVERYDAY);
        if (activity != null && activity.getStatusCnt().isEmpty()) {
            int pLv = player.lord.getLevel();
            activity.getStatusCnt().put(0, (long) pLv); // 存储当时等级
        }
        activityDataManager.syncActChange(player, ActivityConst.ACT_LOGIN_EVERYDAY);
        // 原来的的登陆发奖
        // Date last = new Date(player.loginRewardTime * 1000L);
        // Date nowDate = new Date(now * 1000L);
        // if (DateUtils.isSameDay(last, nowDate)) {// 同一天不发生
        // return;
        // }
        // List<StaticDailyReward> sDailyReward = StaticVipDataMgr.getDailyRewardList();
        // if (!CheckNull.isEmpty(sDailyReward)) {
        // StaticDailyReward st = sDailyReward.stream().filter(sdr -> {
        // int minLv = sdr.getRoleLevel().get(0);
        // int maxLv = sdr.getRoleLevel().get(1);
        // int lv = player.lord.getLevel();
        // return lv >= minLv && lv <= maxLv;
        // }).findFirst().orElse(null);
        //
        // if (st != null) {
        // player.loginRewardTime = now;
        // List<Award> awards = PbHelper.createAwardsPb(st.getReward());
        // mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_LOGIN_REWARD,
        // AwardFrom.LOGIN_REWARD_MAIL, now);
        // }
        // }
    }

    /**
     * 发送上线提醒
     *
     * @param lord
     */
    private void sendOnlineChat(Lord lord) {
        int nowDay = TimeHelper.getCurrentDay();
        int lastDay = TimeHelper.getDay(lord.getOnTime());
        if (nowDay != lastDay) {
            // 检测官网,发上限提醒
            if (PartyConstant.Job.KING == lord.getJob()) {// 总司令
                chatDataManager.sendSysChat(ChatConst.CHAT_KING_LOGIN, lord.getCamp(), 0, lord.getCamp(),
                        lord.getNick());
                LogUtil.debug("lordId:", lord.getLordId(), " 阵营:", lord.getCamp(), " 总司令上线提醒", ", now:", nowDay,
                        " lastDay:", lastDay);
            } else if (PartyConstant.Job.COMMISSAR == lord.getJob()) {// 政委
                chatDataManager.sendSysChat(ChatConst.CHAT_PRIME_LOGIN, lord.getCamp(), 0, lord.getNick(),
                        lord.getCamp());
                LogUtil.debug("lordId:", lord.getLordId(), " 阵营:", lord.getCamp(), " 政委上线提醒", ", now:", nowDay,
                        " lastDay:", lastDay);
            } else if (PartyConstant.Job.CHIEF == lord.getJob()) {// 参谋长
                chatDataManager.sendSysChat(ChatConst.CHAT_ADVISER_LOGIN, lord.getCamp(), 0, lord.getNick(),
                        lord.getCamp());
                LogUtil.debug("lordId:", lord.getLordId(), " 阵营:", lord.getCamp(), " 参谋长上线提醒", ", now:", nowDay,
                        " lastDay:", lastDay);
            }
        }
    }

    /**
     * 客户端请求玩家数据
     *
     * @param roleId
     * @return
     */
    public GetLordRs getLord(long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            Lord lord = player.lord;
            // 重新计算一下战斗力
            CalculateUtil.reCalcFight(player);
            // rankDataManager.setFight(lord);
            rankDataManager.setStars(lord);

            // 重新计算一下VIP等级
            vipDataManager.processVip(player);

            com.gryphpoem.game.zw.pb.GamePb1.GetLordRs.Builder builder = GetLordRs.newBuilder();
            builder.setLordId(lord.getLordId());
            builder.setNick(lord.getNick());
            builder.setPortrait(lord.getPortrait());
            builder.setLevel(lord.getLevel());
            builder.setExp(lord.getExp());
            builder.setVip(lord.getVip());
            builder.setTopup(lord.getVipExp());
            builder.setPos(lord.getPos());
            builder.setGold(lord.getGold());
            builder.setCamp(lord.getCamp());
            builder.setRanks(lord.getRanks());
            builder.setFight(lord.getFight());
            builder.setRebuild(player.common.getReBuild());
            builder.setPower(lord.getPower());
            builder.setPowerTime(playerDataManager.leftBackPowerTime(player));
            builder.setBanditCnt(player.getBanditCnt());
            builder.setCollectMineCount(player.getCollectMineCount());
            Resource resource = player.resource;
            CommonPb.Resource.Builder resourcePb = CommonPb.Resource.newBuilder();
            resourcePb.setEle(resource.getElec());
            resourcePb.setFood(resource.getFood());
            resourcePb.setOil(resource.getOil());
            resourcePb.setOre(resource.getOre());
            resourcePb.setUranium(resource.getUranium());
            builder.setResource(resourcePb);
            CommonPb.Arm.Builder armPb = CommonPb.Arm.newBuilder();
            armPb.setArm1(resource.getArm1());
            armPb.setArm2(resource.getArm2());
            armPb.setArm3(resource.getArm3());
            builder.setArm(armPb);
            builder.setScoutCd(player.common.getScoutCdTime());
            builder.setAutoArmy(player.common.getAutoArmy());
            builder.setExploit(lord.getExploit());
            builder.setOlTime(player.onLineTime());
            builder.setGuideIndex(lord.getNewState());
            builder.setHeroToken(lord.getHeroToken());
            builder.setRenameCnt(player.common.getRenameCnt());
            builder.setTreasureWareGolden(player.lord.getTreasureWareGolden());
            builder.setTreasureWareDust(player.lord.getTreasureWareDust());
            builder.setTreasureWareEssence(player.lord.getTreasureWareEssence());
            if (!player.getEffect().isEmpty()) {
                for (Entry<Integer, Effect> kv : player.getEffect().entrySet()) {
                    builder.addEffect(PbHelper.createEffectPb(kv.getValue()));
                }
            }
            Integer cnt = player.trophy.get(TrophyConstant.TROPHY_2);
            cnt = cnt != null ? cnt : 0;
            builder.setBuyBuildCnt(cnt);
            if (!CheckNull.isNullTrim(lord.getSignature())) {
                builder.setSignature(lord.getSignature());
            }
            // 自动建造状态
            builder.setAutoBuildCnt(player.common.getAutoBuildCnt());
            builder.setAutoBuildOnOff(player.common.getAutoBuildOnOff());
            builder.setHeroSkin(player.heroSkin);// 某个特殊将领的皮肤
            builder.setIsFireState(player.isFireState());
            Date createDate = player.account.getCreateDate();
            int createRoleTime = (int) (createDate.getTime() / 1000);
            builder.setCreateRoleTime(createRoleTime);
            builder.setPaySumAmoumt(player.getPaySumAmoumt());
            builder.setCiaIsOpen(player.getCia() != null);
//            builder.setCiaOpenTime(DateHelper.afterDayTime(createDate, Constant.OPEN_CIA_TIME));
            builder.setArea(player.lord.getArea());
            for (Entry<Integer, Integer> kv : player.getMixtureData().entrySet()) {
                if (PlayerConstant.isInShowClientList(kv.getKey())) {// 过滤只需要让显示的东西
                    builder.addMixtureData(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
                }
            }
            DecisiveInfo decisiveInfo = player.getDecisiveInfo();
            if (decisiveInfo != null) {
                // 决战的失败冒火时间处理
                int fireTimeEnd = decisiveInfo.getFlyTime() + WorldConstant.DECISIVE_BATTLE_FINAL_TIME.get(2);
                builder.setBlazeTime(fireTimeEnd);
                // 决战失败白旗时间处理
                int whiteTimeEnd = decisiveInfo.getFlyTime() + WorldConstant.DECISIVE_BATTLE_FINAL_TIME.get(0);
                builder.setWhiteFlagTime(whiteTimeEnd);
            }
            // 外观装扮
            builder.setCurCastleSkin(player.getCurCastleSkin());
            Optional.ofNullable(player.getDressUp())
                    .ifPresent(dressUp -> {
                        builder.setCurNamePlate(dressUp.getCurNamePlate());
                        builder.setCurPortraitFrame(dressUp.getCurPortraitFrame());
                        builder.setCurMarchEffect(dressUp.getCurMarchEffect());
                        builder.setCurChatBubble(dressUp.getCurrChatBubble());
                        builder.setCurTitle(dressUp.getCurTitle());
                    });
            builder.setFishingGuide(player.getFishingData().getGuide());
            builder.setFirstMakeTw(player.getMakeTreasureWare().createPb(false));

            builder.setAncientBook(player.lord.getAncientBook());
            builder.addAllCharacter(PbHelper.createTwoIntListByMap(player.getCharacterData()));
            builder.addAllScoutData(PbHelper.createTwoIntListByMap(player.getScoutData()));
            player.getMapCellData().forEach((cellId, cellState) -> {
                CommonPb.MapCell.Builder mapCell = CommonPb.MapCell.newBuilder();
                mapCell.setCellId(cellId);
                if (cellState.get(1) == null) {
                    StaticHomeCityCell staticHomeCityCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
                    cellState.add(staticHomeCityCell.getHasBandit() == null ? 0 : staticHomeCityCell.getHasBandit());
                }
                mapCell.addAllState(cellState);
                builder.addMapCellData(mapCell.build());
            });
            for (BuildingState buildingState : player.getBuildingData().values()) {
                builder.addBuildingState(buildingState.creatPb());
            }
            builder.addAllFoundationData(player.getFoundationData());
            builder.setResidentMaxCnt(player.getResidentMaxCnt());
            builder.setResidentData(PbHelper.createTwoIntPb(player.getResidentTotalCnt(), player.getIdleResidentCnt()));
            builder.setTestLong(Long.MAX_VALUE);
            return builder.build();
        }
        return null;
    }


    /**
     * 获取玩家扩展数据
     *
     * @param roleId 玩家唯一id
     * @return 玩家扩展数据
     * @throws MwException 自定义异常
     */
    public GetMixtureDataRs getMixtureData(long roleId) throws MwException {
        GetMixtureDataRs.Builder builder = GetMixtureDataRs.newBuilder();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        for (Entry<Integer, Integer> kv : player.getMixtureData().entrySet()) {
            if (PlayerConstant.isInShowClientList(kv.getKey())) {// 过滤只需要让显示的东西
                builder.addMixtureData(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
            }
        }
        return builder.build();
    }

    /**************************** 离线收益 start *********************************/
    /**
     * @Title: getOffLineIncome @Description: 客户端请求玩家离线收益 @param roleId @return 参数 OffLineIncomeRs 返回类型 @throws
     */
    public OffLineIncomeRs getOffLineIncome(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        OffLineIncomeRs.Builder builder = OffLineIncomeRs.newBuilder();

        // 判断功能是否解锁
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.OFF_LINE_INCOME_LOCK_LV)) {
            return builder.build();
        }

        // 获取玩家重建家园奖励
        List<Award> awards = player.awards.get(Constant.AwardType.TYPE_1);
        if (awards != null) {
            builder.addAllAward(awards);
        }

        // 获取玩家生产完成的收益
        int elecOut = 0;// 电力
        int foodOut = 0;// 粮食
        int oilOut = 0;// 石油
        int oreOut = 0;// 矿石
        Iterator<Mill> it2 = player.mills.values().iterator();
        while (it2.hasNext()) {
            Mill mill = it2.next();
            // 检测资源是否能征收
            if (!buildingDataManager.checkMillCanGain(player, mill)) {
                continue;
            }
            int resCnt = mill.getResCnt();// 获取征收次数
            if (resCnt <= 0) {
                continue;
            }
            int now = TimeHelper.getCurrentSecond();
            // mill.setResTime(now);
            if (mill.getLv() <= 0) {
                continue;
            }
            StaticBuildingLv staticBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(mill.getType(),
                    mill.getLv());
            if (staticBuildingLevel == null) {
                LogUtil.error("BuildingLv config error,type=" + mill.getType() + ",lv=" + mill.getLv());
                continue;
            }

            List<Integer> resOuts = staticBuildingLevel.getResourceOut();
            if (resOuts == null || resOuts.size() < 3) {
                continue;
            }
            // 获取加成
            int gain = buildingDataManager.getResourceMult(player, mill.getType(), resOuts.get(2));

            LogUtil.debug(player.roleId + ",离线收获资源获取=" + gain + ",建筑类型=" + mill.getType() + ",领取次数=" + resCnt + ",基础产量="
                    + resOuts.get(2));

            switch (mill.getType()) {
                case BuildingType.RES_ELE:
                    elecOut += gain * resCnt;
                    break;
                case BuildingType.RES_FOOD:
                    foodOut += gain * resCnt;
                    break;
                case BuildingType.RES_OIL:
                    oilOut += gain * resCnt;
                    break;
                case BuildingType.RES_ORE:
                    oreOut += gain * resCnt;
                    break;
            }
        }
        if (elecOut != 0 || foodOut != 0 || oilOut != 0 || oreOut != 0) {
            CommonPb.Resource.Builder resourcePb = CommonPb.Resource.newBuilder();
            resourcePb.setEle(elecOut);
            resourcePb.setFood(foodOut);
            resourcePb.setOil(oilOut);
            resourcePb.setOre(oreOut);
            builder.setResource(resourcePb);
        }

        // 获取玩家训练完成的兵力
        int chariot = getArmByFactoryType(player, BuildingType.FACTORY_1);// 战车
        int tank = getArmByFactoryType(player, BuildingType.FACTORY_2);// 坦克
        int rocket = getArmByFactoryType(player, BuildingType.FACTORY_3);// 火箭

        // 获取玩家超级工厂
        for (int type : Arrays.asList(BuildingType.TRAIN_FACTORY_1, BuildingType.TRAIN_FACTORY_2)) {
            BuildingExt buildingExt = player.buildingExts.get(type);
            if (buildingExt != null && buildingExt.isUnlock()) {
                int num = getArmByFactoryType(player, type);// 超级工厂生产量
                switch (buildingExt.getType()) {
                    case BuildingType.TRAIN_FACTORY_1:
                        chariot += num;
                        break;
                    case BuildingType.TRAIN_FACTORY_2:
                        tank += num;
                        break;
                    case BuildingType.TRAIN_FACTORY_3:
                        rocket += num;
                        break;
                }
            }
        }

        if (chariot != 0 || tank != 0 || rocket != 0) {
            CommonPb.Arm.Builder armPb = CommonPb.Arm.newBuilder();
            armPb.setArm1(chariot);
            armPb.setArm2(tank);
            armPb.setArm3(rocket);
            builder.setArm(armPb);
        }

        // 获取玩家研究完成的科技
        Tech tech = player.tech;
        if (tech == null) {
            tech = new Tech();
            player.tech = tech;
        }
        TechQue que = tech.getQue();
        int now = TimeHelper.getCurrentSecond();
        if (que != null && que.getId() > 0 && que.getEndTime() > 0 && now >= que.getEndTime()) {// 存在科技列队 并且 已完成
            if (!tech.getTechLv().containsKey(que.getId())) {
                tech.getTechLv().put(que.getId(), new TechLv(que.getId(), 0, 1));
            }
            TechLv techLv = tech.getTechLv().get(que.getId());
            builder.setTech(PbHelper.createTechLv(techLv));
        }

        // 获取玩家打造完成的科技
        if (player.equipQue != null && !player.equipQue.isEmpty()) {// 列队存在
            // 获取领取的装备打造列队
            EquipQue buildQue = player.equipQue.get(0);
            if (buildQue != null && now >= buildQue.getEndTime()) {
                Equip equip = new Equip();
                equip.setKeyId(buildQue.getKeyId());
                equip.setEquipId(buildQue.getEquipId());
                builder.setEquip(PbHelper.createEquipPb(equip));
            }
        }

        // 获取化工列队完成的材料
        Chemical chemical = player.chemical;
        Map<Integer, ChemicalQue> map = new HashMap<Integer, ChemicalQue>();// key 材料id
        if (chemical != null && chemical.getPosQue() != null && !chemical.getPosQue().isEmpty()) {// 化工厂存在 且 生产列队不为空
            Iterator<String> it = chemical.getPosQue().keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                ChemicalQue v = chemical.getPosQue().get(key);
                if (v != null && now >= v.getEndTime()) {
                    // 判断是否已经存在
                    if (checkChemical(map, v)) {
                        ChemicalQue c = map.get(v.getId());
                        map.put(c.getId(),
                                ChemicalQue.newBuilder().setPos(c.getPos()).setId(c.getId()).setPeople(c.getPeople())
                                        .setPeriod(c.getPeriod()).setEndTime(c.getEndTime())
                                        .setClonePos(c.getClonePos()).setSid(c.getSid())
                                        .setCount(c.getCount() + v.getCount()).setStartTime(c.getStartTime()).build());
                    } else {
                        map.put(v.getId(), v);
                    }
                }
            }
        }
        builder.addAllChemicalQue(map.values());

        // 获取升级完成的 建筑
        Map<Integer, OffLineBuild> offLineBuilds = player.offLineBuilds;
        if (offLineBuilds != null) {
            builder.addAllOffLineBuilds(offLineBuilds.values());
            player.offLineBuilds = null;
        }

        // 获取玩家离线时长
        builder.setOffLineTime(player.lord.getOnTime() - player.lord.getOffTime());

        return builder.build();
    }

    /**
     * @param map
     * @param v
     * @return boolean true 是
     * @Title: checkChemical
     * @Description: 验证该材料是否重复生产
     */
    public boolean checkChemical(Map<Integer, ChemicalQue> map, ChemicalQue v) {
        for (ChemicalQue c : map.values()) {
            if (c.getId() == v.getId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @Title: getArmByFactoryType @Description: 根据兵营类型获取 已完成的兵力 @param player @param type @return 参数 int 返回类型 @throws
     */
    public int getArmByFactoryType(Player player, int type) {
        Factory factory = player.factory.get(type);
        int buildingLv = BuildingDataManager.getBuildingLv(type, player);
        BuildingExt ext = player.buildingExts.get(type);
        int addNum = 0;
        if (factory != null && buildingLv >= 1 && !CheckNull.isNull(ext)) {// 有募兵列队 并且 建筑已建造 并且 有建筑扩展
            // 查看结束的
            Iterator<ArmQue> it = factory.getAddList().iterator();
            while (it.hasNext()) {
                ArmQue armQue = it.next();
                if (TimeHelper.getCurrentSecond() >= armQue.getEndTime()) {
                    addNum += armQue.getAddArm();
                }
            }
        }
        return addNum;
    }

    /**************************** 离线收益end *********************************/

    /**
     * 重新计算所有人的战斗力
     */
    public void reCalcFightAllPlayer() {
        LogUtil.debug("============================重新计算战斗力 Start ============================");
        for (Player player : playerDataManager.getPlayers().values()) {
            try {
                long preFight = player.lord.getFight();
                CalculateUtil.reCalcBattleHeroAttr(player);
                long nowFight = player.lord.getFight();
                LogUtil.debug("roleId:", player.roleId, ", preFight:", preFight, ", nowFight:", nowFight);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error("重新计算战斗力出错 roleId:", player.roleId);
            }
        }
        LogUtil.debug("============================重新计算战斗力 End ============================");
    }

    /**
     * 使用兑换码
     *
     * @param req
     * @return
     */
    public GiftCodeRs useGiftCodeLogic(final UseGiftCodeRs req) {
        long roleId = req.getLordId();
        String award = req.getAward();

        Player player = playerDataManager.getPlayer(roleId);
        ChannelHandlerContext ctx = player.ctx;

        GiftCodeRs.Builder builder = GiftCodeRs.newBuilder();
        builder.setState(req.getState());

        int state = req.getState();
        if (state != 0) {
            if (player.isLogin && ctx != null) {
                return builder.build();
            }
        }

        try {
            JSONArray arrays = JSONArray.parseArray(award);
            for (int i = 0; i < arrays.size(); i++) {
                JSONArray array = arrays.getJSONArray(i);
                if (array.size() != 3) continue;
                int type = array.getInteger(0);
                int id = array.getInteger(1);
                int count = array.getInteger(2);
                int keyId = rewardDataManager.addAward(player, type, id, count, AwardFrom.GIFT_CODE);
                builder.addAward(PbHelper.createAwardPb(type, id, count, keyId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (player.isLogin && ctx != null) {
            return builder.build();
        }
        return null;
    }

    public Base.Builder giftCode(long roleId, String code) throws MwException {
        if (code.length() < 2) {
            throw new MwException(GameError.GIFT_CODE_LENTH.getCode(), "兑换码不正确, roleId:" + roleId + ", code:" + code);
        }

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        UseGiftCodeRq.Builder builder = UseGiftCodeRq.newBuilder();
        builder.setCode(code);
        builder.setLordId(player.roleId);
        builder.setServerId(player.account.getServerId());
        builder.setPlatNo(player.account.getPlatNo());

        Base.Builder baseBuilder = PbHelper.createRqBase(UseGiftCodeRq.EXT_FIELD_NUMBER, 0L, UseGiftCodeRq.ext,
                builder.build());
        return baseBuilder;
    }

    /**
     * 客户端获取服务器时间
     *
     * @param handler
     * @return
     */
    public GetTimeRs getTime(AbsClientHandler handler) {
        GetTimeRs.Builder builder = GetTimeRs.newBuilder();
        builder.setTime(TimeHelper.getCurrentSecond());
        builder.setOpenPay(serverSetting.isOpenPay());

        return builder.build();
    }

    public List<String> getAvailabelNames() {
        return playerDataManager.generateNames();
    }

    /**
     * 保存玩家角色相关数据定时任务
     */
    public void saveTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        int saveCount = 0;
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (saveCount >= PlayerSaveConfig.ONCE_SAVE_CNT) {
                break;
            }
            if (PlayerSaveConfig.isCanSave(player, now)) {
                try {
                    saveCount++;
                    player.lastSaveTime = now;
                    SavePlayerServer.getIns().saveData(new Role(player, Role.SAVE_MODE_TIMER));
                    player.immediateSave = false;
                } catch (Exception e) {
                    LogUtil.error("保存玩家数据出错, roleId:" + player.roleId, e);
                }
            }
            // if (player.immediateSave || (now - player.lastSaveTime) >= 300) {
            // try {
            // if (saveCount >= 500) {
            // break;
            // }
            //
            // saveCount++;
            // player.lastSaveTime = now;
            // SavePlayerServer.getIns().saveData(new Role(player));
            // if (player.immediateSave) {
            // player.immediateSave = false;
            // }
            // } catch (Exception e) {
            // LogUtil.error("保存玩家数据出错, roleId:" + player.roleId, e);
            // }
            // }
        }

        if (saveCount != 0) {
            LogUtil.save("save player count:" + saveCount);
        }
    }

    /**
     * 查询玩家
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public SeachPlayerRs seachPlayer(SeachPlayerRq req, Long roleId) throws MwException {
        String nick = req.getNick();

        Player player = playerDataManager.getPlayer(nick);
        if (player == null) {
            // 根据id查找,转换id失败进行捕获
            try {
                player = playerDataManager.getPlayer(Long.valueOf(nick));
            } catch (NumberFormatException e) {
            }
        }
        if (player == null) {
            player = playerDataManager.getPlayer(nick);
            throw new MwException(GameError.PLAYER_NOT_EXIST.getCode(),
                    "查询玩家,玩家不存在, roleId:" + roleId + ", nick:" + nick);
        }
        Lord lord = player.lord;
        SeachPlayerRs.Builder builder = SeachPlayerRs.newBuilder();
        CommonPb.Man.Builder man = CommonPb.Man.newBuilder();
        man.setLordId(lord.getLordId());
        man.setNick(lord.getNick());
        man.setSex(lord.getSex());
        man.setIcon(lord.getPortrait());
        man.setPortraitFrame(player.getDressUp().getCurPortraitFrame());
        man.setLevel(lord.getLevel());
        man.setRanks(lord.getRanks());
        man.setFight(lord.getFight());
        man.setArea(lord.getArea());
        man.setCamp(lord.getCamp());
        builder.setMan(man.build());
        return builder.build();
    }

    public void handlePlayerState() {
        List<GlobalActivityData> globalActivityDataList = anniversaryEggService.checkRefreshEgg();
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().forEach(player -> {
            // buff刷新
            player.refreshEffect();
            // 恢复体力
            if (player.isActive()) {
                playerDataManager.restoreProsAndPower(player, now);
            }
            // 检查挂机
            this.checkOnHook(player);
            //刷新周年庆彩蛋
            anniversaryEggService.refreshEgg(globalActivityDataList, player);
            //通用刷新处理
            Optional.ofNullable(refreshTimerServices).ifPresent(services -> services.forEach(service -> service.checkAndRefresh(player)));
        });
    }

//    /**
//     * 恢复能量的定时器逻辑
//     * 1、检查内挂状态
//     */
//    public void restoreDataTimerLogic() {
//        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
//        int now = TimeHelper.getCurrentSecond();
//
//        while (iterator.hasNext()) {
//            Player player = iterator.next();
//
//            try {
//
//            } catch (Exception e) {
//                LogUtil.error("恢复能量和繁荣度的定时器报错, lordId:" + player.lord.getLordId(), e);
//            }
//            this.checkOnHook(player);
//        }
//    }

    /**
     * 设置玩家当前新手引导进度
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public SetGuideRs setGuide(SetGuideRq req, long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int index = req.getIndex();
        int oldIndex = player.lord.getNewState();
        if (index > oldIndex) {// 保存最大的那个值
            player.lord.setNewState(index);
        }

        SetGuideRs.Builder builder = SetGuideRs.newBuilder();

        StaticGuidAward staticGuidAward = StaticLordDataMgr.getStaticGuidAward(index);
        if (staticGuidAward != null && index > oldIndex) {
            // if (!staticGuidAward.getCond().isEmpty()) {
            // Integer taskId = staticGuidAward.getCond().get(1);
            // if (taskId != null) {
            // Task task = player.majorTasks.get(taskId);
            // if (task != null && task.getStatus() > 0) {
            // // 完成任务，发送奖励
            // if (!staticGuidAward.getRewards().isEmpty()) {
            // builder.addAllAward(rewardDataManager.sendReward(player, staticGuidAward.getRewards(),
            // AwardFrom.GUID_AWARD, "新手引导获得奖励"));
            // LogUtil.debug("获得新手引导奖励");
            // }
            //
            // if (staticGuidAward.getAutoNum() > 0) {
            // player.common.setAutoBuildCnt(staticGuidAward.getAutoNum());
            // builder.setAutoNum(player.common.getAutoBuildCnt());
            // }
            // } else {
            // LogUtil.debug(roleId + ",新手引导未完成任务taskId=" + taskId);
            // }
            // }
            // } else {
            // if (!staticGuidAward.getRewards().isEmpty()) {
            // builder.addAllAward(rewardDataManager.sendReward(player, staticGuidAward.getRewards(),
            // AwardFrom.GUID_AWARD, "新手引导获得奖励"));
            // LogUtil.debug("获得新手引导奖励");
            // }
            //
            // if (staticGuidAward.getAutoNum() > 0) {
            // player.common.setAutoBuildCnt(staticGuidAward.getAutoNum());
            // builder.setAutoNum(player.common.getAutoBuildCnt());
            // }
            // }
            if (!staticGuidAward.getRewards().isEmpty()) {
                builder.addAllAward(rewardDataManager.addAwardDelaySync(player, staticGuidAward.getRewards(), null,
                        AwardFrom.GUID_AWARD));
                LogUtil.debug("获得新手引导奖励");
            }

            if (staticGuidAward.getAutoNum() > 0) {
                player.common.setAutoBuildCnt(staticGuidAward.getAutoNum());
                builder.setAutoNum(player.common.getAutoBuildCnt());

                player.common.setAutoBuildOnOff(1);// 开启自动建造
                buildingDataManager.syncAutoBuildInfo(player);// 同步自动建造
                buildingService.addAtuoBuild(player);// 触发自动建造
            }
            // 需要设置的id
            int nextGuideId = staticGuidAward.getNextGuideId();
            if (nextGuideId > 0 && nextGuideId > index) {
                player.lord.setNewState(nextGuideId);
            }
        }
        builder.setIndex(player.lord.getNewState());
        return builder.build();
    }

    /**
     * 修改名字
     *
     * @param roleId
     * @param name
     * @return
     * @throws MwException
     */
    public ChangeLordNameRs changeLordName(long roleId, String name) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (ChatHelper.isCorrect(name) || ReNickIsIllegal(name)) {
            throw new MwException(GameError.NICK_NAME_ERR.getCode(), "昵称格式错误 roleId:", roleId, ", name:", name);
        }
        name = name.trim();

        // if (player.common.getRenameCnt() < 1) { // 消耗免费次数时
        // // 消耗改名次数
        // if (!name.equals(player.lord.getNick())) {
        // // 名字不同才执行改名
        // if (!playerDataManager.takeNick(name)) {
        // throw new MwException(GameError.SAME_NICK.getCode(), "昵称有相同");
        // }
        // player.common.setRenameCnt(1);
        // playerDataManager.rename(player, name);
        // }
        // // 更新改名任务
        // taskDataManager.updTask(player, TaskType.COND_31, 1);
        // } else {
        // }
        // 消耗改名贴时
        if (!name.equals(player.lord.getNick())) { // 名字不同才执行改名
            if (!playerDataManager.canUseName(name)) {
                throw new MwException(GameError.SAME_NICK.getCode(), "昵称有相同");
            }
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.PROP_RENAME_CARD, 1,
                    AwardFrom.USE_PROP);
            if (!playerDataManager.takeNick(name)) {
                throw new MwException(GameError.SAME_NICK.getCode(), "昵称有相同");
            }
            playerDataManager.rename(player, name);
        }
        ChangeLordNameRs.Builder builder = ChangeLordNameRs.newBuilder();
        builder.setName(name);
        taskDataManager.updTask(player, TaskType.COND_507, 1);
        return builder.build();
    }

    /**
     * 修改个性签名
     *
     * @param roleId
     * @param signature
     * @return
     * @throws MwException
     */
    public ChangeSignatureRs changeSignature(long roleId, String signature) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, 9010)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), String.format("个性签名等级不足, roleId:%d, lv:%d", player.roleId, player.lord.getLevel()));
        }
        if (CheckNull.isNullTrim(signature) || signature.length() > 150 || EmojiHelper.containsEmoji(signature)) {
            throw new MwException(GameError.SIGNATURE_ERR.getCode(), "个性签名格式错误");
        }
        player.lord.setSignature(signature);
        LogLordHelper.commonChat("changeSignature", AwardFrom.CHANGE_SIGNATURE, player.account, player.lord, signature, serverSetting.getServerID());
        ChangeSignatureRs.Builder builder = ChangeSignatureRs.newBuilder();
        builder.setSignature(signature);
        return builder.build();
    }

    /**
     * 获取月卡信息
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetMonthCardRs getMonthCard(GamePb4.GetMonthCardRq req, long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetMonthCardRs.Builder builder = GetMonthCardRs.newBuilder();
        builder.setDay(player.lord.getMouthCardDay());
        List<StaticPay> staticPays = StaticVipDataMgr.getPayList().stream().filter(pay -> pay.getBanFlag() != 3).collect(Collectors.toList());
        Collection<CommonPb.PayInfo> payInfos = PbHelper.createPayInfo(staticPays);
        for (String f : player.firstPayDouble) {
            String[] platAndPayid = f.split("_");
            if (platAndPayid.length != 2) {
                continue;
            }
            int plat = NumberHelper.strToInt(platAndPayid[0], 0);
            int payId = NumberHelper.strToInt(platAndPayid[1], 0);
            if (payId == 0) {
                continue;
            }
            builder.addPayIds(PbHelper.createTwoIntPb(plat, payId));
        }
        builder.addAllPayInfo(payInfos);
        for (FunCard fc : player.funCards.values()) {
            builder.addFunCard(fc.ser());
        }
        FunCard funCard = seasonService.getCurrSeasonCard(player);
        if (Objects.nonNull(funCard)) {
            CommonPb.FunCard.Builder builder1 = funCard.ser().toBuilder();
            builder1.setType(FunCard.CARD_TYPE[9]);
            builder.setSeasonCard(builder1.build());
        }
        return builder.build();
    }

    /**
     * 给所有玩家加上罩子
     *
     * @param protectTime 罩子的时长
     */
    public void setAllPlayerProtect(int protectTime) {
        if (protectTime < 0) return;
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().forEach(p -> {
            Effect effect = p.getEffect().get(EffectConstant.PROTECT);
            if (effect == null) {
                p.getEffect().put(EffectConstant.PROTECT,
                        effect = new Effect(EffectConstant.PROTECT, 0, now + protectTime));
            } else {
                int oldEndTime = effect.getEndTime();
                if (oldEndTime < now) { // 已经过期
                    effect.setEndTime(now + protectTime);
                } else {
                    effect.setEndTime(oldEndTime + protectTime);
                }
            }
            propService.syncBuffRs(p, effect);
            LogUtil.debug("加上保护罩 roleId:", p.roleId, ", endTime:", effect.getEndTime());
        });
        worldDataManager.synCleanMapRefreshChg();
    }

    /**
     * 修改聊天气泡框
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public ChangeChatBubbleRs changeChatBubble(long roleId, ChangeChatBubbleRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int bubbleId = req.getBubbleId();
        // 等于0默气泡不处理
        if (bubbleId != 0) {
            StaticChatBubble sChatBubble = StaticLordDataMgr.getChatBubbleMapById(bubbleId);
            if (sChatBubble == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "聊天气泡配置未配置 : bubbleId:", bubbleId, ", roleId:",
                        roleId);
            }
            int type = sChatBubble.getType();
            if (type == StaticChatBubble.TYPE_FREE) { // 免费不处理
            } else if (type == StaticChatBubble.TYPE_VIP_LV) { // 判断VIP等级
                if (player.lord.getVip() < sChatBubble.getParam()) {
                    throw new MwException(GameError.VIP_NOT_ENOUGH.getCode(), "vip等级不够: bubbleId:", bubbleId,
                            ", vipLv:", player.lord.getVip(), ", roleId:", roleId);
                }
            } else if (type == StaticChatBubble.TYPE_GOLD
                    || type == StaticChatBubble.TYPE_AWARD
                    || type == StaticChatBubble.TYPE_ACT_AWARD) { // 判断是否拥有
                if (!player.getChatBubbles().contains(bubbleId)) {
                    throw new MwException(GameError.NOT_HAS_CHAT_BUBBLE.getCode(), "未拥有该聊天气泡: bubbleId:", bubbleId,
                            ", roleId:", roleId);
                }
            } else {
                throw new MwException(GameError.NO_CONFIG.getCode(), "聊天气类型错误 :", bubbleId, ", roleId:", roleId);
            }
        }
        player.setCurChatBubble(bubbleId);
        ChangeChatBubbleRs.Builder builder = ChangeChatBubbleRs.newBuilder();
        builder.setBubbleId(bubbleId);
        return builder.build();
    }

    /**
     * 离线时长发送推送的定时器
     */
    public void offLineTimeTimer() {
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().filter(p -> p.canPushOffLine(now)).forEach(p -> {
            Integer status = p.getPushRecord(PushConstant.OFF_LINE_ONE_DAY);
            int offTime = now - p.lord.getOffTime();
            if (status != null && offTime > 0) {
                // 没有发送过推送, 校验离线时间 大于24小时 小于48小时
                if (status == 0 && offTime > TimeHelper.DAY_S && offTime < (TimeHelper.DAY_S * 2)) {
                    PushMessageUtil.pushMessage(p.account, PushConstant.OFF_LINE_ONE_DAY);
                    p.putPushRecord(PushConstant.OFF_LINE_ONE_DAY, 1);
                } else if (status == 1 && offTime > (TimeHelper.DAY_S * 2) && offTime < (TimeHelper.DAY_S * 3)) {
                    // 已发送离线24小时的推送, 大于48小时发送第二次推送
                    PushMessageUtil.pushMessage(p.account, PushConstant.OFF_LINE_TWO_DAY);
                    p.putPushRecord(PushConstant.OFF_LINE_ONE_DAY, 2);
                } else if (status == 2 && offTime > (TimeHelper.DAY_S * 3)) {
                    // 已发送离线48小时的推送, 大于72小时发送第二次推送
                    PushMessageUtil.pushMessage(p.account, PushConstant.OFF_LINE_THREE_DAY);
                    p.putPushRecord(PushConstant.OFF_LINE_ONE_DAY, 3);
                }
            }
        });
    }

    /**
     * 爱丽丝达到推送的定时器
     */
    public void aliceArriveTimer() {
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().filter(p -> canPushAliceArrive(p, now)).forEach(p -> {
            PushMessageUtil.pushMessage(p.account, PushConstant.ALICE_ARRIVE);
            p.putPushRecord(PushConstant.ALICE_ARRIVE, PushConstant.ALICE_ARRIVE);
        });
    }

    /**
     * 判断是否可以推送爱丽丝达到消息(应用外推送)
     *
     * @param player 玩家
     * @param now    现在的时间
     * @return 是否可以推送
     */
    private boolean canPushAliceArrive(Player player, int now) {
        Integer status = player.getPushRecord(PushConstant.ALICE_ARRIVE);
        int aliceAwardTime = player.getMixtureDataById(PlayerConstant.ALICE_AWARD_TIME);
        return status != null && now > aliceAwardTime && status != PushConstant.ALICE_ARRIVE;
    }


    /**
     * 加入社群奖励
     *
     * @param roleId 玩家角色id
     * @return
     * @throws MwException 自定义异常
     */
    public JoinCommunityRs joinCommunity(long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int val = player.getMixtureDataById(PlayerConstant.JOIN_COMMUNITY_AWARD);

        if (val != 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "领取加入社群奖励, 已经领取过奖励了");
        }

        Award awardPb = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, Constant.JOIN_COMMUNITY_AWARD);
        // 发送奖励邮件
        mailDataManager.sendAttachMail(player, Collections.singletonList(awardPb), MailConstant.MOLD_JOIN_COMMUNITY_REWARD, AwardFrom.JOIN_COMMUNITY_AWARD, TimeHelper.getCurrentSecond());
        // 设置已领取的状态
        player.setMixtureData(PlayerConstant.JOIN_COMMUNITY_AWARD, 1);
        return JoinCommunityRs.newBuilder().build();

    }


    /**
     * 切磋
     *
     * @param roleId   发起方
     * @param targetId 目标方
     * @param heroIds  将领id
     * @return 战报数据
     * @throws MwException 自定义异常
     */
    public CompareNotesRs compareNotes(long roleId, long targetId, List<Integer> heroIds) throws MwException {


        Player mPlayer = playerDataManager.checkPlayerIsExist(roleId);

        Player tPlayer = playerDataManager.checkPlayerIsExist(targetId);

        if (mPlayer.lord.getCamp() != tPlayer.lord.getCamp()) {
            throw new MwException(GameError.COMPARE_NOTES_CAMP_ERROR.getCode(), "只有相同阵营才能切磋");
        }

        return warService.compareNotesFightLogic(mPlayer, tPlayer, heroIds);
    }

    private boolean checkCardUnexpired(Player player, int type) {
        FunCard funCard = player.funCards.get(type);
        if (Objects.isNull(funCard)) {
            return false;
        }
        int now = TimeHelper.getCurrentSecond();
        if (now >= funCard.getExpire()) {
            return false;
        }
        return true;
    }

    private void checkOnHook(Player player) {
        try {
            if (Objects.isNull(player)) {
                return;
            }
            PlayerOnHook playerOnHook = player.getPlayerOnHook();
            if (checkCardUnexpired(player, FunCard.CARD_TYPE[8])) {
                if (playerOnHook.getState() == ONHOOK_TYPE[1]) {
                    boolean stop = false;
                    if (playerOnHook.getArmys().values().stream().collect(Collectors.summarizingInt(x -> x.intValue())).getSum() < Constant.ONHOOK_1061 ||
                            (!player.isLogin && TimeHelper.getCurrentSecond() - player.lord.getOffTime() >= Constant.ONHOOK_1062) ||
                            player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
                        stop = true;
                    }
                    if (stop) {
                        playerOnHook.setState(ONHOOK_TYPE[0]);
                        //停止挂机清除每次剿灭数量
                        playerOnHook.setAskAnnihilateNumber(0);
                        playerOnHook.setAskLastAnnihilateNumber(0);
                        this.syncOnHookData(player);
                    } else {
                        int now = TimeHelper.getCurrentSecond();
                        if (now - playerOnHook.getLastStamp() >= Constant.ONHOOK_1063) {
                            playerOnHook.setLastStamp(now);
                            List<Award> dropAwards = marchService.onHookBandit(player, playerOnHook.getCurRebelLv());
                            playerOnHook.addDropAward(dropAwards);
                            //如果每次剩余剿灭叛军数量为0则赋值为当前每次请求剿灭数量
                            if (playerOnHook.getAskLastAnnihilateNumber() == 0) {
                                playerOnHook.setAskLastAnnihilateNumber(playerOnHook.getAskAnnihilateNumber());
                            }
                            playerOnHook.setAskLastAnnihilateNumber(playerOnHook.getAskLastAnnihilateNumber() - 1);
                            if (playerOnHook.getAskLastAnnihilateNumber() == 0) {
                                playerOnHook.setAskAnnihilateNumber(0);
                            }
                            //如果不是停止挂机状态且每次请求剿灭叛军阈值和每次剩余剿灭叛军数量等于0则表示已经到达每次清缴上限
                            if (playerOnHook.getState() != ONHOOK_TYPE[0]
                                    && playerOnHook.getAskAnnihilateNumber() == 0 && playerOnHook.getAskLastAnnihilateNumber() == 0) {
                                playerOnHook.setState(ONHOOK_TYPE[0]);
                            }
                            this.syncOnHookData(player);
//                            LogUtil.error(">>>>>>>>>>>>>>>>>>>>>>>挂机叛军 : " + JSON.toJSONString(playerOnHook));
                        }
                    }
                }
            } else {
                if (playerOnHook.getState() != ONHOOK_TYPE[0]) {
                    playerOnHook.setState(ONHOOK_TYPE[0]);
                    //如果挂机卡失效则清除
                    playerOnHook.setAskAnnihilateNumber(0);
                    playerOnHook.setAskLastAnnihilateNumber(0);
                    this.syncOnHookData(player);
                }
            }
        } catch (Exception e) {
            LogUtil.error("Check Month Card Occur Error, roleId=" + player.roleId + ", ", e);
        }
    }

    private void syncOnHookData(Player player) {
        if (Objects.nonNull(player) && player.isLogin) {
            SyncOnHookDataRs.Builder builder = SyncOnHookDataRs.newBuilder();
            builder.setWipeoutNum(player.getBanditCnt());
            builder.addAllDrops(PbHelper.createAwards(player.getPlayerOnHook().getDrops()));
            builder.setState(player.getPlayerOnHook().getState());
            player.getPlayerOnHook().getArmys().entrySet().forEach(entry -> builder.addArmys(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
            builder.setCountDown(getOnHookCountDown(player.getPlayerOnHook()));
            if (Objects.nonNull(player.getPlayerOnHook().getAskAnnihilateNumber())) {
                builder.setAskAnnihilateNumber(player.getPlayerOnHook().getAskAnnihilateNumber());
            }
            if (Objects.nonNull(player.getPlayerOnHook().getAskLastAnnihilateNumber())) {
                builder.setAskLastAnnihilateNumber(player.getPlayerOnHook().getAskLastAnnihilateNumber());
            }
            BasePb.Base msg = PbHelper.createSynBase(SyncOnHookDataRs.EXT_FIELD_NUMBER, SyncOnHookDataRs.ext, builder.build()).build();
            this.syncMsgToPlayer(msg, player);
        }
    }

    /**
     * 挂机 - 获取信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public OnHookGetInfoRs onHookGetInfoRs(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        PlayerOnHook playerOnHook = player.getPlayerOnHook();
        OnHookGetInfoRs.Builder resp = OnHookGetInfoRs.newBuilder();
        resp.setMaxRebelLv(playerOnHook.getMaxRebelLv());
        playerOnHook.getArmys().entrySet().forEach(entry -> resp.addArmys(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
        playerOnHook.getDrops().forEach(o -> resp.addDrops(PbHelper.createAward(o)));
        resp.setWipeoutNum(player.getBanditCnt());
        FunCard funCard = player.funCards.get(FunCard.CARD_TYPE[8]);
        resp.setCardExpire(Objects.isNull(funCard) ? 0 : funCard.getExpire());
        resp.setState(playerOnHook.getState());
        resp.setCurRebelLv(playerOnHook.getCurRebelLv());
        resp.setCountDown(getOnHookCountDown(playerOnHook));
        //如果没有买挂机卡或者最后一次战斗的时间戳为0
        if (Objects.isNull(funCard) || playerOnHook.getLastStamp() == 0) {
            resp.setAskAnnihilateNumber(0);
            resp.setAskLastAnnihilateNumber(0);
        } else {
            //当前时间
            LocalDateTime sysDateTime = LocalDateTime.now();
            //最后一次挂机的时间戳
            LocalDateTime lastDateTime = LocalDateTime.ofEpochSecond(playerOnHook.getLastStamp(), 0, ZoneOffset.of("+8"));
            Duration duration = Duration.between(lastDateTime, sysDateTime);
            //如果跨天了则重置剩余剿灭次数
            if (duration.toDays() >= 1) {
                resp.setAskAnnihilateNumber(0);
                resp.setAskLastAnnihilateNumber(0);
            } else {
                resp.setAskAnnihilateNumber(playerOnHook.getAskAnnihilateNumber());
                resp.setAskLastAnnihilateNumber(playerOnHook.getAskLastAnnihilateNumber());
            }
        }
        return resp.build();
    }

    /**
     * 挂机 - 补兵
     *
     * @param roleId
     * @param armyType
     * @return
     * @throws MwException
     */
    public OnHookReplenishRs onHookReplenishRs(long roleId, int armyType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        EArmyType eArmyType = EArmyType.get(armyType);
        if (Objects.isNull(eArmyType)) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "roleId=" + roleId + ", armyType=" + armyType);
        }
        int total = playerDataManager.getArmCount(player.resource, armyType);
        if (total <= 0) {
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_REPLENISH_INSUFFICIENT, player);
            throw new MwException(GameError.ARM_COUNT_ERROR.getCode(), "roleId=" + roleId + ", armyType=" + armyType);
        }
        int curr = player.getPlayerOnHook().getArmys().getOrDefault(armyType, 0);
        int need = Constant.ONHOOK_1064 - curr;
        int add;
        if (need > 0) {
            if (total >= need) {
                add = need;
            } else {
                add = total;
            }
            if (add > 0) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.ARMY, armyType, add, AwardFrom.ONHOOK_REPLENISH);
                player.getPlayerOnHook().getArmys().merge(armyType, add, Integer::sum);
            }
        }

        OnHookReplenishRs.Builder resp = OnHookReplenishRs.newBuilder();
        player.getPlayerOnHook().getArmys().entrySet().forEach(entry -> resp.addArmys(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
        return resp.build();
    }

    private static final int[] ONHOOK_TYPE = {0, 1};//[0]停止挂机[1]开始挂机

    /**
     * 挂机 - 开始挂机/停止挂机
     *
     * @param roleId
     * @param type
     * @param anniNumberThreshold 剿灭叛军阈值
     * @return
     * @throws MwException
     */
    public OnHookOperateRs onHookOperateRs(long roleId, int type, int rebelLv, int anniNumberThreshold) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (Arrays.binarySearch(ONHOOK_TYPE, type) < 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "type值错误, roleId=" + roleId + ", type=" + type);
        }
        PlayerOnHook playerOnHook = player.getPlayerOnHook();
        if (rebelLv < 1 || rebelLv > playerOnHook.getMaxRebelLv()) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "叛军等级值错误, roleId=" + roleId + ", type=" + type + ", rebelLv=" + rebelLv);
        }
        if (type == ONHOOK_TYPE[0]) {
            if (playerOnHook.getState() != ONHOOK_TYPE[0]) {
                playerOnHook.setState(ONHOOK_TYPE[0]);
            }
        } else if (type == ONHOOK_TYPE[1]) {
            if (anniNumberThreshold == 0) {
                throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "剿灭叛军阈值错误, roleId=" + roleId + ", type=" + type + ", rebelLv=" + rebelLv);
            }
            long sumArmys = playerOnHook.getArmys().values().stream().collect(Collectors.summarizingInt(x -> x.intValue())).getSum();
            if (sumArmys < Constant.ONHOOK_1061) {
                throw new MwException(GameError.ONHOOK_RUN_ARMY_NOENOUGHT.getCode(), "挂机兵力不足, roleId=" + roleId);
            }
            if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
                throw new MwException(GameError.ONHOOK_RUN_NON_DAYTIMES.getCode(), "次数用完, roleId=" + roleId);
            }
            if (!checkCardUnexpired(player, FunCard.CARD_TYPE[8])) {
                throw new MwException(GameError.ONHOOK_CARD_EXPIRE.getCode(), "特权卡到期, roleId=" + roleId);
            }
            if (playerOnHook.getState() != ONHOOK_TYPE[1]) {
                playerOnHook.setState(ONHOOK_TYPE[1]);
                playerOnHook.setCurRebelLv(rebelLv);
                playerOnHook.setLastStamp(TimeHelper.getCurrentSecond());
                playerOnHook.setAskAnnihilateNumber(anniNumberThreshold);
                playerOnHook.setAskLastAnnihilateNumber(anniNumberThreshold);
            }
        }
        OnHookOperateRs.Builder resp = OnHookOperateRs.newBuilder();
        resp.setState(playerOnHook.getState());
        resp.setCountDown(getOnHookCountDown(playerOnHook));
        return resp.build();
    }

    private int getOnHookCountDown(PlayerOnHook playerOnHook) {
        return playerOnHook.getLastStamp() + Constant.ONHOOK_1063;
    }

    /**
     * 挂机 - 领取奖励
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public OnHookGetAwardRs onHookGetAwardRs(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        PlayerOnHook playerOnHook = player.getPlayerOnHook();
        if (playerOnHook.getDrops().isEmpty()) {
            throw new MwException(GameError.ONHOOK_NON_AWARD.getCode(), "roleId=" + roleId);
        }

        List<Award> dropAwards = PbHelper.createAwards(playerOnHook.getDrops());
        rewardDataManager.sendRewardByAwardList(player, dropAwards, AwardFrom.ONHOOK_GET_DROP_AWARD);

        playerOnHook.getDrops().clear();

        OnHookGetAwardRs.Builder resp = OnHookGetAwardRs.newBuilder();
        resp.addAllGot(dropAwards);
        playerOnHook.getDrops().forEach(drop -> resp.addDrops(PbHelper.createAward(drop)));
        return resp.build();
    }

    // <editor-fold desc="自测挂机功能的代码" defaultstate="collapsed">
    public void test_onHook(Player player, String... params) throws Exception {
        if (params[0].equalsIgnoreCase("armys")) {
            player.getPlayerOnHook().getArmys().entrySet().forEach(entry -> entry.setValue(Integer.parseInt(params[1])));
        }
        if (params[0].equalsIgnoreCase("subarmys")) {
            long s1 = System.currentTimeMillis();
            marchService.onHookSubArmy(player, Integer.parseInt(params[1]));
        }
        if (params[0].equalsIgnoreCase("getinfo")) {
            LogUtil.c2sMessage(this.onHookGetInfoRs(player.roleId), player.roleId);
        }
        if (params[0].equalsIgnoreCase("setexpire")) {
            player.funCards.get(FunCard.CARD_TYPE[8]).setExpire(TimeHelper.getCurrentSecond() + Integer.parseInt(params[1]));
            syncOnHookData(player);
        }
        if (params[0].equalsIgnoreCase("hit")) {
            PlayerOnHook playerOnHook = player.getPlayerOnHook();
            List<Award> dropAwards = marchService.onHookBandit(player, Integer.parseInt(params[1]));
            playerOnHook.addDropAward(dropAwards);
            this.syncOnHookData(player);
        }
    }
// </editor-fold>


    public void syncMsgToCamp(BasePb.Base msg, int... camps) {
        playerDataManager.getAllOnlinePlayer().values().forEach(p -> {
            if (ListUtils.contains(camps, p.getCamp())) MsgDataManager.getIns().add(new Msg(p.ctx, msg, p.roleId));
        });
    }

    public void syncMsgToPlayer(BasePb.Base msg, long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        this.syncMsgToPlayer(msg, player);
    }

    public void syncMsgToPlayer(BasePb.Base msg, Player player) {
        if (Objects.nonNull(player) && player.isLogin) {
            MsgDataManager.getIns().add(new Msg(player.ctx, msg, player.roleId));
        }
    }

    public void syncMsgToAll(BasePb.Base msg) {
        playerDataManager.getAllOnlinePlayer().values().forEach(p -> MsgDataManager.getIns().add(new Msg(p.ctx, msg, p.roleId)));
    }

    /**
     * 推送消息给满足条件的玩家
     *
     * @param msg
     * @param function
     * @param params
     */
    public void syncMsgToAll(BasePb.Base msg, Function<Map, Boolean> function, Map params) {
        playerDataManager.getAllOnlinePlayer().values().forEach(p -> {
            params.put("player", p);
            if (function.apply(params)) {
                MsgDataManager.getIns().add(new Msg(p.ctx, msg, p.roleId));
            }
        });
    }

    public String getInitName() {
        if (StringUtils.isEmpty(initName))
            return "主公@";
        return initName;
    }

    @GmCmd("player")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "lv160":
                List<List<Integer>> list = ListUtils.createItems(AwardType.MONEY, AwardType.Money.EXP, Integer.MAX_VALUE);
                rewardDataManager.sendReward(player, list, AwardFrom.DO_SOME);

                Field[] fields = player.building.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (!field.getName().equals("lordId")) {
                        field.set(player.building, 32);
                    }
                }
                Stream.iterate(1, n -> n + 1).limit(36).forEach(id -> {
                    for (; ; ) {
                        TechLv techLv = player.tech.getTechLv().computeIfAbsent(id, v -> new TechLv(id, 0, 1));
                        StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv() + 1);
                        if (Objects.isNull(staticTechLv)) {
                            break;
                        }
                        techLv.setLv(staticTechLv.getLv());
                    }
                });
                break;
            default:
        }
    }
}
