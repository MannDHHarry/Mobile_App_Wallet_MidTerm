package y3.mobiledev.mywallet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.R;

import java.util.Arrays;
import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
    private final Context context;
    private final List<Integer> colors;
    private int selectedPosition = 0;
    private final OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int colorResId);
    }

    public ColorAdapter(Context context, OnColorSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        // Sample colors - replace with your actual color resources
        this.colors = Arrays.asList(
                R.color.cat_red,
                R.color.cat_pink,
                R.color.cat_purple,
                R.color.cat_blue,
                R.color.cat_teal,
                R.color.cat_green,
                R.color.cat_lime,
                R.color.cat_yellow,
                R.color.cat_orange,
                R.color.cat_brown
        );
    }

    /**
     * Set the currently selected color (for edit mode)
     * @param colorResId The resource ID of the color to pre-select
     */
    public void setSelectedColor(int colorResId) {
        int position = colors.indexOf(colorResId);
        if (position != -1) {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_color_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int colorRes = colors.get(position);
        holder.vColor.setBackgroundColor(ContextCompat.getColor(context, colorRes));

        // Show selection indicator
        holder.itemView.setBackgroundResource(
                position == selectedPosition ? R.drawable.bg_selected : 0
        );

        holder.itemView.setOnClickListener(v -> {
            // Get the current, reliable position at the moment of the click
            int currentPosition = holder.getAdapterPosition();

            // Always check for NO_POSITION
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            // Update selection if different
            if (currentPosition != selectedPosition) {
                int oldPos = selectedPosition;
                selectedPosition = currentPosition;

                // Notify the adapter about the changes
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
            }

            // Send the color for the correctly selected position to the listener
            listener.onColorSelected(colors.get(selectedPosition));
        });
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vColor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            vColor = itemView.findViewById(R.id.vColor);
        }
    }
}