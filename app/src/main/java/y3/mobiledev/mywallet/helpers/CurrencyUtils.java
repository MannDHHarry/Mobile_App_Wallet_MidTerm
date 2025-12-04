package y3.mobiledev.mywallet.helpers;

import java.util.Locale;

/**
 * Utility methods for formatting amounts in Vietnamese Dong (VND).
 *
 * Rules:
 * - Use "," as thousand separator.
 * - For everyday display, show values in thousands with a "K" suffix where appropriate.
 *   Example: 20,000 → "20K ₫", 1,250,000 → "1.3K ₫" (i.e., 1,250K).
 * - Always append the VND symbol "₫".
 */
public class CurrencyUtils {

    /**
     * Format a raw amount in VND without an explicit +/− sign.
     * Applies "K" compact notation for values >= 1,000.
     */
    public static String formatVnd(double amount) {
        boolean negative = amount < 0;
        double abs = Math.abs(amount);
        String numberPart = String.format(Locale.US, "%,.0f", abs);
        return (negative ? "-" : "") + numberPart + " ₫";
    }

    /**
     * Format an amount for transactions with explicit +/− depending on type.
     */
    public static String formatTransactionAmount(double amount, boolean isExpense) {
        // Always treat amount as absolute and add sign based on type.
        String base = formatVnd(Math.abs(amount));
        return (isExpense ? "-" : "+") + base;
    }

    /**
     * Convenience method for plain amounts (no +/- sign, e.g. totals, balances).
     */
    public static String formatPlainAmount(double amount) {
        return formatVnd(amount);
    }

    /**
     * Format a raw amount without compacting (always show full value with comma separators).
     */
    public static String formatFullAmount(double amount) {
        return formatVnd(amount);
    }
}


