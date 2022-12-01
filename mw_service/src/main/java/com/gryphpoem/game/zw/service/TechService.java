package com.gryphpoem.game.zw.service;

import com.google.common.collect.Lists;
import com.gryphpoem.cross.constants.PlayerUploadTypeDefine;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.TypeInfo;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.StaticCommandMult;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticTechLv;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 科技
 *
 * @author tyler
 */
@Service
public class TechService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private BuildingDataManager buildingDataManager;

    @Autowired
    private TechDataManager techDataManager;

    @Autowired
    private EquipService equipService;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private CiaService ciaService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;

    /**
     * 获取科技信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetTechRs getTech(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GetTechRs.Builder builder = GetTechRs.newBuilder();
        Gains gains = player.gains.get(BuildingType.TECH);
        if (gains != null) {
            if (TimeHelper.getCurrentSecond() > gains.getEndTime()) {
                player.gains.remove(BuildingType.TECH);
            } else {
                builder.setId(gains.getId());
                builder.setEndTime(gains.getEndTime());
            }
        }
        Tech tech = player.tech;
        if (tech != null) {
            if (tech.getTechLv() != null) {
                for (Entry<Integer, TechLv> kv : tech.getTechLv().entrySet()) {
                    if (kv.getValue() != null) {
                        builder.addTech(PbHelper.createTechLv(kv.getValue()));
                    }
                }
            }
            if (tech.getQue() != null) {
                builder.setQue(PbHelper.createTechQue(tech.getQue()));
            }
        }

        return builder.build();
    }

    /**
     * 科技馆招募
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public TechAddRs doTechAdd(long roleId, int id) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        if (player != null) {
            StaticCommandMult staticCommandMult = StaticBuildingDataMgr.getCommandMult(id);
            if (staticCommandMult == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "科技馆招募，找不到配置, roleId: " + roleId);
            }
            if (staticCommandMult.getLordLv() > player.lord.getLevel()) {
                throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "科技馆招募，等级不满足, roleId: " + roleId);
            }

            if (staticCommandMult.getNeedBuildingType() != BuildingType.TECH) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "雇佣的NPC 必须是学者!!! roleId: " + roleId);
            }

            int lv = BuildingDataManager.getBuildingTopLv(player, staticCommandMult.getNeedBuildingType());
            if (staticCommandMult.getNeedBuildingLv() > lv) {
                throw new MwException(GameError.TECH_LV_NOT_ENOUGH.getCode(), "科技馆招募，司令部等级不满足, roleId: " + roleId);
            }


            int nowSec = TimeHelper.getCurrentSecond();
            //如果当前正在雇佣学者,再次雇佣则必须雇佣比当前学者更高级的学者(大学升级解锁了更高级的)
            Gains gainsNow = player.gains.get(staticCommandMult.getType());
            if (Objects.nonNull(gainsNow) && nowSec < gainsNow.getEndTime()) {
                StaticCommandMult scm0 = StaticBuildingDataMgr.getCommandMult(gainsNow.getId());
                if (Objects.isNull(scm0) || staticCommandMult.getLv() < scm0.getLv() || staticCommandMult.getId() == scm0.getId()) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("学者雇佣异常, 角色ID :%d, 当前正在雇佣的学者ID :%d, 到期时间 :%s, 本次想雇佣学者ID :%d ",
                            roleId, scm0.getId(), DateHelper.simpleTimeFormat(new Date(gainsNow.getEndTime())), id));
                }
            } else {
                //当前没有雇佣的话只能雇佣当前大学建筑解锁的最高等级的学者
                Map<Integer, StaticCommandMult> cmMap = StaticBuildingDataMgr.getCommandMultMap();
                //KEY:建筑等级, VALUE:该建筑解锁的学者列表
                TreeMap<Integer, List<StaticCommandMult>> lvMap = cmMap.values().stream()
                        .filter(scm -> scm.getNeedBuildingType() == staticCommandMult.getNeedBuildingType())
                        .collect(Collectors.groupingBy(StaticCommandMult::getNeedBuildingLv, TreeMap::new, Collectors.toList()));
                Entry<Integer, List<StaticCommandMult>> entry = lvMap.floorEntry(lv);
                if (Objects.isNull(entry) || !entry.getValue().contains(staticCommandMult)) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, 建筑等级 :%d, 想雇佣的学者ID :%d", roleId, lv, id));
                }
            }

            TechAddRs.Builder builder = TechAddRs.newBuilder();
            Gains gains = new Gains(staticCommandMult.getType());

            gains.setId(id);
            gains.setEndTime(nowSec + staticCommandMult.getAddTime());
            if (staticCommandMult.isFirstFree()) {
                // 记录首次免费雇佣
                List<History> list = player.typeInfo.computeIfAbsent(TypeInfo.TYPE_1, k -> new ArrayList<>());
                boolean free = true;
                for (History history : list) {
                    if (history.getId() == id) {
                        free = false;
                        rewardDataManager.checkAndSubPlayerRes(player, staticCommandMult.getCost(), AwardFrom.TECH_ADD);
                    }
                }
                if (free) {
                    list.add(new History(id, 1));
                    gains.setEndTime(nowSec + staticCommandMult.getFreeTime());
                }
            } else {
                rewardDataManager.checkAndSubPlayerRes(player, staticCommandMult.getCost(), AwardFrom.TECH_ADD);
            }

            Tech tech = player.tech;
            if (tech == null) {
                tech = new Tech();
                player.tech = tech;
            }
            TechQue que = tech.getQue();
            if (que != null) {
                // 已经加速过，算差值
                if (que.getFreeCnt() > 0) {
                    que.setFreeTime(staticCommandMult.getSpeedTime(player) - que.getFreeTime());
                } else {
                    que.setFreeTime(staticCommandMult.getSpeedTime(player));
                }
                que.setFreeCnt(0);
                builder.setQue(PbHelper.createTechQue(que));
            }
            player.gains.put(staticCommandMult.getType(), gains);
            builder.setId(id);
            builder.setEndTime(gains.getEndTime());
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_RECRUIT, staticCommandMult.getType(),
                    staticCommandMult.getQuality());

            taskDataManager.updTask(player, TaskType.COND_30, 1, staticCommandMult.getLv());
            return builder.build();
        }
        return null;
    }

    /**
     * 科技升级
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public UpTechRs doUpTech(long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        buildingDataManager.checkBuildingIsCreate(BuildingType.TECH, player);
        if (id < TechConstant.TYPE_1 || id > TechConstant.TYPE_36) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "科技升级时，科技ID不正确, roleId:" + roleId + ",id=" + id);
        }
        // 检查建筑是否在升级
        boolean isUping = buildingDataManager.checkBuildIsUpping(player, BuildingType.TECH);
        if (isUping) {
//            if (player.shop == null || !player.shop.getVipId().contains(Constant.TECH_QUICK_VIP_BAG)) {
//                throw new MwException(GameError.BUILD_NOT_TECH_QUICK_VIP_BAG.getCode(), "roleId:", roleId, " 没有购买科技快研礼包");
//            }
//            if (!techDataManager.isAdvanceTechGain(player)) {
//                throw new MwException(GameError.NOT_ADVANCE_TECH_GAIN.getCode(), "roleId:", roleId, " 没有雇佣高级研究院");
//            }
        }

        Tech tech = player.tech;
        if (tech == null) {
            tech = new Tech();
            player.tech = tech;
        }
        TechQue que = tech.getQue();
        if (que != null && que.getId() > 0) {
            throw new MwException(GameError.TECH_QUE_FULL.getCode(), "科技升级时，队列满, roleId:" + roleId + ",id=" + id);
        }
        if (!tech.getTechLv().containsKey(id)) {
            tech.getTechLv().put(id, new TechLv(id, 0, 1));
        }
        TechLv techLv = tech.getTechLv().get(id);
        StaticTechLv nowTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
        if (nowTechLv == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "科技升级时，找不到配置, roleId:" + roleId + ",id=" + id + ",lv=" + (techLv.getLv()));
        }

        // 判断科技是否可以开启
        checkPreTech(roleId, id, nowTechLv, techLv.getLv(), tech);

        // 判断是否能升下一级
        if (nowTechLv.getCnt() <= 1 || techLv.getStep() >= nowTechLv.getCnt()) {
            StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv() + 1);
            // 判断科技是否已升到最高
            if (staticTechLv == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(),
                        "科技升级时，找不到配置（已到最高级）, roleId:" + roleId + ",id=" + id + ",lv=" + (techLv.getLv() + 1));
            }
            if (!CheckNull.isEmpty(staticTechLv.getNeedTech())) {
                checkPreTech(roleId, id, staticTechLv, techLv.getLv() + 1, tech);
            }
        }

        rewardDataManager.checkAndSubPlayerRes(player, nowTechLv.getCost(), AwardFrom.UP_TECH);
        // 计算科技时间
        int needTime = calcTechTime(player, nowTechLv.getNeedTime());
        tech.setQue(new TechQue(id, needTime + TimeHelper.getCurrentSecond(), 0, 0));
        UpTechRs.Builder builder = UpTechRs.newBuilder();
        builder.setQue(PbHelper.createTechQue(tech.getQue()));
        builder.setTech(PbHelper.createTechLv(tech.getTechLv().get(id)));
        builder.setResource(PbHelper.createCombatPb(player.resource));
        taskDataManager.updTask(player, TaskType.COND_19, 1, id);
        taskDataManager.updTask(player, TaskType.COND_500, techLv.getLv() + 1, id);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_19, 1);
        taskDataManager.updTask(player, TaskType.COND_TECH_UP_44, 1);
        return builder.build();
    }

    /**
     * 计算科技研究时间
     *
     * @param player
     * @param bastTime
     * @return
     */
    private int calcTechTime(Player player, int bastTime) {
        double agentEffect = ciaService.getAgentSkillVal(player, CiaConstant.SKILL_TECH_ACC) / Constant.TEN_THROUSAND;
        // 柏林官员加成
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_TECH_TIME) / Constant.TEN_THROUSAND;
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_402) / Constant.TEN_THROUSAND;
        // 科技研究时间=基础升级时间*（1-特工减免[%]-官职减免[%]-赛季天赋减少） 单位秒，向上取整
        int time = (int) Math.ceil(bastTime * (1 - agentEffect - berlinJobEffect - seasonTalentEffect));
        return time;
    }

    private void checkPreTech(long roleId, int techId, StaticTechLv nowTechLv, int techLv, Tech tech)
            throws MwException {
        if (!CheckNull.isEmpty(nowTechLv.getNeedTech())) {
            StaticTechLv needTech = null;
            for (int tId : nowTechLv.getNeedTech()) {
                needTech = StaticBuildingDataMgr.getTechMap(tId);
                if (needTech.getLv() == 0) {
                    continue;
                }
                TechLv myTech = tech.getTechLv().get(needTech.getTechId());
                if (myTech == null || needTech.getLv() > myTech.getLv()) {
                    throw new MwException(GameError.SCIENCE_LOCKED.getCode(),
                            "科技升级时，未解锁前置科技, roleId:" + roleId + ",id=" + techId + ",lv=" + techLv + ",需要前置科技ID=" + tId
                                    + ",等级=" + needTech.getLv() + ",阶段=" + needTech.getCnt());
                }
            }
        }
    }

    /**
     * 科技加速
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public TechSpeedRs doTechSpeed(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Tech tech = player.tech;
        if (tech == null) {
            tech = new Tech();
            player.tech = tech;
        }
        TechQue que = tech.getQue();
        if (que == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "科技加速，无队列, roleId:", roleId);
        }

        int now = TimeHelper.getCurrentSecond();

        int second = que.getEndTime() - now;
        if (second <= 0) {
            throw new MwException(GameError.TECH_QUE_FINISH.getCode(), "科技加速，无队列, roleId:", roleId);
        }

        if (type == 2) {// 金币加速
            int needGold = (int) Math.ceil(second * 1.00 / 60);
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold * 2,
                    AwardFrom.TECH_SPEED, que.getId());
            // rewardDataManager.checkMoneyIsEnough(player.lord, AwardType.Money.GOLD, needGold * 2, "招募金币加速");
            // rewardDataManager.subMoney(player, AwardType.Money.GOLD, needGold * 2,
            // AwardFrom.FACTORYRECRUIT_SPEED_GOLD,
            // "招募金币加速");
        } else if (type == 1) {// 科研官免费加速
            // 判断是否可以免费加速
            Gains gains = player.gains.get(BuildingType.TECH);
            if (gains == null || gains.getEndTime() < TimeHelper.getCurrentSecond() || que.getFreeCnt() > 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "科技加速，无免费次数, roleId:" + roleId + ",freeCnt=" + que.getFreeCnt());
            }
            StaticCommandMult commandMult = StaticBuildingDataMgr.getCommandMult(gains.getId());
            if (commandMult == null || commandMult.getEffectType() != StaticBuildingDataMgr.CD_TYPE) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "科技加速，无免费次数, roleId:" + roleId + ",commandMult=" + commandMult);
            }
            int staticSpeed = commandMult.getSpeedTime(player);
            if (que.getFreeTime() > 0) {
                staticSpeed = que.getFreeTime();
            }
            second = Math.min(staticSpeed, second); // 道具具体加速
            que.setFreeCnt(1);
            que.setFreeTime(commandMult.getSpeedTime(player));
        } else if (type == 3) {// 奖励获取的免费加速
            if (que.getFreeOtherCnt() < 1) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "科技加速，无免费次数, roleId:", roleId, ",techQue:", que);
            }
            que.decreaseFreeOtherCnt();// 次数减1
            second = Math.min(que.getParam(), second);
        }
        que.setEndTime(que.getEndTime() - second);
        // 潮哥 因为客户端显示误差,所以已经结束的队列,再减5秒
        if (now >= que.getEndTime()) {
            LogUtil.debug("科技加速多减5秒");
            que.setEndTime(que.getEndTime() - 5);
        }
        TechSpeedRs.Builder builder = TechSpeedRs.newBuilder();
        builder.setQue(PbHelper.createTechQue(que));
        builder.setGold(player.lord.getGold());
        taskDataManager.updTask(player, TaskType.COND_33, 1);
        return builder.build();
    }

    /**
     * 科技升级完成
     *
     * @param roleId
     * @return
     */
    public TechFinishRs doTechFinish(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Tech tech = player.tech;
        if (tech == null) {
            tech = new Tech();
            player.tech = tech;
        }
        TechQue que = tech.getQue();
        if (que == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "科技完成升级，无队列, roleId:" + roleId);
        }
        int now = TimeHelper.getCurrentSecond();
        if (now < que.getEndTime()) {
            throw new MwException(GameError.EQUIP_BUILD_TIME.getCode(), "科技完成升级,科技列表不存在或者时间未到, roleId:", roleId,
                    ", que:", que);
        }
        TechFinishRs.Builder builder = TechFinishRs.newBuilder();
        if (!tech.getTechLv().containsKey(que.getId())) {
            tech.getTechLv().put(que.getId(), new TechLv(que.getId(), 0, 1));
        }
        TechLv techLv = tech.getTechLv().get(que.getId());
        StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
        if (staticTechLv != null && staticTechLv.getCnt() > 1 && staticTechLv.getCnt() > techLv.getStep()) {
            techLv.setStep(techLv.getStep() + 1);
        } else {
            techLv.setStep(1);
            techLv.setLv(techLv.getLv() + 1);
            uploadCrossData(player, que.getId());
            processTechLvUp(player, techLv, player.resource);
        }
        LogUtil.debug(roleId + ",type=" + techLv.getId() + ",techLV=" + techLv.getLv() + ",step=" + techLv.getStep());
        LogLordHelper.commonLog("techUp", AwardFrom.UP_TECH, player, techLv.getId(), techLv.getLv(), techLv.getStep());
        StaticTechLv nextStaticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
        EventDataUp.technologySuccess(player, techLv.getId(), CheckNull.isNull(nextStaticTechLv) ? 0 : nextStaticTechLv.getId(), techLv.getLv());
        // 重置科技升级完成消息推送状态
        // player.removePushRecord(PushConstant.ID_UP_TECH_FINISH);
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_TECH);
        taskDataManager.updTask(player, TaskType.COND_508, 1);

        //貂蝉任务-升级科技
        ActivityDiaoChanService.completeTask(player, ETask.TECHNOLOGY_UP);
        TaskService.processTask(player, ETask.TECHNOLOGY_UP);

        tech.setQue(null);

        // 科技研究-初级指挥学、初级统率力、中级指挥学、中级统率力 发送世界消息
        if (staticTechLv.getTechId() == TechConstant.TYPE_7 || staticTechLv.getTechId() == TechConstant.TYPE_10
                || staticTechLv.getTechId() == TechConstant.TYPE_16
                || staticTechLv.getTechId() == TechConstant.TYPE_20) {
            StaticTechLv baseStaticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv() + 1);
            // 判断科技是否已升到最高
            if (baseStaticTechLv == null) {// 已到最高级
                int chatId = 0;
                // if (staticTechLv.getTechId() == TechConstant.TYPE_7) {
                //     chatId = ChatConst.CHAT_SCIENTIFIC_RESEARCH_CJZHX;
                // } else if (staticTechLv.getTechId() == TechConstant.TYPE_10) {
                //     chatId = ChatConst.CHAT_SCIENTIFIC_RESEARCH_CJTSL;
                // } else if (staticTechLv.getTechId() == TechConstant.TYPE_16) {
                //     chatId = ChatConst.CHAT_SCIENTIFIC_RESEARCH_ZJZHX;
                // } else if (staticTechLv.getTechId() == TechConstant.TYPE_20) {
                //     chatId = ChatConst.CHAT_SCIENTIFIC_RESEARCH_ZJTSL;
                // }
                if (chatId != 0) {
                    chatDataManager.sendSysChat(chatId, player.lord.getCamp(), 0, player.lord.getCamp(),
                            player.lord.getNick());
                }
            }
        }

        builder.setTech(PbHelper.createTechLv(techLv));
        builder.addAllResAdd(buildingDataManager.listResAdd(player));
        return builder.build();
    }

    public void uploadCrossData(Player player, int techType) {
        if (techType != TechConstant.TYPE_28 & techType != TechConstant.TYPE_29 && techType != TechConstant.TYPE_30) {
            return;
        }

        Hero hero;
        int heroType = 0;
        StaticHero staticHero;
        for (PartnerHero partnerHero : player.getPlayerFormation().getHeroBattle()) {
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            hero = partnerHero.getPrincipalHero();
            if (CheckNull.isNull(hero))
                continue;

            staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
            if (CheckNull.isNull(staticHero))
                continue;

            if (convertToTechType(staticHero.getHeroType()) != null) {
                if (hero.isOnBattle() || hero.isOnWall()) {
                    List<Long> rolesId = new ArrayList<>();
                    EventBus.getDefault().post(
                            new Events.CrossPlayerChangeEvent(PlayerUploadTypeDefine.UPLOAD_TYPE_HERO,
                                    0, CrossFunction.CROSS_WAR_FIRE, rolesId));
                    return;
                }
            }
        }
    }

    private Integer convertToTechType(int heroType) {
        Integer techType = null;
        if (heroType == 1) {
            techType = TechConstant.TYPE_28;
        } else if (heroType == 2) {
            techType = TechConstant.TYPE_29;
        } else if (heroType == 3) {
            techType = TechConstant.TYPE_30;
        }

        return techType;
    }

    private void processTechLvUp(Player player, TechLv techLv, Resource resource) {
        playerDataManager.createRoleOpt(player, Constant.OptId.id_2, String.valueOf(techLv.getId()),
                String.valueOf(techLv.getLv()));
        // 检测装备洗练
        if (techLv.getId() == TechConstant.TYPE_21 && techDataManager.isOpen(player, TechConstant.TYPE_21)) {
            for (Equip equip : player.equips.values()) {
                boolean isTrigger = equipService.triggerEquip4Skill(equip);
                if (isTrigger && equip.isOnEquip()) {
                    Hero hero = player.heros.get(equip.getHeroId());
                    if (hero != null) {
                        CalculateUtil.processAttr(player, hero);
                    }

                }
            }
            // 更新战斗力
            // rankDataManager.setFight(player.lord);
        }

        // 点兵技能
        if (techLv.getId() == TechConstant.TYPE_7
                || techLv.getId() == TechConstant.TYPE_16
                || techLv.getId() == TechConstant.TYPE_31
                || techLv.getId() == TechConstant.TYPE_32) {
            for (Hero h : player.heros.values()) {
                // if (h.isOnAcq() || h.isOnBattle() || h.isOnWall()) {// 重新计算兵排
                // }
                // 点兵科技加所有将领的兵排
                CalculateUtil.processAttr(player, h);
            }
            // 更新战斗力
            // rankDataManager.setFight(player.lord);
        }

        // 增加兵种攻击的三个科技，ID:8.9.15
        if (techLv.getId() == TechConstant.TYPE_8 || techLv.getId() == TechConstant.TYPE_9
                || techLv.getId() == TechConstant.TYPE_15) {
            for (Hero h : player.heros.values()) {
                CalculateUtil.processAttr(player, h);
            }
            // 更新战斗力
            // rankDataManager.setFight(player.lord);
        }

        // 增加兵种强化的三个科技，Id 28 29 30
        if (techLv.getId() == TechConstant.TYPE_28 || techLv.getId() == TechConstant.TYPE_29
                || techLv.getId() == TechConstant.TYPE_30) {
            for (Hero h : player.heros.values()) {
                CalculateUtil.processAttr(player, h);
            }
        }

        // activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_TECH, techLv.getId(), techLv.getLv());
        // StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
        // if (staticTechLv == null || staticTechLv.getEffect()==null || staticTechLv.getEffect().isEmpty()) {
        // return;
        // }
        // int val = staticTechLv.getEffect().get(0);
        // switch (techLv.getId()) {
        // case TechConstant.TYPE_1:
        // resource.setOilOutF(val);
        // break;
        // case TechConstant.TYPE_2:
        // resource.setElecOutF(val);
        // break;
        // case TechConstant.TYPE_3:
        // resource.setFoodOutF(val);
        // break;
        // case TechConstant.TYPE_17:
        // resource.setOreOutF(val);
        // break;
        // }
    }

    /**
     * 科技升级类任务过滤器
     */
    Predicate<StaticTask> techTaskFilter = t -> (t.getCond() == TaskType.COND_19);

    /**
     * 科技升级任务中，科技id大小比较器
     */
    Comparator<StaticTask> taskTechIdComparator = (t1, t2) -> (t1.getCondId() - t2.getCondId());

    /**
     * 科技等级比较器
     */
    Comparator<StaticTechLv> techLvComparator = new TechLvComparator();

    public void addAutoTechUp(Player player) {
        if (null == player) return;

        if (null == player.tech) {
            player.tech = new Tech();
        }

        TechQue que = getPlayerTechQue(player);
        if (null != que && que.getId() > 0) {
            int now = TimeHelper.getCurrentSecond();
            // 科技升级队列已满，跳过，返回
            if (que.getEndTime() > now) {
                return;
            }

            // 科技升级已完成，执行完成逻辑
            TechLv techLv = player.tech.getTechLv().get(que.getId());
            StaticTechLv staticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
            if (staticTechLv != null && staticTechLv.getCnt() > 1 && staticTechLv.getCnt() > techLv.getStep()) {
                techLv.setStep(techLv.getStep() + 1);
            } else {
                techLv.setStep(1);
                techLv.setLv(techLv.getLv() + 1);
                uploadCrossData(player, que.getId());
                processTechLvUp(player, techLv, player.resource);
            }
            LogUtil.robot(player.roleId + ", type=" + techLv.getId() + ", techLv=" + techLv.getLv() + ", step="
                    + techLv.getStep());
            StaticTechLv nextStaticTechLv = StaticBuildingDataMgr.getTechLvMap(techLv.getId(), techLv.getLv());
            EventDataUp.technologySuccess(player, techLv.getId(), CheckNull.isNull(nextStaticTechLv) ? 0 : nextStaticTechLv.getId(), techLv.getLv());
            player.tech.setQue(null);
        }

        long start = System.nanoTime();
        List<Integer> curTaskIds = Lists.newArrayList(player.chapterTask.getOpenTasks().keySet());
        List<StaticTask> buildTask = curTaskIds.stream().map(StaticTaskDataMgr::getTaskById).filter(techTaskFilter)
                .collect(Collectors.toList());
        long end = System.nanoTime();
        LogUtil.robot("机器人科技，过滤科技任务耗时（微秒）:", (end - start) / 1000);

        int techId = -1;
        if (!CheckNull.isEmpty(buildTask)) {// 任务中有科技升级，优先在任务中查找
            // 先过滤出可解锁的科技，在这些结果中找id最小的科技返回
            start = System.nanoTime();
            Optional<StaticTask> minTech = buildTask.stream().filter(t -> techCanUnlock(player, t.getCondId()))
                    .min(taskTechIdComparator);
            end = System.nanoTime();
            LogUtil.robot("机器人科技，科技任务中找id最小的科技耗时（微秒）:", (end - start) / 1000);

            if (minTech.isPresent()) {
                techId = minTech.get().getCondId();
            }
        }

        if (techId < 0) {// 没有查找到科技升级相关的任务，优先找等级低的科技升级
            start = System.nanoTime();
            Optional<StaticTechLv> minTechLv = StaticBuildingDataMgr.getTechMap().values().stream()
                    .filter(t -> techCanUnlock(player, t.getTechId())).min(techLvComparator);
            end = System.nanoTime();
            LogUtil.robot("机器人科技，所有科技中找可升级的科技耗时（微秒）:", (end - start) / 1000);
            if (minTechLv.isPresent()) {
                techId = minTechLv.get().getTechId();
            }
        }

        if (techId > 0) {
            start = System.nanoTime();
            Tech tech = player.tech;
            TechLv techLv = tech.getTechLv().get(techId);
            if (null == techLv) {
                techLv = new TechLv(techId, 0, 1);
                tech.getTechLv().put(techId, techLv);
            }

            StaticTechLv staticTechLv = null;
            try {
                staticTechLv = StaticBuildingDataMgr.getTechLvMap(techId, techLv.getLv());

                // 扣除升级资源
                rewardDataManager.checkAndSubPlayerRes(player, staticTechLv.getCost(), AwardFrom.UP_TECH);

                // 开启升级队列
                int needTime = staticTechLv.getNeedTime();
                tech.setQue(new TechQue(techId, needTime + TimeHelper.getCurrentSecond(), 0, 0));
            } catch (MwException e) {
                LogUtil.robot("机器人升级科技出错, roleId:" + player.roleId + ", tech:" + staticTechLv);
            }
            end = System.nanoTime();
            LogUtil.robot("机器人科技，升级科技耗时（微秒）:", (end - start) / 1000);
        }
    }

    /**
     * 判断科技是否可以解锁并升级（会检查解锁条件和升级条件）
     *
     * @param player
     * @param techId
     * @return
     */
    private boolean techCanUnlock(Player player, int techId) {
        TechLv techLv = player.tech.getTechLv().get(techId);
        int curLv = 0;
        if (null != techLv) {
            curLv = techLv.getLv();
        }

        StaticTechLv curTechLv = StaticBuildingDataMgr.getTechLvMap(techId, curLv);
        if (null == curTechLv) {
            return false;// 科技id未配置，跳过
        }

        try {
            // 判断该科技是否可解锁
            checkPreTech(player.roleId, techId, curTechLv, curLv, player.tech);

            // 判断科技是否可以升级
            StaticTechLv nextTechLv = StaticBuildingDataMgr.getTechLvMap(techId, curLv + 1);
            if (null == nextTechLv) {
                return false;
            }
            checkPreTech(player.roleId, techId, nextTechLv, curLv + 1, player.tech);

            // 检查资源是否足够
            rewardDataManager.checkPlayerResIsEnough(player, curTechLv.getCost(), "科技解锁");
            return true;
        } catch (MwException e) {
            return false;
        }
    }

    public boolean techQueIsFull(Player player) {
        TechQue que = getPlayerTechQue(player);
        return null != que && que.getId() > 0;
    }

    /**
     * 科技升级定时任务处理
     */
    public void techUpTimerLogic() {
        int now = TimeHelper.getCurrentSecond();
        for (Player player : playerDataManager.getPlayers().values()) {
            TechQue que = getPlayerTechQue(player);
            if (null != que && now >= que.getEndTime()) {
                if (!player.hasPushRecord(String.valueOf(PushConstant.ID_UP_TECH_FINISH))) {
                    player.putPushRecord(PushConstant.ID_UP_TECH_FINISH, PushConstant.PUSH_HAS_PUSHED);
                    // 科技升级结束
                    TechLv techLv = player.tech.getTechLv().get(que.getId());
                    if (techLv != null) {
                        int lv = techLv.getLv() + 1;
                        pushTechUpFinish(player.account, que.getId(), lv);
                    }
                }
            }
        }
    }

    /**
     * 向玩家推送科技升级完成消息
     *
     * @param account
     */
    private void pushTechUpFinish(Account account, int techId, int lv) {
        StaticTechLv curTechLv = StaticBuildingDataMgr.getTechLvMap(techId, lv);
        if (curTechLv == null) {
            LogUtil.error("科技类型名称未找到，跳过消息推送");
            return;
        }
        String id = "s_tech_lv_" + curTechLv.getId();
        String name = StaticIniDataMgr.getTextName(id);
        if (name == null) {
            LogUtil.error("科技类型名称未找到，跳过消息推送, roleId:", account.getLordId(), ", id:", id);
            return;
        }
        PushMessageUtil.pushMessage(account, PushConstant.ID_UP_TECH_FINISH, name, lv);
    }

    private TechQue getPlayerTechQue(Player player) {
        Tech tech = player.tech;
        return null == tech ? null : tech.getQue();
    }
}

/**
 * 科技自动升级，科技等级比较器
 */
class TechLvComparator implements Comparator<StaticTechLv> {

    @Override
    public int compare(StaticTechLv t1, StaticTechLv t2) {
        if (t1.getCnt() < t2.getCnt()) {// 优先升级次数少的
            return -1;
        } else if (t1.getCnt() > t2.getCnt()) {
            return 1;
        } else {
            if (t1.getLv() < t2.getId()) {// 优先等级低的
                return -1;
            } else if (t1.getLv() > t2.getId()) {
                return 1;
            } else {
                if (t1.getNeedTime() < t2.getNeedTime()) { // 优先需要时间少的
                    return -1;
                } else if (t1.getNeedTime() > t2.getNeedTime()) {
                    return 1;
                }
            }
        }
        return 0;
    }

}
