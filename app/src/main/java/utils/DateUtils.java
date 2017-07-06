package utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/12/13 0013.
 */

public class DateUtils {
//    private static boolean DEBUG = AppConfig.DEBUG;
        private static boolean DEBUG = true;
    private static String TAG = "DateUtils";

    static {
    }



    /**
     * getLocalTimeFromUTC utcDate:2017-06-22T04:00:00Z,time_zone_id:Pacific Standard Time
     * getLocalTimeFromUTC getDisplayName:北美太平洋标准时间,id:America/Dawson,date:Thu Jun 22 19:00:00 GMT+08:00 2017
     *
     * @param utcDate
     * @param time_zone_id
     * @return
     */
    public static void getLocalTimeDateFromUTC2(String utcDate, String time_zone_id, boolean ishow) {
        SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        sourceFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsed = null; // => Date is in UTC now
        try {
            parsed = sourceFormat.parse(utcDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TimeZone timeZone = TimeZone.getTimeZone("Indian/Mauritius");

        //再指定的time_zone
        for (String tzId : TimeZone.getAvailableIDs()) {
            TimeZone tz = TimeZone.getTimeZone(tzId);
            if (tz.getDisplayName(Locale.ENGLISH).equals(time_zone_id)) {
                if (DEBUG && ishow) {
                    Log.e(TAG, "==:tzId:" + tzId + ",time_zone_id:" + time_zone_id + ",offeset:" + tz.getRawOffset());
                }
                timeZone = tz;
                break;
            }
        }
        SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        destFormat.setTimeZone(timeZone);

        String result = destFormat.format(parsed);
        if (DEBUG && ishow) {
            Log.e(TAG, "result:" +result);
        }

    }
    public static Date getLocalTimeDateFromUTC(String utcDate, String time_zone_id, boolean ishow) {
        try {
            if (DEBUG && ishow) {
                Log.e(TAG, "getLocalTimeFromUTC utcDate:" + utcDate + ",time_zone_id:" + time_zone_id);
            }
            //先一般的utc
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            sdf.setTimeZone(timeZone);
            Date date = sdf.parse(utcDate);

            if (DEBUG && ishow) {
                Log.e(TAG, ",date" + date);
            }
            //再指定的time_zone
            for (String tzId : TimeZone.getAvailableIDs()) {
                TimeZone tz = TimeZone.getTimeZone(tzId);
                if (tz.getDisplayName(Locale.ENGLISH).equals(time_zone_id)) {
                    if (DEBUG && ishow) {
                        Log.e(TAG, "==:tzId:" + tzId + ",time_zone_id:" + time_zone_id + ",offeset:" + tz.getRawOffset());
                    }
                    timeZone = tz;
                    break;
                }
            }
            //获取偏移量
            long newTime = date.getTime() + timeZone.getRawOffset();
            Date date1 = new Date(newTime);

            //减去第一次设置的utc偏移量
            date1.setHours(date1.getHours() - 8);
            if (DEBUG && ishow) {
                Log.e(TAG, "date:" + date + ",date1:" + date1 );
            }
            return date1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
