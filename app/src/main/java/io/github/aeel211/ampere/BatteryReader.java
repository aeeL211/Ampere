package io.github.aeel211.ampere;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Reads the battery state without permissions, sensors, or third-party code.
 */
final class BatteryReader {

    /** True once a raw reading proved that the sensor reports microamperes. */
    private static volatile boolean nativeMicroAmps;

    private BatteryReader() {
    }

    static Reading read(Context context) {
        int level = -1;
        int status = BatteryManager.BATTERY_STATUS_UNKNOWN;
        int plugged = 0;

        Intent battery = context.registerReceiver(
                null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery != null) {
            int rawLevel = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            if (rawLevel >= 0 && scale > 0) {
                level = Math.round(rawLevel * 100f / scale);
            }
            status = battery.getIntExtra(
                    BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
            plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        }

        int raw = Integer.MIN_VALUE;
        BatteryManager manager = context.getSystemService(BatteryManager.class);
        if (manager != null) {
            try {
                raw = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            } catch (RuntimeException ignored) {
                // Some vendor implementations throw instead of returning MIN_VALUE.
            }
        }
        if (raw == Integer.MIN_VALUE) {
            return new Reading(0L, level, status, plugged, false);
        }

        long microAmps = raw;

        // The platform contract is microamperes, but a few OEMs expose milliamps.
        // 30 A is beyond any phone, so such a reading proves microamperes; remember
        // that so a later small reading is not inflated 1,000x.
        if (Math.abs(microAmps) >= 30_000L) {
            nativeMicroAmps = true;
        } else if (Math.abs(microAmps) < 10_000L
                && !nativeMicroAmps && !mayBeSmallCurrent(status)) {
            microAmps *= 1000L;
        }

        microAmps = normalizeSign(microAmps, status, plugged);

        return new Reading(microAmps, level, status, plugged, true);
    }

    /**
     * A full or paused charge has a genuinely tiny net current. Trust the
     * documented microamp unit there instead of turning 8 mA into 8 A.
     */
    private static boolean mayBeSmallCurrent(int status) {
        return status == BatteryManager.BATTERY_STATUS_FULL
                || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING;
    }

    /**
     * Some vendor kernels invert the documented sign. The charging state is
     * the source of truth: current cannot enter an unplugged, discharging, or
     * explicitly not-charging battery.
     */
    private static long normalizeSign(long microAmps, int status, int plugged) {
        if (plugged == 0
                || status == BatteryManager.BATTERY_STATUS_DISCHARGING
                || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
            return -Math.abs(microAmps);
        }
        if (status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL) {
            return Math.abs(microAmps);
        }
        return microAmps;
    }
}
