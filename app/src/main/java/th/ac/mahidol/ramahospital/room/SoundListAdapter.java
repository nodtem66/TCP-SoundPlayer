package th.ac.mahidol.ramahospital.room;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.JcPlayerManagerListener;
import com.example.jean.jcplayer.general.JcStatus;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import th.ac.mahidol.ramahospital.R;
import th.ac.mahidol.ramahospital.utils.RealPathUtils;

public class SoundListAdapter extends RecyclerView.Adapter<SoundListAdapter.SoundViewHolder> {
    class SoundViewHolder extends RecyclerView.ViewHolder {
        private final TextView codeTextView;
        private final TextView filenameTextView;
        private final ImageButton deleteButton;

        private SoundViewHolder(View itemView) {
            super(itemView);
            codeTextView = itemView.findViewById(R.id.codeTextView);
            filenameTextView = itemView.findViewById(R.id.fileNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private final Context mContext;
    private LayoutInflater mInflater;
    private SoundViewModel viewModel;
    private List<Sound> mSounds = Collections.emptyList();
    private JcPlayerView player;
    private OnPlayAudioListening listening;

    public SoundListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public SoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycleview_setting_row_layout, parent, false);
        return new SoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundViewHolder holder, int position) {
        Sound sound = mSounds.get(position);
        holder.codeTextView.setText(sound.code);
        holder.filenameTextView.setText(sound.filename);
        // delete sound when click
        holder.deleteButton.setOnClickListener(view -> {
            viewModel.delete(sound);
            Toast.makeText(mContext, String.format("Delete sound: %s", sound.filename), Toast.LENGTH_LONG).show();
        });
        // Play sound when click
        holder.filenameTextView.setOnClickListener(view -> {
            try {
                Uri uri = Uri.parse(sound.uri);
                String path = RealPathUtils.getRealPath(mContext, uri);

                Log.e("realpath", path);
                Toast.makeText(mContext, String.format("Play sound: %s", path), Toast.LENGTH_LONG).show();

                if (player != null && player.isPlaying()) {
                    player.pause();
                }
                ArrayList<JcAudio> jcAudios = new ArrayList<>();
                jcAudios.add(JcAudio.createFromFilePath(sound.filename, path));
                player.initPlaylist(jcAudios, new JcPlayerManagerListener() {

                    @Override
                    public void onTimeChanged(@NotNull JcStatus jcStatus) {
                    }

                    @Override
                    public void onPreparedAudio(@NotNull JcStatus jcStatus) {
                    }

                    @Override
                    public void onPlaying(@NotNull JcStatus jcStatus) {
                    }

                    @Override
                    public void onCompletedAudio() {
                        listening.setPlayerVisibility(false);
                    }

                    @Override
                    public void onPaused(@NotNull JcStatus jcStatus) {
                        listening.setPlayerVisibility(false);
                    }

                    @Override
                    public void onContinueAudio(@NotNull JcStatus jcStatus) {
                        listening.setPlayerVisibility(false);
                    }

                    @Override
                    public void onStopped(@NotNull JcStatus jcStatus) {
                        listening.setPlayerVisibility(false);
                    }

                    @Override
                    public void onJcpError(@NotNull Throwable throwable) {
                        listening.setPlayerVisibility(false);
                    }
                });
                player.playAudio(jcAudios.get(0));
                listening.setPlayerVisibility(true);
            } catch (Exception ignore) {}
        });
    }

    @Override
    public int getItemCount() {
        return mSounds.size();
    }

    public void setSounds(List<Sound> sounds) {
        mSounds = sounds;
        notifyDataSetChanged();
    }

    public void setViewModel(SoundViewModel soundViewModel) {
        viewModel = soundViewModel;
    }

    public void setPlayer(JcPlayerView view) {
        player = view;
    }

    public void setListening(OnPlayAudioListening listening) {
        this.listening = listening;
    }

    public interface OnPlayAudioListening {
        void setPlayerVisibility(boolean isShow);
    }
}
