package com.gathin.tvhome;

import android.app.Activity;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Small, best-effort weather fetcher; never performs network I/O on the UI thread. */
final class V41Weather {
    private static final String ENDPOINT =
            "https://api.open-meteo.com/v1/forecast?latitude=8.08&longitude=98.81"
            + "&current=temperature_2m,weather_code&timezone=Asia%2FBangkok";
    private V41Weather() {}

    static void refresh(Activity activity, TextView card) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            String label;
            try {
                connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
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
                JSONObject current = new JSONObject(response.toString()).getJSONObject("current");
                double temperature = current.getDouble("temperature_2m");
                if (!Double.isFinite(temperature) || temperature < -90 || temperature > 65) {
                    throw new IllegalStateException("Invalid temperature");
                }
                String observed = current.optString("time", "");
                label = String.format(Locale.US, "WEATHER · Krabi\n%.0f°C · %s\nUpdated %s",
                        temperature, condition(current.optInt("weather_code", -1)), observed);
            } catch (Exception error) {
                label = "WEATHER · Krabi\nData unavailable · check connection";
            } finally {
                if (connection != null) connection.disconnect();
            }
            final String text = label;
            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) card.setText(text);
            });
        }, "v41-weather").start();
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
