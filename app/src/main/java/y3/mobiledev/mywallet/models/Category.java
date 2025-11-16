package y3.mobiledev.mywallet.Models;

public class Category {
    private int categoryId;
    private int userId;
    private String name;
    private int iconResId;
    private int colorResId;
    private boolean isIncome;

    public Category(int categoryId, int userId, String name, int iconResId,
                    int colorResId, boolean isIncome) {
        this.categoryId = categoryId;
        this.userId = userId;
        this.name = name;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
        this.isIncome = isIncome;
    }

    // Backward compatibility constructor
    public Category(int categoryId, String name, int iconResId, int colorResId, boolean isIncome) {
        this(categoryId, 1, name, iconResId, colorResId, isIncome);
    }

    // Getters
    public int getCategoryId() { return categoryId; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public int getColorResId() { return colorResId; }
    public boolean isIncome() { return isIncome; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setColorResId(int colorResId) { this.colorResId = colorResId; }

    public void setIncome(boolean income) {    isIncome = income;     }
}
