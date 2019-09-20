package th.ac.mahidol.ramahospital.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Sound.class}, version = 2)
public abstract class SoundDatabase extends RoomDatabase {
    private static volatile SoundDatabase INSTANCE;
    public abstract SoundDAO soundDAO();
    static SoundDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SoundDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            SoundDatabase.class, "sound_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
