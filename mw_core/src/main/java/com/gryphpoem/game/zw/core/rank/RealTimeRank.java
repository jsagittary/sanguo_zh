package com.gryphpoem.game.zw.core.rank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RealTimeRank extends SimpleRank4SkipSet {

    private Rank<Long, RankItem> rankImp;

    private Comparator comparator;

    public RealTimeRank(int capacity, Comparator comparator) {
        super(capacity, comparator);
        this.comparator = comparator;
        this.rankImp = new Rank<>(capacity, comparator);
    }

    /**
     * @param fromIndex 包含
     * @param toIndex   不包含
     * @return 排名列表
     */
    @Override
    public List<RankItem> getRankList(int fromIndex, int toIndex) {
        List<RankItem> rankItems = new ArrayList<>();
        this.rankImp.forEach((rank, k, v) -> {
            rankItems.add(v);
        }, fromIndex + 1, toIndex);
        return rankItems;
    }

    @Override
    public void forEach(Consumer consumer) {
        this.rankImp.forEach((rank, k, v) -> {
            consumer.accept(v);
        });
    }

    @Override
    public int getRank(long lordId) {
        return this.rankImp.getRank(lordId);
    }

    @Override
    public RankItem getRankItem(long roleId) {
        return this.rankImp.get(roleId);
    }

    @Override
    public int getRank(RankItem ri) {
        return this.rankImp.getRank(ri.getLordId());
    }

    @Override
    public RankItem getRankItem(int rank) {
        return this.rankImp.rankGetV(rank);
    }

    @Override
    public ConcurrentSkipListSet<RankItem> getAll() {
        ConcurrentSkipListSet s = new ConcurrentSkipListSet(this.comparator);
        s.addAll(this.rankImp.values());
        return s;
    }

    @Override
    public int size() {
        return this.rankImp.size();
    }

    @Override
    public synchronized void update(RankItem item) {
        this.rankImp.update(item.getLordId(), item);
    }

    @Override
    public synchronized void tickRankOut(long roleId) {
        this.rankImp.remove(roleId);
    }

    @Override
    public void clear() {
        this.rankImp.clear();
    }

    @Override
    public void print() {
        String string = this.rankImp.values().stream().map(this::getItemString).collect(Collectors.joining(" \r\n"));
        System.out.println(string);
        System.out.println("--------------------");
    }

    @Override
    public Map<Long, RankItem> getLordMap() {
        return this.rankImp;
    }

    @Override
    public String getItemString(RankItem item) {
        return super.getItemString(item);
    }

    @Override
    public long getVersion() {
        return 0;
    }
}
