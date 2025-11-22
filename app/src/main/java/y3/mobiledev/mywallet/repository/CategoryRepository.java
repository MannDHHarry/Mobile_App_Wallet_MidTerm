package y3.mobiledev.mywallet.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import y3.mobiledev.mywallet.dao.CategoryDao;
import y3.mobiledev.mywallet.database.AppDatabase;
import y3.mobiledev.mywallet.models.Category;

public class CategoryRepository {

    private CategoryDao categoryDao;

    public CategoryRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        categoryDao = database.categoryDao();
    }

    // Get active categories (non-archived)
    public LiveData<List<Category>> getActiveExpenseCategories(int userId) {
        return categoryDao.getActiveExpenseCategories(userId);
    }

    public LiveData<List<Category>> getActiveIncomeCategories(int userId) {
        return categoryDao.getActiveIncomeCategories(userId);
    }

    // Get all categories (including archived) for management
    public LiveData<List<Category>> getAllExpenseCategories(int userId) {
        return categoryDao.getAllExpenseCategories(userId);
    }

    public LiveData<List<Category>> getAllIncomeCategories(int userId) {
        return categoryDao.getAllIncomeCategories(userId);
    }

    /**
     * Add a new custom category
     */
    public Long addCategory(Category category) {
        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                // Check if category with same name already exists
                if (categoryDao.checkCategoryExists(category.getUserId(), category.getName(), category.isIncome()) > 0) {
                    throw new Exception("Category already exists");
                }
                return categoryDao.insert(category);
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
     * Update category
     */
    public void updateCategory(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.update(category);
        });
    }

    /**
     * Archive category (soft delete)
     * Checks if category has transactions first
     */
    public boolean archiveCategory(int categoryId) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Check if category has transactions
                int transactionCount = categoryDao.getTransactionCountForCategory(categoryId);

                if (transactionCount > 0) {
                    // Has transactions - archive it
                    categoryDao.archiveCategory(categoryId);
                    return true;
                } else {
                    // No transactions - can actually delete it
                    Category category = categoryDao.getCategoryById(categoryId);
                    if (category != null) {
                        categoryDao.delete(category);
                    }
                    return true;
                }
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
     * Unarchive category
     */
    public void unarchiveCategory(int categoryId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.unarchiveCategory(categoryId);
        });
    }

    /**
     * Hard delete category (only if no transactions)
     */
    public boolean deleteCategory(int categoryId) {
        Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int transactionCount = categoryDao.getTransactionCountForCategory(categoryId);

                if (transactionCount > 0) {
                    // Cannot delete - has transactions
                    return false;
                } else {
                    Category category = categoryDao.getCategoryById(categoryId);
                    if (category != null) {
                        categoryDao.delete(category);
                        return true;
                    }
                    return false;
                }
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
     * Get transaction count for category
     */
    public int getTransactionCount(int categoryId) {
        Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return categoryDao.getTransactionCountForCategory(categoryId);
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