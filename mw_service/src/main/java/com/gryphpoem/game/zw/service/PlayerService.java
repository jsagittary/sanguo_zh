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
import com.gryphpoem.game.zw.resource.domain.s.StaticIniLord;
import com.gryphpoem.game.zw.resource.domain.s.StaticPay;
import com.gryphpoem.game.zw.resource.domain.s.StaticRecommend;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.domain.s.StaticTechLv;
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
     * ????????????????????????????????????
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
            // ????????????
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
            campTurple = playerDataManager.getRecommendCamp(); // ????????????????????????
            LogUtil.debug("=====???????????????===== camp:", campTurple.getA(), ", ????????? keyId:", campTurple.getB());
            account.setRecommendCamp(campTurple.getA());// ??????????????????
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
                // ??????????????????????????????,????????????account
                if (smallIdManager.isSmallId(dbAccount.getLordId())) {
                    campTurple = playerDataManager.getRecommendCamp(); // ????????????????????????
                    account.setRecommendCamp(campTurple.getA());// ??????????????????
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

            // ??????????????????
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

        if (account.getCreated() == LoginConstant.ROLE_CREATED) {// ???????????????
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
     * ??????????????????????????????????????????
     *
     * @param nick
     * @return true???????????????
     */
    private boolean nickIsIllegal(String nick) {
        return CheckNull.isNullTrim(nick) || nick.length() >= 16 || EmojiHelper.containsEmoji(nick)
                || EmojiHelper.containsSpecialSymbol(nick);
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param nick
     * @return true???????????????
     */
    private boolean ReNickIsIllegal(String nick) {
        return CheckNull.isNullTrim(nick) || nick.length() >= 16 || EmojiHelper.containsEmoji(nick)
                || EmojiHelper.ReNickContainsSpecialSymbol(nick);
    }

    /**
     * ????????????
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
     * ??????????????????????????????
     */
    @Deprecated
    public void checkHasHeroGivePortrait() {
//        playerDataManager.getPlayers().values().forEach(p -> {
//            Constant.USE_HERO_PORTRAIT.forEach((heroId, portraitId) -> {
//                if (p.heros.containsKey(heroId) && !p.portraits.contains(portraitId)) {// ??????????????????
//                    rewardDataManager.addAward(p, AwardType.PORTRAIT, portraitId, 1, AwardFrom.VIP_GIFT_BUY);
//                }
//            });
//        });
    }


    /**
     * ??????????????????
     *
     * @param req
     * @param roleId
     * @param heroSkin ?????????????????????
     * @return
     * @throws MwException
     */
    public Base.Builder createRole(CreateRoleRq req, long roleId, int heroSkin) throws MwException {
        int state;
        CreateRoleRs.Builder builder = CreateRoleRs.newBuilder();
        Player newPlayer = playerDataManager.getNewPlayer(roleId);
        if (newPlayer == null) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "???????????????????????????????????????, roleId:" + roleId);
        }
        int tjCamp = 0;
        if (req.hasCampAward()) {
            // ?????????????????????????????????????????????(????????????)
            tjCamp = req.getCampAward();
        }
        int now = TimeHelper.getCurrentSecond();
        if (newPlayer.account.getCreated() == LoginConstant.ROLE_CREATED) {
            state = LoginConstant.CREATE_STATE_CREATED;
        } else {
            int camp = req.getCamp();// ?????????????????????
            if (camp < 0 || camp > Constant.Camp.UNION) {
                throw new MwException(GameError.NO_CAMP.getCode(), "??????????????????????????????, roleId:" + roleId + ", camp:" + camp);
            }
            // ?????????????????????????????????
            camp = checkAndTransformCamp(newPlayer.account.getServerId(), camp);

            String nick = "";
            if (req.hasNick()) {
                nick = req.getNick();
            } else {
                // nick = playerDataManager.getFreeManName();// ??????????????????
                nick = newPlayer.getName();
            }
            if (nickIsIllegal(nick)) {
                throw new MwException(GameError.NICK_NAME_ERR.getCode(), "?????????????????? roleId:", roleId, ", name:", nick);
            }
            nick = nick.trim();
            if (!playerDataManager.canUseName(nick)) {
                // throw new MwException(GameError.SAME_NICK.getCode(), "???????????????");
                // ?????????????????????????????????
                nick = newPlayer.getName(now);
            }

            if (playerDataManager.takeNick(nick)) {
                // ????????????????????????
                StaticIniLord ini = StaticIniDataMgr.getLordIniData();
                // ?????????????????????????????????????????????
                boolean isEnd = false;
                List<CommonPb.LifeSimulatorStep> lifeSimulatorStepList = req.getLifeSimulatorStepList();
                List<List<Integer>> finalRewardList = new ArrayList<>();
                List<List<Integer>> finalCharacterFixList = new ArrayList<>();
                int portrait = ini.getPortrait();
                for (CommonPb.LifeSimulatorStep lifeSimulatorStep : lifeSimulatorStepList) {
                    int chooseId = lifeSimulatorStep.getChooseId();
                    if (chooseId > 0) {
                        StaticSimulatorChoose sSimulatorChoose = StaticBuildCityDataMgr.getStaticSimulatorChoose(chooseId);
                        // ???????????????
                        List<List<Integer>> characterFix = sSimulatorChoose.getCharacterFix();
                        finalCharacterFixList.addAll(characterFix);
                        List<List<Integer>> rewardList = sSimulatorChoose.getRewardList();
                        List<Integer> portraitAward = rewardList.stream().filter(tmp -> tmp.size() == 4 && tmp.get(0) == AwardType.PORTRAIT).findFirst().orElse(null);
                        if (portraitAward != null) {
                            portrait = portraitAward.get(1); // ?????????????????????, ??????????????????id, ??????s_portrait???id
                        }
                        finalRewardList.addAll(rewardList);
                        // TODO ??????buff??????
                        List<List<Integer>> buff = sSimulatorChoose.getBuff();
                    }
                    long stepId = lifeSimulatorStep.getStepId();
                    StaticSimulatorStep staticSimulatorStep = StaticBuildCityDataMgr.getStaticSimulatorStep(stepId);
                    // ????????????, ???????????????????????????
                    if (!isEnd) {
                        long nextId = staticSimulatorStep.getNextId();
                        List<List<Long>> staticChooseList = staticSimulatorStep.getChoose();
                        boolean isExistForwardStep = staticChooseList.stream().anyMatch(temp -> temp.get(0) == (long) chooseId && temp.get(1) == 0L);
                        if (nextId == 0L && isExistForwardStep) {
                            isEnd = true;
                        }
                    }
                }
                int recommendCamp = tjCamp != 0 ? tjCamp : newPlayer.lord.getCamp();// ?????????????????????????????????????????????(????????????)
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
                    // ????????????????????????
                    rewardDataManager.sendReward(player, Constant.REGISTER_REWARD, AwardFrom.REGISTER_REWARD);
                    if (!Constant.MAIL_FOR_CREATE_ROLE.isEmpty()) {
                        for (List<Integer> param : Constant.MAIL_FOR_CREATE_ROLE) {
                            if (param.size() > 1) {
                                // ????????????
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
                    // ??????????????????
                    if (camp == recommendCamp) {
                        StaticRecommend sr = StaticLordDataMgr
                                .getRecommendCampById(playerDataManager.getRecommendKeyId());
                        List<Award> awards = PbHelper.createAwardsPb(sr.getAward());
                        // ???????????????????????????????????????
                        mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_RECOMMEND_CAMP_REWARD,
                                AwardFrom.RECOMMEND_CAMP_REWARD, now, camp, camp);
                    }
                    // ????????????????????????
                    newPlayer.heroSkin = heroSkin;

                    // ????????????????????????, ???????????????
                    if (isEnd) {
                        // ????????????????????????????????????
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
                        // ????????????????????????
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
                                        rewardDataManager.subPlayerResCanSubCount(player, awardType, awardId, awardCount, AwardFrom.SIMULATOR_CHOOSE_REWARD, "");
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
                    throw new MwException(GameError.SERVER_EXCEPTION.getCode(), "????????????????????????, roleId:" + roleId);
                }
            } else {
                state = LoginConstant.CREATE_STATE_FAIL;
            }
        }

        builder.setState(state);
        return PbHelper.createRsBase(CreateRoleRs.EXT_FIELD_NUMBER, CreateRoleRs.ext, builder.build());
    }

    /**
     * ????????????????????????
     *
     * @param serverId
     * @param camp
     * @return
     * @throws MwException
     */
    private int checkAndTransformCamp(final int serverId, final int camp) throws MwException {
        int tCamp = camp;
        List<TargetServerCamp> allowJoinServerIdCampList = serverSetting.getAllowJoinServerIdCampList();
        // 0 ????????????
        if (camp == 0) {
            TargetServerCamp ts = allowJoinServerIdCampList.stream().filter(tsc -> tsc.getOriginServerId() == serverId)
                    .findAny().orElse(null);
            if (ts == null) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "???????????????????????????  serverId:", serverId, ", camp:",
                        camp, ", allowServerIdCamp:", serverSetting.getAllowJoinServerCampStr());
            }
            tCamp = ts.getCamp();
        } else {
            long count = allowJoinServerIdCampList.stream()
                    .filter(tsc -> tsc.getOriginServerId() == serverId && tsc.getCamp() == camp).count();
            if (count <= 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "?????????????????????  serverId:", serverId, ", camp:", camp,
                        ", allowServerIdCamp:", serverSetting.getAllowJoinServerCampStr());
            }
        }
        return tCamp;
    }

    /**
     * ??????????????????
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
            throw new MwException(GameError.INVALID_PARAM.getCode(), "??????????????????????????????????????????????????????");
        }

        if (player.account.getCreated() != LoginConstant.ROLE_CREATED) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "??????????????????????????????????????????????????????");
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
                // ??????
                syncLoginState(preCtx, roleId, 1);
                preCtx.close();
            }
        } else {
            player.setLogin(true);
            playerDataManager.addOnline(player);
            sendOnlineChat(player.lord);
            // ?????????????????????,??????????????????
            /*Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_ONLINE_GIFT);
            if (lastOLDay != currentDay && activity != null && CheckNull.isEmpty(activity.getStatusCnt())) {
                activityDataManager.updActivity(player, ActivityConst.ACT_ONLINE_GIFT, TimeHelper.getCurrentSecond(),
                        0);
                activity.getPropMap().put(0, player.lord.getLevel());
            }*/
            int nowDay = TimeHelper.getCurrentDay();
            int lastDay = TimeHelper.getDay(player.lord.getOnTime());
            if (nowDay != lastDay) {
                //???????????????
                titleService.processTask(player, ETask.LOGIN_DAYS_SUM);
            }
            player.logIn();
            // ???????????????????????? ????????????????????????
            SendRoleInfosRq.Builder roleInfoBuilder = SendRoleInfosRq.newBuilder();
            roleInfoBuilder.setAccountKey(player.account.getAccountKey());
            roleInfoBuilder.setRoleId(player.roleId);
            roleInfoBuilder.setServerId(player.account.getServerId());
            roleInfoBuilder.setLevel(String.valueOf(player.lord.getLevel()));
            roleInfoBuilder.setRoleName(player.lord.getNick());
            roleInfoBuilder.setServerName(serverSetting.getServerName());
            map.put("sendRoleInfosRq", roleInfoBuilder.build());
        }
        //???????????????????????????????????????(???????????????????????????)
//        castleSkinService.loginAfter(player);
        //??????????????????????????????
        Optional.ofNullable(loginServices).ifPresent(services -> services.forEach(service -> service.afterLogin(player)));
        //????????????????????????????????????(??????????????????????????????????????????????????????????????????????????????????????????)
        CalculateUtil.reCalcAllHeroAttr(player);


        // ????????????????????????
        sendLoginRewardMail(player, TimeHelper.getCurrentSecond());
        player.enterWorldCnt = 0;// ????????????????????????????????????
        //??????40????????????????????????rpc-player ?????????
        dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        // ?????????vip
        // Calendar now = Calendar.getInstance();
        // int day = now.get(Calendar.DAY_OF_MONTH);
        // int vip = player.lord.getVip();
        // if (vip < day - 4) {
        // player.lord.setVip(day - 4);
        // }
        //????????????????????????
        LogLordHelper.logLogin(player);
        //??????????????????????????????
        LogLordHelper.logLord(player);
        LogLordHelper.commonLog("fightingChange", AwardFrom.COMMON, player, player.lord.getFight());
        map.put("roleLoginRs", builder.build());
        return map;
    }

    /**
     * ????????????????????????
     *
     * @param preCtx
     * @param roleId
     * @param state  1 ??????????????????????????????
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
     * ????????????
     */
    public void acrossTheDayProcess() {
        try {
            int now = TimeHelper.getCurrentSecond();
            LocalDateTime localDateTime = LocalDateTime.now();
            // ????????????????????????
            Java8Utils.invokeNoExceptionICommand(() -> {
                HonorDaily honorDaily = honorDailyService.getHonorDaily();
                if (!CheckNull.isNull(honorDaily)) {
                    honorDaily.getDailyReports().clear();
                }
            });
            // ????????????????????????
            Java8Utils.invokeNoExceptionICommand(() -> {
                globalDataManager.getGameGlobal().cleanMixtureData(GlobalConstant.getCleanList());
            });

            // ????????????????????????
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
                        LogLordHelper.logLogin(p);// ??????????????????
                    }
                    p.setCollectMineCount(0);//????????????????????????????????????
                    p.setBanditCnt(0); // ??????????????????
                    p.setMedalGoodsRefNum(0);// ????????????????????????????????????
                    p.setPeacekeepingForcesNum(0);// ??????????????????-???????????? ????????????
                    Java8Utils.invokeNoExceptionICommand(() -> p.getStoneInfo().cleanCnt());// ?????????????????????????????? ??? ??????????????????
                    Java8Utils.invokeNoExceptionICommand(() -> {
                        List<Integer> cleanList = PlayerConstant.getCleanList();
                        // ?????????????????????????????????
                        cleanList.add(localDateTime.getDayOfWeek().getValue() + PlayerConstant.RECENTLY_PAY);
                        p.cleanMixtureData(cleanList);
                    });// ????????????
                    Java8Utils.invokeNoExceptionICommand(() -> taskDataManager.refreshDailyTask(p));// ??????????????????
                    Java8Utils.invokeNoExceptionICommand(
                            () -> taskDataManager.updTask(p, TaskType.COND_MONTH_CARD_STATE_45, 1));
                    Java8Utils.invokeNoExceptionICommand(() -> taskDataManager.updTask(p, TaskType.COND_LOGIN_36, 1));
                    Java8Utils.invokeNoExceptionICommand(() -> activityService.checkAndSendPay7DayMail(p));// 7??????????????????????????????
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
                    // ??????????????????
                    if (StaticFunctionDataMgr.funcitonIsOpen(p, MedalConst.MEDAL_SYS_LOCK)) {
                        medalDataManager.initMedalGoods(p, MedalConst.MEDAL_GOODS_0_TYPE);
                    }
                    // ??????????????????
                    Java8Utils.invokeNoExceptionICommand(() -> FriendService.rmInvalidFriend(p));
                    // ?????????????????????
                    Java8Utils.invokeNoExceptionICommand(() -> ciaService.refreshCntAndTime(p));

                    //???????????????????????????????????????
                    Java8Utils.invokeNoExceptionICommand(() -> activityDiaoChanService.handleAcrossDay(p));

                    //??????????????????????????????
                    seasonService.resetTreasuryDataInResetTime(p);
                    seasonService.resetTreasuryDataInAwardTime(p);

                    //??????????????????
                    activityTemplateService.execActivityDay(p);
                    // ??????????????????
                    treasureCombatService.acrossTheDayProcess(p);
                    // ????????????????????????
                    challengePlayerService.acrossTheDayProcess(p);
                    // ????????????????????????
                    Java8Utils.invokeNoExceptionICommand(() -> drawCardPlanTemplateService.execFunctionDay(p));
                    // ???????????????????????????
                    lifeSimulatorService.assignCityEventToPlayerJob(p);
                } catch (Exception e) {
                    LogUtil.error("roleId: ", p.roleId, ", ????????????????????????: ", e);
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     */
    private void sendLoginRewardMail(Player player, int now) {
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LOGIN_EVERYDAY);
        if (activity != null && activity.getStatusCnt().isEmpty()) {
            int pLv = player.lord.getLevel();
            activity.getStatusCnt().put(0, (long) pLv); // ??????????????????
        }
        activityDataManager.syncActChange(player, ActivityConst.ACT_LOGIN_EVERYDAY);
        // ????????????????????????
        // Date last = new Date(player.loginRewardTime * 1000L);
        // Date nowDate = new Date(now * 1000L);
        // if (DateUtils.isSameDay(last, nowDate)) {// ??????????????????
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
     * ??????????????????
     *
     * @param lord
     */
    private void sendOnlineChat(Lord lord) {
        int nowDay = TimeHelper.getCurrentDay();
        int lastDay = TimeHelper.getDay(lord.getOnTime());
        if (nowDay != lastDay) {
            // ????????????,???????????????
            if (PartyConstant.Job.KING == lord.getJob()) {// ?????????
                chatDataManager.sendSysChat(ChatConst.CHAT_KING_LOGIN, lord.getCamp(), 0, lord.getCamp(),
                        lord.getNick());
                LogUtil.debug("lordId:", lord.getLordId(), " ??????:", lord.getCamp(), " ?????????????????????", ", now:", nowDay,
                        " lastDay:", lastDay);
            } else if (PartyConstant.Job.COMMISSAR == lord.getJob()) {// ??????
                chatDataManager.sendSysChat(ChatConst.CHAT_PRIME_LOGIN, lord.getCamp(), 0, lord.getNick(),
                        lord.getCamp());
                LogUtil.debug("lordId:", lord.getLordId(), " ??????:", lord.getCamp(), " ??????????????????", ", now:", nowDay,
                        " lastDay:", lastDay);
            } else if (PartyConstant.Job.CHIEF == lord.getJob()) {// ?????????
                chatDataManager.sendSysChat(ChatConst.CHAT_ADVISER_LOGIN, lord.getCamp(), 0, lord.getNick(),
                        lord.getCamp());
                LogUtil.debug("lordId:", lord.getLordId(), " ??????:", lord.getCamp(), " ?????????????????????", ", now:", nowDay,
                        " lastDay:", lastDay);
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param roleId
     * @return
     */
    public GetLordRs getLord(long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            Lord lord = player.lord;
            // ???????????????????????????
            CalculateUtil.reCalcFight(player);
            // rankDataManager.setFight(lord);
            rankDataManager.setStars(lord);

            // ??????????????????VIP??????
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
            // ??????????????????
            builder.setAutoBuildCnt(player.common.getAutoBuildCnt());
            builder.setAutoBuildOnOff(player.common.getAutoBuildOnOff());
            builder.setHeroSkin(player.heroSkin);// ???????????????????????????
            builder.setIsFireState(player.isFireState());
            Date createDate = player.account.getCreateDate();
            int createRoleTime = (int) (createDate.getTime() / 1000);
            builder.setCreateRoleTime(createRoleTime);
            builder.setPaySumAmoumt(player.getPaySumAmoumt());
            builder.setCiaIsOpen(player.getCia() != null);
//            builder.setCiaOpenTime(DateHelper.afterDayTime(createDate, Constant.OPEN_CIA_TIME));
            builder.setArea(player.lord.getArea());
            for (Entry<Integer, Integer> kv : player.getMixtureData().entrySet()) {
                if (PlayerConstant.isInShowClientList(kv.getKey())) {// ?????????????????????????????????
                    builder.addMixtureData(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
                }
            }
            DecisiveInfo decisiveInfo = player.getDecisiveInfo();
            if (decisiveInfo != null) {
                // ?????????????????????????????????
                int fireTimeEnd = decisiveInfo.getFlyTime() + WorldConstant.DECISIVE_BATTLE_FINAL_TIME.get(2);
                builder.setBlazeTime(fireTimeEnd);
                // ??????????????????????????????
                int whiteTimeEnd = decisiveInfo.getFlyTime() + WorldConstant.DECISIVE_BATTLE_FINAL_TIME.get(0);
                builder.setWhiteFlagTime(whiteTimeEnd);
            }
            // ????????????
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
            return builder.build();
        }
        return null;
    }


    /**
     * ????????????????????????
     *
     * @param roleId ????????????id
     * @return ??????????????????
     * @throws MwException ???????????????
     */
    public GetMixtureDataRs getMixtureData(long roleId) throws MwException {
        GetMixtureDataRs.Builder builder = GetMixtureDataRs.newBuilder();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        for (Entry<Integer, Integer> kv : player.getMixtureData().entrySet()) {
            if (PlayerConstant.isInShowClientList(kv.getKey())) {// ?????????????????????????????????
                builder.addMixtureData(PbHelper.createTwoIntPb(kv.getKey(), kv.getValue()));
            }
        }
        return builder.build();
    }

    /**************************** ???????????? start *********************************/
    /**
     * @Title: getOffLineIncome @Description: ????????????????????????????????? @param roleId @return ?????? OffLineIncomeRs ???????????? @throws
     */
    public OffLineIncomeRs getOffLineIncome(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        OffLineIncomeRs.Builder builder = OffLineIncomeRs.newBuilder();

        // ????????????????????????
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.OFF_LINE_INCOME_LOCK_LV)) {
            return builder.build();
        }

        // ??????????????????????????????
        List<Award> awards = player.awards.get(Constant.AwardType.TYPE_1);
        if (awards != null) {
            builder.addAllAward(awards);
        }

        // ?????????????????????????????????
        int elecOut = 0;// ??????
        int foodOut = 0;// ??????
        int oilOut = 0;// ??????
        int oreOut = 0;// ??????
        Iterator<Mill> it2 = player.mills.values().iterator();
        while (it2.hasNext()) {
            Mill mill = it2.next();
            // ???????????????????????????
            if (!buildingDataManager.checkMillCanGain(player, mill)) {
                continue;
            }
            int resCnt = mill.getResCnt();// ??????????????????
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
            // ????????????
            int gain = buildingDataManager.getResourceMult(player, mill.getType(), resOuts.get(2));

            LogUtil.debug(player.roleId + ",????????????????????????=" + gain + ",????????????=" + mill.getType() + ",????????????=" + resCnt + ",????????????="
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

        // ?????????????????????????????????
        int chariot = getArmByFactoryType(player, BuildingType.FACTORY_1);// ??????
        int tank = getArmByFactoryType(player, BuildingType.FACTORY_2);// ??????
        int rocket = getArmByFactoryType(player, BuildingType.FACTORY_3);// ??????

        // ????????????????????????
        for (int type : Arrays.asList(BuildingType.TRAIN_FACTORY_1, BuildingType.TRAIN_FACTORY_2)) {
            BuildingExt buildingExt = player.buildingExts.get(type);
            if (buildingExt != null && buildingExt.isUnlock()) {
                int num = getArmByFactoryType(player, type);// ?????????????????????
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

        // ?????????????????????????????????
        Tech tech = player.tech;
        if (tech == null) {
            tech = new Tech();
            player.tech = tech;
        }
        TechQue que = tech.getQue();
        int now = TimeHelper.getCurrentSecond();
        if (que != null && que.getId() > 0 && que.getEndTime() > 0 && now >= que.getEndTime()) {// ?????????????????? ?????? ?????????
            if (!tech.getTechLv().containsKey(que.getId())) {
                tech.getTechLv().put(que.getId(), new TechLv(que.getId(), 0, 1));
            }
            TechLv techLv = tech.getTechLv().get(que.getId());
            builder.setTech(PbHelper.createTechLv(techLv));
        }

        // ?????????????????????????????????
        if (player.equipQue != null && !player.equipQue.isEmpty()) {// ????????????
            // ?????????????????????????????????
            EquipQue buildQue = player.equipQue.get(0);
            if (buildQue != null && now >= buildQue.getEndTime()) {
                Equip equip = new Equip();
                equip.setKeyId(buildQue.getKeyId());
                equip.setEquipId(buildQue.getEquipId());
                builder.setEquip(PbHelper.createEquipPb(equip));
            }
        }

        // ?????????????????????????????????
        Chemical chemical = player.chemical;
        Map<Integer, ChemicalQue> map = new HashMap<Integer, ChemicalQue>();// key ??????id
        if (chemical != null && chemical.getPosQue() != null && !chemical.getPosQue().isEmpty()) {// ??????????????? ??? ?????????????????????
            Iterator<String> it = chemical.getPosQue().keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                ChemicalQue v = chemical.getPosQue().get(key);
                if (v != null && now >= v.getEndTime()) {
                    // ????????????????????????
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

        // ????????????????????? ??????
        Map<Integer, OffLineBuild> offLineBuilds = player.offLineBuilds;
        if (offLineBuilds != null) {
            builder.addAllOffLineBuilds(offLineBuilds.values());
            player.offLineBuilds = null;
        }

        // ????????????????????????
        builder.setOffLineTime(player.lord.getOnTime() - player.lord.getOffTime());

        return builder.build();
    }

    /**
     * @param map
     * @param v
     * @return boolean true ???
     * @Title: checkChemical
     * @Description: ?????????????????????????????????
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
     * @Title: getArmByFactoryType @Description: ???????????????????????? ?????????????????? @param player @param type @return ?????? int ???????????? @throws
     */
    public int getArmByFactoryType(Player player, int type) {
        Factory factory = player.factory.get(type);
        int buildingLv = BuildingDataManager.getBuildingLv(type, player);
        BuildingExt ext = player.buildingExts.get(type);
        int addNum = 0;
        if (factory != null && buildingLv >= 1 && !CheckNull.isNull(ext)) {// ??????????????? ?????? ??????????????? ?????? ???????????????
            // ???????????????
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

    /**************************** ????????????end *********************************/

    /**
     * ?????????????????????????????????
     */
    public void reCalcFightAllPlayer() {
        LogUtil.debug("============================????????????????????? Start ============================");
        for (Player player : playerDataManager.getPlayers().values()) {
            try {
                long preFight = player.lord.getFight();
                CalculateUtil.reCalcBattleHeroAttr(player);
                long nowFight = player.lord.getFight();
                LogUtil.debug("roleId:", player.roleId, ", preFight:", preFight, ", nowFight:", nowFight);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error("??????????????????????????? roleId:", player.roleId);
            }
        }
        LogUtil.debug("============================????????????????????? End ============================");
    }

    /**
     * ???????????????
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
            throw new MwException(GameError.GIFT_CODE_LENTH.getCode(), "??????????????????, roleId:" + roleId + ", code:" + code);
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
     * ??????????????????????????????
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
     * ??????????????????????????????????????????
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
                    LogUtil.error("????????????????????????, roleId:" + player.roleId, e);
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
            // LogUtil.error("????????????????????????, roleId:" + player.roleId, e);
            // }
            // }
        }

        if (saveCount != 0) {
            LogUtil.save("save player count:" + saveCount);
        }
    }

    /**
     * ????????????
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
            // ??????id??????,??????id??????????????????
            try {
                player = playerDataManager.getPlayer(Long.valueOf(nick));
            } catch (NumberFormatException e) {
            }
        }
        if (player == null) {
            player = playerDataManager.getPlayer(nick);
            throw new MwException(GameError.PLAYER_NOT_EXIST.getCode(),
                    "????????????,???????????????, roleId:" + roleId + ", nick:" + nick);
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
            // buff??????
            player.refreshEffect();
            // ????????????
            if (player.isActive()) {
                playerDataManager.restoreProsAndPower(player, now);
            }
            // ????????????
            this.checkOnHook(player);
            //?????????????????????
            anniversaryEggService.refreshEgg(globalActivityDataList, player);
            //??????????????????
            Optional.ofNullable(refreshTimerServices).ifPresent(services -> services.forEach(service -> service.checkAndRefresh(player)));
        });
    }

//    /**
//     * ??????????????????????????????
//     * 1?????????????????????
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
//                LogUtil.error("??????????????????????????????????????????, lordId:" + player.lord.getLordId(), e);
//            }
//            this.checkOnHook(player);
//        }
//    }

    /**
     * ????????????????????????????????????
     *
     * @param req
     * @param roleId
     * @return
     * @throws MwException
     */
    public SetGuideRs setGuide(SetGuideRq req, long roleId) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int index = req.getIndex();
        int oldIndex = player.lord.getNewState();
        if (index > oldIndex) {// ????????????????????????
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
            // // ???????????????????????????
            // if (!staticGuidAward.getRewards().isEmpty()) {
            // builder.addAllAward(rewardDataManager.sendReward(player, staticGuidAward.getRewards(),
            // AwardFrom.GUID_AWARD, "????????????????????????"));
            // LogUtil.debug("????????????????????????");
            // }
            //
            // if (staticGuidAward.getAutoNum() > 0) {
            // player.common.setAutoBuildCnt(staticGuidAward.getAutoNum());
            // builder.setAutoNum(player.common.getAutoBuildCnt());
            // }
            // } else {
            // LogUtil.debug(roleId + ",???????????????????????????taskId=" + taskId);
            // }
            // }
            // } else {
            // if (!staticGuidAward.getRewards().isEmpty()) {
            // builder.addAllAward(rewardDataManager.sendReward(player, staticGuidAward.getRewards(),
            // AwardFrom.GUID_AWARD, "????????????????????????"));
            // LogUtil.debug("????????????????????????");
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
                LogUtil.debug("????????????????????????");
            }

            if (staticGuidAward.getAutoNum() > 0) {
                player.common.setAutoBuildCnt(staticGuidAward.getAutoNum());
                builder.setAutoNum(player.common.getAutoBuildCnt());

                player.common.setAutoBuildOnOff(1);// ??????????????????
                buildingDataManager.syncAutoBuildInfo(player);// ??????????????????
                buildingService.addAtuoBuild(player);// ??????????????????
            }
            // ???????????????id
            int nextGuideId = staticGuidAward.getNextGuideId();
            if (nextGuideId > 0 && nextGuideId > index) {
                player.lord.setNewState(nextGuideId);
            }
        }
        builder.setIndex(player.lord.getNewState());
        return builder.build();
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param name
     * @return
     * @throws MwException
     */
    public ChangeLordNameRs changeLordName(long roleId, String name) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (ChatHelper.isCorrect(name) || ReNickIsIllegal(name)) {
            throw new MwException(GameError.NICK_NAME_ERR.getCode(), "?????????????????? roleId:", roleId, ", name:", name);
        }
        name = name.trim();

        // if (player.common.getRenameCnt() < 1) { // ?????????????????????
        // // ??????????????????
        // if (!name.equals(player.lord.getNick())) {
        // // ???????????????????????????
        // if (!playerDataManager.takeNick(name)) {
        // throw new MwException(GameError.SAME_NICK.getCode(), "???????????????");
        // }
        // player.common.setRenameCnt(1);
        // playerDataManager.rename(player, name);
        // }
        // // ??????????????????
        // taskDataManager.updTask(player, TaskType.COND_31, 1);
        // } else {
        // }
        // ??????????????????
        if (!name.equals(player.lord.getNick())) { // ???????????????????????????
            if (!playerDataManager.canUseName(name)) {
                throw new MwException(GameError.SAME_NICK.getCode(), "???????????????");
            }
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.PROP_RENAME_CARD, 1,
                    AwardFrom.USE_PROP);
            if (!playerDataManager.takeNick(name)) {
                throw new MwException(GameError.SAME_NICK.getCode(), "???????????????");
            }
            playerDataManager.rename(player, name);
        }
        ChangeLordNameRs.Builder builder = ChangeLordNameRs.newBuilder();
        builder.setName(name);
        taskDataManager.updTask(player, TaskType.COND_507, 1);
        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @param signature
     * @return
     * @throws MwException
     */
    public ChangeSignatureRs changeSignature(long roleId, String signature) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, 9010)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), String.format("????????????????????????, roleId:%d, lv:%d", player.roleId, player.lord.getLevel()));
        }
        if (CheckNull.isNullTrim(signature) || signature.length() > 150 || EmojiHelper.containsEmoji(signature)) {
            throw new MwException(GameError.SIGNATURE_ERR.getCode(), "????????????????????????");
        }
        player.lord.setSignature(signature);
        LogLordHelper.commonChat("changeSignature", AwardFrom.CHANGE_SIGNATURE, player.account, player.lord, signature, serverSetting.getServerID());
        ChangeSignatureRs.Builder builder = ChangeSignatureRs.newBuilder();
        builder.setSignature(signature);
        return builder.build();
    }

    /**
     * ??????????????????
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
     * ???????????????????????????
     *
     * @param protectTime ???????????????
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
                if (oldEndTime < now) { // ????????????
                    effect.setEndTime(now + protectTime);
                } else {
                    effect.setEndTime(oldEndTime + protectTime);
                }
            }
            propService.syncBuffRs(p, effect);
            LogUtil.debug("??????????????? roleId:", p.roleId, ", endTime:", effect.getEndTime());
        });
        worldDataManager.synCleanMapRefreshChg();
    }

    /**
     * ?????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public ChangeChatBubbleRs changeChatBubble(long roleId, ChangeChatBubbleRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int bubbleId = req.getBubbleId();
        // ??????0??????????????????
        if (bubbleId != 0) {
            StaticChatBubble sChatBubble = StaticLordDataMgr.getChatBubbleMapById(bubbleId);
            if (sChatBubble == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "??????????????????????????? : bubbleId:", bubbleId, ", roleId:",
                        roleId);
            }
            int type = sChatBubble.getType();
            if (type == StaticChatBubble.TYPE_FREE) { // ???????????????
            } else if (type == StaticChatBubble.TYPE_VIP_LV) { // ??????VIP??????
                if (player.lord.getVip() < sChatBubble.getParam()) {
                    throw new MwException(GameError.VIP_NOT_ENOUGH.getCode(), "vip????????????: bubbleId:", bubbleId,
                            ", vipLv:", player.lord.getVip(), ", roleId:", roleId);
                }
            } else if (type == StaticChatBubble.TYPE_GOLD
                    || type == StaticChatBubble.TYPE_AWARD
                    || type == StaticChatBubble.TYPE_ACT_AWARD) { // ??????????????????
                if (!player.getChatBubbles().contains(bubbleId)) {
                    throw new MwException(GameError.NOT_HAS_CHAT_BUBBLE.getCode(), "????????????????????????: bubbleId:", bubbleId,
                            ", roleId:", roleId);
                }
            } else {
                throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????? :", bubbleId, ", roleId:", roleId);
            }
        }
        player.setCurChatBubble(bubbleId);
        ChangeChatBubbleRs.Builder builder = ChangeChatBubbleRs.newBuilder();
        builder.setBubbleId(bubbleId);
        return builder.build();
    }

    /**
     * ????????????????????????????????????
     */
    public void offLineTimeTimer() {
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().filter(p -> p.canPushOffLine(now)).forEach(p -> {
            Integer status = p.getPushRecord(PushConstant.OFF_LINE_ONE_DAY);
            int offTime = now - p.lord.getOffTime();
            if (status != null && offTime > 0) {
                // ?????????????????????, ?????????????????? ??????24?????? ??????48??????
                if (status == 0 && offTime > TimeHelper.DAY_S && offTime < (TimeHelper.DAY_S * 2)) {
                    PushMessageUtil.pushMessage(p.account, PushConstant.OFF_LINE_ONE_DAY);
                    p.putPushRecord(PushConstant.OFF_LINE_ONE_DAY, 1);
                } else if (status == 1 && offTime > (TimeHelper.DAY_S * 2) && offTime < (TimeHelper.DAY_S * 3)) {
                    // ???????????????24???????????????, ??????48???????????????????????????
                    PushMessageUtil.pushMessage(p.account, PushConstant.OFF_LINE_TWO_DAY);
                    p.putPushRecord(PushConstant.OFF_LINE_ONE_DAY, 2);
                } else if (status == 2 && offTime > (TimeHelper.DAY_S * 3)) {
                    // ???????????????48???????????????, ??????72???????????????????????????
                    PushMessageUtil.pushMessage(p.account, PushConstant.OFF_LINE_THREE_DAY);
                    p.putPushRecord(PushConstant.OFF_LINE_ONE_DAY, 3);
                }
            }
        });
    }

    /**
     * ?????????????????????????????????
     */
    public void aliceArriveTimer() {
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().filter(p -> canPushAliceArrive(p, now)).forEach(p -> {
            PushMessageUtil.pushMessage(p.account, PushConstant.ALICE_ARRIVE);
            p.putPushRecord(PushConstant.ALICE_ARRIVE, PushConstant.ALICE_ARRIVE);
        });
    }

    /**
     * ?????????????????????????????????????????????(???????????????)
     *
     * @param player ??????
     * @param now    ???????????????
     * @return ??????????????????
     */
    private boolean canPushAliceArrive(Player player, int now) {
        Integer status = player.getPushRecord(PushConstant.ALICE_ARRIVE);
        int aliceAwardTime = player.getMixtureDataById(PlayerConstant.ALICE_AWARD_TIME);
        return status != null && now > aliceAwardTime && status != PushConstant.ALICE_ARRIVE;
    }


    /**
     * ??????????????????
     *
     * @param roleId ????????????id
     * @return
     * @throws MwException ???????????????
     */
    public JoinCommunityRs joinCommunity(long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int val = player.getMixtureDataById(PlayerConstant.JOIN_COMMUNITY_AWARD);

        if (val != 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????, ????????????????????????");
        }

        Award awardPb = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.GOLD, Constant.JOIN_COMMUNITY_AWARD);
        // ??????????????????
        mailDataManager.sendAttachMail(player, Collections.singletonList(awardPb), MailConstant.MOLD_JOIN_COMMUNITY_REWARD, AwardFrom.JOIN_COMMUNITY_AWARD, TimeHelper.getCurrentSecond());
        // ????????????????????????
        player.setMixtureData(PlayerConstant.JOIN_COMMUNITY_AWARD, 1);
        return JoinCommunityRs.newBuilder().build();

    }


    /**
     * ??????
     *
     * @param roleId   ?????????
     * @param targetId ?????????
     * @param heroIds  ??????id
     * @return ????????????
     * @throws MwException ???????????????
     */
    public CompareNotesRs compareNotes(long roleId, long targetId, List<Integer> heroIds) throws MwException {


        Player mPlayer = playerDataManager.checkPlayerIsExist(roleId);

        Player tPlayer = playerDataManager.checkPlayerIsExist(targetId);

        if (mPlayer.lord.getCamp() != tPlayer.lord.getCamp()) {
            throw new MwException(GameError.COMPARE_NOTES_CAMP_ERROR.getCode(), "??????????????????????????????");
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
                        //????????????????????????????????????
                        playerOnHook.setAskAnnihilateNumber(0);
                        playerOnHook.setAskLastAnnihilateNumber(0);
                        this.syncOnHookData(player);
                    } else {
                        int now = TimeHelper.getCurrentSecond();
                        if (now - playerOnHook.getLastStamp() >= Constant.ONHOOK_1063) {
                            playerOnHook.setLastStamp(now);
                            List<Award> dropAwards = marchService.onHookBandit(player, playerOnHook.getCurRebelLv());
                            playerOnHook.addDropAward(dropAwards);
                            //???????????????????????????????????????0??????????????????????????????????????????
                            if (playerOnHook.getAskLastAnnihilateNumber() == 0) {
                                playerOnHook.setAskLastAnnihilateNumber(playerOnHook.getAskAnnihilateNumber());
                            }
                            playerOnHook.setAskLastAnnihilateNumber(playerOnHook.getAskLastAnnihilateNumber() - 1);
                            if (playerOnHook.getAskLastAnnihilateNumber() == 0) {
                                playerOnHook.setAskAnnihilateNumber(0);
                            }
                            //??????????????????????????????????????????????????????????????????????????????????????????????????????0???????????????????????????????????????
                            if (playerOnHook.getState() != ONHOOK_TYPE[0]
                                    && playerOnHook.getAskAnnihilateNumber() == 0 && playerOnHook.getAskLastAnnihilateNumber() == 0) {
                                playerOnHook.setState(ONHOOK_TYPE[0]);
                            }
                            this.syncOnHookData(player);
//                            LogUtil.error(">>>>>>>>>>>>>>>>>>>>>>>???????????? : " + JSON.toJSONString(playerOnHook));
                        }
                    }
                }
            } else {
                if (playerOnHook.getState() != ONHOOK_TYPE[0]) {
                    playerOnHook.setState(ONHOOK_TYPE[0]);
                    //??????????????????????????????
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
     * ?????? - ????????????
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
        //???????????????????????????????????????????????????????????????0
        if (Objects.isNull(funCard) || playerOnHook.getLastStamp() == 0) {
            resp.setAskAnnihilateNumber(0);
            resp.setAskLastAnnihilateNumber(0);
        } else {
            //????????????
            LocalDateTime sysDateTime = LocalDateTime.now();
            //??????????????????????????????
            LocalDateTime lastDateTime = LocalDateTime.ofEpochSecond(playerOnHook.getLastStamp(), 0, ZoneOffset.of("+8"));
            Duration duration = Duration.between(lastDateTime, sysDateTime);
            //??????????????????????????????????????????
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
     * ?????? - ??????
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

    private static final int[] ONHOOK_TYPE = {0, 1};//[0]????????????[1]????????????

    /**
     * ?????? - ????????????/????????????
     *
     * @param roleId
     * @param type
     * @param anniNumberThreshold ??????????????????
     * @return
     * @throws MwException
     */
    public OnHookOperateRs onHookOperateRs(long roleId, int type, int rebelLv, int anniNumberThreshold) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (Arrays.binarySearch(ONHOOK_TYPE, type) < 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "type?????????, roleId=" + roleId + ", type=" + type);
        }
        PlayerOnHook playerOnHook = player.getPlayerOnHook();
        if (rebelLv < 1 || rebelLv > playerOnHook.getMaxRebelLv()) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "?????????????????????, roleId=" + roleId + ", type=" + type + ", rebelLv=" + rebelLv);
        }
        if (type == ONHOOK_TYPE[0]) {
            if (playerOnHook.getState() != ONHOOK_TYPE[0]) {
                playerOnHook.setState(ONHOOK_TYPE[0]);
            }
        } else if (type == ONHOOK_TYPE[1]) {
            if (anniNumberThreshold == 0) {
                throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "????????????????????????, roleId=" + roleId + ", type=" + type + ", rebelLv=" + rebelLv);
            }
            long sumArmys = playerOnHook.getArmys().values().stream().collect(Collectors.summarizingInt(x -> x.intValue())).getSum();
            if (sumArmys < Constant.ONHOOK_1061) {
                throw new MwException(GameError.ONHOOK_RUN_ARMY_NOENOUGHT.getCode(), "??????????????????, roleId=" + roleId);
            }
            if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
                throw new MwException(GameError.ONHOOK_RUN_NON_DAYTIMES.getCode(), "????????????, roleId=" + roleId);
            }
            if (!checkCardUnexpired(player, FunCard.CARD_TYPE[8])) {
                throw new MwException(GameError.ONHOOK_CARD_EXPIRE.getCode(), "???????????????, roleId=" + roleId);
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
     * ?????? - ????????????
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

    // <editor-fold desc="???????????????????????????"??defaultstate="collapsed">
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
     * ????????????????????????????????????
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
            return "??????@";
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
