package conger.com.pandamusic.executor;

import android.app.Activity;
import android.text.TextUtils;

import java.io.File;

import conger.com.pandamusic.download.DownloadEngine;
import conger.com.pandamusic.model.DownloadInfo;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.model.OnlineMusic;
import conger.com.pandamusic.net.RetrofitHelper;
import conger.com.pandamusic.utils.FileUtils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 播放在线音乐
 * Created by selfimpr on 2017/4/20.
 */
public abstract class PlayOnlineMusic extends PlayMusic {

    private OnlineMusic mOnlineMusic;

    public PlayOnlineMusic(Activity activity, OnlineMusic onlineMusic) {
        super(activity, 3);
        mOnlineMusic = onlineMusic;
    }

    @Override
    protected void getPlayInfo() {

        //获取 歌曲名字 和 作者
        String artist = mOnlineMusic.getArtist_name();
        String title = mOnlineMusic.getTitle();

        //根据作者和歌曲名称生成 歌词名称
        String lrcFileName = FileUtils.getLrcFileName(artist,title);

        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (!lrcFile.exists() && !TextUtils.isEmpty(mOnlineMusic.getLrclink())) {
            //下载歌词
            downloadLrc(mOnlineMusic.getLrclink(), artist, title);
        } else {
            mCounter++;
        }

        // 下载封面
        String albumFileName = FileUtils.getAlbumFileName(artist,title);
        File albumFile = new File(FileUtils.getAlbumDir(), albumFileName);
        String picUrl = mOnlineMusic.getPic_big();
        if (TextUtils.isEmpty(picUrl)) {
            picUrl = mOnlineMusic.getPic_small();
        }
        if (!albumFile.exists() && !TextUtils.isEmpty(picUrl)) {
            downloadAlbum(picUrl, artist, title);
        } else {
            mCounter++;
        }

        music = new Music();
        music.setType(Music.Type.ONLINE);
        music.setTitle(title);
        music.setArtist(artist);
        music.setAlbum(mOnlineMusic.getAlbum_title());


        RetrofitHelper.getRetrofitHelper().fetchDownloadInfo(mOnlineMusic.getSong_id()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DownloadInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        if (downloadInfo == null || downloadInfo.getBitrate() == null) {
                            onError(null);
                            return;
                        }

                        music.setPath(downloadInfo.getBitrate().getFile_link());
                        music.setDuration(downloadInfo.getBitrate().getFile_duration() * 1000);
                        checkCounter();

                    }
                });

    }

    private void downloadLrc(String url, String artist, String title) {

        DownloadEngine.downloadLrc(url,artist,title);

    }

    private void downloadAlbum(String picUrl, String artist, String title) {

        DownloadEngine.downloadAlbum(picUrl,artist,title);

    }
}
