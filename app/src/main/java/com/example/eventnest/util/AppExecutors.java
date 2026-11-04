package com.example.eventnest.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A utility class for managing and running background and main-thread tasks
 * across the application.
 * DISK: A single-thread executor dedicated to background operations such as database queries
 * MAIN: A Handler tied to the main (UI) looper for posting tasks to the main thread
 *
 * @see  <a href="https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html">Executor</a>
 * @see <a href="https://developer.android.com/reference/java/util/concurrent/Executors">Executors(android)</a>
 * @see <a href="https://developer.android.com/reference/android/os/Looper">Looper</a>
 */
public class AppExecutors {
    public AppExecutors() {
    }

    //
    private static final ExecutorService DISK = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /**
     * Runs a given task on the background disk thread
     *
     * @param dbWork The runnable task to be executed (typically database).
     */
    public static void run(Runnable dbWork) { DISK.execute(dbWork); }

    /**
     * Posts a given task to be executed on the main thread
     *
     * @param r The runnable task to be executed on the main thread (typically UI).
     */
    public static void runOnMain(Runnable r) { MAIN.post(r); }
}
