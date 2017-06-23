package conger.com.pandamusic.executor;

import android.app.Activity;
import android.text.TextUtils;

import java.io.File;


import conger.com.pandamusic.model.DownloadInfo;
import conger.com.pandamusic.model.Lrc;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.model.SearchMusic;
import conger.com.pandamusic.net.RetrofitHelper;
import conger.com.pandamusic.utils.FileUtils;
import conger.com.pandamusic.utils.ToastUtils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 播放搜索的音乐
 * Created by selfimpr on 2017/4/21.
 */
public abstract class PlaySearchedMusic extends PlayMusic {
    private SearchMusic.Song mSong;

    public PlaySearchedMusic(Activity activity, SearchMusic.Song song) {
        super(activity, 2);
        mSong = song;
    }

    @Override
    protected void getPlayInfo() {
        String lrcFileName = FileUtils.getLrcFileName(mSong.getArtistname(), mSong.getSongname());
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (!lrcFile.exists()) {
            downloadLrc(lrcFile.getPath());
        } else {
            mCounter++;
        }

        music = new Music();
        music.setType(Music.Type.ONLINE);
        music.setTitle(mSong.getSongname());
        music.setArtist(mSong.getArtistname());


        RetrofitHelper.getRetrofitHelper().fetchDownloadInfo(mSong.getSongid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DownloadInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show("Error");
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

    private void downloadLrc(final String filePath) {

        RetrofitHelper.getRetrofitHelper().fetchLrc(mSong.getSongid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Lrc>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Lrc lrc) {
                        if (lrc == null || TextUtils.isEmpty(lrc.getLrcContent())) {
                            return;
                        }

                        FileUtils.saveLrcFile(filePath, lrc.getLrcContent());
                    }
                });


    }
}
