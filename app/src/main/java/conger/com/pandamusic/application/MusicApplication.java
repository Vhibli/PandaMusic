package conger.com.pandamusic.application;

import android.app.Application;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;



import conger.com.pandamusic.utils.Preferences;



public class MusicApplication extends Application {

    private static MusicApplication INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE =this;
        AppCache.init(this);
        AppCache.updateNightMode(Preferences.isNightMode());
        initImageLoader();



    }


    public static MusicApplication getInstance(){

        return INSTANCE;

    }


    private void initImageLoader() {
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSize(2 * 1024 * 1024) // 2MB
                .diskCacheSize(50 * 1024 * 1024) // 50MB
                .build();
        ImageLoader.getInstance().init(configuration);
    }


}
