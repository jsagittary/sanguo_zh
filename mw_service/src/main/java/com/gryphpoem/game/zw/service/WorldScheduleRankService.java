package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.constant.ScheduleConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticScheduleBossRank;
import com.gryphpoem.game.zw.resource.domain.s.StaticScheduleRank;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.global.ScheduleRank;
import com.gryphpoem.game.zw.resource.pojo.global.ScheduleRankItem;
import com.gryphpoem.game.zw.resource.pojo.global.WorldSchedule;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by pengshuo on 2019/2/26 15:01 <br>
 * Description: 世界进程排行榜数据 <br>
 * Modified By: <br>
 * Version:
 *
 * @author pengshuo
 */
@Service
public class WorldScheduleRankService {

    /**
     * 世界进程1阶段
     */
    private final static int SCHEDULE_1 = 1;
    /**
     * 世界进程2阶段
     */
    private final static int SCHEDULE_2 = 2;
    /**
     * 世界进程3阶段
     */
    private final static int SCHEDULE_3 = 3;
    /**
     * 世界进程4阶段
     */
    private final static int SCHEDULE_4 = 4;
    /**
     * 世界进程5阶段
     */
    private final static int SCHEDULE_5 = 5;
    /**
     * 世界进程6阶段
     */
    private final static int SCHEDULE_6 = 6;
    /**
     * 世界进程7阶段
     */
    private final static int SCHEDULE_7 = 7;
    /**
     * 世界进程8阶段
     */
    private final static int SCHEDULE_8 = 8;
    /**
     * 世界进程9阶段
     */
    private final static int SCHEDULE_9 = 9;
    /**
     * 世界进程10阶段
     */
    private final static int SCHEDULE_10 = 10;

    /**
     * 排行榜是否区分区域 0 不区分, 1 区分
     */
    private final static int SCHEDULE_RANK_AREA_YES = 1;

    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    /**
     * 获取玩家排行
     *
     * @param player
     * @return
     */
    public List<CommonPb.PersonalWorldScheduleRank> campRank(Player player) {
        List<CommonPb.PersonalWorldScheduleRank> res = new ArrayList<>();
        int camp = player.lord.getCamp();
        int area = player.lord.getArea();
        globalDataManager.getGameGlobal().getGlobalSchedule().getScheduleMap().entrySet().stream().filter(
                e -> e.getValue() != null && e.getValue().getRank() != null && e.getValue().getRank().getRank() != null)
                .forEach(e -> {
                    CommonPb.PersonalWorldScheduleRank.Builder builder = CommonPb.PersonalWorldScheduleRank
                            .newBuilder();
                    int currentScheduleId = e.getKey();
                    ScheduleRank scheduleRank = e.getValue().getRank();
                    if (scheduleRank.getRankArea() == SCHEDULE_RANK_AREA_YES) {
                        CommonPb.ScheduleRankItem.Builder serRankItem = CommonPb.ScheduleRankItem.newBuilder();
                        ScheduleRankItem scheduleRankItem = scheduleRank.getRank().stream()
                                .filter(s -> s.getCamp() == camp && s.getArea() == area).findFirst().orElse(null);
                        if (scheduleRankItem != null) {
                            serRankItem.setCamp(camp);
                            serRankItem.setArea(area);
                            serRankItem.setValue(scheduleRankItem.getValue());
                            builder.setId(currentScheduleId);
                            builder.setRank(scheduleRank.getRank().indexOf(scheduleRankItem) + 1);
                            builder.setItem(serRankItem.build());
                        }
                    } else {
                        CommonPb.ScheduleRankItem.Builder serRankItem = CommonPb.ScheduleRankItem.newBuilder();
                        ScheduleRankItem scheduleRankItem = scheduleRank.getRank().stream()
                                .filter(s -> s.getCamp() == camp).findFirst().orElse(null);
                        if (scheduleRankItem != null) {
                            serRankItem.setCamp(camp);
                            serRankItem.setValue(scheduleRankItem.getValue());
                            builder.setId(currentScheduleId);
                            builder.setRank(scheduleRank.getRank().indexOf(scheduleRankItem) + 1);
                            builder.setItem(serRankItem.build());
                        }
                    }
                    if (builder.getId() > 0) {
                        res.add(builder.build());
                    }
                });
        return res;
    }

    /**
     * 新增的个人排行
     * @param player
     * @return
     */
    public List<CommonPb.PersonalWorldScheduleRank> personRank(Player player) {
        List<CommonPb.PersonalWorldScheduleRank> res = new ArrayList<>();
        Long roleId = player.roleId;
        // 只有5和9才有个人上海累积排行
        globalDataManager.getGameGlobal().getGlobalSchedule().getScheduleMap().entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().getRank() != null && e.getValue().getRank().getPersonRank() != null)
                .forEach(e -> {
                    int curId = e.getKey();
                    ScheduleRank scheduleRank = e.getValue().getRank();
                    ScheduleRankItem item = scheduleRank.getPersonRank().stream().filter(s -> s.getRoleId() == roleId).findFirst().orElse(null);
                    List<ScheduleRankItem> items = null;
                    if (!CheckNull.isNull(item)) {
                        // 今日的累积对BOSS伤害值, 存在rank -1 里面
                        if (e.getValue().getStatus() == ScheduleConstant.SCHEDULE_STATUS_PROGRESS) {
                            int val = player.getMixtureDataById(PlayerConstant.DAILY_ATK_BOSS_VAL);
                            CommonPb.PersonalWorldScheduleRank.Builder builder = CommonPb.PersonalWorldScheduleRank
                                    .newBuilder();
                            builder.setId(curId);
                            builder.setRank(-1);
                            CommonPb.ScheduleRankItem.Builder serRankItem = CommonPb.ScheduleRankItem.newBuilder();
                            serRankItem.setValue(val);
                            builder.setItem(serRankItem.build());
                            res.add(builder.build());
                        }
                        int rank = scheduleRank.getPersonRank().indexOf(item) + 1;
                        // 我的排行大于的第11名的时候, 以我的排行前一名取三位
                        if (rank > 11) {
                            int start = scheduleRank.getPersonRank().indexOf(item) - 1;
                            items = scheduleRank.getPersonRank().stream().skip(start).limit(3).collect(Collectors.toList());
                        } else if (rank == 11) {
                            // 我的排行等于11的名的时候, 取我的排行和后面一位
                            int start = scheduleRank.getPersonRank().indexOf(item);
                            items = scheduleRank.getPersonRank().stream().skip(start).limit(2).collect(Collectors.toList());
                        }

                        if (!CheckNull.isEmpty(items)) {
                            // 我和我前后排名的玩家数据
                            for (ScheduleRankItem rankItem : items) {
                                CommonPb.PersonalWorldScheduleRank.Builder builder = CommonPb.PersonalWorldScheduleRank
                                        .newBuilder();
                                builder.setId(curId);
                                int itemRanking = scheduleRank.getPersonRank().indexOf(rankItem) + 1;
                                builder.setRank(itemRanking);
                                CommonPb.ScheduleRankItem.Builder serRankItem = CommonPb.ScheduleRankItem.newBuilder();
                                Player p = playerDataManager.getPlayer(rankItem.getRoleId());
                                if (!CheckNull.isNull(p)) {
                                    serRankItem.setNick(p.lord.getNick());
                                    serRankItem.setCamp(p.lord.getCamp());
                                }
                                serRankItem.setRoleId(rankItem.getRoleId());
                                serRankItem.setValue(rankItem.getValue());
                                builder.setItem(serRankItem.build());
                                res.add(builder.build());
                            }
                        }
                    }

                });
        return res;
    }

    /**
     * 设置city在世界进程几阶段被攻占
     *
     * @param city city
     */
    public void setCityCurrentScheduleId(City city) {
        if (city == null) {
            LogUtil.error("玩家攻占city为null");
            return;
        }
    }

    /**
     * 世界进程第一阶段(根据阵营补给的产生数量生成积分,根据积分高低排出阵营的排名)
     *
     * @param camp  camp
     * @param area  area
     * @param value value
     */
    public void addOneWorldScheduleRankData(int camp, int area, long value) {
        GlobalSchedule globalSchedule = globalDataManager.getGameGlobal().getGlobalSchedule();
        // 当前世界进程阶段值（1）
        int currentScheduleId = globalSchedule.getCurrentScheduleId();
        if (currentScheduleId == SCHEDULE_1) {
            if (area > 0 && value > 0) {
                addWorldScheduleRankData(globalSchedule.getWorldSchedule(currentScheduleId), camp, area, value);
            }
        }
    }

    /**
     * 世界进程第五、九阶段（根据对防线造成的伤害高低排出阵营的排名）
     *
     * @param camp  camp
     * @param area  area
     * @param value value
     */
    public void addBossWorldScheduleRankData(int currentScheduleId, int camp, int area, long value) {
        GlobalSchedule globalSchedule = globalDataManager.getGameGlobal().getGlobalSchedule();
        // 当前世界进程阶段值（5、9）
        if (Stream.of(SCHEDULE_5, SCHEDULE_9).collect(Collectors.toList()).contains(currentScheduleId)) {
            if (area > 0 && value > 0) {
                addWorldScheduleRankData(globalSchedule.getWorldSchedule(currentScheduleId), camp, area, value);
            }
        }
    }

    /**
     * 世界进程第五、九阶段（个人排行）
     *
     * @param curId  当前阶段id
     * @param player 玩家对象
     * @param hurt   伤害值
     */
    public void addBossWorldScheduleRankData(int curId, Player player, int hurt) {
        GlobalSchedule globalSchedule = globalDataManager.getGameGlobal().getGlobalSchedule();
        if (Stream.of(SCHEDULE_5, SCHEDULE_9).collect(Collectors.toList()).contains(curId)) {
            if (!CheckNull.isNull(player) && hurt > 0) {
                addWorldScheduleRankData(globalSchedule.getWorldSchedule(curId), player, hurt);
            }
        }
    }

    /**
     * 增加世界进程排行榜数据（2、3、4、6、7、8）
     */
    public void addCityWorldScheduleRankData() {
        GlobalSchedule globalSchedule = globalDataManager.getGameGlobal().getGlobalSchedule();
        // 当前世界进程阶段值
        int currentScheduleId = globalSchedule.getCurrentScheduleId();
        if (Stream.of(SCHEDULE_2, SCHEDULE_3, SCHEDULE_4, SCHEDULE_6, SCHEDULE_7, SCHEDULE_8)
                .collect(Collectors.toList()).contains(currentScheduleId)) {
            LogUtil.common("增加世界进程排行榜数据（2、3、4、6、7、8） currentScheduleId: " + currentScheduleId);
            // 获取city
            addCityWorldScheduleRankData(globalSchedule.getWorldSchedule(currentScheduleId),
                    worldDataManager.getCityMap().entrySet().stream()
                            // 非npc 已被占领
                            .filter(e -> e.getValue() != null && !e.getValue().isNpcCity()).map(e -> e.getValue())
                            .collect(Collectors.toList()));
        }
    }

    /**
     * 发放世界进度奖励 scheduleId
     *
     * @param scheduleId
     */
    public void worldScheduleRankAward(int scheduleId) {
        GlobalSchedule globalSchedule = globalDataManager.getGameGlobal().getGlobalSchedule();
        worldScheduleRankAward(scheduleId, globalSchedule.getWorldSchedule(scheduleId));
    }

    /**
     * 发放各阶段奖励
     *
     * @param scheduleId
     * @param worldSchedule
     */
    private void worldScheduleRankAward(int scheduleId, WorldSchedule worldSchedule) {
        if (worldSchedule == null) {
            LogUtil.error("世界进度为null,发放奖励失败");
            return;
        }
        ScheduleRank scheduleRank = worldSchedule.getRank();
        if (scheduleRank == null) {
            LogUtil.error("世界进度排行榜为null,发放奖励失败");
            return;
        }
        // 阵营排行奖励
        campRankAward(scheduleId, scheduleRank);
        // 个人排行奖励
        personRankAward(scheduleId, scheduleRank);
    }

    /**
     * 个人排行奖励
     * @param scheduleId
     * @param scheduleRank
     */
    private void personRankAward(int scheduleId, ScheduleRank scheduleRank) {
        // 不是打boss的阶段没有个人排行
        if (!Stream.of(SCHEDULE_5, SCHEDULE_9).collect(Collectors.toList()).contains(scheduleId)) {
            return;
        }
        List<ScheduleRankItem> personRank = scheduleRank.getPersonRank();
        if (CheckNull.isEmpty(personRank)) {
            LogUtil.error("{世界进度}排行榜为null,发放奖励失败");
            return;
        }

        List<StaticScheduleBossRank> sBossRanks = StaticWorldDataMgr.getSchedBossRankById(scheduleId);
        if (CheckNull.isEmpty(sBossRanks)) {
            LogUtil.error("{世界进度}排行榜配置有误 scheduleId:", scheduleId);
            return;
        }
        // 最后一名
        int maxRank = sBossRanks.stream().mapToInt(s -> s.getRank().get(1)).max().getAsInt();
        int now = TimeHelper.getCurrentSecond();
        // 排名, 最大只发放200名的奖励
        personRank.stream().limit(maxRank).forEach(item -> {
            // 获取排名
            int rank = personRank.indexOf(item) + 1;
            StaticScheduleBossRank sBossRankConf = sBossRanks.stream().filter(s -> rank >= s.getRank().get(0) && rank <= s.getRank().get(1)).findFirst().orElse(null);
            if (sBossRankConf != null) {
                long roleId = item.getRoleId();
                Player player = playerDataManager.getPlayer(roleId);
                if (!CheckNull.isNull(player)) {
                    // 邮件奖励 恭喜您达到第%s阶段世界进程 恭喜您达到第%s阶段，本阶段您的排名为：%s,，您获得了：
                    mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(sBossRankConf.getAward()),
                            MailConstant.MOLD_ATK_BOSS_PERSON_RANK_AWARD, AwardFrom.WORLD_SCHEDULE_RANK_AWARD, now,
                            item.getValue(), rank);
                    // 日志记录
                    LogLordHelper.commonLog("worldSchedule", AwardFrom.WORLD_SCHEDULE_RANK_AWARD, player,
                            scheduleId, item.getValue(), rank);
                }
            }
        });
    }

    private void campRankAward(int scheduleId, ScheduleRank scheduleRank) {
        List<ScheduleRankItem> scheduleRankItems = scheduleRank.getRank();
        if (scheduleRankItems == null || scheduleRankItems.isEmpty()) {
            LogUtil.error("{世界进度}排行榜为null,发放奖励失败");
            return;
        }

        Map<Integer, StaticScheduleRank> sRankConfigMap = StaticWorldDataMgr.getScheduleRankMap().get(scheduleId);
        if (CheckNull.isEmpty(sRankConfigMap)) {
            LogUtil.error("{世界进度}排行榜配置有误 scheduleId:", scheduleId);
            return;
        }
        // 是否区分区域
        final boolean isDifferAreaRanking = scheduleRank.getRankArea() == SCHEDULE_RANK_AREA_YES;
        // 排名
        int ranking = 1;
        int now = TimeHelper.getCurrentSecond();
        // 添加的时候已经排过序,所以不需要再排序
        for (ScheduleRankItem item : scheduleRankItems) {
            StaticScheduleRank sRankCfg = sRankConfigMap.get(ranking);
            if (sRankCfg != null) {
                for (Player player : playerDataManager.getPlayerByCamp(item.getCamp()).values()) {
                    boolean areaCond = isDifferAreaRanking ? player.lord.getArea() == item.getArea() : true;
                    if (areaCond && player.lord.getLevel() >= sRankCfg.getLvCond()) {
                        // 邮件奖励 恭喜您达到第%s阶段世界进程 恭喜您达到第%s阶段，本阶段您的排名为：%s,，您获得了：
                        mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(sRankCfg.getAward()),
                                MailConstant.MOLD_WORLD_SCHEDULE_RANK_AWARD, AwardFrom.WORLD_SCHEDULE_RANK_AWARD, now,
                                scheduleId, scheduleId, sRankCfg.getRanking());
                        // 日志记录
                        LogLordHelper.commonLog("worldSchedule", AwardFrom.WORLD_SCHEDULE_RANK_AWARD, player,
                                scheduleId, sRankCfg.getRanking());
                    }
                }
            }
            ranking++;
        }
    }

    /**
     * 增加世界进程排行榜数据（2、3、4、6、7、8）（非实时统计：进程中每日12，21和结束时统计 ）
     *
     * @param worldSchedule 当前世界进度
     * @param worldSchedule worldSchedule
     * @param cities        cities
     */
    private void addCityWorldScheduleRankData(WorldSchedule worldSchedule, List<City> cities) {
        if (worldSchedule == null) {
            LogUtil.error("世界进度为null，不做统计");
            return;
        }
        // 当前世界进程排行榜
        ScheduleRank scheduleRank = worldSchedule.getRank();
        // 排行榜参数 (0：不做判断，其他必须判断相等)
        int rankParam = scheduleRank.getRankParam();
        // 排行榜分区域
        int rankArea = scheduleRank.getRankArea();
        // 排行榜数据
        List<ScheduleRankItem> scheduleRankItems = scheduleRank.getRank();
        LogUtil.common("世界进程排行榜数据（2、3、4、6、7、8） 统计前scheduleRankItems: " + scheduleRankItems);
        cities.forEach(city -> {
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            // 积分排行榜参数,对应city表的cityPointType 相等
            if (rankParam != staticCity.getCityPointType()) {
                return;
            }
            // camp、area、value
            int camp = city.getCamp();
            int area = staticCity.getArea();
            int value = staticCity.getCityPoint();
            LogUtil.common("增加积分 " + "camp: " + camp + " area: " + area + " value: " + value);
            if (value <= 0) {
                return;
            }
            // 阵营不分区域
            if (SCHEDULE_RANK_AREA_YES != rankArea) {
                ScheduleRankItem item = scheduleRankItems.stream().filter(i -> i.getCamp() == camp).findFirst()
                        .orElse(null);
                if (item == null) {
                    scheduleRankItems.add(new ScheduleRankItem(camp, value));
                } else {
                    item.setValue(item.getValue() + value);
                }
            }
            // 阵营分区域
            else {
                ScheduleRankItem item = scheduleRankItems.stream()
                        .filter(i -> i.getCamp() == camp && i.getArea() == area).findFirst().orElse(null);
                if (item != null) {
                    item.setValue(item.getValue() + value);
                } else {
                    scheduleRankItems.add(new ScheduleRankItem(camp, area, value));
                }
            }
        });
        // reset value
        // 排行榜降序排行
        scheduleRankItems.sort(Comparator.comparingLong(ScheduleRankItem::getValue).reversed());
        LogUtil.common("世界进程排行榜数据（2、3、4、6、7、8） 统计后scheduleRankItems: " + scheduleRankItems);
    }

    /**
     * 增加世界进程排行榜数据(1、5、9) (实时统计)
     *
     * @param worldSchedule 当前世界进度
     * @param camp          camp
     * @param area          area
     * @param value         value
     */
    private void addWorldScheduleRankData(WorldSchedule worldSchedule, int camp, int area, long value) {
        if (worldSchedule == null) {
            LogUtil.error("世界进度为null");
            return;
        }
        // 当前世界进程排行榜
        ScheduleRank scheduleRank = worldSchedule.getRank();
        // 排行榜分区域
        int rankArea = scheduleRank.getRankArea();
        // 排行榜数据
        List<ScheduleRankItem> scheduleRankItems = scheduleRank.getRank();
        if (scheduleRankItems.isEmpty()) {
            // 阵营分区域
            if (rankArea == SCHEDULE_RANK_AREA_YES) {
                scheduleRankItems.add(new ScheduleRankItem(camp, area, value));
            }
            // 阵营不分区域
            else {
                scheduleRankItems.add(new ScheduleRankItem(camp, value));
            }
        } else {
            // 阵营分区域
            if (rankArea == SCHEDULE_RANK_AREA_YES) {
                ScheduleRankItem item = scheduleRankItems.stream()
                        .filter(i -> i.getCamp() == camp && i.getArea() == area).findFirst().orElse(null);
                if (item != null) {
                    item.setValue(item.getValue() + value);
                } else {
                    scheduleRankItems.add(new ScheduleRankItem(camp, area, value));
                }
            }
            // 阵营不分区域
            else {
                ScheduleRankItem item = scheduleRankItems.stream().filter(i -> i.getCamp() == camp).findFirst()
                        .orElse(null);
                if (item != null) {
                    item.setValue(item.getValue() + value);
                } else {
                    scheduleRankItems.add(new ScheduleRankItem(camp, value));
                }
            }
        }
        // 排行榜降序排行
        scheduleRankItems.sort(Comparator.comparingLong(ScheduleRankItem::getValue).reversed());
    }


    /**
     * 添加个人的排行榜
     *
     * @param worldSchedule 当前世界进度
     * @param player        玩家对象
     * @param val           伤害值
     */
    private void addWorldScheduleRankData(WorldSchedule worldSchedule, Player player, int val) {
        if (worldSchedule == null) {
            LogUtil.error("世界进度为null");
            return;
        }
        // 当前世界进程排行榜
        ScheduleRank scheduleRank = worldSchedule.getRank();
        // 个人排行
        List<ScheduleRankItem> personRank = scheduleRank.getPersonRank();
        if (CheckNull.isEmpty(personRank)) {
            personRank.add(new ScheduleRankItem(player.roleId, val));
        } else {
            ScheduleRankItem item = personRank.stream().filter(sri -> sri.getRoleId() == player.roleId).findFirst().orElse(null);
            if (item != null) {
                item.addValue(val);
            } else {
                personRank.add(new ScheduleRankItem(player.roleId, val));
            }
        }
        // 排行榜降序排行
        personRank.sort(Comparator.comparingLong(ScheduleRankItem::getValue).reversed());
    }

}
