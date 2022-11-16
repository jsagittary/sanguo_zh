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
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneUpgrade;
import com.gryphpoem.game.zw.resource.pojo.BuildingState;
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
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.EconomicOrder;
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
 * @Description: 玩家对象, 主要存放个人数据, 比如装备{@link Player#equips}
 */
public class Player {

    /**
     * 角色基础数据
     */
    public Lord lord;

    /**
     * 角色账号
     */
    public Account account;

    /**
     * 角色资源数据
     */
    public Resource resource;

    /**
     * 角色通用显示数据
     */
    public Common common;

    /**
     * 建筑
     */
    public Building building;

    /**
     * 科技
     */
    public Tech tech;

    /**
     * 商城
     */
    public Shop shop;

    /**
     * 化工厂
     */
    public Chemical chemical;

    /**
     * 聚宝盆
     */
    public Treasure treasure;

    /**
     * 情报部
     */
    private Cia cia;

    /**
     * 武将相关
     */
    public PlayerHero playerHero;

    /**
     * 教官相关
     */
    private MentorInfo mentorInfo = new MentorInfo();

    private volatile int maxKey;

    /**
     * 宝石相关
     */
    private StoneInfo stoneInfo = new StoneInfo();

    /**
     * 兵营招募扩建
     */
    public Map<Integer, Factory> factory = new ConcurrentHashMap<>();

    /**
     * 成就
     */
    public Map<Integer, Integer> trophy = new ConcurrentHashMap<>();

    /**
     * 普通副本(combatId,Obj)
     */
    public HashMap<Integer, Combat> combats = new HashMap<>();
    /**
     * 普通副本掉落详情
     */
    public CombatInfo combatInfo = new CombatInfo();
    /**
     * 高级副本(combatId,Obj)
     */
    public HashMap<Integer, CombatFb> combatFb = new HashMap<>();
    /**
     * 宝石副本 key:combatId
     */
    public HashMap<Integer, StoneCombat> stoneCombats = new HashMap<>();
    /**
     * 挑战荣耀演副本 <type,PitchCombat>
     */
    private Map<Integer, PitchCombat> pitchCombats = new HashMap<>();
    /**
     * 多人副本
     */
    private MultCombat multCombat;

    /**
     * 已完成的记录(类型1兵工厂官员招募记录，记录)
     */
    public HashMap<Integer, List<History>> typeInfo = new HashMap<>();

    /**
     * 奖励(类型,奖励) 重建家园
     */
    public HashMap<Integer, List<Award>> awards = new HashMap<>();
    /**
     * 是否为高级重建家园 true为高级
     */
    private boolean isAdvanceAward;
    /**
     * 建筑队列
     */
    public Map<Integer, BuildQue> buildQue = new ConcurrentHashMap<>();
    /**
     * 非资源建筑的额外属性,key:建筑Id
     */
    public Map<Integer, BuildingExt> buildingExts = new ConcurrentHashMap<>();
    /**
     * 打造队列
     */
    public LinkedList<EquipQue> equipQue = new LinkedList<>();
    /**
     * 城外工厂
     */
    public Map<Integer, Mill> mills = new ConcurrentHashMap<>();
    /**
     * 司令部，兵工厂官员招募
     */
    public Map<Integer, Gains> gains = new ConcurrentHashMap<>();
//    /**
//     * 主线支线,剧情任务
//     */
//    public Map<Integer, Task> majorTasks = new ConcurrentHashMap<>();
//    /**
//     * 当前显示的支线任务id,此值不会被序列化
//     */
//    public List<Integer> curMajorTaskIds = new ArrayList<>();

    /**
     * 日常任务
     */
    private Map<Integer, Task> dailyTask = new HashMap<>();
    /**
     * 日常任务已经领取奖励的id
     */
    private Set<Integer> dailyIsGet = new HashSet<>();
    /**
     * 日常任务的活跃度
     */
    private int dailyTaskLivenss;

    // 个人目标
    private Map<Integer, Task> advanceTask = new HashMap<>();

    /**
     * 军团任务
     */
    public Map<Integer, Task> partyTask = new HashMap<>();
    /**
     * 世界任务
     */
    public Map<Integer, Task> worldTasks = new HashMap<>();
    /**
     * 剧情任务的章节
     */
    public List<Sectiontask> sectiontask = new ArrayList<>();

    /**
     * 玩家将领
     */
    public Map<Integer, Hero> heros = new HashMap<>();
    /**
     * 上阵将领，记录上阵将领的id，0位为补位，1-4位为上阵将领id
     */
    public int[] heroBattle = new int[HeroConstant.HERO_BATTLE_LEN + 1];

    /**
     * 防守将领, 记录防守将领的id, 0位为补位，1-4位为上阵将领id
     */
    public int[] heroDef = new int[HeroConstant.HERO_BATTLE_LEN + 1];
    /**
     * 上阵将领在其他位置的映射(基于上阵将领为基础,存储的是位置,而非将领id) key:2表示副本 key:3出征将领
     */
    public Map<Integer, List<Integer>> heroBattlePos = new HashMap<>();
    /**
     * 城防将领
     */
    public int[] heroWall = new int[HeroConstant.HERO_BATTLE_LEN + 1];
    /**
     * 采集将领
     */
    public int[] heroAcq = new int[HeroConstant.HERO_BATTLE_LEN + 1];
    /**
     * 特攻将领
     */
    public int[] heroCommando = new int[Constant.ACQ_HERO_REQUIRE.size() + 1];
    /**
     * 道具
     */
    public Map<Integer, Prop> props = new HashMap<>();
    /**
     * 所拥有的头像
     */
    public Set<Integer> portraits = new HashSet<>();
    /**
     * 拥有的聊天气泡
     */
    private Set<Integer> chatBubbles = new HashSet<>();
    /**
     * 当前的聊天气泡
     */
    private int curChatBubble;
    /**
     * 拥有的形象
     */
    private Set<Integer> bodyImages = new HashSet<>();
    /**
     * 当前选中的形象
     */
    private int curBodyImage;
    /**
     * 装备
     */
    public Map<Integer, Equip> equips = new HashMap<>();
    /**
     * 宝具
     */
    public Map<Integer, TreasureWare> treasureWares = new HashMap<>();
    /**
     * 宝具配置表id打造次数
     */
    public Map<Integer, Integer> treasureWareIdMakeCount = new HashMap<>();
    /**
     * 装备宝石, key: keyId
     */
    public Map<Integer, EquipJewel> equipJewel = new HashMap<>();

    /**
     * 战机, key: planeType
     */
    public Map<Integer, WarPlane> warPlanes = new HashMap<>();
    /**
     * 战机碎片, key: chipId
     */
    public Map<Integer, PlaneChip> palneChips = new HashMap<>();

    /**
     * 勋章
     */
    public Map<Integer, Medal> medals = new HashMap<>();

    /**
     * 超级武器(国器)
     */
    public Map<Integer, SuperEquip> supEquips = new HashMap<>();

    /**
     * 超级武器队列(国器)
     */
    public LinkedList<TwoInt> supEquipQue = new LinkedList<>();

    /**
     * 行军队列
     */
    public Map<Integer, Army> armys = new HashMap<>();

    /**
     * 邮件
     */
    public Map<Integer, Mail> mails = new ConcurrentHashMap<>();

    /**
     * 邮件战报 (moldId, Queue<战报>)
     */
    public Map<Integer, MailReportMap> mailReport = new ConcurrentHashMap<>();

    /**
     * 操作记录
     */
    public LinkedList<RoleOpt> opts = new LinkedList<>();

    /**
     * 城墙NPC,key:posId
     */
    public Map<Integer, WallNpc> wallNpc = new ConcurrentHashMap<>();

    /**
     * 效果
     */
    private Map<Integer, Effect> effects = new ConcurrentHashMap<>();

    /**
     * 玩家参与的城战或阵营战记录, key:pos, value:battleId, 同一个位置可以同一个玩家同事发起和加入多个战斗
     */
    public Map<Integer, HashSet<Integer>> battleMap = new HashMap<>();

    /**
     * 个人资源点最后采集日期
     */
    public int acquisiteDate;

    /**
     * 个人资源点已采集次数记录
     */
    public Map<Integer, Integer> acquisiteReward = new HashMap<>();

    /**
     * 个人资源点采集队列
     */
    public List<TwoInt> acquisiteQue = new LinkedList<>();

    /**
     * 任务信息 key:activityType
     */
    public Map<Integer, Activity> activitys = new HashMap<>();

    /**
     * 好友列表,key:好友的 lordId
     */
    public Map<Long, DbFriend> friends = new HashMap<>();

    /**
     * 徒弟列表 key:徒弟的 lordId
     */
    public Map<Long, DbMasterApprentice> apprentices = new HashMap<>();

    /**
     * 功能卡 key:cardType
     */
    public Map<Integer, FunCard> funCards = new HashMap<>();

    /**
     * 师傅, null说明没有师傅
     */
    public DbMasterApprentice master;

    /**
     * 黑名单列表
     */
    private List<Long> blacklist;

    /**
     * 已领取奖励的id(师徒奖励)
     */
    public Map<Integer, Integer> awardedIds = new HashMap<>();

    /**
     * 已购买过的商品id(积分兑换)
     */
    public Set<Integer> boughtIds = new HashSet<>();

    /**
     * 7日活动
     */
    public Day7Act day7Act = new Day7Act();

    /**
     * 攻城掠地活动
     */
    public AtkCityAct atkCityAct = new AtkCityAct();

    /**
     * 黑鹰计划活动
     */
    public ActBlackhawk blackhawkAct = new ActBlackhawk();

    /**
     * 巴顿兑换活动 [key:activityId]
     */
    public Map<Integer, ActBarton> actBarton = new HashMap<>();

    /**
     * 罗宾汉活动
     */
    public Map<Integer, ActRobinHood> actRobinHood = new HashMap<>();

    /**
     * 特殊道具(具备唯一性) key:type ,val:specialId
     */
    public Map<Integer, Set<Integer>> specialProp = new HashMap<>();

    /**
     * 内阁相关数据
     */
    public Cabinet cabinet;

    /**
     * 召唤的信息
     */
    public Summon summon;

    /**
     * 推送信息记录，这里的value不用boolean类型，是为复杂功能留下扩展空间
     */
    private Map<String, Integer> pushRecords = new HashMap<>();

    /**
     * 挑战副本的阵容
     */
    public List<Integer> combatHeroForm = new ArrayList<>();

    /**
     * 本次登陆 进入世界的次数
     */
    public int enterWorldCnt;

    /**
     * 玩家首次金币翻倍记录
     */
    public Set<String> firstPayDouble = new CopyOnWriteArraySet<>();

    /**
     * 每日攻打流寇上线
     */
    private int banditCnt;

    /**
     * 是否为失火状态,true表示失火状态
     */
    private boolean isFireState;

    /**
     * 夜袭流寇数量
     */
    private int nightRaidBanditCnt;

    /**
     * 被击飞次数
     */
    private int hitFlyCount;

    /**
     * 是否离线被击飞
     */
    private boolean isOffOnlineHitFly;

    /**
     * 触发式礼包信息,key:triggerId
     */
    public Map<Integer, Map<Integer, TriggerGift>> triggerGifts = new HashMap<>();

    /**
     * 客户端的包id值
     */
    private String packId;

    /**
     * 充值总金额
     */
    private int paySumAmoumt = 0;

    /**
     * 杂七杂八的 数据记录<br>
     * key值参考{@link PlayerConstant}
     */
    private Map<Integer, Integer> mixtureData = new HashMap<>();

    /**
     * 最后世界发言
     */
    public LinkedList<String> lastChats = new LinkedList<>();

    /**
     * 柏林会战获得的军费
     */
    private int militaryExpenditure = 0;

    /** 荣耀日报红点数 */
    /** private int honorReportTips = 0; */

    /**
     * 装备打造 秘技触发概率
     */
    public int[] cheatCode;

    /**
     * 离线升级的建筑 key 建筑id
     */
    public Map<Integer, OffLineBuild> offLineBuilds = new HashMap<>();

    /**
     * 勋章商店-商品
     */
    public List<Integer> medalGoods;

    /**
     * 每日勋章商店刷新次数
     */
    private int medalGoodsRefNum;

    /**
     * 每日维和部队 特技触发次数
     */
    private int peacekeepingForcesNum;

    /**
     * 勋章特技 军功显赫 上一次触发时间
     */
    private int jGXHLastTime;

    /**
     * 匪军叛乱的个人信息
     */
    private PlayerRebellion playerRebellion;

    /**
     * 每日攻击同阵营采集次數
     */
    private int collectMineCount;

    /**
     * 决战相关信息
     */
    private DecisiveInfo decisiveInfo = new DecisiveInfo();
    /**
     * 签到数据
     */
    public SiginInfo siginInfo = new SiginInfo();
    /**
     * 签到扩展
     */
    public Map<Integer, SiginInfo> signInfoMap = new HashMap<>();
    /**
     * 飞艇的个人数据
     */
    private AirshipPersonData airshipPersonData;
    /**
     * 补给记录
     */
    private SupplyRecord supplyRecord = new SupplyRecord();

    /**
     * 城堡皮肤
     */
    private Set<Integer> ownCastleSkin = new HashSet<>();

    /**
     * 当前的皮肤
     */
    private int curCastleSkin = StaticCastleSkin.DEFAULT_SKIN_ID;

    /**
     * 城堡皮肤存在的时间
     */
    private Map<Integer, Integer> ownCastleSkinTime = new HashMap<>();

    /**
     * 城堡皮肤星数
     */
    private Map<Integer, Integer> ownCastleSkinStar = new HashMap<>();

    /**
     * 跨服的个人数据
     */
    private CrossPersonalData crossPersonalData;

    /**
     * 世界争霸 玩家数据
     */
    private PlayerWorldWarData playerWorldWarData = new PlayerWorldWarData();

    /**
     * 新跨服的个人数据
     */
    public CrossPlayerLocalData crossPlayerLocalData = new CrossPlayerLocalData();

    /**
     * 纽约争霸数据
     */
    public PlayerNewYorkWar newYorkWar = new PlayerNewYorkWar();

    /**
     * 记录触发过首杀的npc城池
     */
    private List<Integer> npcCityFirstKillReward = new ArrayList<>();

    /**
     * 战令活动的个人数据
     */
    private BattlePassPersonInfo battlePassPersonInfo = new BattlePassPersonInfo();

    /**
     * 沙盘演武积分
     */
    private int sandTableScore;

    private Map<Integer, Integer> sandTableBought = new HashMap<>();

    //叛军等级记录
    private PlayerOnHook playerOnHook = new PlayerOnHook();
    /**
     * 装扮
     */
    private DressUp dressUp = new DressUp();
    /**
     * 玩家赛季数据
     */
    private PlayerSeasonData playerSeasonData = new PlayerSeasonData();
    /**
     * 钓鱼数据
     */
    private FishingData fishingData = new FishingData();
    /**
     * 图腾数据
     */
    private TotemData totemData = new TotemData();

    /**
     * 小游戏
     */
    private SmallGame smallGame;

    private RpcPlayer rpcPlayer;

    /**
     * 坐标行军类型
     */
    private Map<Integer, Integer> marchType = new HashMap<>();

    /**
     * 宝具副本
     */
    private TreasureCombat treasureCombat = new TreasureCombat();
    /**
     * 宝具挑战玩家
     */
    private TreasureChallengePlayer treasureChallengePlayer = new TreasureChallengePlayer();

    /**
     * 招募奖励 v1: 对应s_system中id=1102的索引位置, v2: 1 已领取、0 未领取
     */
    private Map<Integer, Integer> recruitReward = new HashMap<>(5);

    /**
     * 玩家抽卡详情
     */
    private DrawCardData drawCardData = new DrawCardData();

    /**
     * 遗迹
     */
    private PlayerRelic playerRelic = new PlayerRelic();
    /**
     * 章节任务
     */
    public ChapterTask chapterTask = new ChapterTask();


    /**
     * 玩家抽卡活动详情
     */
    private PlayerFunctionPlanData functionPlanData = new PlayerFunctionPlanData();

    public PlayerFunctionPlanData getFunctionPlanData() {
        return functionPlanData;
    }


    private PersonalActs personalActs = new PersonalActs();

    /**
     * 人生模拟器记录, 非城镇事件的模拟器信息记录在这<br>
     * <触发方式, 模拟器信息>
     */
    private Map<Integer, List<LifeSimulatorInfo>> lifeSimulatorRecordMap = new HashMap<>();

    public Map<Integer,  List<LifeSimulatorInfo>> getLifeSimulatorRecordMap() {
        return lifeSimulatorRecordMap;
    }

    public void setLifeSimulatorRecordMap(Map<Integer, List<LifeSimulatorInfo>> lifeSimulatorRecordMap) {
        this.lifeSimulatorRecordMap = lifeSimulatorRecordMap;
    }

    /**
     * 周期性城镇事件
     */
    private CityEvent cityEvent = new CityEvent();

    public CityEvent getCityEvent() {
        return cityEvent;
    }

    public void setCityEvent(CityEvent cityEvent) {
        this.cityEvent = cityEvent;
    }

    /**
     * 性格：1-武断; 2-多疑; 3-感性; 4-强硬; 5-尚武; 6-崇文 <br>
     * <性格id, 性格值>
     */
    private Map<Integer, Integer> characterData = new HashMap<>(6);

    public Map<Integer, Integer> getCharacterData() {
        return characterData;
    }

    public void setCharacterData(Map<Integer, Integer> characterData) {
        this.characterData = characterData;
    }

    /**
     * 性格奖励记录 <br>
     * <奖励配置id(对应sim_character_reward的id), 是否已获取(1-是; 0-否)>
     */
    private Map<Integer, Integer> characterRewardRecord = new HashMap<>(8);

    public Map<Integer, Integer> getCharacterRewardRecord() {
        return characterRewardRecord;
    }

    public void setCharacterRewardRecord(Map<Integer, Integer> characterRewardRecord) {
        this.characterRewardRecord = characterRewardRecord;
    }

    /**
     * 侦察兵<br>
     * key: 侦察兵标号; value: 侦察兵状态(0-空闲; 1-任务中)
     */
    private Map<Integer, Integer> scoutData = new HashMap<>();

    public Map<Integer, Integer> getScoutData() {
        return scoutData;
    }

    public void setScoutData(Map<Integer, Integer> scoutData) {
        this.scoutData = scoutData;
    }

    /**
     * 已探索的迷雾格子, key: 格子id; value: 是否已开垦, 1-是, 0-否
     */
    private Map<Integer, List<Integer>> mapCellData = new ConcurrentHashMap<>();

    public Map<Integer, List<Integer>> getMapCellData() {
        return mapCellData;
    }

    public void setMapCellData(Map<Integer, List<Integer>> mapCellData) {
        this.mapCellData = mapCellData;
    }

    /**
     * 已开垦的地基, 记录对应的地基id
     */
    private List<Integer> foundationData = new ArrayList<>();

    public List<Integer> getFoundationData() {
        return foundationData;
    }

    /**
     * 居民数量, [总数, 空闲数, 上限]
     */
    private List<Integer> residentData = new ArrayList<>(3);

    public List<Integer> getResidentData() {
        return residentData;
    }

    public Integer getResidentTotalCnt() {
        return residentData.get(0);
    }

    public void addResidentTotalCnt(int count) {
        this.residentData.set(0, getResidentTotalCnt() + count);
    }

    public void subResidentTotalCnt(int count) {
        int newTotalCount = Math.max(getResidentTotalCnt() - count, 0);
        this.residentData.set(0, newTotalCount);
    }

    public Integer getIdleResidentCnt() {
        return residentData.get(1);
    }

    public void addIdleResidentCnt(int count) {
        this.residentData.set(1, getIdleResidentCnt() + count);
    }

    public void subIdleResidentCnt(int count) {
        int newIdleCount = Math.max(getIdleResidentCnt() - count, 0);
        this.residentData.set(1, newIdleCount);
    }

    // /**
    //  * 探索队列
    //  */
    // private Map<Integer, ExploreQue> exploreQue = new ConcurrentHashMap<>();
    //
    // public Map<Integer, ExploreQue> getExploreQue() {
    //     return exploreQue;
    // }
    //
    // /**
    //  * 开垦队列
    //  */
    // private Map<Integer, ReclaimQue> reclaimQue = new ConcurrentHashMap<>();
    //
    // public Map<Integer, ReclaimQue> getReclaimQue() {
    //     return reclaimQue;
    // }

    /**
     * 幸福度
     */
    private int happiness;

    public int getHappiness() {
        return happiness;
    }

    public void setHappiness(int happiness) {
        this.happiness = happiness;
    }

    /**
     * 增加幸福度
     * @param count
     */
    public void addHappiness(int count) {
        this.happiness += count;
    }

    /**
     * 减少幸福度
     * @param count
     */
    public void subHappiness(int count) {
        this.happiness -= count;
    }

    /**
     * 建筑状态信息, key-建筑id
     */
    private Map<Integer, BuildingState> buildingData = new HashMap<>();

    public Map<Integer, BuildingState> getBuildingData() {
        return buildingData;
    }

    public void setBuildingData(Map<Integer, BuildingState> buildingData) {
        this.buildingData = buildingData;
    }

    /**
     * 已解锁的经济副作物, 作物id
     */
    private List<Integer> unlockEconomicCrops = new ArrayList<>();

    public List<Integer> getUnlockEconomicCrops() {
        return unlockEconomicCrops;
    }

    public void setUnlockEconomicCrops(List<Integer> unlockEconomicCrops) {
        this.unlockEconomicCrops = unlockEconomicCrops;
    }

    /**
     * 订单数上限（不包含预显示订单）
     */
    private int economicOrderMaxCnt;

    public int getEconomicOrderMaxCnt() {
        return economicOrderMaxCnt;
    }

    public void setEconomicOrderMaxCnt(int economicOrderMaxCnt) {
        this.economicOrderMaxCnt = economicOrderMaxCnt;
    }

    /**
     * 可提交的订单
     */
    private Map<Integer, EconomicOrder> canSubmitOrderData = new HashMap<>();

    public Map<Integer, EconomicOrder> getCanSubmitOrderData() {
        return canSubmitOrderData;
    }

    public void setCanSubmitOrderData(Map<Integer, EconomicOrder> canSubmitOrderData) {
        this.canSubmitOrderData = canSubmitOrderData;
    }

    /**
     * 预显示的订单
     */
    private Map<Integer, EconomicOrder> preDisplayOrderData = new HashMap<>();

    public Map<Integer, EconomicOrder> getPreDisplayOrderData() {
        return preDisplayOrderData;
    }

    public void setPreDisplayOrderData(Map<Integer, EconomicOrder> preDisplayOrderData) {
        this.preDisplayOrderData = preDisplayOrderData;
    }

    /**
     * 安民济物记录, key-具体类型; value-最近一次记录时间(秒)
     */
    private Map<Integer, Long> peaceAndWelfareRecord = new HashMap<>(2);

    /**
     * 是否第一次打造宝具
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
     * 这个roleId是否在黑名单列表中
     *
     * @param blacklistRoleId
     * @return true 在黑名单中
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
    // 角色id，所有区服上唯一
    public Long roleId;

    public int surface;

    public int lastSaveTime;

    public boolean isLogin = false;

    public boolean immediateSave = false;

    public ChannelHandlerContext ctx;

    public SaveFlag saveFlag = new SaveFlag();

    public int chatTime;// 记录玩家上次发送聊天的时间

    public int pChatTime;// 记录玩家上次私聊送聊天的时间

    public int pCampMailTime;// 记录玩家上次发送阵营邮件的时间

    public int pRelicChatTime;// 记录玩家上次发送遗迹聊天的时间

    public boolean isTester = false;

    public int appointFreeTime;// 玩家是司令免费任职刷新时间

    public boolean isRobot;// 记录是否是机器人

    public RobotRecord robotRecord;// 记录一些机器人特有的记录量

    public int heroSkin;// 特殊将领的皮肤

    public int loginRewardTime;// 最近一次登陆奖励发放时间

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
        // 移除焦点
        EventBus.getDefault().post(new Events.RmMapFocusEvent(this));
        EventBus.getDefault().post(new Events.PlayerLoginLogoutEvent(this, false)); // 退出登陆

        LogLordHelper.logLogin(this);
        LogLordHelper.loginLong(this);// 在线时长
        LogLordHelper.commonLog("fightingChange", AwardFrom.COMMON, this, lord.getFight()); // 战斗力埋点
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
        // 移除焦点
        // EventBus.getDefault().post(new Events.RmMapFocusEvent(this));
    }

    public void logIn() {
        int now = TimeHelper.getCurrentSecond();
        int nowDay = TimeHelper.getCurrentDay();

        int lastDay = TimeHelper.getDay(lord.getOnTime());
        if (nowDay != lastDay) {
            // 重置每月登录天数
            int monthAndDay = TimeHelper.getMonthAndDay(new Date());
            if ((lord.getOlMonth() / 10000) != monthAndDay / 10000) {// 月份不一样
                lord.setOlMonth(monthAndDay + 1);
            } else {// 月份一样
                if (lord.getOlMonth() / 100 != monthAndDay / 100) {
                    lord.setOlMonth(monthAndDay + lord.getOlMonth() % 100 + 1);
                }
            }

            int ctTime = lord.getCtTime();
            if (TimeHelper.getDay(ctTime) != TimeHelper.getDay(now)) {
                lord.setCtTime(now);
                lord.setOlAward(0);
            }

            // 重置前一次的登录时长
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
     * 在线时长
     *
     * @return
     */
    public int onLineTime() {
        int now = TimeHelper.getCurrentSecond();
        int nowDay = TimeHelper.getCurrentDay();

        int lastDay = TimeHelper.getDay(lord.getOnTime());
        if (nowDay != lastDay) {// 登录时间不为当天,则取0点到当前时间
            int noTime = TimeHelper.getTodayZone(now);
            int ctDay = TimeHelper.getDay(lord.getCtTime());
            if (ctDay != nowDay) {
                lord.setCtTime(noTime);
                lord.setOlAward(0);
            }
            return now - noTime;
        } else {// 登录时间为当天,则取累积时长
            int onlineTime = lord.getOlTime() + now - lord.getOnTime();
            onlineTime = onlineTime > 86400 ? 86400 : onlineTime;
            return onlineTime;
        }
    }

    public boolean isActive() {
        if (account == null) {
            // 说明lord存在，但account不存在其不在smallid表中。出现这种情况是因为手动关联了lord产生的多余数据没有处理
            // 将其加入到smallId中即可
            return false;
        }

        // return account.getCreated() == 1 && lord.getLevel() > 2;
        return account.getCreated() == 1 && lord.getLevel() > 0;
    }

    /**
     * 根据碎片id获取, 碎片信息
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
     * 检测战机是否存在
     *
     * @param planeId
     * @return
     * @throws MwException
     */
    public WarPlane checkWarPlaneIsExist(int planeId) throws MwException {
        StaticPlaneUpgrade planeUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
        if (CheckNull.isNull(planeUpgrade)) {
            throw new MwException(GameError.PLANE_CONFIG_NOT_FOUND.getCode(), "检测战机的时候, 没有找到战机的配置, planeId:", planeId);
        }
        return warPlanes.get(planeUpgrade.getPlaneType());
    }

    /**
     * 根据上阵队列的pos位获取对应的将领信息
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
     * 根据防守队列的pos位获取对应的将领信息
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
     * 获取城墙将领信息
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
     * 获取内阁采集将领信息
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
     * 获取特攻将领信息
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
     * 是否是城墙上阵将领
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
     * 是否是上阵将领
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
     * 是否是内阁采集上阵将领
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
     * 是否是特攻上阵将领
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
     * 判断防守将领是否是空
     *
     * @return true是空
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
     * 获取玩家可以上阵防御的将领 别人过来防守的＞我自己的主将＞城防将＞城防NPC＞别人驻防的将
     *
     * @return
     */
    public List<Hero> getDefendHeros() {
        List<Hero> heroList = new ArrayList<>();
        Hero hero;
        // 驻守本城的其他玩家将领 外部计算

        // 城防将
        int[] myHerDef = heroDef;
        // 在城内的上阵将领
        if (defHeroIsEmpty()) { // 如果城防位置全空就算上阵将领的顺序
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
        // 城防军
        for (int heroId : heroWall) {
            if (heroId > 0) {
                hero = heros.get(heroId);
                if (hero == null) {
                    continue;
                }
                if (hero.isIdle() && hero.getCount() > 0 && hero.getCount() == hero.getAttr()[HeroConstant.ATTR_LEAD]) {
                    heroList.add(hero);
                    LogUtil.debug(roleId + ",城防守将=" + heroId);
                }
            }
        }
        return heroList;
    }

    /**
     * 获取上阵将领战斗力
     *
     * @return
     */
    public Map<Integer, Integer> getBattleHeroShowFightInfo() {
        Map<Integer, Integer> fightInfo = new HashMap<>();
        List<Hero> battleHeros = getAllOnBattleHeros();
        if (!CheckNull.isEmpty(battleHeros)) {
            // 把采集将领加入战斗力模块中
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
     * 获取玩家所有上阵将领
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
     * 是否有上阵将领
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
     * 获取将领兵力排数加成
     *
     * @return
     */
    public int getLineAdd() {
        return 0;
    }

    /**
     * 获取科技等级
     *
     * @param techId
     * @return
     */
    public int getTechLvById(int techId) {
        return tech == null ? 0 : tech.getTechLvById(techId);
    }

    /**
     * 免费洗髓次数是否已满
     *
     * @return
     */
    public boolean washCountFull() {
        return false;
//        return common.washCountFull();
    }

    /**
     * 获取可以添加加速科技队列
     *
     * @return
     */
    public TechQue getCanSpeedTechQue() {
        if (tech == null) {// 无科研所跳过
            return null;
        }
        TechQue que = tech.getQue();
        if (que == null) return null;
        if (que.haveFreeSpeed() || que.getId() <= 0) return null;
        return que;
    }

    /**
     * 获取可以添加加速（没有可以使用的免费加速）的建造队列，当有多个满足条件的队列时，随机选择一个
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
        // 多个随机选一个
        if (!CheckNull.isEmpty(queList)) {
            return queList.get(RandomHelper.randomInSize(queList.size()));
        }
        return null;
    }

    /**
     * 获取可以添加加速（没有可以使用的免费加速）的募兵队列，当有多个满足条件的队列时，随机选择一个
     *
     * @return
     */
    public ArmQue getCanAddSpeedArmQue() {
        // List<ArmQue> list = new ArrayList<>();// 记录所有募兵队列
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
        List<ArmQue> queList = new ArrayList<>(); // 早出时间最晚的
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
        // 多个随机选一个
        if (!CheckNull.isEmpty(queList)) {
            return queList.get(RandomHelper.randomInSize(queList.size()));
        }
        return null;
    }

    public Integer getPushRecord(int pushId) {
        return pushRecords.get(Integer.toString(pushId));
    }

    /**
     * 是否有该特殊道具(先更新)
     *
     * @param type
     * @param id
     * @return true 已拥有唯一道具
     */
    public boolean checkHaveSpecial(int type, int id) {
        return specialProp.containsKey(type) && specialProp.get(type).contains(id);
    }

    /**
     * 更新特殊道具
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
     * 是否有推送过此类推送消息
     *
     * @param pushId
     * @return true 有推送过消息
     */
    public boolean hasPushRecord(String pushId) {
        Integer status = getPushRecord(pushId);
        return !(null == status || status == PushConstant.PUSH_NOT_PUSHED);
    }

    /**
     * 如果该推送功能内不需要细分状态，可以直接使用推送id作为key，则使用该方法
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
     * 具有复杂功能（如装备打造需要区分多个装备的推送）的消息推送，使用push_functionId（pushId+功能内相关id）的形式作为key
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
     * 注意 ser*()方法 一定要和序列化对象名字一致
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
        ser.setCollectMineCount(collectMineCount);//同阵营攻打采集
        ser.setMedalGoodsRefNum(medalGoodsRefNum);// 勋章商店刷新次数
        ser.setPeacekeepingForcesNum(peacekeepingForcesNum);// 维和部队 触发次数
        ser.setJGXHLastTime(jGXHLastTime);// 勋章 军功显赫特技 上一次触发时间
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
        // 玩家勋章商品 序列化
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
        // 世界争霸数据
        if (!CheckNull.isNull(this.playerWorldWarData)) {
            ser.setWorldWar(this.playerWorldWarData.ser());
        }
        for (EquipJewel jewel : this.equipJewel.values()) {
            ser.addJewel(PbHelper.createJewelPb(jewel));
        }
        // 城堡皮肤
        ser.setCurCastleSkin(this.curCastleSkin);
        for (Integer id : this.ownCastleSkin) {
            ser.addOwnCastleSkin(id);
        }
        // 时限皮肤
        ser.addAllOwnCastleSkinTime(PbHelper.createTwoIntListByMap(this.ownCastleSkinTime));
        ser.addAllOwnCastleSkinStar(PbHelper.createTwoIntListByMap(this.ownCastleSkinStar));
        // 纽约争霸
        if (!CheckNull.isNull(this.newYorkWar)) {
            ser.setNewYorkWar(this.newYorkWar.ser());
        }
        // npc首杀玩家记录
        if (!CheckNull.isEmpty(this.npcCityFirstKillReward)) {
            ser.addAllNpcCityFirstKillReward(this.npcCityFirstKillReward);
        }
        // 战令对象的序列化
        if (!CheckNull.isNull(this.battlePassPersonInfo)) {
            ser.setPersonInfo(this.battlePassPersonInfo.ser());
        }
        //沙盘演武积分
        ser.setSandTableScore(this.sandTableScore);
        //沙盘演武兑换
        if (!CheckNull.isEmpty(this.sandTableBought)) {
            this.sandTableBought.entrySet().forEach(o -> {
                ser.addSandTableBought(PbHelper.createTwoIntPb(o.getKey(), o.getValue()));
            });
        }
        //挂机叛军等级记录
        ser.setSerPlayerOnHook(this.playerOnHook.ser());
        //玩家赛季数据
        ser.setSerPlayerSeasonInfo(this.playerSeasonData.ser());
        // 序列化装扮
        ser.setDuData(this.dressUp.ser());
        //钓鱼数据
        ser.setSerFishingData(this.fishingData.ser());

        //玩家rpc 服务数据
        if (Objects.nonNull(rpcPlayer)) {
            ser.setSerRpcPlayerData(rpcPlayer.ser());
        }
        //玩家小游戏数据
        if (Objects.nonNull(smallGame)) {
            ser.setSmallGame(smallGame.ser());
        }
        if (Objects.nonNull(treasureCombat)) {
            // 宝具副本
            ser.setTreasureCombat(treasureCombat.ser(false));
        }
        if (Objects.nonNull(treasureChallengePlayer)) {
            // 宝具挑战玩家数据
            ser.setTreasureChallengePlayer(treasureChallengePlayer.ser());
        }
        if (treasureWareIdMakeCount.size() > 0) {
            //宝具打造次数
            treasureWareIdMakeCount.forEach((id, count) -> {
                ser.addTreasureWareIdMakeCount(PbHelper.createTwoIntPb(id, count));
            });
        }
        if (Objects.nonNull(playerRelic)) {
            ser.setSerPlayerRelic(playerRelic.ser());
        }
        //跨服信息
        if (Objects.nonNull(crossPlayerLocalData)) {
            ser.setSaveCrossData(crossPlayerLocalData.createPb(true));
        }
        if (CheckNull.nonEmpty(recruitReward)) {
            recruitReward.forEach((k, v) -> ser.addRecruitRewardRecord(PbHelper.createTwoIntPb(k, v)));
        }
        // 模拟器信息
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
        // 城镇事件
        if (!CheckNull.isNull(cityEvent)) {
            ser.setCityEvent(cityEvent.ser());
        }
        // 性格
        if (CheckNull.nonEmpty(characterData)) {
            ser.addAllCharacterData(PbHelper.createTwoIntListByMap(characterData));
        }
        // 性格奖励记录
        if (CheckNull.nonEmpty(characterRewardRecord)) {
            ser.addAllCharacterRewardRecord(PbHelper.createTwoIntListByMap(characterRewardRecord));
        }
        // 侦察兵状态
        if (CheckNull.nonEmpty(scoutData)) {
            ser.addAllScoutMap(PbHelper.createTwoIntListByMap(scoutData));
        }
        // 探索的地图格子
        if (CheckNull.nonEmpty(mapCellData)) {
            mapCellData.forEach((cellId, cellState) -> {
                CommonPb.MapCell.Builder mapCell = CommonPb.MapCell.newBuilder();
                mapCell.setCellId(cellId);
                if (cellState.get(1) == null) {
                    StaticHomeCityCell staticHomeCityCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
                    cellState.add(staticHomeCityCell.getHasBandit());
                }
                mapCell.addAllState(cellState);
                ser.addMapCellData(mapCell.build());
            });
        }
        // 建筑状态信息
        if (CheckNull.nonEmpty(buildingData)) {
            for (BuildingState buildingState : buildingData.values()) {
                ser.addBuildingState(buildingState.creatPb());
            }
        }
        // 已开垦的地基
        if (CheckNull.nonEmpty(foundationData)) {
            ser.addAllFoundationData(foundationData);
        }
        // 居民信息
        if (CheckNull.nonEmpty(residentData)) {
            ser.addAllResidentData(residentData);
        }
        // // 探索队列
        // if (CheckNull.nonEmpty(exploreQue)) {
        //     for (ExploreQue tmp : exploreQue.values()) {
        //         ser.addExploreQue(tmp.creatExploreQuePb());
        //     }
        // }
        // // 开垦队列
        // if (CheckNull.nonEmpty(reclaimQue)) {
        //     for (ReclaimQue tmp : reclaimQue.values()) {
        //         ser.addReclaimQue(tmp.creatReclaimQuePb());
        //     }
        // }

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
        // 勋章商店刷新次数 反序列化
        if (ser.hasMedalGoodsRefNum()) {
            medalGoodsRefNum = ser.getMedalGoodsRefNum();
        }
        // 维护部队次数 反序列化
        if (ser.hasPeacekeepingForcesNum()) {
            peacekeepingForcesNum = ser.getPeacekeepingForcesNum();
        }
        // 军功显赫上一次触发时间 反序列化
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
        // 玩家 勋章商品 反序列化
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
        // 玩家世界争霸数据
        if (ser.hasWorldWar()) {
            this.playerWorldWarData.deser(ser.getWorldWar());
        }
        List<CommonPb.EquipJewel> jewelList = ser.getJewelList();
        if (!CheckNull.isEmpty(jewelList)) {
            for (CommonPb.EquipJewel jewel : jewelList) {
                this.equipJewel.put(jewel.getJewelId(), new EquipJewel(jewel));
            }
        }
        // 城堡皮肤
        if (ser.hasCurCastleSkin()) {
            this.curCastleSkin = ser.getCurCastleSkin();
        }
        this.ownCastleSkin.addAll(ser.getOwnCastleSkinList());
        Optional.ofNullable(ser.getOwnCastleSkinTimeList()).ifPresent(cs ->
                cs.forEach(c -> this.ownCastleSkinTime.put(c.getV1(), c.getV2()))
        );
        Optional.ofNullable(ser.getOwnCastleSkinStarList()).ifPresent(tmpList -> tmpList.forEach(o -> this.ownCastleSkinStar.put(o.getV1(), o.getV2())));
        // 玩家纽约争霸数据
        if (ser.hasNewYorkWar()) {
            this.newYorkWar.deser(ser.getNewYorkWar());
        }
        // 反序列化战令的个人数据
        if (ser.hasPersonInfo()) {
            this.battlePassPersonInfo.dser(ser.getPersonInfo());
        }
        //沙盘积分
        this.sandTableScore = ser.getSandTableScore();
        //沙盘兑换
        Optional.ofNullable(ser.getSandTableBoughtList()).ifPresent(tmp -> tmp.forEach(o -> this.sandTableBought.put(o.getV1(), o.getV2())));

        this.playerOnHook.deser(ser.getSerPlayerOnHook());

        Optional.ofNullable(ser.getSerPlayerSeasonInfo()).ifPresent(tmp -> this.playerSeasonData.deser(tmp));

        // 反序列化装扮
        if (ser.hasDuData()) {
            this.dressUp.dser(ser.getDuData());
        }
        //每日攻打同阵营的采集上限
        if (ser.hasCollectMineCount()) {
            this.collectMineCount = ser.getCollectMineCount();
        }
        //钓鱼数据
        if (ser.hasSerFishingData()) {
            this.fishingData.dser(ser.getSerFishingData());
        }
        //跨服 rpc 数据
        if (ser.hasSerRpcPlayerData()) {
            this.rpcPlayer = new RpcPlayer(ser.getSerRpcPlayerData());
        }
        //玩家小游戏
        if (ser.hasSmallGame()) {
            this.smallGame = new SmallGame(ser.getSmallGame());
        }
        if (ser.hasTreasureCombat()) {
            this.treasureCombat.dSer(ser.getTreasureCombat());
        }
        if (ser.hasTreasureChallengePlayer()) {
            this.treasureChallengePlayer.dSer(ser.getTreasureChallengePlayer());
        }
        //宝具id打造次数
        Optional.ofNullable(ser.getTreasureWareIdMakeCountList()).ifPresent(tmp -> tmp.forEach(o -> this.treasureWareIdMakeCount.put(o.getV1(), o.getV2())));
        if (ser.hasSaveCrossData()) {
            this.crossPlayerLocalData = new CrossPlayerLocalData(ser.getSaveCrossData());
        }
        if (ser.hasSerPlayerRelic()) {
            this.playerRelic.dser(ser.getSerPlayerRelic());
        }
        Optional.ofNullable(ser.getRecruitRewardRecordList()).ifPresent(tmp -> tmp.forEach(o -> this.recruitReward.put(o.getV1(), o.getV2())));
        // 模拟器记录
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
        // 城镇事件
        if (ser.hasCityEvent()) {
            this.cityEvent = this.cityEvent.dser(ser.getCityEvent());
        }
        // 性格值
        Optional.ofNullable(ser.getCharacterDataList()).ifPresent(tmp -> tmp.forEach(o -> this.characterData.put(o.getV1(), o.getV2())));
        // 性格奖励记录
        Optional.ofNullable(ser.getCharacterRewardRecordList()).ifPresent(tmp -> tmp.forEach(o -> this.characterRewardRecord.put(o.getV1(), o.getV2())));
        // 侦察兵数据
        if (CheckNull.nonEmpty(ser.getScoutMapList())) {
            ser.getScoutMapList().forEach(o -> this.scoutData.put(o.getV1(), o.getV2()));
        }
        // 解锁的地图格子
        if (CheckNull.nonEmpty(ser.getMapCellDataList())) {
            for (CommonPb.MapCell mapCell : ser.getMapCellDataList()) {
                this.mapCellData.put(mapCell.getCellId(), mapCell.getStateList());
            }
        }
        // 建筑状态信息
        if (CheckNull.nonEmpty(ser.getBuildingStateList())) {
            ser.getBuildingStateList().forEach(o -> this.buildingData.put(o.getBuildingId(), new BuildingState(o)));
        }
        // 解锁的地基数据
        if (CheckNull.nonEmpty(ser.getFoundationDataList())) {
            this.foundationData.addAll(ser.getFoundationDataList());
        }
        // 居民信息
        if (CheckNull.nonEmpty(ser.getResidentDataList())) {
            this.residentData.clear();
            this.residentData.add(0, ser.getResidentData(0));
            this.residentData.add(1, ser.getResidentData(1));
            this.residentData.add(2, ser.getResidentData(2));
        }
        // // 探索对列
        // Optional.ofNullable(ser.getExploreQueList()).ifPresent(tmp -> tmp.forEach(o -> this.exploreQue.put(o.getScoutIndex(), new ExploreQue(o))));
        // // 开垦队列
        // Optional.ofNullable(ser.getReclaimQueList()).ifPresent(tmp -> tmp.forEach(o -> this.reclaimQue.put(o.getIndex(), new ReclaimQue(o))));
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
     * @Description: 玩家勋章数据 序列化
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
     * @Description: 玩家勋章数据 反序列化
     */
    private void dserMedals(SerMedal ser) {
        for (CommonPb.Medal medal : ser.getMedalList()) {
            if (medal.hasHeroId()) {// 该勋章有佩戴将领
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
     * 反序列化Acitivty对象
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
     * 刷新buff加成
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
            // 重新计算将领属性
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
     * 检测玩家是否可以触发npc城池首杀奖励
     *
     * @param cityId 城池id
     * @return true 可以触发，false 不可以
     */
    public boolean checkNpcFirstKillReward(int cityId) {
        if (npcCityFirstKillReward.contains(cityId)) {
            return false;
        }
        return true;
    }

    /**
     * 添加玩家触发的npc城池首杀记录
     *
     * @param cityId 城池id
     */
    public void addNpcFirstKillRecord(int cityId) {
        npcCityFirstKillReward.add(cityId);
    }

    /**
     * 判断是否可以推送体力赠送
     *
     * @return 是否可以推送
     */
    public boolean canPushActPower() {
        //  判断离线时间没有大于3天
        return getPushRecord(PushConstant.OFF_LINE_ONE_DAY) != null && getPushRecord(PushConstant.OFF_LINE_ONE_DAY) < 3;
    }

    /**
     * 判断是否可以推送离线消息(应用外推送)
     *
     * @param now 现在的时间
     * @return 是否可以推送
     */
    public boolean canPushOffLine(int now) {
        Integer status = getPushRecord(PushConstant.OFF_LINE_ONE_DAY);
        int canPushTime = now - (24 * TimeHelper.HOUR_S);
        // 离线时间大于24小时, 并且没有推送过两次
        return status != null && lord.getOffTime() < canPushTime && status < 3;
    }

    /**
     * 根据LordId获取名称
     *
     * @return 跟lordId唯一的名称，有可能被他人占用
     */
    public String getName() {
        long lordId = lord.getLordId();
        String initName = DataResource.ac.getBean(PlayerService.class).getInitName();
        if (lordId != 0) {
            String sLordId = String.valueOf(lordId);
            // 区服和id
            String subStr = sLordId.substring(sLordId.length() - 8);
            // 这里不要用#拼接. 会变成emoji
            // 这里也不要用&拼接. 会变成下划线
            return initName + Integer.toHexString(Integer.parseInt(subStr));
        }
        return null;
    }

    /**
     * 用现在的时间戳转成十六进制拼接名称
     *
     * @param now 现在的时间戳
     * @return 不会重复的名称
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
     * 获取皮肤星数，如果玩家没有星数数据，则取配表的默认星数
     *
     * @param skinId 皮肤配置id
     * @return 皮肤星级
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

    // 获取客户端可执行的模拟器
    public List<CommonPb.LifeSimulatorRecord> createSimulatorRecordList() {
        List<CommonPb.LifeSimulatorRecord> data = new ArrayList<>();

        // 非城镇事件模拟器
        if (CheckNull.isEmpty(this.getLifeSimulatorRecordMap())) {
            this.setLifeSimulatorRecordMap(new HashMap<>());
        }
        for (Entry<Integer, List<LifeSimulatorInfo>> entry : this.lifeSimulatorRecordMap.entrySet()) {
            CommonPb.LifeSimulatorRecord.Builder lifeSimulatorRecordBuilder = CommonPb.LifeSimulatorRecord.newBuilder();
            lifeSimulatorRecordBuilder.setTriggerMode(entry.getKey());
            for (LifeSimulatorInfo lifeSimulatorInfo : entry.getValue()) {
                if (lifeSimulatorInfo.getDelay() > 0) {
                    // 只同步客户端可执行的模拟器
                    continue;
                }
                CommonPb.LifeSimulatorInfo lifeSimulatorInfoPb = lifeSimulatorInfo.ser();
                lifeSimulatorRecordBuilder.addLifeSimulatorInfo(lifeSimulatorInfoPb);
            }
            data.add(lifeSimulatorRecordBuilder.build());
        }

        // 城镇事件模拟器
        if (CheckNull.isNull(this.getCityEvent())) {
            CityEvent cityEvent = new CityEvent();
            this.setCityEvent(cityEvent);
        }
        CommonPb.LifeSimulatorRecord.Builder lifeSimulatorRecordBuilder = CommonPb.LifeSimulatorRecord.newBuilder();
        lifeSimulatorRecordBuilder.setTriggerMode(3);
        for (LifeSimulatorInfo lifeSimulatorInfo : this.cityEvent.getLifeSimulatorInfoList()) {
            if (lifeSimulatorInfo.getDelay() > 0) {
                // 只同步客户端可执行的模拟器
                continue;
            }
            CommonPb.LifeSimulatorInfo lifeSimulatorInfoPb = lifeSimulatorInfo.ser();
            lifeSimulatorRecordBuilder.addLifeSimulatorInfo(lifeSimulatorInfoPb);
        }
        data.add(lifeSimulatorRecordBuilder.build());
        return data;
    }
}
