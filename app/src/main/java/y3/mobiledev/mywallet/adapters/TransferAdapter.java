package y3.mobiledev.mywallet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.Converters;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.models.TransferWithWallets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransferAdapter extends RecyclerView.Adapter<TransferAdapter.TransferViewHolder> {

    private final Context context;
    private List<TransferWithWallets> transfers;
    private final OnTransferClickListener listener;

    public interface OnTransferClickListener {
        void onTransferClick(TransferWithWallets transfer);
    }

    public TransferAdapter(Context context, List<TransferWithWallets> transfers,
                           OnTransferClickListener listener) {
        this.context = context;
        this.transfers = transfers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transfer, parent, false);
        return new TransferViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferViewHolder holder, int position) {
        TransferWithWallets transfer = transfers.get(position);
        holder.bind(transfer);
    }

    @Override
    public int getItemCount() {
        return transfers != null ? transfers.size() : 0;
    }

    public void updateTransfers(List<TransferWithWallets> newTransfers) {
        this.transfers = newTransfers;
        notifyDataSetChanged();
    }

    public static class TransferViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivTransferIcon;
        private final TextView tvWallets;
        private final TextView tvAmount;
        private final TextView tvDate;
        private final View vIconBackground;

        public TransferViewHolder(@NonNull View itemView, OnTransferClickListener listener) {
            super(itemView);
            ivTransferIcon = itemView.findViewById(R.id.ivTransferIcon);
            tvWallets = itemView.findViewById(R.id.tvWallets);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            vIconBackground = itemView.findViewById(R.id.vIconBackground);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    TransferWithWallets transfer = (TransferWithWallets) itemView.getTag();
                    if (transfer != null) {
                        listener.onTransferClick(transfer);
                    }
                }
            });
        }

        public void bind(TransferWithWallets transfer) {
            itemView.setTag(transfer);

            // Display: "From → To"
            String walletsText = transfer.getFromWalletName() + " → " + transfer.getToWalletName();
            tvWallets.setText(walletsText);

            // Amount (no +/- sign, neutral display)
            String amountText = String.format(Locale.US, "$%,.2f", transfer.getAmount());
            tvAmount.setText(amountText);

            // Date
            Date transferDate = Converters.fromTimestamp(transfer.getDate());
            tvDate.setText(formatDate(transferDate));

            // Icon is already set in XML (transfer icon)
        }

        private String formatDate(Date date) {
            if (date == null) return "";

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Calendar now = Calendar.getInstance();
            boolean sameYear = calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR);
            SimpleDateFormat dateFormat = sameYear ?
                    new SimpleDateFormat("d MMM", Locale.US) :
                    new SimpleDateFormat("d MMM yyyy", Locale.US);
            return dateFormat.format(date);
        }
    }
}