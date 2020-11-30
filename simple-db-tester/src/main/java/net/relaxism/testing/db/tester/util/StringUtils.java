package net.relaxism.testing.db.tester.util;

public class StringUtils {

    public static boolean isEmpty(final String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNotEmpty(final String value) {
        return !isEmpty(value);
    }

    public static String defaultValue(final String value, final String valueIfEmpty) {
        return isEmpty(value) ? valueIfEmpty : value;
    }

    public static String trim(final String value) {
        return isEmpty(value) ? value : value.trim();
    }

}
