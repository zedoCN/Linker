package top.zedo.util;

import java.text.DecimalFormat;

public class ByteFormatter {
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
