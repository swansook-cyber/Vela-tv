package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HomeViewportTest {
    @Test public void nextRowStartsBeyondViewportAtCommonTvSizes() {
        for (int height : new int[]{480, 720, 846, 1080}) {
            for (float density : new float[]{1f, 1.5f, 2f}) {
                int dockHeight = Math.round(170 * density);
                int top = HomeViewport.dockTopPadding(height, dockHeight, 0, 0, 0, density);
                assertEquals(height, top + dockHeight);
            }
        }
    }

    @Test public void requestedRowKeepsItsBottomClearance() {
        int top = HomeViewport.dockTopPadding(846, 230, 230, 1, 0, 2f);
        assertEquals(60, 846 - top - 230 - 230);
    }

    @Test public void heroClearanceCanMoveTheDockDown() {
        assertTrue(HomeViewport.dockTopPadding(480, 400, 0, 0, 100, 1f) >= 100);
    }
}
