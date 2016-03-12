package com.shazam.fork.model;

import com.google.common.base.Objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generic class to associate an object to a count.
 *
 * @param <T>
 */
public class Counter<T> {

    private T type;
    private AtomicInteger count;

    public Counter(T type, int initialCount) {
        this.type = type;
        this.count = new AtomicInteger(initialCount);
    }

    public int increaseCount() {
        return count.incrementAndGet();
    }

    public T getType() {
        return type;
    }

    public int getCount() {
        return count.get();
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Counter other = (Counter) obj;
        return Objects.equal(this.type, other.type);
    }
}
