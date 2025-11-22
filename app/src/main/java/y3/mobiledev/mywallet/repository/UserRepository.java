package y3.mobiledev.mywallet.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import y3.mobiledev.mywallet.CategoryTemplate;
import y3.mobiledev.mywallet.dao.CategoryDao;
import y3.mobiledev.mywallet.dao.UserDao;
import y3.mobiledev.mywallet.database.AppDatabase;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.User;

public class UserRepository {

    private UserDao userDao;
    private CategoryDao categoryDao;
    private AppDatabase database;

    public UserRepository(Application application) {
        database = AppDatabase.getInstance(application);
        userDao = database.userDao();
        categoryDao = database.categoryDao();
    }

    /**
     * Register a new user with default categories
     * This is an atomic transaction - either everything succeeds or nothing does
     */
    public User registerUser(String email, String name, String password) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                // 1. Check if email already exists
                if (userDao.checkEmailExists(email) > 0) {
                    throw new Exception("Email already exists");
                }

                // 2. Create and insert user
                User newUser = new User(email, name, password);
                long userId = userDao.insert(newUser);
                newUser.setUserId((int) userId);

                // 3. Create default categories for this user
                List<Category> defaultCategories = CategoryTemplate.createAllDefaultCategories((int) userId);
                categoryDao.insertAll(defaultCategories);

                // 4. Return the created user
                return newUser;
            }
        });

        try {
            return future.get(); // Wait for completion and return result
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Login user
     */
    public User loginUser(String email, String password) {
        Future<User> future = AppDatabase.databaseWriteExecutor.submit(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return userDao.login(email, password);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get user by ID (LiveData - reactive)
     */
    public LiveData<User> getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    /**
     * Update user
     */
    public void updateUser(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.update(user);
        });
    }

    /**
     * Check if email exists
     */
    public boolean checkEmailExists(String email) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return userDao.checkEmailExists(email) > 0;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
}