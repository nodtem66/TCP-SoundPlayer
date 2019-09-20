package th.ac.mahidol.ramahospital;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.example.jean.jcplayer.JcPlayerManagerListener;
import com.example.jean.jcplayer.general.JcStatus;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import th.ac.mahidol.ramahospital.room.Sound;
import th.ac.mahidol.ramahospital.room.SoundViewModel;
import th.ac.mahidol.ramahospital.service.WifiApControl;
import th.ac.mahidol.ramahospital.thread.TCPServerThread;
import th.ac.mahidol.ramahospital.utils.RealPathUtils;

public class FullscreenActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int STATE_STOP = 0;
    public static final int STATE_START = 1;

    private static final int REQUEST_PERMISSION = 1;

    private ImageButton actionButton;
    private ImageButton settingButton;
    private TextView textView;

    private int state = STATE_STOP;
    private Map<String, Uri> soundMap;

    private Timer timer;
    private TCPServerThread thread;
    private JcPlayerView player;
    private ConcurrentLinkedQueue<String> queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_fullscreen);

        actionButton = findViewById(R.id.imageButton2);
        settingButton = findViewById(R.id.imageButton);
        textView = findViewById(R.id.textView);
        player = findViewById(R.id.jcplayer);

        actionButton.setOnClickListener(this);

        queue = new ConcurrentLinkedQueue<>();
        soundMap = new HashMap<>();

        requestMyPermission();
        querySoundsFromDatabase();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageButton2:
                if (state == STATE_STOP) {
                    startServer();
                } else if (state == STATE_START) {
                    stopServer();
                }
                break;
            case R.id.textView:
                if (state == STATE_START) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TetherSettings"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.kill();
        }
        super.onDestroy();
    }

    public void onSettingButtonClicked(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * Run on click the green play button
     */
    private void startServer() {
        if (!WifiApControl.isSupported()) {
            Toast.makeText(this, "Error: Not support for Access Point", Toast.LENGTH_LONG).show();
            return;
        }
        if (soundMap.isEmpty()) {
            Toast.makeText(this, "Empty sound. You need to go 'Setting' and add a sound.", Toast.LENGTH_LONG).show();
            return;
        }
        setupWifi();
        runTimer();
    }

    /**
     * Run on click the yellow stop button
     */
    private void stopServer() {
        Toast.makeText(this, "Stop Wifi server", Toast.LENGTH_SHORT).show();
        if (thread != null) {
            try {
                thread.stopServer();
                thread.interrupt();
                thread.join();
                thread = null;
            } catch (Exception ignored) {Log.e("cardioart", ignored.toString());}
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        queue.clear();
        state = STATE_STOP;
        actionButton.setImageResource(R.drawable.ic_btn1);
        textView.setText("\nTAP TO START\n");
        textView.setOnClickListener(null);
    }

    /**
     * Check the Hotspot connectivity and start TCP server thread
     */
    private void setupWifi() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (invokeHiddenMethod(wifiManager, "isWifiApEnabled", Boolean.class)) {
            textView.setText(String.format(
                    "IP: %s\nPort: 8000",
                    getLocalIpAddress()

            ));
            textView.setOnClickListener(this);
            if (thread != null) {
                try {
                    thread.stopServer();
                    thread.interrupt();
                    thread.join();
                    thread = null;
                } catch (Exception ignored) {Log.e("cardioart", ignored.toString());}
            }
            thread = new TCPServerThread(queue);
            thread.setDaemon(true);
            thread.start();
            state = STATE_START;
            actionButton.setImageResource(R.drawable.ic_btn2);
        } else {
            Toast.makeText(this, "Error: You should connect WIFI and set up HOTSPOT", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Construct and run timer to read code from queue and start sound-player service
     */
    private void runTimer() {
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    final String code = queue.poll();
                    Log.e("play-sound", code);
                    if (soundMap.containsKey(code)) {
                        Uri uri = soundMap.get(code);
                        if (player != null && player.isPlaying()) {
                            player.pause();
                        }
                        player.playAudio(JcAudio.createFromFilePath(code, RealPathUtils.getRealPath(getApplicationContext(), uri)));
                    }
                } catch (Exception ignored) {}
            }
        }, 100, 300);
    }

    /**
     * Request the permission from user
     */
    private void requestMyPermission() {
        List<String> permissions = new ArrayList<>();
        String[] targetPermissions = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MEDIA_CONTENT_CONTROL,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        };
        for (String permission: targetPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_PERMISSION);
        }
    }

    /**
     * Query the sound setting from sqlite database via Room persistance library
     */
    private void querySoundsFromDatabase() {
        SoundViewModel mSoundViewModel = ViewModelProviders.of(this).get(SoundViewModel.class);
        mSoundViewModel.getAllSounds().observe(this, (List<Sound> sounds) -> {
            if (sounds.isEmpty()) {
                Toast.makeText(this, "Warning: Empty setting\nAt 'Setting', you should go to add sound", Toast.LENGTH_LONG).show();
            } else {
                for (Sound sound : sounds) {
                    soundMap.put(sound.code, Uri.parse(sound.uri));
                }
                Toast.makeText(this, String.format("Loaded %d sounds", sounds.size()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper function to get IP Address
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        return null;
    }

    // Helper function to get the grey-list non-SDK API
    private <T> T invokeHiddenMethod(WifiManager wifiManager, String name, Class<T> t) {
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
        for (Method method: wmMethods) {
            if (method.getName().equalsIgnoreCase(name)) {
                try {
                    return t.cast(method.invoke(wifiManager));
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
