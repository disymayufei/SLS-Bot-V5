package cn.disy920.slbot.utils.container;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个缓存类，用于阻塞进程，直到缓存内容被正确设定
 * 缓存区只能存放一个实例，以避免发生冲突
 * @param <T> 待缓存类的类型
 */
public class Cache<T> {
    private T cache = null;

    private int counter = 0;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition needWait = lock.newCondition();

    /**
     * 阻塞式获取缓存区内的元素
     * @return 缓冲区内的元素
     */
    @Nullable
    public T getCache() {
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
        }
        catch (InterruptedException e) {
            return null;
        }

        try {
            while (counter == 0) {
                needWait.await();
            }

            counter--;

            return this.cache;
        }
        catch (InterruptedException e) {
            return null;
        }
        finally {
            lock.unlock();
        }
    }

    @Nullable
    public T getCache(long milliTimeout) {
        final ReentrantLock lock = this.lock;
        try {
            lock.lockInterruptibly();
        }
        catch (InterruptedException e) {
            return null;
        }

        long nanos = TimeUnit.MILLISECONDS.toNanos(milliTimeout);

        try {
            while (counter == 0) {
                if (nanos <= 0L)
                    return null;
                nanos = needWait.awaitNanos(nanos);
            }

            counter--;

            return this.cache;
        }
        catch (InterruptedException e) {
            return null;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * 将特定元素放入缓存区
     * @param cache 待放入的元素
     * @return 是否放置成功，当缓存区已满时则会失败
     */
    public boolean putCache(T cache) {
        if (counter == 0) {
            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();
                this.cache = cache;
                counter++;
                needWait.signal();
                return true;
            }
            catch (InterruptedException e) {
                return false;
            }
            finally {
                lock.unlock();
            }

        }
        else {
            return false;
        }
    }

    /**
     * 获取缓存区的空间情况
     * @return 是否为空的缓存区
     */
    public boolean isEmpty() {
        return counter == 0;
    }
}
