package com.androidleaf.audiorecord;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.coremedia.iso.boxes.Container;
import com.gamedadi.idaddy.R;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import de.greenrobot.event.EventBus;

public class RecordAct extends Activity implements OnClickListener {

    /**
     * Status：录音初始状态
     */
    private static final int STATUS_PREPARE = 0;

    /**
     * Status：正在录音中
     */
    private static final int STATUS_RECORDING = 1;

    /**
     * Status：暂停录音
     */
    private static final int STATUS_PAUSE = 2;

    /**
     * Status：播放初始状态
     */
    private static final int STATUS_PLAY_PREPARE = 3;

    /**
     * Status：播放中
     */
    private static final int STATUS_PLAY_PLAYING = 4;
    /**
     * Status：播放暂停
     */
    private static final int STATUS_PLAY_PAUSE = 5;

    private int status = STATUS_PREPARE;

    /**
     * 录音时间
     */
    private TextView tvRecordTime;

    /**
     * 录音按钮
     */
    private ImageView btnRecord;// 录音按钮

    private PopupWindow popAddWindow;

    /**
     * 试听界面
     */
    private LinearLayout layoutListen;

    /**
     * 录音长度
     */
    private TextView tvLength;

    private TextView recordContinue;

    /**
     * 重置按钮
     */
    private View resetRecord;

    /**
     * 结束录音
     */
    private View recordOver;

    private ImageView audioRecordNextImage;

    private TextView audioRecordNextText;

    /**
     * 音频播放进度
     */
    private TextView tvPosition;

    long startTime = 0;

    /**
     * 最大录音长度
     */
    private static int MAX_LENGTH = 300 * 1000;

    private Handler handler = new Handler();

    private Runnable runnable;

    /**
     * 音频录音的总长度
     */
    private static int voiceLength;

    /**
     * 音频录音帮助类
     */
    private AudioRecordUtils mRecordUtils;

    /**
     * 播放进度条
     */
    private SeekBar seekBar;
    /**
     * 音频播放类
     */
    private Player player;
    /**
     * 录音文件名
     */
    private String audioRecordFileName;

    //视频
    private VideoView videoView;
    private MediaController mediaController;
    private final String VIDEO_FILE_NAME = "hopevideo.mp4";
    private String VIDEO_FILE;
    private final String MIX_FILE = "/sdcard/hope.mp4";
    private String SOUND_FILE;
    private int duration;
    private TextView totalTV;
    private LinearLayout recordLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_add_record);
        EventBus.getDefault().register(this);
        initView();
    }

    public void initView() {
        //视频
        recordLL = (LinearLayout) findViewById(R.id.recordLL);
        totalTV = (TextView) findViewById(R.id.totalTV);
        videoView = (VideoView) findViewById(R.id.videoView);
        //初始化mediaController
        mediaController = new MediaController(this);
        //将videoView与mediaController建立关联
        videoView.setMediaController(mediaController);
        //将mediaController与videoView建立关联
        mediaController.setMediaPlayer(videoView);

        //音频录音的文件名称
        audioRecordFileName = TimeUtils.getTimestamp();
        //初始化音频录音对象
        mRecordUtils = new AudioRecordUtils(this, audioRecordFileName);
        View view = LayoutInflater.from(this).inflate(R.layout.pop_add_record, null);
        tvRecordTime = (TextView) findViewById(R.id.tv_time);
        btnRecord = (ImageView) findViewById(R.id.iv_btn_record);
        btnRecord.setOnClickListener(this);
        recordContinue = (TextView) findViewById(R.id.record_continue_txt);
        resetRecord = findViewById(R.id.btn_record_reset);
        recordOver = findViewById(R.id.btn_record_complete);
        resetRecord.setOnClickListener(this);
        recordOver.setOnClickListener(this);
        audioRecordNextImage = (ImageView) findViewById(R.id.recrod_complete_img);
        audioRecordNextText = (TextView) findViewById(R.id.record_complete_txt);

        layoutListen = (LinearLayout) findViewById(R.id.layout_listen);
        tvLength = (TextView) findViewById(R.id.tv_length);
        tvPosition = (TextView) findViewById(R.id.tv_position);
        seekBar = (SeekBar) findViewById(R.id.seekbar_play);
        seekBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
        seekBar.setEnabled(false);
        player = new Player(seekBar, tvPosition);
        player.setMyPlayerCallback(new MyPlayerCallback() {

            @Override
            public void onPrepared() {
                seekBar.setEnabled(true);
            }

            @Override
            public void onCompletion() {
                status = STATUS_PLAY_PREPARE;
                seekBar.setEnabled(false);
                seekBar.setProgress(0);
                tvPosition.setText("00:00");
                recordContinue.setBackgroundResource(R.drawable.record_audio_play);
            }
        });

        popAddWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        popAddWindow.setFocusable(true);
        popAddWindow.setAnimationStyle(R.style.pop_anim);
        popAddWindow.setBackgroundDrawable(new BitmapDrawable());

        testCopy(this);
        initVideoView(VIDEO_FILE);
    }

    public void testCopy(Context context) {
        String path = context.getFilesDir().getAbsolutePath();
        String name = VIDEO_FILE_NAME;
        copy(context, name, path, name);
    }

    private void copy(Context myContext, String ASSETS_NAME, String savePath, String saveName) {
        String filename = savePath + "/" + saveName;
        File dir = new File(savePath);
        // 如果目录不中存在，创建这个目录
        if (!dir.exists())
            dir.mkdir();
        try {
            if (!(new File(filename)).exists()) {
                InputStream is = myContext.getResources().getAssets().open(ASSETS_NAME);
                FileOutputStream fos = new FileOutputStream(filename);
                byte[] buffer = new byte[7168];
                int count;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        VIDEO_FILE = filename;
        SOUND_FILE = FileUtils.getM4aFilePath(audioRecordFileName);
    }

    private void initVideoView(String fileName) {
        File vFile = new File(fileName);
        if (vFile.exists()) {//如果文件存在
            videoView.setVideoPath(vFile.getAbsolutePath());
            //让videoView获得焦点
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    duration = videoView.getDuration();
                    MAX_LENGTH = duration;
                    SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");//初始化Formatter的转换格式。
                    String hms = formatter.format(duration);
                    totalTV.setText(hms);
                }
            });
            Toast.makeText(this, "video  exists>>>", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "video does not exists...", Toast.LENGTH_LONG).show();
        }
    }

    private void playVideo(String fileName) {
        File vFile = new File(fileName);
        if (vFile.exists()) {//如果文件存在
            //让videoView获得焦点
            videoView.start();
            Toast.makeText(this, "video  exists>>>", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "video does not exists...", Toast.LENGTH_LONG).show();
        }
    }

    private void pauseVideo(String fileName) {
        File vFile = new File(fileName);
        if (vFile.exists()) {//如果文件存在
            videoView.pause();
            Toast.makeText(this, "video  exists>>>", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "video does not exists...", Toast.LENGTH_LONG).show();
        }
    }

    private void resumeVideo(String fileName) {
        File vFile = new File(fileName);
        if (vFile.exists()) {//如果文件存在
            videoView.start();
            Toast.makeText(this, "video  exists>>>", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "video does not exists...", Toast.LENGTH_LONG).show();
        }
    }

    private void mix() throws IOException {
        String audioEnglish = SOUND_FILE;
        String video = VIDEO_FILE;

        //test
        File vFile = new File(SOUND_FILE);
        if (vFile.exists()) {
            Toast.makeText(this, "声音文件存在", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "声音文件没有>>>", Toast.LENGTH_LONG).show();
        }

        vFile = new File(VIDEO_FILE);
        if (vFile.exists()) {
            Toast.makeText(this, "视频文件存在", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "视频文件没有>>>", Toast.LENGTH_LONG).show();
        }
        //

        Movie countVideo = MovieCreator.build(/*video*/audioEnglish);
        Movie countAudioEnglish = MovieCreator.build(/*audioEnglish*/video);
        Track audioTrack2English = countAudioEnglish.getTracks().get(0);
        countVideo.addTrack(audioTrack2English);
        Container out = new DefaultMp4Builder().build(countVideo);
        FileOutputStream fos = new FileOutputStream(new File(MIX_FILE));
        out.writeContainer(fos.getChannel());
        fos.close();
        Toast.makeText(this, "视频合成完成了>>>", Toast.LENGTH_LONG).show();
    }

    public void handleRecord() {
        switch (status) {
            case STATUS_PREPARE:
                mRecordUtils.startRecord();
                btnRecord.setBackgroundResource(R.drawable.record_round_red_bg);
                status = STATUS_RECORDING;
                voiceLength = 0;
                timing();
                playVideo(VIDEO_FILE);
                break;
            case STATUS_RECORDING:
                pauseAudioRecord();
                resetRecord.setVisibility(View.VISIBLE);
                recordOver.setVisibility(View.VISIBLE);
                btnRecord.setBackgroundResource(R.drawable.record_round_blue_bg);
                recordContinue.setVisibility(View.VISIBLE);
                status = STATUS_PAUSE;
                pauseVideo(VIDEO_FILE);
                break;
            case STATUS_PAUSE:
                mRecordUtils.startRecord();
                resetRecord.setVisibility(View.INVISIBLE);
                recordOver.setVisibility(View.INVISIBLE);
                btnRecord.setBackgroundResource(R.drawable.record_round_red_bg);
                recordContinue.setVisibility(View.INVISIBLE);
                status = STATUS_RECORDING;
                resumeVideo(VIDEO_FILE);
                timing();
                break;
            case STATUS_PLAY_PREPARE:
                player.playUrl(FileUtils.getM4aFilePath(audioRecordFileName));
                recordContinue.setBackgroundResource(R.drawable.record_audio_play_pause);
                status = STATUS_PLAY_PLAYING;
                playVideo(MIX_FILE);
                break;
            case STATUS_PLAY_PLAYING:
                player.pause();
                recordContinue.setBackgroundResource(R.drawable.record_audio_play);
                status = STATUS_PLAY_PAUSE;
                pauseVideo(MIX_FILE);
                break;
            case STATUS_PLAY_PAUSE:
                player.play();
                recordContinue.setBackgroundResource(R.drawable.record_audio_play_pause);
                status = STATUS_PLAY_PLAYING;
                resumeVideo(MIX_FILE);
                break;
        }
    }

    /**
     * 暂停录音
     */
    public void pauseAudioRecord() {
        mRecordUtils.pauseRecord();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    /**
     * 停止录音
     */
    public void stopAudioRecord() {
        pauseAudioRecord();
        mRecordUtils.stopRecord();
        status = STATUS_PLAY_PREPARE;
        showListen();
    }

    /**
     * 重新录音参数初始化
     */
    @SuppressLint("NewApi")
    public void resetAudioRecord() {
        //停止播放音频
        player.stop();
        pauseAudioRecord();
        mRecordUtils.reRecord();
        status = STATUS_PREPARE;
        voiceLength = 0;
        tvRecordTime.setTextColor(Color.WHITE);
        tvRecordTime.setText(TimeUtils.convertMilliSecondToMinute2(voiceLength));
        recordContinue.setText(R.string.record_continue);
        recordContinue.setBackground(null);
        recordContinue.setVisibility(View.GONE);
        layoutListen.setVisibility(View.GONE);
        recordLL.setVisibility(View.VISIBLE);
        audioRecordNextImage.setImageResource(R.drawable.btn_record_icon_complete);
        audioRecordNextText.setText(R.string.record_over);
        btnRecord.setBackgroundResource(R.drawable.record_round_blue_bg);
        resetRecord.setVisibility(View.INVISIBLE);
        recordOver.setVisibility(View.INVISIBLE);
    }

    /**
     * 计时功能
     */
    private void timing() {
        runnable = new Runnable() {
            @Override
            public void run() {
                voiceLength += 100;
                if (voiceLength >= (MAX_LENGTH - 10 * 1000)) {
                    tvRecordTime.setTextColor(getResources().getColor(
                            R.color.red_n));
                } else {
                    tvRecordTime.setTextColor(Color.WHITE);
                }
                if (voiceLength > MAX_LENGTH) {
                    stopAudioRecord();

                } else {
                    tvRecordTime.setText(TimeUtils.convertMilliSecondToMinute2(voiceLength));
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.postDelayed(runnable, 100);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.iv_btn_record:
                handleRecord();
                break;
            case R.id.btn_record_reset:
                resetAudioRecord();
                break;
            case R.id.btn_record_complete:
                stopAudioRecord();
                break;
            default:
                break;
        }
    }

    /**
     * 显示播放界面
     */
    private void showListen() {
        layoutListen.setVisibility(View.VISIBLE);
        tvLength.setText(TimeUtils.convertMilliSecondToMinute2(voiceLength));
        recordLL.setVisibility(View.GONE);
        resetRecord.setVisibility(View.VISIBLE);
        recordOver.setVisibility(View.INVISIBLE);
        recordContinue.setVisibility(View.VISIBLE);
        seekBar.setProgress(0);
        tvPosition.setText("00:00");
        btnRecord.setBackgroundResource(R.drawable.record_round_blue_bg);
        recordContinue.setText(null);
        recordContinue.setBackgroundResource(R.drawable.record_audio_play);

    }

    /**
     * SeekBar进度条改变事件监听类
     */
    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
        int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (null != player && player.mediaPlayer != null) {
                this.progress = progress * player.mediaPlayer.getDuration()
                        / seekBar.getMax();
                tvPosition.setText(TimeUtils
                        .convertMilliSecondToMinute2(player.currentPosition));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (player.mediaPlayer != null) {
                player.mediaPlayer.seekTo(progress);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.stop();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(FinishEncodeEvent event) {
        try {
            mix();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
