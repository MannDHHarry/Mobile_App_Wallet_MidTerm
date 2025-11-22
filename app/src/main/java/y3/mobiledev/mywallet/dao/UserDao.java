package y3.mobiledev.mywallet.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import y3.mobiledev.mywallet.models.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE user_id = :userId")
    LiveData<User> getUserById(int userId);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);
}