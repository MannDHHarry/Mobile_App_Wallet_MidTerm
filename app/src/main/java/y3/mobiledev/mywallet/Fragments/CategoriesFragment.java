// ========== CategoriesFragment.java ==========
package y3.mobiledev.mywallet.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.Adapters.CategoryManagementAdapter;
import y3.mobiledev.mywallet.Adapters.WalletManagementAdapter;
import y3.mobiledev.mywallet.Helpers.CategoryWalletDialog;
import y3.mobiledev.mywallet.Models.Category;
import y3.mobiledev.mywallet.Models.Wallet;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private RecyclerView rvExpenseCategories, rvIncomeCategories, rvWallets;
    private CategoryManagementAdapter expenseCategoriesAdapter, incomeCategoriesAdapter;
    private WalletManagementAdapter walletAdapter;
    private TransactionViewModel viewModel;
    private ImageButton btnAddWallet , btnAddCategory ;

    private List<Category> expenseCategories;
    private List<Category> incomeCategories;
    private List<Wallet> wallets;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        initViews(view);
        setupRecyclerViews();
        setupListeners();
        observeCategories();
        observeWallets();

        return view;
    }

    private void initViews(View view) {
        rvExpenseCategories = view.findViewById(R.id.rvExpenseCategories);
        rvIncomeCategories = view.findViewById(R.id.rvIncomeCategories);
        rvWallets = view.findViewById(R.id.rvWallets);
        btnAddWallet = view.findViewById(R.id.btnAddWallet);
        btnAddCategory = view.findViewById(R.id.btnAddCategory);

    }

    private void setupRecyclerViews() {
        // Initialize with empty lists
        expenseCategories = new ArrayList<>();
        incomeCategories = new ArrayList<>();
        wallets = new ArrayList<>();

        // Expense Categories Adapter
        expenseCategoriesAdapter = new CategoryManagementAdapter(
                requireContext(),
                expenseCategories,
                category -> onEditCategory(category),
                category -> onDeleteCategory(category, true)
        );
        rvExpenseCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenseCategories.setAdapter(expenseCategoriesAdapter);
        rvExpenseCategories.setNestedScrollingEnabled(false);

        // Income Categories Adapter
        incomeCategoriesAdapter = new CategoryManagementAdapter(
                requireContext(),
                incomeCategories,
                category -> onEditCategory(category),
                category -> onDeleteCategory(category, false)
        );
        rvIncomeCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvIncomeCategories.setAdapter(incomeCategoriesAdapter);
        rvIncomeCategories.setNestedScrollingEnabled(false);

        // Wallets Adapter
        walletAdapter = new WalletManagementAdapter(
                requireContext(),
                wallets,
                wallet -> onEditWallet(wallet),
                wallet -> onDeleteWallet(wallet)
        );
        rvWallets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWallets.setAdapter(walletAdapter);
        rvWallets.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        btnAddWallet.setOnClickListener(v -> showAddWalletDialog());
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void observeCategories() {
        viewModel.getExpenseCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                expenseCategoriesAdapter.updateCategories(categories);
            }
        });

        viewModel.getIncomeCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                incomeCategoriesAdapter.updateCategories(categories);
            }
        });
    }

    private void observeWallets() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), walletList -> {
            if (walletList != null) {
                walletAdapter.updateWallets(walletList);
            }
        });
    }

    private void showAddCategoryDialog() {
        CategoryWalletDialog.showAddCategoryDialog(requireContext(), (categoryName, isIncome) -> {
            Category newCategory = isIncome ?
                    viewModel.addIncomeCategory(categoryName, R.color.category_orange, android.R.drawable.ic_dialog_info) :
                    viewModel.addExpenseCategory(categoryName, R.color.category_orange, android.R.drawable.ic_dialog_info);

            if (newCategory != null) {
                Toast.makeText(requireContext(), "Category added: " + categoryName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Category already exists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onEditCategory(Category category) {
        CategoryWalletDialog.showEditCategoryDialog(requireContext(), category, () -> {
            viewModel.updateCategory(category);
        });
    }

    private void onDeleteCategory(Category category, boolean isExpense) {
        List<Category> categoryList = isExpense ?
                viewModel.getExpenseCategories().getValue() :
                viewModel.getIncomeCategories().getValue();

        if (categoryList == null) {
            categoryList = new ArrayList<>();
        }

        CategoryWalletDialog.showDeleteCategoryDialog(requireContext(), category, categoryList, () -> {
            viewModel.deleteCategory(category);
        });
    }

    private void onEditWallet(Wallet wallet) {
        CategoryWalletDialog.showEditWalletDialog(requireContext(), wallet, () -> {
            viewModel.updateWallet(wallet);
        });
    }

    private void onDeleteWallet(Wallet wallet) {
        List<Wallet> walletList = viewModel.getWallets().getValue();
        if (walletList == null) {
            walletList = new ArrayList<>();
        }

        CategoryWalletDialog.showDeleteWalletDialog(requireContext(), wallet, walletList, () -> {
            viewModel.deleteWallet(wallet);
        });
    }

    private void showAddWalletDialog() {
        int userId = viewModel.getCurrentUser().getValue().getUserId();

        CategoryWalletDialog.showAddWalletDialog(requireContext(), userId, wallet -> {
            List<Wallet> currentWallets = new ArrayList<>(viewModel.getAllWallets());
            currentWallets.add(wallet);
            viewModel.addWalletDirect(currentWallets);
        });
    }
}