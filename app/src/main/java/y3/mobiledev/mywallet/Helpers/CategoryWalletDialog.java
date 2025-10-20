// ========== CategoryWalletManager.java (Simplified) ==========
package y3.mobiledev.mywallet.Helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.Adapters.ItemDialogAdapter;
import y3.mobiledev.mywallet.Models.Category;
import y3.mobiledev.mywallet.Models.Wallet;
import y3.mobiledev.mywallet.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryWalletDialog {

    public interface OnSelectionListener {
        void onItemSelected(Object item);
    }

    public interface OnOperationCompleteListener {
        void onComplete();
    }

    public interface OnCategoryCreatedListener {
        void onCategoryCreated(String categoryName, boolean isIncome);
    }

    public interface OnWalletCreatedListener {
        void onWalletCreated(Wallet wallet);
    }

    // ===== CATEGORY PICKER (Selection Only) =====
    public static void showCategoryPicker(Context context, List<Category> categories,
                                          boolean isExpense, OnSelectionListener listener) {
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(context, "No categories available", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Object> displayList = new ArrayList<>(categories);
        displayList.add("+ Add New Category");

        ItemDialogAdapter adapter = new ItemDialogAdapter(
                context,
                displayList,
                new ItemDialogAdapter.ItemProvider() {
                    @Override
                    public int getIconResId(Object item) {
                        return item instanceof Category ? ((Category) item).getIconResId() : 0;
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
        builder.setTitle(isExpense ? "Select Expense Category" : "Select Income Category");

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        adapter.setDialog(dialog);
        dialog.show();
    }

    // ===== WALLET PICKER (Selection Only) =====
    public static void showWalletPicker(Context context, List<Wallet> wallets,
                                        OnSelectionListener listener) {
        if (wallets == null || wallets.isEmpty()) {
            Toast.makeText(context, "No wallets available", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Object> displayList = new ArrayList<>(wallets);

        ItemDialogAdapter adapter = new ItemDialogAdapter(
                context,
                displayList,
                new ItemDialogAdapter.ItemProvider() {
                    @Override
                    public int getIconResId(Object item) {
                        return item instanceof Wallet ? ((Wallet) item).getIconResId() : 0;
                    }

                    @Override
                    public int getColorResId(Object item) {
                        return R.color.colorPrimary;
                    }

                    @Override
                    public String getDisplayText(Object item) {
                        if (item instanceof Wallet) {
                            Wallet wallet = (Wallet) item;
                            return wallet.getName() + " ($" +
                                    String.format(Locale.US, "%.2f", wallet.getBalance()) + ")";
                        } else if (item instanceof String) {
                            return (String) item;
                        }
                        return "";
                    }
                },
                listener::onItemSelected
        );

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Wallet");

        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        builder.setView(recyclerView);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        adapter.setDialog(dialog);
        dialog.show();
    }


    // ===== CATEGORY OPERATIONS =====

    public static void showAddCategoryDialog(Context context, OnCategoryCreatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add New Category");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        RadioGroup rgCategoryType = dialogView.findViewById(R.id.rgCategoryType);

        builder.setView(dialogView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String categoryName = etCategoryName.getText().toString().trim();
            boolean isIncome = rgCategoryType.getCheckedRadioButtonId() == R.id.rbCategoryIncome;

            if (categoryName.isEmpty()) {
                Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onCategoryCreated(categoryName, isIncome);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }


    public static void showEditCategoryDialog(Context context, Category category,
                                              OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Category");

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
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = etCategoryName.getText().toString().trim();
            boolean isIncome = rgCategoryType.getCheckedRadioButtonId() == R.id.rbCategoryIncome;

            if (newName.isEmpty()) {
                Toast.makeText(context, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            category.setName(newName);
            category.setIncome(isIncome);

            Toast.makeText(context, "Category updated", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onComplete();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public static void showDeleteCategoryDialog(Context context, Category category,
                                                List<Category> categoryList,
                                                OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Category?");
        builder.setMessage("Are you sure you want to delete \"" + category.getName() + "\"?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onComplete();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }



    // ===== WALLET OPERATIONS =====

    public static void showAddWalletDialog(Context context, int userId,
                                           OnWalletCreatedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add New Wallet");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_wallet, null);
        EditText etWalletName = dialogView.findViewById(R.id.etWalletName);
        EditText etInitialBalance = dialogView.findViewById(R.id.etInitialBalance);

        builder.setView(dialogView);
        builder.setPositiveButton("Create", (dialog, which) -> {
            String walletName = etWalletName.getText().toString().trim();
            String balanceStr = etInitialBalance.getText().toString().trim();

            if (walletName.isEmpty()) {
                Toast.makeText(context, "Wallet name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (balanceStr.isEmpty()) {
                balanceStr = "0";
            }

            try {
                double balance = Double.parseDouble(balanceStr);
                if (balance < 0) {
                    Toast.makeText(context, "Balance cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }

                int walletId = (int) System.currentTimeMillis();
                Wallet newWallet = new Wallet(walletId, userId, walletName,
                        android.R.drawable.ic_menu_myplaces, balance, 0);

                if (listener != null) {
                    listener.onWalletCreated(newWallet);
                }

                Toast.makeText(context, "Wallet created successfully!", Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid balance amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public static void showEditWalletDialog(Context context, Wallet wallet,
                                            OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Wallet");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_wallet, null);
        EditText etWalletName = dialogView.findViewById(R.id.etWalletName);
        EditText etWalletBalance = dialogView.findViewById(R.id.etWalletBalance);

        etWalletName.setText(wallet.getName());
        etWalletBalance.setText(String.format(Locale.US, "%.2f", wallet.getBalance()));

        builder.setView(dialogView);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = etWalletName.getText().toString().trim();
            String balanceStr = etWalletBalance.getText().toString().trim();

            if (newName.isEmpty() || balanceStr.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double newBalance = Double.parseDouble(balanceStr);
                if (newBalance < 0) {
                    Toast.makeText(context, "Balance cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }

                wallet.setName(newName);
                wallet.setBalance(newBalance);

                Toast.makeText(context, "Wallet updated", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onComplete();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid balance amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public static void showDeleteWalletDialog(Context context, Wallet wallet,
                                              List<Wallet> walletList,
                                              OnOperationCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Wallet?");
        builder.setMessage("Are you sure you want to delete \"" + wallet.getName() + "\"?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            walletList.remove(wallet);
            Toast.makeText(context, "Wallet deleted", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onComplete();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}