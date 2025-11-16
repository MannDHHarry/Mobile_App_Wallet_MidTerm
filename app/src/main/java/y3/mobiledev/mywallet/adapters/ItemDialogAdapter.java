package y3.mobiledev.mywallet.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.R;

import java.util.List;


public class ItemDialogAdapter extends RecyclerView.Adapter<ItemDialogAdapter.ItemViewHolder> {
    private final Context context;
    private final List<Object> items;
    private final ItemProvider itemProvider;
    private final OnSelectListener onSelectListener;
    private AlertDialog dialog;

    public interface ItemProvider {
        int getIconResId(Object item);
        int getColorResId(Object item);
        String getDisplayText(Object item);
    }

    public interface OnSelectListener {
        void onSelect(Object item);
    }

    public ItemDialogAdapter(Context context, List<Object> items, ItemProvider itemProvider,
                             OnSelectListener onSelectListener) {
        this.context = context;
        this.items = items;
        this.itemProvider = itemProvider;
        this.onSelectListener = onSelectListener;
    }

    public void setDialog(AlertDialog dialog) {
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dialog_generic, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Object item = items.get(position);

        if (item instanceof String) {
            // "+ Add New" item
            String itemText = (String) item;
            holder.iconView.setImageResource(R.drawable.ic_launcher_foreground);
            holder.iconView.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.nameView.setText(itemText);
            holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            // Regular item
            int iconResId = itemProvider.getIconResId(item);
            int colorResId = itemProvider.getColorResId(item);
            String displayText = itemProvider.getDisplayText(item);

            if (iconResId != 0) {
                holder.iconView.setImageResource(iconResId);
                if (colorResId != 0) {
                    holder.iconView.setColorFilter(ContextCompat.getColor(context, colorResId));
                }
            }
            holder.nameView.setText(displayText);
            holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.text_black));
        }

        // Click for selection
        holder.itemView.setOnClickListener(v -> {
            if (onSelectListener != null) {
                onSelectListener.onSelect(item);
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView nameView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.ivDialogIcon);
            nameView = itemView.findViewById(R.id.tvDialogText);
        }
    }
}
