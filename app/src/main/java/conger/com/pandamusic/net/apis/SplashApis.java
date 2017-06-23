package conger.com.pandamusic.net.apis;


import conger.com.pandamusic.model.Splash;
import retrofit2.http.GET;
import rx.Observable;


public interface SplashApis {

    public static final String HOST = "http://cn.bing.com/";

     static final String SPLASH_URL = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";

    /**
     *  获取启动界面图片
     * @return
     */
    @GET("HPImageArchive.aspx?format=js&idx=0&n=1")
    Observable<Splash> getSplashImage();

}
