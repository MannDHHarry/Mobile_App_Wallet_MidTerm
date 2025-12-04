package y3.mobiledev.mywallet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.models.Subscription;
import y3.mobiledev.mywallet.R;
import y3.mobiledev.mywallet.helpers.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private final Context context;
    private List<Subscription> subscriptions;
    private final OnSubscriptionActionListener listener;

    public interface OnSubscriptionActionListener {
        void onEditClick(Subscription subscription);
        void onToggleClick(Subscription subscription);
        void onDeleteClick(Subscription subscription);
    }

    public SubscriptionAdapter(Context context, List<Subscription> subscriptions,
                               OnSubscriptionActionListener listener) {
        this.context = context;
        this.subscriptions = subscriptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        Subscription subscription = subscriptions.get(position);

        // Set name
        holder.tvName.setText(subscription.getName());

        // Set amount (VND, compact)
        holder.tvAmount.setText(CurrencyUtils.formatPlainAmount(subscription.getAmount()));

        // Set next billing date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
        String nextBillingText = "Next: " +
                dateFormat.format(new Date(subscription.getNextBillingDate()));
        holder.tvNextBilling.setText(nextBillingText);

        // Set status
        if (subscription.isActive()) {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.income_green));
            holder.btnToggle.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.tvStatus.setText("Paused");
            holder.tvStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.text_gray));
            holder.btnToggle.setImageResource(android.R.drawable.ic_media_play);
        }

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(subscription);
            }
        });

        holder.btnToggle.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleClick(subscription);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(subscription);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptions != null ? subscriptions.size() : 0;
    }

    public void updateSubscriptions(List<Subscription> newSubscriptions) {
        this.subscriptions = newSubscriptions;
        notifyDataSetChanged();
    }

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvNextBilling, tvStatus;
        ImageButton btnEdit, btnToggle, btnDelete;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubscriptionName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvNextBilling = itemView.findViewById(R.id.tvNextBilling);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnToggle = itemView.findViewById(R.id.btnToggle);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}