package conger.com.pandamusic.executor;

import android.text.TextUtils;

import conger.com.pandamusic.enums.LoadStateEnum;
import conger.com.pandamusic.model.Lrc;
import conger.com.pandamusic.model.SearchMusic;
import conger.com.pandamusic.net.RetrofitHelper;
import conger.com.pandamusic.utils.FileUtils;
import conger.com.pandamusic.utils.ViewUtils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 如果本地歌曲没有歌词则从网络搜索歌词
 * Created by selfimpr on 2017/4/23.
 */
public abstract class SearchLrc implements IExecutor<String> {
    private String artist;
    private String title;

    public SearchLrc(String artist, String title) {
        this.artist = artist;
        this.title = title;
    }

    @Override
    public void execute() {
        onPrepare();
        searchLrc();
    }

    private void searchLrc() {


        RetrofitHelper.getRetrofitHelper().fetchSearchMusic(title + "-" + artist)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SearchMusic>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {


                    }

                    @Override
                    public void onNext(SearchMusic searchMusic) {
                        if (searchMusic == null || searchMusic.getSong() == null || searchMusic.getSong().isEmpty()) {
                            onError(null);
                            return;
                        }

                        downloadLrc(searchMusic.getSong().get(0).getSongid());
                    }
                });

    }

    private void downloadLrc(String songId) {


        RetrofitHelper.getRetrofitHelper().fetchLrc(songId)
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
                            onError(null);
                            return;
                        }

                        String filePath = FileUtils.getLrcDir() + FileUtils.getLrcFileName(artist, title);
                        FileUtils.saveLrcFile(filePath, lrc.getLrcContent());
                        onExecuteSuccess(filePath);
                    }
                });

    }
}
