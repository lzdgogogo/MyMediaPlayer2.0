package com.example.mymediaplayer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.mymediaplayer.MusicService.mediaPlayer;
import static com.example.mymediaplayer.MusicService.which;

public class MainActivity extends AppCompatActivity {

    private ImageButton isPlay;
    private ImageButton nextSong;
    private ImageButton preSong;

    private ImageView songAlbum;

    private TextView songName;
    private TextView singer;
    // private ObjectAnimator animator;

    private TextView totalTime;         //总时长
    private TextView playingTime;       //播放时长
    private TextView stateText;         //状态

    private SeekBar seekBar;            //进度条


    private ListView mListView;         //歌曲列表
    private List<Mp3info> mp3Infos;     //存放歌曲
    private int listPosition = 0; // 标识列表位置

    private ViewPager viewPager;
    private ArrayList<View> pageView;
    private TextView musicLayout;
    private TextView ListLayout;
    private ImageView scrollbar;
    private int offset=0;           //滚动条初始偏移量
    private int one;                //一倍滚动量
    private int currIndex;

    private EditText onLineSong;   //播放在线歌曲
    private String songURL="";            //在线歌曲的URL
    private DownUtil mSongDownUtil;    //下载
    private Button downLoadButton;

    private TextView lrcView;//歌词文件显示控件
    private LrcUtils lrcUtils;//处理歌词文件实例
    private List<Lyric> lrcInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);


        viewPager=(ViewPager) findViewById(R.id.viewPager);
        LayoutInflater inflater=getLayoutInflater();
        View view1=inflater.inflate(R.layout.music_layout,null);
        View view2=inflater.inflate(R.layout.lrc_layout,null);
        musicLayout=(TextView) findViewById(R.id.musicLayout);
        ListLayout=(TextView)findViewById(R.id.listlayout);
        scrollbar=(ImageView)findViewById(R.id.scrollbar);
        pageView=new ArrayList<>();

        pageView.add(view1);
        pageView.add(view2);
        viewPager.addView(view1);           //很重要，用来加载另外两个界面
        viewPager.addView(view2);
        PagerAdapter mPagerAdapter=new PagerAdapter() {
            //获取当前窗体界面数
            @Override
            public int getCount() {
                return pageView.size();
            }

            @Override
            //判断是否由对象生成界面
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }
            //使从ViewGroup中移出当前View
            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView(pageView.get(arg1));
            }

            //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
            public Object instantiateItem(View arg0, int arg1){
                ((ViewPager)arg0).addView(pageView.get(arg1));
                return pageView.get(arg1);
            }
        };
        viewPager.setAdapter(mPagerAdapter);//绑定适配器
        viewPager.setCurrentItem(0);//设置viewpager的初始界面为第一个界面
        int bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.stop).getWidth();

        DisplayMetrics displayMetrics = new DisplayMetrics();           //为了获取屏幕宽度，新建一个DisplayMetrics对象
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);        //将当前窗口的一些信息放在DisplayMetrics类中
        int screenW = displayMetrics.widthPixels;                                   //得到屏幕的宽度
        offset = (screenW / 2 - bmpW) / 2;                                          //计算出滚动条初始的偏移量
        one = offset * 2 + bmpW;                                                    //计算出切换一个界面时，滚动条的位移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        scrollbar.setImageMatrix(matrix);                                           //将滚动条的初始位置设置成与左边界间隔一个offset


        //ImageView songAlbum = (ImageView) findViewById(songAlbum);


        songName=(TextView)findViewById(R.id.songName);
        singer=(TextView)findViewById(R.id.singer);

        isPlay=(ImageButton) findViewById(R.id.playButton);
        isPlay.setOnClickListener(new myOnClickListener());

        nextSong=(ImageButton) findViewById(R.id.nextSongButton);
        nextSong.setOnClickListener(new myOnClickListener());

        preSong=(ImageButton)findViewById(R.id.preSongButton);
        preSong.setOnClickListener(new myOnClickListener());

        totalTime = (TextView) findViewById(R.id.totalTime);
        playingTime = (TextView) findViewById(R.id.playingTime);
        stateText = (TextView) findViewById(R.id.stateText);

        mListView = (ListView) findViewById(R.id.listView);
        FindSongs finder = new FindSongs();
        mp3Infos = finder.getMp3Infos(MainActivity.this.getContentResolver());
        finder.setListAdpter(getApplicationContext(),mp3Infos,mListView);

        onLineSong=(EditText)findViewById(R.id.onlineedit);
        downLoadButton=(Button) findViewById(R.id.downloadbutton);

        Mp3info mp3info=mp3Infos.get(listPosition);

        bindServiceConnection();
        musicService = new MusicService(mp3info);

        MusicService.animator = ObjectAnimator.ofFloat(songAlbum, "rotation", 0, 359);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekBar.setMax(mediaPlayer.getDuration());

        songAlbum=(ImageView)findViewById(R.id.songAlbum);

//        String path= Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/0test.lrc" ;//歌词文件地址
        lrcView=(TextView)findViewById(R.id.lrcView);
//        File f = new File(path);
//
//        lrcInfos=lrcUtils.readLRC(f);
//        lrcView.setText();

//        try{
//            Log.i("___lyric____","--begin--");
//            lrcInfo=lp.parser(path);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//        String allLrc="";
//        if(lrcInfo.getInfos()!=null) {
//            for (String tmp : lrcInfo.getInfos().values()) {
//                allLrc = allLrc + tmp;
//            }
//            lrcView.setText(allLrc);
//        }

        //songAlbum.setImageBitmap(mp3info.getAlbum());//处理专辑图
    }


    private MusicService musicService;
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private ServiceConnection sc = new ServiceConnection() {
        //传送在service的onBind（）中返回的IBinder
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyBinder) iBinder).getService();
        }

        //当service崩溃了等等发生连接意外丢失时调用
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
        }
    };

    //绑定Activity和Service
    private void bindServiceConnection() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, sc, BIND_AUTO_CREATE);//将Activity和Service进行绑定
    }


    //通过Handler管理UI界面
    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {

            //设置监听器

            mListView.setOnItemClickListener(new MusicListItemClickListener());                     //播放列表的监听器

            musicLayout.setOnClickListener(new myOnClickListener());                                //viewpager的监听器
            ListLayout.setOnClickListener(new myOnClickListener());

            viewPager.addOnPageChangeListener(new MyOnPageChangeListener());                        //切换界面监听器

            if(mediaPlayer.isPlaying()) {
                stateText.setText(R.string.playing);
            } else {
                if (which.equals("stop"))  {
                    stateText.setText(R.string.stop);
                } else if (which.equals("pause")){
                    stateText.setText(R.string.pause);
                }
            }

            playingTime.setText(time.format(mediaPlayer.getCurrentPosition()));
            totalTime.setText(time.format(mediaPlayer.getDuration()));

            //拖动进度条改变播放进度
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });


            //监听回车键,开始播放线上歌曲
            onLineSong.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    songURL=onLineSong.getText().toString().trim();

                    if(mediaPlayer.isPlaying()){
                        changeStop();
                    }
                    musicService.playOnLineSong(songURL);
                    if(mediaPlayer.isPlaying()) {
                        stateText.setText(R.string.playing);
                        isPlay.setBackgroundResource(R.drawable.pause);
                    }
                    return false;
                }
            });

            //songAlbum


            downLoadButton.setOnClickListener(new myOnClickListener());


            //int curTime=mediaPlayer.getCurrentPosition();

            //将所有歌词填充出来
//            Map<Long,String> lrc= lrcInfo.getInfos();
//            String allLrc="";
//            for(String tmp:lrc.values()){
//                allLrc=allLrc+tmp;
//            }
//            lrcView.setText(allLrc);

//            //循环变量类型  循环变量名称 ：被遍历对象
//            for(long key:lrc.keySet()){
//                if(key==curTime){
//                    lrcView.setText(lrc.get(key));
//                }
//            }


            setSongName();
            handler.postDelayed(runnable, 100);
        }
    };

    //播放页面的大部分按钮的监听器
    private class myOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.playButton:
                    changePlay();
                    musicService.playOrPause();
                    break;

                case R.id.nextSongButton:
                    nextSong();
                    break;

                case R.id.preSongButton:
                    preSong();
                    break;

                case R.id.musicLayout:
                    //点击"主页面“时切换到第一页
                    viewPager.setCurrentItem(0);
                    break;

                case R.id.listlayout:
                    //点击“播放器”时切换的第二页
                    viewPager.setCurrentItem(1);
                    break;

                case R.id.downloadbutton:
                    if(!songURL.isEmpty()){
                        downloadSong();
                    }else{
                        Toast.makeText(MainActivity.this,"please inset song's URL first!",Toast.LENGTH_SHORT-1).show();
                    }
                    break;
                default:
                    break;

                //                case R.id.pauseButton:
                //                    if(mediaPlayer.isPlaying()) {
                //                    Toast.makeText(MainActivity.this, "停止播放", Toast.LENGTH_SHORT).show();
                //                }
                //                    musicService.stop();
                //                    changeStop();
                //                    break;
                //
                //                case R.id.stopButton:
                //                    quit();
                //                    break;
            }
        }
    }

    //点击播放列表的事件
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
       @Override
       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            musicService.stop();
            changeStop();

            if(mp3Infos != null) {
                Mp3info mp3info = mp3Infos.get(position);

                Log.d("mp3Info-->",mp3info.toString());
                musicService.playListSong(mp3info.getUrl());
                stateText.setText(R.string.playing);
                isPlay.setBackgroundResource(R.drawable.pause);
                listPosition=position;
            }
        }
    }

    //下载歌曲文件
    private void downloadSong() {
        // 初始化DownUtil对象（最后一个参数指定线程数）
        mSongDownUtil = new DownUtil(songURL,Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/大城小爱.mp3", 1);

        new Thread() {
            @Override
            public void run() {
                try {
                    // 开始下载
                    mSongDownUtil.download();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //设置歌曲名称和歌手名称
    private void setSongName(){
        Mp3info mp3info=mp3Infos.get(listPosition);
        songName.setText(mp3info.getTitle());
        singer.setText(mp3info.getArtist());
    }

    //切换到下一首歌曲的事件
    private void nextSong(){
        musicService.stop();
        changeStop();

        if(mp3Infos!=null){
            if(listPosition==mp3Infos.size()-1){
                listPosition=-1;
            }
            Mp3info mp3info=mp3Infos.get(++listPosition);


            musicService.playListSong(mp3info.getUrl());
            stateText.setText(R.string.playing);
            isPlay.setBackgroundResource(R.drawable.pause);
        }
    }

    //切换回上一首歌的事件
    private void preSong(){
        musicService.stop();
        changeStop();

        if(mp3Infos!=null){
            if(listPosition==0){
                listPosition=mp3Infos.size();
            }
            Mp3info mp3info=mp3Infos.get(--listPosition);


            musicService.playListSong(mp3info.getUrl());
            stateText.setText(R.string.playing);
            isPlay.setBackgroundResource(R.drawable.pause);
        }
    }

    //切换页面事件
    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    /*
                     * TranslateAnimation的四个属性分别为
                     * float fromXDelta 动画开始的点离当前View X坐标上的差值
                     * float toXDelta 动画结束的点离当前View X坐标上的差值
                     * float fromYDelta 动画开始的点离当前View Y坐标上的差值
                     * float toYDelta 动画开始的点离当前View Y坐标上的差值
                     */
                    animation = new TranslateAnimation(one, 0, 0, 0);
                    break;
                case 1:
                    animation = new TranslateAnimation(offset, one, 0, 0);
                    break;
            }
            //arg0为切换到的页的编码
            currIndex = arg0;
            // 将此属性设置为true可以使得图片停在动画结束时的位置
            assert animation != null;
            animation.setFillAfter(true);
            //动画持续时间，单位为毫秒
            animation.setDuration(200);
            //滚动条开始动画
            scrollbar.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    private void changePlay() {

        if(mediaPlayer.isPlaying()){
            stateText.setText(R.string.pause);
            isPlay.setBackgroundResource(R.drawable.play);
        } else {
            stateText.setText(R.string.playing);
            isPlay.setBackgroundResource(R.drawable.pause);
        }
    }

    private void changeStop() {
        stateText.setText(R.string.stop);
        seekBar.setProgress(0);
        isPlay.setBackgroundResource(R.drawable.play);
    }

    private void quit() {
        MusicService.animator.end();
        handler.removeCallbacks(runnable);
        unbindService(sc);                             //解绑Service
        try {
            finish();                                   //将activity移出栈
            System.exit(0);                             //退出整个应用程序
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isApplicationBroughtToBackground()) {
            musicService.isReturnTo = 1;
            Log.e("b","后台中");
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        musicService.isReturnTo = 1;
    }

    @Override
    protected void onResume() {

        musicService.AnimatorAction();
        verifyStoragePermissions(this);

        if(mediaPlayer.isPlaying()) {
            stateText.setText(R.string.playing);
        } else {
            if (which.equals("stop"))  {
                stateText.setText(R.string.stop);
            } else if (which.equals("pause")){
                stateText.setText(R.string.pause);
            }
        }
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekBar.setMax(mediaPlayer.getDuration());
        handler.post(runnable);
        super.onResume();
        Log.d("hint", "handler post runnable");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unbindService(sc);
        super.onDestroy();
    }





    





    
    
    

    //判断程序是否处于后台
    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }





    
    
    
    
    
    
    
    

    // 动态权限申请
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
