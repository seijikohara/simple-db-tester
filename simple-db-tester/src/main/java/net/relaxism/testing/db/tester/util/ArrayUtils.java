package net.relaxism.testing.db.tester.util;

public class ArrayUtils {

    public static <T> boolean isEmpty(final T[] value) {
        return value == null || value.length < 1;
    }

    public static <T> boolean isNotEmpty(final T[] value) {
        return !isEmpty(value);
    }

    public static <T> T[] defaultValue(final T[] value, final T[] valueIfEmpty) {
        return isEmpty(value) ? valueIfEmpty : value;
    }

}
