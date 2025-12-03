package y3.mobiledev.mywallet.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionWithCategory;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByUser(int userId);

    @Query("SELECT * FROM transactions WHERE wallet_id = :walletId ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByWallet(int walletId);

    @Query("SELECT * FROM transactions WHERE category_id = :categoryId ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(int categoryId);

    @Query("SELECT * FROM transactions WHERE transaction_id = :transactionId")
    Transaction getTransactionById(int transactionId);

    // Get count for a category (for soft delete check)
    @Query("SELECT COUNT(*) FROM transactions WHERE category_id = :categoryId")
    int getTransactionCountByCategory(int categoryId);

    // Get count for a wallet
    @Query("SELECT COUNT(*) FROM transactions WHERE wallet_id = :walletId")
    int getTransactionCountByWallet(int walletId);

    @Query("SELECT t.transaction_id, t.user_id, t.wallet_id, t.category_id, " +
            "t.description, t.amount, t.date, t.is_expense, t.receipt_photo_uri, " +  // ← Add this
            "c.name as categoryName, c.icon_res_id as categoryIcon, c.color_res_id as categoryColor " +
            "FROM transactions t " +
            "INNER JOIN categories c ON t.category_id = c.category_id " +
            "WHERE t.user_id = :userId " +
            "ORDER BY t.date DESC")
    LiveData<List<TransactionWithCategory>> getTransactionsWithCategoryByUser(int userId);

    @Query("SELECT t.transaction_id, t.user_id, t.wallet_id, t.category_id, " +
            "t.description, t.amount, t.date, t.is_expense, t.receipt_photo_uri, " +  // ← Add this
            "c.name as categoryName, c.icon_res_id as categoryIcon, c.color_res_id as categoryColor " +
            "FROM transactions t " +
            "INNER JOIN categories c ON t.category_id = c.category_id " +
            "WHERE t.wallet_id = :walletId " +
            "ORDER BY t.date DESC")
    LiveData<List<TransactionWithCategory>> getTransactionsWithCategoryByWallet(int walletId);
}