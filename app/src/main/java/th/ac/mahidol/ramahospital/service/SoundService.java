package th.ac.mahidol.ramahospital.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SoundService extends Service {

    public static final String SOUND_URI = "sound_uri";
    private MediaPlayer player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        closePlayer();
        String uri = intent.getStringExtra(SOUND_URI);
        if (uri != null && !uri.isEmpty()) {
            player = MediaPlayer.create(this, Uri.parse(uri));
            player.setLooping(false);
            player.start();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        closePlayer();
        super.onDestroy();
    }

    private void closePlayer() {
        if (player != null) {
            player.stop();
            player.release();
        }
    }
}
