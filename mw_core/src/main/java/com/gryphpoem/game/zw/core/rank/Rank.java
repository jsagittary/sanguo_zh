package com.gryphpoem.game.zw.core.rank;


import java.io.*;
import java.util.*;


public class Rank<K, V> implements Map<K, V> {

    static class RankImp<K, V> extends AbstractMap<K, V> implements Serializable {
        private static final long serialVersionUID = -7617145893493505102L;

        private final Entry<K, V> rank[];
        private transient HashMap<K, Entry<K, V>> keys;
        private transient Comparator<Entry<K, V>> compartor;
        private int size;
        private transient int modCount;
        private transient Set<Entry<K, V>> entrySet = null;

        private void readObject(ObjectInputStream s) throws IOException,
                ClassNotFoundException {
            s.defaultReadObject();
            keys = new HashMap<K, Entry<K, V>>();
            for (int i = 0; i < size; i++) {
                Entry<K, V> r = rank[i];
                keys.put(r.getKey(), r);
            }
        }

        public RankImp(byte[] data, Comparator<V> compparator) throws IOException, ClassNotFoundException {
            ObjectInputStream ois = new ObjectInputStream(
                    new ByteArrayInputStream(data));
            @SuppressWarnings("unchecked")
            RankImp<K, V> obj = (RankImp<K, V>) ois.readObject();
            ois.close();
            this.rank = obj.rank;
            this.keys = obj.keys;
            this.compartor = (e1, e2) -> compparator.compare(e1.getValue(), e2.getValue());
            this.size = obj.size;
        }

        @SuppressWarnings("unchecked")
        public RankImp(int capacity, Comparator<V> compparator) {
            this.rank = new Entry[capacity];
            this.keys = new HashMap<>();
            this.compartor = (e1, e2) -> compparator.compare(e1.getValue(), e2.getValue());
            this.size = 0;
        }

        private int search(Entry<K, V> r) {
            int kp = Arrays.binarySearch(rank, 0, size, r, compartor);
            int op = kp;
            int dir = 1;
            while (r != rank[kp]) {
                kp += dir;
                if (kp == size || compartor.compare(rank[kp], r) != 0) {
                    kp = op - 1;
                    dir = -1;
                }
            }
            return kp;
        }

        private int updateAndReturnRank(K key, V value) {
            if (value == null) {
                if (containsKey(key)) {
                    remove(key);
                    return 0;
                } else {
                    return -1;
                }
            }
            Entry<K, V> old = keys.get(key);
            return _update(old, key, value);
        }

        private int _update(Entry<K, V> old, K key, V value) {
            Entry<K, V> r = new SimpleEntry<>(key, value);
            if (old == null) {
                if (size < rank.length || compartor.compare(rank[size - 1], r) > 0) {
                    modCount++;
                    int rp = Arrays.binarySearch(rank, 0, size, r, compartor);
                    if (rp < 0)
                        rp = -(rp + 1);
                    else
                        while (rp < size && compartor.compare(rank[rp], r) == 0)
                            rp++;
                    if (size < rank.length)
                        ++size;
                    else
                        keys.remove(rank[size - 1].getKey());
                    System.arraycopy(rank, rp, rank, rp + 1, size - rp - 1);
                    keys.put(key, rank[rp] = r);
                    return rp;
                }
            } else {
                if (compartor.compare(old, r) != 0) {
                    modCount++;
                    int op = search(old);
                    int rp = Arrays.binarySearch(rank, 0, size, r, compartor);
                    if (rp < 0) {
                        rp = -(rp + 1);
                        if (op < rp) {
                            rp--;
                            System.arraycopy(rank, op + 1, rank, op, rp - op);
                        } else {
                            System.arraycopy(rank, rp, rank, rp + 1, op - rp);
                        }
                    } else {
                        if (op < rp) {
                            while (compartor.compare(rank[--rp], r) == 0)
                                ;
                            System.arraycopy(rank, op + 1, rank, op, rp - op);
                        } else {
                            while (compartor.compare(rank[++rp], r) == 0)
                                ;
                            System.arraycopy(rank, rp, rank, rp + 1, op - rp);
                        }
                    }
                    if (rp < rank.length - 1 || rp < size - 1
                            || (rp == op && compartor.compare(rank[rp], r) > 0)) {
                        keys.put(key, rank[rp] = r);
                    } else {
                        size--;
                        keys.remove(key);
                    }
                    return rp;
                }
            }
            return -1;
        }

        private V update(K key, V value) {
            if (value == null) {
                return remove(key);
            }
            Entry<K, V> old = keys.get(key);
            _update(old, key, value);
            return old == null ? null : old.getValue();
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(this);
            oos.close();
            return os.toByteArray();
        }

        public V put(K key, V value) {
            return update(key, value);
        }

        public V get(Object key) {
            Entry<K, V> r = keys.get(key);
            return r == null ? null : r.getValue();
        }

        public V remove(Object key) {
            Entry<K, V> old = keys.remove(key);
            if (old == null)
                return null;
            modCount++;
            int op = search(old);
            Entry<K, V> r = rank[op];
            System.arraycopy(rank, op + 1, rank, op, --size - op);
            return r.getValue();
        }

        public int size() {
            return size;
        }

        public Set<Entry<K, V>> entrySet() {
            return entrySet != null ? entrySet
                    : (entrySet = new AbstractSet<Entry<K, V>>() {
                public Iterator<Entry<K, V>> iterator() {
                    return new Iterator<Entry<K, V>>() {
                        private int cursor = 0;
                        private int last = -1;
                        private int expectedModCount = modCount;

                        public boolean hasNext() {
                            return cursor < size;
                        }

                        public Entry<K, V> next() {
                            if (modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            return rank[last = cursor++];
                        }

                        public void remove() {
                            if (last == -1)
                                throw new IllegalStateException();
                            if (modCount != expectedModCount)
                                throw new ConcurrentModificationException();
                            try {
                                keys.remove(rank[last].getKey());
                                System.arraycopy(rank, last + 1, rank,
                                        last, --size - last);
                                cursor = last;
                                last = -1;
                            } catch (IndexOutOfBoundsException e) {
                                throw new ConcurrentModificationException();
                            }
                        }
                    };
                }

                public int size() {
                    return size;
                }
            });
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public boolean containsKey(Object key) {
            return keys.containsKey(key);
        }

        public boolean containsValue(Object value) {
            for (Entry<K, V> e : rank)
                if (value.equals(e.getValue()))
                    return true;
            return false;
        }

        public void clear() {
            keys.clear();
            size = 0;
        }

        public int keySize() {
            return keys.size();
        }

        public void forEach(RankConsumer<K, V> consumer, int minrank, int maxrank) {
            maxrank = Math.min(size, maxrank);

            for (int rank = Math.max(1, minrank); rank <= maxrank; rank++) {
                Entry<K, V> e = this.rank[rank - 1];
                consumer.accept(rank, e.getKey(), e.getValue());
            }
        }

        public K rankGetK(int rank) {
            int index = rank - 1;
            if (index >= 0 && index < size) {
                return this.rank[index].getKey();
            }
            return null;
        }

        public V rankGetV(int rank) {
            int index = rank - 1;
            if (index >= 0 && index < size) {
                return this.rank[index].getValue();
            }
            return null;
        }

        public int getRank(K k) {
            Entry<K, V> old = keys.get(k);
            if (old == null) {
                return -1;
            }
            int op = search(old);
            return op + 1;
        }
    }

    private final RankImp<K, V> imp;
    private final Map<K, V> map;

    public Rank(byte[] data, Comparator<V> c) throws IOException, ClassNotFoundException {
        map = Collections.synchronizedMap(imp = new RankImp<K, V>(data, c));
    }

    public Rank(int capacity, Comparator<V> c) {
        map = Collections.synchronizedMap(imp = new RankImp<K, V>(capacity, c));
    }

    public V update(K key, V value) {
        synchronized (map) {
            return imp.update(key, value);
        }
    }

    public int updateAndReturnRank(K key, V value) {
        synchronized (map) {
            return imp.updateAndReturnRank(key, value);
        }
    }

    public byte[] toByteArray() throws IOException {
        synchronized (map) {
            return imp.toByteArray();
        }
    }

    public static interface RankConsumer<K, V> {
        void accept(int rank, K k, V v);
    }

    public void forEach(RankConsumer<K, V> consumer, int minrank, int maxrank) {
        synchronized (map) {
            imp.forEach(consumer, minrank, maxrank);
        }
    }

    public void forEach(RankConsumer<K, V> consumer) {
        synchronized (map) {
            imp.forEach(consumer, 1, map.size());
        }
    }

    public K rankGetK(int rank) {
        synchronized (map) {
            return imp.rankGetK(rank);
        }
    }

    public V rankGetV(int rank) {
        synchronized (map) {
            return imp.rankGetV(rank);
        }
    }

    public int getRank(K k) {
        synchronized (map) {
            return imp.getRank(k);
        }
    }

    public Object synchronizer() {
        return map;
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public V get(Object key) {
        return map.get(key);
    }

    public V put(K key, V value) {
        return map.put(key, value);
    }

    public V remove(Object key) {
        return map.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    public void clear() {
        map.clear();
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Collection<V> values() {
        return map.values();
    }

    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
