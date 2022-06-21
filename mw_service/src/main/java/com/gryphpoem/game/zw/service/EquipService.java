package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.pb.GamePb1.*;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Account;
import com.gryphpoem.game.zw.resource.domain.p.EquipQue;
import com.gryphpoem.game.zw.resource.domain.p.Gains;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author TanDonghai
 * @ClassName EquipService.java
 * @Description 装备相关
 * @date 创建时间：2017年3月27日 下午8:31:45
 */
@Service
public class EquipService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private HeroService heroService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private ActivityRobinHoodService activityRobinHoodService;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private WorldScheduleService worldScheduleService;

    /**
     * 获取玩家所有装备
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb1.GetEquipsRs getEquips(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb1.GetEquipsRs.Builder builder = GamePb1.GetEquipsRs.newBuilder();
        for (Equip equip : player.equips.values()) {
            StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
            //兼容线上版本 判断获取的打造装备品质是不是为配置及以上, 如是则设置为已上锁
            if (equip.getEquipLocked() == 0) {
                if (Objects.nonNull(staticEquip) && staticEquip.getQuality() >= EquipConstant.EQUIP_AUTO_LOCK) {
                    equip.setEquipLocked(2);
                } else {
                    equip.setEquipLocked(1);
                }
            }
            builder.addEquip(PbHelper.createEquipPb(equip));
        }
        int now = TimeHelper.getCurrentSecond();
        dealBaptizeCnt(player, now);
        builder.setFree(player.common.getBaptizeCnt());
        // 免费洗练次数大于Constant.EQUIP_MAX_BAPTIZECNT
        if (player.common.getBaptizeTime() != now) {
            builder.setFreeTime(player.common.getBaptizeTime() + Constant.EQUIP_BAPTIZECNT_TIME);
        }
        builder.setBagBuy(player.common.getBagBuy());
        builder.setBagCnt(player.common.getBagCnt());
        // 返回所有装备信息的协议
        return builder.build();
    }

    /**
     * 装备打造
     *
     * @param roleId
     * @param equipId
     * @return
     * @throws MwException
     */
    public GamePb1.EquipForgeRs equipForge(long roleId, int equipId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        buildingDataManager.checkBuildingIsCreate(BuildingType.ORDNANCE_FACTORY, player);

        // 检查装备id是否存在
        StaticEquip staticEquip = checkEquipConfig(roleId, equipId);

        if (equipQueIsFull(player)) {
            throw new MwException(GameError.EQUIP_BUILD_TIME.getCode(), "装备正在打造, roleId:", roleId, ", equipId:",
                    equipId);
        }

        //打造红装检查世界进程
        int currSchedule = worldScheduleService.getCurrentSchduleId();
        if(staticEquip.getQuality() == Constant.Quality.red && currSchedule < Constant.FORGE_RED_EQUIP_SCHEDULE){
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId,"打造红装世界进程不符合",equipId,currSchedule));
        }

        // 检查打造装备需要消耗的资源是否足够
        rewardDataManager.checkPlayerResIsEnough(player, staticEquip.getMaterial());

        // 扣除消耗
        rewardDataManager.subPlayerResHasChecked(player, staticEquip.getMaterial(), true, AwardFrom.EQUIP_FORGE);

        EquipQue equipQue = beginForgeEquipQue(player, staticEquip);

        if (staticEquip.getQuality() >= Constant.Quality.blue) {
            // 打造排行
            activityDataManager.updRankActivity(player, ActivityConst.ACT_FORGE_RANK, 1);
        }

        //貂蝉任务-打造装备
        ActivityDiaoChanService.completeTask(player, ETask.MAKE_EQUIP,equipId,staticEquip.getQuality());
        //喜悦金秋-日出而作-装备打造
        TaskService.processTask(player, ETask.MAKE_EQUIP,equipId,staticEquip.getQuality());

        // 返回协议
        GamePb1.EquipForgeRs.Builder builder = GamePb1.EquipForgeRs.newBuilder();
        builder.setQue(PbHelper.createEquipQuePb(equipQue));
        taskDataManager.updTask(player, TaskType.COND_29, 1, equipId);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_HERO_EQUIPID_QUALITY, 1, staticEquip.getQuality());
        taskDataManager.updTask(player,TaskType.COND_994,1,staticEquip.getQuality());
        return builder.build();
    }

    /**
     * 开启装备打造队列
     *
     * @param player
     * @param staticEquip
     * @return
     */
    public EquipQue beginForgeEquipQue(Player player, StaticEquip staticEquip) {
        int needTime = staticEquip.getBuildTime();
        EquipQue equipQue = new EquipQue(player.maxKey(), staticEquip.getEquipId(), needTime,
                TimeHelper.getCurrentSecond() + needTime, 0, 0);
        // 开启装备打造队列，判断时间
        player.equipQue.add(equipQue);
        return equipQue;
    }

    /**
     * 检查装备id是否有配置，没有则抛出异常，有则返回配置信息
     *
     * @param roleId
     * @param equipId
     * @throws MwException
     */
    private StaticEquip checkEquipConfig(long roleId, int equipId) throws MwException {
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equipId);
        if (null == staticEquip) {
            throw new MwException(GameError.EQUIP_NO_CONFIG.getCode(), "装备未配置, roleId:", roleId, ", equipId:", equipId);
        }
        return staticEquip;
    }

    /**
     * 装备打造队列是否已满
     *
     * @param player
     * @return
     */
    public boolean equipQueIsFull(Player player) {
        return null == player || !player.equipQue.isEmpty();
    }

    /**
     * 装备打造加速
     *
     * @param roleId
     * @param type
     * @return
     */
    public GamePb1.SpeedForgeRs speedForge(long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (CheckNull.isEmpty(player.equipQue)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "装备打造加速，无队列, roleId:" + roleId);
        }
        EquipQue equipQue = player.equipQue.get(0);
        if (equipQue == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "装备打造加速，无队列, roleId:" + roleId);
        }
        int now = TimeHelper.getCurrentSecond();
        int second = equipQue.getEndTime() - now;
        if (second <= 0) {
            throw new MwException(GameError.EQUIP_SPEED_FORGE.getCode(), "装备打造加速，无队列, roleId:" + roleId);
        }

        if (type == -1) {
            // 金币加速
            int needGold = (int) Math.ceil(second * 1.00 / 60);
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold * 2,
                    AwardFrom.EQUIP_SPEED, equipQue.getEquipId());
        } else {
            // 判断是否可以免费加速
            Gains gains = player.gains.get(BuildingType.ORDNANCE_FACTORY);
            if (gains == null || gains.getEndTime() < TimeHelper.getCurrentSecond() || equipQue.getFreeCnt() > 0
                    || gains.getId() == equipQue.getEmployeId()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "装备打造加速，无免费次数, roleId:" + roleId + ",freeCnt=" + equipQue.getFreeCnt());
            }
            StaticCommandMult commandMult = StaticBuildingDataMgr.getCommandMult(gains.getId());
            if (commandMult == null || commandMult.getEffectType() != StaticBuildingDataMgr.CD_TYPE) {
                throw new MwException(GameError.PARAM_ERROR.getCode(),
                        "装备打造加速，无免费次数, roleId:" + roleId + ",commandMult=" + commandMult);
            }

            int staticSpeed = commandMult.getSpeedTime(player);
            if (equipQue.getFreeTime() > 0) {
                staticSpeed = equipQue.getFreeTime();
            }

            // 道具具体加速,并标记已加速
            second = Math.min(staticSpeed, second);
            equipQue.setFreeCnt(1);
            equipQue.setFreeTime(commandMult.getSpeedTime(player));
            equipQue.setEmployeId(commandMult.getId());
            LogUtil.debug("装备打造加速que=" + equipQue);
        }

        equipQue.setEndTime(equipQue.getEndTime() - second);
        SpeedForgeRs.Builder builder = SpeedForgeRs.newBuilder();
        builder.setEndTime(equipQue.getEndTime());
        builder.setFreeCnt(equipQue.getFreeCnt());
        taskDataManager.updTask(player, TaskType.COND_EQUIP_SPEED, 1);
        return builder.build();
    }

    /**
     * 穿戴、卸下装备
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb1.OnEquipRs onEquip(long roleId, OnEquipRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int heroId = req.getHeroId();

        // 检查奖励是否存在
        Hero hero = player.heros.get(heroId);
        if (null == hero) {
            throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "穿戴装备，玩家没有这个将领, roleId:", player.roleId,
                    ", heroId:", heroId);
        }

        // 非空闲状态
        if (!hero.isIdle() && hero.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId, ", heroId:",
                    heroId, ", state:", hero.getState());
        }

        boolean onEquip = req.getType() == PropConstant.EQUIP_ON;
        int equipKeyId = req.getKeyId();
        int newKeyId = 0;
        int pos;

        if (onEquip) {
            // 检查装备是否存在
            Equip equip = player.equips.get(equipKeyId);
            if (null == equip) {
                throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "玩家没有这个装备, roleId:", roleId, ", equipKeyId:",
                        equipKeyId);
            }

            // 检查装备是否已经被穿戴
            if (equip.isOnEquip()) {
                throw new MwException(GameError.EQUIP_HAS_ON_HERO.getCode(), "已经有将领穿戴了这个装备, roleId:", roleId,
                        ", equipKeyId:", equipKeyId, ", 穿戴该装备的将领id:", equip.getHeroId());
            }

            // 获取装备配置信息
            StaticEquip staticEquip = checkEquipConfig(roleId, equip.getEquipId());

            // 获取将领对应装备栏位上的数据
            pos = staticEquip.getEquipPart();
            int keyId = hero.getEquip()[pos];
            if (keyId > 0) {
                // 如果该栏位上已有装备，卸下
                rewardDataManager.checkBagCnt(player);
                downEquip(player, hero, keyId);
            }

            // 将领穿戴装备
            heroOnEquip(player, hero, pos, equipKeyId);
            // 记录穿戴后的新装备id
            newKeyId = equipKeyId;
        } else {
            // 检查装备栏上是否有这个装备
            if (!hero.hasEquip(equipKeyId)) {
                throw new MwException(GameError.EQUIP_NOT_ON_HERO.getCode(), "将领没有穿戴这个装备, roleId:", roleId, ", heroId:",
                        heroId, ", equipKeyId:", equipKeyId);
            }

            // 将领卸下装备
            rewardDataManager.checkBagCnt(player);
            pos = downEquip(player, hero, equipKeyId);
        }

        // 重新计算并更新将领属性
        CalculateUtil.processAttr(player, hero);

        // 返回协议
        GamePb1.OnEquipRs.Builder builder = GamePb1.OnEquipRs.newBuilder();
        builder.setHeroId(heroId);
        builder.setKeyId(newKeyId);
        builder.setPos(pos);
        builder.setHero(PbHelper.createHeroPb(hero, player));
        LogUtil.debug("装备hero新属性" + hero);
        return builder.build();
    }

    /**
     * 将领穿装备
     *
     * @param player
     * @param hero
     * @param pos
     * @param equipKeyId
     * @param upTask     是否更新任务进度
     */
    public void heroOnEquipIsUpTask(Player player, Hero hero, int pos, int equipKeyId, boolean upTask) {
        if (null == hero) {
            return;
        }
        Equip equip = player.equips.get(equipKeyId);
        if (null == equip) {
            return;
        }
        hero.onEquip(pos, equipKeyId);
        equip.onEquip(hero.getHeroId());

        // 获取装备配置信息
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
        if (null == staticEquip) {
            return;
        }
        // 增加装备属性加成
        for (Entry<Integer, Integer> entry : staticEquip.getAttr().entrySet()) {
            hero.getAttr()[entry.getKey()] += entry.getValue();
        }
        if (upTask) {
            taskDataManager.updTask(player, TaskType.COND_EQUIP, staticEquip.getEquipPart(), equip.getHeroId(),
                    staticEquip.getEquipPart());
            // 这里的schedule不传部位了
            taskDataManager.updTask(player, TaskType.COND_HERO_EQUIPID, 1, staticEquip.getEquipId());
            taskDataManager.updTask(player, TaskType.COND_501, 1, pos);
            taskDataManager.updTask(player, TaskType.COND_32, 1, pos);
            taskDataManager.updTask(player, TaskType.COND_519, 1, pos);
            taskDataManager.updTask(player, TaskType.COND_502, 1, equip.getAttrAndLv().stream().mapToInt(Turple::getB).max().orElse(0));
            taskDataManager.updTask(player, TaskType.COND_503, 1, equip.getAttrAndLv().stream().mapToInt(Turple::getB).max().orElse(0));
            taskDataManager.updTask(player, TaskType.COND_504, 1, staticEquip.getQuality());
            taskDataManager.updTask(player, TaskType.COND_505, 1, staticEquip.getQuality());
        }
    }

    /**
     * 将领穿戴装备
     *
     * @param player
     * @param hero
     * @param pos
     * @param equipKeyId
     */
    public void heroOnEquip(Player player, Hero hero, int pos, int equipKeyId) {
        heroOnEquipIsUpTask(player, hero, pos, equipKeyId, true);
    }

    /**
     * 将领卸下装备
     *
     * @param player
     * @param hero
     * @param equipKeyId
     */
    public int downEquip(Player player, Hero hero, int equipKeyId) {
        if (null == hero) {
            return 0;
        }
        // 将领卸下装备
        int pos = hero.downEquip(equipKeyId);
        Equip equip = player.equips.get(equipKeyId);
        if (null != equip) {
            equip.downEquip();// 装备卸下
            // CalculateUtil.processAttr(player, hero);
            CalculateUtil.returnArmy(player, hero);
        }

        return pos;
    }

    public void equipQueTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.isActive()) {
                try {
                    // if (!player.equipQue.isEmpty()) {
                    // dealBuildQue(player, now);
                    // }
                    processEquipQue(player);
                } catch (Exception e) {
                    LogUtil.error("打造队列定时器报错, lordId:" + player.lord.getLordId(), e);
                }

                /*for (EquipQue que : player.equipQue) {
                    // 如果装备打造完成，推送给玩家
                    if (que.getEndTime() <= now) {
                        String key = StringHelper.mergeToKey(PushConstant.ID_EQUIP_FORGE_FINISH, que.getEquipId());
                        Integer status = player.getPushRecord(key);
                        if (null == status || status == PushConstant.PUSH_NOT_PUSHED) {
                            // player.putPushRecord(key, PushConstant.PUSH_HAS_PUSHED);
                            pushEquipForgeFinish(player.account, que.getEquipId());
                        }
                    }
                }*/
            }
        }
    }

    /**
     * 推送装备打造完成消息
     *
     * @param account
     * @param equipId
     */
    private void pushEquipForgeFinish(Account account, int equipId) {
        StaticEquip equip = StaticPropDataMgr.getEquip(equipId);
        if (null == equip) {
            LogUtil.error("推送装备打造完成，装备不存在, roleId:", account.getLordId(), ", equipId:", equipId);
            return;
        }
        String id = "s_equip_" + equipId;
        String equipName = StaticIniDataMgr.getTextName(id);
        if (null == equipName) {
            LogUtil.error("装备名称获取不到，跳过推送, roleId:", account.getLordId(), ", id:", equipId);
            return;
        }
        // 推送消息
        // PushMessageUtil.pushMessage(account, PushConstant.ID_EQUIP_FORGE_FINISH, equipName);
    }

    public void dealBaptizeCnt(Player player, int now) {
        if (player.common != null && player.common.getBaptizeCnt() < Constant.EQUIP_MAX_BAPTIZECNT
                && now >= player.common.getBaptizeTime() + Constant.EQUIP_BAPTIZECNT_TIME) {
            int cnt = (now - player.common.getBaptizeTime()) / Constant.EQUIP_BAPTIZECNT_TIME;
            int baptizeCnt = Math.min(Constant.EQUIP_MAX_BAPTIZECNT, player.common.getBaptizeCnt() + cnt);
            LogLordHelper.commonLog("addBaptizeCnt", AwardFrom.BAPTIZE_CNT, player, baptizeCnt - player.common.getBaptizeCnt(), player.common.getBaptizeCnt());
            player.common.setBaptizeCnt(baptizeCnt);
            player.common.setBaptizeTime(player.common.getBaptizeTime() + (cnt * Constant.EQUIP_BAPTIZECNT_TIME));
        }
        if (player.common != null && player.common.getBaptizeCnt() >= Constant.EQUIP_MAX_BAPTIZECNT) {
            player.common.setBaptizeTime(now);
        }
    }

    private void dealBuildQue(Player player, int now) {
        Iterator<EquipQue> it = player.equipQue.iterator();
        while (it.hasNext()) {
            EquipQue buildQue = it.next();
            if (now >= buildQue.getEndTime()) {
                dealOneQue(player, buildQue);
                it.remove();
                continue;
            }
        }
    }

    public void dealOneQue(Player player, EquipQue buildQue) {
        Equip equip = new Equip();
        equip.setKeyId(buildQue.getKeyId());
        equip.setEquipId(buildQue.getEquipId());
        player.equips.put(buildQue.getKeyId(), equip);
        // 初始附加属性
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(buildQue.getEquipId());
        //判断获取的打造装备品质是不是为配置的及以上, 如是则设置为已上锁
        if (Objects.nonNull(staticEquip)) {
            if (staticEquip.getQuality() >= EquipConstant.EQUIP_AUTO_LOCK) {
                equip.setEquipLocked(2);
            } else {
                equip.setEquipLocked(1);
            }
        }
        StaticEquipQualityExtra staticEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                .get(staticEquip.getQuality());
        if (staticEquipQualityExtra != null && staticEquipQualityExtra.getExtraNum() > 0) {
            List<Turple<Integer, Integer>> attrLv = equip.getAttrAndLv();
            for (int j = 0; j < staticEquipQualityExtra.getExtraNum(); j++) {
                attrLv.add(new Turple<>(Constant.ATTRS[RandomUtils.nextInt(0, Constant.ATTRS.length)],
                        staticEquipQualityExtra.getExtraLv()));
            }
        }
        // 记录玩家获得新装备
        LogLordHelper.equip(AwardFrom.EQUIP_FORGE, player.account, player.lord, buildQue.getEquipId(), equip.getKeyId(),
                Constant.ACTION_ADD);
        playerDataManager.createRoleOpt(player, Constant.OptId.id_5, buildQue.getEquipId() + "");
        // 打造推送
    }

    /**
     * 装备洗练定时器
     */
    public void washEquipTimer() {
        int now = TimeHelper.getCurrentSecond();
        playerDataManager.getPlayers().values().stream().forEach(p -> {
            if (p.common != null && p.common.getBaptizeCnt() < Constant.EQUIP_MAX_BAPTIZECNT) {
                dealBaptizeCnt(p, now);
                if (p.common.getBaptizeCnt() >= Constant.EQUIP_MAX_BAPTIZECNT) {
                    // PushMessageUtil.pushMessage(p.account, PushConstant.WASH_EQUIP_IS_FULL);
                }
            }
        });
    }

    /**
     * 装备洗练
     *
     * @param roleId
     * @param keyId
     * @param useGold
     * @return
     * @throws MwException
     */
    public EquipBaptizeRs equipBaptize(Long roleId, int keyId, boolean useGold, boolean superGold) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        buildingDataManager.checkBuildingIsCreate(BuildingType.REMAKE, player);
        Equip equip = player.equips.get(keyId);
        if (equip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "没有这个装备, roleId:", roleId, ", keyId:", keyId);
        }
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
        StaticEquipQualityExtra sWashEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                .get(staticEquip.getWashQuality());
        // 额外技能是按照 quality,其他按照washQuality
        StaticEquipQualityExtra sEquipQualityExtra = StaticPropDataMgr.getQualityMap().get(staticEquip.getQuality());
        if (sEquipQualityExtra == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "装备Quality配置出错, roleId:", roleId, ", keyId:", keyId,
                    ",equipId:", equip.getEquipId(), ",quality=", staticEquip.getQuality());
        }
        if (sWashEquipQualityExtra == null || sWashEquipQualityExtra.getExtraNum() <= 0
                || equip.getAttrAndLv().size() <= 0) {
            throw new MwException(GameError.BAPTIZE_NO.getCode(), "次装备不能洗练, roleId:", roleId, ", keyId:", keyId,
                    ",equipId:", equip.getEquipId(), ",quality=", staticEquip.getQuality());
        }

        // 是否是戒指
        boolean ring = EquipConstant.isRingEquip(staticEquip.getEquipPart());

        // 技能种类随机(权重)
        List<List<Integer>> skillProbability = ring ?
                EquipConstant.RING_SKILL_PROBABILITY :
                EquipConstant.EQUIP_SKILL_PROBABILITY;
        // 洗练等级概率(万分比)
        Map<Integer, Integer> lvProbability = ring ?
                EquipConstant.RING_LV_PROBABILITY :
                EquipConstant.EQUIP_LV_PROBABILITY;
        // 洗练保底配置
        Map<Integer, Integer> baptizeMin = ring ?
                EquipConstant.RING_BAPTIZE_MIN_UPLV_CONF :
                Constant.EQUIP_BAPTIZE_MIN_UPLV_CONF;

        int now = TimeHelper.getCurrentSecond();
        dealBaptizeCnt(player, now);
        int gold;

        // 秘籍洗练
        if (superGold) {
            if (staticEquip.getWashQuality() < EquipConstant.EQUIP_4_SKILL_QUALITY || !equip
                    .isAllLvMax(sWashEquipQualityExtra.getMaxLv())) {
                throw new MwException(GameError.BAPTIZE_NEED_FULL_LV.getCode(), "未满级不能秘籍洗练, roleId:", roleId,
                        ", keyId:", keyId, ", equip=", equip.getEquipId(), ", maxLv=",
                        sWashEquipQualityExtra.getMaxLv());
            }
            if (!techDataManager.isOpen(player, TechConstant.TYPE_21)) {
                throw new MwException(GameError.BAPTIZE_NEED_TECH.getCode(), "未开启秘籍洗练, roleId:", roleId, ", keyId:",
                        keyId);
            }

            // 排除上次一样的
            Integer oldAttr = equip.getAttrAndLv().get(0).getA();
            List<List<Integer>> attrList = new ArrayList<>();
            for (List<Integer> item : skillProbability) {
                if (item.get(0) == oldAttr.intValue()) {
                    continue;
                }
                attrList.add(item);
            }
            Integer attrId = RandomUtil.getRandomByWeight(attrList);
            if (attrId == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "装备洗练属性随机配置格式错误");
            }

            if (ring) {
                // 戒指超级洗练消耗
                rewardDataManager.checkAndSubPlayerRes(player, EquipConstant.RING_SUPER_BAPTIZE_COST,
                        AwardFrom.EQUIP_BAPTIZE_CHEATS, equip.getEquipId(), LogUtil.obj2ShortStr(equip.getAttrAndLv()));
            } else {
                int propId = PropConstant.PROP_EQUIP_WASP;
                long propCnt = rewardDataManager.getRoleResByType(player, AwardType.PROP, propId);
                if (propCnt >= 1) {
                    rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP,  propId, 1, AwardFrom.EQUIP_BAPTIZE_CHEATS, equip.getEquipId(), LogUtil.obj2ShortStr(equip.getAttrAndLv()));
                } else {
                    // 道具不足
                    gold = Constant.EQUIP_GOLD_BAPTIZE_2;
                    rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, gold,
                            AwardFrom.EQUIP_BAPTIZE_CHEATS, equip.getEquipId(), LogUtil.obj2ShortStr(equip.getAttrAndLv()));
                }
            }

            equip.getAttrAndLv().clear();

            for (int i = 0; i < sWashEquipQualityExtra.getExtraNum() + 1; i++) {
                Turple<Integer, Integer> attrLv = new Turple<>(attrId, sWashEquipQualityExtra.getMaxLv());
                equip.getAttrAndLv().add(attrLv);
            }
        } else {
            // 先随机出属性值,以免配置错误,扣了钱确没加属性
            List<Integer> attrIds = new ArrayList<>();
            for (int i = 0; i < sEquipQualityExtra.getExtraNum(); i++) {
                Integer attrId = RandomUtil.getRandomByWeight(skillProbability);
                if (attrId == null) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "装备洗练属性随机配置格式错误");
                }
                attrIds.add(attrId);
            }
            if (useGold) {
                if (ring) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "戒指洗练属性不能使用金币");
                }
                // 根据装备算价格
                gold = Constant.EQUIP_GOLD_BAPTIZE_1;
                rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, gold, "装备洗练");
                rewardDataManager
                        .subMoney(player, AwardType.Money.GOLD, gold, AwardFrom.EQUIP_BAPTIZE, equip.getEquipId());
            } else {
                if (ring) {
                    // 戒指普通洗练消耗
                    rewardDataManager
                            .checkAndSubPlayerRes(player, EquipConstant.RING_BAPTIZE_COST, AwardFrom.EQUIP_BAPTIZE,
                                    equip.getEquipId());
                } else {
                    if (player.common.getBaptizeCnt() <= 0) {
                        throw new MwException(GameError.BAPTIZE_NO_FREECNT.getCode(), "无免费洗练次数, roleId:", roleId,
                                ", keyId:", keyId);
                    }
                    player.common.setBaptizeCnt(player.common.getBaptizeCnt() - 1);
                    LogLordHelper.commonLog("subBaptizeCnt", AwardFrom.BAPTIZE_CNT, player, 1, player.common.getBaptizeCnt());
                    // 免费改造次数减为0
                    if (player.common.getBaptizeCnt() == 0) {
                        activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_EQUIP_BAPTIZE, player);
                    }
                }
            }

            // 属性设置
            for (int i = 0; i < attrIds.size(); i++) {
                equip.getAttrAndLv().get(i).setA(attrIds.get(i));
            }

            if (equip.isAllLvMax(sWashEquipQualityExtra.getMaxLv())) {
                // 等级全满
                if (equip.getAttrAndLv().size() >= 4) {
                    // 秘籍洗没啦
                    equip.getAttrAndLv().remove(3);
                }
            } else {
                // 获取最小等级
                Turple<Integer, Integer> minLv = equip.getAttrAndLv().stream()
                        .min(Comparator.comparingInt(p -> p.getB().intValue())).get();
                // 最小等级的下标
                Integer probability = lvProbability.get(Integer.valueOf(minLv.getB() + 1));
                if (probability == null) {
                    probability = 0;
                }

                // 加上等级
                // 优先保底值
                Integer cnt = baptizeMin.get(minLv.getB());
                if (cnt != null && equip.getNotUpLvCnt() >= (cnt - 1)) {
                    // 保底启用
                    minLv.setB(minLv.getB() + 1);
                    // 保底清零
                    equip.setNotUpLvCnt(0);
                } else {
                    boolean isUpLv = RandomHelper.isHitRangeIn10000(probability);
                    if (isUpLv) {
                        minLv.setB(minLv.getB() + 1);
                        // 保底清零
                        equip.setNotUpLvCnt(0);
                    } else {
                        equip.setNotUpLvCnt(equip.getNotUpLvCnt() + 1);
                    }
                }
                LogUtil.debug("=========保底值：" + equip.getNotUpLvCnt() + "============");
            }

            // 普通触发三星秘籍
            // TODO: 2021/8/24 秦岭说去掉普通改造触发秘籍的功能
//            if (techDataManager.isOpen(player, TechConstant.TYPE_21)) {
//                triggerEquip4Skill(equip);
//            }
        }
        // 洗炼排行榜
        activityDataManager.updRankActivity(player, ActivityConst.ACT_REMOULD_RANK, 1);
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_EQUIP_WASH);
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_EQUIP_WASH_CNT,  1);
        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_EQUIP_WASH_CNT, 1);

        //貂蝉任务-改造装备
        ActivityDiaoChanService.completeTask(player,ETask.REFORM_EQUIP,equip.getEquipId());
        TaskService.processTask(player,ETask.REFORM_EQUIP,equip.getEquipId());

        EquipBaptizeRs.Builder builder = EquipBaptizeRs.newBuilder();
        builder.setKeyId(keyId);
        builder.setFree(player.common.getBaptizeCnt());
        // 免费洗练次数大于Constant.EQUIP_MAX_BAPTIZECNT
        if (player.common.getBaptizeCnt() < Constant.EQUIP_MAX_BAPTIZECNT) {
            builder.setFreeTime(player.common.getBaptizeTime() + Constant.EQUIP_BAPTIZECNT_TIME);
        }
        builder.setUseGold(player.lord.getGold());

        for (int i = 0; i < equip.getAttrAndLv().size(); i++) {
            builder.addAttr(TwoInt.newBuilder().setV1(equip.getAttrAndLv().get(i).getA())
                    .setV2(equip.getAttrAndLv().get(i).getB()));
        }
        // 重新计算并更新将领属性
        if (equip.getHeroId() > 0) {
            Hero hero = player.heros.get(equip.getHeroId());
            if (hero != null && (hero.getPos() > 0 || hero.getAcqPos() > 0 || hero.getWallPos() > 0 || hero.getCommandoPos() > 0)) {
                CalculateUtil.processAttr(player, hero);
                CalculateUtil.returnArmy(player, hero);
            }
        }
        taskDataManager.updTask(player, TaskType.COND_EQUIP_BAPTIZE, 1);
        taskDataManager.updTask(player, TaskType.COND_502, 1, equip.getAttrAndLv().stream().mapToInt(Turple::getB).max().orElse(0));
        taskDataManager.updTask(player, TaskType.COND_503, 1, equip.getAttrAndLv().stream().mapToInt(Turple::getB).max().orElse(0));
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_EQUIP_BAPTIZE, 1);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_EQUIP_BAPTIZE, 1);
        return builder.build();
    }

    /**
     * 科技升级触发三星秘籍
     *
     * @param equip
     * @return true 为触发升级
     */
    public boolean triggerEquip4Skill(Equip equip) {
        if (equip.getAttrAndLv().size() < 3) {
            return false;
        }
        List<Turple<Integer, Integer>> attrLvs = equip.getAttrAndLv();
        boolean isBingo = true;
        for (int i = 0; i < attrLvs.size(); i++) {
            if (attrLvs.get(0).getA().intValue() != attrLvs.get(i).getA().intValue())
                isBingo = false;
        }

        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
        StaticEquipQualityExtra staticEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                .get(staticEquip.getWashQuality());
        if (staticEquip.getWashQuality() >= EquipConstant.EQUIP_4_SKILL_QUALITY && equip
                .isAllLvMax(staticEquipQualityExtra.getMaxLv()) && isBingo && equip.getAttrAndLv().size() == 3) {
            int attrId = attrLvs.get(0).getA();
            int lv = attrLvs.get(0).getB();
            equip.getAttrAndLv().add(new Turple<>(attrId, lv));
            return true;
        }
        return false;
    }

    /**
     * 装备打造领取
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public EquipGainRs equipGain(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        if (player.equipQue == null || player.equipQue.isEmpty()) {
            throw new MwException(GameError.EQUIP_BUILD_TIME.getCode(), "领取装备打造,装备列表不存在或者时间未到, roleId:", roleId);
        }
        // 获取领取的装备打造列队
        EquipQue buildQue = player.equipQue.get(0);
        // 获取打造装备信息
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(buildQue.getEquipId());
        if (buildQue == null || now < buildQue.getEndTime()) {
            throw new MwException(GameError.EQUIP_BUILD_TIME.getCode(), "领取装备打造,装备列表不存在或者时间未到, roleId:", roleId,
                    ", buildQue:", buildQue);
        }
        // 检查背包满
        rewardDataManager.checkBagCnt(player);
        EquipGainRs.Builder builder = EquipGainRs.newBuilder();

        // 初始化装备信息
        dealOneQue(player, buildQue);

        // 是否是戒指
        boolean ring = EquipConstant.isRingEquip(staticEquip.getEquipPart());

        // 技能种类随机(权重)
        List<List<Integer>> skillProbability = ring ?
                EquipConstant.RING_SKILL_PROBABILITY :
                EquipConstant.EQUIP_SKILL_PROBABILITY;

        // 紫色 橙色装备打造 发送世界消息
        if (staticEquip != null && (staticEquip.getQuality() == Constant.Quality.purple
                || staticEquip.getQuality() == Constant.Quality.orange || staticEquip.getQuality() == Constant.Quality.red)) {

            // 获取排名信息
            Map<Integer, List<Long>> rankEquips = globalDataManager.getGameGlobal().getTrophy().getRankEquips();
            // 根据装备id 查询当前装备的上榜角色数量
            List<Long> lordIds = rankEquips.get(staticEquip.getEquipId());
            // 当前还无人上榜
            if (lordIds == null) {
                lordIds = new ArrayList<>();
            }
            // 打造装备推送消息 配置的最低排名值
            int rankNum = staticEquip.getQuality() == Constant.Quality.purple ?
                    Constant.PART_PURPLE_RANK :
                    Constant.PART_ORANGE_RANK;
            // 已上榜的玩家数量少于配置的排名数 说明可以上榜
            if (lordIds.size() < rankNum) {
                // 判断自己是否已经上榜
                if (!lordIds.contains(player.lord.getLordId())) {
                    // 还没上榜
                    lordIds.add(player.lord.getLordId());
                    // 更新排名数
                    globalDataManager.getGameGlobal().getTrophy().getRankEquips()
                            .put(staticEquip.getEquipId(), lordIds);
                    int chatId = staticEquip.getQuality() == Constant.Quality.purple ?
                            ChatConst.CHAT_MAKE_PURPLE_EQUIPMENT_AKM : staticEquip.getQuality() == Constant.Quality.orange ? ChatConst.CHAT_MAKE_ORANGE_EQUIPMENT_AKM:ChatConst.CHAT_MAKE_RED_EQUIPMENT;
                    chatDataManager.sendSysChat(chatId, player.lord.getCamp(), 0, player.lord.getCamp(), player.lord.getNick(),lordIds.size(), staticEquip.getEquipId());
                }
            }
        }

        // 判断打造品质 紫装以上 极品装备科技
        if (staticEquip.getQuality() >= Constant.Quality.purple) {
            // 判断玩家是否有 极品装备 科技加成
            double techEff = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_23);
            // 有科技加成
            if (techEff > 0.0) {
                Equip equip = player.equips.get(buildQue.getKeyId());

                // 记录打造次数
                player.setMixtureData(PlayerConstant.EQUIP_MAKE_COUNT,
                        player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_COUNT) + 1);
                player.setMixtureData(PlayerConstant.EQUIP_MAKE_NUM,
                        player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_NUM) + 1);

                // 判断玩家是否是第一次打造 或者 矩阵配置有变更
                if (player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_COUNT) == 1 || // 第一次
                        player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_RANGE) != Constant.EQUIP_MAKE_RANGE.get(0)
                        || // 矩阵范围 变更
                        player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_PROBABILITY) != (int) techEff || // 概率变更
                        player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_INTERVAL) != Constant.EQUIP_MAKE_RANGE
                                .get(1) || // 间隔值变更
                        player.cheatCode == null) { // 数据被清空
                    // 初始化 命中值
                    initCheatCode(techEff, player);
                    LogUtil.debug("roleId:", player.roleId, ",极品装备科技概率矩阵初始化---techEff=" + techEff + ",count=" + 1);
                }
                // 开始抽奖 根据当前回合的打造次数查询是否中奖
                if (isExist(player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_NUM), player.cheatCode)) {// 恭喜中奖
                    // 执行中奖操作
                    List<List<Integer>> attrList = new ArrayList<>();
                    for (List<Integer> item : skillProbability) {
                        attrList.add(item);
                    }
                    Integer attrId = RandomUtil.getRandomByWeight(attrList);
                    if (attrId == null) {
                        throw new MwException(GameError.NO_CONFIG.getCode(), "装备洗练属性随机配置格式错误");
                    }

                    equip.getAttrAndLv().clear();

                    StaticEquipQualityExtra sWashEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                            .get(staticEquip.getWashQuality());

                    for (int i = 0; i < sWashEquipQualityExtra.getExtraNum() + 1; i++) {
                        Turple<Integer, Integer> attrLv = new Turple<Integer, Integer>(attrId,
                                sWashEquipQualityExtra.getMaxLv());
                        equip.getAttrAndLv().add(attrLv);
                    }

                    // 发送公告
                    chatDataManager
                            .sendSysChat(ChatConst.THE_BEST_EQUIP, player.lord.getCamp(), 0, player.lord.getCamp(),
                                    player.lord.getNick(), staticEquip.getEquipId());
                }
                // 判断该次抽奖是否是本回合最后一次
                if (player.getMixtureDataById(PlayerConstant.EQUIP_MAKE_NUM) == Constant.EQUIP_MAKE_RANGE.get(0)) {
                    // 重新计算当前回合打造次数 并初始化命中值
                    initCheatCode(techEff, player);
                    LogUtil.debug("roleId:", player.roleId, ",极品装备科技概率矩阵初始化---techEff=" + techEff + ",count=" + player
                            .getMixtureDataById(PlayerConstant.EQUIP_MAKE_COUNT));
                }
            }
        }

        int equipId = buildQue.getEquipId();
        player.equipQue.clear();
        builder.setEquip(PbHelper.createEquipPb(player.equips.get(buildQue.getKeyId())));
        taskDataManager.updTask(player, TaskType.COND_EQUIP_BUILD, 1, equipId);

        // 重置装备打造完成消息推送状态
        // player.removePushRecord(StringHelper.mergeToKey(PushConstant.ID_EQUIP_FORGE_FINISH, equipId));

        return builder.build();
    }

    /**
     * 装备上锁
     */
    public GamePb2.EquipLockedRs equipLocked(Long roleId, GamePb2.EquipLockedRq rq) throws MwException {
        if (rq.getKeyId() == 0 || rq.getEquipLocked() == 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端参数错误, roleId:", roleId, ", keyId:", rq.getKeyId());
        }
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Equip equip = player.equips.get(rq.getKeyId());
        if (equip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "上锁时,没有这个装备, roleId:", roleId, ", keyId:", rq.getKeyId());
        }
        if (equip.isOnEquip()) {
            throw new MwException(GameError.EQUIP_HAS_ON_HERO.getCode(), "已经有将领穿戴了这个装备, roleId:", roleId,
                    ", 穿戴该装备的将领id:", equip.getHeroId());
        }
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
        if (staticEquip == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "装备上锁, 没有对应装备配置, roleId:", roleId, ", keyId:", equip.getEquipId());
        }
        if (rq.getEquipLocked() != 1) {
            equip.setEquipLocked(2);
        } else {
            if (staticEquip.getQuality() >= 12) {
                throw new MwException(GameError.MYTH_SIXTH_ORDER_EQUIP_NOT_UNLOCK.getCode(), "品质12的装备不能解锁, roleId:", roleId, ", keyId:", equip.getEquipId());
            }
            equip.setEquipLocked(1);
        }
        GamePb2.EquipLockedRs.Builder equipLockedRs = GamePb2.EquipLockedRs.newBuilder();
        equipLockedRs.setKeyId(rq.getKeyId());
        equipLockedRs.setEquipLocked(equip.getEquipLocked());
        return equipLockedRs.build();
    }

    /**
     * 装备批量分解
     */
    public GamePb2.EquipBatchDecomposeRs equipBatchDecompose(Long roleId, GamePb2.EquipBatchDecomposeRq rq) throws MwException {
        if (null == rq.getKeyIdList() || rq.getKeyIdList().isEmpty()) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端参数错误, roleId:", roleId);
        }

        List<Integer> keyidList = new ArrayList<>();
        List<CommonPb.Award> awardList = new ArrayList<>();
        for (Integer keyId : rq.getKeyIdList()) {
            EquipDecomposeRs equipDecomposeRs = this.equipDecompose(roleId, keyId);
            keyidList.add(keyId);
            awardList.addAll(equipDecomposeRs.getAwardList());
        }
        GamePb2.EquipBatchDecomposeRs.Builder equipBatchDecomposeRs = GamePb2.EquipBatchDecomposeRs.newBuilder();
        keyidList.forEach(equipBatchDecomposeRs::addKeyId);
        awardList.forEach(equipBatchDecomposeRs::addAward);
        return equipBatchDecomposeRs.build();
    }

    /**
     * 装备分解
     *
     * @param roleId
     * @param keyId
     * @return
     * @throws MwException
     */
    public EquipDecomposeRs equipDecompose(Long roleId, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Equip equip = player.equips.get(keyId);
        if (equip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "分解时,没有这个装备, roleId:", roleId, ", keyId:",
                    keyId);
        }
        if (equip.isOnEquip()) {
            throw new MwException(GameError.EQUIP_HAS_ON_HERO.getCode(), "已经有将领穿戴了这个装备, roleId:", roleId,
                    ", 穿戴该装备的将领id:", equip.getHeroId());
        }
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
        if (staticEquip == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "分解时,没有这个装备配置, roleId:", roleId, ", keyId:", keyId);
        }
        if (!EquipConstant.isRingEquip(staticEquip.getEquipPart()) && staticEquip.getQuality() >= 6) {
            throw new MwException(GameError.EQUIP_NOT_DECOMPOSE.getCode(), "品质6的装备不能分解  roleId:", roleId, ", equipId:",
                    staticEquip.getEquipId());
        }
        if (equip.getEquipLocked() == 2) {
            throw new MwException(GameError.EQUIP_LOCKED.getCode(), "装备已上锁  roleId:", roleId, ", equipId:",
                    staticEquip.getEquipId());
        }
        EquipDecomposeRs.Builder builder = EquipDecomposeRs.newBuilder();
        if (equip instanceof Ring) {
            Ring ring = (Ring) equip;
            if (ring.getJewels().size() > 0) {
                int jewel = ring.getJewels().get(0);
                if (jewel > 0) {
                    ring.downJewel(jewel);
                    EquipJewel equipJewel = player.equipJewel.get(jewel);
                    // 减去一个被镶嵌数量
                    equipJewel.getInlaid().decrementAndGet();
                    builder.addAward(PbHelper.createAwardPb(AwardType.JEWEL, jewel, 1));
                }
            }
        }

        rewardDataManager.subEquip(player, keyId, AwardFrom.EQUIP_DECOMPOSE);

        builder.addAllAward(rewardDataManager
                .addAwardDelaySync(player, staticEquip.getDecompose(), null, AwardFrom.EQUIP_DECOMPOSE));

        // 额外图纸
        if (player.shop.getVipId().contains(Constant.EQUIP_DECOMPOSE_VIP_BAG) && RandomHelper
                .isHitRangeIn100(Constant.EQUIP_DECOMPOSE_RATE) && !staticEquip.getDecompose2().isEmpty()) {
            builder.addAllAward(rewardDataManager
                    .addAwardDelaySync(player, staticEquip.getDecompose2(), null, AwardFrom.EQUIP_DECOMPOSE));
        }

        builder.setKeyId(keyId);
        return builder.build();
    }

    /**
     * 背包扩容
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public BagExpandRs bagExpand(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        BagExpandRs.Builder builder = BagExpandRs.newBuilder();

        // 检查并执行扩充背包逻辑
        checkAndExpandBag(player);

        builder.setCount(player.common.getBagCnt());
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    public void checkAndExpandBag(Player player) throws MwException {
        int needGold = 0;
        int add = 0;
        for (List<Integer> kv : Constant.BAG_BUY) {
            if (kv.get(0) == player.common.getBagBuy() + 1) {
                needGold = kv.get(1);
                add = kv.get(2);
            }
        }
        if (needGold == 0 || add == 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "背包扩容时,没有这个装备配置, roleId:", player.roleId, ", 购买次数:",
                    player.common.getBagBuy());
        }
        rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "背包扩容");
        rewardDataManager.subMoney(player, AwardType.Money.GOLD, needGold, AwardFrom.BAG_EXPAND);
        player.common.setBagBuy(player.common.getBagBuy() + 1);
        player.common.setBagCnt(player.common.getBagCnt() + add);
    }

    /**
     * 国器信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetSuperEquipRs getSuperEquip(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        processEquipQue(player);
        GetSuperEquipRs.Builder builder = GetSuperEquipRs.newBuilder();
        for (Entry<Integer, SuperEquip> kv : player.supEquips.entrySet()) {
            builder.addEquip(PbHelper.createSuperEquipPb(kv.getValue()));
        }
        builder.addAllQue(player.supEquipQue);
        return builder.build();
    }

    /**
     * 国器打造
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public SuperEquipForgeRs superEquipForge(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.supEquips.containsKey(type)) {
            throw new MwException(GameError.SUPER_EQUIP_EXIST.getCode(), "已有此国器, roleId:", roleId, ", type:", type);
        }
        // 等级限制
        StaticSuperEquip staticSuperEquip = StaticPropDataMgr.getSuperEquip(type);
        if (staticSuperEquip.getNeedRoleLv() > player.lord.getLevel()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "已有此国器, roleId:", roleId, ", type:", type);
        }


        // 同步油点的消耗
        ChangeInfo change = ChangeInfo.newIns();

        // 扣材料
        int needOil = staticSuperEquip.getNeedOil();
        if (needOil > 0) {
            if (!rewardDataManager
                    .checkResource(player, AwardType.RESOURCE, AwardType.Resource.OIL, needOil)) {
                throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), "油不足, roleId:", roleId, ", type:", type);
            }
            rewardDataManager.subResource(player, AwardType.Resource.OIL, needOil,
                    AwardFrom.SUPER_EQUIP_CREATE);// , "国器打造"
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.OIL);
        }

        int needElec = staticSuperEquip.getNeedElec();
        if (needElec > 0) {
            if (!rewardDataManager
                    .checkResource(player, AwardType.RESOURCE, AwardType.Resource.ELE, needElec)) {
                throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), "电不足, roleId:", roleId, ", type:", type);
            }
            rewardDataManager.subResource(player, AwardType.Resource.ELE, needElec,
                    AwardFrom.SUPER_EQUIP_CREATE);// , "国器打造"
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.ELE);
        }

        rewardDataManager.checkAndSubPlayerRes(player, staticSuperEquip.getMaterial(), AwardFrom.SUPER_EQUIP_CREATE);
        taskDataManager.updTask(player, TaskType.COND_SUPER_EQUIP, 1, type);
        // 向客户端同步玩家资源数据
        rewardDataManager.syncRoleResChanged(player, change);
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_EQUIP_BUILD);
        TwoInt.Builder que = TwoInt.newBuilder();
        que.setV1(type).setV2(TimeHelper.getCurrentSecond() + staticSuperEquip.getNeedTime());
//        player.supEquipQue.add(que.build());
        //打造立即完成，不在走定时器。定时器会导致主线任务打造完成不能立马完成
        player.supEquips.put(type, new SuperEquip(type, 1, 0, 1, 0));
        SuperEquipForgeRs.Builder builder = SuperEquipForgeRs.newBuilder();
        builder.setQue(que);
        builder.setResource(PbHelper.createCombatPb(player.resource));
        return builder.build();
    }

    /**
     * 国器升级
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public UpSuperEquipRs upSuperEquip(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        processEquipQue(player);
        SuperEquip superEquip = player.supEquips.get(type);
        if (superEquip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "没有这个装备, roleId:", roleId, ", type:", type);
        }

        StaticSuperEquipLv staticSuperEquipLv = StaticPropDataMgr
                .getSuperEquipLv(superEquip.getType(), superEquip.getLv() + 1);
        if (staticSuperEquipLv == null) {
            throw new MwException(GameError.SUPER_EQUIP_LV_FULL.getCode(), "等级已达上限, roleId:", roleId, ", type:", type);
        }
        if (superEquip.getLv() >= player.lord.getLevel()) {
            throw new MwException(GameError.SUPER_EQUIP_LV_FULL.getCode(), "等级已达上限, roleId:", roleId, ", roleLv:",
                    player.lord.getLevel(), ", superEquipLv:", superEquip.getLv());
        }
        staticSuperEquipLv = StaticPropDataMgr.getSuperEquipLv(superEquip.getType(), superEquip.getLv());

        if (staticSuperEquipLv.getNeedGrowLv() > superEquip.getGrowLv()) {
            throw new MwException(GameError.SUPER_EQUIP_NEED_GROW.getCode(), "等级已达上限,请进阶 roleId:", roleId, ", type:",
                    type);
        }
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.ORE,
                staticSuperEquipLv.getNeedOre(), AwardFrom.SUPER_EQUIP_LV);

        int addExp = staticSuperEquipLv.getStep() * superEquip.getBomb();
        if (Constant.SUPER_EQUIP_MAX_STEP > superEquip.getStep() + addExp) {
            superEquip.setStep(Math.min(Constant.SUPER_EQUIP_MAX_STEP, superEquip.getStep() + addExp));

            // 根据科技随机下次暴击
            StaticSuperEquipBomb staticSuperEquipBomb = StaticPropDataMgr
                    .getSuperEquipBomb(techDataManager.getTechLv(player, TechConstant.TYPE_14));
            if (staticSuperEquipBomb != null) {
                int key = RandomUtil.getKeyByMap(staticSuperEquipBomb.getBomb(), 0);
                LogUtil.debug(roleId + ",超武突破暴击=" + key + ",id=" + staticSuperEquipBomb.getLv());
                superEquip.setBomb(key);
            }
        } else {
            staticSuperEquipLv = StaticPropDataMgr.getSuperEquipLv(superEquip.getType(), superEquip.getLv() + 1);
            if (staticSuperEquipLv != null) {
                superEquip.setStep(superEquip.getStep() + addExp - Constant.SUPER_EQUIP_MAX_STEP);
                superEquip.setLv(superEquip.getLv() + 1);

                // 根据科技随机下次暴击
                StaticSuperEquipBomb staticSuperEquipBomb = StaticPropDataMgr
                        .getSuperEquipBomb(techDataManager.getTechLv(player, TechConstant.TYPE_14));
                if (staticSuperEquipBomb != null) {
                    int key = RandomUtil.getKeyByMap(staticSuperEquipBomb.getBomb(), 0);
                    superEquip.setBomb(key);
                    LogUtil.debug(roleId + ",超武突破暴击=" + key + ",id=" + staticSuperEquipBomb.getLv());
                }
                CalculateUtil.reCalcAllHeroAttr(player);

                //貂蝉任务-升级神器
                ActivityDiaoChanService.completeTask(player,ETask.ARTIFACT_UP);
                TaskService.processTask(player,ETask.ARTIFACT_UP);
            } else {
                throw new MwException(GameError.SUPER_EQUIP_LV_FULL.getCode(), "等级已达上限, roleId:", roleId, ", type:",
                        type);
            }
        }
        taskDataManager.updTask(player, TaskType.COND_SUPER_EQUIP, 1, type);
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_SUPER_EQUIP);
        UpSuperEquipRs.Builder builder = UpSuperEquipRs.newBuilder();
        builder.setEquip(PbHelper.createSuperEquipPb(superEquip));
        builder.setResource(PbHelper.createResourcePb(player.resource));
        // taskDataManager.updTask(player, TaskType.COND_SUPER_EQUIP, superEquip.getLv(), type);

        return builder.build();
    }

    /**
     * 处理已完成的队列
     *
     * @param player
     */
    private void processEquipQue(Player player) {
        int now = TimeHelper.getCurrentSecond();
        Iterator<TwoInt> it = player.supEquipQue.iterator();
        boolean up = false;
        while (it.hasNext()) {
            TwoInt equip = it.next();
            if (now >= equip.getV2()) {
                up = true;
                player.supEquips.put(equip.getV1(), new SuperEquip(equip.getV1(), 1, 0, 1, 0));
                it.remove();
            }
        }
        if (up) {
            CalculateUtil.reCalcAllHeroAttr(player);
        }
    }

    /**
     * 国器加速
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public SpeedSuperEquipRs speedSuperEquip(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        processEquipQue(player);
        Iterator<TwoInt> it = player.supEquipQue.iterator();
        int now = TimeHelper.getCurrentSecond();
        while (it.hasNext()) {
            TwoInt equip = it.next();
            if (equip.getV1() == type) {
                int second = equip.getV2() - now;
                int needGold = (int) Math.ceil(second / 60.00) * 2;
                if (second <= 0 || needGold <= 0) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), "加速时，时间结束, roleId:" + roleId);
                }
                // 检查玩家金币是否足够并扣除相关金币
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                        AwardFrom.SUPER_EQUIP_SPEED, type);
                player.supEquips.put(equip.getV1(), new SuperEquip(equip.getV1(), 1, 0, 1, 0));
                it.remove();
                break;
            }
        }
        SpeedSuperEquipRs.Builder builder = SpeedSuperEquipRs.newBuilder();
        builder.setEquip(PbHelper.createSuperEquipPb(player.supEquips.get(type)));
        builder.addAllQue(player.supEquipQue);
        CalculateUtil.reCalcAllHeroAttr(player);
        return builder.build();
    }

    /**
     * 国器进阶
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public GrowSuperEquipRs growSuperEquip(Long roleId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        SuperEquip superEquip = player.supEquips.get(type);
        if (superEquip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "没有这个装备, roleId:", roleId, ", type:", type);
        }
        StaticSuperEquipLv staticSuperEquipLv = StaticPropDataMgr
                .getSuperEquipLv(superEquip.getType(), superEquip.getLv());
        if (staticSuperEquipLv.getGrowCost() == null || staticSuperEquipLv.getGrowCost().isEmpty()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    "国器进阶，找不到配置, roleId:" + roleId + ",type:" + type + ",lv:" + staticSuperEquipLv.getLv());
        }
        rewardDataManager.checkAndSubPlayerRes(player, staticSuperEquipLv.getGrowCost(), AwardFrom.SUPER_EQUIP_GROW);
        superEquip.setGrowLv(superEquip.getGrowLv() + 1);
        GrowSuperEquipRs.Builder builder = GrowSuperEquipRs.newBuilder();
        builder.setEquip(PbHelper.createSuperEquipPb(superEquip));
        builder.setResource(PbHelper.createCombatPb(player.resource));
        CalculateUtil.reCalcAllHeroAttr(player);
        return builder.build();
    }

    /**
     * 检测某个装备是否有秘籍洗练
     *
     * @param equip
     * @return true 触发了4星
     */
    public boolean checkEquipIsEquip4Skill(Equip equip) {
        try {
            StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
            StaticEquipQualityExtra staticEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                    .get(staticEquip.getWashQuality());
            List<Turple<Integer, Integer>> attrLvs = equip.getAttrAndLv();
            boolean isBingo = true;
            for (int i = 0; i < attrLvs.size(); i++) {
                if (attrLvs.get(0).getA().intValue() != attrLvs.get(i).getA().intValue())
                    isBingo = false;
            }
            if (staticEquip.getWashQuality() >= EquipConstant.EQUIP_4_SKILL_QUALITY && equip
                    .isAllLvMax(staticEquipQualityExtra.getMaxLv()) && isBingo && attrLvs.size() >= 4) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * gm加将领授勋装备
     *
     * @param player
     * @param cnt
     */
    public void gmAddEquipForHeroDecorated(Player player, int cnt) {
        StaticHeroDecorated sHeroDecorated = StaticHeroDataMgr.getHeroDecoratedMap().get(cnt);
        if (sHeroDecorated == null)
            return;
        sHeroDecorated.getNeedEquip().forEach(l -> {
            int equipId = l.get(0);
            int attrId = l.get(2);
            // 加装备
            List<Integer> equipKeyList = rewardDataManager.addEquip(player, equipId, 1, AwardFrom.DO_SOME);
            // 加上属性
            if (!CheckNull.isEmpty(equipKeyList)) {
                StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equipId);
                Equip equip = player.equips.get(equipKeyList.get(0));
                StaticEquipQualityExtra sWashEquipQualityExtra = StaticPropDataMgr.getQualityMap()
                        .get(staticEquip.getWashQuality());
                for (Turple<Integer, Integer> attrAndLv : equip.getAttrAndLv()) {
                    attrAndLv.setA(attrId);
                    attrAndLv.setB(sWashEquipQualityExtra.getMaxLv());
                }
                // 触发4星
                triggerEquip4Skill(equip);
            }
        });
    }

    /**
     * 戒指强化
     *
     * @param roleId 角色id
     * @param keyId  戒指唯一key
     * @return 强化后的戒指信息
     * @throws MwException 自定义异常
     */
    public RingUpLvRs ringUpLv(long roleId, int keyId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Equip equip = player.equips.get(keyId);
        if (equip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "强化时,没有这个装备, roleId:", roleId, ", keyId:",
                    keyId);
        }
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
        if (staticEquip == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "强化时,没有这个装备配置, roleId:", roleId, ", keyId:", keyId);
        }
        // 检测装备将领的状态
        Hero hero = checkEquipHeroStatus(player, equip);
        if (!(equip instanceof Ring)) {
            throw new MwException(GameError.RING_UP_ERROR.getCode(), "强化时, 这不是一个戒指, roleId:", roleId, ", keyId:",
                    keyId);
        }
        Ring ring = (Ring) equip;
        if (ring.getLv() >= StaticPropDataMgr.RING_STRENGTHEN_MAX) {
            throw new MwException(GameError.RING_UP_LEVEL_ERROR.getCode(), "强化时, 戒指已经是最大等级了, roleId:", roleId, ", keyId:",
                    keyId, ", lv:", ring.getLv());

        }

        StaticRingStrengthen curConf = StaticPropDataMgr.getRingConfByLv(equip.getEquipId(), ring.getLv());
        StaticRingStrengthen nextConf = StaticPropDataMgr.getRingConfByLv(equip.getEquipId(), ring.getLv() + 1);
        if (curConf == null || nextConf == null) {
            throw new MwException(GameError.RING_UP_LEVEL_ERROR.getCode(), "强化时, 没有这个等级的戒指配置, roleId:", roleId, ", keyId:",
                    keyId, ", lv:", ring.getLv());
        }

        List<Integer> consume = curConf.getConsume();
        rewardDataManager.checkAndSubPlayerRes4List(player, consume, AwardFrom.RING_UP_COST, ring.getEquipId());

        boolean up = false;
        // 概率直接升级
        int upProbability = ring.getUpProbability();
        if (RandomHelper.isHitRangeIn10000(upProbability)) {
            up = true;
        }
        // 加经验升级
        if (up || (ring.getCount() + 1) * nextConf.getExpUp() >= nextConf.getExp()) {
            ring.upLv(nextConf);
        } else {
            ring.addExp(nextConf.getExpUp());
        }
        RingUpLvRs.Builder builder = RingUpLvRs.newBuilder();
        if (!CheckNull.isNull(hero)) {
            // 更新将领属性
            CalculateUtil.processAttr(player, hero);
            builder.setHero(PbHelper.createHeroPb(hero, player));
        }
        builder.setEquip(PbHelper.createEquipPb(ring));
        return builder.build();
    }

    /**
     * 检测装备将领的出征状态
     *
     * @param player 玩家对象
     * @param equip  装备对象
     * @return 返回将领对象
     * @throws MwException 自定义异常
     */
    private Hero checkEquipHeroStatus(Player player, Equip equip) throws MwException {
        int heroId = equip.getHeroId();
        Hero hero = null;
        if (heroId > 0) {
            // 检查能否突破,哪些状态下不能突破
            hero = heroService.checkHeroIsExist(player, heroId);
            if (hero.getState() != ArmyConstant.ARMY_STATE_IDLE) {
                throw new MwException(GameError.HERO_BREAK_BATLLE.getCode(), "将领不在空闲状态, roleId:", player.roleId,
                        ", heroId:", heroId + ",quality:" + hero.getQuality());
            }
            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", player.roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }
        }
        return hero;
    }

    /**
     * 镶嵌卸下宝石
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public InlaidJewelRs inlaidJewel(long roleId, InlaidJewelRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int equipId = req.getEquipId();
        Equip equip = player.equips.get(equipId);
        if (equip == null) {
            throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "镶嵌卸下时,没有这个装备, roleId:", roleId, ", keyId:",
                    equipId);
        }
        StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
        if (staticEquip == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "镶嵌卸下时,没有这个装备配置, roleId:", roleId, ", keyId:", equipId);
        }
        if (!(equip instanceof Ring)) {
            throw new MwException(GameError.RING_UP_ERROR.getCode(), "镶嵌卸下时, 这不是一个戒指, roleId:", roleId, ", keyId:",
                    equipId);
        }
        Ring ring = (Ring) equip;

        // 检测装备将领的状态
        Hero hero = checkEquipHeroStatus(player, equip);
        int jewel = req.getJewel();
        EquipJewel equipJewel = player.equipJewel.get(jewel);
        if (CheckNull.isNull(equipJewel)) {
            throw new MwException(GameError.JEWEL_COUNT_NOT_ENOUGH.getCode(), "镶嵌卸下时, 没有可用的宝石, roleId:", roleId,
                    ", keyId:", equipId, ", jewel:", jewel);
        }
        InlaidJewelRs.Builder builder = InlaidJewelRs.newBuilder();
        int type = req.getType();
        if (type == 0) {
            // 镶嵌
            if (ring.getJewels().size() > 0) {
                int preJewel = ring.getJewels().get(0);
                if (preJewel > 0) {
                    ring.downJewel(preJewel);
                    EquipJewel preEquipJewel = player.equipJewel.get(preJewel);
                    if (CheckNull.isNull(equipJewel)) {
                        throw new MwException(GameError.JEWEL_COUNT_NOT_ENOUGH.getCode(), "镶嵌卸下时, 没有可用的宝石, roleId:", roleId,
                                ", keyId:", equipId, ", jewel:", jewel);
                    }
                    // 减去一个被镶嵌数量
                    preEquipJewel.getInlaid().decrementAndGet();
                    if (preJewel != jewel) {
                        builder.addJewel(PbHelper.createJewelPb(preEquipJewel));
                    }
                }
            }
            int canUseCnt = equipJewel.canUseCnt();
            if (canUseCnt <= 0) {
                throw new MwException(GameError.JEWEL_COUNT_NOT_ENOUGH.getCode(), "镶嵌卸下时, 没有可用的宝石, roleId:", roleId,
                        ", keyId:", equipId, ", jewel:", jewel);
            }
            if (ring.getJewels().size() >= EquipConstant.EQUIP_JEWEL_MAX) {
                throw new MwException(GameError.INLAID_JEWEL_ERROR.getCode(), "镶嵌卸下时, 装备没有可用的空位, 可以镶嵌宝石, roleId:", roleId,
                        ", keyId:", equipId);
            }
            ring.upJewel(jewel);
            // 加上一个被镶嵌数量
            equipJewel.getInlaid().incrementAndGet();
        } else {
            // 卸下
            if (ring.getJewels().size() <= 0) {
                throw new MwException(GameError.INLAID_JEWEL_ERROR.getCode(), "镶嵌卸下时, 装备没有可卸下的宝石, roleId:", roleId,
                        ", keyId:", equipId);
            }
            ring.downJewel(jewel);
            // 减去一个被镶嵌数量
            equipJewel.getInlaid().decrementAndGet();
        }
        if (!CheckNull.isNull(hero)) {
            // 更新将领属性
            CalculateUtil.processAttr(player, hero);
        }
        builder.setEquip(PbHelper.createEquipPb(ring));
        builder.addJewel(PbHelper.createJewelPb(equipJewel));
        return builder.build();
    }

    /**
     * 获取所有的宝石信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetJewelsRs getJewels(long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GetJewelsRs.Builder builder = GetJewelsRs.newBuilder();
        player.equipJewel.values().forEach(jewel -> builder.addJewel(PbHelper.createJewelPb(jewel)));

        return builder.build();
    }

    /**
     * 进阶分解宝石
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public DoJewelImproveRs doJewelImprove(long roleId, DoJewelImproveRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int lv = req.getLv();
        int type = req.getType();

        EquipJewel equipJewel = player.equipJewel.get(lv);
        if (CheckNull.isNull(equipJewel)) {
            throw new MwException(GameError.JEWEL_COUNT_NOT_ENOUGH.getCode(), "未拥有要进阶或者分解的等级宝石, roleId:", roleId,
                    ", lv:", lv);
        }
        StaticJewel sCurJewel = StaticPropDataMgr.getJewelByLv(lv);
        if (CheckNull.isNull(sCurJewel)) {
            throw new MwException(GameError.JEWEL_COUNT_NOT_ENOUGH.getCode(), "未找到要进阶或者分解的等级宝石配置, roleId:", roleId,
                    ", lv:", lv);
        }
        DoJewelImproveRs.Builder builder = DoJewelImproveRs.newBuilder();
        List<Integer> changeRs = new ArrayList<>(2);
        changeRs.add(lv);
        if (type == 0) {
            // 进阶
            if (lv == StaticPropDataMgr.JEWEL_LEVEL_MAX) {
                throw new MwException(GameError.JEWEL_LEVEL_ERROR.getCode(), "进阶宝石, 宝石已经是最大等级了, roleId:", roleId,
                        ", lv:", lv);
            }
            StaticJewel nextJewel = StaticPropDataMgr.getJewelByLv(lv + 1);
            if (CheckNull.isNull(nextJewel)) {
                throw new MwException(GameError.JEWEL_LEVEL_ERROR.getCode(), "未找到要进阶或者分解的等级宝石配置, roleId:", roleId,
                        ", lv:", lv);
            }
            // 下个阶段合成需要的道具
            List<Integer> synthesis = nextJewel.getSynthesis();
            if (!CheckNull.isEmpty(synthesis)) {
                int awardType = synthesis.get(0);
                int id = synthesis.get(1);
                int need = synthesis.get(2);
                // 需要的道具剩余数量
                int useCnt = (int) rewardDataManager.getRoleResByType(player, awardType, id);
                int count = useCnt / need;
                if (count > 0) {
                    // 进阶, 当前阶级减少 count * need
                    rewardDataManager.checkAndSubPlayerRes(player, awardType, id, count * need, AwardFrom.JEWEL_ADVANCED, true);
                    rewardDataManager.addAward(player, awardType, nextJewel.getLevel(), count, AwardFrom.JEWEL_ADVANCED);
                    changeRs.add(nextJewel.getLevel());
                }
            }
        } else {
            //  分解
            if (lv == StaticPropDataMgr.JEWEL_LEVEL_MIN) {
                throw new MwException(GameError.JEWEL_LEVEL_ERROR.getCode(), "分解宝石, 宝石已经是最小等级了, roleId:", roleId,
                        ", lv:", lv);
            }
            StaticJewel staticJewel = StaticPropDataMgr.getJewelByLv(lv - 1);
            if (CheckNull.isNull(staticJewel)) {
                throw new MwException(GameError.JEWEL_LEVEL_ERROR.getCode(), "未找到要进阶或者分解的等级宝石配置, roleId:", roleId,
                        ", lv:", lv);
            }
            // 当前阶段合成需要的道具
            List<Integer> synthesis = sCurJewel.getSynthesis();
            if (!CheckNull.isEmpty(synthesis)) {
                int awardType = synthesis.get(0);
                int id = synthesis.get(1);
                int need = synthesis.get(2);

                // 分解, 当前阶段减少 1, 上个阶段加 need
                rewardDataManager.checkAndSubPlayerRes(player, awardType, sCurJewel.getLevel(), 1, AwardFrom.JEWEL_DECOMPOSITION, true);
                rewardDataManager.addAward(player, awardType, id, need, AwardFrom.JEWEL_DECOMPOSITION);
                changeRs.add(id);
            }
        }
        if (!CheckNull.isEmpty(changeRs)) {
            changeRs.forEach(change -> {
                EquipJewel jewel = player.equipJewel.get(change);
                if (!CheckNull.isNull(jewel)) {
                    builder.addJewel(PbHelper.createJewelPb(jewel));
                }
            });
        }
        return builder.build();
    }

    /*********** 极品装备概率初始化 start *********/
    /**
     * @Title: initCheatCode @Description: 初始化 装备打造 秘技触发概率命中值 @param techEff 参数 void 返回类型 @throws
     */
    public void initCheatCode(double techEff, Player player) {
        // 初始化命中值 根据科技加成获取命中数
        int[] arr = new int[Constant.EQUIP_MAKE_RANGE.get(0)];
        player.setMixtureData(PlayerConstant.EQUIP_MAKE_NUM, 1);
        player.setMixtureData(PlayerConstant.EQUIP_MAKE_PROBABILITY, (int) techEff);
        player.setMixtureData(PlayerConstant.EQUIP_MAKE_INTERVAL, Constant.EQUIP_MAKE_RANGE.get(1));
        player.setMixtureData(PlayerConstant.EQUIP_MAKE_RANGE, Constant.EQUIP_MAKE_RANGE.get(0));

        int hitNum = (int) (Constant.EQUIP_MAKE_RANGE.get(0) * techEff);// 根据科技加成获取命中数 范围值*万分比
        // 判断间隔
        int interval = Constant.EQUIP_MAKE_RANGE.get(1);// 命中值 间隔
        if (interval < 2) {
            interval = 2;
        }

        // 判断命中数
        if (hitNum > Constant.EQUIP_MAKE_RANGE.get(0) / interval - 1) {// -1是为了防止出现极端情况
            hitNum = Constant.EQUIP_MAKE_RANGE.get(0) / interval - 1;
        }
        // 配置异常时 初始化命中数为1
        if (hitNum <= 0) {
            hitNum = 1;
        }

        int[] cheatCode = new int[hitNum];// 命中下标

        for (int i = 0; i < hitNum; i++) {
            int num;
            do {
                num = RandomHelper.randomInSize(Constant.EQUIP_MAKE_RANGE.get(0));// 获取 一个范围值以内的随机数
                if (i == 0) {
                    break;
                }
                // 需要重复获取随机数的条件为 该随机数已经存在 或者 验证前后间隔失败
            } while (arr[num] == 1 || !checkInterval(num, interval, arr));
            arr[num] = 1;
        }
        // 获取命中下标
        for (int i = 0; i < cheatCode.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                if (arr[j] == 1) {
                    cheatCode[i] = j + 1;
                }
            }
        }
        int index = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 1) {
                cheatCode[index++] = i + 1;
            }
        }
        player.cheatCode = cheatCode;
        LogLordHelper.commonLog("equipChanceInit", AwardFrom.EQUIP_CHANCE_INIT, player, Arrays.toString(cheatCode));
    }

    /**
     * @Title: checkInterval @Description: 验证间隔 @param num 当前随机的数组下标 @param interval 间隔值 @return 参数 boolean 返回类型 false
     * 验证失败 需要重新获取 @throws
     */
    private boolean checkInterval(int num, int interval, int[] arr) {
        // 验证后面的
        for (int j = 1; j < interval; j++) {
            // 如果提前到了尽头 则算成功
            if (num + j >= arr.length - 2) {
                return true;
            }
            if (arr[num + j] == 1) {// 失败
                return false;
            }
        }
        // 验证前面的
        for (int j = 1; j < interval; j++) {
            // 如果提前到了起点 则算成功
            if (num - j <= 0) {
                return true;
            }
            if (arr[num - j] == 1) {// 失败
                return false;
            }
        }
        return true;
    }

    /**
     * @Title: isExist @Description: 校验该下标是否存在 @param num @return 参数 boolean 返回类型 true 存在 @throws
     */
    public boolean isExist(int num, int[] cheatCode) {
        for (int i : cheatCode) {
            if (i == num) {
                return true;
            }
        }
        return false;
    }

    /*********** 极品装备概率初始化 end *********/

    /**
     * 判断消耗装备是否上锁
     */
    private void whetherLocked(Player player, List<Integer> equipList) throws MwException {
        for (Integer keyId : equipList) {
            Equip equip = player.equips.get(keyId);//当前升星装备
            if (null == equip) {
                throw new MwException(GameError.EQUIP_NOT_FOUND.getCode(), "玩家背包中没有这个装备, roleId:", player.roleId, ", keyId:", keyId);
            }
            if (equip.getEquipLocked() == 2) {
                throw new MwException(GameError.EQUIP_LOCKED.getCode(), "当前装备已上锁, roleId:", player.roleId, ", keyId:", equip.getKeyId());
            }
        }
    }
}
