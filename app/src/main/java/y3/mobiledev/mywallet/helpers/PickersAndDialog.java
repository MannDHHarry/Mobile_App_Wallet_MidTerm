// ========== CategoryWalletManager.java (Simplified) ==========
package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.adapters.ItemDialogAdapter;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.adapters.ColorAdapter;
import y3.mobiledev.mywallet.adapters.IconAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PickersAndDialog {

    public interface OnSelectionListener {
        void onItemSelected(Object item);
    }

    public interface OnOperationCompleteListener {
        void onComplete();
    }

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(String categoryName, boolean isIncome, int iconResId, int colorResId);
    }

    public interface OnWalletCreatedListener {
        void onWalletCreated(Wallet wallet);
    }

    // ===== CATEGORY PICKER (Selection Only) =====
    public static void showCategoryPicker(Context context, List<Category> categories,
                                          boolean isExpense, OnSelectionListener listener) {
        // Initialize empty list if null
        if (categories == null) {
            categories = new ArrayList<>();
        }

        List<Object> displayList = new ArrayList<>(categories);
        displayList.add(context.getString(R.string.add_new_category_option));

        ItemDialogAdapter adapter = new ItemDialogAdapter(
                context,
                displayList,
                new ItemDialogAdapter.ItemProvider() {
                    @Override
                    public int getIconResId(Object item) {
                        if (item instanceof Category) {
                            Category category = (Category) item;
                            return category.getIconResId();
                        }
                        return 0;

                    }

                    @Override
                    public int getColorResId(Object item) {
                        return item instanceof Category ? ((Category) item).getColorResId() : 0;
                    }

                    @Override
                    public String getDisplayText(Object item) {
                        if (item instanceof Category) {
                            return ((Category) item).getName();
                        } else if (item instanceof String) {
                            return (String) item;
                        }
                        return "";
                    }
                },
                listener::onItemSelected
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(isExpense ? context.getString(R.string.select_expense_category) : context.getString(R.string.select_income_category));

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        builder.setNegativeButton(context.getString(R.string.cancel), null);

        AlertDialog dialog = builder.create();
        adapter.setDialog(dialog);
        dialog.show();
    }

    // ===== WALLET PICKER (Selection Only) =====
    public static void showWalletPicker(Context context, List<Wallet> wallets,
                                        OnSelectionListener listener) {
        if (wallets == null || wallets.isEmpty()) {
            Toast.makeText(context, context.getString(R.string.no_wallets_available), Toast.LENGTH_SHORT).show();
            return;
        }

        List<Object> displayList = new ArrayList<>(wallets);

        ItemDialogAdapter adapter = new ItemDialogAdapter(
                context,
                displayList,
                new ItemDialogAdapter.ItemProvider() {
                    @Override
                    public int getIconResId(Object item) {
                        return item instanceof Wallet ? R.drawable.purse : 0;
                    }

                    @Override
                    public int getColorResId(Object item) {
                        return R.color.colorDarkPurple;
                    }

                    @Override
                    public String getDisplayText(Object item) {
                        if (item instanceof Wallet) {
                            Wallet wallet = (Wallet) item;
                            String balanceText = CurrencyUtils.formatPlainAmount(wallet.getBalance());
                            return wallet.getName() + " (" + balanceText + ")";
                        } else if (item instanceof String) {
                            return (String) item;
                        }
                        return "";
                    }
                },
                listener::onItemSelected
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.select_wallet));

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        builder.setNegativeButton(context.getString(R.string.cancel), null);

        AlertDialog dialog = builder.create();
        adapter.setDialog(dialog);
        dialog.show();
    }


    // ===== CATEGORY OPERATIONS =====

    public static void showAddCategoryDialog(Context context, OnCategoryCreatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);
        builder.setView(view);

        // Find views
        EditText etName = view.findViewById(R.id.etCategoryName);
        RadioGroup rgType = view.findViewById(R.id.rgCategoryType);
        RecyclerView rvIcons = view.findViewById(R.id.rvIcons);
        RecyclerView rvColors = view.findViewById(R.id.rvColors);

        // Default selections
        final int[] selectedIcon = {R.drawable.cat_food};     // replace with your default
        final int[] selectedColor = {R.color.cat_orange};

        // Setup Icon Picker
        rvIcons.setLayoutManager(new GridLayoutManager(context, 5));
        rvIcons.setAdapter(new IconAdapter(context, iconResId -> selectedIcon[0] = iconResId));

        // Setup Color Picker
        rvColors.setLayoutManager(new GridLayoutManager(context, 5));
        rvColors.setAdapter(new ColorAdapter(context, colorResId -> selectedColor[0] = colorResId));

        builder.setPositiveButton(context.getString(R.string.add_category), (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.please_enter_category_name), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isIncome = rgType.getCheckedRadioButtonId() == R.id.rbCategoryIncome;

            // Pass name + income + icon + color via your existing listener
            if (listener != null) {
                listener.onCategoryCreated(name, isIncome, selectedIcon[0], selectedColor[0]);
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }


    public static void showEditCategoryDialog(Context context, Category category,
                                              OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.edit_category));

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        RadioGroup rgCategoryType = dialogView.findViewById(R.id.rgCategoryType);

        etCategoryName.setText(category.getName());
        if (category.isIncome()) {
            rgCategoryType.check(R.id.rbCategoryIncome);
        } else {
            rgCategoryType.check(R.id.rbCategoryExpense);
        }

        builder.setView(dialogView);
        builder.setPositiveButton(context.getString(R.string.update), (dialog, which) -> {
            String newName = etCategoryName.getText().toString().trim();
            boolean isIncome = rgCategoryType.getCheckedRadioButtonId() == R.id.rbCategoryIncome;

            if (newName.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.category_name_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            category.setName(newName);
            category.setIncome(isIncome);

            Toast.makeText(context, context.getString(R.string.category_updated), Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onComplete();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    public static void showDeleteCategoryDialog(Context context, Category category,
                                                List<Category> categoryList,
                                                OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.delete_category));
        builder.setMessage(String.format(context.getString(R.string.delete_category_confirm), category.getName()));
        builder.setPositiveButton(context.getString(R.string.delete), (dialog, which) -> {
            Toast.makeText(context, context.getString(R.string.category_deleted), Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onComplete();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    public static void showArchiveCategoryDialog(
            Context context,
            Category category,
            int transactionCount,
            Runnable onConfirmed) {

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.archive_category))
                .setMessage(String.format(context.getString(R.string.archive_category_message), category.getName(), transactionCount))
                .setPositiveButton(context.getString(R.string.archive), (dialog, which) -> {
                    if (onConfirmed != null) onConfirmed.run();
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }




    // ===== WALLET OPERATIONS =====

    public static void showAddWalletDialog(Context context, int userId,
                                           OnWalletCreatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.add_new_wallet));

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wallet, null);
        EditText etWalletName = dialogView.findViewById(R.id.etWalletName);
        EditText etInitialBalance = dialogView.findViewById(R.id.etInitialBalance);

        builder.setView(dialogView);
        builder.setPositiveButton(context.getString(R.string.create), (dialog, which) -> {
            String walletName = etWalletName.getText().toString().trim();
            String balanceStr = etInitialBalance.getText().toString().trim();

            if (walletName.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.wallet_name_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            if (balanceStr.isEmpty()) {
                balanceStr = "0";
            }

            try {
                double balance = Double.parseDouble(balanceStr);
                if (balance < 0) {
                    Toast.makeText(context, context.getString(R.string.balance_negative), Toast.LENGTH_SHORT).show();
                    return;
                }

                int walletId = (int) System.currentTimeMillis();
                Wallet newWallet = new Wallet(walletId, userId, walletName,
                        android.R.drawable.ic_menu_myplaces, balance, 0);

                if (listener != null) {
                    listener.onWalletCreated(newWallet);
                }

                Toast.makeText(context, context.getString(R.string.wallet_created), Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(context, context.getString(R.string.invalid_balance), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    public static void showEditWalletDialog(Context context, Wallet wallet,
                                            OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.edit_wallet));

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_wallet, null);
        EditText etWalletName = dialogView.findViewById(R.id.etWalletName);
        EditText etWalletBalance = dialogView.findViewById(R.id.etWalletBalance);

        etWalletName.setText(wallet.getName());
        etWalletBalance.setText(String.format(Locale.US, "%.2f", wallet.getBalance()));

        builder.setView(dialogView);
        builder.setPositiveButton(context.getString(R.string.update), (dialog, which) -> {
            String newName = etWalletName.getText().toString().trim();
            String balanceStr = etWalletBalance.getText().toString().trim();

            if (newName.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newBalance = Double.parseDouble(balanceStr);
                if (newBalance < 0) {
                    Toast.makeText(context, context.getString(R.string.balance_negative), Toast.LENGTH_SHORT).show();
                    return;
                }

                wallet.setName(newName);
                wallet.setBalance(newBalance);

                Toast.makeText(context, context.getString(R.string.wallet_updated), Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onComplete();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, context.getString(R.string.invalid_balance), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }

    public static void showDeleteWalletDialog(Context context, Wallet wallet,
                                              List<Wallet> walletList,
                                              OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.delete_wallet));
        builder.setMessage(String.format(context.getString(R.string.delete_wallet_confirm), wallet.getName()));
        builder.setPositiveButton(context.getString(R.string.delete), (dialog, which) -> {
            walletList.remove(wallet);
            Toast.makeText(context, context.getString(R.string.wallet_deleted), Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onComplete();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }
}
