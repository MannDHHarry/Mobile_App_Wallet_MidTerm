package y3.mobiledev.mywallet.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import y3.mobiledev.mywallet.models.Transfer;
import y3.mobiledev.mywallet.models.TransferWithWallets;

@Dao
public interface TransferDao {

    @Insert
    long insert(Transfer transfer);

    @Update
    void update(Transfer transfer);

    @Delete
    void delete(Transfer transfer);

    /**
     * Get all transfers for user with wallet names (for display)
     */
    @Query("SELECT t.transfer_id, t.user_id, t.from_wallet_id, t.to_wallet_id, " +
            "t.amount, t.date, " +
            "w1.name as from_wallet_name, w2.name as to_wallet_name " +
            "FROM transfers t " +
            "INNER JOIN wallets w1 ON t.from_wallet_id = w1.wallet_id " +
            "INNER JOIN wallets w2 ON t.to_wallet_id = w2.wallet_id " +
            "WHERE t.user_id = :userId " +
            "ORDER BY t.date DESC")
    LiveData<List<TransferWithWallets>> getTransfersWithWalletsByUser(int userId);

    /**
     * Get transfer by ID
     */
    @Query("SELECT * FROM transfers WHERE transfer_id = :transferId")
    Transfer getTransferById(int transferId);

    /**
     * Get transfer count for wallet (to prevent deletion if has transfers)
     */
    @Query("SELECT COUNT(*) FROM transfers " +
            "WHERE from_wallet_id = :walletId OR to_wallet_id = :walletId")
    int getTransferCountForWallet(int walletId);
}