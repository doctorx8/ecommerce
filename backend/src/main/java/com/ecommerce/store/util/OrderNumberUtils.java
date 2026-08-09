package com.ecommerce.store.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNumberUtils {
    private OrderNumberUtils() {}

    public static String generate() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int rand = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "ORD-" + date + "-" + rand;
    }
}
