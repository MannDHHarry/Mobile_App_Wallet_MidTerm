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
        holder.itemView.setBackgroundResource(position == selectedPosition ? R.drawable.bg_selected : 0);

        holder.itemView.setOnClickListener(v -> {
            // --- FIX STARTS HERE ---

            // Get the current position at the moment of the click
            int currentPosition = holder.getAdapterPosition();

            // Safety check: RecyclerView can return NO_POSITION if an item is being removed.
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int oldPos = selectedPosition;
            selectedPosition = currentPosition; // Use the reliable current position

            // Notify the adapter about the old and new selections
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition); // Use the reliable current position

            // Get the color for the correct position
            listener.onColorSelected(colors.get(selectedPosition));

            // --- FIX ENDS HERE ---
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