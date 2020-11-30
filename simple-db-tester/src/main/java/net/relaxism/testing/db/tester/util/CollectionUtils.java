package net.relaxism.testing.db.tester.util;

import java.util.Collection;

public class CollectionUtils {

    public static boolean isEmpty(final Collection<?> value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNotEmpty(final Collection<?> value) {
        return !isEmpty(value);
    }

    public static <T> Collection<T> defaultValue(final Collection<T> value, final Collection<T> valueIfEmpty) {
        return isEmpty(value) ? valueIfEmpty : value;
    }

}
