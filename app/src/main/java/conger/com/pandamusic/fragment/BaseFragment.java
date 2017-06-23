package conger.com.pandamusic.fragment;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.security.Permission;

import conger.com.pandamusic.R;
import conger.com.pandamusic.application.AppCache;
import conger.com.pandamusic.model.Music;
import conger.com.pandamusic.service.PlayService;
import conger.com.pandamusic.utils.FileUtils;
import conger.com.pandamusic.utils.SystemUtils;
import conger.com.pandamusic.utils.ToastUtils;
import conger.com.pandamusic.utils.binding.ViewBinder;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


/**
 * 基类<br>
 * Created by selfimpr on 2015/11/26.
 */
@RuntimePermissions
public abstract class BaseFragment extends Fragment {



    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isInitialized;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewBinder.bind(this, view);

        init();
        setListener();
        Logger.d("onCreate: " + getClass().getSimpleName());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                isInitialized = true;
            }
        });
    }



    protected abstract void init();

    protected abstract void setListener();

    public boolean isInitialized() {
        return isInitialized;
    }

    protected PlayService getPlayService() {
        PlayService playService = AppCache.getPlayService();
        if (playService == null) {
            throw new NullPointerException("play service is null");
        }
        return playService;
    }


    /**
     * 分享音乐
     */
    protected void shareMusic(Music music) {
        File file = new File(music.getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }


    /**
     * 设置铃声
     */
    protected void setRingtone(Music music) {
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getPath());
        // 查询音乐文件在媒体库是否存在
        Cursor cursor = getContext().getContentResolver().query(uri, null,
                MediaStore.MediaColumns.DATA + "=?", new String[]{music.getPath()}, null);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_PODCAST, false);

            getContext().getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA + "=?",
                    new String[]{music.getPath()});
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, newUri);
            ToastUtils.show(R.string.setting_ringtone_success);
        }
        cursor.close();
    }


    /**
     * 删除音乐
     */
    protected void deleteMusic(final Music music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        String title = music.getTitle();
        String msg = getString(R.string.delete_music, title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppCache.getMusicList().remove(music);
                File file = new File(music.getPath());
                if (file.delete()) {
                    getPlayService().updatePlayingPosition();
                    updateView();
                    // 刷新媒体库
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + music.getPath()));
                    getContext().sendBroadcast(intent);
                }
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }


    protected void musicInfo(Music music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        StringBuilder sb = new StringBuilder();
        sb.append("艺术家：")
                .append(music.getArtist())
                .append("\n\n")
                .append("专辑：")
                .append(music.getAlbum())
                .append("\n\n")
                .append("播放时长：")
                .append(SystemUtils.formatTime("mm:ss", music.getDuration()))
                .append("\n\n")
                .append("文件名称：")
                .append(music.getFileName())
                .append("\n\n")
                .append("文件大小：")
                .append(FileUtils.b2mb((int) music.getFileSize()))
                .append("MB")
                .append("\n\n")
                .append("文件路径：")
                .append(new File(music.getPath()).getParent());
        dialog.setMessage(sb.toString());
        dialog.show();
    }


    /**
     *  设置铃声前要申请权限
     * @param music
     */
    protected void requestSetRingtone(final Music music) {
        BaseFragmentPermissionsDispatcher.needPermissonWithCheck(this,music);


    }



    protected  void updateView(){



    }

    @NeedsPermission(Manifest.permission.WRITE_SETTINGS)
    void needPermisson(Music music) {

       setRingtone(music);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseFragmentPermissionsDispatcher.onActivityResult(this, requestCode);

    }

    @OnShowRationale(Manifest.permission.WRITE_SETTINGS)
    void showPermissonMessage(final PermissionRequest request) {
    }

    @OnPermissionDenied(Manifest.permission.WRITE_SETTINGS)
    void showPermissonDeniedMessage() {

        ToastUtils.show("没有权限");
    }
}
