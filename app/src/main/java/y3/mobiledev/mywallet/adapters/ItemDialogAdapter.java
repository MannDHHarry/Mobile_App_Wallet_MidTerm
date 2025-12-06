package y3.mobiledev.mywallet.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
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

    public ItemDialogAdapter(Context context, List<Object> items,
                             ItemProvider itemProvider, OnSelectListener onSelectListener) {
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
            // "+ Add New Category"
            holder.nameView.setText((String) item);
            holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.nameView.setTypeface(null, android.graphics.Typeface.BOLD);

            // Primary color background
            GradientDrawable bg = (GradientDrawable) holder.colorBackground.getBackground().mutate();
            bg.setColor(ContextCompat.getColor(context, R.color.colorPrimary));

            // "+" icon
            holder.iconView.setImageResource(android.R.drawable.ic_menu_add);
            holder.iconView.setColorFilter(ContextCompat.getColor(context, R.color.text_white));
        }
        else {
            // Real category
            int iconRes = itemProvider.getIconResId(item);
            Log.d("ItemDialogAdapter", "Icon resource ID: " + iconRes + " for item: " + itemProvider.getDisplayText(item));

            int colorRes = itemProvider.getColorResId(item);
            String text = itemProvider.getDisplayText(item);

            holder.nameView.setText(text);
            holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.text_black));

            // ICON — FROM DATABASE
            holder.iconView.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_profile);
            holder.iconView.setColorFilter(ContextCompat.getColor(context, R.color.text_white));

            // COLOR BACKGROUND
            GradientDrawable bg = (GradientDrawable) holder.colorBackground.getBackground().mutate();
            bg.setColor(ContextCompat.getColor(context, colorRes));
        }

        holder.itemView.setOnClickListener(v -> {
            if (onSelectListener != null) onSelectListener.onSelect(item);
            if (dialog != null) dialog.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        View colorBackground;
        ImageView iconView;
        TextView nameView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            colorBackground = itemView.findViewById(R.id.vColorBackground);
            iconView = itemView.findViewById(R.id.ivDialogIcon);
            nameView = itemView.findViewById(R.id.tvDialogText);
        }
    }
}