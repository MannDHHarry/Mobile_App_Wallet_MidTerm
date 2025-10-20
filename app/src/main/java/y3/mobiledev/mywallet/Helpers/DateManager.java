package y3.mobiledev.mywallet.Helpers;

import java.util.Calendar;
import java.util.Date;

public class DateManager {

    public static Calendar normalizeToMidnight(Calendar cal) {
        Calendar normalized = (Calendar) cal.clone();
        normalized.set(Calendar.HOUR_OF_DAY, 0);
        normalized.set(Calendar.MINUTE, 0);
        normalized.set(Calendar.SECOND, 0);
        normalized.set(Calendar.MILLISECOND, 0);
        return normalized;
    }

    /**
     * Gets today's date normalized to midnight.
     * @return Calendar for today at midnight
     */
    public static Calendar getTodayMidnight() {
        return normalizeToMidnight(Calendar.getInstance());
    }

    /**
     * Gets yesterday's date normalized to midnight.
     * @return Calendar for yesterday at midnight
     */
    public static Calendar getYesterdayMidnight() {
        Calendar yesterday = getTodayMidnight();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday;
    }

    /**
     * Checks if two Calendars represent the same day.
     * @param a First Calendar
     * @param b Second Calendar
     * @return True if same day, false otherwise
     */
    public static boolean isSameDay(Calendar a, Calendar b) {
        return normalizeToMidnight(a).equals(normalizeToMidnight(b));
    }

    /**
     * Determines the group header for a transaction date.
     * @param date Transaction date
     * @return "Today", "Yesterday", or "Earlier"
     */
    public static String getGroupHeader(Date date) {
        Calendar transCal = Calendar.getInstance();
        transCal.setTime(date);
        if (isSameDay(transCal, getTodayMidnight())) {
            return "Today";
        }
        if (isSameDay(transCal, getYesterdayMidnight())) {
            return "Yesterday";
        }
        return "Earlier";
    }

    /**
     * Checks if a date is within the specified range.
     * @param date Transaction date
     * @param range Date range ("Today", "This Week", "This Month", "This Year")
     * @return True if within range, false otherwise
     */
    public static boolean isWithinDateRange(Date date, String range) {
        Calendar transCal = Calendar.getInstance();
        transCal.setTime(date);
        transCal = normalizeToMidnight(transCal);

        Calendar now = getTodayMidnight();

        switch (range) {
            case "Today":
                return isSameDay(transCal, now);
            case "This Week":
                Calendar weekStart = (Calendar) now.clone();
                weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                return transCal.getTimeInMillis() >= weekStart.getTimeInMillis() &&
                        transCal.getTimeInMillis() <= now.getTimeInMillis();
            case "This Month":
                return transCal.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                        transCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
            case "This Year":
                return transCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
            default:
                return true;
        }
    }
}