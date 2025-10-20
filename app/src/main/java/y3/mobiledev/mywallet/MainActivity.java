package y3.mobiledev.mywallet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import y3.mobiledev.mywallet.Fragments.AddTransactionFragment;
import y3.mobiledev.mywallet.Fragments.CategoriesFragment;
import y3.mobiledev.mywallet.Fragments.HomeFragment;
import y3.mobiledev.mywallet.Fragments.StatisticsFragment;
import y3.mobiledev.mywallet.Fragments.TransactionHistoryFragment;

public class MainActivity extends AppCompatActivity {

    private TextView tvUserName;
    private ImageButton btnLogOut;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigation;

    private Fragment currentFragment;

    private TransactionViewModel viewModel;
    private AuthViewModel authViewModel;
    private boolean isInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate called");

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        Log.d("MainActivity", "Current user in MainActivity: " +
                (authViewModel.getCurrentUser().getValue() != null ?
                        authViewModel.getCurrentUser().getValue().getName() : "NULL"));

        initViews();
        setupListeners();
        setupBottomNavigation();

        // Always observe for changes
        authViewModel.getCurrentUser().observe(this, user -> {
            Log.d("MainActivity", "Observer fired - user: " + (user != null ? user.getName() : "null"));
            if (user != null && !isInitialized) {
                Log.d("MainActivity", "User logged in: " + user.getName() + " (userId: " + user.getUserId() + ")");
                initializeAndShow();
            } else if (user == null && isInitialized) {
                Log.d("MainActivity", "User logged out");
                redirectToAuth();
            }
        });
    }

    private void initializeAndShow() {
        if (isInitialized) return;

        isInitialized = true;

        Log.d("MainActivity", "initializeAndShow called");

        // FIRST: Initialize data
        viewModel.initializeUserData(authViewModel.getCurrentUser().getValue());
        Log.d("MainActivity", "Data initialized");

        // SECOND: Update UI
        tvUserName.setText(authViewModel.getCurrentUser().getValue().getName().toUpperCase());

        // THIRD: Load fragment AFTER data is ready
        loadFragment(new HomeFragment());
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        Log.d("MainActivity", "HomeFragment loaded");
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        btnLogOut = findViewById(R.id.btnLogout);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                fabAddTransaction.show();
            } else if (id == R.id.nav_statistics) {
                selectedFragment = new TransactionHistoryFragment();
                fabAddTransaction.hide();
            } else if (id == R.id.nav_categories) {
                selectedFragment = new CategoriesFragment();
                fabAddTransaction.hide();
            } else if (id == R.id.nav_more) {
                selectedFragment = new StatisticsFragment();
                fabAddTransaction.hide();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            currentFragment = fragment;
            return true;
        }
        return false;
    }

    private void setupListeners() {

        btnLogOut.setOnClickListener(v -> showLogoutConfirmation());
        fabAddTransaction.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new AddTransactionFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear user data
                    viewModel.clearUserData();
                    authViewModel.logout();

                    // Navigate to AuthActivity
                    Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void redirectToAuth() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}