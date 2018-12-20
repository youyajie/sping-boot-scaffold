package com.yyj.springbootscaffold.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by yyj on 2018/12/20.
 */
public class TimeUtils {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Long getTimeStamp(String time) {
        LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
        Instant instant = LocalDateTime.from(dateTime).atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli() / 1000;
    }
}
