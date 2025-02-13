package me.wcy.music.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.amap.api.location.AMapLocalWeatherLive;

import java.util.ArrayList;
import java.util.List;

import me.wcy.music.executor.DownloadMusicInfo;
import me.wcy.music.model.Music;
import me.wcy.music.model.SheetInfo;
import me.wcy.music.utils.CoverLoader;
import me.wcy.music.storage.preference.Preferences;
import me.wcy.music.utils.ScreenUtils;
import me.wcy.music.utils.ToastUtils;

/**
 * Created by hzwangchenyan on 2016/11/23.
 */
public class AppCache {
    private Context mContext;
    private final List<Music> mLocalMusicList = new ArrayList<>();      // 本地音乐列表
    private final List<SheetInfo> mSheetList = new ArrayList<>();       // 在线歌单列表
    private final List<Activity> mActivityStack = new ArrayList<>();    // 活动栈
    private final LongSparseArray<DownloadMusicInfo> mDownloadList = new LongSparseArray<>(); // 下载音乐信息列表
    private AMapLocalWeatherLive mAMapLocalWeatherLive;                 // 天气预报

    private AppCache() {
    }

    private static class SingletonHolder {
        private static AppCache instance = new AppCache();
    }

    public static AppCache get() {
        return SingletonHolder.instance;
    }

    public void init(Application application) {
        mContext = application.getApplicationContext();
        ToastUtils.init(mContext);
        Preferences.init(mContext);
        ScreenUtils.init(mContext);
        CrashHandler.getInstance().init();
        CoverLoader.get().init(mContext);
        application.registerActivityLifecycleCallbacks(new ActivityLifecycle());
    }

    public Context getContext() {
        return mContext;
    }

    public List<Music> getLocalMusicList() {
        return mLocalMusicList;
    }

    public List<SheetInfo> getSheetList() {
        return mSheetList;
    }

    // 清空活动栈
    public void clearStack() {
        List<Activity> activityStack = mActivityStack;
        for (int i = activityStack.size() - 1; i >= 0; i--) {
            Activity activity = activityStack.get(i);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activityStack.clear();
    }

    public LongSparseArray<DownloadMusicInfo> getDownloadList() {
        return mDownloadList;
    }

    // 返回天气预报
    public AMapLocalWeatherLive getAMapLocalWeatherLive() {
        return mAMapLocalWeatherLive;
    }

    // 设置天气预报
    public void setAMapLocalWeatherLive(AMapLocalWeatherLive aMapLocalWeatherLive) {
        mAMapLocalWeatherLive = aMapLocalWeatherLive;
    }

    // 活动生命周期
    private class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private static final String TAG = "Activity";

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, "onCreate: " + activity.getClass().getSimpleName());
            mActivityStack.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, "onDestroy: " + activity.getClass().getSimpleName());
            mActivityStack.remove(activity);
        }
    }
}
