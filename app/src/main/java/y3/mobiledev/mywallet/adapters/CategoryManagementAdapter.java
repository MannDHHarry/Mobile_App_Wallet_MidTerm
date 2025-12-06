package y3.mobiledev.mywallet.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import y3.mobiledev.mywallet.models.Category;
import y3.mobiledev.mywallet.R;

import java.util.List;

public class CategoryManagementAdapter extends RecyclerView.Adapter<CategoryManagementAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<Category> categories;
    private final OnEditClickListener onEditClick;
    private final OnDeleteClickListener onDeleteClick;

    public interface OnEditClickListener {
        void onEditClick(Category category);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Category category);
    }

    public CategoryManagementAdapter(Context context, List<Category> categories,
                                     OnEditClickListener onEditClick,
                                     OnDeleteClickListener onDeleteClick) {
        this.context = context;
        this.categories = categories;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_management, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Name
        holder.tvName.setText(category.getName());

        // SAFE ICON SETTING — THIS FIXES THE CRASH
        int iconRes = category.getIconResId();
        try {
            if (iconRes != 0) {
                holder.ivIcon.setImageResource(iconRes);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_profile); // fallback icon
            }
        } catch (Resources.NotFoundException e) {
            // This saves your app from crashing if icon is missing
            holder.ivIcon.setImageResource(R.drawable.ic_profile); // or any default
        }

        holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_white));

        // BACKGROUND: User-selected color as circle
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(ContextCompat.getColor(context, category.getColorResId()));
        holder.vColorBackground.setBackground(background);

        // Buttons
        holder.btnEdit.setOnClickListener(v -> onEditClick.onEditClick(category));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.onDeleteClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }


    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        ImageButton btnEdit;
        ImageButton btnDelete;
        View vColorBackground;  // This is the circle behind the icon

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            vColorBackground = itemView.findViewById(R.id.viewColorBackground); // Make sure ID matches
        }
    }
}