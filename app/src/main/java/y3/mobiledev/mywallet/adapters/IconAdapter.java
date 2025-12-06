package y3.mobiledev.mywallet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.R;

import java.util.Arrays;
import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {
    private final Context context;
    private final List<Integer> icons;
    private int selectedPosition = 0;
    private final OnIconSelectedListener listener;

    public interface OnIconSelectedListener {
        void onIconSelected(int iconResId);
    }

    public IconAdapter(Context context, OnIconSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        // Sample icons - replace with your actual drawables
        this.icons = Arrays.asList(
                android.R.drawable.ic_menu_gallery,  // Placeholder: food
                android.R.drawable.ic_menu_slideshow,  // game
                android.R.drawable.ic_menu_directions,  // car
                android.R.drawable.ic_menu_myplaces,  // home
                android.R.drawable.ic_menu_compass,  // money
                android.R.drawable.ic_menu_recent_history,  // plane
                android.R.drawable.ic_menu_camera,  // movie
                android.R.drawable.ic_menu_call,  // phone
                android.R.drawable.ic_menu_info_details,  // education
                android.R.drawable.ic_menu_help  // health
                // Add more...
        );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int iconRes = icons.get(position);
        holder.ivIcon.setImageResource(iconRes);
        holder.itemView.setBackgroundResource(position == selectedPosition ? R.drawable.bg_selected : 0);

        holder.itemView.setOnClickListener(v -> {
            // --- FIX STARTS HERE ---

            // Get the current, reliable position at the moment of the click
            int currentPosition = holder.getAdapterPosition();

            // Always check for NO_POSITION in case the item was deleted during an animation
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            // Proceed only if the position is valid
            if (currentPosition != selectedPosition) {
                int oldPos = selectedPosition;
                selectedPosition = currentPosition; // Use the reliable current position

                // Notify the adapter about the changes
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition); // Use the reliable current position
            }

            // Send the icon for the correctly selected position to the listener
            listener.onIconSelected(icons.get(selectedPosition));

            // --- FIX ENDS HERE ---
        });
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}