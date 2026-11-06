package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Online prayer timetable with same-day cache. Does not calculate prayer times locally. */
final class V41PrayerTimes {
    private static final String PREFS = "v41_prayer_cache";
    private static final TimeZone BANGKOK = TimeZone.getTimeZone("Asia/Bangkok");
    private static final String[] KEYS = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
    private static final String[] LABELS = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};

    private V41PrayerTimes() {}

    static void refresh(Activity activity, TextView card) {
        final String date = dateNow();
        final V41Location location = V41Location.resolve(activity);
        final String cacheKey = date + ":" + location.cacheKey();
        SharedPreferences cache = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String stored = cache.getString(cacheKey, null);
        if (stored != null) {
            String cached = render(stored, date, "Cached", location.label);
            if (cached != null) card.setText(cached);
        }
        new Thread(() -> {
            HttpURLConnection connection = null;
            String label;
            try {
                String[] parts = date.split("-");
                String apiDate = parts[2] + "-" + parts[1] + "-" + parts[0];
                // Fixed Krabi pilot coordinates until user-selectable location is implemented.
                String endpoint = "https://api.aladhan.com/v1/timings/" + apiDate
                        + "?latitude=" + location.latParam() + "&longitude=" + location.lonParam()
                        + "&school=0&timezonestring=Asia%2FBangkok";
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                if (connection.getResponseCode() != 200) throw new IllegalStateException("HTTP error");
                StringBuilder json = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                        if (json.length() > 32768) throw new IllegalStateException("Oversized response");
                    }
                }
                JSONObject root = new JSONObject(json.toString());
                if (root.optInt("code") != 200) throw new IllegalStateException("API rejected request");
                JSONObject timings = root.getJSONObject("data").getJSONObject("timings");
                JSONObject filtered = new JSONObject();
                for (String key : KEYS) {
                    String time = timings.getString(key);
                    if (!time.matches("\\d{2}:\\d{2}( \\([^)]*\\))?")) {
                        throw new IllegalStateException("Invalid timing " + key);
                    }
                    filtered.put(key, time.substring(0, 5));
                }
                String normalized = filtered.toString();
                label = render(normalized, date, "Online", location.label);
                if (label == null) throw new IllegalStateException("Invalid schedule");
                // Do not cache or display yesterday's results if the date changed mid-request.
                if (!date.equals(dateNow())) return;
                cache.edit().clear().putString(cacheKey, normalized).apply();
            } catch (Exception e) {
                label = stored == null
                        ? "PRAYER TIMES · " + location.label + "\nUnavailable · connect to internet"
                        : render(stored, date, "Cached · offline", location.label);
            } finally {
                if (connection != null) connection.disconnect();
            }
            final String output = label;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()
                        && date.equals(dateNow()) && output != null) card.setText(output);
            });
        }, "v41-prayer-times").start();
    }

    private static String render(String raw, String date, String source, String locationLabel) {
        try {
            JSONObject timings = new JSONObject(raw);
            StringBuilder result = new StringBuilder("PRAYER TIMES  ·  " + locationLabel);
            int currentMinute = minutesNow();
            String next = null;
            int remaining = 0;
            for (int i = 0; i < KEYS.length; i++) {
                String time = timings.getString(KEYS[i]);
                if (!time.matches("\\d{2}:\\d{2}")) return null;
                int minute = Integer.parseInt(time.substring(0, 2)) * 60
                        + Integer.parseInt(time.substring(3, 5));
                if (minute >= 1440) return null;
                if (i == 0 || i == 3) result.append("\n");
                else result.append("    ");
                result.append(LABELS[i]).append(" ").append(time);
                if (next == null && minute > currentMinute) {
                    next = LABELS[i];
                    remaining = minute - currentMinute;
                }
            }
            result.append("\n").append(next == null ? "Today's prayers complete" :
                    "NEXT  " + next + "  ·  " + remaining / 60 + "h " + remaining % 60 + "m");
            result.append("  ·  ").append(source);
            return result.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String dateNow() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setTimeZone(BANGKOK);
        return format.format(new Date());
    }

    private static int minutesNow() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        format.setTimeZone(BANGKOK);
        String now = format.format(new Date());
        return Integer.parseInt(now.substring(0, 2)) * 60
                + Integer.parseInt(now.substring(3, 5));
    }
}
