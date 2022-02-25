package com.gryphpoem.game.zw.task;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.gryphpoem.game.zw.core.structs.TasksQueue;

/**
 * @ClassName TaskQueuePool.java
 * @Description 任务队列池
 * @author QiuKun
 * @date 2019年4月30日
 */
public class TaskQueuePool<K, V> {
    ConcurrentHashMap<K, TasksQueue<V>> map = new ConcurrentHashMap<K, TasksQueue<V>>();

    public void registerTaskQueue(K key) {
        synchronized (map) {
            if (map.containsKey(key)) {
                return;
            }
            map.put(key, new TasksQueue<V>());
        }
    }

    public TasksQueue<V> getTasksQueue(K key) {
        return map.get(key);
    }

    public void removeTasksQueue(K key) {
        synchronized (map) {
            map.remove(key);
        }
    }

    public int getTaskCounts() {
        int count = 0;
        Iterator<Entry<K, TasksQueue<V>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            count += it.next().getValue().size();
        }
        return count;
    }

    public Set<K> getKeys() {
        return map.keySet();
    }
}
