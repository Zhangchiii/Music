package com.ricky.music;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ricky.music.activities.PlayerAct;
import com.ricky.music.adapter.MyAdapter;
import com.ricky.music.bean.Mp3Info;
import com.ricky.music.dao.Mp3Dao;
import com.ricky.music.service.PlayerService;
import com.ricky.music.util.AppConstant;

import java.util.ArrayList;
import java.util.List;

public class IndexAct extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    List<Mp3Info> infoList;
    Mp3Dao mp3dao;

    IndexReceiver indexReceiver;

    private ImageButton playBtn;
    private ImageButton previousBtn;
    private ImageButton nextBtn;
    private TextView musicTitle;
    private TextView musicArtist;
    private ImageView photo;
    private RelativeLayout footLayout;

    private boolean isPlay;
    private boolean isPause;
    private boolean isFirstTime;

    private int listPosition = -1;
    private int currentTime;
    private int duration;

    public static final String UPDATE_ACTION = "com.ricky.action.UPDATE_ACTION";
    public static final String MUSIC_DURATION = "com.ricky.action.MUSIC_DURATION";
    public static final String MUSIC_CURRENT = "com.ricky.action.MUSIC_CURRENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_layout);
        init();

        indexReceiver = new IndexReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_ACTION);
        intentFilter.addAction(MUSIC_CURRENT);
        intentFilter.addAction(MUSIC_DURATION);
        intentFilter.addAction("playBtn");
        registerReceiver(indexReceiver, intentFilter);
    }

    private void init() {
        previousBtn = (ImageButton) findViewById(R.id.btn_previous);
        playBtn = (ImageButton) findViewById(R.id.btn_play);
        nextBtn = (ImageButton) findViewById(R.id.btn_next);
        musicTitle = (TextView) findViewById(R.id.music_title);
        musicArtist = (TextView) findViewById(R.id.music_artist);
        photo = (ImageView) findViewById(R.id.photo);
        footLayout = (RelativeLayout) findViewById(R.id.footer_layout);
        previousBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        footLayout.setOnClickListener(this);
        infoList = new ArrayList<>();
        mp3dao = new Mp3Dao();
        infoList = mp3dao.GetMp3Infos(this);
        ListView musicList = (ListView) findViewById(R.id.music_list);
        musicList.setAdapter(new MyAdapter(this, infoList));
        musicList.setOnItemClickListener(this);
        isFirstTime = true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


        if (infoList != null) {
            if (listPosition != i) {
                listPosition = i;
                isFirstTime = false;
                isPause = false;
                isPlay = true;
                playBtn.setImageResource(R.mipmap.pause);
                musicTitle.setText(infoList.get(listPosition).getTitle());
                musicArtist.setText(infoList.get(listPosition).getArtist());
                Bitmap bitmap = Mp3Dao.getArtWork(IndexAct.this, infoList.get(listPosition).getId(), infoList.get(listPosition).getAlbumId(), true, true);
                photo.setImageBitmap(bitmap);
                goService(listPosition, AppConstant.PlayerMsg.PLAY_MSG);
            } else {
                goActivity(listPosition);
            }
        }
    }

    private void goService(int listPosition, int msg) {

        Mp3Info mp3Info = infoList.get(listPosition);
        Intent intent = new Intent();
        intent.putExtra("isPlay", isPlay);
        intent.putExtra("isPause", isPause);
        intent.putExtra("listPosition", listPosition);
        intent.putExtra("url", mp3Info.getUrl());
        intent.putExtra("msg", msg);
        intent.setClass(IndexAct.this, PlayerService.class);
        startService(intent);
    }

    private void goActivity(int listPosition) {

        if (listPosition >= 0) {
            Mp3Info mp3Info = new Mp3Info();
            mp3Info = infoList.get(listPosition);
            Intent intent = new Intent();
            intent.setClass(IndexAct.this, PlayerAct.class);
            intent.putExtra("currentTime", currentTime);
            intent.putExtra("duration", duration);
            intent.putExtra("isPlay", isPlay);
            intent.putExtra("isPause", isPause);
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("title", mp3Info.getTitle());
            intent.putExtra("artist", mp3Info.getArtist());
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_play:
                if (isFirstTime) {
                    isFirstTime = false;
                    isPlay = true;
                    isPause = false;
                    listPosition = 0;
                    playBtn.setImageResource(R.mipmap.pause);
                    Mp3Info mp3Info = new Mp3Info();
                    mp3Info = infoList.get(listPosition);
                    musicTitle.setText(mp3Info.getTitle());
                    musicArtist.setText(mp3Info.getArtist());
                    Bitmap bitmap = Mp3Dao.getArtWork(IndexAct.this, infoList.get(listPosition).getId(), infoList.get(listPosition).getAlbumId(), true, true);
                    photo.setImageBitmap(bitmap);
                    Intent intent = new Intent();
                    intent.setClass(IndexAct.this, PlayerService.class);
                    intent.putExtra("listPosition", listPosition);
                    intent.putExtra("url", mp3Info.getUrl());
                    intent.putExtra("msg", AppConstant.PlayerMsg.PLAY_MSG);
                    startService(intent);
                } else if (isPlay) {
                    isPlay = false;
                    isPause = true;
                    isFirstTime = false;
                    playBtn.setImageResource(R.mipmap.play);
                    Intent intent = new Intent();
                    intent.setClass(IndexAct.this, PlayerService.class);
                    intent.putExtra("msg", AppConstant.PlayerMsg.PAUSE_MSG);
                    startService(intent);
                } else if (isPause) {
                    isPlay = true;
                    isPause = false;
                    playBtn.setImageResource(R.mipmap.pause);
                    Intent intent = new Intent();
                    intent.setClass(IndexAct.this, PlayerService.class);
                    intent.putExtra("msg", AppConstant.PlayerMsg.CONTINUE_MSG);
                    startService(intent);
                }
                break;
            case R.id.btn_next:
                next();
                isFirstTime = false;
                isPlay = true;
                isPause = false;
                break;
            case R.id.btn_previous:
                isFirstTime = false;
                isPlay = true;
                isPause = false;
                previous();
                break;
            case R.id.footer_layout:
                goActivity(listPosition);
                break;
        }
    }

    private void next() {

        if (listPosition < infoList.size() - 1) {
            listPosition = listPosition + 1;
            playBtn.setImageResource(R.mipmap.pause);
            goService(listPosition, AppConstant.PlayerMsg.NEXT_MSG);
        } else {
            Toast.makeText(IndexAct.this, "没有下一首歌啦！", Toast.LENGTH_SHORT).show();
        }
    }

    private void previous() {

        if (listPosition > 0) {
            listPosition = listPosition - 1;
            playBtn.setImageResource(R.mipmap.pause);
            goService(listPosition, AppConstant.PlayerMsg.PRIVIOUS_MSG);
        } else {
            Toast.makeText(IndexAct.this, "没有上一首歌啦", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("退出").setMessage("您确定要离开我了吗？").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    Intent intent = new Intent(IndexAct.this, PlayerService.class);
                    unregisterReceiver(indexReceiver);
                    stopService(intent);
                }
            }).show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public class IndexReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UPDATE_ACTION)) {
                listPosition = intent.getIntExtra("listPosition", 0);
                if (listPosition >= 0) {
                    musicTitle.setText(infoList.get(listPosition).getTitle());
                    musicArtist.setText(infoList.get(listPosition).getArtist());
                    Bitmap bitmap = Mp3Dao.getArtWork(context, infoList.get(listPosition).getId(), infoList.get(listPosition).getAlbumId(), true, true);
                    photo.setImageBitmap(bitmap);
                }
            } else if (action.equals(MUSIC_DURATION)) {
                duration = intent.getIntExtra("duration", 0);
            } else if (action.equals(MUSIC_CURRENT)) {
                currentTime = intent.getIntExtra("currentTime", 0);
            } else if (action.equals("playBtn")) {
                isPlay = intent.getBooleanExtra("isPlay", false);
                isPause = intent.getBooleanExtra("isPause", false);
                if (isPlay) {
                    playBtn.setImageResource(R.mipmap.pause);
                } else if (isPause) {
                    playBtn.setImageResource(R.mipmap.play);
                }
            }
        }
    }
}
