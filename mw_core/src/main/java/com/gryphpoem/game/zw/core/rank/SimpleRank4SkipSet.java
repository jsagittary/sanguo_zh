package com.gryphpoem.game.zw.core.rank;

import com.gryphpoem.push.util.CheckNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author zhangdh
 * @Date 2020-12-21 16:15
 */
public class SimpleRank4SkipSet<T extends Comparable<T>> {
    private int capacity;//总排名长度
    private ConcurrentSkipListSet<RankItem<T>> sortSet;
    private Map<Long, RankItem<T>> lordMap = new ConcurrentHashMap<>();
    private long version;//客户端获取列表前先检查下版本时候已经发生变化, 如果版本号没有发生变化则不用更新排名

    public SimpleRank4SkipSet(int capacity, Comparator<RankItem<T>> comparator) {
        this.capacity = Math.min(capacity, 10000);
        sortSet = new ConcurrentSkipListSet<>(comparator);
    }

    /**
     * @param fromIndex 包含
     * @param toIndex   不包含
     * @return 排名列表
     */
    public List<RankItem<T>> getRankList(int fromIndex, int toIndex) {
        if (fromIndex >= toIndex || fromIndex < 0) throw new IllegalArgumentException("排名列表参数错误");
        int curIdx = 0;
        List<RankItem<T>> rankList = new ArrayList<>(toIndex - fromIndex);
        for (RankItem<T> rit : sortSet) {
            if (curIdx < fromIndex) {
                curIdx++;
                continue;
            }
            if (curIdx >= toIndex) break;
            rankList.add(rit);
            curIdx++;
        }
        return rankList;
    }

    public int getRank(long lordId) {
        RankItem<T> ri = lordMap.get(lordId);
        if (ri == null) return -1;
        NavigableSet<RankItem<T>> headSet = sortSet.headSet(ri, true);
        return headSet.size();
    }

    public int getRank(RankItem<T> ri) {
        NavigableSet<RankItem<T>> tailSet = sortSet.tailSet(ri);
        return tailSet.size();
    }

    /**
     * 根据排名获取排名项
     * @param rank
     * @return
     */
    public RankItem<T> getRankItem(int rank) {
        if (rank > sortSet.size()) return null;
        int idx = 1;
        for (RankItem<T> rit : sortSet) {
            if (idx == rank) return rit;
            idx++;
        }
        return null;
    }

    public ConcurrentSkipListSet<RankItem<T>> getAll() {
        return sortSet.clone();
    }

    public int size() {
        return sortSet.size();
    }

    /**
     * 更新排名信息,有则更新, 没有则插入
     * 此处没做性能优化
     *
     * @param item 需要更新的排名对象
     */
    public synchronized void update(RankItem<T> item) {
        RankItem<T> old = lordMap.get(item.getLordId());
        if (old != null) sortSet.remove(old);
        sortSet.add(item);
        lordMap.put(item.getLordId(), item);
        while (sortSet.size() > capacity) {
            RankItem<T> lst = sortSet.pollLast();
            if (lst != null) {
                lordMap.remove(lst.getLordId());
            }
        }
        version++;
    }

    public synchronized void tickRankOut(long roleId) {
        RankItem<T> old = lordMap.remove(roleId);
        if (CheckNull.isNull(old)) return;
        sortSet.remove(old);
    }

    public void clear(){
        sortSet.clear();
        lordMap.clear();
    }

    public void print() {
        String string = sortSet.stream().map(this::getItemString).collect(Collectors.joining(" \r\n"));
        System.out.println(string);
        System.out.println("--------------------");
    }

    public Map<Long, RankItem<T>> getLordMap() {
        return lordMap;
    }

    public String getItemString(RankItem<T> item) {
        return item.getRankValue() + " --> " + item.getLordId();
    }

    public void forEach(Consumer<RankItem> consumer) {
        for (RankItem<T> rit : sortSet) {
            consumer.accept(rit);
        }
    }

    public RankItem<T> getRankItem(long roleId) {
        return lordMap.get(roleId);
    }

    public long getVersion() {
        return version;
    }
}
