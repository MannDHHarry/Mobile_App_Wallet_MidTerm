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
import y3.mobiledev.mywallet.dao.TransferDao;
import y3.mobiledev.mywallet.dao.UserDao;
import y3.mobiledev.mywallet.dao.WalletDao;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Subscription;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.Transfer;
import y3.mobiledev.mywallet.models.User;
import y3.mobiledev.mywallet.models.Wallet;

@Database(
        entities = {
                User.class,
                Wallet.class,
                Category.class,
                Transaction.class,
                Subscription.class,
                Transfer.class  // ← NEW
        },
        version = 5,  // ← Increment from 4 to 5 (adds profile_picture_path to users)
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract WalletDao walletDao();
    public abstract CategoryDao categoryDao();
    public abstract TransactionDao transactionDao();
    public abstract SubscriptionDao subscriptionDao();
    public abstract TransferDao transferDao();  // ← NEW

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

    // Migration 3 → 4 (Transfers)
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create transfers table
            database.execSQL("CREATE TABLE transfers (" +
                    "transfer_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "from_wallet_id INTEGER NOT NULL, " +
                    "to_wallet_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "date INTEGER NOT NULL, " +
                    "created_at INTEGER NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(from_wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(to_wallet_id) REFERENCES wallets(wallet_id) ON DELETE CASCADE)");

            // Create indexes for performance
            database.execSQL("CREATE INDEX index_transfers_user_id ON transfers(user_id)");
            database.execSQL("CREATE INDEX index_transfers_from_wallet_id ON transfers(from_wallet_id)");
            database.execSQL("CREATE INDEX index_transfers_to_wallet_id ON transfers(to_wallet_id)");
            database.execSQL("CREATE INDEX index_transfers_date ON transfers(date)");
        }
    };

    // Migration 4 → 5 (Add profile_picture_path to users table)
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN profile_picture_path TEXT");
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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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