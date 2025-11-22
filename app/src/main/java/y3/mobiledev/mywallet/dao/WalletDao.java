package y3.mobiledev.mywallet.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import y3.mobiledev.mywallet.models.Wallet;

@Dao
public interface WalletDao {

    @Insert
    long insert(Wallet wallet);

    @Update
    void update(Wallet wallet);

    @Delete
    void delete(Wallet wallet);

    @Query("SELECT * FROM wallets WHERE user_id = :userId ORDER BY created_at")
    LiveData<List<Wallet>> getWalletsByUser(int userId);

    @Query("SELECT * FROM wallets WHERE wallet_id = :walletId")
    Wallet getWalletById(int walletId);

    @Query("SELECT * FROM wallets WHERE wallet_id = :walletId")
    LiveData<Wallet> getWalletByIdLive(int walletId);

    // Update wallet balance
    @Query("UPDATE wallets SET balance = :newBalance WHERE wallet_id = :walletId")
    void updateBalance(int walletId, double newBalance);

    // Get transaction count for wallet
    @Query("SELECT COUNT(*) FROM transactions WHERE wallet_id = :walletId")
    int getTransactionCountForWallet(int walletId);

    // Check if user has any wallets
    @Query("SELECT COUNT(*) FROM wallets WHERE user_id = :userId")
    int getWalletCount(int userId);
}