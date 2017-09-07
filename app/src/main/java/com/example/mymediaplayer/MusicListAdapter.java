package com.example.mymediaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 刘子恒 on 2017/8/28.
 * 歌曲列表适配器
 */

class ViewContainer{
    public ImageView albumImage;                             //这里是为了下面的使用方便,将很多个控件放在了一个类里面
    public TextView music_title;
    public TextView music_artist;
    public TextView music_duration;
    //public ImageButton list_down_button;
    //public LinearLayout linearLayout;
    //public TextView num;
}

public class MusicListAdapter extends BaseAdapter {

    private Context context;        //上下文对象引用
    private List<Mp3info> mp3Infos;   //存放Mp3Info引用的集合
    private Mp3info mp3Info;        //Mp3Info对象引用
    private int pos = -1;           //列表位置
    private ViewContainer vc;


    public MusicListAdapter(Context context,List<Mp3info> mp3Infos){
        this.context = context;
        this.mp3Infos = mp3Infos;
    }


    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        vc = null;
        if(convertView == null){
            vc = new ViewContainer();
            convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item, null);
            vc.music_title = (TextView)convertView.findViewById(R.id.music_title);
            vc.music_artist = (TextView)convertView.findViewById(R.id.music_Artist);
            vc.music_duration = (TextView)convertView.findViewById(R.id.music_duration);
            //vc.list_down_button = (ImageButton)convertView.findViewById(R.id.list_down_button);
            //vc.num=(TextView)convertView.findViewById(R.id.Num);

            convertView.setTag(vc);
        }
        else{
            vc = (ViewContainer)convertView.getTag();
        }
        mp3Info = mp3Infos.get(position);

        vc.music_title.setText(mp3Info.getTitle());         //显示标题
        vc.music_artist.setText(mp3Info.getArtist());       //显示艺术家
        vc.music_duration.setText(String.valueOf(formatTime(mp3Info.getDuration()))); //显示长度

        return convertView;
    }



    public static String formatTime(Long time){                     //将歌曲的时间转换为分秒的制度
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";

        if(min.length() < 2)
            min = "0" + min;
        switch (sec.length()){
            case 4:
                sec = "0" + sec;
                break;
            case 3:
                sec = "00" + sec;
                break;
            case 2:
                sec = "000" + sec;
                break;
            case 1:
                sec = "0000" + sec;
                break;
        }
        return min + ":" + sec.trim().substring(0,2);
    }
}

