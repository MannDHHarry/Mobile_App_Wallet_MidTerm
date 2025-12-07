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
               R.drawable.cat_512,
                R.drawable.cat_award,
                R.drawable.cat_bag,
                R.drawable.cat_bill,
                R.drawable.cat_book,
                R.drawable.cat_bookmark,
                R.drawable.cat_box,
                R.drawable.cat_cart,
                R.drawable.cat_cash,
                R.drawable.cat_dollar,
                R.drawable.cat_drinks,
                R.drawable.cat_education,
                R.drawable.cat_energy,
                R.drawable.cat_fastfood,
                R.drawable.cat_finance,
                R.drawable.cat_four,
                R.drawable.cat_game,
                R.drawable.cat_gift,
                R.drawable.cat_grocery,
                R.drawable.cat_gym,
                R.drawable.cat_leaf,R.drawable.cat_health,
                R.drawable.cat_leaf,
                R.drawable.cat_lines,
                R.drawable.cat_luggage,
                R.drawable.cat_moneh,
                R.drawable.cat_moneybag,
                R.drawable.cat_music,
                R.drawable.cat_owl,
                R.drawable.cat_pet,
                R.drawable.cat_pig,
                R.drawable.cat_plane,
                R.drawable.cat_plant,
                R.drawable.cat_school,
                R.drawable.cat_shop,
                R.drawable.cat_sunglass,
                R.drawable.cat_sunglass,
                R.drawable.cat_train,
                R.drawable.cat_transport,
                R.drawable.cat_travel,R.drawable.cat_tree

        );
    }

    /**
     * Set the currently selected icon (for edit mode)
     * @param iconResId The resource ID of the icon to pre-select
     */
    public void setSelectedIcon(int iconResId) {
        int position = icons.indexOf(iconResId);
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_icon_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int iconRes = icons.get(position);
        holder.ivIcon.setImageResource(iconRes);
        holder.itemView.setBackgroundResource(position == selectedPosition ? R.drawable.bg_selected : 0);

        holder.itemView.setOnClickListener(v -> {
            // Get the current, reliable position at the moment of the click
            int currentPosition = holder.getAdapterPosition();

            // Always check for NO_POSITION in case the item was deleted during an animation
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }

            // Proceed only if the position is valid
            if (currentPosition != selectedPosition) {
                int oldPos = selectedPosition;
                selectedPosition = currentPosition;

                // Notify the adapter about the changes
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
            }

            // Send the icon for the correctly selected position to the listener
            listener.onIconSelected(icons.get(selectedPosition));
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