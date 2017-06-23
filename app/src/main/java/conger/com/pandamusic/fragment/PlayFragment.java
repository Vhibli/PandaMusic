package conger.com.pandamusic.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import conger.com.pandamusic.R;
import conger.com.pandamusic.adapter.PlayPagerAdapter;
import conger.com.pandamusic.application.AppCache;
import conger.com.pandamusic.constants.Actions;
import conger.com.pandamusic.enums.PlayModeEnum;
import conger.com.pandamusic.executor.SearchLrc;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.utils.CoverLoader;
import conger.com.pandamusic.utils.FileUtils;
import conger.com.pandamusic.utils.Preferences;
import conger.com.pandamusic.utils.ScreenUtils;
import conger.com.pandamusic.utils.SystemUtils;
import conger.com.pandamusic.utils.ToastUtils;
import conger.com.pandamusic.utils.binding.Bind;
import conger.com.pandamusic.widget.DiscView;
import conger.com.pandamusic.widget.IndicatorLayout;
import me.wcy.lrcview.LrcView;

/**
 *  2017.6.5 正在施工中。。。
 *   切换 唱片 切换歌曲还有点逻辑没处理好。。
 */
public class PlayFragment extends BaseFragment implements View.OnClickListener,
        ViewPager.OnPageChangeListener, SeekBar.OnSeekBarChangeListener ,DiscView.IPlayInfo{


    @Bind(R.id.ll_content)
    private LinearLayout llContent;
    @Bind(R.id.iv_play_page_bg)
    private ImageView ivPlayingBg;
    @Bind(R.id.iv_back)
    private ImageView ivBack;
    @Bind(R.id.tv_title)
    private TextView tvTitle;
    @Bind(R.id.tv_artist)
    private TextView tvArtist;
    @Bind(R.id.vp_play_page)
    private ViewPager vpPlay;
    @Bind(R.id.il_indicator)
    private IndicatorLayout ilIndicator;
    @Bind(R.id.sb_progress)
    private SeekBar sbProgress;
    @Bind(R.id.tv_current_time)
    private TextView tvCurrentTime;
    @Bind(R.id.tv_total_time)
    private TextView tvTotalTime;
    @Bind(R.id.iv_mode)
    private ImageView ivMode;
    @Bind(R.id.iv_play)
    private ImageView ivPlay;
    @Bind(R.id.iv_next)
    private ImageView ivNext;
    @Bind(R.id.iv_prev)
    private ImageView ivPrev;

    private LrcView mLrcViewFull;
    private SeekBar sbVolume;
    private AudioManager mAudioManager;
    private List<View> mViewPagerContent;
    private int mLastProgress;
    public SeekBar mVolumeBar;
    public DiscView mDiscView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play, container, false);
    }

    @Override
    protected void init() {
        initSystemBar();
        initViewPager();
        ilIndicator.create(mViewPagerContent.size());
        initPlayMode();
        onChange(getPlayService().getPlayingMusic());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Actions.VOLUME_CHANGED_ACTION);
        getContext().registerReceiver(mVolumeReceiver, filter);
    }

    @Override
    protected void setListener() {
        ivBack.setOnClickListener(this);
        ivMode.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        sbProgress.setOnSeekBarChangeListener(this);
        sbVolume.setOnSeekBarChangeListener(this);
        vpPlay.setOnPageChangeListener(this);
        mDiscView.setPlayInfoListener(this);
    }

    /**
     * 沉浸式状态栏
     */
    private void initSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = ScreenUtils.getSystemBarHeight();
            llContent.setPadding(0, top, 0, 0);
        }
    }

    private void initViewPager() {

        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_cover, null);
        View lrcView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_lrc, null);


        mDiscView = (DiscView) coverView.findViewById(R.id.discview);

        Logger.d(AppCache.getMusicList().size());
        mDiscView.setMusicDataList(AppCache.getMusicList());

        mLrcViewFull = (LrcView) lrcView.findViewById(R.id.lrc_view_full);
        sbVolume = (SeekBar) lrcView.findViewById(R.id.sb_volume);

        initVolume();

        mViewPagerContent = new ArrayList<>(2);
        mViewPagerContent.add(coverView);
        mViewPagerContent.add(lrcView);
        vpPlay.setAdapter(new PlayPagerAdapter(mViewPagerContent));
    }

    private void initVolume() {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        sbVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));


    }

    private void initPlayMode() {
        int mode = Preferences.getPlayMode();
        ivMode.setImageLevel(mode);
    }

    /**
     * 更新播放进度
     */
    public void onPublish(int progress) {
        sbProgress.setProgress(progress);
        if (mLrcViewFull.hasLrc()) {

            mLrcViewFull.updateTime(progress);
        }
        //更新当前播放时间
        if (progress - mLastProgress >= 1000) {
            tvCurrentTime.setText(formatTime(progress));
            mLastProgress = progress;
        }
    }

    public void onChange(Music music) {
        onPlay(music);
    }

    public void onPlayerPause() {
        ivPlay.setSelected(false);
        mDiscView.playOrPause();
    }

    public void onPlayerResume() {
        ivPlay.setSelected(true);
        mDiscView.playOrPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_mode:
                switchPlayMode();
                break;
            case R.id.iv_play:
                play();
                mDiscView.playOrPause();
                break;
            case R.id.iv_next:
                next();
                mDiscView.next();
                break;
            case R.id.iv_prev:
                prev();
                mDiscView.last();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        ilIndicator.setCurrent(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == sbProgress) {
            if (getPlayService().isPlaying() || getPlayService().isPausing()) {
                int progress = seekBar.getProgress();
                getPlayService().seekTo(progress);
                mLrcViewFull.onDrag(progress);
                tvCurrentTime.setText(formatTime(progress));
                mLastProgress = progress;
            } else {
                seekBar.setProgress(0);
            }
        } else if (seekBar == sbVolume) {

            /**
             *  音量控制
             */
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(),
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
    }

    private void onPlay(Music music) {
        if (music == null) {
            return;
        }
        tvTitle.setText(music.getTitle());
        tvArtist.setText(music.getArtist());
        sbProgress.setMax((int) music.getDuration());
        sbProgress.setProgress(0);
        mLastProgress = 0;
        tvCurrentTime.setText(R.string.play_time_start);
        tvTotalTime.setText(formatTime(music.getDuration()));
        setCoverAndBg(music);
        setLrc(music);
        if (getPlayService().isPlaying() || getPlayService().isPreparing()) {
            Logger.d("Music onPlay");
            ivPlay.setSelected(true);
            mDiscView.playOrPause();
        } else {
            Logger.d("Music is not onPlay");
            //图片切换错误 error
            ivPlay.setSelected(false);
            mDiscView.playOrPause();
        }
    }

    /**
     *  播放或者暂停
     */
    private void play() {
        getPlayService().playPause();

    }

    /**
     *  下一首
     */
    private void next() {
        getPlayService().next();
    }


    /**
     *  上一首
     */
    private void prev() {
        getPlayService().prev();
    }

    private void switchPlayMode() {
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case LOOP:
                mode = PlayModeEnum.SHUFFLE;
                ToastUtils.show(R.string.mode_shuffle);
                break;
            case SHUFFLE:
                mode = PlayModeEnum.SINGLE;
                ToastUtils.show(R.string.mode_one);
                break;
            case SINGLE:
                mode = PlayModeEnum.LOOP;
                ToastUtils.show(R.string.mode_loop);
                break;
        }
        Preferences.savePlayMode(mode.value());
        initPlayMode();
    }

    private void onBackPressed() {
        getActivity().onBackPressed();
        ivBack.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivBack.setEnabled(true);
            }
        }, 300);
    }

    /**
     *  设置背景磨玻璃图片
     * @param music
     */
    private void setCoverAndBg(Music music) {

        ivPlayingBg.setImageBitmap(CoverLoader.getInstance().loadBlur(music));
    }

    private void setLrc(final Music music) {
        if (music.getType() == Music.Type.LOCAL) {
            String lrcPath = FileUtils.getLrcFilePath(music);
            if (!TextUtils.isEmpty(lrcPath)) {
                loadLrc(lrcPath);
            } else {
                new SearchLrc(music.getArtist(), music.getTitle()) {
                    @Override
                    public void onPrepare() {
                        // 设置tag防止歌词下载完成后已切换歌曲
                        vpPlay.setTag(music);

                        loadLrc("");
                        setLrcLabel("正在搜索歌词");
                    }

                    @Override
                    public void onExecuteSuccess(@NonNull String lrcPath) {
                        if (vpPlay.getTag() != music) {
                            return;
                        }

                        // 清除tag
                        vpPlay.setTag(null);

                        loadLrc(lrcPath);
                        setLrcLabel("暂无歌词");
                    }

                    @Override
                    public void onExecuteFail(Exception e) {
                        if (vpPlay.getTag() != music) {
                            return;
                        }

                        // 清除tag
                        vpPlay.setTag(null);

                        setLrcLabel("暂无歌词");
                    }
                }.execute();
            }
        } else {
            String lrcPath = FileUtils.getLrcDir() + FileUtils.getLrcFileName(music.getArtist(), music.getTitle());
            loadLrc(lrcPath);
        }
    }

    private void loadLrc(String path) {
        File file = new File(path);

        mLrcViewFull.loadLrc(file);

    }

    private void setLrcLabel(String label) {

        mLrcViewFull.setLabel(label);
    }

    private String formatTime(long time) {
        return SystemUtils.formatTime("mm:ss", time);
    }

    /**
     *  音量
     */
    private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    };

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mVolumeReceiver);
        super.onDestroy();
    }


    @Override
    public void onMusicInfoChanged(Music music) {

        tvArtist.setText(music.getArtist());
        tvTitle.setText(music.getTitle());
       // onChange(music);

    }

    @Override
    public void onMusicPicChanged(String musicPicRes) {

        Logger.d(musicPicRes);

    }

    @Override
    public void onMusicChanged(DiscView.MusicChangedStatus musicChangedStatus) {

        // PLAY, PAUSE, NEXT, LAST, STOP

           switch (musicChangedStatus){

               case PLAY:
                   onPlayerPause();
                   break;
               case PAUSE:
                   onPlayerPause();
                   break;
               case NEXT:
                   next();
                   break;
               case LAST:
                   prev();
                   break;
               case STOP:
                   onPlayerPause();
                   break;


           }
    }
}
