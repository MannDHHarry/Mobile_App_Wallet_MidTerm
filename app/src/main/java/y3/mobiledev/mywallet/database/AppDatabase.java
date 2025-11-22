package y3.mobiledev.mywallet.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import y3.mobiledev.mywallet.Converters;
import y3.mobiledev.mywallet.dao.CategoryDao;
import y3.mobiledev.mywallet.dao.TransactionDao;
import y3.mobiledev.mywallet.dao.UserDao;
import y3.mobiledev.mywallet.dao.WalletDao;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.User;
import y3.mobiledev.mywallet.models.Wallet;

@Database(
        entities = {
                User.class,
                Wallet.class,
                Category.class,
                Transaction.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    // Define DAO getters
    public abstract UserDao userDao();
    public abstract WalletDao walletDao();
    public abstract CategoryDao categoryDao();
    public abstract TransactionDao transactionDao();

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Thread pool for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Get database instance (Singleton pattern)
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mywallet_database"
                            )
                            // Uncomment for debugging - allows queries on main thread (NOT recommended for production)
                            // .allowMainThreadQueries()
                            .addCallback(roomCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback for database creation
     * Can be used to populate initial data
     */
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Database opened
        }
    };
}