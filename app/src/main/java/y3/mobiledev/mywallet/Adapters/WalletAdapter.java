package y3.mobiledev.mywallet.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.Models.Wallet;
import y3.mobiledev.mywallet.R;

import java.util.List;
import java.util.Locale;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

    // Fields
    private final Context context;
    private List<Wallet> walletList;
    private final OnWalletClickListener listener;

    // Interface
    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    // Constructor
    public WalletAdapter(Context context, List<Wallet> walletList, OnWalletClickListener listener) {
        this.context = context;
        this.walletList = walletList;
        this.listener = listener;
    }

    // Adapter Methods
    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallet, parent, false);
        return new WalletViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        Wallet wallet = walletList.get(position);
        if (wallet != null) {
            bindWallet(holder, wallet);
        }
    }

    @Override
    public int getItemCount() {
        return walletList != null ? walletList.size() : 0;
    }

    // ViewHolder Class
    public static class WalletViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWalletIcon;
        TextView tvWalletName;
        TextView tvWalletBalance;

        public WalletViewHolder(@NonNull View itemView, OnWalletClickListener listener) {
            super(itemView);
            ivWalletIcon = itemView.findViewById(R.id.ivWalletIcon);
            tvWalletName = itemView.findViewById(R.id.tvWalletName);
            tvWalletBalance = itemView.findViewById(R.id.tvWalletBalance);
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onWalletClick((Wallet) itemView.getTag());
                }
            });
        }
    }

    // Data Binding Methods
    private void bindWallet(WalletViewHolder holder, Wallet wallet) {
        holder.itemView.setTag(wallet);
        setWalletName(holder, wallet);
        setWalletBalance(holder, wallet);
        setWalletIcon(holder, wallet);
    }

    private void setWalletName(WalletViewHolder holder, Wallet wallet) {
        holder.tvWalletName.setText(wallet.getName());
    }

    private void setWalletBalance(WalletViewHolder holder, Wallet wallet) {
        holder.tvWalletBalance.setText(String.format(Locale.US, "$%,.2f", wallet.getBalance()));
    }

    private void setWalletIcon(WalletViewHolder holder, Wallet wallet) {
        holder.ivWalletIcon.setImageResource(R.drawable.purse);
    }

    // Data Update Method
    public void updateWallets(List<Wallet> newWallets) {
        this.walletList = newWallets;
        notifyDataSetChanged();
    }
}