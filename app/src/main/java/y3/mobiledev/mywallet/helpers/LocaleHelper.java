package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "selected_language";
    
    public static final String ENGLISH = "en";
    public static final String VIETNAMESE = "vi";

    /**
     * Set and persist the app language
     */
    public static Context setLocale(Context context, String languageCode) {
        persist(context, languageCode);
        return updateResources(context, languageCode);
    }

    /**
     * Get the currently selected language code
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, ENGLISH); // Default to English
    }

    /**
     * Persist language selection
     */
    private static void persist(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    /**
     * Update the app resources with the new locale
     */
    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        
        return context.createConfigurationContext(config);
    }

    /**
     * Apply saved locale - call this in attachBaseContext of Activities
     */
    public static Context onAttach(Context context) {
        String lang = getLanguage(context);
        return setLocale(context, lang);
    }

    /**
     * Check if current language is Vietnamese
     */
    public static boolean isVietnamese(Context context) {
        return VIETNAMESE.equals(getLanguage(context));
    }

    /**
     * Get display name for current language
     */
    public static String getLanguageDisplayName(Context context) {
        return isVietnamese(context) ? "Tiếng Việt" : "English";
    }
}

