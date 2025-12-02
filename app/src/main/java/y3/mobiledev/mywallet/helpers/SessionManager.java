package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "user_session_prefs";
    private static final String KEY_LOGGED_IN_USER_ID = "logged_in_user_id";
    private static final String KEY_REMEMBER_ME = "remember_me";

    private SessionManager() {
        // Prevent instantiation
    }

    /**
     * Save user session to SharedPreferences
     * @param context Application context
     * @param userId User ID to save
     * @param rememberMe Whether to remember the user
     */
    public static void saveSession(@NonNull Context context, int userId, boolean rememberMe) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        
        if (rememberMe) {
            editor.putInt(KEY_LOGGED_IN_USER_ID, userId);
            editor.putBoolean(KEY_REMEMBER_ME, true);
            Log.d(TAG, "Session saved for user ID: " + userId);
        } else {
            // Clear session if remember me is false
            editor.remove(KEY_LOGGED_IN_USER_ID);
            editor.putBoolean(KEY_REMEMBER_ME, false);
            Log.d(TAG, "Session not saved (Remember Me unchecked)");
        }
        editor.apply();
    }

    /**
     * Get saved user ID from SharedPreferences
     * @param context Application context
     * @return User ID if session exists, -1 otherwise
     */
    public static int getSavedUserId(@NonNull Context context) {
        return getPrefs(context).getInt(KEY_LOGGED_IN_USER_ID, -1);
    }

    /**
     * Check if a session is saved
     * @param context Application context
     * @return true if session exists, false otherwise
     */
    public static boolean isSessionSaved(@NonNull Context context) {
        SharedPreferences prefs = getPrefs(context);
        int userId = prefs.getInt(KEY_LOGGED_IN_USER_ID, -1);
        boolean rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false);
        return userId != -1 && rememberMe;
    }

    /**
     * Clear saved session from SharedPreferences
     * @param context Application context
     */
    public static void clearSession(@NonNull Context context) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit()
                .remove(KEY_LOGGED_IN_USER_ID)
                .putBoolean(KEY_REMEMBER_ME, false)
                .apply();
        Log.d(TAG, "Session cleared");
    }

    /**
     * Get SharedPreferences instance
     */
    private static SharedPreferences getPrefs(@NonNull Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

