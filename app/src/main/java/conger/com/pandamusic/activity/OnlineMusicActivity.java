package conger.com.pandamusic.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arialyy.aria.core.Aria;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import conger.com.pandamusic.R;
import conger.com.pandamusic.adapter.OnMoreClickListener;
import conger.com.pandamusic.adapter.OnlineMusicAdapter;
import conger.com.pandamusic.constants.Extras;
import conger.com.pandamusic.download.DownloadEngine;
import conger.com.pandamusic.enums.LoadStateEnum;
import conger.com.pandamusic.executor.PlayOnlineMusic;
import conger.com.pandamusic.executor.ShareOnlineMusic;
import conger.com.pandamusic.model.DownloadInfo;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.model.OnlineMusic;
import conger.com.pandamusic.model.OnlineMusicList;
import conger.com.pandamusic.model.SongListInfo;
import conger.com.pandamusic.net.RetrofitHelper;
import conger.com.pandamusic.utils.FileUtils;
import conger.com.pandamusic.utils.ImageUtils;
import conger.com.pandamusic.utils.ScreenUtils;
import conger.com.pandamusic.utils.ToastUtils;
import conger.com.pandamusic.utils.ViewUtils;
import conger.com.pandamusic.utils.binding.Bind;
import conger.com.pandamusic.widget.AutoLoadListView;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static conger.com.pandamusic.application.AppCache.getContext;

/**
 *  在线音乐界面
 *
 */
public class OnlineMusicActivity extends BaseActivity implements OnItemClickListener
        , OnMoreClickListener, AutoLoadListView.OnLoadListener {
    private static final int MUSIC_LIST_SIZE = 20;

    @Bind(R.id.lv_online_music_list)
    private AutoLoadListView lvOnlineMusic;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private View vHeader;
    private SongListInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);
    private ProgressDialog mProgressDialog;
    private int mOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music);

        if (!checkServiceAlive()) {
            return;
        }

        mListInfo = (SongListInfo) getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);
        setTitle(mListInfo.getTitle());

        init();
        onLoad();
    }

    private void init() {
        vHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_music_list_header, null);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(150));
        vHeader.setLayoutParams(params);
        lvOnlineMusic.addHeaderView(vHeader, null, false);
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
    }

    @Override
    protected void setListener() {
        lvOnlineMusic.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }



    @Override
    public void onLoad() {
        getMusic(mOffset);
    }

    private void getMusic(final int offset) {

        RetrofitHelper.getRetrofitHelper().fetchOnLineMusicList(mListInfo.getType(), MUSIC_LIST_SIZE, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<OnlineMusicList>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show(R.string.load_fail);

                    }

                    @Override
                    public void onNext(OnlineMusicList onlineMusicList) {


                        lvOnlineMusic.onLoadComplete();
                        mOnlineMusicList = onlineMusicList;
                        if (offset == 0 && onlineMusicList == null) {
                            ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                            return;
                        } else if (offset == 0) {
                            initHeader();
                            ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                        }
                        if (onlineMusicList == null || onlineMusicList.getSong_list() == null || onlineMusicList.getSong_list().size() == 0) {
                            lvOnlineMusic.setEnable(false);
                            return;
                        }
                        mOffset += MUSIC_LIST_SIZE;
                        mMusicList.addAll(onlineMusicList.getSong_list());
                        mAdapter.notifyDataSetChanged();
                    }
                });


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        play((OnlineMusic) parent.getAdapter().getItem(position));
    }

    /**
     *  弹出分享
     * @param position
     */
    @Override
    public void onMoreClick(int position) {
        final OnlineMusic onlineMusic = mMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(mMusicList.get(position).getTitle());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(onlineMusic.getArtist_name(), onlineMusic.getTitle());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.online_music_dialog_without_download : R.array.online_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(onlineMusic);
                        break;
                    case 1:// 查看歌手信息
                        artistInfo(onlineMusic);
                        break;
                    case 2:// 下载
                        download(onlineMusic);
                        break;
                }
            }
        });
        dialog.show();
    }

    private void initHeader() {
        final ImageView ivHeaderBg = (ImageView) vHeader.findViewById(R.id.iv_header_bg);
        final ImageView ivCover = (ImageView) vHeader.findViewById(R.id.iv_cover);
        TextView tvTitle = (TextView) vHeader.findViewById(R.id.tv_title);
        TextView tvUpdateDate = (TextView) vHeader.findViewById(R.id.tv_update_date);
        TextView tvComment = (TextView) vHeader.findViewById(R.id.tv_comment);
        tvTitle.setText(mOnlineMusicList.getBillboard().getName());
        tvUpdateDate.setText(getString(R.string.recent_update, mOnlineMusicList.getBillboard().getUpdate_date()));
        tvComment.setText(mOnlineMusicList.getBillboard().getComment());
        ImageSize imageSize = new ImageSize(200, 200);
        ImageLoader.getInstance().loadImage(mOnlineMusicList.getBillboard().getPic_s640(), imageSize,
                ImageUtils.getCoverDisplayOptions(), new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        ivCover.setImageBitmap(loadedImage);
                        ivHeaderBg.setImageBitmap(ImageUtils.blur(loadedImage));
                    }
                });
    }

    private void play(OnlineMusic onlineMusic) {
        new PlayOnlineMusic(this, onlineMusic) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Music music) {
                mProgressDialog.cancel();
                getPlayService().play(music);
                ToastUtils.show(getString(R.string.now_play, music.getTitle()));
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
                ToastUtils.show(R.string.unable_to_play);
            }
        }.execute();
    }

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
            @Override
            public void onPrepare() {
                mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                mProgressDialog.cancel();
            }

            @Override
            public void onExecuteFail(Exception e) {
                mProgressDialog.cancel();
            }
        }.execute();
    }

    private void artistInfo(OnlineMusic onlineMusic) {
        ArtistInfoActivity.start(this, onlineMusic.getTing_uid());
    }


    /**
     *  下载音乐
     * @param onlineMusic
     */
    private void download(final OnlineMusic onlineMusic) {



        /**
         *  下载mp3文件
         */
        RetrofitHelper.getRetrofitHelper().fetchDownloadInfo(onlineMusic.getSong_id())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DownloadInfo>() {
                    @Override
                    public void onCompleted() {
                        ToastUtils.show("下载完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show("下载失败");
                    }

                    @Override
                    public void onNext(DownloadInfo downloadInfo) {

                        DownloadEngine.downloadMusic(downloadInfo,onlineMusic.getArtist_name(),onlineMusic.getTitle());
                    }
                });


        /**
         *  下载歌词
         */
        DownloadEngine.downloadLrc(onlineMusic.getLrclink(),onlineMusic.getArtist_name(),onlineMusic.getTitle());


        /**
         *  下载封面
         */
        DownloadEngine.downloadAlbum(onlineMusic.getPic_big(),onlineMusic.getArtist_name(),onlineMusic.getTitle());


    }
}
