package y3.mobiledev.mywallet.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import y3.mobiledev.mywallet.dao.TransactionDao;
import y3.mobiledev.mywallet.dao.WalletDao;
import y3.mobiledev.mywallet.database.AppDatabase;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.models.Wallet;

public class TransactionRepository {

    private TransactionDao transactionDao;
    private WalletDao walletDao;

    public TransactionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        walletDao = database.walletDao();
    }

    /**
     * Get all transactions for user (LiveData - reactive)
     */
    public LiveData<List<Transaction>> getTransactionsByUser(int userId) {
        return transactionDao.getTransactionsByUser(userId);
    }

    /**
     * Get transactions by wallet
     */
    public LiveData<List<Transaction>> getTransactionsByWallet(int walletId) {
        return transactionDao.getTransactionsByWallet(walletId);
    }

    /**
     * Get transactions by category
     */
    public LiveData<List<Transaction>> getTransactionsByCategory(int categoryId) {
        return transactionDao.getTransactionsByCategory(categoryId);
    }

    /**
     * Add new transaction and update wallet balance
     * This is an atomic operation - both happen or neither happens
     */
    public Long addTransaction(Transaction transaction) {
        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                // 1. Insert transaction
                long transactionId = transactionDao.insert(transaction);

                // 2. Update wallet balance
                Wallet wallet = walletDao.getWalletById(transaction.getWalletId());
                if (wallet != null) {
                    double newBalance;
                    if (transaction.isExpense()) {
                        newBalance = wallet.getBalance() - transaction.getAmount();
                    } else {
                        newBalance = wallet.getBalance() + transaction.getAmount();
                    }
                    walletDao.updateBalance(wallet.getWalletId(), newBalance);
                }

                return transactionId;
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update transaction and adjust wallet balance
     */
    public boolean updateTransaction(Transaction oldTransaction, Transaction newTransaction) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // 1. Reverse old transaction effect on wallet
                Wallet oldWallet = walletDao.getWalletById(oldTransaction.getWalletId());
                if (oldWallet != null) {
                    double reversedBalance;
                    if (oldTransaction.isExpense()) {
                        reversedBalance = oldWallet.getBalance() + oldTransaction.getAmount();
                    } else {
                        reversedBalance = oldWallet.getBalance() - oldTransaction.getAmount();
                    }
                    walletDao.updateBalance(oldWallet.getWalletId(), reversedBalance);
                }

                // 2. Update transaction
                transactionDao.update(newTransaction);

                // 3. Apply new transaction effect on wallet
                Wallet newWallet = walletDao.getWalletById(newTransaction.getWalletId());
                if (newWallet != null) {
                    double newBalance;
                    if (newTransaction.isExpense()) {
                        newBalance = newWallet.getBalance() - newTransaction.getAmount();
                    } else {
                        newBalance = newWallet.getBalance() + newTransaction.getAmount();
                    }
                    walletDao.updateBalance(newWallet.getWalletId(), newBalance);
                }

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
     * Delete transaction and adjust wallet balance
     */
    public boolean deleteTransaction(Transaction transaction) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // 1. Reverse transaction effect on wallet
                Wallet wallet = walletDao.getWalletById(transaction.getWalletId());
                if (wallet != null) {
                    double newBalance;
                    if (transaction.isExpense()) {
                        // Was expense, add back to wallet
                        newBalance = wallet.getBalance() + transaction.getAmount();
                    } else {
                        // Was income, subtract from wallet
                        newBalance = wallet.getBalance() - transaction.getAmount();
                    }
                    walletDao.updateBalance(wallet.getWalletId(), newBalance);
                }

                // 2. Delete transaction
                transactionDao.delete(transaction);

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
     * Get transaction by ID
     */
    public Transaction getTransactionById(int transactionId) {
        Future<Transaction> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Transaction>() {
            @Override
            public Transaction call() throws Exception {
                return transactionDao.getTransactionById(transactionId);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<TransactionWithCategory>> getTransactionsWithCategoryByUser(int userId) {
        return transactionDao.getTransactionsWithCategoryByUser(userId);
    }

    public LiveData<List<TransactionWithCategory>> getTransactionsWithCategoryByWallet(int walletId) {
        return transactionDao.getTransactionsWithCategoryByWallet(walletId);
    }
}