package y3.mobiledev.mywallet.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles profile picture storage and retrieval
 */
public class ProfilePhotoManager {

    private static final String TAG = "ProfilePhotoManager";
    private static final String PROFILE_FOLDER = "profiles";
    private static final int MAX_WIDTH = 512;
    private static final int MAX_HEIGHT = 512;

    /**
     * Save a profile picture from URI to app's private storage
     * @return File path string or null if failed
     */
    public static String saveProfilePhoto(Context context, Uri photoUri) {
        try {
            // Create profiles directory if it doesn't exist
            File profilesDir = new File(context.getFilesDir(), PROFILE_FOLDER);
            if (!profilesDir.exists()) {
                profilesDir.mkdirs();
            }

            // Generate unique filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String filename = "profile_" + timestamp + ".jpg";
            File destinationFile = new File(profilesDir, filename);

            // Load and compress the image
            InputStream inputStream = context.getContentResolver().openInputStream(photoUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI");
                return null;
            }

            // Resize to square profile picture
            bitmap = resizeToSquare(bitmap, MAX_WIDTH, MAX_HEIGHT);

            // Save to file
            FileOutputStream fos = new FileOutputStream(destinationFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            bitmap.recycle();

            Log.d(TAG, "Profile photo saved: " + destinationFile.getAbsolutePath());
            return destinationFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Error saving profile photo", e);
            return null;
        }
    }

    /**
     * Load profile photo as Bitmap
     */
    public static Bitmap loadProfilePhoto(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return null;
        }

        File file = new File(photoPath);
        if (!file.exists()) {
            Log.w(TAG, "Profile file not found: " + photoPath);
            return null;
        }

        return BitmapFactory.decodeFile(photoPath);
    }

    /**
     * Delete profile photo
     */
    public static boolean deleteProfilePhoto(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            return false;
        }

        File file = new File(photoPath);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Profile deleted: " + deleted + " - " + photoPath);
            return deleted;
        }
        return false;
    }

    /**
     * Resize bitmap to square while maintaining aspect ratio (crops to center)
     */
    private static Bitmap resizeToSquare(Bitmap original, int size, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Calculate crop size (use smaller dimension to make it square)
        int cropSize = Math.min(width, height);
        
        // Calculate crop coordinates (center crop)
        int x = (width - cropSize) / 2;
        int y = (height - cropSize) / 2;

        // Crop to square
        Bitmap cropped = Bitmap.createBitmap(original, x, y, cropSize, cropSize);

        // Resize if needed
        if (cropSize > maxSize) {
            Bitmap resized = Bitmap.createScaledBitmap(cropped, maxSize, maxSize, true);
            cropped.recycle();
            return resized;
        }

        return cropped;
    }
}

