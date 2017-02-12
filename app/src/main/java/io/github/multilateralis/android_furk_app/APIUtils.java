package io.github.multilateralis.android_furk_app;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class APIUtils {

    public static String formatSize(String strSize)
    {
        try {
            long size = Long.parseLong(strSize);
            if (size >= 1073741824) {
                DecimalFormat df = new DecimalFormat("#.#");
                return df.format(size / 1073741824.0) + "GB";
            } else if (size >= 1048576) {
                return Long.toString(size / 1048576) + " MB";
            } else if (size >= 1024) {
                return Long.toString(size / 1024) + " KB";
            }
            else {
                return Long.toString(size) + " B";
            }
        }
        catch (NumberFormatException e)
        {
            return "N/A";
        }
    }
    public static String formatDate(String strDate)
    {
        try {
            // create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // parse the string into Date object
            Date date = sdfSource.parse(strDate);
            Date now = new Date();

            SimpleDateFormat sdfDestination;
            if(date.getYear() == now.getYear())
                sdfDestination = new SimpleDateFormat("MMM dd");
            else
                sdfDestination = new SimpleDateFormat("yyyy-MM-dd");

            return sdfDestination.format(date);

        } catch (ParseException pe) {
            return "N/A";
        }

    }

    public static String formatBitRate(String bitRate)
    {
        return bitRate + " KB/s";
    }

}
