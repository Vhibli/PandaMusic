package conger.com.pandamusic.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import conger.com.pandamusic.R;
import conger.com.pandamusic.adapter.LocalMusicAdapter;
import conger.com.pandamusic.adapter.OnMoreClickListener;
import conger.com.pandamusic.application.AppCache;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.utils.binding.Bind;



/**
 * 本地音乐列表
 * Created by selfimpr on 2015/11/26.
 */
public class LocalMusicFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnMoreClickListener {

    @Bind(R.id.lv_local_music)
    private ListView lvLocalMusic;
    @Bind(R.id.tv_empty)
    private TextView tvEmpty;
    private LocalMusicAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_music, container, false);
    }
    @Override
    protected void init() {
        mAdapter = new LocalMusicAdapter();
        mAdapter.setOnMoreClickListener(this);
        lvLocalMusic.setAdapter(mAdapter);
        if (getPlayService().getPlayingMusic() != null && getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            lvLocalMusic.setSelection(getPlayService().getPlayingPosition());
        }
        updateView();

    }

    @Override
    protected void setListener() {
        lvLocalMusic.setOnItemClickListener(this);
    }

    @Override
    protected void updateView() {
        if (AppCache.getMusicList().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.updatePlayingPosition(getPlayService());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getPlayService().play(position);
    }

    @Override
    public void onMoreClick(final int position) {
        final Music music = AppCache.getMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        int itemsId = (position == getPlayService().getPlayingPosition()) ? R.array.local_music_dialog_without_delete : R.array.local_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        shareMusic(music);
                        break;
                    case 1:// 设为铃声
                        requestSetRingtone(music);
                        break;
                    case 2:// 查看歌曲信息
                        musicInfo(music);
                        break;
                    case 3:// 删除
                        deleteMusic(music);
                        break;
                }
            }
        });
        dialog.show();
    }

    public void onItemPlay() {
        updateView();
        if (getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            lvLocalMusic.smoothScrollToPosition(getPlayService().getPlayingPosition());
        }
    }





    @Override
    public void onDestroy() {

        super.onDestroy();
    }

}
