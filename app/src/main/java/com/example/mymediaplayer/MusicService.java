package com.example.mymediaplayer;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.LinearInterpolator;

/*
 * Created by 刘子恒 on 2017/8/23.
 */

public class MusicService extends Service {
    public final IBinder binder = new MyBinder();

    //关联Service和Activity
    class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public int isReturnTo = 0;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public static ObjectAnimator animator;

    public MusicService(Mp3info mp3info) {
        initMediaPlayer(mp3info);
    }

    public MusicService() {
        initMediaPlayer();
    }

    //初始化MediaPlayer:读取一个mp3文件并且设置为循环播放
    public void initMediaPlayer(Mp3info mp3info) {
        try {
            String path=mp3info.getUrl();

            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();                      //设置播放器进入prepare状态
            mediaPlayer.setLooping(true);               // 设置循环播放
        } catch (Exception e) {
            Log.d("hint","can't get to the song");
            e.printStackTrace();
        }
    }

    //初始化MediaPlayer:读取一个mp3文件并且设置为循环播放
    public void initMediaPlayer() {
        try {
            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/0test.mp3";

            mediaPlayer.setDataSource(file_path);
            mediaPlayer.prepare();                      //设置播放器进入prepare状态
            mediaPlayer.setLooping(true);               // 设置循环播放
        } catch (Exception e) {
            Log.d("hint","can't get to the song");
            e.printStackTrace();
        }
    }

    public  void AnimatorAction() {
        if (mediaPlayer.isPlaying()) {
            animator.setDuration(5000);                         //旋转一周所用时间
            animator.setInterpolator(new LinearInterpolator()); // 均速旋转
            animator.setRepeatCount(ValueAnimator.INFINITE);    // 无限循环
            animator.setRepeatMode(ValueAnimator.INFINITE);
            animator.start();
        }
    }

    //播放暂停按钮的功能实现
    private boolean isFirst=true;
    private int flag = 0;
    public static String which = "";
    public void playOrPause() {
        flag++;
        if (flag >= 1000) flag = 2;

        which = "pause";
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();        //暂停方法
            animator.pause();
        } else {
            mediaPlayer.start();        //启动文件播放

            if ( (flag==1) || (isReturnTo == 1)) {
                animator.setDuration(5000);                         //旋转一周所用时间
                animator.setInterpolator(new LinearInterpolator()); // 均速旋转
                animator.setRepeatCount(ValueAnimator.INFINITE);    // 无限循环
                animator.setRepeatMode(ValueAnimator.INFINITE);
                animator.start();
                isFirst=false;
            } else {
                animator.resume();                                  //继续
            }
        }
    }

    //播放列表歌曲
    public void playListSong(String listSongPath){
        flag++;
        if (flag >= 1000) flag = 2;

        if(!isFirst) {
            try {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.setDataSource(listSongPath);
                mediaPlayer.prepare();
                mediaPlayer.start();


                animator.resume();                                  //继续

            }catch (Exception e){
                Log.d("hint","list1");
                e.printStackTrace();
            }

        }else{
            isFirst=false;
            try {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.setDataSource(listSongPath);
                mediaPlayer.prepare();
                mediaPlayer.start();

                if ( (flag==1) || (isReturnTo == 1)) {
                    animator.setDuration(5000);                         //旋转一周所用时间
                    animator.setInterpolator(new LinearInterpolator()); // 均速旋转
                    animator.setRepeatCount(ValueAnimator.INFINITE);    // 无限循环
                    animator.setRepeatMode(ValueAnimator.INFINITE);
                    animator.start();
                    isFirst=false;
                } else {
                    animator.resume();                                  //继续
                }
            }catch (Exception e){
                Log.d("hint","list2");
                e.printStackTrace();
            }
        }
    }

    //播放在线歌曲
    public void playOnLineSong(String URL){
        flag++;
        if (flag >= 1000) flag = 2;

        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            mediaPlayer.stop();
        }
        try{
            mediaPlayer.reset();
            mediaPlayer.setDataSource(URL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();


        }catch (Exception e){
            e.printStackTrace();
        }
    }



    //实现停止功能
    public void stop() {
        which = "stop";
        animator.pause();
        if(mediaPlayer != null) {
            mediaPlayer.pause();            //防止点击停止按钮后没有马上停止，首先调用pause()方法
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);      //定位方法可以让播放器从指定位置开始播放
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
