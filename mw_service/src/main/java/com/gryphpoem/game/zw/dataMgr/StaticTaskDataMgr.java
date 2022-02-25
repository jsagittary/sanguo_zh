package com.gryphpoem.game.zw.dataMgr;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticDailyTaskAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticPartyTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticSectiontask;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;

import java.util.*;

public class StaticTaskDataMgr {

    private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

    private static Map<Integer, StaticTask> majorMap = new HashMap<>();
    private static Map<Integer, StaticTask> dayiyMap = new HashMap<>();
    private static Map<Integer, StaticTask> liveMap = new HashMap<>();
    private static Map<Integer, StaticTask> sectionMap = new HashMap<>(); // 剧情任务
    private static Map<Integer, StaticPartyTask> partyTaskMap;
    private static Map<Integer, StaticTask> advanceMap = new HashMap<>();
    private static Map<Integer, List<StaticTask>> triggerMap = new HashMap<>();
    private static List<StaticTask> openList = new ArrayList<>();
    private static List<StaticTask> dayiyList = new ArrayList<StaticTask>();
    private static List<StaticTask> liveList = new ArrayList<StaticTask>();
    private static List<StaticTask> sectionList = new ArrayList<StaticTask>();
    private static List<StaticTask> advanceList = new ArrayList<>();

    // 所有的任务
    private static List<StaticTask> allTask = new ArrayList<>();
    // private List<StaticTaskLive> taskLiveList = new ArrayList<StaticTaskLive>();
    /** 最后一个主线任务任务 */
    private static StaticTask lastMainTask;

    // 剧情章节
    private static List<StaticSectiontask> sectiontaskList;
    // 剧情章节 key:sectionId
    private static Map<Integer, StaticSectiontask> sectiontaskMap = new HashMap<>();
    // 日常任务活跃度奖励
    private static Map<Integer, StaticDailyTaskAward> dailyTaskAwardMap;
    // 记录第一个任务的id
    private static StaticTask firstTask;

    public static void init() {
        List<StaticTask> list = staticDataDao.selectTask();
        StaticTaskDataMgr.allTask = list;
        Map<Integer, StaticTask> majorMap = new HashMap<>();
        Map<Integer, StaticTask> dayiyMap = new HashMap<>();
        Map<Integer, StaticTask> liveMap = new HashMap<>();
        Map<Integer, StaticTask> sectionMap = new HashMap<>();// 剧情任务
        Map<Integer, StaticTask> advanceMap = new HashMap<>();// 个人目标
        Map<Integer, List<StaticTask>> triggerMap = new HashMap<>();
        List<StaticTask> openList = new ArrayList<>();
        List<StaticTask> dayiyList = new ArrayList<StaticTask>();
        List<StaticTask> liveList = new ArrayList<StaticTask>();
        List<StaticTask> sectionList = new ArrayList<StaticTask>();
        List<StaticTask> advanceList = new ArrayList<>();
        for (StaticTask e : list) {
            int triggerId = e.getTriggerId();
            if (e.getType() == TaskType.TYPE_MAIN || e.getType() == TaskType.TYPE_SUB) {
                majorMap.put(e.getTaskId(), e);
                List<StaticTask> ttlist = triggerMap.computeIfAbsent(triggerId, k -> new ArrayList<>());
                ttlist.add(e);
            } else if (e.getType() == TaskType.TYPE_DAYIY) {
                dayiyMap.put(e.getTaskId(), e);
                dayiyList.add(e);
            } else if (e.getType() == TaskType.TYPE_LIVE) {
                liveMap.put(e.getTaskId(), e);
                liveList.add(e);
            } else if (e.getType() == TaskType.TYPE_SECTION) {
                sectionMap.put(e.getTaskId(), e);
                sectionList.add(e);
            } else if (e.getType() == TaskType.TYPE_ADVANCE) {
                advanceMap.put(e.getTaskId(), e);
                advanceList.add(e);
            }
            if (e.getIsOpen() > 0) {
                openList.add(e);
            }
        }
        StaticTaskDataMgr.majorMap = majorMap;
        StaticTaskDataMgr.triggerMap = triggerMap;
        StaticTaskDataMgr.dayiyMap = dayiyMap;
        StaticTaskDataMgr.sectionMap = sectionMap;
        StaticTaskDataMgr.dayiyList = dayiyList;
        StaticTaskDataMgr.liveMap = liveMap;
        StaticTaskDataMgr.liveList = liveList;
        StaticTaskDataMgr.openList = openList;
        StaticTaskDataMgr.sectionList = sectionList;
        StaticTaskDataMgr.advanceMap = advanceMap;
        StaticTaskDataMgr.advanceList = advanceList;

        // List<StaticTaskLive> taskLiveList = staticDataDao.selectLiveTask();
        // this.taskLiveList = taskLiveList;

        Map<Integer, StaticPartyTask> partyTaskMap = staticDataDao.selectPartyTaskMap();
        StaticTaskDataMgr.partyTaskMap = partyTaskMap;
        // 剧情
        StaticTaskDataMgr.sectiontaskList = staticDataDao.selectSectiontask();
        int firstTaskId = -1;
        int firstSectionId = Integer.MAX_VALUE;
        for (StaticSectiontask ss : sectiontaskList) {
            StaticTaskDataMgr.sectiontaskMap.put(ss.getSectionId(), ss);
            if (firstSectionId > ss.getSectionId()) {
                firstSectionId = ss.getSectionId();
                firstTaskId = ss.getSectionTask().get(0);
            }
        }
        if (firstTaskId > 0) {
            StaticTaskDataMgr.firstTask = sectionMap.get(firstTaskId);
        }

        StaticTaskDataMgr.dailyTaskAwardMap = staticDataDao.selectStaticDailyTaskAwardMap();
        StaticTaskDataMgr.lastMainTask = getAllTask().stream().filter(st -> st.getType() == TaskType.TYPE_MAIN)
                .max(Comparator.comparing(StaticTask::getMainSort)).get();

    }

    public static Map<Integer, StaticTask> getMajorMap() {
        return majorMap;
    }

    public static Map<Integer, StaticTask> getDayiyMap() {
        return dayiyMap;
    }

    public static Map<Integer, StaticTask> getLiveMap() {
        return liveMap;
    }

    public static StaticTask getTaskById(int taskId) {
        if (majorMap.containsKey(taskId)) {
            return majorMap.get(taskId);
        } else if (dayiyMap.containsKey(taskId)) {
            return dayiyMap.get(taskId);
        } else if (liveMap.containsKey(taskId)) {
            return liveMap.get(taskId);
        } else if (sectionMap.containsKey(taskId)) {
            return sectionMap.get(taskId);
        } else if (advanceMap.containsKey(taskId)) {
            return advanceMap.get(taskId);
        }
        return null;
    }

    public static List<StaticTask> getInitMajorTask() {
        return triggerMap.get(0);
    }

    public static List<StaticTask> getLiveList() {
        return liveList;
    }

    /*
    public static List<Integer> getRadomDayiyTask() {// 随机五个任务
        List<Integer> rs = new ArrayList<Integer>();
        List<Integer> tempList = new ArrayList<Integer>();
        List<Integer> probabilityList = new ArrayList<Integer>();
        int seed = 0;
        for (StaticTask ee : dayiyList) {
            tempList.add(ee.getTaskId());
            probabilityList.add(ee.getProbability());
            seed += ee.getProbability();
        }
        for (int i = 0; i < 5; i++) {
            int total = 0;
            int goal = RandomHelper.randomInSize(seed);
            for (int j = 0; j < probabilityList.size(); j++) {
                int probability = probabilityList.get(j);
                total += probability;
                if (goal <= total) {
                    seed -= probability;
                    rs.add(tempList.remove(j));
                    probabilityList.remove(j);
                    break;
                }
            }
        }
        return rs;
    }
    
    public static int getOneDayiyTask(Set<Integer> curTaskIds) {
        List<StaticTask> dayTasks = new ArrayList<>();
        int seeds[] = { 0, 0 };
        for (StaticTask ee : dayiyList) {
            if (curTaskIds.contains(ee.getTaskId())) {
                continue;
            }
            seeds[0] += ee.getProbability();
            dayTasks.add(ee);
        }
        seeds[0] = RandomHelper.randomInSize(seeds[0]);
        for (StaticTask ee : dayTasks) {
            seeds[1] += ee.getProbability();
            if (seeds[0] <= seeds[1]) {
                return ee.getTaskId();
            }
        }
        return 0;
    }
      */

    public static List<StaticTask> getTriggerTask(int taskId) {
        return triggerMap.get(taskId);
    }

    public static StaticPartyTask getPartyTask(int id) {
        return partyTaskMap.get(id);
    }

    public static Map<Integer, StaticPartyTask> getPartyTaskMap() {
        return partyTaskMap;
    }

    public static List<StaticTask> getOpenList() {
        return openList;
    }

    public static List<StaticTask> getAllTask() {
        return allTask;
    }

    public static Map<Integer, StaticTask> getSectionMap() {
        return sectionMap;
    }

    public static List<StaticTask> getSectionList() {
        return sectionList;
    }

    public static List<StaticSectiontask> getSectiontaskList() {
        return sectiontaskList;
    }

    public static StaticSectiontask getSectiontaskById(int id) {
        return sectiontaskMap.get(id);
    }

    public static StaticTask getFirstTask() {
        return firstTask;
    }

    public static StaticDailyTaskAward getDailyTaskAwardMapById(int id) {
        return dailyTaskAwardMap.get(id);
    }

    public static StaticTask getLastMainTask() {
        return lastMainTask;
    }

    public static Map<Integer, StaticTask> getAdvanceMap() {
        return advanceMap;
    }

    // public StaticTaskLive getTaskLive(int liveAd, int totalLive) {
    // for (StaticTaskLive e : taskLiveList) {
    // if (e.getLive() > liveAd && e.getLive() <= totalLive) {
    // return e;
    // }
    // }
    // return null;
    // }

}
