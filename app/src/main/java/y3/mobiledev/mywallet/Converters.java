package y3.mobiledev.mywallet;

import androidx.room.TypeConverter;

import java.util.Date;

public class Converters {

    /**
     * Convert timestamp (Long) to Date object
     * Used when reading from database
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Convert Date object to timestamp (Long)
     * Used when writing to database
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}