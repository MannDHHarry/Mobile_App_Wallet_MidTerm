package y3.mobiledev.mywallet.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;
import y3.mobiledev.mywallet.helpers.PickersAndDialog;
import y3.mobiledev.mywallet.models.Wallet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransferDialogFragment extends DialogFragment {

    private TransactionViewModel viewModel;
    private EditText etAmount;
    private TextView tvFromWallet, tvToWallet, tvFromBalance, tvSelectedDate;
    private Button btnTransfer, btnCancel;
    private View layoutFromWallet, layoutToWallet, layoutDate;

    private Wallet fromWallet;
    private Wallet toWallet;
    private Date selectedDate;
    private List<Wallet> wallets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_transfer, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        initViews(view);
        setupListeners();
        observeData();

        selectedDate = new Date();
        updateDateDisplay();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog full width
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void initViews(View view) {
        etAmount = view.findViewById(R.id.etAmount);
        tvFromWallet = view.findViewById(R.id.tvFromWallet);
        tvToWallet = view.findViewById(R.id.tvToWallet);
        tvFromBalance = view.findViewById(R.id.tvFromBalance);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        btnTransfer = view.findViewById(R.id.btnTransfer);
        btnCancel = view.findViewById(R.id.btnCancel);
        layoutFromWallet = view.findViewById(R.id.layoutFromWallet);
        layoutToWallet = view.findViewById(R.id.layoutToWallet);
        layoutDate = view.findViewById(R.id.layoutDate);
    }

    private void setupListeners() {
        layoutFromWallet.setOnClickListener(v -> showFromWalletPicker());
        layoutToWallet.setOnClickListener(v -> showToWalletPicker());
        layoutDate.setOnClickListener(v -> showDatePicker());
        btnTransfer.setOnClickListener(v -> executeTransfer());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void observeData() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), walletList -> {
            if (walletList != null) {
                wallets = new ArrayList<>(walletList);
            }
        });
    }

    private void showFromWalletPicker() {
        PickersAndDialog.showWalletPicker(requireContext(), wallets, item -> {
            if (item instanceof Wallet) {
                fromWallet = (Wallet) item;
                tvFromWallet.setText(fromWallet.getName());
                tvFromBalance.setText(String.format(Locale.US, "Balance: $%,.2f",
                        fromWallet.getBalance()));
                tvFromBalance.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showToWalletPicker() {
        PickersAndDialog.showWalletPicker(requireContext(), wallets, item -> {
            if (item instanceof Wallet) {
                toWallet = (Wallet) item;
                tvToWallet.setText(toWallet.getName());
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    selectedDate = cal.getTime();
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateDisplay() {
        Calendar today = Calendar.getInstance();
        resetTime(today);

        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTime(selectedDate);
        resetTime(selectedCal);

        String dateText;
        if (selectedCal.equals(today)) {
            SimpleDateFormat format = new SimpleDateFormat("MMM d", Locale.US);
            dateText = "Today, " + format.format(selectedDate);
        } else {
            SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d", Locale.US);
            dateText = format.format(selectedDate);
        }
        tvSelectedDate.setText(dateText);
    }

    private void resetTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void executeTransfer() {
        // Validate amount
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(requireContext(), "Amount must be greater than 0",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate wallets
        if (fromWallet == null) {
            Toast.makeText(requireContext(), "Please select source wallet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (toWallet == null) {
            Toast.makeText(requireContext(), "Please select destination wallet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate not same wallet
        if (fromWallet.getWalletId() == toWallet.getWalletId()) {
            Toast.makeText(requireContext(), "Cannot transfer to the same wallet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate sufficient balance
        if (fromWallet.getBalance() < amount) {
            Toast.makeText(requireContext(), "Insufficient balance in source wallet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Execute transfer
        viewModel.executeTransfer(
                fromWallet.getWalletId(),
                toWallet.getWalletId(),
                amount,
                selectedDate.getTime()
        );

        Toast.makeText(requireContext(), "Transfer completed!", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}