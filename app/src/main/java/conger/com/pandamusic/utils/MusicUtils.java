package conger.com.pandamusic.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import conger.com.pandamusic.R;
import conger.com.pandamusic.model.AlbumInfo;
import conger.com.pandamusic.model.LocalArtistInfo;
import conger.com.pandamusic.model.Music;


import static conger.com.pandamusic.constants.Constants.START_FROM_ALBUM;
import static conger.com.pandamusic.constants.Constants.START_FROM_ARTIST;
import static conger.com.pandamusic.constants.Constants.START_FROM_LOCAL;

/**
 * 歌曲工具类  主要是使用ContentResolver 从Media库中获取音乐文件
 * Created selfimpr  on 2015/11/27.
 */
public class MusicUtils {

    public static final int FILTER_SIZE = 1 * 1024 * 1024;// 1MB
    public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟


    private static String[] proj_music = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE};
    private static String[] proj_album = new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.NUMBER_OF_SONGS, MediaStore.Audio.Albums.ARTIST};

    private static String[] proj_artist = new String[]{
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists._ID};

    /**
     * 扫描歌曲 本地音乐
     */
    public static void scanMusic(Context context, List<Music> musicList) {
        musicList.clear();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            // 是否为音乐
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic == 0) {
                continue;
            }
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String unknown = context.getString(R.string.unknown);
            artist = artist.equals("<unknown>") ? unknown : artist;
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String coverPath = getCoverPath(context, albumId);
            String fileName = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            long fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            Music music = new Music();
            music.setId(id);
            music.setType(Music.Type.LOCAL);
            music.setTitle(title);
            music.setArtist(artist);
            music.setAlbum(album);
            music.setDuration(duration);
            music.setPath(path);
            music.setCoverPath(coverPath);
            music.setFileName(fileName);
            music.setFileSize(fileSize);
            CoverLoader.getInstance().loadThumbnail(music);
            musicList.add(music);
        }
        cursor.close();
    }


    /**
     * 获取专辑信息
     *
     * @param context
     * @return
     */
    public static List<AlbumInfo> queryAlbums(Context context) {

        ContentResolver cr = context.getContentResolver();
        StringBuilder where = new StringBuilder(MediaStore.Audio.Albums._ID
                + " in (select distinct " + MediaStore.Audio.Media.ALBUM_ID
                + " from audio_meta where (1=1)");
        where.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
        where.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);

        where.append(" )");

        List<AlbumInfo> list = getAlbumList(cr.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, proj_album,
                where.toString(), null, null));
        return list;

    }


    /**
     * 获取歌手信息
     *
     * @param context
     * @return
     */
    public static List<LocalArtistInfo> queryArtist(Context context) {

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        StringBuilder where = new StringBuilder(MediaStore.Audio.Artists._ID
                + " in (select distinct " + MediaStore.Audio.Media.ARTIST_ID
                + " from audio_meta where (1=1 )");
        where.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
        where.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);

        where.append(")");

        List<LocalArtistInfo> list = getArtistList(cr.query(uri, proj_artist,
                where.toString(), null, null));

        return list;

    }


    /**
     *  获取专辑列表
     * @param cursor
     * @return
     */
    public static List<AlbumInfo> getAlbumList(Cursor cursor) {
        List<AlbumInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            AlbumInfo info = new AlbumInfo();
            info.album_name = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.ALBUM));
            info.album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
            info.number_of_songs = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS));
            info.album_art = getAlbumArtUri(info.album_id) + "";
            info.album_artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST));
            list.add(info);
        }
        cursor.close();
        return list;
    }



    public static List<Music> queryMusic1(Context context, String id, int from){

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();

        StringBuilder select = new StringBuilder(" 1=1 and title != ''");
        // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
        select.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
        select.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);


        switch (from) {
            case START_FROM_LOCAL:
                ArrayList<Music> list3 = getLocalMusicList(cr.query(uri, proj_music,
                        select.toString(), null,
                        null));
                return list3;
            case START_FROM_ARTIST:
                select.append(" and " + MediaStore.Audio.Media.ARTIST_ID + " = " + id);
                return getLocalMusicList(cr.query(uri, proj_music, select.toString(), null,
                        null));
            case START_FROM_ALBUM:
                select.append(" and " + MediaStore.Audio.Media.ALBUM_ID + " = " + id);
                return getLocalMusicList(cr.query(uri, proj_music,
                        select.toString(), null,
                        null));
            default:
                return null;
        }

    }

    /**
     *  根据id获取用户信息
     * @param
     * @return
     */
    public static LocalArtistInfo getArtistinfo(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, proj_artist, "_id =" + String.valueOf(id), null, null);
        if (cursor == null) {
            return null;
        }
        LocalArtistInfo artistInfo = new LocalArtistInfo();
        while (cursor.moveToNext()) {
            artistInfo.artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            artistInfo.number_of_tracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
        }
        cursor.close();
        return artistInfo;
    }






    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }


    public static List<LocalArtistInfo> getArtistList(Cursor cursor) {
        List<LocalArtistInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            LocalArtistInfo info = new LocalArtistInfo();
            info.artist_name = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            info.number_of_tracks = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
            info.artist_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
            list.add(info);
        }
        cursor.close();
        return list;
    }


    private static String getCoverPath(Context context, long albumId) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://media/external/audio/albums/" + albumId),
                new String[]{"album_art"}, null, null, null);
        if (cursor != null) {
            cursor.moveToNext();
            path = cursor.getString(0);
            cursor.close();
        }
        return path;
    }


    /**
     *  搜索本地音乐
     * @param cursor
     * @return
     */
    public static ArrayList<Music> getLocalMusicList(Cursor cursor){


        if (cursor == null) {
            return null;
        }

        ArrayList<Music> musicArrayList = new ArrayList<>();

        while (cursor.moveToNext()) {

            Music music1 = new Music();
            music1.setId(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID)));

            music1.setAlbum(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.ALBUM)));

            music1.setDuration(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION)));

            music1.setTitle(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));
            music1.setArtist(cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)));
            String filePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));
            //
            music1.setPath(filePath);

            music1.setFileSize(cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE)));
            music1.setType(Music.Type.LOCAL);

            musicArrayList.add(music1);

        }
        cursor.close();
        return musicArrayList;
    }



}
