package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;

public class NotificationDataManager {

    private static final String TAG = "NotificationDataManager";
    private static final String PREFS_NAME = "notification_data_prefs";
    private static final String KEY_TRANSACTION_GROUPS = "transaction_groups_json";
    private static final String KEY_USER_ID = "current_user_id";

    private NotificationDataManager() {
        // Prevent instantiation
    }

    public static void saveTransactionGroups(@NonNull Context context,
                                             @NonNull List<TransactionGroup> groups,
                                             int userId) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            JSONArray jsonArray = groupListToJson(groups);
            editor.putString(KEY_TRANSACTION_GROUPS, jsonArray.toString());
            editor.putInt(KEY_USER_ID, userId);
            editor.apply();

            Log.d(TAG, "Saved " + groups.size() + " transaction group(s) for user ID: " + userId);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to serialize transaction groups", e);
        }
    }

    @NonNull
    public static List<TransactionGroup> loadTransactionGroups(@NonNull Context context) {
        String jsonString = getPrefs(context).getString(KEY_TRANSACTION_GROUPS, "[]");
        if ("[]".equals(jsonString)) {
            return Collections.emptyList();
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            List<TransactionGroup> groups = new ArrayList<>(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject groupJson = jsonArray.getJSONObject(i);
                String header = groupJson.optString("header", "Unknown Date");

                List<TransactionWithCategory> transactions = jsonToTransactionList(
                        groupJson.getJSONArray("transactions")
                );

                groups.add(new TransactionGroup(header, transactions));
            }

            Log.d(TAG, "Loaded " + groups.size() + " transaction group(s)");
            return groups;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse saved transaction groups", e);
            return Collections.emptyList();
        }
    }

    public static int getUserId(@NonNull Context context) {
        return getPrefs(context).getInt(KEY_USER_ID, -1);
    }

    public static boolean hasData(@NonNull Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.contains(KEY_TRANSACTION_GROUPS)
                && prefs.contains(KEY_USER_ID)
                && prefs.getInt(KEY_USER_ID, -1) != -1;
    }

    public static void clearData(@NonNull Context context) {
        getPrefs(context).edit()
                .clear()
                .apply();
        Log.d(TAG, "Cleared all notification cache data");
    }

   //Helper Functions

    private static SharedPreferences getPrefs(@NonNull Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    private static JSONArray groupListToJson(@NonNull List<TransactionGroup> groups) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (TransactionGroup group : groups) {
            JSONObject groupJson = new JSONObject();
            groupJson.put("header", group.getHeader());

            JSONArray transactionsJson = new JSONArray();
            List<TransactionWithCategory> transactions = group.getTransactions();
            if (transactions != null) {
                for (TransactionWithCategory t : transactions) {
                    transactionsJson.put(transactionToJson(t));
                }
            }
            groupJson.put("transactions", transactionsJson);
            jsonArray.put(groupJson);
        }
        return jsonArray;
    }

    @NonNull
    private static JSONObject transactionToJson(@NonNull TransactionWithCategory t) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("transactionId", t.getTransactionId());
        json.put("userId", t.getUserId());
        json.put("walletId", t.getWalletId());
        json.put("categoryId", t.getCategoryId());
        json.put("description", nullToEmpty(t.getDescription()));
        json.put("amount", t.getAmount());
        json.put("date", t.getDate());
        json.put("isExpense", t.isExpense());
        json.put("categoryName", nullToEmpty(t.getCategoryName()));
        json.put("categoryIcon", t.getCategoryIcon());
        json.put("categoryColor", t.getCategoryColor());
        return json;
    }

    @NonNull
    private static List<TransactionWithCategory> jsonToTransactionList(@Nullable JSONArray array) throws JSONException {
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        }

        List<TransactionWithCategory> list = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);
            TransactionWithCategory t = new TransactionWithCategory(
                    json.getInt("transactionId"),
                    json.getInt("userId"),
                    json.getInt("walletId"),
                    json.getInt("categoryId"),
                    json.optString("description", ""),
                    json.getDouble("amount"),
                    json.getLong("date"),
                    json.getBoolean("isExpense"),
                    json.optString("categoryName", "Unknown"),
                    json.optInt("categoryIcon", 0),
                    json.optInt("categoryColor", 0)
            );
            list.add(t);
        }
        return list;
    }

    @NonNull
    private static String nullToEmpty(@Nullable String value) {
        return value == null ? "" : value;
    }
}
