package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.List;

import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;

public final class CategoryWalletManager {

    private CategoryWalletManager() {}

    /* -------------------  ADD WALLET ------------------- */
    public static void callAddWalletDialog(
            Context ctx,
            TransactionViewModel vm,
            LifecycleOwner owner,
            Runnable onAdded) {

        int userId = vm.getCurrentUser().getValue().getUserId();

        PickersAndDialog.showAddWalletDialog(ctx, userId, wallet -> {
            List<Wallet> all = new ArrayList<>(vm.getAllWallets());
            all.add(wallet);
            vm.addWalletDirect(all);
            toast(ctx, "Wallet created successfully!");
            if (onAdded != null) onAdded.run();
        });
    }

    /* -------------------  ADD CATEGORY ------------------- */
    public static void callAddCategoryDialog(
            Context ctx,
            TransactionViewModel vm,
            boolean isIncome,
            Runnable onAdded) {

        PickersAndDialog.showAddCategoryDialog(ctx, (name, incomeFlag) -> {
            // incomeFlag comes from the dialog – we ignore the outer flag if you want the dialog to decide
            Category c = incomeFlag
                    ? vm.addIncomeCategory(name, R.color.category_orange, android.R.drawable.ic_dialog_info)
                    : vm.addExpenseCategory(name, R.color.category_orange, android.R.drawable.ic_dialog_info);

            if (c != null) {
                toast(ctx, "Category added: " + name);
                if (onAdded != null) onAdded.run();
            } else {
                toast(ctx, "Category already exists");
            }
        });
    }

    /* -------------------  EDIT CATEGORY ------------------- */
    public static void callEditCategoryDialog(
            Context ctx,
            Category category,
            TransactionViewModel vm,
            Runnable onUpdated) {

        PickersAndDialog.showEditCategoryDialog(ctx, category, () -> {
            vm.updateCategory(category);
            toast(ctx, "Category updated");
            if (onUpdated != null) onUpdated.run();
        });
    }

    /* -------------------  DELETE CATEGORY ------------------- */
    public static void callDeleteCategoryDialog(
            Context ctx,
            Category category,
            boolean isExpense,
            TransactionViewModel vm,
            Runnable onDeleted) {

        List<Category> list = isExpense
                ? vm.getExpenseCategories().getValue()
                : vm.getIncomeCategories().getValue();

        if (list == null) list = new ArrayList<>();

        PickersAndDialog.showDeleteCategoryDialog(ctx, category, list, () -> {
            vm.deleteCategory(category);
            toast(ctx, "Category deleted");
            if (onDeleted != null) onDeleted.run();
        });
    }

    /* -------------------  EDIT WALLET ------------------- */
    public static void callEditWalletDialog(
            Context ctx,
            Wallet wallet,
            TransactionViewModel vm,
            Runnable onUpdated) {

        PickersAndDialog.showEditWalletDialog(ctx, wallet, () -> {
            vm.updateWallet(wallet);
            toast(ctx, "Wallet updated");
            if (onUpdated != null) onUpdated.run();
        });
    }

    /* -------------------  DELETE WALLET ------------------- */
    public static void callDeleteWalletDialog(
            Context ctx,
            Wallet wallet,
            TransactionViewModel vm,
            Runnable onDeleted) {

        List<Wallet> list = vm.getWallets().getValue();
        if (list == null) list = new ArrayList<>();

        PickersAndDialog.showDeleteWalletDialog(ctx, wallet, list, () -> {
            vm.deleteWallet(wallet);
            toast(ctx, "Wallet deleted");
            if (onDeleted != null) onDeleted.run();
        });
    }

    /* -------------------  TOAST ------------------- */
    private static void toast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
}