package com.gryphpoem.game.zw.core.rank;

import java.util.Comparator;

/**
 * @Description
 * @Author zhangdh
 * @Date 2020-12-21 11:12
 */
public class SimpleRankComparatorFactory {

    public static <T extends Comparable<T>> Comparator<RankItem<T>> createAscComparable() {
        return createRankComparable();
    }

    public static <T extends Comparable<T>> Comparator<RankItem<T>> createDescComparable() {
        Comparator<RankItem<T>> v = createRankComparable();
        return v.reversed();
    }

    public static <T extends Comparable<T>> Comparator<RankItem<T>> createRankComparable() {
        return (o1, o2) -> {
            int c1 = o1.getRankValue().compareTo(o2.getRankValue());
            if (c1 == 0) {
                int c2 = Long.compare(o1.getLastModifyTime(), o2.getLastModifyTime());
                if (c2 == 0) {
                    return -Long.compare(o1.getLordId(), o2.getLordId()); //lordId 越小值越大
                } else {
                    return c2 < 0 ? 1 : -1;//时间小的值更大
                }
            } else {
                return c1;
            }
        };
    }

}
