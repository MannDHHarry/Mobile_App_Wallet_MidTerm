package y3.mobiledev.mywallet.Fragments;

import android.os.Bundle;
import android.util.Log;
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

import y3.mobiledev.mywallet.Adapters.TransactionAdapter;
import y3.mobiledev.mywallet.Adapters.WalletAdapter;
import y3.mobiledev.mywallet.Helpers.CategoryWalletDialog;
import y3.mobiledev.mywallet.Helpers.DateManager;
import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.TransactionGroup;
import y3.mobiledev.mywallet.Models.Wallet;
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
        displayedWallets = new ArrayList<>();
        walletAdapter = new WalletAdapter(requireContext(), displayedWallets, wallet ->
                Toast.makeText(requireContext(), "Clicked: " + wallet.getName(), Toast.LENGTH_SHORT).show());
        rvWallets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvWallets.setAdapter(walletAdapter);
        rvWallets.setNestedScrollingEnabled(false);

        transactionAdapter = new TransactionAdapter(requireContext(), new ArrayList<>(), transaction ->
                Toast.makeText(requireContext(), "Clicked: " + transaction.getCategory(), Toast.LENGTH_SHORT).show());
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setNestedScrollingEnabled(false);
    }

    private void observeData() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), wallets -> {
            Log.d("HomeFragment", "Observer fired - Wallets: " + (wallets != null ? wallets.size() : "null"));

            if (wallets == null || wallets.isEmpty()) {
                Log.d("HomeFragment", "Wallets is null or empty");
                displayedWallets.clear();
                walletAdapter.updateWallets(displayedWallets);
                updateWalletVisibility(wallets);
                return;
            }

            Log.d("HomeFragment", "Before clear - displayedWallets size: " + displayedWallets.size());
            displayedWallets.clear();
            Log.d("HomeFragment", "After clear - displayedWallets size: " + displayedWallets.size());

            for (int i = 0; i < Math.min(MAX_INITIAL_WALLETS, wallets.size()); i++) {
                displayedWallets.add(wallets.get(i));
                Log.d("HomeFragment", "Added wallet #" + i + ": " + wallets.get(i).getName());
            }

            Log.d("HomeFragment", "Final displayedWallets size: " + displayedWallets.size());
            walletAdapter.updateWallets(displayedWallets);
            updateWalletVisibility(wallets);
            updateBalanceCard();
        });

        viewModel.getTransactionGroups().observe(getViewLifecycleOwner(), groups -> {
            transactionAdapter.updateTransactions(groups);
            updateTransactionVisibility(groups);
            updateBalanceCard();
        });
    }


    private void updateBalanceCard() {
        List<Wallet> wallets = viewModel.getWallets().getValue();
        List<TransactionGroup> groups = viewModel.getTransactionGroups().getValue();
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
        if (groups != null) {
            for (TransactionGroup group : groups) {
                for (Transaction transaction : group.getTransactions()) {
                    if (transaction.getDate().after(firstDayOfMonth.getTime())) {
                        if (transaction.isExpense()) {
                            totalExpense += transaction.getAmount();
                        } else {
                            totalIncome += transaction.getAmount();
                        }
                    }
                }
            }
        }
        tvTotalBalance.setText(String.format(Locale.US, "$%,.2f", totalBalance));
        tvIncome.setText(String.format(Locale.US, "$%,.0f", totalIncome));
        tvExpense.setText(String.format(Locale.US, "$%,.0f", totalExpense));
    }

    private void updateTimePeriod() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_MONTH_YEAR, Locale.US);
        String currentMonth = dateFormat.format(new Date());
        tvTimePeriod.setText(getString(R.string.this_month, currentMonth));
    }

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

    private void updateTransactionVisibility(List<TransactionGroup> groups) {
        rvTransactions.setVisibility(groups == null || groups.isEmpty() ? View.GONE : View.VISIBLE);
        emptyStateTransactions.setVisibility(groups == null || groups.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        btnAddWallet.setOnClickListener(v -> showAddWalletDialog());
        btnShowMoreWallets.setOnClickListener(v -> toggleWalletDisplay());
        btnViewAllTransactions.setOnClickListener(v -> Toast.makeText(requireContext(), "View All Transactions", Toast.LENGTH_SHORT).show());

        View btnAddFirstWallet = emptyStateWallets.findViewById(R.id.btnAddFirstWallet);
        if (btnAddFirstWallet != null) {
            btnAddFirstWallet.setOnClickListener(v -> showAddWalletDialog());
        }
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