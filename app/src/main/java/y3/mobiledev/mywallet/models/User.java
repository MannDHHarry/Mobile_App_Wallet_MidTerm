package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users",
        indices = {@Index(value = "email", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Primary Constructor - for Room Db
    public User(int userId, String email, String name, String password, long createdAt) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.password = password;
        this.createdAt = createdAt;
    }

    //Constructor for creating new users
    @Ignore
    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public int getUserId() { return userId; }

    public String getEmail() { return email; }

    public String getName() { return name; }

    public String getPassword() { return password; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }

    public void setEmail(String email) { this.email = email; }

    public void setName(String name) { this.name = name; }

    public void setPassword(String password) { this.password = password; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}