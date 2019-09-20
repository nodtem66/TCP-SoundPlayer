package th.ac.mahidol.ramahospital.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SoundDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Sound sound);

    @Query("DELETE FROM sound")
    void deleteAll();

    @Query("SELECT * FROM sound ORDER BY id ASC")
    LiveData<List<Sound>> getAllSounds();

    @Delete
    void deleteSounds(Sound... sounds);
}
