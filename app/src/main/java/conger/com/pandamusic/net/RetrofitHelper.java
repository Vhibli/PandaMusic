package conger.com.pandamusic.net;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import conger.com.pandamusic.BuildConfig;
import conger.com.pandamusic.constants.Constants;
import conger.com.pandamusic.model.ArtistInfo;
import conger.com.pandamusic.model.DownloadInfo;
import conger.com.pandamusic.model.Lrc;
import conger.com.pandamusic.model.OnlineMusicList;
import conger.com.pandamusic.model.SearchMusic;
import conger.com.pandamusic.model.Splash;
import conger.com.pandamusic.net.apis.MusicApis;
import conger.com.pandamusic.net.apis.SplashApis;
import conger.com.pandamusic.utils.NetworkUtils;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;


/**
 * Created by selfimpr on 2017/6/4.
 */

public class RetrofitHelper {


    private static OkHttpClient mOkHttpClient;
    private static SplashApis mSplashApisClient;
    private static MusicApis mMusicApisClient;

    private RetrofitHelper(){

        initOkHttp();
        mSplashApisClient = getSplashApisClient();
        mMusicApisClient = getMusicApisClient();

    }




    public static RetrofitHelper getRetrofitHelper(){

        return Holder.mRetrofitHelper;

    }


    private static class Holder{

        public static final RetrofitHelper mRetrofitHelper  = new RetrofitHelper();

    }

    /**
     *  初始化OkHttp
     */
    private static void initOkHttp() {


        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(loggingInterceptor);
        }
        File cacheFile = new File(Constants.PATH_CACHE);
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 50);
        Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!NetworkUtils.isNetworkAvailable()) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response = chain.proceed(request);
                if (NetworkUtils.isNetworkAvailable()) {
                    int maxAge = 0;
                    // 有网络时, 不缓存, 最大保存时长为0
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    // 无网络时，设置超时为4周
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };

        //设置缓存
        builder.addNetworkInterceptor(cacheInterceptor);
        builder.addInterceptor(cacheInterceptor);
        builder.addInterceptor(new HttpInterceptor());
        builder.cache(cache);
        //设置超时
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);


        mOkHttpClient = builder.build();
    }



    private static Retrofit.Builder createRetrofitBuilder(){

        Retrofit.Builder builder = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(mOkHttpClient);


        return builder;


    }


    private static Retrofit createRetrofit(String baseurl){

        return createRetrofitBuilder().baseUrl(baseurl).build();

    }


    private static SplashApis getSplashApisClient() {

        return createRetrofit(SplashApis.HOST).create(SplashApis.class);

    }


    private static MusicApis getMusicApisClient(){

        return createRetrofit(MusicApis.HOST).create(MusicApis.class);


    }


    /**
     *  获取启动图片
     * @return
     */
    public Observable<Splash> fetchSplashImage(){

        return mSplashApisClient.getSplashImage();

    }


    /**
     *  获取在线音乐列表
     * @return
     */
    public Observable<OnlineMusicList> fetchOnLineMusicList(String type, int size, int offset){

        return mMusicApisClient.getOnLineMusicList(MusicApis.METHOD_GET_MUSIC_LIST,type,size,offset);

    }


    /**
     *  获取下载信息
     * @return
     */
    public Observable<DownloadInfo> fetchDownloadInfo(String songId){

        return mMusicApisClient.getDownloadInfo(MusicApis.METHOD_DOWNLOAD_MUSIC,songId);

    }

    /**
     *  获取歌词
     * @return
     */
    public Observable<Lrc> fetchLrc(String songId){

        return mMusicApisClient.getLrc(MusicApis.METHOD_LRC,songId);
    }

    /**
     *  获取搜索音乐
     * @return
     */
    public Observable<SearchMusic> fetchSearchMusic(String keyword){


        return mMusicApisClient.getSearchMusic(MusicApis.METHOD_SEARCH_MUSIC,keyword);

    }


    /**
     *  获取歌手信息
     * @return
     */
    public Observable<ArtistInfo> fetchArtistInfo(String tingUid){

        return mMusicApisClient.getArtistInfo(MusicApis.METHOD_ARTIST_INFO,tingUid);

    }


}
