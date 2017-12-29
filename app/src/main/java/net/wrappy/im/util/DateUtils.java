package net.wrappy.im.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by CuongDuong on 12/29/2017.
 */

public class DateUtils {
    
    private DateUtils(){}

    public static boolean checkCurrentDay(Date date) {
        Date currentDay = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        boolean isCurrentDay = sdf.format(date).equals(sdf.format(currentDay));

        return isCurrentDay;
    }

    public static boolean checkCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return week == targetWeek && year == targetYear;
    }

    public static String convertHourMinuteFormat(Date date) {
        SimpleDateFormat hourMinuteFormat = new SimpleDateFormat("HH:mm");
        String hm = hourMinuteFormat.format(date);

        return hm;
    }

    public static String convertTodayFormat(Date date) {
        SimpleDateFormat toDayFormat = new SimpleDateFormat("EEE");
        String today = toDayFormat.format(date);

        return today;
    }

    public static String convertMonthDayFormat(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM dd");
        String monthDay = monthFormat.format(date);

        return monthDay;
    }

}
