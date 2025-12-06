package y3.mobiledev.mywallet.fragments;

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

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;
import y3.mobiledev.mywallet.adapters.TransferAdapter;
import y3.mobiledev.mywallet.helpers.DateManager;
import y3.mobiledev.mywallet.helpers.TransferDetailDialog;
import y3.mobiledev.mywallet.models.TransferWithWallets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransfersFragment extends Fragment {

    private EditText etSearchTransfer;
    private ImageButton btnClearSearch;
    private Spinner spDateRange;
    private RecyclerView rvTransfers;
    private TransferAdapter transferAdapter;
    private TransactionViewModel viewModel;

    // Filter states
    private String currentSearchText = "";
    private String currentDateFilter = "All Time";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfers, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupFilterSpinners();
        setupSearchListener();
        observeData();

        return view;
    }

    private void initViews(View view) {
        etSearchTransfer = view.findViewById(R.id.etSearchTransfer);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        spDateRange = view.findViewById(R.id.spDateRange);
        rvTransfers = view.findViewById(R.id.rvTransfers);
    }

    private void setupRecyclerView() {
        transferAdapter = new TransferAdapter(
                requireContext(),
                new ArrayList<>(),
                transfer -> onTransferClick(transfer)
        );
        rvTransfers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransfers.setAdapter(transferAdapter);
        rvTransfers.setNestedScrollingEnabled(false);
    }

    private void setupFilterSpinners() {
        // Date Range Spinner
        String[] dateRanges = {"All Time", "Today", "This Week", "This Month", "This Year"};
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                dateRanges
        );
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
    }

    private void setupSearchListener() {
        btnClearSearch.setOnClickListener(v -> etSearchTransfer.setText(""));

        etSearchTransfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase().trim();
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        List<TransferWithWallets> transfers = viewModel.getTransfersWithWallets().getValue();
        if (transfers == null) {
            transferAdapter.updateTransfers(new ArrayList<>());
            return;
        }

        List<TransferWithWallets> filtered = new ArrayList<>(transfers);

        // Filter by search text (wallet names or amount)
        if (!currentSearchText.isEmpty()) {
            filtered.removeIf(t ->
                    !t.getFromWalletName().toLowerCase().contains(currentSearchText) &&
                            !t.getToWalletName().toLowerCase().contains(currentSearchText) &&
                            !String.format(Locale.US, "%.2f", t.getAmount()).contains(currentSearchText)
            );
        }

        // Filter by date range
        if (!currentDateFilter.equals("All Time")) {
            filtered.removeIf(t -> !DateManager.isWithinDateRange(new Date(t.getDate()), currentDateFilter));
        }

        transferAdapter.updateTransfers(filtered);
    }

    private void onTransferClick(TransferWithWallets transfer) {
        TransferDetailDialog dialog = new TransferDetailDialog(requireContext(), transfer);
        dialog.setOnActionListener(transferToDelete -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_transfer))
                    .setMessage(getString(R.string.delete_transfer_confirm))
                    .setPositiveButton(getString(R.string.delete), (d, w) -> {
                        viewModel.deleteTransfer(transferToDelete);
                        Toast.makeText(requireContext(), getString(R.string.transfer_deleted), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });
        dialog.show();
    }

    private void observeData() {
        viewModel.getTransfersWithWallets().observe(getViewLifecycleOwner(), transfers -> applyFilters());
    }
}