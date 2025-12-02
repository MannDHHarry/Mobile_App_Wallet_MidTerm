package y3.mobiledev.mywallet;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import y3.mobiledev.mywallet.helpers.SessionManager;
import y3.mobiledev.mywallet.models.User;
import y3.mobiledev.mywallet.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private final ExecutorService executorService;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Static holder to persist across ViewModelProvider instances
    private static User staticUser = null;
    private static boolean staticLoggedIn = false;

    public AuthViewModel(@NonNull Application application) {
        super(application);

        // Initialize repository
        userRepository = new UserRepository(application);
        executorService = Executors.newSingleThreadExecutor();

        // Restore from static holder first (for same app session)
        if (staticUser != null) {
            currentUser.setValue(staticUser);
            isLoggedIn.setValue(staticLoggedIn);
        } else {
            // Try to restore from SharedPreferences (for app restart)
            // Note: restoreSession() will be called explicitly from AuthActivity
        }
    }

    // Getters
    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<Boolean> getIsLoggedIn() { return isLoggedIn; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    // User Log In
    public void login(String email, String password, boolean rememberMe) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        executorService.execute(() -> {
            try {

                if (email == null || email.trim().isEmpty()) {
                    postError("Email cannot be empty");
                    return;
                }

                if (password == null || password.trim().isEmpty()) {
                    postError("Password cannot be empty");
                    return;
                }

                // Authenticate with database
                User user = userRepository.loginUser(email.trim(), password);

                if (user != null) {
                    // Login successful - save to static holder
                    staticUser = user;
                    staticLoggedIn = true;
                    
                    // Save session to SharedPreferences if rememberMe is true
                    SessionManager.saveSession(getApplication(), user.getUserId(), rememberMe);
                    
                    currentUser.postValue(user);
                    isLoggedIn.postValue(true);
                    errorMessage.postValue(null);
                } else {

                    postError("Invalid email or password");
                }
            } catch (Exception e) {
                postError("Login failed: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

   // Register New User
    public void register(String email, String password, String name, boolean rememberMe) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        executorService.execute(() -> {
            try {
                // Validate inputs
                if (email == null || email.trim().isEmpty()) {
                    postError("Email cannot be empty");
                    return;
                }

                if (password == null || password.trim().isEmpty()) {
                    postError("Password cannot be empty");
                    return;
                }

                if (name == null || name.trim().isEmpty()) {
                    postError("Name cannot be empty");
                    return;
                }

                if (password.length() < 6) {
                    postError("Password must be at least 6 characters");
                    return;
                }

                // Check if email already exists
                if (userRepository.checkEmailExists(email.trim())) {
                    postError("Email already registered");
                    return;
                }

                // Register new user and Create Default Categories for this User
                User newUser = userRepository.registerUser(email.trim(), name.trim(), password);

                if (newUser != null) {
                    // Registration successful - save to static holder
                    staticUser = newUser;
                    staticLoggedIn = true;

                    // Save session to SharedPreferences if rememberMe is true
                    SessionManager.saveSession(getApplication(), newUser.getUserId(), rememberMe);

                    currentUser.postValue(newUser);
                    isLoggedIn.postValue(true);
                    errorMessage.postValue(null);
                } else {
                    postError("Registration failed. Please try again.");
                }
            } catch (Exception e) {
                postError("Registration failed: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Restore user session from SharedPreferences
     * Called on app startup if session exists
     */
    public void restoreSession() {
        if (SessionManager.isSessionSaved(getApplication())) {
            int userId = SessionManager.getSavedUserId(getApplication());
            if (userId != -1) {
                isLoading.setValue(true);
                executorService.execute(() -> {
                    try {
                        User user = userRepository.getUserByIdSync(userId);
                        if (user != null) {
                            // Restore user session
                            staticUser = user;
                            staticLoggedIn = true;
                            currentUser.postValue(user);
                            isLoggedIn.postValue(true);
                        } else {
                            // User not found in database, clear session
                            SessionManager.clearSession(getApplication());
                        }
                    } catch (Exception e) {
                        // Error restoring session, clear it
                        SessionManager.clearSession(getApplication());
                    } finally {
                        isLoading.postValue(false);
                    }
                });
            }
        }
    }

    public void logout() {
        // Clear static holder
        staticUser = null;
        staticLoggedIn = false;

        // Clear session from SharedPreferences
        SessionManager.clearSession(getApplication());

        currentUser.setValue(null);
        isLoggedIn.setValue(false);
        errorMessage.setValue(null);
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    //Helpers
    private void postError(String message) {
        errorMessage.postValue(message);
        isLoading.postValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}