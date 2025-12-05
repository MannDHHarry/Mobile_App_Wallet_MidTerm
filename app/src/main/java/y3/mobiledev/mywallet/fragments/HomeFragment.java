package y3.mobiledev.mywallet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.adapters.TransactionAdapter;
import y3.mobiledev.mywallet.adapters.WalletAdapter;
import y3.mobiledev.mywallet.helpers.CategoryWalletManager;
import y3.mobiledev.mywallet.helpers.CurrencyUtils;
import y3.mobiledev.mywallet.helpers.DateManager;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionGroup;
import y3.mobiledev.mywallet.models.TransactionWithCategory;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final int MAX_INITIAL_WALLETS = 3;
    private static final String DATE_FORMAT_MONTH_YEAR = "MMM yyyy";

    private TextView tvTotalBalance, tvIncome, tvExpense, tvTimePeriod, btnViewAllTransactions, btnShowMoreWallets;
    private ImageButton btnAddWallet;
    private RecyclerView rvWallets, rvTransactions;
    private View emptyStateWallets, emptyStateTransactions;
    private WalletAdapter walletAdapter;
    private TransactionAdapter transactionAdapter;
    private List<Wallet> displayedWallets;
    private boolean showingAllWallets = false;
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        initViews(view);
        setupRecyclerViews();
        setupListeners();
        observeData();
        updateTimePeriod();
        return view;
    }

    //Init Views by signing them to variables
    private void initViews(View view) {
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvIncome = view.findViewById(R.id.tvIncome);
        tvExpense = view.findViewById(R.id.tvExpense);
        tvTimePeriod = view.findViewById(R.id.tvTimePeriod);
        btnAddWallet = view.findViewById(R.id.btnAddWallet);
        rvWallets = view.findViewById(R.id.rvWallets);
        btnShowMoreWallets = view.findViewById(R.id.btnShowMoreWallets);
        emptyStateWallets = view.findViewById(R.id.emptyStateWallets);
        btnViewAllTransactions = view.findViewById(R.id.btnViewAllTransactions);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        emptyStateTransactions = view.findViewById(R.id.emptyStateTransactions);
    }

    private void setupRecyclerViews() {

        //Setting Up Wallet RecyclerView
        displayedWallets = new ArrayList<>();

        walletAdapter = new WalletAdapter(requireContext(), displayedWallets, wallet ->
                Toast.makeText(requireContext(), "Clicked: " + wallet.getName(), Toast.LENGTH_SHORT).show());
        rvWallets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWallets.setAdapter(walletAdapter);
        rvWallets.setNestedScrollingEnabled(false);

        //Setting up Transaction RecyclerView

        transactionAdapter = new TransactionAdapter(requireContext(), new ArrayList<>(), transaction ->
                Toast.makeText(requireContext(), "Clicked: " + transaction.getCategory(), Toast.LENGTH_SHORT).show());
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setNestedScrollingEnabled(false);

    }

    //setting up onClickListeners

    private void setupListeners() {
        btnAddWallet.setOnClickListener(v -> onAddWallet());
        btnShowMoreWallets.setOnClickListener(v -> toggleWalletDisplay());
        btnViewAllTransactions.setOnClickListener(v -> Toast.makeText(requireContext(), "View All Transactions", Toast.LENGTH_SHORT).show());

        View btnAddFirstWallet = emptyStateWallets.findViewById(R.id.btnAddFirstWallet);
        if (btnAddFirstWallet != null) {
            btnAddFirstWallet.setOnClickListener(v -> onAddWallet());
        }
    }

    //Data Observation in Live Data
    private void observeData() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets -> {
            if (wallets == null || wallets.isEmpty()) {
                displayedWallets.clear();
                walletAdapter.updateWallets(displayedWallets);
                updateWalletVisibility(wallets);
                return;
            }

            displayedWallets.clear();
            for (int i = 0; i < Math.min(MAX_INITIAL_WALLETS, wallets.size()); i++) {
                displayedWallets.add(wallets.get(i));
            }

            walletAdapter.updateWallets(displayedWallets);
            updateWalletVisibility(wallets);
            updateBalanceCard();
        });

        // ✅ CHANGED - Observe TransactionGroups from ViewModel
        viewModel.getTransactionGroups().observe(getViewLifecycleOwner(), groups -> {
            transactionAdapter.updateTransactions(groups);
            updateTransactionVisibility(groups);
            updateBalanceCard();
        });
    }

    // ✅ UPDATED - Now works with TransactionWithCategory
    private void updateBalanceCard() {
        List<Wallet> wallets = viewModel.getWallets().getValue();

        double totalBalance = 0;
        if (wallets != null) {
            for (Wallet wallet : wallets) {
                totalBalance += wallet.getBalance();
            }
        }

        double totalIncome = 0;
        double totalExpense = 0;
        Calendar firstDayOfMonth = DateManager.getTodayMidnight();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

        // ✅ Get transaction groups and calculate income/expense
        List<TransactionGroup> groups = viewModel.getTransactionGroups().getValue();
        if (groups != null) {
            for (TransactionGroup group : groups) {
                for (Object transaction : group.getTransactions()) {
                    // Handle both Transaction and TransactionWithCategory
                    long transactionDate;
                    boolean isExpense;
                    double amount;

                    if (transaction instanceof TransactionWithCategory) {
                        TransactionWithCategory twc = (TransactionWithCategory) transaction;
                        transactionDate = twc.getDate();
                        isExpense = twc.isExpense();
                        amount = twc.getAmount();
                    } else {
                        // Fallback for regular Transaction
                        y3.mobiledev.mywallet.models.Transaction t =
                                (y3.mobiledev.mywallet.models.Transaction) transaction;
                        transactionDate = t.getDate();
                        isExpense = t.isExpense();
                        amount = t.getAmount();
                    }

                    if (new Date(transactionDate).after(firstDayOfMonth.getTime())) {
                        if (isExpense) {
                            totalExpense += amount;
                        } else {
                            totalIncome += amount;
                        }
                    }
                }
            }
        }

        tvTotalBalance.setText(CurrencyUtils.formatFullAmount(totalBalance));
        tvIncome.setText(CurrencyUtils.formatPlainAmount(totalIncome));
        tvExpense.setText(CurrencyUtils.formatPlainAmount(totalExpense));
    }


    private void updateTimePeriod() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_MONTH_YEAR, Locale.US);
        String currentMonth = dateFormat.format(new Date());
        tvTimePeriod.setText(getString(R.string.this_month, currentMonth));
    }

    //Display all wallet of just 3
    private void toggleWalletDisplay() {
        List<Wallet> allWallets = viewModel.getWallets().getValue();
        if (allWallets == null) return;
        if (showingAllWallets) {
            displayedWallets.clear();
            for (int i = 0; i < Math.min(MAX_INITIAL_WALLETS, allWallets.size()); i++) {
                displayedWallets.add(allWallets.get(i));
            }
            btnShowMoreWallets.setText(R.string.show_more);
            showingAllWallets = false;
        } else {
            displayedWallets.clear();
            displayedWallets.addAll(allWallets);
            btnShowMoreWallets.setText(R.string.show_less);
            showingAllWallets = true;
        }
        walletAdapter.updateWallets(displayedWallets);
        updateWalletVisibility(allWallets);
    }

    //if there is no wallet no Recycler View
    private void updateWalletVisibility(List<Wallet> wallets) {
        if (wallets == null || wallets.isEmpty()) {
            rvWallets.setVisibility(View.GONE);
            emptyStateWallets.setVisibility(View.VISIBLE);
            btnShowMoreWallets.setVisibility(View.GONE);
        } else {
            rvWallets.setVisibility(View.VISIBLE);
            emptyStateWallets.setVisibility(View.GONE);
            btnShowMoreWallets.setVisibility(wallets.size() > MAX_INITIAL_WALLETS ? View.VISIBLE : View.GONE);
        }
    }

    //if there is no Transaction there is no Transaction Recycler
    private void updateTransactionVisibility(List<TransactionGroup> groups) {
        rvTransactions.setVisibility(groups == null || groups.isEmpty() ? View.GONE : View.VISIBLE);
        emptyStateTransactions.setVisibility(groups == null || groups.isEmpty() ? View.VISIBLE : View.GONE);
    }


    private void onAddWallet() {
        CategoryWalletManager.callAddWalletDialog(
                requireContext(),
                viewModel,
                getViewLifecycleOwner(),
                null);
    }

}