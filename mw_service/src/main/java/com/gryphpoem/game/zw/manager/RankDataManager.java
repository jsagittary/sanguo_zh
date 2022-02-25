package com.gryphpoem.game.zw.manager;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.pb.GamePb2.GetRankRs;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.Constant.RankType;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Cia;
import com.gryphpoem.game.zw.resource.domain.p.FemaleAgent;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.p.PitchCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticPitchCombat;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RankDataManager {
    private static final int MAX_RANK_NUM = 1000000; // 排行榜上限
    /** 0.全服 */
    public static final int WORLD_SCOPE = 0;
    /** 1.本区域 */
    public static final int AREA_SCOPE = 1;
    /** 2.本阵营 */
    public static final int CAMP_SCOPE = 2;
    // 3 被客户端占用
    /** 4.指定阵营 */
    public static final int CUSTOM_CAMP_SCOPE = 4;

    // @Autowired
    // private PartyDataManager partyDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private RobotDataManager robotDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private SeasonService seasonService;

    // //战斗力
    // public RankList fightRankList = new RankList();
    // public Set<Long> fightRankSet = new HashSet<>();
    //
    // // 等级
    // public RankList lvRankList = new RankList();
    // public Set<Long> lvRankSet = new HashSet<>();
    //
    // // 军工
    // public RankList exploitList = new RankList();
    // public Set<Long> exploitSet = new HashSet<>();
    //
    // // 军阶
    // public RankList ranksList = new RankList();
    // public Set<Long> ranksSet = new HashSet<>();
    //
    // // 副本
    // public RankList starsRankList = new RankList();
    // public Set<Long> starsRankSet = new HashSet<>();

    // 混合排行
    public RankList complexRankList = new RankList();
    public Set<Long> complexRankSet = new HashSet<>();

    // 特工总等级
    public RankList agentAllLvRankList = new RankList();
    public Set<Long> agentAllLvRankSet = new HashSet<>();

    // 荣耀演练场副本排行 type=1
    public RankList pitchComabtType1RankList = new RankList();
    public Set<Long> pitchComabtType1RankSet = new HashSet<>();

    public void init() {
        // 注册EventBus
        EventBus.getDefault().register(this);

        //加载赛季排行数据
        seasonService.loadRankDataOnStartup();
    }

    /**
     * 计算特工总等级
     * 
     * @param player
     * @return
     */
    public static int calcAgentAllLv(Player player) {
        Cia cia = player.getCia();
        if (cia == null) {
            return 0;
        }
        int allLv = 0;
        for (FemaleAgent fa : cia.getFemaleAngets().values()) {
            int lv = fa.getQuality() - 1;
            lv = lv <= 0 ? 0 : lv;
            allLv += lv;
        }
        return allLv;
    }

    public void load(Player player) {
        if (robotDataManager.isRobot(player.lord.getLordId())) {
            return;
        }
        Lord lord = player.lord;
        // fightRankList.add(lord);
        // lvRankList.add(lord);
        // exploitList.add(lord);
        // ranksList.add(lord);
        // if (lord.combatId > 0) {
        // starsRankList.add(lord);
        // }
        complexRankList.add(lord);
        // 计算特工总等级
        int allLv = calcAgentAllLv(player);
        lord.setAgentAllLv(allLv);
        agentAllLvRankList.add(lord);

        // 荣耀演练场副本排行 type=1
        PitchCombat pitchCombat = player.getPitchCombat(StaticPitchCombat.PITCH_COMBAT_TYPE_1);
        int combatIdType1 = pitchCombat == null ? 0 : pitchCombat.getHighestCombatId();
        lord.setPitchType1CombatId(combatIdType1);
        pitchComabtType1RankList.add(lord);

    }

    @Subscribe
    public void fightChangeEvent(Events.FightChangeEvent events) {
        this.setFight(events.player.lord);
        playerDataManager.syncFightChange(events.player);
        // 7日判断推送
        final long fight = events.player.lord.getFight();
        Java8Utils.syncMethodInvoke(
                () -> activityDataManager.updDay7ActSchedule(events.player, ActivityConst.ACT_TASK_FIGHT, fight));
        Java8Utils.syncMethodInvoke(
                () -> activityDataManager.updRankActivity(events.player, ActivityConst.ACT_CAMP_RANK, fight));
        Java8Utils.syncMethodInvoke(
                () -> activityDataManager.updRankActivity(events.player, ActivityConst.ACT_CAMP_FIGHT_RANK, fight));
    }

    public void sort() {
        // Collections.sort(fightRankList.getList(), new ComparatorFight());
        // while (fightRankList.getSize() > MAX_RANK_NUM) {
        // fightRankList.removeLast();
        // }
        //
        // Collections.sort(lvRankList.getList(), new ComparatorLv());
        // while (lvRankList.getSize() > MAX_RANK_NUM) {
        // lvRankList.removeLast();
        // }
        //
        // Collections.sort(exploitList.getList(), new ComparatorExploit());
        // while (exploitList.getSize() > MAX_RANK_NUM) {
        // exploitList.removeLast();
        // }
        // Collections.sort(ranksList.getList(), new ComparatorRanks());
        // while (ranksList.getSize() > MAX_RANK_NUM) {
        // ranksList.removeLast();
        // }
        //
        // Collections.sort(starsRankList.getList(), new ComparatorStarsRank());
        // while (starsRankList.getSize() > MAX_RANK_NUM) {
        // starsRankList.removeLast();
        // }

        Collections.sort(complexRankList.getList(), new ComparatorComplexRank());
        while (complexRankList.getSize() > MAX_RANK_NUM) {
            complexRankList.removeLast();
        }

        Collections.sort(agentAllLvRankList.getList(), new ComparatorAgentAllLvRank());
        while (agentAllLvRankList.getSize() > MAX_RANK_NUM) {
            agentAllLvRankList.removeLast();
        }

        Collections.sort(pitchComabtType1RankList.getList(), new ComparatorPitcombatType1Rank());
        while (pitchComabtType1RankList.getSize() > MAX_RANK_NUM) {
            pitchComabtType1RankList.removeLast();
        }

        // for (Lord lord : fightRankList.getList()) {
        // fightRankSet.add(lord.getLordId());
        // }
        // for (Lord lord : lvRankList.getList()) {
        // lvRankSet.add(lord.getLordId());
        // }
        // for (Lord lord : exploitList.getList()) {
        // exploitSet.add(lord.getLordId());
        // }
        // for (Lord lord : ranksList.getList()) {
        // ranksSet.add(lord.getLordId());
        // }
        // for (Lord lord : starsRankList.getList()) {
        // starsRankSet.add(lord.getLordId());
        // }
        for (Lord lord : complexRankList.getList()) {
            complexRankSet.add(lord.getLordId());
        }
        for (Lord lord : agentAllLvRankList.getList()) {
            agentAllLvRankSet.add(lord.getLordId());
        }
        for (Lord lord : pitchComabtType1RankList.getList()) {
            pitchComabtType1RankSet.add(lord.getLordId());
        }
    }

    /**
     * 更新战力榜
     * 
     * @param lord
     */
    public void setFight(Lord lord) {
        // updateRank(lord, fightRankList, fightRankSet, RankType.type_1, new ComparatorFight());
        setComplex(lord);
        setAgentAllLv(lord);
        setPitchType1CombatId(lord);
    }

    /**
     * 更新等级榜
     * 
     * @param lord
     */
    public void setRoleLv(Lord lord) {
        // updateRank(lord, lvRankList, lvRankSet, RankType.type_2, new ComparatorLv());
        setComplex(lord);
    }

    /**
     * 更新军工榜
     * 
     * @param lord
     */
    public void setExploit(Lord lord) {
        // updateRank(lord, exploitList, exploitSet, RankType.type_3, new ComparatorExploit());
    }

    /**
     * 更新军阶榜
     * 
     * @param lord
     */
    public void setRanks(Lord lord) {
        // updateRank(lord, ranksList, ranksSet, RankType.type_4, new ComparatorRanks());
        setComplex(lord);
        setAgentAllLv(lord);
        setPitchType1CombatId(lord);
    }

    /**
     * 更新副本榜
     * 
     * @param lord
     */
    public void setStars(Lord lord) {
        // updateRank(lord, starsRankList, starsRankSet, RankType.type_5, new ComparatorStarsRank());
    }

    /**
     * 更新混合排行榜 (军阶>战力>等级)
     * 
     * @param lord
     */
    public void setComplex(Lord lord) {
        refreshBaseRank(lord, complexRankList, complexRankSet, new ComparatorComplexRank());

        // if (robotDataManager.isRobot(lord.getLordId())) {
        // return;
        // }
        // if (complexRankSet.contains(lord.getLordId())) { // 排行榜内部进行重排序
        // Collections.sort(complexRankList.getList(), new ComparatorComplexRank());
        // } else {
        // // 排行榜没有数据
        // int size = complexRankList.getSize();
        // if (size == 0) {// 排行榜空时添加
        // complexRankList.add(lord);
        // complexRankSet.add(lord.getLordId());
        // } else if (size <= MAX_RANK_NUM) {
        // complexRankList.add(lord);
        // complexRankSet.add(lord.getLordId());
        // Collections.sort(complexRankList.getList(), new ComparatorComplexRank());
        // } else {
        // complexRankList.add(lord);
        // complexRankSet.add(lord.getLordId());
        // Collections.sort(complexRankList.getList(), new ComparatorComplexRank());
        // // 移除最后一个
        // long rmId = complexRankList.removeLast();
        // complexRankSet.remove(rmId);
        // }
        // }
        // Camp party = partyDataManager.getCampInfo(lord.getCamp());
        // if (party.getStatus() != 0) {
        // party.addEclection(lord.getLordId(), lord.getNick(), lord.getLevel(), lord.getFight(), lord.getRanks());
        // }
    }

    /**
     * 更新荣耀演练场副本排行
     * 
     * @param lord
     */
    public void setPitchType1CombatId(Lord lord) {
        refreshBaseRank(lord, pitchComabtType1RankList, pitchComabtType1RankSet, new ComparatorPitcombatType1Rank());
        // if (lord.getPitchType1CombatId() > 0) {
        // }
    }

    /**
     * 更新特工等级排行
     * 
     * @param lord
     */
    public void setAgentAllLv(Lord lord) {
        refreshBaseRank(lord, agentAllLvRankList, agentAllLvRankSet, new ComparatorAgentAllLvRank());
    }

    private void refreshBaseRank(Lord lord, RankList rankList, Set<Long> set, Comparator<Lord> cmptor) {
        if (robotDataManager.isRobot(lord.getLordId())) {
            return;
        }
        if (set.contains(lord.getLordId())) { // 排行榜内部进行重排序
            Collections.sort(rankList.getList(), cmptor);
        } else {
            // 排行榜没有数据
            int size = rankList.getSize();
            if (size == 0) {// 排行榜空时添加
                rankList.add(lord);
                set.add(lord.getLordId());
            } else if (size <= MAX_RANK_NUM) {
                rankList.add(lord);
                set.add(lord.getLordId());
                Collections.sort(rankList.getList(), cmptor);
            } else {
                rankList.add(lord);
                set.add(lord.getLordId());
                Collections.sort(rankList.getList(), cmptor);
                // 移除最后一个
                long rmId = rankList.removeLast();
                set.remove(rmId);
            }
        }
    }

    private void updateRank(Lord lord, RankList rankList, Set<Long> rankSet, int type, Comparator<Lord> comparator) {
        // 处于线程安全考虑，机器人暂不加入排行榜
        if (robotDataManager.isRobot(lord.getLordId())) {
            return;
        }

        if (rankSet.contains(lord.getLordId())) {
            Collections.sort(rankList.getList(), comparator);
        } else {
            int size = rankList.getSize();
            if (size == 0) {
                rankList.add(lord);
                rankSet.add(lord.getLordId());
            } else {
                boolean added = false;
                ListIterator<Lord> it = rankList.getList().listIterator(size);
                while (it.hasPrevious()) {
                    if (type == RankType.type_1) { // 战力榜
                        if (lord.getFight() <= it.previous().getFight()) {
                            it.next();
                            it.add(lord);
                            added = true;
                            break;
                        }
                    } else if (type == RankType.type_2) {
                        if (lord.getLevel() <= it.previous().getLevel()) {
                            it.next();
                            it.add(lord);
                            added = true;
                            break;
                        }
                    } else if (type == RankType.type_3) {
                        if (lord.getExploit() <= it.previous().getExploit()) {
                            it.next();
                            it.add(lord);
                            added = true;
                            break;
                        }
                    } else if (type == RankType.type_4) {
                        if (lord.getRanks() <= it.previous().getRanks()) {
                            it.next();
                            it.add(lord);
                            added = true;
                            break;
                        }
                    } else if (type == RankType.type_5) {
                        if (lord.getCombatId() <= it.previous().getCombatId()) {
                            it.next();
                            it.add(lord);
                            added = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }

                if (!added) {
                    rankList.getList().addFirst(lord);
                }

                rankList.setSize(size + 1);
                rankSet.add(lord.getLordId());

                if (rankList.getSize() > MAX_RANK_NUM) {
                    rankSet.remove(rankList.removeLast());
                }
            }
        }
    }

    public GetRankRs getRank(Lord myLord, int type, int page, int scope, int camp) {
        GetRankRs.Builder builder = GetRankRs.newBuilder();
        switch (type) {
            // case RankType.type_1: // 战力榜
            // processRank(myLord, type, page, scope, fightRankList, builder);
            // break;
            // case RankType.type_2:
            // processRank(myLord, type, page, scope, lvRankList, builder);
            // break;
            // case RankType.type_3:
            // processRank(myLord, type, page, scope, exploitList, builder);
            // break;
            // case RankType.type_4:
            // processRank(myLord, type, page, scope, ranksList, builder);
            // break;
            // case RankType.type_5:
            // processRank(myLord, type, page, scope, starsRankList, builder);
            // break;
            case RankType.type_6:
                processRank(myLord, type, page, scope, camp, complexRankList, builder);
                break;
            case RankType.type_7:
                processRank(myLord, type, page, scope, camp, agentAllLvRankList, builder);
                break;
            case RankType.type_8:
                processRank(myLord, type, page, scope, camp, pitchComabtType1RankList, builder);
                break;

        }
        if (page == 1) {// 第一页时给出自己的排名
            int[] rankAndSize = getPlayerRank(type, myLord, scope, camp);
            if (rankAndSize != null) {
                builder.setRank(rankAndSize[0]);
                builder.setRankSize(rankAndSize[1]);
            }

        }
        return builder.build();
    }

    private void processRank(Lord myLord, int type, int page, int scope, int camp, RankList fightRankList,
            GetRankRs.Builder builder) {
        final int pageCount = Constant.RANK_PAGE_CNT;// 每页显示多少个
        int begin = (page - 1) * pageCount;
        int end = begin + pageCount;
        int index = 0;
        // long val = 0;
        Iterator<Lord> it = fightRankList.getList().iterator();
        while (it.hasNext()) {
            if (index >= end) {
                break;
            }
            Lord lord = (Lord) it.next();
            Player player = playerDataManager.getPlayer(lord.getLordId());
            if (Objects.isNull(player)) {
                break;
            }
            // if (scope > 0 && lord.getArea() != scope) {
            // continue;
            // }
            if (AREA_SCOPE == scope && myLord.getArea() != lord.getArea()) {// 本区域
                continue;
            }
            if (CAMP_SCOPE == scope && myLord.getCamp() != lord.getCamp()) {// 本阵营
                continue;
            }
            if (CUSTOM_CAMP_SCOPE == scope && camp != lord.getCamp()) {// 指定阵营
                continue;
            }

            if (index >= begin) {
                if (type == RankType.type_7) {
                    String agentLv = String.valueOf(lord.getAgentAllLv());
                    builder.addRankData(
                            PbHelper.createRankDataPb(lord.getNick(), lord.getLevel(), lord.getFight(), lord.getArea(),
                                    lord.getCamp(), lord.getPortrait(), lord.getRanks(), lord.getLordId(), player.getDressUp().getCurPortraitFrame(), agentLv));
                } else if (type == RankType.type_8) {
                    String combatId = String.valueOf(lord.getPitchType1CombatId());
                    builder.addRankData(
                            PbHelper.createRankDataPb(lord.getNick(), lord.getLevel(), lord.getFight(), lord.getArea(),
                                    lord.getCamp(), lord.getPortrait(), lord.getRanks(), lord.getLordId(), player.getDressUp().getCurPortraitFrame(), combatId));
                } else {
                    builder.addRankData(PbHelper.createRankDataPb(lord.getNick(), lord.getLevel(), lord.getFight(),
                            lord.getArea(), lord.getCamp(), lord.getPortrait(), lord.getRanks(), lord.getLordId(), player.getDressUp().getCurPortraitFrame()));
                }
            }
            ++index;
        }
    }

    /**
     * 获取自己的排名和榜总人数
     * 
     * @param type
     * @param myLord
     * @param scope 0.全服 1.本区域 2.本阵营
     * @return
     */
    private int[] getPlayerRank(int type, Lord myLord, int scope, int camp) {
        switch (type) {
            // case RankType.type_1:
            // return getMyRankAndAllSize(fightRankList, myLord, scope);
            // case RankType.type_2:
            // return getMyRankAndAllSize(lvRankList, myLord, scope);
            // case RankType.type_3:
            // return getMyRankAndAllSize(exploitList, myLord, scope);
            // case RankType.type_4:
            // return getMyRankAndAllSize(ranksList, myLord, scope);
            // case RankType.type_5:
            // return getMyRankAndAllSize(starsRankList, myLord, scope);
            case RankType.type_6:
                return getMyRankAndAllSize(complexRankList, myLord, scope, camp);
            case RankType.type_7:
                return getMyRankAndAllSize(agentAllLvRankList, myLord, scope, camp);
            case RankType.type_8:
                return getMyRankAndAllSize(pitchComabtType1RankList, myLord, scope, camp);
        }
        return null;
    }

    /**
     * 仅获取自己的排名
     * 
     * @param type
     * @param myLord
     * @param scope 0.全服 1.本区域 2.本阵营
     * @return
     */
    public int getMyRankByTypeAndScop(int type, Lord myLord, int scope) {
        switch (type) {
            // case RankType.type_1:
            // return getMyRank(fightRankList, myLord, scope);
            // case RankType.type_2:
            // return getMyRank(lvRankList, myLord, scope);
            // case RankType.type_3:
            // return getMyRank(exploitList, myLord, scope);
            // case RankType.type_4:
            // return getMyRank(ranksList, myLord, scope);
            // case RankType.type_5:
            // return getMyRank(starsRankList, myLord, scope);
            case RankType.type_6:
                return getMyRank(complexRankList, myLord, scope);
            case RankType.type_7:
                return getMyRank(agentAllLvRankList, myLord, scope);
            case RankType.type_8:
                return getMyRank(pitchComabtType1RankList, myLord, scope);
        }
        return 0;
    }

    /**
     * 
     * @param rankList
     * @param myLord
     * @param scope 0.全服 1.本区域 2.本阵营
     * @return index 0 自己的排名, index 1 整个榜的人数
     */
    private int[] getMyRankAndAllSize(RankList rankList, Lord myLord, int scope, int camp) {
        int[] rankAndSize = new int[2];
        int rank = 0;
        Iterator<Lord> it = rankList.getList().iterator();
        while (it.hasNext()) {
            Lord lord = (Lord) it.next();
            // if (scope > 0 && lord.getArea() != scope) {
            // continue;
            // }
            if (AREA_SCOPE == scope && myLord.getArea() != lord.getArea()) {// 过滤掉非本区域
                continue;
            }
            if (CAMP_SCOPE == scope && myLord.getCamp() != lord.getCamp()) {// 过滤掉非本阵营
                continue;
            }
            if (CUSTOM_CAMP_SCOPE == scope && camp != lord.getCamp()) {// 过滤掉非本阵营
                continue;
            }
            rank++;
            if (lord.getLordId() == myLord.getLordId()) {
                rankAndSize[0] = rank;
            }
        }
        rankAndSize[1] = rank;
        return rankAndSize;
    }

    /**
     * 
     * @param rankList
     * @param myLord
     * @param scope 0.全服 1.本区域 2.本阵营
     * @return
     */
    public int getMyRank(RankList rankList, Lord myLord, int scope) {
        int rank = 0;
        Iterator<Lord> it = rankList.getList().iterator();
        while (it.hasNext()) {
            Lord lord = (Lord) it.next();
            // if (scope > 0 && lord.getArea() != scope) {
            // continue;
            // }
            if (1 == scope && myLord.getArea() != lord.getArea()) {// 过滤掉非本区域
                continue;
            }
            if (2 == scope && myLord.getCamp() != lord.getCamp()) {// 过滤掉非本阵营
                continue;
            }
            rank++;
            if (lord.getLordId() == myLord.getLordId()) {
                return rank;
            }
        }
        return rank;
    }

    /**
     * 获取混合排行榜数据
     * 
     * @return
     */
    public List<Lord> getComplexRank() {
        return complexRankList.getList();
    }
}

class ComparatorFight implements Comparator<Lord> {

    /**
     * Overriding: compare
     * 
     * @param o1
     * @param o2
     * @return
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Lord o1, Lord o2) {
        long d1 = o1.getFight();
        long d2 = o2.getFight();

        if (d1 < d2)
            return 1;
        else if (d1 > d2) {
            return -1;
        }

        return 0;
    }
}

class ComparatorLv implements Comparator<Lord> {
    @Override
    public int compare(Lord o1, Lord o2) {
        long d1 = o1.getLevel();
        long d2 = o2.getLevel();

        if (d1 < d2)
            return 1;
        else if (d1 > d2) {
            return -1;
        }

        return 0;
    }
}

/**
 * 副本进度
 * 
 * @author tyler
 *
 */
class ComparatorCombat implements Comparator<Lord> {
    @Override
    public int compare(Lord o1, Lord o2) {
        long d1 = o1.getCombatId();
        long d2 = o2.getCombatId();

        if (d1 < d2)
            return 1;
        else if (d1 > d2) {
            return -1;
        }

        return 0;
    }
}

/**
 * 军阶
 * 
 * @author tyler
 *
 */
class ComparatorRanks implements Comparator<Lord> {
    @Override
    public int compare(Lord o1, Lord o2) {
        long d1 = o1.getRanks();
        long d2 = o2.getRanks();

        if (d1 < d2)
            return 1;
        else if (d1 > d2) {
            return -1;
        }

        return 0;
    }
}

/**
 * 军工
 * 
 * @author tyler
 *
 */
class ComparatorExploit implements Comparator<Lord> {
    @Override
    public int compare(Lord o1, Lord o2) {
        long d1 = o1.getExploit();
        long d2 = o2.getExploit();

        if (d1 < d2)
            return 1;
        else if (d1 > d2) {
            return -1;
        }

        return 0;
    }
}

/**
 * 关卡进度
 * 
 * @author tyler
 *
 */
class ComparatorStarsRank implements Comparator<Lord> {
    @Override
    public int compare(Lord o1, Lord o2) {
        long d1 = o1.getCombatId();
        long d2 = o2.getCombatId();

        if (d1 < d2)
            return 1;
        else if (d1 > d2) {
            return -1;
        }

        return 0;
    }
}

/**
 * 更新混合排行榜
 * 
 */
class ComparatorComplexRank implements Comparator<Lord> {

    @Override
    public int compare(Lord o1, Lord o2) {
        long r1 = o1.getRanks();
        long r2 = o2.getRanks();

        long f1 = o1.getFight();
        long f2 = o2.getFight();

        long lv1 = o1.getLevel();
        long lv2 = o2.getLevel();

        // (等级＞战力＞军衔)
        if (lv1 == lv2) {
            if (f1 == f2) {
                return Long.compare(r2, r1);
            } else if (f1 > f2) {
                return -1;
            } else {
                return 1;
            }
        } else if (lv1 > lv2) {
            return -1;
        } else {
            return 1;
        }


        // (军阶>战力>等级)
        // if (r1 == r2) {
        //     if (f1 == f2) {
        //         if (lv1 == lv2) {
        //             return 0;
        //         } else if (lv1 > lv2) {
        //             return -1;
        //         } else {
        //             return 1;
        //         }
        //     } else if (f1 > f2) {
        //         return -1;
        //     } else {
        //         return 1;
        //     }
        // } else if (r1 > r2) {
        //     return -1;
        // } else {
        //     return 1;
        // }
    }
}

class ComparatorPitcombatType1Rank implements Comparator<Lord> {

    @Override
    public int compare(Lord o1, Lord o2) {
        // (副本id>军阶>战力)

        long agentLv1 = o1.getPitchType1CombatId();
        long agentLv2 = o2.getPitchType1CombatId();

        long r1 = o1.getRanks();
        long r2 = o2.getRanks();

        long f1 = o1.getFight();
        long f2 = o2.getFight();

        if (agentLv1 == agentLv2) {
            if (r1 == r2) {
                if (f1 == f2) {
                    return 0;
                } else if (f1 > f2) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (r1 > r2) {
                return -1;
            } else {
                return 1;
            }
        } else if (agentLv1 > agentLv2) {
            return -1;
        } else if (agentLv1 < agentLv2) {
            return 1;
        }
        return 0;
    }
}

class ComparatorAgentAllLvRank implements Comparator<Lord> {

    @Override
    public int compare(Lord o1, Lord o2) {
        // (特工等级>军阶>战力)

        long agentLv1 = o1.getAgentAllLv();
        long agentLv2 = o2.getAgentAllLv();

        long r1 = o1.getRanks();
        long r2 = o2.getRanks();

        long f1 = o1.getFight();
        long f2 = o2.getFight();

        if (agentLv1 == agentLv2) {
            if (r1 == r2) {
                if (f1 == f2) {
                    return 0;
                } else if (f1 > f2) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (r1 > r2) {
                return -1;
            } else {
                return 1;
            }
        } else if (agentLv1 > agentLv2) {
            return -1;
        } else if (agentLv1 < agentLv2) {
            return 1;
        }
        return 0;
    }
}

class RankList {
    private LinkedList<Lord> list = new LinkedList<>();
    private int size = 0;

    public LinkedList<Lord> getList() {
        return list;
    }

    public void setList(LinkedList<Lord> list) {
        this.list = list;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void add(Lord lord) {
        list.add(lord);
        ++size;
    }

    public long removeLast() {
        Lord lord = list.removeLast();
        --size;
        return lord.getLordId();
    }
}
