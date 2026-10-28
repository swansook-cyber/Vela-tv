package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** D-pad first preview/apply screen. Picking an image does not change Home until Apply is pressed. */
public final class WallpaperSettingsActivity extends Activity {
    private static final int PICK_IMAGE = 401;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private SharedPreferences choices;
    private ImageView preview;
    private TextView status;
    private TextView apply;
    private TextView importChoice;
    private String candidate;
    private Uri candidateUri;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        choices = getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFF101419);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(38);
        content.setPadding(dp(72), pad, dp(72), pad);
        scroll.addView(content);
        TextView title = new TextView(this);
        title.setText(R.string.v4_wallpaper_title);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(32);
        content.addView(title);
        preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams picture = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(250));
        picture.topMargin = dp(18);
        content.addView(preview, picture);
        status = new TextView(this);
        status.setTextColor(0xFFCCDAE5);
        status.setTextSize(18);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.topMargin = dp(10);
        content.addView(status, statusParams);

        addChoice(content, R.string.v4_wallpaper_aurora, V4Wallpapers.AURORA);
        addChoice(content, R.string.v4_wallpaper_dusk, V4Wallpapers.DUSK);
        addChoice(content, R.string.v4_wallpaper_midnight, V4Wallpapers.MIDNIGHT);
        importChoice = row(content, R.string.v4_wallpaper_import);
        importChoice.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE).setType("image/*")
                    .putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"});
            pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            // AOSP TV has a DocumentsStub that resolves OPEN_DOCUMENT but never opens a picker.
            ComponentName handler = pick.resolveActivity(getPackageManager());
            if (handler != null && "com.android.tv.frameworkpackagestubs".equals(handler.getPackageName())) {
                pick = new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("image/*").putExtra(Intent.EXTRA_MIME_TYPES,
                                new String[]{"image/jpeg", "image/png"});
            }
            try { startActivityForResult(pick, PICK_IMAGE); }
            catch (RuntimeException failure) { Toast.makeText(this, R.string.v4_wallpaper_unavailable, Toast.LENGTH_LONG).show(); }
        });
        apply = row(content, R.string.v4_wallpaper_apply);
        apply.setOnClickListener(v -> apply());
        TextView reset = row(content, R.string.v4_wallpaper_reset);
        reset.setOnClickListener(v -> {
            choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply();
            changed();
            candidate = null;
            candidateUri = null;
            preview.setImageDrawable(null);
            status.setText(R.string.v4_wallpaper_theme_default);
            apply.setEnabled(false);
            Toast.makeText(this, R.string.v4_wallpaper_reset_done, Toast.LENGTH_SHORT).show();
        });
        setContentView(scroll);
        String saved = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        if (V4Wallpapers.isChoice(saved)) {
            File applied = V4Wallpapers.file(this, saved);
            if (applied.isFile()) {
                candidate = saved;
                candidateUri = V4Wallpapers.IMPORTED.equals(saved) ? Uri.fromFile(applied) : null;
                showPreview();
            }
        }
        if (candidate == null) {
            status.setText(R.string.v4_wallpaper_theme_default);
            apply.setEnabled(false);
        }
        content.getChildAt(3).requestFocus();
    }

    private void addChoice(LinearLayout parent, int label, String id) {
        row(parent, label).setOnClickListener(v -> {
            candidate = id;
            candidateUri = null;
            showPreview();
        });
    }

    private TextView row(LinearLayout parent, int label) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextSize(19);
        view.setTextColor(0xFFFFFFFF);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(dp(18), 0, dp(18), 0);
        view.setBackgroundResource(R.drawable.settings_choice_bg);
        view.setFocusable(true);
        view.setClickable(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.topMargin = dp(9);
        parent.addView(view, params);
        return view;
    }

    private void showPreview() {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap image;
            if (candidateUri == null) image = BitmapFactory.decodeResource(getResources(), V4Wallpapers.resource(candidate), opts);
            else {
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                try (InputStream stream = getContentResolver().openInputStream(candidateUri)) {
                    BitmapFactory.decodeStream(stream, null, bounds);
                }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || bounds.outWidth > 8192
                        || bounds.outHeight > 8192 || !("image/png".equals(bounds.outMimeType)
                        || "image/jpeg".equals(bounds.outMimeType))) throw new IllegalArgumentException("Invalid image");
                opts.inSampleSize = Math.max(1, Integer.highestOneBit(Math.max(bounds.outWidth / 480,
                        bounds.outHeight / 270)));
                try (InputStream stream = getContentResolver().openInputStream(candidateUri)) {
                    image = BitmapFactory.decodeStream(stream, null, opts);
                }
            }
            if (image == null) throw new IllegalArgumentException("Unreadable image");
            preview.setImageBitmap(image);
            status.setText(R.string.v4_wallpaper_preview);
            apply.setEnabled(true);
        } catch (Exception | OutOfMemoryError failure) {
            preview.setImageDrawable(null);
            status.setText(R.string.v4_wallpaper_invalid);
            apply.setEnabled(false);
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_IMAGE) return;
        importChoice.requestFocus();
        if (result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) { /* app-private copy made on Apply remains durable */ }
        candidate = V4Wallpapers.IMPORTED;
        candidateUri = uri;
        showPreview();
    }

    private void apply() {
        if (candidate == null || !apply.isEnabled()) return;
        String selected = candidate;
        Uri uri = candidateUri;
        apply.setEnabled(false);
        worker.execute(() -> {
            boolean installed = uri != null && "file".equals(uri.getScheme())
                    && V4Wallpapers.IMPORTED.equals(selected)
                    ? V4Wallpapers.file(this, selected).isFile()
                    : V4Wallpapers.install(getApplicationContext(), selected, uri);
            runOnUiThread(() -> {
                if (installed) {
                    choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, selected).apply();
                    changed();
                    status.setText(R.string.v4_wallpaper_applied);
                } else {
                    status.setText(R.string.v4_wallpaper_invalid);
                }
                apply.setEnabled(true);
            });
        });
    }

    private void changed() {
        choices.edit().putLong(Config.SETTINGS_REVISION_KEY,
                choices.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).apply();
        setResult(RESULT_OK);
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override protected void onDestroy() {
        worker.shutdown();
        super.onDestroy();
    }
}
