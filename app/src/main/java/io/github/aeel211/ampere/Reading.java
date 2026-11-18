package io.github.aeel211.ampere;

import android.os.BatteryManager;

/**
 * One snapshot of the battery current sensor.
 *
 * <p>The Android platform reports net current in microamperes. Positive values
 * mean current is flowing into the battery, negative values mean it is flowing
 * out. Consequently {@link #inUa()} and {@link #outUa()} can never both be
 * non-zero at the same instant, and {@code in - out == net}.</p>
 */
public record Reading(long netUa, int level, int status, int plugged, boolean supported) {

    public long inUa() {
        return supported ? Math.max(0L, netUa) : 0L;
    }

    public long outUa() {
        return supported ? Math.max(0L, -netUa) : 0L;
    }

    public boolean full() {
        return status == BatteryManager.BATTERY_STATUS_FULL;
    }
}
