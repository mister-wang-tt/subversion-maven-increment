package com.example.subversionMavenIncrement.run;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池单例模式
 *
 * @author 蚕豆的生活
 */
public enum MyThreadPoolExecutor {

    /**
     * 实例
     */
    INSTANCE;

    /**
     * 线程池
     */
    private final ThreadPoolExecutor threadPoolExecutor;

    private MyThreadPoolExecutor() {
        threadPoolExecutor = new ThreadPoolExecutor(2, 3, 2,
                TimeUnit.SECONDS,
                // 队列
                new ArrayBlockingQueue<Runnable>(5),
                // CallerRunsPolicy策略确保竞争失败的线程也能被执行
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public ThreadPoolExecutor getThreadPoolExecutor(){
        return threadPoolExecutor;
    }
}
