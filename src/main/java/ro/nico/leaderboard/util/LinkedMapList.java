package ro.nico.leaderboard.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class LinkedMapList<K, V> {
    private final LinkedList<K> linkedList;
    private final LinkedHashMap<K, V> map;

    public LinkedMapList() {
        this.linkedList = new LinkedList<>();
        this.map = new LinkedHashMap<>();
    }

    public void put(K key, V value) {
        linkedList.add(key);
        map.put(key, value);
    }

    public V get(K key) {
        return map.get(key);
    }

    public void putAll(LinkedMapList<K, V> other) {
        linkedList.addAll(other.linkedList);
        map.putAll(other.map);
    }

    public Map<K, V> asMap() {
        return map;
    }

    public void putAll(Map<K, V> other) {
        for (Map.Entry<K, V> entry : other.entrySet()) { // mainting order
            linkedList.add(entry.getKey());
            map.put(entry.getKey(), entry.getValue());
        }
    }

    public V get(int index) {
        return map.get(linkedList.get(index));
    }

    public void remove(int index) {
        map.remove(linkedList.remove(index));
    }

    public void remove(K key) {
        linkedList.remove(key);
        map.remove(key);
    }

    public K getFirstKey() {
        return linkedList.getFirst();
    }

    public K getLastKey() {
        return linkedList.getLast();
    }

    public int size() {
        return linkedList.size();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return map.containsValue(value);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        linkedList.clear();
        map.clear();
    }

    public int indexOf(K key) {
        return linkedList.indexOf(key);
    }
}
