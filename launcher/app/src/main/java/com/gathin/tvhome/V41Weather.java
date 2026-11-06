package com.gathin.tvhome;

import android.app.Activity;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Small, best-effort weather fetcher; never performs network I/O on the UI thread. */
final class V41Weather {
    private V41Weather() {}

    static void refresh(Activity activity, TextView card) {
        final V41Location location = V41Location.resolve(activity);
        new Thread(() -> {
            HttpURLConnection connection = null;
            String label;
            try {
                connection = (HttpURLConnection) new URL("https://api.open-meteo.com/v1/forecast?latitude=" + location.latParam()
                        + "&longitude=" + location.lonParam()
                        + "&current=temperature_2m,weather_code,relative_humidity_2m&daily=weather_code,temperature_2m_max,temperature_2m_min&forecast_days=5&timezone=Asia%2FBangkok").openConnection();
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                connection.setRequestProperty("Accept", "application/json");
                if (connection.getResponseCode() != 200) {
                    throw new IllegalStateException("HTTP " + connection.getResponseCode());
                }
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        if (response.length() > 32768) throw new IllegalStateException("Response too large");
                    }
                }
                JSONObject forecast = new JSONObject(response.toString());
                JSONObject current = forecast.getJSONObject("current");
                double temperature = current.getDouble("temperature_2m");
                if (!Double.isFinite(temperature) || temperature < -90 || temperature > 65) {
                    throw new IllegalStateException("Invalid temperature");
                }
                String observed = current.optString("time", "");
                label = formatForecast(location.label, temperature, current, forecast.getJSONObject("daily"));
            } catch (Exception error) {
                label = "WEATHER · " + location.label + "\nData unavailable · check connection";
            } finally {
                if (connection != null) connection.disconnect();
            }
            final String text = label;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) card.setText(text);
            });
        }, "v41-weather").start();
    }

    private static String formatForecast(String place, double temp, JSONObject current, JSONObject daily)
            throws Exception {
        StringBuilder out = new StringBuilder();
        int code = current.optInt("weather_code", -1);
        out.append(String.format(Locale.US, "%.0f°  %s\n", temp, symbol(code)));
        out.append(place).append("  ·  ").append(condition(code)).append("\n");
        out.append("Humidity ").append(current.optInt("relative_humidity_2m", 0)).append("%\n\n");
        org.json.JSONArray dates = daily.getJSONArray("time");
        org.json.JSONArray codes = daily.getJSONArray("weather_code");
        org.json.JSONArray high = daily.getJSONArray("temperature_2m_max");
        org.json.JSONArray low = daily.getJSONArray("temperature_2m_min");
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
        for (int i = 1; i < Math.min(5, dates.length()); i++) {
            String day = java.time.LocalDate.parse(dates.getString(i)).format(formatter);
            out.append(String.format(Locale.US, "%s  %s  %.0f°/%.0f°", day,
                    symbol(codes.getInt(i)), high.getDouble(i), low.getDouble(i)));
            if (i < Math.min(5, dates.length()) - 1) out.append("\n");
        }
        return out.toString();
    }

    private static String symbol(int code) {
        if (code == 0) return "☀";
        if (code <= 3 && code > 0) return "⛅";
        if (code == 45 || code == 48) return "☁";
        if (code >= 95) return "⛈";
        if (code >= 51 && code <= 82) return "☂";
        return "☁";
    }

    private static String condition(int code) {
        if (code == 0) return "Clear";
        if (code <= 3 && code > 0) return "Cloudy";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 67) return "Rain";
        if (code >= 80 && code <= 82) return "Showers";
        if (code >= 95) return "Thunderstorm";
        return "Weather";
    }
}
