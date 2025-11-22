package y3.mobiledev.mywallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import y3.mobiledev.mywallet.fragments.LoginFragment;
import y3.mobiledev.mywallet.models.User;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";
    private AuthViewModel authViewModel;
    private TransactionViewModel transactionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialize ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Observe login status
        authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            if (isLoggedIn != null && isLoggedIn) {
                User user = authViewModel.getCurrentUser().getValue();
                if (user != null) {
                    Log.d(TAG, "User logged in: " + user.getEmail());
                    onLoginSuccess(user);
                }
            }
        });

        // Load login fragment by default
        if (savedInstanceState == null) {
            loadFragment(new LoginFragment());
        }
    }

    /**
     * Called when user successfully logs in or registers
     */
    private void onLoginSuccess(User user) {
        // Initialize TransactionViewModel with user data
        transactionViewModel.initializeUserData(user);

        // Navigate to MainActivity
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Load a fragment into the container
     */
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear any error messages
        if (authViewModel != null) {
            authViewModel.clearError();
        }
    }
}
