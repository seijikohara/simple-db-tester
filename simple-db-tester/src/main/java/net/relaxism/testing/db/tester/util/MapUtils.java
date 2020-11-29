package net.relaxism.testing.db.tester.util;

import java.util.Map;

public class MapUtils {

    public static boolean isEmpty(final Map<?, ?> value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNotEmpty(final Map<?, ?> value) {
        return !isEmpty(value);
    }

    public static <K, V> Map<K, V> defaultValue(final Map<K, V> value, final Map<K, V> valueIfEmpty) {
        return isEmpty(value) ? valueIfEmpty : value;
    }

}
