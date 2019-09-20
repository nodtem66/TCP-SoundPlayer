package th.ac.mahidol.ramahospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableRow;

import com.example.jean.jcplayer.view.JcPlayerView;

import java.util.ArrayList;
import java.util.List;

import th.ac.mahidol.ramahospital.room.Sound;
import th.ac.mahidol.ramahospital.room.SoundListAdapter;
import th.ac.mahidol.ramahospital.room.SoundViewModel;

public class SettingActivity extends AppCompatActivity implements AddSoundDialogFragment.OnFragmentInteractionListener, SoundListAdapter.OnPlayAudioListening {

    private SoundViewModel mSoundViewModel;

    private static final int REQUEST_PERMISSION = 1;

    private JcPlayerView playerView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_setting);

        RecyclerView recyclerView = findViewById(R.id.recycleview);
        TableRow emptyRow = findViewById(R.id.emptyTableRow);
        final SoundListAdapter adapter = new SoundListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        playerView = findViewById(R.id.jcplayer);
        button = findViewById(R.id.button);
        mSoundViewModel = ViewModelProviders.of(this).get(SoundViewModel.class);
        mSoundViewModel.getAllSounds().observe(this, (List<Sound> sounds) -> {
            if (!sounds.isEmpty()) {
                emptyRow.setVisibility(View.GONE);
            }
            adapter.setSounds(sounds);
        });
        adapter.setViewModel(mSoundViewModel);
        adapter.setPlayer(playerView);
        adapter.setListening(this);

        requestMyPermission();
    }

    @Override
    public void onFragmentInteraction(Bundle bundle) {
        Sound sound = new Sound();
        sound.code = bundle.getString(AddSoundDialogFragment.PARAM_CODE, null);
        sound.uri = bundle.getString(AddSoundDialogFragment.PARAM_PATH, null);
        sound.filename = bundle.getString(AddSoundDialogFragment.PARAM_FILENAME, null);
        mSoundViewModel.insert(sound);
    }

    @Override
    protected void onDestroy() {
        if (playerView != null) {
            playerView.kill();
        }
        super.onDestroy();
    }

    public void onBackButtonClicked(View view) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        startActivity(intent);
    }

    public void onAddButtonClicked(View view) {
        DialogFragment dialog = new AddSoundDialogFragment();
        dialog.show(getSupportFragmentManager(), "Sound Dialog");
    }

    private void requestMyPermission() {
        List<String> permission = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permission.isEmpty()) {
            ActivityCompat.requestPermissions(this, permission.toArray(new String[0]), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    @Override
    public void setPlayerVisibility(boolean isShow) {
        playerView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        button.setVisibility(isShow ? View.GONE : View.VISIBLE);
    }
}
