package y3.mobiledev.mywallet;

import java.util.ArrayList;
import java.util.List;

import y3.mobiledev.mywallet.models.Category;

// Template that creates Default Categories for every new user
public class CategoryTemplate {


    public static List<Category> createDefaultExpenseCategories(int userId) {
        List<Category> categories = new ArrayList<>();

        categories.add(new Category(
                userId, "Food",
                R.drawable.ic_launcher_foreground,
                R.color.category_orange,
                false, true // isIncome=false, isDefault=true
        ));

        categories.add(new Category(
                userId, "Transport",
                R.drawable.ic_launcher_foreground,
                R.color.category_blue,
                false, true
        ));

        categories.add(new Category(
                userId, "Utilities",
                R.drawable.ic_launcher_foreground,
                R.color.category_orange,
                false, true
        ));

        categories.add(new Category(
                userId, "Shopping",
                R.drawable.ic_launcher_foreground,
                R.color.category_teal,
                false, true
        ));

        categories.add(new Category(
                userId, "Entertainment",
                R.drawable.ic_launcher_foreground,
                R.color.category_purple,
                false, true
        ));

        categories.add(new Category(
                userId, "Healthcare",
                R.drawable.ic_launcher_foreground,
                R.color.category_pink,
                false, true
        ));

        categories.add(new Category(
                userId, "Bills",
                R.drawable.ic_launcher_foreground,
                R.color.category_orange,
                false, true
        ));

        categories.add(new Category(
                userId, "Education",
                R.drawable.ic_launcher_foreground,
                R.color.category_blue,
                false, true
        ));

        return categories;
    }

    public static List<Category> createDefaultIncomeCategories(int userId) {
        List<Category> categories = new ArrayList<>();

        categories.add(new Category(
                userId, "Salary",
                R.drawable.ic_launcher_foreground,
                R.color.category_green,
                true, true // isIncome=true, isDefault=true
        ));

        categories.add(new Category(
                userId, "Freelance",
                R.drawable.ic_launcher_foreground,
                R.color.category_green,
                true, true
        ));

        categories.add(new Category(
                userId, "Investment",
                R.drawable.ic_launcher_foreground,
                R.color.category_green,
                true, true
        ));

        categories.add(new Category(
                userId, "Gift",
                R.drawable.ic_launcher_foreground,
                R.color.category_green,
                true, true
        ));

        categories.add(new Category(
                userId, "Other Income",
                R.drawable.ic_launcher_foreground,
                R.color.category_green,
                true, true
        ));

        return categories;
    }

    public static List<Category> createAllDefaultCategories(int userId) {
        List<Category> allCategories = new ArrayList<>();
        allCategories.addAll(createDefaultExpenseCategories(userId));
        allCategories.addAll(createDefaultIncomeCategories(userId));
        return allCategories;
    }
}