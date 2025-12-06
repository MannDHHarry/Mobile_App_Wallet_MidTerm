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

   //------Wallet Related Operations still don't have edit balance yet
    public static void callAddWalletDialog(
            Context ctx,
            TransactionViewModel vm,
            LifecycleOwner owner,
            Runnable onAdded) {

        int userId = vm.getCurrentUser().getValue().getUserId();

        PickersAndDialog.showAddWalletDialog(ctx, userId, wallet -> {
            // ✅ UPDATED - Use new ViewModel method
            vm.addWallet(wallet.getName(), wallet.getIconResId(), wallet.getBalance());
            toast(ctx, ctx.getString(R.string.wallet_created));
            if (onAdded != null) onAdded.run();
        });
    }

    public static void callEditWalletDialog(
            Context ctx,
            Wallet wallet,
            TransactionViewModel vm,
            Runnable onUpdated) {

        PickersAndDialog.showEditWalletDialog(ctx, wallet, () -> {
            vm.updateWallet(wallet);
            toast(ctx, ctx.getString(R.string.wallet_updated));
            if (onUpdated != null) onUpdated.run();
        });
    }

    public static void callDeleteWalletDialog(
            Context ctx,
            Wallet wallet,
            TransactionViewModel vm,
            Runnable onDeleted) {

        List<Wallet> list = vm.getWallets().getValue();
        if (list == null) list = new ArrayList<>();

        // Check if it's the last wallet
        if (list.size() <= 1) {
            toast(ctx, ctx.getString(R.string.cannot_delete_last_wallet));
            return;
        }

        PickersAndDialog.showDeleteWalletDialog(ctx, wallet, list, () -> {
            // ✅ UPDATED - deleteWallet now returns boolean
            boolean deleted = vm.deleteWallet(wallet);
            if (deleted) {
                toast(ctx, ctx.getString(R.string.wallet_deleted));
                if (onDeleted != null) onDeleted.run();
            } else {
                toast(ctx, ctx.getString(R.string.cannot_delete_wallet));
            }
        });
    }


    /* -------------------  ADD CATEGORY ------------------- */

    public static void callAddCategoryDialog(Context ctx, TransactionViewModel vm, boolean isIncomeFlag, Runnable onAdded) {
        PickersAndDialog.showAddCategoryDialog(ctx, (name, isIncome, iconRes, colorRes) -> {
            Long result = isIncome
                    ? vm.addIncomeCategory(name, iconRes, colorRes)
                    : vm.addExpenseCategory(name, iconRes, colorRes);

            if (result != null && result > 0) {
                toast(ctx, ctx.getString(R.string.category_added));
                if (onAdded != null) onAdded.run();
            } else {
                toast(ctx, ctx.getString(R.string.category_exists));
            }
        });
    }



    public static void callEditCategoryDialog(
            Context ctx,
            Category category,
            TransactionViewModel vm,
            Runnable onUpdated) {

        PickersAndDialog.showEditCategoryDialog(ctx, category, () -> {
            // ✅ Same - updateCategory still works
            vm.updateCategory(category);
            toast(ctx, ctx.getString(R.string.category_updated));
            if (onUpdated != null) onUpdated.run();
        });
    }

    public static void callDeleteCategoryDialog(
            Context ctx,
            Category category,
            boolean isExpense,
            TransactionViewModel vm,
            Runnable onDeleted) {

        // Get active categories (non-archived)
        List<Category> list = isExpense
                ? vm.getExpenseCategories().getValue()
                : vm.getIncomeCategories().getValue();

        if (list == null) list = new ArrayList<>();

        // Check if category has transactions first
        int transactionCount = vm.getCategoryTransactionCount(category.getCategoryId());

        if (transactionCount > 0) {
            // Category has transactions - show archive confirmation
            PickersAndDialog.showArchiveCategoryDialog(ctx, category, transactionCount, () -> {
                // Archive instead of delete
                vm.archiveCategory(category.getCategoryId());
                toast(ctx, ctx.getString(R.string.category_archived));
                if (onDeleted != null) onDeleted.run();
            });
        } else {
            // No transactions - safe to delete
            PickersAndDialog.showDeleteCategoryDialog(ctx, category, list, () -> {
                vm.deleteCategory(category.getCategoryId());
                toast(ctx, ctx.getString(R.string.category_deleted));
                if (onDeleted != null) onDeleted.run();
            });
        }
    }

    private static void toast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
}
