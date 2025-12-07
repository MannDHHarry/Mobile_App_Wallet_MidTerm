package y3.mobiledev.mywallet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import y3.mobiledev.mywallet.fragments.ExchangeRateFragment;
import y3.mobiledev.mywallet.fragments.HomeFragment;
import y3.mobiledev.mywallet.fragments.StatisticsFragment;
import y3.mobiledev.mywallet.fragments.TransactionHistoryFragment;
import y3.mobiledev.mywallet.fragments.TransactionsTabFragment;
import y3.mobiledev.mywallet.fragments.TransferDialogFragment;



//Notification Import
import y3.mobiledev.mywallet.helpers.NotificationHelper;
import y3.mobiledev.mywallet.helpers.NotificationScheduler;
import y3.mobiledev.mywallet.helpers.NotificationDataManager;
import y3.mobiledev.mywallet.helpers.SubscriptionNotificationHelper;
import y3.mobiledev.mywallet.helpers.SubscriptionScheduler;
import y3.mobiledev.mywallet.models.User;

//Subscription Import
import y3.mobiledev.mywallet.fragments.SubscriptionFragment;

//Locale Import
import y3.mobiledev.mywallet.helpers.LocaleHelper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvUserName;
    private FloatingActionButton fabMain, fabAddTransaction, fabAddTransfer;
    private TextView tvAddTransaction, tvAddTransfer;
    private View dimOverlay;
    private boolean isFabMenuOpen = false;

    private BottomNavigationView bottomNavigation;

    private Fragment currentFragment;

    //Drawer Layout
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ImageButton btnMenu;

    private TransactionViewModel viewModel;
    private AuthViewModel authViewModel;
    private boolean isInitialized = false;


    private static final String PREFS_NAME = "welcome_prefs";
    private static final String PREF_WELCOME_SHOWN = "welcome_notification_shown";
    private static final String PREF_FRAGMENT_STATE = "saved_fragment_state";
    private static final String PREF_NAV_ITEM_ID = "saved_nav_item_id";

    //Permission Request laucher for notification

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("MainActivity", "Notification permission granted");
                    if (!hasShownWelcomeNotification()) {
                        NotificationHelper.showWelcomeNotification(this);
                        setWelcomeNotificationShown();
                    }
                    scheduleNotificationIfNeeded();
                } else {
                    Log.w("MainActivity", "Notification permission denied");
                }
            });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

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

        // THIRD: Load fragment AFTER data is ready - check for notification navigation first
        handleNotificationNavigation();

        Log.d("MainActivity", "Initial fragment loaded");
        scheduleNotificationIfNeeded();

        //Subscription Schedular
        initializeSubscriptionScheduler();

    }

    private void initializeSubscriptionScheduler() {
        SubscriptionNotificationHelper.createNotificationChannel(this);
        SubscriptionScheduler.scheduleDailyCheck(this);
        Log.d("MainActivity", "Subscription scheduler initialized");
    }

    private void handleNotificationNavigation() {
        String navigateTo = getIntent().getStringExtra("navigate_to");
        if ("statistics".equals(navigateTo)) {
            Log.d("MainActivity", "Navigating to Statistics from notification");
            loadFragment(new StatisticsFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_more);
            fabAddTransaction.hide();
            getIntent().removeExtra("navigate_to"); // Prevent re-navigation on config change
        } else {
            // Check if we need to restore fragment state after language change
            restoreFragmentState();
        }
    }
    
    private void restoreFragmentState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedNavItemId = prefs.getInt(PREF_NAV_ITEM_ID, -1);
        
        if (savedNavItemId != -1) {
            Log.d("MainActivity", "Restoring fragment state: nav_item_id=" + savedNavItemId);
            
            Fragment fragmentToLoad = null;
            boolean shouldShowFab = false;
            
            if (savedNavItemId == R.id.nav_home) {
                fragmentToLoad = new HomeFragment();
                shouldShowFab = true;
            } else if (savedNavItemId == R.id.nav_statistics) {
                fragmentToLoad = new TransactionHistoryFragment();
            } else if (savedNavItemId == R.id.nav_categories) {
                fragmentToLoad = new CategoriesFragment();
            } else if (savedNavItemId == R.id.nav_more) {
                fragmentToLoad = new StatisticsFragment();
            } else {
                // Check for drawer menu items
                String savedFragmentState = prefs.getString(PREF_FRAGMENT_STATE, null);
                if (savedFragmentState != null) {
                    if (savedFragmentState.equals(SubscriptionFragment.class.getName())) {
                        fragmentToLoad = new SubscriptionFragment();
                        shouldShowFab = false; // Hide FAB for SubscriptionFragment
                    } else if (savedFragmentState.equals(ExchangeRateFragment.class.getName())) {
                        fragmentToLoad = new ExchangeRateFragment();
                        shouldShowFab = false; // Hide FAB for ExchangeRateFragment
                    }
                }
            }
            
            if (fragmentToLoad != null) {
                loadFragment(fragmentToLoad);
                if (savedNavItemId != -1 && savedNavItemId != 0) {
                    bottomNavigation.setSelectedItemId(savedNavItemId);
                }
                if (shouldShowFab) {
                    fabMain.show();
                } else {
                    fabMain.hide();
                }
                
                // Clear saved state after restoration
                prefs.edit()
                    .remove(PREF_FRAGMENT_STATE)
                    .remove(PREF_NAV_ITEM_ID)
                    .apply();
                return;
            }
        }
        
        // Default: load Home fragment
        loadFragment(new HomeFragment());
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        // Handle notification click when app is already running
        String navigateTo = intent.getStringExtra("navigate_to");
        if ("statistics".equals(navigateTo) && isInitialized) {
            Log.d("MainActivity", "Navigating to Statistics from notification (app was running)");
            loadFragment(new StatisticsFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_more);
            fabAddTransaction.hide();
            intent.removeExtra("navigate_to");
        }
    }

    private void initViews() {

        tvUserName = findViewById(R.id.tvUserName);
        // FAB views
        fabMain = findViewById(R.id.fabMain);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        fabAddTransfer = findViewById(R.id.fabAddTransfer);
        tvAddTransaction = findViewById(R.id.tvAddTransaction);
        tvAddTransfer = findViewById(R.id.tvAddTransfer);
        dimOverlay = findViewById(R.id.dimOverlay);

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

            if (isFabMenuOpen) {
                closeFabMenu();
            }

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                fabMain.show();
            } else if (id == R.id.nav_statistics) {
                selectedFragment = new TransactionHistoryFragment();
                fabMain.hide();
            } else if (id == R.id.nav_categories) {
                selectedFragment = new CategoriesFragment();
                fabMain.hide();
            } else if (id == R.id.nav_more) {
                selectedFragment = new StatisticsFragment();
                fabMain.hide();
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
        fabMain.setOnClickListener(v -> toggleFabMenu());
        fabAddTransaction.setOnClickListener(v -> {
            closeFabMenu();
            navigateToAddTransaction();
        });
        fabAddTransfer.setOnClickListener(v -> {
            closeFabMenu();
            openTransferDialog();
        });
        dimOverlay.setOnClickListener(v -> closeFabMenu());
    }

    // Add these new methods for FAB animation:
    private void toggleFabMenu() {
        if (isFabMenuOpen) {
            closeFabMenu();
        } else {
            openFabMenu();
        }
    }

    private void openFabMenu() {
        isFabMenuOpen = true;

        // Show dim overlay
        dimOverlay.setVisibility(View.VISIBLE);
        dimOverlay.animate()
                .alpha(1f)
                .setDuration(200)
                .start();

        // Rotate main FAB
        fabMain.animate()
                .rotation(45f)
                .setDuration(200)
                .start();

        // Show and animate Transaction FAB
        fabAddTransaction.setVisibility(View.VISIBLE);
        fabAddTransaction.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .start();

        tvAddTransaction.setVisibility(View.VISIBLE);
        tvAddTransaction.animate()
                .alpha(1f)
                .setDuration(200)
                .start();

        // Show and animate Transfer FAB (with delay)
        fabAddTransfer.setVisibility(View.VISIBLE);
        fabAddTransfer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setStartDelay(50)
                .start();

        tvAddTransfer.setVisibility(View.VISIBLE);
        tvAddTransfer.animate()
                .alpha(1f)
                .setDuration(200)
                .setStartDelay(50)
                .start();
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;

        // Hide dim overlay
        dimOverlay.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> dimOverlay.setVisibility(View.GONE))
                .start();

        // Rotate main FAB back
        fabMain.animate()
                .rotation(0f)
                .setDuration(200)
                .start();

        // Hide Transaction FAB
        fabAddTransaction.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction(() -> fabAddTransaction.setVisibility(View.GONE))
                .start();

        tvAddTransaction.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> tvAddTransaction.setVisibility(View.GONE))
                .start();

        // Hide Transfer FAB
        fabAddTransfer.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(200)
                .withEndAction(() -> fabAddTransfer.setVisibility(View.GONE))
                .start();

        tvAddTransfer.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> tvAddTransfer.setVisibility(View.GONE))
                .start();
    }

    private void navigateToAddTransaction() {

        // Create an instance of your actual fragment
        AddTransactionFragment addTransactionFragment = new AddTransactionFragment();

        // Use a FragmentTransaction to display it
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, addTransactionFragment)
                // THIS IS CRUCIAL: It allows the user to press the back button to return
                .addToBackStack(null)
                .commit();
    }


    private void openTransferDialog() {
        TransferDialogFragment dialog = new TransferDialogFragment();
        dialog.show(getSupportFragmentManager(), "TransferDialog");
    }





    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_confirm))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {


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
                .setNegativeButton(getString(R.string.no), null)
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
                if (!hasShownWelcomeNotification()) {
                    NotificationHelper.showWelcomeNotification(this);
                    setWelcomeNotificationShown();
                }
                scheduleNotificationIfNeeded();
            }
        } else {
            if (!hasShownWelcomeNotification()) {
                showNotificationPermissionDialog();
            } else {
                scheduleNotificationIfNeeded();
            }
        }
    }

    private void showNotificationPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.enable_notifications))
                .setMessage(getString(R.string.notification_prompt))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    Log.d("MainActivity", "User enabled notifications");
                    NotificationHelper.showWelcomeNotification(this);
                    setWelcomeNotificationShown();
                    scheduleNotificationIfNeeded();
                })
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                    Log.d("MainActivity", "User declined notifications");
                    setWelcomeNotificationShown();
                })
                .setCancelable(false)
                .show();
    }

    private boolean hasShownWelcomeNotification() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREF_WELCOME_SHOWN, false);
    }

    private void setWelcomeNotificationShown() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_WELCOME_SHOWN, true).apply();
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
            fabMain.hide();
        } else if (id == R.id.nav_exchange_rates) {
            loadFragment(new ExchangeRateFragment());
            fabAddTransaction.hide();
        } else if (id == R.id.nav_language) {
            showLanguageDialog();
        } else if (id == R.id.nav_logout) {
            drawerLayout.closeDrawer(GravityCompat.START);
            showLogoutConfirmation();
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Tiếng Việt"};
        int currentSelection = LocaleHelper.isVietnamese(this) ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setSingleChoiceItems(languages, currentSelection, (dialog, which) -> {
                    String selectedLang = (which == 0) ? LocaleHelper.ENGLISH : LocaleHelper.VIETNAMESE;

                    if (!selectedLang.equals(LocaleHelper.getLanguage(this))) {
                        // Save current fragment state before recreating
                        saveFragmentState();
                        LocaleHelper.setLocale(this, selectedLang);
                        dialog.dismiss();
                        // Recreate activity to apply new language
                        recreate();
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    
    private void saveFragmentState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        if (currentFragment != null) {
            // Save fragment class name
            editor.putString(PREF_FRAGMENT_STATE, currentFragment.getClass().getName());
            Log.d("MainActivity", "Saving fragment state: " + currentFragment.getClass().getName());
            
            // Determine and save navigation item ID based on fragment type
            int navItemId = -1;
            if (currentFragment instanceof HomeFragment) {
                navItemId = R.id.nav_home;
            } else if (currentFragment instanceof TransactionHistoryFragment) {
                navItemId = R.id.nav_statistics;
            } else if (currentFragment instanceof CategoriesFragment) {
                navItemId = R.id.nav_categories;
            } else if (currentFragment instanceof StatisticsFragment) {
                navItemId = R.id.nav_more;
            }
            // SubscriptionFragment and ExchangeRateFragment don't have bottom nav items
            
            if (navItemId != -1) {
                editor.putInt(PREF_NAV_ITEM_ID, navItemId);
                Log.d("MainActivity", "Saving nav item ID: " + navItemId);
            }
        }
        
        editor.apply();
    }

}

