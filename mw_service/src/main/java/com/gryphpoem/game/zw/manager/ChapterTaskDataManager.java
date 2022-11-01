package com.gryphpoem.game.zw.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticChapterTaskDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticEquip;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticTaskChapter;
import com.gryphpoem.game.zw.resource.pojo.Equip;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.chapterTask.ChapterTask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * desc: 章节任务
 * author: huangxm
 * date: 2022/5/25 9:54
 **/
@Component
public class ChapterTaskDataManager {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private TaskDataManager taskDataManager;

    /**
     * 更新玩家任务
     *
     * @param player
     * @param cond     参见TaskType类
     * @param schedule 增加进度值
     * @param param
     */
    public void updTask(Player player, int cond, int schedule, int... param) {
        if (null == player || cond <= 0 || schedule <= 0) {
            return;
        }
        checkPlayerChapterTask(player);
        // 主线任务
        List<CommonPb.Task> changeTaskList = Lists.newArrayList();
        ChapterTask chapterTask = player.chapterTask;
        Map<Integer, Task> updateTask = chapterTask.getOpenTasks();
        for (Task task : updateTask.values()) {
            StaticTask staticTask = StaticTaskDataMgr.getTaskById(task.getTaskId());
            if (Objects.isNull(staticTask) || staticTask.getCond() != cond) continue;
            if (taskDataManager.modifyTaskSchedule(player, task, staticTask.getCond(), staticTask.getCondId(), staticTask.getSchedule(), schedule, param)
                    && isSync(player, staticTask)) {
                changeTaskList.add(PbHelper.createTaskPb(task, staticTask.getType()));
            }
        }
        this.synTaskInfo(player, changeTaskList);
    }

    /**
     * 是否给玩家推送此任务的进度
     *
     * @param player
     * @param staticTask
     * @return
     */
    public boolean isSync(Player player, StaticTask staticTask) {
        if (Objects.isNull(player) || Objects.isNull(staticTask)) return false;
        Map<Integer, StaticTask> staticChapterTaskMap = StaticTaskDataMgr.getStaticChapterTaskMap(player.chapterTask.getChapterId());
        if (Objects.isNull(staticChapterTaskMap)) return false;
        if (staticTask.getType() == TaskType.TYPE_SUB) return true;
        return staticTask.getType() == TaskType.TYPE_MAIN && staticChapterTaskMap.containsKey(staticTask.getTaskId());
    }

    /**
     * 同步任务改变
     *
     * @param player
     * @param changeList
     */
    public void synTaskInfo(Player player, List<CommonPb.Task> changeList) {
        if (CheckNull.nonEmpty(changeList)) {
            GamePb5.SynChapterTaskInfoRs builder = GamePb5.SynChapterTaskInfoRs.newBuilder().addAllTask(changeList).build();
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb5.SynChapterTaskInfoRs.EXT_FIELD_NUMBER, GamePb5.SynChapterTaskInfoRs.ext, builder);
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 起服或者迭代改变时检查所有玩家身上的任务
     */
    public void checkAllPlayerChapterTask() {
        playerDataManager.getAllPlayer().values().stream().filter(Objects::nonNull).forEach(this::checkPlayerChapterTask);
    }

    /**
     * 检查玩家的任务
     */
    public void checkPlayerChapterTask(Player player) {
        if (Objects.isNull(player)) return;
        ChapterTask chapterTask = player.chapterTask;
        // 当前章节任务已领取，说明下一章节不存在，当迭代刷新时会刷新下一章节
        if (chapterTask.currentChapterIsReceive()) {
            chapterTask.setChapter(getNextChapter(player));
        }
        Map<Integer, StaticTask> staticTaskNewMap = StaticTaskDataMgr.getStaticChapterTaskMap(chapterTask.getChapterId());
        // 累计的任务和已完成的任务
        Map<Integer, Task> playerOpenTasks = chapterTask.getOpenTasks();
        if (CheckNull.nonEmpty(staticTaskNewMap)) {
            staticTaskNewMap.values().forEach(staticTaskNew -> {
                playerOpenTasks.computeIfAbsent(staticTaskNew.getTaskId(), x -> new Task(staticTaskNew.getTaskId()));
            });
        } else {
            LogUtil.error("获取玩家章节任务为空，玩家id：", player.roleId, "玩家任务章节：", chapterTask.getChapterId());
        }
        List<StaticTask> openList = StaticTaskDataMgr.getOpenList();
        if (CheckNull.nonEmpty(openList)) {
            openList.forEach(staticTaskNew -> {
                playerOpenTasks.computeIfAbsent(staticTaskNew.getTaskId(), x -> new Task(staticTaskNew.getTaskId()));
            });
        } else {
            LogUtil.error("获取玩家累计任务为空，玩家id：", player.roleId);
        }
    }

    /**
     * 获取玩家的下一章节id
     * 没有则返回当前章节
     */
    public int getNextChapter(Player player) {
        Map<Integer, StaticTaskChapter> staticTaskChapterMap = StaticChapterTaskDataMgr.getStaticTaskChapterMap();
        StaticTaskChapter staticTaskChapter = staticTaskChapterMap.values().stream()
                .filter(chapterTask -> chapterTask.getChapterId() > player.chapterTask.getChapterId())
                .min(Comparator.comparing(StaticTaskChapter::getChapterId)).orElse(null);
        if (Objects.nonNull(staticTaskChapter)) {
            return staticTaskChapter.getChapterId();
        }
        return player.chapterTask.getChapterId();
    }

    /**
     * 获取玩家的任务列表
     *
     * @param player
     * @return
     */
    public Map<Integer, CommonPb.Task> getTasksPb(Player player) {
        Map<Integer, CommonPb.Task> tasksPbMap = Maps.newHashMap();
        if (Objects.isNull(player)) return tasksPbMap;
        ChapterTask chapterTask = player.chapterTask;
        int chapterId = chapterTask.getChapterId();
        // 章节任务(主线任务)
        Optional.ofNullable(StaticTaskDataMgr.getStaticChapterTaskMap(chapterId))
                .ifPresent(map -> {
                    for (StaticTask staticTask : map.values()) {
                        if (Objects.isNull(staticTask)) {
                            continue;
                        }
                        Task task = chapterTask.getOpenTasks().computeIfAbsent(staticTask.getTaskId(), x -> new Task(staticTask.getTaskId()));
                        taskDataManager.currentMajorTask(player, task, staticTask);
                        if (task.getSchedule() >= staticTask.getSchedule() && task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                            task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        }
                        if (task.getSchedule() < staticTask.getSchedule() && task.getStatus() == TaskType.TYPE_STATUS_FINISH) {
                            task.setStatus(TaskType.TYPE_STATUS_UNFINISH);
                        }
                        tasksPbMap.put(task.getTaskId(), PbHelper.createTaskPb(task, staticTask.getType()));
                    }
                });


        // 获取已经完成主线任务
        List<StaticTask> mainSTasks = chapterTask.getOpenTasks().values().stream()
                .filter(t -> t.getStatus() == TaskType.TYPE_STATUS_REWARD)// 已经完成
                .map(t -> StaticTaskDataMgr.getMajorMap().get(t.getTaskId()))
                .filter(staticTask -> staticTask != null && staticTask.getType() == TaskType.TYPE_MAIN) // 主线任务
                .sorted(Comparator.comparing(StaticTask::getTaskId))
                .collect(Collectors.toList());

        // 2. 找当前正在进行支线任务
        List<StaticTask> curTriggerTasks = new ArrayList<>(); // 已经触发的任务
        for (StaticTask st : mainSTasks) {// 找每个主线 已经触发的支线任务
            if (getTaskStatusByPlayer(player, st) == TaskType.TYPE_STATUS_REWARD) {// 主线领取才能触发
                curTriggerTasks.addAll(findSubTask(player, st));
            }
        }
        // 每种类型只能有一种
        Set<Integer> subTypes = new HashSet<>();
        for (StaticTask cst : curTriggerTasks) {
            if (Objects.isNull(cst) || cst.getChapter() != 0) continue;
            Task task = chapterTask.getOpenTasks().computeIfAbsent(cst.getTaskId(), e -> new Task(cst.getTaskId()));
            if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) continue;
            int type = cst.getSubType();
            if (subTypes.contains(type)) continue;
            subTypes.add(type);
            taskDataManager.currentMajorTask(player, task, cst);
            tasksPbMap.put(task.getTaskId(), PbHelper.createTaskPb(task, cst.getType()));
        }
        return tasksPbMap;
    }

    /**
     * 获取某个任务的状态
     */
    public int getTaskStatusByPlayer(Player player, StaticTask t) {
        Task task = player.chapterTask.getOpenTasks().get(t.getTaskId());
        return Objects.isNull(task) ? TaskType.TYPE_STATUS_UNFINISH : task.getStatus();
    }

    /**
     * 更具主线获取支线
     */
    public List<StaticTask> findSubTask(Player player, StaticTask staticTask) {
        List<StaticTask> subTask = Lists.newArrayList();
        Optional.ofNullable(StaticTaskDataMgr.getTriggerTask(staticTask.getTaskId()))
                .ifPresent(triggerList -> {
                    // 有可触发的任务
                    for (StaticTask t : triggerList) {
                        if (t.getType() == TaskType.TYPE_SUB) {
                            if (getTaskStatusByPlayer(player, t) == TaskType.TYPE_STATUS_REWARD) {
                                subTask.addAll(findSubTask(player, t));
                            } else {
                                subTask.add(t);
                            }
                        }
                    }
                });
        return subTask;
    }

    /**
     * 获取玩家所有英雄指定位置、某一品质装备的个数
     */
    public long getHeroQualityEquipCount(Player player, int sCondId, int quality) {
        return player.getAllOnBattleHeros().stream()
                .filter(hero -> {
                    if (sCondId <= hero.getEquip().length) {
                        Equip equip = player.equips.get(hero.getEquip()[sCondId]);
                        if (Objects.nonNull(equip)) {
                            StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
                            return Objects.nonNull(staticEquip) && staticEquip.getQuality() >= quality;
                        }
                    }
                    return false;
                }).count();
    }

    /**
     * 所有英雄穿戴中的装备共有x条x级及以上攻击改造属性
     */
    public long getHeroEquipReformAttack(Player player, int sCondId) {
        List<List<Turple<Integer, Integer>>> collect = player.equips.values().stream().map(e -> {
            if (e.getHeroId() > 0 && CheckNull.nonEmpty(e.getAttrAndLv())) {
                return e.getAttrAndLv();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        long count = 0;
        for (List<Turple<Integer, Integer>> turples : collect) {
            for (Turple<Integer, Integer> turple : turples) {
                if (Objects.nonNull(turple) && turple.getA() == 1 && turple.getB() >= sCondId) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 所有英雄穿戴中的装备共有x条x级及以上任意改造属性
     */
    public long getHeroEquipReform(Player player, int sCondId) {
        List<List<Turple<Integer, Integer>>> collect = player.equips.values().stream().map(e -> {
            if (e.getHeroId() > 0 && CheckNull.nonEmpty(e.getAttrAndLv())) {
                return e.getAttrAndLv();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        long count = 0;
        for (List<Turple<Integer, Integer>> turples : collect) {
            for (Turple<Integer, Integer> turple : turples) {
                if (Objects.nonNull(turple) && turple.getB() >= sCondId) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 上阵英雄一共穿戴x件x品质及以上的装备
     */
    public long battleHeroWearingEquipment(Player player, int sCondId) {
        long count = 0;
        for (Hero hero : player.getAllOnBattleHeros()) {
            int[] equips = hero.getEquip();
            if (Objects.isNull(equips)) continue;
            for (int equipKey : equips) {
                Equip equip = player.equips.get(equipKey);
                if (Objects.isNull(equip)) continue;
                StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
                if (Objects.nonNull(staticEquip) && staticEquip.getQuality() >= sCondId) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 任意一名英雄穿戴x件x品质及以上的装备
     */
    public long arbitrarilyHeroWearingEquipment(Player player, int sCondId, int sSchedule) {
        int maxCount = 0;
        for (Hero hero : player.heros.values()) {
            int count = 0;
            for (int i = 0; i < hero.getEquip().length; i++) {
                Equip equip = player.equips.get(hero.getEquip()[i]);
                if (Objects.nonNull(equip)) {
                    StaticEquip staticEquip = StaticPropDataMgr.getEquipMap().get(equip.getEquipId());
                    if (Objects.nonNull(staticEquip) && staticEquip.getQuality() >= sCondId) {
                        count++;
                    }
                }
            }
            maxCount = Math.max(maxCount, count);
            if (maxCount >= sSchedule) break;
        }
        return maxCount;
    }

    /**
     * 是否是主线、支线任务
     *
     * @param type
     * @return
     */
    public static boolean isMajorTask(int type) {
        return type == TaskType.TYPE_MAIN || type == TaskType.TYPE_SUB;
    }

    /**
     * 向前端推送主线、支线任务
     *
     * @param player
     */
    public void pushMajorTask(Player player, Task task) {
        if (Objects.isNull(task)) return;
        Optional.ofNullable(StaticTaskDataMgr.getTaskById(task.getTaskId())).ifPresent(sTask -> {
            if (isMajorTask(sTask.getType())) {
                synTaskInfo(player, Lists.newArrayList(PbHelper.createTaskPb(task, sTask.getType())));
            }
        });
    }
}