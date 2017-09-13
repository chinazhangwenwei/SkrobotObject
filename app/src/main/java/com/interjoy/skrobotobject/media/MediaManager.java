package com.interjoy.skrobotobject.media;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Title:
 * Description:
 * Company: 北京盛开互动科技有限公司
 * Tel: 010-62538800
 * Mail: support@interjoy.com.cn
 *
 * @author zhangwenwei
 * @date 2017/9/7
 */
public class MediaManager {

    private MediaPlayer mediaPlayer;
    private MediaListener mediaListener;

    public MediaManager() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);

//        mediaPlayer.setVolume(100.0f,100.0f);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                mp.stop();
                if (mediaListener != null) {
                    mediaListener.endPlay();

                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (mediaListener != null) {
                    mediaListener.errorPlay();
                }
                return false;
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (mediaListener != null) {
                    mediaListener.startPlay();
                }

                mp.start();
                mp.setVolume(1.0f, 1.0f);
                //                mp.setPlaybackSpped();
            }
        });

    }

    public void setMediaListener(MediaListener mediaListener) {
        this.mediaListener = mediaListener;
    }

    public void playMusic(String mp3Name) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mp3Name);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            if (mediaListener != null) {
                mediaListener.errorPlay();
            }
        }
    }
    public void playMusic(AssetManager assetManager, String mp3Name) {
        try {
            mediaPlayer.reset();
            AssetFileDescriptor afd = assetManager.openFd(mp3Name);
            mediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            if (mediaListener != null) {
                mediaListener.errorPlay();
            }
        }
    }


    public void destory() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

    }

    public interface MediaListener {
        void startPlay();

        void endPlay();

        void errorPlay();
    }

}
