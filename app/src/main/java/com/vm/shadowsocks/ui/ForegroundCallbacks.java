package com.vm.shadowsocks.ui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * 监听前台后台应用的方法
 * @author boshuai.li
 * @date 创建时间：2018-7-10 上午10:09:37
 * @describe
 */
public class ForegroundCallbacks implements Application.ActivityLifecycleCallbacks {
    public static final long CHECK_DELAY = 500;
    public static final String TAG = ForegroundCallbacks.class.getName();

    public interface Listener {
        public void onBecameForeground();

        public void onBecameBackground();
    }

    private static ForegroundCallbacks instance;
    /**
     * 默认启动时在前台
     */
    private boolean foreground = true, paused = false;
    private static Handler handler = null;
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private Runnable check;

    /**
     * 可使用context强转Application
     * @param application
     * @return
     */
    public static ForegroundCallbacks init(Application application) {
        if (instance == null) {
            instance = new ForegroundCallbacks();
            application.registerActivityLifecycleCallbacks(instance);
            HandlerThread handlerThread = new HandlerThread("thread_foregroundcallbacks");
            handlerThread.start();

            handler = new Handler(handlerThread.getLooper());
        }
        return instance;
    }

    public static void uninit(Application application) {
        if (instance != null) {
            application.unregisterActivityLifecycleCallbacks(instance);
            instance = null;
        }
    }

    public static ForegroundCallbacks get(Application application) {
        if (instance == null) {
            init(application);
        }
        return instance;
    }

    public static ForegroundCallbacks get(Context ctx) {
        if (instance == null) {
            Context appCtx = ctx.getApplicationContext();
            if (appCtx instanceof Application) {
                init((Application) appCtx);
            }
            throw new IllegalStateException("Foreground is not initialised and " + "cannot obtain the Application object");
        }
        return instance;
    }

    public static ForegroundCallbacks get() {
        if (instance == null) {
            throw new IllegalStateException("Foreground is not initialised - invoke " + "at least once with parameterised init/get");
        }
        return instance;
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;
        if (check != null)
            handler.removeCallbacks(check);
        if (wasBackground) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    for (Listener l : listeners) {
                        try {
                            l.onBecameForeground();
                        } catch (Exception exc) {
                        }
                    }

                }
            });
        } else {
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        paused = true;
        if (check != null)
            handler.removeCallbacks(check);
        handler.postDelayed(check = new Runnable() {
            @Override
            public void run() {
                if (foreground && paused) {
                    foreground = false;
                    for (Listener l : listeners) {
                        try {
                            l.onBecameBackground();
                        } catch (Exception exc) {
                        }
                    }
                } else {
                }
            }
        }, CHECK_DELAY);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
