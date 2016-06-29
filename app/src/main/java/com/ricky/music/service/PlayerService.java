package com.ricky.music.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ricky.music.bean.Mp3Info;
import com.ricky.music.dao.Mp3Dao;
import com.ricky.music.util.AppConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricky on 2016/6/20.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    @Nullable
    private MediaPlayer mediaPlayer;
    private String url;
    private boolean isPause;
    private int listPosition;
    private int currentTime;
    private int duration;
    private int status = 3;
    private boolean isPlay;
    private Mp3Dao mp3Dao;
    private List<Mp3Info> infoList;

    public static final String MUSIC_DURATION = "com.ricky.action.MUSIC_DURATION";
    public static final String UPDATE_ACTION = "com.ricky.action.UPDATE_ACTION";
    public static final String MUSIC_CURRENT = "com.ricky.action.MUSIC_CURRENT";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mp3Dao = new Mp3Dao();
        infoList = mp3Dao.GetMp3Infos(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int msg = intent.getIntExtra("msg", 0);
        url = intent.getStringExtra("url");
        listPosition = intent.getIntExtra("listPosition", 0);
        if (msg == AppConstant.PlayerMsg.PLAY_MSG) {
            play(0);
        } else if (msg == AppConstant.PlayerMsg.PAUSE_MSG) {
            pause();
        } else if (msg == AppConstant.PlayerMsg.STOP_MSG) {
            stop();
        } else if (msg == AppConstant.PlayerMsg.NEXT_MSG) {
            next();
        } else if (msg == AppConstant.PlayerMsg.CONTINUE_MSG) {
            resume();
        } else if (msg == AppConstant.PlayerMsg.PRIVIOUS_MSG) {
            previous();
        } else if (msg == AppConstant.PlayerMsg.PROGRESS_CHANGE) {
            currentTime = intent.getIntExtra("progress", 0);
            play(currentTime);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void next() {
        Log.i("player", "下一首");
        Intent intent = new Intent();
        intent.setAction(UPDATE_ACTION);
        intent.putExtra("listPosition", listPosition);
        sendBroadcast(intent);
        play(0);
    }

    private void previous() {
        Log.i("player", "上一首");
        Intent intent = new Intent();
        intent.setAction(UPDATE_ACTION);
        intent.putExtra("listPosition", listPosition);
        sendBroadcast(intent);
        play(0);
    }

    private void resume() {
        if (isPause) {
            Log.i("player", "继续");
            mediaPlayer.start();
            isPause = false;
            isPlay = true;
            Intent intent = new Intent();
            intent.setAction("playBtn");
            intent.putExtra("isPlay", isPlay);
            intent.putExtra("isPause", isPause);
            sendBroadcast(intent);
        }
    }

    private void play(int currentTime) {

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            if (currentTime > 0) {
                mediaPlayer.seekTo(currentTime);
            }
            mediaPlayer.setOnPreparedListener(this);
            handler.sendEmptyMessage(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.i("player", "暂停");
            mediaPlayer.pause();
            isPause = true;
            isPlay = false;
            Intent intent = new Intent();
            intent.setAction("playBtn");
            intent.putExtra("isPlay", isPlay);
            intent.putExtra("isPause", isPause);
            sendBroadcast(intent);
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        Intent intent = new Intent();
        intent.setAction(MUSIC_DURATION);
        intent.putExtra("duration", mediaPlayer.getDuration());
        sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        if (status == 3) {
            if (listPosition < infoList.size() - 1) {
                listPosition++;
                Intent intent = new Intent(UPDATE_ACTION);
                intent.putExtra("listPosition", listPosition);
                sendBroadcast(intent);
                url = infoList.get(listPosition).getUrl();
                play(0);
            } else {
                mediaPlayer.seekTo(0);
                listPosition = 0;
                Intent intent = new Intent(UPDATE_ACTION);
                intent.putExtra("listPosition", listPosition);
                sendBroadcast(intent);
                url = infoList.get(listPosition).getUrl();
                play(0);
            }
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition();
                    Intent intent = new Intent();
                    intent.setAction(MUSIC_CURRENT);
                    intent.putExtra("currentTime", currentTime);
                    sendBroadcast(intent);
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }
        }
    };

}