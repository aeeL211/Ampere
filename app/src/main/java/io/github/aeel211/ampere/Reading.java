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
public final class Reading {

    private final long netUa;
    private final int level;
    private final int status;
    private final int plugged;
    private final boolean supported;

    public Reading(long netUa, int level, int status, int plugged, boolean supported) {
        this.netUa = netUa;
        this.level = level;
        this.status = status;
        this.plugged = plugged;
        this.supported = supported;
    }

    public long netUa() {
        return netUa;
    }

    public int level() {
        return level;
    }

    public int status() {
        return status;
    }

    public int plugged() {
        return plugged;
    }

    public boolean supported() {
        return supported;
    }

    public long inUa() {
        return supported ? Math.max(0L, netUa) : 0L;
    }

    public long outUa() {
        return supported ? Math.max(0L, -netUa) : 0L;
    }

    public boolean full() {
        return status == BatteryManager.BATTERY_STATUS_FULL;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Reading)) {
            return false;
        }
        Reading that = (Reading) other;
        return netUa == that.netUa
                && level == that.level
                && status == that.status
                && plugged == that.plugged
                && supported == that.supported;
    }

    @Override
    public int hashCode() {
        int result = (int) (netUa ^ (netUa >>> 32));
        result = 31 * result + level;
        result = 31 * result + status;
        result = 31 * result + plugged;
        result = 31 * result + (supported ? 1 : 0);
        return result;
    }
}
