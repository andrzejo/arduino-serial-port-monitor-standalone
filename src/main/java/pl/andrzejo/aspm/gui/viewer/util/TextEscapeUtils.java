/*
 * Arduino Serial Port Monitor - Standalone (https://github.com/andrzejo/arduino-serial-port-monitor-standalone)
 * This is free software (GPL v.2).
 *
 * Copyright (c) Andrzej Oczkowicz 2026.
 */

package pl.andrzejo.aspm.gui.viewer.util;

public class TextEscapeUtils {
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public static String escapeText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        int len = text.length();
        StringBuilder sb = new StringBuilder(len + 16);

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if ((c >= 32 && c < 127) || c == '\n' || c == '\r' || c == '\t') {
                sb.append(c);
            } else {
                int val = c & 0xFFFF;
                sb.append('<')
                        .append(HEX_CHARS[(val >>> 4) & 0x0F])
                        .append(HEX_CHARS[val & 0x0F])
                        .append('>');
            }
        }
        return sb.toString();
    }
}
