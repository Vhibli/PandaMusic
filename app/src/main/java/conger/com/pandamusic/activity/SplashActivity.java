package conger.com.pandamusic.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.logger.Logger;

import java.util.Calendar;


import conger.com.pandamusic.R;
import conger.com.pandamusic.application.AppCache;

import conger.com.pandamusic.model.Splash;
import conger.com.pandamusic.net.RetrofitHelper;
import conger.com.pandamusic.service.PlayService;
import conger.com.pandamusic.utils.ToastUtils;
import conger.com.pandamusic.utils.binding.Bind;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *  启动界面。
 */
@RuntimePermissions
public class SplashActivity extends BaseActivity {
    private static final String SPLASH_FILE_NAME = "splash";

    @Bind(R.id.iv_splash)
    private ImageView ivSplash;
    @Bind(R.id.tv_copyright)
    private TextView tvCopyright;
    private ServiceConnection mPlayServiceConnection;
    private PlayService playService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        checkService();
    }

    /**
     *  如果当前播放的Service
     */
    private void checkService() {
        if (AppCache.getPlayService() == null) {
            showSplash();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindService();
                }
            }, 1000);
        } else {
            startMusicActivity();
            finish();
        }
    }


    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void needPermisson() {

        //更新本地音乐列表
        playService.updateMusicList();
        startMusicActivity();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SplashActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showPermissonMessage(final PermissionRequest request) {
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showPermissonDeniedMessage() {

        ToastUtils.show("没有权限扫描本地歌曲");
        finish();
        playService.stop();
    }


    private class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playService = ((PlayService.PlayBinder) service).getService();
            AppCache.setPlayService(playService);
            SplashActivityPermissionsDispatcher.needPermissonWithCheck(SplashActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }


    private void showSplash(){


        RetrofitHelper.getRetrofitHelper().fetchSplashImage().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Splash>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        Logger.d(e);
                    }

                    @Override
                    public void onNext(Splash splash) {


                        Logger.d(splash.getUrl());

                        ImageLoader.getInstance().displayImage(splash.getUrl(),ivSplash);
                    }
                });

    }




    private void startMusicActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MusicActivity.class);
        intent.putExtras(getIntent());
        /**
         *  singleTop
         */
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        if (mPlayServiceConnection != null) {
            unbindService(mPlayServiceConnection);
        }
        super.onDestroy();
    }
}
