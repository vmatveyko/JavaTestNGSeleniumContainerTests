package ru.vmatveyko.common;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import lombok.Getter;

public class ResourcePool<T> {

    @Getter
    public final BlockingQueue<T> pool;

    // Helper interface for resource creation
    @FunctionalInterface
    public interface ResourceFactory<T> {
        T create();
    }

    // Helper interface to clear resource pool
    @FunctionalInterface
    public interface ClearFactory<T> {
        void clear();
    }

    /**
     * Initialize the pool with a fixed number of resources
     *
     * @param poolSize
     * @param factory lambda to fill pool
     */
    public ResourcePool(int poolSize, ResourceFactory<T> factory) {
        this.pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            this.pool.add(factory.create());
        }
    }

    /**
     * Borrow a resource (Blocks if none are available)
     *
     * @return pool element
     * @throws InterruptedException
     */
    public T acquire() throws InterruptedException {
        return pool.take();
    }

    /**
     * Return the resource back to the pool
     *
     * @param resource to be released
     */
    public void release(T resource) {
        if (resource != null) {
            pool.offer(resource);
        }
    }

    /**
     * @param factory labda to clear pool resources
     */
    public void removeResources(ClearFactory<T> factory) {
        factory.clear();
        this.pool.clear();
    }
}
