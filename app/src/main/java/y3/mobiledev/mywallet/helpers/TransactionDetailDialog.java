package y3.mobiledev.mywallet.helpers;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager; // Import FragmentManager

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.fragments.AddTransactionDialogFragment;
import y3.mobiledev.mywallet.models.Transaction;
import y3.mobiledev.mywallet.models.TransactionWithCategory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog to show full transaction details including receipt photo.
 * This dialog can now launch the edit screen directly.
 */
public class TransactionDetailDialog extends Dialog {

    private final TransactionWithCategory transaction;
    private OnActionListener actionListener;
    private final FragmentManager fragmentManager; // Variable to hold the FragmentManager

    // Views
    private ImageView ivCategoryIcon, ivReceipt;
    private TextView tvCategoryName, tvDescription, tvAmount, tvDate, tvReceiptHint;
    private Button btnEdit, btnDelete, btnClose;
    private View vIconBackground;
    private CardView cvReceipt;
    private LinearLayout layoutNoReceipt;

    public interface OnActionListener {
        // onEdit is no longer needed here as this dialog handles it.
        void onDelete(Transaction transaction);
    }

    /**
     * Updated constructor to accept a FragmentManager.
     *
     * @param context         The context.
     * @param fragmentManager The FragmentManager from the calling Fragment/Activity.
     * @param transaction     The transaction data to display.
     */
    public TransactionDetailDialog(@NonNull Context context, FragmentManager fragmentManager, TransactionWithCategory transaction) {
        super(context);
        this.fragmentManager = fragmentManager; // Store the FragmentManager
        this.transaction = transaction;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_transaction_detail);

        // Make dialog full width and transparent background
        if (getWindow() != null) {
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.95),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        populateData();
        setupListeners();
    }

    private void initViews() {
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        ivReceipt = findViewById(R.id.ivReceipt);
        vIconBackground = findViewById(R.id.vIconBackground);
        cvReceipt = findViewById(R.id.cvReceipt);
        layoutNoReceipt = findViewById(R.id.layoutNoReceipt);

        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvDescription = findViewById(R.id.tvDescription);
        tvAmount = findViewById(R.id.tvAmount);
        tvDate = findViewById(R.id.tvDate);
        tvReceiptHint = findViewById(R.id.tvReceiptHint);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnClose = findViewById(R.id.btnClose);
    }

    private void populateData() {
        // Category icon and background
        tvCategoryName.setText(transaction.getCategoryName());
        ivCategoryIcon.setImageResource(transaction.getCategoryIcon());

        GradientDrawable drawable = (GradientDrawable) vIconBackground.getBackground();
        drawable.setColor(ContextCompat.getColor(getContext(), transaction.getCategoryColor()));

        // Description
        String description = transaction.getDescription();
        if (description == null || description.isEmpty()) {
            tvDescription.setText(getContext().getString(R.string.no_description));
            tvDescription.setTextColor(ContextCompat.getColor(getContext(), R.color.text_gray));
        } else {
            tvDescription.setText(description);
            tvDescription.setTextColor(ContextCompat.getColor(getContext(), R.color.text_black));
        }

        // Amount
        String amountText = CurrencyUtils.formatTransactionAmount(transaction.getAmount(), transaction.isExpense());
        if (transaction.isExpense()) {
            tvAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.expense_red));
        } else {
            tvAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.income_green));
        }
        tvAmount.setText(amountText);

        // Date
        Date date = new Date(transaction.getDate());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US);
        tvDate.setText(dateFormat.format(date));

        // Receipt photo
        loadReceiptPhoto();
    }

    private void loadReceiptPhoto() {
        String photoPath = transaction.getReceiptPhotoUri();

        if (PhotoManager.hasReceipt(photoPath)) {
            Bitmap bitmap = PhotoManager.loadReceiptPhoto(photoPath);
            if (bitmap != null) {
                ivReceipt.setImageBitmap(bitmap);
                ivReceipt.setVisibility(View.VISIBLE);
                layoutNoReceipt.setVisibility(View.GONE);
                tvReceiptHint.setVisibility(View.VISIBLE);
                cvReceipt.setOnClickListener(v -> showFullScreenImage(bitmap));
            } else {
                showNoReceipt();
            }
        } else {
            showNoReceipt();
        }
    }

    private void showNoReceipt() {
        ivReceipt.setVisibility(View.GONE);
        layoutNoReceipt.setVisibility(View.VISIBLE);
        tvReceiptHint.setVisibility(View.GONE);
        cvReceipt.setOnClickListener(null);
    }

    private void showFullScreenImage(Bitmap bitmap) {
        Dialog fullScreenDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullScreenDialog.setContentView(R.layout.dialog_fullscreen_image);

        ImageView ivFullscreen = fullScreenDialog.findViewById(R.id.ivFullscreenImage);
        ivFullscreen.setImageBitmap(bitmap);

        // Close on image click
        ivFullscreen.setOnClickListener(v -> fullScreenDialog.dismiss());
        fullScreenDialog.show();
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnEdit.setOnClickListener(v -> {
            // 1. Create the dialog for editing
            AddTransactionDialogFragment editDialog = AddTransactionDialogFragment.newInstanceForEdit(transaction.toTransaction());

            // 2. Show the new edit dialog using the stored fragmentManager
            editDialog.show(fragmentManager, "EditTransactionDialog");

            // 3. Immediately dismiss the current detail dialog
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(transaction.toTransaction());
            }
            dismiss();
        });
    }
}