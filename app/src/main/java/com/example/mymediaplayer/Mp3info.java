package com.example.mymediaplayer;

/**
 * Created by 刘子恒 on 2017/8/28.
 * 用来存放歌曲信息的类
 */

public class Mp3info {
    private long id;
    private long album_id;
    private String title;//标题
    private String artist;//艺术家
    private long duration;
    private long size;
    private String url;   //路径
    private String album;
    private int isMusic;

    public void setId(long id){
        this.id = id;
    }

    public long getId(){return this.id;}

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle(){return this.title;}

    public void setArtist(String artist){
        this.artist = artist;
    }

    public String getArtist(){return this.artist;}

    public void setDuration(long duration){this.duration = duration;}

    public long getDuration(){return this.duration;}

    public void setSize(long size){this.size = size;}

    public long getSize(){return this.size;}

    public void setUrl(String url){this.url = url;}

    public String getUrl(){return this.url;}

    public void setAlbum(String album){this.album = album;}

    public String getAlbum(){return this.album;}

    public void setAlbum_id(long album_id){this.album_id = album_id;}

    public long getAlbum_id(){return this.album_id;}

}
