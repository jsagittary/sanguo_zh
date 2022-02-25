
package com.gryphpoem.game.zw.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.util.RankLinkedList.AbsRankItem;

/**
 * @ClassName RankLinkedList.java
 * @Description 排行榜容器
 * @author QiuKun
 * @date 2019年5月29日
 */
public class RankLinkedList<E extends AbsRankItem> {

    private final Comparator<E> comparator;
    private final int capacity;
    private final LinkedList<E> container;

    public RankLinkedList(int capacity, Comparator<E> comparator) {
        this.capacity = capacity;
        this.comparator = comparator;
        this.container = new LinkedList<>();
    }

    // 默认按照 val的倒序, 时间正序进行排列
    public RankLinkedList(int capacity) {
        this(capacity, Comparator.comparing(E::getVal).reversed().thenComparing(E::getmTime));
    }

    /**
     * 用于反序列化使用
     * 
     * @param c
     */
    public void addAll(Collection<E> c) {
        container.addAll(c);
        Collections.sort(container, comparator); // 排序
        // 检测是否超出长度
        while (container.size() > capacity) {
            container.removeLast();
        }
    }

    /**
     * 添加排行榜的item
     * 
     * @param p 用于筛选相等的元素
     * @param s 如果筛选出的元素不存在,就进行创建
     * @param val 旧元素存在需要
     * @return
     */
    public boolean addRankItem(Predicate<E> p, Supplier<E> s, int val) {
        E oldE = container.stream().filter(p).findFirst().orElse(null);
        int now = TimeHelper.getCurrentSecond();
        if (oldE == null) {// 没有才会添加
            E newE = s.get();
            newE.mTime = now;
            newE.setVal(val);
            container.add(newE);
        } else {// 否则只会对原来的值进行操作
            oldE.val += val;
            oldE.mTime = now;
        }
        Collections.sort(container, comparator);
        if (container.size() > capacity) {
            container.removeLast();
        }
        return true;
    }

    public int size() {
        return container.size();
    }

    /**
     * 获取item的排名
     * 
     * @param p
     * @return -1 说明不在榜中
     */
    public int getItemRanking(Predicate<E> p) {
        int ranking = 0;
        E findE = null;
        for (E e : container) {
            ranking++;
            if (p.test(e)) {
                findE = e;
                break;
            }
        }
        return findE == null ? -1 : ranking;
    }

    public boolean remove(E e) {
        return container.remove(e);
    }

    public List<E> getRankList() {
        return container.stream().collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<E> getRankListLimit(int offset, int limit) {
        if (limit < 0) {
            return Collections.EMPTY_LIST;
        }
        return container.stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return container.isEmpty();
    }

    public static abstract class AbsRankItem {
        protected int val;// 变动的值
        protected int mTime;// 修改时间

        public void setmTime(int mTime) {
            this.mTime = mTime;
        }

        public int getmTime() {
            return mTime;
        }

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }
    }

    public void clear() {
        container.clear();
    }

}
