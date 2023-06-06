package cn.disy920.slbot.utils.container;

import java.util.Objects;

public class Pair<K, V> implements Cloneable {
    private final K key;
    private final V value;

    public Pair(K key, V value){
        this.key = key;
        this.value = value;
    }

    public K getFirst(){
        return key;
    }

    public V getSecond(){
        return value;
    }

    public boolean equals(Object anotherObj) {
        if (anotherObj == this) {
            return true;
        }
        else {
            return (anotherObj instanceof Pair<?, ?> anotherPair) && anotherPair.key.equals(this.key) && anotherPair.value.equals(this.value);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Pair<K, V> clone() {
        Pair<K, V> pair = null;

        try {
            pair = (Pair<K, V>) super.clone();
        } catch (CloneNotSupportedException ignored) {}

        return pair;
    }
}
