package y3.mobiledev.mywallet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import y3.mobiledev.mywallet.Models.User;

public class AuthViewModel extends ViewModel {
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Static holder to persist across ViewModelProvider instances
    private static User staticUser = null;
    private static boolean staticLoggedIn = false;

    private int nextUserId = 100;

    public AuthViewModel() {
        // Restore from static holder
        if (staticUser != null) {
            currentUser.setValue(staticUser);
            isLoggedIn.setValue(staticLoggedIn);
        }
    }

    public LiveData<User> getCurrentUser() { return currentUser; }
    public LiveData<Boolean> getIsLoggedIn() { return isLoggedIn; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void login(String email, String password) {
        isLoading.setValue(true);

        User user = null;
        if (email.equals("user1@test.com") && password.equals("password")) {
            user = new User(1, email, "Demo User", password);
        } else if (email.equals("user2@test.com") && password.equals("password")) {
            user = new User(2, email, "Test User", password);
        } else {
            errorMessage.setValue("Invalid email or password");
            isLoading.setValue(false);
            return;
        }

        // Save to static holder
        staticUser = user;
        staticLoggedIn = true;

        currentUser.setValue(user);
        isLoggedIn.setValue(true);
        errorMessage.setValue(null);

        isLoading.setValue(false);
    }

    public void register(String email, String password, String name) {
        isLoading.setValue(true);

        int newUserId = nextUserId++;
        User newUser = new User(newUserId, email, name, password);

        // Save to static holder
        staticUser = newUser;
        staticLoggedIn = true;

        currentUser.setValue(newUser);
        isLoggedIn.setValue(true);

        isLoading.setValue(false);
    }

    public void logout() {
        // Clear static holder
        staticUser = null;
        staticLoggedIn = false;

        currentUser.setValue(null);
        isLoggedIn.setValue(false);
        errorMessage.setValue(null);
    }
}
