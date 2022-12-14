package com.gryphpoem.game.zw.resource.domain;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWarPlaneDataMgr;
import com.gryphpoem.game.zw.gameplay.local.world.newyork.PlayerNewYorkWar;
import com.gryphpoem.game.zw.manager.DressUpDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.BuildingBase;
import com.gryphpoem.game.zw.pb.CommonPb.ChemicalQue;
import com.gryphpoem.game.zw.pb.CommonPb.CombatFB;
import com.gryphpoem.game.zw.pb.CommonPb.DbSpecialProp;
import com.gryphpoem.game.zw.pb.CommonPb.IntListInt;
import com.gryphpoem.game.zw.pb.CommonPb.OffLineBuild;
import com.gryphpoem.game.zw.pb.CommonPb.Report;
import com.gryphpoem.game.zw.pb.CommonPb.RobinHood;
import com.gryphpoem.game.zw.pb.CommonPb.RoleOpt;
import com.gryphpoem.game.zw.pb.CommonPb.SignInInfo;
import com.gryphpoem.game.zw.pb.CommonPb.StrInt;
import com.gryphpoem.game.zw.pb.CommonPb.TotemDataInfo;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.CommonPb.TypeAwards;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.pb.SerializePb.DbActivity;
import com.gryphpoem.game.zw.pb.SerializePb.DbAirshipPersonData;
import com.gryphpoem.game.zw.pb.SerializePb.DbAtkCityAct;
import com.gryphpoem.game.zw.pb.SerializePb.DbDay7Act;
import com.gryphpoem.game.zw.pb.SerializePb.DbDay7ActStatus;
import com.gryphpoem.game.zw.pb.SerializePb.DbTriggerGiftMap;
import com.gryphpoem.game.zw.pb.SerializePb.DbWarPlane;
import com.gryphpoem.game.zw.pb.SerializePb.SerAcquisition;
import com.gryphpoem.game.zw.pb.SerializePb.SerActivity;
import com.gryphpoem.game.zw.pb.SerializePb.SerArmy;
import com.gryphpoem.game.zw.pb.SerializePb.SerBuildQue;
import com.gryphpoem.game.zw.pb.SerializePb.SerCabinet;
import com.gryphpoem.game.zw.pb.SerializePb.SerChapterTask;
import com.gryphpoem.game.zw.pb.SerializePb.SerChemical;
import com.gryphpoem.game.zw.pb.SerializePb.SerCombat;
import com.gryphpoem.game.zw.pb.SerializePb.SerCombatFb;
import com.gryphpoem.game.zw.pb.SerializePb.SerCrossPersonalData;
import com.gryphpoem.game.zw.pb.SerializePb.SerData;
import com.gryphpoem.game.zw.pb.SerializePb.SerDrawCardData;
import com.gryphpoem.game.zw.pb.SerializePb.SerEffects;
import com.gryphpoem.game.zw.pb.SerializePb.SerEquip;
import com.gryphpoem.game.zw.pb.SerializePb.SerEquipQue;
import com.gryphpoem.game.zw.pb.SerializePb.SerFactory;
import com.gryphpoem.game.zw.pb.SerializePb.SerFriend;
import com.gryphpoem.game.zw.pb.SerializePb.SerGains;
import com.gryphpoem.game.zw.pb.SerializePb.SerHero;
import com.gryphpoem.game.zw.pb.SerializePb.SerMasterApprentice;
import com.gryphpoem.game.zw.pb.SerializePb.SerMedal;
import com.gryphpoem.game.zw.pb.SerializePb.SerMill;
import com.gryphpoem.game.zw.pb.SerializePb.SerPlayerExt;
import com.gryphpoem.game.zw.pb.SerializePb.SerProp;
import com.gryphpoem.game.zw.pb.SerializePb.SerRoleOpt;
import com.gryphpoem.game.zw.pb.SerializePb.SerShop;
import com.gryphpoem.game.zw.pb.SerializePb.SerSignInInfo;
import com.gryphpoem.game.zw.pb.SerializePb.SerSuperEquip;
import com.gryphpoem.game.zw.pb.SerializePb.SerSuperEquipQue;
import com.gryphpoem.game.zw.pb.SerializePb.SerTask;
import com.gryphpoem.game.zw.pb.SerializePb.SerTech;
import com.gryphpoem.game.zw.pb.SerializePb.SerTreasure;
import com.gryphpoem.game.zw.pb.SerializePb.SerTreasureWares;
import com.gryphpoem.game.zw.pb.SerializePb.SerTriggerGift;
import com.gryphpoem.game.zw.pb.SerializePb.SerTrophy;
import com.gryphpoem.game.zw.pb.SerializePb.SerTypeAwards;
import com.gryphpoem.game.zw.pb.SerializePb.SerTypeInfo;
import com.gryphpoem.game.zw.pb.SerializePb.SerWallNpc;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.EffectConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.MedalConst;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.constant.PushConstant;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.ActBarton;
import com.gryphpoem.game.zw.resource.domain.p.ActBlackhawk;
import com.gryphpoem.game.zw.resource.domain.p.ActRobinHood;
import com.gryphpoem.game.zw.resource.domain.p.ActTurnplat;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ArmQue;
import com.gryphpoem.game.zw.resource.domain.p.AtkCityAct;
import com.gryphpoem.game.zw.resource.domain.p.BuildQue;
import com.gryphpoem.game.zw.resource.domain.p.Building;
import com.gryphpoem.game.zw.resource.domain.p.BuildingExt;
import com.gryphpoem.game.zw.resource.domain.p.Cabinet;
import com.gryphpoem.game.zw.resource.domain.p.Chemical;
import com.gryphpoem.game.zw.resource.domain.p.Cia;
import com.gryphpoem.game.zw.resource.domain.p.Combat;
import com.gryphpoem.game.zw.resource.domain.p.CombatFb;
import com.gryphpoem.game.zw.resource.domain.p.CombatInfo;
import com.gryphpoem.game.zw.resource.domain.p.Common;
import com.gryphpoem.game.zw.resource.domain.p.CrossPersonalData;
import com.gryphpoem.game.zw.resource.domain.p.CrossPlayerLocalData;
import com.gryphpoem.game.zw.resource.domain.p.DataNew;
import com.gryphpoem.game.zw.resource.domain.p.Day7Act;
import com.gryphpoem.game.zw.resource.domain.p.DbFriend;
import com.gryphpoem.game.zw.resource.domain.p.DbMasterApprentice;
import com.gryphpoem.game.zw.resource.domain.p.DecisiveInfo;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.EquipQue;
import com.gryphpoem.game.zw.resource.domain.p.EquipTurnplat;
import com.gryphpoem.game.zw.resource.domain.p.Factory;
import com.gryphpoem.game.zw.resource.domain.p.Gains;
import com.gryphpoem.game.zw.resource.domain.p.History;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.MailData;
import com.gryphpoem.game.zw.resource.domain.p.MentorInfo;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.p.MultCombat;
import com.gryphpoem.game.zw.resource.domain.p.PersonalActs;
import com.gryphpoem.game.zw.resource.domain.p.PitchCombat;
import com.gryphpoem.game.zw.resource.domain.p.PlayerHero;
import com.gryphpoem.game.zw.resource.domain.p.PlayerOnHook;
import com.gryphpoem.game.zw.resource.domain.p.PlayerRebellion;
import com.gryphpoem.game.zw.resource.domain.p.Resource;
import com.gryphpoem.game.zw.resource.domain.p.Sectiontask;
import com.gryphpoem.game.zw.resource.domain.p.Shop;
import com.gryphpoem.game.zw.resource.domain.p.SiginInfo;
import com.gryphpoem.game.zw.resource.domain.p.StoneCombat;
import com.gryphpoem.game.zw.resource.domain.p.StoneInfo;
import com.gryphpoem.game.zw.resource.domain.p.Summon;
import com.gryphpoem.game.zw.resource.domain.p.Tech;
import com.gryphpoem.game.zw.resource.domain.p.TechLv;
import com.gryphpoem.game.zw.resource.domain.p.TechQue;
import com.gryphpoem.game.zw.resource.domain.p.Treasure;
import com.gryphpoem.game.zw.resource.domain.p.TriggerGift;
import com.gryphpoem.game.zw.resource.domain.p.WallNpc;
import com.gryphpoem.game.zw.resource.domain.s.StaticCastleSkin;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacter;
import com.gryphpoem.game.zw.resource.domain.s.StaticCharacterReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneUpgrade;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.EquipJewel;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.MailReportMap;
import com.gryphpoem.game.zw.resource.pojo.PlaneChip;
import com.gryphpoem.game.zw.resource.pojo.PlayerWorldWarData;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.Ring;
import com.gryphpoem.game.zw.resource.pojo.SmallGame;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.chapterTask.ChapterTask;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.CastleSkinEntity;
import com.gryphpoem.game.zw.resource.pojo.dressup.DressUp;
import com.gryphpoem.game.zw.resource.pojo.fish.FishingData;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.pojo.medal.RedMedal;
import com.gryphpoem.game.zw.resource.pojo.party.SupplyRecord;
import com.gryphpoem.game.zw.resource.pojo.plan.PlayerFunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.relic.PlayerRelic;
import com.gryphpoem.game.zw.resource.pojo.robot.RobotRecord;
import com.gryphpoem.game.zw.resource.pojo.rpc.RpcPlayer;
import com.gryphpoem.game.zw.resource.pojo.season.PlayerSeasonData;
import com.gryphpoem.game.zw.resource.pojo.simulator.CityEvent;
import com.gryphpoem.game.zw.resource.pojo.simulator.LifeSimulatorInfo;
import com.gryphpoem.game.zw.resource.pojo.tavern.DrawCardData;
import com.gryphpoem.game.zw.resource.pojo.totem.TotemData;
import com.gryphpoem.game.zw.resource.pojo.treasureware.MakeTreasureWare;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureChallengePlayer;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureCombat;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureWare;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipPersonData;
import com.gryphpoem.game.zw.resource.pojo.world.battlepass.BattlePassPersonInfo;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.PlayerSerHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.PlayerService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: ZhouJie
 * @Date: 2019-03-20 17:19
 * @Description: ????????????, ????????????????????????, ????????????{@link Player#equips}
 */
public class Player {

    /**
     * ??????????????????
     */
    public Lord lord;

    /**
     * ????????????
     */
    public Account account;

    /**
     * ??????????????????
     */
    public Resource resource;

    /**
     * ????????????????????????
     */
    public Common common;

    /**
     * ??????
     */
    public Building building;

    /**
     * ??????
     */
    public Tech tech;

    /**
     * ??????
     */
    public Shop shop;

    /**
     * ?????????
     */
    public Chemical chemical;

    /**
     * ?????????
     */
    public Treasure treasure;

    /**
     * ?????????
     */
    private Cia cia;

    /**
     * ????????????
     */
    public PlayerHero playerHero;

    /**
     * ????????????
     */
    private MentorInfo mentorInfo = new MentorInfo();

    private volatile int maxKey;

    /**
     * ????????????
     */
    private StoneInfo stoneInfo = new StoneInfo();

    /**
     * ??????????????????
     */
    public Map<Integer, Factory> factory = new ConcurrentHashMap<>();

    /**
     * ??????
     */
    public Map<Integer, Integer> trophy = new ConcurrentHashMap<>();

    /**
     * ????????????(combatId,Obj)
     */
    public HashMap<Integer, Combat> combats = new HashMap<>();
    /**
     * ????????????????????????
     */
    public CombatInfo combatInfo = new CombatInfo();
    /**
     * ????????????(combatId,Obj)
     */
    public HashMap<Integer, CombatFb> combatFb = new HashMap<>();
    /**
     * ???????????? key:combatId
     */
    public HashMap<Integer, StoneCombat> stoneCombats = new HashMap<>();
    /**
     * ????????????????????? <type,PitchCombat>
     */
    private Map<Integer, PitchCombat> pitchCombats = new HashMap<>();
    /**
     * ????????????
     */
    private MultCombat multCombat;

    /**
     * ??????????????????(??????1????????????????????????????????????)
     */
    public HashMap<Integer, List<History>> typeInfo = new HashMap<>();

    /**
     * ??????(??????,??????) ????????????
     */
    public HashMap<Integer, List<Award>> awards = new HashMap<>();
    /**
     * ??????????????????????????? true?????????
     */
    private boolean isAdvanceAward;
    /**
     * ????????????
     */
    public Map<Integer, BuildQue> buildQue = new ConcurrentHashMap<>();
    /**
     * ??????????????????????????????,key:??????Id
     */
    public Map<Integer, BuildingExt> buildingExts = new ConcurrentHashMap<>();
    /**
     * ????????????
     */
    public LinkedList<EquipQue> equipQue = new LinkedList<>();
    /**
     * ????????????
     */
    public Map<Integer, Mill> mills = new ConcurrentHashMap<>();
    /**
     * ?????????????????????????????????
     */
    public Map<Integer, Gains> gains = new ConcurrentHashMap<>();
//    /**
//     * ????????????,????????????
//     */
//    public Map<Integer, Task> majorTasks = new ConcurrentHashMap<>();
//    /**
//     * ???????????????????????????id,????????????????????????
//     */
//    public List<Integer> curMajorTaskIds = new ArrayList<>();

    /**
     * ????????????
     */
    private Map<Integer, Task> dailyTask = new HashMap<>();
    /**
     * ?????????????????????????????????id
     */
    private Set<Integer> dailyIsGet = new HashSet<>();
    /**
     * ????????????????????????
     */
    private int dailyTaskLivenss;

    // ????????????
    private Map<Integer, Task> advanceTask = new HashMap<>();

    /**
     * ????????????
     */
    public Map<Integer, Task> partyTask = new HashMap<>();
    /**
     * ????????????
     */
    public Map<Integer, Task> worldTasks = new HashMap<>();
    /**
     * ?????????????????????
     */
    public List<Sectiontask> sectiontask = new ArrayList<>();

    /**
     * ????????????
     */
    public Map<Integer, Hero> heros = new HashMap<>();
    /**
     * ????????????????????????????????????id???0???????????????1-4??????????????????id
     */
    public int[] heroBattle = new int[HeroConstant.HERO_BATTLE_LEN + 1];

    /**
     * ????????????, ?????????????????????id, 0???????????????1-4??????????????????id
     */
    public int[] heroDef = new int[HeroConstant.HERO_BATTLE_LEN + 1];
    /**
     * ????????????????????????????????????(???????????????????????????,??????????????????,????????????id) key:2???????????? key:3????????????
     */
    public Map<Integer, List<Integer>> heroBattlePos = new HashMap<>();
    /**
     * ????????????
     */
    public int[] heroWall = new int[HeroConstant.HERO_BATTLE_LEN + 1];
    /**
     * ????????????
     */
    public int[] heroAcq = new int[HeroConstant.HERO_BATTLE_LEN + 1];
    /**
     * ????????????
     */
    public int[] heroCommando = new int[Constant.ACQ_HERO_REQUIRE.size() + 1];
    /**
     * ??????
     */
    public Map<Integer, Prop> props = new HashMap<>();
    /**
     * ??????????????????
     */
    public Set<Integer> portraits = new HashSet<>();
    /**
     * ?????????????????????
     */
    private Set<Integer> chatBubbles = new HashSet<>();
    /**
     * ?????????????????????
     */
    private int curChatBubble;
    /**
     * ???????????????
     */
    private Set<Integer> bodyImages = new HashSet<>();
    /**
     * ?????????????????????
     */
    private int curBodyImage;
    /**
     * ??????
     */
    public Map<Integer, Equip> equips = new HashMap<>();
    /**
     * ??????
     */
    public Map<Integer, TreasureWare> treasureWares = new HashMap<>();
    /**
     * ???????????????id????????????
     */
    public Map<Integer, Integer> treasureWareIdMakeCount = new HashMap<>();
    /**
     * ????????????, key: keyId
     */
    public Map<Integer, EquipJewel> equipJewel = new HashMap<>();

    /**
     * ??????, key: planeType
     */
    public Map<Integer, WarPlane> warPlanes = new HashMap<>();
    /**
     * ????????????, key: chipId
     */
    public Map<Integer, PlaneChip> palneChips = new HashMap<>();

    /**
     * ??????
     */
    public Map<Integer, Medal> medals = new HashMap<>();

    /**
     * ????????????(??????)
     */
    public Map<Integer, SuperEquip> supEquips = new HashMap<>();

    /**
     * ??????????????????(??????)
     */
    public LinkedList<TwoInt> supEquipQue = new LinkedList<>();

    /**
     * ????????????
     */
    public Map<Integer, Army> armys = new HashMap<>();

    /**
     * ??????
     */
    public Map<Integer, Mail> mails = new ConcurrentHashMap<>();

    /**
     * ???????????? (moldId, Queue<??????>)
     */
    public Map<Integer, MailReportMap> mailReport = new ConcurrentHashMap<>();

    /**
     * ????????????
     */
    public LinkedList<RoleOpt> opts = new LinkedList<>();

    /**
     * ??????NPC,key:posId
     */
    public Map<Integer, WallNpc> wallNpc = new ConcurrentHashMap<>();

    /**
     * ??????
     */
    private Map<Integer, Effect> effects = new ConcurrentHashMap<>();

    /**
     * ???????????????????????????????????????, key:pos, value:battleId, ?????????????????????????????????????????????????????????????????????
     */
    public Map<Integer, HashSet<Integer>> battleMap = new HashMap<>();

    /**
     * ?????????????????????????????????
     */
    public int acquisiteDate;

    /**
     * ????????????????????????????????????
     */
    public Map<Integer, Integer> acquisiteReward = new HashMap<>();

    /**
     * ???????????????????????????
     */
    public List<TwoInt> acquisiteQue = new LinkedList<>();

    /**
     * ???????????? key:activityType
     */
    public Map<Integer, Activity> activitys = new HashMap<>();

    /**
     * ????????????,key:????????? lordId
     */
    public Map<Long, DbFriend> friends = new HashMap<>();

    /**
     * ???????????? key:????????? lordId
     */
    public Map<Long, DbMasterApprentice> apprentices = new HashMap<>();

    /**
     * ????????? key:cardType
     */
    public Map<Integer, FunCard> funCards = new HashMap<>();

    /**
     * ??????, null??????????????????
     */
    public DbMasterApprentice master;

    /**
     * ???????????????
     */
    private List<Long> blacklist;

    /**
     * ??????????????????id(????????????)
     */
    public Map<Integer, Integer> awardedIds = new HashMap<>();

    /**
     * ?????????????????????id(????????????)
     */
    public Set<Integer> boughtIds = new HashSet<>();

    /**
     * 7?????????
     */
    public Day7Act day7Act = new Day7Act();

    /**
     * ??????????????????
     */
    public AtkCityAct atkCityAct = new AtkCityAct();

    /**
     * ??????????????????
     */
    public ActBlackhawk blackhawkAct = new ActBlackhawk();

    /**
     * ?????????????????? [key:activityId]
     */
    public Map<Integer, ActBarton> actBarton = new HashMap<>();

    /**
     * ???????????????
     */
    public Map<Integer, ActRobinHood> actRobinHood = new HashMap<>();

    /**
     * ????????????(???????????????) key:type ,val:specialId
     */
    public Map<Integer, Set<Integer>> specialProp = new HashMap<>();

    /**
     * ??????????????????
     */
    public Cabinet cabinet;

    /**
     * ???????????????
     */
    public Summon summon;

    /**
     * ??????????????????????????????value??????boolean?????????????????????????????????????????????
     */
    private Map<String, Integer> pushRecords = new HashMap<>();

    /**
     * ?????????????????????
     */
    public List<Integer> combatHeroForm = new ArrayList<>();

    /**
     * ???????????? ?????????????????????
     */
    public int enterWorldCnt;

    /**
     * ??????????????????????????????
     */
    public Set<String> firstPayDouble = new CopyOnWriteArraySet<>();

    /**
     * ????????????????????????
     */
    private int banditCnt;

    /**
     * ?????????????????????,true??????????????????
     */
    private boolean isFireState;

    /**
     * ??????????????????
     */
    private int nightRaidBanditCnt;

    /**
     * ???????????????
     */
    private int hitFlyCount;

    /**
     * ?????????????????????
     */
    private boolean isOffOnlineHitFly;

    /**
     * ?????????????????????,key:triggerId
     */
    public Map<Integer, Map<Integer, TriggerGift>> triggerGifts = new HashMap<>();

    /**
     * ???????????????id???
     */
    private String packId;

    /**
     * ???????????????
     */
    private int paySumAmoumt = 0;

    /**
     * ??????????????? ????????????<br>
     * key?????????{@link PlayerConstant}
     */
    private Map<Integer, Integer> mixtureData = new HashMap<>();

    /**
     * ??????????????????
     */
    public LinkedList<String> lastChats = new LinkedList<>();

    /**
     * ???????????????????????????
     */
    private int militaryExpenditure = 0;

    /** ????????????????????? */
    /** private int honorReportTips = 0; */

    /**
     * ???????????? ??????????????????
     */
    public int[] cheatCode;

    /**
     * ????????????????????? key ??????id
     */
    public Map<Integer, OffLineBuild> offLineBuilds = new HashMap<>();

    /**
     * ????????????-??????
     */
    public List<Integer> medalGoods;

    /**
     * ??????????????????????????????
     */
    private int medalGoodsRefNum;

    /**
     * ?????????????????? ??????????????????
     */
    private int peacekeepingForcesNum;

    /**
     * ???????????? ???????????? ?????????????????????
     */
    private int jGXHLastTime;

    /**
     * ???????????????????????????
     */
    private PlayerRebellion playerRebellion;

    /**
     * ?????????????????????????????????
     */
    private int collectMineCount;

    /**
     * ??????????????????
     */
    private DecisiveInfo decisiveInfo = new DecisiveInfo();
    /**
     * ????????????
     */
    public SiginInfo siginInfo = new SiginInfo();
    /**
     * ????????????
     */
    public Map<Integer, SiginInfo> signInfoMap = new HashMap<>();
    /**
     * ?????????????????????
     */
    private AirshipPersonData airshipPersonData;
    /**
     * ????????????
     */
    private SupplyRecord supplyRecord = new SupplyRecord();

    /**
     * ????????????
     */
    private Set<Integer> ownCastleSkin = new HashSet<>();

    /**
     * ???????????????
     */
    private int curCastleSkin = StaticCastleSkin.DEFAULT_SKIN_ID;

    /**
     * ???????????????????????????
     */
    private Map<Integer, Integer> ownCastleSkinTime = new HashMap<>();

    /**
     * ??????????????????
     */
    private Map<Integer, Integer> ownCastleSkinStar = new HashMap<>();

    /**
     * ?????????????????????
     */
    private CrossPersonalData crossPersonalData;

    /**
     * ???????????? ????????????
     */
    private PlayerWorldWarData playerWorldWarData = new PlayerWorldWarData();

    /**
     * ????????????????????????
     */
    public CrossPlayerLocalData crossPlayerLocalData = new CrossPlayerLocalData();

    /**
     * ??????????????????
     */
    public PlayerNewYorkWar newYorkWar = new PlayerNewYorkWar();

    /**
     * ????????????????????????npc??????
     */
    private List<Integer> npcCityFirstKillReward = new ArrayList<>();

    /**
     * ???????????????????????????
     */
    private BattlePassPersonInfo battlePassPersonInfo = new BattlePassPersonInfo();

    /**
     * ??????????????????
     */
    private int sandTableScore;

    private Map<Integer, Integer> sandTableBought = new HashMap<>();

    //??????????????????
    private PlayerOnHook playerOnHook = new PlayerOnHook();
    /**
     * ??????
     */
    private DressUp dressUp = new DressUp();
    /**
     * ??????????????????
     */
    private PlayerSeasonData playerSeasonData = new PlayerSeasonData();
    /**
     * ????????????
     */
    private FishingData fishingData = new FishingData();
    /**
     * ????????????
     */
    private TotemData totemData = new TotemData();

    /**
     * ?????????
     */
    private SmallGame smallGame;

    private RpcPlayer rpcPlayer;

    /**
     * ??????????????????
     */
    private Map<Integer, Integer> marchType = new HashMap<>();

    /**
     * ????????????
     */
    private TreasureCombat treasureCombat = new TreasureCombat();
    /**
     * ??????????????????
     */
    private TreasureChallengePlayer treasureChallengePlayer = new TreasureChallengePlayer();

    /**
     * ???????????? v1: ??????s_system???id=1102???????????????, v2: 1 ????????????0 ?????????
     */
    private Map<Integer, Integer> recruitReward = new HashMap<>(5);

    /**
     * ??????????????????
     */
    private DrawCardData drawCardData = new DrawCardData();

    /**
     * ??????
     */
    private PlayerRelic playerRelic = new PlayerRelic();
    /**
     * ????????????
     */
    public ChapterTask chapterTask = new ChapterTask();


    /**
     * ????????????????????????
     */
    private PlayerFunctionPlanData functionPlanData = new PlayerFunctionPlanData();

    public PlayerFunctionPlanData getFunctionPlanData() {
        return functionPlanData;
    }


    private PersonalActs personalActs = new PersonalActs();

    /**
     * ?????????????????????, ?????????????????????????????????????????????<br>
     * <????????????, ???????????????>
     */
    private Map<Integer, List<LifeSimulatorInfo>> lifeSimulatorRecordMap = new HashMap<>();

    public Map<Integer,  List<LifeSimulatorInfo>> getLifeSimulatorRecordMap() {
        return lifeSimulatorRecordMap;
    }

    public void setLifeSimulatorRecordMap(Map<Integer, List<LifeSimulatorInfo>> lifeSimulatorRecordMap) {
        this.lifeSimulatorRecordMap = lifeSimulatorRecordMap;
    }

    /**
     * ?????????????????????
     */
    private CityEvent cityEvent = new CityEvent();

    public CityEvent getCityEvent() {
        return cityEvent;
    }

    public void setCityEvent(CityEvent cityEvent) {
        this.cityEvent = cityEvent;
    }

    /**
     * ?????????1-??????; 2-??????; 3-??????; 4-??????; 5-??????; 6-?????? <br>
     * <??????id, ?????????>
     */
    private Map<Integer, Integer> characterData = new HashMap<>(6);

    public Map<Integer, Integer> getCharacterData() {
        return characterData;
    }

    public void setCharacterData(Map<Integer, Integer> characterData) {
        this.characterData = characterData;
    }

    /**
     * ?????????????????? <br>
     * <????????????id(??????sim_character_reward???id), ???????????????(1-???; 0-???)>
     */
    private Map<Integer, Integer> characterRewardRecord = new HashMap<>(8);

    public Map<Integer, Integer> getCharacterRewardRecord() {
        return characterRewardRecord;
    }

    public void setCharacterRewardRecord(Map<Integer, Integer> characterRewardRecord) {
        this.characterRewardRecord = characterRewardRecord;
    }

    /**
     * ???????????????????????????
     */
    private MakeTreasureWare makeTreasureWare = new MakeTreasureWare();

    public MakeTreasureWare getMakeTreasureWare() {
        return makeTreasureWare;
    }

    public Map<Integer, Integer> getRecruitReward() {
        return recruitReward;
    }

    public TreasureCombat getTreasureCombat() {
        return treasureCombat;
    }

    public void setTreasureCombat(TreasureCombat treasureCombat) {
        this.treasureCombat = treasureCombat;
    }

    public Map<Integer, Integer> getMarchType() {
        return marchType;
    }

    public void setMarchType(Map<Integer, Integer> marchType) {
        this.marchType = marchType;
    }

    public SmallGame getSmallGame() {
        return smallGame;
    }

    public void setSmallGame(SmallGame smallGame) {
        this.smallGame = smallGame;
    }

    public FishingData getFishingData() {
        return fishingData;
    }

    public DressUp getDressUp() {
        return dressUp;
    }

    public BattlePassPersonInfo getBattlePassPersonInfo() {
        return battlePassPersonInfo;
    }

    public void setBattlePassPersonInfo(BattlePassPersonInfo battlePassPersonInfo) {
        this.battlePassPersonInfo = battlePassPersonInfo;
    }

    public PlayerWorldWarData getPlayerWorldWarData() {
        return playerWorldWarData;
    }

    public void setPlayerWorldWarData(PlayerWorldWarData playerWorldWarData) {
        this.playerWorldWarData = playerWorldWarData;
    }

    public SupplyRecord getSupplyRecord() {
        return supplyRecord;
    }

    public DecisiveInfo getDecisiveInfo() {
        return decisiveInfo;
    }

    public void addMilitaryExpenditure(int add) {
        militaryExpenditure += add;
    }

    public int getMilitaryExpenditure() {
        return militaryExpenditure;
    }

    public void setMilitaryExpenditure(int militaryExpenditure) {
        this.militaryExpenditure = militaryExpenditure;
    }

    public int getHitFlyCount() {
        return hitFlyCount;
    }

    public void setHitFlyCount(int hitFlyCount) {
        this.hitFlyCount = hitFlyCount;
    }

    public boolean isOffOnlineHitFly() {
        return isOffOnlineHitFly;
    }

    public void setOffOnlineHitFly(boolean offOnlineHitFly) {
        isOffOnlineHitFly = offOnlineHitFly;
    }

    public boolean isFirstHitFly() {
        return hitFlyCount == 0;
    }

    public synchronized int maxKey() {
        return ++maxKey;
    }

    public int getMaxKey() {
        return maxKey;
    }

    public void setMaxKey(int maxKey) {
        this.maxKey = maxKey;
    }

    public int getBanditCnt() {
        return banditCnt;
    }

    public void setBanditCnt(int banditCnt) {
        this.banditCnt = banditCnt;
    }

    public boolean isFireState() {
        return isFireState;
    }

    public void setFireState(boolean isFireState) {
        this.isFireState = isFireState;
    }

    public int getNightRaidBanditCnt() {
        return nightRaidBanditCnt;
    }

    public void setNightRaidBanditCnt(int nightRaidBanditCnt) {
        this.nightRaidBanditCnt = nightRaidBanditCnt;
    }

    public boolean isAdvanceAward() {
        return isAdvanceAward;
    }

    public void setAdvanceAward(boolean isAdvanceAward) {
        this.isAdvanceAward = isAdvanceAward;
    }

    public String getPackId() {
        return packId;
    }

    public void setPackId(String packId) {
        this.packId = packId;
    }

    public StoneInfo getStoneInfo() {
        return stoneInfo;
    }

    public MentorInfo getMentorInfo() {
        return mentorInfo;
    }

    public int getPaySumAmoumt() {
        return paySumAmoumt;
    }

    public void setPaySumAmoumt(int paySumAmoumt) {
        this.paySumAmoumt = paySumAmoumt;
    }

    public void addPaySumAmount(int amount) {
        this.paySumAmoumt += paySumAmoumt;
    }

    public Map<Integer, Task> getDailyTask() {
        return dailyTask;
    }

    public void setDailyTask(Map<Integer, Task> dailyTask) {
        this.dailyTask = dailyTask;
    }

    public Set<Integer> getDailyIsGet() {
        return dailyIsGet;
    }

    public void setDailyIsGet(Set<Integer> dailyIsGet) {
        this.dailyIsGet = dailyIsGet;
    }

    public int getDailyTaskLivenss() {
        return dailyTaskLivenss;
    }

    public void setDailyTaskLivenss(int dailyTaskLivenss) {
        this.dailyTaskLivenss = dailyTaskLivenss;
    }

    public Cia getCia() {
        return cia;
    }

    public void setCia(Cia cia) {
        this.cia = cia;
    }

    public Map<Integer, Integer> getMixtureData() {
        return mixtureData;
    }

    public int getMixtureDataById(int playerConstantKey) {
        Integer v = mixtureData.get(playerConstantKey);
        return v == null ? 0 : v;
    }

    public void setMixtureData(int playerConstantKey, int val) {
        mixtureData.put(playerConstantKey, val);
    }

    public void addMixtureData(int playerConstantKey, int toAdd) {
        mixtureData.put(playerConstantKey, getMixtureDataById(playerConstantKey) + toAdd);
    }

    public void cleanMixtureData(int playerConstantKey) {
        mixtureData.remove(playerConstantKey);
    }

    public void cleanMixtureData(List<Integer> playerConstantKeys) {
        for (int key : playerConstantKeys) {
            mixtureData.remove(key);
        }
    }

    public List<Long> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(List<Long> blacklist) {
        this.blacklist = blacklist;
    }

    public int getCurChatBubble() {
        return curChatBubble;
    }

    public void setCurChatBubble(int curChatBubble) {
        this.curChatBubble = curChatBubble;
    }

    public Set<Integer> getChatBubbles() {
        return chatBubbles;
    }

    public int getCurBodyImage() {
        return curBodyImage;
    }

    public void setCurBodyImage(int curBodyImage) {
        this.curBodyImage = curBodyImage;
    }

    public Set<Integer> getBodyImages() {
        return bodyImages;
    }

    public List<Integer> getMedalGoods() {
        return medalGoods;
    }

    public void setMedalGoods(List<Integer> medalGoods) {
        this.medalGoods = medalGoods;
    }

    public int getMedalGoodsRefNum() {
        return medalGoodsRefNum;
    }

    public void setMedalGoodsRefNum(int medalGoodsRefNum) {
        this.medalGoodsRefNum = medalGoodsRefNum;
    }

    public int getPeacekeepingForcesNum() {
        return peacekeepingForcesNum;
    }

    public void setPeacekeepingForcesNum(int peacekeepingForcesNum) {
        this.peacekeepingForcesNum = peacekeepingForcesNum;
    }

    public int getjGXHLastTime() {
        return jGXHLastTime;
    }

    public void setjGXHLastTime(int jGXHLastTime) {
        this.jGXHLastTime = jGXHLastTime;
    }

    public PlayerRebellion getPlayerRebellion() {
        return playerRebellion;
    }

    public Map<Integer, Task> getAdvanceTask() {
        return advanceTask;
    }

    public void setAdvanceTask(Map<Integer, Task> advanceTask) {
        this.advanceTask = advanceTask;
    }

    public PlayerRebellion getAndCreateRebellion() {
        if (this.playerRebellion == null) {
            this.playerRebellion = new PlayerRebellion();
        }
        return playerRebellion;
    }

    public PitchCombat getPitchCombat(int type) {
        return this.pitchCombats.get(type);
    }

    public PitchCombat getOrCreatePitchCombat(int type) {
        PitchCombat pitchCombat = this.pitchCombats.get(type);
        if (pitchCombat == null) {
            pitchCombat = new PitchCombat(type);
            pitchCombat.firstCreateInit();
            this.pitchCombats.put(type, pitchCombat);
        }
        return pitchCombat;
    }

    public MultCombat getAndCreateMultCombat() {
        if (this.multCombat == null) {
            this.multCombat = new MultCombat();
            this.multCombat.firstCreateInit();
        }
        return this.multCombat;
    }

    public MultCombat getMultCombat() {
        return this.multCombat;
    }

    public AirshipPersonData getAndCreateAirshipPersonData() {
        if (this.airshipPersonData == null) {
            this.airshipPersonData = new AirshipPersonData();
        }
        return this.airshipPersonData;
    }

    public int getCollectMineCount() {
        return collectMineCount;
    }

    public void setCollectMineCount(int mineCount) {
        this.collectMineCount = mineCount;
    }

    public void addCollectMineCount() {
        this.collectMineCount++;
    }

    public DrawCardData getDrawCardData() {
        return drawCardData;
    }

    /**
     * ??????roleId???????????????????????????
     *
     * @param blacklistRoleId
     * @return true ???????????????
     */
    public boolean isInBlacklist(long blacklistRoleId) {
        return this.blacklist != null && this.blacklist.contains(blacklistRoleId);
    }

    @Deprecated
    public Set<Integer> getOwnCastleSkin() {
        return ownCastleSkin;
    }

    public int getCurCastleSkin() {
        return curCastleSkin;
    }

    public void setCurCastleSkin(int curCastleSkin) {
        this.curCastleSkin = curCastleSkin;
    }

    @Deprecated
    public Map<Integer, Integer> getOwnCastleSkinTime() {
        return ownCastleSkinTime;
    }

    public CrossPersonalData getAndCreateCrossPersonalData() {
        if (this.crossPersonalData == null) {
            this.crossPersonalData = new CrossPersonalData();
        }
        return this.crossPersonalData;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // ??????id????????????????????????
    public Long roleId;

    public int surface;

    public int lastSaveTime;

    public boolean isLogin = false;

    public boolean immediateSave = false;

    public ChannelHandlerContext ctx;

    public SaveFlag saveFlag = new SaveFlag();

    public int chatTime;// ???????????????????????????????????????

    public int pChatTime;// ??????????????????????????????????????????

    public int pCampMailTime;// ?????????????????????????????????????????????

    public int pRelicChatTime;// ?????????????????????????????????????????????

    public boolean isTester = false;

    public int appointFreeTime;// ???????????????????????????????????????

    public boolean isRobot;// ????????????????????????

    public RobotRecord robotRecord;// ???????????????????????????????????????

    public int heroSkin;// ?????????????????????

    public int loginRewardTime;// ????????????????????????????????????

    public Player(Lord lord, int nowTime) {
        this.roleId = lord.getLordId();
        this.lord = lord;

        lastSaveTime = nowTime + 180 + (int) (roleId % 300);
    }

    public void setLogin(boolean isLogin) {
        this.isLogin = isLogin;
    }

    public void logOut() {
        isLogin = false;
        ctx = null;
        immediateSave = true;

        int now = TimeHelper.getCurrentSecond();
        lord.setOffTime(now);
        lord.setOlTime(onLineTime());
        // ????????????
        EventBus.getDefault().post(new Events.RmMapFocusEvent(this));
        EventBus.getDefault().post(new Events.PlayerLoginLogoutEvent(this, false)); // ????????????

        LogLordHelper.logLogin(this);
        LogLordHelper.loginLong(this);// ????????????
        LogLordHelper.commonLog("fightingChange", AwardFrom.COMMON, this, lord.getFight()); // ???????????????
    }

    public void tickOut() {
        if (isLogin || isRobot) {
            int now = TimeHelper.getCurrentSecond();
            lord.setOffTime(now);
            lord.setOlTime(onLineTime());
        }

        isLogin = false;
        ctx = null;
        immediateSave = true;
        // ????????????
        // EventBus.getDefault().post(new Events.RmMapFocusEvent(this));
    }

    public void logIn() {
        int now = TimeHelper.getCurrentSecond();
        int nowDay = TimeHelper.getCurrentDay();

        int lastDay = TimeHelper.getDay(lord.getOnTime());
        if (nowDay != lastDay) {
            // ????????????????????????
            int monthAndDay = TimeHelper.getMonthAndDay(new Date());
            if ((lord.getOlMonth() / 10000) != monthAndDay / 10000) {// ???????????????
                lord.setOlMonth(monthAndDay + 1);
            } else {// ????????????
                if (lord.getOlMonth() / 100 != monthAndDay / 100) {
                    lord.setOlMonth(monthAndDay + lord.getOlMonth() % 100 + 1);
                }
            }

            int ctTime = lord.getCtTime();
            if (TimeHelper.getDay(ctTime) != TimeHelper.getDay(now)) {
                lord.setCtTime(now);
                lord.setOlAward(0);
            }

            // ??????????????????????????????
            int offTime = TimeHelper.getDay(lord.getOffTime());
            if (offTime != nowDay) {
                lord.setOlTime(0);
                // lord.setOffTime(now);
            }
        }
        putPushRecord(PushConstant.OFF_LINE_ONE_DAY, PushConstant.PUSH_NOT_PUSHED);
        lord.setOnTime(now);
    }

    /**
     * ????????????
     *
     * @return
     */
    public int onLineTime() {
        int now = TimeHelper.getCurrentSecond();
        int nowDay = TimeHelper.getCurrentDay();

        int lastDay = TimeHelper.getDay(lord.getOnTime());
        if (nowDay != lastDay) {// ????????????????????????,??????0??????????????????
            int noTime = TimeHelper.getTodayZone(now);
            int ctDay = TimeHelper.getDay(lord.getCtTime());
            if (ctDay != nowDay) {
                lord.setCtTime(noTime);
                lord.setOlAward(0);
            }
            return now - noTime;
        } else {// ?????????????????????,??????????????????
            int onlineTime = lord.getOlTime() + now - lord.getOnTime();
            onlineTime = onlineTime > 86400 ? 86400 : onlineTime;
            return onlineTime;
        }
    }

    public boolean isActive() {
        if (account == null) {
            // ??????lord????????????account??????????????????smallid???????????????????????????????????????????????????lord?????????????????????????????????
            // ???????????????smallId?????????
            return false;
        }

        // return account.getCreated() == 1 && lord.getLevel() > 2;
        return account.getCreated() == 1 && lord.getLevel() > 0;
    }

    /**
     * ????????????id??????, ????????????
     *
     * @param chipId
     * @return
     */
    public PlaneChip getPlaneChip(int chipId) {
        PlaneChip planeChip = palneChips.get(chipId);
        if (CheckNull.isNull(planeChip)) {
            planeChip = new PlaneChip();
            planeChip.setChipId(chipId);
            planeChip.setCnt(0);
            palneChips.put(chipId, planeChip);
        }
        return planeChip;
    }

    /**
     * ????????????????????????
     *
     * @param planeId
     * @return
     * @throws MwException
     */
    public WarPlane checkWarPlaneIsExist(int planeId) throws MwException {
        StaticPlaneUpgrade planeUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
        if (CheckNull.isNull(planeUpgrade)) {
            throw new MwException(GameError.PLANE_CONFIG_NOT_FOUND.getCode(), "?????????????????????, ???????????????????????????, planeId:", planeId);
        }
        return warPlanes.get(planeUpgrade.getPlaneType());
    }

    /**
     * ?????????????????????pos??????????????????????????????
     *
     * @param pos
     * @return
     */
    public Hero getBattleHeroByPos(int pos) {
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            return null;
        }

        int heroId = heroBattle[pos];
        if (heroId > 0) {
            return heros.get(heroId);
        }
        return null;
    }

    /**
     * ?????????????????????pos??????????????????????????????
     *
     * @param heroId
     * @return
     */
    public Hero getDefendHeroByPos(int heroId) {
        for (int heroDefId : heroDef) {
            if (heroDefId == heroId) {
                return heros.get(heroId);
            }
        }
        return null;
    }

    /**
     * ????????????????????????
     *
     * @param pos
     * @return
     */
    public Hero getWallHeroByPos(int pos) {
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            return null;
        }

        int heroId = heroWall[pos];
        if (heroId > 0) {
            return heros.get(heroId);
        }
        return null;
    }

    /**
     * ??????????????????????????????
     *
     * @param pos
     * @return
     */
    public Hero getAcqHeroByPos(int pos) {
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            return null;
        }

        int heroId = heroAcq[pos];
        if (heroId > 0) {
            return heros.get(heroId);
        }
        return null;
    }

    /**
     * ????????????????????????
     *
     * @param pos
     * @return
     */
    public Hero getCommandoHeroByPos(int pos) {
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_2) {
            return null;
        }

        int heroId = heroCommando[pos];
        if (heroId > 0) {
            return heros.get(heroId);
        }
        return null;
    }

    /**
     * ???????????????????????????
     *
     * @param heroId
     * @return
     */
    public boolean isOnWallHero(int heroId) {
        for (int pos = 0; pos < heroWall.length; pos++) {
            if (heroWall[pos] == heroId) {
                return true;
            }
        }
        return false;
    }

    /**
     * ?????????????????????
     *
     * @param heroId
     * @return
     */
    public boolean isOnBattleHero(int heroId) {
        for (int pos = 0; pos < heroBattle.length; pos++) {
            if (heroBattle[pos] == heroId) {
                return true;
            }
        }
        return false;
    }

    /**
     * ?????????????????????????????????
     *
     * @param heroId
     * @return
     */
    public boolean isOnAcqHero(int heroId) {
        for (int pos = 0; pos < heroAcq.length; pos++) {
            if (heroAcq[pos] == heroId) {
                return true;
            }
        }
        return false;
    }

    /**
     * ???????????????????????????
     *
     * @param heroId
     * @return
     */
    public boolean isOnCommandoHero(int heroId) {
        for (int pos = 0; pos < heroCommando.length; pos++) {
            if (heroCommando[pos] == heroId) {
                return true;
            }
        }
        return false;
    }

    /**
     * ??????????????????????????????
     *
     * @return true??????
     */
    private boolean defHeroIsEmpty() {
        for (int heroId : heroDef) {
            if (heroId > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * ??????????????????????????????????????? ???????????????????????????????????????????????????????????????NPC?????????????????????
     *
     * @return
     */
    public List<Hero> getDefendHeros() {
        List<Hero> heroList = new ArrayList<>();
        Hero hero;
        // ????????????????????????????????? ????????????

        // ?????????
        int[] myHerDef = heroDef;
        // ????????????????????????
        if (defHeroIsEmpty()) { // ???????????????????????????????????????????????????
            myHerDef = heroBattle;
        }

        int[] heroIds = new int[myHerDef.length + heroCommando.length];
        System.arraycopy(myHerDef, 0, heroIds, 0, myHerDef.length);
        System.arraycopy(heroCommando, 0, heroIds, myHerDef.length, heroCommando.length);

        for (int heroId : heroIds) {
            if (heroId > 0) {
                hero = heros.get(heroId);
                if (hero == null) {
                    continue;
                }
                if (hero.isIdle() && hero.getCount() > 0) {
                    heroList.add(hero);
                }
            }
        }
        // ?????????
        for (int heroId : heroWall) {
            if (heroId > 0) {
                hero = heros.get(heroId);
                if (hero == null) {
                    continue;
                }
                if (hero.isIdle() && hero.getCount() > 0 && hero.getCount() == hero.getAttr()[HeroConstant.ATTR_LEAD]) {
                    heroList.add(hero);
                    LogUtil.debug(roleId + ",????????????=" + heroId);
                }
            }
        }
        return heroList;
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    public Map<Integer, Integer> getBattleHeroShowFightInfo() {
        Map<Integer, Integer> fightInfo = new HashMap<>();
        List<Hero> battleHeros = getAllOnBattleHeros();
        if (!CheckNull.isEmpty(battleHeros)) {
            // ???????????????????????????????????????
            Arrays.stream(heroCommando).boxed().forEach(heroId -> {
                Hero hero = heros.get(heroId);
                if (hero != null) {
                    battleHeros.add(hero);
                }
            });
            for (Hero hero : battleHeros) {
                Map<Integer, Integer> showFight = hero.getShowFight();
                if (!CheckNull.isEmpty(showFight)) {
                    for (Entry<Integer, Integer> entry : showFight.entrySet()) {
                        Integer key = entry.getKey();
                        key = CheckNull.isNull(key) ? 0 : key;
                        Integer val = entry.getValue();
                        val = CheckNull.isNull(val) ? 0 : val;
                        if (key > 0) {
                            int fight = fightInfo.getOrDefault(key, 0);
                            fightInfo.put(key, fight + val);
                        }
                    }
                }
            }
        }
        return fightInfo;
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public List<Hero> getAllOnBattleHeros() {
        List<Hero> heroList = new ArrayList<>();
        for (int heroId : heroBattle) {
            if (heroId > 0 && heros.get(heroId) != null) {
                heroList.add(heros.get(heroId));
            }
        }
        return heroList;
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    public boolean isOnBattle() {
        for (int heroId : heroBattle) {
            if (heroId > 0 && heros.get(heroId) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public int getLineAdd() {
        return 0;
    }

    /**
     * ??????????????????
     *
     * @param techId
     * @return
     */
    public int getTechLvById(int techId) {
        return tech == null ? 0 : tech.getTechLvById(techId);
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public boolean washCountFull() {
        return false;
//        return common.washCountFull();
    }

    /**
     * ????????????????????????????????????
     *
     * @return
     */
    public TechQue getCanSpeedTechQue() {
        if (tech == null) {// ??????????????????
            return null;
        }
        TechQue que = tech.getQue();
        if (que == null) return null;
        if (que.haveFreeSpeed() || que.getId() <= 0) return null;
        return que;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @return
     */
    public BuildQue getCanAddSpeedBuildQue() {
        // int endTime = 0;
        // BuildQue queue = null;
        // for (BuildQue que : buildQue.values()) {
        // if (!que.haveFreeSpeed()) {
        // if (que.getEndTime() > endTime) {
        // endTime = que.getEndTime();
        // queue = que;
        // }
        // }
        // }
        // return queue;
        List<BuildQue> queList = new ArrayList<>();
        for (BuildQue que : buildQue.values()) {
            if (!que.haveFreeSpeed()) {
                queList.add(que);

            }
        }
        // ?????????????????????
        if (!CheckNull.isEmpty(queList)) {
            return queList.get(RandomHelper.randomInSize(queList.size()));
        }
        return null;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @return
     */
    public ArmQue getCanAddSpeedArmQue() {
        // List<ArmQue> list = new ArrayList<>();// ????????????????????????
        // list.addAll(factory.get(BuildingType.FACTORY_1).getAddList());
        // list.addAll(factory.get(BuildingType.FACTORY_2).getAddList());
        // list.addAll(factory.get(BuildingType.FACTORY_3).getAddList());
        // int endTime = 0;
        // int now = TimeHelper.getCurrentSecond();
        // ArmQue queue = null;
        // for (ArmQue que : list) {
        // if (!que.haveFreeSpeed()) {
        // if (que.getEndTime() > now && que.getEndTime() > endTime) {
        // endTime = que.getEndTime();
        // queue = que;
        // }
        // }
        // }
        // return queue;
        int[] sfArr = {BuildingType.FACTORY_1, BuildingType.FACTORY_2, BuildingType.FACTORY_3,
                BuildingType.TRAIN_FACTORY_1, BuildingType.TRAIN_FACTORY_2};
        List<ArmQue> queList = new ArrayList<>(); // ?????????????????????
        for (int sf : sfArr) {
            Factory factory = this.factory.get(sf);
            if (!CheckNull.isNull(factory)) {
                LinkedList<ArmQue> que = factory.getAddList();
                if (que != null) {
                    ArmQue aq = que.stream().sorted(Comparator.comparingInt(ArmQue::getEndTime)).findFirst()
                            .orElse(null);
                    if (aq != null && !aq.haveFreeSpeed()) {
                        queList.add(aq);
                        break;
                    }
                }
            }
        }
        // ?????????????????????
        if (!CheckNull.isEmpty(queList)) {
            return queList.get(RandomHelper.randomInSize(queList.size()));
        }
        return null;
    }

    public Integer getPushRecord(int pushId) {
        return pushRecords.get(Integer.toString(pushId));
    }

    /**
     * ????????????????????????(?????????)
     *
     * @param type
     * @param id
     * @return true ?????????????????????
     */
    public boolean checkHaveSpecial(int type, int id) {
        return specialProp.containsKey(type) && specialProp.get(type).contains(id);
    }

    /**
     * ??????????????????
     *
     * @param type
     * @param id
     */
    public void upSpecialProp(int type, int id) {
        Set<Integer> Special = specialProp.get(type);
        if (CheckNull.isEmpty(Special)) {
            Special = new HashSet<>();
        }
        Special.add(id);
        specialProp.put(type, Special);
    }

    /**
     * ????????????????????????????????????
     *
     * @param pushId
     * @return true ??????????????????
     */
    public boolean hasPushRecord(String pushId) {
        Integer status = getPushRecord(pushId);
        return !(null == status || status == PushConstant.PUSH_NOT_PUSHED);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????id??????key?????????????????????
     *
     * @param pushId
     * @param status
     */
    public void putPushRecord(int pushId, int status) {
        pushRecords.put(Integer.toString(pushId), status);
    }

    public Integer getPushRecord(String key) {
        return pushRecords.get(key);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????push_functionId???pushId+???????????????id??????????????????key
     *
     * @param key
     * @param status
     */
    public void putPushRecord(String key, int status) {
        pushRecords.put(key, status);
    }

    public void removePushRecord(int pushId) {
        pushRecords.remove(Integer.toString(pushId));
    }

    public void removePushRecord(String key) {
        pushRecords.remove(key);
    }

    public MailData serMailData() {
        return PlayerSerHelper.serMailData(this);
    }

    /**
     * ?????? ser*()?????? ???????????????????????????????????????
     *
     * @return
     */
    public DataNew serNewData() {
        DataNew dataNew = new DataNew();
        dataNew.setMaxKey(maxKey);
        dataNew.setLordId(roleId);
        dataNew.setRoleData(serData());
        dataNew.setHeros(serHero());
        dataNew.setEquips(serEquip());
        dataNew.setMedals(serMedal());
        dataNew.setProps(serProp());
        dataNew.setBuildQue(serBuildQue());
        dataNew.setTasks(serTask());
        // dataNew.setMails(serMails());
        dataNew.setMill(serMill());
        dataNew.setGains(serGains());
        dataNew.setFactory(serFactory());
        dataNew.setArmy(serArmy());
        dataNew.setCombats(serCombat());
        dataNew.setEquipQue(serEquipQue());
        dataNew.setTypeInfo(serTypeInfo());
        dataNew.setTech(serTech());
        dataNew.setShop(serShop());
        dataNew.setCombatFb(serCombatFb());
        dataNew.setAcquisition(serAcquisition());
        dataNew.setAwards(serTypeAwards());
        dataNew.setSupEquips(serSuperEquip());
        dataNew.setSupEquipQue(serSuperEquipQue());
        dataNew.setOpts(serRoleOpt());
        dataNew.setWallNpc(serWallNpc());
        dataNew.setEffects(serEffects());
        dataNew.setTreasure(serTreasure());
        dataNew.setChemical(serChemical());
        dataNew.setFriends(serFriend());
        dataNew.setMasterApprentice(serMasterApprentice());
        dataNew.setCabinet(serCabinet());
        dataNew.setTrophy(serTrophy());
        dataNew.setPlayerExt(serPlayerExt());
        dataNew.setDay7Act(serDay7Act());
        dataNew.setActivity(serActivity());
        dataNew.setSignin(serSiginInfo());
        dataNew.setSignInExt(serSiginInfoExt());
        dataNew.setCrossData(serCrossData());
        dataNew.setTotem(totemData.ser().toByteArray());
        dataNew.setTreasureWares(serTreasureWares());
        dataNew.setDrawCardData(getDrawCardData().createPb(true).toByteArray());
        dataNew.setChapterTask(this.chapterTask.ser().toByteArray());
        dataNew.setFunctionPlanData(getFunctionPlanData().createPb(true).toByteArray());
        return dataNew;
    }

    public byte[] serTreasureWares() {
        SerTreasureWares.Builder serTreasureWares = SerTreasureWares.newBuilder();
        treasureWares.values().forEach(treasureWare -> {
            serTreasureWares.addTreasure(treasureWare.createPb(true));
        });
        serTreasureWares.setFirstMakeTw(this.makeTreasureWare.createPb(true));
        return serTreasureWares.build().toByteArray();
    }

    public byte[] serCrossData() {
        if (crossPersonalData != null) {
            return crossPersonalData.toSerCrossPersonalDataPb().toByteArray();
        }
        return null;
    }

    private byte[] serDay7Act() {
        return PbHelper.createDbDay7ActPb(day7Act).toByteArray();
    }

    private byte[] serPlayerExt() {
        SerPlayerExt.Builder ser = SerPlayerExt.newBuilder();
        for (Integer id : portraits) {
            ser.addPortraits(id);
        }
        if (summon != null) {
            ser.setSummon(PbHelper.createSummon(summon));
        }
        if (appointFreeTime != 0) {
            ser.setAppointFreeTime(appointFreeTime);
        }
        if (!CheckNull.isEmpty(combatHeroForm)) {
            ser.addAllCombatHeroForm(combatHeroForm);
        }
        ser.setHeroSkin(heroSkin);
        ser.setLoginRewardTime(loginRewardTime);
        for (Entry<Integer, List<Integer>> kv : heroBattlePos.entrySet()) {
            IntListInt inListInt = PbHelper.createIntListInt(kv.getKey(), kv.getValue());
            ser.addBattleHeroPos(inListInt);
        }
        ser.addAllFirstPayDouble(firstPayDouble);
        ser.setBanditCnt(banditCnt);
        ser.setCollectMineCount(collectMineCount);//?????????????????????
        ser.setMedalGoodsRefNum(medalGoodsRefNum);// ????????????????????????
        ser.setPeacekeepingForcesNum(peacekeepingForcesNum);// ???????????? ????????????
        ser.setJGXHLastTime(jGXHLastTime);// ?????? ?????????????????? ?????????????????????
        ser.setIsFireState(isFireState);
        ser.addAllDbTriggerGiftMap(PbHelper.createDbTriggerGiftMap(triggerGifts));
        ser.setHitFlyCount(hitFlyCount);
        ser.setIsOffOnlineHitFly(isOffOnlineHitFly);
        ser.setNightRaidBanditCnt(nightRaidBanditCnt);
        ser.setIsAdvanceAward(isAdvanceAward);
        ser.setAtkCityAct(PbHelper.createAtkCityActDb(atkCityAct));
        if (packId != null) ser.setPackId(packId);
        if (stoneInfo != null) ser.setStoneInfo(this.stoneInfo.ser());
        if (!CheckNull.isEmpty(specialProp)) {
            for (Entry<Integer, Set<Integer>> kv : specialProp.entrySet()) {
                DbSpecialProp specialProp = PbHelper.createSpecialProp(kv);
                ser.addSpecialProp(specialProp);
            }
        }
        ser.addAllMixtureData(PbHelper.createTwoIntListByMap(mixtureData));
        ser.setMilitaryExpenditure(militaryExpenditure);
        if (cheatCode != null && cheatCode.length > 0) {
            for (int c : cheatCode) {
                ser.addCheatCode(c);
            }
        }
        if (!CheckNull.isEmpty(blacklist)) {
            ser.addAllBlacklist(blacklist);
        }
        // ?????????????????? ?????????
        if (!CheckNull.isEmpty(medalGoods)) {
            ser.addAllMedalGoods(medalGoods);
        }
        if (!CheckNull.isEmpty(offLineBuilds)) {
            ser.addAllOffLineBuild(offLineBuilds.values());
        }
        if (!CheckNull.isEmpty(chatBubbles)) {
            ser.addAllChatBubble(chatBubbles);
        }
        ser.setCurChatBubble(curChatBubble);
        if (!CheckNull.isEmpty(bodyImages)) {
            ser.addAllBodyImage(bodyImages);
        }
        ser.setCurBodyImage(curBodyImage);
        if (!CheckNull.isEmpty(warPlanes)) {
            for (WarPlane plane : warPlanes.values()) {
                ser.addDbPlane(plane.ser());
            }
        }
        if (!CheckNull.isEmpty(palneChips)) {
            for (PlaneChip planeChip : palneChips.values()) {
                ser.addPlaneChips(PbHelper.createTwoIntPb(planeChip.getChipId(), planeChip.getCnt()));
            }
        }
        if (this.playerRebellion != null) {
            ser.setDbPlayerRebellion(this.playerRebellion.ser());
        }
        for (FunCard fc : this.funCards.values()) {
            ser.addFunCard(fc.ser());
        }
        if (!CheckNull.isNull(this.mentorInfo)) ser.setMentorInfo(this.mentorInfo.ser());
        if (!CheckNull.isNull(this.decisiveInfo)) ser.setDecisiveInfo(this.decisiveInfo.ser());
        if (!CheckNull.isNull(this.airshipPersonData)) ser.setAirshipPersonData(this.airshipPersonData.ser());
        if (!CheckNull.isNull(this.supplyRecord)) {
            ser.setSupplyRecord(this.supplyRecord.ser());
        }
        // ??????????????????
        if (!CheckNull.isNull(this.playerWorldWarData)) {
            ser.setWorldWar(this.playerWorldWarData.ser());
        }
        for (EquipJewel jewel : this.equipJewel.values()) {
            ser.addJewel(PbHelper.createJewelPb(jewel));
        }
        // ????????????
        ser.setCurCastleSkin(this.curCastleSkin);
        for (Integer id : this.ownCastleSkin) {
            ser.addOwnCastleSkin(id);
        }
        // ????????????
        ser.addAllOwnCastleSkinTime(PbHelper.createTwoIntListByMap(this.ownCastleSkinTime));
        ser.addAllOwnCastleSkinStar(PbHelper.createTwoIntListByMap(this.ownCastleSkinStar));
        // ????????????
        if (!CheckNull.isNull(this.newYorkWar)) {
            ser.setNewYorkWar(this.newYorkWar.ser());
        }
        // npc??????????????????
        if (!CheckNull.isEmpty(this.npcCityFirstKillReward)) {
            ser.addAllNpcCityFirstKillReward(this.npcCityFirstKillReward);
        }
        // ????????????????????????
        if (!CheckNull.isNull(this.battlePassPersonInfo)) {
            ser.setPersonInfo(this.battlePassPersonInfo.ser());
        }
        //??????????????????
        ser.setSandTableScore(this.sandTableScore);
        //??????????????????
        if (!CheckNull.isEmpty(this.sandTableBought)) {
            this.sandTableBought.entrySet().forEach(o -> {
                ser.addSandTableBought(PbHelper.createTwoIntPb(o.getKey(), o.getValue()));
            });
        }
        //????????????????????????
        ser.setSerPlayerOnHook(this.playerOnHook.ser());
        //??????????????????
        ser.setSerPlayerSeasonInfo(this.playerSeasonData.ser());
        // ???????????????
        ser.setDuData(this.dressUp.ser());
        //????????????
        ser.setSerFishingData(this.fishingData.ser());

        //??????rpc ????????????
        if (Objects.nonNull(rpcPlayer)) {
            ser.setSerRpcPlayerData(rpcPlayer.ser());
        }
        //?????????????????????
        if (Objects.nonNull(smallGame)) {
            ser.setSmallGame(smallGame.ser());
        }
        if (Objects.nonNull(treasureCombat)) {
            // ????????????
            ser.setTreasureCombat(treasureCombat.ser(false));
        }
        if (Objects.nonNull(treasureChallengePlayer)) {
            // ????????????????????????
            ser.setTreasureChallengePlayer(treasureChallengePlayer.ser());
        }
        if (treasureWareIdMakeCount.size() > 0) {
            //??????????????????
            treasureWareIdMakeCount.forEach((id, count) -> {
                ser.addTreasureWareIdMakeCount(PbHelper.createTwoIntPb(id, count));
            });
        }
        if (Objects.nonNull(playerRelic)) {
            ser.setSerPlayerRelic(playerRelic.ser());
        }
        //????????????
        if (Objects.nonNull(crossPlayerLocalData)) {
            ser.setSaveCrossData(crossPlayerLocalData.createPb(true));
        }
        if (CheckNull.nonEmpty(recruitReward)) {
            recruitReward.forEach((k, v) -> ser.addRecruitRewardRecord(PbHelper.createTwoIntPb(k, v)));
        }
        // ???????????????
        if (CheckNull.nonEmpty(lifeSimulatorRecordMap)) {
            for (Entry<Integer, List<LifeSimulatorInfo>> entry : lifeSimulatorRecordMap.entrySet()) {
                CommonPb.LifeSimulatorRecord.Builder lifeSimulatorRecordBuilder = CommonPb.LifeSimulatorRecord.newBuilder();
                Integer triggerMode = entry.getKey();
                lifeSimulatorRecordBuilder.setTriggerMode(triggerMode);
                for (LifeSimulatorInfo lifeSimulatorInfo : entry.getValue()) {
                    lifeSimulatorRecordBuilder.addLifeSimulatorInfo(lifeSimulatorInfo.ser());
                }
                ser.addLifeSimulatorRecord(lifeSimulatorRecordBuilder.build());
            }
        }
        // ????????????
        if (!CheckNull.isNull(cityEvent)) {
            ser.setCityEvent(cityEvent.ser());
        }
        // ??????
        if (CheckNull.nonEmpty(characterData)) {
            ser.addAllCharacterData(PbHelper.createTwoIntListByMap(characterData));
        }
        // ??????????????????
        if (CheckNull.nonEmpty(characterRewardRecord)) {
            ser.addAllCharacterRewardRecord(PbHelper.createTwoIntListByMap(characterRewardRecord));
        }
        return ser.build().toByteArray();
    }

    private byte[] serTrophy() {
        SerTrophy.Builder ser = SerTrophy.newBuilder();
        for (Entry<Integer, Integer> entry : trophy.entrySet()) {
            ser.addTrophy(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        return ser.build().toByteArray();
    }

    private byte[] serCombatFb() {
        SerCombatFb.Builder ser = SerCombatFb.newBuilder();
        for (Entry<Integer, CombatFb> kv : combatFb.entrySet()) {
            ser.addCombatFb(PbHelper.createCombatFBPb(kv.getValue()));
        }
        for (Entry<Integer, StoneCombat> kv : stoneCombats.entrySet()) {
            ser.addStoneCombat(PbHelper.createStoneCombatPb(kv.getValue()));
        }
        for (Entry<Integer, PitchCombat> kv : pitchCombats.entrySet()) {
            ser.addPitchCombat(kv.getValue().ser());
        }
        if (this.multCombat != null) {
            ser.setMultCombat(this.multCombat.ser());
        }
        return ser.build().toByteArray();
    }

    private byte[] serShop() {
        SerShop.Builder ser = SerShop.newBuilder();
        if (shop != null) {
            if (shop.getIdCnt() != null) {
                for (Entry<Integer, Integer> kv : shop.getIdCnt().entrySet()) {
                    ser.addIdCnt(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
                }
            }
            if (shop.getVipId() != null) {
                ser.addAllVipId(shop.getVipId());
            }
            if (shop.getFreeCnt() != null) {
                for (Entry<Integer, Integer> kv : shop.getFreeCnt().entrySet()) {
                    ser.addFreeCnt(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
                }
            }
            ser.setRefreshTime(shop.getRefreshTime());
        }
        return ser.build().toByteArray();
    }

    private byte[] serTech() {
        SerTech.Builder ser = SerTech.newBuilder();
        if (tech != null) {
            Map<Integer, TechLv> map = tech.getTechLv();
            if (map != null) {
                for (TechLv techLv : map.values()) {
                    ser.addTech(PbHelper.createTechLv(techLv));
                }
            }
            TechQue que = tech.getQue();
            if (que != null) {
                ser.setTechQue(PbHelper.createTechQue(que));
            }
        }
        return ser.build().toByteArray();
    }

    private byte[] serChemical() {
        SerChemical.Builder ser = SerChemical.newBuilder();
        if (chemical != null) {
            Map<String, ChemicalQue> map = chemical.getPosQue();
            if (map != null) {
                ser.addAllQue(chemical.getPosQue().values());
            }
            ser.setExpandLv(chemical.getExpandLv());
        }
        return ser.build().toByteArray();
    }

    private byte[] serTreasure() {
        SerTreasure.Builder ser = SerTreasure.newBuilder();
        if (treasure != null) {
            ser.setEndTime(treasure.getEndTime());
            ser.setRed(treasure.isRed());
            ser.setResTime(treasure.getResTime());
            ser.setStatus(treasure.getStatus());
            ser.setUpdTime(treasure.getUpdTime());
            if (treasure.getIdStatus() != null && !treasure.getIdStatus().isEmpty()) {
                for (Entry<Integer, Integer> kv : treasure.getIdStatus().entrySet()) {
                    ser.addIdStatus(TwoInt.newBuilder().setV1(kv.getKey()).setV2(kv.getValue()));
                }
            }
        }
        return ser.build().toByteArray();
    }

    private byte[] serTypeInfo() {
        SerTypeInfo.Builder ser = SerTypeInfo.newBuilder();
        for (Entry<Integer, List<History>> kv : typeInfo.entrySet()) {
            if (kv.getValue() != null) {
                for (History v : kv.getValue()) {
                    ser.addHistory(PbHelper.createHistoryPb(kv.getKey(), v));
                }
            }
        }
        return ser.build().toByteArray();
    }

    private byte[] serEquipQue() {
        SerEquipQue.Builder ser = SerEquipQue.newBuilder();
        for (EquipQue que : equipQue) {
            ser.addQue(PbHelper.createEquipQuePb(que));
        }
        return ser.build().toByteArray();
    }

    private byte[] serCombat() {
        SerCombat.Builder ser = SerCombat.newBuilder();
        for (Entry<Integer, Combat> kv : combats.entrySet()) {
            ser.addCombat(PbHelper.createCombatPb(kv.getValue()));
        }
        ser.setCombatInfo(this.combatInfo.createPb(true));
        return ser.build().toByteArray();
    }

    private byte[] serFactory() {
        SerFactory.Builder ser = SerFactory.newBuilder();
        for (Entry<Integer, Factory> kv : factory.entrySet()) {
            ser.addFactory(PbHelper.createFactoryPb(kv.getKey(), kv.getValue()));
        }
        return ser.build().toByteArray();
    }

    private byte[] serFriend() {
        SerFriend.Builder ser = SerFriend.newBuilder();
        for (Entry<Long, DbFriend> kv : friends.entrySet()) {
            ser.addDbFriend(PbHelper.createDbFriendPb(kv.getValue()));
        }
        return ser.build().toByteArray();
    }

    private byte[] serMasterApprentice() {
        SerMasterApprentice.Builder ser = SerMasterApprentice.newBuilder();
        if (master != null) {
            ser.setMaster(PbHelper.createDbMasterApprenticePb(master));
        }
        for (Entry<Long, DbMasterApprentice> kv : apprentices.entrySet()) {
            ser.addApprentices(PbHelper.createDbMasterApprenticePb(kv.getValue()));
        }

        ser.addAllBoughtIds(boughtIds);
        ser.addAllAwardedIds(PbHelper.createTwoIntListByMap(awardedIds));
        return ser.build().toByteArray();
    }

    private byte[] serCabinet() {
        SerCabinet.Builder ser = SerCabinet.newBuilder();
        if (cabinet != null) {
            ser.setCurPlanId(cabinet.getCurPlanId());
            ser.setPrePlanId(cabinet.getPrePlanId());
            ser.setLeadStep(cabinet.getLeadStep());
            ser.setIsFinsh(cabinet.isFinsh());
            ser.setIsCreateLead(cabinet.isCreateLead());
            ser.setIsLvFinish(cabinet.isLvFinish());
        }
        return ser.build().toByteArray();
    }

    public void dserNewData(DataNew data) throws InvalidProtocolBufferException {
        roleId = data.getLordId();
        if (data.getRoleData() != null) {
            SerData ser = SerData.parseFrom(data.getRoleData());
            dserRoleData(ser);
        }

        if (data.getHeros() != null) {
            SerHero ser = SerHero.parseFrom(data.getHeros());
            dserHeros(ser);
        }

        if (data.getEquips() != null) {
            SerEquip ser = SerEquip.parseFrom(data.getEquips());
            dserEquips(ser);
        }

        if (data.getMedals() != null) {
            SerMedal ser = SerMedal.parseFrom(data.getMedals());
            dserMedals(ser);
        }

        if (data.getProps() != null) {
            SerProp ser = SerProp.parseFrom(data.getProps());
            dserProps(ser);
        }

        if (data.getBuildQue() != null) {
            SerBuildQue ser = SerBuildQue.parseFrom(data.getBuildQue());
            dserBuildQue(ser);
        }

        if (data.getTasks() != null) {
            SerTask ser = SerTask.parseFrom(data.getTasks());
            dserTasks(ser);
        }

        if (data.getMill() != null) {
            SerMill ser = SerMill.parseFrom(data.getMill());
            dserMills(ser);
        }

        if (data.getGains() != null) {
            SerGains ser = SerGains.parseFrom(data.getGains());
            dserGains(ser);
        }

        if (data.getFactory() != null) {
            SerFactory ser = SerFactory.parseFrom(data.getFactory());
            dserFactory(ser);
        }

        if (data.getArmy() != null) {
            SerArmy ser = SerArmy.parseFrom(data.getArmy());
            dserArmy(ser);
        }

        if (data.getCombats() != null) {
            SerCombat ser = SerCombat.parseFrom(data.getCombats());
            dserCombat(ser);
        }

        if (data.getEquipQue() != null) {
            SerEquipQue ser = SerEquipQue.parseFrom(data.getEquipQue());
            dserEquipQue(ser);
        }

        if (data.getTypeInfo() != null) {
            SerTypeInfo ser = SerTypeInfo.parseFrom(data.getTypeInfo());
            dserTypeInfo(ser);
        }

        if (data.getTech() != null) {
            SerTech ser = SerTech.parseFrom(data.getTech());
            dserTech(ser);
        }

        if (data.getShop() != null) {
            SerShop ser = SerShop.parseFrom(data.getShop());
            dserShop(ser);
        }

        if (data.getCombatFb() != null) {
            SerCombatFb ser = SerCombatFb.parseFrom(data.getCombatFb());
            dserCombatFb(ser);
        }

        if (data.getAcquisition() != null) {
            SerAcquisition ser = SerAcquisition.parseFrom(data.getAcquisition());
            dserAcquisition(ser);
        }

        if (data.getAwards() != null) {
            SerTypeAwards ser = SerTypeAwards.parseFrom(data.getAwards());
            dserTypeAwards(ser);
        }
        if (data.getSupEquips() != null) {
            SerSuperEquip ser = SerSuperEquip.parseFrom(data.getSupEquips());
            dserSupEquips(ser);
        }
        if (data.getSupEquipQue() != null) {
            SerSuperEquipQue ser = SerSuperEquipQue.parseFrom(data.getSupEquipQue());
            dserSupEquipQue(ser);
        }
        if (data.getOpts() != null) {
            SerRoleOpt ser = SerRoleOpt.parseFrom(data.getOpts());
            dserRoleOpt(ser);
        }
        if (data.getWallNpc() != null) {
            SerWallNpc ser = SerWallNpc.parseFrom(data.getWallNpc());
            dserWallNpc(ser);
        }
        if (data.getEffects() != null) {
            SerEffects ser = SerEffects.parseFrom(data.getEffects());
            dserEffect(ser);
        }
        if (data.getTreasure() != null) {
            SerTreasure ser = SerTreasure.parseFrom(data.getTreasure());
            dserTreasure(ser);
        }
        if (data.getChemical() != null) {
            SerChemical ser = SerChemical.parseFrom(data.getChemical());
            dserChemical(ser);
        }
        if (data.getFriends() != null) {
            SerFriend ser = SerFriend.parseFrom(data.getFriends());
            dserFriends(ser);
        }
        if (data.getMasterApprentice() != null) {
            SerMasterApprentice ser = SerMasterApprentice.parseFrom(data.getMasterApprentice());
            dserMasterApprentice(ser);
        }
        if (data.getCabinet() != null) {
            SerCabinet ser = SerCabinet.parseFrom(data.getCabinet());
            dserCabinet(ser);
        }
        if (data.getTrophy() != null) {
            SerTrophy ser = SerTrophy.parseFrom(data.getTrophy());
            dserTrophy(ser);
        }
        if (data.getPlayerExt() != null) {
            SerPlayerExt ser = SerPlayerExt.parseFrom(data.getPlayerExt());
            dserPlayerExt(ser);
        }
        if (data.getDay7Act() != null) {
            DbDay7Act ser = DbDay7Act.parseFrom(data.getDay7Act());
            dserDay7Act(ser);
        }
        if (data.getActivity() != null) {
            SerActivity ser = SerActivity.parseFrom(data.getActivity());
            dserActivity(ser);
        }

        if (data.getSignin() != null) {
            SignInInfo ser = SignInInfo.parseFrom(data.getSignin());
            dserSignInInfo(ser);
        }

        if (data.getSignInExt() != null) {
            SerSignInInfo ser = SerSignInInfo.parseFrom(data.getSignInExt());
            dserSignInInfoExt(ser);
        }

        if (data.getCrossData() != null) {
            SerCrossPersonalData ser = SerCrossPersonalData.parseFrom(data.getCrossData());
            dserCrossData(ser);
        }
        if (data.getTotem() != null) {
            TotemDataInfo ser = TotemDataInfo.parseFrom(data.getTotem());
            this.totemData.dser(ser);
        }

        if (data.getTreasureWares() != null) {
            SerTreasureWares ser = SerTreasureWares.parseFrom(data.getTreasureWares());
            dserTreasureWares(ser);
        }

        if (data.getDrawCardData() != null) {
            SerDrawCardData ser = SerDrawCardData.parseFrom(data.getDrawCardData());
            this.getDrawCardData().deSer(ser);
        }

        if (data.getChapterTask() != null) {
            SerChapterTask ser = SerChapterTask.parseFrom(data.getChapterTask());
            this.chapterTask.dser(ser);
        }

        if (data.getFunctionPlanData() != null) {
            SerializePb.SerFunctionPlanData ser = SerializePb.SerFunctionPlanData.parseFrom(data.getFunctionPlanData(), DataResource.getRegistry());
            this.getFunctionPlanData().dePlanFunctionPb(ser);
        }

        setMaxKey(data.getMaxKey());
    }

    private void dserDay7Act(DbDay7Act dbDay7Act) {
        for (Integer v : dbDay7Act.getRecvAwardIdsList()) {
            day7Act.getRecvAwardIds().add(v);
        }
        for (TwoInt v : dbDay7Act.getStatusList()) {
            day7Act.getStatus().put(v.getV1(), v.getV2());
        }
        for (StrInt v : dbDay7Act.getTankTypesList()) {
            day7Act.getTankTypes().put(v.getV1(), v.getV2());
        }
        for (DbDay7ActStatus v : dbDay7Act.getTypeCntList()) {
            Map<Integer, Integer> map = day7Act.getTypeCnt().get(v.getKey());
            if (map == null) {
                map = new HashMap<>();
                day7Act.getTypeCnt().put(v.getKey(), map);
            }
            for (TwoInt vv : v.getStatusList()) {
                map.put(vv.getV1(), vv.getV2());
            }
        }
    }

    private void dserPlayerExt(SerPlayerExt ser) {
        portraits.addAll(ser.getPortraitsList());
        if (ser.hasSummon()) {
            summon = new Summon(ser.getSummon());
        }
        if (ser.hasAppointFreeTime()) {
            appointFreeTime = ser.getAppointFreeTime();
        }
        if (!CheckNull.isEmpty(ser.getCombatHeroFormList())) {
            combatHeroForm.addAll(ser.getCombatHeroFormList());
        }
        if (ser.hasHeroSkin()) {
            heroSkin = ser.getHeroSkin();
        }
        if (ser.hasLoginRewardTime()) {
            loginRewardTime = ser.getLoginRewardTime();
        }
        for (IntListInt ili : ser.getBattleHeroPosList()) {
            List<Integer> v2List = ili.getV2List();
            if (!CheckNull.isEmpty(v2List)) {
                List<Integer> iliList = new ArrayList<>(v2List);
                heroBattlePos.put(ili.getV1(), iliList);
            }
        }
        firstPayDouble.addAll(ser.getFirstPayDoubleList());
        if (ser.hasBanditCnt()) {
            banditCnt = ser.getBanditCnt();
        }
        // ???????????????????????? ????????????
        if (ser.hasMedalGoodsRefNum()) {
            medalGoodsRefNum = ser.getMedalGoodsRefNum();
        }
        // ?????????????????? ????????????
        if (ser.hasPeacekeepingForcesNum()) {
            peacekeepingForcesNum = ser.getPeacekeepingForcesNum();
        }
        // ????????????????????????????????? ????????????
        if (ser.hasJGXHLastTime()) {
            jGXHLastTime = ser.getJGXHLastTime();
        }
        if (ser.hasIsFireState()) {
            isFireState = ser.getIsFireState();
        }
        if (ser.hasHitFlyCount()) {
            hitFlyCount = ser.getHitFlyCount();
        }
        if (ser.hasIsOffOnlineHitFly()) {
            isOffOnlineHitFly = ser.getIsOffOnlineHitFly();
        }
        for (DbTriggerGiftMap dbTriggerGiftMap : ser.getDbTriggerGiftMapList()) {
            int triggerId = dbTriggerGiftMap.getKey();
            HashMap<Integer, TriggerGift> giftMap = new HashMap<>();
            List<SerTriggerGift> serTriggerGiftList = dbTriggerGiftMap.getSerTriggerGiftList();
            for (SerTriggerGift serTriggerGift : serTriggerGiftList) {
                TriggerGift triggerGift = new TriggerGift(serTriggerGift);
                giftMap.put(serTriggerGift.getGiftId(), triggerGift);
            }
            triggerGifts.put(triggerId, giftMap);
        }
        if (ser.hasAtkCityAct()) {
            setAtkCityAct(ser.getAtkCityAct());
        }
        if (ser.hasNightRaidBanditCnt()) {
            setNightRaidBanditCnt(ser.getNightRaidBanditCnt());
        }
        if (ser.hasIsAdvanceAward()) {
            setAdvanceAward(ser.getIsAdvanceAward());
        }
        if (ser.hasPackId()) {
            setPackId(ser.getPackId());
        }
        for (DbSpecialProp prop : ser.getSpecialPropList()) {
            List<Integer> specialId = prop.getSpecialIdList();
            if (!CheckNull.isEmpty(specialId)) {
                Set<Integer> list = new HashSet<>(specialId);
                specialProp.put(prop.getType(), list);
            }
        }
        this.stoneInfo = ser.hasStoneInfo() ? new StoneInfo(ser.getStoneInfo()) : new StoneInfo();
        for (TwoInt twoInt : ser.getMixtureDataList()) {
            mixtureData.put(twoInt.getV1(), twoInt.getV2());
        }

        this.militaryExpenditure = ser.getMilitaryExpenditure();
        if (!CheckNull.isEmpty(ser.getCheatCodeList())) {
            int size = ser.getCheatCodeList().size();
            int[] arr = new int[size];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = ser.getCheatCodeList().get(i);
            }
            cheatCode = arr;
        }
        if (!CheckNull.isEmpty(ser.getBlacklistList())) {
            this.blacklist = new ArrayList<>();
            this.blacklist.addAll(ser.getBlacklistList());
        }
        // ?????? ???????????? ????????????
        if (!CheckNull.isEmpty(ser.getMedalGoodsList())) {
            this.medalGoods = new ArrayList<>();
            this.medalGoods.addAll(ser.getMedalGoodsList());
        }
        if (!CheckNull.isEmpty(ser.getOffLineBuildList())) {
            List<OffLineBuild> list = new ArrayList<OffLineBuild>();
            list = ser.getOffLineBuildList();
            for (OffLineBuild offLineBuild : list) {
                this.offLineBuilds.put(offLineBuild.getId(), offLineBuild);
            }
        }
        this.chatBubbles.addAll(ser.getChatBubbleList());
        setCurChatBubble(ser.getCurChatBubble());

        this.bodyImages.addAll(ser.getBodyImageList());
        this.npcCityFirstKillReward.addAll(ser.getNpcCityFirstKillRewardList());
        List<TwoInt> planeChipsList = ser.getPlaneChipsList();
        if (!CheckNull.isEmpty(planeChipsList)) {
            for (TwoInt twoInt : planeChipsList) {
                int chipId = twoInt.getV1();
                PlaneChip planeChip = getPlaneChip(chipId);
                planeChip.setChipId(chipId);
                planeChip.setCnt(twoInt.getV2());
            }
        }
        List<DbWarPlane> dbPlaneList = ser.getDbPlaneList();
        if (!CheckNull.isEmpty(dbPlaneList)) {
            for (DbWarPlane dbWarPlane : dbPlaneList) {
                int type = dbWarPlane.getType();
                this.warPlanes.put(type, new WarPlane(dbWarPlane));
            }
        }

        setCurBodyImage(ser.getCurBodyImage());
        if (ser.hasDbPlayerRebellion()) {
            PlayerRebellion pr = new PlayerRebellion();
            pr.dser(ser.getDbPlayerRebellion());
            this.playerRebellion = pr;
        }
        List<CommonPb.FunCard> funCardList = ser.getFunCardList();
        if (!CheckNull.isEmpty(funCardList)) {
            for (CommonPb.FunCard cfc : funCardList) {
                FunCard fc = new FunCard(cfc.getType());
                fc.dser(cfc);
                funCards.put(fc.getType(), fc);
            }
        }
        this.mentorInfo = ser.hasMentorInfo() ? new MentorInfo(ser.getMentorInfo())
                : new MentorInfo(ser.getMentorInfo());

        if (ser.hasDecisiveInfo()) {
            this.decisiveInfo.dser(ser.getDecisiveInfo());
        }
        if (ser.hasAirshipPersonData()) {
            DbAirshipPersonData dbSer = ser.getAirshipPersonData();
            this.airshipPersonData = new AirshipPersonData();
            this.airshipPersonData.dser(dbSer);
        }
        if (ser.hasSupplyRecord()) {
            this.supplyRecord.dser(ser.getSupplyRecord());
        }
        // ????????????????????????
        if (ser.hasWorldWar()) {
            this.playerWorldWarData.deser(ser.getWorldWar());
        }
        List<CommonPb.EquipJewel> jewelList = ser.getJewelList();
        if (!CheckNull.isEmpty(jewelList)) {
            for (CommonPb.EquipJewel jewel : jewelList) {
                this.equipJewel.put(jewel.getJewelId(), new EquipJewel(jewel));
            }
        }
        // ????????????
        if (ser.hasCurCastleSkin()) {
            this.curCastleSkin = ser.getCurCastleSkin();
        }
        this.ownCastleSkin.addAll(ser.getOwnCastleSkinList());
        Optional.ofNullable(ser.getOwnCastleSkinTimeList()).ifPresent(cs ->
                cs.forEach(c -> this.ownCastleSkinTime.put(c.getV1(), c.getV2()))
        );
        Optional.ofNullable(ser.getOwnCastleSkinStarList()).ifPresent(tmpList -> tmpList.forEach(o -> this.ownCastleSkinStar.put(o.getV1(), o.getV2())));
        // ????????????????????????
        if (ser.hasNewYorkWar()) {
            this.newYorkWar.deser(ser.getNewYorkWar());
        }
        // ?????????????????????????????????
        if (ser.hasPersonInfo()) {
            this.battlePassPersonInfo.dser(ser.getPersonInfo());
        }
        //????????????
        this.sandTableScore = ser.getSandTableScore();
        //????????????
        Optional.ofNullable(ser.getSandTableBoughtList()).ifPresent(tmp -> tmp.forEach(o -> this.sandTableBought.put(o.getV1(), o.getV2())));

        this.playerOnHook.deser(ser.getSerPlayerOnHook());

        Optional.ofNullable(ser.getSerPlayerSeasonInfo()).ifPresent(tmp -> this.playerSeasonData.deser(tmp));

        // ??????????????????
        if (ser.hasDuData()) {
            this.dressUp.dser(ser.getDuData());
        }
        //????????????????????????????????????
        if (ser.hasCollectMineCount()) {
            this.collectMineCount = ser.getCollectMineCount();
        }
        //????????????
        if (ser.hasSerFishingData()) {
            this.fishingData.dser(ser.getSerFishingData());
        }
        //?????? rpc ??????
        if (ser.hasSerRpcPlayerData()) {
            this.rpcPlayer = new RpcPlayer(ser.getSerRpcPlayerData());
        }
        //???????????????
        if (ser.hasSmallGame()) {
            this.smallGame = new SmallGame(ser.getSmallGame());
        }
        if (ser.hasTreasureCombat()) {
            this.treasureCombat.dSer(ser.getTreasureCombat());
        }
        if (ser.hasTreasureChallengePlayer()) {
            this.treasureChallengePlayer.dSer(ser.getTreasureChallengePlayer());
        }
        //??????id????????????
        Optional.ofNullable(ser.getTreasureWareIdMakeCountList()).ifPresent(tmp -> tmp.forEach(o -> this.treasureWareIdMakeCount.put(o.getV1(), o.getV2())));
        if (ser.hasSaveCrossData()) {
            this.crossPlayerLocalData = new CrossPlayerLocalData(ser.getSaveCrossData());
        }
        if (ser.hasSerPlayerRelic()) {
            this.playerRelic.dser(ser.getSerPlayerRelic());
        }
        Optional.ofNullable(ser.getRecruitRewardRecordList()).ifPresent(tmp -> tmp.forEach(o -> this.recruitReward.put(o.getV1(), o.getV2())));
        // ???????????????
        if (CheckNull.nonEmpty(ser.getLifeSimulatorRecordList())) {
            for (CommonPb.LifeSimulatorRecord lifeSimulatorRecordPb : ser.getLifeSimulatorRecordList()) {
                int triggerMode = lifeSimulatorRecordPb.getTriggerMode();
                List<LifeSimulatorInfo> lifeSimulatorInfoList = new ArrayList<>();
                for (CommonPb.LifeSimulatorInfo lifeSimulatorInfoPb : lifeSimulatorRecordPb.getLifeSimulatorInfoList()) {
                    lifeSimulatorInfoList.add(new LifeSimulatorInfo().dser(lifeSimulatorInfoPb));
                }
                this.lifeSimulatorRecordMap.put(triggerMode, lifeSimulatorInfoList);
            }
        }
        // ????????????
        if (ser.hasCityEvent()) {
            this.cityEvent = this.cityEvent.dser(ser.getCityEvent());
        }
        // ?????????
        if (CheckNull.isEmpty(ser.getCharacterDataList())) {
            // ???????????????
            List<StaticCharacter> staticCharacterList = StaticBuildCityDataMgr.getStaticCharacterList();
            for (int i = 0; i < staticCharacterList.size(); i++) {
                this.characterData.put(i + 1, 0);
            }
        } else {
            ser.getCharacterDataList().forEach(o -> this.characterData.put(o.getV1(), o.getV2()));
        }
        // ??????????????????
        if (CheckNull.isEmpty(ser.getCharacterRewardRecordList())) {
            // ???????????????
            List<StaticCharacterReward> staticCharacterRewardList = StaticBuildCityDataMgr.getStaticCharacterRewardList();
            for (int i = 0; i < staticCharacterRewardList.size(); i++) {
                this.characterRewardRecord.put(i + 1, 0);
            }
        } else {
            ser.getCharacterRewardRecordList().forEach(o -> this.characterRewardRecord.put(o.getV1(), o.getV2()));
        }
    }

    private void dserTrophy(SerTrophy ser) {
        for (TwoInt twoInt : ser.getTrophyList()) {
            trophy.put(twoInt.getV1(), twoInt.getV2());
        }
    }

    private void dserFriends(SerFriend ser) {
        for (CommonPb.DbFriend fPb : ser.getDbFriendList()) {
            friends.put(fPb.getLordId(), new DbFriend(fPb));
        }
    }

    private void dserMasterApprentice(SerMasterApprentice ser) {
        if (ser.hasMaster()) {
            CommonPb.DbMasterApprentice pb = ser.getMaster();
            master = new DbMasterApprentice(pb);
        }
        for (CommonPb.DbMasterApprentice pb : ser.getApprenticesList()) {
            apprentices.put(pb.getLordId(), new DbMasterApprentice(pb));
        }
        boughtIds.addAll(ser.getBoughtIdsList());

        for (TwoInt ti : ser.getAwardedIdsList()) {
            awardedIds.put(ti.getV1(), ti.getV2());
        }
    }

    private void dserCabinet(SerCabinet ser) {
        if (null == cabinet) {
            cabinet = new Cabinet(ser);
        }
    }

    private void dserChemical(SerChemical ser) {
        if (chemical == null) {
            chemical = new Chemical();
        }
        chemical.setExpandLv(ser.getExpandLv());
        for (ChemicalQue pb : ser.getQueList()) {
            chemical.getPosQue().put(pb.getClonePos() ? (pb.getPos() + "_") : (pb.getPos() + ""), pb);
        }
    }

    private void dserTreasure(SerTreasure ser) {
        if (treasure == null) {
            treasure = new Treasure();
        }
        treasure.setEndTime(ser.getEndTime());
        treasure.setRed(ser.getRed());
        treasure.setResTime(ser.getResTime());
        treasure.setStatus(ser.getStatus());
        treasure.setUpdTime(ser.getUpdTime());
        for (TwoInt kv : ser.getIdStatusList()) {
            treasure.getIdStatus().put(kv.getV1(), kv.getV2());
        }
    }

    private void dserCombatFb(SerCombatFb ser) {
        for (CombatFB pb : ser.getCombatFbList()) {
            this.combatFb.put(pb.getCombatId(), new CombatFb(pb.getCombatId(), pb.getStatus(), pb.getCnt(),
                    pb.getEndTime(), pb.getGain(), pb.getBuyCnt()));
        }
        for (CommonPb.StoneCombat pb : ser.getStoneCombatList()) {
            this.stoneCombats.put(pb.getCombatId(), new StoneCombat(pb.getCombatId(), pb.getPassCnt()));
        }
        for (CommonPb.PitchCombat pb : ser.getPitchCombatList()) {
            PitchCombat pc = new PitchCombat(pb.getType());
            pc.dser(pb);
            this.pitchCombats.put(pc.getType(), pc);
        }
        if (ser.hasMultCombat()) {
            CommonPb.MultCombat mc = ser.getMultCombat();
            this.multCombat = new MultCombat();
            this.multCombat.dser(mc);
        }
    }

    private void dserShop(SerShop ser) {
        if (shop == null) {
            shop = new Shop();
        }
        if (ser.getIdCntList() != null) {
            for (TwoInt pb : ser.getIdCntList()) {
                this.shop.getIdCnt().put(pb.getV1(), pb.getV2());
            }
        }
        if (ser.getVipIdList() != null) {
            this.shop.getVipId().addAll(ser.getVipIdList());
        }
        if (ser.getFreeCntList() != null) {
            for (TwoInt t : ser.getFreeCntList()) {
                this.shop.getFreeCnt().put(t.getV1(), t.getV2());
            }
        }
        shop.setRefreshTime(ser.getRefreshTime());
    }

    private void dserTech(SerTech ser) {
        if (tech == null) {
            tech = new Tech();
        }
        if (ser.getTechList() != null) {
            for (CommonPb.Tech pb : ser.getTechList()) {
                this.tech.getTechLv().put(pb.getId(), new TechLv(pb.getId(), pb.getLv(), pb.getExp()));
            }
        }
        CommonPb.TechQue techQuePb = ser.getTechQue();
        if (techQuePb != null) {
            tech.setQue(new TechQue(techQuePb));
        }
    }

    private void dserTypeInfo(SerTypeInfo ser) {
        for (CommonPb.History pb : ser.getHistoryList()) {
            List<History> list = typeInfo.get(pb.getType());
            if (list == null) {
                list = new ArrayList<>();
                typeInfo.put(pb.getType(), list);
            }
            list.add(new History(pb.getId(), pb.getPraram()));
        }
    }

    private void dserEquipQue(SerEquipQue ser) {
        for (CommonPb.EquipQue pb : ser.getQueList()) {
            EquipQue eq = new EquipQue(pb.getKeyId(), pb.getEquipId(), pb.getPeriod(), pb.getEndTime(), pb.getFreeCnt(),
                    pb.getFreeTime());
            this.equipQue.add(eq);
            if (pb.hasEmployeId()) {
                eq.setEmployeId(pb.getEmployeId());
            }
        }
    }

    private void dserCombat(SerCombat ser) {
        for (CommonPb.Combat pb : ser.getCombatList()) {
            this.combats.put(pb.getCombatId(), new Combat(pb.getCombatId(), pb.getStar()));
        }
        this.combatInfo.dseCombatInfoPb(ser.getCombatInfo());
    }

    private void dserMills(SerMill ser) {
        for (CommonPb.Mill mill : ser.getMillList()) {
            this.mills.put(mill.getId(), new Mill(mill));
        }
        if (!CheckNull.isEmpty(ser.getBuildExtList())) {
            for (BuildingBase b : ser.getBuildExtList()) {
                BuildingExt ext = new BuildingExt(b.getId(), b.getType(), b.getUnlock());
                ext.setUnLockTime(b.getUnLockTime());
                this.buildingExts.put(b.getId(), ext);
            }
        }
    }

    private void dserGains(SerGains ser) {
        for (CommonPb.Gains gains : ser.getGainsList()) {
            this.gains.put(gains.getType(), new Gains(gains.getType(), gains.getId(), gains.getEndTime()));
        }
    }

    private void dserFactory(SerFactory ser) {
        for (CommonPb.Factory pb : ser.getFactoryList()) {
            Factory factory = new Factory();
            factory.setFctLv(pb.getFctLv());
            factory.setFctExpLv(pb.getFctExpLv());
            ArmQue armQue;
            for (CommonPb.ArmQue que : pb.getArmQueList()) {
                armQue = new ArmQue(que.getKeyId(), que.getId(), que.getAddArm(), que.getEndTime(), que.getTime(),
                        que.getNeedFood(), que.getNeedOIL());
                if (que.hasFree()) {
                    armQue.setFree(que.getFree());
                }
                if (que.hasParam()) {
                    armQue.setParam(que.getParam());
                }
                if (que.hasIsNotExtendQue()) {
                    armQue.setNotExtendQue(que.getIsNotExtendQue());
                }
                factory.getAddList().add(armQue);
            }
            this.factory.put(pb.getId(), factory);
        }
    }

    private byte[] serData() {
        SerData.Builder ser = SerData.newBuilder();
        for (Entry<String, Integer> entry : pushRecords.entrySet()) {
            ser.addPushRecord(PbHelper.createStrIntPb(entry.getKey(), entry.getValue()));
        }
        return ser.build().toByteArray();
    }

    private byte[] serHero() {
        SerHero.Builder ser = SerHero.newBuilder();
        for (Hero hero : heros.values()) {
            ser.addHero(PbHelper.createHeroPb(hero, this));
        }
        if (cia != null) {
            ser.setCia(cia.ser());
        }
        return ser.build().toByteArray();
    }

    private byte[] serEquip() {
        SerEquip.Builder ser = SerEquip.newBuilder();
        for (Equip equip : equips.values()) {
            ser.addEquip(PbHelper.createEquipSavePb(equip));
        }
        return ser.build().toByteArray();
    }

    /**
     * @return byte[]
     * @Title: serMedal
     * @Description: ?????????????????? ?????????
     */
    private byte[] serMedal() {
        SerMedal.Builder ser = SerMedal.newBuilder();
        for (Medal medal : medals.values()) {
            ser.addMedal(PbHelper.createMedalSavePb(medal));
        }
        return ser.build().toByteArray();
    }

    private byte[] serProp() {
        SerProp.Builder ser = SerProp.newBuilder();
        for (Prop prop : props.values()) {
            ser.addProp(PbHelper.createPropPb(prop));
        }
        return ser.build().toByteArray();
    }

    private byte[] serBuildQue() {
        SerBuildQue.Builder ser = SerBuildQue.newBuilder();
        for (BuildQue buildQue : buildQue.values()) {
            ser.addBuildQue(PbHelper.createBuildQuePb(buildQue));
        }
        return ser.build().toByteArray();
    }

    private byte[] serTask() {
        SerTask.Builder ser = SerTask.newBuilder();
//        for (Task task : majorTasks.values()) {
//            ser.addMajorTask(PbHelper.createTaskPb(task));
//        }

        for (Task task : dailyTask.values()) {
            ser.addDayiyTask(PbHelper.createTaskPb(task));
        }
        ser.addAllDailyIsGet(dailyIsGet);
        ser.setDailyTaskLivenss(dailyTaskLivenss);

        for (Task task : partyTask.values()) {
            ser.addLiveTask(PbHelper.createTaskPb(task));
        }

        for (Task task : worldTasks.values()) {
            ser.addWorldTask(PbHelper.createTaskPb(task));
        }
        for (Sectiontask ss : sectiontask) {
            ser.addSection(ss.ser());
        }

        for (Task task : advanceTask.values()) {
            ser.addAdvanceTask(PbHelper.createTaskPb(task));
        }

        return ser.build().toByteArray();
    }

    private byte[] serMill() {
        SerMill.Builder ser = SerMill.newBuilder();
        for (Mill mill : mills.values()) {
            ser.addMill(PbHelper.createMillPb(mill));
        }
        for (BuildingExt ext : buildingExts.values()) {
            ser.addBuildExt(PbHelper.createBuildingBaseByExtPb(ext));
        }
        return ser.build().toByteArray();
    }

    private byte[] serGains() {
        SerGains.Builder ser = SerGains.newBuilder();
        for (Gains gains : gains.values()) {
            ser.addGains(PbHelper.createGainsPb(gains));
        }
        return ser.build().toByteArray();
    }

    private byte[] serArmy() {
        SerArmy.Builder ser = SerArmy.newBuilder();
        for (Army army : armys.values()) {
            ser.addArmy(PbHelper.createArmyPb(army, true));
        }
        return ser.build().toByteArray();
    }

    private byte[] serAcquisition() {
        SerAcquisition.Builder ser = SerAcquisition.newBuilder();
        ser.setCollectDate(acquisiteDate);
        for (Entry<Integer, Integer> entry : acquisiteReward.entrySet()) {
            ser.addReward(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
        }
        ser.addAllCollect(acquisiteQue);
        return ser.build().toByteArray();
    }

    private byte[] serTypeAwards() {
        SerTypeAwards.Builder ser = SerTypeAwards.newBuilder();
        for (Entry<Integer, List<Award>> kv : awards.entrySet()) {
            ser.addReward(PbHelper.createTypeAwardsPb(kv.getKey(), kv.getValue()));
        }
        return ser.build().toByteArray();
    }

    private byte[] serSuperEquip() {
        SerSuperEquip.Builder ser = SerSuperEquip.newBuilder();
        for (SuperEquip superEquip : supEquips.values()) {
            ser.addSuperEquip(PbHelper.createSuperEquipPb(superEquip));
        }
        return ser.build().toByteArray();
    }

    private byte[] serSuperEquipQue() {
        SerSuperEquipQue.Builder ser = SerSuperEquipQue.newBuilder();
        ser.addAllQue(supEquipQue);
        return ser.build().toByteArray();
    }

    private byte[] serRoleOpt() {
        SerRoleOpt.Builder ser = SerRoleOpt.newBuilder();
        ser.addAllOpt(opts);
        return ser.build().toByteArray();
    }

    private byte[] serWallNpc() {
        SerWallNpc.Builder ser = SerWallNpc.newBuilder();
        for (WallNpc wn : wallNpc.values()) {
            ser.addWallNpc(PbHelper.createWallNpcPb(wn));
        }
        return ser.build().toByteArray();
    }

    private byte[] serEffects() {
        SerEffects.Builder ser = SerEffects.newBuilder();
        for (Effect effect : effects.values()) {
            ser.addEffect(PbHelper.createEffectPb(effect));
        }
        return ser.build().toByteArray();
    }

    private byte[] serActivity() {
        SerActivity.Builder ser = SerActivity.newBuilder();
        Iterator<Activity> it = activitys.values().iterator();
        while (it.hasNext()) {
            Activity next = it.next();
            if (CheckNull.isNull(next)) {
                continue;
            }
            ser.addActivity(PbHelper.createDbActivityPb(next));
        }
        ser.setActBlackhawk(this.blackhawkAct.dser());
        this.actBarton.values().forEach(e -> ser.addActBarton(e.dser()));
        this.actRobinHood.values().forEach(e -> ser.addRobinHood(e.ser()));
        ser.addAllPersonalActs(this.personalActs.createPb(true));
        return ser.build().toByteArray();
    }

    private byte[] serSiginInfo() {
        return PbHelper.createSignInInfoPb(this.siginInfo).toByteArray();
    }

    private byte[] serSiginInfoExt() {
        SerSignInInfo.Builder ser = SerSignInInfo.newBuilder();
        for (Entry<Integer, SiginInfo> en : signInfoMap.entrySet()) {
            ser.addSignInInfo(PbHelper.createSignInInfoPb(en.getValue(), en.getKey()));
        }
        return ser.build().toByteArray();
    }

    private void dserRoleData(SerData ser) {
        for (StrInt strInt : ser.getPushRecordList()) {
            pushRecords.put(strInt.getV1(), strInt.getV2());
        }
    }

    private void dserHeros(SerHero ser) {
        Hero hero;
        for (CommonPb.Hero h : ser.getHeroList()) {
            hero = new Hero(h);
            heros.put(hero.getHeroId(), hero);
            if (hero.isOnBattle()) {
                if (hero.getPos() >= HeroConstant.HERO_BATTLE_1 && hero.getPos() <= HeroConstant.HERO_BATTLE_4) {
                    heroBattle[hero.getPos()] = hero.getHeroId();
                }
                if (hero.getDefPos() >= HeroConstant.HERO_BATTLE_1 && hero.getDefPos() <= HeroConstant.HERO_BATTLE_4) {
                    heroDef[hero.getDefPos()] = hero.getHeroId();
                }
            }
            if (hero.isOnWall()) {
                if (hero.getWallPos() >= HeroConstant.HERO_BATTLE_1
                        && hero.getWallPos() <= HeroConstant.HERO_BATTLE_4) {
                    heroWall[hero.getWallPos()] = hero.getHeroId();
                }
            }
            if (hero.isOnAcq()) {
                if (hero.getAcqPos() >= HeroConstant.HERO_BATTLE_1 && hero.getAcqPos() <= HeroConstant.HERO_BATTLE_4) {
                    heroAcq[hero.getAcqPos()] = hero.getHeroId();
                }
            }
            if (hero.isCommando()) {
                if (hero.getCommandoPos() >= HeroConstant.HERO_BATTLE_1
                        && hero.getCommandoPos() <= Constant.COMMANDO_HERO_REQUIRE.size()) {
                    heroCommando[hero.getCommandoPos()] = hero.getHeroId();
                }
            }
        }

        if (ser.hasCia()) {
            Cia cia = new Cia(ser.getCia());
            this.cia = cia;
        }
    }

    private void dserEquips(SerEquip ser) {
        for (CommonPb.Equip equip : ser.getEquipList()) {
            equips.put(equip.getKeyId(), equip.hasExta() ? new Ring(equip) : new Equip(equip));
        }
    }

    /**
     * @param ser
     * @return void
     * @Title: dserMedals
     * @Description: ?????????????????? ????????????
     */
    private void dserMedals(SerMedal ser) {
        for (CommonPb.Medal medal : ser.getMedalList()) {
            if (medal.hasHeroId()) {// ????????????????????????
                Hero hero = heros.get(medal.getHeroId());
                if (hero != null) {
                    int index = medal.hasExt() ? MedalConst.HERO_MEDAL_INDEX_1 : MedalConst.HERO_MEDAL_INDEX_0;
                    hero.modifyMedalKey(index, medal.getKeyId());
                }
            }
            medals.put(medal.getKeyId(), medal.hasExt() ? new RedMedal(medal) : new Medal(medal));
        }
    }

    private void dserProps(SerProp ser) {
        for (CommonPb.Prop prop : ser.getPropList()) {
            props.put(prop.getPropId(), new Prop(prop));
        }
    }

    private void dserBuildQue(SerBuildQue ser) {
        BuildQue que;
        for (CommonPb.BuildQue buildQue : ser.getBuildQueList()) {
            que = new BuildQue(buildQue.getKeyId(), buildQue.getIndex(), buildQue.getBuildingType(), buildQue.getId(),
                    buildQue.getPeriod(), buildQue.getEndTime());
            que.setNewType(buildQue.getNewType());
            que.setFromType(buildQue.getFromType());
            if (buildQue.hasFree()) {
                que.setFree(buildQue.getFree());
            }
            if (buildQue.hasParam()) {
                que.setParam(buildQue.getParam());
            }
            this.buildQue.put(buildQue.getIndex(), que);
        }
    }

    private void dserTasks(SerTask ser) {
        for (CommonPb.Task e : ser.getMajorTaskList()) {
            Task task = new Task(e.getTaskId(), e.getSchedule(), e.getStatus(), 1);
//            majorTasks.put(e.getTaskId(), task);
        }

        for (CommonPb.Task e : ser.getDayiyTaskList()) {
            Task task = new Task(e.getTaskId(), e.getSchedule(), e.getStatus(), 1);
            dailyTask.put(e.getTaskId(), task);
        }
        if (!CheckNull.isEmpty(ser.getDailyIsGetList())) {
            dailyIsGet.addAll(ser.getDailyIsGetList());
        }
        if (ser.hasDailyTaskLivenss()) {
            dailyTaskLivenss = ser.getDailyTaskLivenss();
        }
        for (CommonPb.Task e : ser.getLiveTaskList()) {
            Task task = new Task(e.getTaskId(), e.getSchedule(), e.getStatus(), 1);
            partyTask.put(e.getTaskId(), task);
        }

        for (CommonPb.Task e : ser.getWorldTaskList()) {
            Task task = new Task(e.getTaskId(), e.getSchedule(), e.getStatus(), 1);
            worldTasks.put(e.getTaskId(), task);
        }

        for (CommonPb.Sectiontask e : ser.getSectionList()) {
            Sectiontask ss = new Sectiontask(e);
            sectiontask.add(ss);
        }

        for (CommonPb.Task e : ser.getAdvanceTaskList()) {
            Task task = new Task(e.getTaskId(), e.getSchedule(), e.getStatus(), 1);
            advanceTask.put(e.getTaskId(), task);
        }
    }

    private void dserArmy(SerArmy ser) {
        for (CommonPb.Army army : ser.getArmyList()) {
            armys.put(army.getKeyId(), new Army(army));
        }
    }

    private void dserAcquisition(SerAcquisition ser) {
        acquisiteDate = ser.getCollectDate();
        for (TwoInt twoInt : ser.getRewardList()) {
            acquisiteReward.put(twoInt.getV1(), twoInt.getV2());
        }
        acquisiteQue.addAll(ser.getCollectList());
    }

    private void dserTypeAwards(SerTypeAwards ser) {
        for (TypeAwards objPb : ser.getRewardList()) {
            awards.put(objPb.getType(), objPb.getRewardList());
        }
    }

    private void dserSupEquips(SerSuperEquip ser) {
        for (CommonPb.SuperEquip objPb : ser.getSuperEquipList()) {
            supEquips.put(objPb.getType(), new SuperEquip(objPb.getType(), objPb.getLv(), objPb.getStep(),
                    objPb.getBomb(), objPb.getGrowLv()));
        }
    }

    private void dserSupEquipQue(SerSuperEquipQue ser) {
        supEquipQue.addAll(ser.getQueList());
    }

    private void dserRoleOpt(SerRoleOpt ser) {
        opts.addAll(ser.getOptList());
    }

    private void dserWallNpc(SerWallNpc ser) {
        for (CommonPb.WallNpc objPb : ser.getWallNpcList()) {
            wallNpc.put(objPb.getPos(), new WallNpc(objPb.getPos(), objPb.getHeroId(), objPb.getLevel(), objPb.getExp(),
                    objPb.getCount(), objPb.getAutoArmy(), objPb.getAddTime()));
        }
    }

    private void dserEffect(SerEffects ser) {
        for (CommonPb.Effect objPb : ser.getEffectList()) {
            effects.put(objPb.getId(), new Effect(objPb.getId(), objPb.getVal(), objPb.getEndTime()));
        }
    }

    private void dserActivity(SerActivity ser) {
        List<DbActivity> activityList = ser.getActivityList();
        for (DbActivity e : activityList) {
            Activity activity = conActivity(e);
            activitys.put(e.getActivityType(), activity);
        }
        if (ser.hasActBlackhawk()) {
            this.blackhawkAct = new ActBlackhawk(ser.getActBlackhawk());
        }
        List<CommonPb.ActBarton> actBartonList = ser.getActBartonList();
        if (actBartonList != null && !actBartonList.isEmpty()) {
            for (CommonPb.ActBarton barton : actBartonList) {
                this.actBarton.put(barton.getActivityId(), new ActBarton(barton));
            }
        }
        List<RobinHood> robinHoodList = ser.getRobinHoodList();
        if (robinHoodList != null && !robinHoodList.isEmpty()) {
            for (RobinHood robinHood : robinHoodList) {
                this.actRobinHood.put(robinHood.getActivityId(), new ActRobinHood(robinHood));
            }
        }
        List<CommonPb.TwoInt> personalActs = ser.getPersonalActsList();
        this.personalActs = new PersonalActs(personalActs);
    }

    private void dserSignInInfo(SignInInfo ser) {
        this.siginInfo.setDate(ser.getDate());
        this.siginInfo.setLevel(ser.getLevel());
        this.siginInfo.setTimes(ser.getTimes());
        this.siginInfo.setSignIn(ser.getSignIn());
        this.siginInfo.setDoubleReward(ser.getDoubleReward());
        this.siginInfo.setPage(ser.getPage());
    }

    private void dserSignInInfoExt(SerSignInInfo ser) {
        List<SignInInfo> signInInfoList = ser.getSignInInfoList();
        for (SignInInfo s : signInInfoList) {
            int actType = s.getActType();
            this.signInfoMap.put(actType, new SiginInfo(s));
        }
    }

    private void dserCrossData(SerCrossPersonalData ser) {
        if (ser != null) {
            this.crossPersonalData = new CrossPersonalData();
            crossPersonalData.dser(ser);
        }
    }

    private void dserTreasureWares(SerTreasureWares ser) {
        if (ser != null && !ObjectUtils.isEmpty(ser.getTreasureList())) {
            ser.getTreasureList().forEach(treasureWare -> {
                treasureWares.put(treasureWare.getKeyId(), new TreasureWare(treasureWare));
            });
            this.makeTreasureWare.dsePb(ser.getFirstMakeTw());
        }
    }

    /**
     * ????????????Acitivty??????
     *
     * @param e
     * @return
     */
    private Activity conActivity(DbActivity e) {
        Activity activity;
        if (e.getActivityType() == ActivityConst.ACT_LUCKY_TURNPLATE
                || e.getActivityType() == ActivityConst.FAMOUS_GENERAL_TURNPLATE
                || e.getActivityType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW
                || e.getActivityType() == ActivityConst.ACT_LUCKY_TURNPLATE_NEW_YEAR
                || e.getActivityType() == ActivityConst.ACT_SEASON_TURNPLATE
                || e.getActivityType() == ActivityConst.ACT_MAGIC_TREASURE_WARE) {
            activity = new ActTurnplat(e);
        } else if (e.getActivityType() == ActivityConst.ACT_EQUIP_TURNPLATE) {
            activity = new EquipTurnplat(e);
        } else {
            activity = new Activity(e);
        }
        return activity;
    }

    public Map<Integer, Effect> getEffect() {
        refreshEffect();
        return effects;
    }

    /**
     * ??????buff??????
     */
    public void refreshEffect() {
        try {
            boolean needReCalcAttr = false;
            int now = TimeHelper.getCurrentSecond();
            Iterator<Entry<Integer, Effect>> it = effects.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, Effect> next = it.next();
                Effect ef = next.getValue();
                if (now >= ef.getEndTime()) {
                    if (ef.getEffectType() == EffectConstant.ATK_MUT || ef.getEffectType() == EffectConstant.DEF_MUT
                            || ef.getEffectType() == EffectConstant.PREWAR_ATK
                            || ef.getEffectType() == EffectConstant.PREWAR_DEF
                            || ef.getEffectType() == EffectConstant.PREWAR_LEAD
                            || ef.getEffectType() == EffectConstant.PREWAR_ATTACK_EXT) {
                        needReCalcAttr = true;
                    }
                    it.remove();
                }
            }
            // ????????????????????????
            if (needReCalcAttr) {
                CalculateUtil.reCalcAllHeroAttr(this);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        // Map<Integer, Effect> rm = null;
        // for (Entry<Integer, Effect> kv : effects.entrySet()) {
        // if (now >= kv.getValue().getEndTime()) {
        // if (rm == null) {
        // rm = new HashMap<>();
        // }
        // rm.put(kv.getKey(), kv.getValue());
        // }
        // }
        // if (rm != null) {
        // for (int key : rm.keySet()) {
        // effects.remove(key);
        // }
        // }
    }

    public Map<Integer, Effect> rmEffect(Integer effectType) {
        refreshEffect();
        effects.remove(effectType);
        return effects;
    }

    public void setAtkCityAct(DbAtkCityAct atkCityAct) {
        for (StrInt v : atkCityAct.getTankTypesList()) {
            this.atkCityAct.getTankTypes().put(v.getV1(), v.getV2());
        }
        for (DbDay7ActStatus v : atkCityAct.getTypeCntList()) {
            Map<Integer, Integer> map = this.atkCityAct.getTypeCnt().get(v.getKey());
            if (map == null) {
                map = new HashMap<>();
                this.atkCityAct.getTypeCnt().put(v.getKey(), map);
            }
            for (TwoInt vv : v.getStatusList()) {
                map.put(vv.getV1(), vv.getV2());
            }
        }
        for (TwoInt v : atkCityAct.getStatusList()) {
            this.atkCityAct.getStatus().put(v.getV1(), v.getV2());
        }
        for (TwoInt v : atkCityAct.getStatusCntList()) {
            this.atkCityAct.getStatusCnt().put(v.getV1(), v.getV2());
        }
        for (Integer v : atkCityAct.getCanRecvKeyIdList()) {
            this.atkCityAct.getCanRecvKeyId().add(v);
        }

    }

    /**
     * ??????????????????????????????npc??????????????????
     *
     * @param cityId ??????id
     * @return true ???????????????false ?????????
     */
    public boolean checkNpcFirstKillReward(int cityId) {
        if (npcCityFirstKillReward.contains(cityId)) {
            return false;
        }
        return true;
    }

    /**
     * ?????????????????????npc??????????????????
     *
     * @param cityId ??????id
     */
    public void addNpcFirstKillRecord(int cityId) {
        npcCityFirstKillReward.add(cityId);
    }

    /**
     * ????????????????????????????????????
     *
     * @return ??????????????????
     */
    public boolean canPushActPower() {
        //  ??????????????????????????????3???
        return getPushRecord(PushConstant.OFF_LINE_ONE_DAY) != null && getPushRecord(PushConstant.OFF_LINE_ONE_DAY) < 3;
    }

    /**
     * ????????????????????????????????????(???????????????)
     *
     * @param now ???????????????
     * @return ??????????????????
     */
    public boolean canPushOffLine(int now) {
        Integer status = getPushRecord(PushConstant.OFF_LINE_ONE_DAY);
        int canPushTime = now - (24 * TimeHelper.HOUR_S);
        // ??????????????????24??????, ???????????????????????????
        return status != null && lord.getOffTime() < canPushTime && status < 3;
    }

    /**
     * ??????LordId????????????
     *
     * @return ???lordId??????????????????????????????????????????
     */
    public String getName() {
        long lordId = lord.getLordId();
        String initName = DataResource.ac.getBean(PlayerService.class).getInitName();
        if (lordId != 0) {
            String sLordId = String.valueOf(lordId);
            // ?????????id
            String subStr = sLordId.substring(sLordId.length() - 8);
            // ???????????????#??????. ?????????emoji
            // ??????????????????&??????. ??????????????????
            return initName + Integer.toHexString(Integer.parseInt(subStr));
        }
        return null;
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param now ??????????????????
     * @return ?????????????????????
     */
    public String getName(int now) {
        String initName = DataResource.ac.getBean(PlayerService.class).getInitName();
        return initName + Integer.toHexString(now);
    }

    public Map<Integer, Integer> getOwnCastleSkinStar() {
        return ownCastleSkinStar;
    }

    public void setOwnCastleSkinStar(Map<Integer, Integer> ownCastleSkinStar) {
        this.ownCastleSkinStar = ownCastleSkinStar;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????
     *
     * @param skinId ????????????id
     * @return ????????????
     */
    public int getCastleSkinStarById(int skinId) {
        DressUpDataManager dataManager = DataResource.ac.getBean(DressUpDataManager.class);
        Map<Integer, BaseDressUpEntity> castleSkinMap = dataManager.getDressUpByType(this, AwardType.CASTLE_SKIN);
        int star = StaticLordDataMgr.getCastleSkinMapById(skinId).getStar();
        if (!CheckNull.isEmpty(castleSkinMap)) {
            BaseDressUpEntity dressUpEntity = castleSkinMap.get(skinId);
            if (Objects.nonNull(dressUpEntity)) {
                CastleSkinEntity castleSkinEntity = (CastleSkinEntity) dressUpEntity;
                star = castleSkinEntity.getStar();
            }
        }
        return star;
    }

    public int getCamp() {
        return lord.getCamp();
    }

    public long getLordId() {
        return lord.getLordId();
    }

    public int getSandTableScore() {
        return sandTableScore;
    }

    public void setSandTableScore(int sandTableScore) {
        this.sandTableScore = sandTableScore;
    }

    public Map<Integer, Integer> getSandTableBought() {
        return sandTableBought;
    }

    public void setSandTableBought(Map<Integer, Integer> sandTableBought) {
        this.sandTableBought = sandTableBought;
    }

    public PlayerOnHook getPlayerOnHook() {
        return playerOnHook;
    }

    public void setPlayerOnHook(PlayerOnHook playerOnHook) {
        this.playerOnHook = playerOnHook;
    }

    public PlayerSeasonData getPlayerSeasonData() {
        return playerSeasonData;
    }

    public void addMail(Mail mail, Report report) {
        mails.put(mail.getKeyId(), mail);
        addReport(mail.getMoldId(), report, mails);
    }

    public void addReport(int moldId, Report report, Map<Integer, Mail> mails) {
        if (CheckNull.isNull(report)) {
            return;
        }
        getMailReportMap(moldId).addReport(report, mails);
    }

    public MailReportMap getMailReportMap(int moldId) {
        MailReportMap reportQueue = mailReport.get(moldId);
        if (ObjectUtils.isEmpty(reportQueue)) {
            synchronized (mailReport) {
                reportQueue = mailReport.get(moldId);
                if (ObjectUtils.isEmpty(reportQueue)) {
                    reportQueue = new MailReportMap();
                    mailReport.put(moldId, reportQueue);
                }
            }
        }

        return reportQueue;
    }

    public Report getMailReport(Mail mail) {
        MailReportMap mailReportMap = getMailReportMap(mail.getMoldId());
        if (Objects.nonNull(mailReportMap)) {
            return mailReportMap.getReport(mail.getKeyId());
        }

        return null;
    }

    public void expiredMail(Map<Integer, List<Integer>> delMailIds) {
        if (ObjectUtils.isEmpty(delMailIds)) {
            return;
        }

        delMailIds.forEach((moldId, list) -> {
            MailReportMap mailReportMap = getMailReportMap(moldId);
            if (Objects.nonNull(mailReportMap)) {
                mailReportMap.expiredMail(list);
            }
        });
    }

    public void expiredMailReport(Map<Integer, List<Integer>> delMailIds) {
        if (ObjectUtils.isEmpty(delMailIds)) {
            return;
        }

        delMailIds.forEach((moldId, list) -> {
            MailReportMap mailReportMap = getMailReportMap(moldId);
            if (Objects.nonNull(mailReportMap)) {
                mailReportMap.expiredReport(list, mails);
            }
        });
    }

    public TotemData getTotemData() {
        return totemData;
    }

    public TreasureChallengePlayer getTreasureChallengePlayer() {
        return treasureChallengePlayer;
    }

    public void setTreasureChallengePlayer(TreasureChallengePlayer treasureChallengePlayer) {
        this.treasureChallengePlayer = treasureChallengePlayer;
    }

    public PersonalActs getPersonalActs() {
        return personalActs;
    }

    public void setPersonalActs(PersonalActs personalActs) {
        this.personalActs = personalActs;
    }

    public PlayerRelic getPlayerRelic() {
        return playerRelic;
    }

    // ????????????????????????????????????
    public List<CommonPb.LifeSimulatorRecord> createSimulatorRecordList() {
        List<CommonPb.LifeSimulatorRecord> data = new ArrayList<>();

        // ????????????????????????
        if (CheckNull.isEmpty(this.getLifeSimulatorRecordMap())) {
            this.setLifeSimulatorRecordMap(new HashMap<>());
        }
        for (Entry<Integer, List<LifeSimulatorInfo>> entry : this.lifeSimulatorRecordMap.entrySet()) {
            CommonPb.LifeSimulatorRecord.Builder lifeSimulatorRecordBuilder = CommonPb.LifeSimulatorRecord.newBuilder();
            lifeSimulatorRecordBuilder.setTriggerMode(entry.getKey());
            for (LifeSimulatorInfo lifeSimulatorInfo : entry.getValue()) {
                if (lifeSimulatorInfo.getDelay() > 0) {
                    // ???????????????????????????????????????
                    continue;
                }
                CommonPb.LifeSimulatorInfo lifeSimulatorInfoPb = lifeSimulatorInfo.ser();
                lifeSimulatorRecordBuilder.addLifeSimulatorInfo(lifeSimulatorInfoPb);
            }
            data.add(lifeSimulatorRecordBuilder.build());
        }

        // ?????????????????????
        if (CheckNull.isNull(this.getCityEvent())) {
            CityEvent cityEvent = new CityEvent();
            this.setCityEvent(cityEvent);
        }
        CommonPb.LifeSimulatorRecord.Builder lifeSimulatorRecordBuilder = CommonPb.LifeSimulatorRecord.newBuilder();
        lifeSimulatorRecordBuilder.setTriggerMode(3);
        for (LifeSimulatorInfo lifeSimulatorInfo : this.cityEvent.getLifeSimulatorInfoList()) {
            if (lifeSimulatorInfo.getDelay() > 0) {
                // ???????????????????????????????????????
                continue;
            }
            CommonPb.LifeSimulatorInfo lifeSimulatorInfoPb = lifeSimulatorInfo.ser();
            lifeSimulatorRecordBuilder.addLifeSimulatorInfo(lifeSimulatorInfoPb);
        }
        data.add(lifeSimulatorRecordBuilder.build());
        return data;
    }
}
