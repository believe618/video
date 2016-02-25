package com.gamedadi.idaddy;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by DavidLee on 2016/2/25.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private Button startBtn, startRecordingBtn, stopRecordingBtn, playBtn, playAudioBtn;
    private boolean isRecording;
    private MediaRecorder mRecorder;
    private String mFileName;
    private final String SOUND_FILE = "/sdcard/hopeaudio.mp4";
    private String VIDEO_FILE;
    private final String VIDEO_FILE_NAME = "hopevideo.mp4";
    private final String MIX_FILE = "/sdcard/hope.mp4";
    private VideoView videoView;
    private MediaController mediaController;
    private String TAG = "TTT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        initView();
    }

    private void initView() {
        playBtn = (Button) findViewById(R.id.playBtn);
        playAudioBtn = (Button) findViewById(R.id.playAudioBtn);
        startRecordingBtn = (Button) findViewById(R.id.startRecordingBtn);
        stopRecordingBtn = (Button) findViewById(R.id.stopRecordingBtn);
        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(this);
        startRecordingBtn.setOnClickListener(this);
        stopRecordingBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        playAudioBtn.setOnClickListener(this);

        videoView = (VideoView) findViewById(R.id.videoView);
        //初始化mediaController
        mediaController = new MediaController(this);
        //将videoView与mediaController建立关联
        videoView.setMediaController(mediaController);
        //将mediaController与videoView建立关联
        mediaController.setMediaPlayer(videoView);
        testCopy(this);
    }

    private void playVideo() {
        File vFile = new File(MIX_FILE);
        if (vFile.exists()) {//如果文件存在
            videoView.setVideoPath(vFile.getAbsolutePath());
            //让videoView获得焦点
            videoView.requestFocus();
            Toast.makeText(this, "video  exists>>>", Toast.LENGTH_LONG).show();
            Log.i(TAG, "video  exists>>>");
        } else {
            Toast.makeText(this, "video does not exists...", Toast.LENGTH_LONG).show();
            Log.i(TAG, "video does not exists...");
        }
    }


    private void playAudio() {
        File vFile = new File(SOUND_FILE);
        if (vFile.exists()) {
            Toast.makeText(this, "声音文件存在><><><", Toast.LENGTH_LONG).show();
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(SOUND_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
//            mediaPlayer.stop();
//            mediaPlayer.release();
        } else {
            Toast.makeText(this, "声音文件不存在.....", Toast.LENGTH_LONG).show();
        }
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
    }

    private void startRecording() {
        if (isRecording) {
            stopRecord();
        }
        mFileName = SOUND_FILE;
        /*mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mRecorder.prepare();
        } catch (IOException e) {

        }
        mRecorder.start();
        isRecording = true;*/

        ///////
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(this, "prepare() failed", Toast.LENGTH_LONG).show();
        }
        mRecorder.start();
        isRecording = true;
    }

    private void stopRecord() {
        if (isRecording) {
            mRecorder.stop();
            mRecorder.release();
            isRecording = false;
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
        Track audioTrackEnglish = countAudioEnglish.getTracks().get(0);
        countVideo.addTrack(audioTrackEnglish);
        Container out = new DefaultMp4Builder().build(countVideo);
        FileOutputStream fos = new FileOutputStream(new File(MIX_FILE));
        out.writeContainer(fos.getChannel());
        fos.close();
        Toast.makeText(this, "视频合成完成了>>>", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.startRecordingBtn:
                startRecording();
                break;
            case R.id.stopRecordingBtn:
                stopRecord();
                break;
            case R.id.startBtn:
                try {
                    mix();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.playBtn:
                playVideo();
                break;
            case R.id.playAudioBtn:
                playAudio();
                break;
        }
    }
}
