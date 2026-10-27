package com.gathin.tvhome;

/** Pure viewport arithmetic shared by layout and unit tests. */
final class HomeViewport {
    private HomeViewport() { }

    static int dockTopPadding(int height, int dockRowHeight, int visibleRowsHeight,
                              int rowsBelowDock, int shelfClearance, float density) {
        // With no requested row below the dock, the next row must begin beyond the viewport.
        // The old 30dp bottom room exposed the top of that row on TCL displays.
        int bottomRoom = rowsBelowDock == 0 ? 0 : Math.round(AppAdapter.BOTTOM_ROOM_DP * density);
        return Math.max(shelfClearance, height - dockRowHeight - visibleRowsHeight - bottomRoom);
    }

    static int scrollDyForChild(int top, int bottom, int viewportHeight, int paddedBottom,
                                boolean snapGridRow) {
        if (top < 0) return top;
        if (snapGridRow && top > 0) return top;
        if (bottom <= viewportHeight) return 0;
        // A focused row entering from below should replace the dock in one motion. A minimal
        // scroll leaves the old dock as a clipped strip at the top of short TV viewports.
        return Math.min(top, bottom - paddedBottom);
    }
}
