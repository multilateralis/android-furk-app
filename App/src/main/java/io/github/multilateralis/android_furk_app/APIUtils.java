package io.github.multilateralis.android_furk_app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class APIUtils {

    public static String formatSize(String strSize)
    {
        try {
            long size = Long.parseLong(strSize);
            String sizePref = "B";
            if (size >= 1073741824) {
                size = size / 1073741824;
                sizePref = "GB";
            } else if (size >= 1048576) {
                size = size / 1048576;
                sizePref = "MB";
            } else if (size >= 1024) {
                size = size / 1024;
                sizePref = "KB";
            }

            return "Size: "+size +" "+ sizePref;
        }
        catch (NumberFormatException e)
        {
            return "";
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
                sdfDestination = new SimpleDateFormat("dd MMM");
            else
                sdfDestination = new SimpleDateFormat("dd/MM/yyyy");

            return "Added on: "+sdfDestination.format(date);

        } catch (ParseException pe) {
            return "";
        }

    }

    public static String formatBitRate(String bitRate)
    {
        return "Bitrate: "+ bitRate + " KB/s";
    }

}
