package conger.com.pandamusic.constants;

import java.io.File;

import conger.com.pandamusic.application.MusicApplication;

/**
 * Created by selfimpr on 2017/4/23.
 */

public class Constants {

    /**
     *  是来自 【本地音乐】 【专辑】 【歌手】 其中一个的标示
     */
    public static final int  START_FROM_ARTIST = 1;
    public static final int START_FROM_ALBUM = 2;
    public static final int START_FROM_LOCAL = 3;

    //缓存的路径
    public static final String PATH_DATA = MusicApplication.getInstance().getCacheDir().getAbsolutePath() + File.separator + "PandaMusic";

    public static final String PATH_CACHE = PATH_DATA + "/NetCache";
}
