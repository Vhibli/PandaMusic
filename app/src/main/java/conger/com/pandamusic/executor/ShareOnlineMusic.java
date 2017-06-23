package conger.com.pandamusic.executor;

import android.content.Context;
import android.content.Intent;

import conger.com.pandamusic.R;
import conger.com.pandamusic.model.DownloadInfo;
import conger.com.pandamusic.net.RetrofitHelper;
import conger.com.pandamusic.utils.ToastUtils;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 分享在线歌曲
 * Created by selfimpr on 2017/4/23.
 */
public abstract class ShareOnlineMusic implements IExecutor<Void> {
    private Context mContext;
    private String mTitle;
    private String mSongId;

    public ShareOnlineMusic(Context context, String title, String songId) {
        mContext = context;
        mTitle = title;
        mSongId = songId;
    }

    @Override
    public void execute() {
        onPrepare();
        share();
    }

    private void share() {


        RetrofitHelper.getRetrofitHelper().fetchDownloadInfo(mSongId)
                .subscribeOn(Schedulers.io())
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
                        if (downloadInfo == null) {
                            onError(null);
                            return;
                        }

                        onExecuteSuccess(null);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.share_music, mContext.getString(R.string.app_name),
                                mTitle, downloadInfo.getBitrate().getFile_link()));
                        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
                    }
                });

    }
}
