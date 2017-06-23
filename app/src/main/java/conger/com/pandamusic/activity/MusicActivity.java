package conger.com.pandamusic.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import conger.com.pandamusic.R;
import conger.com.pandamusic.application.AppCache;
import conger.com.pandamusic.constants.Extras;
import conger.com.pandamusic.executor.NaviMenuExecutor;
import conger.com.pandamusic.fragment.LocalMusicFragment;
import conger.com.pandamusic.fragment.PlayFragment;
import conger.com.pandamusic.fragment.SongListFragment;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.receiver.RemoteControlReceiver;
import conger.com.pandamusic.service.OnPlayerEventListener;
import conger.com.pandamusic.service.PlayService;
import conger.com.pandamusic.utils.CoverLoader;
import conger.com.pandamusic.utils.SystemUtils;
import conger.com.pandamusic.utils.binding.Bind;


/**
 *  程序主界面。SplashActivity跳转到此。
 */
public class MusicActivity extends BaseActivity implements View.OnClickListener, OnPlayerEventListener,
        NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.drawer_layout)
    private DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view)
    private NavigationView navigationView;
    @Bind(R.id.iv_menu)
    private ImageView ivMenu;
    @Bind(R.id.iv_search)
    private ImageView ivSearch;
    @Bind(R.id.onlineMusic)
    Button btn_onLine;
    @Bind(R.id.localMusic)
    Button btn_local;
    @Bind(R.id.downloadmanager)
    Button btn_download;
    @Bind(R.id.fl_play_bar)
    private FrameLayout flPlayBar;
    @Bind(R.id.iv_play_bar_cover)
    private ImageView ivPlayBarCover;
    @Bind(R.id.tv_play_bar_title)
    private TextView tvPlayBarTitle;
    @Bind(R.id.tv_play_bar_artist)
    private TextView tvPlayBarArtist;
    @Bind(R.id.iv_play_bar_play)
    private ImageView ivPlayBarPlay;
    @Bind(R.id.iv_play_bar_next)
    private ImageView ivPlayBarNext;
    @Bind(R.id.pb_play_bar)
    private ProgressBar mProgressBar;

    private View vNavigationHeader;


    //本地音乐Fragment
    private LocalMusicFragment mLocalMusicFragment;
    //在线音乐Fragment
    private SongListFragment mSongListFragment;
    //播放Fragment
    private PlayFragment mPlayFragment;
    private AudioManager mAudioManager;
    private ComponentName mRemoteReceiver;
    private boolean isPlayFragmentShow = false;
    private MenuItem timerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);


        if (!checkServiceAlive()) {
            return;
        }

        getPlayService().setOnPlayEventListener(this);

        setupView();
        registerReceiver();
        onChange(getPlayService().getPlayingMusic());
        parseIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    protected void setListener() {
        ivMenu.setOnClickListener(this);
        ivSearch.setOnClickListener(this);

        flPlayBar.setOnClickListener(this);
        ivPlayBarPlay.setOnClickListener(this);
        ivPlayBarNext.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);

        btn_local.setOnClickListener(this);
        btn_onLine.setOnClickListener(this);
        btn_download.setOnClickListener(this);
    }

    private void setupView() {
        // add navigation header
        vNavigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header2, navigationView, false);
        navigationView.addHeaderView(vNavigationHeader);

        mLocalMusicFragment = new LocalMusicFragment();
        mSongListFragment = new SongListFragment();

    }



    /**
     * 耳机音量控制
     */
    private void registerReceiver() {
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mRemoteReceiver = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mRemoteReceiver);
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    /**
     * 更新播放进度
     */
    @Override
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPublish(progress);
        }
    }

    /**
     *  切换歌曲
     * @param music
     */
    @Override
    public void onChange(Music music) {
        onPlay(music);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onChange(music);
        }
    }


    /**
     *  根据状态来停止和播放
     */
    @Override
    public void onPlayerPause() {
        ivPlayBarPlay.setSelected(false);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPlayerPause();
        }
    }

    /**
     *  继续播放
     */
    @Override
    public void onPlayerResume() {
        ivPlayBarPlay.setSelected(true);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPlayerResume();
        }
    }

    /**
     *  定时播放剩余时间的回调
     * @param remain
     */
    @Override
    public void onTimer(long remain) {
        if (timerItem == null) {
            timerItem = navigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        timerItem.setTitle(remain == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", remain));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_search:
                startActivity(new Intent(this, SearchMusicActivity.class));
                break;

            case R.id.fl_play_bar:
                showPlayingFragment();
                break;
            case R.id.iv_play_bar_play:
                //播放音乐
                play();
                break;
            case R.id.iv_play_bar_next:
                next();
                break;
            case R.id.onlineMusic:

                Intent intent = new Intent(this,ShowMusicActivity.class);
                intent.putExtra(ShowMusicActivity.WHICH_FRAGMETN,ShowMusicActivity.ONLINE_FRAGMENT);
                startActivity(intent);

                break;
            case R.id.localMusic:

                Intent localintent = new Intent(this,ShowMusicActivity.class);
                localintent.putExtra(ShowMusicActivity.WHICH_FRAGMETN,ShowMusicActivity.LOCAL_FRAGMENT);
                startActivity(localintent);
                break;

            case R.id.downloadmanager:
                Intent downloadIntent = new Intent(this,DownloadActivity.class);
                startActivity(downloadIntent);
                break;

        }
    }

    /**
     *  侧边栏点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        drawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
        return NaviMenuExecutor.onNavigationItemSelected(item, this);
    }




    public void onPlay(Music music) {
        if (music == null) {
            return;
        }

        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        ivPlayBarCover.setImageBitmap(cover);
        tvPlayBarTitle.setText(music.getTitle());
        tvPlayBarArtist.setText(music.getArtist());
        if (getPlayService().isPlaying() || getPlayService().isPreparing()) {
            ivPlayBarPlay.setSelected(true);
        } else {
            ivPlayBarPlay.setSelected(false);
        }
        mProgressBar.setMax((int) music.getDuration());
        mProgressBar.setProgress(0);

        if (mLocalMusicFragment != null && mLocalMusicFragment.isInitialized()) {
            mLocalMusicFragment.onItemPlay();
        }
    }

    //根据当前状态播放或者暂停播放
    private void play() {
        getPlayService().playPause();
    }

    //
    private void next() {
        getPlayService().next();
    }

    /**
     *  跳转到播放界面
     */
    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    /**
     *  隐藏播放界面
     */
    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 切换夜间模式不保存状态
    }

    @Override
    protected void onDestroy() {
        /**
         *  监听耳机按钮反注册，否则会内存泄漏
         */
        if (mRemoteReceiver != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteReceiver);
        }
        PlayService service = AppCache.getPlayService();
        if (service != null) {
            service.setOnPlayEventListener(null);
        }
        super.onDestroy();
    }
}
