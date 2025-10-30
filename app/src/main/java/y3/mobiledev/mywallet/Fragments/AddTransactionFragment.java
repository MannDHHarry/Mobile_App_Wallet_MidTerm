package y3.mobiledev.mywallet.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import y3.mobiledev.mywallet.Helpers.CategoryWalletDialog;
import y3.mobiledev.mywallet.Helpers.TransactionManager;
import y3.mobiledev.mywallet.Models.Category;
import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.Wallet;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionFragment extends Fragment {
    private static final int MAX_NOTE_WORDS = 15;

    private EditText etAmount, etNotes;
    private RadioGroup rgTransactionType;
    private RelativeLayout layoutCategoryPicker, layoutWalletPicker, layoutDatePicker;
    private TextView tvSelectedCategory, tvSelectedWallet, tvSelectedDate;
    private Button btnSave;
    private TransactionViewModel viewModel;

    private List<Wallet> wallets = new ArrayList<>();
    private Category selectedCategory;
    private Wallet selectedWallet;
    private Date selectedDate;
    private boolean isExpense = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_transaction, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        initViews(view);
        setupListeners();
        observeData();

        selectedDate = new Date();
        updateDateDisplay();
        etAmount.requestFocus();

        return view;
    }

    private void initViews(View view) {
        btnSave = view.findViewById(R.id.btnSaveTransaction);
        etAmount = view.findViewById(R.id.etAmount);
        etNotes = view.findViewById(R.id.etNotes);
        rgTransactionType = view.findViewById(R.id.rgTransactionType);
        layoutCategoryPicker = view.findViewById(R.id.layoutCategoryPicker);
        layoutWalletPicker = view.findViewById(R.id.layoutWalletPicker);
        layoutDatePicker = view.findViewById(R.id.layoutDatePicker);
        tvSelectedCategory = view.findViewById(R.id.tvSelectedCategory);
        tvSelectedWallet = view.findViewById(R.id.tvSelectedWallet);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveTransaction());

        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            isExpense = checkedId == R.id.rbExpense;
            selectedCategory = null;
            tvSelectedCategory.setText("Select Category");
        });

        layoutCategoryPicker.setOnClickListener(v -> showCategoryPicker());
        layoutWalletPicker.setOnClickListener(v -> showWalletPicker());
        layoutDatePicker.setOnClickListener(v -> showDatePicker());
    }

    private void observeData() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), walletList -> {
            if (walletList != null) {
                wallets = new ArrayList<>(walletList);
            }
        });
    }

    private void showCategoryPicker() {
        List<Category> categoriesToShow = isExpense ?
                viewModel.getExpenseCategories().getValue() :
                viewModel.getIncomeCategories().getValue();

        if (categoriesToShow == null) {
            categoriesToShow = new ArrayList<>();
        }

        CategoryWalletDialog.showCategoryPicker(requireContext(), categoriesToShow, isExpense,
                item -> {
                    if (item instanceof String) {
                        showAddCategoryDialog();  // "+ Add New" clicked
                    } else if (item instanceof Category) {
                        selectedCategory = (Category) item;
                        tvSelectedCategory.setText(selectedCategory.getName());
                    }
                });
    }

    private void showWalletPicker() {
        CategoryWalletDialog.showWalletPicker(requireContext(), wallets,
                item -> {
                    if (item instanceof String) {
                        showAddWalletDialog();  // "+ Add New Wallet" clicked
                    } else if (item instanceof Wallet) {
                        selectedWallet = (Wallet) item;
                        tvSelectedWallet.setText(selectedWallet.getName());
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

    private void saveTransaction() {
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
                Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedWallet == null) {
            Toast.makeText(requireContext(), "Please select a wallet", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = viewModel.getCurrentUser().getValue().getUserId();
        String notes = TransactionManager.truncateToWords(etNotes.getText().toString().trim(), MAX_NOTE_WORDS,false);
        int newId = (int) System.currentTimeMillis();

        Transaction newTransaction = new Transaction(
                newId, userId, selectedCategory.getName(), notes, amount,
                selectedDate, isExpense, android.R.drawable.ic_dialog_info, selectedCategory.getColorResId()
        );

        viewModel.addTransaction(newTransaction);
        viewModel.updateWalletBalance(selectedWallet.getWalletId(), amount, isExpense);

        Toast.makeText(requireContext(), "Transaction saved!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void showAddCategoryDialog() {
        CategoryWalletDialog.showAddCategoryDialog(requireContext(), (categoryName, isIncome) -> {
            Category newCategory = isIncome ?
                    viewModel.addIncomeCategory(categoryName, R.color.category_orange, android.R.drawable.ic_dialog_info) :
                    viewModel.addExpenseCategory(categoryName, R.color.category_orange, android.R.drawable.ic_dialog_info);

                selectedCategory = newCategory;
                tvSelectedCategory.setText(newCategory.getName());
                Toast.makeText(requireContext(), "Category added: " + categoryName, Toast.LENGTH_SHORT).show();

        });
    }

    private void showAddWalletDialog() {
        int userId = viewModel.getCurrentUser().getValue().getUserId();

        CategoryWalletDialog.showAddWalletDialog(requireContext(), userId, wallet -> {
            selectedWallet = wallet;
            tvSelectedWallet.setText(selectedWallet.getName());
            viewModel.addWalletDirect(wallets);
            Toast.makeText(requireContext(), "Wallet added", Toast.LENGTH_SHORT).show();
        });
    }


}