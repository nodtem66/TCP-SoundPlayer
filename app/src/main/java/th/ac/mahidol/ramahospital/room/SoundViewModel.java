package th.ac.mahidol.ramahospital.room;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class SoundViewModel extends AndroidViewModel {
    private SoundDatabase mDatabase;
    private SoundDAO mDAO;
    private List<Sound> mSounds;
    public SoundViewModel(Application application) {
        super(application);
        mDatabase = SoundDatabase.getDatabase(application);
        mDAO = mDatabase.soundDAO();
    }

    public LiveData<List<Sound>> getAllSounds() { return mDAO.getAllSounds(); }

    public void insert(Sound sound) {
        new DAOAsyncTask(mDAO, DAOAsyncTask.INSERT).execute(sound);
    }

    public void delete(Sound sound) {
        new DAOAsyncTask(mDAO, DAOAsyncTask.DELETE).execute(sound);
    }

    private static class DAOAsyncTask extends AsyncTask<Sound, Void, Void> {
        public static final int INSERT = 0;
        public static final int DELETE = 1;
        private SoundDAO mDAO;
        private int mType;
        public DAOAsyncTask(SoundDAO dao, int type) {
            mDAO = dao;
            mType = type;
        }
        @Override
        protected Void doInBackground(final Sound... sounds) {
            switch (mType) {
                case INSERT:
                    mDAO.insert(sounds[0]);
                    break;
                case DELETE:
                    mDAO.deleteSounds(sounds);
                    break;
            }
            return null;
        }
    }
}
