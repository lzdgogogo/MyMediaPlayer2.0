package com.example.mymediaplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by 刘子恒 on 2017/9/12.
 */

public class LricView extends TextView {

    private List<Lyric> lyricList;
    // 标记当前行
    private int currentLine = 0;
    private Paint currentPaint;
    private Paint otherPaint;
    private int currentColor = Color.GREEN;
    private int currentTextSize = 120;

    private int otherColor = Color.BLACK;
    private int otherTextSize = 80;

    // 行间距
    private int lineSpace = 140;
    //当前歌词字体
    private Typeface currentTypeface = Typeface.DEFAULT_BOLD;
    //其他歌词字体
    private Typeface otherTypeface = Typeface.SERIF;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            invalidate(); // 刷新,会再次调用onDraw方法
            super.handleMessage(msg);
        }

    };

    public LricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentPaint = new Paint();
        otherPaint = new Paint();
        lyricList = LrcUtils.readLRC(new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/0test.lrc"));//歌词文件路径

        currentPaint.setColor(currentColor);
        currentPaint.setTextSize(currentTextSize);
        currentPaint.setTextAlign(Paint.Align.CENTER); // 画在中间
        currentPaint.setTypeface(currentTypeface);

        otherPaint.setColor(otherColor);
        otherPaint.setTextSize(otherTextSize);
        otherPaint.setTextAlign(Paint.Align.CENTER);
        otherPaint.setTypeface(otherTypeface);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lyricList != null && currentLine < lyricList.size()) {
            Lyric lyric = null;
            //绘制播放过的歌词
            for (int i = currentLine - 1; i >= 0; i--) {
                lyric = lyricList.get(i);
                canvas.drawText(lyric.lricString, getWidth() / 2,
                        getHeight() / 2 + lineSpace * (i - currentLine), otherPaint);
            }
            lyric = lyricList.get(currentLine);
            // 绘制正在播放的歌词
            canvas.drawText(lyric.lricString, getWidth() / 2,
                    getHeight() / 2, currentPaint);
            //绘制未播放的歌词
            for (int i = currentLine + 1; i < lyricList.size(); i++) {
                lyric = lyricList.get(i);
                canvas.drawText(lyric.lricString, getWidth() / 2,
                        getHeight() / 2 + lineSpace * (i - currentLine), otherPaint);
            }
            lyric = lyricList.get(currentLine);
            handler.sendEmptyMessageDelayed(10, lyric.sleepTime);
            currentLine++;
        } else {
            canvas.drawText("未找到歌词", getWidth() / 2,
                    getHeight() / 2, currentPaint);
        }
        super.onDraw(canvas);
    }

}

