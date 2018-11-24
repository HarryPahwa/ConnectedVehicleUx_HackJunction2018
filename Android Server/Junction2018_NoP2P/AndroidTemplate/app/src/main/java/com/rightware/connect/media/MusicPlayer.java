package com.rightware.connect.media;

import android.content.Intent;
import android.os.Messenger;
import android.os.Message;
import android.os.Handler;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.media.AudioManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.net.URL;
import java.net.URI;

public class MusicPlayer implements OnCompletionListener, OnErrorListener
{
    private static final String TAG = "MusicPlayer";
    
    private Context m_context;
    private MediaPlayer m_mp;
    private Listener m_listener;
    private boolean m_prepared = false;

    public interface Listener {
        public void musicPlayComplete();
        public void musicPlayError();
    }

    public MusicPlayer(Context context, Listener listener) {
        m_context = context;
        m_mp = new MediaPlayer();
        m_mp.setWakeMode(m_context, PowerManager.PARTIAL_WAKE_LOCK);
        m_mp.setOnCompletionListener(this);
        m_mp.setOnErrorListener(this);
        m_mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                Log.d(TAG, "Prepared.");
                m_prepared = true;
                player.start();
            }
        });
        m_listener = listener;
    }
	
    public boolean playUri(String songUri) {
        try {
            URL url = new URL(songUri);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            url = uri.toURL();

            Log.i(TAG, "Play url: " + url.toString());

            m_mp.reset();
            m_mp.setDataSource(url.toString());
            m_mp.prepareAsync();
            m_prepared = false;
            
            return true;
        }
        catch (Exception e) {
            Log.w(TAG, "Play url exception: " + e.getMessage());
        }

        return false;
    }

    public void onCompletion(MediaPlayer mp) {
        if (m_listener != null) {
            m_listener.musicPlayComplete();
        }
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "Error occurred. what=" + what + " extra=" + extra);
        
        m_mp.reset();

        if (m_listener != null) {
            m_listener.musicPlayError();
        }
        m_prepared = false;

        return true;
    }

    public boolean play()
    {
        if (m_prepared) {
            m_mp.start();
        }
        return isPlaying();
    }

    public boolean pause()
    {
        m_mp.pause();
        return !isPlaying();
    }

    public boolean isPlaying()
    {
        return m_mp.isPlaying();
    }

    public boolean stop()
    {
        m_mp.stop();
        return !isPlaying();
    }

    public int getDuration()
    {
        if (m_prepared == false) {
            return 0;
        }
        return m_mp.getDuration();
    }

    public int getPosition()
    {
        if (m_prepared == false) {
            return 0;
        }
        return m_mp.getCurrentPosition();
    }

    public void seek(float pos)
    {
        if (pos < 0.0f)
        {
            pos = 0.0f;
        }
        if (pos > 1.0f)
        {
            pos = 1.0f;
        }

        m_mp.seekTo((int)(pos * m_mp.getDuration()));
    }
}