package y3.mobiledev.mywallet.helpers;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.models.Transfer;
import y3.mobiledev.mywallet.models.TransferWithWallets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog to show transfer details with delete option
 */
public class TransferDetailDialog extends Dialog {

    private TransferWithWallets transfer;
    private OnActionListener actionListener;

    // Views
    private TextView tvAmount, tvFromWallet, tvToWallet, tvDate;
    private Button btnDelete, btnClose;

    public interface OnActionListener {
        void onDelete(Transfer transfer);
    }

    public TransferDetailDialog(@NonNull Context context, TransferWithWallets transfer) {
        super(context);
        this.transfer = transfer;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_transfer_detail);

        // Make dialog full width
        if (getWindow() != null) {
            getWindow().setLayout(
                    (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        populateData();
        setupListeners();
    }

    private void initViews() {
        tvAmount = findViewById(R.id.tvAmount);
        tvFromWallet = findViewById(R.id.tvFromWallet);
        tvToWallet = findViewById(R.id.tvToWallet);
        tvDate = findViewById(R.id.tvDate);
        btnDelete = findViewById(R.id.btnDelete);
        btnClose = findViewById(R.id.btnClose);
    }

    private void populateData() {
        // Amount
        String amountText = String.format(Locale.US, "$%,.2f", transfer.getAmount());
        tvAmount.setText(amountText);

        // From wallet
        tvFromWallet.setText(transfer.getFromWalletName());

        // To wallet
        tvToWallet.setText(transfer.getToWalletName());

        // Date
        Date date = new Date(transfer.getDate());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US);
        tvDate.setText(dateFormat.format(date));
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(transfer.toTransfer());
            }
            dismiss();
        });
    }
}