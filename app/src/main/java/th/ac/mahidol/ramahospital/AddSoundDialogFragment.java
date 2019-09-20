package th.ac.mahidol.ramahospital;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.BundleCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.logging.Logger;


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

    private static final int FILECHOOSER_RESULTCODE=1;

    private OnFragmentInteractionListener mListener;
    private View view;
    private Uri uri;
    private String filename;

    public AddSoundDialogFragment() {
        // Required empty public constructor
    }

    public static AddSoundDialogFragment newInstance(String code, String sound) {
        AddSoundDialogFragment fragment = new AddSoundDialogFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_CODE, param1);
        //fragment.setArguments(args);
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
                getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                filename = getFileName(uri);
                Button button = view.findViewById(R.id.button2);
                button.setText(filename);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("audio/*");
        this.startActivityForResult(Intent.createChooser(i, "Sound Browser"), AddSoundDialogFragment.FILECHOOSER_RESULTCODE);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Bundle bundle);
    }

    private void initView(View view) {
        this.view = view;
        Button button = view.findViewById(R.id.button2);
        button.setOnClickListener(this);
    }

    private String getFileName(Uri uri) {
        String result = uri.toString();
        Log.e("cardioart", result);
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
        return result;
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
