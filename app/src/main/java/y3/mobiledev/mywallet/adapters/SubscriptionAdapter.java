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
        // Enable stable IDs so RecyclerView can track items correctly
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        // Return unique subscription ID for stable identification
        if (subscriptions != null && position >= 0 && position < subscriptions.size()) {
            return subscriptions.get(position).getSubscriptionId();
        }
        return RecyclerView.NO_ID;
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
        String nextBillingText = context.getString(R.string.next_billing,
                dateFormat.format(new Date(subscription.getNextBillingDate())));
        holder.tvNextBilling.setText(nextBillingText);

        // Set status
        if (subscription.isActive()) {
            holder.tvStatus.setText(context.getString(R.string.active));
            holder.tvStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.income_green));
            holder.btnToggle.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.tvStatus.setText(context.getString(R.string.paused));
            holder.tvStatus.setBackgroundTintList(
                    ContextCompat.getColorStateList(context, R.color.text_gray));
            holder.btnToggle.setImageResource(android.R.drawable.ic_media_play);
        }

        // Set click listeners - use getBindingAdapterPosition() to get correct item at click time
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < subscriptions.size()) {
                    listener.onEditClick(subscriptions.get(currentPosition));
                }
            }
        });

        holder.btnToggle.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < subscriptions.size()) {
                    listener.onToggleClick(subscriptions.get(currentPosition));
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < subscriptions.size()) {
                    listener.onDeleteClick(subscriptions.get(currentPosition));
                }
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