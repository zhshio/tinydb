package com.zhshio.tinydb.backend.common;/**
 * @Auther: 张帅
 * @Date: 2023/12/11 - 12 - 11 - 8:35
 * @Description: com.zhshio.tinydb.backend.common
 * @version: 1.0
 */

import com.zhshio.tinydb.commob.Error;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @description:
 * @author: zs
 * @time: 2023/12/11 8:35
 */

// 抽象缓存框架
public abstract class AbstractCache<T> {


    // 实际缓存的数据
    private HashMap<Long, T> cache;
    // 资源引用个数
    private HashMap<Long, Integer> references;
    // 正在被获取的资源(多线程环境下的安全考虑)
    private HashMap<Long, Boolean> getting;
    // 缓存的最大缓存资源数
    private int maxResource;
    // 缓存中元素的个数
    private int count;
    // 缓存锁
    private Lock lock;

    public AbstractCache(int maxResource) {
        this.maxResource = maxResource;
        cache = new HashMap<>();
        references = new HashMap<>();
        getting = new HashMap<>();
        lock = new ReentrantLock();
    }

    // 资源不在缓存时的获取行为
    protected abstract T getForCache(long key) throws Exception;


    // 资源从缓存中移除的驱逐行为
    protected abstract void releaseForCache(T obj);




    protected T get(long key) throws Exception{
        while (true) {
            lock.lock();
            if (getting.containsKey(key)) {
                lock.unlock();
                try {
                    Thread.sleep(1);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            if (cache.containsKey(key)) {
                // 资源在缓存中, 直接返回
                T obj = cache.get(key);
                references.put(key, references.get(key) + 1);
                lock.unlock();
                return obj;
            }

            // 尝试换取该资源
            if (maxResource > 0 && count == maxResource) {
                lock.unlock();
                throw Error.CacheFullException;
            }
            count ++;
            getting.put(key, true);
            lock.unlock();
            break;
        }

        T obj = null;
        try {
            obj = getForCache(key);
        } catch(Exception e) {
            lock.unlock();
            count --;
            getting.remove(key);
            lock.unlock();
            throw e;
        }

        lock.lock();
        getting.remove(key);
        cache.put(key, obj);
        references.put(key, 1);
        lock.unlock();

        return obj;
    }

    // 关闭缓存, 写会所有资源
    protected void close() {
        lock.lock();
        try {
            Set<Long> keys = cache.keySet();
            for (long key : keys) {
                T obj = cache.get(key);
                releaseForCache(obj);
                references.remove(key);
                cache.remove(key);
            }
        }finally {
            lock.unlock();
        }
    }
}
