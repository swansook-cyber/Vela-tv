package com.gathin.tvhome;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;


/** Reads the system's HOME selection and opens only public system choice screens. */
final class HomeSetup {
    enum Status { SET, NOT_SET, BLOCKED }
    enum Route { ROLE, SYSTEM_SETTINGS, NONE }
    private static final int REQUEST_HOME = 3101;

    private HomeSetup() { }

    static Status status(Context context) {
        try {
            RoleManager roles = roleManager(context);
            boolean roleAvailable = roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME);
            boolean roleHeld = roleAvailable && roles.isRoleHeld(RoleManager.ROLE_HOME);
            PackageManager pm = context.getPackageManager();
            ResolveInfo preferred = pm.resolveActivity(new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY);
            boolean packageIsHome = preferred != null && preferred.activityInfo != null
                    && context.getPackageName().equals(preferred.activityInfo.packageName);
            boolean settingsAvailable = canOpenSystemSettings(context);
            return classify(roleHeld, packageIsHome, roleAvailable, settingsAvailable);
        } catch (RuntimeException unavailable) {
            return Status.BLOCKED;
        }
    }

    static Status classify(boolean roleHeld, boolean packageIsHome,
                           boolean roleAvailable, boolean settingsAvailable) {
        // Some Android TV builds grant ROLE_HOME but still route the Home key to
        // their privileged stock launcher. The resolved HOME activity decides success.
        if (packageIsHome) return Status.SET;
        if (roleHeld) return Status.BLOCKED;
        return roleAvailable || settingsAvailable ? Status.NOT_SET : Status.BLOCKED;
    }

    static Route route(boolean roleAvailable, boolean settingsAvailable) {
        if (roleAvailable) return Route.ROLE;
        return settingsAvailable ? Route.SYSTEM_SETTINGS : Route.NONE;
    }

    static boolean request(Activity activity) {
        RoleManager roles = null;
        boolean roleAvailable = false;
        try {
            roles = roleManager(activity);
            roleAvailable = roles != null && roles.isRoleAvailable(RoleManager.ROLE_HOME);
        }
        catch (RuntimeException ignored) { /* Try system settings below. */ }
        if (route(roleAvailable, canOpenSystemSettings(activity)) == Route.ROLE) {
            try {
                activity.startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_HOME), REQUEST_HOME);
                return true;
            } catch (ActivityNotFoundException | SecurityException | IllegalArgumentException unsupported) {
                // Some TV firmware advertises HOME role but has no working chooser.
            }
        }
        return openSystemSettings(activity);
    }

    static boolean openSystemSettings(Activity activity) {
        try {
            activity.startActivityForResult(new Intent(Settings.ACTION_HOME_SETTINGS), REQUEST_HOME);
            return true;
        } catch (ActivityNotFoundException | SecurityException unsupported) {
            return false;
        }
    }

    private static RoleManager roleManager(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? context.getSystemService(RoleManager.class) : null;
    }

    private static boolean canOpenSystemSettings(Context context) {
        try {
            return context.getPackageManager().resolveActivity(
                    new Intent(Settings.ACTION_HOME_SETTINGS), PackageManager.MATCH_DEFAULT_ONLY) != null;
        } catch (RuntimeException unavailable) {
            return false;
        }
    }
}
