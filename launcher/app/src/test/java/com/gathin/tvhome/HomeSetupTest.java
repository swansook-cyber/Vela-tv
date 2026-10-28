package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HomeSetupTest {
    @Test public void packageManagerConfirmsActualHomeRouting() {
        assertEquals(HomeSetup.Status.SET, HomeSetup.classify(false, true, false, true));
        assertEquals(HomeSetup.Status.SET, HomeSetup.classify(true, true, true, true));
    }

    @Test public void roleGrantWithoutHomeRoutingIsBlocked() {
        assertEquals(HomeSetup.Status.BLOCKED, HomeSetup.classify(true, false, true, true));
    }

    @Test public void deniedRoleDoesNotReportSuccess() {
        assertEquals(HomeSetup.Status.NOT_SET, HomeSetup.classify(false, false, true, true));
        assertEquals(HomeSetup.Status.NOT_SET, HomeSetup.classify(false, false, false, true));
    }

    @Test public void missingSystemRoutesAreBlocked() {
        assertEquals(HomeSetup.Status.BLOCKED, HomeSetup.classify(false, false, false, false));
    }

    @Test public void roleTakesPriorityAndSettingsAreFallback() {
        assertEquals(HomeSetup.Route.ROLE, HomeSetup.route(true, true));
        assertEquals(HomeSetup.Route.SYSTEM_SETTINGS, HomeSetup.route(false, true));
        assertEquals(HomeSetup.Route.NONE, HomeSetup.route(false, false));
    }
}
