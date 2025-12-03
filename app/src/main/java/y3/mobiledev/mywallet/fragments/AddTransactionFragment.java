package y3.mobiledev.mywallet.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.fragment.app.Fragment;
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
    private Button btnSave, btnAddReceipt;
    private ImageView ivReceiptPreview, ivRemoveReceipt;
    private CardView cvReceiptPreview;
    private TransactionViewModel viewModel;

    private List<Wallet> wallets = new ArrayList<>();
    private Category selectedCategory;
    private Wallet selectedWallet;
    private Date selectedDate;
    private boolean isExpense = true;


    // Photo handling
    private Uri selectedPhotoUri;
    private String savedPhotoPath;

    // Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_transaction, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        setupLaunchers();
        initViews(view);
        setupListeners();
        observeData();
        selectedDate = new Date();
        updateDateDisplay();
        etAmount.requestFocus();

        return view;
    }

    private void setupLaunchers() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        selectedPhotoUri = result.getData().getData();
                        updateReceiptPreview();
                    }
                }
        );

        // Permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        launchImagePicker();
                    } else {
                        Toast.makeText(requireContext(),
                                "Permission denied. Cannot select photo.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    //Init Views by signing them to variables
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

        // Receipt views
        btnAddReceipt = view.findViewById(R.id.btnAddReceipt);
        ivReceiptPreview = view.findViewById(R.id.ivReceiptPreview);
        ivRemoveReceipt = view.findViewById(R.id.ivRemoveReceipt);
        cvReceiptPreview = view.findViewById(R.id.cvReceiptPreview);

    }

    //Setting up ClickListeners
    private void setupListeners() {

        //Save Transaction
        btnSave.setOnClickListener(v -> saveTransaction());

        //Radio Group for Income or Expense
        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            isExpense = checkedId == R.id.rbExpense;
            selectedCategory = null;
            tvSelectedCategory.setText("Select Category");
        });

        //Category , Wallet and Date pickers
        layoutCategoryPicker.setOnClickListener(v -> onCategoryPicker());
        layoutWalletPicker.setOnClickListener(v -> onWalletPicker());
        layoutDatePicker.setOnClickListener(v -> showDatePicker());

        // Receipt photo button
        btnAddReceipt.setOnClickListener(v -> openImagePicker());

        // Remove receipt button
        ivRemoveReceipt.setOnClickListener(v -> clearReceipt());

    }

    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below
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
            btnAddReceipt.setText("Change Receipt Photo");
        } else {
            cvReceiptPreview.setVisibility(View.GONE);
            btnAddReceipt.setText("Add Receipt Photo");
        }
    }

    private void clearReceipt() {
        selectedPhotoUri = null;
        savedPhotoPath = null;
        updateReceiptPreview();
        Toast.makeText(requireContext(), "Receipt removed", Toast.LENGTH_SHORT).show();
    }



    //Save Transaction with Input Validation
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

        String notes = TransactionManager.truncateToWords(etNotes.getText().toString().trim(), MAX_NOTE_WORDS, false);

        // Save photo if selected
        if (selectedPhotoUri != null) {
            savedPhotoPath = PhotoManager.saveReceiptPhoto(requireContext(), selectedPhotoUri);
            if (savedPhotoPath == null) {
                Toast.makeText(requireContext(), "Failed to save receipt photo", Toast.LENGTH_SHORT).show();
                // Continue anyway - photo is optional
            }
        }

        // Call addTransaction with receipt photo path
        viewModel.addTransaction(
                selectedWallet.getWalletId(),
                selectedCategory.getCategoryId(),
                notes,
                amount,
                selectedDate.getTime(),
                isExpense,
                savedPhotoPath
        );



        Toast.makeText(requireContext(), "Transaction saved!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    //Three Pickers Category , Wallet and Date
    private void onCategoryPicker() {
        List<Category> categoriesToShow = isExpense ?
                viewModel.getExpenseCategories().getValue() :
                viewModel.getIncomeCategories().getValue();

        if (categoriesToShow == null) {
            categoriesToShow = new ArrayList<>();
        }

        PickersAndDialog.showCategoryPicker(requireContext(), categoriesToShow, isExpense,
                item -> {
                    if (item instanceof String) {
                        onAddCategory();  // "+ Add New" clicked
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
                        onAddWallet();  // "+ Add New Wallet" clicked
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

    //Calling Dialogs for new Category and new Wallet
    private void onAddCategory() {
        CategoryWalletManager.callAddCategoryDialog(
                requireContext(),
                viewModel,
                isExpense,
                () -> {
                    // pick the newly created category (last in the list)
                    List<Category> list = isExpense
                            ? viewModel.getExpenseCategories().getValue()
                            : viewModel.getIncomeCategories().getValue();
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
                    // After wallet is added, observe LiveData to get the new wallet
                    List<Wallet> currentWallets = wallets;
                    if (currentWallets != null && !currentWallets.isEmpty()) {
                        selectedWallet = currentWallets.get(currentWallets.size() - 1);
                        tvSelectedWallet.setText(selectedWallet.getName());
                    }
                });
    }

    //Helpers
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



    //Live Data Observer
    private void observeData() {
        viewModel.getWallets().observe(getViewLifecycleOwner(), walletList -> {
            if (walletList != null) {
                wallets = new ArrayList<>(walletList);
            }
        });
    }


}