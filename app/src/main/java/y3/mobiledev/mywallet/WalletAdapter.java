package y3.mobiledev.mywallet;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.Wallet;

import java.util.List;
import java.util.Locale;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

    private Context context;
    private List<Wallet> walletList;
    private OnWalletClickListener listener;

    // Interface for click listener
    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    // Constructor
    public WalletAdapter(Context context, List<Wallet> walletList, OnWalletClickListener listener) {
        this.context = context;
        this.walletList = walletList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallet, parent, false);
        return new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        final Wallet wallet = walletList.get(position);

        // Set wallet name
        holder.tvWalletName.setText(wallet.getName());

        // Set wallet balance with formatting
        holder.tvWalletBalance.setText(String.format(Locale.US, "$%,.2f", wallet.getBalance()));

        // Set wallet icon
        holder.ivWalletIcon.setImageResource(wallet.getIconResId());

        // Set click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onWalletClick(wallet);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return walletList != null ? walletList.size() : 0;
    }

    // ViewHolder class
    public static class WalletViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWalletIcon;
        TextView tvWalletName;
        TextView tvWalletBalance;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWalletIcon = itemView.findViewById(R.id.ivWalletIcon);
            tvWalletName = itemView.findViewById(R.id.tvWalletName);
            tvWalletBalance = itemView.findViewById(R.id.tvWalletBalance);
        }
    }

    // Method to update wallet list
    public void updateWallets(List<Wallet> newWallets) {
        this.walletList = newWallets;
        notifyDataSetChanged();
    }
}