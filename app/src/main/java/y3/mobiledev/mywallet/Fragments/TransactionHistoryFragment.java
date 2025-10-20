package y3.mobiledev.mywallet.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.Adapters.TransactionAdapter;
import y3.mobiledev.mywallet.Helpers.DateManager;
import y3.mobiledev.mywallet.Helpers.TransactionManager;
import y3.mobiledev.mywallet.Models.Category;
import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.TransactionGroup;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.SampleDataProvider;
import y3.mobiledev.mywallet.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryFragment extends Fragment {
    private EditText etSearchTransaction;
    private ImageButton btnClearSearch;
    private Spinner spDateRange, spCategory, spPaymentType;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private TransactionViewModel viewModel;

    // Filter states
    private String currentSearchText = "";
    private String currentDateFilter = "All Time";
    private String currentCategoryFilter = "All Categories";
    private String currentPaymentFilter = "All Types";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_history, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        initViews(view);
        setupRecyclerView();
        setupFilterSpinners();
        setupSearchListener();
        observeData();
        return view;
    }

    private void initViews(View view) {
        etSearchTransaction = view.findViewById(R.id.etSearchTransaction);
        spDateRange = view.findViewById(R.id.spDateRange);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        spCategory = view.findViewById(R.id.spCategory);
        spPaymentType = view.findViewById(R.id.spPaymentType);
        rvTransactions = view.findViewById(R.id.rvTransactions);
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(
                requireContext(),
                new ArrayList<>(),
                transaction -> onTransactionClick(transaction)
        );
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setNestedScrollingEnabled(false);
    }

    private void setupFilterSpinners() {
        // Date Range Spinner
        String[] dateRanges = {"All Time", "Today", "This Week", "This Month", "This Year"};
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dateRanges);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDateRange.setAdapter(dateAdapter);
        spDateRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentDateFilter = dateRanges[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Category Spinner
        viewModel.getExpenseCategories().observe(getViewLifecycleOwner(), expenseCats -> {
            viewModel.getIncomeCategories().observe(getViewLifecycleOwner(), incomeCats -> {
                List<String> categories = new ArrayList<>();
                categories.add("All Categories");
                if (expenseCats != null) {
                    for (Category cat : expenseCats) {
                        categories.add(cat.getName());
                    }
                }
                if (incomeCats != null) {
                    for (Category cat : incomeCats) {
                        categories.add(cat.getName());
                    }
                }
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategory.setAdapter(categoryAdapter);

                spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // Update the filter state with the selected category name
                        currentCategoryFilter = categories.get(position);
                        // Re-apply the filters to update the RecyclerView
                        applyFilters();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No action needed here
                    }
                });
            });
        });

        // Payment Type Spinner
        String[] paymentTypes = {"All Types", "Expense", "Income"};
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, paymentTypes);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentType.setAdapter(paymentAdapter);
        spPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPaymentFilter = paymentTypes[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchListener() {
        btnClearSearch.setOnClickListener(v -> {
            etSearchTransaction.setText("");
        });
        etSearchTransaction.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase().trim();
                // 3. Show or hide the clear button based on text presence
                if (s.length() > 0) {
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    btnClearSearch.setVisibility(View.GONE);
                }
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeData() {
        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> applyFilters());
    }

    private void applyFilters() {
        List<Transaction> transactions = viewModel.getTransactions().getValue();
        if (transactions == null) {
            transactionAdapter.updateTransactions(new ArrayList<>());
            return;
        }
        List<Transaction> filtered = new ArrayList<>(transactions);

        // Filter by search text
        if (!currentSearchText.isEmpty()) {
            filtered.removeIf(t -> !t.getCategory().toLowerCase().contains(currentSearchText) &&
                    !t.getDescription().toLowerCase().contains(currentSearchText) &&
                    !String.format(Locale.US, "%.2f", t.getAmount()).contains(currentSearchText));
        }

        // Filter by date range
        if (!currentDateFilter.equals("All Time")) {
            filtered.removeIf(t -> !DateManager.isWithinDateRange(t.getDate(), currentDateFilter));
        }

        // Filter by category
        if (!currentCategoryFilter.equals("All Categories")) {
            filtered.removeIf(t -> !t.getCategory().equals(currentCategoryFilter));
        }

        // Filter by payment type
        if (!currentPaymentFilter.equals("All Types")) {
            boolean isExpenseFilter = currentPaymentFilter.equals("Expense");
            filtered.removeIf(t -> t.isExpense() != isExpenseFilter);
        }

        List<TransactionGroup> filteredGroups = TransactionManager.groupTransactionsByDate(filtered);
        transactionAdapter.updateTransactions(filteredGroups);
    }

    private void onTransactionClick(Transaction transaction) {
        Toast.makeText(requireContext(), "Clicked: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();
        // TODO: Show transaction detail dialog or navigate to detail fragment
    }
}