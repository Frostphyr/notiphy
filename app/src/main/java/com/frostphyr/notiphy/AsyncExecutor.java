package com.frostphyr.notiphy;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncExecutor {

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static <T> void execute(Task<T> task) {
        executor.execute(() -> {
            try {
                T result = task.execute();
                handler.post(() -> task.handleResult(result));
            } catch (Exception e) {
                handler.post(() -> task.handleException(e));
            }
        });
    }

    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public interface Task<T> {

        T execute() throws Exception;

        void handleResult(T result);

        void handleException(Exception exception);

    }

}
