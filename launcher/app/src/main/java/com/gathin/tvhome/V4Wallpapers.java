package com.gathin.tvhome;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Private, bounded static wallpaper store. A bad import never replaces the applied image. */
final class V4Wallpapers {
    static final String AURORA = "v4_aurora", DUSK = "v4_dusk", MIDNIGHT = "v4_midnight";
    static final String IMPORTED = "v4_imported";
    private static final int MAX_BYTES = 16 * 1024 * 1024;
    private static final int MAX_SIDE = 8192;

    static boolean isChoice(String choice) {
        return AURORA.equals(choice) || DUSK.equals(choice) || MIDNIGHT.equals(choice)
                || IMPORTED.equals(choice);
    }

    static File file(Context context, String choice) {
        if (!isChoice(choice)) return null;
        return new File(new File(context.getFilesDir(), "v4-wallpapers"), choice + ".img");
    }

    static int resource(String choice) {
        if (AURORA.equals(choice)) return R.drawable.v4_aurora;
        if (DUSK.equals(choice)) return R.drawable.v4_dusk;
        if (MIDNIGHT.equals(choice)) return R.drawable.v4_midnight;
        return 0;
    }

    static boolean install(Context context, String choice, Uri uri) {
        File target = file(context, choice);
        if (target == null) return false;
        int resource = resource(choice);
        if (resource == 0 && uri == null) return false;
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) return false;
        File staging = new File(parent, choice + ".pending");
        try (InputStream input = resource != 0 ? context.getResources().openRawResource(resource)
                : context.getContentResolver().openInputStream(uri);
             FileOutputStream output = new FileOutputStream(staging)) {
            if (input == null) return false;
            byte[] buffer = new byte[8192];
            int read, total = 0;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_BYTES) return false;
                output.write(buffer, 0, read);
            }
            output.getFD().sync();
            if (!valid(staging)) return false;
            Files.move(staging.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException | SecurityException failure) {
            return false;
        } finally {
            // A failed import leaves the previously applied file and preferences untouched.
            staging.delete();
        }
    }

    private static boolean valid(File file) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), bounds);
        return ("image/jpeg".equals(bounds.outMimeType) || "image/png".equals(bounds.outMimeType))
                && bounds.outWidth > 0 && bounds.outHeight > 0
                && bounds.outWidth <= MAX_SIDE && bounds.outHeight <= MAX_SIDE;
    }

    private V4Wallpapers() { }
}
