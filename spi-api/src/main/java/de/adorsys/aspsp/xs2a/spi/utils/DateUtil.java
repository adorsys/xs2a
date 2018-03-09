package de.adorsys.aspsp.xs2a.spi.utils;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static Date getDateWithTimeZone(Date date) {
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateTimeFormat.parse(getStringFromDateTime(date));
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getStringFromDate(Date date) {
        if (date != null) {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormatter.format(date);
        }
        return null;
    }

    public static Date getDateFromDateString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date getDateFromDateStringNoTimeZone(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getStringFromDateTime(Date date) {
        if (date != null) {
            DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateTimeFormatter.format(date);
        }
        return null;
    }

    public static Date getDateFromDateTimeString(String dateTimeString) {
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateTimeFormat.parse(dateTimeString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        return calendar.getTime();
    }

    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        return calendar.getTime();
    }

    public static Date addMonth(Date dateFrom, int months){
        LocalDateTime localDateTimeFrom = LocalDateTime.ofInstant(dateFrom.toInstant(), ZoneId.systemDefault());
        LocalDateTime localDateTimeTo = localDateTimeFrom.plusMonths(months);
        return Date.from(localDateTimeTo.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date addDay(Date dateFrom, int days){
        LocalDateTime localDateTimeFrom = LocalDateTime.ofInstant(dateFrom.toInstant(), ZoneId.systemDefault());
        LocalDateTime localDateTimeTo = localDateTimeFrom.plusDays(days);
        return Date.from(localDateTimeTo.atZone(ZoneId.systemDefault()).toInstant());
    }
}
