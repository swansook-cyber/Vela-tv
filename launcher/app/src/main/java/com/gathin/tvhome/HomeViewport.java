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
}
