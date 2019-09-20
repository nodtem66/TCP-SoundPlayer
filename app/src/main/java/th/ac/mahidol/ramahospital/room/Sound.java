package th.ac.mahidol.ramahospital.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Sound {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "code", index = true)
    public String code;
    @ColumnInfo(name = "path")
    public String uri;
    @ColumnInfo(name = "filename")
    public String filename;
}
