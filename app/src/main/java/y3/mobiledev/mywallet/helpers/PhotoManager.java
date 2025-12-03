package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles receipt photo storage and retrieval
 */
public class PhotoManager {

    private static final String TAG = "PhotoManager";
    private static final String RECEIPTS_FOLDER = "receipts";
    private static final int MAX_WIDTH = 1080;
    private static final int MAX_HEIGHT = 1920;

    /**
     * Save a photo from URI to app's private storage
     * @return File path string or null if failed
     */
    public static String saveReceiptPhoto(Context context, Uri photoUri) {
        try {
            // Create receipts directory if it doesn't exist
            File receiptsDir = new File(context.getFilesDir(), RECEIPTS_FOLDER);
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs();
            }

            // Generate unique filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String filename = "receipt_" + timestamp + ".jpg";
            File destinationFile = new File(receiptsDir, filename);

            // Load and compress the image
            InputStream inputStream = context.getContentResolver().openInputStream(photoUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI");
                return null;
            }

            // Resize if too large (save storage space)
            bitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT);

            // Save to file
            FileOutputStream fos = new FileOutputStream(destinationFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();

            Log.d(TAG, "Receipt saved: " + destinationFile.getAbsolutePath());
            return destinationFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error saving receipt photo", e);
            return null;
        }
    }

    /**
     * Load receipt photo as Bitmap
     */
    public static Bitmap loadReceiptPhoto(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return null;
        }

        File file = new File(photoPath);
        if (!file.exists()) {
            Log.w(TAG, "Receipt file not found: " + photoPath);
            return null;
        }

        return BitmapFactory.decodeFile(photoPath);
    }

    /**
     * Delete receipt photo
     */
    public static boolean deleteReceiptPhoto(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return false;
        }

        File file = new File(photoPath);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Receipt deleted: " + deleted + " - " + photoPath);
            return deleted;
        }
        return false;
    }

    /**
     * Check if transaction has receipt
     */
    public static boolean hasReceipt(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return false;
        }
        File file = new File(photoPath);
        return file.exists();
    }

    /**
     * Resize bitmap to fit within max dimensions while maintaining aspect ratio
     */
    private static Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Check if resizing is needed
        if (width <= maxWidth && height <= maxHeight) {
            return original;
        }

        // Calculate scale factor
        float scale = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height
        );

        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        Bitmap resized = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
        original.recycle();
        return resized;
    }

    /**
     * Get file size in KB
     */
    public static long getFileSize(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return 0;
        }
        File file = new File(photoPath);
        return file.exists() ? file.length() / 1024 : 0;
    }
}