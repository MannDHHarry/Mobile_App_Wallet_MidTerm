package y3.mobiledev.mywallet.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("user_id"),
                @Index(value = {"user_id", "name"}, unique = true)
        })

public class Category {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "icon_res_id")
    private int iconResId;

    @ColumnInfo(name = "color_res_id")
    private int colorResId;

    @ColumnInfo(name = "is_income")
    private boolean isIncome;

    @ColumnInfo(name = "is_default")
    private boolean isDefault;

    @ColumnInfo(name = "is_archived")
    private boolean isArchived;

    @ColumnInfo(name = "created_at")
    private long createdAt;



    // Primary Constructor - for Room Db
    public Category(int categoryId, int userId, String name, int iconResId,
                    int colorResId, boolean isIncome, boolean isDefault,
                    boolean isArchived, long createdAt) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.name = name;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
        this.isIncome = isIncome;
        this.isDefault = isDefault;
        this.isArchived = isArchived;
        this.createdAt = createdAt;
    }

    // Constructor for creating new categories
    @Ignore
    public Category(int userId, String name, int iconResId, int colorResId,
                    boolean isIncome, boolean isDefault) {
        this.userId = userId;
        this.name = name;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
        this.isIncome = isIncome;
        this.isDefault = isDefault;
        this.isArchived = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Backward compatibility
    @Ignore
    public Category(int categoryId, int userId, String name, int iconResId,
                    int colorResId, boolean isIncome) {
        this(categoryId, userId, name, iconResId, colorResId, isIncome, false, false, System.currentTimeMillis());
    }

    // Getters
    public int getCategoryId() { return categoryId; }

    public int getUserId() { return userId; }

    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public int getColorResId() { return colorResId; }

    public boolean isIncome() { return isIncome; }
    public boolean isDefault() { return isDefault; }
    public boolean isArchived() { return isArchived; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public void setUserId(int userId) { this.userId = userId; }

    public void setName(String name) { this.name = name; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setColorResId(int colorResId) { this.colorResId = colorResId; }
    public void setIncome(boolean income) { isIncome = income; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public void setArchived(boolean archived) { isArchived = archived; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}