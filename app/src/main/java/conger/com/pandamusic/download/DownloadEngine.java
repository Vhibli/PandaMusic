package conger.com.pandamusic.download;

import android.text.TextUtils;

import com.arialyy.aria.core.Aria;
import com.orhanobut.logger.Logger;

import java.io.File;

import conger.com.pandamusic.model.DownloadInfo;
import conger.com.pandamusic.utils.FileUtils;
import conger.com.pandamusic.utils.ToastUtils;

import static conger.com.pandamusic.application.AppCache.getContext;

/**
 * Created by selfimpr on 2017/6/5. 正在施工。。。。
 *  多线程下载库 Aria
 *  https://github.com/AriaLyy/Aria
 */
public class DownloadEngine {

    /**
     *  下载mp3文件
     * @param downloadInfo
     * @param artist
     * @param title
     */
    public static void downloadMusic(DownloadInfo downloadInfo, String artist, String title) {

        final String fileName = FileUtils.getMp3FileName(artist, title);
        ToastUtils.show("开始下载" + fileName);
        Aria.download(getContext())
                .load(downloadInfo.getBitrate().getFile_link())
                .setDownloadPath(FileUtils.getMusicDir() + fileName)
                .start();


    }

    /**
     *  下载歌词
     * @param lrcLink
     * @param artist
     * @param title
     */
    public static void downloadLrc(String lrcLink, String artist, String title) {
        String lrcFileName = FileUtils.getLrcFileName(artist, title);
        File lrcFile = new File(FileUtils.getLrcDir() + lrcFileName);
        if (!TextUtils.isEmpty(lrcLink) && !lrcFile.exists()) {

            Logger.d(lrcLink);
            Aria.download(getContext())
                    .load(lrcLink)
                    .setDownloadPath(FileUtils.getLrcDir() + lrcFileName)
                    .start();
        } else {

            ToastUtils.show("歌词文件不存在");

        }
    }



    /**
     *  下载歌曲封面
     * @param picLink
     * @param artist
     * @param title
     */
    public static void downloadAlbum(String picLink, String artist, String title) {
        String albumFileName = FileUtils.getAlbumFileName(artist, title);
        File albumFile = new File(FileUtils.getAlbumDir(), albumFileName);

        if (!albumFile.exists() && !TextUtils.isEmpty(picLink)) {

            Logger.d(picLink);
            Aria.download(getContext())
                    .load(picLink)
                    .setDownloadPath(FileUtils.getAlbumDir() + albumFileName)
                    .start();
        }else {

            ToastUtils.show("歌曲封面不存在");

        }


    }
}
