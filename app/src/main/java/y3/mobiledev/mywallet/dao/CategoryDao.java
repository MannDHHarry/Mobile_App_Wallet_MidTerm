package y3.mobiledev.mywallet.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import y3.mobiledev.mywallet.models.Category;

@Dao
public interface CategoryDao {

    @Insert
    long insert(Category category);

    @Insert
    void insertAll(List<Category> categories);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    // Get active (non-archived) expense categories for user
    @Query("SELECT * FROM categories WHERE user_id = :userId AND is_income = 0 AND is_archived = 0 ORDER BY name")
    LiveData<List<Category>> getActiveExpenseCategories(int userId);

    // Get active (non-archived) income categories for user
    @Query("SELECT * FROM categories WHERE user_id = :userId AND is_income = 1 AND is_archived = 0 ORDER BY name")
    LiveData<List<Category>> getActiveIncomeCategories(int userId);

    // Get all categories (including archived) for management screen
    @Query("SELECT * FROM categories WHERE user_id = :userId AND is_income = 0 ORDER BY is_archived, name")
    LiveData<List<Category>> getAllExpenseCategories(int userId);

    @Query("SELECT * FROM categories WHERE user_id = :userId AND is_income = 1 ORDER BY is_archived, name")
    LiveData<List<Category>> getAllIncomeCategories(int userId);

    // Check if category name exists for user
    @Query("SELECT COUNT(*) FROM categories WHERE user_id = :userId AND name = :name AND is_income = :isIncome AND is_archived = 0")
    int checkCategoryExists(int userId, String name, boolean isIncome);

    // Get category by ID
    @Query("SELECT * FROM categories WHERE category_id = :categoryId")
    Category getCategoryById(int categoryId);

    // Soft delete (archive) category
    @Query("UPDATE categories SET is_archived = 1 WHERE category_id = :categoryId")
    void archiveCategory(int categoryId);

    // Unarchive category
    @Query("UPDATE categories SET is_archived = 0 WHERE category_id = :categoryId")
    void unarchiveCategory(int categoryId);

    // Check if category has transactions
    @Query("SELECT COUNT(*) FROM transactions WHERE category_id = :categoryId")
    int getTransactionCountForCategory(int categoryId);
}