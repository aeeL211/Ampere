package io.github.aeel211.ampere;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Reads the battery state without permissions, sensors, or third-party code.
 */
final class BatteryReader {

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
        // Screen-on currents are well above 10 mA, so small values are milliamps.
        if (Math.abs(microAmps) < 10_000L) {
            microAmps *= 1000L;
        }

        // Normalize vendor sign conventions. Current cannot enter the battery
        // while unplugged, or leave it while the platform says it is discharging.
        if (microAmps > 0L
                && (plugged == 0 || status == BatteryManager.BATTERY_STATUS_DISCHARGING)) {
            microAmps = -microAmps;
        }

        return new Reading(microAmps, level, status, plugged, true);
    }
}
