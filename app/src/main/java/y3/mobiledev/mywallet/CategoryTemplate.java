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
                R.drawable.cat_fastfood,
                R.color.category_orange,
                false, true // isIncome=false, isDefault=true
        ));

        categories.add(new Category(
                userId, "Transport",
                R.drawable.cat_transport,
                R.color.cat_red,
                false, true
        ));

        categories.add(new Category(
                userId, "Utilities",
                R.drawable.cat_bill,
                R.color.category_orange,
                false, true
        ));

        categories.add(new Category(
                userId, "Shopping",
                R.drawable.cat_shop,
                R.color.cat_purple,
                false, true
        ));

        categories.add(new Category(
                userId, "Entertainment",
                R.drawable.cat_512,
                R.color.category_teal,
                false, true
        ));

        categories.add(new Category(
                userId, "Healthcare",
                R.drawable.cat_health,
                R.color.category_green,
                false, true
        ));


        categories.add(new Category(
                userId, "Education",
                R.drawable.cat_education,
                R.color.category_blue,
                false, true
        ));

        return categories;
    }

    public static List<Category> createDefaultIncomeCategories(int userId) {
        List<Category> categories = new ArrayList<>();

        categories.add(new Category(
                userId, "Salary",
                R.drawable.cat_moneh,
                R.color.category_green,
                true, true // isIncome=true, isDefault=true
        ));

        categories.add(new Category(
                userId, "Freelance",
                R.drawable.cat_owl,
                R.color.category_blue,
                true, true
        ));

        categories.add(new Category(
                userId, "Investment",
                R.drawable.cat_pig,
                R.color.category_orange,
                true, true
        ));

        categories.add(new Category(
                userId, "Gift",
                R.drawable.cat_gift,
                R.color.category_teal,
                true, true
        ));

        categories.add(new Category(
                userId, "Other Income",
                R.drawable.cat_lines,
                R.color.category_purple,
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