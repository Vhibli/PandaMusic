package conger.com.pandamusic.net.apis;

import conger.com.pandamusic.model.ArtistInfo;
import conger.com.pandamusic.model.DownloadInfo;
import conger.com.pandamusic.model.Lrc;
import conger.com.pandamusic.model.OnlineMusicList;
import conger.com.pandamusic.model.SearchMusic;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface MusicApis {

    String HOST = "http://tingapi.ting.baidu.com/";

    String METHOD_GET_MUSIC_LIST = "baidu.ting.billboard.billList";

    String METHOD_DOWNLOAD_MUSIC = "baidu.ting.song.play";

    String METHOD_LRC = "baidu.ting.song.lry";

    String METHOD_SEARCH_MUSIC = "baidu.ting.search.catalogSug";

    String METHOD_ARTIST_INFO = "baidu.ting.artist.getInfo";

    /**
     *  获取在线音乐列表
     * @param method
     * @param type
     * @param size
     * @param offset
     * @return
     */
    @GET("v1/restserver/ting")
    Observable<OnlineMusicList> getOnLineMusicList(@Query("method") String method, @Query("type") String type, @Query("size") int size, @Query("offset") int offset);


    /**
     *  获取下载的歌曲信息
     * @param method
     * @param songId
     * @return
     */
    @GET("v1/restserver/ting")
    Observable<DownloadInfo> getDownloadInfo(@Query("method") String method, @Query("songid")String songId);


    /**
     *  获取下载的歌词
     * @param method
     * @param songId
     * @return
     */
    @GET("v1/restserver/ting")
    Observable<Lrc> getLrc(@Query("method") String method, @Query("songid")String songId);

    /**
     *  获取搜索的音乐
     * @param method
     * @param keyWord
     * @return
     */
    @GET("v1/restserver/ting")
    Observable<SearchMusic> getSearchMusic(@Query("method") String method, @Query("query") String keyWord);


    /**
     *  获取歌手信息
     * @param method
     * @param tingUid
     * @return
     */
    @GET("v1/restserver/ting")
    Observable<ArtistInfo> getArtistInfo(@Query("method") String method, @Query("tinguid")String tingUid);


}
