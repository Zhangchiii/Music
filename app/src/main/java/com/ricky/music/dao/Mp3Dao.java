package com.ricky.music.dao;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ricky.music.bean.Mp3Info;
import com.ricky.music.util.Common;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricky on 2016/6/20.
 */
public class Mp3Dao {

    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    List<Mp3Info> infoList;
    Mp3Info mp3Info;
    long id;
    String title;
    String artist;
    long duration;
    long size;
    String url;
    int isMusic;
    String album;
    long albumId;

    public List<Mp3Info> GetMp3Infos(Context context) {

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        infoList = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {

            cursor.moveToNext();
            id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

            if (isMusic != 0) {
                mp3Info = new Mp3Info();
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setDuration(Common.formatTime(duration));
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
                mp3Info.setAlbum(album);
                mp3Info.setAlbumId(albumId);
                infoList.add(mp3Info);
            }
        }
        return infoList;
    }


    public static Bitmap getArtWork(Context context, long id, long albumId, boolean allowDefault, boolean small) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, albumId);
        if (uri != null) {
            InputStream inputStream = null;
            try {
                inputStream = resolver.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                if (small) {
                    options.inSampleSize = options.outWidth / 100;
                } else {
                    options.inSampleSize = options.outWidth / 800;
                }

                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                inputStream = resolver.openInputStream(uri);
                return BitmapFactory.decodeStream(inputStream, null, options);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}