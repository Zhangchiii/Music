package com.ricky.music.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ricky.music.R;
import com.ricky.music.bean.Mp3Info;
import com.ricky.music.dao.Mp3Dao;
import com.ricky.music.service.PlayerService;
import com.ricky.music.util.AppConstant;
import com.ricky.music.util.Common;
import com.ricky.music.util.ImageUtil;

import java.util.List;

/**
 * Created by Ricky on 2016/6/27.
 */
public class PlayerAct extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private TextView musicTitle;
    private TextView musicArtist;
    private ImageView musicAlbum;
    private ImageView musicAblumReflection;
    private Button previousBtn;
    private Button playBtn;
    private Button nextBtn;
    private SeekBar musicProgressBar;
    private TextView currentProgress;
    private TextView finalProgress;

    private int listPosition;
    private int currentTime;
    private List<Mp3Info> infoList;
    private boolean isPlay;
    private boolean isPause;

    private PlayReceiver playReceiver;

    public static final String UPDATE_ACTION = "com.ricky.action.UPDATE_ACTION";
    public static final String MUSIC_DURATION = "com.ricky.action.MUSIC_DURATION";
    public static final String MUSIC_CURRENT = "com.ricky.action.MUSIC_CURRENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_layout);
        init();
        Mp3Dao mp3Dao = new Mp3Dao();
        infoList = mp3Dao.GetMp3Infos(this);
        Mp3Info mp3Info=infoList.get(listPosition);
        showArtwork(mp3Info);
        playReceiver = new PlayReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_ACTION);
        intentFilter.addAction(MUSIC_DURATION);
        intentFilter.addAction(MUSIC_CURRENT);
        intentFilter.addAction("playBtn");
        registerReceiver(playReceiver, intentFilter);

    }

    private void init() {
        musicTitle = (TextView) findViewById(R.id.musicTitle);
        musicArtist = (TextView) findViewById(R.id.musicArtist);
        musicAlbum = (ImageView) findViewById(R.id.music_album);
        musicAblumReflection = (ImageView) findViewById(R.id.music_album_reflection);
        previousBtn = (Button) findViewById(R.id.previous_music);
        playBtn = (Button) findViewById(R.id.play_music);
        nextBtn = (Button) findViewById(R.id.next_music);
        musicProgressBar = (SeekBar) findViewById(R.id.music_progress_bar);
        currentProgress = (TextView) findViewById(R.id.current_progress);
        finalProgress = (TextView) findViewById(R.id.final_progress);
        previousBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        musicProgressBar.setOnSeekBarChangeListener(this);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        musicTitle.setText(bundle.getString("title"));
        musicArtist.setText(bundle.getString("artist"));
        currentProgress.setText(Common.formatTime(bundle.getInt("currentTime")));
        finalProgress.setText(Common.formatTime(bundle.getInt("duration")));
        musicProgressBar.setMax(bundle.getInt("duration"));
        musicProgressBar.setProgress(bundle.getInt("currentTime"));
        listPosition = bundle.getInt("listPosition");
        isPlay = bundle.getBoolean("isPlay");
        isPause = bundle.getBoolean("isPause");
        if (isPlay) {
            playBtn.setText("暂停");
        } else if (isPause) {
            playBtn.setText("播放");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.next_music:
                next();
                break;
            case R.id.previous_music:
                previous();
                break;
            case R.id.play_music:
                if (isPlay) {
                    isPlay = false;
                    isPause = true;
                    playBtn.setText("播放");
                    Intent intent = new Intent();
                    intent.setClass(PlayerAct.this, PlayerService.class);
                    intent.putExtra("msg", AppConstant.PlayerMsg.PAUSE_MSG);
                    startService(intent);

                } else if (isPause) {
                    isPlay = true;
                    isPause = false;
                    playBtn.setText("暂停");
                    Intent intent = new Intent();
                    intent.setClass(PlayerAct.this, PlayerService.class);
                    intent.putExtra("msg", AppConstant.PlayerMsg.CONTINUE_MSG);
                    startService(intent);
                }
                break;
        }
    }

    private void previous() {

        if (listPosition > 0) {
            playBtn.setText("暂停");
            listPosition = listPosition - 1;
            goService(listPosition, AppConstant.PlayerMsg.PRIVIOUS_MSG);
        } else {
            Toast.makeText(PlayerAct.this, "没有上一首歌啦", Toast.LENGTH_SHORT).show();
        }
    }

    private void next() {

        if (listPosition < infoList.size() - 1) {
            listPosition = listPosition + 1;
            playBtn.setText("暂停");
            goService(listPosition, AppConstant.PlayerMsg.NEXT_MSG);
        } else {
            Toast.makeText(PlayerAct.this, "没有下一首歌啦！", Toast.LENGTH_SHORT).show();
        }
    }

    private void goService(int listPosition, int msg) {
        Mp3Info mp3Info = infoList.get(listPosition);
        Intent intent = new Intent();
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("url", mp3Info.getUrl());
        intent.putExtra("msg", msg);
        intent.setClass(PlayerAct.this, PlayerService.class);
        startService(intent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) {
            Intent intent = new Intent();
            intent.setClass(PlayerAct.this, PlayerService.class);
            intent.putExtra("url", infoList.get(listPosition).getUrl());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("progress", i);
            intent.putExtra("msg", AppConstant.PlayerMsg.PROGRESS_CHANGE);
            startService(intent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public class PlayReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UPDATE_ACTION)) {
                listPosition = intent.getIntExtra("listPosition", 0);
                if (listPosition >= 0) {
                    musicTitle.setText(infoList.get(listPosition).getTitle());
                    musicArtist.setText(infoList.get(listPosition).getArtist());
                    Mp3Info mp3Info=infoList.get(listPosition);
                    showArtwork(mp3Info);
                }
            } else if (action.equals(MUSIC_DURATION)) {
                int duration = intent.getIntExtra("duration", 0);
                musicProgressBar.setMax(duration);
                finalProgress.setText(Common.formatTime(duration));
            } else if (action.equals(MUSIC_CURRENT)) {
                currentTime = intent.getIntExtra("currentTime", 0);
                currentProgress.setText(Common.formatTime(currentTime));
                musicProgressBar.setProgress(currentTime);
            } else if (action.equals("playBtn")) {
                isPlay = intent.getBooleanExtra("isPlay", false);
                isPause = intent.getBooleanExtra("isPause", false);
                if (isPlay) {
                    playBtn.setText("暂停");
                } else if (isPause) {
                    playBtn.setText("播放");
                }
            }
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(playReceiver);
        super.onStop();
    }

    private void showArtwork(Mp3Info mp3Info) {
        Bitmap bm = Mp3Dao.getArtWork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        //切换播放时候专辑图片出现透明效果
        Animation albumanim = AnimationUtils.loadAnimation(PlayerAct.this, R.anim.album_replace);
        //开始播放动画效果
        musicAlbum.startAnimation(albumanim);
        musicAlbum.setImageBitmap(bm);  //显示专辑封面图片
        musicAblumReflection.setImageBitmap(ImageUtil.createReflectionBitmapForSingle(bm)); //显示倒影
    }
}
