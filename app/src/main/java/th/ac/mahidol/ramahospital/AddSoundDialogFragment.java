package th.ac.mahidol.ramahospital;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddSoundDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddSoundDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddSoundDialogFragment extends DialogFragment implements View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String PARAM_CODE = "code";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_FILENAME = "filename";

    private static final int FILECHOOSER_RESULTCODE = 1;
    private static final int RECORDER_RESULTCODE = 2;

    private OnFragmentInteractionListener mListener;
    private View view;
    private TextView soundUriView;
    private Uri uri;
    private String filename;

    public AddSoundDialogFragment() {
        // Required empty public constructor
    }

    public static AddSoundDialogFragment newInstance() {
        AddSoundDialogFragment fragment = new AddSoundDialogFragment();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_sound_dialog, null);
        initView(view);
        builder.setTitle("Add New Sound");
        builder.setView(view);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendBundle();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // No-OP.
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (data != null) {
                uri = data.getData();
                filename = getFileName(uri);
                soundUriView.setText(filename);
            }
        } else if (requestCode == RECORDER_RESULTCODE) {
            if (resultCode == Activity.RESULT_OK) {
                soundUriView.setText(filename);
            } else {
                soundUriView.setText("Not found");
                filename = "";
                uri = null;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button2) {
            File file = new File(requireContext().getExternalFilesDir(null) + File.separator + "sounds");
            Intent i = new Intent(requireContext(), FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, file.getPath());
            startActivityForResult(i, AddSoundDialogFragment.FILECHOOSER_RESULTCODE);
        } else if (view.getId() == R.id.button3) {
            filename = (System.currentTimeMillis()/1000) + ".m4a";
            String filePath = requireContext().getExternalFilesDir(null) + File.separator + "sounds" + File.separator + filename;
            createFile(filePath);
            uri = Uri.fromFile(new File(filePath));
            AndroidAudioRecorder.with(this)
                    .setFilePath(filePath)
                    .setColor(getResources().getColor(R.color.colorAccent))
                    .setRequestCode(AddSoundDialogFragment.RECORDER_RESULTCODE)
                    .setSource(AudioSource.MIC)
                    .setChannel(AudioChannel.STEREO)
                    .setSampleRate(AudioSampleRate.HZ_8000)
                    .setKeepDisplayOn(true)
                    .recordFromFragment();
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bundle bundle);
    }

    private void initView(View view) {
        this.view = view;
        Button button = view.findViewById(R.id.button2);
        button.setOnClickListener(this);
        button = view.findViewById(R.id.button3);
        button.setOnClickListener(this);
        soundUriView = view.findViewById(R.id.sound_uri);
    }

    private String getFileName(Uri uri) {
        String result = uri.toString();
        // Query real name from media database
        if (uri.getScheme().equals("content")) {
            Cursor cursor = requireContext().getContentResolver().query(uri, new String[] {OpenableColumns.DISPLAY_NAME}, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(column_index);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        // Extract only the last path of URI
        if (result != null) {
            int cut = result.lastIndexOf('/');
            if (cut > -1) {
                result = result.substring(cut+1);
            }
        }

        try {
            result = URLDecoder.decode(result, "utf-8");
        } catch (UnsupportedEncodingException ignored) {}
        return result;
    }

    private void createFile(String filePath) {
        if (filePath != null) {
            File file = new File(filePath);
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        Timber.tag("create-file").d("The file was successfully created: %s", filePath);
                    } else {
                        Timber.tag("create-file").d("The file exists: %s", filePath);
                    }
                } catch (IOException e) {
                    Timber.tag("create-file").e("Failed to create file: %s", filePath);
                    Timber.tag("create-file").e(e);
                }
            } else {
                Timber.tag("create-file").e("Duplicate file: %s", filePath);
            }
            if (!file.canWrite()) {
                Timber.tag("create-file").e("Unwritable file: %s", filePath);
            }
        }
    }

    private void sendBundle() {
        if (mListener != null) {
            Bundle bundle = new Bundle();
            EditText editText = view.findViewById(R.id.editText);
            if (uri != null && editText.getText().length() > 0) {
                bundle.putString(PARAM_CODE, editText.getText().toString());
                bundle.putString(PARAM_PATH, uri.toString());
                bundle.putString(PARAM_FILENAME, getFileName(uri));
                mListener.onFragmentInteraction(bundle);
            } else {
                Toast.makeText(requireContext(), "Missing a code or a sound file", Toast.LENGTH_LONG).show();
            }
        }
    }
}
