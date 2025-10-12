package y3.mobiledev.mywallet;

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

import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.Transaction;
import y3.mobiledev.mywallet.TransactionGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private Context context;
    private List<TransactionGroup> transactionGroups;
    private OnTransactionClickListener listener;

    // Interface for click listener
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }

    // Constructor
    public TransactionAdapter(Context context, List<TransactionGroup> transactionGroups,
                              OnTransactionClickListener listener) {
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
        // This should not happen if getItemCount() is correct, but as a fallback:
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
            return new TransactionViewHolder(view);
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
            final Transaction transaction = getTransactionForPosition(position);
            if (transaction != null) {
                bindTransaction(transactionHolder, transaction);
            }
        }
    }

    private void bindTransaction(TransactionViewHolder holder, final Transaction transaction) {
        // Set category name
        holder.tvCategory.setText(transaction.getCategory());

        // Set description (max 15 words, truncate if needed)
        String description = truncateDescription(transaction.getDescription(), 15);
        holder.tvDescription.setText(description);

        // Set date
        holder.tvDate.setText(formatDate(transaction.getDate()));

        // Set amount with color
        String amountText;
        if (transaction.isExpense()) {
            amountText = String.format(Locale.US, "-$%,.2f", transaction.getAmount());
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
        } else {
            amountText = String.format(Locale.US, "+$%,.2f", transaction.getAmount());
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.income_green));
        }
        holder.tvAmount.setText(amountText);

        // Set category icon
        holder.ivCategoryIcon.setImageResource(transaction.getCategoryIconResId());

        // Set icon background color
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ContextCompat.getColor(context, transaction.getCategoryColor()));
        holder.vIconBackground.setBackground(drawable);

        // Click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTransactionClick(transaction);
                }
            }
        });
    }

    // Truncate description to max words
    private String truncateDescription(String description, int maxWords) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        String[] words = description.split("\\s+");
        if (words.length <= maxWords) {
            return description;
        }
        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            truncated.append(words[i]).append(" ");
        }
        return truncated.toString().trim() + "...";
    }

    // Format date: "12 Jan" or "12 Jan 2024"
    private String formatDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar now = Calendar.getInstance();

        // Check if same year
        boolean sameYear = calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR);

        SimpleDateFormat dateFormat;
        if (sameYear) {
            dateFormat = new SimpleDateFormat("d MMM", Locale.US); // "12 Jan"
        } else {
            dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.US); // "12 Jan 2024"
        }

        return dateFormat.format(date);
    }

    // Get transaction group for position
    private TransactionGroup getGroupForPosition(int position) {
        int currentPos = 0;
        for (TransactionGroup group : transactionGroups) {
            int groupEndPos = currentPos + group.getTransactions().size();
            if (position >= currentPos && position <= groupEndPos) {
                return group;
            }
            currentPos = groupEndPos + 1; // Move to the start of the next group
        }
        return null;
    }

    // Get transaction for position
    private Transaction getTransactionForPosition(int position) {
        int currentPos = 0;
        for (TransactionGroup group : transactionGroups) {
            // Is the position the header? If so, it's not an item.
            if (position == currentPos) {
                return null; // Or handle as an error
            }
            currentPos++; // Move past the header

            int groupSize = group.getTransactions().size();
            if (position < currentPos + groupSize) {
                int itemIndex = position - currentPos;
                return group.getTransactions().get(itemIndex);
            }
            currentPos += groupSize; // Move past the items of this group
        }
        return null;
    }
    @Override
    public int getItemCount() {
        int count = 0;
        for (TransactionGroup group : transactionGroups) {
            count += 1; // Header
            count += group.getTransactions().size(); // Items
        }
        return count;
    }

    // ViewHolder for section headers (Today, Yesterday, Earlier)
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionHeader = itemView.findViewById(R.id.tvSectionHeader);
        }
    }

    // ViewHolder for transaction items
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        View vIconBackground;
        ImageView ivCategoryIcon;
        TextView tvCategory;
        TextView tvDescription;
        TextView tvAmount;
        TextView tvDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            vIconBackground = itemView.findViewById(R.id.vIconBackground);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    // Method to update transaction list
    public void updateTransactions(List<TransactionGroup> newGroups) {
        this.transactionGroups = newGroups;
        notifyDataSetChanged();
    }
}
