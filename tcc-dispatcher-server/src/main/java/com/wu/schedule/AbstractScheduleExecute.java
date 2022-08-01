package com.wu.schedule;

import com.wu.lifecycle.Closeable;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author wuzhouwei
 * @date 2022/7/29
 */
@Slf4j
public abstract class AbstractScheduleExecute implements Closeable {
    private final ScheduledExecutorService processingExecutor;

    public AbstractScheduleExecute(String name){
        processingExecutor = Executors.newScheduledThreadPool(1,
                new NameThreadFactory(name));
        processingExecutor.scheduleWithFixedDelay(new ProcessRunnable(),5000L,5000L, TimeUnit.MILLISECONDS);
    }

    /**
     * process tasks in execute engine.
     */
    protected abstract void processTasks() throws InterruptedException;

    @Override
    public void shutdown() {
        processingExecutor.shutdown();
    }

    private class ProcessRunnable implements Runnable {

        @Override
        public void run() {
            try {
                AbstractScheduleExecute.this.processTasks();
            } catch (Throwable e) {
                log.info(e.toString(), e);
            }
        }
    }
}
