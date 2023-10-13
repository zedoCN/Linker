package top.zedo.util;

import java.text.DecimalFormat;

public class ByteFormatter {

    public static String formatPackets(long packets) {
        if (packets >= 1000000000) {
            return String.format("%.2f", packets / 1000000000.0) + "B";
        } else if (packets >= 1000000) {
            return String.format("%.2f", packets / 1000000.0) + "M";
        } else if (packets >= 1000) {
            return String.format("%.2f", packets / 1000.0) + "K";
        } else {
            return String.valueOf(packets);
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return formatDecimal((double) bytes / 1024) + "KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return formatDecimal((double) bytes / (1024 * 1024)) + "MB";
        } else {
            return formatDecimal((double) bytes / (1024 * 1024 * 1024)) + "GB";
        }
    }




    private static String formatDecimal(double value) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(value);
    }
}
