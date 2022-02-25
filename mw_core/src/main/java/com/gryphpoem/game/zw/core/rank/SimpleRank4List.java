package com.gryphpoem.game.zw.core.rank;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-01-26 14:38
 */
public class SimpleRank4List<T extends Comparable<T>> {
    private int capacity;
    private Comparator<RankItem<T>> comparator;
    private List<RankItem<T>> rankList;

    public SimpleRank4List(int capacity, Comparator<RankItem<T>> comparator) {
        this.capacity = capacity;
        this.comparator = comparator;
        rankList = new CopyOnWriteArrayList<>();
    }

    public List<RankItem<T>> getPageList(int fromIndex, int pageSize) {
        if (fromIndex < 0 || pageSize < 0 ) {
            throw new IllegalArgumentException("toIndex less than fromIndex");
        }
        if (fromIndex >= rankList.size()) return null;
        return rankList.stream()
                .skip(fromIndex)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public synchronized void update(RankItem<T> rit) {
        rankList.add(rit);
        rankList.sort(comparator);
        while (rankList.size() > capacity) {
            rankList.remove(rankList.size() - 1);
        }
    }

}
