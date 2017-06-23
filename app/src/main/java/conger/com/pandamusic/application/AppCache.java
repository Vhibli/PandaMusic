package conger.com.pandamusic.application;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.util.LongSparseArray;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;

import conger.com.pandamusic.activity.BaseActivity;
import conger.com.pandamusic.model.AlbumInfo;
import conger.com.pandamusic.model.LocalArtistInfo;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.model.SongListInfo;
import conger.com.pandamusic.service.PlayService;
import conger.com.pandamusic.utils.MusicUtils;
import conger.com.pandamusic.utils.Preferences;
import conger.com.pandamusic.utils.ScreenUtils;
import conger.com.pandamusic.utils.ToastUtils;


/**
 *   Application进来就工作，主要是管理 播放Service,添加Activty到list,删除Activity
 */
public class AppCache {
    private Context mContext;
    private PlayService mPlayService;
    // 本地歌曲列表
    private final List<Music> mMusicList = new ArrayList<>();
    //专辑列表
    private  List<AlbumInfo> mAlbumInfoList = new ArrayList<>();
    // 歌单列表
    private final List<SongListInfo> mSongListInfos = new ArrayList<>();
    //歌手列表
    private List<LocalArtistInfo> mLocalArtistInfos = new ArrayList<>();

    private final List<BaseActivity> mActivityStack = new ArrayList<>();
    private final LongSparseArray<String> mDownloadList = new LongSparseArray<>();


    private AppCache() {
    }

    private static class SingletonHolder {
        private static AppCache sAppCache = new AppCache();
    }

    public static AppCache getInstance() {
        return SingletonHolder.sAppCache;
    }

    public static void init(Context context) {
        getInstance().onInit(context);
    }

    private void onInit(Context context) {
        mContext = context.getApplicationContext();
        //初始化工具类
        ToastUtils.init(mContext);
        Preferences.init(mContext);
        ScreenUtils.init(mContext);
        startService(mContext);


    }



    public void updateArtistList() {
        mLocalArtistInfos = MusicUtils.queryArtist(mContext);

    }

    public void updateAlbumList() {

        mAlbumInfoList = MusicUtils.queryAlbums(mContext);

    }

    public static Context getContext() {
        return getInstance().mContext;
    }

    public static PlayService getPlayService() {
        return getInstance().mPlayService;
    }

    public static void setPlayService(PlayService service) {
        getInstance().mPlayService = service;
    }

    public static void updateNightMode(boolean on) {


        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
        config.uiMode |= on ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        resources.updateConfiguration(config, dm);
    }

    /**
     *  获取本地音乐
     * @return
     */
    public static List<Music> getMusicList() {
        return getInstance().mMusicList;
    }

    public static List<AlbumInfo> getAlbumList(){

        return getInstance().mAlbumInfoList;

    }


    public static List<LocalArtistInfo> getLocalArtistInfo(){

        return getInstance().mLocalArtistInfos;

    }


    public static List<SongListInfo> getSongListInfos() {
        return getInstance().mSongListInfos;
    }

    public static void addToStack(BaseActivity activity) {
        getInstance().mActivityStack.add(activity);
    }

    public static void removeFromStack(BaseActivity activity) {
        getInstance().mActivityStack.remove(activity);
    }

    /**
     *  清除list中所有activity
     */
    public static void clearStack() {
        List<BaseActivity> activityStack = getInstance().mActivityStack;
        for (int i = activityStack.size() - 1; i >= 0; i--) {
            BaseActivity activity = activityStack.get(i);
            activityStack.remove(activity);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    /**
     * 下载的歌曲 //TODO
     * @return
     */
    public static LongSparseArray<String> getDownloadList() {
        return getInstance().mDownloadList;
    }



    /**
     * 开启音乐播放器
     * @param context
     */
    private void startService(Context context) {
        Intent intent = new Intent(context, PlayService.class);
        context.startService(intent);
    }
}
