package th.ac.mahidol.ramahospital.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Log {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(index = true)
    public String timestamp;
    public String info;
}
