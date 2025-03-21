package org.valr.util.enums;

import java.util.Arrays;

public class Common {
    public static <T extends Enum<T>> T fromBody(Class<T> enumClass, String value) {
        if (value == null) return null;
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
