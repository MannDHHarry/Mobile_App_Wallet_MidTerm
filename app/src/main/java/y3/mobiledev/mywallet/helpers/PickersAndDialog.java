// ========== CategoryWalletManager.java (Simplified) ==========
package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
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

import java.math.BigDecimal;
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
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        EditText etName = view.findViewById(R.id.etCategoryName);
        RadioGroup rgType = view.findViewById(R.id.rgCategoryType);
        RecyclerView rvIcons = view.findViewById(R.id.rvIcons);
        RecyclerView rvColors = view.findViewById(R.id.rvColors);

        // Set title
        tvTitle.setText(context.getString(R.string.add_new_category));

        // Default selections
        final int[] selectedIcon = {R.drawable.cat_food};
        final int[] selectedColor = {R.color.cat_orange};

        // Setup Icon Picker
        rvIcons.setLayoutManager(new GridLayoutManager(context, 5));
        rvIcons.setAdapter(new IconAdapter(context, iconResId -> selectedIcon[0] = iconResId));

        // Setup Color Picker
        rvColors.setLayoutManager(new GridLayoutManager(context, 5));
        rvColors.setAdapter(new ColorAdapter(context, colorResId -> selectedColor[0] = colorResId));

        // Create dialog
        AlertDialog dialog = builder.create();

        // Setup cancel button
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        // Setup add button
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.please_enter_category_name), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isIncome = rgType.getCheckedRadioButtonId() == R.id.rbCategoryIncome;

            if (listener != null) {
                listener.onCategoryCreated(name, isIncome, selectedIcon[0], selectedColor[0]);
            }

            dialog.dismiss();
        });

        dialog.show();
    }


    public static void showEditCategoryDialog(Context context, Category category,
                                              OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);

        // Find all views
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        RadioGroup rgCategoryType = dialogView.findViewById(R.id.rgCategoryType);
        RecyclerView rvIcons = dialogView.findViewById(R.id.rvIcons);
        RecyclerView rvColors = dialogView.findViewById(R.id.rvColors);

        // Set title to "Edit Category"
        tvTitle.setText(context.getString(R.string.edit_category));

        // Pre-fill existing data
        etCategoryName.setText(category.getName());

        if (category.isIncome()) {
            rgCategoryType.check(R.id.rbCategoryIncome);
        } else {
            rgCategoryType.check(R.id.rbCategoryExpense);
        }

        // Current selections
        final int[] selectedIcon = {category.getIconResId()};
        final int[] selectedColor = {category.getColorResId()};

        // Setup Icon Picker
        rvIcons.setLayoutManager(new GridLayoutManager(context, 5));
        IconAdapter iconAdapter = new IconAdapter(context, iconResId -> selectedIcon[0] = iconResId);
        iconAdapter.setSelectedIcon(category.getIconResId()); // Pre-select current icon
        rvIcons.setAdapter(iconAdapter);

        // Setup Color Picker
        rvColors.setLayoutManager(new GridLayoutManager(context, 5));
        ColorAdapter colorAdapter = new ColorAdapter(context, colorResId -> selectedColor[0] = colorResId);
        colorAdapter.setSelectedColor(category.getColorResId()); // Pre-select current color
        rvColors.setAdapter(colorAdapter);

        builder.setView(dialogView);

        // Create dialog first
        AlertDialog dialog = builder.create();

        // Setup buttons manually
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String newName = etCategoryName.getText().toString().trim();
            boolean isIncome = rgCategoryType.getCheckedRadioButtonId() == R.id.rbCategoryIncome;

            if (newName.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.category_name_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            // Update category
            category.setName(newName);
            category.setIncome(isIncome);
            category.setIconResId(selectedIcon[0]);
            category.setColorResId(selectedColor[0]);

            Toast.makeText(context, context.getString(R.string.category_updated), Toast.LENGTH_SHORT).show();

            dialog.dismiss();

            if (listener != null) {
                listener.onComplete();
            }
        });

        // Change button text to "Update" for edit mode
        ((com.google.android.material.button.MaterialButton) dialogView.findViewById(R.id.btnAdd))
                .setText(context.getString(R.string.update));

        dialog.show();
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
        // 1. Use a standard AlertDialog.Builder but without setting its buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wallet, null);
        builder.setView(dialogView);

        // 2. Find the views from your custom layout
        EditText etWalletName = dialogView.findViewById(R.id.etWalletName);
        EditText etInitialBalance = dialogView.findViewById(R.id.etInitialBalance);
        // These are the new custom buttons from your layout
        androidx.appcompat.widget.AppCompatButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        androidx.appcompat.widget.AppCompatButton btnAdd = dialogView.findViewById(R.id.btnAdd);

        // 3. Create the dialog but don't show it yet
        AlertDialog dialog = builder.create();

        // 4. Set OnClickListeners for your custom buttons
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String walletName = etWalletName.getText().toString().trim();
            String balanceStr = etInitialBalance.getText().toString().trim();

            if (walletName.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.wallet_name_empty), Toast.LENGTH_SHORT).show();
                // Don't dismiss, let the user correct the mistake
                return;
            }

            // Default to "0" if the user leaves the balance field empty
            if (balanceStr.isEmpty()) {
                balanceStr = "0";
            }

            try {
                double balance = Double.parseDouble(balanceStr);
                if (balance < 0) {
                    Toast.makeText(context, context.getString(R.string.balance_negative), Toast.LENGTH_SHORT).show();
                    return; // Don't dismiss
                }

                // Create the new wallet object
                int walletId = (int) System.currentTimeMillis();
                Wallet newWallet = new Wallet(walletId, userId, walletName,
                        android.R.drawable.ic_menu_myplaces, balance, 0);

                // Use the listener to send the new wallet back
                if (listener != null) {
                    listener.onWalletCreated(newWallet);
                }

                Toast.makeText(context, context.getString(R.string.wallet_created), Toast.LENGTH_SHORT).show();

                // Success! Now dismiss the dialog.
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(context, context.getString(R.string.invalid_balance), Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Finally, show the dialog
        dialog.show();
    }

    public static void showEditWalletDialog(Context context, Wallet wallet,
                                            OnOperationCompleteListener listener) {
        // 1. Use a standard AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_wallet, null);
        builder.setView(dialogView);

        // 2. Find all the views from your custom layout
        EditText etWalletName = dialogView.findViewById(R.id.etWalletName);
        EditText etWalletBalance = dialogView.findViewById(R.id.etWalletBalance);
        androidx.appcompat.widget.AppCompatButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        androidx.appcompat.widget.AppCompatButton btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        // 3. Pre-fill the fields with existing wallet data
        etWalletName.setText(wallet.getName());
        // Using toPlainString() is safer to avoid scientific notation for large numbers
        etWalletBalance.setText(new BigDecimal(wallet.getBalance()).toPlainString());

        // 4. Create the dialog
        AlertDialog dialog = builder.create();

        // 5. Set OnClickListeners for your custom buttons
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdate.setOnClickListener(v -> {
            String newName = etWalletName.getText().toString().trim();
            String balanceStr = etWalletBalance.getText().toString().trim();

            if (newName.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show();
                return; // Don't dismiss, let user fix it
            }

            try {
                double newBalance = Double.parseDouble(balanceStr);
                if (newBalance < 0) {
                    Toast.makeText(context, context.getString(R.string.balance_negative), Toast.LENGTH_SHORT).show();
                    return; // Don't dismiss
                }

                // Update the existing wallet object
                wallet.setName(newName);
                wallet.setBalance(newBalance);

                Toast.makeText(context, context.getString(R.string.wallet_updated), Toast.LENGTH_SHORT).show();

                // Notify the listener that the operation is complete
                if (listener != null) {
                    listener.onComplete();
                }

                // Success! Now dismiss the dialog.
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(context, context.getString(R.string.invalid_balance), Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Finally, show the dialog
        dialog.show();
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
