package com.ricky.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ricky.music.R;
import com.ricky.music.bean.Mp3Info;
import com.ricky.music.dao.Mp3Dao;

import java.util.List;

/**
 * Created by Ricky on 2016/6/20.
 */
public class MyAdapter extends BaseAdapter {

    private List<Mp3Info> mList;
    private LayoutInflater mInflater;
    private Mp3Info mp3Info;
    private Context context;

    public MyAdapter(Context context, List<Mp3Info> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.item, null);
            viewHolder.albumImage = (ImageView) view.findViewById(R.id.albumImage);
            viewHolder.title = (TextView) view.findViewById(R.id.music_title);
            viewHolder.artist = (TextView) view.findViewById(R.id.music_artist);
            viewHolder.duration = (TextView) view.findViewById(R.id.music_duration);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        mp3Info = mList.get(i);
        Bitmap bitmap = Mp3Dao.getArtWork(context, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
        viewHolder.albumImage.setImageBitmap(bitmap);
        viewHolder.title.setText(mp3Info.getTitle());
        viewHolder.artist.setText(mp3Info.getArtist());
        viewHolder.duration.setText(mp3Info.getDuration());
        return view;
    }

    class ViewHolder {
        public ImageView albumImage;
        public TextView title;
        public TextView artist;
        public TextView duration;
    }

}
