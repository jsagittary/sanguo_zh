package com.gryphpoem.game.zw.core.util;

import com.gryphpoem.game.zw.core.ICommand;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.handler.DealType;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName Java8Utils.java
 * @Description
 * @author QiuKun
 * @date 2018年1月18日
 */
public class Java8Utils {

    public static <T, K1, K2> Collector<T, ?, Map<K1, Map<K2, T>>> groupByMapMap(Function<? super T, ? extends K1> key1Func, Function<? super T, ? extends K2> key2Func) {
        return Collectors.groupingBy(key1Func, Collectors.toMap(key2Func, t -> t));
    }

    public static <T, K1, K2> Collector<T, ?, Map<K1, TreeMap<K2, T>>> groupByMapTreeMap(Function<? super T, ? extends K1> key1Func, Function<? super T, ? extends K2> key2Func) {
        return Collectors.groupingBy(key1Func, Java8Utils.toTreeMap(key2Func, t -> t));
    }

    public static <T, K1, K2> Collector<T, ?, TreeMap<K1, TreeMap<K2, T>>> groupByTreeMapTreeMap(Function<? super T, ? extends K1> key1Func, Function<? super T, ? extends K2> key2Func) {
        return Collectors.groupingBy(key1Func, TreeMap::new, toTreeMap(key2Func, t -> t));
    }

    public static <T, K, U>
    Collector<T, ?, TreeMap<K, U>> toTreeMap(Function<? super T, ? extends K> keyMapper,
                                             Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new);
    }

    public static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    /**
     * List<T> 更具条件去重 使用 list.stream().filter(Java8Utils.distinctByKey(T::toString)).collect(Collectors.toList())
     *
     *
     * @param keyExtractor
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return object -> seen.putIfAbsent(keyExtractor.apply(object), Boolean.TRUE) == null;
    }

    /**
     * 分组之后进行排序
     * <p>
     * 例如 actMonopolyMap.values().stream() .collect(Collectors.groupingBy(StaticActMonopoly::getRound,
     * Java8Utils.toSortedList(Comparator.comparing(StaticActMonopoly::getGrid))));
     *
     * @param c
     * @return
     */
    public static <T> Collector<T, ?, List<T>> toSortedList(Comparator<? super T> c) {
        return Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), l -> {
            l.sort(c);
            return l;
        });
    }

    public static <T> Collector<T, ?, List<T>> toShuffledList() {
        return Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), l -> {
            Collections.shuffle(l);
            return l;
        });
    }

    public static void invokeNoExceptionICommand(ICommand command) {
        try {
            command.action();
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    /**
     * 在主线程执行某个方法
     *
     * @param command
     */
    public static void syncMethodInvoke(ICommand command) {
        Thread mainThread = Optional.ofNullable(DataResource.logicServer)
                .flatMap(ot -> Optional.ofNullable(ot.getThreadByDealType(DealType.MAIN))).orElse(null);
        if (mainThread != null) {
            if (Thread.currentThread().getId() == mainThread.getId()) {
                invokeNoExceptionICommand(command);
            } else {
                DataResource.logicServer.addCommandByMainType(command);
            }
        } else {
            invokeNoExceptionICommand(command);
        }
    }

    public static <T> CompletableFuture<T> toCompletable(Future<T> future, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.<T> toList()));
    }

    public static <T> CompletableFuture<List<T>> sequence(Stream<CompletableFuture<T>> futures) {
        List<CompletableFuture<T>> futureList = futures.filter(f -> f != null).collect(Collectors.toList());
        return sequence(futureList);
    }

    @FunctionalInterface
    public static interface SendMsgToPlayerCallback<T> {
        void sendMsgToPlayer(int command, GeneratedExtension<Base, T> ext, T msg) throws MwException;
    }

}
