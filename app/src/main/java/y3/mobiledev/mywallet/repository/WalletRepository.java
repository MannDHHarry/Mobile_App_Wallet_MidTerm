package y3.mobiledev.mywallet.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import y3.mobiledev.mywallet.dao.WalletDao;
import y3.mobiledev.mywallet.database.AppDatabase;
import y3.mobiledev.mywallet.models.Wallet;

public class WalletRepository {

    private WalletDao walletDao;
    public WalletRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        walletDao = database.walletDao();
    }

    public LiveData<List<Wallet>> getWalletsByUser(int userId) {
        return walletDao.getWalletsByUser(userId);
    }

    public LiveData<Wallet> getWalletById(int walletId) {
        return walletDao.getWalletByIdLive(walletId);
    }

    public Long addWallet(Wallet wallet) {
        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return walletDao.insert(wallet);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateWallet(Wallet wallet) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            walletDao.update(wallet);
        });
    }

    public boolean deleteWallet(Wallet wallet) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Check if this is the last wallet
                int walletCount = walletDao.getWalletCount(wallet.getUserId());

                if (walletCount <= 1) {
                    // Cannot delete last wallet
                    return false;
                }

                walletDao.delete(wallet);
                return true;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update wallet balance
     */
    public void updateBalance(int walletId, double newBalance) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            walletDao.updateBalance(walletId, newBalance);
        });
    }

    /**
     * Get transaction count for wallet
     */
    public int getTransactionCount(int walletId) {
        Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return walletDao.getTransactionCountForWallet(walletId);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Check if user has any wallets
     */
    public boolean hasWallets(int userId) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return walletDao.getWalletCount(userId) > 0;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get wallet count
     */
    public int getWalletCount(int userId) {
        Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return walletDao.getWalletCount(userId);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }
}