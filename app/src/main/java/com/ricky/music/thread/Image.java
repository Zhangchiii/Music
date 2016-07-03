package com.ricky.music.thread;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Ricky on 2016/7/4.
 */
public class Image extends Thread {

    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    private ImageView imageView;
    private Handler handler;
    private boolean small;
    private Context context;
    private long albumId;

    public Image(Context context, ImageView imageView, Handler handler, long albumId, boolean small) {
        this.handler = handler;
        this.imageView = imageView;
        this.small = small;
        this.context = context;
        this.albumId = albumId;
    }

    @Override
    public void run() {
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
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
