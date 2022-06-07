package pl.andrzejo.aspm.utils;

import java.text.MessageFormat;

public class StringUtils {
    public static String format(String fmt, Object... args) {
        // Single quote is used to escape curly bracket arguments.

        // - Prevents strings fixed at translation time to be fixed again
        fmt = fmt.replace("''", "'");
        // - Replace ' with the escaped version ''
        fmt = fmt.replace("'", "''");

        return MessageFormat.format(fmt, args);
    }
}
