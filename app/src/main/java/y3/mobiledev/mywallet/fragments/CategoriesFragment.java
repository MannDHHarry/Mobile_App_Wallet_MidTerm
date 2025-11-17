// ========== CategoriesFragment.java ==========
package y3.mobiledev.mywallet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.adapters.CategoryManagementAdapter;
import y3.mobiledev.mywallet.adapters.WalletManagementAdapter;
import y3.mobiledev.mywallet.helpers.CategoryWalletManager;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Wallet;
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


    //Init Views by signing them to variables
    private void initViews(View view) {
        rvExpenseCategories = view.findViewById(R.id.rvExpenseCategories);
        rvIncomeCategories = view.findViewById(R.id.rvIncomeCategories);
        rvWallets = view.findViewById(R.id.rvWallets);
        btnAddWallet = view.findViewById(R.id.btnAddWallet);
        btnAddCategory = view.findViewById(R.id.btnAddCategory);

    }

    //Setting Up Recycler Views for wallet , expense categories and income categories management
    private void setupRecyclerViews() {

        // Initialize with empty lists
        wallets = new ArrayList<>();
        expenseCategories = new ArrayList<>();
        incomeCategories = new ArrayList<>();

        // Wallets RV with custom WalletManagementAdapter
        walletAdapter = new WalletManagementAdapter(
                requireContext(),
                wallets,
                wallet -> onEditWallet(wallet),
                wallet -> onDeleteWallet(wallet)
        );
        rvWallets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWallets.setAdapter(walletAdapter);
        rvWallets.setNestedScrollingEnabled(false);

        // Expense Categories RV with custom CategoryManagement Adapter

        expenseCategoriesAdapter = new CategoryManagementAdapter(
                requireContext(),
                expenseCategories,
                category -> onEditCategory(category),
                category -> onDeleteCategory(category, true)
        );

        rvExpenseCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenseCategories.setAdapter(expenseCategoriesAdapter);
        rvExpenseCategories.setNestedScrollingEnabled(false);

        // Income Categories RV with custom CategoryManagement Adapter
        incomeCategoriesAdapter = new CategoryManagementAdapter(
                requireContext(),
                incomeCategories,
                category -> onEditCategory(category),
                category -> onDeleteCategory(category, false)
        );
        rvIncomeCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvIncomeCategories.setAdapter(incomeCategoriesAdapter);
        rvIncomeCategories.setNestedScrollingEnabled(false);

    }

    private void setupListeners() {
        btnAddWallet.setOnClickListener(v -> onAddWallet());
        btnAddCategory.setOnClickListener(v -> onAddCategory());
    }

    //Calling Dialogs from the Management Dialogs
    private void onAddCategory() {
        // The dialog itself decides income/expense → we just pass a dummy flag
        CategoryWalletManager.callAddCategoryDialog(
                requireContext(),
                viewModel,
                true,          // flag is ignored inside the manager
                null);
    }

    private void onEditCategory(Category category) {
        CategoryWalletManager.callEditCategoryDialog(
                requireContext(),
                category,
                viewModel,
                null);
    }

    private void onDeleteCategory(Category category, boolean isExpense) {
        CategoryWalletManager.callDeleteCategoryDialog(
                requireContext(),
                category,
                isExpense,
                viewModel,
                null);
    }

    private void onAddWallet() {
        CategoryWalletManager.callAddWalletDialog( requireContext(), viewModel, getViewLifecycleOwner(), null);
    }
    private void onEditWallet(Wallet wallet) {
        CategoryWalletManager.callEditWalletDialog(
                requireContext(),
                wallet,
                viewModel,
                null);
    }

    private void onDeleteWallet(Wallet wallet) {
        CategoryWalletManager.callDeleteWalletDialog(
                requireContext(),
                wallet,
                viewModel,
                null);
    }


    //Live Data Observers
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
}