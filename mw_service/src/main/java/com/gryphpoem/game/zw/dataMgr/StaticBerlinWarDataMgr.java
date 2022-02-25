package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.WorldScheduleService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-17 20:00
 * @description: 柏林会战相关数据处理
 * @modified By:
 */
public class StaticBerlinWarDataMgr {

    // 柏林
    public static final int BERLIN_TYPE = 9;
    // 柏林据点
    public static final int BATTLEFRONT_TYPE = 10;

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    // 柏林会战配置,key: keyId
    private static Map<Integer, StaticBerlinWar> berlinWarMap;

    private static Map<Integer, List<StaticBerlinWar>> berlinWarTypeMap;

    // 柏林会战奖励配置,key: rankType
    private static Map<Integer, List<StaticBerlinWarAward>> berlinWarAwardMap;

    // 柏林会战官职 key: job
    private static Map<Integer, StaticBerlinJob> berlinJob;
    // 柏林会战buff key:buffId
    private static Map<Integer, StaticBerlinBuff> berlinBuff;
    // 柏林战前buff key: schedule, key:buffType
    private static Map<Integer, Map<Integer, List<StaticPrewarBuff>>> berlinPrewarBuff;
    // 柏林战前buff key: schedule, key:buffID
    private static Map<Integer, Map<Integer, StaticPrewarBuff>> berlinPrewarBuffId;
    // 柏林战斗狂热配置
    private static List<StaticBerlinFever> berlinFeverList;

    public static void init() {
        StaticBerlinWarDataMgr.berlinWarMap = staticDataDao.selectBerlinWar();
        Map<Integer, List<StaticBerlinWar>> berlinWarTypeMap = new HashMap<>();
        berlinWarMap.values().forEach(e -> {
            int type = e.getType();
            List<StaticBerlinWar> berlinWars = berlinWarTypeMap.get(type);
            if (CheckNull.isNull(berlinWars)) {
                berlinWars = new ArrayList<>();
            }
            berlinWars.add(e);
            berlinWarTypeMap.put(type, berlinWars);
        });
        StaticBerlinWarDataMgr.berlinWarTypeMap = berlinWarTypeMap;

        List<StaticBerlinWarAward> berlinWarAwardList = staticDataDao.selectBerlinWarAwardList();
        HashMap<Integer, List<StaticBerlinWarAward>> berlinWarAwardMap = new HashMap<>();
        berlinWarAwardList.forEach(staticBerlinWarAward -> {
            int rankType = staticBerlinWarAward.getType();
            List<StaticBerlinWarAward> berlinWarAwards = berlinWarAwardMap.get(rankType);
            if (CheckNull.isNull(berlinWarAwards)) {
                berlinWarAwards = new ArrayList<>();
            }
            berlinWarAwards.add(staticBerlinWarAward);
            berlinWarAwardMap.put(rankType, berlinWarAwards);
        });
        StaticBerlinWarDataMgr.berlinWarAwardMap = berlinWarAwardMap;

        StaticBerlinWarDataMgr.berlinJob = staticDataDao.selectBerlinJobMap();
        StaticBerlinWarDataMgr.berlinBuff = staticDataDao.selectBerlinBuffMap();

        Map<Integer, StaticPrewarBuff> berlinPrewarBuffId = staticDataDao.selectBerlinPrewarBuff();
        StaticBerlinWarDataMgr.berlinPrewarBuffId = berlinPrewarBuffId.values()
                .stream()
                .collect(Collectors.groupingBy(StaticPrewarBuff::getSchedule,
                        Collectors.toMap(StaticPrewarBuff::getId, Function.identity(), (oldV, newV) -> newV)));
        StaticBerlinWarDataMgr.berlinPrewarBuff = berlinPrewarBuffId
                .values()
                .stream()
                .collect(Collectors.groupingBy(StaticPrewarBuff::getSchedule,
                        Collectors.groupingBy(StaticPrewarBuff::getType)));

        StaticBerlinWarDataMgr.berlinFeverList = staticDataDao.selectBerlinFeverList();
    }

    /**
     * 根据buff类型和等级获取当前世界进程配置
     *
     * @param type  buff类型
     * @param level buff等级
     * @return 战前buff配置
     */
    public static StaticPrewarBuff getPrewarBuff(int type, int level) {
        GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (Objects.isNull(berlinWar)) {
            return null;
        }
        StaticPrewarBuff prewarBuff = null;
        Map<Integer, List<StaticPrewarBuff>> typeMap = berlinPrewarBuff.get(berlinWar.getScheduleId());
        if (!CheckNull.isEmpty(typeMap)) {
            List<StaticPrewarBuff> staticPrewarBuffs = typeMap.get(type);
            if (!CheckNull.isEmpty(staticPrewarBuffs)) {
                prewarBuff = staticPrewarBuffs.stream().filter(buff -> buff.getLevel() == level).findFirst().orElse(null);
            }
        }
        return prewarBuff;
    }

    /**
     * 根据buff的id获取当前世界进程配置
     * @param id
     * @return
     */
    public static StaticPrewarBuff getPrewarBuffById(int id) {
        GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (Objects.isNull(berlinWar)) {
            return null;
        }
        StaticPrewarBuff prewarBuff = null;
        Map<Integer, StaticPrewarBuff> idMap = berlinPrewarBuffId.get(berlinWar.getScheduleId());
        if (!CheckNull.isEmpty(idMap)) {
            prewarBuff = idMap.get(id);
        }
        return prewarBuff;
    }

    /**
     * 获取当前世界进程的柏林配置
     *
     * @return 柏林配置
     */
    private static List<StaticBerlinWar> curScheduleBerlinConf() {
        if (CheckNull.isEmpty(berlinWarMap)) {
            return null;
        }
        GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (Objects.nonNull(berlinWar)) {
            if (berlinWar.getScheduleId() == 0) {
                // 记录世界进程阶段
                int scheduleId = DataResource.ac.getBean(WorldScheduleService.class).getCurrentSchduleId();
                // 未初始化
                berlinWar.updateScheduleId(scheduleId);
            }
            int scheduleId = berlinWar.getScheduleId();
            return berlinWarMap.values().stream().filter(sbw -> scheduleId >= sbw.getSchedule().get(0) && scheduleId <= sbw.getSchedule().get(1)).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取所有的圣域配置
     * @return 圣域配置
     */
    public static List<StaticBerlinWar> getAllScheduleBerlin() {
        return new ArrayList<>(berlinWarMap.values());
    }

    /**
     * 获取当前世界进程的柏林配置
     *
     * @return 柏林配置
     */
    public static StaticBerlinWar getBerlinSetting() {
        List<StaticBerlinWar> staticBerlinWars = curScheduleBerlinConf();
        if (CheckNull.isEmpty(staticBerlinWars)) {
            return null;
        }
        // 根据时间进程过滤柏林配置
        return staticBerlinWars.stream().filter(sbw -> BERLIN_TYPE == sbw.getType()).findFirst().orElse(null);
    }

    /**
     * 获取当前世界进程的炮台配置
     *
     * @return 炮台配置
     */
    public static List<StaticBerlinWar> getBerlinBattlefront() {
        List<StaticBerlinWar> staticBerlinWars = curScheduleBerlinConf();
        if (CheckNull.isEmpty(staticBerlinWars)) {
            return null;
        }
        return staticBerlinWars.stream().filter(sbw -> BATTLEFRONT_TYPE == sbw.getType()).collect(Collectors.toList());
    }

    /**
     * 获取当前世界进程指定城池的配置
     *
     * @param cityId 城池id
     * @return 城池配置
     */
    public static StaticBerlinWar getBerlinSettingById(int cityId) {
        List<StaticBerlinWar> staticBerlinWars = curScheduleBerlinConf();
        if (CheckNull.isEmpty(staticBerlinWars)) {
            return null;
        }
        // 根据时间进程过滤柏林配置
        return staticBerlinWars.stream().filter(sbw -> cityId == sbw.getKeyId()).findAny().orElse(null);
    }

    /**
     * 根据榜单类型获取配置
     *
     * @param rankType 榜单类型
     * @return 军费配置
     */
    public static List<StaticBerlinWarAward> getBerlinWarAwardByType(int rankType) {
        if (CheckNull.isEmpty(berlinWarAwardMap)) {
            return null;
        }
        GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (Objects.isNull(berlinWar)) {
            return null;
        }
        int scheduleId = berlinWar.getScheduleId();
        // 根据时间进程过滤军费配置
        return berlinWarAwardMap.get(rankType).stream().filter(bwa -> scheduleId >= bwa.getSchedule().get(0) && scheduleId <= bwa.getSchedule().get(1)).collect(Collectors.toList());
    }

    /**
     * 连续击杀奖励
     *
     * @param rank
     * @return
     */
    public static StaticBerlinWarAward findStreakRankAward(int rank) {
        List<StaticBerlinWarAward> rankList = getBerlinWarAwardByType(WorldConstant.BERLIN_RANK_KILL_STREAK_ARMY);
        if (CheckNull.isEmpty(rankList)) {
            return null;
        }
        return rankList.stream().filter(award -> {
            List<Integer> cond = award.getCond();
            boolean flag;
            if (cond.size() > 1) {
                flag = rank >= cond.get(0) && rank <= cond.get(1);
            } else {
                flag = rank == cond.get(0);
            }
            return flag;
        }).findFirst().orElse(null);
    }

    /**
     * 累积杀敌奖励
     *
     * @param rankVal
     * @return
     */
    public static StaticBerlinWarAward findKillRankAward(long rankVal) {
        List<StaticBerlinWarAward> rankList = getBerlinWarAwardByType(WorldConstant.BERLIN_RANK_KILL_ARMY_CNT);
        if (CheckNull.isEmpty(rankList)) {
            return null;
        }
        return rankList.stream().filter(award -> rankVal >= award.getCond().get(0)).min((award1, award2) -> award2.getCond().get(0) - award1.getCond().get(0)).orElse(null);
    }

    public static Map<Integer, StaticBerlinJob> getBerlinJob() {
        return berlinJob;
    }

    public static Map<Integer, StaticBerlinBuff> getBerlinBuff() {
        return berlinBuff;
    }

    /**
     * 根据攻防兵力差值, 查询战斗狂热持续时长
     * @param exact 攻防兵力差
     * @return 持续时长
     */
    public static int findBFDuration(int exact) {
        if (CheckNull.isEmpty(berlinFeverList)) {
            return 0;
        }
        return berlinFeverList.stream().filter(conf -> exact >= conf.getForceDifference().get(0) && exact <= conf.getForceDifference().get(1)).mapToInt(StaticBerlinFever::getDuration).findAny().orElse(0);
    }

}
