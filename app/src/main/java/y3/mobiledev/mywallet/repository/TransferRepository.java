package y3.mobiledev.mywallet.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import y3.mobiledev.mywallet.dao.TransferDao;
import y3.mobiledev.mywallet.dao.WalletDao;
import y3.mobiledev.mywallet.database.AppDatabase;
import y3.mobiledev.mywallet.models.Transfer;
import y3.mobiledev.mywallet.models.TransferWithWallets;
import y3.mobiledev.mywallet.models.Wallet;

public class TransferRepository {

    private static final String TAG = "TransferRepository";
    private TransferDao transferDao;
    private WalletDao walletDao;

    public TransferRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transferDao = database.transferDao();
        walletDao = database.walletDao();
    }

    /**
     * Get all transfers for user with wallet names (LiveData)
     */
    public LiveData<List<TransferWithWallets>> getTransfersWithWalletsByUser(int userId) {
        return transferDao.getTransfersWithWalletsByUser(userId);
    }

    /**
     * Execute transfer (atomic operation)
     * Updates both wallet balances and inserts transfer record
     */
    public Long executeTransfer(Transfer transfer) {
        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                // Validate wallets exist
                Wallet fromWallet = walletDao.getWalletById(transfer.getFromWalletId());
                Wallet toWallet = walletDao.getWalletById(transfer.getToWalletId());

                if (fromWallet == null || toWallet == null) {
                    Log.e(TAG, "One or both wallets not found");
                    return null;
                }

                // Validate sufficient balance
                if (fromWallet.getBalance() < transfer.getAmount()) {
                    Log.e(TAG, "Insufficient balance in source wallet");
                    return null;
                }

                // Validate not same wallet
                if (transfer.getFromWalletId() == transfer.getToWalletId()) {
                    Log.e(TAG, "Cannot transfer to same wallet");
                    return null;
                }

                // Execute transfer atomically
                try {
                    // 1. Insert transfer record
                    long transferId = transferDao.insert(transfer);

                    // 2. Deduct from source wallet
                    double newFromBalance = fromWallet.getBalance() - transfer.getAmount();
                    walletDao.updateBalance(transfer.getFromWalletId(), newFromBalance);

                    // 3. Add to destination wallet
                    double newToBalance = toWallet.getBalance() + transfer.getAmount();
                    walletDao.updateBalance(transfer.getToWalletId(), newToBalance);

                    Log.d(TAG, "Transfer executed successfully: " + transferId);
                    return transferId;

                } catch (Exception e) {
                    Log.e(TAG, "Error executing transfer", e);
                    throw e; // Rollback transaction
                }
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Transfer execution failed", e);
            return null;
        }
    }

    /**
     * Delete transfer and reverse wallet balances
     */
    public boolean deleteTransfer(Transfer transfer) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    // 1. Get wallets
                    Wallet fromWallet = walletDao.getWalletById(transfer.getFromWalletId());
                    Wallet toWallet = walletDao.getWalletById(transfer.getToWalletId());

                    if (fromWallet == null || toWallet == null) {
                        Log.e(TAG, "Wallets not found for transfer deletion");
                        return false;
                    }

                    // 2. Reverse balances
                    // Add back to source wallet (was deducted)
                    double newFromBalance = fromWallet.getBalance() + transfer.getAmount();
                    walletDao.updateBalance(transfer.getFromWalletId(), newFromBalance);

                    // Subtract from destination wallet (was added)
                    double newToBalance = toWallet.getBalance() - transfer.getAmount();
                    walletDao.updateBalance(transfer.getToWalletId(), newToBalance);

                    // 3. Delete transfer record
                    transferDao.delete(transfer);

                    Log.d(TAG, "Transfer deleted and balances reversed");
                    return true;

                } catch (Exception e) {
                    Log.e(TAG, "Error deleting transfer", e);
                    throw e; // Rollback
                }
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Transfer deletion failed", e);
            return false;
        }
    }

    /**
     * Get transfer by ID
     */
    public Transfer getTransferById(int transferId) {
        Future<Transfer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Transfer>() {
            @Override
            public Transfer call() throws Exception {
                return transferDao.getTransferById(transferId);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Error getting transfer by ID", e);
            return null;
        }
    }
}