package y3.mobiledev.mywallet.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import y3.mobiledev.mywallet.helpers.PhotoManager;
import y3.mobiledev.mywallet.helpers.PickersAndDialog;
import y3.mobiledev.mywallet.helpers.CategoryWalletManager;
import y3.mobiledev.mywallet.helpers.TransactionManager;
import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.Wallet;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.TransactionViewModel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTransactionDialogFragment extends DialogFragment {
    private static final int MAX_NOTE_WORDS = 15;

    private EditText etAmount, etNotes;
    private RadioGroup rgTransactionType;
    private RelativeLayout layoutCategoryPicker, layoutWalletPicker, layoutDatePicker;
    private TextView tvSelectedCategory, tvSelectedWallet, tvSelectedDate;
    private Button btnSave, btnAddReceipt , btnCancel;
    private ImageView ivReceiptPreview, ivRemoveReceipt;
    private CardView cvReceiptPreview;
    private TransactionViewModel viewModel;
    private boolean isEditMode = false;
    private Transaction editingTransaction;
    private TextView tvDialogTitle;

    private List<Wallet> wallets = new ArrayList<>();
    private List<Category> expenseCategories = new ArrayList<>();
    private List<Category> incomeCategories = new ArrayList<>();
    private Category selectedCategory;
    private Wallet selectedWallet;
    private Date selectedDate;
    private boolean isExpense = true;

    private Uri selectedPhotoUri;
    private String savedPhotoPath;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public static AddTransactionDialogFragment newInstance() {
        return new AddTransactionDialogFragment();
    }

    public static AddTransactionDialogFragment newInstanceForEdit(Transaction transaction) {
        AddTransactionDialogFragment fragment = new AddTransactionDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("transaction", transaction); // ✅ Use putParcelable
        args.putBoolean("isEditMode", true);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar);

        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEditMode", false);
            if (isEditMode) {
                editingTransaction = getArguments().getParcelable("transaction"); // ✅ Use getParcelable
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_transaction, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupLaunchers();
        initViews(view);
        setupListeners();
        observeData();

        selectedDate = new Date();
        updateDateDisplay();

        if (isEditMode) {
            loadTransactionData();
        }

        return view;
    }

    private void setupLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        selectedPhotoUri = result.getData().getData();
                        updateReceiptPreview();
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchImagePicker();
                    } else {
                        Toast.makeText(requireContext(),
                                getString(R.string.permission_denied_photo),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initViews(View view) {
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        btnSave = view.findViewById(R.id.btnSaveTransaction);
        btnCancel = view.findViewById(R.id.btnCancel); // ADD THIS

        etAmount = view.findViewById(R.id.etAmount);
        etNotes = view.findViewById(R.id.etNotes);
        rgTransactionType = view.findViewById(R.id.rgTransactionType);
        layoutCategoryPicker = view.findViewById(R.id.layoutCategoryPicker);
        layoutWalletPicker = view.findViewById(R.id.layoutWalletPicker);
        layoutDatePicker = view.findViewById(R.id.layoutDatePicker);
        tvSelectedCategory = view.findViewById(R.id.tvSelectedCategory);
        tvSelectedWallet = view.findViewById(R.id.tvSelectedWallet);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);

        btnAddReceipt = view.findViewById(R.id.btnAddReceipt);
        ivReceiptPreview = view.findViewById(R.id.ivReceiptPreview);
        ivRemoveReceipt = view.findViewById(R.id.ivRemoveReceipt);
        cvReceiptPreview = view.findViewById(R.id.cvReceiptPreview);

        if (isEditMode) {
            tvDialogTitle.setText(R.string.edit_transaction);
            btnSave.setText(R.string.update);
        } else {
            tvDialogTitle.setText(R.string.add_transaction);
            btnSave.setText(R.string.save_transaction);
        }

    }

    private void loadTransactionData() {
        if (!isEditMode || editingTransaction == null) return;

        // Set amount
        etAmount.setText(String.valueOf(editingTransaction.getAmount()));

        // Set description
        etNotes.setText(editingTransaction.getDescription());

        // Set date
        selectedDate = new Date(editingTransaction.getDate());
        updateDateDisplay();

        // Set transaction type
        isExpense = editingTransaction.isExpense();
        if (isExpense) {
            rgTransactionType.check(R.id.rbExpense);
        } else {
            rgTransactionType.check(R.id.rbIncome);
        }

        // Set receipt if exists
        String photoPath = editingTransaction.getReceiptPhotoUri();
        if (photoPath != null && !photoPath.isEmpty()) {
            savedPhotoPath = photoPath;
            selectedPhotoUri = android.net.Uri.parse(photoPath);
            updateReceiptPreview();
        }

        // Category and Wallet will be set when LiveData loads
        // We'll match them by ID in the observer
    }


    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveTransaction());
        btnCancel.setOnClickListener(v -> dismiss());

        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            isExpense = checkedId == R.id.rbExpense;
            selectedCategory = null;
            tvSelectedCategory.setText(getString(R.string.select_category));
        });

        layoutCategoryPicker.setOnClickListener(v -> onCategoryPicker());
        layoutWalletPicker.setOnClickListener(v -> onWalletPicker());
        layoutDatePicker.setOnClickListener(v -> showDatePicker());
        btnAddReceipt.setOnClickListener(v -> openImagePicker());
        ivRemoveReceipt.setOnClickListener(v -> clearReceipt());
    }

    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void updateReceiptPreview() {
        if (selectedPhotoUri != null) {
            ivReceiptPreview.setImageURI(selectedPhotoUri);
            cvReceiptPreview.setVisibility(View.VISIBLE);
            btnAddReceipt.setText(getString(R.string.change_receipt_photo));
        } else {
            cvReceiptPreview.setVisibility(View.GONE);
            btnAddReceipt.setText(getString(R.string.add_receipt_photo));
        }
    }

    private void clearReceipt() {
        selectedPhotoUri = null;
        savedPhotoPath = null;
        updateReceiptPreview();
        Toast.makeText(requireContext(), getString(R.string.receipt_removed), Toast.LENGTH_SHORT).show();
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.please_enter_amount), Toast.LENGTH_SHORT).show();
            etAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(requireContext(), getString(R.string.amount_greater_than_zero), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), getString(R.string.invalid_amount), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory == null) {
            Toast.makeText(requireContext(), getString(R.string.please_select_category), Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedWallet == null) {
            Toast.makeText(requireContext(), getString(R.string.please_select_wallet), Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = TransactionManager.truncateToWords(etNotes.getText().toString().trim(), MAX_NOTE_WORDS, false);

        // Save new photo if selected
        if (selectedPhotoUri != null && (savedPhotoPath == null || !savedPhotoPath.equals(selectedPhotoUri.toString()))) {
            savedPhotoPath = PhotoManager.saveReceiptPhoto(requireContext(), selectedPhotoUri);
            if (savedPhotoPath == null) {
                Toast.makeText(requireContext(), getString(R.string.failed_save_receipt), Toast.LENGTH_SHORT).show();
            }
        }

        if (isEditMode && editingTransaction != null) {
            // Create new transaction object with updated values
            Transaction newTransaction = new Transaction(
                    editingTransaction.getTransactionId(),
                    editingTransaction.getUserId(),
                    selectedWallet.getWalletId(),
                    selectedCategory.getCategoryId(),
                    notes,
                    amount,
                    selectedDate.getTime(),
                    isExpense,
                    editingTransaction.getCreatedAt(),
                    savedPhotoPath
            );

            // Update using your ViewModel method
            viewModel.updateTransaction(editingTransaction, newTransaction);
            Toast.makeText(requireContext(), getString(R.string.transaction_updated), Toast.LENGTH_SHORT).show();
        } else {
            // Add new transaction
            viewModel.addTransaction(
                    selectedWallet.getWalletId(),
                    selectedCategory.getCategoryId(),
                    notes,
                    amount,
                    selectedDate.getTime(),
                    isExpense,
                    savedPhotoPath
            );
            Toast.makeText(requireContext(), getString(R.string.transaction_saved), Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }


    private void onCategoryPicker() {
        List<Category> categoriesToShow = isExpense ? expenseCategories : incomeCategories;

        PickersAndDialog.showCategoryPicker(requireContext(), categoriesToShow, isExpense,
                item -> {
                    if (item instanceof String) {
                        onAddCategory();
                    } else if (item instanceof Category) {
                        selectedCategory = (Category) item;
                        tvSelectedCategory.setText(selectedCategory.getName());
                    }
                });
    }

    private void onWalletPicker() {
        PickersAndDialog.showWalletPicker(requireContext(), wallets,
                item -> {
                    if (item instanceof String) {
                        onAddWallet();
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

    private void onAddCategory() {
        CategoryWalletManager.callAddCategoryDialog(
                requireContext(),
                viewModel,
                isExpense,
                () -> {
                    List<Category> list = isExpense ? expenseCategories : incomeCategories;
                    if (list != null && !list.isEmpty()) {
                        selectedCategory = list.get(list.size() - 1);
                        tvSelectedCategory.setText(selectedCategory.getName());
                    }
                });
    }

    private void onAddWallet() {
        CategoryWalletManager.callAddWalletDialog(
                requireContext(),
                viewModel,
                getViewLifecycleOwner(),
                () -> {
                    List<Wallet> currentWallets = wallets;
                    if (currentWallets != null && !currentWallets.isEmpty()) {
                        selectedWallet = currentWallets.get(currentWallets.size() - 1);
                        tvSelectedWallet.setText(selectedWallet.getName());
                    }
                });
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

    private void observeData() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), walletList -> {
            if (walletList != null) {
                wallets = new ArrayList<>(walletList);

                // Pre-select wallet in edit mode
                if (isEditMode && editingTransaction != null && selectedWallet == null) {
                    for (Wallet wallet : wallets) {
                        if (wallet.getWalletId() == editingTransaction.getWalletId()) {
                            selectedWallet = wallet;
                            tvSelectedWallet.setText(wallet.getName());
                            break;
                        }
                    }
                }
            }
        });

        viewModel.getExpenseCategories().observe(getViewLifecycleOwner(), categoryList -> {
            if (categoryList != null) {
                expenseCategories = new ArrayList<>(categoryList);

                // Pre-select category in edit mode (if expense)
                if (isEditMode && editingTransaction != null && editingTransaction.isExpense() && selectedCategory == null) {
                    for (Category category : expenseCategories) {
                        if (category.getCategoryId() == editingTransaction.getCategoryId()) {
                            selectedCategory = category;
                            tvSelectedCategory.setText(category.getName());
                            break;
                        }
                    }
                }
            } else {
                expenseCategories = new ArrayList<>();
            }
        });

        viewModel.getIncomeCategories().observe(getViewLifecycleOwner(), categoryList -> {
            if (categoryList != null) {
                incomeCategories = new ArrayList<>(categoryList);

                // Pre-select category in edit mode (if income)
                if (isEditMode && editingTransaction != null && !editingTransaction.isExpense() && selectedCategory == null) {
                    for (Category category : incomeCategories) {
                        if (category.getCategoryId() == editingTransaction.getCategoryId()) {
                            selectedCategory = category;
                            tvSelectedCategory.setText(category.getName());
                            break;
                        }
                    }
                }
            } else {
                incomeCategories = new ArrayList<>();
            }
        });
    }

}