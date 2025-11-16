package y3.mobiledev.mywallet.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.Models.Wallet;
import y3.mobiledev.mywallet.R;

import java.util.List;
import java.util.Locale;

public class WalletManagementAdapter extends RecyclerView.Adapter<WalletManagementAdapter.WalletViewHolder> {

    // ---- Fields ----
    private final Context context;
    private List<Wallet> walletList;
    private final OnWalletClickListener listener;
    private final OnEditClickListener onEditClick;
    private final OnDeleteClickListener onDeleteClick;
    private boolean showActions;

    // ---- Interfaces ----
    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    public interface OnEditClickListener {
        void onEditClick(Wallet wallet);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Wallet wallet);
    }

    // ---- Constructors ----
    // Simple click listener (for HomeFragment)
    public WalletManagementAdapter(Context context, List<Wallet> walletList, OnWalletClickListener listener) {
        this(context, walletList, listener, null, null, false);
    }

    // With edit/delete buttons (for CategoriesFragment)
    public WalletManagementAdapter(Context context, List<Wallet> walletList,
                                   OnEditClickListener onEditClick,
                                   OnDeleteClickListener onDeleteClick) {
        this.context = context;
        this.walletList = walletList;
        this.listener = null;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
        this.showActions = true;
    }

    // Full constructor
    private WalletManagementAdapter(Context context, List<Wallet> walletList, OnWalletClickListener listener,
                                    OnEditClickListener onEditClick, OnDeleteClickListener onDeleteClick,
                                    boolean showActions) {
        this.context = context;
        this.walletList = walletList;
        this.listener = listener;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
        this.showActions = showActions;
    }

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = showActions ? R.layout.item_wallet_management : R.layout.item_wallet;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new WalletViewHolder(view);
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

    // ---- ViewHolder ----
    public class WalletViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWalletIcon;
        TextView tvWalletName;
        TextView tvWalletBalance;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWalletIcon = itemView.findViewById(R.id.ivWalletIcon);
            tvWalletName = itemView.findViewById(R.id.tvWalletName);
            tvWalletBalance = itemView.findViewById(R.id.tvWalletBalance);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    // ---- Data Binding ----
    private void bindWallet(WalletViewHolder holder, Wallet wallet) {
        setWalletIcon(holder, wallet);
        setWalletName(holder, wallet);
        setWalletBalance(holder, wallet);

        if (showActions && onEditClick != null && onDeleteClick != null) {
            // Management mode (with edit/delete buttons)
            if (holder.btnEdit != null) {
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnEdit.setOnClickListener(v -> onEditClick.onEditClick(wallet));
            }
            if (holder.btnDelete != null) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> onDeleteClick.onDeleteClick(wallet));
            }
        } else if (listener != null) {
            // Simple click mode (for HomeFragment)
            holder.itemView.setTag(wallet);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onWalletClick(wallet);
                }
            });
        }
    }

    private void setWalletName(WalletViewHolder holder, Wallet wallet) {
        holder.tvWalletName.setText(wallet.getName());
        holder.tvWalletName.setTextColor(ContextCompat.getColor(context, R.color.text_black));
    }

    private void setWalletBalance(WalletViewHolder holder, Wallet wallet) {
        holder.tvWalletBalance.setText(String.format(Locale.US, "$%,.2f", wallet.getBalance()));
        if (showActions) {
            holder.tvWalletBalance.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
        }
    }

    private void setWalletIcon(WalletViewHolder holder, Wallet wallet) {
        if (wallet.getIconResId() != 0) {
            holder.ivWalletIcon.setImageResource(R.drawable.purse);
            if (showActions) {
                holder.ivWalletIcon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
            }
        }
    }

    // ---- Data Updates ----
    public void updateWallets(List<Wallet> newWallets) {
        this.walletList = newWallets;
        notifyDataSetChanged();
    }
}