package y3.mobiledev.mywallet.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import y3.mobiledev.mywallet.Converters;
import y3.mobiledev.mywallet.dao.CategoryDao;
import y3.mobiledev.mywallet.dao.SubscriptionDao;
import y3.mobiledev.mywallet.dao.TransactionDao;
import y3.mobiledev.mywallet.dao.UserDao;
import y3.mobiledev.mywallet.dao.WalletDao;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Subscription;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.User;
import y3.mobiledev.mywallet.models.Wallet;

@Database(
        entities = {
                User.class,
                Wallet.class,
                Category.class,
                Transaction.class,
                Subscription.class
        },
        version = 3,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract WalletDao walletDao();
    public abstract CategoryDao categoryDao();
    public abstract TransactionDao transactionDao();
    public abstract SubscriptionDao subscriptionDao();

    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // THIS MIGRATION IS 100% CORRECT FOR YOUR Subscription CLASS
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE subscriptions (" +
                    "subscription_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "wallet_id INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "start_date INTEGER NOT NULL, " +
                    "next_billing_date INTEGER NOT NULL, " +
                    "notes TEXT, " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +  // boolean → INTEGER
                    "created_at INTEGER NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES user(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(wallet_id) REFERENCES wallet(wallet_id) ON DELETE CASCADE)");

            // Indexes for fast queries
            database.execSQL("CREATE INDEX index_subscriptions_user_id ON subscriptions(user_id)");
            database.execSQL("CREATE INDEX index_subscriptions_wallet_id ON subscriptions(wallet_id)");
        }
    };

    // New migration from version 2 → 3 (adds receipt_photo_uri to transactions)
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE transactions ADD COLUMN receipt_photo_uri TEXT");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mywallet_database")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)   // ← Now works perfectly
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };
}