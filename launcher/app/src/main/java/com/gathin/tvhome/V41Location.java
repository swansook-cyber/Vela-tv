package com.gathin.tvhome;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import java.util.Locale;

/** Best-effort TV location. Never prompts for location permission. */
final class V41Location {
    static final double FALLBACK_LAT = 8.08;
    static final double FALLBACK_LON = 98.81;
    final double latitude;
    final double longitude;
    final String label;

    private V41Location(double latitude, double longitude, String label) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
    }

    static V41Location resolve(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE);
        if (settings.contains("v41_latitude") && settings.contains("v41_longitude")) {
            double lat = Double.longBitsToDouble(settings.getLong("v41_latitude", 0));
            double lon = Double.longBitsToDouble(settings.getLong("v41_longitude", 0));
            if (valid(lat, lon)) return new V41Location(lat, lon, "Selected location");
        }
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (manager != null) {
                    Location best = null;
                    for (String provider : manager.getProviders(true)) {
                        Location last = manager.getLastKnownLocation(provider);
                        if (last != null && valid(last.getLatitude(), last.getLongitude())
                                && last.getTime() > System.currentTimeMillis() - 24L * 60 * 60 * 1000
                                && (best == null || last.getTime() > best.getTime())) best = last;
                    }
                    if (best != null) return new V41Location(best.getLatitude(), best.getLongitude(), "TV location");
                }
            } catch (SecurityException ignored) {
                // The launcher does not request privileged access.
            }
        }
        return new V41Location(FALLBACK_LAT, FALLBACK_LON, "Krabi (default)");
    }

    String latParam() { return String.format(Locale.US, "%.5f", latitude); }
    String lonParam() { return String.format(Locale.US, "%.5f", longitude); }
    String cacheKey() { return latParam() + "," + lonParam(); }

    private static boolean valid(double lat, double lon) {
        return Double.isFinite(lat) && Double.isFinite(lon)
                && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }
}
