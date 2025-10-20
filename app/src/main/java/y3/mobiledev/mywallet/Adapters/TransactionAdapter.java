package y3.mobiledev.mywallet.Adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import y3.mobiledev.mywallet.Helpers.TransactionManager;
import y3.mobiledev.mywallet.Models.Transaction;
import y3.mobiledev.mywallet.Models.TransactionGroup;
import y3.mobiledev.mywallet.R;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ---- Constants ----
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    // ---- Fields ----
    private final Context context;
    private List<TransactionGroup> transactionGroups;
    private final OnTransactionClickListener listener;

    // ---- Interface ----
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    // Constructor
    public TransactionAdapter(Context context, List<TransactionGroup> transactionGroups, OnTransactionClickListener listener) {
        this.context = context;
        this.transactionGroups = transactionGroups;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        int currentPos = 0;
        for (TransactionGroup group : transactionGroups) {
            // Check if the position is the header of this group
            if (position == currentPos) {
                return VIEW_TYPE_HEADER;
            }
            currentPos++; // Move past the header

            // Check if the position is within the items of this group
            if (position < currentPos + group.getTransactions().size()) {
                return VIEW_TYPE_ITEM;
            }
            currentPos += group.getTransactions().size(); // Move past the items of this group
        }
        throw new IllegalArgumentException("Invalid position " + position + " in TransactionAdapter");
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view,listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            TransactionGroup group = getGroupForPosition(position);
            if (group != null) {
                headerHolder.tvSectionHeader.setText(group.getHeader());
            }
        } else if (holder instanceof TransactionViewHolder) {
            TransactionViewHolder transactionHolder = (TransactionViewHolder) holder;
            Transaction transaction = getTransactionForPosition(position);
            if (transaction != null) {
                bindTransaction(transactionHolder, transaction);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (transactionGroups == null) {
            return 0;
        }
        int count = 0;
        for (TransactionGroup group : transactionGroups) {
            count += 1; // Header
            count += group.getTransactions().size(); // Items
        }
        return count;
    }

    /// ---- ViewHolder Classes ----
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionHeader = itemView.findViewById(R.id.tvSectionHeader);
        }
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        View vIconBackground;
        ImageView ivCategoryIcon;
        TextView tvCategory;
        TextView tvDescription;
        TextView tvAmount;
        TextView tvDate;

        public TransactionViewHolder(@NonNull View itemView, OnTransactionClickListener listener) {
            super(itemView);
            vIconBackground = itemView.findViewById(R.id.vIconBackground);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onTransactionClick((Transaction) itemView.getTag());
                }
            });
        }
    }

    // ---- Data Binding ----
    private void bindTransaction(TransactionViewHolder holder, Transaction transaction) {
        holder.itemView.setTag(transaction); // Store transaction for click listener
        setCategoryText(holder, transaction);
        setDescriptionText(holder, transaction);
        setDateText(holder, transaction);
        setAmountText(holder, transaction);
        setIconAndBackground(holder, transaction);
    }

    private void setCategoryText(TransactionViewHolder holder, Transaction transaction) {
        holder.tvCategory.setText(transaction.getCategory());
    }

    private void setDescriptionText(TransactionViewHolder holder, Transaction transaction) {
        String description = TransactionManager.truncateToWords(
                transaction.getDescription(),
                15,
                true
        );

        if (description.isEmpty()) {
            description = "";
        } else if (description.split("\\s+").length >= 15) {
            description += "...";
        }
        holder.tvDescription.setText(description);
    }

    private void setDateText(TransactionViewHolder holder, Transaction transaction) {
        holder.tvDate.setText(formatDate(transaction.getDate()));
    }

    private void setAmountText(TransactionViewHolder holder, Transaction transaction) {
        String amountText;
        if (transaction.isExpense()) {
            amountText = String.format(Locale.US, "-$%,.2f", transaction.getAmount());
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
        } else {
            amountText = String.format(Locale.US, "+$%,.2f", transaction.getAmount());
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
        }
        holder.tvAmount.setText(amountText);
    }

    private void setIconAndBackground(TransactionViewHolder holder, Transaction transaction) {
        holder.ivCategoryIcon.setImageResource(transaction.getCategoryIconResId());
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ContextCompat.getColor(context, transaction.getCategoryColor()));
        holder.vIconBackground.setBackground(drawable);
    }

    // ---- Helper Methods ----
    private String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar now = Calendar.getInstance();
        boolean sameYear = calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR);
        SimpleDateFormat dateFormat = sameYear ?
                new SimpleDateFormat("d MMM", Locale.US) :
                new SimpleDateFormat("d MMM yyyy", Locale.US);
        return dateFormat.format(date);
    }

    private TransactionGroup getGroupForPosition(int position) {
        int currentPos = 0;
        for (TransactionGroup group : transactionGroups) {
            int groupEndPos = currentPos + group.getTransactions().size();
            if (position >= currentPos && position <= groupEndPos) {
                return group;
            }
            currentPos = groupEndPos + 1;
        }
        return null;
    }

    private Transaction getTransactionForPosition(int position) {
        int currentPos = 0;
        for (TransactionGroup group : transactionGroups) {
            if (position == currentPos) {
                return null;
            }
            currentPos++;
            int groupSize = group.getTransactions().size();
            if (position < currentPos + groupSize) {
                return group.getTransactions().get(position - currentPos);
            }
            currentPos += groupSize;
        }
        return null;
    }


    // ---- Data Updates ----
    public void updateTransactions(List<TransactionGroup> newGroups) {
        this.transactionGroups = newGroups;
        notifyDataSetChanged();
    }
}
