package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCiaDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCombatDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticMedalDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.service.CrossAttackService;
import com.gryphpoem.game.zw.gameplay.local.service.newyork.NewYorkWarService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonAttackCityService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonMomentOverService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonWeekIntegralService;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.MapEntityGenerator;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.PlayerMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.BattlePassDataManager;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.MedalDataManager;
import com.gryphpoem.game.zw.manager.MentorDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RankDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.SolarTermsDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.VipDataManager;
import com.gryphpoem.game.zw.manager.WarDataManager;
import com.gryphpoem.game.zw.manager.WarPlaneDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.manager.prop.PropDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.Mail;
import com.gryphpoem.game.zw.pb.GamePb1.DoSomeRq;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRq;
import com.gryphpoem.game.zw.pb.HttpPb;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.Constant.CombatType;
import com.gryphpoem.game.zw.resource.constant.DataSaveConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.GlobalConstant;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.PlaneConstant;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.constant.PushConstant;
import com.gryphpoem.game.zw.resource.constant.ScheduleConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.AccountDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.GlobalDao;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.ActTurnplat;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ArmQue;
import com.gryphpoem.game.zw.resource.domain.p.BuildQue;
import com.gryphpoem.game.zw.resource.domain.p.Building;
import com.gryphpoem.game.zw.resource.domain.p.Combat;
import com.gryphpoem.game.zw.resource.domain.p.CombatFb;
import com.gryphpoem.game.zw.resource.domain.p.EquipQue;
import com.gryphpoem.game.zw.resource.domain.p.Factory;
import com.gryphpoem.game.zw.resource.domain.p.Mentor;
import com.gryphpoem.game.zw.resource.domain.p.MentorEquip;
import com.gryphpoem.game.zw.resource.domain.p.MentorInfo;
import com.gryphpoem.game.zw.resource.domain.p.MentorSkill;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.p.MultCombat;
import com.gryphpoem.game.zw.resource.domain.p.PitchCombat;
import com.gryphpoem.game.zw.resource.domain.p.PlayerRebellion;
import com.gryphpoem.game.zw.resource.domain.p.StoneCombat;
import com.gryphpoem.game.zw.resource.domain.p.TechLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticAgent;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedal;
import com.gryphpoem.game.zw.resource.domain.s.StaticPitchCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticSchedule;
import com.gryphpoem.game.zw.resource.domain.s.StaticScheduleBoss;
import com.gryphpoem.game.zw.resource.domain.s.StaticStoneCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticTechLv;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.GlobalRoyalArena;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.Trophy;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.global.ScheduleBoss;
import com.gryphpoem.game.zw.resource.pojo.global.WorldSchedule;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.sandtable.HisCampRank;
import com.gryphpoem.game.zw.resource.pojo.sandtable.SandTableCamp;
import com.gryphpoem.game.zw.resource.pojo.sandtable.SandTableContest;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinCityInfo;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.CounterAttack;
import com.gryphpoem.game.zw.resource.pojo.world.SuperMine;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.PushMessageUtil;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.server.thread.SendEventDataThread;
import com.gryphpoem.game.zw.service.activity.AbsRankActivityService;
import com.gryphpoem.game.zw.service.activity.ActivityAuctionService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.activity.ActivityTemplateService;
import com.gryphpoem.game.zw.service.activity.PersonalActService;
import com.gryphpoem.game.zw.service.robot.RobotService;
import com.gryphpoem.game.zw.service.sandtable.SandTableContestService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GmService {

    @Autowired
    private ActivityDiaoChanService activityDiaoChanService;

    @Autowired
    private SandTableContestService sandTableContestService;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private GmToolService gmToolService;

    @Autowired
    private CampService campService;

    @Autowired
    private RobotService robotService;

    @Autowired
    private GestapoService gestapoService;

    @Autowired
    private HeroService heroService;

    @Autowired
    private LoadService loadService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private RankDataManager rankDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private CiaService ciaService;
    @Autowired
    private CampDataManager campDataManager;
    @Autowired
    private PayService payService;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private SolarTermsDataManager solarTermsDataManager;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityTemplateService activityTemplateService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private SuperMineService superMineService;
    @Autowired
    private EquipService equipService;
    @Autowired
    private BerlinWarService berlinWarService;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private MailService mailService;
    @Autowired
    private WarPlaneDataManager warPlaneDataManager;
    @Autowired
    private CounterAtkService counterAtkService;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private MentorDataManager mentorDataManager;
    @Autowired
    private TaskService taskService;

    @Autowired
    private MentorService mentorService;
    @Autowired
    private AirshipService airshipService;
    @Autowired
    private WorldScheduleRankService worldScheduleRankService;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private WorldWarSeasonMomentOverService worldWarSeasonMomentOverService;

    @Autowired
    private WorldWarSeasonAttackCityService worldWarSeasonAttackCityService;
    @Autowired
    private WorldWarSeasonWeekIntegralService worldWarSeasonWeekIntegralService;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private WorldWarSeasonDailyAttackTaskService worldWarSeasonDailyAttackTaskService;
    @Autowired
    private WorldWarSeasonDailyRestrictTaskService worldWarSeasonDailyRestrictTaskService;
    @Autowired
    private RebelService rebelService;
    @Autowired
    private NewYorkWarService newYorkWarService;
    @Autowired
    private CityService cityService;

    @Autowired
    private GmServiceExt gmServiceExt;

    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private GlobalDao globalDao;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private CrossAttackService crossAttackService;
    @Autowired
    private SeasonService seasonService;

    @Autowired
    private DressUpDataManager dressUpDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private ArmyService armyService;
    @Autowired
    private GmCmdProcessor gmCmdProcessor;

    // ?????????????????????????????????
    private static boolean clearFlag = false;

    public static boolean isFlag() {
        return clearFlag;
    }

    public static void setFlag(boolean flag) {
        GmService.clearFlag = flag;
    }

    public boolean doSome(DoSomeRq req, long roleId) throws MwException {
        String str = req.getStr();
        str = str.trim();// ?????????
        LogUtil.debug("GM-send------------" + str);
        String[] words = str.split("\\s+");
        int paramCount = words.length;
        if (paramCount < 2 || paramCount > 6) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), "GM??????????????????????????????");
        }

        try {
            String cmd = null;
            String type = null;
            String id = "0";
            String count = "0";
            String lv = "0";
            String refitLv = "0";
            if (paramCount == 2) {
                cmd = words[0];
                type = words[1];
            } else if (paramCount == 3) {
                cmd = words[0];
                type = words[1];
                count = words[2];
                id = "0";
            } else if (paramCount == 4) {
                cmd = words[0];
                type = words[1];
                id = words[2];
                count = words[3];
            } else if (paramCount == 5) {
                cmd = words[0];
                type = words[1];
                id = words[2];
                count = words[3];
                lv = words[4];
            } else if (paramCount == 6) {
                cmd = words[0];
                type = words[1];
                id = words[2];
                count = words[3];
                lv = words[4];
                refitLv = words[5];
            }

            Player player = playerDataManager.getPlayer(roleId);

            // if (player.account.getIsGm() <= 0) {
            // LogUtil.error("player {" + player.roleId + "} ilegal GM operating!");
            // throw new MwException(GameError.NO_AUTHORITY.getCode(), "????????????GM???????????????GM??????");
            // }
            if (player != null) {
                LogUtil.gm("{" + player.lord.getNick() + "|" + player.roleId + "} do operate {" + str + "}");
            } else {
                LogUtil.gm("??????gm???,????????????????????????: roleId:", roleId);
            }

            if ("add".equalsIgnoreCase(cmd)) {
                gmAdd(player, type, Integer.valueOf(id), Integer.valueOf(count), Integer.valueOf(lv),
                        Integer.valueOf(refitLv));
            } else if ("del".equalsIgnoreCase(cmd)) {
                gmDel(player, type, Integer.valueOf(id), Integer.valueOf(count), Integer.valueOf(lv),
                        Integer.valueOf(refitLv));
            } else if ("set".equalsIgnoreCase(cmd)) {
                gmSet(player, type, Integer.valueOf(id), Integer.valueOf(count), lv);
            } else if ("open".equalsIgnoreCase(cmd)) {
                gmOpen(player, type, Integer.valueOf(count), lv);
            } else if ("clear".equalsIgnoreCase(cmd)) {
                gmClear(player, type, Integer.parseInt(count));
            } else if ("build".equalsIgnoreCase(cmd)) {
                gmBuild(player, Integer.parseInt(type), Integer.parseInt(count));
            } else if ("party".equalsIgnoreCase(cmd)) {
                gmParty(player, type);
            } else if ("system".equalsIgnoreCase(cmd)) {
                gmSystem(type);
            } else if ("mail".equalsIgnoreCase(cmd)) {
//                Mail mail = null;
//                if (req.hasMail()) {
//                    mail = req.getMail();
//                } else {
//                    throw new MwException(GameError.PARAM_ERROR.getCode(), "GM????????????????????????");
//                }
//
//                LogUtil.gm("{" + player.roleId + "} send mail {" + mail + "}");
//                gmMail(mail, type);
                mailService.gm(player, words);
            } else if ("platMail".equalsIgnoreCase(cmd)) {
                Mail mail = null;
                if (req.hasMail()) {
                    mail = req.getMail();
                } else {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "GM????????????????????????");
                }

                LogUtil.gm("{" + player.roleId + "} send plat mail {" + mail + "}");
            } else if ("kick".equalsIgnoreCase(cmd)) {
                gmKick(type);
            } else if ("apicmd".equalsIgnoreCase(cmd)) {//kickoffline
                apicmd(type, roleId, words);
            } else if ("silence".equalsIgnoreCase(cmd)) {
                gmSilence(type, Integer.valueOf(count));
            } else if ("ganVip".equalsIgnoreCase(cmd)) {
                gmVip(Integer.valueOf(type), player);
            } else if ("clearPlayer".equalsIgnoreCase(cmd)) {
                gmClearPlayer(type, count);
            } else if ("clearAllPlayer".equalsIgnoreCase(cmd)) {
                gmClearAllPlayer(type);
            } else if ("resetAllPlayer".equalsIgnoreCase(cmd)) {
                gmToolService.resetAllPlayerWorldData();
            } else if ("ganTopup".equalsIgnoreCase(cmd)) {
                gmTopup(type, Integer.valueOf(count));
            } else if ("remove".equalsIgnoreCase(cmd)) {
                gmRemove(player, type, Integer.valueOf(count));
            } else if ("removePlayer".equalsIgnoreCase(cmd)) {
                gmRemovePlayer(type, id, Integer.valueOf(count));
            } else if ("removeAllPlayer".equalsIgnoreCase(cmd)) {
                gmRemoveAllPlayer(type, Integer.valueOf(count));
            } else if ("fix".equalsIgnoreCase(cmd)) {// ??????????????????
                gmFix(player, type, id, count, lv, refitLv);
            } else if ("robot".equalsIgnoreCase(cmd)) { // ?????????
                gmRobot(player, type, id, count);
            } else if ("gestapo".equalsIgnoreCase(cmd)) { // ????????????
                gmGestapo(player, type, id, count);
            } else if ("berlin".equalsIgnoreCase(cmd)) {
                gmBerlin(player, type, id, count, lv);
            } else if ("search".equalsIgnoreCase(cmd)) { // ????????????
                gmSearch(player, type, id, count, lv);
            } else if ("counterAtk".equalsIgnoreCase(cmd)) {
                gmCounterAtk(player, type, id, count, lv);
            } else if ("recover".equalsIgnoreCase(cmd)) { // ??????
                gmRecover(player, type, count);
            }
            // ??????????????????gm
            else if ("amend".equalsIgnoreCase(cmd)) {
                gmAmend(player, type, count);
            } else if ("worldSchedule".equalsIgnoreCase(cmd)) {
                // ??????????????????
                gmWorldSchedule(player, type, id, count);
            }
            // ??????????????????
            else if ("worldWar".equalsIgnoreCase(cmd)) {
                worldWar(player, type, id, count);
            }
            // ??????????????????
            else if ("changePush".equalsIgnoreCase(cmd)) {
                gmChangePushStatus(type);
            }
            // ??????????????????????????????
            else if ("changeDataSaveInterval".equalsIgnoreCase(cmd)) {
                gmChangeDataSaveInterval(type, count);
            }
            // ??????????????????
            else if ("newYorkWar".equalsIgnoreCase(cmd)) {
                gmInitNewYorkWar(type, count);
            } else if ("sandtable".equalsIgnoreCase(cmd)) {
                if (type.equalsIgnoreCase("addscore")) {
                    player.setSandTableScore(player.getSandTableScore() + Integer.parseInt(count));
                    if (player.getSandTableScore() < 0) {
                        player.setSandTableScore(0);
                    }
                    LogLordHelper.sandTableScore(AwardFrom.DO_SOME, player, Integer.parseInt(count));
                } else {
                    gm_sandtable(type, player);
                }
            } else if ("updateServerArmyCount".equalsIgnoreCase(cmd)) {
                armyService.updateServerArmyCount(Integer.parseInt(type), Integer.parseInt(count));
            } else if ("diaochan".equalsIgnoreCase(cmd)) {//????????????
                activityDiaoChanService.test_protocol(player, words);
            } else if ("onhook".equalsIgnoreCase(cmd)) {
                playerService.test_onHook(player, Arrays.copyOfRange(words, 1, words.length));
            } else if ("season".equalsIgnoreCase(cmd)) {
                seasonService.test_protocol(player, words);
            } else if ("dressUp".equalsIgnoreCase(cmd)) {
                gmDressUp(player, type, id, count, lv);
            } else if ("auction".equalsIgnoreCase(cmd)) {
                DataResource.getBean(ActivityAuctionService.class).checkTimer();
            } else if ("sendChat".equalsIgnoreCase(cmd)) {
                GamePb3.SendChatRq.Builder builder = GamePb3.SendChatRq.newBuilder();
                builder.setChannel(Integer.parseInt(type));
                String[] cArr = new String[]{count};
                builder.addAllContent(Arrays.asList(cArr));
                DataResource.ac.getBean(ChatService.class).sendChat(roleId, builder.build());
            } else if ("sendPriChat".equalsIgnoreCase(cmd)) {
                GamePb3.SendChatRq.Builder builder = GamePb3.SendChatRq.newBuilder();
                builder.setChannel(Integer.parseInt(type));
                String[] cArr = new String[]{count};
                builder.addAllContent(Arrays.asList(cArr));
                builder.setTarget(Long.parseLong(id));
                DataResource.ac.getBean(ChatService.class).sendChat(roleId, builder.build());
            } else {
                gmServiceExt.doSome(words, roleId);
                /////////////////?????????GM?????????????????????????????????
                GmCmdProcessor.Relation relation = gmCmdProcessor.getRelation(cmd);
                if (Objects.nonNull(relation)) {
                    relation.invoke(player, Arrays.copyOfRange(words, 1, words.length));
                } else {
                    LogUtil.error(String.format("GM??????%s?????????", str));
                }
            }
        } catch (Exception e) {
            LogUtil.error(e, str);
            return false;
        }
        return true;
    }

    /**
     * ????????????gm
     *
     * @param player ????????????
     * @param type   ???????????????
     * @param id     ???????????????
     * @param count  ???????????????
     * @param lv     ???????????????
     */
    private void gmDressUp(Player player, String type, String id, String count, String lv) {
        try {
            if (StringUtils.isNotBlank(type)) {
                if (StringUtils.equalsIgnoreCase(type, "add")) {
                    // dressUp add AwardType confId time
                    rewardDataManager.addAwardSignle(player, Integer.valueOf(id), Integer.valueOf(count), Integer.valueOf(lv), AwardFrom.DO_SOME);
//                    dressUpDataManager.addDressUp(player, Integer.valueOf(id), Integer.valueOf(count), Long.valueOf(lv), AwardFrom.DO_SOME);
                } else if (StringUtils.equalsIgnoreCase(type, "sub")) {
                    // dressUp sub AwardType confId time
                    dressUpDataManager.subDressUp(player, Integer.valueOf(id), Integer.valueOf(count), Long.valueOf(lv), AwardFrom.DO_SOME);
                } else if (StringUtils.equalsIgnoreCase(type, "allAdd")) {
                    // ???????????????????????????
                    playerDataManager.getAllPlayer().values().forEach(p -> rewardDataManager.addAwardSignle(player, Integer.valueOf(id), Integer.valueOf(count), Integer.valueOf(lv), AwardFrom.DO_SOME));
                    Map<Integer, BaseDressUpEntity> dressUpMap = dressUpDataManager.getDressUpByType(player, Integer.valueOf(id));
                    if (!CheckNull.isEmpty(dressUpMap)) {
                        BaseDressUpEntity baseDressUpEntity = dressUpMap.get(Integer.valueOf(count));
                        if (Objects.nonNull(baseDressUpEntity) && Objects.nonNull(StaticLordDataMgr.getMarchLine(Integer.valueOf(count)))) {
                            dressUpDataManager.syncDressUp(player, dressUpMap.get(Integer.valueOf(count)), DressUpDataManager.UPDATE_EVENT);
                        }
                    }
                }
                //????????????????????????
                else if (StringUtils.equalsIgnoreCase(type, "marchingEffects")) {
                    Map<Integer, BaseDressUpEntity> dressUpMap = dressUpDataManager.getDressUpByType(player, AwardType.MARCH_SPECIAL_EFFECTS);
                    if (!CheckNull.isEmpty(dressUpMap)) {
                        BaseDressUpEntity baseDressUpEntity = dressUpMap.get(Integer.valueOf(count));
                        if (Objects.nonNull(baseDressUpEntity) && Objects.nonNull(StaticLordDataMgr.getMarchLine(Integer.valueOf(count)))) {
                            player.getDressUp().setCurMarchEffect(Integer.valueOf(count));
                            dressUpDataManager.syncDressUp(player, dressUpMap.get(Integer.valueOf(count)), DressUpDataManager.UPDATE_EVENT);
                        }
                    }
                } else if (StringUtils.equalsIgnoreCase(type, "reload")) {
                    DataResource.ac.getBean(DressUpService.class).reloadTable();
                }
            }
        } catch (Exception e) {
            LogUtil.error("GM??????????????????[gm dress up ], " + type, e);
        }
    }

    private void gm_sandtable(String type, Player player) {
        try {
            if (StringUtils.isBlank(type)) {

            } else {
                if (type.equalsIgnoreCase("cleafEffect")) {
                    player.getEffect().clear();
                }
                //?????????????????????
                if (type.equalsIgnoreCase("clear")) {//????????? 1
                    globalDataManager.getGameGlobal().getSandTableContest().clearData();
                } else if (type.equalsIgnoreCase("setDate")) {//????????? 2
                    sandTableContestService.settingOpenDate(player);
                } else if (type.contains("group1")) {//???????????????????????? sandtable group1-2,1,2,3,1,3 //????????? 3
                    String[] strArr = type.substring(type.indexOf("-") + 1).split(",");
                    sandTableContestService.matchGroup1(Integer.parseInt(strArr[0]), Integer.parseInt(strArr[1]), Integer.parseInt(strArr[2]), Integer.parseInt(strArr[3]), Integer.parseInt(strArr[4]), Integer.parseInt(strArr[5]));
                }
                //??????????????????????????????????????????????????????????????????
                else if (type.equalsIgnoreCase("reopen1")) {
                    SandTableContest sandTableContest = globalDataManager.getGameGlobal().getSandTableContest();
                    sandTableContest.setRound(1);
                    sandTableContest.getMatchGroup().values().forEach(o -> {
                        o.state = 0;
                    });
                    sandTableContest.getCampLines().values().forEach(o -> {
                        o.setResult(0);
                        o.getLines().values().forEach(o1 -> {
                            o1.result = 0;
                        });
                    });
                    int now = TimeHelper.getCurrentDay();
                    List<HisCampRank> rmList = sandTableContest.getHisCampRanks().stream().filter(o -> o.hisDate == now).collect(Collectors.toList());
                    sandTableContest.getHisCampRanks().removeAll(rmList);
                    sandTableContest.addHisMatch();
                    for (int camp : Constant.Camp.camps) {
                        SandTableCamp sandTableCamp = sandTableContest.getSandTableCamp(camp);
                        sandTableCamp.clearData();
                    }
                } else if (type.equalsIgnoreCase("clearall")) {
                    globalDataManager.getGameGlobal().setSandTableContest(new SandTableContest());
                    campDataManager.getPartyMap().values().forEach(o -> {
                        o.setSandTableWin(0);
                        o.setSandTableWinMax(0);
                    });
                }
                //???????????????????????????
                if (type.equalsIgnoreCase("group")) {
                    sandTableContestService.matchGroup();
                } else if (type.equalsIgnoreCase("fight")) {
                    sandTableContestService.fightLines();
                } else if (type.equalsIgnoreCase("openBegin")) {
                    sandTableContestService.openBegin();
                } else if (type.equalsIgnoreCase("reopen")) {//?????????????????????????????????????????????????????????
                    sandTableContestService.reOpen();
                }
                //??????????????????
                else if (type.equalsIgnoreCase("getInfo")) {
                    sandTableContestService.getInfo(player.roleId);
                } else if (type.contains("enrollself")) {//
                    String[] strArr = type.split("-");
                    List<Integer> heroIds = new ArrayList<>();
                    player.getAllOnBattleHeros().stream().forEach(o -> heroIds.add(o.getHeroId()));
                    sandTableContestService.enroll(player.roleId, Integer.parseInt(strArr[1]), heroIds);
                } else if (type.equalsIgnoreCase("getHisContest")) {
                    sandTableContestService.getHisContest(player.roleId);
                } else if (type.equalsIgnoreCase("getReplay")) {
                    sandTableContestService.getReplay(player.roleId, 20210106, 1);
                } else if (type.equalsIgnoreCase("getLinePlayers")) {
                    sandTableContestService.getLinePlayers(player.roleId, 1);
                } else if (type.contains("enrollall")) {
                    int line = Integer.parseInt(type.substring(type.indexOf("-") + 1));
                    if (line < 0 || line > 3) return;
                    ConcurrentHashMap<Long, Player> campPlayers = playerDataManager.getPlayerByCamp(player.getCamp());
                    if (line == 0) {
                        int tmpLine = 1;
                        for (Player p : campPlayers.values()) {
                            if (tmpLine > 3) {
                                tmpLine = 1;
                            }
                            try {
                                sandTableContestService.enroll(p.roleId, tmpLine, p.getAllOnBattleHeros().stream().map(hero -> hero.getHeroId()).collect(Collectors.toList()));
                                tmpLine++;
                            } catch (Exception e) {

                            }
                        }
                    } else {
                        for (Player p : campPlayers.values()) {
                            try {
                                sandTableContestService.enroll(p.roleId, line, p.getAllOnBattleHeros().stream().map(hero -> hero.getHeroId()).collect(Collectors.toList()));
                            } catch (Exception e) {

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("GM??????????????????[????????????], " + type, e);
        }
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     */
    private void gmWorldSchedule(Player player, String type, String id, String count) {
        // 3???????????? id?????????,count?????????
        if (type.equalsIgnoreCase("rank")) {
            // ????????????????????????
            int camp = Integer.valueOf(id).intValue();
            int val = Integer.valueOf(count).intValue();
            List<StaticArea> areaOrder1List = StaticWorldDataMgr.getAreaMap().values().stream()
                    .filter(area -> area.getOpenOrder() == 1).collect(Collectors.toList());
            int area = areaOrder1List.get(RandomHelper.randomInSize(areaOrder1List.size())).getArea();
            int curId = worldScheduleService.getCurrentSchduleId();
            if (curId == 1) {
                worldScheduleRankService.addOneWorldScheduleRankData(camp, area, val);
            } else if (Arrays.asList(2, 3, 4, 6, 7, 8).contains(curId)) {
                worldScheduleRankService.addCityWorldScheduleRankData();
            } else if (curId == 5 || curId == 9) {
                worldScheduleRankService.addBossWorldScheduleRankData(curId, camp, area, val);
            }
        } else if (type.equalsIgnoreCase("setRankParam")) {
            int scheduleId = Integer.valueOf(id);
            int val = Integer.valueOf(count);
            GlobalSchedule globalSchedule = worldScheduleService.getGlobalSchedule();
            WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(scheduleId);
            if (worldSchedule != null && worldSchedule.getRank() != null) {
                worldSchedule.getRank().setRankParam(val);
            }
        } else if (type.equalsIgnoreCase("set")) {
            // ??????????????????
            int scheduleId = Integer.valueOf(count);
            if (scheduleId > StaticWorldDataMgr.SCHEDULE_MAX_ID) {
                LogUtil.error("??????????????????????????????id, scheduleId:", scheduleId);
                return;
            }

            Java8Utils.syncMethodInvoke(() -> {
                // ??????boss??????????????????area
                if (scheduleId > ScheduleConstant.SCHEDULE_BOOS_1_ID) {
                    // ????????????2
                    globalDataManager.openAreaData(WorldConstant.AREA_ORDER_2);
                    if (scheduleId > ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                        // ??????????????????????????????
                        ScheduleManager.getInstance().initBerlinJob();
                        // ????????????3
                        globalDataManager.openAreaData(WorldConstant.AREA_ORDER_3);
                    }
                }
                GlobalSchedule globalSchedule = worldScheduleService.getGlobalSchedule();
                globalSchedule.getScheduleMap().clear();
                globalSchedule.setCurrentScheduleId(scheduleId);
                GlobalSchedule newGlobalSchedule = new GlobalSchedule(scheduleId);
                globalSchedule.getScheduleMap().putAll(newGlobalSchedule.getScheduleMap());
            });

        } else if (type.equalsIgnoreCase("modifyHP")) {
            // ??????????????????boss?????????
            Java8Utils.syncMethodInvoke(() -> {
                GlobalSchedule globalSchedule = worldScheduleService.getGlobalSchedule();
                // boss??????
                int schId = Integer.valueOf(id);
                if (!WorldScheduleService.bossSchedule(schId)) {
                    return;
                }
                // ?????????
                int val = Integer.valueOf(count);
                if (val <= 0 || val >= 10000) {
                    LogUtil.error("gm????????????boss?????? , val:", val);
                    return;
                }
                WorldSchedule schedule = globalSchedule.getWorldSchedule(schId);
                if (!CheckNull.isNull(schedule)) {
                    ScheduleBoss boss = schedule.getBoss();
                    if (!CheckNull.isNull(boss) && boss.getRemainHp() > 0) {
                        for (NpcForce force : boss.getNpc()) {
                            int hp = force.getHp();
                            force.setHp((int) (hp * (val / Constant.TEN_THROUSAND)));
                        }
                    }
                }
            });
        } else if (type.equalsIgnoreCase("skipSchedule")) {
            GlobalSchedule globalSchedule = worldScheduleService.getGlobalSchedule();
            int curId = globalSchedule.getCurrentScheduleId();
            // if (!WorldScheduleService.bossSchedule(curId)) {
            //     return;
            // }
            List<StaticScheduleBoss> scheduleBosses = StaticWorldDataMgr.getScheduleBossById(curId);
            // ??????????????????
            List<Integer> posList = new ArrayList<>();
            for (StaticScheduleBoss scheduleBoss : scheduleBosses) {
                posList.add(scheduleBoss.getPos());
            }
            // ??????????????????????????????
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
            // ??????????????????
            if (curId == ScheduleConstant.SCHEDULE_BOOS_1_ID) {
                // ????????????????????????
                campService.openPartyJobDelay();
                chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_1, player.lord.getCamp(), 0,
                        ScheduleConstant.SCHEDULE_BOOS_1_ID);
                // ??????????????????
                worldScheduleService.initRebellion();
            } else if (curId == ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_2, player.lord.getCamp(), 0,
                        ScheduleConstant.SCHEDULE_BOOS_2_ID);
            }
            // ????????????????????????
            globalSchedule.processRefreshSchedule();
            // ??????????????????
            worldScheduleService.syncSchedule();
            // ????????????????????? scheduleId
            worldScheduleRankService.worldScheduleRankAward(curId);
            if (curId == ScheduleConstant.SCHEDULE_BOOS_1_ID || curId == ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                // ??????boss??????????????????area
                globalDataManager.openAreaData(
                        curId == ScheduleConstant.SCHEDULE_BOOS_1_ID ? WorldConstant.AREA_ORDER_2 : WorldConstant.AREA_ORDER_3);
            }
        } else if (type.equalsIgnoreCase("fixSchedule")) {
            GlobalSchedule globalSchedule = worldScheduleService.getGlobalSchedule();
            int curId = globalSchedule.getCurrentScheduleId();
            if (curId == ScheduleConstant.SCHEDULE_BERLIN_ID) {
                StaticSchedule staSch = StaticWorldDataMgr.getScheduleById(ScheduleConstant.SCHEDULE_BERLIN_ID);
                if (Objects.isNull(staSch)) {
                    return;
                }
                WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(curId);
                int finishTime = worldSchedule.getFinishTime();
                if (finishTime <= 0 && staSch.getDurationTime() != -1) {
                    // ??????????????????
                    // ????????????
                    int durationTime = staSch.getDurationTime();
                    // ????????????
                    //?????????????????????????????????????????????????????????
                    WorldSchedule worldSchedule9 = globalSchedule.getWorldSchedule(ScheduleConstant.SCHEDULE_BOOS_2_ID);
                    finishTime = TimeHelper.getSomeDayAfter(TimeHelper.secondToDate(worldSchedule9.getFinishTime()), durationTime - 1, 23, 59, 59);
                    // ??????????????????????????????, ???????????????????????????23???59???59???
                    finishTime = finishTime < TimeHelper.getCurrentSecond() ? TimeHelper.dateToSecond(TimeHelper.getSomeDayAfterOrBerfore(new Date(), 0, 23, 59, 59)) : finishTime;
                    // ???????????????????????????
                    worldSchedule.setFinishTime(finishTime);
                    // ?????????????????????
                    worldSchedule.initGoal(staSch);
                    LogUtil.common(String.format("fix schedule success, serverId: %s, finishTime: %s", serverSetting.getServerID(), DateHelper.getDateFormat1().format(new Date(finishTime * 1000L))));
                }
                worldSchedule.setStatus(ScheduleConstant.SCHEDULE_STATUS_PROGRESS);
            }
        }
    }

    /**
     * ??????????????????
     */
    private void gmInitNewYorkWar(String type, String count) {
        try {
            if ("initJob".equalsIgnoreCase(type)) {
                // ????????? ????????????
                newYorkWarService.initNewYorkWar();
            } else if ("openWeek".equalsIgnoreCase(type)) {
                // ?????? ???????????? ????????????????????????????????????
                Integer week = Integer.parseInt(count);
                WorldConstant.NEWYORK_WAR_BEGIN_WEEK = week;
            } else if ("allMapJoinBattle".equalsIgnoreCase(type)) {
                Integer c = Integer.parseInt(count);
                newYorkWarService.gmJoinNewYorkWar(c);
            } else if ("initTodayJob".equalsIgnoreCase(type)) {
                newYorkWarService.gmInitTodayNewYorkWar();
            } else if ("closeJob".equalsIgnoreCase(type)) {
                newYorkWarService.gmCloseNewYorkWar();
            }
        } catch (Exception e) {
            LogUtil.error("GM ?????????????????? error ", e.getMessage());
        }
    }

    /**
     * gm ????????????????????????
     *
     * @param type
     */
    private void gmChangePushStatus(String type) {
        try {
            Integer status = Integer.parseInt(type);
            // 0?????? 1 ??????
            if (Arrays.asList(0, 1).contains(status)) {
                Constant.PUSH_CONFIG_SWITCH = status;
            }
        } catch (Exception e) {
            LogUtil.error("gm ???????????????????????? error", e);
        }
    }

    /**
     * ????????????????????????
     */
    private void gmChangeDataSaveInterval(String type, String count) {
        try {
            int val = Integer.parseInt(count);
            if ("crossMap".equalsIgnoreCase(type)) {
                DataSaveConstant.CROSS_MAP_DATA_SAVE_INTERVAL_SECOND = val;
            } else if ("globalData".equalsIgnoreCase(type)) {
                DataSaveConstant.GLOBAL_DATA_SAVE_INTERVAL_SECOND = val;
            } else if ("partyData".equalsIgnoreCase(type)) {
                DataSaveConstant.PARTY_DATA_SAVE_INTERVAL_SECOND = val;
            } else if ("activityData".equalsIgnoreCase(type)) {
                DataSaveConstant.ACTIVITY_DATA_SAVE_INTERVAL_SECOND = val;
            }
        } catch (Exception e) {
            LogUtil.error("gm ???????????????????????? error", e);
        }
    }

    /**
     * ???????????? ????????????
     *
     * @param player ??????
     * @param type   ????????????
     * @param keyId  ??????
     * @param count  ??????
     */
    private void worldWar(Player player, String type, String keyId, String count) {
        // ??????????????????????????????
        if ("addCampAttackCityIntegral".equalsIgnoreCase(type)) {
            int val = Integer.valueOf(count).intValue();
            worldWarSeasonAttackCityService.addAttackCityIntegral(player, val);
        }
        // ?????????????????????
        else if ("addPlayerDailyRestrict".equalsIgnoreCase(type)) {
            int key = Integer.valueOf(keyId).intValue();
            int val = Integer.valueOf(count).intValue();
            worldWarSeasonDailyRestrictTaskService.gmUpdatePlayerDailyRestrictTask(player, key, val);
        }
        // ?????????????????????????????????
        else if ("addPlayerDailyAttack".equalsIgnoreCase(type)) {
            int val = Integer.valueOf(count).intValue();
            worldWarSeasonDailyAttackTaskService.addPlayerDailyAttackOther(player, val);
        }
        // ????????????????????????
        else if ("dailyAttackOver".equalsIgnoreCase(type)) {
            worldWarSeasonDailyAttackTaskService.dailyOverGiveAward();
        }
        // ????????????????????????
        else if ("dailyRestrictOver".equalsIgnoreCase(type)) {
            worldWarSeasonDailyRestrictTaskService.dailyOverGiveAward();
        }
        // ?????????????????????
        else if ("weekOver".equalsIgnoreCase(type)) {
            worldWarSeasonMomentOverService.weekOver();
        }
        // ????????????????????????
        else if ("seasonOver".equalsIgnoreCase(type)) {
            worldWarSeasonMomentOverService.seasonOver();
        }
        // ????????????????????????
        else if ("seasonDisplayOver".equalsIgnoreCase(type)) {
            worldWarSeasonMomentOverService.seasonOverClearIntegral();
        }
        // ??????????????????city??????
        else if ("initFrom".equalsIgnoreCase(type)) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            cMap.getCityMap().values().stream().map(cityMapEntity -> cityMapEntity.getCity()).forEach(city -> {
                // ????????????????????????
                StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
                if (staticCity == null) {
                    return;
                }
                city.initNpcForm(true);
            });
        } else if ("refreshAirship".equalsIgnoreCase(type)) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            cMap.getMapEntityGenerator().initAndRefreshAirship();
        }
    }

    /**
     * ????????????????????????( amend serverId 1)
     *
     * @param player
     * @param type     ??????
     * @param serverId ??????
     */
    private void gmAmend(Player player, String type, String serverId) {
        try {
            LogUtil.common("type=", type, ", data=", serverId);
            if ("serverId".equalsIgnoreCase(type)) {
                if (player != null) {
                    Integer sid = Integer.parseInt(serverId);
                    Account account = player.account;
                    // ??????serverId?????????0????????????
                    if (account != null && sid > 0) {
                        account.setServerId(sid);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("???????????????????????? error:", e);
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param type
     * @param count
     */
    private void gmRecover(Player player, String type, String count) {
        LogUtil.common("type=", type, ", data=", count);
        if ("medal".equalsIgnoreCase(type)) {
            Medal medal = JSON.parseObject(count, Medal.class);
            // JSONObject parseObject = JSONObject.parseObject(count);
            // JSONObject medalAttrParseObject = parseObject.getJSONObject("medalAttr");
            // Integer a = medalAttrParseObject.getInteger("a");
            // Integer b = medalAttrParseObject.getInteger("b");
            // Turple<Integer, Integer> turple = new Turple<>();
            // turple.setA(a);
            // turple.setB(b);
            // medal.setMedalAttr(turple);
            int keyId = player.maxKey();
            medal.setKeyId(keyId);
            medal.setHeroId(0);
            player.medals.put(medal.getKeyId(), medal);
            ArrayList<Medal> list = new ArrayList<>(1);
            list.add(medal);
            rewardDataManager.syncMedal(player, list);
        }
    }

    private void gmCounterAtk(Player player, String type, String id, String count, String lv) {
        int now = TimeHelper.getCurrentSecond();
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        if (type.equalsIgnoreCase("autoAtk")) {
            // ??????????????????
            String[] split = id.split(",");
            if (!counterAtkService.checkUnLock()
                    || counterAttack.getStatus() == WorldConstant.COUNTER_ATK_BOSS_STATUS_DEAD
                    || counterAttack.isNotInitOrDead()) {
                return;
            }
            Battle battle = warDataManager.getSpecialBattleMap().values().stream()
                    .filter(b -> b.getType() == WorldConstant.BATTLE_TYPE_COUNTER_ATK
                            && b.getBattleType() == WorldConstant.COUNTER_ATK_DEF)
                    .findFirst().orElse(null);
            if (CheckNull.isNull(battle)) {
                return;
            }
            playerDataManager.getPlayerByArea(WorldConstant.AREA_TYPE_13).values().stream().filter(p -> {
                int camp = p.lord.getCamp();
                boolean flag = false;
                for (String s : split) {
                    if (Integer.valueOf(s) == camp) {
                        flag = true;
                        break;
                    }
                }
                return flag;
            }).limit(Integer.valueOf(count)).forEach(p -> {
                // ????????????????????????
                playerDataManager.autoAddArmy(p);
                List<Integer> heros = p.getAllOnBattleHeros().stream()
                        .filter(hero -> hero.getState() == ArmyConstant.ARMY_STATE_IDLE).map(hero -> hero.getHeroId())
                        .collect(Collectors.toList());
                GamePb4.AttackCounterBossRq.Builder builder = GamePb4.AttackCounterBossRq.newBuilder();
                builder.addAllHeroId(heros);
                builder.setBattleId(battle.getBattleId());
                try {
                    counterAtkService.attackCounterBoss(builder.build(), p.roleId);
                } catch (MwException e) {
                    LogUtil.error("???????????????????????????BOSS??????", e);
                }
            });
        } else if (type.equalsIgnoreCase("addCredit")) {
            // ????????????????????????
            int cnt = Integer.valueOf(count);
            counterAtkService.sendCreditAward(cnt, player.roleId);
        } else if (type.equalsIgnoreCase("difficultyCoef")) {
            // ????????????????????????
            Map<Integer, Integer> coefMap = globalDataManager.getGameGlobal()
                    .getMixtureDataById(GlobalConstant.FORM_COEF_OF_DIFFICULTY);
            coefMap.put(GlobalConstant.CoefDifficulty.COUNTER_ATTACK, Integer.valueOf(count));
            LogUtil.error("???????????????????????????????????????, coef:", Integer.valueOf(count));
            globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.FORM_COEF_OF_DIFFICULTY, coefMap);
        } else if (type.equalsIgnoreCase("selectCoef")) {
            Map<Integer, Integer> coefMap = globalDataManager.getGameGlobal()
                    .getMixtureDataById(GlobalConstant.FORM_COEF_OF_DIFFICULTY);
            LogUtil.common(String.format("?????????????????????, serverId: %s, coef: %s", serverSetting.getServerID(), coefMap.getOrDefault(GlobalConstant.CoefDifficulty.COUNTER_ATTACK, 100)));
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param type
     * @param id
     * @param count
     * @param lv
     * @throws MwException
     */
    private void gmSearch(Player player, String type, String id, String count, String lv) throws MwException {
        if (type.equalsIgnoreCase("plane")) {
            int cnt = Integer.valueOf(count);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < cnt; i++) {
                List<Award> awards = warPlaneDataManager.doSearchPlane(player, PlaneConstant.SEARCH_TYPE_SUPER);
                if (!CheckNull.isNull(awards)) {
                    Award award = awards.get(0);
                    if (!CheckNull.isNull(award)) {
                        sb.append("[ type: ").append(award.getType()).append(", id: ").append(award.getId())
                                .append(", count: ").append(award.getCount()).append("]").append("\r\n");
                    }
                }
            }
            LogUtil.error("???gm????????????????????? : \r\n", sb.toString());
        }
    }

    private void gmBerlin(Player player, String type, String id, String count, String lv) {
        int now = TimeHelper.getCurrentSecond();
        if (type.equalsIgnoreCase("expenditure")) { // ????????????
            Integer cnt = Integer.valueOf(count);
            player.addMilitaryExpenditure(cnt);
            // ????????????????????????
            LogLordHelper.commonLog("expenditure", AwardFrom.DO_SOME, player.account, player.lord,
                    player.getMilitaryExpenditure(), cnt);
        } else if (type.equalsIgnoreCase("rank")) { // ?????????????????????
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (CheckNull.isNull(berlinWar)) {
                return;
            }
            Integer cnt = Integer.valueOf(count);
            berlinWar.addPlayerRank(player.roleId, cnt, now, WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
            berlinWar.addPlayerRank(player.roleId, cnt, now, WorldConstant.BERLIN_RANK_KILL_ARMY_CNT);
        } else if (type.equalsIgnoreCase("initBerlinJob")) { // ???????????????????????????????????????
            ScheduleManager.getInstance().initBerlinJob();
        } else if (type.equalsIgnoreCase("initBerlinStatus")) { // ???????????????????????????
            BerlinWar berlinWar = BerlinWar.createNewBerlinWar();
            berlinWar.initBerlinWar();
            BerlinCityInfo cityInfo = berlinWar.getBerlinCityInfo();
            if (!CheckNull.isNull(cityInfo)) {
                City berlin = worldDataManager.getCityById(cityInfo.getCityId());
                if (!CheckNull.isNull(berlin)) {
                    berlin.setCamp(Constant.Camp.NPC);
                }
            }
            globalDataManager.getGameGlobal().setBerlinWar(berlinWar);
        } else if (type.equalsIgnoreCase("autoAttackBerlin")) { // ??????????????????
            String[] split = lv.split(",");
            int cityId = Integer.valueOf(id);
            playerDataManager.getPlayerByArea(WorldConstant.AREA_TYPE_13).values().stream().filter(p -> {
                int camp = p.lord.getCamp();
                boolean flag = false;
                for (String s : split) {
                    if (Integer.valueOf(s) == camp) {
                        flag = true;
                        break;
                    }
                }
                return flag;
            }).limit(Integer.valueOf(count)).forEach(p -> {
                List<Hero> heros = p.getAllOnBattleHeros();
                heros.stream().filter(hero -> hero.getState() == ArmyConstant.ARMY_STATE_IDLE).forEach(hero -> {
                    try {
                        berlinWarService.attackBerlinWar(p.roleId, PbHelper.createAttackBerlinWarRq(cityId,
                                hero.getHeroId(), WorldConstant.BERLIN_ATTACK_TYPE_COMMON));
                    } catch (MwException e) {
                        LogUtil.error(e, "????????????????????????");
                    }
                });
            });

        } else if (type.equalsIgnoreCase("modifyStatus")) {
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (!CheckNull.isNull(berlinWar)) {
                berlinWar.setStatus(Integer.valueOf(count));
            }
        } else if (type.equalsIgnoreCase("goToBerlin")) {
            Java8Utils.syncMethodInvoke(() -> {
                playerDataManager.getAllPlayer().values().stream()
                        .filter(p -> p.lord.getArea() != WorldConstant.AREA_TYPE_13 && !p.getDecisiveInfo().isDecisive())
                        .forEach(p -> worldService.moveCityByGm(p, WorldConstant.AREA_TYPE_13));
            });
        } else if (type.equalsIgnoreCase("clearReport")) {
            Java8Utils.syncMethodInvoke(() -> {
                BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
                if (!CheckNull.isNull(berlinWar)) {
                    // ??????????????????
                    berlinWar.getReports().clear();
                }
            });
        }
    }

    private void gmGestapo(Player player, String type, String id, String count) {
        if (type.equalsIgnoreCase("summonManyGestapos")) {
            Integer areaId = Integer.valueOf(id);
            Integer gestapoCount = Integer.valueOf(count);
            gestapoService.summonManyGestapos(player, areaId, gestapoCount);
        } else if (type.equalsIgnoreCase("sendMail")) {
            activityService.sendUnrewardedMail(ActivityConst.ACT_ATK_GESTAPO, null);
        }
    }

    private void gmRobot(Player player, String type, String id, String count) {
        if (type.equalsIgnoreCase("closeRobot")) {
            robotService.gmCloseRobotTimerLogic();
        } else if (type.equalsIgnoreCase("openExternalBehavior")) {
            Integer areaId = Integer.valueOf(id);
            Integer robotCount = Integer.valueOf(count);
            robotService.gmOpenRobotsExternalBehavior(areaId, robotCount);
        } else if (type.equalsIgnoreCase("closeExternalBehavior")) {
            Integer areaId = Integer.valueOf(id);
            Integer robotCount = Integer.valueOf(count);
            robotService.gmCloseRobotsExternalBehavior(areaId, robotCount);
        }
    }

    private void gmBuild(Player player, int bType, int lv) {
        Building building = player.building;
        switch (bType) {
            case BuildingType.COMMAND:
                building.setCommand(lv);
                break;
            case BuildingType.TECH:
                building.setTech(lv);
                break;
            case BuildingType.STOREHOUSE:
                building.setWare(lv);
                break;
            case BuildingType.WALL:
                building.setWall(lv);
                break;
            case BuildingType.WAR_COLLEGE:
                building.setCollege(lv);
                break;
            case BuildingType.WAR_FACTORY:
                building.setWar(lv);
                break;
            case BuildingType.REMAKE:
                building.setRefit(lv);
                break;
            case BuildingType.ORDNANCE_FACTORY:
                building.setMunition(lv);
                break;
            case BuildingType.CHEMICAL_PLANT:
                building.setChemical(lv);
                break;
            case BuildingType.FACTORY_1:
                building.setFactory1(lv);
                break;
            case BuildingType.FACTORY_2:
                building.setFactory2(lv);
                break;
            case BuildingType.FACTORY_3:
                building.setFactory3(lv);
                break;
            case BuildingType.TRAIN_FACTORY_1:
                building.setTrain(lv);
                break;
            case BuildingType.TRAIN_FACTORY_2:
                building.setTrain2(lv);
                break;
            case BuildingType.TRADE_CENTRE:
                building.setTrade(lv);
                break;
            case BuildingType.CLUB:
                building.setClub(lv);
                break;
            case BuildingType.AIR_BASE:
                building.setAir(lv);
                break;
            case BuildingType.RES_OIL:
            case BuildingType.RES_ELE:
            case BuildingType.RES_FOOD:
            case BuildingType.RES_ORE: {
                for (Mill mill : player.mills.values()) {
                    if (bType == mill.getType()) {
                        mill.setLv(lv);
                    }
                }
                break;
            }
        }
    }

    /**
     * ??????????????????
     */
    private void gmFix(Player player, String str, String id, String count, String lv, String refitLv) {
        if (str.equalsIgnoreCase("partyJob")) {
            campService.gmFixPartyJob();
        } else if (str.equalsIgnoreCase("defHero")) {
            if (player != null) {
                LogUtil.common(String.format("????????????????????? :%d, ?????????????????????????????????", player.roleId));
                heroService.checkAndRepaireHero(player, true);
            } else {
                //?????????????????????
                LogUtil.common("??????????????????????????? ?????????????????????????????????");
                Map<String, Player> players = DataResource.ac.getBean(PlayerDataManager.class).getAllPlayer();
                for (Entry<String, Player> entry : players.entrySet()) {
                    Player player0 = entry.getValue();
                    heroService.checkAndRepaireHero(player0, true);
                }
            }
        } else if (str.equalsIgnoreCase("repairCombat")) {// ??????????????????
            repairCombat();
        } else if (str.equalsIgnoreCase("AllPalyerFight")) {// ???????????????????????????
            playerService.reCalcFightAllPlayer();
        } else if (str.equalsIgnoreCase("refreshBandit")) {
            // LogUtil.debug("-----------------????????????????????????????????? start---------------");
            // Scheduler sched = (Scheduler) DataResource.ac.getBean("schedulerFactoryBean");
            // QuartzHelper.removeJob(sched, "BanditRefreshWorldJob", "BanditWorld");
            // // QuartzHelper.addJob(sched, "BanditRefreshWorldJob", "BanditWorld", BanditRefreshWorldJob.class, "0/5 *
            // *
            // // * * ?");
            // LogUtil.debug("-----------------????????????????????????????????? end---------------");
            LogUtil.debug("-----------------??????????????? start---------------");
            final int cnt = Integer.parseInt(count);
            DataResource.logicServer.addCommandByType(() -> {
                if (cnt < 1 || cnt > 3) {
                    worldDataManager.refreshAllBandit(WorldConstant.REFRESH_TYPE_BANDIT_3);
                } else {
                    worldDataManager.refreshAllBandit(cnt);
                }
            }, DealType.BACKGROUND);
            // worldDataManager.refreshAllBandit(WorldConstant.REFRESH_TYPE_BANDIT_3);
            // worldDataManager.refreshAllBandit(WorldConstant.REFRESH_TYPE_BANDIT_1);
            // worldDataManager.refreshAllBandit(WorldConstant.REFRESH_TYPE_BANDIT_2);
            LogUtil.debug("-----------------??????????????? end---------------");
        } else if (str.equalsIgnoreCase("refreshMine")) {
            LogUtil.debug("--------------????????? type:", count);
            DataResource.logicServer.addCommandByType(() -> worldDataManager.refreshAllMine(Integer.parseInt(count)),
                    DealType.BACKGROUND);
        } else if (str.equalsIgnoreCase("refreshNewMapMine")) {
            Java8Utils.syncMethodInvoke(() -> {
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                if (cMap != null) {
                    cMap.getMapEntityGenerator().cleanAndRefreshMine();
                }
            });
        } else if (str.equals("build0to1")) {
            buildingService.gmBuildZeroToOne(player);

        } else if (str.equals("build0to1all")) {
            playerDataManager.getPlayers().forEach((lordId, p) -> {
                buildingService.gmBuildZeroToOne(p);
            });
        } else if (str.equals("armyState")) {
            LogUtil.debug("--------------???????????????????????? ??????  roleId:", player.roleId);
            // ????????????????????????id
            WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
            WarService warService = DataResource.ac.getBean(WarService.class);
            long roleId = player.roleId;
            for (Army army : player.armys.values()) {
                int armyState = army.getState();
                worldService.retreatArmy(player, army, TimeHelper.getCurrentSecond(), ArmyConstant.MOVE_BACK_TYPE_2);
                LogUtil.debug("--------------??????????????????: ", army);
                int keyId = army.getKeyId();
                try {
                    Integer battleId = army.getBattleId();
                    if (null != battleId && battleId > 0) {
                        Battle battle = warDataManager.getBattleMap().get(battleId);
                        if (null != battle) {
                            int camp = player.lord.getCamp();
                            int armCount = army.getArmCount();
                            battle.updateArm(camp, -armCount);
                            if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY) { // ?????? ?????????
                                if (battle.getSponsor() != null && battle.getSponsor().roleId == roleId) {// ????????????????????????
                                    // ????????????????????????????????????????????????????????????
                                    warService.cancelCityBattle(army.getTarget(), true, battle, true);
                                } else {
                                    // ???????????????,??????battle?????????
                                    worldService.removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                                }
                            } else if (battle.getType() == WorldConstant.BATTLE_TYPE_MINE_GUARD) {
                                //??????????????????, ???????????????????????????????????????
                                warDataManager.removeBattleByIdNoSync(battleId);
                            } else {// ?????????
                                worldService.removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                            }
                            HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
                            if (battleIds != null) {
                                battleIds.remove(battleId);
                            }
                        }
                    } else {
                        if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT) {
                            worldDataManager.removeMineGuard(army.getTarget());
                        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
                            SuperMine sm = worldDataManager.getSuperMineMap().get(army.getTarget());
                            if (Objects.nonNull(sm)) {
                                sm.removeCollectArmy(player.roleId, keyId);
                            }
                        }
                    }

                    //??????????????????????????????????????????, ???????????????????????????
                    if (armyState == ArmyConstant.ARMY_STATE_COLLECT) {
                        //???????????????????????????
                        worldService.cancelMineBattle(army.getTarget(), TimeHelper.getCurrentSecond(), player);
                    }
                } catch (Exception e) {
                    LogUtil.debug(e);
                    e.printStackTrace();
                }
            }
            for (int i = 1; i < player.heroBattle.length; i++) {
                int heroId = player.heroBattle[i];
                Hero hero = player.heros.get(heroId);
                if (hero != null) {
                    hero.setState(HeroConstant.HERO_STATE_IDLE);
                }
                LogUtil.debug("--------------??????????????????????????? heroId: ", heroId);
            }
            for (int i = 1; i < player.heroAcq.length; i++) {
                int heroId = player.heroAcq[i];
                Hero hero = player.heros.get(heroId);
                if (hero != null) {
                    hero.setState(HeroConstant.HERO_STATE_IDLE);
                }
                LogUtil.debug("--------------??????????????????????????? heroId: ", heroId);
            }
            LogUtil.debug("--------------???????????????????????? ??????  roleId:", player.roleId);
        } else if (str.equalsIgnoreCase("gestapaoRank")) { // ????????????????????????????????????
            GlobalActivityData actData = activityDataManager.getActivityMap().get(ActivityConst.ACT_GESTAPO_RANK);
            int now = TimeHelper.getCurrentSecond();
            if (actData != null) {
                LinkedList<ActRank> rankList = actData.getPlayerRanks(ActivityConst.ACT_GESTAPO_RANK);
                if (!CheckNull.isEmpty(rankList)) {
                    LogUtil.debug("--------------???????????????????????????????????? -------------");
                    for (ActRank rank : rankList) {
                        Player p = playerDataManager.getPlayer(rank.getLordId());
                        if (p != null) {
                            Activity act = activityDataManager.getActivityInfo(p, ActivityConst.ACT_GESTAPO_RANK);
                            if (act != null) {
                                long time = --now;
                                act.getStatusCnt().put(0, rank.getRankValue());
                                act.getStatusCnt().put(1, time);
                                LogUtil.debug("----?????????????????????????????? roleId:", p.roleId, ", rankValue:", rank.getRankValue(),
                                        ", time:", time);
                            }
                        }
                    }
                    LogUtil.debug("--------------????????????????????????????????????-------------");
                }
            }
        } else if (str.equalsIgnoreCase("autoExchange")) { // ????????????
            activityService.autoExchangUnrewardeMail(Integer.valueOf(count));
        } else if (str.equalsIgnoreCase("forceUpdate")) { // ????????????
            playerService.forceUpdate(Integer.valueOf(id), count);
        } else if (str.equalsIgnoreCase("superMine")) {
            worldDataManager.addCampSuperMine(1);
            worldDataManager.addCampSuperMine(2);
            worldDataManager.addCampSuperMine(3);
        } else if (str.equalsIgnoreCase("allportrait")) {
            playerService.checkHasHeroGivePortrait();
        } else if (str.equalsIgnoreCase("acrossTheDayProcess")) { // ????????????
            playerService.acrossTheDayProcess();
        } else if (str.equalsIgnoreCase("cityFrom")) {
            CityService cityService = DataResource.ac.getBean(CityService.class);
            WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
            // ???????????????????????????
            for (City city : globalDataManager.getGameGlobal().getCityMap().values()) {
                // ????????????????????????
                StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
                if (city.hasOwner() && !CheckNull.isNull(staticCity)) {
                    // ??????????????????
                    city.initNpcForm(true);
                    // ????????????????????????????????????
                    LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
                    if (null != battleList) {
                        for (Battle b : battleList) {
                            b.updateArm(city.getCamp(), city.getCurArm());
                            b.setDefencer(player);// ??????????????????
                        }
                    }
                }
            }
        } else if (str.equalsIgnoreCase("famousGeneralRank")) { // ????????????????????????
            fixFamousGeneralRank();
        } else if (str.equalsIgnoreCase("planeStatus")) {
            playerDataManager.getPlayers().forEach((lordId, p) -> {
                player.getAllOnBattleHeros().forEach(e -> e.getWarPlanes().clear());
                player.warPlanes.values().forEach(e -> {
                    e.setHeroId(0);
                    e.setPos(0);
                    e.setBattlePos(0);
                    e.setState(PlaneConstant.PLANE_STATE_IDLE);
                });
            });
        } else if (str.equalsIgnoreCase("mentorEquip")) {
            playerDataManager.getPlayers().values().forEach(p -> {
                p.getMentorInfo().getMentors().values()
                        .forEach(mentor -> mentorDataManager.checkAllBetterEquip(p, mentor));
            });
        } else if (str.equalsIgnoreCase("decisivePropTime")) {
            player.getDecisiveInfo().nextPropTime();
        } else if (str.equalsIgnoreCase("planeBattlePos")) { // ?????????????????????????????????
            playerDataManager.getPlayers().forEach((lordId, p) -> {
                p.warPlanes.values().stream()
                        .filter(plane -> plane.getHeroId() > 0 && p.heros.containsKey(plane.getHeroId()))
                        .forEach(plane -> {
                            Hero hero = p.heros.get(plane.getHeroId());
                            if (!CheckNull.isNull(hero)) {
                                plane.setPos(hero.getPos());
                                plane.setBattlePos(1);
                            }
                        });
            });
        } else if (str.equalsIgnoreCase("mentorSkill")) { // ??????????????????
            playerDataManager.getPlayers().forEach((roleId, p) -> {
                Map<Integer, MentorSkill> skillMap = p.getMentorInfo().getSkillMap();
                p.getMentorInfo().getMentors().forEach((k, mentor) -> {
                    mentorService.checkMentorSkill(skillMap, mentor);
                });
                CalculateUtil.reCalcBattleHeroAttr(p); // ??????????????????
            });

        } else if (str.equalsIgnoreCase("playerListKey")) {
            PlayerConstant.CLEAN_LIST_KEY.clear();
        } else if (str.equalsIgnoreCase("clearAndInitCity")) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            MapEntityGenerator mapEntityGenerator = cMap.getMapEntityGenerator();
            mapEntityGenerator.clearAndInitCity();
        } else if (str.equalsIgnoreCase("rebellionTime")) {
            rebelService.reloadData();
        } else if (str.equalsIgnoreCase("newMapCityBattle")) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            MapWarData mapWarData = cMap.getMapWarData();
            int now = TimeHelper.getCurrentSecond();
            for (StaticCity cityCfg : StaticCrossWorldDataMgr.getCityMap().values()) {
                int cityPos = cityCfg.getCityPos();
                List<BaseMapBattle> baseMapBattleList = mapWarData.getBattlesByPos(cityPos);
                if (!CheckNull.isEmpty(baseMapBattleList)) {
                    List<BaseMapBattle> tmpList = new ArrayList<>(baseMapBattleList);
                    for (BaseMapBattle b : tmpList) {
                        if (b.getBattle().getBattleTime() + 20 < now) {
                            BaseMapBattle.returnArmyBattle(mapWarData, b);
                            mapWarData.finishlBattle(b.getBattle().getBattleId());
                        }
                    }
                }
            }
        } else if (str.equalsIgnoreCase("heroBreakMedal")) {
            for (Player p : playerDataManager.getPlayers().values()) {
                for (Medal medal : p.medals.values()) {
                    if (medal.isOnMedal()) {
                        int heroId = medal.getHeroId();
                        Hero hero = p.heros.get(heroId);
                        if (hero == null) {
                            medal.downMedal();
                        }
                    }
                }

            }
        } else if (str.equalsIgnoreCase("rebel")) {
            Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.REBEL_NEXT_OPEN_TIME);
            // ???????????????????????????
            int nextTime = nextOpenMap.getOrDefault(0, 0);
            if (nextTime != 0) {
                // ???????????????????????????
                Date curOpen = TimeHelper.secondToDate(nextTime);
                Date fixDate = TimeHelper.getSomeDayAfterOrBerfore(curOpen, 1, 19, 0, 0);
                int fixTime = TimeHelper.dateToSecond(fixDate);
                nextOpenMap.put(0, fixTime);
                globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.REBEL_NEXT_OPEN_TIME, nextOpenMap);
                LogUtil.debug("??????????????????????????????, fixDate: ", DateHelper.formatDateMiniTime(fixDate));
            }
        } else if (str.equalsIgnoreCase("platNo")) {
            int oldPlatNo = Integer.parseInt(id);
            int newPlatNo = Integer.parseInt(count);
            for (Player p : playerDataManager.getPlayers().values()) {
                if (p.account.getPlatNo() == oldPlatNo) {
                    // ????????????
                    p.account.setPlatNo(newPlatNo);
                    // ?????????
                    p.account.setChildNo(oldPlatNo);
                }
            }
        } else if (str.equalsIgnoreCase("repairSuperEquip")) {
            // ???????????????
            int curCombat = Integer.parseInt(id);
            // ?????????????????????
            int toCombat = Integer.parseInt(count);
            if (StaticCombatDataMgr.getStaticCombat(toCombat) == null) {
                LogUtil.error("????????????Id??????, ???????????????, curCombat: ", curCombat, ", toCombat:", toCombat);
                return;
            }
            playerDataManager.getPlayers().values().stream()
                    .filter(p -> p.lord.combatId == curCombat)
                    .forEach(p -> {
                        p.lord.combatId = toCombat;
                        LogUtil.debug("??????roleId: ", p.roleId, ", ???????????????curCombat: ", curCombat, ", toCombat: ", toCombat);
                    });
        } else if (str.equalsIgnoreCase("webChat")) {
            playerDataManager.getPlayers()
                    .values()
                    .forEach(p -> {
                        Optional.ofNullable(p.activitys.get(ActivityConst.ACT_WECHAT_SIGNIN))
                                .ifPresent(activity -> {
                                    if (Objects.nonNull(StaticActivityDataMgr.getActivityByType(activity.getActivityType())) && !CheckNull.isEmpty(StaticActivityDataMgr.getActAwardById(activity.getActivityId())) && activity.getStatusMap().getOrDefault(7, 0) != 0) {
                                        activity.getStatusMap().clear();
                                    }
                                });
                    });
        } else if (str.equalsIgnoreCase("actSchedule")) {
            activityService.sendUnrewardedMail(Integer.parseInt(count), null);
            LogUtil.debug("fix actSchedule log, actType: ", count);
        } else if (str.equalsIgnoreCase("maxKeyId")) {
            Integer maxKeyId = 0;
            Collection<CrossWorldMap> crossMapArmy = DataResource.getBean(CrossWorldMapDataManager.class).getCrossWorldMapMap().values();
            if (!ObjectUtils.isEmpty(crossMapArmy)) {
                for (CrossWorldMap crossWorldMap : crossMapArmy) {
                    if (ObjectUtils.isEmpty(crossWorldMap))
                        continue;

                    MapMarch mapMarch = crossWorldMap.getMapMarchArmy();
                    if (ObjectUtils.isEmpty(mapMarch))
                        continue;
                    Map<Long, PlayerArmy> playerArmyMap = mapMarch.getPlayerArmyMap();
                    if (ObjectUtils.isEmpty(playerArmyMap))
                        continue;
                    PlayerArmy playerArmy = playerArmyMap.get(player.getLordId());
                    if (ObjectUtils.isEmpty(playerArmy)) {
                        continue;
                    }
                    Map<Integer, BaseArmy> baseArmyMap = playerArmy.getArmy();
                    if (ObjectUtils.isEmpty(baseArmyMap))
                        continue;
                    List<BaseArmy> maxKeyBaseArmy = baseArmyMap.values().stream().sorted(Comparator.comparingInt(BaseArmy::getKeyId)).collect(Collectors.toList());
                    maxKeyId = ObjectUtils.isEmpty(maxKeyBaseArmy) ? maxKeyId : maxKeyBaseArmy.get(maxKeyBaseArmy.size() - 1).getKeyId();
                }
            }

            List<Integer> armyKeyIds = player.armys.keySet().stream().sorted(Comparator.comparingInt(Integer::intValue)).collect(Collectors.toList());
            maxKeyId = ObjectUtils.isEmpty(armyKeyIds) ? maxKeyId : (maxKeyId < armyKeyIds.get(armyKeyIds.size() - 1) ? armyKeyIds.get(armyKeyIds.size() - 1) : maxKeyId);

            List<Integer> mailKeyIds = player.mails.keySet().stream().sorted(Comparator.comparingInt(Integer::intValue)).collect(Collectors.toList());
            maxKeyId = ObjectUtils.isEmpty(mailKeyIds) ? maxKeyId : (maxKeyId < mailKeyIds.get(mailKeyIds.size() - 1) ? mailKeyIds.get(mailKeyIds.size() - 1) : maxKeyId);

            List<Integer> equipKeyIds = player.equips.keySet().stream().sorted(Comparator.comparingInt(Integer::intValue)).collect(Collectors.toList());
            maxKeyId = ObjectUtils.isEmpty(equipKeyIds) ? maxKeyId : (maxKeyId < equipKeyIds.get(equipKeyIds.size() - 1) ? equipKeyIds.get(equipKeyIds.size() - 1) : maxKeyId);

            List<Integer> medalKeyIds = player.medals.keySet().stream().sorted(Comparator.comparingInt(Integer::intValue)).collect(Collectors.toList());
            maxKeyId = ObjectUtils.isEmpty(medalKeyIds) ? maxKeyId : (maxKeyId < medalKeyIds.get(medalKeyIds.size() - 1) ? medalKeyIds.get(medalKeyIds.size() - 1) : maxKeyId);

            List<Integer> stoneKeyIds = player.getStoneInfo().getStoneImproves().keySet().stream().sorted(Comparator.comparingInt(Integer::intValue)).collect(Collectors.toList());
            maxKeyId = ObjectUtils.isEmpty(stoneKeyIds) ? maxKeyId : (maxKeyId < stoneKeyIds.get(stoneKeyIds.size() - 1) ? stoneKeyIds.get(stoneKeyIds.size() - 1) : maxKeyId);

            List<BuildQue> buildQueueKeyIds = player.buildQue.values().stream().sorted(Comparator.comparingInt(BuildQue::getKeyId)).collect(Collectors.toList());
            maxKeyId = ObjectUtils.isEmpty(buildQueueKeyIds) ? maxKeyId : (maxKeyId < buildQueueKeyIds.get(buildQueueKeyIds.size() - 1).getKeyId() ? buildQueueKeyIds.get(buildQueueKeyIds.size() - 1).getKeyId() : maxKeyId);

            List<EquipQue> equipQueKeyIds = new ArrayList<>(player.equipQue).stream().sorted(Comparator.comparingInt(EquipQue::getKeyId)).collect(Collectors.toList());
            maxKeyId = ObjectUtils.isEmpty(equipQueKeyIds) ? maxKeyId : (maxKeyId < equipQueKeyIds.get(equipQueKeyIds.size() - 1).getKeyId() ? equipQueKeyIds.get(equipQueKeyIds.size() - 1).getKeyId() : maxKeyId);

            if (!ObjectUtils.isEmpty(player.factory)) {
                for (Factory factory : player.factory.values()) {
                    if (ObjectUtils.isEmpty(factory) || ObjectUtils.isEmpty(factory.getAddList()))
                        continue;

                    List<ArmQue> armQueList = new ArrayList<>(factory.getAddList()).stream().sorted(Comparator.comparingInt(ArmQue::getKeyId)).collect(Collectors.toList());
                    maxKeyId = ObjectUtils.isEmpty(armQueList) ? maxKeyId : (maxKeyId < armQueList.get(armQueList.size() - 1).getKeyId() ? armQueList.get(armQueList.size() - 1).getKeyId() : maxKeyId);
                }
            }

            if (player.getMaxKey() < maxKeyId) {
                LogUtil.debug("gm fix maxKeyId, lordId: ", player.getLordId(), ", originMaxKeyId: ", player.getMaxKey(), ", newMaxKeyId: ", maxKeyId);
                player.setMaxKey(maxKeyId);
            }
        } else if (str.equalsIgnoreCase("removeBattle")) {
            List<Battle> rallyBattleList = warDataManager.getBattlePosMap()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    // ??????????????????????????????????????????
                    .filter(battle -> DataResource.getBean(CampService.class).checkRallyBattle(battle, player))
                    .filter(battle -> {
                        int battlePos = battle.getPos();
                        // ????????????
                        return DataResource.getBean(CampService.class).getParticipatePlayer(battlePos, player);
                    })
                    .collect(Collectors.toList());

            for (Battle battle : rallyBattleList) {
                if (battle.getBattleTime() < TimeHelper.getCurrentSecond())
                    warDataManager.removeBattleById(battle.getBattleId());
            }
        } else if (str.equalsIgnoreCase("handleReport")) {
            LogUtil.debug("???????????????????????????: ", GameGlobal.closeExpiredReport);
            if ("close".equalsIgnoreCase(count)) {
                GameGlobal.closeExpiredReport = true;
            }
            if ("open".equalsIgnoreCase(count)) {
                GameGlobal.closeExpiredReport = false;
            }
            LogUtil.debug("???????????????????????????: ", GameGlobal.closeExpiredReport);
        } else if (str.equalsIgnoreCase("clearSummon")) {
            Collection<Player> players = playerDataManager.getAllPlayer().values();
            if (!ObjectUtils.isEmpty(players)) {
                players.forEach(p -> {
                    p.summon = null;
                });
            }
        }
    }

    private void fixFamousGeneralRank() {
        // ??????????????????
        int actType = ActivityConst.FAMOUS_GENERAL_TURNPLATE;
        List<ActRank> actRank = getActRank(actType, ActivityConst.DESC);
        if (CheckNull.isEmpty(actRank)) {
            LogUtil.debug("?????????????????????????????????,????????????...");
            return;
        }
        int actId = playerDataManager.getPlayer(actRank.get(0).getLordId()).activitys.get(actType).getActivityId();
        // ???????????????
        int now = TimeHelper.getCurrentSecond();
        List<StaticActAward> sActAward = StaticActivityDataMgr.getRankActAwardByActId(actId);
        int rankSize = actRank.size();
        for (StaticActAward sAward : sActAward) {
            List<Integer> param = sAward.getParam();
            int stage = param.get(0);
            if (stage == 6) continue;// ?????????6???
            int startRank = param.get(1);
            int endRank = sAward.getCond();
            if (startRank > rankSize) break;
            if (endRank > rankSize) endRank = rankSize;
            List<Award> awards = PbHelper.createAwardsPb(sAward.getAwardList());
            for (int r = startRank; r <= endRank; r++) {
                ActRank actR = actRank.get(r - 1);
                Player player = playerDataManager.getPlayer(actR.getLordId());
                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                        AwardFrom.ACT_UNREWARDED_RETURN, now, actType, actId, actType, actId);
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param actType
     * @param orderType ???????????? 1?????????
     * @return
     */
    public List<ActRank> getActRank(final int actType, int orderType) {
        List<ActRank> actRank = new ArrayList<>();
        // ??????????????????
        for (Player player : playerDataManager.getPlayers().values()) {
            Activity act = player.activitys.get(actType);
            if (act == null) continue;
            Long value = act.getStatusCnt().get(0); // ????????????????????????????????? 0 ?????????
            Long time = act.getStatusCnt().get(1);
            if (value == null || time == null) continue;
            actRank.add(new ActRank(player.roleId, actType, value.longValue(), time.intValue()));
        }
        if (orderType == ActivityConst.DESC) { // ??????
            actRank.sort((o1, o2) -> {
                if (o1.getRankValue() < o2.getRankValue()) {
                    return 1;
                } else if (o1.getRankValue() > o2.getRankValue()) {
                    return -1;
                } else {
                    // ???????????????????????????????????????id?????????????????????????????????????????????
                    if (o1.getRankTime() > o2.getRankTime()) {
                        return 1;
                    } else if (o1.getRankTime() < o2.getRankTime()) {
                        return -1;
                    }
                }
                return 0;
            });
        } else {
            actRank.sort((o1, o2) -> {
                if (o1.getRankValue() > o2.getRankValue()) {
                    return 1;
                } else if (o1.getRankValue() < o2.getRankValue()) {
                    return -1;
                } else {
                    // ???????????????????????????????????????id?????????????????????????????????????????????
                    if (o1.getRankTime() > o2.getRankTime()) {
                        return 1;
                    } else if (o1.getRankTime() < o2.getRankTime()) {
                        return -1;
                    }
                }
                return 0;
            });
        }
        return actRank;
    }

    private void gmAdd(Player player, String str, int id, int count, int lv, int refitLv) {
        if (str.equalsIgnoreCase("seasonscore")) {
            rewardDataManager.sendRewardSignle(player, AwardType.SPECIAL, AwardType.Special.SEASON_SCORE, count, AwardFrom.DO_SOME);
        }
        if (str.equalsIgnoreCase("biyuescore")) {
            rewardDataManager.sendRewardSignle(player, AwardType.SPECIAL, AwardType.Special.DIAOCHAN_SCORE, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("sandtablescore")) {
            rewardDataManager.sendRewardSignle(player, AwardType.SANDTABLE_SCORE, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("hero")) {
            rewardDataManager.sendRewardSignle(player, AwardType.HERO, count, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("prop")) {
            rewardDataManager.sendRewardSignle(player, AwardType.PROP, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("stone")) {
            rewardDataManager.sendRewardSignle(player, AwardType.STONE, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("equip")) {
            rewardDataManager.sendRewardSignle(player, AwardType.EQUIP, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("exp")) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.EXP, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("act")) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.ACT, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("arm1")) {
            rewardDataManager.sendRewardSignle(player, AwardType.ARMY, AwardType.Army.FACTORY_1_ARM, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("arm2")) {
            rewardDataManager.sendRewardSignle(player, AwardType.ARMY, AwardType.Army.FACTORY_2_ARM, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("arm3")) {
            rewardDataManager.sendRewardSignle(player, AwardType.ARMY, AwardType.Army.FACTORY_3_ARM, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("gold")) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("line")) {
            player.common.setLineAdd((int) count);
        } else if (str.equalsIgnoreCase("exploit")) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.EXPLOIT, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("herotoken")) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.HERO_TOKEN, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("credit")) {
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.CREDIT, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("oil")) {
            rewardDataManager.sendRewardSignle(player, AwardType.RESOURCE, AwardType.Resource.OIL, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("ele")) {
            rewardDataManager.sendRewardSignle(player, AwardType.RESOURCE, AwardType.Resource.ELE, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("food")) {
            rewardDataManager.sendRewardSignle(player, AwardType.RESOURCE, AwardType.Resource.FOOD, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("ore")) {
            rewardDataManager.sendRewardSignle(player, AwardType.RESOURCE, AwardType.Resource.ORE, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("human")) {
            rewardDataManager.sendRewardSignle(player, AwardType.RESOURCE, AwardType.Resource.HUMAN, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("uranium")) {
            rewardDataManager.sendRewardSignle(player, AwardType.RESOURCE, AwardType.Resource.URANIUM, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("camplead")) {
            campService.addCabinetLeadExp(player.lord.getCamp(), count);
        } else if (str.equalsIgnoreCase("pay")) { // android??????
            payService.gmPay(player, count, 0);
        } else if (str.equalsIgnoreCase("payios")) { // ios??????
            payService.gmPay(player, count, 1);
        } else if (str.equalsIgnoreCase("payNoEarn")) { // android???????????????????????????????????????
            payService.gmPayNoEarn(player, count, 0);
        } else if (str.equalsIgnoreCase("payiosNoEarn")) {
            payService.gmPayNoEarn(player, count, 1);
        } else if (str.equalsIgnoreCase("material")) {// ????????????
            List<List<Integer>> materials = StaticPropDataMgr.getEquip(id).getMaterial();
            for (int i = 0; i < count; i++) {
                for (List<Integer> m : materials) {
                    rewardDataManager.sendRewardSignle(player, m.get(0), m.get(1), m.get(2), AwardFrom.DO_SOME);
                }
            }
        } else if (str.equalsIgnoreCase("active")) {
            activityDataManager.updActivity(player, ActivityConst.ACT_ATTACK_CITY_NEW, count, 0, true);
        } else if (str.equalsIgnoreCase("gestaoRank")) {
            activityDataManager.updRankActivity(player, ActivityConst.ACT_GESTAPO_RANK, count);// ?????????
            activityDataManager.updGlobalActivity(player, ActivityConst.ACT_ATK_GESTAPO, count, player.lord.getCamp());
        } else if (str.equalsIgnoreCase("interaction")) {
            rewardDataManager.addAwardSignle(player, AwardType.SPECIAL, AwardType.Special.INTERACTION_CNT, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("redPacket")) {
            redPacketService.sendSysRedPacket(count, 0, "Gm??????", "1", "35005");
        } else if (str.equalsIgnoreCase("berlinCoin")) {
            player.addMilitaryExpenditure(count);
            // ????????????????????????
            LogLordHelper.commonLog("expenditure", AwardFrom.DO_SOME, player.account, player.lord,
                    player.getMilitaryExpenditure(), count);
        } else if (str.equalsIgnoreCase("heroDecoratedEquip")) {
            equipService.gmAddEquipForHeroDecorated(player, count);
        } else if (str.equalsIgnoreCase("equipNum")) {
            player.setMixtureData(PlayerConstant.EQUIP_MAKE_NUM, count);
        } else if (str.equalsIgnoreCase("honor")) {// ?????? ??????
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.HONOR, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("goldBar")) {// ?????? ??????
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD_BAR, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("plane")) { // ????????????
            rewardDataManager.sendRewardSignle(player, AwardType.PLANE, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("chip")) { // ????????????
            rewardDataManager.sendRewardSignle(player, AwardType.PLANE_CHIP, id, count, AwardFrom.DO_SOME);
        } else if (str.equals("rebelCredit")) {// ?????????????????????
            PlayerRebellion pr = player.getAndCreateRebellion();
            pr.addAndGetCredit(count);
        } else if (str.equals("pitchType1")) { // ????????????????????????
            PitchCombat pitchCombat = player.getOrCreatePitchCombat(StaticPitchCombat.PITCH_COMBAT_TYPE_1);
            pitchCombat.addCombatPoint(count);
        } else if (str.equalsIgnoreCase("mentorEquip")) { // ????????????
            rewardDataManager.sendRewardSignle(player, AwardType.MENTOR_EQUIP, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("mentorBill")) { // ??????
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.MENTOR_BILL, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("multCombat")) {
            MultCombat multCombat = player.getAndCreateMultCombat();
            multCombat.addCombatPoint(count);
        } else if (str.equalsIgnoreCase("jewel")) {
            // ????????????
            rewardDataManager.sendRewardSignle(player, AwardType.JEWEL, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("seasonIntegral")) {
            // ????????????
            rewardDataManager.sendRewardSignle(player, AwardType.PROP, PropConstant.WORLD_WAR_INTEGRAL, count,
                    AwardFrom.DO_SOME);
            worldWarSeasonWeekIntegralService.addWorldWarIntegral(player, count);
        } else if (str.equalsIgnoreCase("ringExchangeProof")) {
            // ???????????????
            rewardDataManager.sendRewardSignle(player, AwardType.PROP, PropConstant.EQUIP_RING_EXCHANGE_PROOF, count,
                    AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("castleSkin")) {
            // ??????
            rewardDataManager.sendRewardSignle(player, AwardType.CASTLE_SKIN, id, 1, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("randomReward")) {
            // ????????????
            rewardDataManager.sendRewardSignle(player, AwardType.RANDOM, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("activityIntegral")) {
            // ??????????????????
            // id ??????type count ????????????
            Activity activity = player.activitys.get(id);
            if (activity != null) {
                if (activity.getStatusCnt().containsKey(0)) {
                    activity.getStatusCnt().put(0, count + activity.getStatusCnt().get(0));
                } else {
                    activity.getStatusCnt().put(0, Long.valueOf(count));
                }
            }
        } else if (str.equalsIgnoreCase("goldIngot")) {
            // ??????
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD_INGOT, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("warfirecoin")) {
            //???????????? --- ?????????
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.WAR_FIRE_COIN, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("seasonTalentStone")) {
            rewardDataManager.sendRewardSignle(player, AwardType.SPECIAL, AwardType.Special.SEASON_TALENT_STONE, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("planeSearchCnt")) {
            // ??????????????????????????????
            player.setMixtureData(PlayerConstant.PLANE_FACTORY_MAKE_NUM,
                    player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_MAKE_NUM) + count);
        } else if (str.equalsIgnoreCase("battlePassExp")) {
            // ??????????????????
            Optional.ofNullable(battlePassDataManager.getPersonInfo(player.roleId)).ifPresent(personInfo -> personInfo.addExp(count));
        } else if (str.equalsIgnoreCase("pts")) {
            GlobalRoyalArena globalRoyalArena = (GlobalRoyalArena) activityDataManager.getGlobalActivity(ActivityConst.ACT_ROYAL_ARENA);
            if (globalRoyalArena != null) {
                // ?????????????????????????????????
                Optional.ofNullable(globalRoyalArena.getPersonInfoById(player.roleId)).ifPresent(personInfo -> royalArenaService.addPst(globalRoyalArena, personInfo, player, count));
            }
        } else if (str.equalsIgnoreCase("heroExp")) {
            // ??????????????????????????????
            try {
                Hero hero = heroService.checkHeroIsExist(player, id);
                heroService.addHeroExp(hero, count, player.lord.getLevel(), player);
            } catch (MwException e) {
                LogUtil.error(e);
            }
        } else if (str.equalsIgnoreCase("agentExp")) {
            Optional.ofNullable(player.getCia()).ifPresent(cia -> Optional.ofNullable(cia.getFemaleAngets().get(id)).ifPresent(agent -> ciaService.addGmAgentExp(player, agent, count)));
        } else if (str.equalsIgnoreCase("captainCityLv")) {
            City captainCity = worldDataManager.checkHasHome(player.lord.getCamp());
            if (captainCity != null) {
                cityService.upCaptainCityLv(captainCity, id);
                // captainCity.setBuildingExp(id);
            }
        } else if (str.equalsIgnoreCase("skin")) {
            rewardDataManager.addAward(player, AwardType.CASTLE_SKIN, id, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("anyMoney")) {
            // ??????????????????????????????
            rewardDataManager.sendRewardSignle(player, AwardType.MONEY, id, count, AwardFrom.DO_SOME);
        }
    }

    private void gmDel(Player player, String str, int id, int count, int lv, int refitLv) {
        try {
            ChangeInfo change = ChangeInfo.newIns();
            if (str.equalsIgnoreCase("prop")) {
                rewardDataManager.subProp(player, id, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.PROP, id);
            } else if (str.equalsIgnoreCase("stone")) {
                rewardDataManager.subStone(player, id, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.STONE, id);
            } else if (str.equalsIgnoreCase("arm1")) {
                rewardDataManager.subArmyResource(player, AwardType.Army.FACTORY_1_ARM, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.ARMY, AwardType.Army.FACTORY_1_ARM);
            } else if (str.equalsIgnoreCase("arm2")) {
                rewardDataManager.subArmyResource(player, AwardType.Army.FACTORY_2_ARM, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.ARMY, AwardType.Army.FACTORY_2_ARM);
            } else if (str.equalsIgnoreCase("arm3")) {
                rewardDataManager.subArmyResource(player, AwardType.Army.FACTORY_3_ARM, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.ARMY, AwardType.Army.FACTORY_3_ARM);
            } else if (str.equalsIgnoreCase("gold")) {
                rewardDataManager.subMoney(player, AwardType.Money.GOLD, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
            } else if (str.equalsIgnoreCase("herotoken")) {
                rewardDataManager.subMoney(player, AwardType.Money.HERO_TOKEN, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.MONEY, AwardType.Money.HERO_TOKEN);
            } else if (str.equalsIgnoreCase("credit")) {
                rewardDataManager.subMoney(player, AwardType.Money.CREDIT, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.MONEY, AwardType.Money.CREDIT);
            } else if (str.equalsIgnoreCase("act")) {
                rewardDataManager.subMoney(player, AwardType.Money.ACT, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.MONEY, AwardType.Money.ACT);
            } else if (str.equalsIgnoreCase("exploit")) {
                rewardDataManager.subMoney(player, AwardType.Money.EXPLOIT, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.MONEY, AwardType.Money.EXPLOIT);
            } else if (str.equalsIgnoreCase("oil")) {
                rewardDataManager.subResource(player, AwardType.Resource.OIL, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.RESOURCE, AwardType.Resource.OIL);
            } else if (str.equalsIgnoreCase("ele")) {
                rewardDataManager.subResource(player, AwardType.Resource.ELE, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.RESOURCE, AwardType.Resource.ELE);
            } else if (str.equalsIgnoreCase("food")) {
                rewardDataManager.subResource(player, AwardType.Resource.FOOD, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
            } else if (str.equalsIgnoreCase("ore")) {
                rewardDataManager.subResource(player, AwardType.Resource.ORE, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.RESOURCE, AwardType.Resource.ORE);
            } else if (str.equalsIgnoreCase("human")) {
                rewardDataManager.subResource(player, AwardType.Resource.HUMAN, count, AwardFrom.DO_SOME);
            } else if (str.equalsIgnoreCase("uranium")) {
                rewardDataManager.subResource(player, AwardType.Resource.URANIUM, count, AwardFrom.DO_SOME);
            } else if (str.equalsIgnoreCase("tech")) {// ????????????????????????
                player.tech.setTechLv(null);
            } else if (str.equals("equip")) {
                Equip equip = player.equips.get(count);
                if (Objects.isNull(equip)) {
                    LogUtil.common(String.format("gm del equip fail, not fount equip, roleId: %s, equipKey: %s", player.roleId, count));
                    return;
                }
                int heroId = equip.getHeroId();
                if (equip.isOnEquip()) {
                    Hero hero = player.heros.get(heroId);
                    if (Objects.nonNull(hero)) {
                        equipService.downEquip(player, hero, count);
                        LogUtil.common(String.format("gm down equip success, roleId: %d, equipKey: %d, heroId: %d", player.roleId, count, heroId));
                    }
                }
                player.equips.remove(count);
                LogUtil.common(String.format("gm del equip success, roleId: %d, equipKey: %d", player.roleId, count));
            } else if (str.equalsIgnoreCase("goldbar")) {
                rewardDataManager.subMoney(player, AwardType.Money.GOLD_BAR, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD_BAR);
            } else if (str.equalsIgnoreCase("ringExchangeProof")) {
                // ???????????????
                rewardDataManager.subProp(player, PropConstant.EQUIP_RING_EXCHANGE_PROOF, count, AwardFrom.DO_SOME);
            } else if (str.equalsIgnoreCase("seasonIntegral")) {
                // ????????????
                rewardDataManager.subProp(player, PropConstant.WORLD_WAR_INTEGRAL, count, AwardFrom.DO_SOME);
                worldWarSeasonWeekIntegralService.addWorldWarIntegral(player, -count);
            } else if (str.equalsIgnoreCase("allEquipsAndHeros")) {
                player.equips.clear();
                player.heros.clear();
                Arrays.fill(player.heroBattle, 0);
                Arrays.fill(player.heroWall, 0);
                Arrays.fill(player.heroAcq, 0);
                Arrays.fill(player.heroDef, 0);
            } else if (str.equalsIgnoreCase("hero")) {
                Hero hero = player.heros.get(count);
                if (hero != null) {
                    // ??????
                    int[] equips = hero.getEquip();
                    if (equips != null) {
                        for (int i : equips) {
                            Equip equip = player.equips.get(i);
                            if (equip != null) {
                                equip.downEquip();
                            }
                        }
                    }
                    // ??????
                    for (Integer i : hero.getWarPlanes()) {
                        WarPlane warPlane = player.warPlanes.get(i);
                        if (warPlane != null) {
                            warPlane.downBattle(hero);
                        }
                    }
                    // ??????
                    if (hero.getMedalKeyId() > 0) {
                        Medal medal = player.medals.get(hero.getMedalKeyId());
                        if (medal != null) {
                            medal.downMedal();
                        }
                    }
                    if (Objects.nonNull(hero.getTreasureWare())) {
                        TreasureWare treasureWare = player.treasureWares.get(hero.getTreasureWare());
                        if (null != treasureWare) {
                            treasureWare.downEquip();
                        }
                    }
                    player.heros.remove(count);
                }
            } else if (str.equalsIgnoreCase("goldIngot")) {
                // ??????
                rewardDataManager.subMoney(player, AwardType.Money.GOLD_INGOT, count, AwardFrom.DO_SOME);
            } else if (str.equalsIgnoreCase("warfirecoin")) {
                // ?????????
                rewardDataManager.subMoney(player, AwardType.Money.WAR_FIRE_COIN, count, AwardFrom.DO_SOME);
                change.addChangeType(AwardType.MONEY, AwardType.Money.WAR_FIRE_COIN);
                rewardDataManager.syncRoleResChanged(player, change);
            }
            // rewardDataManager.syncRoleResChanged(player, change);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gmParty(Player player, String str) {
        if (str.equalsIgnoreCase("openElect")) {
            campService.gmOpenElect(player.lord.getCamp());
        } else if (str.equalsIgnoreCase("closeElect")) {
            campService.gmCloseElect(player.lord.getCamp());
        } else if (str.equalsIgnoreCase("clearVote")) {// ????????????
            gmOpen(player, "myTask", 2);
            gmOpen(player, "worldTask", 2);
            campService.clearPartyVote();
        } else if (str.equalsIgnoreCase("initRYRB")) {
            int now = TimeHelper.getCurrentSecond();
            for (Player p : playerDataManager.getPlayers().values()) {
                // ??????????????????????????????????????????
                if (p.lord.getLevel() >= Constant.ROLE_GRADE_45
                        && p.getMixtureDataById(PlayerConstant.RYRB_LOCK_TIME) == 0) {
                    p.setMixtureData(PlayerConstant.RYRB_LOCK_TIME, now);
                }
            }
        }
    }

    private void gmSet(Player player, String str, int id, int count, String... param) throws MwException {
        if (str.equalsIgnoreCase("strong")) {
            player.isTester = true;
        } else if (str.equalsIgnoreCase("normal")) {
            player.isTester = false;
        } else if (str.equalsIgnoreCase("city")) {
            City city = worldDataManager.getCityById(count);
            city.initNpcForm(true);
            // city.setCamp(3);
        } else if (str.equalsIgnoreCase("playerArea")) {
            worldService.moveCityByGm(player, count);
        } else if (str.equalsIgnoreCase("washFull")) {
//            player.common.setWashCount(WorldConstant.HERO_WASH_FREE_MAX);
//            player.common.washTimeEnd(TimeHelper.getCurrentSecond());
        } else if (str.equalsIgnoreCase("merageArea")) {
            worldService.mergeArea(id, count);
        } else if (str.equalsIgnoreCase("allPlayerEffectProtect")) {
            playerService.setAllPlayerProtect(count);
        } else if (str.equalsIgnoreCase("berlinWinner")) { // ??????????????????
            BerlinWar bl = BerlinWar.getInstance();
            if (bl != null) {
                bl.getBerlinJobs().clear();
                bl.getBerlinCityInfo().setCamp(player.lord.getCamp());
                worldDataManager.getCityById(bl.getBerlinCityInfo().getCityId()).setCamp(player.lord.getCamp());
                berlinWarService.changeBerlinWinner(TimeHelper.getCurrentSecond(), bl, player.roleId);
            }
        } else if (str.equalsIgnoreCase("pos")) {
            player.lord.setPos(count);
        } else if (str.equalsIgnoreCase("medal")) {// ????????????
            // ????????????????????????
            StaticMedal staticmedal = StaticMedalDataMgr.getMedalById(count);
            if (staticmedal == null) {
                return;
            }
            Medal medal = null;
            try {
                medal = medalDataManager.initMedal(null, staticmedal);
            } catch (MwException e) {
                e.printStackTrace();
            }
            if (medal == null) {
                return;
            }
            medal.setKeyId(player.maxKey());
            player.medals.put(medal.getKeyId(), medal);
            LogUtil.debug("GM????????????--???roleId:", player.roleId,
                    ",medalId:" + medal.getMedalId() + ",medalKeyId=" + medal.getKeyId() + ",level=" + medal.getLevel()
                            + ",medalAttr=" + medal.getMedalAttr() + ",auraSkillId=" + medal.getAuraSkillId()
                            + ",specialSkillId=" + medal.getSpecialSkillId() + ",generalSkillId="
                            + medal.getGeneralSkillId());
            // ???????????????????????????
            LogLordHelper.medal(AwardFrom.DO_SOME, player.account, player.lord, medal.getMedalId(), medal.getKeyId(),
                    Constant.ACTION_ADD);
        } else if (str.equalsIgnoreCase("pitchCombatType1")) {
            PitchCombat pc = player.getOrCreatePitchCombat(StaticPitchCombat.PITCH_COMBAT_TYPE_1);
            List<StaticPitchCombat> sPcList = StaticCombatDataMgr
                    .getPitchCombatGroupByType(StaticPitchCombat.PITCH_COMBAT_TYPE_1);
            StaticPitchCombat spc = sPcList.stream().filter(p -> p.getCombatId() == count).findFirst().orElse(null);
            if (spc != null) pc.setHighestCombatId(spc.getCombatId());

        } else if (str.equalsIgnoreCase("multCombat")) {// ??????????????????
            MultCombat mCombat = player.getAndCreateMultCombat();
            if (count == 0 || StaticCombatDataMgr.getMultCombatById(count) != null) {
                mCombat.setHighestCombatId(count);
                mCombat.getTodayCombatId().clear();
            }
        } else if (str.equalsIgnoreCase("ptrophy")) {
            player.trophy.put(id, count);
        } else if (str.equalsIgnoreCase("testPush")) {
            PushMessageUtil.pushMessage(player.account, PushConstant.ACT_IS_FULL);
        } else if (str.equalsIgnoreCase("siginInfo")) { //??????????????????
            player.siginInfo.setTimes(count);
        } else if (str.equalsIgnoreCase("isGm")) {
            if (count != 0 && count != 1) {
                LogUtil.error("??????gm??????, ????????????, isGm:", id);
                return;
            }
            // ?????????gm??????
            int before = player.account.getIsGm();
            if (before != count) {
                player.account.setIsGm(count);
                // ???????????????gm??????
                accountDao.updateAccountGm(player.account);
                if (player.ctx != null) {
                    player.ctx.close();// ?????????
                    player.logOut();// ??????
                }
            }
        } else if (str.equalsIgnoreCase("forbidCreateRole")) {
            // ????????????????????????
            serverSetting.setForbidCreateRole(count);
        } else if (str.equalsIgnoreCase("warFirePlayerScore")) {
            GlobalWarFire gwf = crossWorldMapDataManager.getGlobalWarFire();
            PlayerWarFire pwf = gwf.getPlayerWarFire(player.roleId);
            gwf.addPlayerScore(pwf, count, AwardFrom.DO_SOME);
        } else if (str.equalsIgnoreCase("autoAtkWarFire")) {
            Java8Utils.syncMethodInvoke(() -> {
                String[] split = param[0].split(",");
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                Optional.ofNullable(cMap.getCityMapEntityByCityId(id))
                        .ifPresent(cme -> {
                            cMap.getPlayerMap().values().stream()
                                    .map(PlayerMapEntity::getPlayer)
                                    .filter(p -> {
                                        int camp = p.lord.getCamp();
                                        boolean flag = false;
                                        for (String s : split) {
                                            if (Integer.valueOf(s) == camp) {
                                                flag = true;
                                                break;
                                            }
                                        }
                                        return flag;
                                    }).limit(count)
                                    .forEach(p -> {
                                        List<Hero> heros = p.getAllOnBattleHeros();
                                        heros.stream().filter(hero -> hero.getState() == ArmyConstant.ARMY_STATE_IDLE)
                                                .forEach(hero -> {
                                                    try {
                                                        AttackCrossPosRq.Builder builder = AttackCrossPosRq.newBuilder();
                                                        int pos = cme.getPos();
                                                        builder.setPos(pos);
                                                        builder.setMapId(CrossWorldMapConstant.CROSS_MAP_ID);
                                                        builder.addHeroId(hero.getHeroId());
                                                        crossAttackService.attackCrossPos(p.roleId, builder.build());
                                                    } catch (MwException e) {
                                                        LogUtil.error(e, "??????????????????????????????");
                                                    }
                                                });
                                    });

                        });
            });
        } else if (str.equalsIgnoreCase("techLv")) {
            StaticTechLv s_tech = StaticBuildingDataMgr.getTechLvMap(id, count);
            if (Objects.isNull(s_tech)) throw new MwException(GameError.PARAM_ERROR.getCode());
            Map<Integer, TechLv> lvMap = player.tech.getTechLv();
            TechLv tech = lvMap.computeIfAbsent(id, t -> new TechLv(id, count, 0));
            tech.setLv(count);
        } else if (str.equalsIgnoreCase("autoVisitAltar")) {
            Java8Utils.syncMethodInvoke(() -> {
                String[] split = param[0].split(",");
                worldDataManager.getAltarMap().values()
                        .forEach(altar -> {
                            int pos = altar.getPos();
                            int area = MapHelper.getAreaIdByPos(pos);
                            playerDataManager.getPlayerByArea(area).values().stream()
                                    .filter(p -> {
                                        int camp = p.lord.getCamp();
                                        boolean flag = false;
                                        for (String s : split) {
                                            if (Integer.valueOf(s) == camp) {
                                                flag = true;
                                                break;
                                            }
                                        }
                                        return flag;
                                    }).limit(count)
                                    .forEach(p -> {
                                        // ????????????????????????
                                        playerDataManager.autoAddArmy(p);
                                        p.getAllOnBattleHeros()
                                                .stream()
                                                .filter(Hero::isIdle)
                                                .peek(hero -> hero.setCount(hero.getAttr()[Constant.AttrId.LEAD]))
                                                .findAny()
                                                .ifPresent(hero -> {
                                                    GamePb2.AttackPosRq.Builder builder = GamePb2.AttackPosRq.newBuilder();
                                                    builder.setPos(pos);
                                                    builder.addHeroId(hero.getHeroId());
                                                    builder.setType(id);
                                                    try {
                                                        worldService.attackPos(p.roleId, builder.build());
                                                    } catch (MwException e) {
                                                        LogUtil.error(e, "????????????GM??????");
                                                    }
                                                });
                                    });
                        });
            });
        }
    }

    private void processNextCombat(Player player, Combat preCombat) {
        List<StaticCombat> list = StaticCombatDataMgr.getPreIdCombat(preCombat.getCombatId());
        if (list == null || list.isEmpty()) {
            return;
        }
        for (StaticCombat staticCombat : list) {
            if (staticCombat.getType() == CombatType.type_2) {
                CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
                int combatId = staticCombat.getCombatId();
                if (combatFb == null) {
                    combatFb = new CombatFb(combatId, 0, Constant.COMBAT_RES_CNT,
                            TimeHelper.getCurrentSecond() + Constant.COMBAT_RES_TIME);
                    player.combatFb.put(combatId, combatFb);
                }
            }
        }
    }

    private void gmOpen(Player player, String str, int count, String... param) {
        if (count <= 0) {
            return;
        }
        if (str.equalsIgnoreCase("combat")) {
            player.combats.clear();
            player.combatFb.clear();
            player.lord.combatId = 0;
            for (int i = 0; i <= count; i++) {
                StaticCombat e = StaticCombatDataMgr.getCombatMap().get(i);
                if (e == null) {
                    continue;
                }
                if (e.getType() == 1 && e.getCombatId() > player.lord.combatId) {
                    player.lord.combatId = e.getCombatId();
                }
                int star = 1;
                if (e.getCnt() == 0) {
                    star = 3;
                }
                if (e.getType() == 1) {
                    Combat combat = player.combats.get(e.getCombatId());
                    if (combat == null) {
                        combat = new Combat(e.getCombatId(), star);
                        player.combats.put(e.getCombatId(), combat);
                    }
                    combat.setStar(star);
                    processNextCombat(player, combat);
                } else {
                    CombatFb combat = player.combatFb.get(e.getCombatId());
                    if (combat == null) {
                        combat = new CombatFb(e.getCombatId(), 1, 0, -1);
                        player.combatFb.put(e.getCombatId(), combat);
                    }
                }
            }
            rankDataManager.setStars(player.lord);
        } else if (str.equalsIgnoreCase("stoneCombat")) {
            if (StaticCombatDataMgr.getStoneCombatById(count) == null) {
                LogUtil.error("????????????????????? ", count);
                return;
            }
            player.stoneCombats.clear();
            if (count != 0) {
                List<StaticStoneCombat> spt = StaticCombatDataMgr.getStonePreCombat(0);
                while (!CheckNull.isEmpty(spt)) {
                    StaticStoneCombat ssc = spt.get(0);
                    StoneCombat sc = new StoneCombat(ssc.getCombatId(), 1);
                    player.stoneCombats.put(ssc.getCombatId(), sc);
                    if (count == ssc.getCombatId()) {
                        break;
                    }
                    spt = StaticCombatDataMgr.getStonePreCombat(ssc.getCombatId());
                }
            }

        } else if (str.equalsIgnoreCase("tech")) {
            for (StaticTechLv nowTechLv : StaticBuildingDataMgr.getTechMap().values()) {
                player.tech.getTechLv().put(nowTechLv.getTechId(), new TechLv(nowTechLv.getTechId(), nowTechLv.getLv(),
                        nowTechLv.getCnt() > 0 ? nowTechLv.getCnt() : 1));
            }
        } else if (str.equalsIgnoreCase("openOrder")) {
            globalDataManager.openAreaData(count);
        } else if (str.equalsIgnoreCase("solarTerms")) {// ??????????????????
            solarTermsDataManager.setSolarTermsBeginTime(TimeHelper.getCurrentSecond());
            buildingDataManager.refreshSourceData(player);
        }
    }

    /**
     * ??????????????????
     */
    private void repairCombat() {
        Iterator<Player> it = playerDataManager.getPlayers().values().iterator();
        Combat combat;
        int cnt = 0;
        while (it.hasNext()) {
            Player player = (Player) it.next();
            if (player == null || player.account == null || player.lord == null) {
                continue;
            }
            if (player.combats.isEmpty()) {
                continue;
            }
            Iterator<Combat> combats = player.combats.values().iterator();
            while (combats.hasNext()) {
                combat = combats.next();
                if (combat.getCombatId() > player.lord.combatId) {
                    player.lord.combatId = combat.getCombatId();
                    cnt++;
                }
            }
        }
        LogUtil.debug("????????????????????????=" + cnt);
    }

    public void gmMail(Mail mail, String to) {

    }

    public void gmSystem(String str) throws MwException {
        if (str.equalsIgnoreCase("loadSystem")) {
            loadService.loadSystem();
            // ScheduleManager.loadWorldRule();
            sandTableContestService.resetContestDate4LoadSystem();
        } else if (str.equalsIgnoreCase("loadTable")) {
            loadService.loadAll();
            loadService.checkValid();
            ScheduleManager.getInstance().loadOnConfigChange();
            ScheduleManager.getInstance().initLightningWarJob();
            seasonService.gm_reload();
            activityService.syncActListChg();
            activityTemplateService.handleReloadActivityConfig();
            DataResource.getBean(CrossGamePlayService.class).initSchedule(ScheduleManager.getInstance().getSched());
        } else if (str.equalsIgnoreCase("loadBlackWords")) {
            loadService.loadChat();
        } else if (str.equalsIgnoreCase("mergeBanner")) {
            loadService.loadBanner();
        } else if (str.equalsIgnoreCase("loadNewArea")) {
            StaticWorldDataMgr.init("loadNewArea");
            LogUtil.common("???????????????????????????????????????????????????,??????...");
            worldDataManager.initNewWorldData();
            LogUtil.common("???????????????????????????,??????...");
        } else if (str.equalsIgnoreCase("loadActTable")) {// ????????????????????????
            LogUtil.common("------------------?????????????????????-----------------");
            StaticActivityDataMgr.init();
            ScheduleManager.getInstance().loadOnConfigChange();
            activityService.syncActListChg();
        } else if (str.equalsIgnoreCase("mergeInit")) {// ???????????????????????????
            // ?????????????????????????????????
            Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
            int firstOpenTime = TimeHelper.getSomeDayAfter(1, 12, 0, 0);
            for (Camp camp : partyMap.values()) {
                camp.setEndTime(firstOpenTime);
            }
        } else if (str.equals("mergeBefore")) {// ??????????????????
            //?????????????????????????????????
            int now = TimeHelper.getCurrentSecond();
            playerDataManager.getPlayers().values().forEach(player -> {
                activityService.combineServerAct(player, now);
                mailService.gainAttrMaill(player);
            });
        } else if (str.equalsIgnoreCase("onWeekEnd")) {
            Java8Utils.syncMethodInvoke(() -> {
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                cMap.getWorldWarOpen().onWeekEnd();
            });
        } else if (str.equalsIgnoreCase("goToNewMap")) {
            Java8Utils.syncMethodInvoke(() -> {
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                worldDataManager.getPlayerInArea(WorldConstant.AREA_TYPE_13).stream().filter(player -> {
                    return CheckNull.isEmpty(player.armys) && !player.getDecisiveInfo().isDecisive();
                }).forEach(player -> {
                    int prePos = player.lord.getPos();
                    playerDataManager.getPlayerByArea(player.lord.getArea()).remove(player.roleId);
                    worldDataManager.removePlayerPos(prePos, player);
                    int newPos = cMap.getRandomOpenEmptyPosSafeArea(player.lord.getCamp());
                    int newArea = cMap.getMapId();
                    PlayerMapEntity mapEntity = new PlayerMapEntity(newPos, player);
                    cMap.addWorldEntity(mapEntity);
                    player.lord.setPos(newPos);
                    player.lord.setArea(newArea);
                });
            });
        } else if (str.equalsIgnoreCase("onSeasonEnd")) {
            Java8Utils.syncMethodInvoke(() -> {
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                cMap.getWorldWarOpen().onSeasonEnd();
            });
        } else if (str.equalsIgnoreCase("reloadCrossDate")) {
            Java8Utils.syncMethodInvoke(() -> {
                StaticCrossWorldDataMgr.init();
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                cMap.getWorldWarOpen().onAcrossDayRun();
            });
        } else if (str.equalsIgnoreCase("partyHonor")) {
            Java8Utils.syncMethodInvoke(() -> {
                Arrays.stream(Constant.Camp.camps).forEach(camp -> {
                    Camp party = campDataManager.getParty(camp);
                    Optional.ofNullable(StaticPartyDataMgr.getHonorGift(3, party.getPartyLv()))
                            .ifPresent(sConf -> {
                                party.setBuild(sConf.getBuild());
                                party.setCityBattle(sConf.getCityBattle());
                                party.setCampBattle(sConf.getCampBattle());
                            });
                });
            });

        } else if (str.equalsIgnoreCase("modifyServerInfo")) {
            HttpPb.ModifyServerInfoRq.Builder req = HttpPb.ModifyServerInfoRq.newBuilder();
            int currentSecond = TimeHelper.getCurrentSecond();
            req.setOpenTime(currentSecond + TimeHelper.DAY_S);
            gmModifyServerInfo(req.build());
        } else if ("openEventDebug".equalsIgnoreCase(str)) {
            GameGlobal.openEventDebug = !GameGlobal.openEventDebug;
            SendEventDataThread.THINKINGDATA_LOGGER.info(String.format("current debug status:%s", GameGlobal.openEventDebug));
        } else if ("loadUseProp".equalsIgnoreCase(str)) {
            try {
                DataResource.ac.getBean(PropDataManager.class).load();
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
    }

    public void recalcResource() {

    }

    public void gmKick(String name) {
        Player player = playerDataManager.getPlayer(name);
        if (player != null && player.isLogin && player.account.getIsGm() == 0) {
            if (player.ctx != null) {
                player.ctx.close();
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param type
     * @param roleId
     */
    public void apicmd(String type, long roleId, String... params) {
        //????????????id???????????????
        if (type.equalsIgnoreCase("kickoffline")) {
            Player player = playerDataManager.getPlayer(roleId);
            if (Objects.nonNull(player) && player.isLogin) {
                LogUtil.debug("kick offline ,roleId=" + roleId + ", nick=" + player.lord.getNick());
                GamePb4.SyncForceUpdateRs.Builder builder = GamePb4.SyncForceUpdateRs.newBuilder();
                builder.setType(0);
                builder.setParam("0");
                BasePb.Base base = PbHelper.createSynBase(GamePb4.SyncForceUpdateRs.EXT_FIELD_NUMBER, GamePb4.SyncForceUpdateRs.ext, builder.build()).build();
//                MsgDataManager.getIns().add(new Msg(player.ctx, base.build(), player.roleId));
                playerService.syncMsgToPlayer(base, player);
//                if(Objects.nonNull(player.ctx)){
//                    player.ctx.close();
//                }
//                player.tickOut();
//                player.logOut();
            }
        }
    }

    public void gmSilence(String name, int s) {
        Player player = playerDataManager.getPlayer(name);
        if (player != null && player.account.getIsGm() == 0) {
            player.lord.setSilence(s);
            int now = TimeHelper.getCurrentSecond();
            // ????????????????????????
            rewardDataManager.syncRoleResChanged(player, rewardDataManager.createChangeInfoPb(-1, s >= now || s == 1 ? 1 : 0, s));
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_SILENCE, now);
        }
    }

    public void gmForbidden(String name, int s) {
        Player player = playerDataManager.getPlayer(name);
        if (player != null && player.account.getIsGm() == 0) {
            player.account.setForbid(s);
        }
    }

    public void gmVip(int s, Player player) {
//        player.lord.setVip(s);
        vipDataManager.setVip(player, s);
    }

    private void gmTopup(String name, int s) {
        Player player = playerDataManager.getPlayer(name);
        if (player != null && (s >= 0)) {
            player.lord.setTopup(s);
            // ?????????????????????
            if (player.lord.getTopup() >= ActParamConstant.ACT_DEDICATED_CUSTOMER_SERVICE_CONF.get(0).get(0)) {
                activityDataManager.updActivity(player, ActivityConst.ACT_DEDICATED_CUSTOMER_SERVICE, TimeHelper.getCurrentSecond(), 0, true);
            }
            if (player.lord.getLevel() >= Constant.AUTO_SENDMAIL_LEVEL && player.lord.getTopup() == Constant.AUTO_SENDMAIL_VIP_LV && player.getMixtureDataById(PlayerConstant.SEND_CHANNEL_MAIL) == 0) {
                rewardDataManager.sendChannelMail(player);
            }
        }
    }

    private void gmClear(Player player, String str, int count) {
        if (str.equalsIgnoreCase("mails")) {
            player.mails.clear();
        } else if (str.equalsIgnoreCase("exchange")) {
            player.boughtIds.clear();
        } else if (str.equalsIgnoreCase("maward")) {
            player.awardedIds.clear();
        } else if (str.equalsIgnoreCase("camplead")) {
            // ????????????????????????????????????
            Camp camp = campDataManager.getParty(player.lord.getCamp());
            if (camp != null) {
                camp.setCabinetLeadExp(0);
                camp.setCabinetLeadLv(StaticBuildingDataMgr.getCabinetMinLv());
            }
        } else if (str.equalsIgnoreCase("city")) {
            // ???????????????????????????
            for (City city : globalDataManager.getGameGlobal().getCityMap().values()) {
                city.clearFormList();
            }
        } else if (str.equalsIgnoreCase("cityinit")) {
            // ????????????????????????
            for (City city : globalDataManager.getGameGlobal().getCityMap().values()) {
                city.clearStateToInit();
            }
        } else if (str.equalsIgnoreCase("areaState")) {// ??????????????????
            globalDataManager.resetAreaState();
        } else if (str.equalsIgnoreCase("worldChat")) {// ??????????????????
            chatDataManager.getWorldChat().clear();
        } else if (str.equalsIgnoreCase("campChat")) {// ??????????????????
            chatDataManager.getCampChat(player.lord.getCamp()).clear();
        } else if (str.equalsIgnoreCase("solarTerms")) {// ??????????????????
            solarTermsDataManager.setSolarTermsBeginTime(0);
        } else if (str.equalsIgnoreCase("firstPayDouble")) {// ????????????????????????
            player.firstPayDouble.clear();
        } else if (str.equals("quipQue")) {// ??????????????????????????????
            player.equipQue.clear();
        } else if (str.equalsIgnoreCase("firstKill")) { // ????????????
            for (Area area : globalDataManager.getGameGlobal().getAreaMap().values()) {
                area.clearFirstKillInfo();
            }
        } else if (str.equalsIgnoreCase("atkCityAct")) { // ??????????????????
            playerDataManager.getPlayers().values().forEach(p -> {
                p.atkCityAct.clear();
            });
        } else if (str.equalsIgnoreCase("sendChatCnt")) { // ????????????????????????
            worldDataManager.getSendChatCnt().clear();
        } else if (str.equalsIgnoreCase("lightningWarMap")) {
            globalDataManager.getGameGlobal().getLightningWarBossMap().clear();
        } else if (str.equalsIgnoreCase("desTrain")) { // ????????????????????????
            // TODO: 2018-06-02 ??????????????????????????????,????????????????????????
            player.buildingExts.get(51).setType(0);
        } else if (str.equalsIgnoreCase("prop")) { // ??????????????????
            player.props.clear();
        } else if (str.equalsIgnoreCase("cityStatus")) {// ?????????????????????????????????
            WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
            for (City city : globalDataManager.getGameGlobal().getCityMap().values()) {
                StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
                if (staticCity != null) {
                    LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
                    if (CheckNull.isEmpty(battleList)) {
                        city.setStatus(WorldConstant.CITY_STATUS_CALM);
                    }
                }
            }
        } else if (str.equalsIgnoreCase("allCaptainCityLv")) {// ??????????????????
            for (int camp = 1; camp <= 3; camp++) {
                City captainCity = worldDataManager.checkHasHome(camp);
                if (captainCity != null) {
                    captainCity.setCityLv(1);
                    captainCity.setBuildingExp(0);
                }
                superMineService.gmClearAndRefreshSuperMine();
            }
        } else if (str.equalsIgnoreCase("captainCityLv")) {// ??????????????????
            City captainCity = worldDataManager.checkHasHome(player.lord.getCamp());
            if (captainCity != null) {
                captainCity.setCityLv(1);
                captainCity.setBuildingExp(0);
                superMineService.gmClearAndRefreshSuperMineByCamp(player.lord.getCamp());
            }
        } else if (str.equalsIgnoreCase("superMine")) {
            superMineService.gmRefreshSuperMine();
        } else if (str.equalsIgnoreCase("allCityPortect")) {// ?????????????????????
            for (City city : globalDataManager.getGameGlobal().getCityMap().values()) {
                city.setProtectTime(0);
            }
        } else if (str.equalsIgnoreCase("cheatCode")) {// ?????????????????????????????????????????????
            for (Player p : playerDataManager.getPlayers().values()) {
                p.cheatCode = null;
            }
        } else if (str.equalsIgnoreCase("banditCnt")) {// ????????????????????? ???????????????????????????
            for (Player p : playerDataManager.getPlayers().values()) {
                p.setBanditCnt(0);
            }
        } else if (str.equals("hero")) { // ???????????????????????????????????????
            List<Integer> rmHeroId = player.heros.values().stream()
                    .filter(h -> h.getDefPos() == 0 && h.getAcqPos() == 0 && h.getPos() == 0 && h.getWallPos() == 0)
                    .map(h -> h.getHeroId()).distinct().collect(Collectors.toList());
            rmHeroId.forEach(heroId -> player.heros.remove(heroId));
            // ?????????????????????
            for (Iterator<Integer> it = player.equips.keySet().iterator(); it.hasNext(); ) {
                Integer key = it.next();
                Equip e = player.equips.get(key);
                if (e.getHeroId() > 0 && rmHeroId.contains(e.getHeroId())) {
                    player.equips.remove(key);
                }
            }
        } else if (str.equalsIgnoreCase("medalGoods")) {// ????????????????????????????????????
            for (Player p : playerDataManager.getPlayers().values()) {
                medalDataManager.initMedalGoods(p, MedalConst.MEDAL_GOODS_GM_TYPE);
            }
        } else if (str.equalsIgnoreCase("friend")) { // ??????????????????????????????
            FriendService.rmInvalidFriend(player);
        } else if (str.equalsIgnoreCase("chip")) { // ??????????????????
            player.palneChips.clear();
        } else if (str.equalsIgnoreCase("planeData")) { // ??????????????????????????????
            playerDataManager.getPlayers().values().stream().forEach(p -> {
                p.getAllOnBattleHeros().stream().forEach(hero -> hero.getWarPlanes().clear());
                p.warPlanes.clear();
                p.palneChips.clear();
            });
        } else if (str.equalsIgnoreCase("actGiftPacket")) { // ????????????????????????????????????
            playerDataManager.getPlayers().values().stream().forEach(p -> {
                Activity activity = p.activitys.get(ActivityConst.ACT_VIP_BAG);
                if (activity != null) {
                    activity.cleanActivity(false);
                }
            });
        } else if (str.equalsIgnoreCase("allFirstPayDouble")) {
            // GmService.clearFlag = true;
            for (Player p : playerDataManager.getPlayers().values()) {
                p.firstPayDouble.clear();
            }
        } else if (str.equalsIgnoreCase("cancelAllFirstPayDouble")) {
            GmService.clearFlag = false;
        } else if (str.equalsIgnoreCase("mentor")) {
            for (Player p : playerDataManager.getPlayers().values()) {
                MentorInfo mentorInfo = p.getMentorInfo();
                if (mentorInfo != null) {
                    mentorInfo.getMentors().clear();
                    mentorInfo.getEquipMap().clear();
                    mentorInfo.getSkillMap().clear();
                    mentorInfo.getBetterEquip().clear();
                    LogUtil.debug("---------??????????????????---------- roleId:", p.roleId);
                }
            }
        } else if (str.equalsIgnoreCase("decisive")) { // ??????????????????
            playerDataManager.getPlayers().values().forEach(p -> p.getDecisiveInfo().setDecisive(false));
        } else if (str.equalsIgnoreCase("multCombatTrophy")) {
            Trophy trophy = globalDataManager.getGameGlobal().getTrophy();
            trophy.getPassMultCombat().clear();
        } else if (str.equalsIgnoreCase("airship")) {
            airshipService.gmRefreshAirship();
        } else if (str.equalsIgnoreCase("10mentorEquip")) {
            doGmRmMentorEquip();
        } else if (str.equalsIgnoreCase("supplyRecord")) { // ?????????????????????????????????
            player.getSupplyRecord().gmClear();
        } else if (str.equalsIgnoreCase("campRecord")) { // ?????????????????????????????????
            Map<Integer, Camp> partyMap = campDataManager.getPartyMap();
            partyMap.values().forEach(party -> {
                party.getSupplyRecord().gmClear();
                party.getPartySupplies().clear();
            });

        } else if (str.equalsIgnoreCase("heroState")) {
            player.heros.values().forEach(hero -> hero.setState(0));
        } else if (str.equalsIgnoreCase("historyWinner")) {
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            // ????????????????????????
            berlinWar.getRoleInfos().clear();
            // ???????????????????????????
            berlinWar.setAtkCD(0);
            // ????????????
            berlinWar.getBerlinJobs().clear();
            // ????????????
            berlinWar.getHistoryWinner().clear();
            berlinWar.getBerlinCityInfo().setNextAtkTime(-1);
            berlinWar.getBerlinCityInfo().setCamp(Constant.Camp.NPC);
            berlinWar.getBattlefronts().values().forEach(bf -> {
                bf.setCamp(Constant.Camp.NPC);
                bf.setNextAtkTime(-1);
            });
        } else if (str.equalsIgnoreCase("agentExp")) {
            Optional.ofNullable(player.getCia())
                    .ifPresent(cia -> cia.getFemaleAngets().values().forEach(femaleAgent -> {
                        int attrVal = femaleAgent.getAttrVal();
                        femaleAgent.setExp(0);
                        //????????????
                        StaticAgent sNextAgent = StaticCiaDataMgr.getAgentConfByAgent(femaleAgent);
                        if (sNextAgent != null) {
                            femaleAgent.setAttrVal(sNextAgent.getAttributeVal());
                            femaleAgent.setSkillVal(sNextAgent.getSkillVal());
                            femaleAgent.setQuality(sNextAgent.getQuality());
                            if (attrVal != femaleAgent.getAttrVal()) {
                                // ??????????????????????????????
                                CalculateUtil.reCalcBattleHeroAttr(player);
                            }
                        }
                    }));
        } else if (str.equalsIgnoreCase("crossMapEntity")) {
            Java8Utils.syncMethodInvoke(() -> {
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
                if (cMap != null) {
                    MapEntityGenerator mapEntityGenerator = cMap.getMapEntityGenerator();
                    // ???????????????????????????????????????????????????
                    mapEntityGenerator.cleanAndRefreshMine();
                    mapEntityGenerator.clearAndInitCity();
                    mapEntityGenerator.cleanAndRefreshBandit();
                    mapEntityGenerator.initAndRefreshAirship();
                }
            });
        } else if (str.equalsIgnoreCase("act_211")) {
            ActTurnplat turnplat = (ActTurnplat) activityDataManager.getActivityInfo(player, 211);
            Optional.ofNullable(turnplat).ifPresent(o -> {
                o.setTodayCnt(0);
                o.setGoldCnt(0);
                o.setCnt(0);
                o.getWinCnt().clear();
                o.getWinCnt211().clear();
            });
        } else if (str.equalsIgnoreCase("warFireMapData")) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            GlobalWarFire gwf = cMap != null ? cMap.getGlobalWarFire() : null;
            int stage = gwf == null ? -1 : gwf.getStage();
            if (stage == GlobalWarFire.STAGE_END_PLAYER || stage == GlobalWarFire.STAGE_OVER) {
                gwf.endLogic();
            } else {
                LogUtil.error(String.format("???????????????????????? %d, ?????????????????????!!!", stage));
            }
        } else if (str.equalsIgnoreCase("warFireEvent")) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            GlobalWarFire gwf = cMap != null ? cMap.getGlobalWarFire() : null;
            if (Objects.nonNull(gwf)) {
                gwf.gmClearWarfireEvents(player);
            }
        } else if (str.equalsIgnoreCase("warFireDate")) {
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(CrossWorldMapConstant.CROSS_MAP_ID);
            GlobalWarFire gwf = cMap != null ? cMap.getGlobalWarFire() : null;
            if (Objects.nonNull(gwf)) {
                gwf.setBeginDate(null);
                gwf.setEndDate(null);
                for (Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
                    entry.getValue().getMixtureData().remove(PlayerConstant.LEAVE_WAR_FIRE_TIME);
                }
            }
        } else if (str.equalsIgnoreCase("agents")) {
            player.getCia().getFemaleAngets().clear();
        } else if (str.equalsIgnoreCase("dressup")) {
//            player.getChatBubbles().clear();
//            player.getChatBubbles().add(0);
//            player.setCurChatBubble(0);
            if (count == AwardType.CHAT_BUBBLE) {
                player.getDressUp().getDressUpEntityMapByType(AwardType.CHAT_BUBBLE).clear();
            }
            if (count == AwardType.PORTRAIT_FRAME) {
                player.getDressUp().getDressUpEntityMapByType(AwardType.PORTRAIT_FRAME).clear();
            }
            if (count == AwardType.NAMEPLATE) {
                player.getDressUp().getDressUpEntityMapByType(AwardType.NAMEPLATE).clear();
            }
            if (count == AwardType.MARCH_SPECIAL_EFFECTS) {
                player.getDressUp().getDressUpEntityMapByType(AwardType.MARCH_SPECIAL_EFFECTS).clear();
            }
        } else if (str.equalsIgnoreCase("errorSuperMineGuard")) {
            LogUtil.common("????????????????????????????????????!!!");
            worldDataManager.cntCityCamp(0, 0);
        } else if (str.equalsIgnoreCase("activity")) {
            int activityType = count;
            ActivityBase activityBase;
            if (PersonalActService.isPersonalAct(activityType)) {
                activityBase = StaticActivityDataMgr.getPersonalActivityList().stream()
                        .filter(a -> a.getActivityType() == activityType)
                        .findFirst()
                        .orElse(null);
            } else {
                activityBase = StaticActivityDataMgr.getActivityByType(activityType);
            }

            if (activityBase == null) {
                LogUtil.error("??????????????????, ??????????????????; activityType = " + activityType);
                return;
            }

            Date beginTime = activityBase.getBeginTime();
            int begin = TimeHelper.getDay(beginTime);
            Activity activity = activityDataManager.conActivity(activityBase, activityType, begin, player);
            activity.setOpen(activityBase.getBaseOpen());
            player.activitys.put(activityType, activity);

            if (AbsRankActivityService.isActRankAct(activityType)) {
                GlobalActivityData gActDate = activityDataManager.getGlobalActivity(activityType);
                if (gActDate == null) {
                    return;
                }

                gActDate.getRanks().remove(activityType);
                AbsRankActivityService.loadActRankAct(activityBase, gActDate);
            }
        } else if ("clearPlayerCampChat".equalsIgnoreCase(str)) {
            LinkedList<CommonPb.Chat> chats = chatDataManager.getCampChat(player.lord.getCamp());
            if (CheckNull.nonEmpty(chats)) {
                Iterator<CommonPb.Chat> chatIterator = chats.iterator();
                while (chatIterator.hasNext()) {
                    CommonPb.Chat chat = chatIterator.next();
                    if (CheckNull.isNull(chat)) continue;
                    if (chat.getTime() == count) chatIterator.remove();
                }
            }
        }
    }

    /**
     * ??????10???????????????,??????10???????????????
     */
    private void doGmRmMentorEquip() {
        for (Player p : playerDataManager.getPlayers().values()) {
            // ????????????
            boolean needReCalcFight = false;
            MentorInfo mentorInfo = p.getMentorInfo();
            if (mentorInfo != null) {
                Map<Integer, MentorEquip> equipMap = mentorInfo.getEquipMap();
                for (Iterator<Entry<Integer, MentorEquip>> it = equipMap.entrySet().iterator(); it.hasNext(); ) {
                    Entry<Integer, MentorEquip> next = it.next();
                    MentorEquip meq = next.getValue();
                    if (meq.getLv() == 10) {
                        it.remove();
                        if (meq.getMentorId() != 0) {
                            // ?????????????????????,?????????????????????????????????
                            Mentor mentor = mentorInfo.getMentors().get(1);
                            if (mentor != null) {
                                int[] equips = mentor.getEquips();
                                for (int i = 0; i < equips.length; i++) {
                                    if (equips[i] == meq.getKeyId()) {
                                        equips[i] = 0;
                                        needReCalcFight = true;
                                        break;
                                    }
                                }
                            }
                        }
                        LogLordHelper.commonLog("gmRmMentorEquip", AwardFrom.DO_SOME, p, "rmMentorEquip",
                                meq.getEquipId(), meq.getMentorId());
                    }
                }
            }
            Map<Integer, Prop> props = p.props;
            for (Iterator<Entry<Integer, Prop>> it = props.entrySet().iterator(); it.hasNext(); ) {
                Entry<Integer, Prop> next = it.next();
                Prop prop = next.getValue();
                if (prop.getPropId() == 9010) {
                    it.remove();
                    LogLordHelper.commonLog("gmRmMentorEquip", AwardFrom.DO_SOME, p, "rmProp", prop.getPropId(),
                            prop.getCount());
                }
            }
            if (needReCalcFight) {// ?????????????????????
                CalculateUtil.reCalcBattleHeroAttr(p);
            }
        }
    }

    private void gmRemove(Player player, String str, int id) {

    }

    private void gmRemovePlayer(String name, String str, int id) {
        Player player = playerDataManager.getPlayer(name);
        if (player != null) {
            gmRemove(player, str, id);
        }
    }

    private void gmRemoveAllPlayer(String str, int id) {
        Iterator<Player> it = playerDataManager.getPlayers().values().iterator();
        while (it.hasNext()) {
            Player player = it.next();
            try {
                gmRemove(player, str, id);
            } catch (Exception e) {
                LogUtil.error(e + str + "|" + player.lord.getNick() + "|" + player.lord.getLordId());
            }
        }
    }

    private void gmClearPlayer(String name, String str) {
        Player player = playerDataManager.getPlayer(name);
        if (player != null) {
            gmClear(player, str, 0);
        }
    }

    private void gmClearAllPlayer(String str) {
        Iterator<Player> it = playerDataManager.getPlayers().values().iterator();
        while (it.hasNext()) {
            Player player = it.next();
            try {
                gmClear(player, str, 0);
            } catch (Exception e) {
                LogUtil.error(e + str + "|" + player.lord.getNick() + "|" + player.lord.getLordId());
            }
        }
    }

    /**
     * gm??????????????????
     *
     * @param req ????????????
     * @return ????????????
     */
    public HttpPb.BackModifyServerInfoRq gmModifyServerInfo(HttpPb.ModifyServerInfoRq req) {
        HttpPb.BackModifyServerInfoRq.Builder builder = HttpPb.BackModifyServerInfoRq.newBuilder();
        if (req.hasSign()) {
            // ???????????????sign???
            builder.setSign(req.getSign());
        }
        // ??????????????????
        if (req.hasOpenTime()) {
            int openTime = req.getOpenTime();
            // ??????????????????
            Date openServerDate = serverSetting.getOpenServerDate();
            // ?????????????????????
            // if (openTime <= TimeHelper.dateToSecond(openServerDate)) {
            //     // ???????????????????????????
            //     builder.setCode(GameError.MODIFY_SERVER_TIME_ERROR.getCode());
            //     builder.setMsg(GameError.MODIFY_SERVER_TIME_ERROR.getMsg());
            //     return builder.build();
            // }
            if (openTime <= TimeHelper.getCurrentSecond()) {
                // ???????????????????????????
                builder.setCode(GameError.MODIFY_SERVER_TIME_ERROR.getCode());
                builder.setMsg(GameError.MODIFY_SERVER_TIME_ERROR.getMsg());
                return builder.build();
            }
            // if (playerDataManager.getAllPlayer().values().stream().anyMatch(p -> p.account.getPlatNo() != 1)) {
            //     // ????????????????????????????????????????????????
            //     builder.setCode(GameError.MODIFY_SERVER_HAVE_REAL_PLAYER.getCode());
            //     builder.setMsg(GameError.MODIFY_SERVER_HAVE_REAL_PLAYER.getMsg());
            //     return builder.build();
            // }
            // ????????????
            String nowDate = DateHelper.formatDateTime(new Date(), DateHelper.format1);
            // ????????????????????????
            String beforeDate = DateHelper.formatDateTime(openServerDate, DateHelper.format1);
            // ????????????????????????
            String afterDate = DateHelper.formatDateTime(TimeHelper.secondToDate(openTime), DateHelper.format1);
            // ?????????
            String tableName = "p_global_" + nowDate;
            /*//???????????????
            globalDao.dropGlobalBack();
            // ??????p_global?????????
            int copyRow = globalDao.copyGlobal();
            LogUtil.gm("p_global???????????????, beforeDate", beforeDate, ", nowDate:", nowDate, ", table_name:", tableName, ", affect row:", copyRow);
            if (copyRow >= 1) {*/
            // ??????global??????
            int deleteRow = globalDao.clearGlobal();
            LogUtil.gm("p_global???????????????, beforeDate:", beforeDate, ", nowDate:", nowDate, ", table_name:", tableName, ", affect row:", deleteRow);
            serverSetting.setOpenTime(afterDate);
            LogUtil.gm("??????????????????????????????, beforeDate:", beforeDate, ", nowDate:", nowDate, ", afterDate:", afterDate);
            try {
                // ???????????????????????????
                loadService.loadAll();
                // ?????????global??????
                globalDataManager.init();
                LogUtil.start("???????????????global??????");
                // ??????????????????????????????
                worldDataManager.init(false);
                // ????????????????????????????????????, ????????????????????????????????????
                worldDataManager.refreshAllBandit(WorldConstant.REFRESH_TYPE_BANDIT_3);
                worldDataManager.gmClearAllMine();
                worldDataManager.refreshAllMine(WorldConstant.REFRESH_TYPE_MINE_2);
                airshipService.gmRefreshAirship();
                LogUtil.start("?????????????????????????????????");
                // ??????????????????
                chatDataManager.init();
                // LogUtil.start("?????????????????????????????????");
                // ?????????????????????
                worldScheduleService.init();
                // ???????????????map
                warDataManager.initBattle();
                // ?????????????????????
                ScheduleManager.getInstance().initSandTableContest();
                LogUtil.start("?????????????????????????????????");
            } catch (InvalidProtocolBufferException e) {
                LogUtil.error("???????????????: global??????");
            }
            /* }*/
        }
        // ??????????????????
        if (req.hasServerName()) {
            String serverName = req.getServerName();
            if (StringUtils.isNotBlank(serverName)) {
                serverSetting.setServerName(serverName);
            }
        }

        builder.setCode(GameError.OK.getCode());
        builder.setMsg(GameError.MODIFY_SERVER_TIME_SUCCESS.getMsg());
        return builder.build();
    }

    @GmCmd("logLevel")
    public void logLevel(Player player, String... params) {
        String logName = params[0].toUpperCase();
        String level = params[1];
        Level logLevel = Level.toLevel(level);
        LogUtil.setLevel(logName, logLevel);
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param player
     * @param params
     */
    @GmCmd("hotfixMain")
    public void hotfixMain(Player player, String... params) {
        String paramStr = String.join(" ", params);
        DataResource.logicServer.addCommandByMainType(() -> this.doSome(DoSomeRq.newBuilder().setStr(paramStr).build(), player.roleId));
    }
}
