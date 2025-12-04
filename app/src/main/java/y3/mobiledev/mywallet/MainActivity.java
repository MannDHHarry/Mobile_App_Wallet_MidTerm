package y3.mobiledev.mywallet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import y3.mobiledev.mywallet.fragments.AddTransactionFragment;
import y3.mobiledev.mywallet.fragments.CategoriesFragment;
import y3.mobiledev.mywallet.fragments.HomeFragment;
import y3.mobiledev.mywallet.fragments.StatisticsFragment;
import y3.mobiledev.mywallet.fragments.TransactionHistoryFragment;

//Notification Import
import y3.mobiledev.mywallet.helpers.NotificationHelper;
import y3.mobiledev.mywallet.helpers.NotificationScheduler;
import y3.mobiledev.mywallet.helpers.NotificationDataManager;
import y3.mobiledev.mywallet.helpers.SubscriptionNotificationHelper;
import y3.mobiledev.mywallet.helpers.SubscriptionScheduler;
import y3.mobiledev.mywallet.models.User;

//Subscription Import
import y3.mobiledev.mywallet.fragments.SubscriptionFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvUserName;
    private FloatingActionButton fabAddTransaction;
    private BottomNavigationView bottomNavigation;

    private Fragment currentFragment;

    //Drawer Layout
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageButton btnMenu;

    private TransactionViewModel viewModel;
    private AuthViewModel authViewModel;
    private boolean isInitialized = false;


    //Permission Request laucher for notification

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("MainActivity", "Notification permission granted");
                    scheduleNotificationIfNeeded();
                } else {
                    Log.w("MainActivity", "Notification permission denied");
                }
            });

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
        requestNotificationPermission();

        if (NotificationDataManager.hasData(this)) {
            Log.d("MainActivity", "App restarted - rescheduling notification");
            NotificationScheduler.scheduleDailyNotification(this);
        }

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

        User user = authViewModel.getCurrentUser().getValue();
        if (user == null) {
            Log.e("MainActivity", "User is null in initializeAndShow!");
            redirectToAuth();
            return;
        }

        // FIRST: Initialize data
        viewModel.initializeUserData(user);
        Log.d("MainActivity", "Data initialized");

        // SECOND: Update UI
        tvUserName.setText(authViewModel.getCurrentUser().getValue().getName().toUpperCase());

        setupDrawer();
        updateNavHeader(user);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // THIRD: Load fragment AFTER data is ready
        loadFragment(new HomeFragment());
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        Log.d("MainActivity", "HomeFragment loaded");
        scheduleNotificationIfNeeded();

        //Subscription Schedular
        initializeSubscriptionScheduler();

    }

    private void initializeSubscriptionScheduler() {
        SubscriptionNotificationHelper.createNotificationChannel(this);
        SubscriptionScheduler.scheduleDailyCheck(this);
        Log.d("MainActivity", "Subscription scheduler initialized");
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnMenu = findViewById(R.id.btnMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);


    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateNavHeader(User user) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView tvNavUserName = headerView.findViewById(R.id.tvNavUserName);
            TextView tvNavUserEmail = headerView.findViewById(R.id.tvNavUserEmail);

            if (tvNavUserName != null) {
                tvNavUserName.setText(user.getName());
            }
            if (tvNavUserEmail != null) {
                tvNavUserEmail.setText(user.getEmail());
            }
        }
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


                    NotificationScheduler.cancelDailyNotification(this);
                    NotificationDataManager.clearData(this);
                    Log.d("MainActivity", "Notifications cancelled and data cleared");

                    SubscriptionScheduler.cancelDailyCheck(this);
                    Log.d("MainActivity", "Subscription checks cancelled");


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

    //Notif helper function
    private void scheduleNotificationIfNeeded() {
        // Create notification channel (safe to call multiple times)
        NotificationHelper.createNotificationChannel(this);

        // Check if already scheduled
        if (!NotificationScheduler.isNotificationScheduled(this)) {
            NotificationScheduler.scheduleDailyNotification(this);
            Log.d("MainActivity", "Daily notification scheduled for: " +
                    NotificationScheduler.getNextScheduledTimeString(this));
        } else {
            Log.d("MainActivity", "Notification already scheduled for: " +
                    NotificationScheduler.getNextScheduledTimeString(this));
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Requesting notification permission");
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d("MainActivity", "Notification permission already granted");
            }
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            loadFragment(new HomeFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else if (id == R.id.nav_profile) {
            // TODO: Open profile fragment or activity
            // loadFragment(new ProfileFragment());
        } else if (id == R.id.nav_subscriptions){
            loadFragment(new SubscriptionFragment());
            fabAddTransaction.hide();
        }
        else if (id == R.id.nav_logout) {
            drawerLayout.closeDrawer(GravityCompat.START);
            showLogoutConfirmation();
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}

