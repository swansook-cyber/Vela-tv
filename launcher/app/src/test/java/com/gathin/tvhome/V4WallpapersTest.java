package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class V4WallpapersTest {
    @Test public void onlyBundledAndImportedChoicesOverrideThemes() {
        assertTrue(V4Wallpapers.isChoice(V4Wallpapers.AURORA));
        assertTrue(V4Wallpapers.isChoice(V4Wallpapers.DUSK));
        assertTrue(V4Wallpapers.isChoice(V4Wallpapers.MIDNIGHT));
        assertTrue(V4Wallpapers.isChoice(V4Wallpapers.IMPORTED));
        assertFalse(V4Wallpapers.isChoice(null));
        assertFalse(V4Wallpapers.isChoice("none"));
        assertFalse(V4Wallpapers.isChoice("wallpaper.jpg"));
        assertFalse(V4Wallpapers.isChoice("../outside"));
    }
}
